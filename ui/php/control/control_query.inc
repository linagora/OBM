<script language="php">
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
// OBM - File : control_query.inc                                            //
//     - Desc : Control panel query & db File                                //
// 2002-09-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get number of user defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_user_infos($domain_id='') {
  global $cdg_sql;

  if (! ($domain_id > 0)) {
    $obm = $_SESSION['obm'];
    $domain_id = $obm['domain_id'];
  }

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "AND userobm_domain_id $domain_id"; 
  }

  // get # users
  $query = "SELECT count(*) AS nb FROM P_UserObm WHERE 1=1 $where_domain";

  display_debug_msg($query, $cdg_sql, 'get_control_user_infos(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb'] = $obm_q->f('nb');

  // get # archive users
  $query = "SELECT count(*) AS nb FROM P_UserObm
    WHERE userobm_archive=1
      $where_domain";

  display_debug_msg($query, $cdg_sql, 'get_control_user_infos(2)');
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb_archive'] = $obm_q->f('nb');

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Get number of group defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_group_infos($domain_id='') {
  global $cdg_sql;

  if (! ($domain_id > 0)) {
    $obm = $_SESSION['obm'];
    $domain_id = $obm['domain_id'];
  }

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "AND group_domain_id $domain_id"; 
  }

  // get # groups

  $query = "SELECT count(*) AS nb FROM P_UGroup WHERE 1=1 $where_domain";
  display_debug_msg($query, $cdg_sql, 'get_control_group_infos(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb_public'] = $obm_q->f('nb');

  // get # private groups

  $query = "SELECT count(*) AS nb FROM UGroup
    WHERE group_privacy = 1
      $where_domain";
  display_debug_msg($query, $cdg_sql, 'get_control_group_infos(2)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb_private'] = $obm_q->f('nb');

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Get number of mailshare defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_mailshare_infos($domain_id='') {
  global $cdg_sql;

  if (! ($domain_id > 0)) {
    $obm = $_SESSION['obm'];
    $domain_id = $obm['domain_id'];
  }

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "WHERE mailshare_domain_id $domain_id"; 
  }

  $query = "SELECT count(*) AS nb FROM P_MailShare $where_domain";

  display_debug_msg($query, $cdg_sql, 'get_control_mailshare_infos()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get disk free space
///////////////////////////////////////////////////////////////////////////////
function get_sys_disk_use() {
  global $var_device;

  $ret = exec("df -m | grep '$var_device' | cut -b52-55");

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get mail statistics
// Returns:
//   - $m[] with values : mail_*
///////////////////////////////////////////////////////////////////////////////
function get_mail_stats() {
  global $cdg_sql;

  $query = "SELECT stats_name, stats_value
  FROM Stats
  WHERE stats_name like 'mail_%'";

  display_debug_msg($query, $cdg_sql, 'get_mail_stats()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $name = $obm_q->f('stats_name');
    $value = $obm_q->f('stats_value');
    $m["$name"] = $value;
  }

  return $m;
}


///////////////////////////////////////////////////////////////////////////////
function check_data($control) {

  return true;
}


</script>
