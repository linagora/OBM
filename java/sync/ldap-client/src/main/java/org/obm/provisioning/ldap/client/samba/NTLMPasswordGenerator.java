/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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
package org.obm.provisioning.ldap.client.samba;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.obm.provisioning.ldap.client.bean.NTLMPassword;

public class NTLMPasswordGenerator {

	public static final int NTLM_MAX_PASSWORD_LENGTH = 14;
	public static final byte[] MAGIC_CONSTANT = {'K', 'G', 'S', '!', '@', '#', '$', '%'};

	public static NTLMPassword computeNTLMPassword(String clearTextPassword) throws Exception {
		return NTLMPassword
				.builder()
				.lmHash(toHex(lmHash(clearTextPassword)))
				.ntHash(toHex(ntHash(clearTextPassword)))
				.build();
	}

	private static String toHex(byte[] raw) {
		return Hex.encodeHexString(raw).toUpperCase();
	}

	private static byte[] ntHash(String password) throws Exception {
		MD4 md4 = new MD4();
		byte[] unicodePassword = password.getBytes("UnicodeLittleUnmarked");

		md4.engineUpdate(unicodePassword, 0, unicodePassword.length);

		return md4.engineDigest();
	}

	/**
	 * Part of the code below is from Apache HTTP client.
	 * See {@link org.apache.http.impl.auth.NTLMEngineImpl.CipherGen}
	 */

	private static byte[] lmHash(String password) throws Exception {
		Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
		byte[] oemPassword = password.toUpperCase().getBytes("US-ASCII");
		int length = Math.min(oemPassword.length, NTLM_MAX_PASSWORD_LENGTH);
		byte[] keyBytes = new byte[NTLM_MAX_PASSWORD_LENGTH];

		System.arraycopy(oemPassword, 0, keyBytes, 0, length);

		des.init(Cipher.ENCRYPT_MODE, createDESKey(keyBytes, 0));
		byte[] lowHash = des.doFinal(MAGIC_CONSTANT);

		des.init(Cipher.ENCRYPT_MODE, createDESKey(keyBytes, 7));
		byte[] highHash = des.doFinal(MAGIC_CONSTANT);

		byte[] lmHash = new byte[16];
		System.arraycopy(lowHash, 0, lmHash, 0, 8);
		System.arraycopy(highHash, 0, lmHash, 8, 8);

		return lmHash;
	}

	private static Key createDESKey(byte[] bytes, int offset) {
		byte[] keyBytes = new byte[7];
		System.arraycopy(bytes, offset, keyBytes, 0, 7);

		byte[] material = new byte[8];
		material[0] = keyBytes[0];
		material[1] = (byte) (keyBytes[0] << 7 | (keyBytes[1] & 0xff) >>> 1);
		material[2] = (byte) (keyBytes[1] << 6 | (keyBytes[2] & 0xff) >>> 2);
		material[3] = (byte) (keyBytes[2] << 5 | (keyBytes[3] & 0xff) >>> 3);
		material[4] = (byte) (keyBytes[3] << 4 | (keyBytes[4] & 0xff) >>> 4);
		material[5] = (byte) (keyBytes[4] << 3 | (keyBytes[5] & 0xff) >>> 5);
		material[6] = (byte) (keyBytes[5] << 2 | (keyBytes[6] & 0xff) >>> 6);
		material[7] = (byte) (keyBytes[6] << 1);
		oddParity(material);

		return new SecretKeySpec(material, "DES");
	}

	private static void oddParity(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3) ^ (b >>> 2) ^ (b >>> 1)) & 0x01) == 0;

			if (needsParity) {
				bytes[i] |= (byte) 0x01;
			} else {
				bytes[i] &= (byte) 0xfe;
			}
		}
	}
}
