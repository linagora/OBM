package org.obm.push.minig.imap.command;

import java.util.Map;

import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.Maps;

public class GetACLCommand extends SimpleCommand<Map<String,String>> {
	
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
		String[] responses = response.getPayload().split(" ");
		for (int i = 3; i < responses.length-1; i += 2) {
			data.put(responses[i], responses[i+1]);
		}
	}

	@Override
	public void setDataInitialValue() {
		data = Maps.newHashMap();
	}
}
