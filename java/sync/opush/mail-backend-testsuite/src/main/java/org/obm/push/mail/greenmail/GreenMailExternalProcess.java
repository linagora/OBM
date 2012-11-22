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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class GreenMailExternalProcess extends JavaExternalProcess {
	
	public static final String MAILBOX_ARG_TAG = "-mailbox:";
	public static final String PASSWORD_ARG_TAG = "-password:";
	public static final String IMAP_PORT_ARG_TAG = "-imapPort:";
	public static final String SMTP_PORT_ARG_TAG = "-smtpPort:";

	public static final boolean DEFAULT_HEAP_INCONFIG = false;
	public static final int DEFAULT_HEAP_MAXSIZE = 0;
	public static final int DEFAULT_PROCESS_TTL = 300 * 1000;
	public static final int DEFAULT_STARTTIME = 100;
	
	public GreenMailExternalProcess(String mailbox, String password, 
			int imapPort, int smtpPort) {
		this(mailbox, password, imapPort, smtpPort, DEFAULT_HEAP_INCONFIG, DEFAULT_HEAP_MAXSIZE);
	}

	public GreenMailExternalProcess(String mailbox, String password, 
			int imapPort, int smtpPort,
			boolean useDefaultHeapSize, long heapMaxSizeInByte) {
		super(GreenMailStandalone.class, new Config(
				false, useDefaultHeapSize, heapMaxSizeInByte, DEFAULT_PROCESS_TTL, DEFAULT_STARTTIME));
		setArguments(mailbox, password, imapPort, smtpPort);
	}
	
	private void setArguments(String mailbox, String password, int imapPort, int smtpPort) {
		Map<String, String> cliArgs = Maps.newLinkedHashMap();
		cliArgs.put(MAILBOX_ARG_TAG, mailbox);
		cliArgs.put(PASSWORD_ARG_TAG, password);
		cliArgs.put(IMAP_PORT_ARG_TAG, String.valueOf(imapPort));
		cliArgs.put(SMTP_PORT_ARG_TAG, String.valueOf(smtpPort));
		setCommandLineArgs(cliArgs);
	}
	
	public static class GreenMailStandalone {
		
		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public static void main(String[] args) {
//			Injector metaInjector = Guice.createInjector();
//			Module module = metaInjector.getInstance(MailEnvModule.class);
//			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, module);
			GreenMailStandalone greenMail = new GreenMailStandalone(args);
//			injector.injectMembers(greenMail);
			greenMail.initAfterInjected();
		}
		
		private GreenMail greenMail;
		private String mailbox;
		private String password;
		private int imapPort;
		private int smtpPort;
		
		private GreenMailStandalone(String[] args) {
			Preconditions.checkArgument(args.length == 4, String.format(
					"Need four args : '%s', '%s', '%s' and '%s'", 
					MAILBOX_ARG_TAG, PASSWORD_ARG_TAG, IMAP_PORT_ARG_TAG, SMTP_PORT_ARG_TAG));
			
			logger.info("Attempt to run a standalone GreenMail server. Args received : {}",
					Joiner.on("; ").join(args));
			
		    mailbox = parseArgumentValue(args[0], MAILBOX_ARG_TAG);
		    password = parseArgumentValue(args[1], PASSWORD_ARG_TAG);
		    imapPort = Integer.valueOf(parseArgumentValue(args[2], IMAP_PORT_ARG_TAG));
		    smtpPort = Integer.valueOf(parseArgumentValue(args[3], SMTP_PORT_ARG_TAG));
		    greenMail = new GreenMail(new ServerSetup[] {
		    	new ServerSetup(imapPort, null, ServerSetup.PROTOCOL_IMAP),
		    	new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP)
		    });
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
