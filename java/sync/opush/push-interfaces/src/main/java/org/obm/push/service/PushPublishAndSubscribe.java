package org.obm.push.service;

import java.util.LinkedList;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.PIMBackend;

public interface PushPublishAndSubscribe {

	public interface Factory {
		PushPublishAndSubscribe create(PIMBackend backend);
	}
	
	LinkedList<PushNotification> listPushNotification(
			Set<ICollectionChangeListener> ccls);

	void emit(Set<ICollectionChangeListener> ccls);

}