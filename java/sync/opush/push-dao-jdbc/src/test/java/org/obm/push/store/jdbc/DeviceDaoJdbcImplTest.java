/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.store.jdbc;

import java.sql.ResultSet;

import org.junit.Rule;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.push.bean.Device;
import org.obm.push.bean.User;
import org.obm.push.dao.testsuite.DeviceDaoTest;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;

import com.google.inject.Inject;

@GuiceModule(DeviceDaoJdbcImplTest.Env.class)
public class DeviceDaoJdbcImplTest extends DeviceDaoTest implements H2TestClass {

	public static class Env extends DaoTestModule {
		@Override
		protected void configureImpl() {
			bind(DeviceDao.class).to(DeviceDaoJdbcImpl.class);
		}
	}
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initialDeviceSchema.sql");

	@Inject H2InMemoryDatabase db;
	
	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}

	@Override
	protected void createUnknownDeviceSyncPerm(User user, Device device, PolicyStatus policyStatus) throws Exception {
		db.executeUpdate(
				"INSERT INTO opush_sync_perms (policy, device_id, owner, pending_accept) " +
				"VALUES (NULL, " + device.getDatabaseId() + ", " + getUserDatabaseId(user) + ", " + status(policyStatus) + ")");
	}

	@Override
	protected boolean userHasUnknownDeviceSyncPerm(User user) throws Exception {
		return db
			.execute(
				"SELECT owner, device_id, policy FROM opush_sync_perms " +
				"WHERE owner=? AND policy IS NULL", getUserDatabaseId(user))
			.next();
	}

	private int getUserDatabaseId(User user) throws Exception {
		ResultSet resultSet = db.execute(
				"SELECT userobm_id FROM userobm " +
				"INNER JOIN Domain ON userobm_domain_id=domain_id " +
				"WHERE userobm_login=? AND domain_name=?", user.getLogin(), user.getDomain());
		resultSet.next();
		return resultSet.getInt("userobm_id");
	}

	private boolean status(PolicyStatus policyStatus) {
		return DeviceDaoJdbcImpl.policyStatusToPendingAccept(policyStatus);
	}

}
