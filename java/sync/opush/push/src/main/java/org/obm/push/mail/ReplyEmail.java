package org.obm.push.mail;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

public class ReplyEmail extends SendEmail {

	public ReplyEmail(Mime4jUtils mime4jUtils, String defaultFrom, MSEmail originMail, Message message) throws ParserException, IOException, MimeException {
		super(defaultFrom, message);
		Body modifiedBody = quoteAndAppendRepliedMail(originMail);
		Message newMessage = mime4jUtils.createMessage();
		newMessage.setBody(modifiedBody);
		this.message = newMessage;
	}

	private Body quoteAndAppendRepliedMail(MSEmail originMail)
			throws IOException {
		String repliedEmail = originMail.getBody().getValue(MSEmailBodyType.PlainText);
		if (repliedEmail != null) {
			if (message.isMultipart()) {
				Multipart multipart = (Multipart)message.getBody();
				if (multipart.getSubType().equalsIgnoreCase("alternative")) {
					Entity plainTextPart = getPlainTextPart(multipart);
					if (plainTextPart != null) {
						return appendRepliedMailToPlainText((TextBody)plainTextPart.getBody(), repliedEmail);
					}
				}
				//TODO: handle multipart/mixed
			} else {
				if (message.getMimeType().equalsIgnoreCase("text/plain")) {
					Body body = message.getBody();
					return appendRepliedMailToPlainText((TextBody)body, repliedEmail);
				}
			}
		}
		return message; 
	}

	private TextBody appendRepliedMailToPlainText(TextBody plainTextPart, String repliedEmail) throws IOException {
		String newMailContent = CharStreams.toString(plainTextPart.getReader());
		StringBuilder stringBuilder = new StringBuilder(newMailContent);
		stringBuilder.append('\n');
		for(String line: Splitter.on('\n').split(repliedEmail)) {
			stringBuilder.append("> " + line);
		}
		BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
		return basicBodyFactory.textBody(stringBuilder.toString(), plainTextPart.getMimeCharset());

	}

	private Entity getPlainTextPart(Multipart multipart) {
		for (Entity entity: multipart.getBodyParts()) {
			if (entity.getMimeType().equalsIgnoreCase("text/plain")) {
				return entity;
			}
		}
		return null;
	}
}
