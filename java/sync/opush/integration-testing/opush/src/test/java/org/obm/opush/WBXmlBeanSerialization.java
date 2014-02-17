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
package org.obm.opush;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.protocol.data.MSEmailHeaderSerializer;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;


public class WBXmlBeanSerialization {


	@Test
	public void testMSEmailHeaderWBXmlEncoding() throws WBXmlException, IOException {
		Date date = DateUtils.date("2012-02-05T11:46:32");
		
		Document doc = DOMUtils.createDoc("AirSync", "ApplicationData");
		MSEmailHeader msEmailHeader = MSEmailHeader.builder()
			.from(new MSAddress("from@obm.lng.org"))
			.replyTo(new MSAddress("from@mydomain.org"))
			.cc(new MSAddress("cc@obm.lng.org"))
			.to(new MSAddress("to.1@obm.lng.org"), new MSAddress("to.2@obm.lng.org"))
			.date(date)
			.subject("Subject").build();
		
		new MSEmailHeaderSerializer(doc.getDocumentElement(), msEmailWithHeader(msEmailHeader)).serializeMSEmailHeader();
		byte[] wbxml = new WBXMLTools().toWbxml("AirSync", doc);
		assertThat(wbxml).isNotNull().isNotEmpty();
	}

	private MSEmail msEmailWithHeader(MSEmailHeader msEmailHeader) {
		return MSEmail.builder()
			.header(msEmailHeader)
			.body(MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream("text".getBytes())))
					.bodyType(MSEmailBodyType.PlainText)
					.estimatedDataSize(0)
					.charset(Charsets.UTF_8)
					.truncated(false)
					.build())
			.build();
	}
	
}
