package org.obm.provisioning.dao;

import java.util.Collection;

import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class PermissionDaoHardcodedImpl implements PermissionDao {

	private enum ProfileToPermissionsMapping {
		
		ADMIN("admin", ImmutableList.of("*:*")),
		DELEGATE_ADMIN("admin_delegue", ImmutableList.of("batches:read,create,delete", "users:*", "groups:*", "profiles:*")),
		EDITOR("editor", ImmutableList.of("users:read", "groups:read", "profiles:*")),
		USER("user", ImmutableList.of("users:read", "groups:read", "profiles:*"));
		
		private String profile;
		private Collection<String> permissions;
		
		ProfileToPermissionsMapping(String profile, Collection<String> roles) {
			this.profile = profile;
			this.permissions = roles;
		}
		
		Collection<String> getPermissions() {
			return permissions;
		}
		
		String getProfile() {
			return profile;
		}
		
	    public static ProfileToPermissionsMapping get(String profile) { 
	        for(ProfileToPermissionsMapping p2p : values()) {
	            if(p2p.getProfile().equals(profile)) return p2p;
	        }
	        return null;
	    }
	}
	
	@Override
	public Collection<String> getPermissionsForProfile(ProfileName profile, ObmDomain domain) throws DaoException, PermissionsNotFoundException {

		ProfileToPermissionsMapping p2r = ProfileToPermissionsMapping.get(profile.getName());
		if (p2r != null) {
			return p2r.getPermissions();
		}
		throw new PermissionsNotFoundException(profile, domain);
	}

}
