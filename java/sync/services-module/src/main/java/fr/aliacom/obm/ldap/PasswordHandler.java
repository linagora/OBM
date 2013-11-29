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
package fr.aliacom.obm.ldap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;

public class PasswordHandler {

	PasswordHandler() {
		super();
	}

	public synchronized boolean verify(String digest, String password)
			throws NoSuchAlgorithmException {

		String alg = null;
		int size = 0;

		if (digest.regionMatches(true, 0, "{CRYPT}", 0, 7)) {
			digest = digest.substring(7);
			return UnixCrypt.matches(digest, password);
		} else if (digest.regionMatches(true, 0, "{SHA}", 0, 5)) {
			digest = digest.substring(5); // ignore the label
			alg = "SHA-1";
			size = 20;
		} else if (digest.regionMatches(true, 0, "{SSHA}", 0, 6)) {
			digest = digest.substring(6); // ignore the label
			alg = "SHA-1";
			size = 20;
		} else if (digest.regionMatches(true, 0, "{MD5}", 0, 5)) {
			digest = digest.substring(5); // ignore the label
			alg = "MD5";
			size = 16;
		} else if (digest.regionMatches(true, 0, "{SMD5}", 0, 6)) {
			digest = digest.substring(6); // ignore the label
			alg = "MD5";
			size = 16;
		}

		// TODO: vérifier si le synchronized que j'ai ajouté est nécessaire
		MessageDigest msgDigest = MessageDigest.getInstance(alg);

		byte[][] hs = split(Base64.decodeBase64(digest), size);
		byte[] hash = hs[0];
		byte[] salt = hs[1];

		msgDigest.reset();
		msgDigest.update(password.getBytes(Charsets.UTF_8));
		msgDigest.update(salt);

		byte[] pwhash = msgDigest.digest();

		return MessageDigest.isEqual(hash, pwhash);
	}

	public String generateDigest(String password, String saltHex,
			String algorithm) throws NoSuchAlgorithmException {

		if (algorithm.equalsIgnoreCase("crypt")) {
			return "{CRYPT}" + UnixCrypt.crypt(password);
		} else if (algorithm.equalsIgnoreCase("sha")) {
			algorithm = "SHA-1";
		} else if (algorithm.equalsIgnoreCase("md5")) {
			algorithm = "MD5";
		}

		MessageDigest msgDigest = MessageDigest.getInstance(algorithm);

		byte[] salt = {};
		if (saltHex != null) {
			salt = fromHex(saltHex);
		}

		String label = null;

		if (algorithm.startsWith("SHA")) {
			label = (salt.length > 0) ? "{SSHA}" : "{SHA}";
		} else if (algorithm.startsWith("MD5")) {
			label = (salt.length > 0) ? "{SMD5}" : "{MD5}";
		}
		else {
			throw new IllegalArgumentException(String.format("Unknown algorithm: %s", algorithm));
		}

		msgDigest.reset();
		msgDigest.update(password.getBytes(Charsets.UTF_8));
		msgDigest.update(salt);

		byte[] pwhash = msgDigest.digest();

		StringBuffer digest = new StringBuffer(label);
		digest.append(Base64.encodeBase64String(concatenate(pwhash, salt)));

		return digest.toString();
	}

	private static byte[] concatenate(byte[] l, byte[] r) {
		byte[] b = new byte[l.length + r.length];
		System.arraycopy(l, 0, b, 0, l.length);
		System.arraycopy(r, 0, b, l.length, r.length);
		return b;
	}

	private static byte[][] split(byte[] src, int n) {
		byte[] l, r;
		if (src.length <= n) {
			l = src;
			r = new byte[0];
		} else {
			l = new byte[n];
			r = new byte[src.length - n];
			System.arraycopy(src, 0, l, 0, n);
			System.arraycopy(src, n, r, 0, r.length);
		}
		byte[][] lr = { l, r };
		return lr;
	}

	private static String hexits = "0123456789abcdef";

	private static byte[] fromHex(String s) {
		s = s.toLowerCase();
		byte[] b = new byte[(s.length() + 1) / 2];
		int j = 0;
		int h;
		int nybble = -1;
		for (int i = 0; i < s.length(); ++i) {
			h = hexits.indexOf(s.charAt(i));
			if (h >= 0) {
				if (nybble < 0) {
					nybble = h;
				} else {
					b[j++] = (byte) ((nybble << 4) + h);
					nybble = -1;
				}
			}
		}
		if (nybble >= 0) {
			b[j++] = (byte) (nybble << 4);
		}
		if (j < b.length) {
			byte[] b2 = new byte[j];
			System.arraycopy(b, 0, b2, 0, j);
			b = b2;
		}
		return b;
	}
}
