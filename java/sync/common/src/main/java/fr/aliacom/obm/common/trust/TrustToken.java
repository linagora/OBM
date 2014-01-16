/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.trust;

import java.util.Date;

import com.google.common.base.Objects;

/**
 * A representation of a {@code Trust} token.<br />
 * A {@link TrustToken} is used for implicit logins to OBM-Sync from other OBM servers.
 */
public class TrustToken {
	private final String token;
	private final Date creationDate;

	public TrustToken(String token) {
		this(token, new Date());
	}

	public TrustToken(String token, Date creationDate) {
		this.token = token;
		this.creationDate = creationDate;
	}

	public boolean isExpired(long timeoutInSeconds) {
		return (System.currentTimeMillis() - creationDate.getTime()) >= (timeoutInSeconds * 1000);
	}

	public boolean isTokenValid(String token) {
		return Objects.equal(this.token, token);
	}

	public String getToken() {
		return token;
	}

	public Date getCreatedDate() {
		return creationDate;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("token", token).add("creationDate", creationDate).toString();
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(getToken(), getCreatedDate());
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj != null && obj instanceof TrustToken) {
			TrustToken other = (TrustToken) obj;
			
			return Objects.equal(token, other.token) && Objects.equal(creationDate, other.creationDate);
		}
		
		return false;
	}
}
