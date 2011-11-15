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
	private Entity originTextPlainPart;
	private Entity originTextHtmlPart;
	
	public ReplyEmail(OpushConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, MSEmail originMail, Message message) throws ParserException, MimeException, IOException, TransformerException {
		super(defaultFrom, message);
		this.configuration = configuration;
		this.mime4jUtils = mime4jUtils;
		this.message = quoteAndAppendRepliedMail(originMail);
	}

	private Message quoteAndAppendRepliedMail(MSEmail originMail) throws IOException, TransformerException {
		String originalEmail = originMail.getBody().getValue(MSEmailBodyType.PlainText);
		String originalEmailAsHtml = originMail.getBody().getValue(MSEmailBodyType.HTML);

		if (nothingToQuote(originalEmail, originalEmailAsHtml)) {
			return originalMessage; 
		}

		if (originalMessage.isMultipart()) {
			return quoteAndReplyMultipart(originalEmail, originalEmailAsHtml);
		} else {
			String mimeType = originalMessage.getMimeType();
			if (mime4jUtils.isMessagePlainText(originalMessage) && originalEmail != null) {
				return createMessageWithBody(mimeType,
						appendQuotedMailToPlainText((TextBody)originalMessage.getBody(), originalEmail));
			} else if (mime4jUtils.isMessageHtmlText(originalMessage) && originalEmailAsHtml != null) {
				return createMessageWithBody(mimeType,
						appendRepliedMailToHtml((TextBody)originalMessage.getBody(), originalEmailAsHtml));
			}
		}
		return message;
	}

	private Message quoteAndReplyMultipart(String originalEmail,
			String originalEmailAsHtml)
					throws IOException, TransformerException {

		Multipart multipart = (Multipart)originalMessage.getBody();
		TextBody quotedBodyText = quoteBodyText(originalEmail, multipart);
		TextBody quotedBodyTextOverHtml = quoteBodyHtml(originalEmail, multipart);
		TextBody quotedBodyHtmlOverHtml = quoteBodyHtml(originalEmailAsHtml, multipart);
		return buildSingleOrMultipartMessage(quotedBodyText, quotedBodyTextOverHtml, quotedBodyHtmlOverHtml);	
	}

	private boolean nothingToQuote(String repliedEmail, String repliedEmailAsHtml) {
		return repliedEmail == null && repliedEmailAsHtml == null;
	}

	private Message buildSingleOrMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlOverText, TextBody modifiedBodyHtmlOverHtml ) {
		if (modifiedBodyText == null && modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null) {
			throw new InvalidParameterException();
		}

		TextBody modifiedBodyHtmlPrefered = getPreferedHtmlPart(modifiedBodyHtmlOverText,modifiedBodyHtmlOverHtml); 

		if (modifiedBodyText != null && modifiedBodyHtmlPrefered != null) {
			return createMultipartMessage(modifiedBodyText, modifiedBodyHtmlPrefered, false);
		} else if (this.mime4jUtils.isMessageMultipartMixed(this.originalMessage)) {
			return createMultipartMessage(modifiedBodyText, modifiedBodyHtmlPrefered, true);
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

	private Message createMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlPrefered, boolean createMixed) {
		Multipart multipartReply = createMultipartMixedOrAlternative(createMixed);
		
		if (modifiedBodyText != null){
			multipartReply.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyText,ContentTypeField.TYPE_TEXT_PLAIN));
		}
		if (modifiedBodyHtmlPrefered != null){
			multipartReply.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyHtmlPrefered,"text/html"));
		}
		
		Map<String, String> params = mime4jUtils.getContentTypeHeaderMultipartParams(configuration.getDefaultEncoding());
		MessageImpl newMessage = mime4jUtils.createMessage();
		newMessage.setBody(multipartReply, originalMessage.getMimeType(), params);
		return newMessage;
	}

	private Multipart createMultipartMixedOrAlternative(boolean createMixed) {
		Multipart multipartReply;
		if (createMixed) {
			multipartReply = this.mime4jUtils.createMultipartMixed();
			copyOriginalMessagePartsToMultipartMessage(multipartReply);
		} else {
			multipartReply = this.mime4jUtils.createMultipartAlternative();
		}
		return multipartReply;
	}

	private void copyOriginalMessagePartsToMultipartMessage(Multipart multipart) {
		Multipart originalMultipart = (Multipart)originalMessage.getBody();
		for (Entity part: originalMultipart.getBodyParts()) {
			if (includePart(part)) {
				multipart.addBodyPart(part);
			}
		}
	}

	private boolean includePart(Entity part) {
		if (part.equals(originTextPlainPart) || part.equals(originTextHtmlPart)) {
			return false;
		}
		return true;
	}

	private TextBody getPreferedHtmlPart(TextBody modifiedBodyHtmlOverText,	TextBody modifiedBodyHtmlOverHtml) {
		if (modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null){
			return null;
		}
		return Objects.firstNonNull(modifiedBodyHtmlOverHtml, modifiedBodyHtmlOverText);
	}
	
	private TextBody quoteBodyText(String originalText, Multipart multipart) 
			throws IOException {
		Entity textPlainPart = mime4jUtils.getFirstTextPlainPart(multipart);
		if (textPlainPart != null && originalText != null) {
			setOriginTextPlainPart(textPlainPart);
			return appendQuotedMailToPlainText((TextBody)textPlainPart.getBody(), originalText);
		}
		return null;
	}
	
	private void setOriginTextPlainPart(Entity textPlainPart) {
		this.originTextPlainPart = textPlainPart;
	}

	private TextBody quoteBodyHtml(String originalHtml, Multipart multipart) 
			throws IOException, TransformerException {
		Entity textHtmlPart = this.mime4jUtils.getFirstTextHTMLPart(multipart);
		if (textHtmlPart != null && originalHtml != null) {
			setTextHtmlPart(textHtmlPart);
			return appendRepliedMailToHtml((TextBody)textHtmlPart.getBody(), originalHtml);
		}
		return null;
	}
	
	private void setTextHtmlPart(Entity htmlTextPart) {
		this.originTextHtmlPart = htmlTextPart;
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

			final String docAsText = DOMUtils.serializeHtmlDocument(replyHtmlDoc);

			BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
			return basicBodyFactory.textBody(docAsText, htmlPart.getMimeCharset());
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
