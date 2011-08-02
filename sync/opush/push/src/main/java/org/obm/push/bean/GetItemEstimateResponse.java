package org.obm.push.bean;

import java.util.Collection;

public class GetItemEstimateResponse {

	public static class Estimate {
		
		private final SyncCollection collection;
		private final int estimate;

		public Estimate(SyncCollection collection, int estimate) {
			this.collection = collection;
			this.estimate = estimate;
		}
		
		public SyncCollection getCollection() {
			return collection;
		}
		
		public int getEstimate() {
			return estimate;
		}
	}
	
	private final Collection<Estimate> estimates;
	
	public GetItemEstimateResponse(Collection<Estimate> estimates) {
		this.estimates = estimates;
	}
	
	public Collection<Estimate> getEstimates() {
		return estimates;
	}
}
