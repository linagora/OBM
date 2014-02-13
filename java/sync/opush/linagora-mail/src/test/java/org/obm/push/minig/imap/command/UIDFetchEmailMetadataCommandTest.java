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
package org.obm.push.minig.imap.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.exception.MailException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.BodyParams;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.mail.mime.MimePartImpl;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;


public class UIDFetchEmailMetadataCommandTest {

	private BodyStructureParser bodyStructureParser;

	@Before
	public void setUp() {
		bodyStructureParser = new BodyStructureParser();
	}
	
	@Test
	public void getImapCommand() {
		String imapCommand = new UIDFetchEmailMetadataCommand(bodyStructureParser, 12l).getImapCommand();
		
		assertThat(imapCommand).isEqualTo(" (UID FLAGS RFC822.SIZE BODYSTRUCTURE ENVELOPE)");
	}
	
	@Test
	public void buildCommand() {
		CommandArgument command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 12l).buildCommand();
		
		assertThat(command.hasLiteralData()).isFalse();
		assertThat(command.getLiteralData()).isNull();
		assertThat(command.getCommandString()).isEqualTo("UID FETCH 12 (UID FLAGS RFC822.SIZE BODYSTRUCTURE ENVELOPE)");
	}
	
	@Test
	public void buildCommandMaxLong() {
		CommandArgument command = new UIDFetchEmailMetadataCommand(bodyStructureParser, Long.MAX_VALUE).buildCommand();

		assertThat(command.hasLiteralData()).isFalse();
		assertThat(command.getLiteralData()).isNull();
		assertThat(command.getCommandString()).isEqualTo("UID FETCH 9223372036854775807 (UID FLAGS RFC822.SIZE BODYSTRUCTURE ENVELOPE)");
	}
	
	@Test
	public void matchResponseWhenSizeIsMissing() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchResponseWhenUIDIsMissing() {
		String response = "* 31 FETCH (FLAGS (\\Seen) RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchResponseWhenFlagsAreMissing() {
		String response = "* 31 FETCH (UID 41 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchResponseWhenBodyStructureIsMissing() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\"))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchResponseWhenEnvelopeIsMissing() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 RFC822.SIZE 5179"+
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchRightResponseButWrongUID() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 51 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isFalse();
	}
	
	@Test
	public void matchRightResponse() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"=?UTF-8?Q?Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"QUOTED-PRINTABLE\" 465 14 NIL NIL NIL NIL)" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\"" +
				" 864 25 NIL NIL NIL NIL)(\"TEXT\" \"CALENDAR\"" +
				" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REPLY\") NIL NIL \"7BIT\" 716 26 NIL NIL NIL NIL)" +
				" \"ALTERNATIVE\" (\"BOUNDARY\" \"----=_Part_4_10352157.1341253062472\") NIL NIL NIL)" +
				"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 980 NIL" +
				" (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL)" +
				" \"MIXED\" (\"BOUNDARY\" \"----=_Part_3_4302978.1341253062472\") NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		
		assertThat(command.isMatching(new IMAPResponse("OK", response))).isTrue();
	}
	
	@Test(expected=NumberFormatException.class)
	public void handleResponseWithoutSize() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\")" +
				" NIL NIL \"7BIT\" 226 7 NIL NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		command.setDataInitialValue();
		command.handleResponse(new IMAPResponse("OK", response));
	}
	
	@Test(expected=MailException.class)
	public void handleRightResponseWithWrongUid() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 51 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\")" +
				" NIL NIL \"7BIT\" 226 7 NIL NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		command.setDataInitialValue();
		command.handleResponse(new IMAPResponse("OK", response));
	}
	
	@Test
	public void handleRightResponse() {
		String response = "* 31 FETCH (FLAGS (\\Seen) UID 41 RFC822.SIZE 5179 ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\")" +
				" NIL NIL \"7BIT\" 226 7 NIL NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		command.setDataInitialValue();
		command.handleResponse(new IMAPResponse("OK", response));
		
		assertThat(command.getReceivedData()).isEqualTo(EmailMetadata.builder()
				.uid(41l)
				.size(5179l)
				.flags(new FlagsList(ImmutableList.of(Flag.SEEN)))
				.envelope(Envelope.builder()
					.subject("Subject")
					.date(date("2012-07-02T20:17:42+02"))
					.from(addresses("user@thilaire.lng.org", "user user"))
					.replyTo(addresses("user@thilaire.lng.org", "user user"))
					.to(addresses("zadmin@thilaire.lng.org", "zadmin zadmin"))
					.messageID("<615111.5.1341253062499.JavaMail.root@thilaireOBM01>")
					.build())
				.mimeMessage(MimeMessageImpl.builder()
					.uid(41l)
					.size(5179)
					.from(MimePartImpl.builder()
						.bodyParams(BodyParams.builder()
							.add(new BodyParam("charset", "ISO-8859-1"))
							.add(new BodyParam("format", "flowed"))
							.build())
						.contentType("text/plain")
						.encoding("7BIT")
						.size(226)
						.build())
					.build())
				.build());
	}
	
	@Test
	public void handleRightResponseInDifferentOrder() {
		String response = "* 31 FETCH (ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:17:42 +0200 (CEST)\" \"Subject\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<615111.5.1341253062499.JavaMail.root@thilaireOBM01>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\")" +
				" NIL NIL \"7BIT\" 226 7 NIL NIL NIL NIL) FLAGS (\\Seen) UID 41 RFC822.SIZE 5179 )";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 41l);
		command.setDataInitialValue();
		command.handleResponse(new IMAPResponse("OK", response));

		assertThat(command.getReceivedData()).isEqualTo(EmailMetadata.builder()
				.uid(41l)
				.size(5179l)
				.flags(new FlagsList(ImmutableList.of(Flag.SEEN)))
				.envelope(Envelope.builder()
					.subject("Subject")
					.date(date("2012-07-02T20:17:42+02"))
					.from(addresses("user@thilaire.lng.org", "user user"))
					.replyTo(addresses("user@thilaire.lng.org", "user user"))
					.to(addresses("zadmin@thilaire.lng.org", "zadmin zadmin"))
					.messageID("<615111.5.1341253062499.JavaMail.root@thilaireOBM01>")
					.build())
				.mimeMessage(MimeMessageImpl.builder()
					.uid(41l)
					.size(5179)
					.from(MimePartImpl.builder()
						.bodyParams(BodyParams.builder()
							.add(new BodyParam("charset", "ISO-8859-1"))
							.add(new BodyParam("format", "flowed"))
							.build())
						.contentType("text/plain")
						.encoding("7BIT")
						.size(226)
						.build())
					.build())
				.build());
	}

	@Test
	public void handleAnotherRightResponse() {
		String response = "* 41 FETCH (UID 4052013 FLAGS (\\Seen \\Deleted) ENVELOPE" +
				" (\"Mon,  2 Jul 2012 20:18:42 +0200 (CEST)\" \"Cool\"" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"user user\" NIL \"user\" \"thilaire.lng.org\"))" +
				" ((\"zadmin zadmin\" NIL \"zadmin\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<messageId@thilaireOBM01>\")" +
				" RFC822.SIZE 1337" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"FORMAT\" \"flowed\")" +
				" NIL NIL \"7BIT\" 1337 7 NIL NIL NIL NIL))";
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 4052013l);
		command.setDataInitialValue();
		command.handleResponse(new IMAPResponse("OK", response));

		assertThat(command.getReceivedData()).isEqualTo(EmailMetadata.builder()
				.uid(4052013l)
				.size(1337l)
				.flags(new FlagsList(ImmutableList.of(Flag.SEEN, Flag.DELETED)))
				.envelope(Envelope.builder()
					.subject("Cool")
					.date(date("2012-07-02T20:18:42+02"))
					.from(addresses("user@thilaire.lng.org", "user user"))
					.replyTo(addresses("user@thilaire.lng.org", "user user"))
					.to(addresses("zadmin@thilaire.lng.org", "zadmin zadmin"))
					.messageID("<messageId@thilaireOBM01>")
					.build())
				.mimeMessage(MimeMessageImpl.builder()
					.uid(4052013l)
					.size(1337)
					.from(MimePartImpl.builder()
						.bodyParams(BodyParams.builder()
							.add(new BodyParam("format", "flowed"))
							.build())
						.contentType("text/plain")
						.encoding("7BIT")
						.size(1337)
						.build())
					.build())
				.build());
	}

	@Test
	public void handleRightResponseWithMoreData() {
		String response = "* 17926 FETCH (FLAGS (\\Seen NonJunk)" +
				" UID 741144 RFC822.SIZE 5516" +
				" ENVELOPE (\"Tue, 15 Jan 2013 20:42:42 +0100\"" +
				" \"Re: Subject\" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User Two\" NIL \"usertwo\" \"linagora.com\")" +
				"(\"User Three\" NIL \"userthree\" \"linagora.com\"))" +
				" ((\"User Four\" NIL \"userfour\" \"domain.org\")" +
				"(NIL NIL \"userfive\" \"thilaire.lng.org\")) NIL {47}";
		
		String moreData = "<CD1B3CB8.260000000000E09%usertwo@linagora.com>" +
				" \"<50F5B132.5000203@domain.org>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"8BIT\" 2807 108 NIL NIL NIL))";
		
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		imapResponse.setStreamData(new ByteArrayInputStream(moreData.getBytes(Charsets.UTF_8)));
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 741144l);
		command.setDataInitialValue();
		command.handleResponse(imapResponse);

		EmailMetadata data = command.getReceivedData();
		assertThat(data.getUid()).isEqualTo(741144l);
		assertThat(data.getSize()).isEqualTo(5516l);
		assertThat(data.getFlags()).isEqualTo(new FlagsList(ImmutableList.of(Flag.SEEN)));
		assertThat(data.getEnvelope()).isEqualTo(Envelope.builder()
					.subject("Re: Subject")
					.date(date("2013-01-15T20:42:42+01"))
					.from(addresses("userone@domain.org", "User One"))
					.replyTo(addresses("userone@domain.org", "User One"))
					.to(ImmutableList.of(
							new Address("User Two", "usertwo@linagora.com"),
							new Address("User Three", "userthree@linagora.com")))
					.cc(ImmutableList.of(
							new Address("User Four", "userfour@domain.org"),
							new Address(null, "userfive@thilaire.lng.org")))
					.messageID("<50F5B132.5000203@domain.org>")
					.build());
		assertThat(data.getMimeMessage()).isEqualTo(MimeMessageImpl.builder()
					.uid(741144l)
					.size(5516)
					.from(MimePartImpl.builder()
						.bodyParams(BodyParams.builder()
							.add(new BodyParam("charset", "UTF-8"))
							.build())
						.contentType("text/plain")
						.encoding("8BIT")
						.size(2807)
						.build())
					.build());
	}
	
	@Test
	public void matchRightResponseWithMoreData() {
		String response = "* 17926 FETCH (FLAGS (\\Seen NonJunk)" +
				" UID 741144 RFC822.SIZE 5516" +
				" ENVELOPE (\"Tue, 15 Jan 2013 20:42:42 +0100\"" +
				" \"Re: Subject\" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User One\" NIL \"userone\" \"domain.org\"))" +
				" ((\"User Two\" NIL \"usertwo\" \"linagora.com\")" +
				"(\"User Three\" NIL \"userthree\" \"linagora.com\"))" +
				" ((\"User Four\" NIL \"userfour\" \"domain.org\")" +
				"(NIL NIL \"userfive\" \"thilaire.lng.org\")) NIL {47}";
		
		String moreData = "<CD1B3CB8.260000000000E09%usertwo@linagora.com>" +
				" \"<50F5B132.5000203@domain.org>\")" +
				" BODYSTRUCTURE (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\")" +
				" NIL NIL \"8BIT\" 2807 108 NIL NIL NIL))";
		
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		imapResponse.setStreamData(new ByteArrayInputStream(moreData.getBytes(Charsets.UTF_8)));
		
		UIDFetchEmailMetadataCommand command = new UIDFetchEmailMetadataCommand(bodyStructureParser, 741144l);
		assertThat(command.isMatching(imapResponse)).isTrue();
	}

	private List<Address> addresses(String address, String displayName) {
		return ImmutableList.of(
				new Address(displayName, address));
	}
}
