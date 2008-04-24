<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obmlib.inc                                                   //
//     - Desc : Local PHPLIB file for OBM                                    //
// 1999-03-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$db_type_mysql = "MYSQL";
$db_type_pgsql = "PGSQL";

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
require_once("$obminclude/of/of_session.inc");
require_once("$obminclude/of/of_query.inc");
require_once("$obminclude/of/of_display.inc");

///////////////////////////////////////////////////////////////////////////////
// OBM Classes                                                               //
///////////////////////////////////////////////////////////////////////////////

class OBM_Session extends Session {
}


class OBM_Perm extends Perm {
}


class DB_OBM extends DB_Sql {
  var $Host     = "";
  var $Database = "";
  var $type     = "";
  var $User     = "";
  var $Password = "";
  var $Rows_nolimit = "";

  function DB_OBM ($query = "") {
    global $obmdb_host, $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;

    $this->Host     = "$obmdb_host";
    $this->type     = "$obmdb_dbtype";
    $this->Database = "$obmdb_db";
    $this->User     = "$obmdb_user";
    $this->Password = "$obmdb_password";

    $this->query($query);
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
    $now = date("Y-m-d H:i:s", time());
    $table = $this->database_table;
    $lastpage = "$module:$action";
    // update duration of visit
    $uquery = "UPDATE $table SET
        activeuserobm_userobm_id = '$u_id',
        activeuserobm_timeupdate = '$now',
        activeuserobm_nb_connexions = activeuserobm_nb_connexions + 1,
        activeuserobm_lastpage = '$lastpage'
      WHERE activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $squery = "SELECT count(*)
      FROM $table
      WHERE activeuserobm_userobm_id = '$u_id'
        and activeuserobm_timeupdate = '$now'
        and activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $iquery = "INSERT INTO $table (
        activeuserobm_sid,
        activeuserobm_session_name,
        activeuserobm_userobm_id,
        activeuserobm_timeupdate,
        activeuserobm_timecreate,
        activeuserobm_nb_connexions,
        activeuserobm_lastpage,
        activeuserobm_ip
      ) VALUES ('$id', '$name', '$u_id', '$now', '$now', 1, '$lastpage', '$REMOTE_ADDR')";

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
  var $magic          = "Simsalabim";  // Challenge seed
  var $database_class = "DB_OBM";
  var $database_table = "UserObm";

  function of_session_dis_login_page() {
    global $obminclude, $l_obm_title, $obm_version, $module, $path;
    global $login_action, $obmdb_dbtype, $password_encryption;
    global $params, $c_singleNameSpace, $c_default_domain;

    $login_page = "$path/../$obminclude/login.inc";
    include($login_page);
  }
  
  function auth_validatelogin() {
    global $obm, $params;
    global $c_default_domain, $c_singleNameSpace, $l_error;

    $login = $params["login"];
    $password = stripslashes($params['password']);
    $domain = $params["domain"];
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

    $user_table = $this->database_table;
    $user_login = addslashes($login);

    $query_login = "SELECT
        userobm_id,
        userobm_perms,
        userobm_password,
        userobm_password_type,
        userobm_domain_id,
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
      $pass = $this->db->f("userobm_password");
      // This trim for a strange behaviour while testing oea db
      $crypt = trim(strtoupper($this->db->f("userobm_password_type")));
      $domain_id = $this->db->f("userobm_domain_id");
      $delegation = $this->db->f("userobm_delegation_target");
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
      if ($encrypted == $pass) {
        $unfreeze = global_unfreeze_user($uid);
        if ($unfreeze) {
          $obm["profile"] = $perm;
          $obm["domain_id"] = $domain_id;
          $obm["delegation"] = $delegation;
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
}


class CAS_Auth extends Auth {

  var $lifetime       =  0;
  var $magic          = "CASSimsalabim";  ## Challenge seed
  var $database_class = "DB_OBM";
  var $database_table = "UserObm";

  function of_session_dis_login_page() {
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri,$lock;

    include_once("obmCAS.inc");
    obmCAS::setDebug('/tmp/obm-cas.log');
    if (!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri,true);
      $lock = true;
    }
    obmCAS::forceAuthentication();
  }

  function is_authenticated() {
    global $ticket, $obm;
    $ticket = $_REQUEST["ticket"];
    if (
      $obm["uid"] 
        && 
      (($this->lifetime <= 0) || (time() < $this->auth["exp"]))
    ) {
      # If more than $this->refresh minutes are passed since last check,
      # perform auth data refreshing. Refresh is only done when current
      # session is valid (registered, not expired).
      if (
        ($this->refresh > 0) 
         && 
        ($this->auth["refresh"])
         && 
        ($this->auth["refresh"] < time())
      ) {
        if ( $this->auth_refreshlogin() ) {
          $this->auth["refresh"] = time() + (60 * $this->refresh);
        } else {
          return false;
        }
      }
      $unfreeze = global_unfreeze_user($obm["uid"]);
      if ($unfreeze) {
        return $obm["uid"] ;
      } else {
        return false;
      }
    } elseif (isset($ticket)) {
      return "form" ;
    } else { 
      global_freeze_user($params);
      return false;
    }
  }

  function auth_validatelogin () {
    global $obm, $lock;
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri;

    include_once("obmCAS.inc");
    if (!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri);
      $lock = "true";
    }
    //valider le login CAS
    if (!obmCAS::checkAuthentication()) {
      return false;
    }
    $login = obmCAS::getUser();
    // NTLM authentication hack - prerequisite: login do not contain \
    if (ereg("[\\]", $login)){$tmp = split("[\\]", $login);$login = $tmp[1];}

    $class = $this->database_class;
    $this->db = new $class;

    $this->db->query(sprintf("select userobm_id,
      userobm_perms,
      userobm_domain_id
      from %s
      where userobm_login = '%s'",
			     $this->database_table,
			     addslashes($login)));
    if ($this->db->next_record()) {
      $domain_id = $this->db->f("userobm_domain_id");
      $uid  = $this->db->f("userobm_id");
      $perm = $this->db->f("userobm_perms");
    } else {
      header("location:".obmCAS::getServerBaseURL());
    }

    if (global_allow_access($perm)) {
      $obm['profile'] = $perm;
      $obm['login'] = $login;
      $obm['domain_id'] = $domain_id;
      return $uid;
    } else {
      return false;
    }
  }
   
  function logout($nobody = "") {
    global $lock;
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri;
    
    include_once("obmCAS.inc");
    $_SESSION['auth'] = "";
    unset($this->auth["uname"]);
    $this->unauth($nobody == "" ? $this->nobody : $nobody);
    
    if (!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri);
      $lock = "true";
    }

    obmCAS::logout();
  }
  
}


</script>
