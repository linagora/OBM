package org.obm.push.impl;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;

public interface IContinuationHandler {
	void sendResponse(BackendSession bs, Responder responder,
			boolean sendHierarchyChanges, IContinuation continuation);
	
	void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder,
			IContinuation continuation);
	
	void sendError(Responder responder, String errorStatus, IContinuation continuation);
}
