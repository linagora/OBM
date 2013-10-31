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
package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.IMAPResponseParser;
import org.obm.push.minig.imap.impl.MinaIMAPMessage;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class UIDFetchEnvelopeCommandTest {

	@Test
	public void testMoreThanOneLine() {
		String firstLine = 
			"* 20 FETCH (UID 20 ENVELOPE (\"Tue, 28 Apr 2009 17:10:03 +0200\" {43}";
		String continuation =
			"schema pour formation, \"redirec after post\" " +
			"((\"Olivier Boyer\" NIL \"olivier.boyer\" \"linagora.com\")) " +
			"((\"Olivier Boyer\" NIL \"olivier.boyer\" \"linagora.com\")) " +
			"((\"Olivier Boyer\" NIL \"olivier.boyer\" \"linagora.com\")) " +
			"((\"Patrick PAYSANT\" NIL \"patrick.paysant\" \"aliacom.fr\")(\"=?UTF-8?B?UmFwaGHDq2wgUg==?= =?UTF-8?B?b3VnZXJvbg==?=\" NIL \"raphael.rougeron\" \"aliasource.fr\")) NIL NIL NIL \"<49F71C4B.3040205@linagora.com>\"))";
		MinaIMAPMessage minaMsg = new MinaIMAPMessage(firstLine);
		minaMsg.addLine(continuation.getBytes());
		IMAPResponseParser imapResponseParser = new IMAPResponseParser();
		IMAPResponse response = imapResponseParser.parse(minaMsg);
		UIDFetchEnvelopeCommand command = new UIDFetchEnvelopeCommand(MessageSet.singleton(20l));
		command.handleResponses(ImmutableList.of(response, new IMAPResponse("OK", "")));
		Assert.assertNotNull(command.getReceivedData());
	}
	
	@Test
	public void testParseEnvelopeSyntax() {
		String envelopeData = "ENVELOPE (\"Thu, 13 Sep 2012 14:16:45 +0200\"" +
				" \"subject\" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"toName toName\" NIL \"toName\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<5051CEAD.8020706@thilaire.lng.org>\")";

		Envelope envelope = UIDFetchEnvelopeCommand.parseEnvelope(envelopeData);
		
		assertThat(envelope.getFrom()).containsOnly(new Address("sender", "sender@thilaire.lng.org"));
		assertThat(envelope.getTo()).containsOnly(new Address("toName toName", "toName@thilaire.lng.org"));
		assertThat(envelope.getCc()).isEmpty();
		assertThat(envelope.getBcc()).isEmpty();
		assertThat(envelope.getDate()).isEqualTo(DateUtils.date("2012-09-13T14:16:45+02"));
		assertThat(envelope.getMessageId()).isEqualTo("<5051CEAD.8020706@thilaire.lng.org>");
		assertThat(envelope.getReplyTo()).containsOnly(new Address("sender", "sender@thilaire.lng.org"));
		assertThat(envelope.getSubject()).isEqualTo("subject");
	}
	
	@Test
	public void testParseEnvelopePayloadInIMAPResponse() {
		String expectedEnvelopePayload = 
				"(\"Thu, 13 Sep 2012 14:16:45 +0200\"" +
				" \"subject\" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"sender\" NIL \"sender\" \"thilaire.lng.org\"))" +
				" ((\"toName toName\" NIL \"toName\" \"thilaire.lng.org\"))" +
				" NIL NIL NIL \"<5051CEAD.8020706@thilaire.lng.org>\")";
		
		String fullPayload = "* 2 FETCH (UID 28 ENVELOPE " + expectedEnvelopePayload + ")";
		
		String parsedEnvelope = UIDFetchEnvelopeCommand.getEnvelopePayload(fullPayload);
		assertThat(parsedEnvelope).isEqualTo(expectedEnvelopePayload);
	}
	
	@Test
	public void testParseEnvelopeParenthesisInSubject() {
		String fullPayload = "* 1444 FETCH (UID 590923 ENVELOPE " +
				"(\"Fri, 11 Feb 2011 17:14:44 +0100\" \"Re: Tu ne m'oublies pas ;-)\" " +
				"((\"Robert Dupont\" NIL \"rdupont\" \"linagora.com\")) " +
				"((\"Robert Dupont\" NIL \"rdupont\" \"linagora.com\")) " +
				"((\"Robert Dupont\" NIL \"rdupont\" \"linagora.com\")) " +
				"((NIL NIL \"boss\" \"linagora.com\")) NIL NIL \"<4D5547D2.9050008@linagora.com>\" \"<4D556074.8050406@linagora.com>\"))";
		
		Envelope envelope = UIDFetchEnvelopeCommand.parseEnvelope(fullPayload);
		
		assertThat(envelope.getFrom()).containsOnly(new Address("Robert Dupont", "rdupont@linagora.com"));
		assertThat(envelope.getTo()).containsOnly(new Address(null, "boss@linagora.com"));
		assertThat(envelope.getCc()).isEmpty();
		assertThat(envelope.getBcc()).isEmpty();
		assertThat(envelope.getDate()).isEqualTo(DateUtils.date("2011-02-11T17:14:44"));
		assertThat(envelope.getMessageId()).isEqualTo("<4D556074.8050406@linagora.com>");
		assertThat(envelope.getReplyTo()).containsOnly(new Address("Robert Dupont", "rdupont@linagora.com"));
		assertThat(envelope.getSubject()).isEqualTo("Re: Tu ne m'oublies pas ;-)");
	}
	
	@Test
	public void testHandleMultipleResponsesWithOnlyOneCorresponding() {
		IMAPResponse response = new IMAPResponse("OK", "* 16931 FETCH (FLAGS (Junk) UID 735417)");
		IMAPResponse response2 = new IMAPResponse("OK", "* 16864 FETCH (UID 735319 ENVELOPE (\"Mon, 17 Dec 2012 10:54:27 +0100\" \"Re: Courrier =?UTF-8?Q?recommand=C3=A9=20-=20Statut=20-=20PV?=\" " +
				"((NIL NIL \"usera\" \"linagora.com\")) ((NIL NIL \"usera\" \"linagora.com\")) " +
				"((NIL NIL \"usera\" \"linagora.com\")) ((\"=?UTF-8?Q?Jean-S=C3=A9bastien_Din=C3=A9ty?=\" NIL \"dinety\" \"other.fr\")) " +
				"((\"User B\" NIL \"userb\" \"linagora.com\")(NIL NIL \"userc\" \"other.fr\")(NIL NIL \"userd\" \"other.fr\")" +
					"(\"User E\" NIL \"usere\" \"linagora.com\")(\"Administration\" NIL \"administration\" \"linagora.com\")" +
					"(\"=?UTF-8?Q?userf=C3=A7ois_userf?=\" NIL \"userf.domain\" \"other.fr\")) " +
				"NIL \"<50CEDB75.80401@other.fr>\" \"<91b86812066230173c396bbf195bbbfd@linagora.com>\"))");
		IMAPResponse response3 = new IMAPResponse("OK", "");
		
		UIDFetchEnvelopeCommand command = new UIDFetchEnvelopeCommand(MessageSet.singleton(735417));
		command.handleResponses(ImmutableList.of(response, response2, response3));
		
		assertThat(command.getReceivedData()).hasSize(1);
	}
	
	@Test
	public void testParseEnvelopeWithDifferentFromThanReplyTo() {
		String fullPayload = "* 1444 FETCH (UID 590923 ENVELOPE " +
				"(\"Fri, 11 Feb 2011 17:14:44 +0100\" \"Re: Tu ne m'oublies pas ;-)\" " +
				"((\"from address\" NIL \"from\" \"linagora.com\")) " +
				"((\"sender address\" NIL \"sender\" \"linagora.com\")) " +
				"((\"reply-to address\" NIL \"reply-to\" \"linagora.com\")) " +
				"((NIL NIL \"boss\" \"linagora.com\")) NIL NIL \"<4D5547D2.9050008@linagora.com>\" \"<4D556074.8050406@linagora.com>\"))";
		
		Envelope envelope = UIDFetchEnvelopeCommand.parseEnvelope(fullPayload);
		
		assertThat(envelope.getFrom()).containsOnly(new Address("from address", "from@linagora.com"));
		assertThat(envelope.getTo()).containsOnly(new Address(null, "boss@linagora.com"));
		assertThat(envelope.getCc()).isEmpty();
		assertThat(envelope.getBcc()).isEmpty();
		assertThat(envelope.getDate()).isEqualTo(DateUtils.date("2011-02-11T17:14:44"));
		assertThat(envelope.getMessageId()).isEqualTo("<4D556074.8050406@linagora.com>");
		assertThat(envelope.getReplyTo()).containsOnly(new Address("reply-to address", "reply-to@linagora.com"));
		assertThat(envelope.getSubject()).isEqualTo("Re: Tu ne m'oublies pas ;-)");
	}
}
