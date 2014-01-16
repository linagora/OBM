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

package org.obm.push.mail.conversation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;

public class MailBody {

	Map<String, String> formatValueMap;

	public MailBody() {
		formatValueMap = new HashMap<String, String>();
	}

	public MailBody(String mime, String value) {
		this();
		formatValueMap.put(mime, value);
	}

	public void addConverted(String mime, String value) {
		formatValueMap.put(mime, value);
	}

	public Set<String> availableFormats() {
		return formatValueMap.keySet();
	}

	public String getValue(String format) {
		return formatValueMap.get(format);
	}
	
	public void addMailPart(String mime, String part){
		String body = this.formatValueMap.get(mime);
		if(body!=null){
			body += part;
			this.addConverted(mime, body);
		}
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailBody other = (MailBody) obj;
		if (formatValueMap == null) {
			if (other.formatValueMap != null)
				return false;
		} else {
			for(Iterator<String> it = formatValueMap.keySet().iterator();it.hasNext();){
				String key = it.next();
				if(!formatValueMap.get(key).equals(other.formatValueMap.get(key))){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(formatValueMap);
	}
	
	public void clear(){
		this.formatValueMap.clear();
	}
	
	
}
