package com.fundaccess.com.fundaccess.controller;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fundaccess.com.fundaccess.model.ExchangeRate;
import com.fundaccess.com.fundaccess.service.ExchangeRateRepository;
import com.fundaccess.com.fundaccess.serviceimpl.Currency;
import com.fundaccess.com.fundaccess.serviceimpl.ExchangeRateAccess;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

@RestController
@RequestMapping("/api/fundaccess")

public class controller {

	@Autowired
	private ExchangeRateAccess exchangeRateAccess;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<List<ExchangeRate>> listAllRates() throws IOException {
		List<ExchangeRate> rates = exchangeRateAccess.getAllRates();
		if (rates == null || rates.isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<ExchangeRate>>(rates, HttpStatus.OK);
	}

	@RequestMapping(value = "/import", method = RequestMethod.GET)
	public String importInto() throws IOException, ParserConfigurationException, SAXException, ParseException {
		exchangeRateAccess.insertCsv();
		return "Importing to database Completed";
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public String downloadFiles() throws IOException, ParserConfigurationException, SAXException, ParseException {
		exchangeRateAccess.downloadFile();
		return "Downloading completed";
	}

	@RequestMapping(value = "/findByDate", method = RequestMethod.GET)
	public ResponseEntity<List<ExchangeRate>> getAllByDate(
			@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
		List<ExchangeRate> rates = exchangeRateAccess.getAllRatesByDate(date);
		if (rates == null || rates.isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<List<ExchangeRate>>(rates, HttpStatus.OK);
	}

	@RequestMapping(value = "/findByCurrency", method = RequestMethod.GET)
	public ResponseEntity<List<ExchangeRate>> getAllByCurrency(@RequestParam("cur") String currency) {
		List<ExchangeRate> rates = exchangeRateAccess.getAllRatesByCurrency(currency);
		if (rates == null || rates.isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<ExchangeRate>>(rates, HttpStatus.OK);
	}

	@RequestMapping(value = "/convert", method = RequestMethod.GET)
	public ResponseEntity<Double> convertByDateAndCurrency(
			@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
			@RequestParam("fromCurrency") String fromCurrency, @RequestParam("toCurrency") String toCurrency,
			@RequestParam("amount") Double amount) {
		Double value = exchangeRateAccess.convert(date, fromCurrency, toCurrency, amount);
		if (value == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Double>(value, HttpStatus.OK);
	}

	@RequestMapping(value = "/convertToEuro", method = RequestMethod.GET)
	public ResponseEntity<Double> convertByDateAndCurrency(
			@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
			@RequestParam("fromCurrency") String fromCurrency, @RequestParam("amount") Double amount) {
		Double value = exchangeRateAccess.convertToEuro(date, fromCurrency, amount);
		if (value == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Double>(value, HttpStatus.OK);
	}

	@PostMapping("/addDetails")
	private long saveExchangeRate(@RequestBody ExchangeRate exchangeRate) {
		exchangeRateAccess.saveOrUpdate(exchangeRate);
		return exchangeRate.getId();
	}
}
