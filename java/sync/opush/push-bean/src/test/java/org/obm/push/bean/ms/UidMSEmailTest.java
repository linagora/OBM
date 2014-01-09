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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;

import com.google.common.collect.Sets;


public class UidMSEmailTest {

	@Test(expected=IllegalStateException.class)
	public void testUidMSEmailBuilderRequireUid() {
		UidMSEmail.uidBuilder()
			.email(createMock(MSEmail.class))
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testUidMSEmailBuilderRequireEmail() {
		UidMSEmail.uidBuilder()
			.uid(1l)
			.build();
	}

	@Test
	public void testUidMSEmailBuilderWithRequirement() {
		Set<MSAttachement> expectedAttachments = Sets.newHashSet(new MSAttachement());
		MSEmailBody expectedMSEmailBody = createMock(MSEmailBody.class);
		MSEmailHeader expectedMSHeader = createMock(MSEmailHeader.class);
		MSMeetingRequest expectedMeetingRequest = createMock(MSMeetingRequest.class);
		
		MSEmail msEmailMock = createMock(MSEmail.class);
		expect(msEmailMock.getSubject()).andReturn("a subject");
		expect(msEmailMock.getAttachments()).andReturn(expectedAttachments);
		expect(msEmailMock.getBody()).andReturn(expectedMSEmailBody);
		expect(msEmailMock.getHeader()).andReturn(expectedMSHeader);
		expect(msEmailMock.getImportance()).andReturn(MSImportance.HIGH);
		expect(msEmailMock.getMeetingRequest()).andReturn(expectedMeetingRequest);
		expect(msEmailMock.getMessageClass()).andReturn(MSMessageClass.SCHEDULE_MEETING_CANCELED);
		expect(msEmailMock.isAnswered()).andReturn(true);
		expect(msEmailMock.isStarred()).andReturn(true);
		expect(msEmailMock.isRead()).andReturn(true);
		
		replay(msEmailMock, expectedMSEmailBody, expectedMSHeader, expectedMeetingRequest);
		
		UidMSEmail uidMSEmail = UidMSEmail.uidBuilder()
			.uid(1l)
			.email(msEmailMock)
			.build();
		
		verify(msEmailMock, expectedMSEmailBody, expectedMSHeader, expectedMeetingRequest);
		
		assertThat(uidMSEmail.getSubject()).isEqualTo("a subject");
		assertThat(uidMSEmail.getAttachments()).isEqualTo(expectedAttachments);
		assertThat(uidMSEmail.getBody()).isSameAs(expectedMSEmailBody);
		assertThat(uidMSEmail.getHeader()).isSameAs(expectedMSHeader);
		assertThat(uidMSEmail.getImportance()).isEqualTo(MSImportance.HIGH);
		assertThat(uidMSEmail.getMeetingRequest()).isSameAs(expectedMeetingRequest);
		assertThat(uidMSEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_CANCELED);
		assertThat(uidMSEmail.isAnswered()).isTrue();
		assertThat(uidMSEmail.isAnswered()).isTrue();
		assertThat(uidMSEmail.isAnswered()).isTrue();
	}
}
