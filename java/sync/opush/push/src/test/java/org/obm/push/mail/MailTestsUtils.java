package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.obm.push.utils.Mime4jUtils;

public class MailTestsUtils {

	private static Mime4jUtils mime4jUtils = new Mime4jUtils();
	
	public static InputStream loadEmail(Class<?> testClass, String name) {
		return testClass.getClassLoader().getResourceAsStream("eml/" + name);
	}
	
	public static Message loadMimeMessage(Class<?> testClass, String name) throws MimeException, IOException {
		InputStream eml = loadEmail(testClass, name);
		Message message = mime4jUtils.parseMessage(eml);
		return message;
	}
}
