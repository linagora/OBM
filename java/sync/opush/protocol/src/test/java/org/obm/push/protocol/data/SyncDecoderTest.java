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
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.ASRequestBooleanFieldException;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.ASRequestStringFieldException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequestCollection;
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

	@Test(expected=ASRequestBooleanFieldException.class)
	public void testPartialWhenNotBoolean() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial>yeah</Partial>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		new SyncDecoder().isPartial(request.getDocumentElement());
	}

	@Test
	public void testPartialWhenNotPresent() throws Exception {
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
		
		Boolean isPartial = new SyncDecoder().isPartial(request.getDocumentElement());

		assertThat(isPartial).isNull();
	}

	@Test
	public void testPartialWhenEmptyIsTrue() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial/>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Boolean isPartial = new SyncDecoder().isPartial(request.getDocumentElement());

		assertThat(isPartial).isTrue();
	}

	@Test
	public void testPartialFalse() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial>0</Partial>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Boolean isPartial = new SyncDecoder().isPartial(request.getDocumentElement());

		assertThat(isPartial).isFalse();
	}

	@Test
	public void testPartialTrue() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial>1</Partial>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Boolean isPartial = new SyncDecoder().isPartial(request.getDocumentElement());

		assertThat(isPartial).isTrue();
	}

	@Test
	public void testWindowSizeByDefault() throws Exception {
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
		
		Integer windowSize = new SyncDecoder().getWindowSize(request.getDocumentElement());

		assertThat(windowSize).isNull();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testWindowSizeWhenNotANumber() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<WindowSize>a1</WindowSize>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Integer windowSize = new SyncDecoder().getWindowSize(request.getDocumentElement());

		assertThat(windowSize).isNull();
	}

	@Test
	public void testWindowSize() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<WindowSize>150</WindowSize>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		
		Integer windowSize = new SyncDecoder().getWindowSize(request.getDocumentElement());

		assertThat(windowSize).isEqualTo(150);
	}

	@Test
	public void testCollectionWhenNotPresent() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
					"</Collections>" +
				"</Sync>");
		
		SyncRequest syncRequest = new SyncDecoder().decodeSync(request);

		assertThat(syncRequest.getCollections()).isEmpty();
	}

	@Test
	public void testCollectionNoOptionNoCommand() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Class>Email</Class>" +
					"<WindowSize>150</WindowSize>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getSyncKey()).isEqualTo(new SyncKey("ddcf2e35-9834-49de-96ff-09979c7e2aa0"));
		assertThat(collection.getId()).isEqualTo(2);
		assertThat(collection.getDataClass()).isEqualTo("Email");
		assertThat(collection.getWindowSize()).isEqualTo(150);
	}

	@Test(expected=ASRequestStringFieldException.class)
	public void testCollectionWhenNoSyncKey() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<CollectionId>2</CollectionId>" +
					"<Class>Email</Class>" +
					"<WindowSize>150</WindowSize>" +
				"</Collection>");
		
		new SyncDecoder().getCollection(request.getDocumentElement());
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testCollectionWhenNoId() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<Class>Email</Class>" +
					"<WindowSize>150</WindowSize>" +
				"</Collection>");
		
		new SyncDecoder().getCollection(request.getDocumentElement());
	}

	@Test
	public void testCollectionWhenNoClass() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<WindowSize>150</WindowSize>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());
		
		assertThat(collection.getDataClass()).isNull();
	}

	@Test
	public void testCollectionWhenNoWindowSize() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Class>Email</Class>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());
		
		assertThat(collection.getWindowSize()).isNull();
	}

	@Test
	public void testCollectionOptionsWhenNot() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions()).isNull();
		assertThat(collection.hasOptions()).isFalse();
	}

	@Test
	public void testCollectionOptions() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<FilterType>5</FilterType>" +
						"<Conflict>1</Conflict>" +
						"<MIMETruncation>8</MIMETruncation>" +
						"<MIMESupport>2</MIMESupport>" +
						"<BodyPreference>" +
							"<Type>4</Type>" +
							"<TruncationSize>1000</TruncationSize>" +
							"<AllOrNone>1</AllOrNone>" +
						"</BodyPreference>" +
						"<BodyPreference>" +
							"<Type>2</Type>" +
							"<TruncationSize>5000</TruncationSize>" +
							"<AllOrNone>0</AllOrNone>" +
						"</BodyPreference>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getFilterType()).isEqualTo(FilterType.ONE_MONTHS_BACK);
		assertThat(collection.getOptions().getConflict()).isEqualTo(1);
		assertThat(collection.getOptions().getMimeTruncation()).isEqualTo(8);
		assertThat(collection.getOptions().getMimeSupport()).isEqualTo(2);
		assertThat(collection.getOptions().getBodyPreferences()).containsOnly(
				BodyPreference.builder().bodyType(MSEmailBodyType.MIME).truncationSize(1000).allOrNone(true).build(),
				BodyPreference.builder().bodyType(MSEmailBodyType.HTML).truncationSize(5000).allOrNone(false).build());
	}

	@Test
	public void testCollectionOptionsFilterTypeIsNotRequired() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<Conflict>1</Conflict>" +
						"<MIMETruncation>8</MIMETruncation>" +
						"<MIMESupport>2</MIMESupport>" +
						"<BodyPreference>" +
							"<Type>4</Type>" +
							"<TruncationSize>1000</TruncationSize>" +
							"<AllOrNone>1</AllOrNone>" +
						"</BodyPreference>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getFilterType()).isNull();
	}

	@Test
	public void testCollectionOptionsConflictIsNotRequired() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<FilterType>5</FilterType>" +
						"<MIMETruncation>8</MIMETruncation>" +
						"<MIMESupport>2</MIMESupport>" +
						"<BodyPreference>" +
							"<Type>4</Type>" +
							"<TruncationSize>1000</TruncationSize>" +
							"<AllOrNone>1</AllOrNone>" +
						"</BodyPreference>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getConflict()).isNull();
	}

	@Test
	public void testCollectionOptionsMimeTruncationIsNotRequired() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<FilterType>5</FilterType>" +
						"<Conflict>1</Conflict>" +
						"<MIMESupport>2</MIMESupport>" +
						"<BodyPreference>" +
							"<Type>4</Type>" +
							"<TruncationSize>1000</TruncationSize>" +
							"<AllOrNone>1</AllOrNone>" +
						"</BodyPreference>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getMimeTruncation()).isNull();
	}

	@Test
	public void testCollectionOptionsMimeSupportIsNotRequired() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<FilterType>5</FilterType>" +
						"<Conflict>1</Conflict>" +
						"<MIMETruncation>8</MIMETruncation>" +
						"<BodyPreference>" +
							"<Type>4</Type>" +
							"<TruncationSize>1000</TruncationSize>" +
							"<AllOrNone>1</AllOrNone>" +
						"</BodyPreference>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getMimeSupport()).isNull();
	}

	@Test
	public void testCollectionOptionsBodyPreferenceIsNotRequired() throws Exception {
		Document request = DOMUtils.parse(
				"<Collection>" +
					"<SyncKey>ddcf2e35-9834-49de-96ff-09979c7e2aa0</SyncKey>" +
					"<CollectionId>2</CollectionId>" +
					"<Options>" +
						"<FilterType>5</FilterType>" +
						"<Conflict>1</Conflict>" +
						"<MIMETruncation>8</MIMETruncation>" +
						"<MIMESupport>2</MIMESupport>" +
					"</Options>" +
				"</Collection>");
		
		SyncRequestCollection collection = new SyncDecoder().getCollection(request.getDocumentElement());

		assertThat(collection.getOptions().getBodyPreferences()).isEmpty();
	}
}
