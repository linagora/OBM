package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;


public class RandomGeneratedInputStream extends InputStream {

	private long length;

	public RandomGeneratedInputStream(long length) {
		this.length = length;
	}
	
	@Override
	public int read() throws IOException {
		if (length-- > 0) {
			return 66;
		} else {
			return -1;
		}
	}

}