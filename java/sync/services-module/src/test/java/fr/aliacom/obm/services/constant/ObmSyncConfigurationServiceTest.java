/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.services.constant;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.IniFile.Factory;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.calendar.CalendarEncoding;

@RunWith(SlowFilterRunner.class)
public class ObmSyncConfigurationServiceTest {

	private ObmSyncConfigurationService service;
	private IniFile configuration;
	private IMocksControl control;
	private Factory factory;

	@Before
	public void setUp() {
		control = createControl();
		configuration = control.createMock(IniFile.class);
		factory = control.createMock(IniFile.Factory.class);
		expect(factory.build(anyObject(String.class))).andReturn(configuration);
	}

	@Test
	public void testGetEmailCalendarEncodingInvalidEncoding() {
		expect(configuration.getData()).andReturn(
				ImmutableMap.of(ObmSyncConfigurationService.EMAIL_CALENDAR_ENCODING_PARAMETER, "InvalidEncoding"));
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.Auto);
		control.verify();
	}
	
	@Test
	public void testGetEmailCalendarEncodingEmptyPropertyDefined() {
		expect(configuration.getData()).andReturn(
				ImmutableMap.of(ObmSyncConfigurationService.EMAIL_CALENDAR_ENCODING_PARAMETER, ""));
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.Auto);
		control.verify();
	}

	@Test
	public void testGetEmailCalendarEncodingNoPropertyDefined() {
		expect(configuration.getData()).andReturn(ImmutableMap.<String, String>of());
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.Auto);
		control.verify();
	}
	
	@Test
	public void testGetEmailCalendarEncodingBase64() {
		expect(configuration.getData()).andReturn(
				ImmutableMap.of(ObmSyncConfigurationService.EMAIL_CALENDAR_ENCODING_PARAMETER, "Base64"));
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.Base64);
		control.verify();
	}
	
	@Test
	public void testGetEmailCalendarEncodingQuotedPrintable() {
		expect(configuration.getData()).andReturn(
				ImmutableMap.of(ObmSyncConfigurationService.EMAIL_CALENDAR_ENCODING_PARAMETER, "QuotedPrintable"));
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.QuotedPrintable);
		control.verify();
	}
	
	@Test
	public void testGetEmailCalendarEncodingSevenBit() {
		expect(configuration.getData()).andReturn(
				ImmutableMap.of(ObmSyncConfigurationService.EMAIL_CALENDAR_ENCODING_PARAMETER, "SevenBit"));
		control.replay();
		service = new ObmSyncConfigurationService(factory, "test");
		assertThat(service.getEmailCalendarEncoding()).isEqualTo(CalendarEncoding.SevenBit);
		control.verify();
	}
}
