<?php

//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// Copyright (c) 2009, Guillaume Lardon - glardon<at>linagora.com
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice, this
//     list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//   * Neither the name of the LINAGORA GROUP nor the names of its contributors may
//     be used to endorse or promote products derived from this software without
//     specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//require_once(dirname(__FILE__) . '/LemonLDAP_Engine.php');
//

/**
 * LemonLDAP authentication class
 */
class LemonLDAP_Auth extends Auth {

	/**
	 * Auto create/update accounts and groups, or not.
	 */
	var $_auto = false;

	/**
	 * LemonLDAP logout URL.
	 */
	var $_logout = null;

	/**
	 * LemonLDAP Server IP.
	 */
	var $_server = null;

	/**
	 * Check or not LemonLDAP request.
	 */
	var $_server_check = false;

	/**
	 * Groups header name.
	 */
	var $_groups_header_name = null;

	/**
	 * The LemonLDAP engine.
	 */
	var $_engine = null;

	/**
	 * Backward compatibilities.
	 * Still used by Auth object.
	*/
	var $database_class = 'DB_OBM';

	/**
	 * Constructor.
	 * Initialize internal attribut with parameters found in configuration.
	 */
	function __construct ()
	{
		$database = new $this->database_class;
		$this->_engine = new LemonLDAP_Engine();
		$this->_engine->setDatabase($database);
		$this->_initFromConfiguration();
	}

	/**
	 * Print some debug trace.
	 * @param $msg The message to trace.
	 */
	function _debug ($msg)
	{
		if ($this->_debug)
		{
			$now = date("Y-m-d H:i:s");
			$f = fopen($this->_debug_file, "a+");
			fputs($f, "[$now] $msg\n");
			fclose($f);
		}
	}

	/**
	 * Set internal attributes from parameters found in configuration.
	 */
	function _initFromConfiguration ()
	{
		global $lemonldap_config;
		$this->_auto = $lemonldap_config['auto_update'];
		$this->_logout = $lemonldap_config['url_logout'];
		$this->_server = $lemonldap_config['server_ip_address'];
		$this->_server_check = $lemonldap_config['server_ip_server_check'];
		$this->_debug = $lemonldap_config['debug'];
		$this->_debug_file = $lemonldap_config['debug_filepath'];
		$this->_groups_header_name = $lemonldap_config['group_header_name'];
		if (is_array($lemonldap_config['headers_map']))
			$this->_engine->setHeadersMap($lemonldap_config['headers_map']);
	}

	/**
	 * Check if authentication requests is valide.
	 * This function checks that headers contains the HTTP_X_FORWADED_FOR header.
	 * If not, then if $_SERVER['REMOTE_ADDR'] matches to $_server.
	 * @return boolean
	 */
	function checkRequest ()
	{
		if (!$this->_server_check)
			return true;
		$hn = 'HTTP_X_FORWARDED_FOR';
		$hv = $this->_engine->getHeaderValue($hn);
		$succeed = false;
		if (($hv !== false && strcasecmp(trim($hv), $this->_server) == 0)
				|| strcasecmp($_SERVER['REMOTE_ADDR'], $this->_server) == 0)
			$succeed = true;
		$this->_debug("checkRequest = $succeed");
		return $succeed;
	}

	/**
	 * Verify that user is authenticated.
	 * @return boolean
	 */
	function is_authenticated ()
	{
		return parent::is_authenticated();
	}

	/**
	 * This function retrieve information from headers, and starts a session
	 * automatically for the user found.
	 * @return boolean
	 */
	function auth_validatelogin ()
	{
		global $obm, $lock;

		if (!$this->checkRequest())
			return false;

		//
		// First of all, we have to check if the user exists.
		//

		$header = $this->_engine->getHeaderName('userobm_login');
		$login = $this->_engine->getHeaderValue($header);
		$domain_id = $this->_engine->getDomainID($login);
		$user_id = $this->_engine->isUserExists($login, $domain_id);

		//
		// Then, we try to update/create the account. If this operation failed,
		// we can not do anything else. If not, it is not necessary for the user
		// to be authenticated so that its groups informations will be updated too.
		//

		if ($this->_auto)
		{
			$user_id = $this->sso_manageAccount($login, $user_id, $domain_id);

			if ($user_id !== false)
				$this->sso_manageGroups($user_id, $domain_id);

			if ($this->_engine->isExternalUpdate())
                                $this->_engine->updateExternalData($user_name, $domain_id, $user_id);
		}

		$user_authenticated = $this->sso_authenticate($user_id, $domain_id);
		if (!$user_authenticated)
			$user_id = null;

		$this->_debug("auth_validatelogin / authenticate = " . $user_id);
		return $user_authenticated;
	}

	/**
	 * Disconnect the user by destroying its session.
	 */
	function logout ($nobody = "")
	{
		global $obm, $lock, $sess;

		$login = $_SESSION['obm']['uid'];
		$sess->delete();
		$_SESSION['obm'] = '';
		$_SESSION['auth'] = '';
		unset($this->auth['uname']);
		$this->unauth($nobody == "" ? $this->nobody : $nobody);
		$sess->delete();

		$this->_debug("logout = $login");
		header("location: " . $this->_logout);
		exit();
	}

	/**
	 * Display login page.
	 * For LemonLDAP authentication process, we display a blank page.
	 */
	function of_session_dis_login_page ()
	{
		$this->_debug("of_session_dis_login_page()");
		echo "Permission denied.";
	}

	/**
	 * Authenticate user
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return int The user identifier, or false.
	 */
	function sso_authenticate ($user_id, $domain_id)
	{
		global $obm;

		$data = $this->_engine->getUserDataFromId($user_id, $domain_id);

		if (!is_array($data) || !array_key_exists('user_id', $data))
			return false;

		$unfreeze = global_unfreeze_user($data['user_id']);

		if ($unfreeze)
		{
			$obm['login'] = $data['login'];
			$obm['profile'] = $data['profile'];
			$obm['domain_id'] = $domain_id;
			$obm['delegation'] = $data['delegation_target'];
			return $data['user_id'];
		}

		return false;
	}

	/**
	 * Manage user's account.
	 * The account is create or update.
	 * @param $user_name The user name used to authentify the user.
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return int The user identifier if the user is created or updated, or false.
	 */
	function sso_manageAccount ($user_name, $user_id, $domain_id)
	{
		if (!is_null($user_id) && $user_id !== false)
		{
			if ($this->_engine->verifyUserData($user_name, $domain_id, $user_id))
				return $this->_engine->updateUser($user_name, $domain_id, $user_id);
			return $user_id; 
		}
		else
		{
			return $this->_engine->addUser($user_name, $domain_id);
		}
	}

	/**
	 * Manage user's groups.
	 * Note that group should be update or created, but never deleted. By the
	 * way, user should be associate with or deassociated from a group.
	 * @param $user_id The user unique identifier.
	 * @param $domain_id The domain identifier.
	 * @return boolean True is the user information is created or updated.
	 */
	function sso_manageGroups ($user_id, $domain_id)
	{
		//
		// First step, we update or create the groups found in HTTP
		// headers. Do not need to check function's returns, because
		// we have to do all operations.
		//

		$groups_ldap = $this->_engine->parseGroupsHeader($this->_groups_header_name);

		if (is_null($groups_ldap) || sizeof($groups_ldap) == 0 || $groups_ldap === false)
			return false;

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
			if (!array_key_exists($group_name, $groups_db))
				$this->_engine->addUserInGroup($user_id, $group_data['group_id'], $domain_id);

		foreach ($groups_db as $group_name => $group_id)
			if (!array_key_exists($group_name, $groups_ldap))
				$this->_engine->removeUserFromGroup($user_id, $group_id, $domain_id);

		return true;
	}

}

?>
