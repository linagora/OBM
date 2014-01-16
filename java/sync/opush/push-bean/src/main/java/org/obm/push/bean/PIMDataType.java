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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum PIMDataType implements DBEnum {

	UNKNOWN("Unknown", "unknown"),
	EMAIL("Email", "email"), 
	CALENDAR("Calendar", "calendar"),
	CONTACTS("Contacts", "contacts"),
	TASKS("Tasks", "tasks");

	private static final String DB_FIELD_NAME = "pimdata_type";
	
	private final String xmlValue;
	private final String collectionPathValue;
	
	private PIMDataType(String xmlValue, String collectionPathValue) {
		this.xmlValue = xmlValue;
		this.collectionPathValue = collectionPathValue;
	}
	
	public String asXmlValue() {
		return xmlValue;
	}
	
	public String asCollectionPathValue() {
		return collectionPathValue;
	}

	@Override
	public String getDbFieldName() {
		return DB_FIELD_NAME;
	}

	@Override
	public String getDbValue() {
		return asCollectionPathValue().toUpperCase();
	}

    
    public static PIMDataType fromSpecificationValue(String specificationValue) {
    	if (specValueToEnum.containsKey(specificationValue)) {
    		return specValueToEnum.get(specificationValue);
    	}
		return null;
    }

    private static Map<String, PIMDataType> specValueToEnum;
    
    static {
    	Builder<String, PIMDataType> builder = ImmutableMap.builder();
    	for (PIMDataType status : PIMDataType.values()) {
    		builder.put(status.asXmlValue(), status);
    	}
    	specValueToEnum = builder.build();
    }

	public static PIMDataType recognizeDataType(String dataClass) {
		if (!Strings.isNullOrEmpty(dataClass)) {
			PIMDataType recognizedDataType = fromSpecificationValue(dataClass);
			return Objects.firstNonNull(recognizedDataType, UNKNOWN);
		}
		return null;
	}
}
