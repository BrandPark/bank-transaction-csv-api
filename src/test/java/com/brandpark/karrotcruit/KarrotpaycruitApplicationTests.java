package com.brandpark.karrotcruit;

import com.brandpark.karrotcruit.api.BankTransactionRepository;
import com.brandpark.karrotcruit.api.dto.BankTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KarrotpaycruitApplicationTests {

	@Autowired BankTransactionRepository bankTransactionRepository;

	@DisplayName("test")
	@Test
	public void test234() throws Exception {

	    // given
		bankTransactionRepository.save(BankTransaction.createBankTransaction("1,2021,1,1,4,004,29000,DEPOSIT".split(",")));

	    // when

	    // then
	}
}
