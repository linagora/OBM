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
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.bean;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EqualsVerifierUtils {

	public void test(Class<?>... classes) {
		for (Class<?> clazz: classes) {
			createEqualsVerifier(clazz).verify();
		}
	}

	public void test(ImmutableList<Class<?>> list) {
		for (Class<?> clazz: list) {
			createEqualsVerifier(clazz).verify();
		}
	}

	public static EqualsVerifier<?> createEqualsVerifier(Class<?> clazz) {
		return EqualsVerifier.forClass(clazz)
				.suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE);
	}

	public static class RedBlack<T> {
		
		private T red;
		private T black;
		
		public RedBlack(T red, T black) {
			this.red = red;
			this.black = black;
		}
	}

	public static class EqualsVerifierBuilder {
		
		public static EqualsVerifierBuilder builder() {
			return new EqualsVerifierBuilder();
		}
		
		private final Map<Class<?>, RedBlack<?>> prefabValues;
		private final List<Class<?>> classes;
		private boolean withSuperClass;
		
		private EqualsVerifierBuilder() {
			prefabValues = Maps.newHashMap();
			classes = Lists.newArrayList();
			withSuperClass = false;
		}
		
		public EqualsVerifierBuilder equalsVerifiers(Collection<Class<?>> classes) {
			this.classes.addAll(classes);
			return this;
		}
		
		public EqualsVerifierBuilder hasCharsetField() {
			prefabValues.put(Charset.class, new RedBlack<Charset>(Charsets.UTF_8, Charsets.US_ASCII));
			return this;
		}
		
		public <T> EqualsVerifierBuilder prefabValue(Class<T> clazz, T red, T black) {
			prefabValues.put(clazz, new RedBlack<T>(red, black));
			return this;
		}
		
		public EqualsVerifierBuilder withSuperClass(boolean withSuperClass) {
			this.withSuperClass = withSuperClass;
			return this;
		}
	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void verify() {
			for (Class<?> clazz : classes) {
				EqualsVerifier<?> verifier = createEqualsVerifier(clazz);
				for (Entry<Class<?>, RedBlack<?>> prefabValue : prefabValues.entrySet()) {
					RedBlack value = prefabValue.getValue();
					addPrefab(verifier, (Class) prefabValue.getKey(), value);
				}
				if (withSuperClass) {
					verifier.withRedefinedSuperclass();
				}
				verifier.verify();
			}
		}

		private <T> void addPrefab(EqualsVerifier<T> verifier, Class<T> key, RedBlack<T> value) {
			verifier.withPrefabValues(key, value.red, value.black);
		}
	}
}
