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

import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.AddressBookDaoJdbcImpl;
import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.ObmInfoDaoJdbcImpl;
import org.obm.domain.dao.UserDaoJdbcImpl;
import org.obm.domain.dao.UserPatternDao;
import org.obm.domain.dao.UserPatternDaoJdbcImpl;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ArchiveTreatmentJdbcImpl;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl;
import org.obm.imap.archive.dao.ImapFolderDao;
import org.obm.imap.archive.dao.ImapFolderJdbcImpl;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.dao.ProcessedFolderJdbcImpl;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;

import com.google.inject.AbstractModule;

public class DaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DomainConfigurationDao.class).to(DomainConfigurationJdbcImpl.class);
		bind(ArchiveTreatmentDao.class).to(ArchiveTreatmentJdbcImpl.class);
		bind(ImapFolderDao.class).to(ImapFolderJdbcImpl.class);
		bind(ProcessedFolderDao.class).to(ProcessedFolderJdbcImpl.class);
		install(new ObmDaoModule());
	}
	
	private static class ObmDaoModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(DomainDao.class);
			bind(org.obm.domain.dao.UserDao.class).to(UserDaoJdbcImpl.class);
			bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
			bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
			bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
			bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
			bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		}
	}
}
