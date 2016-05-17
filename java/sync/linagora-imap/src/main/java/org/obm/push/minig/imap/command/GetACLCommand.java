package org.obm.push.minig.imap.command;

import java.util.Set;

import org.obm.push.exception.InvalidIMAPResponseException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class GetACLCommand extends SimpleCommand<Set<Acl>> {
	
	private final static String IMAP_COMMAND = "GETACL";
	private final static int INDEXOFFIRSTPAIR = 0;

	private final String mailbox; 

	public GetACLCommand(String mailbox) {
		super(IMAP_COMMAND + " " + toUtf7(mailbox));
		this.mailbox = mailbox;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String imapResponse = response.getPayload();
		Optional<String> rights = rights(imapResponse);
		if (Strings.isNullOrEmpty(imapResponse) || !rights.isPresent()) {
			return;
		}

		String[] responses = rights.get().split(" ");
		if (responses.length % 2 != 0) {
			throw new InvalidIMAPResponseException("Invalid imap response syntax");
		}
		for (int i = INDEXOFFIRSTPAIR; i < responses.length-1; i += 2) {
			data.add(
				Acl.builder().user(responses[i]).rights(responses[i+1]).build());
		}
	}

	private Optional<String> rights(String imapResponse) {
		int indexOfRights = imapResponse.indexOf(mailbox) + mailbox.length() + 1;
		if (imapResponse.length() <= indexOfRights) {
			return Optional.absent();
		}
		String rights = imapResponse.substring(indexOfRights);
		if (rights.startsWith(" ")) {
			return Optional.of(rights.substring(1));
		}
		return Optional.of(rights);
	}

	@Override
	public void setDataInitialValue() {
		data = Sets.newHashSet();
	}
}
