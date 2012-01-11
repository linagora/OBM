package org.obm.push.service;

import java.util.LinkedList;
import java.util.Set;

import org.obm.push.IContentsExporter;
import org.obm.push.backend.ICollectionChangeListener;

public interface PushPublishAndSubscribe {

	public interface Factory {
		PushPublishAndSubscribe create(IContentsExporter contentsExporter);
	}
	
	LinkedList<PushNotification> listPushNotification(
			Set<ICollectionChangeListener> ccls);

	void emit(Set<ICollectionChangeListener> ccls);

}