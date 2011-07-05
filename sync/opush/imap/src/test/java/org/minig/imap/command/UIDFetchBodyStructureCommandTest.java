package org.minig.imap.command;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.minig.imap.command.parser.BodyStructureParser;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.mime.MimeMessage;

import com.google.common.collect.ImmutableList;

public class UIDFetchBodyStructureCommandTest {

	private static final String INPUT_LINE1 = 
		"* 11 FETCH (UID 54 BODYSTRUCTURE ((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 620 23 NIL NIL NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dpl=F4m=E9s_avant_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 266416 NIL (\"INLINE\" (\"FILENAME*\" {93}";
	private static final String INPUT_BYTESTREAM =
		"ISO-8859-1''%44%70%6C%F4%6D%E9%73%20%61%76%61%6E%74%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dipl=F4m=E9s_depuis_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 518522 NIL (\"INLINE\" (\"FILENAME*\" {99}" +
		"ISO-8859-1''%44%69%70%6C%F4%6D%E9%73%20%64%65%70%75%69%73%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL) \"MIXED\" (\"BOUNDARY\" \"------------040903010203040509010609\") NIL NIL NIL))";

	private static final String OUTPUT = 
		"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 620 23 NIL NIL NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dpl=F4m=E9s_avant_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 266416 NIL (\"INLINE\" (\"FILENAME*\" {93}" +
		"ISO-8859-1''%44%70%6C%F4%6D%E9%73%20%61%76%61%6E%74%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL)(\"APPLICATION\" \"X-ZIP-COMPRESSED\" (\"NAME\" \"=?ISO-8859-1?Q?Dipl=F4m=E9s_depuis_2001-2002=2Ezip?=\") NIL NIL \"BASE64\" 518522 NIL (\"INLINE\" (\"FILENAME*\" {99}" +
		"ISO-8859-1''%44%69%70%6C%F4%6D%E9%73%20%64%65%70%75%69%73%20%32%30%30%31%2D%32%30%30%32%2E%7A%69%70)) NIL NIL) \"MIXED\" (\"BOUNDARY\" \"------------040903010203040509010609\") NIL NIL NIL)";

	
	@Test
	public void testResponseReceived() {
		BodyStructureParser resultCallback = EasyMock.createMock(BodyStructureParser.class);
		Capture<String> result = new Capture<String>(CaptureType.FIRST);
		EasyMock.expect(resultCallback.parseBodyStructure(EasyMock.capture(result))).andReturn(new MimeMessage(null));
		EasyMock.replay(resultCallback);
		UIDFetchBodyStructureCommand uidFetchBodyStructureCommand = 
			new UIDFetchBodyStructureCommand(resultCallback, ImmutableList.of(54l));
		IMAPResponse response = new IMAPResponse("OK", INPUT_LINE1);
		response.setStreamData(new ByteArrayInputStream(INPUT_BYTESTREAM.getBytes()));
		uidFetchBodyStructureCommand.responseReceived(
				Arrays.asList(response, new IMAPResponse("OK", "")));
		EasyMock.verify(resultCallback);
		Assert.assertEquals(OUTPUT, result.getValue());
	}

}
