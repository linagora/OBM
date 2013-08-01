/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.processing.impl.users;

import java.util.Arrays;
import java.util.Set;

import org.obm.annotations.transactional.Transactional;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.EntityRightDao;
import org.obm.domain.dao.UserDao;
import org.obm.provisioning.Group;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.push.mail.bean.Acl;
import org.obm.sync.Right;
import org.obm.utils.ObmHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.profile.CheckBoxState;
import fr.aliacom.obm.common.profile.Module;
import fr.aliacom.obm.common.profile.ModuleCheckBoxStates;
import fr.aliacom.obm.common.profile.Profile;
import fr.aliacom.obm.common.user.ObmUser;

public class CreateUserOperationProcessor extends AbstractUserOperationProcessor {

	private final String ANYONE_IDENTIFIER = "anyone";

	@Inject
	private GroupDao groupDao;
	@Inject
	private ProfileDao profileDao;
	@Inject
	private EntityRightDao entityRightDao;
	@Inject
	private ObmHelper obmHelper;

	@Inject
	CreateUserOperationProcessor() {
		super(HttpVerb.POST);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		ObmUser user = getUserFromRequestBody(operation, batch);
		ObmUser userFromDao = createUserInDao(user);

		addUserInDefaultGroup(userFromDao);
		setDefaultUserRights(userFromDao);

		if (user.isEmailAvailable()) {
			createUserMailboxes(userFromDao);
		}

		createUserInLdap(userFromDao);
	}

	private void setDefaultUserRights(ObmUser user) {
		Profile profile = null;

		try {
			profile = profileDao.getUserProfile(user);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot fetch user profile %s from the database.", user.getProfileName()), e);
		}

		setDefaultUserRightsOnModule(user, profile, Module.CALENDAR, "Calendar");
		setDefaultUserRightsOnModule(user, profile, Module.MAILBOX, "Mailbox");
	}

	private void setDefaultUserRightsOnModule(ObmUser user, Profile profile, Module module, String entityType) {
		try {
			Integer entityId = obmHelper.fetchEntityId(entityType, user.getUid());
			Set<Right> defaultRights = computeRightsFromDefaultCheckBoxStates(profile.getDefaultCheckBoxStates().get(module));

			entityRightDao.grantRights(entityId, null, defaultRights);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot set default user rights on module %s for user %s.", module, user.getLogin()), e);
		}
	}

	private Set<Right> computeRightsFromDefaultCheckBoxStates(final ModuleCheckBoxStates states) {
		return FluentIterable
				.from(Arrays.asList(Right.values()))
				.filter(new Predicate<Right>() {

					@Override
					public boolean apply(Right input) {
						return isRightEnabled(states, input);
					}

				}).toSet();
	}

	private boolean isRightEnabled(ModuleCheckBoxStates states, Right right) {
		CheckBoxState state = states.getCheckBoxState(right);

		return CheckBoxState.CHECKED.equals(state) || CheckBoxState.DISABLED_CHECKED.equals(state);
	}

	private void createUserMailboxes(ObmUser user) {
		CyrusManager cyrusManager = null;
		
		try {
			cyrusManager = buildCyrusManager(user);
			cyrusManager.create(user);
			cyrusManager.applyQuota(user);
			cyrusManager.setAcl(
					user,
					ANYONE_IDENTIFIER,
					Acl.builder()
						.user(user.getLogin())
						.rights(Acl.Rights.Post.asSpecificationValue())
						.build());
		} catch (Exception e) {
			throw new ProcessingException(
					String.format(
							"Cannot create cyrus mailbox for user '%s' (%s).",
							user.getLogin(), user.getExtId()), e);
		} finally {
			if (cyrusManager != null) {
				cyrusManager.shutdown();
			}
		}
	}

	private ObmUser createUserInDao(ObmUser user) {
		try {
			return userDao.create(user);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot insert new user '%s' (%s) in database.", user.getLogin(), user.getExtId()), e);
		}
	}

	private void addUserInDefaultGroup(ObmUser user) {
		ObmDomain domain = user.getDomain();

		try {
			Group defaultGroup = groupDao.getByGid(domain, UserDao.DEFAULT_GID);

			if (defaultGroup == null) {
				throw new ProcessingException(String.format("Default group with GID %s not found for domain %s.", UserDao.DEFAULT_GID, domain.getName()));
			}

			groupDao.addUser(domain, defaultGroup.getUid(), user);
		}
		catch (DaoException e) {
			throw new ProcessingException(String.format("Cannot add user '%s' (%s) in the default group.", user.getLogin(), user.getExtId()), e);
		}
	}

	private void createUserInLdap(ObmUser user) {
		LdapManager ldapManager = buildLdapManager(user);

		try {
			ldapManager.createUser(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot insert new user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

}
