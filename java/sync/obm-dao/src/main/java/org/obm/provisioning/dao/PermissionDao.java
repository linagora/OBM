package org.obm.provisioning.dao;

import java.util.Collection;

import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;

import fr.aliacom.obm.common.domain.ObmDomain;

public interface PermissionDao {
	
	public Collection<String> getPermissionsForProfile(ProfileName profile, ObmDomain domain) throws DaoException, PermissionsNotFoundException;

}
