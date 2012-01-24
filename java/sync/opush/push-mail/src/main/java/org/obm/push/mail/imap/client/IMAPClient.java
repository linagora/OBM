package org.obm.push.mail.imap.client;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.sun.mail.imap.IMAPFolder;

public interface IMAPClient {

	void appendMessage(IMAPFolder folder, Message msg) throws MessagingException;

}
