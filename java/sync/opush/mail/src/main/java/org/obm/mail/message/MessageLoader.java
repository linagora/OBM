package org.obm.mail.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;

import org.minig.imap.IMAPHeaders;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.obm.mail.conversation.MailBody;
import org.obm.mail.conversation.MailMessage;
import org.obm.mail.imap.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


	
public class MessageLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(MessageLoader.class);
	
	private static final BodyParam formatFlowed = new BodyParam("format", "flowed");
	private final MessageFetcher messageFetcher;
	private final List<String> mimeSubtypeInPriorityOrder;
	private final boolean bodyOnly;
	private final MimeMessage message;
	private int nbAttachments;
	private MailMessage rootMailMessage;
	
	public MessageLoader(MessageFetcher messageFetcher, List<String> mimeSubtypeInPriorityOrder, boolean bodyOnly, MimeMessage message) {
		super();
		this.messageFetcher = messageFetcher;
		this.bodyOnly = bodyOnly;
		this.message = message;
		this.nbAttachments = 0;
		this.mimeSubtypeInPriorityOrder = mimeSubtypeInPriorityOrder;
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

	private MailMessage fetchOneMessage(IMimePart mimePart)
			throws IOException, StoreException {
		
		MailMessage mm = extractMailMessage(mimePart);
		IMAPHeaders h = messageFetcher.fetchPartHeaders(message, mimePart);
		copyHeaders(h, mm);
		mm.setUid(message.getUid());

		return mm;
	}

	private void copyHeaders(IMAPHeaders h, MailMessage mm) {
		mm.setSender(h.getFrom());
		mm.setDate(h.getDate());
		mm.setSubject(h.getSubject());
		mm.setHeaders(h.getRawHeaders());
		mm.setCc(h.getCc());
		mm.setTo(h.getTo());
		mm.setBcc(h.getBcc());
		mm.setSmtpId(h.getRawHeader("Message-ID"));
	}

	private MailMessage extractMailMessage(IMimePart mimePart) throws IOException,
			StoreException {
		MailMessage mailMessage = new MailMessage();
		IMimePart chosenPart = new BodySelector(mimePart,
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
		MailMessageInvitation invitation = extractInvitation(chosenPart);
		mailMessage.setBody(mailBody);
		mailMessage.setAttachments(attach);
		mailMessage.setInvitation(invitation);
		return mailMessage;
	}

	private MailMessageInvitation extractInvitation(IMimePart mimePart) {
		IMimePart parentMessage = findRootMessage(mimePart);
		for (IMimePart mp : parentMessage.listLeaves(true, true)) {
			if (mp.isInvitation() || mp.isCancelInvitation()) {
				return new MailMessageInvitation(mp.getContentId(), mp);
			} 	
		}
		return null;
	}

	private IMimePart findRootMessage(IMimePart mimePart) {
		IMimePart current = mimePart;
		while (true) {
			if (current == null) {
				return message;
			}
			if (current.isNested()) {
				return current;
			}
			current = current.getParent();
		}
	}
	
	private List<MailMessageAttachment> extractAttachments(IMimePart mimePart) {
		List<MailMessageAttachment> attachments = new ArrayList<MailMessageAttachment>();
		IMimePart parentMessage = findRootMessage(mimePart);
		for (IMimePart mp : parentMessage.listLeaves(true, true)) {
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
	
	private void fetchQuotedText(IMimePart message, MailMessage mailMessage) 
		throws IOException,	StoreException {
		for (IMimePart part: message.getChildren()) {
			fetchFlowed(mailMessage, part);
		}
	}

	private void fetchFlowed(MailMessage mailMessage, IMimePart part)
			throws IOException, StoreException {
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

	private void fetchForwardMessages(IMimePart t, MailMessage mailMessage) 
		throws IOException,	StoreException {

		for (IMimePart part: t.getChildren()) {
			fetchNested(mailMessage, part);
			fetchMultipart(mailMessage, part);
		}
	}

	private void fetchMultipart(MailMessage mailMessage, IMimePart part)
			throws IOException, StoreException {

		if (part.isMultipart()) {
			for (IMimePart mp: part.getChildren()) {
				fetchNested(mailMessage, mp);
			}
		}
	}

	private void fetchNested(MailMessage mailMessage, IMimePart m)
			throws IOException, StoreException {
		
		if (m.isNested()) {
			MailMessage mm = fetchOneMessage(m);
			fetchQuotedText(m, mm);
			fetchForwardMessages(m, mm);
			mailMessage.addForwardMessage(mm);
			fetchForwardMessages(m, mm);
		}
	}
	
	private MailBody getMailBody(IMimePart chosenPart) throws IOException, StoreException {
		MailBody mb = new MailBody();
		InputStream bodyText = messageFetcher.fetchPart(message, chosenPart);
		Charset charsetName = computeSupportedCharset(chosenPart.getCharset());
		String partText = CharStreams.toString(new InputStreamReader(bodyText, charsetName));
		mb.addConverted(chosenPart.getFullMimeType(), partText);
		logFullTextBody(chosenPart, partText);
		return mb;
	}

	private void logFullTextBody(IMimePart chosenPart, String partText) {
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
	
	private MailMessageAttachment extractMailMessageAttachment(IMimePart mp) {
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
