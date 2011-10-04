package org.obm.push.utils;

import java.io.FilterInputStream;
import java.io.InputStream;

public class NoArgFilterInputStream extends FilterInputStream {
	
	public NoArgFilterInputStream() {
		super(null);
	}
	
	public NoArgFilterInputStream(InputStream inputStream) {
		super(inputStream);
	}
}