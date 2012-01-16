package org.obm.opush.command.sync;

import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChanges;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasAddItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasDeleteItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasNoChange;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncClasses;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.PortNumber;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.DataDeltaBuilder;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.ItemChangesBuilder;
import org.obm.push.bean.MSEmail;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Delete;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.SyncResponse;

import com.google.inject.Inject;

public class SyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(SyncHandlerTestModule.class);

	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testSyncDefaultMailFolderUnchange() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "1";
		int syncEmailCollectionId = 4;
		DataDelta delta = new DataDeltaBuilder().withSyncDate(new Date()).build();
		mockHierarchyChanges(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());

		checkSyncDefaultMailFolderHasNoChange(inbox, syncEmailResponse);
	}
	
	@Test
	public void testSyncOneInboxMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;

		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":0")
							.withApplicationData(new MSEmail())))
			.withSyncDate(new Date()).build();
		
		mockHierarchyChanges(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		checkSyncDefaultMailFolderHasAddItems(inbox, syncEmailResponse, 
				new Add(syncEmailCollectionId + ":" + 0));
	}

	@Test
	public void testSyncTwoInboxMails() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		
		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":0")
							.withApplicationData(new MSEmail()))
					.addItemChange(
						new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":1")
							.withApplicationData(new MSEmail())))
			.withSyncDate(new Date()).build();
		
		mockHierarchyChanges(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());

		checkSyncDefaultMailFolderHasAddItems(inbox, syncEmailResponse, 
				new Add(syncEmailCollectionId + ":" + 0),
				new Add(syncEmailCollectionId + ":" + 1));
	}

	@Test
	public void testSyncOneInboxDeletedMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		
		DataDelta delta = new DataDeltaBuilder()
			.addDeletions(
				new ItemChangesBuilder().addItemChange(
					new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":0")
						.withApplicationData(new MSEmail())))
			.withSyncDate(new Date()).build();
		
		mockHierarchyChanges(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		checkSyncDefaultMailFolderHasDeleteItems(inbox, syncEmailResponse, 
				new Delete(syncEmailCollectionId + ":" + 0));
	}

	@Test
	public void testSyncInboxOneNewOneDeletedMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder().addItemChange(
						new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":123")
							.withApplicationData(new MSEmail())))
			.addDeletions(
				new ItemChangesBuilder().addItemChange(
						new ItemChangeBuilder().withServerId(syncEmailCollectionId + ":122")
							.withApplicationData(new MSEmail())))
			.withSyncDate(new Date()).build();
		
		mockHierarchyChanges(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		
		checkSyncDefaultMailFolderHasItems(inbox, syncEmailResponse, 
				Arrays.asList(new Add(syncEmailCollectionId + ":123")),
				Arrays.asList(new Delete(syncEmailCollectionId + ":122")));
	}

}
