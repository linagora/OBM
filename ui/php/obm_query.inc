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
// OBM - File : obm_query.inc                                                //
//     - Desc : OBM entry page query File                                    //
// 2004-01-20 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Gets the total of task length for The current user
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_days_unfilled() {
  global $cdg_sql, $cdg_param, $obm;
  global $c_day_fraction, $c_working_days;

  $uid = $obm["uid"];

  $u = get_user_info($uid);
  $unix_ts = $u["datebegin"]["unix_timestamp"];
  $iso = $u["datebegin"]["iso"];
  $jan_first = mktime (0, 0, 0, 1, 1, date("Y"));
  if ($unix_ts > $jan_first) {
    $date = $iso;
  } else {
    $date = date("Y0101");
  }

  // First and last days of month
  $query = "
    SELECT
      sum(timetask_length) as total_length
    FROM TimeTask
    WHERE timetask_date <= '".date("Ymd")."'
      AND timetask_date >= '$date'
      AND timetask_user_id = '$uid'
";
  
  display_debug_msg($query, $cdg_sql, "run_query_days_unfilled");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  $filled_days = $obm_db->f("total_length");
  $filled_days = $filled_days/$c_day_fraction;
  $workingdays = 0;
  $current_day = strtotime($date);
  $today = time();
  while($current_day <= $today) {
    $day = date("w",$current_day);
    if ($c_working_days[$day] == 1) {
      $workingdays ++;
    }
    $current_day = strtotime("+1 day",$current_day);
  }

  return $workingdays - $filled_days;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of leads according to date ranges and users
// Parameters:
//   - $date_ranges : ranges to return array(array("2006-10-18",2006-10-22")..
//   - $user_ids    : array of User id
// Returns:
//     array : [total], [alarm], [no_alarm], [0]=#range 0, [n] = range[n]...
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_time_range($date_ranges, $user_ids=array()) {
  global $cdg_sql, $cdg_param;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, "lead_date", "date");
  $datealarm = sql_date_format($db_type, "lead_datealarm", "datealarm");
  $multidomain = sql_multidomain('lead');
  // Manager filter
  if ((is_array($user_ids)) && (count($user_ids)>0)) {
    $w_user .= " AND (";
    $or = "";
    foreach ($user_ids as $u_id) {
      $w_user .= "$or lead_manager_id='$u_id'";
      $or = " OR";
    }
    $w_user .= ")";
  }

  $query = "SELECT
      lead_id,
      $date,
      $datealarm
    FROM Lead
    WHERE 1=1 $w_user $multidomain
";
  
  display_debug_msg($query, $cdg_sql, "run_query_lead_time_range()");
  $obm_q->query($query);
  $today = strtotime(date("Ymd"));

  foreach($date_ranges as $num => $range) {
    $ts_date_ranges[$num] = array (strtotime($range[0]), strtotime($range[1]));
  }

  $res["alarm"] = 0;
  $res["no_alarm"] = 0;
  $res["total"] = 0;
  while ($obm_q->next_record()) {
    $date = $obm_q->f("date");
    $datealarm = $obm_q->f("datealarm");
    foreach($ts_date_ranges as $num => $range) {
      if (($date >= $range[0]) && ($date <= $range[1])) {
	$res["date"][$num]++;
      }
    }
    if ($datealarm != "") {
      if  ($datealarm <= $today) {
	$res["alarm"]++;
      }
    } else {
      $res["no_alarm"]++;
    }
  }

  $res["total"] = $obm_q->num_rows();

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of active deal by status for one user
// Parameters:
//   - $user_id : User id
// Returns:
//     array : array[status_label] = nb
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status($user_id) {
  global $cdg_sql, $cdg_param;

  $query = "SELECT
      count(*) as nb,
      dealstatus_label
    FROM Deal
      LEFT Join DealStatus on deal_status_id = dealstatus_id
      LEFT join DealType on deal_type_id = dealtype_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      AND deal_marketingmanager_id = '$user_id'
      $multidomain
    GROUP BY deal_status_id, dealstatus_label
";
  
  display_debug_msg($query, $cdg_sql, "run_query_deal_status");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $res[$obm_q->f("dealstatus_label")] = $obm_q->f("nb");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of project (by member status) for one user
// Parameters:
//   - $user_id : User id
// Returns:
//     array : array[memberstatus] = nb, array[total] = total
///////////////////////////////////////////////////////////////////////////////
function run_query_project_memberstatus($user_id) {
  global $cdg_sql, $cdg_param;

  $query = "SELECT
      projectuser_manager,
      count(*) as nb
    FROM ProjectUser
      LEFT JOIN Project ON projectuser_project_id=project_id
    WHERE projectuser_projecttask_id is null
      AND projectuser_user_id = '$user_id'
      AND project_archive = '0'
    GROUP BY projectuser_manager
";
  
  display_debug_msg($query, $cdg_sql, "run_query_project_memberstatus");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  $tot = 0;
  while ($obm_q->next_record()) {
    $nb = $obm_q->f("nb");
    $res[$obm_q->f("projectuser_manager")] = $nb;
    $tot += $nb;
  }
  $res["total"] = $tot;

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of active incident by status for one user
// Parameters:
//   - $user_id : User id
// Returns:
//     array : array[status_label] = nb, array[0] = total
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_status($user_id) {
  global $cdg_sql, $cdg_param;

  $query = "SELECT
      count(*) as nb,
      incident_status_id
    FROM Incident
    WHERE incident_archive = '0'
      AND incident_owner = '$user_id'
    GROUP BY incident_status_id 
";
  
  display_debug_msg($query, $cdg_sql, "run_query_incident_status");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $status_label = get_incident_status();
  $tot = 0;
  while ($obm_q->next_record()) {
    $nb = $obm_q->f("nb");
    $res[$status_label[$obm_q->f("incident_status_id")]] = $nb;
    $tot += $nb;
  }
  $res["0"] = $tot;

  return $res;
}

/**
 * get_incident_status 
 * 
 * @param string $target 
 * @access public
 * @return void
 */
function get_incident_status($target='') {
  global $cdg_sql, $cincident_status;

  $sta = array();

  if ($target != '') {
    $status_target[$target] = $cincident_status[$target];
  } else {
    $status_target = $cincident_status;
  }

  foreach ($status_target as $status => $s_id) {
    $s_label = $GLOBALS["l_incidentstatus_$status"];
    $sta[$s_id] = $s_label;
  }

  return $sta;
}
///////////////////////////////////////////////////////////////////////////////
// Get the number of contracts by range of dates (expired,...)
// Parameters:
//   - $user_id : User id
// Returns:
//     array : array[range] = nb
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_range($user_id) {
  global $cdg_sql, $cdg_param;
  global $cr_date_tosign, $cr_date_tobegin, $cr_date_current, $cr_date_torenew, $cr_date_ended;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $ds = sql_date_format($db_type, "contract_datesignature", "datesignature");
  $db = sql_date_format($db_type, "contract_datebegin", "datebegin");
  $de = sql_date_format($db_type, "contract_dateexp", "dateexp");
  $dr = sql_date_format($db_type, "contract_daterenew", "daterenew");

  $query = "SELECT
      $ds,
      $db,
      $de,
      $dr
    FROM Contract
    WHERE contract_archive = '0'
      AND (contract_marketmanager_id = '$user_id'
        OR contract_techmanager_id = '$user_id')
";
  
  display_debug_msg($query, $cdg_sql, "run_query_contract_range");
  $obm_q->query($query);
  
  $res[$cr_date_tosign] = 0;
  $res[$cr_date_tobegin] = 0;
  $res[$cr_date_current] = 0;
  $res[$cr_date_torenew] = 0;
  $res[$cr_date_ended] = 0;
  $now = time();
  while ($obm_q->next_record()) {
    $ds = $obm_q->f("datesignature");
    $db = $obm_q->f("datebegin");
    $de = $obm_q->f("dateexp");
    $dr = $obm_q->f("daterenew");

    if (($ds != "") && ($now < $ds)) {
      $res[$cr_date_tosign]++;
    } else if (($db != "") && ($now < $db)) {
      $res[$cr_date_tobegin]++;
    } else if (($dr != "") && ($now < $dr)) {
      $res[$cr_date_current]++;
    } else if (($de != "") && ($now < $de)) {
      $res[$cr_date_torenew]++;
    } else if (($de != "") && ($now > $de)) {
      $res[$cr_date_ended]++;
    }
  }

  return $res;
}

</script>
