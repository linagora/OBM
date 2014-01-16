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
package org.obm.push.technicallog.jaxb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.jaxb.JAXBBean;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.bean.jaxb.Resource;
import org.obm.push.technicallog.bean.jaxb.Transaction;
import org.obm.push.technicallog.jaxb.JAXBParser;

import com.google.common.collect.Lists;

public class JAXBParserTest {
	
	@Test
	public void testMarshalResource() throws JAXBException {
		DateTime now = DateTime.now();
		Resource resource = Resource.builder()
				.resourceId(Long.valueOf(1))
				.resourceType(ResourceType.HTTP_CLIENT)
				.transactionId(Long.valueOf(2))
				.resourceStartTime(now)
				.build();
		
		String expectedLog = 
			"<resource resourceId=\"1\">" +
				"<resourceType>HTTP_CLIENT</resourceType>" +
				"<transactionId>2</transactionId>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>";
		
		String log = marshal(resource);
		
		assertThat(log).isEqualTo(expectedLog);
	}

	@Test
	public void testMarshalRequest() throws JAXBException {
		DateTime now = DateTime.now();
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Ping")
				.requestId(12)
				.transactionId(Long.valueOf(1))
				.requestStartTime(now)
				.add(Resource.builder()
						.resourceId(Long.valueOf(1))
						.resourceType(ResourceType.HTTP_CLIENT)
						.transactionId(Long.valueOf(1))
						.resourceStartTime(now)
						.build())
				.add(Resource.builder()
						.resourceId(Long.valueOf(2))
						.transactionId(Long.valueOf(1))
						.resourceType(ResourceType.IMAP_CONNECTION)
						.resourceStartTime(now)
						.build())
				.build();
		
		String expectedLog = 
			"<request deviceId=\"devId\" deviceType=\"devType\" command=\"Ping\" requestId=\"12\">" +
				"<transactionId>1</transactionId>" +
				"<requestStartTime>" + now + "</requestStartTime>" +
				"<resources resourceId=\"1\">" +
					"<resourceType>HTTP_CLIENT</resourceType>" +
					"<transactionId>1</transactionId>" +
					"<resourceStartTime>" + now + "</resourceStartTime>" +
				"</resources>" +
				"<resources resourceId=\"2\">" +
					"<resourceType>IMAP_CONNECTION</resourceType>" +
					"<transactionId>1</transactionId>" +
					"<resourceStartTime>" + now + "</resourceStartTime>" +
				"</resources>" +
			"</request>";
		
		String log = marshal(request);
		
		assertThat(log).isEqualTo(expectedLog);
	}

	@Test
	public void testMarshalTransaction() throws JAXBException {
		DateTime now = DateTime.now();
		Long transactionId = Long.valueOf(12);
		Transaction transaction = Transaction.builder()
				.id(transactionId)
				.transactionStartTime(now)
				.build();
		
		String expectedLog = 
			"<transaction id=\"" + transactionId + "\">" +
				"<transactionStartTime>" + now + "</transactionStartTime>" +
			"</transaction>";
			
		String log = marshal(transaction);
		
		assertThat(log).isEqualTo(expectedLog);
	}

	@Test
	public void testMarshalMultipleBeans() throws JAXBException {
		DateTime now = DateTime.now();
		Resource resource = Resource.builder()
				.resourceId(Long.valueOf(1))
				.resourceType(ResourceType.HTTP_CLIENT)
				.transactionId(Long.valueOf(12))
				.resourceStartTime(now)
				.build();
		Resource resource2 = Resource.builder()
				.resourceId(Long.valueOf(2))
				.resourceType(ResourceType.IMAP_CONNECTION)
				.transactionId(Long.valueOf(12))
				.resourceStartTime(now)
				.build();
		
		String expectedLog = 
			"<resource resourceId=\"1\">" +
				"<resourceType>HTTP_CLIENT</resourceType>" +
				"<transactionId>12</transactionId>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>" +
			"<resource resourceId=\"2\">" +
				"<resourceType>IMAP_CONNECTION</resourceType>" +
				"<transactionId>12</transactionId>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>";
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JAXBParser.marshal(resource, byteArrayOutputStream);
		JAXBParser.marshal(resource2, byteArrayOutputStream);
		String log = new String(byteArrayOutputStream.toByteArray());
		
		assertThat(log).isEqualTo(expectedLog);
	}
	
	private String marshal(JAXBBean toMarshall) throws JAXBException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JAXBParser.marshal(toMarshall, byteArrayOutputStream);
		return new String(byteArrayOutputStream.toByteArray());
	}
	
	@Test
	public void testUnmarshalResource() throws JAXBException {
		DateTime now = DateTime.now();
		String log = 
			"<resource resourceId=\"1\">" +
				"<resourceType>HTTP_CLIENT</resourceType>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>";
			
		Resource expectedResource = Resource.builder()
				.resourceId(Long.valueOf(1))
				.resourceType(ResourceType.HTTP_CLIENT)
				.resourceStartTime(now)
				.build();
			
		byte[] bytes = log.getBytes();
		Resource resource = JAXBParser.unmarshal(Resource.class, new ByteArrayInputStream(bytes));
		
		assertThat(resource).isEqualTo(expectedResource);
	}
	
	@Test
	public void testUnmarshalRequest() throws JAXBException {
		DateTime now = DateTime.now();
		String log = 
			"<request deviceId=\"devId\" deviceType=\"devType\" command=\"Ping\">" +
				"<transactionId>1</transactionId>" +
				"<requestStartTime>" + now + "</requestStartTime>" +
				"<resources resourceId=\"1\">" +
					"<resourceType>HTTP_CLIENT</resourceType>" +
					"<resourceStartTime>" + now + "</resourceStartTime>" +
				"</resources>" +
				"<resources resourceId=\"2\">" +
					"<resourceType>IMAP_CONNECTION</resourceType>" +
					"<resourceStartTime>" + now + "</resourceStartTime>" +
				"</resources>" +
			"</request>";
		
		Request expectedRequest = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Ping")
				.transactionId(Long.valueOf(1))
				.requestStartTime(now)
				.add(Resource.builder()
						.resourceId(Long.valueOf(1))
						.resourceType(ResourceType.HTTP_CLIENT)
						.resourceStartTime(now)
						.build())
				.add(Resource.builder()
						.resourceId(Long.valueOf(2))
						.resourceType(ResourceType.IMAP_CONNECTION)
						.resourceStartTime(now)
						.build())
				.build();
		
		byte[] bytes = log.getBytes();
		Request request = JAXBParser.unmarshal(Request.class, new ByteArrayInputStream(bytes));
		
		assertThat(request).isEqualTo(expectedRequest);
	}

	@Test
	public void testUnmarshalTransaction() throws JAXBException {
		DateTime now = DateTime.now();
		Long transactionId = Long.valueOf(12);
		String log = 
			"<transaction id=\"" + transactionId + "\">" +
				"<transactionStartTime>" + now + "</transactionStartTime>" +
			"</transaction>";
		
		Transaction expectedTransaction = Transaction.builder()
				.id(transactionId)
				.transactionStartTime(now)
				.build();
		
		byte[] bytes = log.getBytes();
		Transaction transaction = JAXBParser.unmarshal(Transaction.class, new ByteArrayInputStream(bytes));
		
		assertThat(transaction).isEqualTo(expectedTransaction);
	}

	@Test
	public void testUnmarshalEmptyStream() throws Exception {
		List<JAXBBean> beans = JAXBParser.unmarshal(new ByteArrayInputStream(new byte[] {} ));
		assertThat(beans).hasSize(0);
	}

	@Test
	public void testUnmarshaStreamSingleBean() throws Exception {
		DateTime now = DateTime.now();
		int resourceId = 1;
		String log = 
			"<resource resourceId=\"" + resourceId + "\">" +
				"<resourceType>HTTP_CLIENT</resourceType>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>";
			
		List<Resource> expectedResources = Lists.newArrayList();
		expectedResources.add(Resource.builder()
				.resourceId(Long.valueOf(resourceId))
				.resourceType(ResourceType.HTTP_CLIENT)
				.resourceStartTime(now)
				.build());
		
		byte[] bytes = log.getBytes();
		List<JAXBBean> beans = JAXBParser.unmarshal(new ByteArrayInputStream(bytes));
		assertThat(beans).hasSize(resourceId);
		assertThat(beans).isEqualTo(beans);
	}

	@Test
	public void testUnmarshalStreamComplexeMultipleBeans() throws Exception {
		DateTime now = DateTime.now();
		Long transactionId = Long.valueOf(12);
		Long resourceId = Long.valueOf(1);
		Long resourceId2 = Long.valueOf(2);
		String log = 
			"<transaction id=\"" + transactionId + "\">" +
				"<transactionStartTime>" + now + "</transactionStartTime>" +
			"</transaction>" +
			"<resource resourceId=\"" + resourceId + "\">" +
				"<resourceType>HTTP_CLIENT</resourceType>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>" +
			"<request deviceId=\"devId\" deviceType=\"devType\" command=\"Ping\">" +
				"<transactionId>1</transactionId>" +
				"<requestStartTime>" + now + "</requestStartTime>" +
				"<resources resourceId=\"" + resourceId2 + "\">" +
					"<resourceType>IMAP_CONNECTION</resourceType>" +
					"<resourceStartTime>" + now + "</resourceStartTime>" +
				"</resources>" +
			"</request>" +
			"<resource resourceId=\"2\">" +
				"<resourceType>IMAP_CONNECTION</resourceType>" +
				"<resourceStartTime>" + now + "</resourceStartTime>" +
			"</resource>";
			
		List<JAXBBean> expectedResources = Lists.newArrayList();
		expectedResources.add(Transaction.builder()
				.id(transactionId)
				.transactionStartTime(now)
				.build());
		expectedResources.add(Resource.builder()
				.resourceId(Long.valueOf(resourceId))
				.resourceType(ResourceType.HTTP_CLIENT)
				.resourceStartTime(now)
				.build());
		Resource resource2 = Resource.builder()
				.resourceId(resourceId2)
				.resourceType(ResourceType.IMAP_CONNECTION)
				.resourceStartTime(now)
				.build();
		expectedResources.add(Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Ping")
				.transactionId(resourceId)
				.requestStartTime(now)
				.add(resource2)
				.build());
		expectedResources.add(resource2);
		
		byte[] bytes = log.getBytes();
		List<JAXBBean> beans = JAXBParser.unmarshal(new ByteArrayInputStream(bytes));
		assertThat(beans).hasSize(4);
		assertThat(beans).isEqualTo(beans);
	}
}