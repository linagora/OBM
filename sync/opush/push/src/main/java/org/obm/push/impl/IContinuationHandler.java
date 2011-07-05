package org.obm.push.impl;

import java.util.Collection;
import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;
import org.obm.push.store.SyncCollection;

public interface IContinuationHandler {
	void sendResponse(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders, boolean sendHierarchyChanges, IContinuation continuation);
	
	void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders, IContinuation continuation);
	
	void sendError(Responder responder,
			Set<SyncCollection> changedFolders, String errorStatus, IContinuation continuation);
}
