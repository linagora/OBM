/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
 * and its applicable Additional Terms for OBM aint with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.bean;

import com.google.common.base.Preconditions;

public class ASTimeZone {

	private final int bias;
	private final String standardName;
	private final ASSystemTime standardDate;
	private final int standardBias;
	private final String dayLightName;
	private final ASSystemTime dayLightDate;
	private final int dayLightBias;

	public static class Builder {

		private Integer bias;
		private String standardName;
		private ASSystemTime standardDate;
		private Integer standardBias;
		private String dayLightName;
		private ASSystemTime dayLightDate;
		private Integer dayLightBias;

		public Builder bias(int bias) {
			this.bias = bias;
			return this;
		}
		
		public Builder standardName(String standardName) {
			this.standardName = standardName;
			return this;
		}
		
		public Builder standardDate(ASSystemTime standardDate) {
			this.standardDate = standardDate;
			return this;
		}
		
		public Builder standardBias(int standardBias) {
			this.standardBias = standardBias;
			return this;
		}
		
		public Builder dayLightName(String dayLightName) {
			this.dayLightName = dayLightName;
			return this;
		}
		
		public Builder dayLightDate(ASSystemTime dayLightDate) {
			this.dayLightDate = dayLightDate;
			return this;
		}
		
		public Builder dayLightBias(int dayLightBias) {
			this.dayLightBias = dayLightBias;
			return this;
		}
		
		public ASTimeZone build() {
			Preconditions.checkNotNull(bias);
			Preconditions.checkNotNull(standardBias);
			Preconditions.checkNotNull(standardDate);
			Preconditions.checkNotNull(dayLightBias);
			Preconditions.checkNotNull(dayLightDate);
			
			return new ASTimeZone(bias,
					standardName, standardDate, standardBias,
					dayLightName, dayLightDate, dayLightBias);
		}
	}
	
	private ASTimeZone(int bias,
			String standardName, ASSystemTime standardDate, int standardBias,
			String dayLightName, ASSystemTime dayLightDate, int dayLightBias) {
		
		this.bias = bias;
		this.standardName = standardName;
		this.standardDate = standardDate;
		this.standardBias = standardBias;
		this.dayLightName = dayLightName;
		this.dayLightDate = dayLightDate;
		this.dayLightBias = dayLightBias;
	}

	public int getBias() {
		return bias;
	}

	public String getStandardName() {
		return standardName;
	}

	public ASSystemTime getStandardDate() {
		return standardDate;
	}

	public int getStandardBias() {
		return standardBias;
	}

	public String getDayLightName() {
		return dayLightName;
	}

	public ASSystemTime getDayLightDate() {
		return dayLightDate;
	}

	public int getDayLightBias() {
		return dayLightBias;
	}

}
