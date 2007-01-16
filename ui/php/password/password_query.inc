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
      userobm_login,
      userobm_password
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
  global $auth, $cdg_sql, $cdg_exe, $cgp_use, $cmd_ldap_passwd, $password_encryption;

  $login = $usr_q->f("userobm_login");
  $old_pass = trim($password["old_password"]);
  $new_pass = trim($password["new_password"]);

  if ($password_encryption == "crypt") {
    $new_passwd_crypt = crypt($new_pass);
  } else if ($password_encryption == "md5") {
    $new_passwd_crypt = md5($new_pass);
  } else {
    $new_passwd_crypt = $new_pass;
  }

  $passwd_update = "userobm_password='$new_passwd_crypt',
    userobm_password_plain='$new_pass'";

  // Modification a chaud du mot de passe dans le LDAP
  // XXXX cas user non encore cree (uniquement dans base maj) ?
  if ($cgp_use["service"]["samba"]) {
    $cmd = "$cmd_ldap_passwd --samba --unix --login $login --passwd-plain $new_pass --old-passwd $old_pass";
  }else {
    $cmd = "$cmd_ldap_passwd --unix --login $login --passwd-plain $new_pass --old-passwd $old_pass";
  }

  display_debug_msg($cmd, $cdg_exe, "run_query_password_update()");
  exec($cmd);

  $query = "UPDATE UserObm SET
    $passwd_update
  WHERE userobm_id = '$uid'";

  display_debug_msg("DB_OBM : $query", $cdg_sql, "run_query_password_update()");
  // Update database update
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  $usr_q = new DB_OBM_Prod;
  $retour = $usr_q->query($query);

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
  global $err_msg, $err_field, $action, $cdg_sql;

  $id = $usr_q->f("userobm_id");
  $login = $usr_q->f("userobm_login");
  $pass = $usr_q->f("userobm_password");

  $old_pass = trim($password["old_password"]);
  $new_pass = trim($password["new_password"]);
  $new_pass_bis = trim($password["new_password_bis"]);

  if ($action == "update") {
    // Verifie que les mots de passe sont saisis
    if (($old_pass == "") || ($new_pass == "")) {
      $err_msg = "$l_fill_password";
      return false;
    }

    // Verifie que les 2 mots de passe nouveaux sont identiques
    if ($new_pass != $new_pass_bis) {
      $err_msg = "$l_new_password_differ";
      return false;
    }

    // Verifie que le mot de passe actuel est correct
    if ($password_encryption == "crypt") {
      $old_passwd_crypt = crypt($old_pass);
    } else if ($password_encryption == "md5") {
      $old_passwd_crypt = md5($old_pass);
    } else {
      $old_passwd_crypt = $old_pass;
    }
    if ($pass != $old_passwd_crypt) {
      $err_msg = "$l_wrong_password";
      return false;
    }

    // Password is correct
    if ( (trim($new_pass) == "")
	 || (preg_match($php_regexp_password, $new_pass) == false) ) {
      $err_msg = "$l_password_invalid";
      $err_field = "new_password";
      return false;
    }
  }

  return true;
}


</script>
