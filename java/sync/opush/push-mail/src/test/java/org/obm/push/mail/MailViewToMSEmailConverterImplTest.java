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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.icalendar.ICalendar;
import org.obm.icalendar.ical4jwrapper.ICalendarEvent;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.conversation.EmailViewAttachment;
import org.obm.push.mail.conversation.EmailViewInvitationType;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.service.EventService;
import org.obm.push.utils.UserEmailParserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class MailViewToMSEmailConverterImplTest {

	public static class EmailViewFixture {
		long uid = 1l;
		
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
		
		List<Address> from = ImmutableList.of(new Address("from@domain.test")); 
		List<Address> to = ImmutableList.of(new Address("to@domain.test")); 
		List<Address> cc = ImmutableList.of(new Address("cc@domain.test"));
		String subject = "a subject";
		Date date = DateUtils.date("2004-12-14T22:00:00");

		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
		int estimatedDataSize = 0;
		MSEmailBodyType bodyType = MSEmailBodyType.PlainText;
		List<EmailViewAttachment> attachments = ImmutableList.of(EmailViewAttachment.builder()
				.id("id")
				.displayName(subject)
				.fileReference("file")
				.size(20)
				.inline(true)
				.build());
		InputStream attachmentInputStream = resourceAsStream("ics/attendee.ics");
		ICalendar iCalendar = null;
		EmailViewInvitationType invitationType = EmailViewInvitationType.REQUEST;

		private InputStream resourceAsStream(String file) {
			return ClassLoader.getSystemClassLoader().getResourceAsStream(file);
		}
		Boolean truncated = false;
	}

	private EmailViewFixture emailViewFixture;
	
	@Before
	public void setUp() {
		emailViewFixture = new EmailViewFixture();
	}
	
	@Test
	public void testFlagAnsweredPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.answered = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isAnswered()).isTrue();
	}

	@Test
	public void testFlagAnsweredNotPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.answered = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isAnswered()).isFalse();
	}
	
	@Test
	public void testFlagStarredPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.starred = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isStarred()).isTrue();
	}

	@Test
	public void testFlagStarredNotPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.starred = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isStarred()).isFalse();
	}
	
	@Test
	public void testFlagReadPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.read = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isRead()).isTrue();
	}

	@Test
	public void testFlagReadNotPresent() throws IOException, ParserException, DaoException {
		emailViewFixture.read = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.isRead()).isFalse();
	}

	@Test
	public void testUid() throws IOException, ParserException, DaoException {
		emailViewFixture.uid = 54;
		
		UidMSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getUid()).isEqualTo(54);
	}

	@Test
	public void testHeaderFromNull() throws IOException, ParserException, DaoException {
		emailViewFixture.from = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(MSEmailHeader.DEFAULT_FROM_ADDRESS);
	}

	@Test
	public void testHeaderFromEmpty() throws IOException, ParserException, DaoException {
		emailViewFixture.from = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(MSEmailHeader.DEFAULT_FROM_ADDRESS);
	}
	
	@Test
	public void testHeaderFromSingle() throws IOException, ParserException, DaoException {
		emailViewFixture.from = ImmutableList.of(new Address("from@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(new MSAddress("from@domain.test"));
	}
	
	@Test
	public void testHeaderFromMultiple() throws IOException, ParserException, DaoException {
		emailViewFixture.from = ImmutableList.of(
				new Address("from@domain.test"), new Address("from2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getFrom()).containsOnly(
				new MSAddress("from@domain.test"), new MSAddress("from2@domain.test"));
	}

	@Test
	public void testHeaderToNull() throws IOException, ParserException, DaoException {
		emailViewFixture.to = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).isEmpty();
	}

	@Test
	public void testHeaderToEmpty() throws IOException, ParserException, DaoException {
		emailViewFixture.to = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).isEmpty();
	}
	
	@Test
	public void testHeaderToSingle() throws IOException, ParserException, DaoException {
		emailViewFixture.to = ImmutableList.of(new Address("to@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).containsOnly(new MSAddress("to@domain.test"));
	}
	
	@Test
	public void testHeaderToMultiple() throws IOException, ParserException, DaoException {
		emailViewFixture.to = ImmutableList.of(
				new Address("to@domain.test"), new Address("to2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getTo()).containsOnly(
				new MSAddress("to@domain.test"), new MSAddress("to2@domain.test"));
	}

	@Test
	public void testHeaderCcNull() throws IOException, ParserException, DaoException {
		emailViewFixture.cc = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).isEmpty();
	}

	@Test
	public void testHeaderCcEmpty() throws IOException, ParserException, DaoException {
		emailViewFixture.cc = ImmutableList.of(newEmptyAddress());

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).isEmpty();
	}
	
	@Test
	public void testHeaderCcSingle() throws IOException, ParserException, DaoException {
		emailViewFixture.cc = ImmutableList.of(new Address("cc@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).containsOnly(new MSAddress("cc@domain.test"));
	}
	
	@Test
	public void testHeaderCcMultiple() throws IOException, ParserException, DaoException {
		emailViewFixture.cc = ImmutableList.of(
				new Address("cc@domain.test"), new Address("cc2@domain.test")); 

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getCc()).containsOnly(
				new MSAddress("cc@domain.test"), new MSAddress("cc2@domain.test"));
	}
	
	@Test
	public void testHeaderSubjectNull() throws IOException, ParserException, DaoException {
		emailViewFixture.subject = null;
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderSubjectEmpty() throws IOException, ParserException, DaoException {
		emailViewFixture.subject = "";
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();

		assertThat(convertedMSEmail.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderSubjectIfNoInvitation() throws Exception {
		emailViewFixture.subject = "a subject";
		emailViewFixture.attachmentInputStream = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isEqualTo("a subject");
	}
	
	@Test
	public void testInvitationSubjectIsPriorOnSubject() throws Exception {
		emailViewFixture.attachmentInputStream = emailViewFixture.resourceAsStream("ics/attendee.ics");
		emailViewFixture.subject = "a subject";
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isEqualTo("Nouvel événement");
	}

	@Test
	public void testInvitationSubjectNull() throws Exception {
		emailViewFixture.attachmentInputStream = emailViewFixture.resourceAsStream("ics/no_summary.ics");
		emailViewFixture.subject = "a subject";
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getSubject()).isNull();
	}

	@Test
	public void testInvitationSubjectEmptyGetsEmailSubject() throws Exception {
		emailViewFixture.attachmentInputStream = emailViewFixture.resourceAsStream("ics/empty_summary.ics");
		emailViewFixture.subject = "a subject";
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();

		assertThat(convertedMSEmail.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderDateNull() throws IOException, ParserException, DaoException {
		emailViewFixture.date = null;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getDate()).isNull();
	}
	
	@Test
	public void testHeaderDate() throws IOException, ParserException, DaoException {
		emailViewFixture.date = DateUtils.date("2004-12-14T22:00:00");

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getDate()).isEqualTo(DateUtils.date("2004-12-14T22:00:00"));
	}
	
	@Test
	public void testNullEstimatedDataSize() throws IOException, ParserException, DaoException {
		emailViewFixture.estimatedDataSize = 0;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getEstimatedDataSize()).isEqualTo(0);
	}
	
	@Test
	public void testEstimatedDataSize() throws IOException, ParserException, DaoException {
		emailViewFixture.estimatedDataSize = 1024;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getEstimatedDataSize()).isEqualTo(1024);
	}
	
	@Test
	public void testBodyContentTypePlainText() throws IOException, ParserException, DaoException {
		emailViewFixture.bodyType = MSEmailBodyType.HTML;

		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().getBodyType()).isEqualTo(MSEmailBodyType.HTML);
	}

	@Test
	public void testWithoutAttachments() throws IOException, ParserException, DaoException {
		emailViewFixture.attachments = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getAttachments()).isEmpty();
	}
	
	@Test
	public void testAttachments() throws IOException, ParserException, DaoException {
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getAttachments()).hasSize(1);
	}

	@Test
	public void testWithoutMeetingRequest() throws IOException, ParserException, DaoException {
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMeetingRequest()).isNull();
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.NOTE);
	}
	
	@Test
	public void testMeetingRequest() throws IOException, ParserException, DaoException {
		emailViewFixture.invitationType = EmailViewInvitationType.REQUEST;
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMeetingRequest()).isNotNull();
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	@Test
	public void testEventExceptionMeetingRequest() throws IOException, ParserException, DaoException {
		emailViewFixture.attachmentInputStream = null;
		
		ICalendar iCalendar = createMock(ICalendar.class);
		ICalendarEvent iCalendarEvent = createMock(ICalendarEvent.class);
		expect(iCalendar.hasEvent()).andReturn(true).anyTimes();
		expect(iCalendar.getICalendarEvent()).andReturn(iCalendarEvent).anyTimes();
		expect(iCalendarEvent.recurrenceId()).andReturn(new Date()).anyTimes();
		expect(iCalendarEvent.summary()).andReturn(null).anyTimes();
		replay(iCalendar, iCalendarEvent);
		
		emailViewFixture.iCalendar = iCalendar;
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();

		verify(iCalendar, iCalendarEvent);
		
		assertThat(convertedMSEmail.getMeetingRequest()).isNull();
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.NOTE);
	}
	
	@Test
	public void testNoteMessageClass() throws IOException, ParserException, DaoException {
		emailViewFixture.attachmentInputStream = null;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.NOTE);
	}
	
	@Test
	public void testRequestedMessageClass() throws IOException, ParserException, DaoException {
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_REQUEST);
	}
	
	@Test
	public void testCanceledMessageClass() throws IOException, ParserException, DaoException {
		emailViewFixture.invitationType = EmailViewInvitationType.CANCELED;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getMessageClass()).isEqualTo(MSMessageClass.SCHEDULE_MEETING_CANCELED);
	}
	
	@Test(expected=EmailViewBuildException.class)
	public void testExpectEmailViewExceptionTruncated() throws IOException, ParserException, DaoException {
		emailViewFixture.truncated = null;
		
		makeConversionFromEmailViewFixture();
	}
	
	@Test
	public void testNotTruncated() throws IOException, ParserException, DaoException {
		emailViewFixture.truncated = false;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().isTruncated()).isFalse();
	}
	
	@Test
	public void testTruncated() throws IOException, ParserException, DaoException {
		emailViewFixture.truncated = true;
		
		MSEmail convertedMSEmail = makeConversionFromEmailViewFixture();
		
		assertThat(convertedMSEmail.getBody().isTruncated()).isTrue();
	}
	
	@Test
	public void testMethodWhenNullContentType() {
		ContentType contentType = null;
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}
	
	@Test
	public void testMethodWhenTextPlain() {
		ContentType contentType = ContentType.builder().contentType("text/plain").build();
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}

	@Test
	public void testMethodWhenTextHtml() {
		ContentType contentType = ContentType.builder().contentType("text/html").build();
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}

	@Test
	public void testMethodWhenMultipart() {
		ContentType contentType = ContentType.builder().contentType("multipart/mixed").build();
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}

	@Test
	public void testMethodWhenApplicationPDF() {
		ContentType contentType = ContentType.builder().contentType("application/pdf").build();
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}

	@Test
	public void testMethodWhenImage() {
		ContentType contentType = ContentType.builder().contentType("image/jpeg").build();
		assertMethodForContentType(contentType, MethodAttachment.NormalAttachment);
	}

	@Test
	public void testMethodWhenRfc822() {
		ContentType contentType = ContentType.builder().contentType("message/rfc822").build();
		assertMethodForContentType(contentType, MethodAttachment.EmbeddedMessage);
	}

	private void assertMethodForContentType(ContentType contentType, MethodAttachment expectedMethod) {
		IMocksControl mocks = createControl();
		MSEmailHeaderConverter converter = mocks.createMock(MSEmailHeaderConverter.class);
		EventService service = mocks.createMock(EventService.class);
		
		mocks.replay();
		MethodAttachment method = new MailViewToMSEmailConverterImpl(converter, service).method(contentType);
		mocks.verify();
		
		assertThat(method).isEqualTo(expectedMethod);
	}
	
	private UidMSEmail makeConversionFromEmailViewFixture() throws IOException, ParserException, DaoException {
		EventService eventService = createMock(EventService.class);
		UserDataRequest userDataRequest = createMock(UserDataRequest.class);
		
		return new MailViewToMSEmailConverterImpl(
				new MSEmailHeaderConverter(new UserEmailParserUtils()), eventService)
					.convert(newEmailViewFromFixture(), userDataRequest);
	}

	private void buildICalendar() throws IOException, ParserException {
		if (emailViewFixture.attachmentInputStream != null) {
			emailViewFixture.iCalendar = ICalendar.builder().inputStream(emailViewFixture.attachmentInputStream).build();
		}
	}
	
	private EmailView newEmailViewFromFixture() throws IOException, ParserException {
		buildICalendar();
		
		return EmailView.builder()
			.uid(emailViewFixture.uid)
			.flags(flagsListFromFixture())
			.envelope(envelopeFromFixture())
			.bodyMimePartData(emailViewFixture.bodyData)
			.estimatedDataSize(emailViewFixture.estimatedDataSize)
			.attachments(emailViewFixture.attachments)
			.iCalendar(emailViewFixture.iCalendar)
			.invitationType(emailViewFixture.invitationType)
			.bodyType(emailViewFixture.bodyType)
			.truncated(emailViewFixture.truncated)
			.build();
	}

	private Collection<Flag> flagsListFromFixture() {
		Builder<Flag> flagsListBuilder = ImmutableSet.<Flag>builder();
		if (emailViewFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (emailViewFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		if (emailViewFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		return flagsListBuilder.build();
	}

	private Envelope envelopeFromFixture() {
		return Envelope.builder()
			.from(emailViewFixture.from)
			.to(emailViewFixture.to)
			.cc(emailViewFixture.cc)
			.subject(emailViewFixture.subject)
			.date(emailViewFixture.date)
			.build();
	}

	public Address newEmptyAddress() {
		return new Address("");
	}
	
	public MSAddress newEmptyMSAddress() {
		return new MSAddress("");
	}
}
