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
package org.obm.push.mail.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.conversation.MailBody;
import org.obm.push.mail.conversation.MailMessage;
import org.obm.push.mail.imap.StoreException;
import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.mime.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.CharStreams;


	
public class MessageLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(MessageLoader.class);
	
	private static final BodyParam formatFlowed = new BodyParam("format", "flowed");
	private final MailboxService mailboxService;
	private final List<String> mimeSubtypeInPriorityOrder;
	private final boolean bodyOnly;
	private final MimeMessage message;
	private final UserDataRequest udr;
	private int nbAttachments;
	private MailMessage rootMailMessage;

	private final String collectionPath;

	
	public MessageLoader(UserDataRequest udr, MailboxService mailboxService, List<String> mimeSubtypeInPriorityOrder, 
			boolean bodyOnly, MimeMessage message, String collectionPath) {
		super();
		this.mailboxService = mailboxService;
		this.bodyOnly = bodyOnly;
		this.message = message;
		this.collectionPath = collectionPath;
		this.nbAttachments = 0;
		this.mimeSubtypeInPriorityOrder = mimeSubtypeInPriorityOrder;
		this.udr = udr;
	}

	public MailMessage fetch() throws IOException, StoreException {
		if (rootMailMessage == null) {
			MailMessage mm = fetchOneMessage(message);

			// do load messages forwarded as attachments into the indexers, as it
			// ignores them
			if (!bodyOnly) {
				fetchQuotedText(message, mm);
				fetchForwardMessages(message, mm);
			}
			rootMailMessage = mm;
		}
		return rootMailMessage;
	}

	private MailMessage fetchOneMessage(MimePart mimePart)
			throws IOException {
		
		MailMessage mm = extractMailMessage(mimePart);
		long messageUid = message.getUid();
		IMAPHeaders h = mailboxService.fetchPartHeaders(udr, collectionPath, MessageSet.singleton(messageUid), mimePart).get(messageUid);
		copyHeaders(h, mm);
		mm.setUid(messageUid);

		return mm;
	}

	private void copyHeaders(IMAPHeaders h, MailMessage mm) {
		mm.setSender(h.getFrom());
		mm.setDate(Objects.firstNonNull(h.getDate(), new Date()));
		mm.setSubject(h.getSubject());
		mm.setHeaders(h.getRawHeaders());
		mm.setCc(h.getCc());
		mm.setTo(h.getTo());
		mm.setBcc(h.getBcc());
		mm.setSmtpId(h.getRawHeader("Message-ID"));
	}

	private MailMessage extractMailMessage(MimePart mimePart) throws IOException {
		MailMessage mailMessage = new MailMessage();
		MimePart chosenPart = new BodySelector(mimePart,
				mimeSubtypeInPriorityOrder).findBodyTextPart();
		MailBody mailBody = null;
		if (chosenPart == null) {
			chosenPart = mimePart;
			mailBody = new MailBody();
			mailBody.addConverted("text/plain", "");
		} else {
			mailBody = getMailBody(chosenPart);
		}
		List<MailMessageAttachment> attach = extractAttachments(chosenPart);
		mailMessage.setBody(mailBody);
		mailMessage.setAttachments(attach);
		mailMessage.setInvitation(chosenPart.getInvitation());
		return mailMessage;
	}

	private List<MailMessageAttachment> extractAttachments(MimePart mimePart) {
		List<MailMessageAttachment> attachments = new ArrayList<MailMessageAttachment>();
		MimePart parentMessage = mimePart.findRootMimePartInTree();
		for (MimePart mp : parentMessage.listLeaves(true, true)) {
			if (mp.isAttachment()) {
				MailMessageAttachment mailMessageAttachment = extractMailMessageAttachment(mp);
				if (mailMessageAttachment != null) {
					attachments.add(mailMessageAttachment);
				}
			} else {
				if (mp.isInvitation() || mp.isCancelInvitation()) {
					MailMessageAttachment mailMessageAttachment =  new MailMessageAttachment(mp.getContentId(), mp.getName(), mp);
					attachments.add(mailMessageAttachment);
				} 	
			}
		}
		return attachments;
	}
	
	private void fetchQuotedText(MimePart message, MailMessage mailMessage) 
		throws IOException {
		for (MimePart part: message.getChildren()) {
			fetchFlowed(mailMessage, part);
		}
	}

	private void fetchFlowed(MailMessage mailMessage, MimePart part)
			throws IOException {
		if (formatFlowed.equals(part.getBodyParam("format"))) {
			MailMessage mm = fetchOneMessage(part);
			if (!mailMessage.getBody().equals(mm.getBody())) {
				for (String format : mm.getBody().availableFormats()) {
					String mailPart = mm.getBody().getValue(format);
					if (mailPart != null) {
						mailMessage.getBody().addMailPart(format, mailPart);
					}
				}
			}
		}
	}

	private void fetchForwardMessages(MimePart t, MailMessage mailMessage) 
		throws IOException,	StoreException {

		for (MimePart part: t.getChildren()) {
			fetchNested(mailMessage, part);
			fetchMultipart(mailMessage, part);
		}
	}

	private void fetchMultipart(MailMessage mailMessage, MimePart part)
			throws IOException, StoreException {

		if (part.isMultipart()) {
			for (MimePart mp: part.getChildren()) {
				fetchNested(mailMessage, mp);
			}
		}
	}

	private void fetchNested(MailMessage mailMessage, MimePart m)
			throws IOException, StoreException {
		
		if (m.isNested()) {
			MailMessage mm = fetchOneMessage(m);
			fetchQuotedText(m, mm);
			fetchForwardMessages(m, mm);
			mailMessage.addForwardMessage(mm);
			fetchForwardMessages(m, mm);
		}
	}
	
	private MailBody getMailBody(MimePart chosenPart) throws IOException {
		MailBody mb = new MailBody();
		InputStream bodyText = 
				chosenPart.decodeMimeStream(
						mailboxService.fetchMimePartStream(udr, collectionPath, message.getUid(), chosenPart.getAddress()));
		Charset charsetName = computeSupportedCharset(chosenPart.getCharset());
		String partText = CharStreams.toString(new InputStreamReader(bodyText, charsetName));
		mb.addConverted(chosenPart.getFullMimeType(), partText);
		logFullTextBody(chosenPart, partText);
		return mb;
	}

	private void logFullTextBody(MimePart chosenPart, String partText) {
		if (logger.isDebugEnabled()) {
			logger.debug("Added part " + chosenPart.getFullMimeType() + "\n" + partText + "\n------");
		}
	}
	
	private Charset computeSupportedCharset(String charset) {
		if (charset != null) {
			try {
				
				if (Charset.isSupported(charset)) {
					return Charset.forName(charset);
				}
			} catch (IllegalCharsetNameException e) {
			} catch (IllegalArgumentException e) {
			}
		}
		return Charsets.UTF_8;
	}
	
	private MailMessageAttachment extractMailMessageAttachment(MimePart mp) {
		String id = "at_" + message.getUid() + "_" + (nbAttachments++);
		String partName = mp.getName();
		if (partName != null) {
			return new MailMessageAttachment(id, partName, mp);
		}
		String contentId = mp.getContentId();
		if (contentId != null) {
			return new MailMessageAttachment(id, contentId, mp);
		}
		return null;
	}
}
