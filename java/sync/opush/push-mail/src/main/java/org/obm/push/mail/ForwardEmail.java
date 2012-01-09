package org.obm.push.mail;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.obm.configuration.ConfigurationService;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;

public class ForwardEmail extends ReplyEmail {

	public ForwardEmail(ConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, MSEmail forwarded, Message message) throws MimeException, NotQuotableEmailException {
		super(configuration, mime4jUtils, defaultFrom, forwarded, message);
	}
}
