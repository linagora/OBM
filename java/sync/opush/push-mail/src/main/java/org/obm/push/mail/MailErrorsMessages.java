/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.mail;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.obm.push.configuration.OpushConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailErrorsMessages {

	private final ResourceBundle bundle;
	
	@Inject
	private MailErrorsMessages(OpushConfiguration opushConfiguration) {
		bundle = opushConfiguration.getResourceBundle(Locale.getDefault());
	}
	
	private String getString(String key, Object... arguments) {
		String isoEncodedString = bundle.getString(key);
		String string = new String(isoEncodedString.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
		MessageFormat format = new MessageFormat(string, bundle.getLocale());
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
