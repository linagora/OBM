package org.obm.provisioning.authorization;

import java.util.Collection;

import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.PermissionDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class AuthorizationServiceImpl implements AuthorizationService {

	private ProfileDao profileDao;
	private PermissionDao roleDao;
	
	@Inject
	private AuthorizationServiceImpl(ProfileDao profileDao, PermissionDao roleDao) {
		this.profileDao = profileDao;
		this.roleDao = roleDao;
	}
	
	@Override
	public Collection<String> getPermissions(String login, ObmDomain domain) throws AuthorizationException {
		
		try {
			ProfileName profile = profileDao.getProfileForUser(login, domain.getUuid());
			return roleDao.getPermissionsForProfile(profile, domain);
		} catch (DaoException e) {
			throw new AuthorizationException(login, domain, true, e);
		} catch (UserNotFoundException e) {
			throw new AuthorizationException(login, domain, false, e);
		} catch (PermissionsNotFoundException e) {
			throw new AuthorizationException(login, domain, false, e);
		}
	}
}
