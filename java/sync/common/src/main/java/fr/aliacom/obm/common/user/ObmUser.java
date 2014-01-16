/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import org.obm.provisioning.Group;
import org.obm.provisioning.ProfileName;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;
import org.obm.sync.utils.DisplayNameUtils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ObmUser {
	
	public static final String EMAIL_FIELD_SEPARATOR = "\r\n";
	public static final int MAXIMUM_SUPPORTED_ADDRESSES = 3;
	public static final int MAXIMUM_SUPPORTED_FAXES = 2;
	public static final int MAXIMUM_SUPPORTED_PHONES = 2;
	public static final String PATTERN_AT_STAR = "@*";

	public static Builder builder() {
		return new Builder();
	}

	public static Iterable<String> retrieveEmailsFromObmDao(String emails) {
		Iterable<String> emailAndAlias = Splitter
				.on(EMAIL_FIELD_SEPARATOR)
				.omitEmptyStrings()
				.split(emails);
		return emailAndAlias;
	}

	public static class Builder {
		
		private Integer uid;
		private EntityId entityId;
		private UserExtId extId;
		private String login;
		private Boolean admin;
		private String commonName;
		private String lastName;
		private String firstName;
		private String email;
		private ImmutableSet.Builder<String> emailAlias;
		private Boolean hidden;
		
		private String address1;
		private String address2;
		private String address3;

		private String expresspostal;
		private String mobile;
		private String service;
		private String title;
		private String town;
		private String zipCode;
		private String description;

		private Date timeCreate;
		private Date timeUpdate;
		private ObmUser createdBy;
		private ObmUser updatedBy;

		private ObmDomain domain;
		private boolean publicFreeBusy;

		private String password;
		private ProfileName profileName;
		private String kind;
		private String company;
		private String direction;
		private String countryCode;
		private String phone;
		private String phone2;
		private String fax;
		private String fax2;
		private Integer mailQuota;
		private ObmHost mailHost;
		private Boolean archived;

		private Integer uidNumber;
		private Integer gidNumber;
		
		private ImmutableSet.Builder<Group> groups;

		private Builder() {
			emailAlias = ImmutableSet.builder();
			groups = ImmutableSet.builder();
		}

		public Builder from(ObmUser user) {
			return uid(user.uid)
					.login(user.login)
					.email(user.email)
					.emailAlias(user.emailAlias)
					.domain(user.domain)
					.firstName(user.firstName)
					.lastName(user.lastName)
					.publicFreeBusy(user.publicFreeBusy)
					.commonName(user.commonName)
					.extId(user.extId)
					.entityId(user.entityId)
					.password(user.password)
					.profileName(user.profileName)
					.kind(user.kind)
					.title(user.title)
					.description(user.description)
					.company(user.company)
					.service(user.service)
					.direction(user.direction)
					.address1(user.address1)
					.address2(user.address2)
					.address3(user.address3)
					.town(user.town)
					.zipCode(user.zipCode)
					.expresspostal(user.expresspostal)
					.countryCode(user.countryCode)
					.phone(user.phone)
					.phone2(user.phone2)
					.mobile(user.mobile)
					.fax(user.fax)
					.fax2(user.fax2)
					.mailQuota(user.mailQuota)
					.mailHost(user.mailHost)
					.archived(user.archived)
					.hidden(user.hidden)
					.timeCreate(user.timeCreate)
					.timeUpdate(user.timeUpdate)
					.createdBy(user.createdBy)
					.updatedBy(user.updatedBy)
					.uidNumber(user.uidNumber)
					.gidNumber(user.gidNumber)
					.groups(user.groups);
		}

		public Builder uid(Integer uid) {
			this.uid = uid;
			return this;
		}
		public Builder entityId(EntityId entityId) {
			this.entityId = entityId;
			return this;
		}
		public Builder extId(UserExtId extId) {
			this.extId = extId;
			return this;
		}
		public Builder login(String login) {
			this.login = login;
			return this;
		}
		public Builder admin(boolean admin) {
			this.admin = admin;
			return this;
		}
		public Builder commonName(String commonName) {
			this.commonName = commonName;
			return this;
		}
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		public Builder address1(String address1) {
			this.address1 = address1;
			return this;
		}
		public Builder address2(String address2) {
			this.address2 = address2;
			return this;
		}
		public Builder address3(String address3) {
			this.address3 = address3;
			return this;
		}
		public Builder expresspostal(String expresspostal) {
			this.expresspostal = expresspostal;
			return this;
		}
		public Builder mobile(String mobile) {
			this.mobile = mobile;
			return this;
		}
		public Builder service(String service) {
			this.service = service;
			return this;
		}
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		public Builder town(String town) {
			this.town = town;
			return this;
		}
		public Builder zipCode(String zipCode) {
			this.zipCode = zipCode;
			return this;
		}
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		public Builder timeCreate(Date timeCreate) {
			this.timeCreate = timeCreate;
			return this;
		}
		public Builder timeUpdate(Date timeUpdate) {
			this.timeUpdate = timeUpdate;
			return this;
		}
		public Builder createdBy(ObmUser createdBy) {
			this.createdBy = createdBy;
			return this;
		}
		public Builder updatedBy(ObmUser updatedBy) {
			this.updatedBy = updatedBy;
			return this;
		}
		public Builder domain(ObmDomain domain) {
			this.domain = domain;
			return this;
		}
		public Builder publicFreeBusy(boolean publicFreeBusy) {
			this.publicFreeBusy = publicFreeBusy;
			return this;
		}
		public Builder profileName(ProfileName profileName) {
			this.profileName = profileName;
			return this;
		}
		public Builder kind(String kind) {
			this.kind = kind;
			return this;
		}
		public Builder company(String company) {
			this.company = company;
			return this;
		}
		public Builder direction(String direction) {
			this.direction = direction;
			return this;
		}
		public Builder countryCode(String countryCode) {
			this.countryCode = countryCode;
			return this;
		}
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
		public Builder phone2(String phone2) {
			this.phone2 = phone2;
			return this;
		}
		public Builder phones(Iterable<String> phones) {
			Preconditions.checkNotNull(phones);
			Preconditions.checkState(Iterables.size(phones) <= MAXIMUM_SUPPORTED_PHONES);

			phone = Iterables.get(phones, 0, null);
			phone2 = Iterables.get(phones, 1, null);
			return this;
		}
		public Builder fax(String fax) {
			this.fax = fax;
			return this;
		}
		public Builder fax2(String fax2) {
			this.fax2 = fax2;
			return this;
		}
		public Builder faxes(Iterable<String> faxes) {
			Preconditions.checkNotNull(faxes);
			Preconditions.checkState(Iterables.size(faxes) <= MAXIMUM_SUPPORTED_FAXES);

			fax = Iterables.get(faxes, 0, null);
			fax2 = Iterables.get(faxes, 1, null);
			return this;
		}
		public Builder mailQuota(Integer mailQuota) {
			this.mailQuota = mailQuota;
			return this;
		}
		public Builder mailHost(ObmHost mailHost) {
			this.mailHost = mailHost;
			return this;
		}

		public Builder archived(boolean archived) {
			this.archived = archived;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}
		public Builder emailAndAliases(String emailAndAliases) {
			Preconditions.checkNotNull(emailAndAliases);
			email = null;

			Iterable<String> emailAndAlias = retrieveEmailsFromObmDao(emailAndAliases);

			for (String emailOrAlias: emailAndAlias) {
				if (email == null) {
					email = emailOrAlias;	
				} else {
					emailAlias.add(emailOrAlias);
				}			
			}
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder emailAlias(Iterable<String> emailAlias) {
			Preconditions.checkNotNull(emailAlias);
			this.emailAlias.addAll(emailAlias);
			return this;
		}

		public Builder mails(Iterable<String> mails) {
			Preconditions.checkNotNull(mails);
			email = Iterables.getFirst(mails, null);
			emailAlias.addAll(Iterables.skip(mails, 1));
			return this;
		}
		
		public Builder hidden(boolean hidden) {
			this.hidden = hidden;
			return this;
		}
		
		public Builder addresses(Iterable<String> addresses) {
			Preconditions.checkNotNull(addresses);
			Preconditions.checkState(Iterables.size(addresses) <= MAXIMUM_SUPPORTED_ADDRESSES);
			address1 = Iterables.get(addresses, 0, null);
			address2 = Iterables.get(addresses, 1, null);
			address3 = Iterables.get(addresses, 2, null);
			return this;
		}

		public Builder uidNumber(Integer uidNumber) {
			this.uidNumber = uidNumber;
			return this;
		}
		public Builder gidNumber(Integer gidNumber) {
			this.gidNumber = gidNumber;
			return this;
		}

		public Builder groups(Iterable<Group> groups) {
			Preconditions.checkNotNull(groups);
			this.groups.addAll(groups);
			return this;
		}

		public ObmUser build() {
			Preconditions.checkState(uid != null || extId != null);
			Preconditions.checkState(login != null);
			Preconditions.checkState(domain != null);
			admin = Objects.firstNonNull(admin, false);

			// The DB model uses 0 in the mail quota column to mean "no quota"
			// ObmUser uses null internally to mean "no quota"
			if (mailQuota != null && mailQuota == 0) {
				mailQuota = null;
			}

			archived = Objects.firstNonNull(archived, false);			
			hidden = Objects.firstNonNull(hidden, false);

			return new ObmUser(
					uid, entityId, login, extId, admin, commonName, lastName, firstName,
					email, emailAlias.build(), hidden,
					address1, address2, address3, expresspostal, mobile, service, title, town,
					zipCode, description, timeCreate, timeUpdate, createdBy, updatedBy,
					domain, publicFreeBusy, profileName, kind, company, direction, countryCode,
					phone, phone2, fax, fax2, mailQuota, mailHost, archived, password, uidNumber, gidNumber, groups.build());
		}
		
	}
	
	private final Integer uid;
	private final EntityId entityId;
	private final String login;
	private final UserExtId extId;
	private final boolean admin;
	private final String commonName;
	private final String lastName;
	private final String firstName;
	private final String email;
	private final Set<String> emailAlias;
	private final boolean hidden;
	
	private final String address1;
	private final String address2;
	private final String address3;

	private final String expresspostal;
	private final String mobile;
	private final String service;
	private final String title;
	private final String town;
	private final String zipCode;
	private final String description;

	private final Date timeCreate;
	private final Date timeUpdate;
	private final ObmUser createdBy;
	private final ObmUser updatedBy;

	private final ObmDomain domain;
	private final boolean publicFreeBusy;

	private final String password;
	private final ProfileName profileName;
	private final String kind;
	private final String company;
	private final String direction;
	private final String countryCode;
	private final String phone;
	private final String phone2;
	private final String fax;
	private final String fax2;
	private final Integer mailQuota;
	private final ObmHost mailHost;
	private final boolean archived;

	private final Integer uidNumber;
	private final Integer gidNumber;
	
	private Set<Group> groups;

	public ObmUser(Integer uid, EntityId entityId, String login, UserExtId extId, boolean admin, String commonName,
			String lastName, String firstName, String email,
			Set<String> emailAlias, boolean hidden,
			String address1, String address2,
			String address3, String expresspostal,
			String mobile, String service, String title, String town,
			String zipCode,
			String description, Date timeCreate, Date timeUpdate,
			ObmUser createdBy, ObmUser updatedBy, ObmDomain domain,
			boolean publicFreeBusy, ProfileName profileName, String kind, String company,
			String direction, String countryCode, String phone, String phone2, String fax, String fax2,
			Integer mailQuota, ObmHost mailHost, boolean archived, String password, Integer uidNumber, Integer gidNumber, Set<Group> groups) {
		this.uid = uid;
		this.entityId = entityId;
		this.login = login;
		this.extId = extId;
		this.admin = admin;
		this.commonName = commonName;
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.emailAlias = emailAlias;
		this.hidden = hidden;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.expresspostal = expresspostal;
		this.mobile = mobile;
		this.service = service;
		this.title = title;
		this.town = town;
		this.zipCode = zipCode;
		this.description = description;
		this.timeCreate = timeCreate;
		this.timeUpdate = timeUpdate;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.domain = domain;
		this.publicFreeBusy = publicFreeBusy;
		this.profileName = profileName;
		this.kind = kind;
		this.company = company;
		this.direction = direction;
		this.countryCode = countryCode;
		this.phone = phone;
		this.phone2 = phone2;
		this.fax = fax;
		this.fax2 = fax2;
		this.mailQuota = mailQuota;
		this.mailHost = mailHost;
		this.archived = archived;
		this.password = password;
		this.uidNumber = uidNumber;
		this.gidNumber = gidNumber;
		this.groups = groups;
	}

	public int getUid() {
		return uid;
	}

	public EntityId getEntityId() {
		return entityId;
	}

	public String getLogin() {
		return login;
	}

	public UserExtId getExtId() {
		return extId;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public String getCommonName() {
		return commonName;
	}
	
	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public Set<String> getEmailAlias() {
		return emailAlias;
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public String getAddress1() {
		return address1;
	}

	public String getAddress2() {
		return address2;
	}

	public String getAddress3() {
		return address3;
	}

	public String getExpresspostal() {
		return expresspostal;
	}


	public String getMobile() {
		return mobile;
	}

	public String getService() {
		return service;
	}

	public String getTitle() {
		return title;
	}

	public String getTown() {
		return town;
	}


	public String getZipCode() {
		return zipCode;
	}

	public String getDescription() {
		return description;
	}

	public Date getTimeCreate() {
		return timeCreate;
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public ObmUser getCreatedBy() {
		return createdBy;
	}

	public ObmUser getUpdatedBy() {
		return updatedBy;
	}

	public ObmDomain getDomain() {
		return domain;
	}

	public boolean isPublicFreeBusy() {
		return publicFreeBusy;
	}
	
	public String getEmail() {
		return email;
	}

	public String getEmailAtDomain() {
		return appendDomainToEmailIfRequired(email, domain.getName());
	}
	
	public String getLoginAtDomain() {
		return login + "@" + domain.getName();
	}
	
	private String appendDomainToEmailIfRequired(String emailAddress, String domainName) {
		return appendPatternToEmailIfRequired(emailAddress, "@" + domainName);
	}

	private String appendAtStarToEmailIfRequired(String emailAddress) {
		return appendPatternToEmailIfRequired(emailAddress, PATTERN_AT_STAR);
	}

	private String appendPatternToEmailIfRequired(String emailAddress, String pattern) {
		if(emailAddress != null && !emailAddress.contains("@")) {
			return emailAddress + pattern;
		}
		return emailAddress;
	}

	public String getDisplayName(){
		return DisplayNameUtils.getDisplayName(commonName, firstName, lastName);
	}
	
	public void addAlias(String alias) {
		emailAlias.add(alias);
	}

	private Set<String> emailAndAliases() {
		if (isEmailAvailable()) {
			return Sets.union(ImmutableSet.of(email), emailAlias);
		}
		return ImmutableSet.of();
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
				})
				.toSet();
	}

	public Iterable<String> buildMailsDefinition() {
		return FluentIterable
				.from(emailAndAliases())
				.transform(new Function<String, String>() {
					@Override
					public String apply(String input) {
						return appendAtStarToEmailIfRequired(input);
					}
				});
	}

	public Set<String> getAddresses() {
		return Sets.newHashSet(address1, address2, address3);
	}
	
	public ProfileName getProfileName() {
		return profileName;
	}

	public String getKind() {
		return kind;
	}

	public String getCompany() {
		return company;
	}

	public String getDirection() {
		return direction;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getPhone() {
		return phone;
	}

	public String getPhone2() {
		return phone2;
	}

	public String getFax() {
		return fax;
	}

	public String getFax2() {
		return fax2;
	}

	public Integer getMailQuota() {
		return mailQuota;
	}

	public int getMailQuotaAsInt() {
		return mailQuota != null ? mailQuota : 0;
	}

	public ObmHost getMailHost() {
		return mailHost;
	}

	public boolean isArchived() {
		return archived;
	}

	public String getPassword() {
		return password;
	}

	public Integer getUidNumber() {
		return uidNumber;
	}

	public Integer getGidNumber() {
		return gidNumber;
	}
	
	public Set<Group> getGroups() {
		return groups;
	}

	public boolean isEmailAvailable() {
		return !Strings.isNullOrEmpty(email) || !Iterables.isEmpty(emailAlias);
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(uid, entityId, login, extId, admin, commonName, lastName, firstName, email,
				emailAlias, hidden, address1, address2, address3, expresspostal, mobile,
				service, title, town, zipCode,	description, createdBy, updatedBy, domain, publicFreeBusy, profileName, kind, company,
				direction, countryCode, phone, phone2, fax, fax2, mailQuota, archived, mailHost, password, uidNumber, gidNumber, groups);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof ObmUser) {
			ObmUser that = (ObmUser) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.entityId, that.entityId)
				&& Objects.equal(this.login, that.login)
				&& Objects.equal(this.extId, that.extId)
				&& Objects.equal(this.admin, that.admin)
				&& Objects.equal(this.commonName, that.commonName)
				&& Objects.equal(this.lastName, that.lastName)
				&& Objects.equal(this.firstName, that.firstName)
				&& Objects.equal(this.email, that.email)
				&& Objects.equal(this.emailAlias, that.emailAlias)
				&& Objects.equal(this.hidden, that.hidden)
				&& Objects.equal(this.address1, that.address1)
				&& Objects.equal(this.address2, that.address2)
				&& Objects.equal(this.address3, that.address3)
				&& Objects.equal(this.expresspostal, that.expresspostal)
				&& Objects.equal(this.mobile, that.mobile)
				&& Objects.equal(this.service, that.service)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.town, that.town)
				&& Objects.equal(this.zipCode, that.zipCode)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.createdBy, that.createdBy)
				&& Objects.equal(this.updatedBy, that.updatedBy)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.publicFreeBusy, that.publicFreeBusy)
				&& Objects.equal(this.profileName, that.profileName)
				&& Objects.equal(this.kind, that.kind)
				&& Objects.equal(this.company, that.company)
				&& Objects.equal(this.direction, that.direction)
				&& Objects.equal(this.countryCode, that.countryCode)
				&& Objects.equal(this.phone, that.phone)
				&& Objects.equal(this.phone2, that.phone2)
				&& Objects.equal(this.fax, that.fax)
				&& Objects.equal(this.fax2, that.fax2)
				&& Objects.equal(this.mailQuota, that.mailQuota)
				&& Objects.equal(this.mailHost, that.mailHost)
				&& Objects.equal(this.archived, that.archived)
				&& Objects.equal(this.password, that.password)
				&& Objects.equal(this.uidNumber, that.uidNumber)
				&& Objects.equal(this.gidNumber, that.gidNumber)
				&& Objects.equal(this.groups, that.groups);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("entityId", entityId)
			.add("login", login)
			.add("extId", extId)
			.add("admin", admin)
			.add("commonName", commonName)
			.add("lastName", lastName)
			.add("firstName", firstName)
			.add("email", email)
			.add("emailAlias", emailAlias)
			.add("hidden", hidden)
			.add("address1", address1)
			.add("address2", address2)
			.add("address3", address3)
			.add("expresspostal", expresspostal)
			.add("mobile", mobile)
			.add("service", service)
			.add("title", title)
			.add("town", town)
			.add("zipCode", zipCode)
			.add("description", description)
			.add("timeCreate", timeCreate)
			.add("timeUpdate", timeUpdate)
			.add("createdBy", createdBy)
			.add("updatedBy", updatedBy)
			.add("domain", domain)
			.add("publicFreeBusy", publicFreeBusy)
			.add("profileName", profileName)
			.add("kind", kind)
			.add("company", company)
			.add("direction", direction)
			.add("countryCode", countryCode)
			.add("phone", phone)
			.add("phone2", phone2)
			.add("fax", fax)
			.add("fax2", fax2)
			.add("mailQuota", mailQuota)
			.add("mailHost", mailHost)
			.add("archived", archived)
			.add("uidNumber", uidNumber)
			.add("gidNumber", gidNumber)
			.add("groups", groups)
			.toString();
	}
}