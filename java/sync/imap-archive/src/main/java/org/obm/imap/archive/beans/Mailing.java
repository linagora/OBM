/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.beans;

import java.util.List;

import org.obm.sync.base.EmailAddress;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public class Mailing {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Mailing empty() {
		return new Builder().build();
	}
	
	public static Mailing from(List<EmailAddress> emailAddresses) {
		return new Builder().addAll(emailAddresses).build();
	}
	
	public static Mailing fromStrings(List<String> mailingEmails) {
		Preconditions.checkNotNull(mailingEmails);
		return Mailing.from(FluentIterable.from(mailingEmails)
				.transform(new Function<String, EmailAddress>() {

					@Override
					public EmailAddress apply(String emailAddress) {
						return EmailAddress.loginAtDomain(emailAddress);
					}
				}).toList());
	}
	
	public static class Builder {
		
		private ImmutableList.Builder<EmailAddress> emailAddresses;
		
		private Builder() {
			emailAddresses = ImmutableList.builder();
		}
		
		public Builder add(EmailAddress emailAddress) {
			Preconditions.checkNotNull(emailAddress);
			this.emailAddresses.add(emailAddress);
			return this;
		}
		
		public Builder addAll(List<EmailAddress> emailAddresses) {
			this.emailAddresses.addAll(emailAddresses);
			return this;
		}
		
		public Mailing build() {
			return new Mailing(emailAddresses.build());
		}
	}
	
	private final List<EmailAddress> emailAddresses;
	
	private Mailing(List<EmailAddress> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
	
	public List<EmailAddress> getEmailAddresses() {
		return emailAddresses;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(emailAddresses);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Mailing) {
			Mailing that = (Mailing) object;
			return Objects.equal(this.emailAddresses, that.emailAddresses);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("emailAddresses", emailAddresses)
			.toString();
	}
}
