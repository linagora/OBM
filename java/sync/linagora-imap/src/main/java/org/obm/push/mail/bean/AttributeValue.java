/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
package org.obm.push.mail.bean;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class AttributeValue {

	public static AttributeValue privateValue(String value) {
		return builder().attributeSuffix(AttributeSuffix.PRIVATE).value(value).build();
	}

	public static AttributeValue sharedValue(String value) {
		return builder().attributeSuffix(AttributeSuffix.SHARED).value(value).build();
	}
	
	@VisibleForTesting static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private AttributeSuffix attributeSuffix;
		private String value;
		
		private Builder() {
		}
		
		public Builder attributeSuffix(AttributeSuffix attributeSuffix) {
			Preconditions.checkNotNull(attributeSuffix);
			this.attributeSuffix = attributeSuffix;
			return this;
		}
		
		public Builder value(String value) {
			Preconditions.checkNotNull(value);
			this.value = value;
			return this;
		}
		
		public AttributeValue build() {
			Preconditions.checkState(attributeSuffix != null);
			Preconditions.checkState(value != null);
			return new AttributeValue(attributeSuffix, value);
		}
	}

	@VisibleForTesting enum AttributeSuffix {
		
		PRIVATE("priv"),
		SHARED("shared");
		
		private static final String VALUE = "value";
		private final String suffix;
		
		AttributeSuffix(String suffix) {
			this.suffix = suffix;
		}
		
		public String serialize() {
			return "\"" + VALUE + "." + suffix + "\""; 
		}
	}

	private final AttributeSuffix attributeSuffix;
	private final String value;

	private AttributeValue(AttributeSuffix attributeSuffix, String value) {
		this.attributeSuffix = attributeSuffix;
		this.value = value;
	}
	
	public AttributeSuffix getAttributeSuffix() {
		return attributeSuffix;
	}

	public String getValue() {
		return value;
	}
	
	public String serialize() {
		return Joiner.on(" ").join(attributeSuffix.serialize(), serializeValue());
	}
	
	private String serializeValue() {
		return "\"" + value + "\"";
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(attributeSuffix, value);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof AttributeValue) {
			AttributeValue that = (AttributeValue) object;
			return Objects.equal(this.attributeSuffix, that.attributeSuffix)
				&& Objects.equal(this.value, that.value);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("attributeSuffix", attributeSuffix)
			.add("value", value)
			.toString();
	}
}
