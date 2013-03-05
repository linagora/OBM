/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;



public class MSContact implements IApplicationData, Serializable {
	
	private static final long serialVersionUID = -4482842460309227921L;
	
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

	@Override
	public final int hashCode(){
		return Objects.hashCode(assistantName, assistantPhoneNumber, assistnamePhoneNumber, 
				business2PhoneNumber, businessAddressCity, businessPhoneNumber, webPage, 
				businessAddressCountry, department, email1Address, email2Address, email3Address, 
				businessFaxNumber, fileAs, firstName, middleName, homeAddressCity, 
				homeAddressCountry, homeFaxNumber, homePhoneNumber, home2PhoneNumber, 
				homeAddressPostalCode, homeAddressState, homeAddressStreet, mobilePhoneNumber, 
				suffix, companyName, otherAddressCity, otherAddressCountry, carPhoneNumber, 
				otherAddressPostalCode, otherAddressState, otherAddressStreet, pagerNumber, 
				title, businessPostalCode, lastName, spouse, businessState, businessStreet, 
				jobTitle, yomiFirstName, yomiLastName, yomiCompanyName, officeLocation, 
				radioPhoneNumber, picture, data, anniversary, birthday, categories, children, 
				customerId, governmentId, iMAddress, iMAddress2, iMAddress3, managerName, 
				companyMainPhone, accountName, nickName, mMS);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSContact) {
			MSContact that = (MSContact) object;
			return Objects.equal(this.assistantName, that.assistantName)
				&& Objects.equal(this.assistantPhoneNumber, that.assistantPhoneNumber)
				&& Objects.equal(this.assistnamePhoneNumber, that.assistnamePhoneNumber)
				&& Objects.equal(this.business2PhoneNumber, that.business2PhoneNumber)
				&& Objects.equal(this.businessAddressCity, that.businessAddressCity)
				&& Objects.equal(this.businessPhoneNumber, that.businessPhoneNumber)
				&& Objects.equal(this.webPage, that.webPage)
				&& Objects.equal(this.businessAddressCountry, that.businessAddressCountry)
				&& Objects.equal(this.department, that.department)
				&& Objects.equal(this.email1Address, that.email1Address)
				&& Objects.equal(this.email2Address, that.email2Address)
				&& Objects.equal(this.email3Address, that.email3Address)
				&& Objects.equal(this.businessFaxNumber, that.businessFaxNumber)
				&& Objects.equal(this.fileAs, that.fileAs)
				&& Objects.equal(this.firstName, that.firstName)
				&& Objects.equal(this.middleName, that.middleName)
				&& Objects.equal(this.homeAddressCity, that.homeAddressCity)
				&& Objects.equal(this.homeAddressCountry, that.homeAddressCountry)
				&& Objects.equal(this.homeFaxNumber, that.homeFaxNumber)
				&& Objects.equal(this.homePhoneNumber, that.homePhoneNumber)
				&& Objects.equal(this.home2PhoneNumber, that.home2PhoneNumber)
				&& Objects.equal(this.homeAddressPostalCode, that.homeAddressPostalCode)
				&& Objects.equal(this.homeAddressState, that.homeAddressState)
				&& Objects.equal(this.homeAddressStreet, that.homeAddressStreet)
				&& Objects.equal(this.mobilePhoneNumber, that.mobilePhoneNumber)
				&& Objects.equal(this.suffix, that.suffix)
				&& Objects.equal(this.companyName, that.companyName)
				&& Objects.equal(this.otherAddressCity, that.otherAddressCity)
				&& Objects.equal(this.otherAddressCountry, that.otherAddressCountry)
				&& Objects.equal(this.carPhoneNumber, that.carPhoneNumber)
				&& Objects.equal(this.otherAddressPostalCode, that.otherAddressPostalCode)
				&& Objects.equal(this.otherAddressState, that.otherAddressState)
				&& Objects.equal(this.otherAddressStreet, that.otherAddressStreet)
				&& Objects.equal(this.pagerNumber, that.pagerNumber)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.businessPostalCode, that.businessPostalCode)
				&& Objects.equal(this.lastName, that.lastName)
				&& Objects.equal(this.spouse, that.spouse)
				&& Objects.equal(this.businessState, that.businessState)
				&& Objects.equal(this.businessStreet, that.businessStreet)
				&& Objects.equal(this.jobTitle, that.jobTitle)
				&& Objects.equal(this.yomiFirstName, that.yomiFirstName)
				&& Objects.equal(this.yomiLastName, that.yomiLastName)
				&& Objects.equal(this.yomiCompanyName, that.yomiCompanyName)
				&& Objects.equal(this.officeLocation, that.officeLocation)
				&& Objects.equal(this.radioPhoneNumber, that.radioPhoneNumber)
				&& Objects.equal(this.picture, that.picture)
				&& Objects.equal(this.data, that.data)
				&& Objects.equal(this.anniversary, that.anniversary)
				&& Objects.equal(this.birthday, that.birthday)
				&& Objects.equal(this.categories, that.categories)
				&& Objects.equal(this.children, that.children)
				&& Objects.equal(this.customerId, that.customerId)
				&& Objects.equal(this.governmentId, that.governmentId)
				&& Objects.equal(this.iMAddress, that.iMAddress)
				&& Objects.equal(this.iMAddress2, that.iMAddress2)
				&& Objects.equal(this.iMAddress3, that.iMAddress3)
				&& Objects.equal(this.managerName, that.managerName)
				&& Objects.equal(this.companyMainPhone, that.companyMainPhone)
				&& Objects.equal(this.accountName, that.accountName)
				&& Objects.equal(this.nickName, that.nickName)
				&& Objects.equal(this.mMS, that.mMS);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("assistantName", assistantName)
			.add("assistantPhoneNumber", assistantPhoneNumber)
			.add("assistnamePhoneNumber", assistnamePhoneNumber)
			.add("business2PhoneNumber", business2PhoneNumber)
			.add("businessAddressCity", businessAddressCity)
			.add("businessPhoneNumber", businessPhoneNumber)
			.add("webPage", webPage)
			.add("businessAddressCountry", businessAddressCountry)
			.add("department", department)
			.add("email1Address", email1Address)
			.add("email2Address", email2Address)
			.add("email3Address", email3Address)
			.add("businessFaxNumber", businessFaxNumber)
			.add("fileAs", fileAs)
			.add("firstName", firstName)
			.add("middleName", middleName)
			.add("homeAddressCity", homeAddressCity)
			.add("homeAddressCountry", homeAddressCountry)
			.add("homeFaxNumber", homeFaxNumber)
			.add("homePhoneNumber", homePhoneNumber)
			.add("home2PhoneNumber", home2PhoneNumber)
			.add("homeAddressPostalCode", homeAddressPostalCode)
			.add("homeAddressState", homeAddressState)
			.add("homeAddressStreet", homeAddressStreet)
			.add("mobilePhoneNumber", mobilePhoneNumber)
			.add("suffix", suffix)
			.add("companyName", companyName)
			.add("otherAddressCity", otherAddressCity)
			.add("otherAddressCountry", otherAddressCountry)
			.add("carPhoneNumber", carPhoneNumber)
			.add("otherAddressPostalCode", otherAddressPostalCode)
			.add("otherAddressState", otherAddressState)
			.add("otherAddressStreet", otherAddressStreet)
			.add("pagerNumber", pagerNumber)
			.add("title", title)
			.add("businessPostalCode", businessPostalCode)
			.add("lastName", lastName)
			.add("spouse", spouse)
			.add("businessState", businessState)
			.add("businessStreet", businessStreet)
			.add("jobTitle", jobTitle)
			.add("yomiFirstName", yomiFirstName)
			.add("yomiLastName", yomiLastName)
			.add("yomiCompanyName", yomiCompanyName)
			.add("officeLocation", officeLocation)
			.add("radioPhoneNumber", radioPhoneNumber)
			.add("picture", picture)
			.add("data", data)
			.add("anniversary", anniversary)
			.add("birthday", birthday)
			.add("categories", categories)
			.add("children", children)
			.add("customerId", customerId)
			.add("governmentId", governmentId)
			.add("iMAddress", iMAddress)
			.add("iMAddress2", iMAddress2)
			.add("iMAddress3", iMAddress3)
			.add("managerName", managerName)
			.add("companyMainPhone", companyMainPhone)
			.add("accountName", accountName)
			.add("nickName", nickName)
			.add("mMS", mMS)
			.toString();
	}

	
}
