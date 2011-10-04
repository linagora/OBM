package org.obm.push.mail;

import java.io.InputStream;

import org.minig.imap.StoreClient;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.obm.mail.conversation.MailMessage;
import org.obm.mail.message.AbstractMessageFetcherImpl;

public class MessageFetcherImpl extends AbstractMessageFetcherImpl {

	private final StoreClient storeClient;

	public MessageFetcherImpl(StoreClient storeClient) {
		super();
		this.storeClient = storeClient;
	}
	
	@Override
	protected InputStream uidFetchPart(MimeMessage message, String part) {
		return storeClient.uidFetchPart(message.getUid(), part);
	}

	@Override
	protected InputStream uidFetchPart(MimeMessage message, IMimePart mimePart) {
		return storeClient.uidFetchPart(message.getUid(), mimePart.getAddress().toString());
	}

	@Override
	protected InputStream uidFetchPart(MailMessage message, IMimePart mimePart) {
		return storeClient.uidFetchPart(message.getUid(), mimePart.getAddress().toString());
	}

}
