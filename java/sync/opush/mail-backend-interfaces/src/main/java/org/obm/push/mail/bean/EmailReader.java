/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.mail.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.input.ReaderInputStream;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class EmailReader extends Reader {

	private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
	
	private final Reader reader;
	private final Charset charset;
	private final AtomicBoolean toInputStreamLock;
	private final AtomicBoolean readerActionLock;
	
	public EmailReader(Reader reader, Charset charset) {
		this.reader = new BufferedReader(reader);
		this.charset = charset;
		this.toInputStreamLock = new AtomicBoolean(false);
		this.readerActionLock = new AtomicBoolean(false);
	}
	
	public EmailReader(Reader reader) {
		this(reader, DEFAULT_CHARSET);
	}

	public EmailReader(InputStreamReader inputStreamReader) {
		this(inputStreamReader, Charset.forName(inputStreamReader.getEncoding()));
	}

	public EmailReader(InputStream inputStream) {
		this(new InputStreamReader(inputStream, DEFAULT_CHARSET));
	}

	public Charset charset() {
		return charset;
	}

	public ReaderInputStream toInputStream() {
		Preconditions.checkState(!toInputStreamLock.getAndSet(true) && !readerActionLock.getAndSet(true),
				"Cannot call toInputStream when any action have been done");
		return new ReaderInputStream(reader, charset.name());
	}

	private void checkCanAccessUnderlayingReader() {
		Preconditions.checkState(!readerActionLock.get(),
				"Cannot do action on the reader when toInputStream has been called");
	}

	private void checkAndBlockAccessUnderlayingReader() {
		checkCanAccessUnderlayingReader();
		toInputStreamLock.set(true);
	}

	@Override
	public int read(CharBuffer target) throws IOException {
		checkAndBlockAccessUnderlayingReader();
		return reader.read(target);
	}

	@Override
	public int read() throws IOException {
		checkAndBlockAccessUnderlayingReader();
		return reader.read();
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		checkAndBlockAccessUnderlayingReader();
		return reader.read(cbuf);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		checkAndBlockAccessUnderlayingReader();
		return reader.read(cbuf, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		checkAndBlockAccessUnderlayingReader();
		return reader.skip(n);
	}

	@Override
	public boolean ready() throws IOException {
		checkCanAccessUnderlayingReader();
		return reader.ready();
	}

	@Override
	public boolean markSupported() {
		checkCanAccessUnderlayingReader();
		return reader.markSupported();
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		checkCanAccessUnderlayingReader();
		reader.mark(readAheadLimit);
	}

	@Override
	public void reset() throws IOException {
		checkCanAccessUnderlayingReader();
		reader.reset();
	}

	@Override
	public void close() throws IOException {
		checkAndBlockAccessUnderlayingReader();
		reader.close();
	}

	@Override
	public boolean equals(Object obj) {
		return reader.equals(obj);
	}

	@Override
	public String toString() {
		return reader.toString();
	}

	@Override
	public int hashCode() {
		return reader.hashCode();
	}
}
