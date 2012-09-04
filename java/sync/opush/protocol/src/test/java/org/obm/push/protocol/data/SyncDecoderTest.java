/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.protocol.data;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

@RunWith(SlowFilterRunner.class)
public class SyncDecoderTest {

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testGetWaitWhenNotANumber() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Sync>" +
					"<Wait>a10</Wait>" + 
					"<Collections>" +
						"<Collection>" + 
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" + 
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		new SyncDecoder().getWait(request.getDocumentElement());
	}

	@Test
	public void testGetWaitWhenJustTagReturnNull() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait/>" + 
					"<Collections>" + 
						"<Collection>" + 
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" + 
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		Integer wait = new SyncDecoder().getWait(request.getDocumentElement());
		
		assertThat(wait).isNull();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testGetWaitWhenEmpty() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait> </Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		new SyncDecoder().getWait(request.getDocumentElement());
	}
	
	@Test
	public void testGetWaitWhenNotReturnNull() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Integer wait = new SyncDecoder().getWait(request.getDocumentElement());

		assertThat(wait).isNull();
	}

	@Test
	public void testGetWaitWhen0() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>0</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		int wait = new SyncDecoder().getWait(request.getDocumentElement());

		assertThat(wait).isEqualTo(0);
	}

	@Test
	public void testGetWaitWhen1000() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>1000</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		int wait = new SyncDecoder().getWait(request.getDocumentElement());

		assertThat(wait).isEqualTo(1000);
	}
}
