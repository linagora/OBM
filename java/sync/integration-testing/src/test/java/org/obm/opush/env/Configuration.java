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
package org.obm.opush.env;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;

public class Configuration {

	public static class SyncPerms {
		public String blacklist = "";
		public boolean allowUnkwownDevice = true;
	}

	public static class Mail {
		public boolean activateTls = false;
		public boolean loginWithDomain = true;
		public int timeoutInMilliseconds = 5000;
		public int imapPort = 143;
		protected int maxMessageSize = 1024;
		protected int fetchBlockSize = 1 << 20;
	}

	public static class Transaction {
		public int timeoutInSeconds = 10;
		public boolean usePersistentCache = true;
	}

	public ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.FRANCE);
	public SyncPerms syncPerms = new SyncPerms();
	public Mail mail = new Mail();
	public Transaction transaction = new Transaction();
	public File dataDir;
	public String locatorUrl = null;
	public String obmUiBaseUrl = null;
	public String obmSyncUrl = null;
	public int locatorCacheTimeout = 10;
	public TimeUnit locatorCacheTimeUnit = TimeUnit.SECONDS;
	public String activeSyncServletUrl = null;
	public Charset defautEncoding = Charsets.UTF_8;
	public int trustTokenTimeoutInSeconds = 10;
	public int solrCheckingInterval = 10;

}