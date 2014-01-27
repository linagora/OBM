/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.store.ehcache;

import java.util.Map;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public interface EhCacheConfiguration {

	static final int STATS_SAMPLING_IN_SECONDS = 1; 
	
	/**
	 * @return Global ehcache heap memory allowed
	 */
	int maxMemoryInMB();
	
	/**
	 * @return percentage related to the global heap memory allowed for the given cache
	 */
	Percentage percentageAllowedToCache(String cacheName);

	/**
	 * @return percentage related to the global heap memory allowed for each caches
	 */
	Map<String, Percentage> percentageAllowedToCaches();

	long timeToLiveInSeconds();
	TransactionalMode transactionalMode();
	
	/**
	 * @return maximum history sample size to record
	 */
	int statsSampleToRecordCount();

	/**
	 * @return number of seconds taken by the short statistics sample
	 */
	int statsShortSamplingTimeInSeconds();

	/**
	 * @return number of seconds taken by the medium statistics sample
	 */
	int statsMediumSamplingTimeInSeconds();
	
	/**
	 * @return number of seconds taken by the long statistics sample
	 */
	int statsLongSamplingTimeInSeconds();

	/**
	 * @return number of minutes until statistics sampling stop
	 */
	int statsSamplingTimeStopInMinutes();
	
	public static class Percentage {

		public static final Percentage UNDEFINED = new Percentage(null);
		public static final Percentage of(int percentage) {
			Preconditions.checkArgument(percentage >= 0, "must be positive");
			Preconditions.checkArgument(percentage <= 100, "must between 0 and 100");
			return new Percentage(percentage);
		}
		
		private final Integer percentage;
		
		private Percentage(Integer percent) {
			this.percentage = percent;
		}
		
		public boolean isDefined() {
			return percentage != null;
		}

		public String get() {
			Preconditions.checkState(isDefined(), "cannot call get() on undefined percentage");
			return String.valueOf(percentage) + "%";
		}

		public int getIntValue() {
			Preconditions.checkState(isDefined(), "cannot call get() on undefined percentage");
			return percentage;
		}
		
		public int applyTo(int to) {
			Preconditions.checkState(isDefined(), "undefined percentage");
			Preconditions.checkArgument(to >= 0, "must be a positive value");
			return Math.round((to / 100f) * percentage);
		}

		
		@Override
		public final int hashCode(){
			return Objects.hashCode(percentage);
		}
		
		@Override
		public final boolean equals(Object object){
			if (object instanceof Percentage) {
				Percentage that = (Percentage) object;
				return Objects.equal(this.percentage, that.percentage);
			}
			return false;
		}

		@Override
		public String toString() {
			return get();
		}
	}
}
