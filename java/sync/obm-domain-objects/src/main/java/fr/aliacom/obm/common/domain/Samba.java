/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2015  Linagora
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
package fr.aliacom.obm.common.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Samba implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Samba from(Map<String, String> sambaProperties) {
		Builder builder = builder();
		for (Entry<String, String> entry : sambaProperties.entrySet()) {
			switch (entry.getKey()) {
				case "sid":
					builder.sid(entry.getValue());
					break;
				case "profile":
					builder.profile(entry.getValue());
					break;
				case "home":
					builder.home(entry.getValue());
					break;
				case "drive":
					builder.drive(entry.getValue());
					break;
			}
		}
		return builder.build();
	}
	
	public static class Builder {

		private String sid;
		private String profile;
		private String home;
		private String drive;
		
		private Builder() {
		}
		
		public Builder sid(String sid) {
			Preconditions.checkNotNull(sid);
			this.sid = sid;
			return this;
		}
		
		public Builder profile(String profile) {
			this.profile = profile;
			return this;
		}
		
		public Builder home(String home) {
			this.home = home;
			return this;
		}
		
		public Builder drive(String drive) {
			this.drive = drive;
			return this;
		}
		
		public Samba build() {
			Preconditions.checkState(!Strings.isNullOrEmpty(sid));
			
			return new Samba(sid, profile, home, drive);
		}
	}
	
	private final String sid;
	private final String profile;
	private final String home;
	private final String drive;
	
	private Samba(String sid, String profile, String home, String drive) {
		this.sid = sid;
		this.profile = profile;
		this.home = home;
		this.drive = drive;
	}
	
	public String getSid() {
		return sid;
	}

	public String getProfile() {
		return profile;
	}

	public String getHome() {
		return home;
	}

	public String getDrive() {
		return drive;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(sid, profile, home, drive);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Samba) {
			Samba that = (Samba) object;
			return Objects.equal(this.sid, that.sid)
				&& Objects.equal(this.profile, that.profile)
				&& Objects.equal(this.home, that.home)
				&& Objects.equal(this.drive, that.drive);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("sid", sid)
			.add("profile", profile)
			.add("home", home)
			.add("drive", drive)
			.toString();
	}
}
