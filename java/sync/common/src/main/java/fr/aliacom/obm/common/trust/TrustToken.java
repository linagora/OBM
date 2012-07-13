/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

import com.google.common.base.Objects;

/**
 * A representation of a {@code Trust} token.<br />
 * A {@link TrustToken} is used for implicit logins to OBM-Sync from other OBM servers.
 */
public class TrustToken {
	private String token;
	private long created;

	/**
	 * Builds a new, empty {@link TrustToken}.
	 */
	public TrustToken() {
		this("");
	}

	/**
	 * Builds a new {@link TrustToken} for the given {@code token}.<br />
	 * This is equivalent to calling: {@code new TrustToken(token, System.currentTimeMillis());}.
	 * 
	 * @param token The actual trust {@code token}.
	 */
	public TrustToken(String token) {
		this(token, System.currentTimeMillis());
	}

	/**
	 * Builds a new {@link TrustToken}.
	 * 
	 * @param token The actual trust {@code token}.
	 * @param created The timestamp of the token creation.
	 */
	public TrustToken(String token, long created) {
		this.token = token;
		this.created = created;
	}

	/**
	 * Checks whether or not this {@link TrustToken} is expired in regards to the given timeout.
	 * 
	 * @param timeoutInSeconds The timeout (in seconds) for the {@link TrustToken}.
	 * 
	 * @return @code true} if and only if the {@link TrustToken} is expired.
	 */
	public boolean isExpired(long timeoutInSeconds) {
		return (System.currentTimeMillis() - created) >= (timeoutInSeconds * 1000);
	}

	/**
	 * Checks and returns whether the given {@code token} is valid by comparing it to the actual trust {@code token} of this {@link TrustToken} instance.
	 * 
	 * @param token The token to check.
	 * 
	 * @return {@code true} if and only if the given token is valid.
	 */
	public boolean isTokenValid(String token) {
		return Objects.equal(this.token, token);
	}

	/**
	 * Getter for field {@link #token} of type {@code String}.
	 * 
	 * @return The value of {@link #token}.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Getter for field {@link #created} of type {@code long}.
	 * 
	 * @return The value of {@link #created}.
	 */
	public long getCreated() {
		return created;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("token", token).add("created", created).toString();
	}
}
