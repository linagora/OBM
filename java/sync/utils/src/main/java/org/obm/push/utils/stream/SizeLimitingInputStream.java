package org.obm.push.utils.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizeLimitingInputStream  extends FilterInputStream {

	private final int limit;
	private int readBytes;
	
	public SizeLimitingInputStream(InputStream inputStream, int limit) {
		super(inputStream);
		this.limit = limit;
		this.readBytes = 0;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);
		if (read != -1) {
			readBytes += read;
			if (readBytes > limit) {
				throw new SizeLimitExceededException();
			}
		}
		return read;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}
	
}
