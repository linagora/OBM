package org.obm.push.minig.imap.command;

import org.obm.push.minig.imap.impl.IMAPResponse;

public class GetACLCommand extends SimpleCommand<Boolean> {
	
	private final static String IMAP_COMMAND = "GETACL";

	public GetACLCommand(String mailbox) {
		super(IMAP_COMMAND + " " + toUtf7(mailbox));
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		data = response.isOk();
	}

}
