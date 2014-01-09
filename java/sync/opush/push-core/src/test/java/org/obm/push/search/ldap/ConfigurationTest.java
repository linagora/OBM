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
package org.obm.push.search.ldap;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import javax.naming.directory.DirContext;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.IniFile.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;


public class ConfigurationTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String FILE = "/etc/opush/ldap_conf.ini";
	private static final String KEY_URL = "search.ldap.url";
	private static final String KEY_BASE = "search.ldap.basedn";
	private static final String KEY_FILTER = "search.ldap.filter";
	
	private IMocksControl mocks;
	private Factory iniFileFactory;
	private IniFile iniFile;

	@Before
	public void setUp() {
		mocks = createControl();
		iniFileFactory = mocks.createMock(IniFile.Factory.class);
		iniFile = mocks.createMock(IniFile.class);
		expect(iniFileFactory.build(FILE)).andReturn(iniFile);
	}
	
	@Test
	public void testUrlNone() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}
	
	@Test
	public void testUrlEmpty() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}
	
	@Test
	public void testUrlNoProtocol() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "127.0.0.1");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isEqualTo("ldap://127.0.0.1");
		assertThat(configuration.isValidConfiguration()).isTrue();
	}

	@Test
	public void testUrlBadProtocol() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "http://ldapserver");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testUrlNoIp() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "ldap://");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testUrlLDAP() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "ldap://ldapserver");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();
		
		assertThat(configuration.getUrl()).isEqualTo("ldap://ldapserver");
		assertThat(configuration.isValidConfiguration()).isTrue();
	}

	@Test
	public void testUrlLDAPS() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter",
				KEY_URL, "ldaps://ldapserver");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getUrl()).isEqualTo("ldaps://ldapserver");
		assertThat(configuration.isValidConfiguration()).isTrue();
	}

	@Test
	public void testBaseNone() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getBaseDn()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testBaseEmpty() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_FILTER, "filter",
				KEY_BASE, "");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getBaseDn()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testBase() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_FILTER, "filter",
				KEY_BASE, "%d,dc=local");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getBaseDn()).isEqualTo("%d,dc=local");
		assertThat(configuration.isValidConfiguration()).isTrue();
	}

	@Test
	public void testFilterNone() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_BASE, "%d,dc=local");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getFilter()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testFilterEmpty() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getFilter()).isNull();
		assertThat(configuration.isValidConfiguration()).isFalse();
	}

	@Test
	public void testFilter() {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		Configuration configuration = new Configuration(iniFileFactory, logger);
		mocks.verify();

		assertThat(configuration.getFilter()).isEqualTo("filter");
		assertThat(configuration.isValidConfiguration()).isTrue();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildContextConnectionFailsIfUrlIsMissing() throws Exception {
		Map<String, String> settings = ImmutableMap.of(
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		new Configuration(iniFileFactory, logger).buildContextConnection();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildContextConnectionFailsIfBaseIsMissing() throws Exception {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		new Configuration(iniFileFactory, logger).buildContextConnection();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildContextConnectionFailsIfFilterIsMissing() throws Exception {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_BASE, "%d,dc=local");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		new Configuration(iniFileFactory, logger).buildContextConnection();
	}

	@Ignore("A connection toward the url tries to be done")
	@Test
	public void testBuildContextConnection() throws Exception {
		Map<String, String> settings = ImmutableMap.of(
				KEY_URL, "ldaps://ldapserver",
				KEY_BASE, "%d,dc=local",
				KEY_FILTER, "filter");
		expect(iniFile.getData()).andReturn(settings).anyTimes();
		
		mocks.replay();
		DirContext connection = new Configuration(iniFileFactory, logger).buildContextConnection();
		mocks.verify();

		assertThat(connection).isNotNull();
	}
}
