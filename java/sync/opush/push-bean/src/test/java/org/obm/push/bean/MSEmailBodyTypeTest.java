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
package org.obm.push.bean;

import org.fest.assertions.api.Assertions;
import org.junit.Test;


public class MSEmailBodyTypeTest {

	@Test
	public void testPlainText() {
		MSEmailBodyType plainText = MSEmailBodyType.getValueOf(1);
		Assertions.assertThat(plainText).isEqualTo(MSEmailBodyType.PlainText);
		Assertions.assertThat(plainText.asXmlValue()).isEqualTo(1);
	}
	
	@Test
	public void testHTML() {
		MSEmailBodyType html = MSEmailBodyType.getValueOf(2);
		Assertions.assertThat(html).isEqualTo(MSEmailBodyType.HTML);
		Assertions.assertThat(html.asXmlValue()).isEqualTo(2);
	}
	
	@Test
	public void testRTF() {
		MSEmailBodyType rtf = MSEmailBodyType.getValueOf(3);
		Assertions.assertThat(rtf).isEqualTo(MSEmailBodyType.RTF);
		Assertions.assertThat(rtf.asXmlValue()).isEqualTo(3);
	}
	
	@Test
	public void testMime() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(4);
		Assertions.assertThat(mime).isEqualTo(MSEmailBodyType.MIME);
		Assertions.assertThat(mime.asXmlValue()).isEqualTo(4);
	}
	
	@Test
	public void testInvalidInteger() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(0);
		Assertions.assertThat(mime).isNull();
	}
	
	@Test
	public void testNullInteger() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(null);
		Assertions.assertThat(mime).isNull();
	}
}