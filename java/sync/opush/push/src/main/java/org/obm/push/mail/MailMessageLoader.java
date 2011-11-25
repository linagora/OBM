/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.StoreClient;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.minig.mime.QuotedPrintableDecoderInputStream;
import org.obm.mail.conversation.MailBody;
import org.obm.mail.conversation.MailMessage;
import org.obm.mail.conversation.MessageId;
import org.obm.mail.imap.StoreException;
import org.obm.mail.message.MailMessageAttachment;
import org.obm.mail.message.MailMessageInvitation;
import org.obm.mail.message.MessageLoader;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MessageClass;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.service.EventService;
import org.obm.push.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Creates a {@link MailMessage} from a {@link MessageId}.
 */
public class MailMessageLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(MailMessageLoader.class);
	
	private final AbstractEventSyncClient calendarClient;
	private final List<String> htmlMimeSubtypePriority;
	private final StoreClient storeClient;
	private final EventService eventService;
	
	public MailMessageLoader(final StoreClient store, final AbstractEventSyncClient calendarClient, EventService eventService) {
		this.storeClient = store;
		this.calendarClient = calendarClient;
		this.eventService = eventService;
		this.htmlMimeSubtypePriority = Arrays.asList("html", "plain", "calendar");
	}

	public MSEmail fetch(final Integer collectionId, final long messageId, final BackendSession bs) {
		MSEmail msEmail = null;
		try {
			
			final List<Long> messageIdAsList = Arrays.asList(messageId);
			final Collection<Envelope> envelopes = storeClient.uidFetchEnvelope(messageIdAsList);
			if (envelopes.size() != 1 || envelopes.iterator().next() == null) {
				return null;
			}
			
			final MimeMessage mimeMessage = getFirstMimeMessage(messageIdAsList);
			if (mimeMessage != null) {
				final MessageFetcherImpl messageFetcherImpl = new MessageFetcherImpl(storeClient);
				final MessageLoader helper = new MessageLoader(messageFetcherImpl, htmlMimeSubtypePriority, false, mimeMessage);
				final MailMessage message = helper.fetch();
				
				msEmail = convertMailMessageToMSEmail(message, bs, mimeMessage.getUid(), collectionId, messageId);
				setMsEmailFlags(msEmail, messageIdAsList);
				fetchMimeData(msEmail, messageId);
				msEmail.setSmtpId(envelopes.iterator().next().getMessageId());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (StoreException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return msEmail;
	}

	private MimeMessage getFirstMimeMessage(final List<Long> messageIdAsList) {
		final Collection<MimeMessage> mts = storeClient.uidFetchBodyStructure(messageIdAsList);
		final MimeMessage tree = Iterables.getFirst(mts, null);
		return tree;
	}
	
	private void setMsEmailFlags(final MSEmail msEmail, final List<Long> messageIdAsList) {
		final Collection<FlagsList> fl = storeClient.uidFetchFlags(messageIdAsList);
		if (!fl.isEmpty()) {
			final FlagsList fl0 = fl.iterator().next();
			msEmail.setRead(fl0.contains(Flag.SEEN));
			msEmail.setStarred(fl0.contains(Flag.FLAGGED));
			msEmail.setAnswered(fl0.contains(Flag.ANSWERED));
		}
	}
	
	private void fetchMimeData(final MSEmail mm, final long messageId) {
		try {
			final InputStream mimeData = storeClient.uidFetchMessage(messageId);

			final SendEmailHandler handler = new SendEmailHandler("");
			final MimeEntityConfig config = new MimeEntityConfig();
			config.setMaxContentLen(Integer.MAX_VALUE);
			config.setMaxLineLen(Integer.MAX_VALUE);
			final MimeStreamParser parser = new MimeStreamParser(config);
			parser.setContentHandler(handler);
			parser.parse(mimeData);

			mm.setMimeData(handler.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (MimeException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private MSEmail convertMailMessageToMSEmail(final MailMessage mailMessage, final BackendSession bs, 
			final long uid, final Integer collectionId, long messageId) {
		
		final MSEmail msEmail = new MSEmail();
		msEmail.setSubject(mailMessage.getSubject());
		msEmail.setBody(convertMailBodyToMSEmailBody(mailMessage.getBody()));
		msEmail.setFrom(convertAdressToMSAddress(mailMessage.getSender()));
		msEmail.setDate(mailMessage.getDate());
		msEmail.setHeaders(mailMessage.getHeaders());
		msEmail.setForwardMessage(convertAllMailMessageToMSEmail(mailMessage.getForwardMessage(), bs, uid, collectionId, messageId));
		msEmail.setAttachements(convertMailMessageAttachmentToMSAttachment(mailMessage, uid, collectionId, messageId));	
		msEmail.setUid(mailMessage.getUid());
		
		msEmail.setTo(convertAllAdressToMSAddress(mailMessage.getTo()));
		msEmail.setBcc(convertAllAdressToMSAddress(mailMessage.getBcc()));
		msEmail.setCc(convertAllAdressToMSAddress(mailMessage.getCc()));
		
		if (this.calendarClient != null && mailMessage.getInvitation() != null) {
			setInvitation(msEmail, bs, mailMessage.getInvitation(), uid, messageId);
		}
		
		return msEmail;
	}

	private void setInvitation(final MSEmail msEmail, final BackendSession bs, final MailMessageInvitation mailMessageInvitation, 
			final long uid, final long messageId) {			
		final IMimePart mimePart = mailMessageInvitation.getPart();
		try {	
			final InputStream inputStreamInvitation = extractInputStreamInvitation(mimePart, uid, messageId);
			final MSEvent event = getInvitation(bs, inputStreamInvitation);
			if (mimePart.isInvitation()) {
				msEmail.setInvitation(event, MessageClass.ScheduleMeetingRequest);
			} else if (mimePart.isCancelInvitation()) {
				msEmail.setInvitation(event, MessageClass.ScheduleMeetingCanceled);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private InputStream extractInputStreamInvitation(final IMimePart mp, final long uid, final long messageId) throws IOException {
		byte[] data = null;
		final InputStream part = storeClient.uidFetchPart(uid, mp.getAddress().toString());
		data = extractPartData(mp, part, messageId);
		if (data != null) {
			return new ByteArrayInputStream(data);
		}
		return null;
	}
	
	private MSEvent getInvitation(BackendSession bs, InputStream invitation) throws IOException {
		final String ics = FileUtils.streamString(invitation, true);
		if (ics != null && !"".equals(ics) && ics.startsWith("BEGIN")) {
			final AccessToken at = calendarClient.login(bs.getLoginAtDomain(),
					bs.getPassword(), ObmSyncBackend.OBM_SYNC_ORIGIN);
			try {
				final List<Event> obmEvents = calendarClient.parseICS(at, ics);
				if (obmEvents.size() > 0) {
					final Event icsEvent = obmEvents.get(0);
					return eventService.convertEventToMSEvent(bs, icsEvent);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage() + ", ics was:\n" + ics, e);
			} finally {
				calendarClient.logout(at);
			}
		}
		return null;
	}

	private Set<MSAttachement> convertMailMessageAttachmentToMSAttachment(final MailMessage mailMessage, 
			final long uid, final Integer collectionId, final long messageId) {
		final Set<MSAttachement> msAttachements = new HashSet<MSAttachement>();
		for (MailMessageAttachment mailMessageAttachment: mailMessage.getAttachments()) {			
			if (!mailMessageAttachment.getPart().isInvitation()) {
				final MSAttachement extractAttachments = extractAttachmentData(mailMessageAttachment.getPart(), uid, collectionId, messageId);
				if (extractAttachments != null) {
					msAttachements.add(extractAttachments);	
				}	
			}
		}
		return msAttachements;
	}
	
	private Set<MSEmail> convertAllMailMessageToMSEmail(final Set<MailMessage> set, final BackendSession bs, 
			final long uid, final Integer collectionId, final long messageId) {
		final Set<MSEmail> msEmails = new HashSet<MSEmail>();
		for (final MailMessage mailMessage: set) {
			msEmails.add(convertMailMessageToMSEmail(mailMessage, bs, uid, collectionId, messageId));
		}
		return msEmails;
	}

	private MSAddress convertAdressToMSAddress(Address adress) {
		if (adress != null) {
			return new MSAddress(adress.getDisplayName(), adress.getMail());
		}
		return null;
	}
	
	private List<MSAddress> convertAllAdressToMSAddress(List<Address> adresses) {
		List<MSAddress> msAdresses = new ArrayList<MSAddress>();
		if (adresses != null) {
			for (Address adress: adresses) {
				msAdresses.add(convertAdressToMSAddress(adress));
			}
		}
		return msAdresses;
	}

	private MSEmailBody convertMailBodyToMSEmailBody(final MailBody body) {
		final MSEmailBody emailBody = new MSEmailBody();
		for (final String format: body.availableFormats()) {
			final String value = body.getValue(format);
			emailBody.addConverted(MSEmailBodyType.getValueOf(format), value);
		}		
		return emailBody;
	}
	
	private byte[] extractPartData(final IMimePart mp, final InputStream bodyText, final long messageId) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(bodyText, out, true);
		byte[] rawData = out.toByteArray();
		if (logger.isDebugEnabled()) {
			logger.debug("[" + messageId + "] transfer encoding for part: "
					+ mp.getContentTransfertEncoding() + " "
					+ mp.getFullMimeType());
		}
		if ("QUOTED-PRINTABLE".equals(mp.getContentTransfertEncoding())) {
			out = new ByteArrayOutputStream();
			InputStream in = new QuotedPrintableDecoderInputStream(
					new ByteArrayInputStream(rawData));
			FileUtils.transfer(in, out, true);
			rawData = out.toByteArray();
		} else if ("BASE64".equals(mp.getContentTransfertEncoding())) {
			rawData = new Base64().decode(rawData);
		}
		return rawData;
	}
	
	private MSAttachement extractAttachmentData(final IMimePart mp, final long uid, 
			final Integer collectionId, final long messageId) {
		try {
			
			if (mp.getName() != null || mp.getContentId() != null) {
				byte[] data = null;
				final InputStream part = storeClient.uidFetchPart(uid, mp.getAddress().toString());
				data = extractPartData(mp, part, messageId);
				
				final String id = AttachmentHelper.getAttachmentId(collectionId.toString(), String.valueOf(messageId), 
						mp.getAddress().toString(), mp.getFullMimeType(), mp.getContentTransfertEncoding());
				
				String name = mp.getName();
				if (name == null) {
					name = mp.getContentId();
				}
				
				MSAttachement att = new MSAttachement();
				att.setFileReference(id);
				att.setMethod(MethodAttachment.NormalAttachment);
				att.setEstimatedDataSize(data.length);
				att.setDisplayName(name);
				return att;
			}			
			
		} catch (Exception e) {
			logger.error("Error extract attachment["+uid+"]");
		}
		return null;
	}
	
}
