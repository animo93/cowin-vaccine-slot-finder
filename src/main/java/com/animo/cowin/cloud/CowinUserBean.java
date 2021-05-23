package com.animo.cowin.cloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CowinUserBean {
	
	private String name;
	private String emailAddress;
	private int pinCode1;
	private int pinCode2;
	private String district;
	private String state;
	private String deviceToken;
	private int ageLimit;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public int getPinCode1() {
		return pinCode1;
	}
	public void setPinCode1(int pinCode1) {
		this.pinCode1 = pinCode1;
	}
	public int getPinCode2() {
		return pinCode2;
	}
	public void setPinCode2(int pinCode2) {
		this.pinCode2 = pinCode2;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	public int getAgeLimit() {
		return ageLimit;
	}
	public void setAgeLimit(int ageLimit) {
		this.ageLimit = ageLimit;
	}
	
	public List<Integer> getPinCode() {
		return Arrays.asList(new Integer[] {pinCode1,pinCode2});
	}
	
	public Map<String, Object> getUserBeanMap(){
		Map<String, Object> userBeanMap = new HashMap<String, Object>();
		userBeanMap.put("name", name);
		userBeanMap.put("device_token", deviceToken);
		userBeanMap.put("state", state);
		userBeanMap.put("district", district);
		userBeanMap.put("preferred_pincode", getPinCode());
		userBeanMap.put("email_address", emailAddress);
		userBeanMap.put("age_limit", ageLimit);
		
		return userBeanMap;
	}
	

}
