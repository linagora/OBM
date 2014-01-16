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
		private UserLogin login;
		private Boolean admin;
		private UserIdentity identity;
		private UserAddress address;
		private UserPhones phones;
		private UserWork work;
		private String email;
		private final ImmutableSet.Builder<String> emailAlias;
		private Boolean hidden;
		
		private String description;

		private Date timeCreate;
		private Date timeUpdate;
		private ObmUser createdBy;
		private ObmUser updatedBy;

		private ObmDomain domain;
		private boolean publicFreeBusy;

		private String password;
		private ProfileName profileName;
		private Integer mailQuota;
		private ObmHost mailHost;
		private Boolean archived;

		private Integer uidNumber;
		private Integer gidNumber;
		
		private final ImmutableSet.Builder<Group> groups;

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
					.identity(user.identity)
					.address(user.address)
					.publicFreeBusy(user.publicFreeBusy)
					.extId(user.extId)
					.entityId(user.entityId)
					.password(user.password)
					.profileName(user.profileName)
					.description(user.description)
					.work(user.work)
					.phones(user.phones)
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
		public Builder login(UserLogin login) {
			this.login = login;
			return this;
		}
		public Builder identity(UserIdentity identity) {
			this.identity = identity;
			return this;
		}
		public Builder admin(boolean admin) {
			this.admin = admin;
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
		public Builder work(UserWork work) {
			this.work = work;
			return this;
		}
		public Builder phones(UserPhones phones) {
			this.phones = phones;
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
		
		public Builder address(UserAddress address) {
			Preconditions.checkNotNull(address);
			this.address = address;
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

			UserIdentity identity = Objects.firstNonNull(this.identity, UserIdentity.empty());
			UserAddress address = Objects.firstNonNull(this.address, UserAddress.empty());
			UserPhones phones = Objects.firstNonNull(this.phones, UserPhones.empty());
			UserWork work = Objects.firstNonNull(this.work, UserWork.empty());

			return new ObmUser(
					uid, entityId, login, extId, admin, identity,
					email, emailAlias.build(), hidden,
					address, phones, work,  
					description, timeCreate, timeUpdate, createdBy, updatedBy,
					domain, publicFreeBusy, profileName,
					mailQuota, mailHost, archived, password, uidNumber, gidNumber, groups.build());
		}
		
	}
	
	private final Integer uid;
	private final EntityId entityId;
	private final UserLogin login;
	private final UserExtId extId;
	private final boolean admin;
	private final UserIdentity identity;
	private final UserAddress address;
	private final UserPhones phones;
	private final UserWork work;
	private final String email;
	private final Set<String> emailAlias;
	private final boolean hidden;
	
	private final String description;

	private final Date timeCreate;
	private final Date timeUpdate;
	private final ObmUser createdBy;
	private final ObmUser updatedBy;

	private final ObmDomain domain;
	private final boolean publicFreeBusy;

	private final String password;
	private final ProfileName profileName;
	private final Integer mailQuota;
	private final ObmHost mailHost;
	private final boolean archived;

	private final Integer uidNumber;
	private final Integer gidNumber;
	
	private final Set<Group> groups;

	private ObmUser(Integer uid, EntityId entityId, UserLogin login, UserExtId extId, boolean admin, UserIdentity identity,
			String email,
			Set<String> emailAlias, boolean hidden,
			UserAddress address, UserPhones phones, UserWork work,
			String description, Date timeCreate, Date timeUpdate,
			ObmUser createdBy, ObmUser updatedBy, ObmDomain domain,
			boolean publicFreeBusy, ProfileName profileName,
			Integer mailQuota, ObmHost mailHost, boolean archived, String password, Integer uidNumber, Integer gidNumber, Set<Group> groups) {
		this.uid = uid;
		this.entityId = entityId;
		this.login = login;
		this.extId = extId;
		this.admin = admin;
		this.identity = identity;
		this.email = email;
		this.emailAlias = emailAlias;
		this.hidden = hidden;
		this.address = address;
		this.phones = phones;
		this.work = work;
		this.description = description;
		this.timeCreate = timeCreate;
		this.timeUpdate = timeUpdate;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.domain = domain;
		this.publicFreeBusy = publicFreeBusy;
		this.profileName = profileName;
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
		return login.getStringValue();
	}

	public UserExtId getExtId() {
		return extId;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public UserIdentity getIdentity() {
		return identity;
	}
	
	public String getCommonName() {
		return identity.getCommonName();
	}
	
	public String getLastName() {
		return identity.getLastName();
	}

	public String getFirstName() {
		return identity.getFirstName();
	}

	public Set<String> getEmailAlias() {
		return emailAlias;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public UserAddress getAddress() {
		return address;
	}
	
	public String getAddress1() {
		return address.getAddress1();
	}

	public String getAddress2() {
		return address.getAddress2();
	}

	public String getAddress3() {
		return address.getAddress3();
	}

	public String getExpresspostal() {
		return address.getExpressPostal();
	}

	public UserPhones getPhones() {
		return phones;
	}
	
	public String getMobile() {
		return phones.getMobile();
	}

	public UserWork getWork() {
		return work;
	}
	
	public String getService() {
		return work.getService();
	}

	public String getTitle() {
		return work.getTitle();
	}

	public String getTown() {
		return address.getTown();
	}


	public String getZipCode() {
		return address.getZipCode();
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
		return identity.getDisplayName();
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
		return ImmutableSet.copyOf(address.getAddressParts());
	}
	
	public ProfileName getProfileName() {
		return profileName;
	}

	public String getKind() {
		return identity.getKind();
	}

	public String getCompany() {
		return work.getCompany();
	}

	public String getDirection() {
		return work.getDirection();
	}

	public String getCountryCode() {
		return address.getCountryCode();
	}

	public String getPhone() {
		return phones.getPhone1();
	}

	public String getPhone2() {
		return phones.getPhone2();
	}

	public String getFax() {
		return phones.getFax1();
	}

	public String getFax2() {
		return phones.getFax2();
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
		return Objects.hashCode(uid, entityId, login, extId, admin, identity, email,
				emailAlias, hidden, address, phones, work,
				description, createdBy, updatedBy, domain, publicFreeBusy, profileName,
				mailQuota, archived, mailHost, password, uidNumber, gidNumber, groups);
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
				&& Objects.equal(this.identity, that.identity)
				&& Objects.equal(this.email, that.email)
				&& Objects.equal(this.emailAlias, that.emailAlias)
				&& Objects.equal(this.hidden, that.hidden)
				&& Objects.equal(this.address, that.address)
				&& Objects.equal(this.phones, that.phones)
				&& Objects.equal(this.work, that.work)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.createdBy, that.createdBy)
				&& Objects.equal(this.updatedBy, that.updatedBy)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.publicFreeBusy, that.publicFreeBusy)
				&& Objects.equal(this.profileName, that.profileName)
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
			.add("identity", identity)
			.add("email", email)
			.add("emailAlias", emailAlias)
			.add("hidden", hidden)
			.add("address", address)
			.add("phones", phones)
			.add("work", work)
			.add("description", description)
			.add("timeCreate", timeCreate)
			.add("timeUpdate", timeUpdate)
			.add("createdBy", createdBy)
			.add("updatedBy", updatedBy)
			.add("domain", domain)
			.add("publicFreeBusy", publicFreeBusy)
			.add("profileName", profileName)
			.add("mailQuota", mailQuota)
			.add("mailHost", mailHost)
			.add("archived", archived)
			.add("uidNumber", uidNumber)
			.add("gidNumber", gidNumber)
			.add("groups", groups)
			.toString();
	}

}