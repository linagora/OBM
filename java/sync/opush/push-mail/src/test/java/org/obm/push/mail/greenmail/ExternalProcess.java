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
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public abstract class ExternalProcess {

	
	private final CommandLine cli;
	private final Map<String, String> environment;
	private final PumpStreamHandler streamHandler;
	
	private long processTimeout;
	private long processStartTimeNeeded;

	public ExternalProcess(String executablePath, long processTimeout) {
		this(executablePath, processTimeout, 0);
	}

	public ExternalProcess(String executablePath, long processTimeout, long processStartTimeNeeded) {
		Preconditions.checkNotNull(Strings.emptyToNull(executablePath));
		Preconditions.checkArgument(processTimeout > 0);
		
		this.cli = new CommandLine(executablePath);
		this.environment = Maps.newHashMap();
		this.streamHandler = new PumpStreamHandler();
		this.processTimeout = processTimeout;
		this.processStartTimeNeeded = processStartTimeNeeded;
	}
	
	public ClosableProcess execute() throws ExternalProcessException {
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		final Executor executor = buildExecutor();
		executeProcess(resultHandler, executor);
		waitForProcessStartTime(resultHandler);
		
		return new ClosableProcess() {
			@Override
			public void closeProcess() {
				executor.getWatchdog().destroyProcess();
			}
		};
	}

	private Executor buildExecutor() {
		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(streamHandler);
		setTimeoutWatchdog(executor);
		return executor;
	}

	private void setTimeoutWatchdog(Executor executor) {
		executor.setWatchdog(new ExecuteWatchdog(processTimeout));
	}

	private void executeProcess(DefaultExecuteResultHandler resultHandler, Executor executor)
			throws ExternalProcessException {
		try {
			executor.execute(cli, environment, resultHandler);
		} catch (IOException e) {
			throw new ExternalProcessException("Could not execute process", e);
		}
	}

	private void waitForProcessStartTime(DefaultExecuteResultHandler resultHandler)
			throws ExternalProcessException {
		try {
			if (processStartTimeNeeded > 0) {
				resultHandler.waitFor(processStartTimeNeeded);
			}
		} catch (InterruptedException e) {
			throw new ExternalProcessException("Could not execute process", e);
		}
	}

	protected void addEnvironmentVariable(String key, String value) {
		environment.put(key, value);
	}
	
	protected void addSimpleCliArgument(String simpleArg) {
		cli.addArgument(simpleArg);
	}
	
	protected void addTaggedCliArgument(String name, String value) {
		cli.addArgument(name.concat(value));
	}
	
	public PumpStreamHandler getStreamHandler() {
		return streamHandler;
	}

	public long getProcessStartTimeNeeded() {
		return processStartTimeNeeded;
	}
}
