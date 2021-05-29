package com.animo.cowin.cloud;

import java.io.Serializable;

import com.google.gson.JsonObject;

public class CowinPubSubMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String centerName; 
	private int centerId; 
	private int pincode; 
	private String districtName; 
	private String date; 
	private int minAgeLimit; 
	private int availableCapacityDose1;
	private int availableCapacityDose2;
	private String vaccine;
	
	
	public String getCenterName() {
		return centerName;
	}
	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}
	public int getCenterId() {
		return centerId;
	}
	public void setCenterId(int centerId) {
		this.centerId = centerId;
	}
	public int getPincode() {
		return pincode;
	}
	public void setPincode(int pincode) {
		this.pincode = pincode;
	}
	public String getDistrictName() {
		return districtName;
	}
	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getMinAgeLimit() {
		return minAgeLimit;
	}
	public void setMinAgeLimit(int minAgeLimit) {
		this.minAgeLimit = minAgeLimit;
	}
	
	
	public int getAvailableCapacityDose1() {
		return availableCapacityDose1;
	}
	public void setAvailableCapacityDose1(int availableCapacityDose1) {
		this.availableCapacityDose1 = availableCapacityDose1;
	}
	public int getAvailableCapacityDose2() {
		return availableCapacityDose2;
	}
	public void setAvailableCapacityDose2(int availableCapacityDose2) {
		this.availableCapacityDose2 = availableCapacityDose2;
	}
	public String getVaccine() {
		return vaccine;
	}
	public void setVaccine(String vaccine) {
		this.vaccine = vaccine;
	}
	public String toJson() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty("centerName", getCenterName());
		jsonObject.addProperty("centerId", getCenterId());
		jsonObject.addProperty("pincode", getPincode());
		jsonObject.addProperty("districtName", getDistrictName());
		jsonObject.addProperty("date", getDate());
		jsonObject.addProperty("minAgeLimit", getMinAgeLimit());
		jsonObject.addProperty("availableCapacityDose1", getAvailableCapacityDose1());
		jsonObject.addProperty("availableCapacityDose2", getAvailableCapacityDose2());
		jsonObject.addProperty("vaccine", getVaccine());
		
		return jsonObject.toString();
	}
	
	

}
