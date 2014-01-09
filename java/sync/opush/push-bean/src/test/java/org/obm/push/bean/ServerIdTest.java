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
package org.obm.push.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.obm.push.exception.activesync.InvalidServerId;



public class ServerIdTest {

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testEmptyString() throws InvalidServerId {
		new ServerId("");
	}

	@SuppressWarnings("unused")
	@Test(expected=NullPointerException.class)
	public void testNullString() throws InvalidServerId {
		new ServerId(null);
	}

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntCollectionIdString() throws InvalidServerId {
		new ServerId("azd");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntCollectionIdString2() throws InvalidServerId {
		new ServerId("azd:123");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooLargeIntCollectionIdString() throws InvalidServerId {
		new ServerId("123456789123456");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntItemIdString() throws InvalidServerId {
		new ServerId("123:abc");
	}

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooLargeIntItemIdString() throws InvalidServerId {
		new ServerId("123:123456789123456");
	}

	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooManyPartsString() throws InvalidServerId {
		new ServerId("123:123:123");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testWeirdString() throws InvalidServerId {
		new ServerId(":123:123");
	}
	
	@Test
	public void testSimpleCollectionIdString() throws InvalidServerId {
		ServerId serverId = new ServerId("123");
		assertThat(serverId.getCollectionId()).isEqualTo(123);
		assertThat(serverId.getItemId()).isNull();
		assertThat(serverId.isItem()).isFalse();
	}
	
	@Test
	public void testSimpleServerIdString() throws InvalidServerId {
		ServerId serverId = new ServerId("123:345");
		assertThat(serverId.getCollectionId()).isEqualTo(123);
		assertThat(serverId.getItemId()).isEqualTo(Integer.valueOf(345));
		assertThat(serverId.isItem()).isTrue();
	}
	
	@Test
	public void testSimpleEquals() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123:345");
		ServerId serverId2 = new ServerId("123:345");
		assertThat(serverId1.equals(serverId2)).isTrue();
		assertThat(serverId2.equals(serverId1)).isTrue();
		assertThat(serverId1.hashCode()).isEqualTo(serverId2.hashCode());
	}
	
	@Test
	public void testNotEquals() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123:456");
		ServerId serverId2 = new ServerId("123:345");
		assertThat(serverId1.hashCode()).isNotEqualTo(serverId2.hashCode());
		assertThat(serverId1).isNotEqualTo(serverId2);
		assertThat(serverId2).isNotEqualTo(serverId1);
	}
	
	@Test
	public void testNotEquals2() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123");
		ServerId serverId2 = new ServerId("123:345");
		assertThat(serverId1.hashCode()).isNotEqualTo(serverId2.hashCode());
		assertThat(serverId1).isNotEqualTo(serverId2);
		assertThat(serverId2).isNotEqualTo(serverId1);
	}
	
	@Test
	public void testBuildServerIdAsStringZero() {
		assertThat(ServerId.buildServerIdString(0, 0)).isEqualTo("0:0");
	}

	@Test
	public void testBuildServerIdAsStringNegative() {
		assertThat(ServerId.buildServerIdString(-10, -5)).isEqualTo("-10:-5");
	}

	@Test
	public void testBuildServerIdAsString() {
		assertThat(ServerId.buildServerIdString(10, 5)).isEqualTo("10:5");
	}
}
