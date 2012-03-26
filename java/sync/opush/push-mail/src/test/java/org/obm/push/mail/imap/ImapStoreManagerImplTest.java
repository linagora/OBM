/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.mail.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.opush.mail.StreamMailTestsUtils;

public class ImapStoreManagerImplTest {

	private ManagedLifecycleImapStore managedLifecycleImapStore;
	private ImapStoreManagerImpl imapStoreManager;

	@Before
	public void setUp() {
		managedLifecycleImapStore = EasyMock.createStrictMock(ManagedLifecycleImapStore.class);
		imapStoreManager = new ImapStoreManagerImpl();
		imapStoreManager.setImapStore(managedLifecycleImapStore);
	}
	
	@Test
	public void testConstruction() {
		expectClosableIsNotCalled();
		Assertions.assertThat(imapStoreManager.getImapStore()).isNotNull();
		Assertions.assertThat(imapStoreManager.getStreams()).isNotNull().isEmpty();
		Assertions.assertThat(imapStoreManager.isCloseRequired()).isFalse();
	}

	@Test
	public void testCloseWhenDoneTriggersCloseRequired() {
		imapStoreManager.closeWhenDone();

		Assertions.assertThat(imapStoreManager.isCloseRequired()).isTrue();
	}

	@Test
	public void testBindToAddAStream() {
		InputStream bindStream = imapStoreManager.bindTo(outboundStream());

		Assertions.assertThat(imapStoreManager.getStreams()).containsOnly(bindStream);
	}
	
	@Test
	public void testCloseAddedStreamRemoveIt() throws IOException {
		InputStream bindStream = imapStoreManager.bindTo(outboundStream());
		bindStream.close();
		
		Assertions.assertThat(imapStoreManager.getStreams()).isEmpty();
	}
	
	@Test
	public void testCloseIsntCalledWhenCloseWhenDoneButStreamsExists() {
		expectClosableIsNotCalled();
		
		imapStoreManager.bindTo(outboundStream());
		imapStoreManager.closeWhenDone();

		verify();
	}
	
	@Test
	public void testCloseIsntCalledIfNoStreamExistButCloseWhenDoneNotCalled() throws IOException {
		expectClosableIsNotCalled();
		
		InputStream bindStream = imapStoreManager.bindTo(outboundStream());
		bindStream.close();
		
		verify();
	}
	
	@Test
	public void testCloseIsCalledIfNoStreamExistAndCloseWhenDone() throws IOException {
		expectClosableIsCalled();
		
		InputStream bindStream = imapStoreManager.bindTo(outboundStream());
		imapStoreManager.closeWhenDone();
		bindStream.close();

		verify();
	}
	
	@Test
	public void testConcurrencyWhenAddingAndClosingStreams() throws InterruptedException, ExecutionException {
		expectClosableIsCalled();

		ThreadPoolExecutor threadPoolExecutor = 
				new ThreadPoolExecutor(20, 20, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

		List<Future<InputStream>> futuresBind = new ArrayList<Future<InputStream>>();
		for (int i = 0; i < 10000; ++i) {
			 futuresBind.add(newBindStreamAction(threadPoolExecutor));	
		}
		
		for (Future<InputStream> f: futuresBind) {
			InputStream bindStream = f.get();
			scheduleCloseBindStream(threadPoolExecutor, bindStream);
		}
		threadPoolExecutor.awaitTermination(2, TimeUnit.SECONDS);
		imapStoreManager.closeWhenDone();
		
		Assertions.assertThat(threadPoolExecutor.getQueue()).isEmpty();
		Assertions.assertThat(imapStoreManager.getStreams()).isEmpty();
		verify();
	}
	
	private void scheduleCloseBindStream(ThreadPoolExecutor threadPoolExecutor, final InputStream bindStream) {
		threadPoolExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					bindStream.close();
				} catch (IOException e) {
					// Memory stream, no fail will happen
				}
			}
		});
	}

	private Future<InputStream> newBindStreamAction(ThreadPoolExecutor threadPoolExecutor) {
		return threadPoolExecutor.submit(new Callable<InputStream>() {

			@Override
			public InputStream call() throws Exception {
				return imapStoreManager.bindTo(outboundStream());
			}
		});
	}

	private InputStream outboundStream() {
		return StreamMailTestsUtils.newInputStreamFromString("data");
	}

	private void expectClosableIsCalled() {
		managedLifecycleImapStore.close();
		EasyMock.expectLastCall().times(1);
		replay();
	}
	
	private void expectClosableIsNotCalled() {
		replay();
	}

	private void replay() {
		EasyMock.replay(managedLifecycleImapStore);
	}
	
	private void verify() {
		EasyMock.verify(managedLifecycleImapStore);
	}
}
