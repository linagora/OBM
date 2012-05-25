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
package org.obm.push.mail;

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.Address;
import org.minig.imap.EmailView;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.ContentType;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.MimePart;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.imap.ImapMailboxService;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

@RunWith(SlowFilterRunner.class)
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

		Integer bodyTruncation = 1000;
		MSEmailBodyType bodyType = MSEmailBodyType.PlainText;
		String bodyPrimaryType = "text";
		String bodySubType = "plain";
		String bodyCharset = Charsets.UTF_8.displayName();
		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
	}
	
	private MessageFixture messageFixture;
	private String messageCollectionName;
	private String mailbox;
	private String password;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		mailbox = "to@localhost.com";
		password = "password";
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		
		messageFixture = new MessageFixture();
		messageCollectionName = IMAP_INBOX_NAME;
	}
	
	@Test
	public void testFlagAnsweredTrue() throws Exception {
		messageFixture.answered = true;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getFlags()).contains(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagAnsweredFalse() throws Exception {
		messageFixture.answered = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagReadTrue() throws Exception {
		messageFixture.read = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).contains(Flag.SEEN);
	}
	
	@Test
	public void testFlagReadFalse() throws Exception {
		messageFixture.read = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.SEEN);
	}
	
	@Test
	public void testFlagStarredTrue() throws Exception {
		messageFixture.starred = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).contains(Flag.FLAGGED);
	}
	
	@Test
	public void testFlagStarredFalse() throws Exception {
		messageFixture.starred = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.FLAGGED);
	}
	
	@Test
	public void testHeaderFromNull() throws Exception {
		messageFixture.from = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getFrom()).isEmpty();
	}

	@Test
	public void testHeaderFromEmpty() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getFrom()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderFrom() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(new Address("from@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getFrom()).containsOnly(new Address("from@domain.test"));
	}
	
	@Test
	public void testHeaderToNull() throws Exception {
		messageFixture.to = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getTo()).isEmpty();
	}

	@Test
	public void testHeaderToEmpty() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getTo()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderToSingle() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(new Address("to@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getTo()).containsOnly(new Address("to@domain.test"));
	}
	
	@Test
	public void testHeaderToMultiple() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(
				new Address("to@domain.test"), new Address("to2@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getTo()).containsOnly(
				new Address("to@domain.test"), new Address("to2@domain.test"));
	}
	
	@Test
	public void testHeaderCcNull() throws Exception {
		messageFixture.cc = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getCc()).isEmpty();
	}

	@Test
	public void testHeaderCcEmpty() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getCc()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderCcSingle() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(new Address("cc@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getCc()).containsOnly(new Address("cc@domain.test"));
	}
	
	@Test
	public void testHeaderCcMultiple() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(
				new Address("cc@domain.test"), new Address("cc2@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getCc()).containsOnly(
				new Address("cc@domain.test"), new Address("cc2@domain.test"));
	}
	
	@Test
	public void testHeaderSubjectNull() throws Exception {
		messageFixture.subject = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderSubjectEmpty() throws Exception {
		messageFixture.subject = "";

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getSubject()).isEmpty();
	}
	
	@Test
	public void testHeaderSubject() throws Exception {
		messageFixture.subject = "a subject";

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getSubject()).isEqualTo("a subject");
	}
	
	@Test
	public void testHeaderDateNull() throws Exception {
		messageFixture.date = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getDate()).isNull();
	}
	
	@Test
	public void testHeaderDate() throws Exception {
		messageFixture.date = DateUtils.date("2004-12-14T22:00:00");

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getDate()).isEqualTo(DateUtils.date("2004-12-14T22:00:00"));
	}
	
	@Test
	public void testUid() throws Exception {
		messageFixture.uid = 165l; 

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getUid()).isEqualTo(165l);
	}
	
	@Test
	public void testBodyTruncationNull() throws Exception {
		messageFixture.bodyTruncation = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyTruncation()).isNull();
	}
	
	@Test
	public void testBodyTruncation() throws Exception {
		messageFixture.bodyTruncation = 1505;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyTruncation()).isEqualTo(1505);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBodyMimePartDataNull() throws Exception {
		messageFixture.bodyData = null;

		newFetcherFromExpectedFixture().fetch();
	}
	
	@Test
	public void testBodyMimePartData() throws Exception {
		messageFixture.bodyData = StreamMailTestsUtils.newInputStreamFromString("email data");

		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyMimePartData())
			.hasContentEqualTo(StreamMailTestsUtils.newInputStreamFromString("email data"));
	}
	
	@Test
	public void testBodyMimePartCharsetNull() throws Exception {
		messageFixture.bodyCharset = null;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyMimePart().getCharset()).isNull();
	}
	
	@Test
	public void testBodyMimePartCharsetEmtpy() throws Exception {
		messageFixture.bodyCharset = "";
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyMimePart().getCharset()).isEmpty();
	}

	@Test
	public void testBodyMimePartCharsetASCII() throws Exception {
		String asciiCharsetName = Charsets.US_ASCII.displayName();
		messageFixture.bodyCharset = asciiCharsetName;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyMimePart().getCharset()).isEqualTo(asciiCharsetName);
	}

	@Test
	public void testBodyMimePartCharsetUTF8() throws Exception {
		String utf8CharsetName = Charsets.UTF_8.displayName();
		messageFixture.bodyCharset = utf8CharsetName;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getBodyMimePart().getCharset()).isEqualTo(utf8CharsetName);
	}

	@Test
	public void testBodyMimePartContentTypeNull() throws Exception {
		messageFixture.bodyPrimaryType = null;
		messageFixture.bodySubType = null;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getBodyMimePart().getPrimaryType()).isNull();
		assertThat(emailView.getBodyMimePart().getSubtype()).isNull();
	}

	@Test
	public void testBodyMimePartContentTypeEmpty() throws Exception {
		messageFixture.bodyPrimaryType = "";
		messageFixture.bodySubType = "";
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getBodyMimePart().getPrimaryType()).isEmpty();
		assertThat(emailView.getBodyMimePart().getSubtype()).isEmpty();
	}

	@Test
	public void testBodyMimePartContentTypePlainText() throws Exception {
		messageFixture.bodyPrimaryType = "plain";
		messageFixture.bodySubType = "text";
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getBodyMimePart().getPrimaryType()).isEqualTo("plain");
		assertThat(emailView.getBodyMimePart().getSubtype()).isEqualTo("text");
	}

	@Test
	public void testBodyMimePartContentTypeCalendar() throws Exception {
		messageFixture.bodyPrimaryType = "plain";
		messageFixture.bodySubType = "calendar";
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getBodyMimePart().getPrimaryType()).isEqualTo("plain");
		assertThat(emailView.getBodyMimePart().getSubtype()).isEqualTo("calendar");
	}

	@Test
	public void testBodyMimePartContentTypeAttachment() throws Exception {
		messageFixture.bodyPrimaryType = "application";
		messageFixture.bodySubType = "octet-stream";
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getBodyMimePart().getPrimaryType()).isEqualTo("application");
		assertThat(emailView.getBodyMimePart().getSubtype()).isEqualTo("octet-stream");
	}
	
	private ImapMailboxService messageFixtureToMailboxServiceMock() throws Exception {
		ImapMailboxService mailboxService = createStrictMock(ImapMailboxService.class);
		mockMailboxServiceFlags(mailboxService);
		mockMailboxServiceEnvelope(mailboxService);
		mockMailboxServiceBody(mailboxService);
		replay(mailboxService);
		return mailboxService;
	}

	private void mockMailboxServiceFlags(ImapMailboxService mailboxService) throws MailException {
		Builder<Flag> flagsListBuilder = ImmutableList.builder();
		if (messageFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (messageFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		if (messageFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		expect(mailboxService.fetchFlags(udr, messageCollectionName, messageFixture.uid))
				.andReturn(flagsListBuilder.build()).once();
	}

	private void mockMailboxServiceEnvelope(ImapMailboxService mailboxService) throws MailException {
		Envelope envelope = new Envelope.Builder()
			.from(messageFixture.from)
			.to(messageFixture.to)
			.cc(messageFixture.cc)
			.subject(messageFixture.subject)
			.date(messageFixture.date)
			.build();
		
		expect(mailboxService.fetchEnvelope(udr, messageCollectionName, messageFixture.uid))
			.andReturn(new UIDEnvelope(messageFixture.uid, envelope)).once();
	}
	
	private void mockMailboxServiceBody(ImapMailboxService mailboxService) throws MailException {
		expect(mailboxService.fetchBodyStructure(udr, messageCollectionName, messageFixture.uid))
			.andReturn(mockAggregateMimeMessage()).once();

		expect(mailboxService.fetchMimePartData(
				anyObject(UserDataRequest.class),
				anyObject(String.class),
				anyLong(),
				anyObject(FetchInstructions.class)))
			.andReturn(messageFixture.bodyData).once();
	}

	private MimeMessage mockAggregateMimeMessage() {
		MimePart mimePart = createMock(MimePart.class);
		expect(mimePart.getCharset()).andReturn(messageFixture.bodyCharset);
		expect(mimePart.getPrimaryType()).andReturn(messageFixture.bodyPrimaryType);
		expect(mimePart.getSubtype()).andReturn(messageFixture.bodySubType);

		MimeMessage mimeMessage = createMock(MimeMessage.class);
		expect(mimeMessage.findMainMessage(anyObject(ContentType.class))).andReturn(mimePart).anyTimes();

		replay(mimeMessage, mimePart);
		return mimeMessage;
	}
	
	private EmailViewPartsFetcherImpl newFetcherFromExpectedFixture() throws Exception {
		return new EmailViewPartsFetcherImpl(
				messageFixtureToMailboxServiceMock(), bodyPreferences(),
				udr, messageCollectionName, messageFixture.uid);
	}

	public Address newEmptyAddress() {
		return new Address("");
	}

	private ArrayList<BodyPreference> bodyPreferences() {
		BodyPreference.Builder builder = new BodyPreference.Builder()
			.bodyType(messageFixture.bodyType);
		if (messageFixture.bodyTruncation != null) {
			builder.truncationSize(messageFixture.bodyTruncation);
		}
		return Lists.newArrayList(builder.build());
	}

}
