/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.provisioning.bean;

import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.obm.provisioning.Configuration;

import com.google.common.base.Objects;
import com.google.inject.Inject;

public class LdapUserMembership {

	public static class Builder {
		private String memberUid;
		private String mailBox;
		
		private final Configuration configuration;

		@Inject
		private Builder(Configuration configuration) {
			this.configuration = configuration;
		}
		
		public Builder memberUid(String memberUid) {
			this.memberUid = memberUid;
			return this;
		}
		
		public Builder mailBox(String mailBox) {
			this.mailBox = mailBox;
			return this;
		}
		
		public LdapUserMembership build() {
			return new LdapUserMembership(memberUid, buildMember(), mailBox);
		}

		private String buildMember() {
			return "uid=" + memberUid + "," + configuration.getUserBaseDn().getName();
		}
	}
	
	private final String memberUid;
	private final String member;
	private final String mailBox;
	
	private LdapUserMembership(String memberUid, String member, String mailBox) {
		this.memberUid = memberUid;
		this.member = member;
		this.mailBox = mailBox;
	}
	
	public String getMemberUid() {
		return memberUid;
	}

	public String getMember() {
		return member;
	}

	public String getMailBox() {
		return mailBox;
	}

	public Modification[] buildAddModifications() {
		return buildModifications(ModificationOperation.ADD_ATTRIBUTE);
	}

	public Modification[] buildRemoveModifications() {
		return buildModifications(ModificationOperation.REMOVE_ATTRIBUTE);
	}

	private Modification[] buildModifications(ModificationOperation operation) {
		return new Modification[] {
				new DefaultModification(operation, "memberUid", getMemberUid()),
				new DefaultModification(operation, "member", getMember()),
				new DefaultModification(operation, "mailBox", getMailBox())
		};
	}


	@Override
	public final int hashCode(){
		return Objects.hashCode(memberUid, member, mailBox);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof LdapUserMembership) {
			LdapUserMembership that = (LdapUserMembership) object;
			return Objects.equal(this.memberUid, that.memberUid)
				&& Objects.equal(this.member, that.member)
				&& Objects.equal(this.mailBox, that.mailBox);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("memberUid", memberUid)
			.add("member", member)
			.add("mailBox", mailBox)
			.toString();
	}
}
