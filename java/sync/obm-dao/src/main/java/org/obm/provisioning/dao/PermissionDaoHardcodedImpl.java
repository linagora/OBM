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
