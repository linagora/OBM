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
package org.obm.opush;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationTestUtils.buildCalendarCollectionPath;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.TransformerException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.ConfigurationModule.PolicyConfigurationProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.DefaultOpushModule;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HeartbeatDao;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(DefaultOpushModule.class)
public class PingHandlerTest {

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	@Inject PolicyConfigurationProvider policyConfigurationProvider;
	
	private List<OpushUser> fakeTestUsers;
	private int pingOnCollectionId;
	private CloseableHttpClient httpClient;

	@Before
	public void init() {
		httpClient = HttpClientBuilder.create().build();
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
		pingOnCollectionId = 1432;

		expect(policyConfigurationProvider.get()).andReturn("fakeConfiguration");
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		httpClient.close();
		Files.delete(configuration.dataDir);
	}

	@Test
	@Ignore("OBMFULL-4125")
	public void testInterval() throws Exception {
		testHeartbeatInterval(5, 5, 5);
	}


	@Test
	@Ignore("OBMFULL-4125")
	public void testMinInterval() throws Exception {
		testHeartbeatInterval(1, 5, 5);
	}

	@Test
	@Ignore("OBMFULL-5442")
	public void testNoChange() throws Exception {
		prepareMockNoChange();

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		Document document = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
				"<HeartbeatInterval>5</HeartbeatInterval>" +
					"<Folders>" +
						"<Folder>" +
							"<Id>1</Id>" +
							"<Class>Calendar</Class>" +
						"</Folder>" +
						"<Folder>" +
							"<Id>4</Id>" +
							"<Class>Contacts</Class>" +
						"</Folder>" +
					"</Folders>" +
				"</Ping>");
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(2, 5, stopwatch);
		assertThat(DOMUtils.serialize(response))
			.isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Status>2</Status>" +
				"</Ping>");
		
	}
	
	@Test
	@Ignore("OBMFULL-4125")
	public void test3BlockingClient() throws Exception {
		prepareMockNoChange();

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		
		ThreadPoolExecutor threadPoolExecutor = 
				new ThreadPoolExecutor(20, 20, 1,TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

		Stopwatch stopwatch = Stopwatch.createStarted();
		
		List<Future<Document>> futures = new ArrayList<Future<Document>>();
		for (int i = 0; i < 4; ++i) {
			 futures.add(queuePingCommand(opClient, threadPoolExecutor));	
		}
		
		for (Future<Document> f: futures) {
			Document response = f.get();
			checkNoChangeResponse(response);
		}
		
		checkExecutionTime(2, 5, stopwatch);
	}

	@Test
	@Ignore("OBMFULL-4125")
	public void testPushNotificationOnBackendChangeShort() throws Exception {
		prepareMockHasChanges(1);

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		Document document = buildPingCommand(20);
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(5, 1, stopwatch);
		checkHasChangeResponse(response);
	}

	@Test
	@Ignore("OBMFULL-4125")
	public void testPushNotificationOnBackendChangeLong() throws Exception {
		prepareMockHasChanges(2);

		opushServer.start();

		Document document = buildPingCommand(20);
		Stopwatch stopwatch = Stopwatch.createStarted();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(5, 6, stopwatch);
		checkHasChangeResponse(response);
	}

	@Test
	@Ignore("OBMFULL-4125")
	public void testPushNotificationOnBackendHierarchyChangedException() throws Exception {
		prepareMockHierarchyChangedException();

		opushServer.start();

		Document document = buildPingCommand(20);

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkFolderSyncRequiredResponse(response);
	}
	
	private void prepareMockNoChange() throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, AuthFault, ConversionException, HierarchyChangedException {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockForPingNeeds();
		mockForNoChangePing();
		mocksControl.replay();
	}

	private void prepareMockHasChanges(int noChangeIterationCount) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, AuthFault, ConversionException, HierarchyChangedException {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockForPingNeeds();
		mockForCalendarHasChangePing(noChangeIterationCount);
		mocksControl.replay();
	}

	private void prepareMockHierarchyChangedException() throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, AuthFault, ConversionException, HierarchyChangedException {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockForPingNeeds();
		mockForCalendarHierarchyChangedException();
		mocksControl.replay();
	}
	
	public void testHeartbeatInterval(int heartbeatInterval, int delta, int expected) throws Exception {
		prepareMockNoChange();
		
		opushServer.start();
		
		Document document = buildPingCommand(heartbeatInterval);
		Stopwatch stopwatch = Stopwatch.createStarted();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort(), httpClient);
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(delta, expected, stopwatch);
		checkNoChangeResponse(response);
	}

	private void checkExecutionTime(int delta, int expected,
			Stopwatch stopwatch) {
		stopwatch.stop();
		long elapsedTime = stopwatch.elapsed(TimeUnit.SECONDS);
		assertThat(elapsedTime)
			.isGreaterThanOrEqualTo(expected)
			.isLessThan(expected + delta);
	}

	private void checkNoChangeResponse(Document response)
			throws TransformerException {
		assertThat(DOMUtils.serialize(response))
			.isEqualTo(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<Ping><Status>1</Status><Folders/></Ping>");
	}
	
	private void checkHasChangeResponse(Document response) throws TransformerException {
		assertThat(DOMUtils.serialize(response))
			.isEqualTo(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Ping>" +
					"<Status>2</Status>" +
					"<Folders><Folder>1432</Folder></Folders></Ping>");
	}
	
	private void checkFolderSyncRequiredResponse(Document response) throws TransformerException {
		assertThat(DOMUtils.serialize(response))
			.isEqualTo(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Ping>" +
					"<Status>7</Status>" +
					"</Ping>");
	}

	private void mockForPingNeeds() throws DaoException {
		HeartbeatDao heartbeatDao = classToInstanceMap.get(HeartbeatDao.class);
		mockHeartbeatDao(heartbeatDao);
	}
	
	private void mockForNoChangePing() throws DaoException, CollectionNotFoundException,
			UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		mockCalendarBackendHasNoChange(calendarBackend);

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, Sets.newHashSet(pingOnCollectionId));
	}

	private void mockForCalendarHasChangePing(int noChangeIterationCount) 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);

		mockCollectionDaoHasChange(collectionDao, noChangeIterationCount);
		mockCalendarBackendHasContentChanges(calendarBackend);
	}

	private void mockForCalendarHierarchyChangedException() 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);

		mockCollectionDaoHasChange(collectionDao, 1);
		mockCalendarBackendHierarchyChangedException(calendarBackend);
	}

	private void mockCollectionDaoHasChange(CollectionDao collectionDao, int noChangeIterationCount) 
			throws DaoException, CollectionNotFoundException {
		Date dateFirstSyncFromASSpecs = new Date(0);
		Date dateWhenChangesAppear = new Date();
		int collectionNoChangeIterationCount = noChangeIterationCount;

		expectCollectionDaoUnchangeForXIteration(collectionDao, dateFirstSyncFromASSpecs, collectionNoChangeIterationCount);

		for (OpushUser user : fakeTestUsers) {
			String collectionPathWhereChangesAppear = buildCalendarCollectionPath(user);
			
			ItemSyncState syncState = ItemSyncState.builder()
					.syncKey(new SyncKey("sync state"))
					.syncDate(DateUtils.getCurrentDate())
					.build();
			expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).once();

			expect(collectionDao.getCollectionPath(anyInt())).andReturn(collectionPathWhereChangesAppear).anyTimes();

			ChangedCollections hasChangesCollections = buildSyncCollectionWithChanges(
					dateWhenChangesAppear, collectionPathWhereChangesAppear);
			expect(collectionDao.getCalendarChangedCollections(dateFirstSyncFromASSpecs)).andReturn(hasChangesCollections).once();
		}
	}
	
	private void expectCollectionDaoUnchangeForXIteration(CollectionDao collectionDao, Date activeSyncSpecFirstSyncDate, 
			int noChangeIterationCount) throws DaoException {
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(new SyncKey("sync state"))
				.syncDate(DateUtils.getCurrentDate())
				.build();
		expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).times(noChangeIterationCount);
		ChangedCollections noChangeCollections = new ChangedCollections(activeSyncSpecFirstSyncDate, ImmutableSet.<String>of());
		expect(collectionDao.getContactChangedCollections(activeSyncSpecFirstSyncDate)).andReturn(noChangeCollections).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(activeSyncSpecFirstSyncDate)).andReturn(noChangeCollections).times(noChangeIterationCount);
	}

	private void mockCalendarBackendHasContentChanges(CalendarBackend calendarBackend)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {
		
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(calendarBackend.getItemEstimateSize(
				anyObject(UserDataRequest.class), 
				anyObject(ItemSyncState.class),
				anyObject(Integer.class),
				anyObject(SyncCollectionOptions.class)))
			.andReturn(1).times(2);
	}

	private void mockCalendarBackendHierarchyChangedException(CalendarBackend calendarBackend)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {
		
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(calendarBackend.getItemEstimateSize(
				anyObject(UserDataRequest.class), 
				anyObject(ItemSyncState.class),
				anyObject(Integer.class),
				anyObject(SyncCollectionOptions.class)))
			.andThrow(new HierarchyChangedException(new NotAllowedException("Not allowed")));
	}

	private void mockCalendarBackendHasNoChange(CalendarBackend calendarBackend) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {
		
		expect(calendarBackend.getItemEstimateSize(
				anyObject(UserDataRequest.class), 
				anyObject(ItemSyncState.class),
				anyObject(Integer.class),
				anyObject(SyncCollectionOptions.class)))
			.andReturn(0).anyTimes();
	}

	private Future<Document> queuePingCommand(final OPClient opClient,
			ThreadPoolExecutor threadPoolExecutor) {
		return threadPoolExecutor.submit(new Callable<Document>() {
			@Override
			public Document call() throws Exception {
				Document document = buildPingCommand(5);
				return opClient.postXml("Ping", document, "Ping", null, false);
			}
		});
	}

	private void mockHeartbeatDao(HeartbeatDao heartbeatDao) throws DaoException {
		heartbeatDao.updateLastHeartbeat(anyObject(Device.class), anyLong());
		expectLastCall().anyTimes();
	}
	
	private ChangedCollections buildSyncCollectionWithChanges(Date dateWhenChangesAppear, 
			String collectionPathWhereChangesAppear) {
		ChangedCollections calendarHasChangeCollections = new ChangedCollections(dateWhenChangesAppear , ImmutableSet.<String>of(collectionPathWhereChangesAppear));
		return calendarHasChangeCollections;
	}

	private Document buildPingCommand(int heartbeatInterval)
			throws SAXException, IOException {
		return DOMUtils.parse("<Ping>"
				+ "<HeartbeatInterval>"
				+ heartbeatInterval
				+ "</HeartbeatInterval>"
				+ "<Folders>"
				+ "<Folder>"
				+ "<Id>" + String.valueOf(pingOnCollectionId) +"</Id>"
				+ "</Folder>"
				+ "</Folders>"
				+ "</Ping>");
	}
}
