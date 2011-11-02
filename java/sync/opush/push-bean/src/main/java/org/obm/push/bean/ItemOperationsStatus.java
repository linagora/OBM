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

public enum ItemOperationsStatus {
	SUCCESS, // 1 Success
	PROTOCOL_VIOLATION, // 2 Protocol error - protocol violation/XML
	// validation error.
	SERVER_ERROR, // 3
	DOCUMENT_LIBRARY_BAD_URI, // 4 Document library access - The specified URI
	// is bad.
	DOCUMENT_LIBRARY_ACCESS_DENIED, // 5 Document library - Access denied.
	DOCUMENT_LIBRARY_NOT_FOUND, // 6 Document library - The object was
	// not found.
	DOCUMENT_LIBRARY_CONNECTION_FAILED, // 7 Document library - Failed to
	// connect to the server.
	DOCUMENT_LIBRARY_INVALID_BYTE_RANGE, // 8 Document library - The byte-range
	// is invalid or too large.
	DOCUMENT_LIBRARY_STORE_UNKNOWN, // 9 Document library - The store is unknown
	// or unsupported.
	DOCUMENT_LIBRARY_EMPTY_FILE, // 10 Document library - The file is empty.
	DOCUMENT_LIBRARY_DATA_TOO_LARGE, // 11 Document library - The requested data
	// size is too large.
	DOCUMENT_LIBRARY_DOWNLOAD_FAILED, // 12 Document library - Failed to
	// download file because of input/output
	// (I/O) failure.
	MAILBOX_INVALID_BODY_PREFERENCE, // 13 Mailbox fetch provider - The body
	// preference option is invalid.
	MAILBOX_ITEM_FAILED_CONVERSATION, // 14 Mailbox fetch provider - The item
	// failed conversion.
	MAILBOX_INVALID_ATTACHMENT_ID, // 15 Attachment fetch provider - Attachment
	// or attachment ID is invalid.
	BLOCKED_ACCESS;// 16 Policy-related - Server blocked access.

	public String asXmlValue() {
		switch (this) {
		case PROTOCOL_VIOLATION:
			return "2";
		case SERVER_ERROR:
			return "3";
		case DOCUMENT_LIBRARY_BAD_URI:
			return "4";
		case DOCUMENT_LIBRARY_ACCESS_DENIED:
			return "5";
		case DOCUMENT_LIBRARY_NOT_FOUND:
			return "6";
		case DOCUMENT_LIBRARY_CONNECTION_FAILED:
			return "7";
		case DOCUMENT_LIBRARY_INVALID_BYTE_RANGE:
			return "8";
		case DOCUMENT_LIBRARY_STORE_UNKNOWN:
			return "9";
		case DOCUMENT_LIBRARY_EMPTY_FILE:
			return "10";
		case DOCUMENT_LIBRARY_DATA_TOO_LARGE:
			return "11";
		case DOCUMENT_LIBRARY_DOWNLOAD_FAILED:
			return "12";
		case MAILBOX_INVALID_BODY_PREFERENCE:
			return "13";
		case MAILBOX_ITEM_FAILED_CONVERSATION:
			return "14";
		case MAILBOX_INVALID_ATTACHMENT_ID:
			return "15";
		case BLOCKED_ACCESS:
			return "16";
		case SUCCESS:
		default:
			return "1";
		}
	}
}
