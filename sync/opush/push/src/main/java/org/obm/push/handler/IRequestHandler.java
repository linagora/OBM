package org.obm.push.handler;

import java.io.IOException;

import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.request.ActiveSyncRequest;

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
