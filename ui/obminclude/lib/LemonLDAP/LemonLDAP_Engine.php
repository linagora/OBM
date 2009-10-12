<?php

//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
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
//require_once(dirname(__FILE__) . '/obm/of_category.inc');
//require_once(dirname(__FILE__) . '/obm/group_query.inc');
//require_once(dirname(__FILE__) . '/obm/user_query.inc');
//require_once(dirname(__FILE__) . '/LemonLDAP_Constants.php');
//

/**
 * LemonLDAP engine.
 * Required php/user/user_query.inc.
 */
class LemonLDAP_Engine {

	/**
	 * Valid HTTP headers.
	 */
	var $_headers = array();

	/**
	 * Map between SQL fields and HTTP headers.
	 */
	var $_headersMap = array(
		        'userobm_gid'			=> 'HTTP_OBM_GID',
			//'userobm_domain_id'		=> ,
			'userobm_login'			=> 'HTTP_OBM_UID',
			'userobm_password'              => 'HTTP_OBM_USERPASSWORD',
			//'userobm_password_type'	=> ,
			//'userobm_perms'		=> ,
			//'userobm_kind'		=> ,
			'userobm_lastname'		=> 'HTTP_OBM_SN',
			'userobm_firstname'		=> 'HTTP_OBM_GIVENNAME',
			'userobm_title'			=> 'HTTP_OBM_TITLE',
			'userobm_email'			=> 'HTTP_OBM_MAIL',
			'userobm_datebegin'		=> 'HTTP_OBM_DATEBEGIN',
			//'userobm_account_dateexp'	=> ,
			//'userobm_delegation_target'	=> ,
			//'userobm_delegation'		=> ,
			'userobm_description'		=> 'HTTP_OBM_DESCRIPTION',
			//'userobm_archive'		=> ,
			//'userobm_hidden'		=> ,
			//'userobm_status'		=> ,
			//'userobm_local'		=> ,
			//'userobm_photo_id'		=> ,
			'userobm_phone'			=> 'HTTP_OBM_TELEPHONENUMBER',
			//'userobom_phone2'		=> ,
			//'userobm_mobile'		=> ,
			'userobm_fax'			=> 'HTTP_OBM_FACSIMILETELEPHONENUMBER',
			//'userobm_fax2'		=> ,
			'userobm_company'		=> 'HTTP_OBM_C',
			//'userobm_direction'		=> ,
			'userobm_service'		=> 'HTTP_OBM_OU',
			'userobm_address1'		=> 'HTTP_OBM_POSTALADDRESS',
			//'userobm_address2'		=> ,
			//'userobm_address3'		=> ,
			'userobm_zipcode'		=> 'HTTP_OBM_POSTALCODE',
			'userobm_town'			=> 'HTTP_OBM_L',
			//'userobm_expresspostal'	=> ,
			//'userobm_host_id'		=> ,
			//'userobm_web_perms'		=> ,
			//'userobm_web_list'		=> ,
			//'userobm_web_all'		=> ,
			//'userobm_mail_perms'		=> ,
			//'userobm_mail_ext_perms'	=> ,
			//'userobm_mail_server_id'	=> ,
			//'userobm_mail_server_hostname' => ,
			//'userobm_mail_quota'		=> ,
			//'userobm_nomade_perms'	=> ,
			//'userobm_nomade_enable'	=> ,
			//'userobm_nomade_local_copy'	=> ,
			//'userobm_email_nomade'	=> ,
			//'userobm_vacation_enable'	=> ,
			//'userobm_vacation_datebegin'	=> ,
			//'userobm_vacation_dateend'	=> ,
			//'userobm_vacation_message'	=> ,
			//'userobm_samba_perms'		=> ,
			//'userobm_samba_home'		=> ,
			//'userobm_samba_home_drive'	=> ,
			//'userobm_samba_logon_script'	=> ,
			// ---- Unused values ? ----
			//'userobm_ext_id'              => ,
			//'userobm_system'              => ,
			//'userobm_nomade_datebegin'    => ,
			//'userobm_nomade_dateend'      => ,
			//'userobm_location'            => ,
			//'userobm_education'           => ,
		);

	/**
	 * Map between SQL fields and OBM keys.
	 */
	var $_sqlMap = array(
			'userobm_id'			=> 'user_id',
			'userobm_gid'			=> 'gid',
			'userobm_domain_id'		=> 'domain_id',
			'userobm_login'			=> 'login',
			'userobm_password'		=> 'passwd',
			'userobm_password_type'		=> 'password_type',
			'userobm_perms'			=> 'profile',
			'userobm_kind'			=> 'kind',
			'userobm_lastname'		=> 'lastname',
			'userobm_firstname'		=> 'firstname',
			'userobm_title'			=> 'title',
			'userobm_email'			=> 'email',
			'userobm_datebegin'		=> 'datebegin',
			'userobm_account_dateexp'	=> 'dateexp',
			'userobm_delegation_target'	=> 'delegation_target',
			'userobm_delegation'		=> 'delegation',
			'userobm_description'		=> 'desc',
			'userobm_archive'		=> 'archive',
			'userobm_hidden'		=> 'hidden',
			'userobm_status'		=> 'status',
			'userobm_local'			=> 'local',
			'userobm_photo_id'		=> 'photo_id',
			'userobm_phone'			=> 'phone',
			'userobom_phone2'		=> 'phone2',
			'userobm_mobile'		=> 'mobile',
			'userobm_fax'			=> 'fax',
			'userobm_fax2'			=> 'fax2',
			'userobm_company'		=> 'company',
			'userobm_direction'		=> 'direction',
			'userobm_service'		=> 'service',
			'userobm_address1'		=> 'ad1',
			'userobm_address2'		=> 'ad2',
			'userobm_address3'		=> 'ad3',
			'userobm_zipcode'		=> 'zip',
			'userobm_town'			=> 'town',
			'userobm_expresspostal'		=> 'cdx',
			'userobm_host_id'		=> 'host_id',
			'userobm_web_perms'		=> 'web_perms',
			'userobm_web_list'		=> 'web_list',
			'userobm_web_all'		=> 'web_all',
			'userobm_mail_perms'		=> 'mail_perms',
			'userobm_mail_ext_perms'	=> 'mail_ext_perms',
			'userobm_mail_server_id'	=> 'mail_server_id',
			'userobm_mail_server_hostname'	=> 'mail_server_hostname',
			'userobm_mail_quota'		=> 'mail_quota',
			'userobm_nomade_perms'		=> 'nomade_perms',
			'userobm_nomade_enable'		=> 'nomade_enable',
			'userobm_nomade_local_copy'	=> 'nomade_local_copy',
			'userobm_email_nomade'		=> 'email_nomade',
			'userobm_vacation_enable'	=> 'vacation_enable',
			'userobm_vacation_datebegin'	=> 'vacation_datebegin',
			'userobm_vacation_dateend'	=> 'vacation_dateend',
			'userobm_vacation_message'	=> 'vacation_message',
			'userobm_samba_perms'		=> 'smb_perms',
			'userobm_samba_home'		=> 'smb_home',
			'userobm_samba_home_drive'	=> 'smb_home_drive',
			'userobm_samba_logon_script'	=> 'smb_logon',
			// ---- Unused values ? ----
			//'userobm_ext_id'		=> ,
			//'userobm_system'		=> ,
			//'userobm_nomade_datebegin'	=> ,
			//'userobm_nomade_dateend'	=> ,
			//'userobm_location'		=> ,
			//'userobm_education'		=> ,
			'group_id'		=> 'group_id',
			'group_domain_id'	=> 'domain_id',
			'group_system'		=> 'system',
			'group_samba'		=> 'samba',
			'group_name'		=> 'name',
			'group_desc'		=> 'desc',
			'group_email'		=> 'email',
			'group_privacy'		=> 'privacy',
			'group_delegation'	=> 'delegation',
			'group_manager_id'	=> 'manager',
			'group_usercreate'	=> 'usercreate',
			'group_userupdate'	=> 'userupdate',
		);

	/**
	 * Database object.
	 */
	var $_db = null;

	/**
	 * Do not need to retrieve user data everytime, so store them into a kind
	 * of buffer.
	 */
	var $_db_userData = null;

	/**
	 * Flag that indicate if the connected user should be update on external
	 * database (ldap,etc.)
	 */
	var $_externalUpdate = false;

	/**
	 * Constructor.
	 * Initialize headers from what are found into the HTTP request.
	 */
	function __construct ()
	{
		$this->_headers = $this->_parseHeaders();
	}

	/**
	 * Build user data array from informations found in headers.
	 * Most of informations will be retrieved from headers. This array MUST
	 * then be checked then by lmng_check_user_data_form function. Also, all
	 * array keys do not correspond to SQL field names, but mapping does. So
	 * a transformation will be done.
	 * @return Array An associative array that could be used directly by OBM.
	 */
	function _buildInternalUserData ()
	{
		$values = Array();

		foreach ($this->_sqlMap as $key => $key_obm)
		{
			$value = $this->getHeaderValue($this->_headersMap[$key]);
			if (!is_null($value))
			{
				// No multi values for any user keys.
				if (strncmp($key, 'userobm', strlen('userobm')) == 0)
				{
					$tab = split(';', $value);
					$value = $tab[0];
				}
				$values[$key_obm] = $value;
			}
		}

		return $values;
	}

	/**
	 * Check if internal attributes are OK, such as database object or
	 * SQL mapping.
	 * @return boolean
	 */
	function _checkInternalObjects ()
	{
		if (is_null($this->_db) || is_null($this->_headersMap))
			return false;
		return true;
	}

	/**
	 * Parse HTTP headers.
	 * Return an associative array of valid headers, where keys are HTTP
	 * headers and values their corresponding values.
	 * @return Array All HTTP headers found.
	 */
	function _parseHeaders ()
	{
		$headers = array();
		foreach ($_SERVER as $key => $value)
			if (ereg('HTTP_.+', $key) && strlen($value) != 0)
				$headers[strtoupper($key)] = $value;
		return $headers;
	}

	/**
	 * Set default values for group data.
	 * @param $data One array of the one returns by _parseGroupsHeader.
	 * @param $domain_id A valid domaine identifier.
	 * @param $user_id A valid user identifier.
	 * @return Array Group data with default values set, or false.
	 */
	function _setDefaultGroupData ($data, $domain_id, $user_id)
	{
		if (!is_array($data))
			return false;

		foreach ($this->_sqlMap as $key => $key_obm)
		{
			$add = true;
			$value= null;
			if (!array_key_exists($key_obm, $data) || is_null($data[$key_obm]))
			{
				switch ($key)
				{
					case 'group_domain_id':
                                                $value = $domain_id;
                                                break;
					case 'group_usercreate':
						$value = $user_id;
						break;
					default:
						$add = false;
						break;			
				}
			}
			switch ($key)
			{
				case 'group_privacy':
					//
					// Set group private if no mail found.
					//
					if (array_key_exists($this->_sqlMap['group_email'], $data))
						$value = 0;
					else
						$value = 1;
					break;
			}
			if ($add && !is_null($value))
				$data[$key_obm] = $value;
		}

		return $data;
	}

	/**
	 * Set default values for user data.
	 * @param $data An associative array returns by _buildInternalUserData.
	 * @param $login A login.
	 * @param $domain_id A valid domain identifier.
	 * @return Array User data with default value sets, or false.
	 */
	function _setDefaultUserData ($data, $login, $domain_id)
	{
		if (!is_array($data))
			return false;

		$backup['obm_domain_id'] = $obm['domain_id'];
		$obm['domain_id'] = $domain_id;

		foreach ($this->_sqlMap as $key => $key_obm)
		{
			$add = true;
			$value = null;
			if (!array_key_exists($key_obm, $data) || is_null($data[$key_obm]))
			{
				switch ($key)
				{
					case 'userobm_perms':
						$value = DEFAULT_USEROBM_PROFILE;
						break;
					case 'userobm_status':
						$value = DEFAULT_USEROBM_STATUS;
						break;
					case 'userobm_hidden':
						$value = DEFAULT_USEROBM_HIDDEN;
						break;
					case 'userobm_archive':
						$value = DEFAULT_USEROBM_ARCHIVE;
						break;
					case 'userobm_local':
						$value = DEFAULT_USEROBM_LOCAL;
						break;
					case 'userobm_domain_id':
						$value = $domain_id;
						break;
					case 'userobm_login':
						$value = $login;
						break;
					default:
						$add = false;
						break;
				}
			}
			if ($add && !is_null($value))
				$data[$key_obm] = $value;
		}

		//
		// Post. We can not do this during previous loop, because default
		// settings could not be already setted.
		//

		if (array_key_exists($this->_sqlMap['userobm_email'], $data))
		{
			$params['mail_server_id'] = 'auto';
			$params = lmng_get_user_params_mail_server_id($params);
			$data[$this->_sqlMap['userobm_mail_server_id']] = $params['mail_server_id'];
			$data[$this->_sqlMap['userobm_mail_perms']] = 1;
		}
		else
		{
			$data[$this->_sqlMap['userobm_mail_server_id']] = null;
                        $data[$this->_sqlMap['userobm_mail_perms']] = null;
		}

		$obm['domain_id'] = $backup['obm_domain_id'];
		unset($backup);

		return $data;
	}

	/**
	 * Add a new group.
	 * @param group_name The name of the group.
	 * @param domain_id A valid domain identifier.
	 * @param user_id A valid user identifier.
	 * @param group_data Information that will be insert in the group.
	 * @return int The group identifier of false.
	 */
	function addGroup($group_name, $group_data, $user_id, $domain_id)
	{
		global $obm;

		$params_db = $this->_setDefaultGroupData($group_data, $domain_id, $user_id);
                $params_db['action'] = INSERT_MODIFICATION_TYPE;

		$backup['obm_uid'] = $obm['uid'];
                $backup['obm_domain_id'] = $obm['domain_id'];
                $backup['globals_module'] = $GLOBALS['module'];
		$obm['uid'] = $user_id;
                $obm['domain_id'] = $domain_id;
                $GLOBALS['module'] = "group";

                $succeed = false ;
                if (lmng_check_group_data_form($params_db)
                                && ($group_id = lmng_run_query_group_insert($params_db)))
                {
                        set_update_state();
                        $succeed = $group_id;
                }

		$obm['uid'] = $backup['obm_uid'];
                $obm['domain_id'] = $backup['obm_domain_id'];
                $GLOBALS['module'] = $backup['globals_module'];
                unset($backup);

                return $succeed;
	}

	/**
	 * Add a new user with informations found into HTTP headers.
	 * @param $login A username.
	 * @param $domain_id A domain identifier.
	 * @return int The user identifier, or false.
	 */
	function addUser($login, $domain_id)
	{
		global $obm;

		//
		// Some internal functions used
		// global variables. To prevent from unstable creation, we prefer
		// to set those internal variables.
		//

		$backup['domain_id'] = $obm['domain_id'];
		$backup['uid'] = $obm['uid'];
		$backup['globals_module'] = $GLOBALS['module'];

		$obm['uid'] = 1;		// Contains the parent user
		$obm['domain_id'] = $domain_id;
		$GLOBALS['module'] = "user";

		//
		// The above code is taken from php/user/user_index.php, when
		// $user['action'] equals to 'insert'. But, we have to insert
		// the file that contains all necessary user methods. It seems
		// that it is $path which contains may OBM directory.
		//

		$params = $this->_buildInternalUserData($login, $domain_id);
		$params = $this->_setDefaultUserData($params, $login, $domain_id);

		//
		// Original one use also check_user_defined_rules() function
		// in the following condition.
		//

		if (!lmng_check_user_data_form('', $params))
			return false ;

		$succeed = false;
		if (($user_id = lmng_run_query_user_insert($params)) > 0)
		{
			$params['user_id'] = $user_id;
			set_update_state();
			$succeed = $user_id;
			$this->_externalUpdate = true;
		}

		$obm['uid'] = $backup['uid'];
		$obm['domain_id'] = $backup['domain_id'];
		$GLOBALS['module'] = $backup['globals_module'];
		unset($backup);

		return $succeed;
	}

	/**
	 * Add a user in a group.
	 * @param $user_id The user identifier.
	 * @param $group_id A group identifier.
	 * @param $domain_id A valid domain identifier.
	 * @return boolean True if succeed.
	 */
	function addUserInGroup ($user_id, $group_id, $domain_id)
	{
		global $obm;
		$backup['obm_domain_id'] = $obm['domain_id'];
		$obm['domain_id'] = $domain_id;

		$data = Array();
		$data['user_nb'] = 1;
		$data['user1'] = $user_id;
		$data['group_id'] = $group_id;

		$c = lmng_run_query_group_usergroup_insert($data, $domain_id);

		$obm['domain_id'] = $backup['obm_domain_id'];
		unset($backup);

		if ($c > 0)
			return true;
		return false;
	}

	/**
	 * Get domain identifier from a login that match the pattern
	 * username@domain. If the domain is not found into the database, then
	 * it is the default that will be used.
	 * @param $login A username.
	 * @return int A domain identifier.
	 */
	function getDomainID ($login = null)
	{
		global $c_default_domain, $c_singleNameSpace;

		if (!$this->_checkInternalObjects())
			return false;

		$domain_id = null;

		if (!$c_singleNameSpace && !is_null($login) && strpos($login,"@") !== false)
		{
			list($login, $domain) = split('@', $login);
			$domain = addslashes($domain);

			$sql_query = 'SELECT domain_id FROM domain WHERE domain_name = ' . $domain;
			$this->_db->query($sql_query);

			while ($this->_db->next_record() && is_null($domain_id))
				$domain_id = $this->_db->f('domain_id');
		}

		if (is_null($domain_id))
			return $c_default_domain;
		return $domain_id;
	}

	/**
	 * Get a header name from a SQL field.
	 * @param $sqlField SQL field name to search for.
	 * @return String The valid HTTP header name, or false.
	 */
	function getHeaderName ($sqlField = null)
	{
		if (!$this->_checkInternalObjects())
			return false;
		if (is_null($sqlField) || !array_key_exists($sqlField, $this->_headersMap))
			return false;
		return strtoupper($this->_headersMap[$sqlField]);
	}

	/**
	 * Get a header value from a header name.
	 * @param $headerName A value HTTP header name.
	 * @return String Value corresponding to the HTTP header name, could be null.
	 */
	function getHeaderValue ($headerName = null)
	{
		if (is_null($headerName) || !array_key_exists(strtoupper($headerName), $this->_headers))
			return null;
		return $this->_headers[strtoupper($headerName)];
	}

	/**
	 * Retrieve all groups for a particular user.
	 * @param $user_id The user identifier.
	 * @param $domain_id The domain identifier.
	 * @return Array An array of all groups names, or false.
	 */
	function getGroups ($user_id, $domain_id)
	{
		if (!$this->_checkInternalObjects())
			return false;

		$groups = Array();

		$sql_query = 'SELECT group_id, group_name ';
		$sql_query .= 'FROM of_usergroup, UGroup ';
		$sql_query .= 'WHERE of_usergroup_group_id = group_id ';
		$sql_query .= 'AND of_usergroup_user_id = \'' . addslashes($user_id) . '\' ';
		$sql_query .= 'AND group_domain_id = \'' . addslashes($domain_id) . '\' ';

		$this->_db->query($sql_query);
		while ($this->_db->next_record())
		{
			$group_name = $this->_db->f('group_name');
			$group_id = $this->_db->f('group_id');
			$groups[$group_name] = $group_id;
		}

		$sql_query = 'SELECT group_id, group_name ';
		$sql_query .= 'FROM UserObm, UGroup ';
		$sql_query .= 'WHERE userobm_gid = group_gid ';
		
		$this->_db->query($sql_query);
		while ($this->_db->next_record())
		{
			$group_name = $this->_db->f('group_name');
			$group_id = $this->_db->f('group_id');
			if (array_key_exists($group_name, $groups))
				unset($groups[$group_name]);
		}

		return $groups;
	}

	/**
	 * Retrieve all necessary group data.
	 * Be carefull, there is no verification on parameters.
	 * @param $groupname A valid group name.
	 * @param $domain_id A valid domain identifier (not a domain name).
	 * @return Array An associative array where keys are SQL fields, or false.
	 */
	function getGroupData ($groupname, $domain_id)
	{
		if (!$this->_checkInternalObjects())
			return false;

		$sql_query = 'SELECT group_id';
		$sql_query .= ' FROM UGroup WHERE group_name = \'' . addslashes($groupname) . '\'';
		$sql_query .= ' AND group_domain_id = \'' . addslashes($domain_id) . '\'';

		$group_id = null;
		$this->_db->query($sql_query);
		while ($this->_db->next_record() && is_null($group_id))
			$group_id = $this->_db->f('group_id');

		if (is_null($group_id))
			return false;

		return $this->getGroupDataFromId($group_id, $domain_id);
	}

	/**
	 * Retrieve all necessary group data.
	 * Be carefull, there is no verification on parameters.
	 * @param $group_id A group unique identifier.
	 * @param $domain_id A domain identifier.
	 * @param $force Force to perform the SQL request.
	 * @return Array A associative array where keys are SQL fields, or false.
	 */
	function getGroupDataFromId ($group_id, $domain_id)
	{
		global $obm;

		if (is_null($group_id))
			return false;

		$backup['obm_domain_id'] = $obm['domain_id'];
		$obm['domain_id'] = $domain_id;
		$grp_q = lmng_run_query_group_detail($group_id);

		$group = Array();
		foreach ($this->_sqlMap as $key => $key_obm)
		{
			if (substr($key, 0, strpos($key, '_')) != "group")
				continue;
			$group[$key_obm] = $grp_q->f($key);
		}

		$obm['domain_id'] = $backup['obm_domain_id'];
		unset($backup);

		return $group;
	}

	/**
	 * Retrieve all necessary user data.
	 * Be carefull, there is no verification on parameters.
	 * @param $login A valid username.
	 * @param $domain_id A valid domain idenfifier (not a domain name).
	 * @param $force Force to perform the SQL request.
	 * @return Array An associative array where keys are SQL fields, or false.
	 */
	function getUserData ($login, $domain_id, $force = false)
	{
		if (!$this->_checkInternalObjects())
			return false;
		if (!is_null($this->_db_userData) && !$force)
			return $this->_db_userData;

		//
		// First, we search for a user ID into the database. We do not know
		// it, so we make a direct SQL query into the database.
		//

		$sql_query = 'SELECT userobm_id';
		$sql_query .= ' FROM UserObm WHERE userobm_login = \'' . addslashes($login) . '\'';
		$sql_query .= ' AND userobm_domain_id = \'' . addslashes($domain_id) . '\'';

		$user_id = null;
		$this->_db->query($sql_query);
		while ($this->_db->next_record() && is_null($user_id))
		{
			$user_id = $this->_db->f('userobm_id');
		}

		if (is_null($user_id))
			return false;

		//
		// Now, we have the user ID. We can get all informations from the
		// database corresponding to this user. All index is formated to
		// be directly use then by OBM function, for creating or updating
		// user for example.
		//

		return $this->getUserDataFromId($user_id, $domain_id, $force);
	}

	/**
	 * Retrieve all necessary user data.
	 * Be carefull, there is no verification on parameters.
	 * @param $user_id A user unique identifier.
	 * @param $domain_id A domain identifier.
	 * @param $force Force to perform the SQL request.
	 * @return Array A associative array where keys are sql field, or false.
	 */
	function getUserDataFromId ($user_id, $domain_id, $force = false)
	{
		global $obm;

		if (is_null($user_id))
			return false;
		if (!is_null($this->_db_userData) && !$force)
			return $this->_db_userData;

		$backup['obm_domain_id'] = $obm['domain_id'];
		$obm['domain_id'] = $domain_id;
		$usr_q = lmng_run_query_user_detail($user_id);

		$user = Array();
		foreach ($this->_sqlMap as $key => $key_obm)
		{
			if (substr($key, 0, strpos($key, '_')) != "userobm")
				continue;
			switch ($key)
			{
				case 'userobm_datebegin':
					$value = of_date_upd_format($usr_q->f($key_obm),true);
					break;
				case 'userobm_account_dateexp':
					$value = of_date_upd_format($usr_q->f($key_obm),true);
					break;
				case 'userobm_mail_server_id':
					$value = $usr_q->f('mailserver_id');
					break;
				case 'userobm_mail_server_hostname':
					$value = $usr_q->f('mailserver_hostname');
					break;
				case 'userobm_vacation_datebegin':
					$value = of_date_upd_format($usr_q->f($key), true);
					break;
				case 'userobm_vacation_dateend':
					$value = of_date_upd_format($usr_q->f($key), true);
					break;
				case 'userobm_vacation_message':
					$value = stripslashes($usr_q->f($key));
					break;
				case 'userobm_samba_home_drive':
					$value = trim($usr_q->f($key));
					break;
				default:
					$value = $usr_q->f($key);
					break;
			}
			$user[$key_obm] = $value;
		}

		$this->_db_userData = $user;
		$obm['domain_id'] = $backup['obm_domain_id'];
		unset($backup);

		return $user;
	}

	/**
	 * Indicates if a external update should be made.
	 */
	function isExternalUpdate ()
	{
		return $this->_externalUpdate;
	}

	/**
	 * Indicates if a group exists in the database.
	 * @param $groupname A group name.
	 * @param $domain_id A valid domain identifier.
	 * @return int Group identifier or false if not found.
	 */
	function isGroupExists ($groupname, $domain_id)
	{
		if (!$this->_checkInternalObjects())
			return false;
		if (is_null($groupname))
			return false;

		$group = $this->getGroupData($groupname, $domain_id);

		if (is_array($group) && array_key_exists('group_id', $group))
			return $group['group_id'];
		return false;
	}

	/**
	 * Indicates if a user exists in the database.
	 * @param $login A username.
	 * @param $domain_id A valid domain identifier.
	 * @return int User identifier or false if not found.
	 */
	function isUserExists ($login, $domain_id)
	{
	        if (!$this->_checkInternalObjects())
			return false;
		if (is_null($login))
			return false;

		$user = $this->getUserData($login, $domain_id, true);

		if (is_array($user) && array_key_exists('user_id', $user))
			return $user['user_id'];
		return false;
	}

	/**
	 * Parse a group header.
	 * @param $headerName The header name of the header that contains all
	 * 		groups of the current user.
	 * @return Array An associative array where key are group names or false.
	 */
	function parseGroupsHeader ($headerName)
	{
		$values = $this->getHeaderValue($headerName);
		if (is_null($values))
			return false;

		$groups = Array();
		$groups_str = explode(' ', $values);

		foreach ($groups_str as $group_str)
		{
			if (strlen($group_str))
			{
			$group = explode('|', $group_str);
			for ($i=0; $i<sizeof($group); $i++)
			{
				switch($i)
				{
					case 0:
						$group[$this->_sqlMap['group_name']] = $group[$i];
						break;
					case 1:
						$group[$this->_sqlMap['group_email']] = $group[$i];
						break;
					case 2:
						$group[$this->_sqlMap['group_privacy']] = $group[$i];
						break;
				}
				unset($group[$i]);
			}
			$groups[$group[$this->_sqlMap['group_name']]] = $group;
		}
		}

		return $groups;
	}

	/**
	 * Remove a user in a group.
	 * @param $user_id The user identifier.
	 * @param $group_id A group identifier.
	 * @param $domain_id A domain identifier.
	 * @return boolean True if succeed.
	 */
	function removeUserFromGroup ($user_id, $group_id, $domain_id)
	{
		global $obm;
		$backup['obm_domain_id'] = $obm['domain_id'];
		$obm['domain_id'] = $domain_id;

		$data = Array();
		$data['user_nb'] = 1;
		$data['user1'] = $user_id;
		$data['group_id'] = $group_id;

		$c = lmng_run_query_group_usergroup_delete($data, null);

		$obm['domain_id'] = $backup['obm_domain_id'];
		unset($backup);

		if ($c > 0)
			return true;
		return false;
	}

	/**
	 * Update group information.
	 * @param $group_name A group name.
	 * @param $domain_id A valid domain identifier.
	 * @param $user_id A valid user identifier.
	 * @param $group_id The corresponding group identifier.
	 * @param $group_data Associative array of group data, included group_id.
	 * @return int The group identifier updated.
	 */
	function updateGroup($group_name, $group_id, $group_data, $user_id, $domain_id)
	{
		global $obm;

		$params_db = $this->getGroupDataFromId($group_id, $domain_id);
		foreach ($params_db as $key => $value)
		{
			if (array_key_exists($key, $group_data))
				$params_db[$key] = $group_data[$key];
		}
		$params_db = $this->_setDefaultGroupData($params_db, $domain_id, $user_id);
		$params_db['action'] = UPDATE_MODIFICATION_TYPE;

		$backup['obm_uid'] = $obm['uid'];
		$backup['obm_domain_id'] = $obm['domain_id'];
		$backup['globals_module'] = $GLOBALS['module'];
		$obm['domain_id'] = $domain_id;
		$obm['uid'] = $params_db[$this->_sqlMap['group_usercreate']];
		$GLOBALS['module'] = "group";

		$succeed = false ;
		if (lmng_check_group_data_form($params_db)
				&& lmng_run_query_group_update($params_db))
		{
			set_update_state();
			$succeed = $group_id;
		}

		$obm['uid'] = $backup['obm_uid'];
		$obm['domain_id'] = $backup['obm_domain_id'];
		$GLOBALS['module'] = $backup['globals_module'];
		unset($backup);

		return $succeed;
	}

	/**
	 * Update user information.
	 * @param $login A username.
	 * @param $domain_id A valid domain identifier.
	 * @param $user_id The corresponding user identifier.
	 * @return int The user identifier, or false.
	 */
	function updateUser($login, $domain_id, $user_id)
	{
		global $obm;

		//
		// Some internal functions used global variables. To prevent from
		// unstable creation, we prefer to set those internal variables.
		//

		$backup['domain_id'] = $obm['domain_id'];
		$backup['uid'] = $obm['uid'];
		$backup['globals_module'] = $GLOBALS['module'];

		$obm['uid'] = $user_id;
		$obm['domain_id'] = $domain_id;
		$GLOBALS['module'] = "user";

		//
		// We have to retrieve all informations from database. Do not
		// be worried, OBM return hash which could contains int index
		// also. That does not match $params structure, so we will
		// remove those type of index.
		//

		$params = $this->_buildInternalUserData($login, $domain_id);
		$params_db = $this->getUserDataFromId($user_id, $domain_id);
		foreach ($params_db as $key => $value)
		{
			if (array_key_exists($key, $params))
				$params_db[$key] = $params[$key];
		}
		$params_db = $this->_setDefaultUserData($params_db, $login, $domain_id);

		$params['action'] = UPDATE_MODIFICATION_TYPE;

		//
		// The above code is taken from php/user/user_index.php, when
		// $user['action'] equals to 'update'.
		//

		$succeed = false ;
		if (lmng_check_user_data_form($user_id, $params_db)
				&& lmng_run_query_user_update($user_id, $params_db))
		{
			$succeed = $user_id;
		}

		$obm['uid'] = $backup['uid'];
		$obm['domain_id'] = $backup['domain_id'];
		$GLOBALS['module'] = $backup['globals_module'];
		unset($backup);

		return $succeed;
	}

	/**
	 * Update external repositories, such the OBM LDAP directory,
	 * with new informations provides by LemonLDAP.
	 * User data are synchronized, like groups and user password.
	 * @param $user_name Login of the user
	 * @param $domain_id
	 * @param $user_id
	 */
	function updateExternalData ($user_name, $domain_id, $user_id)
	{
		// Call global external update
		$params['update_type'] = "global";
		$params['domain_id'] = $domain_id;
		$params['realm'] = 'domain';
		set_update_lock();
		set_update_state($domain_id);
		store_update_data($params);
		$res = exec_tools_update_update($params);
		remove_update_lock();

		$user_data = $this->getUserDataFromId($user_id, $domain_id);
		$password_new = $this->getHeaderValue($this->getHeaderName('userobm_password'));
		$password_old = $user_data[$this->_sqlMap['userobm_password']];

		if (strcmp($password_new, $password_old) != 0)
		{
			passthru("/usr/share/obm/www/auto/changePasswd.pl --login $user_name --domain-id $domain_id --passwd $password_new --old-passwd $password_old --unix");
			passthru("/usr/share/obm/www/auto/changePasswd.pl --login $user_name --domain-id $domain_id --passwd $password_new --old-passwd $password_old --samba");
                }
	}

	/**
	 * Set database object to be used.
	 * @param $database The database object.
	 */
	function setDatabase ($database)
	{
		$this->_db = $database;
	}

	/**
	 * Set the map between SQL fields and HTTP headers.
	 * @param $headersMap The associative array which defines mapping.
	 */
	function setHeadersMap ($headersMap)
	{
		$this->_headersMap = $headersMap;
	}

	/**
	 * Verify if user should be updated.
	 * @param $login
	 * @param $domain_id
	 * @param $user_id
	 */
	function verifyUserData ($login, $domain_id, $user_id)
	{
		$user_data = $this->_buildInternalUserData();
		$sql_query = '';
		$sql_query_tab = Array();

		foreach ($this->_sqlMap as $key => $key_obm)
		{
			if (strncmp($key, 'group', strlen('group')) == 0)
				continue;
			if (array_key_exists($key_obm, $user_data) && strlen($user_data[$key_obm]) > 0)
				$sql_query_tab[] = $key . ' = \'' . addslashes($user_data[$key_obm]) . '\'';
		}

		$sql_query_tab_max = sizeof($sql_query_tab);
		for ($i=0; $i<$sql_query_tab_max-1; $i++)
		{
			$sql_query .= $sql_query_tab[$i] . ' AND ';
		}
		$sql_query .= $sql_query_tab[$sql_query_tab_max-1];

                $sql_query = 'SELECT userobm_id FROM UserObm WHERE ' . $sql_query;

		$this->_db->query($sql_query);
		while ($this->_db->next_record() && is_null($user_id_tmp))
		{
			$user_id_tmp = $this->_db->f('userobm_id');
		}

		$update = false;
		if (is_null($user_id_tmp))
			$update = true;

		$this->_externalUpdate = $update;
		return $update;
	}
}

?>
