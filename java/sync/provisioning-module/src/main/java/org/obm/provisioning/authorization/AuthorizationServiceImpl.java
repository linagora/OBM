package org.obm.provisioning.authorization;

import java.util.Collection;

import org.obm.domain.dao.DomainDao;
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
	private PermissionDao permissionDao;
	private DomainDao domainDao;
	
	@Inject
	private AuthorizationServiceImpl(ProfileDao profileDao, PermissionDao roleDao, DomainDao domainDao) {
		this.profileDao = profileDao;
		this.permissionDao = roleDao;
		this.domainDao = domainDao;
	}
	
	@Override
	public Collection<String> getPermissions(String login, String domainName) throws AuthorizationException {
		ObmDomain domain = null;
		try {
			domain = domainDao.findDomainByName(domainName);
			ProfileName profile = profileDao.getProfileForUser(login, domain.getUuid());
			return permissionDao.getPermissionsForProfile(profile, domain);
		} catch (DaoException e) {
			throw new AuthorizationException(login, domain, true, e);
		} catch (UserNotFoundException e) {
			throw new AuthorizationException(login, domain, false, e);
		} catch (PermissionsNotFoundException e) {
			throw new AuthorizationException(login, domain, false, e);
		}
	}
}
