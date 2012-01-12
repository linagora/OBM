package org.obm.push.handler;

import java.io.IOException;

import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.request.ActiveSyncRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AutodiscoverHandler implements IRequestHandler {

	@Inject
	private AutodiscoverHandler() { }

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {
		// TODO Auto-generated method stub
	}

}
