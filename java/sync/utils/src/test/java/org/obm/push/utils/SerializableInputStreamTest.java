package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fest.assertions.Assertions;
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
