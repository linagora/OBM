package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;


public class RandomGeneratedInputStream extends InputStream {

	@Override
	public int read() throws IOException {
		return getRandomChar();
	}

	private int getRandomChar() {
		return 100 + (int)(Math.random() * 100);
	}
}