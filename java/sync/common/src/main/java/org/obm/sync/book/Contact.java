package org.obm.sync.book;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obm.sync.calendar.EventObmId;
import org.obm.sync.utils.DisplayNameUtils;
import com.google.common.base.Objects;

public class Contact {

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
			.toString();
	}
	
}