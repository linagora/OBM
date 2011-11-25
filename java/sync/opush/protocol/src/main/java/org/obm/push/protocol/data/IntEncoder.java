package org.obm.push.protocol.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IntEncoder {

	@Inject
	/* package */ IntEncoder() {
	}
	
	public byte[] toByteArray(int value) {
		return new byte[] {
		        (byte) value,
		        (byte) (value >> 8),
		        (byte) (value >> 16),
		        (byte) (value >> 24)};
	}
	
}
