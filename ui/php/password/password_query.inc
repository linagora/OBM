<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT userobm_id,
      userobm_domain_id,
      userobm_login,
      userobm_password,
      userobm_password_type,
      userobm_samba_perms
    FROM UserObm
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_password()');
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
function run_query_password_update($password, $uid, $usr_info_q) {
  global $cdg_sql, $cdg_exe, $cgp_use, $cmd_ldap_passwd, $password_encryption;

  $login = $usr_info_q->f('userobm_login');
  $domain_id = $usr_info_q->f('userobm_domain_id');
  $samba_perms = $usr_info_q->f('userobm_samba_perms');

  $usr_q = new DB_OBM;
  $new_pass = stripslashes(trim($password['new_password']));
  $did = sql_parse_id($domain_id, true);
  $uid = sql_parse_id($uid, true);
  if ($password_encryption == 'CRYPT') {
    $new_passwd_crypt = crypt($new_pass);
  } else if ($password_encryption == 'MD5SUM') {
    $new_passwd_crypt = md5($new_pass);
  } else {
    $new_passwd_crypt = $new_pass;
  }
  
  $passwd_update = '';

  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm SET
    userobm_password='".$usr_q->escape($new_passwd_crypt)."',
    userobm_password_type='$password_encryption'
  WHERE userobm_id $uid ";

  display_debug_msg("DB_OBM : $query", $cdg_sql, 'run_query_password_update()');
  // Update database update
  $retour = $usr_q->query($query);

  // Update 'P_UserObm' and system ONLY if the user already exist in
  // 'P_UserObm'
  $query = "SELECT count(*) AS nb_user FROM P_UserObm WHERE userobm_login='".$login."' AND userobm_domain_id $did";
  $error = $usr_q->query($query);
  $usr_q->next_record();

  if ($usr_q->f('nb_user') == 1 && $cgp_use['service']['ldap']) {

    $query = "UPDATE P_UserObm SET
      userobm_password='".$usr_q->escape($new_passwd_crypt)."',
      userobm_password_type='$password_encryption'
    WHERE userobm_id $uid ";

    $retour = $usr_q->query($query);

    // Modification a chaud du mot de passe dans le LDAP
    // Uniquement dans le cas où la mise à jour de la table de prooduction c'est
    // produite sans erreur (tuple inexistant...)
    if ($retour) {
      $parameters = ' --unix';

      if ($samba_perms && ($cgp_use['service']['samba'] || $password_encryption != 'PLAIN')) {
        $parameters .= ' --samba';
      }

      if ($parameters) {
        $cmd = $cmd_ldap_passwd.$parameters.' --login '.$login.' --domain-id '.$domain_id." --passwd '".$new_pass."' --no-old";

        display_debug_msg($cmd, $cdg_exe, 'run_query_password_update()');
        exec($cmd);
      }
    }
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

  $id = $usr_q->f('userobm_id');
  $login = $usr_q->f('userobm_login');
  $pass = $usr_q->f('userobm_password');
  $passtype = $usr_q->f('userobm_password_type');


  # On supprime les caractères invisibles en début et fin de chaines de l'ancien
  # mot de passe
  # On enlève les caractères d'échapement de l'ancien mot de passe
  $old_pass = stripslashes(trim($password['old_password']));

  # On supprime les caractères invisibles en début et fin de chaines des nouvaux
  # mots de passe - les caractères d'échapement sont enlevés au besoin (appel de
  # l'automate, ...)
  $new_pass = stripslashes(trim($password['new_password']));
  $new_pass_bis = stripslashes(trim($password['new_password_bis']));
  if ($action == 'update') {
    // Verifie que le mot de passe actuel est correct
    if ($passtype == 'CRYPT') {
      $old_passwd_crypt = crypt($old_pass,$pass);
    } else if ($passtype == 'MD5SUM') {
      $old_passwd_crypt = md5($old_pass);
    } else {
      $old_passwd_crypt = $old_pass;
    }
    if ($pass != $old_passwd_crypt) {
      $err['msg'] = "$l_wrong_password";
      $err['field'] = 'old_password';
      return false;
    }

    // Verifie que les mots de passe sont saisis
    if (($old_pass == '') || ($new_pass == '')) {
      $err['msg'] = "$l_fill_password";
      $err['field'] = 'new_password';
      return false;
    }

    // Verifie que les 2 mots de passe nouveaux sont identiques
    if ($new_pass != $new_pass_bis) {
      $err['msg'] = "$l_new_password_differ";
      $err['field'] = 'new_password_bis';
      return false;
    }

    // Password is correct
    //    $check_passwd = format_name(stripslashes($new_pass), false, true);
    if ( (trim($new_pass) == '')
        || (preg_match($php_regexp_password, $new_pass) == false) ) {
      $err['msg'] = "$l_password_invalid";
      $err['field'] = 'new_password';
      return false;
    }
  }

  return true;
}


</script>
