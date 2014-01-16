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
package org.obm.push.minig.imap.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.minig.imap.impl.IMAPParsingTools;

public class IMAPParsingToolsTest {

	@Test
	public void testGetNextNumberNullString() {
		String nextNumber = IMAPParsingTools.getNextNumber(null);
		assertThat(nextNumber).isNull();
	}

	@Test
	public void testGetNextNumberEmptyString() {
		String nextNumber = IMAPParsingTools.getNextNumber("");
		assertThat(nextNumber).isEmpty();
	}

	@Test
	public void testGetNextNumberWithoutDigit() {
		String nextNumber = IMAPParsingTools.getNextNumber("sdqf");
		assertThat(nextNumber).isEmpty();
	}

	@Test
	public void testGetNextNumberOnlyDigits() {
		String nextNumber = IMAPParsingTools.getNextNumber("1234");
		assertThat(nextNumber).isEqualTo("1234");
	}

	@Test
	public void testGetNextNumber() {
		String nextNumber = IMAPParsingTools.getNextNumber("1234a");
		assertThat(nextNumber).isEqualTo("1234");
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketSameOpenCloseCount() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket("(a b (c) (d)((e)f))");
		assertThat(parsedInnerBracketContent).isEqualTo("(a b (c) (d)((e)f))");
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketMoreClose() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket("(a b (c) (d)((e)f)))");
		assertThat(parsedInnerBracketContent).isEqualTo("(a b (c) (d)((e)f))");
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketCloseBeforeEnd() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket("(a b (c)) (d)((e)f))");
		assertThat(parsedInnerBracketContent).isEqualTo("(a b (c))");
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketBeginWithOtherChar() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket(" a (a b (c) (d)((e)f))");
		assertThat(parsedInnerBracketContent).isEqualTo("(a b (c) (d)((e)f))");
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketNoMatchedClose() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket("(a b (c) (d)((ef))");
		assertThat(parsedInnerBracketContent).isNull();
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketWithNullArg() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket(null);
		assertThat(parsedInnerBracketContent).isNull();
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketWithEmptyArg() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket("");
		assertThat(parsedInnerBracketContent).isNull();
	}

	@Test
	public void testSubstringFromOpeningToClosingBracketWithOnlySpaceArg() {
		String parsedInnerBracketContent = IMAPParsingTools.substringFromOpeningToClosingBracket(" ");
		assertThat(parsedInnerBracketContent).isNull();
	}
}
