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
package org.obm.configuration.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.utils.IniFile;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@RunWith(SlowFilterRunner.class)
public class IniFileTest {

	private Map<String, String> settings;
	private IniFile iniFile;
	
	@Before
	public void setup() {
		settings = Maps.newHashMap();
		iniFile = new IniFile(settings, "discarded");
	}
	
	@Test
	public void testGetTrue() {
		settings.put("key", "true");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(true);
	}

	@Test
	public void testGetFalse() {
		settings.put("key", "false");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(false);
	}

	
	@Test
	public void testGetRandomCaseTrue() {
		settings.put("key", "tRuE");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(true);
	}

	@Test
	public void testGetRandomCaseFalse() {
		settings.put("key", "fAlSe");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(false);
	}

	
	@Test
	public void testGetOneAsBoolean() {
		settings.put("key", "1");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(false);
	}
	
	@Test
	public void testGetZeroAsBoolean() {
		settings.put("key", "0");
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(false);
	}
	
	@Test
	public void testGetNullAsBoolean() {
		assertThat(iniFile.getBooleanValue("key")).isEqualTo(false);
	}

	@Test
	public void testGetTrueWithDefault() {
		settings.put("key", "true");
		assertThat(iniFile.getBooleanValue("key", false)).isEqualTo(true);
	}

	@Test
	public void testGetFalseWithDefault() {
		settings.put("key", "false");
		assertThat(iniFile.getBooleanValue("key", true)).isEqualTo(false);
	}

	
	@Test
	public void testGetRandomCaseTrueWithDefault() {
		settings.put("key", "tRuE");
		assertThat(iniFile.getBooleanValue("key", false)).isEqualTo(true);
	}

	@Test
	public void testGetRandomCaseFalseWithDefault() {
		settings.put("key", "fAlSe");
		assertThat(iniFile.getBooleanValue("key", true)).isEqualTo(false);
	}

	
	@Test
	public void testGetOneAsBooleanWithDefault() {
		settings.put("key", "1");
		assertThat(iniFile.getBooleanValue("key", true)).isEqualTo(false);
	}
	
	@Test
	public void testGetZeroAsBooleanWithDefault() {
		settings.put("key", "0");
		assertThat(iniFile.getBooleanValue("key", true)).isEqualTo(false);
	}
	
	@Test
	public void testGetNullAsBooleanWithDefault() {
		assertThat(iniFile.getBooleanValue("key", true)).isEqualTo(true);
	}

	@Test
	public void testGetNullableTrueWithDefault() {
		settings.put("key", "true");
		assertThat(iniFile.getNullableBooleanValue("key", false)).isEqualTo(true);
	}

	@Test
	public void testGetNullableFalseWithDefault() {
		settings.put("key", "false");
		assertThat(iniFile.getNullableBooleanValue("key", true)).isEqualTo(false);
	}

	
	@Test
	public void testGetNullableRandomCaseTrueWithDefault() {
		settings.put("key", "tRuE");
		assertThat(iniFile.getNullableBooleanValue("key", false)).isEqualTo(true);
	}

	@Test
	public void testGetNullableRandomCaseFalseWithDefault() {
		settings.put("key", "fAlSe");
		assertThat(iniFile.getNullableBooleanValue("key", true)).isEqualTo(false);
	}

	
	@Test
	public void testGetOneAsNullableBooleanWithDefault() {
		settings.put("key", "1");
		assertThat(iniFile.getNullableBooleanValue("key", true)).isEqualTo(false);
	}
	
	@Test
	public void testGetZeroAsNullableBooleanWithDefault() {
		settings.put("key", "0");
		assertThat(iniFile.getNullableBooleanValue("key", true)).isEqualTo(false);
	}
	

	@Test
	public void testGetNullAsNullableBooleanWithNullDefault() {
		assertThat(iniFile.getNullableBooleanValue("key", null)).isNull();
	}
	
	@Test
	public void testGetCategory() {
		assertThat(iniFile.getCategory()).isEqualTo("discarded");
	}
	
	@Test
	public void testGetData() {
		settings.put("key", "value");
		assertThat(iniFile.getData()).isEqualTo(ImmutableMap.of("key", "value"));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetDataReturnsAnImmutableMap() {
		Map<String, String> data = iniFile.getData();
		data.put("key2", "value2");
	}
	
	@Test
	public void testGetInt() {
		settings.put("key", "1");
		assertThat(iniFile.getIntValue("key", 2)).isEqualTo(1);
	}
	
	@Test
	public void testGetUnparsableInt() {
		settings.put("key", "one");
		assertThat(iniFile.getIntValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetTooBigInt() {
		settings.put("key", "111111111111");
		assertThat(iniFile.getIntValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetNullInt() {
		assertThat(iniFile.getIntValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetInteger() {
		settings.put("key", "1");
		assertThat(iniFile.getIntegerValue("key", 2)).isEqualTo(1);
	}
	
	@Test
	public void testGetUnparsableInteger() {
		settings.put("key", "one");
		assertThat(iniFile.getIntegerValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetTooBigInteger() {
		settings.put("key", "111111111111");
		assertThat(iniFile.getIntegerValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetNullInteger() {
		assertThat(iniFile.getIntegerValue("key", 2)).isEqualTo(2);
	}
	
	@Test
	public void testGetNullIntegerWithNullDefault() {
		assertThat(iniFile.getIntegerValue("key", null)).isNull();
	}
	
	
	@Test
	public void testGetNullString() {
		assertThat(iniFile.getStringValue("key")).isNull();
	}
	
	@Test
	public void testGetNullStringWithDefault() {
		assertThat(iniFile.getStringValue("key", "default")).isEqualTo("default");
	}
	
	@Test
	public void testGetStringWithDefault() {
		settings.put("key", "111111111111");
		assertThat(iniFile.getStringValue("key", "default")).isEqualTo("111111111111");
	}
	
	@Test
	public void testGetString() {
		settings.put("key", "111211111111");
		assertThat(iniFile.getStringValue("key")).isEqualTo("111211111111");
	}
}
