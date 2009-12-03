<?php

//
// $Id$
//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//   * Neither the name of the LINAGORA GROUP nor the names of its contributors
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

/**
 * LemonLDAP synchronization class.
 * It will all this connector to perform a search on a LDAP directory, to
 * retrieve all users and groups.
 */
class LemonLDAP_Sync {

	/**
	 * Force update instead of verify if there are available updates.
	 */
	var $_forceUpdate = false;

	/**
	 * The LemonLDAP engine.
	 */
	var $_engine = null;

	/**
	 * Constructor.
	 */
	function __construct ($engine)
	{
		$this->_engine = $engine;
		$this->initializeFromConfiguration();
	}

	/**
	 * Initiliaze internal parameters from configuration.
	 */
	function initializeFromConfiguration()
	{
		global $lemonldap_config;
		$this->_forceUpdate = $lemonldap_config['auto_update_force'];
	}

	/**
	 * Force synchronization of external repositories, such as the OBM LDAP directory,
	 * with new informations provides by LemonLDAP.
	 * User data are synchronized, like groups and user password.
	 * @param $user_name Login of the user
	 * @param $domain_id
	 * @param $user_id
	 */
	function syncExternalData ($user_name, $domain_id, $user_id)
	{
		//
		// $entities is defined into the file package.inc.php, from
		// this LemonLDAP library. It is temporary.
		//
		global $obm, $entities;

		$params['domain_id'] = $domain_id;
		$params['update_type'] = 'incremental';
		$params['realm'] = 'domain';

		$backup['uid'] = $obm['uid'];
		$obm['uid'] = $user_id;

		set_update_lock();
		set_update_state($domain_id);
		store_update_data($params);
		$res = exec_tools_update_update($params);
		remove_update_lock();

		$obm['uid'] = $backup['uid'];
		unset($backup);

		$user_data = $this->_engine->getUserDataFromId($user_id, $domain_id);
		$pswd_new = $this->_engine->getHeaderValue($this->_engine->getHeaderName('userobm_password'));
		$pswd_old = $user_data[$this->_engine->_sqlMap['userobm_password']];

		if (strcmp($pswd_new, $pswd_old) != 0)
		{
			passthru(DEFAULT_AUTOMATE_DIRECTORY . "/changePasswd.pl --login $user_name --domain-id $domain_id --passwd $pswd_new --old-passwd $pswd_old --unix");
			passthru(DEFAULT_AUTOMATE_DIRECTORY . "/changePasswd.pl --login $user_name --domain-id $domain_id --passwd $pswd_new --old-passwd $pswd_old --samba");
		}
	}

	/**
	 * Manage user account synchronization.
	 * The account is create or update.
	 * @param $user_name The user name used to authentify the user.
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return int The user identifier if the user is created or updated, or false.
	 */
	function syncUserAccount ($user_name, $user_id, $domain_id)
	{
		if (!is_null($user_id) && $user_id !== false)
		{
			if ($this->_forceUpdate || $this->_engine->verifyUserData($user_name, $domain_id, $user_id))
				return $this->_engine->updateUser($user_name, $domain_id, $user_id);
			return $user_id; 
		}
		else
		{
			return $this->_engine->addUser($user_name, $domain_id);
		}
	}

	/**
	 * Manage user groups synchronization.
	 * Note that group should be update or created, but never deleted. By the
	 * way, user should be associate with or deassociated from a group.
	 * @param $groups All groups send by LemonLDAP.
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return boolean True is the user information is created or updated.
	 */
	function syncUserGroups ($groups, $user_id, $domain_id)
	{
		$groups_ldap = $groups;

		if (is_null($groups_ldap) || sizeof($groups_ldap) == 0 || $groups_ldap === false)
			return true;

		//
		// Update or create groups in OBM.
		//

		foreach ($groups_ldap as $group_name => $group_data)
		{
			$group_id = $this->_engine->isGroupExists($group_name, $domain_id);

			if ($group_id !== false)
				$this->_engine->updateGroup($group_name, $group_id, $group_data, $user_id, $domain_id);
			else
				$group_id = $this->_engine->addGroup($group_name, $group_data, $user_id, $domain_id);

			if ($group_id !== false)
				$groups_ldap[$group_name]['group_id'] = $group_id;
                }

		//
		// Calculate the intersection between groups in database and groups
		// in HTTP headers. For all groups that are in HTTP headers but not
		// in database, the user will be associated. For all groups that are
		// in database but not in HTTP headers, the user will be disassociated.
		//

		$groups_db = $this->_engine->getGroups($user_id, $domain_id);

		foreach ($groups_ldap as $group_name => $group_data)
			if (!array_key_exists($group_name, $groups_db) && ($group_id = $this->_engine->isGroupExists($group_name, $domain_id)) !== false)
					$this->_engine->addUserInGroup($user_id, $group_id, $domain_id);

		foreach ($groups_db as $group_name => $group_id)
			if (!array_key_exists($group_name, $groups_ldap))
				$this->_engine->removeUserFromGroup($user_id, $group_id, $domain_id);

		return true;
	}

	/**
	 * Manage user informations synchronization.
	 * This function will call syncUserAccountn, syncUserGroups and
	 * syncExternalData if necessary.
	 * @param $user_name The user name used to authentify the user.
	 * @param $groups All groups send by LemonLDAP in HTTP header.
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return The user identifier.
	 */
	function syncUserInfo ($user_name, $groups, $user_id, $domain_id)
	{

		$user_id_sync = $this->syncUserAccount($user_name, $user_id, $domain_id);

		if ($user_id_sync !== false)
		{
			$this->_engine->debug("manage user account (SUCCEED)");
		}
		else
		{
			$this->_engine->debug("manage user account (FAILED)");
			return;
		}

		if ($this->syncUserGroups($groups, $user_id_sync, $domain_id) !== false)
		{
			$this->_engine->debug("manage user groups (SUCCEED)");
		}
		else
		{
			$this->_engine->debug("manage user groups (FAILED)");
			return;
		}

		if ($this->_engine->isDataUpdated())
		{
			$this->_engine->debug("proceed to external updates");
			$this->syncExternalData($user_name, $domain_id, $user_id_sync);
		}

		return $user_id_sync;
	}

}

?>
