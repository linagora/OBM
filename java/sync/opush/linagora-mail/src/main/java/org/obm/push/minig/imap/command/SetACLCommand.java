package org.obm.push.minig.imap.command;

import org.obm.push.minig.imap.impl.IMAPResponse;

public class SetACLCommand extends SimpleCommand<Boolean> {
	
	private final static String IMAP_COMMAND = "SETACL";
	

	public SetACLCommand(String mailbox, String identifier, String accessRights) {
		super(
			String.format("%s %s %s %s",
					IMAP_COMMAND,
					toUtf7(mailbox),
					toUtf7(identifier),
					toUtf7(accessRights)));
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
