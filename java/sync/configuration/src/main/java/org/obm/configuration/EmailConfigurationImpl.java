/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.configuration;



import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailConfigurationImpl extends AbstractConfigurationService implements EmailConfiguration {
	
	private static final int IMAP_PORT = 143;
	private static final int MESSAGE_DEFAULT_MAX_SIZE = 10485760;
	
	private static final String BACKEND_CONF_FILE = "/etc/opush/mail_conf.ini";
	private static final String BACKEND_IMAP_LOGIN_WITH_DOMAIN = "imap.loginWithDomain";
	private static final String BACKEND_IMAP_ACTIVATE_TLS = "imap.activateTLS";
	private static final String BACKEND_MESSAGE_MAX_SIZE = "message.maxSize";
	
	@Inject
	private EmailConfigurationImpl() {
		super(BACKEND_CONF_FILE);
	}	
	
	private boolean isOptionEnabled(String option) {
		String entryContent = getStringValue(option);
		return !"false".equals(entryContent);
	}
	
	@Override
	public boolean activateTls() {
		return isOptionEnabled(BACKEND_IMAP_ACTIVATE_TLS);
	}
	
	@Override
	public boolean loginWithDomain() {
		return isOptionEnabled(BACKEND_IMAP_LOGIN_WITH_DOMAIN);
	}
	
	@Override
	public int getMessageMaxSize() {
		return getIntValue(BACKEND_MESSAGE_MAX_SIZE, MESSAGE_DEFAULT_MAX_SIZE);
	}
	
	@Override
	public int imapPort() {
		return IMAP_PORT;
	}
}
