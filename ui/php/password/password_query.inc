<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : password_query.inc                                           //
//     - Desc : Pasword query                                                //
// 2004-03-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Password query execution
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function run_query_password($u_id) {
  global $cdg_sql;

  $query = "SELECT userobm_id,
      userobm_domain_id,
      userobm_login,
      userobm_password,
      userobm_password_type
    FROM UserObm
    WHERE userobm_id='$u_id'";

  display_debug_msg($query, $cdg_sql, "run_query_password()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User Password update query execution
// As this action is "hot plug", Update database and LDAP are updated
// No need to store it in production Database
// Parameters:
//   - $password[] : entry values
//     keys used   : email_nomade, nomade_enable
//   - $uid        : user id
//   - $usr_q      : User Password database result
///////////////////////////////////////////////////////////////////////////////
function run_query_password_update($password, $uid, $usr_q) {
  global $cdg_sql, $cdg_exe, $cgp_use, $cmd_ldap_passwd, $password_encryption;

  $login = $usr_q->f("userobm_login");
  $domain_id = $usr_q->f("userobm_domain_id");;
  $old_pass = trim($password["old_password"]);
  $new_pass = trim($password["new_password"]);

  if ($password_encryption == "CRYPT") {
    $new_passwd_crypt = crypt($new_pass);
  } else if ($password_encryption == "MD5SUM") {
    $new_passwd_crypt = md5($new_pass);
  } else {
    $new_passwd_crypt = $new_pass;
  }

  $passwd_update = "";

  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm SET
    userobm_password='$new_passwd_crypt',
    userobm_password_type='$password_encryption'
  WHERE userobm_id = '$uid'";

  display_debug_msg("DB_OBM : $query", $cdg_sql, "run_query_password_update()");
  // Update database update
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  // Update 'P_UserObm' ands system ONLY if the user already exist in
  // 'P_UserObm'
  $query = "SELECT count(*) AS nb_user FROM P_UserObm WHERE userobm_login='".$login."' AND userobm_domain_id='".$domain_id."'";
  $error = $usr_q->query($query);
  $usr_q->next_record();

  if( $usr_q->f("nb_user") == 1 ) {

    $query = "UPDATE P_UserObm SET
      userobm_password='$new_passwd_crypt',
      userobm_password_type='$password_encryption'
    WHERE userobm_id = '$uid'";

    $retour = $usr_q->query($query);

    // Modification a chaud du mot de passe dans le LDAP
    // XXXX cas user non encore cree (uniquement dans base maj) ?
    $cmd = "$cmd_ldap_passwd --unix --login $login --domain $domain_id --type $password_encryption --passwd $new_pass --old-passwd $old_pass";

    display_debug_msg($cmd, $cdg_exe, "run_query_password_update()");
    exec($cmd);
  }




  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $usr_q      : User Password database result
//   - $password[] : values checked
//     keys used  : all
///////////////////////////////////////////////////////////////////////////////
function check_data_form($usr_q, $password) {
  global $l_fill_password, $l_wrong_password, $l_new_password_differ, $l_password_invalid;
  global $php_regexp_password, $password_encryption;
  global $err, $action, $cdg_sql;

  $id = $usr_q->f("userobm_id");
  $login = $usr_q->f("userobm_login");
  $pass = $usr_q->f("userobm_password");
  $passtype = $usr_q->f("userobm_password_type");


  $old_pass = trim($password["old_password"]);
  $new_pass = trim($password["new_password"]);
  $new_pass_bis = trim($password["new_password_bis"]);
  if ($action == "update") {
    // Verifie que le mot de passe actuel est correct
    if ($passtype == "CRYPT") {
      $old_passwd_crypt = crypt($old_pass,$pass);
    } else if ($passtype == "MD5SUM") {
      $old_passwd_crypt = md5($old_pass);
    } else {
      $old_passwd_crypt = $old_pass;
    }
    if ($pass != $old_passwd_crypt) {
      $err["msg"] = "$l_wrong_password";
      $err["field"] = "old_password";
      return false;
    }

    // Verifie que les mots de passe sont saisis
    if (($old_pass == "") || ($new_pass == "")) {
      $err["msg"] = "$l_fill_password";
      $err["field"] = "new_password";
      return false;
    }

    // Verifie que les 2 mots de passe nouveaux sont identiques
    if ($new_pass != $new_pass_bis) {
      $err["msg"] = "$l_new_password_differ";
      $err["field"] = "new_password_bis";
      return false;
    }

    // Password is correct
    if ( (trim($new_pass) == "")
	 || (preg_match($php_regexp_password, $new_pass) == false) ) {
      $err["msg"] = "$l_password_invalid";
      $err["field"] = "new_password";
      return false;
    }
  }

  return true;
}


</script>
