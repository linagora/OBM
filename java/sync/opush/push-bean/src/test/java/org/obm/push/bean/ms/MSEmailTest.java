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
package org.obm.push.bean.ms;

import java.io.ByteArrayInputStream;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSEventExtId;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;


public class MSEmailTest {

	@Test(expected=IllegalStateException.class)
	public void testMSEmailBuilderRequireBody() {
		MSEmail.builder()
		.header(MSEmailHeader.builder().build())
		.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testMSEmailBuilderRequireHeader() {
		MSEmail.builder()
				.body(MSEmailBody.builder()
						.mimeData(new SerializableInputStream(new ByteArrayInputStream("text".getBytes())))
						.bodyType(MSEmailBodyType.PlainText)
						.estimatedDataSize(0)
						.charset(Charsets.UTF_8)
						.truncated(false)
						.build())
				.build();
	}
	
	@Test
	public void testMSEmailBuilderDefaultImportanceValue() {
		MSEmail.MSEmailBuilder msEmailBuilder = requirementsInitializedBuilder();
		
		MSEmail msEmail = msEmailBuilder
				.build();
	
		Assertions.assertThat(msEmail.getImportance()).isEqualTo(MSImportance.NORMAL);
	}
	
	@Test
	public void testMSEmailBuilderDefaultMessageClassValue() {
		MSEmail.MSEmailBuilder msEmailBuilder = requirementsInitializedBuilder();
		
		MSEmail msEmail = msEmailBuilder
				.meetingRequest(anyMeetingRequest())
				.build();
	
		Assertions.assertThat(msEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	@Test
	public void testMSEmailBuilderMessageClassCanceled() {
		MSEmail.MSEmailBuilder msEmailBuilder = requirementsInitializedBuilder();
		
		MSEmail msEmail = msEmailBuilder
				.meetingRequest(anyMeetingRequest(), MSMessageClass.SCHEDULE_MEETING_CANCELED)
				.build();
	
		Assertions.assertThat(msEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_CANCELED);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMSEmailBuilderPreconditionOnMessageClass() {
		MSEmail.MSEmailBuilder msEmailBuilder = requirementsInitializedBuilder();

		MSEmail msEmail = msEmailBuilder
				.meetingRequest(anyMeetingRequest(), null)
				.build();
	
		Assertions.assertThat(msEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	@Test
	public void testMSEmailSubjectNull() {
		MSEmail msEmail = requirementsInitializedBuilder()
				.subject(null)
				.build();
	
		Assertions.assertThat(msEmail.getSubject()).isNull();
	}
	
	@Test
	public void testMSEmailSubjectEmpty() {
		MSEmail msEmail = requirementsInitializedBuilder()
				.subject("")
				.build();
	
		Assertions.assertThat(msEmail.getSubject()).isNull();
	}

	@Test
	public void testMSEmailSubjectValue() {
		MSEmail msEmail = requirementsInitializedBuilder()
				.subject("a subject")
				.build();
	
		Assertions.assertThat(msEmail.getSubject()).isEqualTo("a subject");
	}

	private MSEmail.MSEmailBuilder requirementsInitializedBuilder() {
		String message = "text";
		MSEmailBodyType emailBodyType = MSEmailBodyType.PlainText;
		return MSEmail.builder()
				.header(MSEmailHeader.builder().build())
					.body(MSEmailBody.builder()
							.mimeData(new SerializableInputStream(new ByteArrayInputStream(message.getBytes())))
							.bodyType(emailBodyType)
							.estimatedDataSize(0)
							.charset(Charsets.UTF_8)
							.truncated(false)
							.build());
	}

	private MSMeetingRequest anyMeetingRequest() {
		return MSMeetingRequest.builder()
			.msEventExtId(new MSEventExtId("anyExtId"))
			.instanceType(MSMeetingRequestInstanceType.SINGLE)
			.startTime(new DateTime("2012-07-10T07:00:00").toDate())
			.dtStamp(new DateTime("2012-04-24T07:57:07").toDate())
			.endTime(new DateTime("2012-07-10T07:15:00").toDate())
			.build();
	}
}
