package com.fundaccess.com.fundaccess.serviceimpl;

import com.fundaccess.com.fundaccess.model.ExchangeRate;
import com.fundaccess.com.fundaccess.service.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ExchangeRateAccess {

	private static final int BUFFER_SIZE = 4096;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ExchangeRateRepository exchangeRateRepository;

	public List<ExchangeRate> getAllRates() {
		return exchangeRateRepository.findAll();
	}

	public List<ExchangeRate> getAllRatesByDate(Date date) {
		return exchangeRateRepository.findAllByDate(date);
	}

	public List<ExchangeRate> getAllRatesByCurrency(String currency) {
		return exchangeRateRepository.findAllByCurrency(currency);
	}

	public void saveOrUpdate(ExchangeRate exchangeRate) {
		exchangeRateRepository.save(exchangeRate);
	}

	public void downloadFile() throws IOException {
		for (Currency b : Currency.values()) {
			StringBuffer stringBuffer = new StringBuffer();
			byte[] data = null;
			String url1 = "https://www.bundesbank.de/statistic-rmi/StatisticDownload?tsId=BBEX3.D." + b
					+ ".EUR.BB.AC.000&its_fileFormat=sdmx&mode=its";
			DecimalFormat decimalFormat = new DecimalFormat("###.###");
			String fileName = b + ".xml";
			File file = new File(fileName);
			try {
				URL url = new URL(url1);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				System.out.println("Connected :)" + b);
				InputStream inputStream = connection.getInputStream();
				long read_start = System.nanoTime();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				int i;
				while ((i = reader.read()) != -1) {
					char c = (char) i;
					if (c == '\n') {
						stringBuffer.append("\n");
					} else {
						stringBuffer.append(String.valueOf(c));
					}
				}
				reader.close();
				long read_end = System.nanoTime();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				data = stringBuffer.toString().getBytes();
			}
			try (FileOutputStream fop = new FileOutputStream(file)) {
				if (!file.exists()) {
					file.createNewFile();
				}
				long now = System.nanoTime();
				fop.write(data);
				fop.flush();
				fop.close();
				System.out.println("Finished writing CSV in " + b
						+ decimalFormat.format((System.nanoTime() - now) / Math.pow(10, 6)) + " milliseconds!");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.gc();
		}
	}

	public void insertCsv() throws IOException, SAXException, ParserConfigurationException, ParseException {
		for (Currency b : Currency.values()) {
			String fileCsvName = b + ".xml";
			File file = new File(fileCsvName);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList currencyNode = document.getElementsByTagName("bbk:Series");
			String currency = currencyNode.item(0).getAttributes().getNamedItem("UNIT").getTextContent();
			NodeList node = document.getElementsByTagName("bbk:Obs");
			for (int i = 0; i < node.getLength(); i++) {
				Node item = node.item(i);
				NamedNodeMap attributes = item.getAttributes();
				Node date = attributes.getNamedItem("TIME_PERIOD");
				if (Objects.isNull(attributes.getNamedItem("BBK_OBS_STATUS"))) {
					Node value = attributes.getNamedItem("BBK_DIFF");
					Node rate = attributes.getNamedItem("OBS_VALUE");
					Date dateParsed = new SimpleDateFormat("yyyy-MM-dd").parse(date.getTextContent());
					ExchangeRate exchangeRate = new ExchangeRate(currency, Double.valueOf(rate.getTextContent()),
							dateParsed);
					List<ExchangeRate> allByDate = exchangeRateRepository.findAllByDateAndCurrency(dateParsed,
							currency);
					if (allByDate.isEmpty()) {
						exchangeRateRepository.save(exchangeRate);
					}
				}
			}
			System.out.println("done importing" + fileCsvName);
		}
	}

	public Double convert(Date date, String fromCurrency, String toCurrency, Double amount) {
		if (date == null) {
			date = new Date();
		}
		double fromExchangeRateVal = 1f;
		double toExchangeRateVal = 1f;
		try {
			fromExchangeRateVal = getExchangeRateValue(date, fromCurrency, fromExchangeRateVal);
			toExchangeRateVal = getExchangeRateValue(date, toCurrency, toExchangeRateVal);
			return (amount * toExchangeRateVal) / fromExchangeRateVal;
		} catch (Exception e) {
			return null;
		}
	}

	public Double convertToEuro(Date date, String fromCurrency, Double amount) {
		if (date == null) {
			date = new Date();
		}
		double fromExchangeRateVal = 1f;
		try {
			fromExchangeRateVal = getExchangeRateValue(date, fromCurrency, fromExchangeRateVal);
			return (amount * fromExchangeRateVal);
		} catch (Exception e) {
			return null;
		}
	}

	private double getExchangeRateValue(Date date, String currency, double exchangeRateVal) {
		if (!"EUR".equalsIgnoreCase(currency)) {
			ExchangeRate exchangeRate = exchangeRateRepository.findByDateAndCurrency(date, currency);
			exchangeRateVal = exchangeRate.getRate();
		}
		return exchangeRateVal;
	}
}
