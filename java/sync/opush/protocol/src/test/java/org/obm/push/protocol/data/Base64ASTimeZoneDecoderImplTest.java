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
package org.obm.push.protocol.data;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.IntEncoder;


public class Base64ASTimeZoneDecoderImplTest {

	private Base64ASTimeZoneDecoderImpl base64asTimeZoneDecoder;

	@Before
	public void before() {
		base64asTimeZoneDecoder = new Base64ASTimeZoneDecoderImpl(
				new WCHAREncoder(), new IntEncoder(), new SystemTimeEncoder());
	}
	
	@Test
	public void testDecodeEuropeParisBase64ASTimeZone() {
		ASTimeZone actualASTimeZone = toASTimeZone("Europe/Paris");
		byte[] base64asTimeZone = toBase64(actualASTimeZone);

		ASTimeZone expectedASTimeZone = base64asTimeZoneDecoder.decode(base64asTimeZone);
		
		Assertions.assertThat(actualASTimeZone.getBias()).isEqualTo(expectedASTimeZone.getBias());
		Assertions.assertThat(actualASTimeZone.getStandardBias()).isEqualTo(expectedASTimeZone.getStandardBias());
		Assertions.assertThat(actualASTimeZone.getDayLightBias()).isEqualTo(expectedASTimeZone.getDayLightBias());
		Assertions.assertThat(actualASTimeZone.getDayLightDate()).isEqualTo(expectedASTimeZone.getDayLightDate());
		Assertions.assertThat(actualASTimeZone.getStandardDate()).isEqualTo(expectedASTimeZone.getStandardDate());
	}

	private ASTimeZone toASTimeZone(String timeZoneID) {
		return new TimeZoneConverterImpl().
				convert(TimeZone.getTimeZone(timeZoneID), Locale.US);
	}

	private byte[] toBase64(ASTimeZone asTimeZone) {
		TimeZoneEncoderImpl timeZoneEncoderImpl = 
				new TimeZoneEncoderImpl(new IntEncoder(), new WCHAREncoder(), new SystemTimeEncoder());
		return Base64.encodeBase64(timeZoneEncoderImpl.encode(asTimeZone));
	}
}
