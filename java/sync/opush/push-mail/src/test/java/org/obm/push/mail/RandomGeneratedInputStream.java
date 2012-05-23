package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;


public class RandomGeneratedInputStream extends InputStream {

	private static final int NUMBER_CHARACTERS_PER_LINE = 76;
	private static final int CRLF_FIRST_PART_END_LINE = 74;
	private static final int CRLF_SECOND_PART_END_LINE = 75;
	private static final int CHARACTERS = 66;
	
	private long length;
	private int cpt;

	public RandomGeneratedInputStream(long length) {
		this.length = length;
		this.cpt = 0;
	}
	
	@Override
	public int read() throws IOException {
		cpt += 1;
		if (cpt < length) {
			long modulo = cpt % NUMBER_CHARACTERS_PER_LINE;
			if (modulo == CRLF_FIRST_PART_END_LINE) {
				return '\r';
			} else if (modulo == CRLF_SECOND_PART_END_LINE) {
				return '\n';
			} else {
				return CHARACTERS;
			}
		} else {
			return -1;
		}
	}
	
	@Override
	public synchronized void reset() throws IOException {
		cpt = 0;
	}

}