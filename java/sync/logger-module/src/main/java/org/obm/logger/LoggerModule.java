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
package org.obm.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class LoggerModule extends AbstractModule {

	public static final String AUTH = "AUTHENTICATION";
	public static final String TRIMMED_REQUEST = "REQUEST.TRIMMED";
	public static final String FULL_REQUEST = "REQUEST.FULL";
	public static final String MAIL_DATA = "MAIL.DATA";
	public static final String OBM_SYNC = "OBM-SYNC";
	public static final String CONFIGURATION = "CONFIGURATION";
	public static final String TECHNICAL_LOG = "technical_log";
	public static final String MIGRATION = "MIGRATION";
	public static final String RESOURCES = "RESOURCES";
	
	@Override
	protected void configure() {
		
		bind(Logger.class).annotatedWith(Names.named(AUTH)).toInstance(LoggerFactory.getLogger(AUTH));
		bind(Logger.class).annotatedWith(Names.named(TRIMMED_REQUEST)).toInstance(LoggerFactory.getLogger(TRIMMED_REQUEST));
		bind(Logger.class).annotatedWith(Names.named(FULL_REQUEST)).toInstance(LoggerFactory.getLogger(FULL_REQUEST));
		bind(Logger.class).annotatedWith(Names.named(MAIL_DATA)).toInstance(LoggerFactory.getLogger(MAIL_DATA));
		bind(Logger.class).annotatedWith(Names.named(OBM_SYNC)).toInstance(LoggerFactory.getLogger(OBM_SYNC));
		bind(Logger.class).annotatedWith(Names.named(CONFIGURATION)).toInstance(LoggerFactory.getLogger(CONFIGURATION));
		bind(Logger.class).annotatedWith(Names.named(TECHNICAL_LOG)).toInstance(LoggerFactory.getLogger(TECHNICAL_LOG));
		bind(Logger.class).annotatedWith(Names.named(MIGRATION)).toInstance(LoggerFactory.getLogger(MIGRATION));
		bind(Logger.class).annotatedWith(Names.named(RESOURCES)).toInstance(LoggerFactory.getLogger(RESOURCES));
	}
	
}
