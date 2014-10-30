/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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
package org.obm.imap.archive.authentication;

import java.util.List;

import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class AuthorizationServiceImpl implements AuthorizationService {

	private final UserDao userDao;
	private final DomainDao domainDao;
	
	@Inject
	@VisibleForTesting AuthorizationServiceImpl(UserDao userDao, DomainDao domainDao) {
		this.userDao = userDao;
		this.domainDao = domainDao;
	}
	
	@Override
	public List<Authorization> getRoles(String login, String domainName) {
		ObmDomain domain = domainDao.findDomainByName(domainName);
		ObmUser user = userDao.findUserByLogin(login, domain);
		if (user == null) {
			return ImmutableList.of(Authorization.NONE);
		}
		
		ImmutableList.Builder<Authorization> builder = ImmutableList.<Authorization> builder()
				.add(Authorization.NONE)
				.add(Authorization.USER);
		
		if (user.isAdmin()) {
			builder.add(Authorization.ADMIN);
		}
		return builder.build();
	}
}
