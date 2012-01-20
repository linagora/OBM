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
package org.obm.push.mail.greenmail;

import java.util.Map;

import org.obm.push.mail.MailEnvModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.icegreen.greenmail.util.GreenMail;

public class GreenMailExternalProcess extends JavaExternalProcess {
	
	public static final String MAILBOX_ARG_TAG = "-mailbox:";
	public static final String PASSWORD_ARG_TAG = "-password:";

	public static final boolean DEFAULT_HEAP_INCONFIG = false;
	public static final int DEFAULT_HEAP_MAXSIZE = 0;
	public static final int DEFAULT_TIMEOUT = 10*1000;
	public static final int DEFAULT_STARTTIME = 1*1000;
	
	public GreenMailExternalProcess(String mailbox, String password) {
		this(mailbox, password, DEFAULT_HEAP_INCONFIG, DEFAULT_HEAP_MAXSIZE);
	}

	public GreenMailExternalProcess(String mailbox, String password,
			boolean useDefaultHeapSize, int heapMaxSizeInMo) {
		super(GreenMailStandalone.class, new Config(
				false, useDefaultHeapSize, heapMaxSizeInMo, DEFAULT_TIMEOUT, DEFAULT_STARTTIME));
		setArguments(mailbox, password);
	}
	
	private void setArguments(String mailbox, String password) {
		Map<String, String> cliArgs = Maps.newHashMap();
		cliArgs.put(MAILBOX_ARG_TAG, mailbox);
		cliArgs.put(PASSWORD_ARG_TAG, password);
		setCommandLineArgs(cliArgs);
	}
	
	public static class GreenMailStandalone {
		
		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public static void main(String[] args) {
			Injector metaInjector = Guice.createInjector();
			Module module = metaInjector.getInstance(MailEnvModule.class);
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, module);
			GreenMailStandalone greenMail = new GreenMailStandalone(args);
			injector.injectMembers(greenMail);
			greenMail.initAfterInjected();
		}
		
		@Inject GreenMail greenMail;
		private String mailbox;
		private String password;
		
		private GreenMailStandalone(String[] args) {
			Preconditions.checkArgument(args.length == 2, String.format(
					"Need two args : '%s' then '%s'", MAILBOX_ARG_TAG, PASSWORD_ARG_TAG));
			
			logger.info("Attempt to run a standalone GreenMail server. Args received : {}",
					Joiner.on("; ").join(args));
			
		    mailbox = parseArgumentValue(args[0], MAILBOX_ARG_TAG);
		    password = parseArgumentValue(args[1], PASSWORD_ARG_TAG);
		}

		private String parseArgumentValue(String arg, String tagPrefix) {
			return arg.substring(tagPrefix.length());
		}

		private void initAfterInjected() {
			greenMail.start();
			greenMail.setUser(mailbox, password);
			
			logger.info("GreenMail server is running. Known user : {}/{}",
					new Object[]{mailbox, password});
		}
	}
}
