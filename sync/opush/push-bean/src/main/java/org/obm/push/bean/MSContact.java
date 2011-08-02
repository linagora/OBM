package org.obm.push.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class MSContact implements IApplicationData, Serializable {
	
	private String assistantName;
	private String assistantPhoneNumber;
	private String assistnamePhoneNumber;
	private String business2PhoneNumber;
	private String businessAddressCity;
	private String businessPhoneNumber;
	private String webPage;
	private String businessAddressCountry;
	private String department;
	private String email1Address;
	private String email2Address;
	private String email3Address;
	private String businessFaxNumber;
	private String fileAs;
	private String firstName;
	private String middleName;
	private String homeAddressCity;
	private String homeAddressCountry;
	private String homeFaxNumber;
	private String homePhoneNumber;
	private String home2PhoneNumber;
	private String homeAddressPostalCode;
	private String homeAddressState;
	private String homeAddressStreet;
	private String mobilePhoneNumber;
	private String suffix;
	private String companyName;
	private String otherAddressCity;
	private String otherAddressCountry;
	private String carPhoneNumber;
	private String otherAddressPostalCode;
	private String otherAddressState;
	private String otherAddressStreet;
	private String pagerNumber;
	private String title;
	private String businessPostalCode;
	private String lastName;
	private String spouse;
	private String businessState;
	private String businessStreet;
	private String jobTitle;
	private String yomiFirstName;
	private String yomiLastName;
	private String yomiCompanyName;
	private String officeLocation;
	private String radioPhoneNumber;
	private String picture;
	private String data;

	private Date anniversary;
	private Date birthday;

	private List<String> categories;
	private List<String> children;

	// Contacts2

	private String customerId;
	private String governmentId;
	private String iMAddress;
	private String iMAddress2;
	private String iMAddress3;
	private String managerName;
	private String companyMainPhone;
	private String accountName;
	private String nickName;
	private String mMS;

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}

	public List<String> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<String> children) {
		this.children = children;
	}

	public String getAssistantName() {
		return assistantName;
	}

	public void setAssistantName(String assistantName) {
		this.assistantName = assistantName;
	}

	public String getAssistantPhoneNumber() {
		return assistantPhoneNumber;
	}

	public void setAssistantPhoneNumber(String assistantPhoneNumber) {
		this.assistantPhoneNumber = assistantPhoneNumber;
	}

	public String getAssistnamePhoneNumber() {
		return assistnamePhoneNumber;
	}

	public void setAssistnamePhoneNumber(String assistnamePhoneNumber) {
		this.assistnamePhoneNumber = assistnamePhoneNumber;
	}

	public String getBusiness2PhoneNumber() {
		return business2PhoneNumber;
	}

	public void setBusiness2PhoneNumber(String business2PhoneNumber) {
		this.business2PhoneNumber = business2PhoneNumber;
	}

	public String getBusinessAddressCity() {
		return businessAddressCity;
	}

	public void setBusinessAddressCity(String businessAddressCity) {
		this.businessAddressCity = businessAddressCity;
	}

	public String getBusinessPhoneNumber() {
		return businessPhoneNumber;
	}

	public void setBusinessPhoneNumber(String businessPhoneNumber) {
		this.businessPhoneNumber = businessPhoneNumber;
	}

	public String getWebPage() {
		return webPage;
	}

	public void setWebPage(String webPage) {
		this.webPage = webPage;
	}

	public String getBusinessAddressCountry() {
		return businessAddressCountry;
	}

	public void setBusinessAddressCountry(String businessAddressCountry) {
		this.businessAddressCountry = businessAddressCountry;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmail1Address() {
		return email1Address;
	}

	public void setEmail1Address(String email1Address) {
		this.email1Address = email1Address;
	}

	public String getEmail2Address() {
		return email2Address;
	}

	public void setEmail2Address(String email2Address) {
		this.email2Address = email2Address;
	}

	public String getEmail3Address() {
		return email3Address;
	}

	public void setEmail3Address(String email3Address) {
		this.email3Address = email3Address;
	}

	public String getBusinessFaxNumber() {
		return businessFaxNumber;
	}

	public void setBusinessFaxNumber(String businessFaxNumber) {
		this.businessFaxNumber = businessFaxNumber;
	}

	public String getFileAs() {
		return fileAs;
	}

	public void setFileAs(String fileAs) {
		this.fileAs = fileAs;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getHomeAddressCity() {
		return homeAddressCity;
	}

	public void setHomeAddressCity(String homeAddressCity) {
		this.homeAddressCity = homeAddressCity;
	}

	public String getHomeAddressCountry() {
		return homeAddressCountry;
	}

	public void setHomeAddressCountry(String homeAddressCountry) {
		this.homeAddressCountry = homeAddressCountry;
	}

	public String getHomeFaxNumber() {
		return homeFaxNumber;
	}

	public void setHomeFaxNumber(String homeFaxNumber) {
		this.homeFaxNumber = homeFaxNumber;
	}

	public String getHomePhoneNumber() {
		return homePhoneNumber;
	}

	public void setHomePhoneNumber(String homePhoneNumber) {
		this.homePhoneNumber = homePhoneNumber;
	}

	public String getHome2PhoneNumber() {
		return home2PhoneNumber;
	}

	public void setHome2PhoneNumber(String home2PhoneNumber) {
		this.home2PhoneNumber = home2PhoneNumber;
	}

	public String getHomeAddressPostalCode() {
		return homeAddressPostalCode;
	}

	public void setHomeAddressPostalCode(String homeAddressPostalCode) {
		this.homeAddressPostalCode = homeAddressPostalCode;
	}

	public String getHomeAddressState() {
		return homeAddressState;
	}

	public void setHomeAddressState(String homeAddressState) {
		this.homeAddressState = homeAddressState;
	}

	public String getHomeAddressStreet() {
		return homeAddressStreet;
	}

	public void setHomeAddressStreet(String homeAddressStreet) {
		this.homeAddressStreet = homeAddressStreet;
	}

	public String getMobilePhoneNumber() {
		return mobilePhoneNumber;
	}

	public void setMobilePhoneNumber(String mobilePhoneNumber) {
		this.mobilePhoneNumber = mobilePhoneNumber;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getOtherAddressCity() {
		return otherAddressCity;
	}

	public void setOtherAddressCity(String otherAddressCity) {
		this.otherAddressCity = otherAddressCity;
	}

	public String getOtherAddressCountry() {
		return otherAddressCountry;
	}

	public void setOtherAddressCountry(String otherAddressCountry) {
		this.otherAddressCountry = otherAddressCountry;
	}

	public String getCarPhoneNumber() {
		return carPhoneNumber;
	}

	public void setCarPhoneNumber(String carPhoneNumber) {
		this.carPhoneNumber = carPhoneNumber;
	}

	public String getOtherAddressPostalCode() {
		return otherAddressPostalCode;
	}

	public void setOtherAddressPostalCode(String otherAddressPostalCode) {
		this.otherAddressPostalCode = otherAddressPostalCode;
	}

	public String getOtherAddressState() {
		return otherAddressState;
	}

	public void setOtherAddressState(String otherAddressState) {
		this.otherAddressState = otherAddressState;
	}

	public String getOtherAddressStreet() {
		return otherAddressStreet;
	}

	public void setOtherAddressStreet(String otherAddressStreet) {
		this.otherAddressStreet = otherAddressStreet;
	}

	public String getPagerNumber() {
		return pagerNumber;
	}

	public void setPagerNumber(String pagerNumber) {
		this.pagerNumber = pagerNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBusinessPostalCode() {
		return businessPostalCode;
	}

	public void setBusinessPostalCode(String businessPostalCode) {
		this.businessPostalCode = businessPostalCode;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getSpouse() {
		return spouse;
	}

	public void setSpouse(String spouse) {
		this.spouse = spouse;
	}

	public String getBusinessState() {
		return businessState;
	}

	public void setBusinessState(String businessState) {
		this.businessState = businessState;
	}

	public String getBusinessStreet() {
		return businessStreet;
	}

	public void setBusinessStreet(String businessStreet) {
		this.businessStreet = businessStreet;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getYomiFirstName() {
		return yomiFirstName;
	}

	public void setYomiFirstName(String yomiFirstName) {
		this.yomiFirstName = yomiFirstName;
	}

	public String getYomiLastName() {
		return yomiLastName;
	}

	public void setYomiLastName(String yomiLastName) {
		this.yomiLastName = yomiLastName;
	}

	public String getYomiCompanyName() {
		return yomiCompanyName;
	}

	public void setYomiCompanyName(String yomiCompanyName) {
		this.yomiCompanyName = yomiCompanyName;
	}

	public String getOfficeLocation() {
		return officeLocation;
	}

	public void setOfficeLocation(String officeLocation) {
		this.officeLocation = officeLocation;
	}

	public String getRadioPhoneNumber() {
		return radioPhoneNumber;
	}

	public void setRadioPhoneNumber(String radioPhoneNumber) {
		this.radioPhoneNumber = radioPhoneNumber;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getGovernmentId() {
		return governmentId;
	}

	public void setGovernmentId(String governmentId) {
		this.governmentId = governmentId;
	}

	public String getIMAddress() {
		return iMAddress;
	}

	public void setIMAddress(String address) {
		iMAddress = address;
	}

	public String getIMAddress2() {
		return iMAddress2;
	}

	public void setIMAddress2(String address2) {
		iMAddress2 = address2;
	}

	public String getIMAddress3() {
		return iMAddress3;
	}

	public void setIMAddress3(String address3) {
		iMAddress3 = address3;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getCompanyMainPhone() {
		return companyMainPhone;
	}

	public void setCompanyMainPhone(String companyMainPhone) {
		this.companyMainPhone = companyMainPhone;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getMMS() {
		return mMS;
	}

	public void setMMS(String mms) {
		mMS = mms;
	}

	@Override
	public PIMDataType getType() {
		return PIMDataType.CONTACTS;
	}

	public Date getAnniversary() {
		return anniversary;
	}

	public void setAnniversary(Date anniversary) {
		this.anniversary = anniversary;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
