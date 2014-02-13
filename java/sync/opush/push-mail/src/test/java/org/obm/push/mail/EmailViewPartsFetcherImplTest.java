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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.property.Organizer;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.icalendar.ICalendar;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.MailException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.conversation.EmailViewAttachment;
import org.obm.push.mail.conversation.EmailViewInvitationType;
import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.BodyParams;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.mime.MimePartImpl;
import org.obm.push.mail.transformer.TestIdentityTransformerFactory;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;


public class EmailViewPartsFetcherImplTest {

	public static class MessageFixture {

		long uid = 1l;
		
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
		
		List<Address> from = ImmutableList.<Address>of(new Address("from@domain.test")); 
		List<Address> to = ImmutableList.<Address>of(new Address("to@domain.test")); 
		List<Address> cc = ImmutableList.<Address>of(new Address("cc@domain.test"));
		String subject = "a subject";
		Date date = DateUtils.date("2004-12-14T22:00:00");

		int estimatedDataSize = 1000;
		MSEmailBodyType bodyType = MSEmailBodyType.PlainText;
		String bodyPrimaryType = "text";
		String bodySubType = "plain";
		String fullMimeType = bodyPrimaryType + "/" + bodySubType;
		String bodyCharset = Charsets.UTF_8.displayName();
		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
		InputStream bodyDataDecoded = StreamMailTestsUtils.newInputStreamFromString("message data");
		InputStream attachment;
		InputStream attachmentDecoded;
		String contentId = "contentId";
		boolean isAttachment = false;
		boolean isInvitation = false;
		boolean isICSAttachment = false;
		String encoding = null;
		Boolean truncated = false;
	}
	
	private MessageFixture messageFixture;
	private String messageCollectionName;
	private Integer messageCollectionId;
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private MimeAddress mimeAddress;
	private IMocksControl control;
	private MimePart mimePart;

	@Before
	public void setUp() throws IOException {
		mailbox = "to@localhost.com";
		password = "password";
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
		
		messageFixture = new MessageFixture();
		messageFixture.attachment = Resources.getResource("ics/attendee.ics").openStream();
		messageFixture.attachmentDecoded = Resources.getResource("ics/attendee.ics").openStream();
		messageCollectionName = IMAP_INBOX_NAME;
		messageCollectionId = 1;
		mimeAddress = new MimeAddress("address");
		control = createControl();
		mimePart = control.createMock(MimePart.class);
	}
	
	@Test
	public void testFlagAnsweredTrue() throws Exception {
		messageFixture.answered = true;
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getFlags()).contains(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagAnsweredFalse() throws Exception {
		messageFixture.answered = false;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getFlags()).doesNotContain(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagReadTrue() throws Exception {
		messageFixture.read = true;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getFlags()).contains(Flag.SEEN);
	}
	
	@Test
	public void testFlagReadFalse() throws Exception {
		messageFixture.read = false;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getFlags()).doesNotContain(Flag.SEEN);
	}
	
	@Test
	public void testFlagStarredTrue() throws Exception {
		messageFixture.starred = true;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getFlags()).contains(Flag.FLAGGED);
	}
	
	@Test
	public void testFlagStarredFalse() throws Exception {
		messageFixture.starred = false;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getFlags()).doesNotContain(Flag.FLAGGED);
	}
	
	@Test
	public void testHeaderFromNull() throws Exception {
		messageFixture.from = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getFrom()).isEmpty();
	}

	@Test
	public void testHeaderFromEmpty() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(newEmptyAddress());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getFrom()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderFrom() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(new Address("from@domain.test")); 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getFrom()).containsOnly(new Address("from@domain.test"));
	}
	
	@Test
	public void testHeaderToNull() throws Exception {
		messageFixture.to = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getTo()).isEmpty();
	}

	@Test
	public void testHeaderToEmpty() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(newEmptyAddress());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getTo()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderToSingle() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(new Address("to@domain.test")); 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getTo()).containsOnly(new Address("to@domain.test"));
	}
	
	@Test
	public void testHeaderToMultiple() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(
				new Address("to@domain.test"), new Address("to2@domain.test")); 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getTo()).containsOnly(
				new Address("to@domain.test"), new Address("to2@domain.test"));
	}
	
	@Test
	public void testHeaderCcNull() throws Exception {
		messageFixture.cc = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getCc()).isEmpty();
	}

	@Test
	public void testHeaderCcEmpty() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(newEmptyAddress());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getCc()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderCcSingle() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(new Address("cc@domain.test")); 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getCc()).containsOnly(new Address("cc@domain.test"));
	}
	
	@Test
	public void testHeaderCcMultiple() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(
				new Address("cc@domain.test"), new Address("cc2@domain.test")); 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getCc()).containsOnly(
				new Address("cc@domain.test"), new Address("cc2@domain.test"));
	}
	
	@Test
	public void testHeaderSubjectNull() throws Exception {
		messageFixture.subject = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderSubjectEmpty() throws Exception {
		messageFixture.subject = "";

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getSubject()).isEmpty();
	}
	
	@Test
	public void testHeaderSubject() throws Exception {
		messageFixture.subject = "a subject";

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getSubject()).isEqualTo("a subject");
	}
	
	@Test
	public void testHeaderDateNull() throws Exception {
		messageFixture.date = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getDate()).isNull();
	}
	
	@Test
	public void testHeaderDate() throws Exception {
		messageFixture.date = DateUtils.date("2004-12-14T22:00:00");

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getDate()).isEqualTo(DateUtils.date("2004-12-14T22:00:00"));
	}
	
	@Test
	public void testUid() throws Exception {
		messageFixture.uid = 165l; 

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getUid()).isEqualTo(165l);
	}
	
	@Test
	public void testBodyTruncationNull() throws Exception {
		messageFixture.estimatedDataSize = 0;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getEstimatedDataSize()).isEqualTo(0);
	}
	
	@Test
	public void testBodyTruncation() throws Exception {
		messageFixture.estimatedDataSize = 1505;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();
		
		assertThat(emailView.getEstimatedDataSize()).isEqualTo(1505);
	}
	
	@Test(expected=EmailViewPartsFetcherException.class)
	public void testBodyMimePartDataNull() throws Exception {
		messageFixture.bodyData = null;
		messageFixture.bodyDataDecoded = null;

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		try {
			assertThat(emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy())).isNull();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void testBodyMimePartData() throws Exception {
		messageFixture.bodyData = StreamMailTestsUtils.newInputStreamFromString("email data");
		messageFixture.bodyDataDecoded = StreamMailTestsUtils.newInputStreamFromString("email data");

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getBodyMimePartData())
			.hasContentEqualTo(StreamMailTestsUtils.newInputStreamFromString("email data"));
	}
	
	@Test
	public void testWithoutAttachment() throws Exception {
		messageFixture.isAttachment = false;
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getAttachments()).isEmpty();
	}
	
	@Test
	public void testAttachment() throws Exception {
		messageFixture.isAttachment = true;
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getAttachments()).hasSize(1);
		EmailViewAttachment emailViewAttachment = Iterables.getOnlyElement(emailView.getAttachments());
		assertThat(emailViewAttachment.getId()).isEqualTo("at_" + messageFixture.uid + "_0");
	}
	
	@Test
	public void testInvitation() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixtureMultipleDecodeMimeStream()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getICalendar()).isNotNull();
		assertThat(emailView.getInvitationType()).isEqualTo(EmailViewInvitationType.REQUEST);
	}
	
	@Test
	public void testContentType() throws Exception {
		String mimeType = "text/html";
		messageFixture.bodyType = MSEmailBodyType.fromMimeType(mimeType);
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getBodyType().getMimeType()).isEqualTo(mimeType);
	}

	@Test
	public void testInvitationInBASE64() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		messageFixture.encoding = "BASE64";
		messageFixture.attachment = Resources.getResource("ics/base64.ics").openStream();
		messageFixture.attachmentDecoded = new Base64InputStream(Resources.getResource("ics/base64.ics").openStream());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixtureMultipleDecodeMimeStream()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getICalendar()).isNotNull();
		assertThat(emailView.getICalendar().getICalendar()).contains("DESCRIPTION:Encoding Invitation to BASE64 !");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvitationInBadEncodingFormat() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		messageFixture.encoding = "QUOTED-PRINTABLE";
		messageFixture.attachment = Resources.getResource("ics/base64.ics").openStream();
		messageFixture.attachmentDecoded = new QuotedPrintableInputStream(Resources.getResource("ics/base64.ics").openStream());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		try {
			emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void testBodyDataInBASE64() throws Exception {
		messageFixture.encoding = "BASE64";
		messageFixture.bodyData = StreamMailTestsUtils.newInputStreamFromString("RW5jb2RpbmcgYm9keURhdGEgdG8gQkFTRTY0ICE=");
		messageFixture.bodyDataDecoded = new Base64InputStream( 
				StreamMailTestsUtils.newInputStreamFromString("RW5jb2RpbmcgYm9keURhdGEgdG8gQkFTRTY0ICE="));

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(messageFixture.uid, new AnyMatchBodyPreferencePolicy());
		control.verify();

		assertThat(emailView.getBodyMimePartData()).hasContentEqualTo(
				StreamMailTestsUtils.newInputStreamFromString("Encoding bodyData to BASE64 !"));
	}
	
	@Test
	public void testNoAttachmentFoundWhenLeafIsNestedMultipartMixed() {
		testNoAttachmentFoundWhenLeafHasContentTypeOf("multipart/mixed");
	}

	@Test
	public void testNoAttachmentFoundWhenLeafIsNestedMultipartAlternative() {
		testNoAttachmentFoundWhenLeafHasContentTypeOf("multipart/alternative");
	}

	private void testNoAttachmentFoundWhenLeafHasContentTypeOf(String contentType) {
		EmailView.Builder shouldGetEmptyAttachmentListViewBuilder = control.createMock(EmailView.Builder.class);
		expect(shouldGetEmptyAttachmentListViewBuilder.attachments(Collections.<EmailViewAttachment>emptyList()))
			.andReturn(shouldGetEmptyAttachmentListViewBuilder)
			.once();
		
		MimePart multipartLeaf = MimePartImpl.builder().contentType(contentType).build();
		int multipartLeafIndex = 5;

		MimePart parentMimePart = control.createMock(MimePart.class);
		expect(parentMimePart.findRootMimePartInTree()).andReturn(parentMimePart);
		expect(parentMimePart.listLeaves(true, true)).andReturn(ImmutableList.of(multipartLeaf)).anyTimes();
		multipartLeaf.defineParent(parentMimePart, multipartLeafIndex);
		
		FetchInstruction fetchInstruction = FetchInstruction.builder()
			.mimePart(parentMimePart)
			.mailTransformation(MailTransformation.NONE)
			.build();
		
		EmailViewPartsFetcherImpl partsFetcher = new EmailViewPartsFetcherImpl(identityMailTransformerFactory(), null, null, null, null, null);
		
		control.replay();
		long messageUid = 1l;
		partsFetcher.fetchAttachments(shouldGetEmptyAttachmentListViewBuilder, fetchInstruction, messageUid);
		
		control.verify();
	}
	
	@Test
	public void testEmlAttachmentIsALeaf() {
		String attachmentName = "attachment.eml";
		long attachmentMailUid = 3;
		int attachmentCollection = 12;
		int attachmentSize = 1337;
		
		MimePart textPlain = MimePartImpl.builder().contentType("text/plain").build();
		MimePart rfc822EmbeddedAttachment = MimePartImpl.builder()
				.contentType("message/rfc822")
				.contentDisposition("attachment")
				.size(attachmentSize)
				.bodyParams(BodyParams.builder().add(new BodyParam("name", attachmentName)).build())
				.addChild(MimePartImpl.builder()
						.contentType("multipart/alternative")
						.addChild(MimePartImpl.builder().contentType("text/plain").build())
						.addChild(MimePartImpl.builder().contentType("text/html").build())
						.build())
				.build();
		MimeMessage message = MimeMessageImpl.builder().addChild(
				MimePartImpl.builder()
					.contentType("multipart/mixed")
					.addChild(textPlain)
					.addChild(rfc822EmbeddedAttachment)
					.build())
				.build();

		String expectedId = "at_" + attachmentMailUid + "_" + "0";
		String expectedFileReference = AttachmentHelper
				.getAttachmentId(
					String.valueOf(attachmentCollection), String.valueOf(attachmentMailUid), 
					rfc822EmbeddedAttachment.getAddress().getAddress(),
					rfc822EmbeddedAttachment.getFullMimeType(),
					rfc822EmbeddedAttachment.getContentTransfertEncoding());
		EmailViewAttachment expectedAttachment = EmailViewAttachment.builder()
				.id(expectedId)
				.displayName(attachmentName)
				.fileReference(expectedFileReference)
				.size(attachmentSize)
				.contentType(ContentType.builder()
						.contentType("message/rfc822")
						.add(BodyParams.builder().add(new BodyParam("name", attachmentName)).build())
						.build())
				.inline(false)
				.build();
		
		EmailView.Builder emailViewBuilder = control.createMock(EmailView.Builder.class);
		expect(emailViewBuilder.attachments(Lists.newArrayList(expectedAttachment))).andReturn(emailViewBuilder);

		FetchInstruction fetchInstruction = FetchInstruction.builder().mimePart(message).build();
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = new EmailViewPartsFetcherImpl(identityMailTransformerFactory(), null, null, null, null, attachmentCollection);
		
		control.replay();
		emailViewPartsFetcherImpl.fetchAttachments(emailViewBuilder, fetchInstruction, attachmentMailUid);
		
		control.verify();
	}
	
	@Test
	public void testDisplayNameWhenNoNameNoContentId() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePartImpl.builder()
				.contentType("text/plain")
				.build());
		
		assertThat(displayName.isPresent()).isFalse();
	}
	
	@Test
	public void testDisplayNameWhenName() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePartImpl.builder()
				.contentType("text/plain")
				.bodyParams(BodyParams.builder().add(
						new BodyParam("name", "hello"))
						.build())
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello");
	}
	
	@Test
	public void testDisplayNameWhenContentId() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePartImpl.builder()
				.contentType("text/plain")
				.contentId("hello")
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("ATT00000");
	}
	
	@Test
	public void testDisplayNameGetNameWhenBoth() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePartImpl.builder()
				.contentType("text/plain")
				.contentId("hello contentId")
				.bodyParams(BodyParams.builder().add(
						new BodyParam("name", "hello Name"))
						.build())
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello Name");
	}
	
	@Test
	public void testFetchInvitationMailWithoutInvitation() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = false;
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMockFetchingInvitation());
		
		control.replay();
		ICalendar calendar = emailViewPartsFetcherImpl.fetchInvitation(messageFixture.uid);
		control.verify();

		assertThat(calendar).isNull();
	}
	
	@Test
	public void testFetchInvitationMail() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMockFetchingInvitation());
		
		control.replay();
		ICalendar calendar = emailViewPartsFetcherImpl.fetchInvitation(messageFixture.uid);
		control.verify();

		assertThat(calendar).isNotNull();
	}

	@Test
	public void testFetchInvitationMailInBASE64() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		messageFixture.encoding = "BASE64";
		messageFixture.attachment = Resources.getResource("ics/base64.ics").openStream();
		messageFixture.attachmentDecoded = new Base64InputStream(Resources.getResource("ics/base64.ics").openStream());

		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMockFetchingInvitation());
		
		control.replay();
		ICalendar calendar = emailViewPartsFetcherImpl.fetchInvitation(messageFixture.uid);
		control.verify();

		assertThat(calendar).isNotNull();
	}
	
	private MailboxService messageFixtureToMailboxServiceMockFetchingInvitation() throws Exception {
		FlagsList fetchingFlagsFromFixture = buildFetchingFlagsFromFixture();
		Envelope fetchingEnvelopeFromFixture = buildFetchingEnvelopeFromFixture();
		MimePart mimePart = control.createMock(MimePart.class);
		expect(mimePart.isAttachment()).andReturn(messageFixture.isAttachment).anyTimes();
		expect(mimePart.getAddress()).andReturn(mimeAddress).anyTimes();
		expect(mimePart.isInvitation()).andReturn(messageFixture.isInvitation);
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.attachmentDecoded).anyTimes();

		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.findRootMimePartInTree()).andReturn(mimeMessage);
		expect(mimeMessage.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of(mimePart));

		MailboxService mailboxService = control.createMock(MailboxService.class);
		expect(mailboxService.fetchEmailMetadata(udr, messageCollectionName, messageFixture.uid))
			.andReturn(EmailMetadata.builder()
					.uid(messageFixture.uid)
					.size(messageFixture.estimatedDataSize)
					.flags(fetchingFlagsFromFixture)
					.envelope(fetchingEnvelopeFromFixture)
					.mimeMessage(mimeMessage)
					.build());
		
		expect(mailboxService.findAttachment(udr, messageCollectionName, messageFixture.uid, mimeAddress))
			.andReturn(messageFixture.attachment).anyTimes();
		return mailboxService;
	}
	
	@Test
	public void testDisplayNameWhenExtensionMapped() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePartImpl.builder()
				.contentType("image/jpeg")
				.contentId("hello contentId")
				.bodyParams(BodyParams.builder().add(
						new BodyParam("name", "hello Name"))
						.build())
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello Name");
	}

	private Optional<String> getDisplayNameOfMimePart(MimePart attachment) {
		TransformersFactory transformer = control.createMock(TransformersFactory.class);
		MailboxService mailboxService = control.createMock(MailboxService.class);
		List<BodyPreference> preferences = control.createMock(List.class);
		
		
		control.replay();
		Optional<String> displayName = new EmailViewPartsFetcherImpl(transformer, mailboxService, preferences, udr, "name", 15)
			.selectDisplayName(attachment, 0);
	
		control.verify();
		return displayName;
	}
	
	private MailboxService messageFixtureToMailboxServiceMock(MimeMessage fetchingMimeMessageFromFixture) throws Exception {
		FlagsList fetchingFlagsFromFixture = buildFetchingFlagsFromFixture();
		Envelope fetchingEnvelopeFromFixture = buildFetchingEnvelopeFromFixture();

		MailboxService mailboxService = control.createMock(MailboxService.class);
		expect(mailboxService.fetchEmailMetadata(udr, messageCollectionName, messageFixture.uid))
			.andReturn(EmailMetadata.builder()
					.uid(messageFixture.uid)
					.size(messageFixture.estimatedDataSize)
					.flags(fetchingFlagsFromFixture)
					.envelope(fetchingEnvelopeFromFixture)
					.mimeMessage(fetchingMimeMessageFromFixture)
					.build());
		
		mockMailboxServiceFetchingData(mailboxService);
		return mailboxService;
	}

	private FlagsList buildFetchingFlagsFromFixture() throws MailException {
		ImmutableSet.Builder<Flag> flagsListBuilder = ImmutableSet.builder();
		if (messageFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (messageFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		if (messageFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		return new FlagsList(flagsListBuilder.build());
	}

	private Envelope buildFetchingEnvelopeFromFixture() throws MailException {
		return Envelope.builder()
			.from(messageFixture.from)
			.to(messageFixture.to)
			.cc(messageFixture.cc)
			.subject(messageFixture.subject)
			.date(messageFixture.date)
			.build();
	}
	
	private void mockMailboxServiceFetchingData(MailboxService mailboxService) throws MailException {
		if (messageFixture.estimatedDataSize != 0) {
			expect(mailboxService.fetchPartialMimePartStream(
					anyObject(UserDataRequest.class),
					anyObject(String.class),
					anyLong(),
					anyObject(MimeAddress.class),
					anyInt()))
				.andReturn(messageFixture.bodyData).anyTimes();
		} else {
			expect(mailboxService.fetchMimePartStream(
					anyObject(UserDataRequest.class),
					anyObject(String.class),
					anyLong(),
					anyObject(MimeAddress.class)))
				.andReturn(messageFixture.bodyData).once();
		}
		
		expect(mailboxService.findAttachment(udr, messageCollectionName, messageFixture.uid, mimeAddress))
			.andReturn(messageFixture.attachment).anyTimes();
	}

	private MimeMessage buildFetchingMimeMessageFromFixture() {
		
		expect(mimePart.getCharset()).andReturn(messageFixture.bodyCharset).anyTimes();
		expect(mimePart.getPrimaryType()).andReturn(messageFixture.bodyPrimaryType).anyTimes();
		expect(mimePart.getSubtype()).andReturn(messageFixture.bodySubType).anyTimes();
		expect(mimePart.findRootMimePartInTree()).andReturn(mimePart).anyTimes();
		expect(mimePart.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of(mimePart)).anyTimes();
		expect(mimePart.isAttachment()).andReturn(messageFixture.isAttachment).anyTimes();
		expect(mimePart.getAttachmentExtension()).andReturn("ATT00001").anyTimes();
		expect(mimePart.getName()).andReturn(messageFixture.subject).anyTimes();
		expect(mimePart.getAddress()).andReturn(mimeAddress).anyTimes();
		expect(mimePart.getFullMimeType()).andReturn(messageFixture.fullMimeType).anyTimes();
		expect(mimePart.getContentType()).andReturn(ContentType.builder().contentType(messageFixture.fullMimeType).build()).anyTimes();
		expect(mimePart.getContentTransfertEncoding()).andReturn(messageFixture.encoding).anyTimes();
		expect(mimePart.getSize()).andReturn(messageFixture.estimatedDataSize).anyTimes();
		expect(mimePart.isInvitation()).andReturn(messageFixture.isInvitation).anyTimes();
		expect(mimePart.isCancelInvitation()).andReturn(false).anyTimes();
		expect(mimePart.isReplyInvitation()).andReturn(false).anyTimes();
		expect(mimePart.containsCalendarMethod()).andReturn(messageFixture.isInvitation).anyTimes();
		expect(mimePart.getContentId()).andReturn(messageFixture.contentId).anyTimes();
		expect(mimePart.getContentLocation()).andReturn(null).anyTimes();
		expect(mimePart.isInline()).andReturn(false).anyTimes();
		expect(mimePart.isICSAttachment()).andReturn(messageFixture.isICSAttachment).anyTimes();
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.bodyDataDecoded).anyTimes();

		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(anyObject(ContentType.class))).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findRootMimePartInTree()).andReturn(mimeMessage).anyTimes();
		expect(mimeMessage.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of(mimePart)).anyTimes();

		return mimeMessage;
	}

	private MimeMessage buildFetchingMimeMessageFromFixtureMultipleDecodeMimeStream() {
		
		expect(mimePart.getCharset()).andReturn(messageFixture.bodyCharset).anyTimes();
		expect(mimePart.getPrimaryType()).andReturn(messageFixture.bodyPrimaryType).anyTimes();
		expect(mimePart.getSubtype()).andReturn(messageFixture.bodySubType).anyTimes();
		expect(mimePart.findRootMimePartInTree()).andReturn(mimePart).anyTimes();
		expect(mimePart.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of(mimePart)).anyTimes();
		expect(mimePart.isAttachment()).andReturn(messageFixture.isAttachment).anyTimes();
		expect(mimePart.getAttachmentExtension()).andReturn("ATT00001").anyTimes();
		expect(mimePart.getName()).andReturn(messageFixture.subject).anyTimes();
		expect(mimePart.getAddress()).andReturn(mimeAddress).anyTimes();
		expect(mimePart.getFullMimeType()).andReturn(messageFixture.fullMimeType).anyTimes();
		expect(mimePart.getContentType()).andReturn(ContentType.builder().contentType(messageFixture.fullMimeType).build()).anyTimes();
		expect(mimePart.getContentTransfertEncoding()).andReturn(messageFixture.encoding).anyTimes();
		expect(mimePart.getSize()).andReturn(messageFixture.estimatedDataSize).anyTimes();
		expect(mimePart.isInvitation()).andReturn(messageFixture.isInvitation).anyTimes();
		expect(mimePart.isCancelInvitation()).andReturn(false).anyTimes();
		expect(mimePart.isReplyInvitation()).andReturn(false).anyTimes();
		expect(mimePart.containsCalendarMethod()).andReturn(messageFixture.isInvitation).anyTimes();
		expect(mimePart.getContentId()).andReturn(messageFixture.contentId).anyTimes();
		expect(mimePart.getContentLocation()).andReturn(null).anyTimes();
		expect(mimePart.isInline()).andReturn(false).anyTimes();
		expect(mimePart.isICSAttachment()).andReturn(messageFixture.isICSAttachment).anyTimes();
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.bodyDataDecoded);
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.attachmentDecoded);

		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(anyObject(ContentType.class))).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findRootMimePartInTree()).andReturn(mimeMessage).anyTimes();
		expect(mimeMessage.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of(mimePart)).anyTimes();

		return mimeMessage;
	}

	private EmailViewPartsFetcherImpl newFetcherFromExpectedFixture(MailboxService mailboxServiceMock) {
		return new EmailViewPartsFetcherImpl(
				identityMailTransformerFactory(),
				mailboxServiceMock, bodyPreferences(),
				udr, messageCollectionName, messageCollectionId);
	}

	private TransformersFactory identityMailTransformerFactory() {
		TransformersFactory transformersFactory = control.createMock(TransformersFactory.class);
		expect(transformersFactory.create(anyObject(FetchInstruction.class))).andDelegateTo(new TestIdentityTransformerFactory()).anyTimes();
		return transformersFactory;
	}

	public Address newEmptyAddress() {
		return new Address("");
	}

	private List<BodyPreference> bodyPreferences() {
		BodyPreference.Builder builder = BodyPreference.builder()
			.bodyType(messageFixture.bodyType);
		if (messageFixture.estimatedDataSize != 0) {
			builder.truncationSize(messageFixture.estimatedDataSize);
		}
		return Lists.newArrayList(builder.build());
	}

	@Test(expected=NullPointerException.class)
	public void testOrganizerFallbackWithNullEmailMetadata() {
		EmailMetadata emailMetadata = null;
		noDependencyTestee().organizerFallback(emailMetadata);
	}

	@Test
	public void testOrganizerFallbackWithEmptyFromList() {
		Envelope envelope = Envelope.builder().build();
		EmailMetadata emailMetadata = control.createMock(EmailMetadata.class);
		expect(emailMetadata.getEnvelope()).andReturn(envelope);
		
		control.replay();
		Organizer organizerFallback = noDependencyTestee().organizerFallback(emailMetadata);
		control.verify();
		
		assertThat(organizerFallback).isNull();
	}

	@Test
	public void testOrganizerFallbackWithNotEmptyFromListButNoEmail() {
		Envelope envelope = Envelope.builder().from(ImmutableList.of(new Address(null))).build();
		EmailMetadata emailMetadata = control.createMock(EmailMetadata.class);
		expect(emailMetadata.getEnvelope()).andReturn(envelope);
		
		control.replay();
		Organizer organizerFallback = noDependencyTestee().organizerFallback(emailMetadata);
		control.verify();
		
		assertThat(organizerFallback).isNull();
	}

	@Test
	public void testOrganizerFallbackWithNotEmptyFromList() throws URISyntaxException {
		Envelope envelope = Envelope.builder().from(ImmutableList.of(new Address("login@domain"))).build();
		EmailMetadata emailMetadata = control.createMock(EmailMetadata.class);
		expect(emailMetadata.getEnvelope()).andReturn(envelope);
		
		control.replay();
		Organizer organizerFallback = noDependencyTestee().organizerFallback(emailMetadata);
		control.verify();
		
		assertThat(organizerFallback).isEqualTo(new Organizer("MAILTO:login@domain"));
	}

	@Test(expected=NullPointerException.class)
	public void testAddressToOrganizerWithNullFrom() {
		Address from = null;
		noDependencyTestee().addressToOrganizer(from);
	}
	
	@Test
	public void testAddressToOrganizerWithNullEmail() {
		Address from = new Address(null);
		
		Organizer organizer = noDependencyTestee().addressToOrganizer(from);
		
		assertThat(organizer).isNull();
	}
	
	@Test
	public void testAddressToOrganizerWithFrom() throws URISyntaxException {
		Address from = new Address("login@domain");
		Organizer organizer = noDependencyTestee().addressToOrganizer(from);
		
		assertThat(organizer).isEqualTo(new Organizer("MAILTO:login@domain"));
	}
	
	@Test
	public void testAddressToOrganizerWithBadFromSyntax() throws URISyntaxException {
		Address from = new Address("login@@domain");
		Organizer organizer = noDependencyTestee().addressToOrganizer(from);
		
		assertThat(organizer).isEqualTo(new Organizer("MAILTO:login@@domain"));
	}

	private EmailViewPartsFetcherImpl noDependencyTestee() {
		TransformersFactory transformer = null;
		MailboxService mailboxService = null;
		List<BodyPreference> preferences = null;
		return new EmailViewPartsFetcherImpl(transformer, mailboxService, preferences, udr, "collectionPath", 15);
	}
	
	@Test
	public void testFetchStrictBodyPreferencePlainText() throws Exception {
		messageFixture.bodyType = MSEmailBodyType.PlainText;
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(1, new StrictMatchBodyPreferencePolicy());
	
		control.verify();
		assertThat(emailView).isNotNull();
		assertThat(emailView.getBodyType()).isEqualTo(MSEmailBodyType.PlainText);
	}
	
	@Test
	public void testFetchStrictBodyPreferenceHTML() throws Exception {
		messageFixture.bodyType = MSEmailBodyType.HTML;
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(1, new StrictMatchBodyPreferencePolicy());
	
		control.verify();
		assertThat(emailView).isNotNull();
		assertThat(emailView.getBodyType()).isEqualTo(MSEmailBodyType.HTML);
	}
	
	@Test
	public void testFetchStrictBodyPreferenceRTF() throws Exception {
		messageFixture.bodyType = MSEmailBodyType.RTF;
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildFetchingMimeMessageFromFixture()));
		
		control.replay();
		EmailView emailView = emailViewPartsFetcherImpl.fetch(1, new StrictMatchBodyPreferencePolicy());
	
		control.verify();
		assertThat(emailView).isNotNull();
		assertThat(emailView.getBodyType()).isEqualTo(MSEmailBodyType.RTF);
	}
	
	@Test(expected=EmailViewBuildException.class)
	public void testFetch() throws Exception {
		messageFixture.bodyType = MSEmailBodyType.PlainText;
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = newFetcherFromExpectedFixture(messageFixtureToMailboxServiceMock(buildEmptyMimeMessageFromFixture()));
		
		control.replay();
		try {
			emailViewPartsFetcherImpl.fetch(1, new StrictMatchBodyPreferencePolicy());
		} finally {
			control.verify();
		}
	}

	private MimeMessage buildEmptyMimeMessageFromFixture() {
		
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(anyObject(ContentType.class))).andReturn(null).anyTimes();
		expect(mimeMessage.findRootMimePartInTree()).andReturn(mimeMessage).anyTimes();
		expect(mimeMessage.listLeaves(true, true)).andReturn(ImmutableList.<MimePart> of()).anyTimes();

		return mimeMessage;
	}
}
