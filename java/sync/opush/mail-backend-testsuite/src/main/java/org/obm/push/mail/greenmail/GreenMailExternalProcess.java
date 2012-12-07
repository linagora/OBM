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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class GreenMailExternalProcess extends JavaExternalProcess {
	
	public static final String MAILBOX_ARG_TAG = "-mailbox:";
	public static final String PASSWORD_ARG_TAG = "-password:";
	public static final String IMAP_PORT_ARG_TAG = "-imapPort:";
	public static final String SMTP_PORT_ARG_TAG = "-smtpPort:";
	private static final String STARTED_TAG = "PROCESS_STARTED_OK";

	public static final int DEFAULT_PROCESS_TTL = 300 * 1000;
	public static final int MIN_PROCESS_STARTTIME = 50;
	public static final int MAX_PROCESS_STARTTIME = 3000;
	
	private Integer imapPort;
	private Integer smtpPort;
	
	public GreenMailExternalProcess() {
		super(GreenMailStandalone.class, new Config(false, DEFAULT_PROCESS_TTL, MIN_PROCESS_STARTTIME));
	}
	
	public ClosableProcess startGreenMail(String mailbox, String password) throws ExternalProcessException {
		ClosableProcess greenmailProcess = execute(buildGreenmailArgs(mailbox, password));
		tryToReadGreenmailListeningPorts();
		return greenmailProcess;
	}
	
	public void tryToReadGreenmailListeningPorts() throws ExternalProcessException {
		try {
			readGreenmailListeningPorts();
		} catch (IOException e) {
			throw new ExternalProcessException("Cannot read Greenmail listening ports", e);
		} catch (InterruptedException e) {
			throw new ExternalProcessException("Cannot read Greenmail listening ports", e);
		}
	}

	private void readGreenmailListeningPorts() throws IOException, InterruptedException {
		imapPort = null;
		smtpPort = null;
		for (String processOutputLine : readProcessOutputLinesUntilStartedTag()) {
			assignImapOrSmtpPort(processOutputLine);
			if (imapPort != null && smtpPort != null) {
				return;
			}
		}
		throw new ExternalProcessException("Cannot read Greenmail listening ports");
	}

	private List<String> readProcessOutputLinesUntilStartedTag() throws IOException, InterruptedException {
		List<String> outputLines = Lists.newArrayList();
		Stopwatch stopwatch = new Stopwatch().start();
		do {
			outputLines.addAll(CharStreams.readLines(readProcessOutput()));
			if (outputLines.contains(STARTED_TAG)) {
				return outputLines;
			}
			Thread.sleep(5);
		} while (stopwatch.elapsedMillis() < MAX_PROCESS_STARTTIME);
		throw new ExternalProcessException(
				"Process started tag not received in accepted delay of : " + MAX_PROCESS_STARTTIME + " ms");
	}

	private void assignImapOrSmtpPort(String processOutputLine) {
		if (processOutputLine.startsWith(IMAP_PORT_ARG_TAG)) {
			imapPort = readIntegerProcessOutputArgument(processOutputLine, IMAP_PORT_ARG_TAG);
		} else if (processOutputLine.startsWith(SMTP_PORT_ARG_TAG)) {
			smtpPort = readIntegerProcessOutputArgument(processOutputLine, SMTP_PORT_ARG_TAG);
		}
	}

	private Map<String, String> buildGreenmailArgs(String mailbox, String password) {
		Map<String, String> cliArgs = Maps.newLinkedHashMap();
		cliArgs.put(MAILBOX_ARG_TAG, mailbox);
		cliArgs.put(PASSWORD_ARG_TAG, password);
		return cliArgs;
	}

	public ServerSetup buildImapServerSetup() {
		return new ServerSetup(imapPort, null, ServerSetup.IMAP.getProtocol());
	}
	
	public Integer getImapPort() {
		return imapPort;
	}
	
	public ServerSetup buildSmtpServerSetup() {
		return new ServerSetup(smtpPort, null, ServerSetup.SMTP.getProtocol());
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public static class GreenMailStandalone {
		
		private final Logger logger = LoggerFactory.getLogger(getClass());

		private static final int AUTOMATIC_PORT = 0;
		
		public static void main(String[] args) {
			GreenMailStandalone greenMail = new GreenMailStandalone(args);
			greenMail.start();
			System.out.println(IMAP_PORT_ARG_TAG + greenMail.greenMail.getImap().getPort());
			System.out.println(SMTP_PORT_ARG_TAG + greenMail.greenMail.getSmtp().getPort());
			System.out.println(STARTED_TAG);
		}
		
		private GreenMail greenMail;
		private String mailbox;
		private String password;
		
		private GreenMailStandalone(String[] args) {
			Preconditions.checkArgument(args.length == 2, String.format(
					"Need two args : '%s' and '%s'", 
					MAILBOX_ARG_TAG, PASSWORD_ARG_TAG));
			
			logger.info("Attempt to run a standalone GreenMail server. Args received : {}",
					Joiner.on("; ").join(args));
			
		    mailbox = parseArgumentValue(args[0], MAILBOX_ARG_TAG);
		    password = parseArgumentValue(args[1], PASSWORD_ARG_TAG);
		    greenMail = new GreenMail(new ServerSetup[] {
		    	new ServerSetup(AUTOMATIC_PORT, null, ServerSetup.PROTOCOL_IMAP),
		    	new ServerSetup(AUTOMATIC_PORT, null, ServerSetup.PROTOCOL_SMTP)
		    });
		}

		private String parseArgumentValue(String arg, String tagPrefix) {
			return arg.substring(tagPrefix.length());
		}

		private void start() {
			greenMail.start();
			greenMail.setUser(mailbox, password);
			
			logger.info("GreenMail server is running. Known user : {}/{}",
					new Object[]{mailbox, password});
		}
	}
}
