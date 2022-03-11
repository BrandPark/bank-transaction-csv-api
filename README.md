# 구현 요구사항
- 사용자는 대용량의 거래내역이 담긴 csv파일을 저장할 수 있습니다.
- 사용자는 저장된 거래내역을 사용자별로 조회할 수 있습니다.
    - `거래날짜(yyyy-MM-dd)`와 `거래타입`으로 거래내역을 조회할 수 있습니다.
    - 조건들은 nullable이며 모두 null일 경우 모든 거래내역을 조회합니다.
- 사용자는 저장된 거래내역을 은행별로 조회할 수 있습니다.
    - `거래날짜(yyyy-MM-dd)`와 `거래타입`, `은행코드`로 거래내역을 조회할 수 있습니다.
    - 조건들은 nullable이며 모두 null일 경우 모든 거래내역을 조회합니다.
- 대용량 데이터를 처리할 수 있도록 설계합니다.
 
# CSV 파일 컬럼 형식
    
|id|year|month|day|user_id|bank_code|transaction_amount|transaction_type|
|---|---|---|---|---|---|---|---|
|거래ID|거래 연도|거래 월|거래 일|사용자ID|은행코드|거래액|거래타입|

- 은행 코드
    - 004(국민은행)
    - 011(농협은행)
    - 020(우리은행)
    - 088(신한은행)
    - 090(카카오뱅크)

- 거래타입
    - WITHDRAW(출금)
    - DEPOSIT(입금)

# 사용 기술
- `Java`
- `Spring Boot`
- `Spring Data Jpa`
- `H2`

# 로컬 환경 구동 가이드

1. 프로젝트를 클론합니다.
2. IDE로 프로젝트를 실행하면 http://localhost:8080 으로 서버가 구동됩니다.
3. 데이터베이스는 Embedded H2를 사용하였으며 홈 디렉토리에 db파일이 생성됩니다.

# ERD

![image](https://user-images.githubusercontent.com/53790137/155931606-3756593e-fee7-4290-a559-08762d4d6c1c.png)

- `Index`는 우선 카디널리티가 높은 **TRANSACTION_DATE**만 설정했습니다.
- `TRANSACTION_DATE`는 csv파일에 입력된 YEAR과 MONTH, DAY가 유효한지 확인할 수 있고, 추후에 날짜 범위 조회가 꼭 필요할 것 같아서 추가했습니다. 

# 문제 해결 전략

제가 이해한 구현 요구사항은 데이터 파일(.csv)을 통해 거래내역을 저장하고 조회할 수 있는 API 구현입니다. 

핵심 문제는 **대용량 데이터를 저장**하는 방식과 **조회**하는 방식이라고 생각하고 문제들을 해결했습니다.

### 1. 대용량 데이터 저장

저는 객체지향적인 코딩과 기본적인 CRUD 메서드를 지원받기 위해 `JPA(Hibernate)`를 사용했습니다. 그에 따라 다음과 같은 부가적인 문제가 발생했습니다.

> 1.`JpaRepository.save` 메서드를 사용할 경우 id가 이미 있기 때문에 준영속 상태로 인지하여 <ins>**엔티티 하나 하나에 Select가 발생**</ins>

내부적으로 `merge()`가 호출되어 준영속상태로 판단한다는 것을 파악하고 직접 `EntityManager.psersist`를 호출하여 해결했습니다.

결과적으로 2배 이상의 성능차이를 보였습니다.

> 2.대용량 데이터 엔티티를 모았다가 한 번에 flush 하는 것은 <ins>**서버의 메모리 부족 문제**</ins>가 발생할 수 있다.

제가 생각한 해결전략은 다음과 같습니다.
1. 버퍼를 구현하여 일정량 이상의 엔티티가 모이면 바로 flush 하여 메모리를 반환시킨다.
2. `hibernate.jdbc.batch_size` 옵션을 사용하여 DB에 batch insert 문이 전송되도록 하여 성능을 향상시킨다. 

핵심 코드는 아래와 같습니다.
```java
try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

    String line = null;
    long rowNum = 0;

    while ((line = br.readLine()) != null) {
        rowNum++;

        String[] row = line.split(",");

        BankTransaction transactionEntity = csvRowConvertToBankTransaction(row, rowNum);

        batchInsertBuff.add(transactionEntity);

        if(++count % batchSize == 0) {
        totalInsertedRow += batchInsertBuff.size();

        flushBuff(batchInsertBuff);
    }
}

if(!batchInsertBuff.isEmpty()) {
    totalInsertedRow += batchInsertBuff.size();

    flushBuff(batchInsertBuff);
}
```

### 2. 대용량 데이터 조회 

대용량 데이터를 조회할 때 제가 주목한 문제는 다음과 같습니다.

> 1.결과 데이터가 너무 많아 서버의 메모리 부족 현상이 발생할 수 있다.

저는 Paging을 통해 너무 많은 데이터가 서버에 로드되지 않도록 했습니다.

```java
@GetMapping("/bank-transactions/by-user")
    public PageResult<BankTransactionResponse> getAllTransactionListByUser(
            @RequestParam(value = "transaction_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate transactionDate
            , @RequestParam(value = "transaction_type", required = false) TransactionType transactionType
            , Pageable pageable) {
```

> 2.DB에서 조회하는 시간이 오래걸릴 수 있다.

저는 `TRNASACTION_DATE`에 index를 설정하여 최소한의 조회 성능 향상을 꾀했습니다.

`거래일자`는 다른 조회 조건인 `은행코드`, `거래타입`에 비해 카디널리티가 높기 때문에 설정하였습니다.

물론 `거래ID` 순서와 `거래일자`의 순서가 일치한다면 의미가 없겠지만 아닐 수도 있다는 가정하고 선택하였습니다.

후에 성능 문제가 되는 조회는 `커버링 인덱스`를 사용하여 성능 향상을 꾀할 수 있을 것 같습니다.

### 3. 은행코드, 거래타입 유지보수성 높이기

은행코드와 거래타입을 String으로 코딩하기에는 휴먼 에러가 발생할 확률이 높습니다. 

`Enum`을 생성하여 실수 확률을 줄이고 유지보수성을 높였습니다.

그로인해 부가적으로 다음과 같은 문제가 발생하였습니다. 

> 1.은행코드는 DB에 '004'와 같이 코드로 저장되어야 하고 로드될 때는 매핑되어 Enum으로 Deserialize 되어야 한다.

`AttributeConverter`를 구현함으로써 해결했습니다. (구현체 : BankCodePersistConverter)

> 2.은행별 조회 API에서 은행코드 '004'로 조회하면 Enum으로 받을 수가 없다.

타입체크로 유효성을 체크하기 위해 은행코드 Param을 Enum으로 받도록 하고 싶었습니다.

저는 `org.springframework.core.convert.converter.Converter`를 구현함으로써 해결했습니다.

### 4. csv 데이터, 조회 조건 유효성 체크

csv 파일의 데이터 컬럼들은 타입변환이 이루어지고 엔티티로 생성되어 저장됩니다. 이 과정에서 타입변환이 실패하면 예외를 발생하고 핸들링 하도록 했습니다.

조회 조건인 `거래일자`, `거래타입`, `은행코드` 모두 ReferenceType을 사용하여 받기 때문에 타입 체크로 빠르게 유효성을 체크할 수 있습니다.

### 5. 거래내역 응답 JSON 형식

저는 이 API가 관리자가 사용하는 API라고 가정했습니다.

주 기능은 <ins>**두 조회 API 모두 거래내역 조회**</ins>이기 때문에 응답 형식은 같습니다.

단, <ins>**유저별 조회 API**</ins>는 `거래일자`와 `유저ID` 오름차순으로 정렬했고,
<ins>**은행별 조회 API**</ins>는 `거래일자`와 `은행코드` 오름차순으로 정렬하여 반환합니다.

응답 데이터를 통해 추가적인 작업을 할 수 있도록 필요할 것 같은 데이터를 모두 넣었습니다.

```json
// ex
{
  "bankTransactionId": 63,
  "userId": 1,
  "bankCode": "004",
  "transactionType": "DEPOSIT",
  "transactionDate": "2021-01-04",
  "transactionAmount": 142000
}
```

# API Docs

## 입출금 거래 데이터 저장

**persistTransactionListUsingCsv**
---

Csv 파일로 DB에 거래 데이터를 저장합니다.

* **URL**

  `/api/v1/bank-transactions/persist-csv`

* **Method:**

  `POST`

* **Body**
    * Content-Type : multipart/form-data
    * Required
    * Key name : file

```
    "file" : transaction.csv
```

* **Success Response:** 저장된 거래내역의 수를 반환합니다.
    * **Code :** 200 OK
    * **Content :** </br>

    ```
        7329
    ```

* **Error Response:**

    * **Code :** 400 <br/>
    * **Content-Type :** application/json
    * **Case :**
      * csv 파일이 아닌 경우
      * 컬럼 중 타입이 올바르지 않은 것이 있는 경우
      * 컬럼 수가 8개가 아닌 경우
    * **Content :** </br>
    ```json
        
        {
            "status": "BAD_REQUEST",
            "timestamp": "2022-02-28 20:02:10",
            "message": "지원되지 않는 파일형식입니다. csv 파일만 가능합니다.",
            "debugMessage": "지원되지 않는 파일형식입니다. csv 파일만 가능합니다."
        }
    ```
---

## 유저별 거래 내역 조회

**GetAllTransactionListByUser**
---
거래 일자와 거래 타입을 입력받아서, 특정 일자에 발생한 특정 거래 타입의 내역을 가져옵니다.

`거래일자`와 `유저ID`를 기준으로 **오름차순 정렬**되어 반환됩니다.

* **URL**

  `/api/v1/bank-transactions/by-user`

* **Method:**

  `GET`

* **URL Params**

   - `transaction_date` (optional) : 거래일자입니다. (yyyy-MM-dd 형식) 
   - `transaction_type` (optional) : 거래타입입니다.
   - `page` (optional) : 페이지 번호입니다. 0번부터 시작하며 기본 값은 0입니다.
   - `size` (optional) : 페이지에 포함할 최대 거래내역의 수입니다. 기본 값은 20입니다.
* **Success Response:**

    * **Code :** 200
    * **Content :** </br>
  
    ```json
    {
        "pageNumber": 0,  /*페이지 번호*/
        "pageSize": 2,  /*페이지 크기*/
        "offset": 0,  /*조회 시작 offset*/
        "totalPages": 5,  /*조회한 결과의 총 페이지 수*/  
        "totalElements": 9, /*조회한 결과의 총 거래내역 수*/
        "contentsSize": 2,  /*조회 결과 페이지의 거래내역 수*/
        "contents": [ /*거래내역 들*/
            {
                "bankTransactionId": 77,
                "userId": 3,
                "bankCode": "011",
                "transactionType": "WITHDRAW",
                "transactionDate": "2021-01-04",
                "transactionAmount": 934000
            },
            {
                "bankTransactionId": 76,
                "userId": 3,
                "bankCode": "004",
                "transactionType": "WITHDRAW",
                "transactionDate": "2021-01-04",
                "transactionAmount": 936000
            }
        ]
    }
    ```
* **Error Response:**

    * **Code :** 400 <br/>
    * **Content-Type :** application/json
    * **Case :**
        * 파라미터의 값이 유효하지 않은 경우
    * **Content :** </br> 
  
    ```json
    {
        "status": "BAD_REQUEST",
        "timestamp": "2022-02-28 20:17:18",
        "message": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat java.time.LocalDate] for value '2021-01-1'; nested exception is java.lang.IllegalArgumentException: Parse attempt failed for value [2021-01-1]",
        "debugMessage": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat java.time.LocalDate] for value '2021-01-1'; nested exception is java.lang.IllegalArgumentException: Parse attempt failed for value [2021-01-1]"
    }
    ```
---

## 은행별 거래 내역 조회

**GetAllTransactionListByBank**
---
거래 일자, 은행 코드 그리고 거래 타입을 입력 받아서, 특정 일자에 발생한 특정 은행의 거래 내역을 가져옵니다.

`거래일자`와 `은행코드`를 기준으로 **오름차순 정렬**되어 반환됩니다.

* **URL**

  `/api/v1/bank-transactions/by-bank`

* **Method:**

  `GET`

* **URL Params**

    - `transaction_date` (optional) : 거래일자입니다. (yyyy-MM-dd 형식)
    - `transaction_type` (optional) : 거래타입입니다.
    - `bank_code` (optional) : 은행코드입니다.
    - `page` (optional) : 페이지 번호입니다. 0번부터 시작하며 기본 값은 0입니다.
    - `size` (optional) : 페이지에 포함할 최대 거래내역의 수입니다. 기본 값은 20입니다.
* **Success Response:**

    * **Code :** 200
    * **Content :** </br>

    ```json
    {
        "pageNumber": 0,  /*페이지 번호*/
        "pageSize": 2,  /*페이지 크기*/
        "offset": 0,  /*조회 시작 offset*/
        "totalPages": 1,  /*조회한 결과의 총 페이지 수*/  
        "totalElements": 2, /*조회한 결과의 총 거래내역 수*/
        "contentsSize": 2,  /*조회 결과 페이지의 거래내역 수*/
        "contents": [ /*거래내역 들*/
            {
                "bankTransactionId": 64,
                "userId": 8,
                "bankCode": "090",
                "transactionType": "WITHDRAW",
                "transactionDate": "2021-01-04",
                "transactionAmount": 562000
            },
            {
                "bankTransactionId": 60,
                "userId": 21,
                "bankCode": "090",
                "transactionType": "WITHDRAW",
                "transactionDate": "2021-01-04",
                "transactionAmount": 64000
            }
        ]
    }
    ```
* **Error Response:**

    * **Code :** 400 <br/>
    * **Content-Type :** application/json
    * **Case :**
        * 파라미터의 값이 유효하지 않은 경우
    * **Content :** </br>

    ```json
    {
        "status": "BAD_REQUEST",
        "timestamp": "2022-02-28 20:17:18",
        "message": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat java.time.LocalDate] for value '2021-01-1'; nested exception is java.lang.IllegalArgumentException: Parse attempt failed for value [2021-01-1]",
        "debugMessage": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat java.time.LocalDate] for value '2021-01-1'; nested exception is java.lang.IllegalArgumentException: Parse attempt failed for value [2021-01-1]"
    }
    ```
---
