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
package org.obm.push.bean;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum MSMessageClass {
	
	NOTE("IPM.Note"),
	NOTE_RULES_OOFTEMPLATE_MS("IPM.Note.Rules.OofTemplate.Microsoft"),
	NOTE_SMIME("IPM.Note.SMIME"),
	NOTE_SMIME_MULTIPART_SIGNED("IPM.Note.SMIME.MultipartSigned"),
	SCHEDULE_MEETING_REQUEST("IPM.Schedule.Meeting.Request"),
	SCHEDULE_MEETING_CANCELED("IPM.Schedule.Meeting.Canceled"),
	SCHEDULE_MEETING_RESP_POS("IPM.Schedule.Meeting.Resp.Pos"),
	SCHEDULE_MEETING_RESP_TENT("IPM.Schedule.Meeting.Resp.Tent"),
	SCHEDULE_MEETING_RESP_NEG("IPM.Schedule.Meeting.Resp.Neg"),
	POST("IPM.Post");
	
	private final String value;

	private MSMessageClass(String value) {
		this.value = value;
	}
	
	public String specificationValue() {
		return value;
	}

	public static MSMessageClass fromSpecificationValue(String specificationValue) {
    	if (specValueToEnum.containsKey(specificationValue)) {
    		return specValueToEnum.get(specificationValue);
    	}
		return null;
    }

    private static Map<String, MSMessageClass> specValueToEnum;
    
    static {
    	Builder<String, MSMessageClass> builder = ImmutableMap.builder();
    	for (MSMessageClass enumeration : MSMessageClass.values()) {
    		builder.put(enumeration.specificationValue(), enumeration);
    	}
    	specValueToEnum = builder.build();
    }
}
