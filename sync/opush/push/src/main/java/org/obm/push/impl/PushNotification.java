package org.obm.push.impl;

import org.obm.push.backend.ICollectionChangeListener;

public class PushNotification {

	//private Set<SyncCollection> changedCollections;
	private ICollectionChangeListener listener;

	public PushNotification(/*Set<SyncCollection> changedCollections,*/
			ICollectionChangeListener listener) {
		super();
		//this.changedCollections = changedCollections;
		this.listener = listener;
	}

	public void emit() {
		listener.changesDetected();
	}

}
