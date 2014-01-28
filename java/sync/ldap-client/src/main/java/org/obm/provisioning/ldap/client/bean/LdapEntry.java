/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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
package org.obm.provisioning.ldap.client.bean;

import java.util.List;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class LdapEntry {

	public static class Builder {
		private Dn dn;
		private final ImmutableList.Builder<Attribute<?>> attributes;

		private Builder() {
			attributes = ImmutableList.builder();
		}

		public Builder dn(Dn dn) {
			this.dn = dn;
			return this;
		}

		public Builder attribute(Attribute<?> attribute) {
			attributes.add(attribute);
			return this;
		}

		public LdapEntry build() {
			Preconditions.checkState(dn != null, "dn should not be null");

			return new LdapEntry(dn, attributes.build());
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final Dn dn;
	private final ImmutableList<Attribute<?>> attributes;

	private LdapEntry(Dn dn, ImmutableList<Attribute<?>> attributes) {
		this.dn = dn;
		this.attributes = attributes;
	}

	public DefaultEntry toDefaultEntry() throws LdapException {
		return new DefaultEntry(dn.get(), formatAttributes());
	}

	private Object[] formatAttributes() {
		List<String> formattedAttributes = Lists.newArrayList();
		for (Attribute<?> attribute : attributes) {
			if (attribute.isNull()) {
				continue;
			}
			formattedAttributes.add(String.format("%s: %s",
					attribute.getName(), attribute.getValueString()));
		}
		return formattedAttributes.toArray(new Object[0]);
	}
}
