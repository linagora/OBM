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
package org.obm.domain.dao;

import org.obm.metadata.DatabaseMetadataModule;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.BatchDaoJdbcImpl;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.OperationDao;
import org.obm.provisioning.dao.OperationDaoJdbcImpl;
import org.obm.provisioning.dao.PermissionDao;
import org.obm.provisioning.dao.PermissionDaoHardcodedImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;
import org.obm.sync.date.DateProvider;
import org.obm.utils.ObmHelper;

import com.google.inject.AbstractModule;

public class DaoModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new DatabaseMetadataModule());
		
		bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		bind(BatchDao.class).to(BatchDaoJdbcImpl.class);
		bind(OperationDao.class).to(OperationDaoJdbcImpl.class);
		bind(UserSystemDao.class).to(UserSystemDaoJdbcImpl.class);
		bind(DateProvider.class).to(ObmHelper.class);
		bind(PermissionDao.class).to(PermissionDaoHardcodedImpl.class);
		bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
		bind(EntityRightDao.class).to(EntityRightDaoJdbcImpl.class);
		bind(PUserDao.class).to(PUserDaoJdbcImpl.class);
		bind(PGroupDao.class).to(PGroupDaoJdbcImpl.class);

		bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
		bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
		bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
		bind(UserDao.class).to(UserDaoJdbcImpl.class);
		bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
		bind(CalendarDao.class).to(CalendarDaoJdbcImpl.class);
		bind(ContactDao.class).to(ContactDaoJdbcImpl.class);
		bind(CommitedOperationDao.class).to(CommitedOperationDaoJdbcImpl.class);
	}
}
