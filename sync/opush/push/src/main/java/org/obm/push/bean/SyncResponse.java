package org.obm.push.bean;

import java.util.Collection;
import java.util.Map;

import org.obm.push.backend.BackendSession;
import org.obm.push.data.EncoderFactory;
import org.obm.push.store.SyncCollection;

public class SyncResponse {

	private final Collection<SyncCollection> changedFolders;
	private final BackendSession backendSession;
	private final EncoderFactory encoderFactory;
	private final Map<String, String> processedClientIds;

	public SyncResponse(Collection<SyncCollection> changedFolders, BackendSession bs, EncoderFactory encoderFactory, Map<String, String> processedClientIds) {
		this.changedFolders = changedFolders;
		this.backendSession = bs;
		this.encoderFactory = encoderFactory;
		this.processedClientIds = processedClientIds;
	}
	
	public SyncResponse() {
		this(null, null, null, null);
	}

	public Collection<SyncCollection> listChangedFolders() {
		return changedFolders;
	}
	
	public BackendSession getBackendSession() {
		return backendSession;
	}
	
	public EncoderFactory getEncoderFactory() {
		return encoderFactory;
	}
	
	public Map<String, String> listProcessedClientIds() {
		return processedClientIds;
	}
	
}
