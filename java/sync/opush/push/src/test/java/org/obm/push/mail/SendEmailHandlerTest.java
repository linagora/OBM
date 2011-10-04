package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.junit.Ignore;

import junit.framework.TestCase;

@Ignore
public class SendEmailHandlerTest extends TestCase {

	public void testAndroidIsInvitation() throws MimeException, IOException{
		InputStream eml = loadDataFile("androidInvit.eml");
		SendEmailHandler handler = new SendEmailHandler("john@test.opush");
		MimeStreamParser parser = new MimeStreamParser();
		parser.setContentHandler(handler);
		parser.parse(eml);
		
		assertTrue(handler.isInvitation());
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/eml/" + name);
	}
}
