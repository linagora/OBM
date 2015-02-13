/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.obm.provisioning.ldap.client.samba;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class NTLMUtils {

	public static final int NTLM_MAX_PASSWORD_LENGTH = 14;
	public static final byte[] MAGIC_CONSTANT = {'K', 'G', 'S', '!', '@', '#', '$', '%'};

	public static byte[] lmHash(String password) throws Exception {
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
