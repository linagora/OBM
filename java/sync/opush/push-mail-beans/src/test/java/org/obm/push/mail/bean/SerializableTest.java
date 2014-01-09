package org.obm.push.mail.bean;
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncKey;
import org.obm.push.utils.DateUtils;


public class SerializableTest {

	private ObjectOutputStream objectOutputStream;


	@Before
	public void buildOutputStream() throws IOException {
		objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());	
	}

	@Test
	public void testEmail() throws IOException {
		Email email = Email.builder()
				.uid(1)
				.read(true)
				.date(DateUtils.getCurrentDate())
				.answered(true)
				.build();
		objectOutputStream.writeObject(email);
	}
	
	@Test
	public void testSnapshot() throws IOException {
		Snapshot snapshot = Snapshot.builder()
				.collectionId(1)
				.deviceId(new DeviceId("deviceId"))
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(new SyncKey("syncKey"))
				.uidNext(2)
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.answered(true)
						.build())
				.build();
		objectOutputStream.writeObject(snapshot);
	}
	
	@Test
	public void testDeviceId() throws IOException {
		DeviceId deviceId = new DeviceId("deviceId");
		objectOutputStream.writeObject(deviceId);
	}

	@Test
	public void testSyncKey() throws IOException {
		SyncKey syncKey = new SyncKey("syncKey");
		objectOutputStream.writeObject(syncKey);
	}

}
