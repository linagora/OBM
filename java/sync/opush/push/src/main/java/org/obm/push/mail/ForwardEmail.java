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
import org.obm.push.utils.Mime4jUtils;

import com.google.common.base.Charsets;

public class ForwardEmail extends SendEmail {

	private static final Charset DEFAULT_ENCODING = Charsets.UTF_8;
	private static final String FORWARD_FILENAME = "forwarded_message.eml";
	private Multipart mixedMultiPart;

	public ForwardEmail(Mime4jUtils mime4jUtils, String defaultFrom, InputStream forwarded, Message message) throws FileNotFoundException, IOException, ParserException, MimeException {
		super(defaultFrom, message);
		
		mixedMultiPart = mime4jUtils.createMultiPartMixed();
		mixedMultiPart.addBodyPart(this.message);
		mime4jUtils.attach(mixedMultiPart, forwarded, FORWARD_FILENAME, ContentTypeField.TYPE_TEXT_PLAIN);
		
		MessageImpl newMessage = mime4jUtils.createMessage();
		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(DEFAULT_ENCODING);
		newMessage.setMultipart(mixedMultiPart,params);
		
		mimeData = serializeMimeData(newMessage).toByteArray();
	}
}
