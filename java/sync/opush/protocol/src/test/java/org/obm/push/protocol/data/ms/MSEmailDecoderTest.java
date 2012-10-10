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

package org.obm.push.protocol.data.ms;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.mail.internet.AddressException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.MSAddress;

@RunWith(SlowFilterRunner.class)
public class MSEmailDecoderTest {

	private MSEmailDecoder decoder;

	@Before
	public void setup(){
		decoder = new MSEmailDecoder();
	}

	@Test
	public void parseAddressWhenSimple() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("login@domain.org");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayName() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("<login@domain.org>");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayNameButSpace() throws AddressException {
		List<MSAddress> addresses = decoder.addresses(" <login@domain.org> ");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayNameButInnerSpace() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("< login@domain.org >");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWithDisplayName() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("\"display name\" <login@domain.org>");
		assertThat(addresses).containsOnly(new MSAddress("display name", "login@domain.org"));
	}
	
	@Test
	public void parseAddressReturnsEmptyAsItsOptionalWhenNull() throws AddressException {
		assertThat(decoder.addresses(null)).isEmpty();
	}
	
	@Test
	public void parseAddressReturnsEmptyAsItsOptionalWhenEmpty() throws AddressException {
		assertThat(decoder.addresses("")).isEmpty();
	}
	
	@Test(expected=AddressException.class)
	public void parseAddressWithoutEmailData() throws AddressException {
		decoder.addresses("\"display name\" <>");
	}
	
	@Test(expected=AddressException.class)
	public void parseAddressWithoutEmail() throws AddressException {
		decoder.addresses("\"display name\"");
	}

}
