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
package org.obm.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class VMArgumentsUtilsTest {
	
	@Test
	public void testBooleanArgumentValueNull() {
		boolean value = VMArgumentsUtils.booleanArgumentValue("null");
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueTrue() {
		String argument = "arg";
		System.setProperty(argument, "true");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isTrue();
	}
	
	@Test
	public void testBooleanArgumentValueFalse() {
		String argument = "arg";
		System.setProperty(argument, "false");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueCases() {
		String argument = "Arg";
		System.setProperty("arg", "true");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueEmptyValue() {
		String argument = "arg";
		System.setProperty(argument, "");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueIntegerValue() {
		String argument = "arg";
		System.setProperty(argument, "1");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueOtherValue() {
		String argument = "arg";
		System.setProperty(argument, "other");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isFalse();
	}
	
	@Test
	public void testBooleanArgumentValueTrueCases() {
		String argument = "arg";
		System.setProperty(argument, "TRue");
		boolean value = VMArgumentsUtils.booleanArgumentValue(argument);
		assertThat(value).isTrue();
	}
	
	@Test
	public void testStringArgumentValue() {
		String argument = "arg";
		String expectedValue = "value";
		System.setProperty(argument, expectedValue);
		String value = VMArgumentsUtils.stringArgumentValue(argument);
		assertThat(value).isEqualTo(expectedValue);
	}
	
	@Test
	public void testStringArgumentValueEmpty() {
		String argument = "arg";
		String expectedValue = "";
		System.setProperty(argument, expectedValue);
		String value = VMArgumentsUtils.stringArgumentValue(argument);
		assertThat(value).isEqualTo(expectedValue);
	}
	
	@Test
	public void testStringArgumentValueUnknown() {
		String argument = "arg";
		String value = VMArgumentsUtils.stringArgumentValue(argument);
		assertThat(value).isEmpty();
	}
	
	@Test
	public void testIntegerArgumentValue() {
		String argument = "arg";
		String expectedValue = "5";
		System.setProperty(argument, expectedValue);
		Integer value = VMArgumentsUtils.integerArgumentValue(argument);
		assertThat(value).isEqualTo(5);
	}
	
	@Test
	public void testIntegerArgumentValueNotAnInteger() {
		String argument = "arg";
		String expectedValue = "a5b";
		System.setProperty(argument, expectedValue);
		Integer value = VMArgumentsUtils.integerArgumentValue(argument);
		assertThat(value).isNull();
	}
	
	@Test
	public void testIntegerArgumentValueEmpty() {
		String argument = "arg";
		String expectedValue = "";
		System.setProperty(argument, expectedValue);
		Integer value = VMArgumentsUtils.integerArgumentValue(argument);
		assertThat(value).isNull();
	}
	
	@Test
	public void testIntegerArgumentValueUnknown() {
		String argument = "arg";
		Integer value = VMArgumentsUtils.integerArgumentValue(argument);
		assertThat(value).isNull();
	}
}
