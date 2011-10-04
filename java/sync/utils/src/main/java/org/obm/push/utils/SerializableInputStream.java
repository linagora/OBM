package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.google.common.io.ByteStreams;

public class SerializableInputStream extends NoArgFilterInputStream implements Serializable {

	public SerializableInputStream() {
		super();
	}
	
	public SerializableInputStream(InputStream inputStream) {
		super(inputStream);
	}
	
	 private void writeObject(final ObjectOutputStream out)
		     throws IOException {
		 byte[] byteArray = ByteStreams.toByteArray(in);
		 out.writeInt(byteArray.length);
		 out.write(byteArray);
	 }
	 
	 private void readObject(final ObjectInputStream ois)
			 throws IOException {
		 int lenght = ois.readInt();
		 byte[] data = new byte[lenght];
		 ByteStreams.readFully(ois, data);
		 in = new ByteArrayInputStream(data);
	 }

}
