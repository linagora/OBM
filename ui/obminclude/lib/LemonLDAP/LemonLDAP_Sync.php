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
// 

/**
 * LemonLDAP synchronization class.
 * It will all this connector to perform a search on a LDAP directory, to
 * retrieve all users and groups.
 */
class LemonLDAP_Sync {

  /**
   * Indicates if the synchronization engine is enabled or not.
   * @var boolean
   */
  private $_isEnabled = false;

  /**
   * Force user update instead of verify if there are available updates.
   * If this option is true, then updates are force whether or not
   * there are available updates.
   * @var boolean
   */
  private $_forceUserUpdate = false;

  /**
   * Force groups update in OBM.
   * It means that LDAP groups are exactly the same as OBM groups. If this
   * option is false, then it allows OBM to have local groups.
   * @var boolean
   */
  private $_forceGroupUpdate = false;

  /**
   * The name of HTTP header that contains groups.
   * @var string
   */
  private $_groupsHeaderName = 'HTTP_OBM_GROUPS';

  /**
   * The LemonLDAP engine.
   * @var LemonLDAP_Engine
   */
  private $_engine = null;

  /**
   * The LemonLDAP logger.
   * @var LemonLDAP_Logger
   */
  private $_logger = null;

  /**
   * Constructor.
   */
  public function __construct ($engine)
  {
    $this->_engine = $engine;
    $this->_logger = LemonLDAP_Logger::getInstance();
    $this->initializeFromConfiguration();
  }

  /**
   * Enable the synchronization.
   * @param $activate Enable or not.
   */
  public function enable ($activate = true)
  {
    $this->_isEnabled = $activate;
  }

  /**
   * Initiliaze internal parameters from configuration.
   */
  public function initializeFromConfiguration ($config = null)
  {
    global $lemonldap_config;
    if (is_null($config))
    {
      $config = $lemonldap_config;
    }
    if (array_key_exists('auto_update', $config) !== false)
    {
      $this->_isEnabled = $config['auto_update'];
    }
    if (array_key_exists('auto_update_force_user', $config) !== false)
    {
      $this->_forceUserUpdate = $config['auto_update_force_user'];
    }
    if (array_key_exists('auto_update_force_group', $config) !== false)
    {
      $this->_forceGroupUpdate = $config['auto_update_force_group'];
    }
    if (array_key_exists('group_header_name', $config) !== false)
    {
      $this->_groupsHeaderName = $config['group_header_name'];
    }
  }

  /**
   * Is this synchronization object enabled or not ?
   * @return boolean
   */
  public function isEnabled ()
  {
    return $this->_isEnabled;
  }

  /**
   * Force synchronization of external repositories, such as the OBM LDAP directory,
   * with new informations provides by LemonLDAP.
   * User data are synchronized, like groups and user password.
   * @param $user_id
   * @param $domain_id
   * @param $username Login of the user
   */
  protected function syncExternalData ($user_id, $domain_id, $username)
  {
    global $obm, $entities;
    //
    // $entities is defined into the file package.inc.php, from
    // this LemonLDAP library. It is temporary.
    //
    if (!$this->isEnabled())
    {
      return false;
    }
    //
    // Fixe parameters.
    //
    $params['domain_id'] = $domain_id;
    $params['update_type'] = 'incremental';
    $params['realm'] = 'domain';
    $backup['uid'] = $obm['uid'];
    $obm['uid'] = $user_id;
    //
    // Launch external update. This could take few seconds.
    //
    set_update_lock();
    set_update_state($domain_id);
    store_update_data($params);
    exec_tools_update_update($params);
    remove_update_lock();
    $obm['uid'] = $backup['uid'];
    unset($backup);
  }

  /**
   * Manage user account synchronization.
   * The account is create or update.
   * @param $user_id The user unique identifier.
   * @param $domain_id The domain identifier.
   * @param $username The user name used to authentify the user.
   * @return int The user identifier if the user is created or updated, or false.
   */
  protected function syncUserAccount ($user_id, $domain_id, $username)
  {
    if (!$this->isEnabled())
    {
      return false;
    }
    if (!is_null($user_id) && $user_id !== false)
    {
      $update = $this->_engine->verifyUserData($username, $domain_id, $user_id);
      if ($update || $this->_forceUserUpdate)
      {
        return $this->_engine->updateUser($username, $domain_id, $user_id);
      }
      return $user_id;
    }
    else
    {
      return $this->_engine->addUser($username, $domain_id);
    }
  }

  /**
   * Manage user groups synchronization.
   * Note that group should be update or created, but never deleted. By the
   * way, user should be associate with or deassociated from a group. Note that
   * if one group is not correctly created or updated, then this function will
   * return false.
   * @param $user_id The user unique identifier.
   * @param $domain_id The domain identifier.
   * @param $groups Groups of user.
   * @return boolean True is the user groups are correctly created or updated.
   */
  protected function syncUserGroups ($user_id, $domain_id, $groups)
  {
    if (!$this->isEnabled())
    {
      return false;
    }
    if (!$this->_forceGroupUpdate && sizeof($groups))
    {
      return true;
    }
    //
    // Update or create groups in OBM. The primary default group have not to be
    // managed by this library.
    //
    $sync_succeed = true;
    $groups_ldap = $groups;
    foreach ($groups_ldap as $group_name => $group_data)
    {
      $group_id = $this->_engine->isGroupExists($group_name, $domain_id);
      if ($group_id !== false)
      {
        $group_id = $this->_engine->updateGroup(
            $group_name, $group_id, $group_data, $user_id, $domain_id);
      }
      else
      {
        $group_id = $this->_engine->addGroup(
            $group_name, $group_data, $user_id, $domain_id);
      }
      if ($group_id !== false)
      {
        $groups_ldap[$group_name]['group_id'] = $group_id;
      }
      else
      {
        $sync_succeed = false;
      }
    }
    //
    // Calculate the intersection between groups in database and groups
    // in HTTP headers. For all groups that are in HTTP headers but not
    // in database, the user will be associated. For all groups that are
    // in database but not in HTTP headers, the user will be disassociated.
    // If we have only one error during groups synchronization in OBM,
    // we do not update user information in groups.
    //
    $groups_db = $this->_engine->getGroups($user_id, $domain_id);
    foreach ($groups_ldap as $group_name => $group_data)
    {
      if (array_key_exists($group_name, $groups_db))
      {
        continue;
      }
      $group_id = $this->_engine->isGroupExists($group_name, $domain_id);
      if ($group_id === false)
      {
        continue;
      }
      if (!$this->_engine->addUserInGroup($user_id, $group_id, $domain_id))
      {
        $this->_logger->warn("Fail to add user in group $group_name");
        $sync_succeed = false;
      }
    }
    //
    // Now, remove each DB group which not have a corresponding LDAP group.
    // This will be applied if and only if the option is set by configuration.
    //
    if ($sync_succeed && $this->_forceGroupUpdate)
    {
      foreach ($groups_db as $group_name => $group_id)
      {
        if ($group_name == DEFAULT_USEROBM_GROUPNAME)
        {
          continue;
        }
        if (!array_key_exists($group_name, $groups_ldap))
        {
          $this->_engine->removeUserFromGroup($user_id, $group_id, $domain_id);
        }
      }
    }
    return $sync_succeed;
  }

  /**
   * Manage user informations synchronization.
   * This function will call syncUserAccount, syncUserGroups and
   * syncExternalData if necessary.
   * @param $user_id The user unique identifier.
   * @param $domain_id The domain identifier.
   * @param $username The user name (optional).
   * @param $domain The domain name (optional).
   * @param $groups Groups information (optional).
   * @return The user identifier or false.
   */
  public function syncUser ($user_id, $domain_id, $username = null, $domain = null, $groups = null)
  {
    if (!$this->isEnabled())
    {
      $this->_logger->debug("synchronization is disabled");
      return false;
    }
    if (is_null($username))
    {
      $username = $this->_engine->getUserLogin();
    }
    if (is_null($domain))
    {
      $domain = $this->_engine->getUserDomain();
    }
    if (is_null($groups) || $groups === false || !is_array($groups))
    {
      $groups = $this->_engine->parseGroupsHeader($this->groupsHeaderName);
      $groups = $groups !== false ? $groups : Array();
    }
    //
    // OBM do not considere automatic updates of users and groups.
    // A file is included once here to force the use of redefined
    // functions.
    //
    require_once dirname(__FILE__) . '/functions.inc';
    $this->_logger->info("proceed to synchronization for $username@$domain");
    //
    // Synchronize user information.
    //
    $user_id_sync = $this->syncUserAccount($user_id, $domain_id, $username);
    if ($user_id_sync !== false)
    {
      $this->_logger->info("synchronize user account: SUCCEED");
    }
    else
    {
      $this->_logger->error("synchronize user account: FAILED");
      return false;
    }
    //
    // Synchronize group information.
    //
    if ($this->syncUserGroups($user_id_sync, $domain_id, $groups) !== false)
    {
      $this->_logger->info("synchronize user groups: SUCCEED");
    }
    else
    {
      $this->_logger->error("synchronize user groups: FAILED");
    }
    //
    // Even if groups synchronization does not work, it could have
    // some synchronization to be done. To see if external synchronization
    // are correctly performed, see system log.
    //
    if ($this->_engine->isDataUpdated())
    {
      $this->_logger->info("proceed to external updates");
      $this->syncExternalData($user_id_sync, $domain_id, $username);
    }
    return $user_id_sync;
  }

}

?>
