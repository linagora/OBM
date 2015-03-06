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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;


public class UIDFetchMessageCommandTest {
	
	@Test
	public void requestWithoutTruncation() {
		assertThat(new UIDFetchMessageCommand(12, null).buildCommand().getCommandString())
			.isEqualTo("UID FETCH 12 (UID BODY.PEEK[])");
	}
	
	@Test
	public void requestWithTruncation() {
		assertThat(new UIDFetchMessageCommand(12, 15l).buildCommand().getCommandString())
			.isEqualTo("UID FETCH 12 (UID BODY.PEEK[]<0.15>)");
	}
	
	@Test
	public void testHandleMultipleResponsesWithOnlyOneCorresponding() throws IOException {
		IMAPResponse response = new IMAPResponse("OK", "* OK [COPY 23 1 2]");
		IMAPResponse response2 = new IMAPResponse("OK", "* 1 FETCH (UID 12 BODY[] {1321}");
		String streamData =
			"Return-Path: <userb@antoine.org>" +
			"Received: from debian.antoine.org (localhost [127.0.0.1])" +
			"	 by debian (Cyrus v2.4.16-Debian-2.4.16-2~obm60+1) with LMTPA;" +
			"	 Fri, 14 Dec 2012 10:56:18 +0100" +
			"X-Sieve: CMU Sieve 2.4" +
			"Received: from [127.0.1.1] (localhost [127.0.0.1])" +
			"	by debian.antoine.org (Postfix) with ESMTP id 282F83C5B3" +
			"	for <usera@antoine.org>; Fri, 14 Dec 2012 10:56:18 +0100 (CET)" +
			"MIME-Version: 1.0" +
			"From: userb@antoine.org" +
			"Subject: Test" +
			"Date: Fri, 14 Dec 2012 10:46:28 +0100" +
			"To: \"usera@antoine.org\" <usera@antoine.org>" +
			"Content-Type: multipart/alternative;" +
			"	boundary=\"_7BCFA324-AD66-0AD4-44B3-CB2D2FF20425_\"" +
			"Message-Id: <20121214095618.282F83C5B3@debian.antoine.org>" +
			"" +
			"--_7BCFA324-AD66-0AD4-44B3-CB2D2FF20425_" +
			"Content-Transfer-Encoding: quoted-printable" +
			"Content-Type: text/plain; charset=\"Windows-1252\"" +
			"" +
			"" +
			"" +
			"Envoy=E9 =E0 partir de mon Windows Phone" +
			"--_7BCFA324-AD66-0AD4-44B3-CB2D2FF20425_" +
			"Content-Transfer-Encoding: quoted-printable" +
			"Content-Type: text/html; charset=\"Windows-1252\"" +
			"" +
			"<html><head><meta content=3D\"text/html; charset=3Dwindows-1252\" http-equiv=" +
			"=3D\"Content-Type\"></head><body><div><div style=3D\"font-family: Calibri,sans=" +
			"-serif; font-size: 11pt;\"><br><br>Envoy=E9 =E0 partir de mon Windows Phone<=" +
			"br></div></div></body></html>" +
			"--_7BCFA324-AD66-0AD4-44B3-CB2D2FF20425_--" +
			"";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(new String(streamData + ")").getBytes());
		response2.setStreamData(inputStream);
		IMAPResponse response3 = new IMAPResponse("OK", "");
		
		UIDFetchMessageCommand command = new UIDFetchMessageCommand(12);
		command.handleResponses(ImmutableList.of(response, response2, response3));
		
		assertThat(ByteStreams.toByteArray(command.getReceivedData()))
			.isEqualTo(streamData.getBytes());
	}
}
