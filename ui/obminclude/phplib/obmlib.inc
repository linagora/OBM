<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obmlib.inc                                                   //
//     - Desc : Local PHPLIB file for OBM                                    //
// 1999-03-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Required packages                                                         //
///////////////////////////////////////////////////////////////////////////////
//require("$obminclude/phplib/db_pgsql.inc");       //Postgres
require("$obminclude/phplib/db_mysql.inc");     //Mysql
require("$obminclude/phplib/ct_sql.inc");
require("$obminclude/phplib/session4.inc");
require("$obminclude/phplib/auth.inc");
require("$obminclude/phplib/perm.inc");
require("$obminclude/phplib/page4.inc");

$db_type_mysql="MYSQL";

//$db_type_pgsql="PGSQL";
///////////////////////////////////////////////////////////////////////////////
// OBM Classes                                                               //
///////////////////////////////////////////////////////////////////////////////

class DB_OBM extends DB_Sql {
  var $Host     = "";
  var $Database = "";
  var $type     = "";
  var $User     = "";
  var $Password = "";

  function DB_OBM () {
    global $obmdb_host, $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;

    $this->Host     = "$obmdb_host";
    $this->type     = "$obmdb_dbtype";
    $this->Database = "$obmdb_db";
    $this->User     = "$obmdb_user";
    $this->Password = "$obmdb_password";
  }
}


class OBM_CT_Sql extends CT_Sql {
  var $database_class = "DB_OBM";           ## Which database to connect...
  var $database_table = "ActiveSessions";  ## and find our session data in this table.
}


class OBM_User_Sql extends CT_Sql {
  var $database_class = "DB_OBM";         ## Which database to connect...
  var $database_table = "ActiveUserObm";  ## our session data in this table.

  function ac_store($id, $name, $str) {
    $ret = true;
    $name = addslashes($name);

    ## update duration of visit
    global $HTTP_REFERER, $HTTP_SERVER_VARS, $HTTP_USER_AGENT, $REMOTE_ADDR, $action;
    $module_name = substr(strrchr($HTTP_SERVER_VARS['SCRIPT_NAME'],"/"),1);
    $module_name = substr($module_name,0,strlen($module_name)-10);
  //  $module_name = substr(strrchr("/",$HTTP_SERVER_VARS['SCRIPT_NAME']),0,strpos(strrchr("/",$HTTP_SERVER_VARS['SCRIPT_NAME']),"_"));
    $now = date("YmdHis", time());
    $uquery = sprintf("update %s
               set activeuserobm_userobm_id='%d',
                 activeuserobm_timeupdate='%s', 
                 activeuserobm_nb_connexions = activeuserobm_nb_connexions + 1,
                 activeuserobm_lastpage= '%s'
	       where activeuserobm_sid='%s'
               and activeuserobm_session_name='%s'",
      $this->database_table,
      $str,
      $now,
      ucfirst($module_name).":".ucfirst($action),
      $id,
      $name);
    $squery = sprintf("select count(*)
        from %s
        where activeuserobm_userobm_id='%s'
          and activeuserobm_timeupdate='%s' 
          and activeuserobm_sid='%s'
          and activeuserobm_session_name='%s'",
      $this->database_table,
      $str,
      $now,
      $id,
      $name);
    $iquery = sprintf("insert into %s
          (activeuserobm_sid,
           activeuserobm_session_name,
           activeuserobm_userobm_id,
           activeuserobm_timeupdate,
           activeuserobm_timecreate,
           activeuserobm_nb_connexions,
           activeuserobm_lastpage,
           activeuserobm_ip) 
        values ('%s', '%s', '%d', '%s','%s', '%d', '%s', '%s')",
      $this->database_table,
      $id,
      $name,
      $str,
      $now,
      $now,
      1, 
      ucfirst($module_name).":".ucfirst($action),
      $REMOTE_ADDR);

    $this->db->query($uquery);

    # FIRST test to see if any rows were affected.
    #   Zero rows affected could mean either there were no matching rows
    #   whatsoever, OR that the update statement did match a row but made
    #   no changes to the table data (i.e. UPDATE tbl SET col = 'x', when
    #   "col" is _already_ set to 'x') so then,
    # SECOND, query(SELECT...) on the sid to determine if the row is in
    #   fact there,
    # THIRD, verify that there is at least one row present, and if there
    #   is not, then
    # FOURTH, insert the row as we've determined that it does not exist.
 
    if ( $this->db->affected_rows() == 0
        && $this->db->query($squery)
	&& $this->db->next_record() && $this->db->f(0) == 0
        && !$this->db->query($iquery)) {

        $ret = false;
    }
    return $ret;
  }
}


class OBM_Session extends Session {
  var $classname 	= "OBM_Session";
  var $cookiename     	= "";           ## defaults to classname
  var $mode           	= "cookie";     ## We propagate session ID with cookies
  var $lifetime_default = 0x7fffffff;   ## session cookie if global lifetime=0
  var $that_class     	= "OBM_CT_Sql"; ## Which database to connect...
  var $allowcache	= "" ;
}


class OBM_Challenge_Auth extends Auth {
  var $classname      = "OBM_Challenge_Auth";
  var $lifetime       =  1200;

  var $magic          = "Simsalabim";  ## Challenge seed
  var $database_class = "DB_OBM";
  var $database_table = "UserObm";

  function auth_loginform() {
    global $sess, $obminclude, $obm_version, $menu, $path, $challenge;

    $login_form = "$obminclude/auth/crloginform.ihtml";
    $challenge = md5(uniqid($this->magic));
    $sess->register("challenge");
    
    include($login_form);
  }
  
  function auth_validatelogin() {
    global $login, $password, $challenge, $response;

    if(isset($login)) {
      $this->auth["uname"]=$login;     ## This provides access for "loginform.ihtml"
    }
    $this->db->query(sprintf("select userobm_id,userobm_perms,userobm_password ".
                "from %s where userobm_login = '%s' and userobm_archive = '0'",
                          $this->database_table,
                          addslashes($login)));

    while($this->db->next_record()) {
      $uid   = $this->db->f("userobm_id");
      $perm  = $this->db->f("userobm_perms");
      $pass  = $this->db->f("userobm_password");
    }
    $exspected_response = md5("$login:$pass:$challenge");

    ## True when JS is disabled
    if ($response == "") {
      if ($password != $pass) {
        return false;
      } else {
        $this->auth["perm"] = $perm;
        return $uid;
      }
    }
    
    ## Response is set, JS is enabled
    if ($exspected_response != $response) {
      return false;
    } else {
      $this->auth["perm"] = $perm;
      return $uid;
    }
  }
}


class OBM_Perm extends Perm {
  var $classname = "OBM_Perm";

  function OBM_Perm (){
    global $perm_user, $perm_editor ,$perm_admin;
    $this-> permissions = array(
                            "user"       => $perm_user,
                            "author"     => 3,
                            "editor"     => $perm_editor,
                            "supervisor" => 15,
                            "admin"      => $perm_admin
                          );
  }

  function perm_invalid($does_have, $must_have) {
     global $perm, $auth, $sess, $obminclude,$l_error, $l_invalid_perm;
     global $perm_invalid,$menu,$section;
     display_head($l_error);     // Head & Body 
     generate_menu($menu,$section);         // Menu
     display_err_msg($l_invalid_perm);
     display_end();
  }

  function check() {
    global $auth;
    global $actions, $action, $menu;
    if(($this->permissions[$auth->auth["perm"]] & $actions[$menu][$action]["Right"]) != $actions[$menu][$action]["Right"]) {
      $this->perm_invalid($auth->auth["perm"], $p);
      exit();
    }
  }

}


</SCRIPT>
