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
package org.obm.push.spushnik.service;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import org.obm.push.spushnik.bean.Credentials;

import com.google.common.base.Strings;
import com.google.inject.Singleton;

@Singleton
public class CredentialsService {

	public void validate(Credentials credentials) throws InvalidCredentialsException {
		verifyNotEmptyLoginAtDomainAndPassword(credentials);
		verifyCertificateIsValid(credentials); 
	}
	
	private void verifyNotEmptyLoginAtDomainAndPassword(Credentials credentials) throws InvalidCredentialsException {
		verifyNotEmptyLoginAtDomain(credentials);
		verifyNotEmptyPassword(credentials);
	}

	private void verifyNotEmptyPassword(Credentials credentials) throws InvalidCredentialsException {
		if (Strings.isNullOrEmpty(credentials.getPassword())) {
			throw new InvalidCredentialsException("Invalid password: " + credentials.getPassword());
		}
	}

	private void verifyNotEmptyLoginAtDomain(Credentials credentials) throws InvalidCredentialsException {
		if (Strings.isNullOrEmpty(credentials.getLoginAtDomain())) {
			throw new InvalidCredentialsException("Invalid loginAtDomain: " + credentials.getLoginAtDomain());
		}
	}

	private boolean verifyCertificateIsValid(Credentials credentials) throws InvalidCredentialsException {
		return credentials.getPkcs12() == null || pkcs12IsWellFormated(credentials);
	}

	private boolean pkcs12IsWellFormated(Credentials credentials) throws InvalidCredentialsException {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new ByteArrayInputStream(credentials.getPkcs12()), credentials.getPkcs12Password());
			return true;
		} catch (Exception e) {
			throw new InvalidCredentialsException("Invalid certificate", e);
		}
	}
}
