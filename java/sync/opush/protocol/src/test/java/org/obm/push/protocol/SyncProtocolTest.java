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
package org.obm.push.protocol;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class SyncProtocolTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(ProtocolModuleTest.class);
	
	@Inject CollectionPathHelper collectionPathHelper;

	private UserDataRequest udr;
	private String mailbox;
	private String password;

	@Before
	public void setUp() {
	    mailbox = "to@localhost.com";
	    password = "password";
	    udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
	}

	@Test
	public void testEncodeValidResponse() throws TransformerException, IOException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setNewSyncKey(new SyncKey("123456789"));
		collectionResponse.setCollectionValidity(true);
		
		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNoChangeResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseCollectionIdError() throws TransformerException, IOException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setCollectionValidity(false);

		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNotFoundResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseSyncKeyError() throws TransformerException, IOException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setCollectionValidity(true);
		collectionResponse.getSyncCollection().setStatus(SyncStatus.INVALID_SYNC_KEY);
		
		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newSyncKeyErrorResponse(collectionId));
	}
	private String newCollectionNoChangeResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>123456789</SyncKey>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>1</Status>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String newCollectionNotFoundResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>8</Status>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String newSyncKeyErrorResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>3</Status>" +
							"<SyncKey>0</SyncKey>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String encodeResponse(SyncCollectionResponse collectionResponse) throws TransformerException, IOException {
		Document endcodedResponse = new SyncProtocol(null).endcodeResponse(syncResponse(collectionResponse));
		return DOMUtils.serialize(endcodedResponse);
	}
	
	private SyncResponse syncResponse(SyncCollectionResponse collectionResponse) {
		return new SyncResponse(Sets.newHashSet(collectionResponse), udr, null, Collections.<String, String>emptyMap());
	}

	private SyncCollectionResponse newSyncCollectionResponse(int collectionId) {
		String collectionPath = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setSyncKey(new SyncKey("123456789"));
		SyncCollectionResponse collectionResponse = new SyncResponse.SyncCollectionResponse(syncCollection);
		collectionResponse.setItemChanges(Collections.<ItemChange>emptyList());
		collectionResponse.setItemChangesDeletion(Collections.<ItemDeletion>emptyList());
		return collectionResponse;
	}
	
	@Test(expected=NoDocumentException.class)
	public void testDecodeRequestWithNullDocument() throws Exception {
		Document request = null;
		
		SyncDecoder syncDecoder = createStrictMock(SyncDecoder.class);
		
		new SyncProtocol(syncDecoder).getRequest(request, udr);
	}

	@Test
	public void testDecodeRequestWithNullUserDataRequest() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>10</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncDecoder syncDecoder = createStrictMock(SyncDecoder.class);
		expect(syncDecoder.decodeSync(request, null)).andReturn(null);
		replay(syncDecoder);
		
		new SyncProtocol(syncDecoder).getRequest(request, null);

		verify(syncDecoder);
	}
	
	@Test
	public void testDecodeRequestCallsSyncDecoderWithExpectedArguments() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>10</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncDecoder syncDecoder = createStrictMock(SyncDecoder.class);
		expect(syncDecoder.decodeSync(request, udr)).andReturn(null);
		replay(syncDecoder);
		
		new SyncProtocol(syncDecoder).getRequest(request, udr);

		verify(syncDecoder);
	}
	
}
