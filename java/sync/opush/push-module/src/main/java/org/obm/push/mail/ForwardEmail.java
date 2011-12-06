package org.obm.push.mail;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.OpushConfigurationService;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;

public class ForwardEmail extends ReplyEmail {

	public ForwardEmail(OpushConfigurationService configuration, Mime4jUtils mime4jUtils, String defaultFrom, MSEmail forwarded, Message message) throws ParserException, MimeException, NotQuotableEmailException {
		super(configuration, mime4jUtils, defaultFrom, forwarded, message);
	}
}
