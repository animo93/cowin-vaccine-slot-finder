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
	private int availableCapacity;
	
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
	public int getAvailableCapacity() {
		return availableCapacity;
	}
	public void setAvailableCapacity(int availableCapacity) {
		this.availableCapacity = availableCapacity;
	}
	
	public String toJson() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty("centerName", getCenterName());
		jsonObject.addProperty("centerId", getCenterId());
		jsonObject.addProperty("pincode", getPincode());
		jsonObject.addProperty("districtName", getDistrictName());
		jsonObject.addProperty("date", getDate());
		jsonObject.addProperty("minAgeLimit", getMinAgeLimit());
		jsonObject.addProperty("availableCapacity", getAvailableCapacity());
		
		return jsonObject.toString();
	}
	
	

}
