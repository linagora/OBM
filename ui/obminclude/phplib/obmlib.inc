<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obmlib.inc                                                   //
//     - Desc : Local PHPLIB file for OBM                                    //
// 1999-03-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$db_type_mysql = 'MYSQL';
$db_type_pgsql = 'PGSQL';

///////////////////////////////////////////////////////////////////////////////
// Required packages                                                         //
///////////////////////////////////////////////////////////////////////////////
// Package for the selected DB backend
if ($obmdb_dbtype == $db_type_pgsql) {
  require_once("$obminclude/phplib/db_pgsql.inc");
} elseif ($obmdb_dbtype == $db_type_mysql) {
  require_once("$obminclude/phplib/db_mysql.inc");
} else {
  require_once("$obminclude/phplib/db_mysql.inc");
}

require_once("$obminclude/phplib/ct_sql.inc");
require_once("$obminclude/of/of_query.inc");
require_once("$obminclude/of/of_captcha.php");
require_once("$obminclude/of/of_session.inc");
require_once("$obminclude/of/of_display.inc");

///////////////////////////////////////////////////////////////////////////////
// OBM Classes                                                               //
///////////////////////////////////////////////////////////////////////////////

class OBM_Session extends Session {
}


class OBM_No_Session extends Session {
  function OBM_No_Session() {
    global $cs_lifetime, $cgp_cookie_name, $cgp_cookie_domain;

    $this->cookie_name   = "$cgp_cookie_name-Anonymous";
    $this->cookie_domain = "$cgp_cookie_domain";

    if ($cs_lifetime > 0) {
      $this->lifetime = $cs_lifetime;
    }
  }  
}


class OBM_Perm extends Perm {
}


class DB_OBM extends DB_Sql {
  var $Host     = '';
  var $Database = '';
  var $type     = '';
  var $User     = '';
  var $Password = '';
  var $Rows_nolimit = '';
  var $obm_query_exportall = '';

  function DB_OBM ($query = '') {
    global $obmdb_host, $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;

    $this->Host     = "$obmdb_host";
    $this->type     = "$obmdb_dbtype";
    $this->Database = "$obmdb_db";
    $this->User     = "$obmdb_user";
    $this->Password = "$obmdb_password";

    parent::__construct($query);
  }

  // AliaSource : Set the total rows number (case query is constraint by limit)
  function set_num_rows_total ($total) {
    $this->Rows_nolimit = $total;
  }

  // AliaSource : Get the total rows number (case query is constraint by limit)
  function num_rows_total () {
    if ($this->Rows_nolimit != "")
      return $this->Rows_nolimit;
    else
      return $this->num_rows();
  }
}

class OBM_User_Sql extends CT_Sql {
  var $database_class = "DB_OBM";         // Which database to connect...
  var $database_table = "ActiveUserObm";  // our session data in this table.

  function ac_store($id, $name, $u_id) {
    global $module, $REMOTE_ADDR, $action;

    $ret = true;
    $name = addslashes($name);
    $table = $this->database_table;
    $lastpage = "$module:$action";
    // update duration of visit
    $uid = sql_parse_id($u_id);
    $uquery = "UPDATE $table SET
        activeuserobm_userobm_id = $uid,
        activeuserobm_timeupdate = NOW(),
        activeuserobm_nb_connexions = activeuserobm_nb_connexions + 1,
        activeuserobm_lastpage = '$lastpage'
      WHERE activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $uid = sql_parse_id($u_id, true);
    $squery = "SELECT count(*)
      FROM $table
      WHERE activeuserobm_userobm_id $uid 
        and activeuserobm_timeupdate = NOW() 
        and activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $uid = sql_parse_id($u_id);
    $iquery = "INSERT INTO $table (
        activeuserobm_sid,
        activeuserobm_session_name,
        activeuserobm_userobm_id,
        activeuserobm_timeupdate,
        activeuserobm_timecreate,
        activeuserobm_nb_connexions,
        activeuserobm_lastpage,
        activeuserobm_ip
      ) VALUES ('$id', '$name', $uid, NOW(), NOW(), 1, '$lastpage', '$REMOTE_ADDR')";

    $this->db->query($uquery);

    // FIRST test to see if any rows were affected.
    //   Zero rows affected could mean either there were no matching rows
    //   whatsoever, OR that the update statement did match a row but made
    //   no changes to the table data (i.e. UPDATE tbl SET col = 'x', when
    //   "col" is _already_ set to 'x') so then,
    // SECOND, query(SELECT...) on the sid to determine if the row is in
    //   fact there,
    // THIRD, verify that there is at least one row present, and if there
    //   is not, then
    // FOURTH, insert the row as we've determined that it does not exist.
 
    if ( $this->db->affected_rows() == 0
        && $this->db->query($squery)
	&& $this->db->next_record() && $this->db->f(0) == 0
        && !$this->db->query($iquery)) {

        $ret = false;
    }
    return $ret;
  }
}


class OBM_Challenge_Auth extends Auth {

  var $lifetime       =  86400;
  var $magic          = 'Simsalabim';  // Challenge seed
  var $database_class = 'DB_OBM';
  var $database_table = 'UserObm';

  function of_session_dis_login_page() {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action, $obmdb_dbtype, $password_encryption;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = "$path/../$obminclude/login.inc";
    include_once($login_page);
  }
  
  function auth_validatelogin() {
    global $obm, $params;
    global $c_default_domain, $c_singleNameSpace, $l_error;
    
    if ( !$this->auth_validate_captcha() ) {
      $params["auth_captcha_failed"] = true;
      return false;
    }

    if(isset($params['auth_ticket'])) {
      $udata = $this->check_ticket_auth($params['auth_ticket']);
      if(!$udata) {
        return false;
      }
      $params = array_merge($params,$udata);
    }
    $login = $params['login'];
    $password = stripslashes($params['password']);

    $class = $this->database_class;
    $this->db = new $class;

    //if(!$login && $_SERVER['PHP_AUTH_USER']) {
    //  $login = $_SERVER['PHP_AUTH_USER'];
    //  $password = $_SERVER['PHP_AUTH_PW'];
    //}
    $domain = $params['domain_id'];
    if(!$c_singleNameSpace) {
      if(strpos($login, '@') !== false) {
        list($login, $domain) = split('@',$login);
        $domain_query = 'domain_name';
      } else {
        $domain_query = 'userobm_domain_id';
      }      
      // if no domain set, set the default domain
      if ((! isset($domain)) || ($domain == '')) {
        $domain = "$c_default_domain";
      }
      $where_domain = "AND $domain_query = '".$this->db->escape($domain)."'";

    }
    if (isset($login)) {
      $obm['login'] = $login; // provides access for loginform.ihtml
    }

    $user_table = $this->database_table;
    $user_login = addslashes($login);

    $query_login = "SELECT
        userobm_id,
        userobm_perms,
        userobm_password,
        userobm_password_type,
        userobm_domain_id,
        userobm_delegation,
        userobm_delegation_target
      FROM $user_table
      LEFT JOIN Domain ON userobm_domain_id = domain_id
      WHERE userobm_login = '$user_login'
        AND userobm_archive = 0
        $where_domain";
    $this->db->query($query_login);
    while ($this->db->next_record()) {
      $uid  = $this->db->f("userobm_id");
      $perm = $this->db->f("userobm_perms");
      $pass = $this->db->f("userobm_password");
      // This trim for a strange behaviour while testing oea db
      $crypt = trim(strtoupper($this->db->f("userobm_password_type")));
      $domain_id = $this->db->f("userobm_domain_id");
      $delegation = $this->db->f('userobm_delegation');
      $delegation_target = $this->db->f('userobm_delegation_target');
    }

    switch($crypt) {
      case "MD5SUM": 
        $encrypted = md5($password);
        break;
      case "CRYPT":
        $encrypted = crypt($password,$pass);
        break;
      case "PLAIN":
        $encrypted = $password;
        break;
      default:
        break;
    }
    if (global_allow_access($perm)) {
      if (isset($params['auth_ticket']) || $encrypted == $pass) {
        $unfreeze = global_unfreeze_user($uid);
        if ($unfreeze) {
          $obm['profile'] = $perm;
          $obm['domain_id'] = $domain_id;
          $obm['delegation'] = $delegation;
          $obm['delegation_target'] = $delegation_target;
          return $uid;
        } else {
          return false;
        }
      } else {
        global_freeze_user($params);
        return false;
      }
    } else {
      return false;
    }

  }

  /**
   * Check the given ticket against the OBM SSO server.
   * If ok, get the username else return false 
   */
  function check_ticket_auth($ticket) {
    $url = $GLOBALS['cgp_host']."/sso/sso_index.php?action=validate&ticket=$ticket";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $buf = curl_exec ($ch);
    if ( $buf === FALSE || $buf == '') {
      return false;
    }
    curl_close ($ch);
    if($buf == 'invalidOBMTicket') {
      return false;
    }
    parse_str($buf,$values);
    return $values;
  }

}

class CAS_Auth extends Auth {

  var $lifetime       =  0;
  var $magic          = 'CASSimsalabim';  ## Challenge seed
  var $database_class = 'DB_OBM';
  var $database_table = 'UserObm';
  var $client_inited  = false;

  function of_session_dis_login_page() {
    $this->createClient();
    phpCAS::forceAuthentication();
  }

  function is_authenticated() {
    global $ticket, $obm;
    $ticket = $_REQUEST['ticket'];
    if (
      $obm['uid']
        && 
      (($this->lifetime <= 0) || (time() < $this->auth['exp']))
    ) {
      # If more than $this->refresh minutes are passed since last check,
      # perform auth data refreshing. Refresh is only done when current
      # session is valid (registered, not expired).
      if (
        ($this->refresh > 0)
         &&
        ($this->auth['refresh'])
         &&
        ($this->auth['refresh'] < time())
      ) {
        if ( $this->auth_refreshlogin() ) {
          $this->auth['refresh'] = time() + (60 * $this->refresh);
        } else {
          return false;
        }
      }
      $unfreeze = global_unfreeze_user($obm['uid']);
      if ($unfreeze) {
        return $obm['uid'];
      } else {
        return false;
      }
    } elseif (isset($ticket)) {
      return 'form';
    } else { 
      global_freeze_user($params);
      return false;
    }
  }

  function auth_validate_callback() {
    global $obm, $cas_server_login_attribute;

    if(empty($cas_server_login_attribute)) {
      $tmpLogin = phpCAS::getUser();
    }
    else {
      $tmpLogin = phpCAS::getAttribute($cas_server_login_attribute);
      if(empty($tmpLogin)) {
        $tmpLogin = phpCAS::getUser();
      }
    }
    $login = $tmpLogin;

    // NTLM authentication hack - prerequisite: login do not contain \
    if (ereg("[\\]", $login)){$tmp = split("[\\]", $login);$login = $tmp[1];}

    $class = $this->database_class;
    $this->db = new $class;

    $this->db->query(sprintf("select userobm_id,
      userobm_perms,
      userobm_domain_id,
      userobm_delegation,
      userobm_delegation_target
      from %s
      where userobm_login = '%s'",
			     $this->database_table,
			     addslashes($login)));
    if ($this->db->next_record()) {
      $domain_id = $this->db->f('userobm_domain_id');
      $uid  = $this->db->f('userobm_id');
      $perm = $this->db->f('userobm_perms');
      $delegation = $this->db->f('userobm_delegation');
      $delegationTarget = $this->db->f('userobm_delegation_target');
    } else {
	die("Login ".htmlentities($login)." not found in OBM database. Aborting");
    }
    if (global_allow_access($perm)) {
      $obm['profile'] = $perm;
      $obm['login'] = $login;
      $obm['domain_id'] = $domain_id;
      $obm['uid'] = $uid;
      $obm['delegation'] = $delegation;
      $obm['delegation_target'] = $delegationTarget;

      $obm = $this->add_domain_infos($obm);
      $_SESSION["obm"] = $obm;
      return $uid;
    } else {
      return false;
    }
  }

  function auth_validatelogin () {
    $this->createClient();

    //valider le login CAS
    if (!phpCAS::checkAuthentication()) {
      return false;
    }
    return $this->auth_validate_callback();
  }

  function logout($nobody = '') {
    $_SESSION['auth'] = '';
    unset($this->auth['uname']);
    $this->unauth($nobody == '' ? $this->nobody : $nobody);
    $this->createClient();
    phpCAS::logout();
  }
  
  function createClient() {
    global $obminclude, $cas_server, $cas_server_port, $cas_server_uri, $cas_debug;
    global $cas_validation, $cas_cert;
    if ($this->client_inited) {
      return ;
    }
    require_once("$obminclude/lib/CAS/CAS.php");
    if ( $cas_debug ) {
      phpCAS::setDebug($cas_debug);
    }

    phpCAS::client(CAS_VERSION_2_0, $cas_server, $cas_server_port, $cas_server_uri, false);
    if ($cas_validation == 'ca') {
        phpCAS::setCasServerCACert($cas_cert);
    }
    else {
        phpCAS::setNoCasServerValidation();
    }
    phpCAS::setPostAuthenticateCallback(array($this,"auth_validate_callback"));

    $this->client_inited = true;
  }
}


class LDAP_Auth extends Auth {

  var $lifetime       = 86400;
  var $magic          = 'Simsalabim';  // Challenge seed
  var $database_class = 'DB_OBM';
  var $database_table = 'UserObm';
  static public $storePassword  = FALSE;

  function of_session_dis_login_page() {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = $path.'/../'.$obminclude.'/login.inc';
    include_once($login_page);
  }

  function auth_validatelogin() {
    global $obm, $params;
    global $c_default_domain, $c_singleNameSpace, $l_error;

    if ( !$this->auth_validate_captcha() ) {
      $params["auth_captcha_failed"] = true;
      return false;
    }

    self::$storePassword = $GLOBALS['c_store_password'];
    if(isset($params['auth_ticket'])) {
      $udata = $this->check_ticket_auth($params['auth_ticket']);
      if(!$udata) {
        return false;
      }
      $params = array_merge($params,$udata);
    }

    $login = $params["login"];
    $domain = $params["domain_id"];
    $password = stripslashes($params['password']);
    if(!$c_singleNameSpace) {
      if(strpos($login,"@") !== false) {
        list($login, $domain) = split("@",$login);
        $domain_query = "domain_name";
      } else {
        $domain_query = "userobm_domain_id";
      }

      // if no domain set, set the default domain
      if ((! isset($domain)) || ($domain == "")) {
        $domain = "$c_default_domain";
      }
      $where_domain = "AND $domain_query = '$domain'";
    }

    if (isset($login)) {
      $obm["login"] = $login; // provides access for loginform.ihtml
    }

    // authenticate user against ldap server, return uid or false
    $user_table = $this->database_table;
    $user_login = addslashes($login);

    $query_login = "SELECT
                      userobm_id,
                      userobm_login,
                      domain_name,
                      userobm_perms,
                      userobm_domain_id,
                      userobm_delegation,
                      userobm_delegation_target
                    FROM $user_table
                    LEFT JOIN Domain ON userobm_domain_id = domain_id
                    WHERE userobm_login = '$user_login'
                      AND userobm_archive = '0'
                      $where_domain";

    $class = $this->database_class;
    $this->db = new $class;

    $this->db->query($query_login);
    while ($this->db->next_record()) {
      $uid  = $this->db->f("userobm_id");
      $perm = $this->db->f("userobm_perms");
      $domain_id = $this->db->f("userobm_domain_id");
      $delegation = $this->db->f('userobm_delegation');
      $delegation_target = $this->db->f('userobm_delegation_target');
      $dbLogin = $this->db->f("userobm_login");
      $domain_name = $this->db->f("domain_name");
    }

    if( !$dbLogin ) {
      error_log( "User '".$user_login."' unknown in OBM database" );
    }

    if (global_allow_access($perm)) {
      $unfreeze = global_unfreeze_user($uid);
      if (isset($params['auth_ticket']) || $this->auth_ldap_validatelogin($dbLogin, $password, $domain_name)) {
        if(self::$storePassword) {
          $this->db->query("UPDATE UserObm set userobm_password='$password', userobm_password_type='PLAIN', userobm_timeupdate=userobm_timeupdate WHERE userobm_id = $uid");
        }
        if ($unfreeze) {
          $obm['profile'] = $perm;
          $obm['domain_id'] = $domain_id;
          $obm['delegation'] = $delegation;
          $obm['delegation_target'] = $delegation_target;
          return $uid;
        } else {
          return false;
        }
      } else {
        global_freeze_user($params);
        return false;
      }
    } else {
      return false;
    }
    
  }


  /**
   * Check the given ticket against the OBM SSO server.
   * If ok, get the username else return false 
   */
  function check_ticket_auth($ticket) {
    $url = $GLOBALS['cgp_host']."/sso/sso_index.php?action=validate&ticket=$ticket";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $buf = curl_exec ($ch);
    if ( $buf === FALSE || $buf == "") {
      return false;
    }
    curl_close ($ch);
    if($buf == "invalidOBMTicket") {
      return false;
    }
    parse_str($buf,$values);
    return $values;
  }

  function auth_ldap_validatelogin($login, $password, $domain){
    global $auth_ldap_server, $auth_ldap_basedn, $auth_ldap_binddn;
    global $auth_ldap_bindpw, $auth_ldap_filter, $auth_ldap_userid_attribute;
    global $auth_ldap_attribute, $auth_ldap_sizelimit;

    if(!$login) {
      return false;
    }
    if(trim($password) == '') {
      return false;
    }
    $auth_ldap_attribute = array("dn", $auth_ldap_userid_attribute);
    $auth_ldap_sizelimit = 1;
    $auth_ldap_filter = str_replace('%u', $login, $auth_ldap_filter);
    $auth_ldap_filter = str_replace('%d', $domain, $auth_ldap_filter);
    
    $ldapconn=ldap_connect($auth_ldap_server);
    if (! ldap_set_option($ldapconn, LDAP_OPT_PROTOCOL_VERSION, 3)) {
      ldap_set_option($ldapconn, LDAP_OPT_PROTOCOL_VERSION, 2) or
        error_log( 'Failed to negociate LDAP protocol version with LDAP server : '.$auth_ldap_server );
        die('Failed to negociate LDAP protocol version with LDAP server : '.$auth_ldap_server);
    }
    	
    if (isset($auth_ldap_binddn) and strlen($auth_ldap_binddn) > 0) {
      $ldapbindconn=@ldap_bind($ldapconn,$auth_ldap_binddn,$auth_ldap_bindpw);
    } else {
      $ldapbindconn=@ldap_bind($ldapconn);
    }
    	
    if ($ldapbindconn) {
      $result=ldap_search($ldapconn, $auth_ldap_basedn,$auth_ldap_filter,
      $auth_ldap_attribute, 0, $auth_ldap_sizelimit);
      if ($result and ldap_count_entries($ldapconn, $result) > 0) {
        $ldap_data = ldap_get_entries($ldapconn, $result);
        $ldap_userdn = $ldap_data[0]["dn"];
	// Lowercase the attribute because that's what php-ldap expects
        $ldap_useruid = $ldap_data[0][strtolower($auth_ldap_userid_attribute)][0];
      } else {
        error_log( "Duplicate LDAP entry for user '".$login."', domaine '".$domain."'" );
        return false;
      }
  
      if (@ldap_bind($ldapconn, $ldap_userdn, $password)) {
        return $ldap_useruid;
      } else {
        error_log( "Bad password for user '".$login."', domaine '".$domain."'" );
        return false;
      }
    } else {
      error_log( 'Connection failed to LDAP server : '.$auth_ldap_server );
      return false;
    }
  }
}


class OBM_Token_Auth extends Auth {

  var $lifetime       =  1;
  var $magic          = 'Simsalabim';  // Challenge seed
  var $database_class = 'DB_OBM';
  var $database_table = 'UserObm';
  var $nobody         = true;

  function of_session_dis_login_page() {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action, $obmdb_dbtype, $password_encryption;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = "$path/../$obminclude/login.inc";
    include_once($login_page);
  }
  
  function auth_validatelogin() {
    global $obm, $params;
    global $c_default_domain, $c_singleNameSpace, $l_error;
      
    $GLOBALS['profiles']['external']['module']['default'] = $GLOBALS['perm_user'];
    $GLOBALS['profiles']['external']['section']['default'] = $GLOBALS['perm_user'];
    $obm['login'] = "anonymous"; // provides access for loginform.ihtml
    $obm['profile'] = 'user';
    $obm['domain_id'] = '1';
    $obm['delegation'] = '';
    $obm['delegation_target'] = '';
    return 0;

  }

  function auth_preauth() {
    $class = $this->database_class;
    $this->db = new $class;    
    if($_REQUEST['externalToken']) {
      $token = $_REQUEST['externalToken'];
    } elseif($_SESSION['externalToken']) {
      $token = $_SESSION['externalToken'];
    } else {
      return false;
    }
    $GLOBALS['profiles']['__external']['module']['default'] = 0;
    $GLOBALS['profiles']['__external']['section']['default'] = 0;
    $this->db->query("SELECT * FROM token WHERE token='$token'");
    if($this->db->nf() > 0) {      
      $GLOBALS['obm']['profile'] = '__external';
      $_SESSION['externalToken'] = $token;
      $GLOBALS['obm']['login'] = $token;
      while($this->db->next_record()) {
        if(isset($GLOBALS['token'][$this->db->f('property')])) {
          if(!is_array($GLOBALS['token'][$this->db->f('property')])) {
            $GLOBALS['token'][$this->db->f('property')] = array($GLOBALS['token'][$this->db->f('property')]);
          }
           array_push($GLOBALS['token'][$this->db->f('property')], $this->db->f('value'));
        } else {
          $GLOBALS['token'][$this->db->f('property')] = $this->db->f('value');
        }
      }
      if(isset($GLOBALS['token']['domain'])) $GLOBALS['obm']['domain_id'] = $GLOBALS['token']['domain']; else return false;
      if(isset($GLOBALS['token']['delegation'])) $GLOBALS['obm']['delegation'] = $GLOBALS['token']['delegation']; else return false;
      if(isset($GLOBALS['token']['delegation'])) $GLOBALS['obm']['delegation_target'] = $GLOBALS['token']['domain']; else $GLOBALS['obm']['delegation_target'] = $GLOBALS['token']['delegation'];
      if(isset($GLOBALS['token']['uid'])) $uid = $GLOBALS['token']['uid']; else $uid = 0;
      if(isset($GLOBALS['token']['readAccess'])) {
        if(!is_array($GLOBALS['token']['readAccess'])) $GLOBALS['token']['readAccess'] = array($GLOBALS['token']['readAccess']);
        foreach($GLOBALS['token']['readAccess'] as $module) {
          $GLOBALS['profiles']['__external']['module'][$module] = $GLOBALS['perm_user'];
        }
      }
      if(isset($GLOBALS['token']['writeAccess'])) {
        if(!is_array($GLOBALS['token']['writeAccess'])) $GLOBALS['token']['writeAccess'] = array($GLOBALS['token']['writeAccess']);
        foreach($GLOBALS['token']['writeAccess'] as $module) {
          $GLOBALS['profiles']['__external']['module'][$module] = $GLOBALS['perm_editor'];
        }
      }
      return $uid;
    }
    return false;
  }
}

class OBM_No_Auth extends Auth {

  var $lifetime       =  1;
  var $magic          = 'Simsalabim';  // Challenge seed
  var $database_class = 'DB_OBM';
  var $nobody         = true;

  function of_session_dis_login_page() {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action, $obmdb_dbtype, $password_encryption;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = "$path/../$obminclude/login.inc";
    include_once($login_page);    
  }

  function auth_validatelogin() {
    return 0;

  }

  function auth_preauth() {
    if(!$_GET['login']) {
      return false;
    }

    list($login, $domain) = split('@',$_GET['login']);
    $query_login = "SELECT
        userobm_id,
        userobm_perms,
        userobm_domain_id,
        userobm_delegation,
        userobm_delegation_target
      FROM UserObm 
      INNER JOIN Domain ON userobm_domain_id = domain_id
      WHERE userobm_login = '$login'
      AND userobm_archive = 0
      AND domain_name = '$domain'";
    $class = $this->database_class;
    $this->db = new $class;

    $this->db->query($query_login);
    while ($this->db->next_record()) {
      $GLOBALS['obm']['profile'] = $this->db->f("userobm_perms");
      $GLOBALS['obm']['domain_id'] = $this->db->f("userobm_domain_id");
      $GLOBALS['obm']['delegation'] = $this->db->f('userobm_delegation');
      $GLOBALS['obm']['delegation_target'] = $this->db->f('userobm_delegation_target');
      $GLOBALS['obm']['login'] = $login;
      return $this->db->f("userobm_id");
    }
    
    return false;
//    $class = $this->database_class;
//    $this->db = new $class;    
//    if($_REQUEST['externalToken']) {
//      $token = $_REQUEST['externalToken'];
//    } elseif($_SESSION['externalToken']) {
//      $token = $_SESSION['externalToken'];
//    } else {
//      return false;
//    }
//    $GLOBALS['profiles']['__external']['module']['default'] = 0;
//    $GLOBALS['profiles']['__external']['section']['default'] = 0;
//    $this->db->query("SELECT * FROM token WHERE token='$token'");
//    if($this->db->nf() > 0) {      
//      $GLOBALS['obm']['profile'] = '__external';
//      $_SESSION['externalToken'] = $token;
//      $GLOBALS['obm']['login'] = $token;
//      while($this->db->next_record()) {
//        if(isset($GLOBALS['token'][$this->db->f('property')])) {
//          if(!is_array($GLOBALS['token'][$this->db->f('property')])) {
//            $GLOBALS['token'][$this->db->f('property')] = array($GLOBALS['token'][$this->db->f('property')]);
//          }
//           array_push($GLOBALS['token'][$this->db->f('property')], $this->db->f('value'));
//        } else {
//          $GLOBALS['token'][$this->db->f('property')] = $this->db->f('value');
//        }
//      }
//      if(isset($GLOBALS['token']['domain'])) $GLOBALS['obm']['domain_id'] = $GLOBALS['token']['domain']; else return false;
//      if(isset($GLOBALS['token']['delegation'])) $GLOBALS['obm']['delegation'] = $GLOBALS['token']['delegation']; else return false;
//      if(isset($GLOBALS['token']['delegation'])) $GLOBALS['obm']['delegation_target'] = $GLOBALS['token']['domain']; else $GLOBALS['obm']['delegation_target'] = $GLOBALS['token']['delegation'];
//      if(isset($GLOBALS['token']['uid'])) $uid = $GLOBALS['token']['uid']; else $uid = 0;
//      if(isset($GLOBALS['token']['readAccess'])) {
//        if(!is_array($GLOBALS['token']['readAccess'])) $GLOBALS['token']['readAccess'] = array($GLOBALS['token']['readAccess']);
//        foreach($GLOBALS['token']['readAccess'] as $module) {
//          $GLOBALS['profiles']['__external']['module'][$module] = $GLOBALS['perm_user'];
//        }
//      }
//      if(isset($GLOBALS['token']['writeAccess'])) {
//        if(!is_array($GLOBALS['token']['writeAccess'])) $GLOBALS['token']['writeAccess'] = array($GLOBALS['token']['writeAccess']);
//        foreach($GLOBALS['token']['writeAccess'] as $module) {
//          $GLOBALS['profiles']['__external']['module'][$module] = $GLOBALS['perm_editor'];
//        }
//      }
//      return $uid;
//    }
//    return false;
  }
}
</script>
