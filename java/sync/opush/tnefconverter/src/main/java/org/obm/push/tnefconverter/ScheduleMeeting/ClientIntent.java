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
package org.obm.push.tnefconverter.ScheduleMeeting;

public enum ClientIntent {
	ciManager, // 1
	ciDelegate,// 2
	ciDeletedWithNoResponse,// 4
	ciDeletedExceptionWithNoResponse,// 8
	ciRespondedTentative,// 10
	ciRespondedAccept,// 20
	ciRespondedDecline,// 40
	ciModifiedStartTime,// 80
	ciModifiedEndTime,// 100
	ciModifiedLocation,// 200
	ciRespondedExceptionDecline,// 400
	ciCanceled,// 800
	ciExceptionCanceled;// 1000
	
	public static ClientIntent getClientIntent(String val){
		if("1".equals(val)){
			return ciManager;
		} else if("2".equals(val)){
			return ciDelegate;
		} else if("4".equals(val)){
			return ciDeletedWithNoResponse;
		} else if("8".equals(val)){
			return ciDeletedExceptionWithNoResponse;
		} else if("10".equals(val)){
			return ciRespondedTentative;
		} else if("20".equals(val)){
			return ciRespondedAccept;
		} else if("40".equals(val)){
			return ciRespondedDecline;
		} else if("80".equals(val)){
			return ciModifiedStartTime;
		} else if("100".equals(val)){
			return ciModifiedEndTime;
		} else if("200".equals(val)){
			return ciModifiedLocation;
		} else if("400".equals(val)){
			return ciRespondedExceptionDecline;
		} else if("800".equals(val)){
			return ciCanceled;
		} else if("1000".equals(val)){
			return ciExceptionCanceled;
		} 
		return null;
	}

}
