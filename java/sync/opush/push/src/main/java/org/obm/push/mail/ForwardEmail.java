package org.obm.push.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.MessageImpl;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.OpushConfigurationService;
import org.obm.push.utils.Mime4jUtils;

public class ForwardEmail extends SendEmail {

	private static final String FORWARD_FILENAME = "forwarded_message.eml";
	private Multipart mixedMultiPart;

	public ForwardEmail(OpushConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, InputStream forwarded, Message message) throws FileNotFoundException, IOException, ParserException, MimeException {
		super(defaultFrom, message);
		
		mixedMultiPart = mime4jUtils.createMultiPartMixed();
		mixedMultiPart.addBodyPart(this.originalMessage);
		mime4jUtils.attach(mixedMultiPart, forwarded, FORWARD_FILENAME, ContentTypeField.TYPE_TEXT_PLAIN);
		
		MessageImpl newMessage = mime4jUtils.createMessage();
		Charset defaultEncoding = configuration.getDefaultEncoding();
		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(defaultEncoding);
		newMessage.setMultipart(mixedMultiPart,params);
		
		mimeData = serializeMimeData(newMessage).toByteArray();
	}
}
