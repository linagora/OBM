package org.obm.push.impl;

import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IListenerRegistration;

public class ListenerRegistration implements IListenerRegistration {

	private ICollectionChangeListener ccl;
	private Set<ICollectionChangeListener> registered;

	public ListenerRegistration(ICollectionChangeListener ccl,
			Set<ICollectionChangeListener> registered) {
		this.ccl = ccl;
		this.registered = registered;
	}

	@Override
	public void cancel() {
		synchronized (registered) {
			registered.remove(ccl);
		}
	}

}
