<?php

//
// $Id$
//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// Copyright (c) 2009, Guillaume Lardon - glardon<at>linagora.com
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
 * LemonLDAP authentication class
 */
class LemonLDAP_Auth extends Auth {

  /**
   * LemonLDAP logout URL.
   * @var String
   */
  private $_logout_url = null;

  /**
   * LemonLDAP Server IP.
   * @var String
   */
  private $_server_ip = null;

  /**
   * Check or not LemonLDAP request.
   * @var Boolean
   */
  private $_server_check = false;

  /**
   * Groups header name.
   * @var String
   */
  private $_groups_header_name = 'HTTP_OBM_GROUPS';

  /**
   * Specify the header that will be trace in debug file.
   * @var String
   */
  private $_debug_header_name = 'HTTP_OBM_UID';

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
   * Indicates if user is already logged or not.
   */
  private $_logged = false;

  /**
   * Backward compatibilities.
   * Still used by Auth object.
   * @var String
   */
  protected $database_class = 'DB_OBM';

  /**
   * Constructor.
   * Initialize internal attribut with parameters found in configuration.
   */
  public function __construct ()
  {
    $database = new $this->database_class;
    $this->_logger = LemonLDAP_Logger::getInstance();
    $this->_engine = new LemonLDAP_Engine();
    $this->_engine->setDatabase($database);
    $this->initializeFromConfiguration();
  }

  /**
   * Set internal attributes from parameters found in configuration.
   * @param $config Array of parameters.
   */
  public function initializeFromConfiguration ($config = null)
  {
    global $lemonldap_config;
    if (is_null($config))
    {
      $config = $lemonldap_config;
    }
    if (array_key_exists('url_logout', $config) !== false)
    {
      $this->_logout_url = $config['url_logout'];
    }
    if (array_key_exists('server_ip_address', $config) !== false)
    {
      $this->_server_ip = $config['server_ip_address'];
    }
    if (array_key_exists('server_ip_server_check', $config) !== false)
    {
      $this->_server_check = $config['server_ip_server_check'];
    }
    if (array_key_exists('debug_header_name', $config) !== false)
    {
      $this->_debug_header_name = $config['debug_header_name'];
    }
    if (array_key_exists('group_header_name', $config) !== false)
    {
      $this->_groups_header_name = $config['group_header_name'];
    }
    if (array_key_exists('headers_map', $config) !== false)
    {
      if (is_array($config['headers_map']))
      {
        $this->_engine->setHeadersMap($config['headers_map']);
      }
    }
  }

  /**
   * This function retrieve information from headers, and starts a session
   * automatically for the user found.
   * @return boolean
   */
  public function auth_validatelogin ()
  {
    global $obm;
    
    $this->_logger->debug("Headers: "
        . var_export($this->_engine->getHeaders(), true));

    // Check if headers are not found, use normal authentication process.
    // The method auth_validatelogin() corresponding to class defined
    // by the constant DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS will be automatically called.
    // We can not use auth_preauth function instead, because it does not
    // the job correctly for us.

    if (!$this->sso_isValidAuthenticationRequest()) {
      $this->_logger->debug('not a valid authentication request, proceed to auth failover');
      $d_auth_class_name = DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS;
      $d_auth_object = new $d_auth_class_name ();
      return $d_auth_object->auth_validatelogin();
    }
    if (!$this->sso_checkRequest())
    {
      return false;
    }

    //
    // First of all, we have to check if the user exists.
    // OBM stores login in lowercase. Groups are also retrieved here.
    //

    $header = $this->_engine->getHeaderName('userobm_login');
    $username = $this->_engine->getHeaderValue($header);
    $login = $this->_engine->getUserLogin($username);
    $domain_id = $this->_engine->getDomainID($username);
    $user_id = $this->_engine->isUserExists($login, $domain_id);
    $groups = $this->_engine->parseGroupsHeader($this->_groups_header_name);

    //
    // Then, we try to update/create the account. If this operation failed we
    // can not do anything else. If not it is not necessary for the user to be
    // authenticated so that its groups informations will be updated too.
    // If the synchronization tool can synchronize, then we do sync operations.
    //

    $sync = new LemonLDAP_Sync($this->_engine);
    if ($sync->isEnabled()) {
      $user_id = $sync->syncUserInfo(
          $login,
          $groups !== false ? $groups : Array(),
          $user_id,
          $domain_id
        );
    }

    // Now, authenticate the user.

    if (!$user_authenticated = $this->sso_authenticate($user_id, $domain_id))
    {
      $user_id = null;
    }

    $this->_logger->info('authentication for '
        . $this->_engine->getHeaderValue($this->_debug_header_name)
        . ' (' . (is_null($user_id) ? 'FAILED' : 'SUCCEED') . ')');

    //
    // If $user_id is null, then user is not authenticated. In the case $user_id
    // is not null, a flag that indicates that user is logged through LemonLDAP
    // is stored. This flag could be then used to personnalize OBM modules, and
    // lock some functionnalities (such as changing OBM password).
    //

    if (!is_null($user_id))
    {
      $this->_logged = true;
    }

    return $user_authenticated;
  }

  /**
   * Disconnect the user by destroying its session.
   */
  public function logout ($nobody = '')
  {
    global $obm, $sess;

    if (!$this->sso_isValidAuthenticationRequest())
    {
      $this->_logger->debug('not a valid authentication request, proceed to auth failover');
      $d_auth_class_name = DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS;
      $d_auth_object = new $d_auth_class_name ();
      if (method_exists($d_auth_object, 'logout'))
      {
	return $d_auth_object->logout();
      }
      return;
    }

    $login = $_SESSION['obm']['uid'];
    $sess->delete();
    $_SESSION['obm'] = '';
    $_SESSION['auth'] = '';
    unset($this->auth['uname']);
    $this->unauth($nobody == '' ? $this->nobody : $nobody);
    $sess->delete();

    $this->_logger->info('disconnect '
        . $this->_engine->getHeaderValue($this->_debug_header_name));

    header('location: ' . $this->_logout_url);
    exit();
  }

  /**
   * Display login page.
   * For LemonLDAP authentication process, we display a blank page. ???
   */
  public function of_session_dis_login_page ()
  {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = $path.'/../'.$obminclude.'/login.inc';
    include($login_page);
  }

  /**
   * Authenticate user
   * @param $user_id The user unique identifier.
   * @param $domain_id The domain identifier.
   * @return int The user identifier, or false.
   */
  public function sso_authenticate ($user_id, $domain_id)
  {
    global $obm;

    $data = $this->_engine->getUserDataFromId($user_id, $domain_id);

    if (!is_array($data) || !array_key_exists('user_id', $data))
    {
      return false;
    }

    if (global_unfreeze_user($data['user_id']))
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
   * Check if authentication requests is valide.
   * This function checks that headers contains the HTTP_X_FORWADED_FOR header.
   * If not, then if $_SERVER['REMOTE_ADDR'] matches to $_server_ip.
   * @return boolean
   */
  public function sso_checkRequest ()
  {
    if (!$this->_server_check)
    {
      return true;
    }
    $hn = 'HTTP_X_FORWARDED_FOR';
    $hv = $this->_engine->getHeaderValue($hn);
    $succeed = false;
    if (($hv !== false && strcasecmp(trim($hv), $this->_server_ip) == 0)
	|| strcasecmp($_SERVER['REMOTE_ADDR'], $this->_server_ip) == 0)
    {
      $succeed = true;
    }
    $this->_logger->info($succeed ? "SUCCEED" : "FAILED");
    return $succeed;
  }

  /**
   * This will tell us if user is logged through SSO or not.
   * @return boolean
   */
  public function sso_isLoggedThrough()
  {
    return $this->_logged;
  }

  /**
   * This will tell us if we could use headers to authenticate a user.
   * The obligatory header correspond to the key userobm_login.
   */
  public function sso_isValidAuthenticationRequest ()
  {
    $header = $this->_engine->getHeaderName('userobm_login');
    $login = $this->_engine->getHeaderValue($header);

    if (!is_null($login))
    {
      return true;
    }

    return false;
  }

}

?>
