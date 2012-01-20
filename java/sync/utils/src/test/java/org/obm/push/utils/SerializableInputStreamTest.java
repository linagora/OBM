/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class SerializableInputStreamTest {

	@Test
	public void testRead() throws IOException {
		byte[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(expected);
		SerializableInputStream serializableInputStream = new SerializableInputStream(byteInputStream);
		byte[] read = ByteStreams.toByteArray(serializableInputStream);
		Assertions.assertThat(read).isEqualTo(expected);
	}

	@Test
	public void testSerializeThenRead() throws IOException, ClassNotFoundException {
		byte[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(expected);
		SerializableInputStream serializableInputStream = new SerializableInputStream(byteInputStream);
		
		byte[] serializedForm = serializeAsByteArray(serializableInputStream);
		InputStream deserializedInputStream = (InputStream) deserializeByteArray(serializedForm);
		
		byte[] read = ByteStreams.toByteArray(deserializedInputStream);
		Assertions.assertThat(read).isEqualTo(expected);
	}

	private byte[] serializeAsByteArray(Object obj) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		objectOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}
	
	private Object deserializeByteArray(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		return objectInputStream.readObject();
	}
	
}
