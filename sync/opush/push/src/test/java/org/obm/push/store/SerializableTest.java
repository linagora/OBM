package org.obm.push.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.Device;
import org.obm.push.backend.MSAddress;
import org.obm.push.backend.MSAttachement;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSContact;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEmailBody;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.MSTask;
import org.obm.push.backend.Recurrence;
import org.obm.push.impl.Credentials;

import com.google.common.collect.ImmutableSet;

public class SerializableTest {

	private ObjectOutputStream objectOutputStream;


	@Before
	public void buildOutputStream() throws IOException {
		objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());	
	}

	@Test
	public void testSyncCollectionOptions() throws IOException {
		SyncCollectionOptions obj = new SyncCollectionOptions();
		obj.addBodyPreference(new BodyPreference());
		objectOutputStream.writeObject(obj);
	}
	
	@Test
	public void testSyncCollection() throws IOException {
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.addChange(new SyncCollectionChange("serverId", "clientId", "modType", new MSContact(), PIMDataType.CALENDAR));
		syncCollection.setSyncState(new SyncState("path", "key", new Date()));
		objectOutputStream.writeObject(syncCollection);
	}
	
	@Test
	public void testMSEmail() throws IOException {
		MSEmail msEmail = new MSEmail();
		msEmail.setBody(new MSEmailBody());
		msEmail.setFrom(new MSAddress("toto@titi.com"));
		msEmail.setAttachements(ImmutableSet.of(new MSAttachement()));
		objectOutputStream.writeObject(msEmail);
	}

	@Test
	public void testMSEvent() throws IOException {
		MSEvent msEvent = new MSEvent();
		msEvent.addAttendee(new MSAttendee());
		msEvent.setRecurrence(new Recurrence());
		objectOutputStream.writeObject(msEvent);
	}
	
	@Test
	public void testMSTask() throws IOException {
		MSTask msTask = new MSTask();
		msTask.setRecurrence(new Recurrence());
		objectOutputStream.writeObject(msTask);
	}
	
	@Test
	public void testDevice() throws IOException {
		Device obj = new Device("toto", "toto", new Properties());
		objectOutputStream.writeObject(obj);
	}

	@Test
	public void testCredentials() throws IOException {
		Credentials obj = new Credentials("titi", "tata");
		objectOutputStream.writeObject(obj);
	}
	
}
