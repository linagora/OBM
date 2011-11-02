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

public enum ProvisionStatus {

	SUCCESS, // 1

	// if the parent element is the 'PROVISION' element
	PROTOCOL_ERROR, // 2
	GENERAL_SERVER_ERROR, // 3
	
	// if the parent element is the 'POLICY' element (the child of the POLICY is in the RESPONSE)

	POLICY_NOT_DEFINED, // 2
	UNKNOW_POLICY_TYPE_VALUE, // 3
	THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY; // 5
	
	public String asXmlValue() {
		switch (this) {
		case PROTOCOL_ERROR:
		case POLICY_NOT_DEFINED:
			return "2";
		case GENERAL_SERVER_ERROR:
		case UNKNOW_POLICY_TYPE_VALUE:
				return "3";
		case THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY:
			return "5";
		case SUCCESS:
		default:
			return "1";
		}
	}
}
