package org.obm.push.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.utils.Mime4jUtils;

public class ForwardEmail extends SendEmail {


	private static final String forwardFilename = "forwarded_message.eml";
	private Multipart mixedMultiPart;
	
	public ForwardEmail(Mime4jUtils mime4jUtils, String defaultFrom, InputStream forwarded, Message message) throws FileNotFoundException, IOException, ParserException, MimeException {
		super(defaultFrom, message);
		
		mixedMultiPart = mime4jUtils.createMultiPartMixed();
		mixedMultiPart.addBodyPart(this.message);
		mime4jUtils.attach(mixedMultiPart, forwarded, forwardFilename, "rfc822");
		Message newMessage = mime4jUtils.createMessage();
		newMessage.setBody(mixedMultiPart);
		mimeData = serializeMimeData(newMessage).toByteArray();
	}
	
}
