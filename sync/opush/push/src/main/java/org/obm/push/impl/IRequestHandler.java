package org.obm.push.impl;

import java.io.IOException;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContinuation;

/**
 * Interface to handle client ActiveSync requests
 * 
 * @author tom
 * 
 */
public interface IRequestHandler {

	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException;

}
