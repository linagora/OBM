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
package org.obm.provisioning.ldap.client.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.ldap.client.EmbeddedLdapModule;
import org.obm.provisioning.ldap.client.bean.LdapGroup.Cn;

import com.google.inject.Inject;

@GuiceModule(EmbeddedLdapModule.class)
@RunWith(GuiceRunner.class)
public class LdapGroupTest {
	
	@Inject LdapGroup.Builder ldapGroupBuilder;
	
	@Test
	public void testBuildModification() {
		LdapGroup.Builder builder = ldapGroupBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson"})
				.cn(Cn.valueOf("cn"))
				.gidNumber(1)
				.mailAccess("mailAccess")
				.mail("mail")
				.domain(LdapDomain.valueOf("domain"));
		
		LdapGroup oldGroup = builder.build();
		LdapGroup newGroup = builder
				.cn(Cn.valueOf("newCn"))
				.gidNumber(2)
				.mailAccess("newMailAccess")
				.mail("newMail")
				.domain(LdapDomain.valueOf("newDomain"))
				.build();
		
		assertThat(newGroup.buildDiffModifications(oldGroup)).isEqualTo(new Modification[] {
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", "newCn"),
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "gidNumber", "2"),
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mailAccess", "newMailAccess"),
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mail", "newMail"),
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "obmDomain", "newDomain")
		});
	}
	
	@Test
	public void testBuildNoModification() {
		LdapGroup.Builder builder = ldapGroupBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson"})
				.cn(Cn.valueOf("cn"))
				.gidNumber(1)
				.mailAccess("mailAccess")
				.mail("mail")
				.domain(LdapDomain.valueOf("domain"));
		
		LdapGroup oldGroup = builder.build();
		LdapGroup newGroup = builder.build();
		
		assertThat(newGroup.buildDiffModifications(oldGroup)).isEqualTo(new Modification[0]);
	}
}
