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
package org.obm.push.dao.testsuite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.dateUTC;

import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class FolderSyncStateBackendMappingDaoTest {

	public static final ImmutableList<Date> EXPECTED_DATE_VALUES = ImmutableList.of(
			dateUTC("2012-05-04T10:00:00"),
			dateUTC("2012-05-05T11:00:00"),
			dateUTC("2012-05-06T12:00:00"),
			dateUTC("2012-05-07T13:00:00")); 
	
	@Inject FolderSyncStateBackendMappingDao folderStateDao;
	@Inject CollectionDao collectionDao;
	
	private Device device;

	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test(expected=DaoException.class)
	public void testCreateMappingWhenNonExistingFolderState() {
		FolderSyncState folderSyncState = FolderSyncState.builder().id(4).syncKey(new SyncKey("123")).build();
		folderStateDao.createMapping(PIMDataType.EMAIL, folderSyncState);
	}
	
	@Test
	public void testCreateMapping() {
		FolderSyncState folderSyncState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));
		
		folderStateDao.createMapping(PIMDataType.EMAIL, folderSyncState);
		folderStateDao.createMapping(PIMDataType.CALENDAR, folderSyncState);
		folderStateDao.createMapping(PIMDataType.CONTACTS, folderSyncState);
		
		assertThat(folderStateDao.getLastSyncDate(PIMDataType.EMAIL, folderSyncState)).isEqualTo(EXPECTED_DATE_VALUES.get(0));
		assertThat(folderStateDao.getLastSyncDate(PIMDataType.CALENDAR, folderSyncState)).isEqualTo(EXPECTED_DATE_VALUES.get(1));
		assertThat(folderStateDao.getLastSyncDate(PIMDataType.CONTACTS, folderSyncState)).isEqualTo(EXPECTED_DATE_VALUES.get(2));
		assertThat(folderStateDao.getLastSyncDate(PIMDataType.TASKS, folderSyncState)).isNull();
	}
	
	@Test
	public void testGetLastSyncDateWhenMappingOverriding() {
		FolderSyncState folderSyncState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));

		folderStateDao.createMapping(PIMDataType.EMAIL, folderSyncState);
		folderStateDao.createMapping(PIMDataType.EMAIL, folderSyncState);
		folderStateDao.createMapping(PIMDataType.EMAIL, folderSyncState);
		
		assertThat(folderStateDao.getLastSyncDate(PIMDataType.EMAIL, folderSyncState)).isEqualTo(EXPECTED_DATE_VALUES.get(2));
	}
}
