<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : deal_query.inc                                               //
//     - Desc : Deal query File                                              //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Deal Search query execution 
// Parameters :
//   - $deal[]   : deal search criteria
//   - $all : if it is set to 1 then return also the private deals ...
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_search($deal, $all="") {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $deal["sql_order_dir"];
  $sql_order_field = $deal["sql_order_field"];

  $parent = $deal["parentdeal_id"];
  $label = sql_search_text_parse($deal["label"]);
  $company_id = $deal["company_id"];
  $name = sql_search_text_parse($deal["company_name"]);
  $zip = sql_search_text_parse($deal["zip"]);
  $archive = $deal["archive"];
  $kind = $deal["kind_id"];
  $region = $deal["region"];
  $tt = $deal["tasktype"];
  $role = $deal["role"];
  $status = $deal["status_id"];
  $manager = $deal["manager"];
  $date_field = ( $deal["date_field"] ? $deal["date_field"] : "datealarm");
  $date_after = $deal["date_after"];
  $date_before = $deal["date_before"];
  $role_company = sql_search_text_parse($deal["role_company"]);
  $contact_id = $deal["contact_id"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "deal_datebegin", "deal_datebegin");
  $datealarm = sql_date_format($db_type, "deal_datealarm", "deal_datealarm");
  $dateexp = sql_date_format($db_type, "deal_dateexpected", "deal_dateexpected");
  $dateend = sql_date_format($db_type, "deal_dateend", "deal_dateend");
  $multidomain = sql_multidomain("deal");
	
  $where .= sql_obm_entity_privacy("deal");

  if ($label != "") {
    $where .= " AND deal_label $like '%$label%'";
  }
  if (($kind != $c_all) && ($kind != "")) {
    $where .= " AND deal_type_id='$kind'";
  }
  if (($region != $c_all) && ($region != "")) {
    $where .= " AND deal_region_id='$region'";
  }
  if (($tt != $c_all) && ($tt != "")) {
    $where .= " AND deal_tasktype_id='$tt'";
  }
  if (($role != $c_all) && ($role != "")) {
    $join_role = "LEFT JOIN DealCompany ON deal_id=dealcompany_deal_id";
    $where .= " AND dealcompany_role_id='$role'";
  }
  if ($role_company != "") {
    $join_role = "LEFT JOIN DealCompany ON deal_id=dealcompany_deal_id";
    $join_role .= " LEFT JOIN Company as dc ON dealcompany_company_id=dc.company_id";
    $where .= sql_global_company_name_advanced_search($role_company, $like, "dc");
  }
  if ($company_id != "") {
    $join_comp = "LEFT JOIN Company ON deal_company_id=company_id";
    $where .= " AND deal_company_id='$company_id'";
  }
  if ($name != "") {
    $join_comp = "LEFT JOIN Company ON deal_company_id=company_id";
    $where .= sql_global_company_name_advanced_search($name, $like);
  }
  if ($contact_id > 0) {
    $where .= " AND (deal_contact1_id='$contact_id'
                    OR deal_contact2_id='$contact_id')";
  }
  if ($zip != "") {
    $join_comp = "LEFT JOIN Company ON deal_company_id=company_id";
    $where .= " AND company_zipcode $like '$zip%'";
  }
  if (($status != $c_all) && ($status != "")) {
    $where .= " AND deal_status_id='$status'";
  }
  if ($date_after != "") {
    $where .= " AND deal_$date_field > '$date_after'";
  }
  if ($date_before != "") {
    $where .= " AND deal_$date_field < '$date_before'";
  }
  if ($archive != "1") {
    $where .= " AND deal_archive=0";
  }
  if (($manager != $c_all) && ($manager != "")) {
    $where .= " AND (deal_technicalmanager_id='$manager'
                    OR deal_marketingmanager_id='$manager') ";
  } 
  if ($parent != "") {
    $where .= " AND deal_parentdeal_id='$parent'";
  }

  // User defined data
  $userdata = of_userdata_query_search("deal", $deal);
  if ($userdata["where"] != "") {
    $where .= " AND ". $userdata["where"];
    $join_userdata = $userdata["join"];
  }

  $whereq = "WHERE $where $multidomain";


  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "deal_$date_field";
  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field, "expected_quarter")==0) {
    $order = "deal_dateexpected";
  } else if (strcmp($sql_order_field, "end_quarter")==0) {
    $order = "deal_dateend";
  } else if (strcmp($sql_order_field, "sale_delay")==0) {
    $order = "deal_dateexpected-deal_datebegin";
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";


  $select = "SELECT DISTINCT
      deal_id as id,
      deal_id,
      deal_label,
      deal_parentdeal_id,
      deal_type_id,
      deal_tasktype_id,
      deal_company_id,
      deal_marketingmanager_id,
      deal_technicalmanager_id,
      deal_source_id,
      deal_source,
      deal_commission,
      deal_amount,
      deal_hitrate,
      deal_status_id,
      $datebegin,
      $datealarm,
      $dateexp,
      $dateend,
      deal_archive,
      deal_todo,
      deal_privacy,
      dealtype_label,
      region_label,
      leadsource_label,
      Company.company_name as deal_company_name,
      Company.company_zipcode as deal_company_zipcode,
      tasktype_label,
      dealstatus_label,
      userobm_lastname as deal_marketingmanager
    FROM Deal";

  $left_join = "
        LEFT JOIN Company as Company ON deal_company_id=Company.company_id
        LEFT JOIN DealType ON deal_type_id=dealtype_id
        LEFT JOIN Region ON deal_region_id=region_id
        LEFT JOIN TaskType ON deal_tasktype_id=tasktype_id
        LEFT JOIN DealStatus ON deal_status_id=dealstatus_id
        LEFT JOIN LeadSource ON deal_source_id=leadsource_id
        LEFT JOIN UserObm ON deal_marketingmanager_id=userobm_id
        $join_role
        $join_userdata";

  $query = "$select $left_join $whereq $orderq $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(DISTINCT deal_id) FROM Deal $left_join $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "deal_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deal detail query execution
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_detail($id) {
  global $cdg_sql, $db_type_mysql, $db_type_pgsql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $datebegin = sql_date_format($db_type, "deal_datebegin", "datebegin");
  $dateprop = sql_date_format($db_type, "deal_dateproposal", "dateproposal");
  $datealarm = sql_date_format($db_type, "deal_datealarm", "datealarm");
  $dateexp = sql_date_format($db_type, "deal_dateexpected", "dateexpected");
  $dateend = sql_date_format($db_type, "deal_dateend", "dateend");
  $timeupdate = sql_date_format($db_type, "deal_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "deal_timecreate", "timecreate");
  $multidomain = sql_multidomain("deal");
	
  $query = "SELECT Deal.*,
      deal_privacy as privacy,
      leadsource_label,
      dealtype_label,
      region_label,
      tasktype_label,
      dealstatus_label,
      company_name, company_address1, company_zipcode, company_town,
      company_phone,
      u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
      u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
      c1.contact_lastname as lnamec1, c1.contact_firstname as fnamec1,
      c1.contact_phone as phonec1, c2.contact_phone as phonec2,
      c2.contact_lastname as lnamec2, c2.contact_firstname as fnamec2,
      $datebegin,
      $dateprop,
      $dateexp,
      $datealarm,
      $dateend,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM
      Deal
        LEFT JOIN UserObm u1 ON deal_marketingmanager_id=u1.userobm_id
        LEFT JOIN UserObm u2 ON deal_technicalmanager_id=u2.userobm_id
        LEFT JOIN Company ON deal_company_id=company_id
        LEFT JOIN Contact c1 ON deal_contact1_id=c1.contact_id
        LEFT JOIN Contact c2 ON deal_contact2_id=c2.contact_id
        LEFT JOIN UserObm c ON deal_usercreate=c.userobm_id
        LEFT JOIN UserObm u ON deal_userupdate=u.userobm_id
        LEFT JOIN DealType ON deal_type_id=dealtype_id
        LEFT JOIN DealStatus ON deal_status_id=dealstatus_id
        LEFT JOIN LeadSource ON deal_source_id=leadsource_id
        LEFT JOIN Region ON deal_region_id=region_id,
      TaskType
    WHERE deal_id='$id'
      AND tasktype_id=deal_tasktype_id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : deal insertion
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_insert($deal) {
  global $auth, $cdg_sql, $l_no, $obm;

  $now = date("Y-m-d H:i:s");
  $pid = ($deal["parentdeal_id"] ? "'".$deal["parentdeal_id"]."'" : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["privacy"];
  $datebegin = ($deal["datebegin"] ? "'".$deal["datebegin"]."'" : "null");
  $dateprop = ($deal["dateproposal"] ? "'".$deal["dateproposal"]."'" : "null");
  $dateend = ($deal["dateend"] ? "'".$deal["dateend"]."'" : "null");
  $dateexp = ($deal["dateexpected"] ? "'".$deal["dateexpected"]."'" : "null");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $kind = $deal["kind_id"]; 
  $region = $deal["region"]; 
  $tt = $deal["tasktype"];
  $comp = $deal["company_id"];
  $market = ($deal["market"] ? "'".$deal["market"]."'" : "null");
  $tech = ($deal["tech"] ? "'".$deal["tech"]."'" : "null");
  $source_id = ($deal["source_id"] ? "'".$deal["source_id"]."'" : "0");
  $source = $deal["source"];
  $amount = ($deal["amount"] ? "'".$deal["amount"]."'" : "null");
  $commission = ($deal["commission"] ? "'".$deal["commission"]."'" : "0");
  if (($deal["hitrate"] != "")
      && ($deal["hitrate"] >= 0) && ($deal["hitrate"] <= 100)) {
    $hitrate = "'".$deal["hitrate"]."'";
  } else {
    $hitrate = "null";
  }
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "0");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "0");
  $status = $deal["status_id"];
  $archive = ($deal["archive"] == "1" ? "1" : "0");
  $todo = $deal["todo"];
  $comment = $deal["comment"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $today = date("Y-m-d");
    if ($today == $datecomment) {
      $datecomment = $now;
    }
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $mail_comment = $deal["mail_comment"];
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO Deal (deal_timeupdate,
    deal_timecreate,
    deal_userupdate,
    deal_usercreate,
    deal_domain_id,
    deal_number,
    deal_label,
    deal_datebegin,
    deal_type_id,
    deal_region_id,
    deal_tasktype_id,
    deal_company_id,
    deal_contact1_id,
    deal_contact2_id,
    deal_marketingmanager_id,
    deal_technicalmanager_id,
    deal_source_id,
    deal_source,
    deal_dateproposal,
    deal_dateexpected,
    deal_dateend,
    deal_amount,
    deal_commission,
    deal_hitrate,
    deal_status_id,
    deal_datealarm,
    deal_comment,
    deal_archive,
    deal_privacy,
    deal_parentdeal_id,
    deal_todo)
  VALUES (null,
    '$now',
    null,
    '" . $auth->auth["uid"] . "',
    '$domain_id',
    '$num',
    '$label',
    $datebegin,
    '$kind',
    '$region',
    '$tt',
    '$comp',
    '$con1',
    '$con2',
    $market,
    $tech,
    $source_id,
    '$source',
    $dateprop,
    $dateexp,
    $dateend,
    $amount,
    $commission,
    $hitrate,
    '$status',
    $datealarm,
    '$comment',
    '$archive',
    '$priv',
    $pid,
    '$todo')";

  display_debug_msg($query, $cdg_sql, "run_query_deal_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);  

  $d_id = run_query_deal_id($deal);
  $deal["deal_id"] = $d_id;

  // if the deal inserted is not archived, we increment the company deal number
  if (($d_id) && ($archive == '0')) {
    run_query_deal_company_active_deal_inc($comp);
  }

  // we increment the company total deal number
  if ($d_id) {
    run_query_deal_company_total_deal_inc($comp);
  }

  // DealCompany relations handling
  if ( ($retour) && (is_array($deal["dc"]) )) {
    $ret = run_query_deal_dealcompany_update($d_id, $deal["dc"]);
  }

  if ($d_id > 0) {
    $ret = of_userdata_query_update("deal", $d_id, $deal);
  }

  // If mail comment activated, send the updates by mail
  if (($mail_comment != "") && ($mail_comment != "$l_no")) {
    deal_send_mail($deal);
  }

  return $d_id;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Update query execution
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_update($deal) {
  global $auth, $cdg_sql, $l_no;

  $now = date("Y-m-d H:i:s");
  $id = $deal["deal_id"];
  $pid = ($deal["parentdeal_id"] ? "'".$deal["parentdeal_id"]."'" : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["privacy"];
  $datebegin = ($deal["datebegin"] ? "'".$deal["datebegin"]."'" : "null");
  $dateend = ($deal["dateend"] ? "'".$deal["dateend"]."'" : "null");
  $dateprop = ($deal["dateproposal"] ? "'".$deal["dateproposal"]."'" : "null");
  $dateexp = ($deal["dateexpected"] ? "'".$deal["dateexpected"]."'" : "null");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $kind = $deal["kind_id"]; 
  $region = $deal["region"]; 
  $tt = $deal["tasktype"];
  $comp = $deal["company_id"];
  $market = ($deal["market"] ? $deal["market"] : "null");
  $tech = ($deal["tech"] ? $deal["tech"] : "null");
  $source_id = ($deal["source_id"] ? $deal["source_id"] : "0");
  $source = $deal["source"];
  $amount = ($deal["amount"] ? "'".$deal["amount"]."'" : "null");
  $commission = ($deal["commission"] ? "'".$deal["commission"]."'" : "0");
  if (($deal["hitrate"] != "")
      && ($deal["hitrate"] >= 0) && ($deal["hitrate"] <= 100)) {
    $hitrate = "'".$deal["hitrate"]."'";
  } else {
    $hitrate = "null";
  }
  $con1 = ($deal["contact1"] ? "'".$deal["contact1"]."'" : "null");
  $con2 = ($deal["contact2"] ? "'".$deal["contact2"]."'" : "null");
  $status = $deal["status_id"];
  $archive = ($deal["archive"] == "1" ? "1" : "0");
  $todo = $deal["todo"];
  $comment = $deal["comment"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $today = date("Y-m-d");
    if ($today == $datecomment) {
      $datecomment = $now;
    }
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $mail_comment = $deal["mail_comment"];
  $multidomain = sql_multidomain("deal");
  
  $query = "UPDATE Deal SET
    deal_timeupdate='$now',
    deal_userupdate='".$auth->auth["uid"]."',
    deal_number='$num',
    deal_label='$label',
    deal_datebegin=$datebegin,
    deal_type_id='$kind',
    deal_region_id='$region',
    deal_tasktype_id='$tt',
    deal_company_id='$comp',
    deal_contact1_id=$con1,
    deal_contact2_id=$con2,
    deal_marketingmanager_id='$market',
    deal_technicalmanager_id='$tech',
    deal_source_id='$source_id',
    deal_source='$source',
    deal_dateproposal=$dateprop,
    deal_dateexpected=$dateexp,
    deal_dateend=$dateend,
    deal_amount=$amount,
    deal_commission=$commission,
    deal_hitrate=$hitrate,
    deal_status_id='$status',
    deal_datealarm=$datealarm,
    deal_comment='$comment',
    deal_archive='$archive',
    deal_privacy='$priv',
    deal_todo='$todo'
  WHERE deal_id='$id'
    $multidomain";

  $archive_old = get_deal_archive($id);

  display_debug_msg($query, $cdg_sql, "run_query_deal_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $ret = of_userdata_query_update("deal", $id, $deal);
  }

  // If deal archive status has changed, update the company deal number
  if (($retour) && ($archive != $archive_old)) {
    if ($archive == '1') {
      run_query_deal_company_active_deal_dec($comp);
    } else {
      run_query_deal_company_active_deal_inc($comp);
    }
  }

  // DealCompany relations handling
  if ( ($retour) && (is_array($deal["dc"]) )) {
    $ret = run_query_deal_dealcompany_update($id, $deal["dc"]);
  }

  // If mail comment activated, send the updates by mail
  if (($mail_comment != "") && ($mail_comment != "$l_no")) {
    deal_send_mail($deal);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update the Deal dealcompany relations
// Parameters:
//   - $deal_id : deal id
//   - $dc      : array with dealcompany infos
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_dealcompany_update($deal_id, $dc) {
  global $cdg_sql;


  if ( (is_array($dc)) && (count($dc) > 0) ) {
    $query = "DELETE FROM DealCompany WHERE dealcompany_deal_id='$deal_id'";
    display_debug_msg($query, $cdg_sql, "run_query_deal_dealcompany_update(1)");
    $obm_q = new DB_OBM;
    $obm_q->query($query);

    foreach ($dc as $one_dc) {
      $c_id = $one_dc["company_id"];
      $role_id = $one_dc["role_id"];
      
      if ($c_id > 0) {
  	$query = "INSERT INTO DealCompany (
          dealcompany_deal_id,
          dealcompany_company_id,
          dealcompany_role_id
        ) VALUES (
          '$deal_id',
          '$c_id',
          '$role_id'
        )";

	display_debug_msg($query, $cdg_sql, "run_query_deal_dealcompany_update(2)");
	$obm_q->query($query);
      }
    }
  }

  return $res;
}



///////////////////////////////////////////////////////////////////////////////
// Deal Send Mail
// Parameters:
//   - $deal[] : deal hash info
///////////////////////////////////////////////////////////////////////////////
function deal_send_mail($deal) {
  global $auth, $uid, $cdg_sql, $cg_com;
  global $l_no, $l_members, $l_group, $l_obm_title, $l_deal, $l_company;
  global $l_amount, $l_hitrate, $l_todo, $l_status, $l_alarm, $l_expected, $l_end;

  $id = $deal["deal_id"];
  $d = get_deal_info($id);
  $mail_comment = $deal["mail_comment"];
  $market = $d["market"];
  $tech = $d["tech"];
  $company = $d["company"];
  $label = $d["label"];
  $status = $d["status"];
  $amount = $deal["amount"];
  $hitrate = $deal["hitrate"];
  $dateexp = $deal["dateexpected"];
  $dateend = $deal["dateend"];
  $datealarm = $deal["datealarm"];
  $todo = $deal["todo"];
  $add_comment = $deal["add_comment"];
  $datecomment = $deal["datecomment"];
  $usercomment = $deal["usercomment"];

  $subject = "$l_obm_title : $l_deal : $company - $label";
  $message = "$l_company: $company
$l_deal: $label
$l_amount: $amount
$l_hitrate: $hitrate
$l_status: $status
$l_expected: $dateexp
$l_end : $dateend
$l_alarm: $datealarm
$l_todo: $todo
$datecomment:$usercomment:$add_comment";
  if ($mail_comment == "$l_members") {
    $users_array = array($market, $tech);
  } else if ($mail_comment == "$l_group") {
    $users_array = get_all_users_id_from_group($cg_com);
  }
  if (! is_array($users_array)) {
    $users_array = array();
  }
  $recipient_array = $users_array;
  send_mail($subject, $message, $recipient_array,"");

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Quick Update query execution
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_quick_update($deal) {
  global $auth, $cdg_sql, $l_no;

  $id = $deal["deal_id"];
  $hitrate = ($deal["hitrate"] ? "'".$deal["hitrate"]."'" : "null");
  $dateexp = ($deal["dateexpected"] ? "'".$deal["dateexpected"]."'" : "null");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $status = $deal["status_id"];
  $todo = $deal["todo"];
  $comment = $deal["comment"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $mail_comment = $deal["mail_comment"];
  $multidomain = sql_multidomain("deal");
  
  $query = "UPDATE Deal SET
    deal_timeupdate='". date("Y-m-d H:i:s")."',
    deal_userupdate='".$auth->auth["uid"]."',
    deal_hitrate=$hitrate,
    deal_status_id='$status',
    deal_dateexpected=$dateexp,
    deal_datealarm=$datealarm,
    deal_comment='$comment',
    deal_todo='$todo'
  WHERE deal_id='$id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_quick_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If mail comment activated, send the updates by mail
  if (($mail_comment != "") && ($mail_comment != "$l_no")) {
    deal_send_mail($deal);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deal ID : Get the id of the deal inserted.
// Parameters:
//   - $deal[] : deal hash info : keys used : all
// Returns:
//   deal Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_id($deal) {
  global $cdg_sql, $obm;

  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["privacy"];
  $datebegin = ($deal["datebegin"] ? "='".$deal["datebegin"]."'" : "is null");
  $kind = $deal["kind_id"];
  $region = $deal["region"];
  $tt = $deal["tasktype"];
  $comp = $deal["company_id"];
  $market = ($deal["market"] ? "='".$deal["market"]."'" : "is null");
  $tech = ($deal["tech"] ? "='".$deal["tech"]."'" : "is null");
  $dateprop = ($deal["dateproposal"] ? "='".$deal["dateproposal"]."'" : "is null");
  $amount = ($deal["amount"] ? "='".$deal["amount"]."'" : "is null");
  $hitrate = ($deal["hitrate"] ? "='".$deal["hitrate"]."'" : "is null");
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "0");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "0");
  $datealarm = ($deal["datealarm"] ? "='".$deal["datealarm"]."'" : "is null");
  $status = $deal["status_id"];
  $todo = $deal["todo"];
  $archive = ($deal["archive"] == "1" ? "1" : "0");
  $domain_id = $obm["domain_id"];
	
  // comment not searched because processed before stored in db

  $query = "SELECT deal_id
  FROM Deal
  WHERE deal_number='$num'
    AND deal_label='$label'
    AND deal_datebegin $datebegin
    AND deal_type_id='$kind'
    AND deal_region_id='$region'
    AND deal_tasktype_id='$tt'
    AND deal_company_id='$comp'
    AND deal_contact1_id='$con1'
    AND deal_contact2_id='$con2'
    AND deal_marketingmanager_id $market
    AND deal_technicalmanager_id $tech
    AND deal_dateproposal $dateprop
    AND deal_amount $amount
    AND deal_status_id='$status'
    AND deal_datealarm $datealarm
    AND deal_archive='$archive'
    AND deal_privacy='$priv'
    AND deal_todo='$todo'
    AND deal_domain_id='$domain_id'";

  display_debug_msg($query, $cdg_sql, "run_query_deal_id()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f("deal_id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction
// Parameters:
//   - $id : deal id 
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_delete($id) {
  global $cdg_sql;

  $archive = get_deal_archive($id);
  $comp_id = get_deal_company_id($id);
  $multidomain = sql_multidomain("deal");
 	
  // If deal archive status is off (0), update the company deal number
  if ($archive == '0') {
    run_query_deal_company_active_deal_dec($comp_id);
  }

  // update the company total deal number
  run_query_deal_company_total_deal_dec($comp_id);
  run_query_global_delete_document_links($id, "deal");

  $ret = of_userdata_query_delete("deal", $id);

  $query = "DELETE FROM Deal WHERE deal_id='$id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_deal_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Company info
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_company_info($id) {
  global $cdg_sql;

  $query = "SELECT company_name,
      company_marketingmanager_id,
      company_address1,
      company_zipcode,
      company_town
    FROM Company
    WHERE company_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_deal_company_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
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
	
	$multidomain = sql_multidomain("deal");
	
  $query = "SELECT
      deal_marketingmanager_id AS uid,
      count(*) as nb,
      sum(deal_amount) as amount,
      sum(deal_amount * deal_hitrate / 100) as amount_balanced,
      sum(deal_amount * deal_commission / 100) as commission,
      sum(deal_amount * deal_commission * deal_hitrate / 10000) as commission_balanced,
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
			$multidomain
    GROUP BY deal_marketingmanager_id,
      userobm_lastname,
      userobm_firstname
";
  
  display_debug_msg($query, $cdg_sql, "run_query_deal_potential(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $am = 0;
  $ab = 0;
  $cm = 0;
  $cb = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    $res["market"]["$u_id"]["amount"] = $obm_q->f("amount");
    $res["market"]["$u_id"]["amount_balanced"] = $obm_q->f("amount_balanced");
    $res["market"]["$u_id"]["commission"] = $obm_q->f("commission");
    $res["market"]["$u_id"]["commission_balanced"] = $obm_q->f("commission_balanced");
    $res["market"]["$u_id"]["number"] = $obm_q->f("nb");
    $res["market"]["$u_id"]["name"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
    $am += $res["market"]["$u_id"]["amount"];
    $ab += $res["market"]["$u_id"]["amount_balanced"];
    $cm += $res["market"]["$u_id"]["commission"];
    $cb += $res["market"]["$u_id"]["commission_balanced"];
    $nb += $res["market"]["$u_id"]["number"];
  }

  $res["market"]["0"]["amount"] = $am;
  $res["market"]["0"]["amount_balanced"] = $ab;
  $res["market"]["0"]["commission"] = $cm;
  $res["market"]["0"]["commission_balanced"] = $cb;
  $res["market"]["0"]["number"] = $nb;

  // Tech manager board
  $query = "SELECT
      deal_technicalmanager_id AS uid,
      count(*) as nb,
      sum(deal_amount) as amount,
      sum(deal_amount * deal_hitrate / 100) as amount_balanced,
      sum(deal_amount * deal_commission / 100) as commission,
      sum(deal_amount * deal_commission * deal_hitrate / 10000) as commission_balanced,
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
			$multidomain
    GROUP BY deal_technicalmanager_id,
      userobm_lastname,
      userobm_firstname
";

  display_debug_msg($query, $cdg_sql, "run_query_deal_potential(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $am = 0;
  $ab = 0;
  $cm = 0;
  $cb = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    $res["tech"]["$u_id"]["amount"] = $obm_q->f("amount");
    $res["tech"]["$u_id"]["amount_balanced"] = $obm_q->f("amount_balanced");
    $res["tech"]["$u_id"]["commission"] = $obm_q->f("commission");
    $res["tech"]["$u_id"]["commission_balanced"] = $obm_q->f("commission_balanced");
    $res["tech"]["$u_id"]["number"] = $obm_q->f("nb");
    $res["tech"]["$u_id"]["name"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
    $am += $res["tech"]["$u_id"]["amount"];
    $ab += $res["tech"]["$u_id"]["amount_balanced"];
    $cm += $res["tech"]["$u_id"]["commission"];
    $cb += $res["tech"]["$u_id"]["commission_balanced"];
    $nb += $res["tech"]["$u_id"]["number"];
  }

  $res["tech"]["0"]["amount"] = $am;
  $res["tech"]["0"]["amount_balanced"] = $ab;
  $res["tech"]["0"]["commission"] = $cm;
  $res["tech"]["0"]["commission_balanced"] = $cb;
  $res["tech"]["0"]["number"] = $nb;

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get global Deal infos
// Parameters:
//   - $date_range  : array [date_start, date_end] for classification only
//   - $target_date : deal_dateexpected, deal_dateend : date to sort
//   - $users_id    : array of marketing User id (empty for all deals)
//   - $sel_tt      : [optionnal] tasktype array filter
// Returns:
//     $res :array[date][u_id]([amount]|[balanced]|[commission][commission_ba])
//            total in u_id=0
//                 [users_id] = array(u_id)
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_dashboard_infos($date_range, $target_date="deal_dateexpected", $users_id="", $tts="") {
  global $cdg_sql, $cdg_param;
  global $l_total;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $dexp = sql_date_format($db_type, "deal_dateexpected", "dateexpected");
  $dend = sql_date_format($db_type, "deal_dateend", "dateend");

  if (is_array($date_range)) {
    $past = $date_range[0];
    $future = $date_range[1];
  }

  // Tasktype filter
  if (is_array($tts)) {
    $where_tt .= " AND (";
    $or = "";
    foreach ($tts as $tt_id) {
      $where_tt .= "$or deal_tasktype_id='$tt_id'";
      $or = " OR";
    }
    $where_tt .= ")";
  }

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $where_market_uid = "AND (";
    $or = "";
    foreach ($users_id as $u_id) {
      $where_market_uid .= " $or deal_marketingmanager_id='$u_id'";
      $or = "OR";
    }
    $where_market_uid .= ")";
  }
	$multidomain = sql_multidomain("deal");
  $query = "SELECT
      $dexp,
      $dend,
      deal_marketingmanager_id AS uid,
      deal_amount,
      (deal_amount * deal_hitrate / 100) as amount_balanced,
      (deal_amount * deal_commission / 100) as commission,
      (deal_amount * deal_commission * deal_hitrate / 10000) as commission_balanced,
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
      $where_tt
			$multidomain
    ORDER BY
      $target_date
";
  
  display_debug_msg($query, $cdg_sql, "run_query_deal_dashboard_infos()");
  $obm_q->query($query);

  $res_users = array();
  $am = 0;
  $ab = 0;
  $cm = 0;
  $cb = 0;
  $nb = 0;

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("uid");
    if (! in_array($u_id, $res_users)) {
      $res_users[] = $u_id;
    }
    $am = $obm_q->f("deal_amount");
    $ab = $obm_q->f("amount_balanced");
    $ca = $obm_q->f("commission");
    $cb = $obm_q->f("commission_balanced");
    $end_date = of_isodate_format($obm_q->f("dateend"), 1);
    $exp_date = of_isodate_format($obm_q->f("dateexpected"), 1);
    if ($target_date == "deal_dateexpected") {
      $date_used = $exp_date;
    } else {
      $date_used = $end_date;
    }
    if ($date_used == "") {
      $month = "no_date";
    } else if ($date_used < $past) {
      $month = "past";
    } else if ($date_used > $future) {
      $month = "future";
    } else {
      $month = substr($date_used, 0, 7);
    }

    $res[$month]["$u_id"]["amount"] += $am;
    $res[$month]["$u_id"]["amount_balanced"] += $ab;
    $res[$month]["$u_id"]["commission"] += $ca;
    $res[$month]["$u_id"]["commission_balanced"] += $cb;
    $res[$month]["$u_id"]["number"] ++;
    $res["name"]["$u_id"] = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");

    $res[$month][0]["amount"] += $am;
    $res[$month][0]["amount_balanced"] += $ab;
    $res[$month][0]["commission"] += $ca;
    $res[$month][0]["commission_balanced"] += $cb;
    $res[$month][0]["number"] ++;
  }

  $res["users_id"] = $res_users;

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the infos of potential deals for a list of users
// Parameters:
//   - $deal[]   : deal hash info
//   - $users_id : array of User id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_dashboard_list($deal, $users_id="") {
  global $cdg_sql, $cdg_param, $ctu_sql_limit;

  $date_after = $deal["date_after"];
  $date_before = $deal["date_before"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "deal_datebegin", "deal_datebegin");
  $datealarm = sql_date_format($db_type, "deal_datealarm", "deal_datealarm");
  $dateexp = sql_date_format($db_type, "deal_dateexpected", "deal_dateexpected");
  $dateend = sql_date_format($db_type, "deal_dateend", "deal_dateend");

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $where_uid = "AND (";
    $or = "";
    foreach ($users_id as $u_id) {
      $where_uid .= " $or deal_marketingmanager_id='$u_id'";
      $or = "OR";
    }
    $where_uid .= ")";
  }
 
  if ($date_before == "0000-00-00") {
    $where_date .= " AND deal_dateexpected IS NULL";
  } else {
    if ($date_after != "") {
      $where_date .= " AND deal_dateexpected >= '$date_after'";
    }
    if ($date_before != "") {
      $where_date .= " AND deal_dateexpected <= '$date_before'";
    }
  }
	$multidomain = sql_multidomain("deal");

  // Tasktype filter
  if (is_array($deal["tasktype"])) {
    $where_tt .= " AND (";
    $or = "";
    foreach ($deal["tasktype"] as $tt_id) {
      $where_tt .= "$or deal_tasktype_id='$tt_id'";
      $or = " OR";
    }
    $where_tt .= ")";
  }

  // Order by clause
  $sql_order_field = $deal["sql_order_field"];
  $sql_order_dir = $deal["sql_order_dir"];
  
  $field = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "deal_marketingmanager";
  $orderq = " ORDER BY $field $sql_order_dir";

  $query = "SELECT
      deal_id,
      deal_id as id,
      deal_marketingmanager_id AS uid,
      deal_company_id,
      deal_label,
      deal_source_id,
      deal_source,
      deal_commission,
      deal_amount,
      deal_hitrate,
      $datebegin,
      $datealarm,
      $dateexp,
      $dateend,
      deal_archive,
      deal_todo,
      deal_privacy,
      dealtype_label,
      region_label,
      leadsource_label,
      company_name as deal_company_name,
      company_zipcode as deal_company_zipcode,
      tasktype_label,
      dealstatus_label,
      userobm_lastname as deal_marketingmanager
    FROM Deal
      LEFT JOIN Company ON deal_company_id = company_id
      LEFT JOIN DealType ON deal_type_id = dealtype_id
      LEFT JOIN Region ON deal_region_id = region_id
      LEFT JOIN TaskType ON deal_tasktype_id=tasktype_id 
      LEFT JOIN DealStatus ON deal_status_id = dealstatus_id
      LEFT JOIN LeadSource ON deal_source_id=leadsource_id
      LEFT JOIN UserObm ON deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_uid
      $where_date
      $multidomain
      $where_tt
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $query_count = "SELECT count(deal_id)
    FROM Deal
      LEFT JOIN DealType ON deal_type_id = dealtype_id
      LEFT JOIN DealStatus ON deal_status_id = dealstatus_id
      LEFT JOIN UserObm ON deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_uid
      $where_date
			$multidomain
";
    $count = get_query_count($query_count);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_deal_dashboard_list()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get Deal manager list (from deal database content)
// Parameters:
//   - $archive : if true, get managers of archive deals too
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_manager($archive = false) {
  global $cdg_sql;

	$multidomain = sql_multidomain("userobm");

  if (! $archive) {
    $where_arch .= " deal_archive=0";
  }

  if ($where_arch != "") {
    $where = "WHERE $where_arch $multidomain";
  } else {
  	 $where = "WHERE 1=1 $multidomain";
  }

  $query = "SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Deal
      LEFT JOIN UserObm ON deal_marketingmanager_id=userobm_id
    $where
  UNION DISTINCT
    SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Deal
      LEFT JOIN UserObm ON deal_technicalmanager_id=userobm_id
    $where
  ORDER BY userobm_lastname";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_deal_manager()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact list from the company :  Query execution
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_contact($p_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("contact");

  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_phone
    FROM Contact
    WHERE contact_company_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_contact()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company active deal number increment
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_company_active_deal_inc($comp_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("company");

  $query = "UPDATE Company SET
      company_deal_number=company_deal_number+1
    WHERE company_id='$comp_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_company_active_deal_inc()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company active deal number decrement
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_company_active_deal_dec($comp_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("company");

  $query = "UPDATE Company SET
      company_deal_number=company_deal_number-1
    WHERE company_id='$comp_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_company_active_deal_dec()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company total deal number increment
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_company_total_deal_inc($comp_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("company");

  $query = "UPDATE Company SET
      company_deal_total=company_deal_total+1
    WHERE company_id='$comp_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_company_total_deal_inc()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company total deal number decrement
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_company_total_deal_dec($comp_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("company");

  $query = "UPDATE Company SET
      company_deal_total=company_deal_total-1
    WHERE company_id='$comp_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_company_total_deal_dec()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal archive status
// Parameters:
//   - $deal_id : deal id
// Returns: archived status : 1 = archived, 0 = not archived
///////////////////////////////////////////////////////////////////////////////
function get_deal_archive($deal_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT deal_archive 
		FROM Deal
		WHERE deal_id='$deal_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_archive()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $retour = $obm_q->f("deal_archive");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal infos
// Parameters:
//   - $deal_id : deal id
// Returns: array of deal infos
///////////////////////////////////////////////////////////////////////////////
function get_deal_info($deal_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT *,
      company_name,
      dealstatus_label,
      dealtype_label
    FROM Deal
      LEFT JOIN Company on deal_company_id = company_id
      LEFT JOIN DealStatus on deal_status_id = dealstatus_id
      LEFT JOIN DealType on deal_type_id = dealtype_id
    WHERE deal_id='$deal_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $res["label"] = $obm_q->f("deal_label");
  $res["company"] = $obm_q->f("company_name");
  $res["status"] = $obm_q->f("dealstatus_label");
  $res["type"] = $obm_q->f("dealtype_label");
  $res["market"] = $obm_q->f("deal_marketingmanager_id");
  $res["tech"] = $obm_q->f("deal_technicalmanager_id");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the dealcompany links infos (other companies)
// Parameters:
//   - $deal_id : deal id
// Returns: array of deal infos
///////////////////////////////////////////////////////////////////////////////
function get_deal_dealcompany_info($deal_id) {
  global $cdg_sql;

  $query = "SELECT
      dealcompany_id,
      dealcompany_company_id,
      dealcompany_role_id,
      company_name,
      dealcompanyrole_code,
      dealcompanyrole_label
    FROM DealCompany
      LEFT JOIN Company on dealcompany_company_id = company_id
      LEFT JOIN DealCompanyRole on dealcompany_role_id = dealcompanyrole_id
    WHERE dealcompany_deal_id='$deal_id'
    ORDER BY dealcompany_role_id";

  display_debug_msg($query, $cdg_sql, "get_deal_dealcompany_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $cpt = 0;
  while ($obm_q->next_record()) {
    $company_id = $obm_q->f("dealcompany_company_id");
    $company_name = $obm_q->f("company_name");
    $role_id = $obm_q->f("dealcompany_role_id");
    $role_code = $obm_q->f("dealcompanyrole_code");
    $role_label = $obm_q->f("dealcompanyrole_label");
    $res[$cpt]["company_id"] = $company_id;
    $res[$cpt]["company_name"] = $company_name;
    $res[$cpt]["role_id"] = $role_id;
    $res[$cpt]["role_code"] = $role_code;
    $res[$cpt]["role_label"] = $role_label;
    $cpt++;
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the lead infos
// Parameters:
//   - $lead_id : lead id
// Returns: array of lead infos
///////////////////////////////////////////////////////////////////////////////
function get_lead_info($lead_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("lead");

  $query = "SELECT *
    FROM Lead
    WHERE lead_id='$lead_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_lead_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $res["name"] = $obm_q->f("lead_name");
  $res["company_id"] = $obm_q->f("lead_company_id");
  $res["source_id"] = $obm_q->f("lead_source_id");
  $res["manager"] = $obm_q->f("lead_manager_id");
  $res["date"] = $obm_q->f("lead_date");
  $res["datealarm"] = $obm_q->f("lead_datealarm");
  $res["todo"] = $obm_q->f("lead_todo");
  $res["comment"] = $obm_q->f("lead_comment");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal company id
// Parameters:
//   - $deal_id : deal id
// Returns: company id
///////////////////////////////////////////////////////////////////////////////
function get_deal_company_id($deal_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT deal_company_id FROM Deal WHERE deal_id='$deal_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_company_id()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $retour = $obm_q->f("deal_company_id");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal Kind list
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_type() {
  global $cdg_sql;

  $multidomain = sql_multidomain("dealtype");

  $query = "SELECT * FROM DealType WHERE 1=1 $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_type()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Kind insert
// Parameters:
//   - $deal[] : deal hash info : keys used : kind_label, kind_inout
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_kind_insert($deal) {
  global $auth, $cdg_sql, $obm;
	
  $label = $deal["kind_label"];
  $inout = $deal["kind_inout"];
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO DealType (dealtype_timeupdate,
    dealtype_timecreate,
    dealtype_usercreate,
		dealtype_domain_id,
    dealtype_label,
    dealtype_inout)
  VALUES (null,
    '".date("Y-m-d H:i:s")."',
    '".$auth->auth["uid"]."',
    '$domain_id',
    '$label',
    '$inout')";

  display_debug_msg($query, $cdg_sql, "run_query_deal_kind_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Kind update
// Parameters:
//   - $deal[] : deal hash info : keys used : kind, kind_label, kind_inout
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_kind_update($deal) {
  global $auth, $cdg_sql;  

  $label = $deal["kind_label"];
  $kind = $deal["kind"];
  $inout = $deal["kind_inout"];
  //$inout = (($deal["kind_inout"]) == "+" ? $deal["kind_inout"] : '-');
  $multidomain = sql_multidomain("deal");
	
  $query = "UPDATE DealType SET
      dealtype_label='$label',
      dealtype_timeupdate='" .date("Y-m-d H:i:s") . "',
      dealtype_userupdate='" .$auth->auth["uid"] ."',
      dealtype_inout='$inout'
    WHERE dealtype_id='$kind'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_kind_update()");
  $kind_q = new DB_OBM;
  $retour = $kind_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : kind deletion
// Parameters:
//   - $id : kind id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_kind_delete($id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "DELETE FROM DealType WHERE dealtype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_kind_delete()");
  $kind_q = new DB_OBM;
  $retour = $kind_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Kind given
// Parameters:
//   - $k_id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_kind_links($k_id) {
  global $cdg_sql;
	
	$multidomain = sql_multidomain("deal");

  $query = "SELECT deal_id, deal_label FROM Deal 
		WHERE deal_type_id='$k_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_kind_links()");
  $kind_q = new DB_OBM;
  $kind_q->query($query);

  return $kind_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal Status list
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status_list() {
  global $cdg_sql;

	$multidomain = sql_multidomain("dealstatus");

  $query = "SELECT * FROM DealStatus WHERE 1=1 $multidomain 
			ORDER BY dealstatus_order ASC";

  display_debug_msg($query, $cdg_sql, "run_query_deal_status_list()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status insert
// Parameters:
//   - $deal[] : deal hash info, keys used : status_label, status_order hitrate
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status_insert($deal) {
  global $auth, $cdg_sql, $obm;
	
  $label = $deal["status_label"];
  $order = $deal["status_order"];
  $hitrate = $deal["status_hitrate"];
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO DealStatus (dealstatus_timeupdate,
    dealstatus_timecreate,
    dealstatus_usercreate,
    dealstatus_domain_id,
    dealstatus_label,
    dealstatus_order,
    dealstatus_hitrate)
  VALUES (null,
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$domain_id',
    '$label',
    '$order',
    '$hitrate')";

  display_debug_msg($query, $cdg_sql, "run_query_deal_status_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status update
// Parameters:
//   - $deal[] : deal hash info, keys used : status_label, status_order hitrate
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status_update($deal) {
  global $auth, $cdg_sql;

  $status = $deal["status"];
  $label = $deal["status_label"];
  $order = $deal["status_order"];
  $hitrate = $deal["status_hitrate"];
  $multidomain = sql_multidomain("dealstatus");
	
  $query = "UPDATE DealStatus SET
    dealstatus_label='$label',
    dealstatus_timeupdate='".date("Y-m-d H:i:s")."',
    dealstatus_userupdate='".$auth->auth["uid"]."',
    dealstatus_order='$order',
    dealstatus_hitrate='$hitrate'
  WHERE dealstatus_id='$status'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_status_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status deletion
// Parameters:
//   - $id : Status id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("dealstatus");

  $query = "DELETE FROM DealStatus WHERE dealstatus_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_status_delete()");
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Status given
// Parameters:
//   - $s_id : status id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_status_links($s_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("deal");
	
  $query = "SELECT deal_id, deal_label FROM Deal WHERE deal_status_id='$s_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_status_links()");
  $sta_q = new DB_OBM;
  $sta_q->query($query);

  return $sta_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Form Data checking and formatting
// Parameters:
//   - $did    : deal id : (empty on insertion)
//   - $deal[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_deal_form($did, $deal) {
  global $php_regexp_isodate, $l_invalid_date, $l_alarm;
  global $err_msg, $l_err_label_empty, $l_err_hitrate, $l_date_comment;
  global $l_begin, $l_expected, $l_proposal, $l_end, $l_date_inconsistency;
  global $l_err_kind_empty, $l_err_task_empty, $l_err_market_empty, $l_err_tech_empty;

  // Check if the label is filled
  if (trim($deal["label"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  // Check if the kind is filled
  if ($deal["kind_id"] < 1) {
    $err_msg = $l_err_kind_empty;
    return false;
  }

  // Check if the tasktype is filled
  if ($deal["tasktype"] < 1) {
    $err_msg = $l_err_task_empty;
    return false;
  }

  // Check if the marketing resp. is filled
  if ($deal["market"] < 1) {
    $err_msg = $l_err_market_empty;
    return false;
  }

  // Check if the tech resp. is filled
  if ($deal["tech"] < 1) {
    $err_msg = $l_err_tech_empty;
    return false;
  }

  // If the amount is filled, Check if the hitrate is between 0 and 100
  if ( ( ($deal["amount"]) && !($deal["amount"] == 0) )
       || (trim($deal["hitrate"]) != "") ) {
    if ( ($deal["hitrate"] == "") || (! is_numeric($deal["hitrate"]))
	 || !( ($deal["hitrate"] >= 0) && ($deal["hitrate"] <= 100)) ) {
      $err_msg = $l_err_hitrate;
      return false;
    }
  }

  $datebegin = $deal["datebegin"];
  // Begin Date check
  if (($datebegin != "") && (preg_match($php_regexp_isodate, $datebegin) == 0)) {
    $err_msg = "$l_begin : $l_invalid_date"; 
    return false;
  }

  $dateprop = $deal["dateproposal"];
  // Expected Date check
  if (($dateprop != "") && (preg_match($php_regexp_isodate, $dateprop) == 0)) {
    $err_msg = "$l_proposal : $l_invalid_date";
    return false;
  }

  $dateexp = $deal["dateexpected"];
  // Expected Date check
  if (($dateexp != "") && (preg_match($php_regexp_isodate, $dateexp) == 0)) {
    $err_msg = "$l_expected : $l_invalid_date";
    return false;
  }

  $dateend = $deal["dateend"];
  // End Date check
  if (($datend != "") && (preg_match($php_regexp_isodate, $dateend) == 0)) {
    $err_msg = "$l_end : $l_invalid_date";
    return false;
  }

  $datealarm = $deal["datealarm"];
  // Alarm Date check
  if (($datealarm != "") && (preg_match($php_regexp_isodate, $datealarm) == 0)) {
    $err_msg = "$l_alarm : $l_invalid_date";
    return false;
  }

  // Check dates inconsistencies
  if ($datebegin != "") { $ts_begin = strtotime($datebegin); }
  if ($dateend != "") { $ts_end = strtotime($dateend); }
  if ($dateprop != "") { $ts_prop = strtotime($dateprop); }
  if ($dateexp != "") { $ts_exp = strtotime($dateexp); }
  if ($datealarm != "") { $ts_alarm = strtotime($datealarm); }

  // Begin Date after proposal date
  if (($datebegin != "") && ($dateprop != "") && ($ts_prop < $ts_begin)) {
    $err_msg = "$l_date_inconsistency : $l_begin > $l_proposal !";
    return false;
  }

  // Begin Date after expected date
  if (($datebegin != "") && ($dateexp != "") && ($ts_exp < $ts_begin)) {
    $err_msg = "$l_date_inconsistency : $l_begin > $l_expected !";
    return false;
  }

  // Begin Date after alarm date
  if (($datebegin != "") && ($datealarm != "") && ($ts_alarm < $ts_begin)) {
    $err_msg = "$l_date_inconsistency : $l_begin > $l_alarm !";
    return false;
  }

  // Begin Date after end date
  if (($datebegin != "") && ($dateend != "") && ($ts_end < $ts_begin)) {
    $err_msg = "$l_date_inconsistency : $l_begin > $l_end !";
    return false;
  }

  // Proposal Date after expected date
  if (($dateprop != "") && ($dateexp != "") && ($ts_exp < $ts_prop)) {
    $err_msg = "$l_date_inconsistency : $l_proposal > $l_expected !";
    return false;
  }

  // Proposal Date after alarm date
  if (($dateprop != "") && ($datealarm != "") && ($ts_alarm < $ts_prop)) {
    $err_msg = "$l_date_inconsistency : $l_proposal > $l_alarm !";
    return false;
  }

  // Proposal Date after end date
  if (($dateprop != "") && ($dateend != "") && ($ts_end < $ts_prop)) {
    $err_msg = "$l_date_inconsistency : $l_proposal > $l_end !";
    return false;
  }


  $datecomment = $deal["datecomment"];
  // Comment Date check
  if (($datecomment != "") && (preg_match($php_regexp_isodate, $datecomment) == 0)) {
    $err_msg = "$l_date_comment : $l_invalid_date";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Form Data checking and formatting
// Parameters:
//   - $deal[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_deal_quick_form($deal) {
  global $php_regexp_isodate, $l_invalid_date, $l_alarm, $l_date_comment;
  global $err_msg, $l_expected;

  $dateexp = $deal["dateexpected"];
  // Expected Date check
  if (($dateexp != "") && (preg_match($php_regexp_isodate, $dateexp) == 0)) {
    $err_msg = "$l_expected : $l_invalid_date";
    return false;
  }

  $datealarm = $deal["datealarm"];
  // Alarm Date check
  if (($datealarm != "") && (preg_match($php_regexp_isodate, $datealarm) == 0)) {
    $err_msg = "$l_alarm : $l_invalid_date";
    return false;
  }

  $datecomment = $deal["datecomment"];
  // Comment Date check
  if (($datecomment != "") && (preg_match($php_regexp_isodate, $datecomment) == 0)) {
    $err_msg = "$l_date_comment : $l_invalid_date";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the deal can be deleted
// Parameters:
//   - $p_id : deal id
// Returns:
//   true if the deal can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_deal($p_id) {
  global $err_msg, $ok_msg;
  global $l_link_contract, $l_link_contract_no, $l_link_project;
  global $l_link_invoice, $l_link_invoice_no, $l_link_project_no;

  $delete_ok = true;

  // Links from Contract
  $nb = get_global_linked_contract_nb($p_id, "deal", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_contract";
  } else {
    $ok_msg .= "$l_link_contract_no";
  }

  // Links from Project
  $nb = get_global_linked_project_nb($p_id, "deal", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_project";
  } else {
    $ok_msg .= "<br />$l_link_project_no";
  }

  // Links from Invoice
  $nb = get_global_linked_invoice_nb($p_id, "deal", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_invoice";
  } else {
    $ok_msg .= "<br />$l_link_invoice_no";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// ParentDeal Form Data checking and formatting
// Parameters:
//   - $pid    : parentdeal id : (empty on insertion)
//   - $deal[] : values to check : plabel
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_deal_parent_form($pid, $deal) {
  global $php_regexp_isodate, $l_invalid_date;
  global $err_msg, $l_err_label_empty;

  // Check the label is filled
  if (trim($deal["plabel"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the deal can be deleted
// Parameters:
//   - $p_id : deal id
// Returns:
//   true if the deal can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_deal_can_delete_parentdeal($p_id) {
  global $cdg_sql, $err_msg, $ok_msg;
  global $l_link_deal;

  $delete_ok = true;
	$multidomain = sql_multidomain("deal");
  // Check if the parentdeal has any deals

  $query = "SELECT count(*) FROM Deal 
		WHERE deal_parentdeal_id='$pid' $multidomain";

  display_debug_msg($query, $cdg_sql, "check_deal_can_delete_parentdeal()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_deal";
  }

  return $delete_ok;
}


//
//
//
///////////////////////////////////////////////////////////////////////////////
// -- ParentDeal Requests  ---->
///////////////////////////////////////////////////////////////////////////////
//
//
//


///////////////////////////////////////////////////////////////////////////////
// Return the result of the parentdeal's search
// Parameters :
//   - $deal[]       : parentdeal search criteria
//     keys used     : plabel, pmanager, parchive
//   - $sql_order_field  : if it's set, it replaces the default "order by" value
//   - $sql_order_dir  : direction for order clause (asc, desc)
//////////////////////////////////////////////////////////////////////////////
function run_query_deal_search_parentdeal($deal) {
  global $auth, $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $deal["sql_order_dir"];
  $sql_order_field = $deal["sql_order_field"];

  $plabel = sql_search_text_parse($deal["plabel"]);
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
	$multidomain = sql_multidomain("parentdeal");
	
  if ($plabel != "") {
    $where .= "parentdeal_label $like '$plabel%'";
    $and = "AND";
  }
  if ($parchive != 1) {
    $where .= " $and parentdeal_archive=0";
    $and = "AND";
  }
  if (($pmanager != $c_all) && ($pmanager != "")) { 
     $where .= " $and (parentdeal_technicalmanager_id='$pmanager'
		    OR parentdeal_marketingmanager_id='$pmanager') ";
    $and = "AND";
  }

  $whereq = "WHERE 1=1 $where $multidomain";

  // Order by clause
  $order=(strcmp($sql_order_field,"") != 0) ? $sql_order_field : "parentdeal_timecreate"; 
  $orderq = " ORDER BY $order $sql_order_dir";

  $query = "SELECT
      parentdeal_id as id,
      parentdeal_id,
      parentdeal_label,
      parentdeal_marketingmanager_id,
      parentdeal_technicalmanager_id,
      parentdeal_archive,
      um.userobm_lastname as parentdeal_marketing_lastname,
      ut.userobm_lastname as parentdeal_technical_lastname
    FROM ParentDeal
      LEFT JOIN UserObm as um ON parentdeal_marketingmanager_id=um.userobm_id
      LEFT JOIN UserObm as ut ON parentdeal_technicalmanager_id=ut.userobm_id
    $whereq
    $orderq
    $limit
";
   
  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM ParentDeal $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "parentdeal_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ParentDeal insertion
// Parameters:
//   - $deal[] : deal hash info : keys used : plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_insert_parentdeal($deal) {
  global $auth, $obm, $cdg_sql;

  $plabel = $deal["plabel"];
  $pmarket = ($deal["pmarket"] ? $deal["pmarket"] : "null");
  $ptech = ($deal["ptech"] ? $deal["ptech"] : "null");
  $pcomment = $deal["pcom"];
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO ParentDeal (parentdeal_timeupdate,
    parentdeal_timecreate,
    parentdeal_userupdate,
    parentdeal_usercreate,
    parentdeal_label,
		parentdeal_domain_id,
    parentdeal_marketingmanager_id,
    parentdeal_technicalmanager_id,
    parentdeal_archive,
    parentdeal_comment)
  VALUES (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$plabel',
    '$domain_id',
    '$pmarket',
    '$ptech',
    '0',
    '$pcomment')";

  display_debug_msg($query, $cdg_sql, "run_query_deal_insert_parentdeal()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Parent Deal detail
// Parameters:
//   - $pid : parentdeal id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_parentdeal_detail($pid) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "parentdeal_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "parentdeal_timecreate", "timecreate");
  $multidomain = sql_multidomain("parentdeal");
	
  $query = "SELECT ParentDeal.*,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        $timeupdate,
        $timecreate,
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      FROM ParentDeal
        LEFT JOIN UserObm u1 ON parentdeal_marketingmanager_id=u1.userobm_id
        LEFT JOIN UserObm u2 ON parentdeal_technicalmanager_id=u2.userobm_id
        LEFT JOIN UserObm c ON parentdeal_usercreate=c.userobm_id
        LEFT JOIN UserObm u ON parentdeal_userupdate=u.userobm_id
      WHERE parentdeal_id='$pid' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_parentdeal_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return($obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Parent Deal Update query execution
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_parentdeal_update($deal) {
  global $auth, $cdg_sql;

  $pid = ($deal["parentdeal_id"] ? $deal["parentdeal_id"] : "null");
  $plabel = $deal["plabel"];
  $pmarket = ($deal["pmarket"] ? $deal["pmarket"] : "null");
  $ptech = ($deal["ptech"] ? $deal["ptech"] : "null");
  $pcomment = $deal["pcom"];
  $multidomain = sql_multidomain("parentdeal");
	
  $query = "UPDATE ParentDeal SET
    parentdeal_timeupdate='". date("Y-m-d H:i:s")."',
    parentdeal_userupdate='".$auth->auth["uid"]."',
    parentdeal_label='$plabel',
    parentdeal_marketingmanager_id='$pmarket',
    parentdeal_technicalmanager_id='$ptech',
    parentdeal_comment='$pcomment'
  WHERE parentdeal_id='$pid' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_parentdeal_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Affect a deal to a parentdeal 
// Parameters:
//   - $id     : deal id
//   - $deal[] : deal hash info : keys used : sel_parent
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_affect_deal_parentdeal($id, $deal) {
  global $auth, $cdg_sql;
    
  $pid = $deal["parent"];
  $mutidomain = sql_multidomain("deal");
	
  $query = "UPDATE Deal SET
    deal_parentdeal_id='$pid',
    deal_timeupdate='". date("Y-m-d H:i:s")."',
    deal_userupdate='".$auth->auth["uid"]."'
  WHERE deal_id='$id' $mutidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_affect_deal_parentdeal()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - ParentDeal deletion query
// Parameters:
//   - $p_id : parent_deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_parentdeal_delete($p_id) {
  global $cdg_sql;
	
  $multidomain = sql_multidomain("parentdeal");
	
  $query = "DELETE FROM ParentDeal WHERE parentdeal_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_deal_parentdeal_delete()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Update archive parentdeal query
// Parameters:
//   - $p_id : parent_deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_updatearchive_parentdeal($p_id) {
  global $cdg_sql;
  
  $multidomain = sql_multidomain("parentdeal");
  
  $query = "UPDATE ParentDeal SET
    parentdeal_archive=1
  WHERE parentdeal_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_deal_updatearchive_parentdeal()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Kind
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function get_deal_kind_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("dealtype");

  $query = "SELECT dealtype_label 
		FROM DealType WHERE dealtype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_kind_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("dealtype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Status
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function get_deal_status_label($id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("dealstatus");

  $query = "SELECT dealstatus_label FROM DealStatus 
		WHERE dealstatus_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_status_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("dealstatus_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the label of the specified ParentDeal
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_label($p_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("parentdeal");

  // We group by, to only have one row result
  $query = "SELECT parentdeal_label
    FROM ParentDeal
    WHERE parentdeal_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_deal_parent_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("parentdeal_label");
}


///////////////////////////////////////////////////////////////////////////////
// Return the status of the specified ParentDeal - Calculated
// = min Status (order) among all deals contained in the ParentDeal)
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_status($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("dealstatus");

  // We order by dealstatus_id to get the min
  $query = "SELECT dealstatus_order, dealstatus_label
    FROM DealStatus, Deal
    WHERE deal_parentdeal_id='$p_id'
      AND deal_status_id=dealstatus_id $multidomain
    ORDER BY dealstatus_order";

  display_debug_msg($query, $cdg_sql, "get_deal_parent_status()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("dealstatus_label");
}


///////////////////////////////////////////////////////////////////////////////
// Return the proposal date of a parentdeal - Calculated 
// it's the nearest proposal date among all the deals of the parentdeal
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_dateproposal($p_id)  {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT deal_dateproposal
    FROM Deal
    WHERE deal_parentdeal_id='$p_id'
      AND deal_archive != '1'
      AND deal_dateproposal is not null
			$multidomain
    ORDER BY deal_dateproposal";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  return $obm_q->f("deal_dateproposal");
}


///////////////////////////////////////////////////////////////////////////////
// Return the alarm date of a parentdeal 
// it's the nearest alarm date among all the deals of the parentdeal
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_datealarm($p_id)  {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT deal_datealarm
    FROM Deal
    WHERE deal_parentdeal_id='$p_id' AND deal_archive != '1' $multidomain
    ORDER BY deal_datealarm";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  return $obm_q->f("deal_datealarm");
}


///////////////////////////////////////////////////////////////////////////////
// Return the total expense of the deals of the parentdeal
// Calculated : sum of amount fields noted "-"
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_expense($p_id)  {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT sum(deal_amount) as res
    FROM Deal, DealType
    WHERE deal_parentdeal_id='$p_id'
      AND deal_hitrate = '100'
      AND deal_type_id=dealtype_id
      AND dealtype_inout='-'
			$multidomain
    GROUP BY deal_parentdeal_id"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("sum(deal_amount)");
}


///////////////////////////////////////////////////////////////////////////////
// Return the total income done of the deals of the parentdeal
// Calculated : sum of amount fields noted "+" where hitrate is 100 %
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_income($p_id)  {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT sum(deal_amount) as res
    FROM Deal, DealType
    WHERE deal_parentdeal_id='$p_id'
      AND deal_hitrate = '100'
      AND deal_type_id=dealtype_id
      AND dealtype_inout='+'
			$multidomain
    GROUP BY deal_parentdeal_id"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("res");
}


///////////////////////////////////////////////////////////////////////////////
// Return the potential income of the deals of the parentdeal
// Calculated : sum of amount fields noted "+" where hitrate < 100 %
// Parameters:
//   - $p_id : parent deal id
///////////////////////////////////////////////////////////////////////////////
function get_deal_parent_potential($p_id)  {
  global $cdg_sql;

	$multidomain = sql_multidomain("deal");

  $query = "SELECT sum(deal_amount * deal_hitrate / 100) as res_balanced,
      sum(deal_amount) as res
    FROM Deal, DealType
    WHERE deal_parentdeal_id='$p_id'
      AND deal_hitrate < 100
      AND deal_type_id=dealtype_id
      AND dealtype_inout='+'
			$multidomain
    GROUP BY deal_parentdeal_id"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return number_format($obm_q->f("res_balanced")) . " / " .$obm_q->f("res");
}

?>
