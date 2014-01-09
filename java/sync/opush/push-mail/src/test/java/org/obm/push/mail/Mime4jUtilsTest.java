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
package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.field.address.LenientAddressBuilder;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MessageImpl;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.MimeContentType;

import com.google.common.base.Charsets;


public class Mime4jUtilsTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void parseInvalidEmailDontThrowIndexOutOfBound() {
		AddressList addressList = LenientAddressBuilder.DEFAULT.parseAddressList("To: <@domain.com>");
		Assertions.assertThat(addressList).isNotNull();
	}
	
	@Test
	public void testIsAttachmentExist() throws IOException {
		MessageImpl message = mime4jUtils.createMessage();
		
		Multipart multipart = mime4jUtils.createMultipartMixed();

		BodyPart part = mime4jUtils.createTextPart("Body Text", "plain");
		multipart.addBodyPart(part);
		
		ByteArrayInputStream attachment = 
				new ByteArrayInputStream(new String("Text attachment").getBytes());
		mime4jUtils.attach(multipart, attachment, "attachment.txt", "message/rfc822");

		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(Charsets.UTF_8);
		message.setBody(multipart, MimeContentType.MULTIPART_MIXED.getContentType(), params);
		
		int attachmentCount = mime4jUtils.getAttachmentCount((Multipart) message.getBody());
		boolean attachmentsExist = mime4jUtils.isAttachmentsExist(message);

		Assertions.assertThat(attachmentCount).isEqualTo(1);
		Assertions.assertThat(attachmentsExist).isTrue();
	}
	
	@Test
	public void testIsAttachmentExistWithMultipartAlternative() throws IOException {
		MessageImpl message = mime4jUtils.createMessage();
		
		Multipart multipart = mime4jUtils.createMultipartAlternative();

		BodyPart plainBodyPart = mime4jUtils.createTextPart("Body Text", "plain");
		BodyPart htmlBodyPart = mime4jUtils.createTextPart("<html><body>html body</body></html>", "html");
		multipart.addBodyPart(plainBodyPart);
		multipart.addBodyPart(htmlBodyPart);
		
		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(Charsets.UTF_8);
		message.setBody(multipart, MimeContentType.MULTIPART_ALTERNATIVE.getContentType(), params);
		
		int attachmentCount = mime4jUtils.getAttachmentCount((Multipart) message.getBody());
		boolean attachmentsExist = mime4jUtils.isAttachmentsExist(message);

		Assertions.assertThat(attachmentCount).isZero();
		Assertions.assertThat(attachmentsExist).isFalse();
	}

	@Test
	public void testIsAttachmentExistWithNoMultipart() throws UnsupportedEncodingException {
		MessageImpl message = mime4jUtils.createMessage();
		Body body = mime4jUtils.createBody("Sample Text !");
		message.setBody(body);
		
		Assertions.assertThat(mime4jUtils.isAttachmentsExist(message)).isFalse();
	}
	

}
