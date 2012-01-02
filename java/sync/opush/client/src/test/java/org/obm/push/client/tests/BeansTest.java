package org.obm.push.client.tests;

import org.junit.Before;
import org.junit.Test;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Delete;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderHierarchy;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.GetItemEstimateSingleFolderResponse;
import org.obm.sync.push.client.SyncResponse;

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
				.add(AccountInfos.class)
				.add(Add.class)
				.add(Collection.class)
				.add(Folder.class)
				.add(FolderHierarchy.class)
				.add(FolderSyncResponse.class)
				.add(GetItemEstimateSingleFolderResponse.class)
				.add(SyncResponse.class)
				.add(Delete.class)
				.build();
		equalsVerifierUtilsTest.test(list);
	}
}