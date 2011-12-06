package org.obm.push.bean;

import org.junit.Before;
import org.junit.Test;
import org.obm.sync.bean.EqualsVerifierUtils;

import com.google.common.collect.ImmutableList;

public class BeansTest {

	private EqualsVerifierUtils equalsVerifierUtilsTest;
	
	@Before
	public void init() {
		equalsVerifierUtilsTest = new EqualsVerifierUtils();
	}
	
	@Test
	public void test() {
		ImmutableList<Class<?>> list = 
				ImmutableList.<Class<?>>builder()
					.add(BackendSession.class) 
					.add(BodyPreference.class)
					.add(ChangedCollections.class)
					.add(Credentials.class)
					.add(Device.class)
					.add(Email.class)
					.add(ItemChange.class)
					.add(MeetingResponse.class)
					.add(MoveItem.class)
					.add(MSAddress.class)
					.add(MSAttachement.class)
					.add(MSAttachementData.class)
					.add(MSAttendee.class)
					.add(MSEmail.class)
					.add(MSContact.class)
					.add(MSEmailBody.class)
					.add(MSEvent.class)
					.add(MSTask.class)
					.add(Recurrence.class)
					.add(SearchResult.class)
					.add(ServerId.class)
					.add(Sync.class)
					.add(SyncCollection.class)
					.add(SyncCollectionChange.class)
					.add(SyncCollectionOptions.class)
					.add(SyncState.class)
					.add(MSEventUid.class)
					.add(User.class)
					.build();
		equalsVerifierUtilsTest.test(list);
	}
	
}
