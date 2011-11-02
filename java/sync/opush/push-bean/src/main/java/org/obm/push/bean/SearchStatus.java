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

public enum SearchStatus {
	
	SUCCESS, //1
	PROTOCOL_VIOLATION,// 2 XML validation error.
	SERVER_ERROR,// 3
	BAD_LINK, // 4
	ACCESS_DENIED, // 5
	NOT_FOUND, // 6
	CONNECTION_FAILED,// 7 
	QUERY_TOO_COMPLEX,// 8 The search query is too complex.
	INDEXING_NOT_LOADED,// 9 Unable to execute this query because Content Indexing is not loaded.
	TIME_OUT, // 10
	BAD_COLLECTION_ID, // 11 Bad CollectionId (the client MUST perform a FolderSync).
	END_OF_RANGE,// 12 Server reached the end of the range that is retrievable by synchronization.
	ACCESS_BLOCKED, // 13 Access Blocked (policy restriction)
	CREDENTIALS_REQUIRED; // 14 Credentials Required to Continue

	
	public String asXmlValue() {
		switch (this) {
		case PROTOCOL_VIOLATION:
			return "2";
		case SERVER_ERROR:
			return "3";
		case BAD_LINK:
			return "4";
		case ACCESS_DENIED:
			return "5";
		case NOT_FOUND:
			return "6";
		case CONNECTION_FAILED:
			return "7";
		case QUERY_TOO_COMPLEX:
			return "8";
		case INDEXING_NOT_LOADED:
			return "9";
		case TIME_OUT:
			return "10";
		case BAD_COLLECTION_ID:
			return "11";
		case END_OF_RANGE:
			return "12";
		case ACCESS_BLOCKED:
			return "13";
		case CREDENTIALS_REQUIRED:
			return "14";
		case SUCCESS:
		default:
			return "1";
		}
	}
}
