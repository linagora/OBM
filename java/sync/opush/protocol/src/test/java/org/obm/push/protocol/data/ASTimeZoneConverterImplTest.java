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
package org.obm.push.protocol.data;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.protocol.bean.ASTimeZone;

import com.google.common.collect.Maps;

@RunWith(SlowFilterRunner.class)
public class ASTimeZoneConverterImplTest {

	private ASTimeZoneConverterImpl asTimeZoneConverter;

	@Before
	public void before() {
		asTimeZoneConverter = new ASTimeZoneConverterImpl(new TimeZoneConverterImpl());
	}

	@Test
	public void testConvertEuropeParisTimeZone() {
		TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
		ASTimeZone asTimeZone = toASTimeZone(timeZone);

		TimeZone expectedTimeZone = asTimeZoneConverter.convert(asTimeZone);

		Assertions.assertThat(timeZone.getID()).isEqualTo(expectedTimeZone.getID());
	}
	
	@Test
	public void testConvertAllTimeZonePreferences() {
		Map<String, TimeZone> map = Maps.newHashMap();
		for (String tZI : ASTimeZoneConverterImpl.TIME_ZONE_PREFERENCES) {
			TimeZone timeZone = TimeZone.getTimeZone(tZI);
			ASTimeZone asTimeZone = toASTimeZone(timeZone);

			TimeZone expectedTimeZone = asTimeZoneConverter.convert(asTimeZone);
			map.put(tZI, expectedTimeZone);
		}
		assertTimezoneIdsAreCorrect(map);
	}
	
	@Test
	public void testConvertSpecialTimeZones() {
		Map<String, String> tzi = Maps.newHashMap();
		tzi.put("Europe/Madrid", "Europe/Paris");
		tzi.put("Asia/Tel_Aviv", "Asia/Jerusalem");
		tzi.put("Asia/Amman", "Asia/Amman");
		tzi.put("America/Asuncion", "America/Asuncion");
		tzi.put("America/Adak", "America/Adak");
		
		Map<String, TimeZone> map = Maps.newHashMap();
		for (Entry<String, String> entry : tzi.entrySet()) {
			TimeZone timeZone = TimeZone.getTimeZone(entry.getKey());
			ASTimeZone asTimeZone = toASTimeZone(timeZone);

			TimeZone expectedTimeZone = asTimeZoneConverter.convert(asTimeZone);
			map.put(entry.getValue(), expectedTimeZone);
		}
		assertTimezoneIdsAreCorrect(map);
	}
	
	private void assertTimezoneIdsAreCorrect(Map<String, TimeZone> map) {
		for (java.util.Map.Entry<String, TimeZone> e: map.entrySet()) {
			Assertions.assertThat(e.getKey()).isEqualTo(e.getValue().getID());
		}
	}
	
	private ASTimeZone toASTimeZone(TimeZone timeZone) {
		return new TimeZoneConverterImpl().convert(timeZone, Locale.US);
	}
}
