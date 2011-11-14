package org.obm.push.mail;

import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.MessageImpl;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.OpushConfigurationService;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.Mime4jUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

public class ReplyEmail extends SendEmail {

	private final Mime4jUtils mime4jUtils;
	private final OpushConfigurationService configuration;
	
	public ReplyEmail(OpushConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, MSEmail originMail, Message message) throws ParserException, MimeException, IOException, TransformerException {
		super(defaultFrom, message);
		this.configuration = configuration;
		this.mime4jUtils = mime4jUtils;
		message = quoteAndAppendRepliedMail(originMail);
	}

	private Message quoteAndAppendRepliedMail(MSEmail originMail) throws IOException, TransformerException {
		String originalEmail = originMail.getBody().getValue(MSEmailBodyType.PlainText);
		String originalEmailAsHtml = originMail.getBody().getValue(MSEmailBodyType.HTML);

		if (nothingToQuote(originalEmail, originalEmailAsHtml)) {
			return originalMessage; 
		}

		if (originalMessage.isMultipart()) {
			Multipart multipart = (Multipart)originalMessage.getBody();
			if (isMultipartAlternative(multipart)) {

				return quoteAndReplyMultipartAlternative(originalEmail, 
						originalEmailAsHtml, multipart);
			}
			//TODO: handle multipart/mixed
		} else {
			String mimeType = originalMessage.getMimeType();
			if (mimeType.equalsIgnoreCase("text/plain") && originalEmail != null) {
				return createMessageWithBody(mimeType,
						appendQuotedMailToPlainText((TextBody)originalMessage.getBody(), originalEmail));
			} else if (mimeType.equalsIgnoreCase("text/html") && originalEmailAsHtml != null) {
				return createMessageWithBody(mimeType,
						appendRepliedMailToHtml((TextBody)originalMessage.getBody(), originalEmailAsHtml));
			}
		}
		return message;
	}

	private Message quoteAndReplyMultipartAlternative(String originalEmail,
			String originalEmailAsHtml, Multipart multipart)
			throws IOException, TransformerException {
		TextBody modifiedBodyText = null;
		TextBody modifiedBodyHtmlOverText = null;
		TextBody modifiedBodyHtmlOverHtml = null;
		
		Entity plainTextPart = mime4jUtils.getFirstTextPlainPart(multipart);
		if (plainTextPart != null) {
			modifiedBodyText = quotePlainTextPart(originalEmail, plainTextPart);
		}
		
		Entity htmlTextPart = mime4jUtils.getFirstTextHTMLPart(multipart);
		if (htmlTextPart != null) {
			modifiedBodyHtmlOverText = quoteHtmlTextPart(originalEmail, htmlTextPart);
			modifiedBodyHtmlOverHtml = quoteHtmlTextPart(originalEmailAsHtml, htmlTextPart);
		}

		return buildSingleOrMultipartMessage(modifiedBodyText, modifiedBodyHtmlOverText, modifiedBodyHtmlOverHtml);
	}

	private TextBody quoteHtmlTextPart(String repliedEmailAsHtml, Entity htmlTextPart) 
			throws IOException, TransformerException {
		if (repliedEmailAsHtml != null) {		
			return appendRepliedMailToHtml((TextBody)htmlTextPart.getBody(), repliedEmailAsHtml);
		}
		return null;
	}

	private TextBody quotePlainTextPart(String repliedEmail, Entity plainTextPart) 
			throws IOException {
		if (repliedEmail != null) {
			return appendQuotedMailToPlainText((TextBody)plainTextPart.getBody(), repliedEmail);
		}
		return null;
	}

	private boolean isMultipartAlternative(Multipart multipart) {
		return multipart.getSubType().equalsIgnoreCase("alternative");
	}

	private boolean nothingToQuote(String repliedEmail,
			String repliedEmailAsHtml) {
		return repliedEmail == null && repliedEmailAsHtml == null;
	}

	private Message buildSingleOrMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlOverText, TextBody modifiedBodyHtmlOverHtml ) {
		if (modifiedBodyText == null && modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null) {
			throw new InvalidParameterException();
		}

		TextBody modifiedBodyHtmlPrefered = getPreferedHtmlPart(modifiedBodyHtmlOverText,modifiedBodyHtmlOverHtml); 

		if (modifiedBodyText != null && modifiedBodyHtmlPrefered != null) {
			return createTwoPartsMultipartAlternative(modifiedBodyText, modifiedBodyHtmlPrefered);
		} else {
			if (modifiedBodyText != null) {
				return createMessageWithBody(ContentTypeField.TYPE_TEXT_PLAIN, modifiedBodyText);
			} else {
				return createMessageWithBody("text/html", modifiedBodyHtmlPrefered);
			}
		}
	}

	private Message createMessageWithBody(String mimeType, TextBody modifiedBodyText) {
		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(configuration.getDefaultEncoding());
		MessageImpl newMessage = mime4jUtils.createMessage();
		newMessage.setBody(modifiedBodyText, mimeType, params);
		return newMessage;
	}

	private TextBody getPreferedHtmlPart(TextBody modifiedBodyHtmlOverText,	TextBody modifiedBodyHtmlOverHtml) {
		
		if (modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null){
			return null;
		}
		return Objects.firstNonNull(modifiedBodyHtmlOverHtml, modifiedBodyHtmlOverText);
	}

	private Message createTwoPartsMultipartAlternative(TextBody modifiedBodyText,
			TextBody modifiedBodyHtml) {
		Multipart multipartReply = this.mime4jUtils.createMultipartAlternative();
		multipartReply.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyText,ContentTypeField.TYPE_TEXT_PLAIN));
		multipartReply.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyHtml,"text/html"));
		
		Map<String, String> params = mime4jUtils.getContentTypeHeaderMultipartParams(configuration.getDefaultEncoding());
		MessageImpl newMessage = mime4jUtils.createMessage();
		newMessage.setBody(multipartReply, originalMessage.getMimeType(), params);
		
		return newMessage;
	}

	private TextBody appendQuotedMailToPlainText(TextBody plainTextPart, String repliedEmail) throws IOException {
		String newMailContent = CharStreams.toString(plainTextPart.getReader());
		StringBuilder stringBuilder = new StringBuilder(newMailContent);
		
		stringBuilder.append('\n');
		for (String line: Splitter.on('\n').split(repliedEmail)) {
			stringBuilder.append("\n> ").append(line);
		}
		BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
		return basicBodyFactory.textBody(stringBuilder.toString(), plainTextPart.getMimeCharset());

	}
	
	private TextBody appendRepliedMailToHtml(TextBody htmlPart, String repliedEmail) throws IOException, TransformerException {
		try {
			final InputSource replySource = new InputSource(htmlPart.getReader());
			final InputSource originalSource = new InputSource(new StringReader(repliedEmail));

			final Document replyHtmlDoc = DOMUtils.parseHtmlAsDocument(replySource);
			final Node originalHtmlNode = DOMUtils.parseHtmlAsFragment(originalSource);

			final Element quoteBlock = insertIntoQuoteblock(replyHtmlDoc, originalHtmlNode);
			final Element bodyNode = DOMUtils.getUniqueElement(replyHtmlDoc.getDocumentElement(), "BODY");
			bodyNode.appendChild(quoteBlock);

			String buffer = DOMUtils.serializeHtmlDocument(replyHtmlDoc);
			BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
			return basicBodyFactory.textBody(buffer, htmlPart.getMimeCharset());
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return appendQuotedMailToPlainText(htmlPart, repliedEmail);
	}

	private Element insertIntoQuoteblock(Document replyDoc, Node originalNodeToQuote) {
		originalNodeToQuote = replyDoc.importNode(originalNodeToQuote, true);

		final Element quoteBlock = replyDoc.createElement("blockquote");
        quoteBlock.setAttribute("style", "border-left:1px solid black;");
        quoteBlock.appendChild(originalNodeToQuote);
        return quoteBlock;
	}
	
}
