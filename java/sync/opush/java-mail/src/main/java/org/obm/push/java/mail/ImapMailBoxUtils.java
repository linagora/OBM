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
package org.obm.push.java.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Flags;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;

import org.obm.push.mail.MailException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPMultipartDataSource;
import com.sun.mail.imap.IMAPNestedMessage;

@Singleton
public class ImapMailBoxUtils {

	private static final ImmutableMap<Flags.Flag, String> flags = 
			ImmutableMap.<Flags.Flag, String>builder().
			put(Flags.Flag.ANSWERED, "ANSWERED").
			put(Flags.Flag.DELETED, "DELETED").
			put(Flags.Flag.DRAFT, "DRAFT").
			put(Flags.Flag.FLAGGED, "FLAGGED").
			put(Flags.Flag.RECENT, "RECENT").
			put(Flags.Flag.SEEN, "SEEN").
			put(Flags.Flag.USER, "USER").build();
	
	public String flagToString(Flags.Flag flag) {
		if (flags.containsKey(flag)) {
			return "Flag." + flags.get(flag);
		} else {
			throw new IllegalArgumentException("Flag not found !");
		}
	}
	
	public List<Email> orderEmailByUid(Collection<Email> emails) {
		ArrayList<Email> listOfEmails = Lists.newArrayList(emails);
		Collections.sort(listOfEmails, new Comparator<Email>() {
			@Override
			public int compare(Email o1, Email o2) {
				return (int) (o1.getUid() - o2.getUid());
			}
		});
		return listOfEmails;
	}
	
	public Envelope buildEnvelopeFromMessage(IMAPMessage message) throws MailException {
		try {
			Date sentDate = message.getSentDate();
			String subject = message.getSubject();
			String messageID = message.getMessageID();
			List<Address> to = buildAddressListFromJavaMailAddress(message.getRecipients(RecipientType.TO));
			List<Address> cc = buildAddressListFromJavaMailAddress(message.getRecipients(RecipientType.CC));
			List<Address> bcc = buildAddressListFromJavaMailAddress(message.getRecipients(RecipientType.BCC));
			List<Address> from = buildAddressListFromJavaMailAddress(message.getFrom());
			List<Address> replyTo = buildAddressListFromJavaMailAddress(message.getReplyTo());
			
			return Envelope.builder().
					date(sentDate).subject(subject).to(to).cc(cc).bcc(bcc).from(from).
					messageID(messageID).replyTo(replyTo).build();
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
	
	private List<Address> buildAddressListFromJavaMailAddress(javax.mail.Address[] addresses) {
		List<Address> buildAddresses = new ArrayList<Address>();
		if (addresses != null) {
			for (javax.mail.Address address: addresses) {
				buildAddresses.add( buildAddressFromJavaMailAddress(address) );
			}
		}
		return buildAddresses;
	}
	
	private Address buildAddressFromJavaMailAddress(javax.mail.Address address) {
		if (address instanceof InternetAddress) {
			InternetAddress internetAddress = (InternetAddress) address;
			return new Address(internetAddress.getPersonal(), internetAddress.getAddress());
		} else {
			return new Address(address.toString());
		}
	}

	public Collection<FastFetch> buildFastFetchFromIMAPMessage(Map<Long, IMAPMessage> imapMessages) throws MailException {
		Collection<FastFetch> fastFetchCollection = new ArrayList<FastFetch>();
		for (Entry<Long, IMAPMessage> entry: imapMessages.entrySet()) {
			addIfNotDeletedEmail(fastFetchCollection, entry);
		}
		return fastFetchCollection;
	}

	private void addIfNotDeletedEmail(Collection<FastFetch> fastFetchCollection, Entry<Long, IMAPMessage> entry) {
		try {
			if (!entry.getValue().getFlags().contains(Flags.Flag.DELETED)) {
				FastFetch fastFetch = buildFastFetch(entry.getKey(), entry.getValue());
				fastFetchCollection.add(fastFetch);
			}
		} catch (MessagingException ex) {
			throw new MailException(ex);
		}
	}

	private FastFetch buildFastFetch(long uid, IMAPMessage imapMessage) throws MessagingException {
		Set<Flag> buildFlagToIMAPMessageFlags = buildFlagToIMAPMessageFlags(imapMessage.getFlags());
		Date receivedDate = imapMessage.getReceivedDate();
		int size = imapMessage.getSize();
		
		FastFetch.Builder builder = FastFetch.builder();
		builder.uid(uid).internalDate(receivedDate).flags(buildFlagToIMAPMessageFlags).size(size);
		return builder.build();
	}

	public Set<Flag> buildFlagToIMAPMessageFlags(Flags imapMessageFlags) {
		Set<Flag> flags = new HashSet<Flag>();
		for (javax.mail.Flags.Flag javaMailFlag: imapMessageFlags.getSystemFlags()) {
			Flag flag = Flag.toFlag( ImapMailBoxUtils.flags.get(javaMailFlag) );
			if (flag != null) {
				flags.add(flag);
			}
		}
		return flags;
	}

	public Collection<MimeMessage> buildMimeMessageCollectionFromIMAPMessage(Map<Long, IMAPMessage> imapMessages) 
			throws MailException {
		
		Collection<MimeMessage> mimeMessageCollection = new ArrayList<MimeMessage>();
		for (Entry<Long, IMAPMessage> entry: imapMessages.entrySet()) {
			mimeMessageCollection.add(buildMimeMessageFromIMAPMessage(entry.getKey(), entry.getValue()));
		}
		return mimeMessageCollection;
	}

	public MimeMessage buildMimeMessageFromIMAPMessage(long uid, IMAPMessage message) {
		MimePart mimePart = buildMimePartTree(message);
		return MimeMessage.builder().from(mimePart).uid(uid).build();
	}

	private MimePart buildMimePartTree(javax.mail.internet.MimePart mimePart) throws MailException {
		try {
			DataSource dataSource = getDataSource(mimePart);
			if (dataSource instanceof IMAPMultipartDataSource) {
				return buildMimePartFromIMAPMultipartDataSource(mimePart);
			} else if (dataSource instanceof MimePartDataSource) {
				return buildMimePart(mimePart);
			} else {
				return buildMimePartFromIMAPNestedMessage(mimePart);
			}
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}

	private DataSource getDataSource(Part part) throws MessagingException {
		DataHandler dataHandler = part.getDataHandler();
		return dataHandler.getDataSource();
	}

	private MimePart buildMimePartFromIMAPMultipartDataSource(javax.mail.internet.MimePart mimePart) 
			throws MessagingException, MailException {
		
		IMAPMultipartDataSource imapMultipartDataSource = (IMAPMultipartDataSource) getDataSource(mimePart);
		MimePart parentMimePart = mimePartBuilder(mimePart).addChildren(childMimePart(imapMultipartDataSource)).build();
		return parentMimePart;
	}

	private List<MimePart> childMimePart(IMAPMultipartDataSource imapMultipartDataSource) 
			throws MessagingException, MailException {
		
		int countChild = imapMultipartDataSource.getCount();
		List<MimePart> parts = Lists.newArrayList();
		for (int cpt = 0; cpt < countChild; cpt++) {
			MimeBodyPart mimeBodyPart = (MimeBodyPart) imapMultipartDataSource.getBodyPart(cpt);
			parts.add(buildMimePartTree(mimeBodyPart) );
		}
		return parts;
	}

	private MimePart.Builder mimePartBuilder(javax.mail.internet.MimePart mimePart) throws MessagingException {
		return MimePart.builder()
			.contentType(mimePart.getContentType())
			.contentId(mimePart.getContentID())
			.encoding(mimePart.getEncoding())
			.size(mimePart.getSize());
	}
	
	private MimePart buildMimePart(javax.mail.internet.MimePart mimePart) throws MessagingException {
		return mimePartBuilder(mimePart).build();
	}
	
	private MimePart buildMimePartFromIMAPNestedMessage(javax.mail.internet.MimePart mimePart) 
			throws MessagingException, MailException {
		
		try {
			Object content = mimePart.getContent();
			if (content instanceof IMAPNestedMessage) {
				return buildMimePart((IMAPNestedMessage) content);
			} else {
				throw new MailException("Unknown MimePart type.");
			}
		} catch (IOException e) {
			throw new MailException(e);
		}
	}
}