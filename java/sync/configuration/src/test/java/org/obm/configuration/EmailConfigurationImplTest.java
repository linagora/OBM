/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.configuration;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration.ExpungePolicy;
import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.IniFile.Factory;

public class EmailConfigurationImplTest {

	private IMocksControl control;
	private IniFile iniFile;
	private EmailConfigurationImpl testee;

	@Before
	public void setup() {
		control = createControl();
		iniFile = control.createMock(IniFile.class);

		Factory fileFactory = createMock(IniFile.Factory.class);
		expect(fileFactory.build(anyObject(String.class))).andReturn(iniFile).anyTimes();
		replay(fileFactory);
		
		testee = new EmailConfigurationImpl(fileFactory);
	}
	
	@Test
	public void testExpungeDefaultValue() {
		expect(iniFile.getStringValue("imap.expungePolicy")).andReturn(null);
		control.replay();
		assertThat(testee.expungePolicy()).isEqualTo(ExpungePolicy.ALWAYS);
		control.verify();
	}
	
	@Test
	public void testExpungeInvalidValue() {
		expect(iniFile.getStringValue("imap.expungePolicy")).andReturn("foo");
		control.replay();
		assertThat(testee.expungePolicy()).isEqualTo(ExpungePolicy.ALWAYS);
		control.verify();
	}
	
	@Test
	public void testExpungeAlways() {
		expect(iniFile.getStringValue("imap.expungePolicy")).andReturn("always");
		control.replay();
		assertThat(testee.expungePolicy()).isEqualTo(ExpungePolicy.ALWAYS);
		control.verify();
	}
	
	@Test
	public void testExpungeNever() {
		expect(iniFile.getStringValue("imap.expungePolicy")).andReturn("never");
		control.replay();
		assertThat(testee.expungePolicy()).isEqualTo(ExpungePolicy.NEVER);
		control.verify();
	}
	
	@Test
	public void testExpungeNeverStrangeCase() {
		expect(iniFile.getStringValue("imap.expungePolicy")).andReturn("nEvEr");
		control.replay();
		assertThat(testee.expungePolicy()).isEqualTo(ExpungePolicy.NEVER);
		control.verify();
	}

}
