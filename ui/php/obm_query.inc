<script language="php">
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
  global $cdg_sql, $cdg_param, $auth;
  global $c_day_fraction, $c_working_days;

  $uid = $auth->auth["uid"];

  $u = get_user_info($uid);
  $begindate = $u["datebegin"];
  $unix_ts = $begindate["unix_timestamp"];
  $iso = $begindate["iso"];
  $jan_first = mktime (0, 0, 0, 1, 1, date("Y"));
  if ($unix_ts > $jan_first) {
    $date = $iso;
  } else {
    $date = date("Y0101");
  }

  // First and last days of month
  $query = "
    select
      sum(timetask_length) as total_length
    from TimeTask
    where timetask_date <= '".date("Ymd")."'
    and timetask_date >= '$date'
      and timetask_user_id = '$uid'
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
// XXXX Exists in obm_query.inc too
// Get the amount, balanced amount and number of deals for a set of users
// Parameters:
//   - $users_id : array of User id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_potential($users_id="") {
  global $cdg_sql, $cdg_param;

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $where_market_uid = "AND (";
    $where_tech_uid = "AND (";
    $or = "";
    foreach ($users_id as $u_id) {
      $where_market_uid .= " $or deal_marketingmanager_id='$u_id'";
      $where_tech_uid .= " $or deal_technicalmanager_id='$u_id'";
      $or = "OR";
    }
    $where_market_uid .= ")";
    $where_tech_uid .= ")";
  }

  $query = "SELECT
      deal_marketingmanager_id AS uid,
      count(*) as nb,
      sum(deal_amount) as amount,
      sum(deal_amount * deal_hitrate / 100) as amount_balanced,
      userobm_lastname,
      userobm_firstname
    FROM Deal
      LEFT JOIN DealType ON deal_type_id = dealtype_id
      LEFT JOIN UserObm ON deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_market_uid
    GROUP BY deal_marketingmanager_id,
      userobm_lastname,
      userobm_firstname
";
  
  display_debug_msg($query, $cdg_sql, "run_query_deal_potential(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $am = 0;
  $ab = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    $res["market"]["$u_id"]["amount"] = $obm_q->f("amount");
    $res["market"]["$u_id"]["amount_balanced"] = $obm_q->f("amount_balanced");
    $res["market"]["$u_id"]["number"] = $obm_q->f("nb");
    $res["market"]["$u_id"]["name"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
    $am += $res["market"]["$u_id"]["amount"];
    $ab += $res["market"]["$u_id"]["amount_balanced"];
    $nb += $res["market"]["$u_id"]["number"];
  }

  $res["0"]["amount"] = $am;
  $res["0"]["amount_balanced"] = $ab;
  $res["0"]["number"] = $nb;

  // Tech manager stats
  $query = "SELECT
      deal_technicalmanager_id AS uid,
      count(*) as nb,
      sum(deal_amount) as amount,
      sum(deal_amount * deal_hitrate / 100) as amount_balanced,
      userobm_lastname,
      userobm_firstname
    FROM Deal
      LEFT JOIN DealType ON deal_type_id = dealtype_id
      LEFT JOIN UserObm ON deal_technicalmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_tech_uid
    GROUP BY deal_technicalmanager_id,
      userobm_lastname,
      userobm_firstname
";

  display_debug_msg($query, $cdg_sql, "run_query_deal_potential(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $am = 0;
  $ab = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    $res["tech"]["$u_id"]["amount"] = $obm_q->f("amount");
    $res["tech"]["$u_id"]["amount_balanced"] = $obm_q->f("amount_balanced");
    $res["tech"]["$u_id"]["number"] = $obm_q->f("nb");
    $res["tech"]["$u_id"]["name"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
    $am += $res["tech"]["$u_id"]["amount"];
    $ab += $res["tech"]["$u_id"]["amount_balanced"];
    $nb += $res["tech"]["$u_id"]["number"];
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the amount, balanced amount and number of deals for a set of users
// Parameters:
//   - $users_id : array of User id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_potential2($users_id="") {
  global $cdg_sql, $cdg_param;

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $where_uid = "AND (";
    $or = "";
    foreach ($users_id as $u_id) {
      $where_uid .= " $or deal_marketingmanager_id='$u_id'";
      $or = "OR";
    }
    $where_uid .= ")";
  }

  $query = "SELECT
      deal_marketingmanager_id AS uid,
      count(*) as nb,
      sum(deal_amount) as amount,
      sum(deal_amount * deal_hitrate / 100) as amount_balanced,
      userobm_lastname,
      userobm_firstname
    FROM Deal
      LEFT join DealType on deal_type_id = dealtype_id
      LEFT join UserObm on deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_uid
    GROUP BY deal_marketingmanager_id, userobm_lastname, userobm_firstname
";
  
  display_debug_msg($query, $cdg_sql, "run_query_deal_potential");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $am = 0;
  $ab = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    $res["$u_id"]["amount"] = $obm_q->f("amount");
    $res["$u_id"]["amount_balanced"] = $obm_q->f("amount_balanced");
    $res["$u_id"]["number"] = $obm_q->f("nb");
    $res["$u_id"]["name"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
    $am += $res["$u_id"]["amount"];
    $ab += $res["$u_id"]["amount_balanced"];
    $nb += $res["$u_id"]["number"];
  }

  $res["0"]["amount"] = $am;
  $res["0"]["amount_balanced"] = $ab;
  $res["0"]["number"] = $nb;

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// XXXX Exists in deal_query.inc too
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
      incidentstatus_label
    FROM Incident
      LEFT Join IncidentStatus on incident_status_id = incidentstatus_id
    WHERE incident_archive = '0'
      AND incident_owner = '$user_id'
    GROUP BY incident_status_id, incidentstatus_label
";
  
  display_debug_msg($query, $cdg_sql, "run_query_incident_status");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  $tot = 0;
  while ($obm_q->next_record()) {
    $nb = $obm_q->f("nb");
    $res[$obm_q->f("incidentstatus_label")] = $nb;
    $tot += $nb;
  }
  $res["0"] = $tot;

  return $res;
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
