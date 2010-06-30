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
    //
    // First of all, we have to check if headers are set.
    //
    $user   = $this->_engine->getUserLogin();
    $domain = $this->_engine->getUserDomain();
    //
    // If headers are not found, use normal authentication process.
    // The method auth_validatelogin() corresponding to class defined
    // by the constant DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS will be
    // automatically called. We can not use auth_preauth function instead,
    // because it does not the job correctly for us.
    //
    if (strlen($user) == 0)
    {
      $this->_logger->debug('Proceed to non-SSO authentication');
      $d_auth_class_name = DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS;
      $d_auth_object = new $d_auth_class_name ();
      return $d_auth_object->auth_validatelogin();
    }
    //
    // Trace SSO Headers, and check if the request is correct.
    //
    //
    $this->_logger->debug("Headers: "
        . var_export($this->_engine->getHeaders(), true));
    if (!$this->checkLemonldapRequest())
    {
      $this->_logger->warn('Not a valid Lemonldap request, stop authentication');
      return false;
    }
    //
    // Search for ID corresponding to the user and the domain. If the user
    // does not exists, user_id will be false.
    //
    $domain_id = $this->_engine->getDomainID($domain);
    $user_id   = $this->_engine->isUserExists($user, $domain_id);
    $user_id   = $user_id !== false ? $user_id : null;
    //
    // Then, we try to update/create the account, only if the synchronization
    // is allowed. The synchronization could be failed, and the function could
    // return false. In this case, it means that there is something wrong
    // during the synchronization.
    //
    $sync = new LemonLDAP_Sync($this->_engine);
    if ($sync->isEnabled())
    {
      $user_id_sync = $sync->syncUser($user_id, $domain_id, $user, $domain);
      if ($user_id_sync !== false)
      {
        $user_id = $user_id_sync;
      }
    }
    //
    // The synchronization task have to return the user_id: the one
    // created or the one found during an update. Even if the synchronization
    // fails, we authenticate the user.
    // A flag that indicates that user is logged through LemonLDAP is stored.
    // This flag could be then used to personnalize OBM modules, and lock some
    // functionnalities (such as changing OBM password).
    //
    $user_auth = false;
    $user_data = $this->_engine->getUserDataFromId($user_id, $domain_id);
    if (is_array($user_data) && array_key_exists('user_id', $user_data))
    {
      if (global_unfreeze_user($user_data['user_id']))
      {
        $obm['login'] = $user_data['login'];
        $obm['profile'] = $user_data['profile'];
        $obm['domain_id'] = $domain_id;
        $obm['delegation'] = $user_data['delegation_target'];
        $user_auth = $user_data['user_id'];
        $this->_logged = true;
      }
    }
    $this->_logger->info("authentication for $user@$domain: "
        . ($this->_logged ? "SUCCEED" : "FAILED"));
    return $user_auth;
  }

  /**
   * Disconnect the user by destroying its session.
   */
  public function logout ($nobody = '')
  {
    global $obm, $sess;
    //
    // First of all, we have to check if headers are set.
    //
    $user   = $this->_engine->getUserLogin();
    $domain = $this->_engine->getUserDomain();
    //
    // If headers are not found, use normal logout process.
    // The method logout() corresponding to class defined by the constant
    // DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS will be automatically called.
    //
    if (strlen($user) == 0)
    {
      $this->_logger->debug('Proceed to non-SSO logout');
      $d_auth_class_name = DEFAULT_LEMONLDAP_SECONDARY_AUTHCLASS;
      $d_auth_object = new $d_auth_class_name ();
      if (method_exists($d_auth_object, 'logout'))
      {
	return $d_auth_object->logout();
      }
      return;
    }
    //
    // The logout process consist in disconnecting the user from OBM, and
    // then redirecting it to the Lemonldap logout URL.
    //
    $login = $_SESSION['obm']['uid'];
    $sess->delete();
    $_SESSION['obm'] = '';
    $_SESSION['auth'] = '';
    unset($this->auth['uname']);
    $this->unauth($nobody == '' ? $this->nobody : $nobody);
    $sess->delete();
    $this->_logger->info('disconnect ' . $user);
    header('location: ' . $this->_logout_url);
    exit();
  }

  /**
   * Display login page, for second authentication mechanism.
   */
  public function of_session_dis_login_page ()
  {
    global $obminclude, $path;
    include $path . '/../' . $obminclude . '/login.inc';
  }

  /**
   * Main method called by OBM to launch authentication process.
   * A flag is stored into user session, to prevent from infinite loop
   * due to infinite redirection via HTTP header location.
   */
  function start ()
  {
    global $obm, $auth;
    // The following function stops itself, when user is not logged in.
    // In this case, authentication form should be display instead.
    parent::start();
    // Here, user is logged in.
    if (!isset($_SESSION['lemonldap_auth']))
    { 
      $_SESSION['lemonldap_auth'] = true;
      $url_proto  = 'http' . (strcasecmp($_SERVER["HTTPS"], 'on') == 0 ? 's' : '') . '://';
      $url_domain = $_SERVER["HTTP_HOST"];
      $url_query  = $_SERVER["REQUEST_URI"];
      $url        = $url_proto . $url_domain . $url_query;
      header('location: ' . $url);
    }
    return true;
  }

  /**
   * Check if authentication requests is valide.
   * This function checks that headers contains the HTTP_X_FORWADED_FOR header.
   * If not, then if $_SERVER['REMOTE_ADDR'] matches to $_server_ip.
   * @return boolean
   */
  public function checkLemonldapRequest ()
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
  public function isLoggedThroughLemonldap ()
  {
    return $this->_logged;
  }

}

?>
