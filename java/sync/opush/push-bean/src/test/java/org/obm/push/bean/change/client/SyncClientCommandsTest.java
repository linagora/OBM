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
package org.obm.push.bean.change.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.change.client.SyncClientCommands.Add;
import org.obm.push.bean.change.client.SyncClientCommands.Update;

@SuppressWarnings("unused")

public class SyncClientCommandsTest {

	@Test(expected=IllegalArgumentException.class)
	public void testChangeWhenNull() {
		new Update(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testChangeWhenEmpty() {
		new Update("");
	}

	@Test
	public void testChangeWhenOk() {
		Update actual = new Update("123");
		assertThat(actual.serverId).isEqualTo("123");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddWhenBothNull() {
		new Add(null, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddWhenClientIdNull() {
		new Add(null, "123");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddWhenServerIdNull() {
		new Add("123", null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddWhenClientIdEmpty() {
		new Add("", "123");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddWhenServerIdEmpty() {
		new Add("123", "");
	}

	@Test
	public void testAddWhenOk() {
		Add actual = new Add("123", "98:7");
		assertThat(actual.serverId).isEqualTo("98:7");
		assertThat(actual.clientId).isEqualTo("123");
	}

	@Test
	public void testMergeAddEmpty() {
		SyncClientCommands root = SyncClientCommands.builder()
				.putAdd(new Add("1234", "98:7"))
				.putAdd(new Add("1234", "98:6"))
				.merge(SyncClientCommands.empty())
				.build();
		
		assertThat(root.getAdds()).containsOnly(new Add("1234", "98:7"), new Add("1234", "98:6"));
		assertThat(root.getChanges()).isEmpty();
	}

	@Test
	public void testMergeAdd() {
		SyncClientCommands root = SyncClientCommands.builder()
				.putAdd(new Add("1234", "98:7"))
				.putAdd(new Add("1234", "98:6"))
				.merge(SyncClientCommands.builder()
						.putAdd(new Add("1235", "98:5"))
						.putAdd(new Add("1235", "98:4"))
						.build())
				.build();
		
		assertThat(root.getAdds()).containsOnly(
				new Add("1234", "98:7"),
				new Add("1234", "98:6"),
				new Add("1235", "98:5"),
				new Add("1235", "98:4"));
		assertThat(root.getChanges()).isEmpty();
	}

	@Test
	public void testMergeChangesEmpty() {
		SyncClientCommands root = SyncClientCommands.builder()
				.putChange(new Update("98:7"))
				.putChange(new Update("98:6"))
				.merge(SyncClientCommands.empty())
				.build();
		
		assertThat(root.getAdds()).isEmpty();
		assertThat(root.getChanges()).containsOnly(new Update("98:7"), new Update("98:6"));
	}

	@Test
	public void testMergeChanges() {
		SyncClientCommands root = SyncClientCommands.builder()
				.putChange(new Update("98:7"))
				.putChange(new Update("98:6"))
				.merge(SyncClientCommands.builder()
						.putChange(new Update("98:5"))
						.putChange(new Update("98:4"))
						.build())
				.build();
		
		assertThat(root.getAdds()).isEmpty();
		assertThat(root.getChanges()).containsOnly(
				new Update("98:7"),
				new Update("98:6"),
				new Update("98:5"),
				new Update("98:4"));
	}

	@Test
	public void testMergeAddsAndChanges() {
		SyncClientCommands root = SyncClientCommands.builder()
				.putAdd(new Add("1234", "98:7"))
				.putChange(new Update("98:7"))
				.merge(SyncClientCommands.builder()
						.putAdd(new Add("1235", "98:6"))
						.putChange(new Update("98:5"))
						.build())
				.build();
		
		assertThat(root.getAdds()).containsOnly(
				new Add("1234", "98:7"),
				new Add("1235", "98:6"));
		assertThat(root.getChanges()).containsOnly(
				new Update("98:7"),
				new Update("98:5"));
	}

	@Test
	public void testSumOfCommandsWhenEmpty() {
		SyncClientCommands actual = SyncClientCommands.empty();
		assertThat(actual.sumOfCommands()).isEqualTo(0);
	}

	@Test
	public void testSumOfCommandsWhenNoPutWithBuilder() {
		SyncClientCommands actual = SyncClientCommands.builder().build();
		assertThat(actual.sumOfCommands()).isEqualTo(0);
	}

	@Test
	public void testSumOfCommandsWhenTwoAdd() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putAdd(new Add("456", "98:6"))
				.build();
		assertThat(actual.sumOfCommands()).isEqualTo(2);
	}

	@Test
	public void testSumOfCommandsWhenTwoChange() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putChange(new Update("98:7"))
				.putChange(new Update("98:6"))
				.build();
		assertThat(actual.sumOfCommands()).isEqualTo(2);
	}

	@Test
	public void testSumOfCommandsWhenTwoAddAndTwoChange() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putAdd(new Add("456", "98:6"))
				.putChange(new Update("98:7"))
				.putChange(new Update("98:6"))
				.build();
		assertThat(actual.sumOfCommands()).isEqualTo(4);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testHasCommandWithServerIdNull() {
		SyncClientCommands actual = SyncClientCommands.builder().build();
		actual.hasCommandWithServerId(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testHasCommandWithServerIdEmpty() {
		SyncClientCommands actual = SyncClientCommands.builder().build();
		actual.hasCommandWithServerId("");
	}
	
	@Test
	public void testHasCommandWithServerIdWhenNoPutWithBuilder() {
		SyncClientCommands actual = SyncClientCommands.builder().build();
		assertThat(actual.hasCommandWithServerId("123")).isFalse();
	}

	@Test
	public void testHasCommandWithServerIdWhenEmpty() {
		SyncClientCommands actual = SyncClientCommands.empty();
		assertThat(actual.hasCommandWithServerId("123")).isFalse();
	}

	@Test
	public void testHasCommandWithServerIdDontMatch() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putChange(new Update("7:97"))
				.build();
		assertThat(actual.hasCommandWithServerId("7:98")).isFalse();
	}

	@Test
	public void testHasCommandWithServerIdMatchAddClientId() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putChange(new Update("7:97"))
				.build();
		assertThat(actual.hasCommandWithServerId("123")).isFalse();
	}

	@Test
	public void testHasCommandWithServerIdMatchAddServerId() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putChange(new Update("7:97"))
				.build();
		assertThat(actual.hasCommandWithServerId("98:7")).isTrue();
	}

	@Test
	public void testHasCommandWithServerIdMatchChangeServerId() {
		SyncClientCommands actual = SyncClientCommands.builder()
				.putAdd(new Add("123", "98:7"))
				.putChange(new Update("7:97"))
				.build();
		assertThat(actual.hasCommandWithServerId("7:97")).isTrue();
	}
}
