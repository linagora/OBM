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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ObmUser {
	
	public static Builder builder() {
		return new Builder();
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
		private UserEmails emails;
		private Boolean hidden;
		
		private String description;

		private Date timeCreate;
		private Date timeUpdate;
		private ObmUser createdBy;
		private ObmUser updatedBy;

		private ObmDomain domain;
		private boolean publicFreeBusy;

		private UserPassword password;
		private ProfileName profileName;
		private Boolean archived;

		private Integer uidNumber;
		private Integer gidNumber;

		private Date expirationDate;
		private String delegation;
		
		private final ImmutableSet.Builder<Group> groups;

		private Builder() {
			groups = ImmutableSet.builder();
		}

		public Builder from(ObmUser user) {
			return uid(user.uid)
					.login(user.login)
					.emails(user.emails)
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
					.archived(user.archived)
					.hidden(user.hidden)
					.timeCreate(user.timeCreate)
					.timeUpdate(user.timeUpdate)
					.createdBy(user.createdBy)
					.updatedBy(user.updatedBy)
					.uidNumber(user.uidNumber)
					.gidNumber(user.gidNumber)
					.groups(user.groups)
					.expirationDate(user.expirationDate)
					.delegation(user.delegation);
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
		public Builder emails(UserEmails emails) {
			this.emails = emails;
			return this;
		}

		public Builder archived(boolean archived) {
			this.archived = archived;
			return this;
		}

		public Builder password(UserPassword password) {
			this.password = password;
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
		public Builder expirationDate(Date expirationDate) {
			this.expirationDate =  expirationDate;
			return this;
		}
		public Builder delegation(String delegation) {
			this.delegation = delegation;
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
			archived = Objects.firstNonNull(archived, false);			
			hidden = Objects.firstNonNull(hidden, false);

			UserIdentity identity = Objects.firstNonNull(this.identity, UserIdentity.empty());
			UserAddress address = Objects.firstNonNull(this.address, UserAddress.empty());
			UserPhones phones = Objects.firstNonNull(this.phones, UserPhones.empty());
			UserWork work = Objects.firstNonNull(this.work, UserWork.empty());
			UserEmails emails = Objects.firstNonNull(this.emails, UserEmails.builder().domain(domain).build());
			
			return new ObmUser(
					uid, entityId, login, extId, admin, identity,
					hidden, address, phones, work, emails,
					description, timeCreate, timeUpdate, createdBy, updatedBy,
					domain, publicFreeBusy, profileName,
					archived, password, uidNumber, gidNumber, groups.build(), expirationDate, delegation);
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
	private final UserEmails emails;
	private final boolean hidden;
	
	private final String description;

	private final Date timeCreate;
	private final Date timeUpdate;
	private final ObmUser createdBy;
	private final ObmUser updatedBy;

	private final ObmDomain domain;
	private final boolean publicFreeBusy;

	private final UserPassword password;
	private final ProfileName profileName;
	private final boolean archived;

	private final Integer uidNumber;
	private final Integer gidNumber;
	
	private final Set<Group> groups;
	private final Date expirationDate;
	private final String delegation;

	private ObmUser(Integer uid, EntityId entityId, UserLogin login, UserExtId extId, boolean admin, UserIdentity identity,
			boolean hidden, UserAddress address, UserPhones phones, UserWork work, UserEmails emails,
			String description, Date timeCreate, Date timeUpdate,
			ObmUser createdBy, ObmUser updatedBy, ObmDomain domain,
			boolean publicFreeBusy, ProfileName profileName,
			boolean archived, UserPassword password, Integer uidNumber, Integer gidNumber, Set<Group> groups,  Date expirationDate, String delegation) {
		this.uid = uid;
		this.entityId = entityId;
		this.login = login;
		this.extId = extId;
		this.admin = admin;
		this.identity = identity;
		this.emails = emails;
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
		this.archived = archived;
		this.password = password;
		this.uidNumber = uidNumber;
		this.gidNumber = gidNumber;
		this.groups = groups;
		this.expirationDate = expirationDate;
		this.delegation = delegation;
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

	public List<String> getEmailAlias() {
		return emails.getAliases();
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
		return emails.getPrimaryAddress();
	}

	public String getEmailAtDomain() {
		return emails.getFullyQualifiedPrimaryAddress();
	}

	public Iterable<String> expandAllEmailDomainTuples() {
		return emails.expandAllEmailDomainTuples();
	}
	
	public String getLoginAtDomain() {
		return login.getStringValue() + "@" + domain.getName();
	}

	public String getDisplayName(){
		return identity.getDisplayName();
	}

	public UserEmails getUserEmails() {
		return emails;
	}
	
	public List<String> getEmails() {
		return emails.getAddresses();
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
		return emails.getQuota();
	}

	public ObmHost getMailHost() {
		return emails.getServer();
	}

	public boolean isArchived() {
		return archived;
	}

	public UserPassword getPassword() {
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
		return emails.isEmailAvailable();
	}

	public Date getExpirationDate() {
		return expirationDate;
	}
	
	public String getDelegation(){
		return delegation;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(uid, entityId, login, extId, admin, identity, emails,
				hidden, address, phones, work,
				description, createdBy, updatedBy, domain, publicFreeBusy, profileName,
				archived, password, uidNumber, gidNumber, groups, expirationDate, delegation);
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
				&& Objects.equal(this.emails, that.emails)
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
				&& Objects.equal(this.archived, that.archived)
				&& Objects.equal(this.password, that.password)
				&& Objects.equal(this.uidNumber, that.uidNumber)
				&& Objects.equal(this.gidNumber, that.gidNumber)
				&& Objects.equal(this.groups, that.groups)
				&& Objects.equal(this.expirationDate, that.expirationDate)
				&& Objects.equal(this.delegation, that.delegation);
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
			.add("emails", emails)
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
			.add("archived", archived)
			.add("uidNumber", uidNumber)
			.add("gidNumber", gidNumber)
			.add("groups", groups)
			.add("expirationDate", expirationDate)
			.add("delegation", delegation)
			.toString();
	}

}