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
package org.obm.provisioning.dao;

import java.util.Collection;

import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class PermissionDaoHardcodedImpl implements PermissionDao {

	private enum ProfilePermissionsTemplates {
		
		ADMIN("admin", ImmutableList.of("*:*")),
		DELEGATE_ADMIN("admin_delegue", ImmutableList.of("batches:read,create,delete", "users:*", "groups:*", "profiles:*")),
		EDITOR("editor", ImmutableList.of("users:read", "groups:read", "profiles:*")),
		USER("user", ImmutableList.of("users:read", "groups:read", "profiles:*"));
		
		private String profile;
		private Collection<String> permissionsTemplate;
		
		ProfilePermissionsTemplates(String profile, Collection<String> permissionsTemplate) {
			this.profile = profile;
			this.permissionsTemplate = permissionsTemplate;
		}
		
		Collection<String> getPermissionsTemplate() {
			return permissionsTemplate;
		}
		
		String getProfile() {
			return profile;
		}
		
	    public static ProfilePermissionsTemplates get(String profile) {
	        for(ProfilePermissionsTemplates permissionsTemplates : values()) {
	            if(permissionsTemplates.getProfile().equals(profile)) {
	            		return permissionsTemplates;
	            }
	        }
	        return null;
	    }
	}
	
	private String domainPermission(ObmDomain domain) {
		if (domain.isGlobal() == true) {
			return "*";
		} else {
			return domain.getUuid().get();
		}
	}
	
	@Override
	public Collection<String> getPermissionsForProfile(ProfileName profile, ObmDomain domain) throws DaoException, PermissionsNotFoundException {
		
		ProfilePermissionsTemplates ppt = ProfilePermissionsTemplates.get(profile.getName());
		if (ppt != null) {
			
			Collection<String> permissionsTemplate = ppt.getPermissionsTemplate();
			Collection<String> permissions = Lists.newArrayListWithCapacity(ppt.getPermissionsTemplate().size());
			
			for (String permissionTemplate : permissionsTemplate) {
				permissions.add(String.format("%s:%s", domainPermission(domain), permissionTemplate));
			}
			return permissions;
		}
		throw new PermissionsNotFoundException(profile, domain);
	}
}
