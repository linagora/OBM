/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.mail.FolderClosedException;
import javax.net.SocketFactory;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Assert;

import com.google.common.io.ByteStreams;

public class MailTestsUtils {
	
	public static void assertThatIsJavaSocketTimeoutException(Exception e) {
		FolderClosedException hasTimeoutException = 
				getThrowableInCauseOrNull(e, FolderClosedException.class);
		Assertions.assertThat(hasTimeoutException).hasMessageContaining(SocketTimeoutException.class.getName());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T getThrowableInCauseOrNull(Throwable from, Class<T> seekingCause) {
		if (from.getClass().equals(seekingCause)) {
			return (T) from; // Cast unchecked
		} else if (from.getCause() != null){
			return getThrowableInCauseOrNull(from.getCause(), seekingCause);
		} else {
			return null;
		}
	}
	
	public static InputStream loadEmail(String name) throws IOException {
		return new ByteArrayInputStream(ByteStreams.toByteArray(ClassLoader.getSystemResourceAsStream("eml/" + name)));
	}
	
	public static void waitForGreenmailAvailability(String imapHost, int imapPort) throws InterruptedException {
		SocketFactory socketFactory = SocketFactory.getDefault();
		DateTime end = new DateTime().plusSeconds(30);
		
		while (true) {
			try {
				Socket socket = socketFactory.createSocket(imapHost, imapPort);
				socket.close();
				return;
			} catch (IOException e) {
				if (new DateTime().isAfter(end)) {
					Assert.fail("greenmail is not reachable");
				}
				Thread.sleep(50);
			}
		}
	}
}
