package org.obm.push.monitor;

import java.util.LinkedList;
import java.util.Set;

import org.minig.imap.IMAPException;
import org.minig.imap.IdleClient;
import org.minig.imap.idle.IIdleCallback;
import org.minig.imap.idle.IdleLine;
import org.minig.imap.idle.IdleTag;
import org.obm.locator.LocatorClientException;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.impl.PushNotification;
import org.obm.push.mail.IEmailManager;

public class EmailMonitoringThread extends OpushMonitoringThread implements IIdleCallback {
	
	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;
	protected ObmSyncBackend backend;
	protected IEmailManager emailManager;
	
	private BackendSession bs;
	private String collectionName;
	private Boolean remainConnected;  

	private IdleClient store;

	public EmailMonitoringThread(ObmSyncBackend cb,
			Set<ICollectionChangeListener> ccls, BackendSession bs,
			Integer collectionId, IEmailManager emailManager, 
			IContentsExporter contentsExporter) throws CollectionNotFoundException, DaoException {
		
		super(contentsExporter);
		
		remainConnected = false;
		this.ccls = ccls;
		this.backend = cb;
		this.emailManager = emailManager;
		this.bs = bs;
		collectionName = backend.getCollectionPathFor(collectionId);
	}

	public synchronized void startIdle() throws IMAPException, LocatorClientException {
		if (store == null) {
			store = getIdleClient(bs);
			store.login(emailManager.getActivateTLS());
			store.select(emailManager.parseMailBoxName(bs,
					collectionName));
			store.startIdle(this);
		}
		remainConnected = true;
		logger.info("Start monitoring for collection : '{}'", collectionName);
	}
	
	public synchronized void stopIdle() {
		if (store != null) {
			store.stopIdle();
			store.logout();
			store = null;
		}
		remainConnected = false;
		logger.info("Stop monitoring for collection : '{}'", collectionName);
	}

	@Override
	public synchronized void receive(IdleLine line) {
		if (line != null) {
		
			if ((IdleTag.EXISTS.equals(line.getTag()) || IdleTag.FETCH
					.equals(line.getTag()))) {
				
				stopIdle();
				emit(ccls);
			}
		}
	}

	@Override
	protected void addPushNotification(
			final LinkedList<PushNotification> pushNotifyList,
			final ICollectionChangeListener ccl) {

		this.stopIdle();
		pushNotifyList.add(new PushNotification(ccl));
	}
	
	private IdleClient getIdleClient(BackendSession bs) throws LocatorClientException {
		User user = bs.getUser();
		String loginAtdomain = user.getLoginAtDomain();
		boolean useDomain = emailManager.getLoginWithDomain();
		if (!useDomain) {
			loginAtdomain = user.getLogin();
		}
		logger.debug("Creating idleClient with login: {}, (useDomain {})", loginAtdomain, useDomain);
		IdleClient idleCli = new IdleClient(emailManager.locateImap(bs), 143, loginAtdomain, bs
				.getPassword());
		return idleCli;
	}

	@Override
	public synchronized void disconnectedCallBack() {
		if(store != null){
			try{
				stopIdle();
			} catch (Throwable e) {
				logger.error(e.getMessage(),e );
			}
		}
		if(remainConnected){
			try {
				startIdle();
			} catch (IMAPException e) {
				logger.error("SEND ERROR TO PDA",e );
				//TODO SEND ERROR TO PDA
			} catch (LocatorClientException e) {
				logger.error("SEND ERROR TO PDA",e );
				//TODO SEND ERROR TO PDA
			}	
		}
	}
	
}
