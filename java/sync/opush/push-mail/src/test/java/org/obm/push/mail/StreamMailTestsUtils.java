package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamMailTestsUtils {

	private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};
	
	public static InputStream getTinyEmailInputStream() {
		return newInputStreamFromString(getTinyEmail());
	}

	public static InputStream getTinyEmailAsShouldBeFetched() {
		return getUniqueTinyEmailAsShouldBeFetched(null);
	}

	public static InputStream getUniqueTinyEmailInputStream(int uniqueNumber) {
		return newInputStreamFromString(getTinyEmail(String.valueOf(uniqueNumber)));
	}
	
	public static InputStream getUniqueTinyEmailAsShouldBeFetched(Integer uniqueNumber) {
		String lineBreak = new String(CRLF);
		StringBuilder shouldHasCRLF = new StringBuilder();
		shouldHasCRLF.append(lineBreak);
		if (uniqueNumber == null) {
			shouldHasCRLF.append(getTinyEmail());
		} else {
			shouldHasCRLF.append(getTinyEmail(String.valueOf(uniqueNumber)));
		}
		shouldHasCRLF.append(lineBreak); 
		return newInputStreamFromString(shouldHasCRLF.toString());
	}
	
	public static String getTinyEmail() {
		return getTinyEmail("");
	}

	public static String getTinyEmail(String moreData) {
		return "I'm a small email".concat(moreData);
	}

	public static InputStream getStreamOfFortyChars() {
		return newInputStreamFromString("This message is forty characters long...");
	}
	
	public static InputStream newInputStreamFromString(String content) {
		return new ByteArrayInputStream(new String(content).getBytes());
	}
	
	public static InputStream getStreamBeginingByLineBreak(final InputStream originalStream) {
		return new InputStream() {

			boolean firstRead = true;
			boolean printedAntiSlashR = false;
			
			@Override
			public int available() throws IOException {
				return originalStream.available();
			}

			@Override
			public int read() throws IOException {

				if (firstRead) {
					if(!printedAntiSlashR){
						printedAntiSlashR = true;
						return '\r';
					} else {
						firstRead = false;
						return '\n';
					}
				} else {
					return originalStream.read();
				}
			}
		};
	}
}
