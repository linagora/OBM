/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ASTimeZone {

	private final int bias;
	private final String standardName;
	private final ASSystemTime standardDate;
	private final int standardBias;
	private final String dayLightName;
	private final ASSystemTime dayLightDate;
	private final int dayLightBias;

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private Integer bias;
		private String standardName;
		private ASSystemTime standardDate;
		private Integer standardBias;
		private String dayLightName;
		private ASSystemTime dayLightDate;
		private Integer dayLightBias;

		private Builder() {
		}
		
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
			Preconditions.checkState(bias != null);
			Preconditions.checkState(standardBias != null);
			Preconditions.checkState(standardDate != null);
			Preconditions.checkState(dayLightBias != null);
			Preconditions.checkState(dayLightDate != null);
			
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

	public boolean useDaylightTime() {
		return getDayLightBias() != 0;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(bias, standardName, standardDate, standardBias, dayLightName, dayLightDate, dayLightBias);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ASTimeZone) {
			ASTimeZone that = (ASTimeZone) object;
			return Objects.equal(this.bias, that.bias)
				&& Objects.equal(this.standardName, that.standardName)
				&& Objects.equal(this.standardDate, that.standardDate)
				&& Objects.equal(this.standardBias, that.standardBias)
				&& Objects.equal(this.dayLightName, that.dayLightName)
				&& Objects.equal(this.dayLightDate, that.dayLightDate)
				&& Objects.equal(this.dayLightBias, that.dayLightBias);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("bias", bias)
			.add("standardName", standardName)
			.add("standardDate", standardDate)
			.add("standardBias", standardBias)
			.add("dayLightName", dayLightName)
			.add("dayLightDate", dayLightDate)
			.add("dayLightBias", dayLightBias)
			.toString();
	}

	public boolean equalsDiscardingNames(ASTimeZone asTimeZone) {
		Preconditions.checkNotNull(asTimeZone, "asTimeZone is required");
		return Objects.equal(this.bias, asTimeZone.bias)
			&& Objects.equal(this.standardDate, asTimeZone.standardDate)
			&& Objects.equal(this.standardBias, asTimeZone.standardBias)
			&& Objects.equal(this.dayLightDate, asTimeZone.dayLightDate)
			&& Objects.equal(this.dayLightBias, asTimeZone.dayLightBias);
	}
}
