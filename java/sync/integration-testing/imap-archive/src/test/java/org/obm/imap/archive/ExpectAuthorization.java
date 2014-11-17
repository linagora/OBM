/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.sql.SQLException;

import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;
import org.obm.imap.archive.beans.ExcludedUser;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserLogin;

public class ExpectAuthorization {
	
	public static void expectAdmin(DomainDao domainDao, String domainName, UserDao userDao, String login) {
		ObmDomain domain = expectDomain(domainDao, domainName);
		
		expect(userDao.findUserByLogin(login, domain))
			.andReturn(ObmUser.builder()
					.extId(UserExtId.valueOf("d4ad341d-89eb-4f3d-807a-cb372314845d"))
					.login(UserLogin.valueOf(login))
					.domain(domain)
					.admin(true)
					.build()).anyTimes();
	}
	
	public static void expectCheckUsers(UserDao userDao, String domainName, ExcludedUser... users) throws SQLException, UserNotFoundException {
		ObmDomain domain = ObmDomain.builder().name(domainName).build();
		for (ExcludedUser user : users) {
			expect(userDao.getByExtId(eq(user.getId()), anyObject(ObmDomain.class)))
				.andReturn(ObmUser.builder()
						.extId(user.getId())
						.login(UserLogin.valueOf(user.getLogin()))
						.domain(domain)
						.build());
		}
	}
	
	public static void expectSimpleUser(DomainDao domainDao, String domainName, UserDao userDao, String login) {
		ObmDomain domain = expectDomain(domainDao, domainName);
		
		expect(userDao.findUserByLogin(login, domain))
			.andReturn(ObmUser.builder()
					.extId(UserExtId.valueOf("d4ad341d-89eb-4f3d-807a-cb372314845d"))
					.login(UserLogin.valueOf(login))
					.domain(domain)
					.build()).anyTimes();
	}

	private static ObmDomain expectDomain(DomainDao domainDao, String domainName) {
		ObmDomain domain = ObmDomain.builder().name(domainName).build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(domain).anyTimes();
		return domain;
	}

}
