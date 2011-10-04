package org.obm.push;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.inject.Singleton;

@Singleton
public class Messages {

	private ResourceBundle bundle;
	private final Locale locale;

	public Messages(Locale locale) {
		this.locale = locale;
		bundle = ResourceBundle.getBundle("Messages", this.locale);
	}
	
	private String getString(String key, Object... arguments) {
		String isoEncodedString = bundle.getString(key);
		String string = new String(isoEncodedString.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
		MessageFormat format = new MessageFormat(string, locale);
		return format.format(arguments);
	}
	
	public String mailTooLargeTitle() {
		return getString("MailTooLargeTitle");
	}
	
	public String mailTooLargeBodyStructure(int maxSize, String previousMessageReferenceText) {
		String humanReadableSize = FileUtils.byteCountToDisplaySize(maxSize);
		return getString("MailTooLargeBodyStructure", humanReadableSize, previousMessageReferenceText);
	}
	
	public String mailTooLargeHeaderFormat(String messageId, String subject, String to, String cc, String bcc) {
		return getString("MailTooLargeHeaderFormat", messageId, subject, 
				Strings.nullToEmpty(to), 
				Strings.nullToEmpty(cc), 
				Strings.nullToEmpty(bcc));
	}
}
