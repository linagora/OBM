package org.obm.push;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.field.address.ParseException;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class ErrorsManagerTest {

	@Test
	public void testPrepareMessage() throws ParseException, FileNotFoundException, IOException {
		User user = User.Factory.create().createUser("test@domain", "test@domain", "displayName");
		BackendSession backendSession = new BackendSession(new Credentials(user, "password", null), null, null, null);
		
		Mime4jUtils mime4jUtils = new Mime4jUtils();
		
		ErrorsManager errorsManager = new ErrorsManager(null, null, mime4jUtils);
		Message message = errorsManager.prepareMessage(backendSession, "Subject", "Body", 
				StreamMailTestsUtils.newInputStreamFromString("It's mail content !"));
		
		InputStream stream = mime4jUtils.toInputStream(message);
		String messageAsString = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
		
		Assertions.assertThat(message).isNotNull();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(messageAsString).contains("test@domain")
					.contains("Subject: Subject")
					.contains("Body")
					.contains("Content-Disposition: attachment; filename=error_message.eml")
					.contains("It's mail content !");
	}

}
