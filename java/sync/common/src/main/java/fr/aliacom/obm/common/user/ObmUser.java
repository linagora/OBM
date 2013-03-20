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
package fr.aliacom.obm.common.user;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.utils.DisplayNameUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ObmUser {

	private int uid;
	private int entityId;
	private String login;
	private String commonName;
	private String lastName;
	private String firstName;
	private String email;
	private Set<String> emailAlias;
	
	private String address1;
	private String address2;
	private String address3;

	private String expresspostal;
	private String homePhone;
	private String mobile;
	private String service;
	private String title;
	private String town;
	private String workFax;
	private String workPhone;
	private String zipCode;
	private String description;

	private Date timeCreate;
	private Date timeUpdate;
	private ObmUser createdBy;
	private ObmUser updatedBy;

	private ObmDomain domain;
	private boolean publicFreeBusy;

	public ObmUser() {
		emailAlias = Sets.newHashSet();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public String getExpresspostal() {
		return expresspostal;
	}

	public void setExpresspostal(String expresspostal) {
		this.expresspostal = expresspostal;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getWorkFax() {
		return workFax;
	}

	public void setWorkFax(String workFax) {
		this.workFax = workFax;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public ObmDomain getDomain() {
		return domain;
	}

	public void setDomain(ObmDomain domain) {
		this.domain = domain;
	}

	public String getEmail() {
		return appendDomainToEmailIfRequired(email);
	}

	private String appendDomainToEmailIfRequired(String emailAddress) {
		return appendDomainToEmailIfRequired(emailAddress, domain.getName());
	}
	
	private String appendDomainToEmailIfRequired(String emailAddress, String domainName) {
		if(emailAddress != null && !emailAddress.contains("@")) {
			return emailAddress + "@" + domainName;
		}
		return emailAddress;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public Set<String> getEmailAlias() {
		return emailAlias;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstname) {
		this.firstName = firstname;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	
	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastname) {
		this.lastName = lastname;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Date getTimeCreate() {
		return timeCreate;
	}

	public void setTimeCreate(Date timeCreate) {
		this.timeCreate = timeCreate;
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Date timeUpdate) {
		this.timeUpdate = timeUpdate;
	}

	public ObmUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(ObmUser createdBy) {
		this.createdBy = createdBy;
	}

	public ObmUser getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(ObmUser updatedBy) {
		this.updatedBy = updatedBy;
	}

	public boolean isPublicFreeBusy() {
		return publicFreeBusy;
	}

	public void setPublicFreeBusy(boolean publicFreeBusy) {
		this.publicFreeBusy = publicFreeBusy;
	}
	
	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getDisplayName(){
		return DisplayNameUtils.getDisplayName(commonName, firstName, lastName);
	}
	
	public void addAlias(String alias) {
		emailAlias.add(alias);
	}

	private Set<String> emailAndAliases() {
		return Sets.union(ImmutableSet.of(email), emailAlias);
	}
	
	public Iterable<String> buildAllEmails() {
		return FluentIterable
				.from(Sets.cartesianProduct(
						ImmutableList.of(emailAndAliases(), domain.getNames())))
				.transform(new Function<List<String>, String>() {
					@Override
					public String apply(List<String> input) {
						return appendDomainToEmailIfRequired(input.get(0), input.get(1));
					}
				});
	}
	
	@Override
	public String toString() {
		return getEmail();
	}
}