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
package org.obm.sync.book;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obm.sync.calendar.EventObmId;
import org.obm.sync.utils.DisplayNameUtils;

import com.google.common.base.Objects;

public class Contact implements Serializable {

	private Integer uid;

	private String commonname;
	private String firstname;
	private String lastname;
	private String middlename;
	private String suffix;

	private String title;
	private String service;
	private String aka;
	private String comment;

	private String company;

	private Date birthday;
	private EventObmId birthdayId;

	private Date anniversary;
	private EventObmId anniversaryId;

	private String assistant;
	private String manager;
	private String spouse;

	private Integer entityId;
	private Integer folderId;

	private boolean collected;

	private String calUri;
	
	private Map<String, Phone> phones;
	private HashSet<Website> websites;
	private Map<String, Email> emails;
	private Map<String, InstantMessagingId> imIdentifiers;
	private Map<String, Address> addresses;

	public Contact() {
		phones = new HashMap<String, Phone>();
		websites = new HashSet<Website>();
		emails = new HashMap<String, Email>();
		addresses = new HashMap<String, Address>();
		imIdentifiers = new HashMap<String, InstantMessagingId>();
		collected = false;
	}

	public String getCommonname() {
		return commonname;
	}

	public void setCommonname(String commonname) {
		this.commonname = commonname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getAka() {
		return aka;
	}

	public void setAka(String aka) {
		this.aka = aka;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Map<String, Phone> getPhones() {
		return phones;
	}

	public HashSet<Website> getWebsites() {
		return websites;
	}
	
	public Website getWebsite() {
		if (websites.size() > 0) {
			return websites.iterator().next();
		}
		return null;
	}

	public Map<String, Email> getEmails() {
		return emails;
	}

	public Map<String, InstantMessagingId> getImIdentifiers() {
		return imIdentifiers;
	}

	public Map<String, Address> getAddresses() {
		return addresses;
	}

	public void addPhone(String lbl, Phone p) {
		phones.put(lbl, p);
	}

	public void addAddress(String lbl, Address p) {
		addresses.put(lbl, p);
	}

	public void addWebsite(Website website) {
		websites.add(website);
	}

	public void addIMIdentifier(String lbl, InstantMessagingId imid) {
		imIdentifiers.put(lbl, imid);
	}

	public void addEmail(String lbl, Email email) {
		emails.put(lbl, email);
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public EventObmId getBirthdayId() {
		return birthdayId;
	}

	public void setBirthdayId(EventObmId birthdayId) {
		this.birthdayId = birthdayId;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Date getAnniversary() {
		return anniversary;
	}

	public void setAnniversary(Date anniversary) {
		this.anniversary = anniversary;
	}

	public EventObmId getAnniversaryId() {
		return anniversaryId;
	}

	public void setAnniversaryId(EventObmId anniversaryId) {
		this.anniversaryId = anniversaryId;
	}

	public String getAssistant() {
		return assistant;
	}

	public void setAssistant(String assistant) {
		this.assistant = assistant;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getSpouse() {
		return spouse;
	}

	public void setSpouse(String spouse) {
		this.spouse = spouse;
	}

	public boolean isCollected() {
		return collected;
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}

	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	public Integer getFolderId() {
		return folderId;
	}

	public String getCalUri() {
		return calUri;
	}

	public void setCalUri(String calUri) {
		this.calUri = calUri;
	}
	
	public String getDisplayName(){
		return DisplayNameUtils.getDisplayName(commonname, firstname, lastname);
	}

	public Set<String> listWebSitesLabel() {
		Set<String> labels = new HashSet<String>();
		for (Website website: websites) {
			labels.add(website.getLabel());
		}
		return labels;
	}
	
	public void updateWebSites(HashSet<Website> websites) {
		this.websites = websites;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("commonname", commonname)
			.add("firstname", firstname)
			.add("folderId", folderId)
			.toString();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, 
				commonname, 
				firstname, 
				lastname, 
				middlename, 
				suffix, 
				title, 
				service, 
				aka, 
				comment, 
				company, 
				birthday, 
				birthdayId, 
				anniversary, 
				anniversaryId, 
				assistant, 
				manager, 
				spouse, 
				entityId, 
				folderId, 
				collected, 
				calUri, 
				phones, 
				websites, 
				emails, 
				imIdentifiers, 
				addresses);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Contact) {
			Contact that = (Contact) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.commonname, that.commonname)
				&& Objects.equal(this.firstname, that.firstname)
				&& Objects.equal(this.lastname, that.lastname)
				&& Objects.equal(this.middlename, that.middlename)
				&& Objects.equal(this.suffix, that.suffix)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.service, that.service)
				&& Objects.equal(this.aka, that.aka)
				&& Objects.equal(this.comment, that.comment)
				&& Objects.equal(this.company, that.company)
				&& Objects.equal(this.birthday, that.birthday)
				&& Objects.equal(this.birthdayId, that.birthdayId)
				&& Objects.equal(this.anniversary, that.anniversary)
				&& Objects.equal(this.anniversaryId, that.anniversaryId)
				&& Objects.equal(this.assistant, that.assistant)
				&& Objects.equal(this.manager, that.manager)
				&& Objects.equal(this.spouse, that.spouse)
				&& Objects.equal(this.entityId, that.entityId)
				&& Objects.equal(this.folderId, that.folderId)
				&& Objects.equal(this.collected, that.collected)
				&& Objects.equal(this.calUri, that.calUri)
				&& Objects.equal(this.phones, that.phones)
				&& Objects.equal(this.websites, that.websites)
				&& Objects.equal(this.emails, that.emails)
				&& Objects.equal(this.imIdentifiers, that.imIdentifiers)
				&& Objects.equal(this.addresses, that.addresses);
		}
		return false;
	}
}