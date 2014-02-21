/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MessageImpl;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.exception.NotQuotableEmailException;
import org.obm.push.utils.DOMUtils;
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
	private final OpushConfiguration opushConfiguration;
	private Entity originTextPlainPart;
	private Entity originTextHtmlPart;

	private final Map<String, MSAttachementData> originalMailAttachments;
	
	public ReplyEmail(OpushConfiguration opushConfiguration, Mime4jUtils mime4jUtils, String defaultFrom, Map<MSEmailBodyType, EmailView> originMails, 
			Message message, Map<String, MSAttachementData> originalMailAttachments) throws MimeException, NotQuotableEmailException, UnsupportedEncodingException, IOException {
		
		super(defaultFrom, message);
		this.opushConfiguration = opushConfiguration;
		this.mime4jUtils = mime4jUtils;
		this.originalMailAttachments = originalMailAttachments;
		setNewMessage(quoteAndAppendRepliedMail(originMails));
	}

	private void setNewMessage(Message newMessage) throws MimeException {
		newMessage.setSender(this.message.getSender());
		newMessage.setSubject(this.message.getSubject());
		newMessage.setBcc(this.message.getBcc());
		newMessage.setCc(this.message.getCc());
		newMessage.setTo(this.message.getTo());
		newMessage.setFrom(this.message.getFrom());
		newMessage.setDate(this.message.getDate());
		newMessage.setReplyTo(this.message.getReplyTo());
		setMessage(newMessage);
	}
	
	private Message quoteAndAppendRepliedMail(Map<MSEmailBodyType, EmailView> originMails) 
			throws NotQuotableEmailException, MimeException, UnsupportedEncodingException, IOException {
		
		String originalEmail = getBodyValue(MSEmailBodyType.PlainText, originMails);
		String originalEmailAsHtml = getBodyValue(MSEmailBodyType.HTML, originMails);

		if (nothingToQuote(originalEmail, originalEmailAsHtml)) {
			return originalMessage; 
		}

		if (originalMessage.isMultipart()) {
			return quoteAndReplyMultipart(originalEmail, originalEmailAsHtml);
		} else {
			String mimeType = originalMessage.getMimeType();
			if (isTextOverText(originalEmail)) {
				return createMessageWithBody(mimeType,
						appendQuotedMailToPlainText((TextBody)originalMessage.getBody(), originalEmail.trim()));
			} else if (isHtmlOverHtml(originalEmailAsHtml)) {
				return createMessageWithBody(mimeType,
						appendRepliedMailToHtml((TextBody)originalMessage.getBody(), originalEmailAsHtml.trim()));
			} else if (isTextOverHtml(originalEmailAsHtml)) {
				return createMessageWithBody("text/html",
						appendRepliedTextMailToHtml((TextBody)originalMessage.getBody(), originalEmailAsHtml.trim()));
			}
		}
		return message;
	}
	
	private String getBodyValue(MSEmailBodyType bodyType, Map<MSEmailBodyType, EmailView> originMails) throws UnsupportedEncodingException, IOException {
		EmailView originalPlainTextEmail = originMails.get(bodyType);
		if (originalPlainTextEmail != null) {
			return CharStreams.toString(new InputStreamReader(originalPlainTextEmail.getBodyMimePartData(), originalPlainTextEmail.getCharset()));
		}
		return null;
	}

	private boolean isTextOverHtml(String originalEmailAsHtml) {
		return mime4jUtils.isMessagePlainText(originalMessage) && originalEmailAsHtml != null;
	}

	private boolean isHtmlOverHtml(String originalEmailAsHtml) {
		return mime4jUtils.isMessageHtmlText(originalMessage) && originalEmailAsHtml != null;
	}

	private boolean isTextOverText(String originalEmail) {
		return mime4jUtils.isMessagePlainText(originalMessage) && originalEmail != null;
	}

	private Message quoteAndReplyMultipart(String originalEmail, String originalEmailAsHtml) 
			throws NotQuotableEmailException, MimeException {

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

	private Message buildSingleOrMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlOverText, TextBody modifiedBodyHtmlOverHtml ) 
			throws NotQuotableEmailException, MimeException {
		
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

	private Message buildSingleMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlPrefered) throws MimeException {
		if (modifiedBodyText != null) {
			return createMessageWithBody(Mime4jUtils.TYPE_TEXT_PLAIN, modifiedBodyText);
		} else {
			return createMessageWithBody(Mime4jUtils.TYPE_TEXT_HTML, modifiedBodyHtmlPrefered);
		}
	}

	private Message createMessageWithBody(String mimeType, TextBody modifiedBodyText) throws MimeException {
		MessageImpl newMessage = mime4jUtils.createMessage();

		boolean alreadyAttachmentsExist = outgoingMessageContainsAttachments();
		if (alreadyAttachmentsExist || originalMailAttachments.isEmpty()) {
			Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(opushConfiguration.getDefaultEncoding());
			newMessage.setBody(modifiedBodyText, mimeType, params);
		} else {
			Multipart mixedMultipart = this.mime4jUtils.createMultipartMixed();
			BodyPart bodyPart = this.mime4jUtils.bodyToBodyPart(modifiedBodyText, mimeType);
			mixedMultipart.addBodyPart(bodyPart);
			copyOriginalMessageAttachmentsToMultipartMessage(mixedMultipart);
			newMessage.setBody(mixedMultipart, Mime4jUtils.TYPE_MULTIPART_MIXED, 
					mime4jUtils.getContentTypeHeaderMultipartParams(opushConfiguration.getDefaultEncoding()));
		}
		return newMessage;
	}

	private Message createMultipartMessage(TextBody modifiedBodyText, TextBody modifiedBodyHtmlPrefered, boolean createMixed) 
			throws MimeException {
		
		final Multipart multipart = createMultipartMixedOrAlternative(createMixed);
		addBodyPart(multipart, modifiedBodyText, modifiedBodyHtmlPrefered);

		Multipart newMultipart = createNewMultipartWithAttachment(multipart);
		String mimeType = Mime4jUtils.TYPE_MULTIPART_PREFIX + newMultipart.getSubType();

		MessageImpl newMessage = mime4jUtils.createMessage();
		Map<String, String> params = mime4jUtils.getContentTypeHeaderMultipartParams(opushConfiguration.getDefaultEncoding());
		newMessage.setBody(newMultipart, mimeType, params);
		return newMessage;
	}

	private Multipart createNewMultipartWithAttachment(final Multipart multipart) throws MimeException {
		boolean alreadyAttachmentsExist = outgoingMessageContainsAttachments();
		if (alreadyAttachmentsExist || originalMailAttachments.isEmpty()) {
			return multipart;
		} else {
			if (multipart.getSubType().equals(Mime4jUtils.SUBTYPE_MULTIPART_MIXED)) {
				copyOriginalMessageAttachmentsToMultipartMessage(multipart);
				return multipart;
			} else {
				Map<String, String> contentTypeHeaderMultipartParams = 
						mime4jUtils.getContentTypeHeaderMultipartParams(opushConfiguration.getDefaultEncoding());
				
				Multipart mixedMultipart = this.mime4jUtils.createMultipartMixed();
				String mimeType = Mime4jUtils.TYPE_MULTIPART_PREFIX + multipart.getSubType();
				BodyPart bodyPart = this.mime4jUtils.bodyToBodyPart(multipart, mimeType, contentTypeHeaderMultipartParams);
				mixedMultipart.addBodyPart(bodyPart);
				copyOriginalMessageAttachmentsToMultipartMessage(mixedMultipart);
				return mixedMultipart;
			}
		}
	}

	private boolean outgoingMessageContainsAttachments() {
		return mime4jUtils.isAttachmentsExist(getMimeMessage());
	}

	private Multipart createMultipartMixedOrAlternative(boolean createMixed) {
		Multipart multipart;
		if (createMixed) {
			multipart = this.mime4jUtils.createMultipartMixed();
			copyOriginalMessagePartsToMultipartMessage(multipart);
		} else {
			multipart = this.mime4jUtils.createMultipartAlternative();
		}
		return multipart;
	}

	private void addBodyPart(Multipart multipart, TextBody modifiedBodyText, TextBody modifiedBodyHtmlPrefered) {
		if (modifiedBodyText != null) {
			multipart.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyText, Mime4jUtils.TYPE_TEXT_PLAIN));
		}
		if (modifiedBodyHtmlPrefered != null) {
			multipart.addBodyPart(this.mime4jUtils.bodyToBodyPart(modifiedBodyHtmlPrefered, Mime4jUtils.TYPE_TEXT_HTML));
		}
	}

	private void copyOriginalMessageAttachmentsToMultipartMessage(Multipart multipart) throws MimeException {
		for (Entry<String, MSAttachementData> att: originalMailAttachments.entrySet()) {
			MSAttachementData value = att.getValue();
			try {
				mime4jUtils.attach(multipart, value.getFile(), att.getKey(), value.getContentType());
			} catch (IOException e) {
				throw new MimeException(e);
			}
		}
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
		return encodeTxtLinesInHtml(Splitter.on('\n').split(originalText));
	}

	private String encodeTxtLinesInHtml(Iterable<String> originalTextLines) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String line: originalTextLines) {
			stringBuilder.append(StringEscapeUtils.escapeHtml(line)).append("<BR/>");
		}
		return stringBuilder.toString();
	}

	private TextBody appendQuotedMailToPlainText(TextBody plainTextPart, String repliedEmail) throws NotQuotableEmailException {
		try {	
			StringBuilder bodyTextPlainBuilder = new StringBuilder();
			bodyTextPlainBuilder.append(cleanLineBreaks(plainTextPart.getReader()));
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
		return LineTerminationCorrecter.correctLineTermination(content);
	}
	
	private Reader textToHtmlReader(Reader reader) throws IOException {
		return new StringReader(encodeTxtLinesInHtml(CharStreams.readLines(reader)));
	}

	private TextBody appendRepliedTextMailToHtml(TextBody htmlPart, String repliedEmail) throws NotQuotableEmailException {
		try {
			Reader textAsHtmlReader = textToHtmlReader(htmlPart.getReader());
			return appendRepliedMailToHtml(textAsHtmlReader, Charset.forName(htmlPart.getMimeCharset()), repliedEmail);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Html part isn't quotable", e);
		}
	}

	private TextBody appendRepliedMailToHtml(TextBody htmlPart, String repliedEmail) throws NotQuotableEmailException {
		try {
			return appendRepliedMailToHtml(htmlPart.getReader(), Charset.forName(htmlPart.getMimeCharset()), repliedEmail);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NotQuotableEmailException("Html part isn't quotable", e);
		}
	}

	private TextBody appendRepliedMailToHtml(Reader partReader, Charset partCharset, String repliedEmail) throws NotQuotableEmailException {
		try {
			final InputSource replySource = new InputSource(partReader);
			final InputSource originalSource = new InputSource(new StringReader(repliedEmail));

			final Document replyHtmlDoc = DOMUtils.parseHtmlAsDocument(replySource);
			final Node originalHtmlNode = DOMUtils.parseHtmlAsFragment(originalSource);

			final Element quoteBlock = insertIntoQuoteblock(replyHtmlDoc, originalHtmlNode);
			final Element bodyNode = DOMUtils.getUniqueElement(replyHtmlDoc.getDocumentElement(), "BODY");
			bodyNode.appendChild(quoteBlock);

			StringReader replyHtmlDocReader = new StringReader(DOMUtils.serializeHtmlDocument(replyHtmlDoc));

			BasicBodyFactory basicBodyFactory = new BasicBodyFactory();
			return basicBodyFactory.textBody(cleanLineBreaks(replyHtmlDocReader), partCharset);
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
