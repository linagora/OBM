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
package org.obm.push.bean;

public enum PingStatus {

	NO_CHANGES, // 1
	CHANGES_OCCURED, // 2
	MISSING_REQUEST_PARAMS, // 3
	SYNTAX_ERROR_IN_REQUEST, // 4
	INVALID_HEARTBEAT_INTERVAL, // 5
	TOO_MANY_FOLDERS, // 6
	FOLDER_SYNC_REQUIRED, // 7
	SERVER_ERROR; // 8

	public String asXmlValue() {
		switch (this) {
		case CHANGES_OCCURED:
			return "2";
		case MISSING_REQUEST_PARAMS:
			return "3";
		case SYNTAX_ERROR_IN_REQUEST:
			return "4";
		case INVALID_HEARTBEAT_INTERVAL:
			return "5";
		case TOO_MANY_FOLDERS:
			return "6";
		case FOLDER_SYNC_REQUIRED:
			return "7";
		case SERVER_ERROR:
			return "8";

		case NO_CHANGES:
		default:
			return "1";
		}
	}

}
