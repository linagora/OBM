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
 * and its applicable Additional Terms for OBM along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to
 * OBM connectors.
 *
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.bean;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;


public enum Permissions {
	BATCHES_READ("batches:read"),
	BATCHES_CREATE("batches:create"),
	BATCHES_UPDATE("batches:update"),
	BATCHES_PATCH("batches:patch"),
	BATCHES_DELETE("batches:delete"),
	USERS_READ("users:read"),
	USERS_CREATE("users:create"),
	USERS_UPDATE("users:update"),
	USERS_PATCH("users:patch"),
	USERS_DELETE("users:delete"),
	GROUPS_READ("groups:read"),
	GROUPS_CREATE("groups:create"),
	GROUPS_UPDATE("groups:update"),
	GROUPS_PATCH("groups:patch"),
	GROUPS_DELETE("groups:delete"),
	PROFILES_READ("profiles:read"),
	PROFILES_CREATE("profiles:create"),
	PROFILES_UPDATE("profiles:update"),
	PROFILES_PATCH("profiles:patch"),
	PROFILES_DELETE("profiles:delete");
	
	private final String specificationValue;

	private Permissions(String asSpecificationValue) {
		this.specificationValue = asSpecificationValue;
	}
	
	public String asSpecificationValue() {
		return specificationValue;
	}
	
	public static Permissions fromSpecificationValue(String specificationValue) {
		if (specValueToEnum.containsKey(specificationValue)) {
			return specValueToEnum.get(specificationValue);
		}
		throw new IllegalArgumentException("No Permissions for '" + specificationValue + "'");
	}

	private static Map<String, Permissions> specValueToEnum;
	
	static {
		Builder<String, Permissions> builder = ImmutableMap.builder();
		for (Permissions permission : values()) {
			builder.put(permission.specificationValue, permission);
		}
		specValueToEnum = builder.build();
	}

	public static final String batches_read = "batches:read";
	public static final String batches_create = "batches:create";
	public static final String batches_update = "batches:update";
	public static final String batches_patch = "batches:patch";
	public static final String batches_delete = "batches:delete";
	
	public static final String users_read = "users:read";
	public static final String users_create = "users:create";
	public static final String users_update = "users:update";
	public static final String users_patch = "users:patch";
	public static final String users_delete = "users:delete";
	
	public static final String groups_read = "groups:read";
	public static final String groups_create = "groups:create";
	public static final String groups_update = "groups:update";
	public static final String groups_patch = "groups:patch";
	public static final String groups_delete = "groups:delete";
	
	public static final String profiles_read = "profiles:read";
	public static final String profiles_create = "profiles:create";
	public static final String profiles_update = "profiles:update";
	public static final String profiles_patch = "profiles:patch";
	public static final String profiles_delete = "profiles:delete";
}
