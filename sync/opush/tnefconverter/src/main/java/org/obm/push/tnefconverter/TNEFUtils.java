package org.obm.push.tnefconverter;

import java.io.InputStream;

import net.freeutils.tnef.Message;

import org.apache.james.mime4j.parser.MimeStreamParser;
import org.obm.push.tnefconverter.ScheduleMeeting.TNEFExtractorUtils;

/**
 * 
 * @author adrienp
 * 
 */
public class TNEFUtils {

	public static Boolean isScheduleMeetingRequest(InputStream email)
			throws TNEFConverterException {
		try {
			MimeStreamParser parser = new MimeStreamParser();
			EmailTnefHandler handler = new EmailTnefHandler();
			parser.setContentHandler(handler);
			parser.parse(email);
			Message message = handler.getTNEFMsg();
			return (message != null && TNEFExtractorUtils
					.isScheduleMeetingRequest(message));
		} catch (Throwable e) {
			throw new TNEFConverterException(e);
		}
	}

	public static Boolean containsTNEFAttchment(InputStream email)
			throws TNEFConverterException {
		try {
			MimeStreamParser parser = new MimeStreamParser();
			EmailTnefHandler handler = new EmailTnefHandler();
			parser.setContentHandler(handler);
			parser.parse(email);
			Message message = handler.getTNEFMsg();
			return message != null;
		} catch (Throwable e) {
			throw new TNEFConverterException(e);
		}
	}
}
