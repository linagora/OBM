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
package org.obm.push.mail.greenmail;

import java.io.File;
import java.util.Map;


public class JavaExternalProcess extends ExternalProcess {
	
	private static final String JAVA_PATH = 
			System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	
	private final Class<?> mainClass;
	private final Config config;
	
	private Long heapMaxSizeInByte;
	
	public JavaExternalProcess(Class<?> mainClass, Config config) {
		super(JAVA_PATH, config.processTimeToLive, config.processStartTimeNeeded);
		this.mainClass = mainClass;
		this.config = config;
	}
	
	@Override
	protected ClosableProcess execute(Map<String, String> cliArgs) throws ExternalProcessException {
		setClasspath();
		setDebugMode(config);
		setHeapMaxSize();
		setMainClass(mainClass);
		return super.execute(cliArgs);
	}

	public void setHeapMaxSize(long heapMaxSizeInByte){
		this.heapMaxSizeInByte = heapMaxSizeInByte;
	}
	
	private int bytesToMegaBytes(long bytes) {
		return Long.valueOf(bytes >> 20).intValue();
	}
	
	private void setDebugMode(Config config) {
		if (config.debug) {
			addSimpleCliArgument("-Xdebug");
			addSimpleCliArgument("-Xrunjdwp:transport=dt_socket,address=8008,server=y,suspend=n");
		}
	}
	
	private void setMainClass(Class<?> mainClass) {
		addSimpleCliArgument(mainClass.getName());
	}

	private void setHeapMaxSize(){
		if (heapMaxSizeInByte != null) {
			addSimpleCliArgument(String.format("-Xmx%dm", bytesToMegaBytes(heapMaxSizeInByte)));
		}
	}

	private void setClasspath() {
		addEnvironmentVariable("CLASSPATH", System.getProperty("java.class.path"));
	}
	
	public static class Config {
		public final boolean debug;
		public final int processTimeToLive;
		public final int processStartTimeNeeded;
		
		public Config(boolean debug, int processTimeToLive, int processStartTimeNeeded) {
			this.debug = debug;
			this.processTimeToLive = processTimeToLive;
			this.processStartTimeNeeded = processStartTimeNeeded;
		}
	}
}
