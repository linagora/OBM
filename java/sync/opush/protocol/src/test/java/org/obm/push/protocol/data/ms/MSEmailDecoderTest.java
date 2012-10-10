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
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.mail.internet.AddressException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;

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
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsPunctuation() throws ParseException {
		decoder.date("20021126T160000Z");
	}
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsTime() throws ParseException {
		decoder.date("2002-11-26");
	}
	
	@Test
	public void parseDateReturnNullAsItsOptionalWhenNull() throws ParseException {
		assertThat(decoder.date(null)).isNull();
	}

	@Test
	public void parseDateReturnNullAsItsOptionalWhenEmpty() throws ParseException {
		assertThat(decoder.date("")).isNull();
	}
	
	@Test
	public void parseDate() throws ParseException {
		Date parsed = decoder.date("2000-12-25T08:35:00.000Z");
		assertThat(parsed).isEqualTo(date("2000-12-25T08:35:00+00"));
	}
	
	@Test
	public void parseBody() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isEqualTo(MSEmailBodyType.HTML);
		assertThat(body.getEstimatedDataSize()).isEqualTo(930);
		assertThat(body.isTruncated()).isTrue();
		assertThat(body.getMimeData()).hasContentEqualTo(new ByteArrayInputStream("Email data".getBytes(Charsets.UTF_8)));
	}

	@Test
	public void parseBodyTruncatedTrue() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.isTruncated()).isTrue();
	}

	@Test
	public void parseBodyTruncatedFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>0</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.isTruncated()).isFalse();
	}

	@Test
	public void parseBodyTypeMime() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>4</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>0</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isEqualTo(MSEmailBodyType.MIME);
	}
	
	@Test
	public void parseBodyHasOptionalData() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getMimeData()).isNull();
	}
	
	@Test
	public void parseBodyHasOptionalType() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isNull();
	}
}
