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
package org.obm.push.protocol.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.client.SyncClientCommands.Add;
import org.obm.push.bean.change.item.ItemChange;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(SlowFilterRunner.class)
public class SyncResponseTest {
	
	@Test
	public void testEmptyProcessClientIds() {
		SyncResponse syncResponse = SyncResponse.builder()
				.addResponse(SyncCollectionResponse.builder()
						.collectionId(1)
						.build())
				.status(SyncStatus.OK)
				.build();
		
		assertThat(syncResponse.getProcessedClientIds()).isEmpty();
	}
	
	@Test
	public void testNotEmptyProcessClientIds() {
		String serverId = "123";
		String clientId = "456";
		SyncResponse syncResponse = SyncResponse.builder()
				.addResponse(SyncCollectionResponse.builder()
						.collectionId(1)
						.responses(SyncCollectionCommands.Response.builder()
								.changes(ImmutableList.of(new ItemChange(serverId)), 
										SyncClientCommands.builder()
											.putAdd(new Add(clientId , serverId))
											.build())
								.build())
						.build())
				.status(SyncStatus.OK)
				.build();
		
		assertThat(syncResponse.getProcessedClientIds()).isEqualTo(ImmutableMap.of(serverId, clientId));
	}
}
