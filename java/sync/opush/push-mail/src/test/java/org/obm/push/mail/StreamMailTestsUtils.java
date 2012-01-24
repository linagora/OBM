package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;

public class StreamMailTestsUtils {
	
	public static InputStream getStreamEndingByLineBreak(final InputStream originalStream) {
		return new InputStream() {

			boolean printedAntiSlashR = false;
			boolean printedAntiSlashN = false;
			@Override
			public int available() throws IOException {
				return originalStream.available();
			}

			@Override
			public int read() throws IOException {
				int read = originalStream.read();

				if (read != -1) {
					return read;
				} else {
					if(!printedAntiSlashR){
						printedAntiSlashR = true;
						return '\r';
					} else if(!printedAntiSlashN){
						printedAntiSlashN = true;
						return '\n';
					}
					return -1;
				}
			}
		};
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
