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
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ObmUser {
	
	public static final String EMAIL_FIELD_SEPARATOR = "\r\n";

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		
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
		
		private Builder() {
			super();
		}
		
		public Builder uid(int uid) {
			this.uid = uid;
			return this;
		}
		public Builder entityId(int entityId) {
			this.entityId = entityId;
			return this;
		}
		public Builder login(String login) {
			this.login = login;
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
		public Builder homePhone(String homePhone) {
			this.homePhone = homePhone;
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
		public Builder workFax(String workFax) {
			this.workFax = workFax;
			return this;
		}
		public Builder workPhone(String workPhone) {
			this.workPhone = workPhone;
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

		public Builder emailAndAliases(String emailAndAliases) {
			email = null;
			emailAlias = Sets.newHashSet();
			Iterable<String> emailAndAlias = Splitter.on(EMAIL_FIELD_SEPARATOR).split(emailAndAliases);
			for (String emailOrAlias: emailAndAlias) {
				if (email == null) {
					email = emailOrAlias;	
				} else {
					emailAlias.add(emailOrAlias);
				}			
			}
			return this;
		}
		
		public ObmUser build() {
			Preconditions.checkNotNull(uid);
			Preconditions.checkNotNull(login);
			Preconditions.checkNotNull(domain);
			
			return new ObmUser(uid, entityId, login, commonName, lastName, firstName, email, emailAlias,
					address1, address2, address3, expresspostal, homePhone, mobile, service, title, town,
					workFax, workPhone, zipCode, description, timeCreate, timeUpdate, createdBy, updatedBy,
					domain, publicFreeBusy);
		}
		
	}
	
	private final int uid;
	private final int entityId;
	private final String login;
	private final String commonName;
	private final String lastName;
	private final String firstName;
	private final String email;
	private final Set<String> emailAlias;
	
	private final String address1;
	private final String address2;
	private final String address3;

	private final String expresspostal;
	private final String homePhone;
	private final String mobile;
	private final String service;
	private final String title;
	private final String town;
	private final String workFax;
	private final String workPhone;
	private final String zipCode;
	private final String description;

	private final Date timeCreate;
	private final Date timeUpdate;
	private final ObmUser createdBy;
	private final ObmUser updatedBy;

	private final ObmDomain domain;
	private final boolean publicFreeBusy;

	private ObmUser(int uid, int entityId, String login, String commonName,
			String lastName, String firstName, String email,
			Set<String> emailAlias, String address1, String address2,
			String address3, String expresspostal, String homePhone,
			String mobile, String service, String title, String town,
			String workFax, String workPhone, String zipCode,
			String description, Date timeCreate, Date timeUpdate,
			ObmUser createdBy, ObmUser updatedBy, ObmDomain domain,
			boolean publicFreeBusy) {
		this.uid = uid;
		this.entityId = entityId;
		this.login = login;
		this.commonName = commonName;
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.emailAlias = emailAlias;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.expresspostal = expresspostal;
		this.homePhone = homePhone;
		this.mobile = mobile;
		this.service = service;
		this.title = title;
		this.town = town;
		this.workFax = workFax;
		this.workPhone = workPhone;
		this.zipCode = zipCode;
		this.description = description;
		this.timeCreate = timeCreate;
		this.timeUpdate = timeUpdate;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.domain = domain;
		this.publicFreeBusy = publicFreeBusy;
	}

	public int getUid() {
		return uid;
	}

	public int getEntityId() {
		return entityId;
	}

	public String getLogin() {
		return login;
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

	public String getHomePhone() {
		return homePhone;
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

	public String getWorkFax() {
		return workFax;
	}

	public String getWorkPhone() {
		return workPhone;
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
	public final int hashCode() {
		return Objects.hashCode(uid, entityId, login, commonName, lastName, firstName, email,
				emailAlias, address1, address2, address3, expresspostal, homePhone, mobile,
				service, title, town, workFax, workPhone, zipCode,	description, timeCreate,
				timeUpdate, createdBy, updatedBy, domain, publicFreeBusy);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof ObmUser) {
			ObmUser that = (ObmUser) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.entityId, that.entityId)
				&& Objects.equal(this.login, that.login)
				&& Objects.equal(this.commonName, that.commonName)
				&& Objects.equal(this.lastName, that.lastName)
				&& Objects.equal(this.firstName, that.firstName)
				&& Objects.equal(this.email, that.email)
				&& Objects.equal(this.emailAlias, that.emailAlias)
				&& Objects.equal(this.address1, that.address1)
				&& Objects.equal(this.address2, that.address2)
				&& Objects.equal(this.address3, that.address3)
				&& Objects.equal(this.expresspostal, that.expresspostal)
				&& Objects.equal(this.homePhone, that.homePhone)
				&& Objects.equal(this.mobile, that.mobile)
				&& Objects.equal(this.service, that.service)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.town, that.town)
				&& Objects.equal(this.workFax, that.workFax)
				&& Objects.equal(this.workPhone, that.workPhone)
				&& Objects.equal(this.zipCode, that.zipCode)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.timeCreate, that.timeCreate)
				&& Objects.equal(this.timeUpdate, that.timeUpdate)
				&& Objects.equal(this.createdBy, that.createdBy)
				&& Objects.equal(this.updatedBy, that.updatedBy)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.publicFreeBusy, that.publicFreeBusy);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("entityId", entityId)
			.add("login", login)
			.add("commonName", commonName)
			.add("lastName", lastName)
			.add("firstName", firstName)
			.add("email", email)
			.add("emailAlias", emailAlias)
			.add("address1", address1)
			.add("address2", address2)
			.add("address3", address3)
			.add("expresspostal", expresspostal)
			.add("homePhone", homePhone)
			.add("mobile", mobile)
			.add("service", service)
			.add("title", title)
			.add("town", town)
			.add("workFax", workFax)
			.add("workPhone", workPhone)
			.add("zipCode", zipCode)
			.add("description", description)
			.add("timeCreate", timeCreate)
			.add("timeUpdate", timeUpdate)
			.add("createdBy", createdBy)
			.add("updatedBy", updatedBy)
			.add("domain", domain)
			.add("publicFreeBusy", publicFreeBusy)
			.toString();
	}
}