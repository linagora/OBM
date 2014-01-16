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
package org.obm.push.bean.autodiscover;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;


public enum AutodiscoverStatus {

	SUCCESS("1"),
	PROTOCOL_ERROR("2");
	
	private final String specificationValue;
	
	private AutodiscoverStatus(String specificationValue) {
		this.specificationValue = specificationValue;
	}

	/**
	 * 1 : Success. Because the Status element is only returned when the command encounters an error,
		the success status code is never included in a response message.<br>
	 * 2 : Protocol error
	 *
	 * @return status code that corresponds to the error
	 */
	public String asSpecificationValue() {
		return specificationValue;
	}
	

	public static AutodiscoverStatus fromSpecificationValue(String specificationValue){
		if (specValueToEnum.containsKey(specificationValue)) {
			return specValueToEnum.get(specificationValue);
		}
		return null;
	}
	
	private static Map<String, AutodiscoverStatus> specValueToEnum;
	
	static {
		Builder<String, AutodiscoverStatus> builder = ImmutableMap.builder();
		for (AutodiscoverStatus autodiscoverStatus : values()) {
			builder.put(autodiscoverStatus.specificationValue, autodiscoverStatus);
		}
		specValueToEnum = builder.build();
	}
}
