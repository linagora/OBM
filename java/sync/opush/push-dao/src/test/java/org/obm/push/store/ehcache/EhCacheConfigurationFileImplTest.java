/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.push.store.ehcache;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.store.ehcache.EhCacheConfigurationFileImpl.*;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.IniFile.Factory;

@RunWith(SlowFilterRunner.class)
public class EhCacheConfigurationFileImplTest {

	private IMocksControl control;
	private IniFile iniFile;
	private Factory factory;

	@Before
	public void setup() {
		control = createControl();
		iniFile = control.createMock(IniFile.class);
		factory = control.createMock(IniFile.Factory.class);
		expect(factory.build("/etc/opush/ehcache_conf.ini")).andReturn(iniFile);
	}

	private EhCacheConfigurationFileImpl testee() {
		return new EhCacheConfigurationFileImpl(factory);
	}

	@Test(expected=IllegalStateException.class)
	public void testMaxMemoryInMBWhenZero() throws Exception {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(0);
		
		control.replay();
		try {
			testee().maxMemoryInMB();
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testMaxMemoryInMBWhenNegative() throws Exception {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(-1);
		
		control.replay();
		try {
			testee().maxMemoryInMB();
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testMaxMemoryInMBWhenHigherThanXmx() throws Exception {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(Integer.MAX_VALUE);
		
		control.replay();
		try {
			testee().maxMemoryInMB();
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test
	public void testMaxMemoryInMB() {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(150);
		
		control.replay();
		assertThat(testee().maxMemoryInMB()).isEqualTo(150);
		control.verify();
	}

	@Test
	public void testPercentageWhenUndefined() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(null);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.UNDEFINED);
		control.verify();
	}

	@Test
	public void testPercentageWhenOne() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(1);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.of(1));
		control.verify();
	}

	@Test
	public void testPercentageWhenOneHundred() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(100);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.of(100));
		control.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPercentageWhenMoreThanOneHundred() throws Exception {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(101);
		
		control.replay();
		try {
			testee().percentageAllowedToCache("cacheName");
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPercentageWhenNegative() throws Exception {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(-1);
		
		control.replay();
		try {
			testee().percentageAllowedToCache("cacheName");
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test
	public void testPercentageWhenZeroIsUndefined() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(0);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.UNDEFINED);
		control.verify();
	}
}
