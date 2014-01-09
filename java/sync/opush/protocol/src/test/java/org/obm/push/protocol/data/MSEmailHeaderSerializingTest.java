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

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.utils.SerializableInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Charsets;


public class MSEmailHeaderSerializingTest {

	private SimpleDateFormat sdf;
	private SerializingTest serializingTest;
	private MSEmailBody simpleBody;

	@Before
	public void setUp() {
		sdf = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_PATTERN);
		serializingTest = new SerializingTest();
		simpleBody = MSEmailBody.builder()
				.mimeData(new SerializableInputStream(new ByteArrayInputStream("text".getBytes())))
				.bodyType(MSEmailBodyType.PlainText)
				.estimatedDataSize(0)
				.charset(Charsets.UTF_8)
				.truncated(false)
				.build();
	}
	
	@Test
	public void testSerializeFullMSEmailHeaderObject() {
		Date date = DateUtils.date("2012-02-05T11:46:32");
		
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
				.body(simpleBody)
					.header(MSEmailHeader.builder()
					.from(new MSAddress("from@obm.lng.org"))
					.replyTo(new MSAddress("from@mydomain.org"))
					.cc(new MSAddress("cc@obm.lng.org"))
					.to(new MSAddress("to.1@obm.lng.org"), new MSAddress("to.2@obm.lng.org"))
					.date(date)
					.build())
				.subject("Subject")
				.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tagValue(parentElement, ASEmail.FROM)).isEqualTo(" <from@obm.lng.org> ");
		Assertions.assertThat(tagValue(parentElement, ASEmail.REPLY_TO)).isEqualTo(" <from@mydomain.org> ");
		Assertions.assertThat(tagValue(parentElement, ASEmail.CC)).isEqualTo(" <cc@obm.lng.org> ");
		Assertions.assertThat(tagValue(parentElement, ASEmail.TO)).isEqualTo(" <to.1@obm.lng.org> , <to.2@obm.lng.org> ");
		Assertions.assertThat(tagValue(parentElement, ASEmail.DISPLAY_TO)).isEqualTo(" <to.1@obm.lng.org> ");
		Assertions.assertThat(tagValue(parentElement, ASEmail.SUBJECT)).isEqualTo("Subject");
		Assertions.assertThat(tagValue(parentElement, ASEmail.DATE_RECEIVED)).isEqualTo(sdf.format(date));
	}

	@Test
	public void testSerializeFrom() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.body(simpleBody)
			.header(MSEmailHeader.builder()
				.from(new MSAddress("from@obm.lng.org")).build())
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tagValue(parentElement, ASEmail.FROM)).isEqualTo(" <from@obm.lng.org> ");
	}
	
	@Test
	public void testSerializeEmptyFrom() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tagValue(parentElement, ASEmail.FROM)).isEqualTo("\"Empty From\" <o-push@linagora.com> ");
	}
	
	@Test
	public void testSerializeNullSubject() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tag(parentElement, ASEmail.SUBJECT)).isNull();
	}
	
	@Test
	public void testSerializeEmptySubject() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.subject("")
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tag(parentElement, ASEmail.SUBJECT)).isNull();
	}
	
	@Test
	public void testSerializeSubject() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.subject("a subject")
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tagValue(parentElement, ASEmail.SUBJECT)).isEqualTo("a subject");
	}
	
	@Test
	public void testSerializeOnlySpaceSubject() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.subject(" ")
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tagValue(parentElement, ASEmail.SUBJECT)).isEqualTo(" ");
	}
	
	@Test
	public void testSerializeEmptyField() {
		Element parentElement = createRootDocument();
		MSEmail msEmail = MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(simpleBody)
			.build();
		
		new MSEmailHeaderSerializer(parentElement, msEmail).serializeMSEmailHeader();
		
		Assertions.assertThat(tag(parentElement, ASEmail.CC)).isNull();
		Assertions.assertThat(tag(parentElement, ASEmail.TO)).isNull();
		Assertions.assertThat(tag(parentElement, ASEmail.DATE_RECEIVED)).isNull();
		Assertions.assertThat(tag(parentElement, ASEmail.DISPLAY_TO)).isNull();
	}

	private Node tag(Element element, ASEmail asemail) {
		return serializingTest.tag(element, asemail);
	}

	private String tagValue(Element element, ASEmail asemail) {
		return serializingTest.tagValue(element, asemail);
	}

	private Element createRootDocument() {
		return serializingTest.createRootDocument();
	}
}
