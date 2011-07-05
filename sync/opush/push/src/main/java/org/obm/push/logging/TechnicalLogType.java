package org.obm.push.logging;

import java.util.TreeMap;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import com.google.common.collect.Maps;

public enum TechnicalLogType {
	HTTP_REQUEST("HttpRequest"),
	ACTIVE_SYNC_REQUEST("ActiveSyncXmlRequest"),
	ACTIVE_SYNC_REQUEST_HEADERS("ActiveSyncRequestHeaders"),
	ACTIVE_SYNC_REQUEST_INFO("ActiveSyncRequestInfo"),
	ACTIVE_SYNC_RESPONSE("ActiveSyncXmlResponse"),
	ACTIVE_SYNC_RESPONSE_HEADERS("ActiveSyncResponseHeaders");

	private final Marker marker;
	private final String markerName;

	private TechnicalLogType(String markerName) {
		this.marker = MarkerFactory.getMarker(markerName);
		this.markerName = markerName;
	}

	public Marker getMarker(){
		return this.marker;
	}

	public String getMarkerName(){
		return this.markerName;
	}

	public enum Index {
		INSTANCE;

		private TreeMap<String, TechnicalLogType> index;

		private Index() {
			index = Maps.newTreeMap();
			for (TechnicalLogType logType: TechnicalLogType.values()) {
				index.put(logType.getMarkerName(), logType);
			}
		}

		public TechnicalLogType searchByName(String name) {
			return index.get(name);
		}
	}
}
