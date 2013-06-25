package org.obm.push.minig.imap.command;

import java.util.Set;

import org.obm.push.exception.InvalidIMAPResponseException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.Sets;

public class GetACLCommand extends SimpleCommand<Set<Acl>> {
	
	private final static String IMAP_COMMAND = "GETACL";
	private final static int INDEXOFFIRSTPAIR = 3; 

	public GetACLCommand(String mailbox) {
		super(IMAP_COMMAND + " " + toUtf7(mailbox));
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String[] responses = response.getPayload().split(" ");
		if (responses.length % 2 == 0) {
			throw new InvalidIMAPResponseException("Invalid imap response syntax");
		}
		for (int i = INDEXOFFIRSTPAIR; i < responses.length-1; i += 2) {
			data.add(
				Acl.builder().user(responses[i]).rights(responses[i+1]).build());
		}
	}

	@Override
	public void setDataInitialValue() {
		data = Sets.newHashSet();
	}
}
