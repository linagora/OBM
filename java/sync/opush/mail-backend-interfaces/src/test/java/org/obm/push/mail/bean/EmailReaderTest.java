/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.push.mail.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.junit.Test;
import org.obm.opush.mail.StreamMailTestsUtils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


public class EmailReaderTest {

	@Test
	public void testConstructorInputStreamReaderWhenUTF8() throws IOException {
		Charset charset = Charsets.UTF_8;
		EmailReader testee = new EmailReader(new InputStreamReader(in("data°£€þðØ¿¶", charset), charset));
		assertThat(testee.charset()).isEqualTo(charset);
		assertThat(CharStreams.toString(testee)).isEqualTo(new String("data°£€þðØ¿¶".getBytes(charset)));
	}
	
	@Test
	public void testConstructorInputStreamReaderWhenISO() throws IOException {
		Charset charset = Charsets.ISO_8859_1;
		EmailReader testee = new EmailReader(new InputStreamReader(in("data®¥þðØ¿¶", charset), charset));
		assertThat(testee.charset()).isEqualTo(charset);
		assertThat(CharStreams.toString(testee)).isEqualTo("data®¥þðØ¿¶");
	}
	
	@Test(expected=IllegalStateException.class)
	public void testToInputStreamIsDenyWhenAlreadyReadSomething() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		boolean somethingHasBeenRead = false;
		Character charRead = null;
		try {
			charRead = (char)testee.read();
			somethingHasBeenRead = true;
			testee.toInputStream();
		} catch (Exception e) {
			assertThat(charRead).isEqualTo('0');
			assertThat(somethingHasBeenRead).isTrue();
			throw e;
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testToInputStreamIsDenyWhenAlreadyClose() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		boolean hasBeenClosed = false;
		try {
			testee.close();
			hasBeenClosed = true;
			testee.toInputStream();
		} catch (Exception e) {
			assertThat(hasBeenClosed).isTrue();
			throw e;
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testToInputStreamIsDenyWhenCallTwice() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		InputStream stream = StreamMailTestsUtils.newInputStreamFromString("");
		try {
			stream = testee.toInputStream();
			testee.toInputStream();
		} catch (Exception e) {
			assertThat((char)stream.read()).isEqualTo('0');
			throw e;
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testReadIsDenyWhenCallToInputStreamHasBeenDone() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		boolean somethingHasBeenRead = false;
		InputStream stream = StreamMailTestsUtils.newInputStreamFromString("");
		try {
			stream = testee.toInputStream();
			testee.read();
			somethingHasBeenRead = true;
		} catch (Exception e) {
			assertThat((char)stream.read()).isEqualTo('0');
			assertThat(somethingHasBeenRead).isFalse();
			throw e;
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCloseIsDenyWhenCallToInputStreamHasBeenDone() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		boolean hasBeenClosed = false;
		InputStream stream = StreamMailTestsUtils.newInputStreamFromString("");
		try {
			stream = testee.toInputStream();
			testee.close();
			hasBeenClosed = true;
		} catch (Exception e) {
			assertThat((char)stream.read()).isEqualTo('0');
			assertThat(hasBeenClosed).isFalse();
			throw e;
		}
	}
	
	@Test
	public void testToInputStreamIsAllowedAfterMarkAndReset() throws Exception {
		EmailReader testee = new EmailReader(new StringReader("0123456789"));
		
		testee.mark(100);
		testee.reset();
		InputStream stream = testee.toInputStream();
		
		assertThat((char)stream.read()).isEqualTo('0');
	}
	
	private InputStream in(String data, Charset charset) {
		return StreamMailTestsUtils.newInputStreamFromString(data, charset);
	}
	
}
