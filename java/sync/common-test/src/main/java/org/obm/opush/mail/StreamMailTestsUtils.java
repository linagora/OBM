package org.obm.opush.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.base.Charsets;

public class StreamMailTestsUtils {

	private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};
	
	public static InputStream getHeaders() {
		String crlf = new String(CRLF);
		return newInputStreamFromString("From: toto" + crlf + crlf);
	}
	
	public static ByteArrayInputStream newInputStreamFromString(String content) {
		return new ByteArrayInputStream(new String(content).getBytes(Charsets.UTF_8));
	}
	
}
