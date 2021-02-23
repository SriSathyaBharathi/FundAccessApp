package com.fundaccess.com.fundaccess.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "currency", "date" }))
@Entity
public class ExchangeRate implements Serializable {

	private static final long serialVersionUID = 2432078397208040228L;

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@Column(nullable = false)
	private String currency;

	@Column(nullable = false)
	private Double rate;

	@Column(nullable = false)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date date;

	protected ExchangeRate() {
		// required by JPA spec
	}

	public ExchangeRate(String currency, Double rate, Date date) {
		this.currency = currency;
		this.rate = rate;
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "ExchangeRate [id=" + id + ", currency=" + currency + ", rate=" + rate + ", date=" + date + "]";
	}

}
