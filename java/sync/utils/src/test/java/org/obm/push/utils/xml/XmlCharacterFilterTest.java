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
package org.obm.push.utils.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class XmlCharacterFilterTest {
	
	@Test(expected=NullPointerException.class)
	public void filterNull() {
		XmlCharacterFilter.filter(null);
	}

	@Test
	public void filterEmpty() {
		assertThat(XmlCharacterFilter.filter("")).isEmpty();
	}

	@Test
	public void filterDigit() {
		assertThat(XmlCharacterFilter.filter("12 34")).isEqualTo("12 34");
	}

	@Test
	public void filterDigitNegative() {
		assertThat(XmlCharacterFilter.filter("-34")).isEqualTo("-34");
	}

	@Test
	public void filterGreaterThan() {
		assertThat(XmlCharacterFilter.filter("da > ta")).isEqualTo("da > ta");
	}

	@Test
	public void filterLessThan() {
		assertThat(XmlCharacterFilter.filter("da < ta")).isEqualTo("da < ta");
	}

	@Test
	public void filterAnd() {
		assertThat(XmlCharacterFilter.filter("da & ta")).isEqualTo("da & ta");
	}

	@Test
	public void filterAmp() {
		assertThat(XmlCharacterFilter.filter("da &amp; ta")).isEqualTo("da &amp; ta");
	}
	
	@Test
	public void filterFirstLegal0X9() {
		assertThat(XmlCharacterFilter.filter("da " + (char)0x9 + " ta")).isEqualTo("da " + (char)0x9 + " ta");
	}

	@Test
	public void filter0X92() {
		assertThat(XmlCharacterFilter.filter("da " + (char)0x92 + " ta")).isEqualTo("da " + (char)0x92 + " ta");
	}
	
	@Test
	public void filterIllegalChar() {
		assertThat(XmlCharacterFilter.filter("da  ta")).isEqualTo("da  ta");
	}
	
	@Test
	public void filterSomeLegalChars() {
//		Legals : #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
		String illegalChars = 
				new String(new char[] {0x9, 0xA, 0xD,}) +
				charRange((char)0x20, (char)0xD7FF) +
				charRange((char)0xE000, (char)0xFFFD);
		
		assertThat(XmlCharacterFilter.filter(illegalChars)).isEqualTo(illegalChars);
	}
	
	@Test
	public void filterSomeIllegalChars() {
//		Legals : #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
		String illegalChars = 
				charRange((char)0x1, (char)0x8) +
				charRange((char)0x10, (char)0x15) +
				charRange((char)0xB, (char)0xC) +
				charRange((char)0xE, (char)0x19) +
				charRange((char)0xD800, (char)0xDFFF);
		
		assertThat(XmlCharacterFilter.filter(illegalChars)).isEmpty();
	}

	private String charRange(char from, char to) {
		StringBuilder builder = new StringBuilder();
		char current = from;
		while (current <= to) {
			builder.append(current++);
		}
		return builder.toString();
	}
}
