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
package org.obm.sync.book;

import java.io.Serializable;

import org.obm.annotations.database.DatabaseField;

import com.google.common.base.Objects;
import com.google.common.base.Strings;


public class Website implements Serializable {

	public static final String WEBSITE_TABLE = "Website";

	private String url;
	private String label;
	
	public Website() {
	}

	public Website(String label, String url) {
		super();
		this.label = label;
		this.url = url;
	}

	@DatabaseField(table = WEBSITE_TABLE, column = "website_url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@DatabaseField(table = WEBSITE_TABLE, column = "website_label")
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isCalendarUrl() {
		if (getLabel().toLowerCase().startsWith("caluri")) {
			return true;
		}
		return false;
	}


	@Override
	public int hashCode(){
		return Objects.hashCode(
			Strings.nullToEmpty(label).toLowerCase(), 
			url
		);
	}

	@Override
	public boolean equals(Object object){
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (object instanceof Website) {
			Website that = (Website) object;
			return Objects.equal(this.url, that.url)
				&& Objects.equal(
					this.label != null ? this.label.toLowerCase() : null,
					that.label != null ? that.label.toLowerCase() : null);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("label", label)
				.add("url", url)
				.toString();
	}
}
