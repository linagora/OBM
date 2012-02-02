/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
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

	protected final static String EMAIL_LINEBREAKER = "\r\n";

	private final Mime4jUtils mime4jUtils;
	private final ConfigurationService configuration;
	private Entity originTextPlainPart;
	private Entity originTextHtmlPart;
	
	public ReplyEmail(ConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, 
			MSEmail originMail, Message message) throws MimeException, NotQuotableEmailException {
		
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

	private Message quoteAndReplyMultipart(String originalEmail, String originalEmailAsHtml) 
			throws NotQuotableEmailException {

		Multipart multipart = getMultiPart();
		TextBody quotedBodyText = quoteBodyText(originalEmail, multipart);
		TextBody quotedBodyTextOverHtml = quoteBodyHtml(originalEmail, multipart, false);
		TextBody quotedBodyHtmlOverHtml = quoteBodyHtml(originalEmailAsHtml, multipart, true);	
		return buildSingleOrMultipartMessage(quotedBodyText, quotedBodyTextOverHtml, quotedBodyHtmlOverHtml);	
	}

	private Multipart getMultiPart() {
		Multipart multipart = (Multipart)originalMessage.getBody();
		if (mime4jUtils.isMessageMultipartMixed(originalMessage)) {
			Multipart alternativeMultiPart = mime4jUtils.getAlternativeMultiPart(multipart);
			if (alternativeMultiPart != null) {
				return alternativeMultiPart;
			}
		}
		return multipart;
	}

	private boolean nothingToQuote(String repliedEmail, String repliedEmailAsHtml) {
		return repliedEmail == null && repliedEmailAsHtml == null;
	}

	private Message buildSingleOrMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlOverText, TextBody modifiedBodyHtmlOverHtml ) throws NotQuotableEmailException {
		if (modifiedBodyText == null && modifiedBodyHtmlOverText == null && modifiedBodyHtmlOverHtml == null) {
			throw new NotQuotableEmailException("No parts are quotable");
		}

		TextBody modifiedBodyHtmlPrefered = getPreferedHtmlPart(modifiedBodyHtmlOverText,modifiedBodyHtmlOverHtml); 
		boolean isMultipartMixed = this.mime4jUtils.isMessageMultipartMixed(this.originalMessage);

		if (modifiedBodyText != null && modifiedBodyHtmlPrefered != null || isMultipartMixed) {
			return createMultipartMessage(modifiedBodyText, modifiedBodyHtmlPrefered, isMultipartMixed);
		} else {
			return buildSingleMessage(modifiedBodyText, modifiedBodyHtmlPrefered);
		}
	}

	private Message buildSingleMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlPrefered) {
		if (modifiedBodyText != null) {
			return createMessageWithBody(ContentTypeField.TYPE_TEXT_PLAIN, modifiedBodyText);
		} else {
			return createMessageWithBody("text/html", modifiedBodyHtmlPrefered);
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
		
		if (modifiedBodyText != null) {
			multipartReply.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyText,ContentTypeField.TYPE_TEXT_PLAIN));
		}
		if (modifiedBodyHtmlPrefered != null) {
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
			return basicBodyFactory.textBody( 
					cleanLineBreaks( new StringReader(docAsText) ), 
					htmlPart.getMimeCharset());
			
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
