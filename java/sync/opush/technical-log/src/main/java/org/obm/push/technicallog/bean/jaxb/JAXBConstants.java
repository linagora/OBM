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
package org.obm.push.technicallog.bean.jaxb;

public class JAXBConstants {
	
	public final static String REQUEST_ROOT = "request";
	public final static String REQUEST_EMPTY_METHOD_NAME = "createEmptyRequest";
	public final static String DEVICE_ID = "deviceId";
	public final static String DEVICE_TYPE = "deviceType";
	public final static String COMMAND = "command";
	public final static String REQUEST_ID = "requestId";
	public final static String TRANSACTION_ID = "transactionId";
	public final static String REQUEST_START_TIME = "requestStartTime";
	public final static String REQUEST_END_TIME = "requestEndTime";
	public final static String RESOURCES = "resources";
	
	public final static String RESOURCE_ROOT = "resource";
	public final static String RESOURCE_EMPTY_METHOD_NAME = "createEmptyResource";
	public final static String RESOURCE_ID = "resourceId";
	public final static String RESOURCE_TYPE = "resourceType";
	public final static String RESOURCE_START_TIME = "resourceStartTime";
	public final static String RESOURCE_END_TIME = "resourceEndTime";
	
	public final static String TRANSACTION_ROOT = "transaction";
	public final static String TRANSACTION_EMPTY_METHOD_NAME = "createEmptyTransaction";
	public final static String ID = "id";
	public final static String TRANSACTION_START_TIME = "transactionStartTime";
	public final static String TRANSACTION_END_TIME = "transactionEndTime";
	
	public final static String LOG_FILE_ROOT = "logFile";
	public final static String LOG_FILE_EMPTY_METHOD_NAME = "createEmptyLogFile";
	public final static String FILE_NAME = "fileName";
}
