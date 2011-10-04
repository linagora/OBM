package org.obm.mail.message;

import java.io.IOException;
import java.io.InputStream;

import org.minig.imap.IMAPHeaders;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.obm.mail.conversation.MailMessage;
import org.obm.mail.imap.StoreException;


public interface MessageFetcher {

	IMAPHeaders fetchPartHeaders(MimeMessage message, IMimePart mimePart) throws IOException, StoreException;

	InputStream fetchPart(MimeMessage message, IMimePart mimePart) throws IOException, StoreException;

	InputStream fetchPart(MailMessage message, IMimePart part) throws IOException, StoreException;

}
