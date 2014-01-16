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
// OBM - File : mailforward_query.inc                                        //
//     - Desc : Mail Forwarding query & db File                              //
// 2002-09-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Mail Forwardig query execution
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailforward_detail($u_id) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT userobm_id,
      userobm_domain_id,
      userobm_login,
      userobm_email,
      userobm_nomade_perms,
      userobm_nomade_enable,
      userobm_email_nomade,
      userobm_nomade_local_copy,
      domain_name
    FROM UserObm
    LEFT JOIN Domain ON domain_id = userobm_domain_id
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_mailforward_detail()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User (mail forwarding info) update query execution
// As this action is "hot plug", Production and Update databases are updated
// Parameters:
//   - $forward[] : entry values
//     keys used  : email_nomade, nomade_enable
//   - $u_id       : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailforward_update($forward, $u_id) {
  global $cdg_sql;

  $usr_q = new DB_OBM;
  $email_nomade = $forward["email_nomade"];
  $nomade_enable = sql_parse_int($forward["nomade_enable"]);
  $nomade_local_copy = sql_parse_int($forward["nomade_local_copy"]);  
  $u_id = sql_parse_id($u_id, true);
  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm set
    userobm_email_nomade    = '".$usr_q->escape($email_nomade)."',
    userobm_nomade_enable   = $nomade_enable,
    userobm_nomade_local_copy=$nomade_local_copy    
  WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_mailforward_update()');
  $retour = $usr_q->query($query);

  $query = "UPDATE P_UserObm set
    userobm_email_nomade    = '".$usr_q->escape($email_nomade)."',
    userobm_nomade_enable   = $nomade_enable,
    userobm_nomade_local_copy=$nomade_local_copy    
  WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_mailforward_update()');
  $retour = $usr_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Tell if the nomade email is allowed
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function nomade_is_allowed($u_id) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT userobm_nomade_perms
    FROM UserObm
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, 'nomade_is_allowed()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('userobm_nomade_perms');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Execute the Change Alias command according paratemers given
// Parameters:
//   - $usr_q     : current user values (used are email, login, nomade_perms)
//   - $forward[] : values to set
///////////////////////////////////////////////////////////////////////////////
function exec_change_alias($usr_q, $forward) {
  global $cmd_mailforward, $cdg_exe;

  $login = $usr_q->f('userobm_login');
  $domain = $usr_q->f('userobm_domain_id');

  // Si le forward est autorisé et activé
  if ( ($usr_q->f('userobm_nomade_perms')) && ($forward['nomade_enable']) ) {
    $dst = $forward['email_nomade'];
  } else {
    // pas autorise ou inactif, destination est le compte local
    $dst = $usr_q->f('userobm_login');
  }

  $cmd = $cmd_mailforward.' --login '.$login.' --domain-id '.$domain;
  display_debug_msg($cmd, $cdg_exe);
  exec($cmd);

  return;
}


///////////////////////////////////////////////////////////////////////////////
// MailForward Data checking and formatting
// Parameters:
//   - $usr_q     : User mail forwarding database result
//   - $forward[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_mailforward_data_form($usr_q, $forward) {
  global $l_nomade_not_allowed, $l_err_email, $l_email_nomad_error_loop;
  global $php_regexp_email, $l_err_nomade_local_copy, $l_email_nomad_exist;
  global $err, $action;

  $email = $usr_q->f('userobm_email');
  $nomade_perms = $usr_q->f('userobm_nomade_perms');
  $email_nomade = stripslashes($forward['email_nomade']);
  $nomade_enable = $forward['nomade_enable'];
  $nomade_local_copy = $forward['nomade_local_copy'];
  
  // Should not happen here !
  if (! $nomade_perms) {
    $err['msg'] = $l_nomade_not_allowed;
    return false;
  }

  // Email nomade must be filled if nomade activated
  if ( ($nomade_enable) && (trim($email_nomade) == '')) {
    $err['msg'] = "$email_nomade : $l_err_email";
    $err['field'] = 'email_nomade';
    return false;
  }

  // User nomad email
  if ($email_nomade != '') {
    $emails_nomade = array();
    $em = strtok(strtolower($email_nomade),"\r\n");
    while ($em) {

      if (!preg_match($php_regexp_email, $em)) {
        $err['msg'] = "$em : $l_email_error";
        $err['field'] = 'email_nomade';
        return false;
      }

      if (in_array($em, $emails_nomade)) {
        $err['msg'] = "$l_email_nomade : $l_uemail_exist : $em";
        $err['field'] = 'email_nomade';
        return false;
      }
      $emails_nomade[] = $em;

      $em = strtok("\r\n");
    }
  }

  // User nomade local copy only if autorized and activated
  if ( ($nomade_local_copy) && ((! $nomade_perms) || (! $nomade_enable)) ) {
    $err['msg'] = $l_err_nomade_local_copy;
    $err['field'] = 'nomade_local_copy';
    return false;
  }

  // Check that nomad email doesn't loop to one of the user email
  if ( ($nomade_perms) && ($nomade_enable) ) {
    $u = get_user_info();
    $domain_aliases = array($u['domain_name']);
    $one_dom = strtok($u['domain_alias'], "\r\n");
    while ($one_dom) {
      $domain_aliases[] = $one_dom; 
      $one_dom = strtok("\r\n");
    }
    $user_emails = array();
    $one_email = strtok($email, "\r\n");
    while ($one_email) {
      $user_emails[] = $one_email; 
      $one_email = strtok("\r\n");
    }

    foreach ($user_emails as $one_email) {
      if (strpos($one_email, '@')) {
	if (in_array($one_email, $emails_nomade)) {
	  $err['msg'] = "$l_email_nomad_error_loop";
	  $err['field'] = 'email_nomade';
	  return false;
	}
      } else {
	// email name (@ left side), check with all domain aliases
	foreach ($domain_aliases as $one_dom) {
	  if (in_array($one_email.'@'.$one_dom, $emails_nomade)) {
	    $err['msg'] = "$l_email_nomad_error_loop";
	    $err['field'] = 'email_nomade';
	    return false;
	  }
	}	    
      }
    }
  }
  
  return true;
}
