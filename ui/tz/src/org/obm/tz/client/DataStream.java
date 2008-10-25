package org.obm.tz.client;

public class DataStream {

	private byte[] data;
	private int idx;

	public DataStream(byte[] s) {
		this.data = s;
		idx = 0;
	}

	public void skip(int skip) {
		idx += skip;
	}

	public int readInt() {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public void readFully(byte[] transTypes) {
		for (int i = 0; i<transTypes.length;i++) {
			transTypes[i] = readByte();
		}
	}

	private int read() {
		return data[idx++] & 0xff;
	}
	
	public byte readByte() {
		return (byte) read();
	}

	public void close() {
	}

}
