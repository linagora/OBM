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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class UIDFetchBodyStructureCommandTest {

	private static final String INPUT_LINE1 = 
		"* 11 FETCH (UID 54 RFC822.SIZE 123 BODYSTRUCTURE ((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 620 23 NIL NIL NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dpl=F4m=E9s_avant_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 266416 NIL (\"INLINE\" (\"FILENAME*\" {93}";
	private static final String INPUT_BYTESTREAM =
		"ISO-8859-1''%44%70%6C%F4%6D%E9%73%20%61%76%61%6E%74%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dipl=F4m=E9s_depuis_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 518522 NIL (\"INLINE\" (\"FILENAME*\" {99}" +
		"ISO-8859-1''%44%69%70%6C%F4%6D%E9%73%20%64%65%70%75%69%73%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL) \"MIXED\" (\"BOUNDARY\" \"------------040903010203040509010609\") NIL NIL NIL))";

	private static final String OUTPUT = 
		"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 620 23 NIL NIL NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dpl=F4m=E9s_avant_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 266416 NIL (\"INLINE\" (\"FILENAME*\" {93}" +
		"ISO-8859-1''%44%70%6C%F4%6D%E9%73%20%61%76%61%6E%74%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dipl=F4m=E9s_depuis_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 518522 NIL (\"INLINE\" (\"FILENAME*\" {99}" +
		"ISO-8859-1''%44%69%70%6C%F4%6D%E9%73%20%64%65%70%75%69%73%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL) \"MIXED\" (\"BOUNDARY\" \"------------040903010203040509010609\") NIL NIL NIL)";

	
	@Test @Slow
	public void testHandleResponses() {
		BodyStructureParser resultCallback = createMock(BodyStructureParser.class);
		Capture<String> result = new Capture<String>(CaptureType.FIRST);
		MimeMessageImpl.Builder mimeMessageBuilder = createNiceMock(MimeMessageImpl.Builder.class);
		expect(mimeMessageBuilder.uid(anyLong())).andReturn(mimeMessageBuilder);
		expect(mimeMessageBuilder.size(anyInt())).andReturn(mimeMessageBuilder);
		expect(resultCallback.parseBodyStructure(capture(result))).andReturn(mimeMessageBuilder);
		replay(resultCallback, mimeMessageBuilder);
		UIDFetchBodyStructureCommand uidFetchBodyStructureCommand = 
			new UIDFetchBodyStructureCommand(resultCallback, MessageSet.singleton(54l));
		IMAPResponse response = new IMAPResponse("OK", INPUT_LINE1);
		response.setStreamData(new ByteArrayInputStream(INPUT_BYTESTREAM.getBytes()));
		uidFetchBodyStructureCommand.responseReceived(
				Arrays.asList(response, new IMAPResponse("OK", "")));
		verify(resultCallback, mimeMessageBuilder);
		assertThat(result.getValue()).isEqualTo(OUTPUT);
	}
	
	@Test
	public void testHandleMultipleResponsesWithOnlyOneCorresponding() {
		BodyStructureParser resultCallback = createMock(BodyStructureParser.class);
		Capture<String> result = new Capture<String>(CaptureType.FIRST);
		MimeMessageImpl.Builder mimeMessageBuilder = createNiceMock(MimeMessageImpl.Builder.class);
		expect(mimeMessageBuilder.uid(anyLong())).andReturn(mimeMessageBuilder);
		expect(mimeMessageBuilder.size(anyInt())).andReturn(mimeMessageBuilder);
		expect(resultCallback.parseBodyStructure(capture(result))).andReturn(mimeMessageBuilder);
		replay(resultCallback, mimeMessageBuilder);
		
		UIDFetchBodyStructureCommand uidFetchBodyStructureCommand = 
				new UIDFetchBodyStructureCommand(resultCallback, MessageSet.singleton(54l));
		IMAPResponse response = new IMAPResponse("OK", "* 16931 FETCH (FLAGS (Junk) UID 735417)");
		IMAPResponse response2 = new IMAPResponse("OK", INPUT_LINE1);
		response2.setStreamData(new ByteArrayInputStream(INPUT_BYTESTREAM.getBytes()));
		IMAPResponse response3 = new IMAPResponse("OK", "");
		
		uidFetchBodyStructureCommand.responseReceived(ImmutableList.of(response, response2, response3));
		
		verify(resultCallback, mimeMessageBuilder);
		assertThat(result.getValue()).isEqualTo(OUTPUT);
		assertThat(uidFetchBodyStructureCommand.getReceivedData()).hasSize(1);
	}
}
