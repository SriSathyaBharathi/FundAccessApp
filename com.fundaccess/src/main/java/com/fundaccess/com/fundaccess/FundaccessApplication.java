package com.fundaccess.com.fundaccess;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FundaccessApplication {
	
	public static void main(String[] args) throws SQLException {
		SpringApplication.run(FundaccessApplication.class, args);
	}

}
