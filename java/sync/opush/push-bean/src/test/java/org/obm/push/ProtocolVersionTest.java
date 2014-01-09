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
package org.obm.push;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;


public class ProtocolVersionTest {

	@Test
	public void test12Dot0LesserThan12Dot1() {
		assertThat(ProtocolVersion.V120.compareTo(ProtocolVersion.V121)).isEqualTo(-1);
	}

	@Test
	public void test12Dot1GreaterThan12Dot0() {
		assertThat(ProtocolVersion.V121.compareTo(ProtocolVersion.V120)).isEqualTo(1);
	}
	
	@Test
	public void test12Dot1CompareItself() {
		assertThat(ProtocolVersion.V121.compareTo(ProtocolVersion.V121)).isEqualTo(0);
	}
	
	@Test
	public void test12Dot0CompareItself() {
		assertThat(ProtocolVersion.V120.compareTo(ProtocolVersion.V120)).isEqualTo(0);
	}

	@Test
	public void test12Dot0SpecificationValue() {
		assertThat(ProtocolVersion.V120.asSpecificationValue()).isEqualTo("12.0");
	}
	
	@Test
	public void test12Dot1SpecificationValue() {
		assertThat(ProtocolVersion.V121.asSpecificationValue()).isEqualTo("12.1");
	}
		
	@Test
	public void test12Dot0DecimalValue() {
		assertThat(ProtocolVersion.V120.asDecimalValue()).isEqualTo(new BigDecimal("12.0"));
	}
	
	@Test
	public void test12Dot1DecimalValue() {
		assertThat(ProtocolVersion.V121.asDecimalValue()).isEqualTo(new BigDecimal("12.1"));
	}
	
	@Test
	public void test12Dot1FromSpecificationValue() {
		assertThat(ProtocolVersion.fromSpecificationValue("12.1")).isEqualTo(ProtocolVersion.V121);
	}
	
	@Test
	public void test12Dot0FromSpecificationValue() {
		assertThat(ProtocolVersion.fromSpecificationValue("12.0")).isEqualTo(ProtocolVersion.V120);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnsupportedFromSpecificationValue() {
		ProtocolVersion.fromSpecificationValue("12.2");
	}
}
