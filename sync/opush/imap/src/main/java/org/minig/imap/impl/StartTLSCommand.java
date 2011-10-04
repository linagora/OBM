package org.minig.imap.impl;

import java.util.List;

import org.minig.imap.command.SimpleCommand;

public class StartTLSCommand extends SimpleCommand<Boolean> {

	public StartTLSCommand() {
		super("STARTTLS");
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		data = isOk(rs);
		if (data) {
			logger.info("STARTTLS accepted by server.");
		}
	}

}
