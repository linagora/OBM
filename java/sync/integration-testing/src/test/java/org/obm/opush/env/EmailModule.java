package org.obm.opush.env;

import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.smtp.SmtpSender;

public final class EmailModule extends AbstractOverrideModule {

	public EmailModule() {
		super();
	}

	@Override
	protected void configureImpl() {
		bindWithMock(ImapClientProvider.class);
		bindWithMock(SmtpSender.class);
	}

}