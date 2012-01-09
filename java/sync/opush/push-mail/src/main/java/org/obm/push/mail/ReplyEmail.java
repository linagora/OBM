package org.obm.push.mail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.MessageImpl;
import org.obm.configuration.ConfigurationService;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.exception.NotQuotableEmailException;
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

	private final static String EMAIL_LINEBREAKER = "\r\n";

	private final Mime4jUtils mime4jUtils;
	private final ConfigurationService configuration;
	private Entity originTextPlainPart;
	private Entity originTextHtmlPart;
	
	public ReplyEmail(ConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, MSEmail originMail, Message message) throws MimeException, NotQuotableEmailException {
		super(defaultFrom, message);
		this.configuration = configuration;
		this.mime4jUtils = mime4jUtils;
		setNewMessage(quoteAndAppendRepliedMail(originMail));
	}

	private void setNewMessage(Message newMessage) throws MimeException {
		newMessage.setSender(this.message.getSender());
		newMessage.setSubject(this.message.getSubject());
		newMessage.setBcc(this.message.getBcc());
		newMessage.setCc(this.message.getCc());
		newMessage.setTo(this.message.getTo());
		newMessage.setFrom(this.message.getFrom());
		setMessage(newMessage);
	}
	
	private Message quoteAndAppendRepliedMail(MSEmail originMail) throws NotQuotableEmailException {
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
						appendQuotedMailToPlainText((TextBody)originalMessage.getBody(), originalEmail.trim()));
			} else if (mime4jUtils.isMessageHtmlText(originalMessage) && originalEmailAsHtml != null) {
				return createMessageWithBody(mimeType,
						appendRepliedMailToHtml((TextBody)originalMessage.getBody(), originalEmailAsHtml.trim()));
			}
		}
		return message;
	}

	private Message quoteAndReplyMultipart(String originalEmail,
			String originalEmailAsHtml)
					throws NotQuotableEmailException {

		Multipart multipart = (Multipart)originalMessage.getBody();
		TextBody quotedBodyText = quoteBodyText(originalEmail, multipart);
		TextBody quotedBodyTextOverHtml = quoteBodyHtml(originalEmail, multipart, false);
		TextBody quotedBodyHtmlOverHtml = quoteBodyHtml(originalEmailAsHtml, multipart, true);
		return buildSingleOrMultipartMessage(quotedBodyText, quotedBodyTextOverHtml, quotedBodyHtmlOverHtml);	
	}

	private boolean nothingToQuote(String repliedEmail, String repliedEmailAsHtml) {
		return repliedEmail == null && repliedEmailAsHtml == null;
	}

	private Message buildSingleOrMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlOverText, TextBody modifiedBodyHtmlOverHtml ) throws NotQuotableEmailException {
		if (modifiedBodyText == null && modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null) {
			throw new NotQuotableEmailException("No parts are quotable");
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
	
	private TextBody quoteBodyText(String originalText, Multipart multipart) throws NotQuotableEmailException {
		Entity textPlainPart = mime4jUtils.getFirstTextPlainPart(multipart);
		if (textPlainPart != null && originalText != null) {
			setOriginTextPlainPart(textPlainPart);
			return appendQuotedMailToPlainText((TextBody)textPlainPart.getBody(), originalText.trim());
		}
		return null;
	}
	
	private void setOriginTextPlainPart(Entity textPlainPart) {
		this.originTextPlainPart = textPlainPart;
	}

	private TextBody quoteBodyHtml(String originalText, Multipart multipart, boolean originalIsHtml) 
			throws NotQuotableEmailException {
		Entity textHtmlPart = this.mime4jUtils.getFirstTextHTMLPart(multipart);
		if (textHtmlPart != null && originalText != null) {
			setTextHtmlPart(textHtmlPart);
			String htmlToQuote = originalIsHtml ? originalText.trim() : encodeTxtInHtml(originalText.trim());
			return appendRepliedMailToHtml((TextBody)textHtmlPart.getBody(), htmlToQuote);
		}
		return null;
	}
	
	private void setTextHtmlPart(Entity htmlTextPart) {
		this.originTextHtmlPart = htmlTextPart;
	}
	
	private String encodeTxtInHtml(String originalText) {
		StringBuilder stringBuilder = new StringBuilder();
		String lineHtml;
		for (String line: Splitter.on('\n').split(originalText)) {
			lineHtml = StringEscapeUtils.escapeHtml(line);
			stringBuilder.append(lineHtml).append("<BR/>");
		}
		return stringBuilder.toString();
	}

	private TextBody appendQuotedMailToPlainText(TextBody plainTextPart, String repliedEmail) throws NotQuotableEmailException {
		try {	
			StringBuilder bodyTextPlainBuilder = new StringBuilder();
			Reader plainTextReader = plainTextPart.getReader();
			bodyTextPlainBuilder.append(cleanLineBreaks(plainTextReader));
			bodyTextPlainBuilder.append(quoteOnLineBreaks(repliedEmail));
			BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
			return basicBodyFactory.textBody(bodyTextPlainBuilder.toString(), plainTextPart.getMimeCharset());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Text part isn't quotable", e);
		}
	}

	private String quoteOnLineBreaks(String toQuote) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(EMAIL_LINEBREAKER);

		List<String> linesWithoutTermination = CharStreams.readLines(new StringReader(toQuote));
		for (String line: linesWithoutTermination) {
			stringBuilder.append(EMAIL_LINEBREAKER).append("> ").append(line);
		}
		return stringBuilder.toString();
	}
	
	private String cleanLineBreaks(Reader content) throws IOException {
		// RFC 2821 2.3.7 : \r and \n are not supposed to be encountered alone 
		List<String> linesWithoutTermination = CharStreams.readLines(content);
		StringBuilder stringBuilder = new StringBuilder();
		for (String line : linesWithoutTermination) {
			stringBuilder.append(line).append(EMAIL_LINEBREAKER);
		}
		return stringBuilder.toString();
	}

	private TextBody appendRepliedMailToHtml(TextBody htmlPart, String repliedEmail) throws NotQuotableEmailException {
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
		} catch (TransformerException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Html part isn't quotable", e);
		} catch (SAXException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Html part isn't quotable", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Html part isn't quotable", e);
		}
	}

	private Element insertIntoQuoteblock(Document replyDoc, Node originalNodeToQuote) {
		originalNodeToQuote = replyDoc.importNode(originalNodeToQuote, true);

		final Element quoteBlock = replyDoc.createElement("blockquote");
        quoteBlock.setAttribute("style", "border-left:1px solid black; padding-left:1px;");
        quoteBlock.appendChild(originalNodeToQuote);
        return quoteBlock;
	}
}
