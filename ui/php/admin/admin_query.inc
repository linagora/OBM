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
// OBM - File : admin_query.inc                                              //
//     - Desc : Admin Query File                                             //
// 2001-12-27 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Query execution : Return the number of entries in ActiveSessions Table    //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Get the passive session number                                            //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - number of passive session
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_count_passive_sess($delay) {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $time_pass = date("Y-m-d H:i:s",time() - $delay);
  $query = "select count(*) as num
    from ActiveUserObm
    INNER JOIN UserObm ON activeuserobm_userobm_id = userobm_id
    where ActiveUserObm.activeuserobm_timeupdate <= '$time_pass' $multidomain";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("num");
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of User session (rows in ActiveUserObm)                    //
// Returns:
//   - number of user session
///////////////////////////////////////////////////////////////////////////////
function get_admin_usersession_number() {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $query = "select count(*) as num from ActiveUserObm INNER JOIN UserObm ON activeuserobm_userobm_id = userobm_id WHERE 1=1 $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("num");
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - List of Active User session (not older than delay)      //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - DBO : list of active user session
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_active_user($delay) {
  global $cdg_sql, $c_activeUserObmMaxRows;

  // if lifetime = 0 (or bad:< 0) we consider all session actives.
  if ($delay > 0) {
    $date_limit = date("Y-m-d H:i:s",time() - $delay);
    $where_date = "and ActiveUserObm.activeuserobm_timeupdate >= '$date_limit'";
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain('userobm');

  $timeupdate = sql_date_format($db_type, "activeuserobm_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "activeuserobm_timecreate", "timecreate");

  $query = "SELECT UserObm.userobm_login, UserObm.userobm_timelastaccess,
                   ActiveUserObm.*, $timecreate, $timeupdate
            FROM UserObm, ActiveUserObm
            WHERE ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
            $where_date $multidomain
            LIMIT $c_activeUserObmMaxRows";
  display_debug_msg($query,$cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - List of Passive User session (older than delay)         //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - DBO : list of passive user session
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_passive_user($delay) {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $date_limit = date("Y-m-d H:i:s",time() - $delay);
  $query = "select UserObm.userobm_login,
                   ActiveUserObm.*
            from UserObm, ActiveUserObm
            where ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
              and ActiveUserObm.activeuserobm_timeupdate < '$date_limit' $multidomain";
  display_debug_msg($query,$cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query Execution - Purge ActiveUserObm entries                             //
// Parameters:
//   - $ltime : lifetime
// Returns:
//   - DBO : list of passive user session
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_activeuserobm_purge($ltime) {
  global $cdg_sql;

  $date_limit = date('Y-m-d H:i:s',time() - $ltime);
  $query = "DELETE FROM ActiveUserObm WHERE activeuserobm_timeupdate < '$date_limit'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}
?>
