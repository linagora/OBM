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
package org.obm.push.client.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.push.client.ItemOperationFetchResponse;
import org.obm.sync.push.client.ItemOperationResponse;
import org.obm.sync.push.client.MoveItemsResponse;
import org.obm.sync.push.client.ProvisionResponse;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.Add;
import org.obm.sync.push.client.beans.Collection;
import org.obm.sync.push.client.beans.Delete;
import org.obm.sync.push.client.beans.Folder;
import org.obm.sync.push.client.beans.GetItemEstimateSingleFolderResponse;
import org.obm.sync.push.client.beans.SyncResponse;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
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
				.add(GetItemEstimateSingleFolderResponse.class)
				.add(SyncResponse.class)
				.add(Delete.class)
				.add(ProvisionResponse.class)
				.add(ItemOperationResponse.class)
				.add(ItemOperationFetchResponse.class)
				.add(MoveItemsResponse.class)
				.build();
		equalsVerifierUtilsTest.test(list);
	}
}