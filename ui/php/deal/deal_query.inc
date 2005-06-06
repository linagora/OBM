<script language="php">
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
//     keys used      : parent, label, company_name, archive, kind, cat, state,
//                      manager, dateafter, datebefore
//   - $all : if it is set to 1 then return also the private deals ...
//   - $p_new_order  : if it's set, it replaces the default "order by" value
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($deal, $all="", $p_new_order="", $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $parent = $deal["parent"];
  $label = sql_search_text_parse($deal["label"]);
  $company_id = $deal["company"];
  $name = sql_search_text_parse($deal["company_name"]);
  $zip = sql_search_text_parse($deal["company_zip"]);
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $tt = $deal["tasktype"];
  $cat = $deal["category"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = $deal["dateafter"];
  $datebefore = $deal["datebefore"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datealarm = sql_date_format($db_type, "deal_datealarm", "deal_datealarm");

  $where .= sql_obm_entity_privacy("deal");

  if ($label != "") {
    $where .= " and deal_label $like '%$label%'";
  }
  if (($kind != $c_all) && ($kind != "")) {
    $where .= " and dealtype_id='$kind'";
  }
  if (($tt != $c_all) && ($tt != "")) {
    $where .= " and deal_tasktype_id='$tt'";
  }
  if (($cat != $c_all) && ($cat != "")) {
    $join_cat = "LEFT JOIN DealCategoryLink on deal_id=dealcategorylink_deal_id";
    $where .= " and dealcategorylink_category_id='$cat'";
  }
  if ($company_id != "") {
    $where .= " and deal_company_id='$company_id'";
  }
  if ($name != "") {
    $where .= " and company_name $like '$name%'";
    $join_comp = "left join Company on deal_company_id=company_id";
  }
  if ($zip != "") {
    $where .= " and company_zipcode $like '$zip%'";
    $join_comp = "left join Company on deal_company_id=company_id";
  }
  if (($state != $c_all) && ($state != "")) {
    $where .= " and deal_status_id='$state'";
  }
  if ($dateafter != "") {
    $where .= " and deal_datealarm > '$dateafter'";
  }
  if ($datebefore != "") {
    $where .= " and deal_datealarm < '$datebefore'";
  }
  if ($archive != "1") {
    $where .= " and deal_archive=0";
  }
  if (($manager != $c_all) && ($manager != "")) {
    $where .= " and (deal_technicalmanager_id='$manager'
                    or deal_marketingmanager_id='$manager') ";
  } 
  if ($parent != "") {
    $where .= " and deal_parentdeal_id='$parent'";
  }

  $whereq = "where $where";

  // Order by clause
  $field = (strcmp($p_new_order,"") != 0) ? $p_new_order : "Deal.deal_datealarm";
  $orderq = " order by $field $p_order_dir";

  $query = "SELECT 
      deal_id as id,
      deal_id,
      deal_label,
      deal_datebegin,
      deal_parentdeal_id,
      deal_type_id,
      deal_tasktype_id,
      deal_company_id,
      deal_marketingmanager_id,
      deal_technicalmanager_id,
      deal_amount,
      deal_hitrate,
      deal_status_id,
      deal_datealarm,
      deal_archive,
      deal_todo,
      deal_privacy,
      dealtype_label,
      company_name as deal_company_name,
      company_zipcode as deal_company_zipcode,
      tasktype_label,
      dealstatus_label,
      userobm_lastname as deal_marketingmanager,
      $datealarm
    FROM Deal
        left join Company on deal_company_id=company_id 
        left join DealType on deal_type_id=dealtype_id 
        left join TaskType on deal_tasktype_id=tasktype_id 
        left join DealStatus on deal_status_id=dealstatus_id 
        left join UserObm on deal_marketingmanager_id=userobm_id 
        $join_cat
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Deal $join_comp $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
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
function run_query_detail($id) {
  global $cdg_sql, $db_type_mysql, $db_type_pgsql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $datebegin = sql_date_format($db_type, "deal_datebegin", "datebegin");
  $dateproposal = sql_date_format($db_type, "deal_dateproposal", "dateproposal");
  $datealarm = sql_date_format($db_type, "deal_datealarm", "datealarm");
  $timeupdate = sql_date_format($db_type, "deal_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "deal_timecreate", "timecreate");

  $query = "SELECT Deal.*,
      dealtype_label,
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
      $dateproposal,
      $datealarm,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM
      Deal
      left join UserObm u1 on deal_marketingmanager_id=u1.userobm_id
      left join UserObm u2 on deal_technicalmanager_id=u2.userobm_id
      left join Company on deal_company_id=company_id
      left join Contact c1 on deal_contact1_id=c1.contact_id
      left join Contact c2 on deal_contact2_id=c2.contact_id
      left join UserObm c on deal_usercreate=c.userobm_id
      left join UserObm u on deal_userupdate=u.userobm_id
      left join DealType on deal_type_id=dealtype_id
      left join DealStatus on deal_status_id=dealstatus_id,
      TaskType
    WHERE deal_id='$id'
      and tasktype_id=deal_tasktype_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : deal insertion
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($deal) {
  global $auth, $cdg_sql,$obm_q;

  $id = $deal["id"];
  $pid = ($deal["parent"] ? "'".$deal["parent"]."'" : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["priv"];
  $datebegin = ($deal["datebegin"] ? "'".$deal["datebegin"]."'" : "null");
  $kind = $deal["kind"]; 
  $tt = $deal["tasktype"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? "'".$deal["market"]."'" : "null");
  $tech = ($deal["tech"] ? "'".$deal["tech"]."'" : "null");
  $dateprop = ($deal["dateprop"] ? "'".$deal["dateprop"]."'" : "null");
  $amount = ($deal["amount"] ? "'".$deal["amount"]."'" : "null");
  $hitrate = ($deal["hitrate"] ? "'".$deal["hitrate"]."'" : "null");
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "0");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "0");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $status = $deal["state"];
  $archive = ($deal["archive"] == 1 ? "1" : "0");
  $todo = $deal["todo"];
  $cats = $deal["category"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  $query = "INSERT	into Deal (deal_timeupdate,
    deal_timecreate,
    deal_userupdate,
    deal_usercreate,
    deal_number,
    deal_label,
    deal_datebegin,
    deal_type_id,
    deal_tasktype_id,
    deal_company_id,
    deal_contact1_id,
    deal_contact2_id,
    deal_marketingmanager_id,
    deal_technicalmanager_id,
    deal_dateproposal,
    deal_amount,
    deal_hitrate,
    deal_status_id,
    deal_datealarm,
    deal_comment,
    deal_archive,
    deal_privacy,
    deal_parentdeal_id,
    deal_todo)
  VALUES (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$num',
    '$label',
    $datebegin,
    '$kind',
    '$tt',
    '$comp',
    '$con1',
    '$con2',
    $market,
    $tech,
    $dateprop,
    $amount,
    $hitrate,
    '$status',
    $datealarm,
    '$comment',
    '$archive',
    '$priv',
    $pid,
    '$todo')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);  

  $d_id = run_query_deal_id($deal);

  if ($retour && is_array($cats)) {
    foreach ($cats as $categoryid) {
      $query = "insert into DealCategoryLink (
	  dealcategorylink_deal_id,
	  dealcategorylink_category_id)
	values (
          '$d_id',
          '$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }


  // if the deal inserted is not archived, we increment the company deal number
  if (($d_id) && ($archive == '0')) {
    run_query_company_active_deal_inc($comp);
  }

  // we increment the company total deal number
  if ($d_id) {
    run_query_company_total_deal_inc($comp);
  }

  return $d_id;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Update query execution
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($deal) {
  global $auth, $cdg_sql;

  $id = $deal["id"];
  $pid = ($deal["parent"] ? "'".$deal["parent"]."'" : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["priv"];
  $datebegin = ($deal["datebegin"] ? "'".$deal["datebegin"]."'" : "null");
  $kind = $deal["kind"]; 
  $tt = $deal["tasktype"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? $deal["market"] : "null");
  $tech = ($deal["tech"] ? $deal["tech"] : "null");
  $dateprop = ($deal["dateprop"] ? "'".$deal["dateprop"]."'" : "null");
  $amount = ($deal["amount"] ? "'".$deal["amount"]."'" : "null");
  $hitrate = ($deal["hitrate"] ? "'".$deal["hitrate"]."'" : "null");
  $con1 = ($deal["contact1"] ? "'".$deal["contact1"]."'" : "null");
  $con2 = ($deal["contact2"] ? "'".$deal["contact2"]."'" : "null");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $status = $deal["state"];
  $archive = ($deal["archive"] == 1 ? "1" : "0");
  $todo = $deal["todo"];
  $comment = $deal["com"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $cat_m = $deal["category"];
  
  $query = "UPDATE Deal SET
    deal_timeupdate='". date("Y-m-d H:i:s")."',
    deal_userupdate='".$auth->auth["uid"]."',
    deal_number='$num',
    deal_label='$label',
    deal_datebegin=$datebegin,
    deal_type_id='$kind',
    deal_tasktype_id='$tt',
    deal_company_id='$comp',
    deal_contact1_id=$con1,
    deal_contact2_id=$con2,
    deal_marketingmanager_id='$market',
    deal_technicalmanager_id='$tech',
    deal_dateproposal=$dateprop,
    deal_amount=$amount,
    deal_hitrate=$hitrate,
    deal_status_id='$status',
    deal_datealarm=$datealarm,
    deal_comment='$comment',
    deal_archive='$archive',
    deal_privacy='$priv',
    deal_todo='$todo'
  WHERE deal_id='$id'";

  $archive_old = get_deal_archive($id);

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if (($retour) && (is_array($cat_m))) {
    $query = "DELETE
    FROM DealCategoryLink
    WHERE dealcategorylink_deal_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);

    foreach ($cat_m as $categoryid) {
      $query = "INSERT INTO DealCategoryLink (
	  dealcategorylink_deal_id,
	  dealcategorylink_category_id)
	VALUES (
          '$id',
          '$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }


  // If deal archive state has changed, update the company deal number
  if (($retour) && ($archive != $archive_old)) {
    if ($archive == '1') {
      run_query_company_active_deal_dec($comp);
    } else {
      run_query_company_active_deal_inc($comp);
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Quick Update query execution
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_quick_update($deal) {
  global $auth, $cdg_sql;

  $id = $deal["id"];
  $hitrate = ($deal["hitrate"] ? "'".$deal["hitrate"]."'" : "null");
  $datealarm = ($deal["datealarm"] ? "'".$deal["datealarm"]."'" : "null");
  $status = $deal["state"];
  $todo = $deal["todo"];
  $comment = $deal["com"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $cat_m = $deal["category"];
  
  $query = "UPDATE Deal SET
    deal_timeupdate='". date("Y-m-d H:i:s")."',
    deal_userupdate='".$auth->auth["uid"]."',
    deal_hitrate=$hitrate,
    deal_status_id='$status',
    deal_datealarm=$datealarm,
    deal_comment='$comment',
    deal_todo='$todo'
  WHERE deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

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
  global $cdg_sql,$c_id;

  $num = $deal["num"];
  $label = $deal["label"];
  $priv = $deal["priv"];
  $datebegin = ($deal["datebegin"] ? "='".$deal["datebegin"]."'" : "is null");
  $kind = $deal["kind"]; 
  $tt = $deal["tasktype"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? "='".$deal["market"]."'" : "is null");
  $tech = ($deal["tech"] ? "='".$deal["tech"]."'" : "is null");
  $dateprop = ($deal["dateprop"] ? "='".$deal["dateprop"]."'" : "is null");
  $amount = ($deal["amount"] ? "='".$deal["amount"]."'" : "is null");
  $hitrate = ($deal["hitrate"] ? "='".$deal["hitrate"]."'" : "is null");
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "0");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "0");
  $datealarm = ($deal["datealarm"] ? "='".$deal["datealarm"]."'" : "is null");
  $status = $deal["state"];
  $todo = $deal["todo"];
  $archive = ($deal["archive"] == 1 ? "1" : "0");
  // comment not searched because processed before stored in db

  $query = "SELECT deal_id FROM Deal WHERE 
    deal_number='$num' and
    deal_label='$label' and
    deal_datebegin $datebegin and
    deal_type_id='$kind' and
    deal_tasktype_id='$tt' and
    deal_company_id='$comp' and
    deal_contact1_id='$con1' and
    deal_contact2_id='$con2' and
    deal_marketingmanager_id $market and
    deal_technicalmanager_id $tech and
    deal_dateproposal $dateprop and
    deal_amount $amount and
    deal_hitrate $hitrate and
    deal_status_id='$status' and
    deal_datealarm $datealarm and
    deal_archive='$archive' and
    deal_privacy='$priv' and
    deal_todo='$todo'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_delete($id) {
  global $cdg_sql;

  $archive = get_deal_archive($id);
  $comp_id = get_deal_company_id($id);

  // If deal archive state is off (0), update the company deal number
  if ($archive == '0') {
    run_query_company_active_deal_dec($comp_id);
  }

  // update the company total deal number
  run_query_company_total_deal_dec($comp_id);
  run_query_delete_document_links($id, "deal");
  $query = "delete from Deal where deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contracts attached to the deal
// Parameters:
//   - $p_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_contract_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct contract_id, contract_label 
            from Contract
            where contract_deal_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the projects attached to the deal
// Parameters:
//   - $p_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_project_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct project_id, project_name 
            from Project
            where project_deal_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the invoices attached to the deal
// Parameters:
//   - $p_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_invoice_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "SELECT distinct invoice_id,
      invoice_label 
    FROM Invoice
    WHERE invoice_deal_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Company info                                            //
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_info($id) {
  global $cdg_sql;

  $query = "select company_name, company_marketingmanager_id, company_address1, company_zipcode, company_town from Company where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
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
      sum(deal_amount/deal_hitrate) as amount_balanced,
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
    GROUP BY deal_marketingmanager_id
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
// Get the infos of potential deals for a list of users
// Parameters:
//   - $deal[]   : deal hash info
//   - $users_id : array of User id
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_stats($deal, $users_id="") {
  global $cdg_sql, $cdg_param, $ctu_sql_limit;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $datealarm = sql_date_format($db_type, "deal_datealarm", "deal_datealarm");

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $where_uid = "AND (";
    $or = "";
    foreach ($users_id as $u_id) {
      $where_uid .= " $or deal_marketingmanager_id='$u_id'";
      $or = "OR";
    }
    $where_uid .= ")";
  }

  // Order by clause
  $new_order = $deal["new_order"];
  $order_dir = $deal["order_dir"];
  $field = (strcmp($new_order,"") != 0) ? $new_order : "deal_marketingmanager";
  $orderq = " ORDER BY $field $order_dir";

  $query = "SELECT
      deal_id,
      deal_id as id,
      deal_marketingmanager_id AS uid,
      deal_label,
      deal_amount,
      deal_hitrate,
      $datealarm,
      company_name as deal_company_name,
      company_zipcode as deal_company_zipcode,
      dealtype_label,
      tasktype_label,
      dealstatus_label,
      deal_todo,
      userobm_lastname as deal_marketingmanager
    FROM Deal
      LEFT join Company on deal_company_id = company_id
      LEFT join DealType on deal_type_id = dealtype_id
      LEFT join TaskType on deal_tasktype_id=tasktype_id 
      LEFT join DealStatus on deal_status_id = dealstatus_id
      LEFT join UserObm on deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_uid
    $orderq
    $limit
";
  
  if ($ctu_sql_limit) {
    $query_count = "SELECT count(deal_id)
    FROM Deal
      LEFT join DealType on deal_type_id = dealtype_id
      LEFT join DealStatus on deal_status_id = dealstatus_id
      LEFT join UserObm on deal_marketingmanager_id=userobm_id
    WHERE dealtype_inout = '+'
      AND deal_hitrate > 0
      AND deal_hitrate < 100
      AND deal_archive = '0'
      $where_uid
";
    $count = get_query_count($query_count);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_deal_stats()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact list from the company :  Query execution                          //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_deal($p_id) {
  global $cdg_sql;

  $query = "select contact_id,
      contact_lastname,
      contact_firstname,
      contact_phone
    from Contact
    where contact_company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company active deal number increment                    //
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_active_deal_inc($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_deal_number=company_deal_number+1
              where company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company active deal number decrement                    //
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_active_deal_dec($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_deal_number=company_deal_number-1
              where company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company total deal number increment
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_total_deal_inc($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_deal_total=company_deal_total+1
              where company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company total deal number decrement                     //
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_total_deal_dec($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_deal_total=company_deal_total-1
              where company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal archive state                                                //
// Parameters:
//   - $deal_id : deal id
// Returns: archived state : 1 = archived, 0 = not archived
///////////////////////////////////////////////////////////////////////////////
function get_deal_archive($deal_id) {
  global $cdg_sql;

  $query = "select deal_archive from Deal where deal_id='$deal_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $retour = $obm_q->f("deal_archive");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal company id                                                   //
// Parameters:
//   - $deal_id : deal id
// Returns: company id
///////////////////////////////////////////////////////////////////////////////
function get_deal_company_id($deal_id) {
  global $cdg_sql;

  $query = "select deal_company_id from Deal where deal_id='$deal_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $retour = $obm_q->f("deal_company_id");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat_label($id) {
  global $cdg_sql;

  $query = "select dealcategory_label
    from DealCategory
    where dealcategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("dealcategory_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_dealcategory() {
  global $cdg_sql, $cgp_hide;

  if (! $cgp_hide["deal"]["category1"]) {
    $query = "select * from DealCategory order by dealcategory_code"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the deal
// Parameters:
//   - $d_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_dealcategory($d_id) {
  global $cdg_sql;

  $query = "SELECT dealcategory_id
    FROM DealCategoryLink, DealCategory
    WHERE dealcategorylink_category_id = dealcategory_id
      and dealcategorylink_deal_id = '$d_id'
    ORDER by dealcategory_code";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $categories = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f("dealcategory_id");
    $categories[] = $id;
  }

  return $categories;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the deal
// Parameters:
//   - $d_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_dealcategory_info($d_id) {
    global $cdg_sql;

  $query = "SELECT dealcategory_label, dealcategory_code
    FROM DealCategoryLink, DealCategory
    WHERE dealcategorylink_category_id = dealcategory_id
      and dealcategorylink_deal_id = '$d_id'
    ORDER by dealcategory_code";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// DealCategory - Deal links query execution
// Parameters:
//   - $c_id : dealcategory id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_links($c_id) {
  global $cdg_sql;

  $query = "select deal_label, deal_id
    from Deal, DealCategoryLink
    where deal_id = dealcategorylink_deal_id
      and dealcategorylink_category_id = '$c_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Category1 insertion query construction and execution
// Parameters:
//   - $contact : contact hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_insert($deal) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat_label = $deal["cat_label"];
  $cat_code = $deal["cat_code"];

  $query = "insert into DealCategory (
    dealcategory_timecreate,
    dealcategory_usercreate,
    dealcategory_label,
    dealcategory_code)
  values (
    '$timecreate',
    '$usercreate',
    '$cat_label',
    '$cat_code')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category 1 update query execution
// Parameters:
//   - $contact : contact hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_update($deal) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $deal["category"];
  $cat_label = $deal["cat_label"];
  $cat_code = $deal["cat_code"];
  
  $query = "update DealCategory set
      dealcategory_label='$cat_label',
      dealcategory_code='$cat_code',
      dealcategory_timeupdate='$timeupdate',
      dealcategory_userupdate='$userupdate'
    where
      dealcategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category1 deletion query execution
// Parameters:
//   - $id : Category1 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_delete($id) {
  global $cdg_sql;

  if ($id != '') {
    $query = "delete from DealCategory where dealcategory_id='$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }
  else 
    $retour = 1;
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal Kind list
///////////////////////////////////////////////////////////////////////////////
function run_query_dealtype() {
  global $cdg_sql;

  $query = "SELECT * FROM DealType";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Kind insert                                             //
// Parameters:
//   - $deal[] : deal hash info : keys used : kind_label, kind_inout
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_insert($deal) {
  global $auth, $cdg_sql;
	
  $label = $deal["kind_label"];
  $inout = $deal["kind_inout"];

  $query = "insert into DealType (dealtype_timeupdate,
    dealtype_timecreate,
    dealtype_usercreate,
    dealtype_label,
    dealtype_inout)
  values (null,
    '".date("Y-m-d H:i:s")."',
    '".$auth->auth["uid"]."',
    '$label',
    '$inout')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Kind update                                             //
// Parameters:
//   - $deal[] : deal hash info : keys used : kind, kind_label, kind_inout
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_update($deal) {
  global $auth, $cdg_sql;  

  $label = $deal["kind_label"];
  $kind = $deal["kind"];
  $inout = $deal["kind_inout"];

  $query = "update DealType set dealtype_label='$label',
    dealtype_timeupdate='" .date("Y-m-d H:i:s") . "',
    dealtype_userupdate='" .$auth->auth["uid"] ."',
    dealtype_inout='$inout'
   where dealtype_id='$kind'";

  display_debug_msg($query, $cdg_sql);
  $kind_q = new DB_OBM;
  $retour = $kind_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : kind deletion                                           //
// Parameters:
//   - $id : kind id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_delete($id) {
  global $cdg_sql;

  $query = "delete from DealType where dealtype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $kind_q = new DB_OBM;
  $retour = $kind_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Kind given                                 //
// Parameters:
//   - $k_id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_links($k_id) {
  global $cdg_sql;

  $query = "select deal_id, deal_label from Deal where deal_type_id='$k_id'";

  display_debug_msg($query, $cdg_sql);
  $kind_q = new DB_OBM;
  $kind_q->query($query);

  return $kind_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal Status list                                        //
///////////////////////////////////////////////////////////////////////////////
function run_query_dealstatus() {
  global $cdg_sql;

  $query = "select * from DealStatus order by dealstatus_order asc";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status insert                                           //
// Parameters:
//   - $deal[] : deal hash info, keys used : status_label, status_order hitrate
///////////////////////////////////////////////////////////////////////////////
function run_query_status_insert($deal) {
  global $auth, $cdg_sql;
	
  $label = $deal["status_label"];
  $order = $deal["status_order"];
  $hitrate = $deal["hitrate"];

  $query = "insert into DealStatus (dealstatus_timeupdate,
    dealstatus_timecreate,
    dealstatus_usercreate,
    dealstatus_label,
    dealstatus_order,
    dealstatus_hitrate)
  values (null,
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$label',
    '$order',
    '$hitrate')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status update                                           //
// Parameters:
//   - $deal[] : deal hash info, keys used : status_label, status_order hitrate
///////////////////////////////////////////////////////////////////////////////
function run_query_status_update($deal) {
  global $auth, $cdg_sql;

  $status = $deal["state"];
  $label = $deal["status_label"];
  $order = $deal["status_order"];
  $hitrate = $deal["hitrate"];

  $query = "update DealStatus set dealstatus_label='$label',
    dealstatus_timeupdate='".date("Y-m-d H:i:s")."',
    dealstatus_userupdate='".$auth->auth["uid"]."',
    dealstatus_order='$order',
    dealstatus_hitrate='$hitrate'
  where dealstatus_id='$status'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status deletion                                         //
// Parameters:
//   - $id : Status id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_status_delete($id) {
  global $cdg_sql;

  $query = "delete from DealStatus where dealstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Status given                               //
// Parameters:
//   - $s_id : status id
///////////////////////////////////////////////////////////////////////////////
function run_query_status_links($s_id) {
  global $cdg_sql;

  $query = "select deal_id, deal_label from Deal where deal_status_id='$s_id'";

  display_debug_msg($query, $cdg_sql);
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
  global $php_regexp_isodate, $l_invalid_date, $l_date_init, $l_date_alarm;
  global $err_msg, $l_err_label_empty, $l_err_hitrate, $l_date_comment;
  global $l_err_kind_empty, $l_err_task_empty, $l_err_market_empty, $l_err_tech_empty;

  // Check if the label is filled
  if (trim($deal["label"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  // Check if the kind is filled
  if ($deal["kind"] < 1) {
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
    $err_msg = "$l_date_init : $l_invalid_date"; 
    return false;
  }

  $datealarm = $deal["datealarm"];

  // Alarm Date check
  if (($datealarm != "") && (preg_match($php_regexp_isodate, $datealarm) == 0)) {
    $err_msg = "$l_date_alarm : $l_invalid_date";
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
  global $php_regexp_isodate, $l_invalid_date, $l_date_alarm, $l_date_comment;
  global $err_msg;

  $datealarm = $deal["datealarm"];

  // Alarm Date check
  if (($datealarm != "") && (preg_match($php_regexp_isodate, $datealarm) == 0)) {
    $err_msg = "$l_date_alarm : $l_invalid_date";
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
// ParentDeal Form Data checking and formatting
// Parameters:
//   - $pid    : parentdeal id : (empty on insertion)
//   - $deal[] : values to check : plabel
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_parent_form($pid, $deal) {
  global $php_regexp_isodate, $l_invalid_date, $l_date_init;
  global $err_msg, $l_err_label_empty;

  // Check the label is filled
  if (trim($deal["plabel"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a ParentDeal has any deal associated
// Parameters:
//   - $pid    : parentdeal id
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_parent_has_deal($pid) {
  global $cdg_sql;

  $query = "select count(*) from Deal where deal_parentdeal_id='$pid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  if ($nb > 0) {
    return true;
  } else {
    return false;
  }
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
//   - $p_new_order  : if it's set, it replaces the default "order by" value
//   - $p_order_dir  : direction for order clause (asc, desc)
//////////////////////////////////////////////////////////////////////////////
function run_query_search_parentdeal($deal, $p_new_order="", $p_order_dir="") {
  global $auth, $c_all, $cdg_sql, $ctu_sql_limit;

  $plabel = sql_search_text_parse($deal["plabel"]);
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  if ($plabel != "") {
    $where .= "parentdeal_label $like '$plabel%'";
    $and = "and";
  }
  if ($parchive != 1) {
    $where .= " $and parentdeal_archive=0";
    $and = "and";
  }
  if (($pmanager != $c_all) && ($pmanager != "")) { 
     $where .= " $and (parentdeal_technicalmanager_id='$pmanager'
		    or parentdeal_marketingmanager_id='$pmanager') ";
    $and = "and";
  }

  $whereq = "where $where";

  // Order by clause
  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "parentdeal_timecreate"; 
  $orderq = " order by $order $p_order_dir";

  $query = "select
      parentdeal_id as id,
      parentdeal_id,
      parentdeal_label,
      parentdeal_marketingmanager_id,
      parentdeal_technicalmanager_id,
      parentdeal_archive,
      um.userobm_lastname as parentdeal_marketing_lastname,
      ut.userobm_lastname as parentdeal_technical_lastname
    from ParentDeal
      LEFT JOIN UserObm as um on parentdeal_marketingmanager_id=um.userobm_id
      LEFT JOIN UserObm as ut on parentdeal_technicalmanager_id=ut.userobm_id
    $whereq
    $orderq
    $limit
";
   
  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from ParentDeal $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "parentdeal_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ParentDeal insertion                                    //
// Parameters:
//   - $deal[] : deal hash info : keys used : plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_parentdeal($deal) {
  global $auth, $cdg_sql;

  $plabel = $deal["plabel"];
  $pmarket = ($deal["pmarket"] ? $deal["pmarket"] : "null");
  $ptech = ($deal["ptech"] ? $deal["ptech"] : "null");
  $pcomment = $deal["pcom"];

  $query = "insert into ParentDeal (parentdeal_timeupdate,
    parentdeal_timecreate,
    parentdeal_userupdate,
    parentdeal_usercreate,
    parentdeal_label,
    parentdeal_marketingmanager_id,
    parentdeal_technicalmanager_id,
    parentdeal_archive,
    parentdeal_comment)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$plabel',
    '$pmarket',
    '$ptech',
    '0',
    '$pcomment')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Parent Deal detail                                      //
// Parameters:
//   - $pid : parentdeal id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail_parentdeal($pid) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "parentdeal_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "parentdeal_timecreate", "timecreate");

  $query = "select ParentDeal.*,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        $timeupdate,
        $timecreate,
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      from 
        ParentDeal
        left join UserObm u1 on parentdeal_marketingmanager_id=u1.userobm_id
        left join UserObm u2 on parentdeal_technicalmanager_id=u2.userobm_id
        left join UserObm c on parentdeal_usercreate=c.userobm_id
        left join UserObm u on parentdeal_userupdate=u.userobm_id
      where parentdeal_id='$pid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return($obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Parent Deal Update query execution                                        //
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update_parentdeal($deal) {
  global $auth, $cdg_sql;

  $pid = ($deal["parent"] ? $deal["parent"] : "null");
  $plabel = $deal["plabel"];
  $pmarket = ($deal["pmarket"] ? $deal["pmarket"] : "null");
  $ptech = ($deal["ptech"] ? $deal["ptech"] : "null");
  $pcomment = $deal["pcom"];

  $query = "update ParentDeal set
    parentdeal_timeupdate='". date("Y-m-d H:i:s")."',
    parentdeal_userupdate='".$auth->auth["uid"]."',
    parentdeal_label='$plabel',
    parentdeal_marketingmanager_id='$pmarket',
    parentdeal_technicalmanager_id='$ptech',
    parentdeal_comment='$pcomment'
  where parentdeal_id='$pid'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_affect_deal_parentdeal($id, $deal) {
  global $auth, $cdg_sql;
    
  $pid = $deal["sel_parent"];

  $query="update Deal set deal_parentdeal_id='$pid', deal_timeupdate='". date("Y-m-d H:i:s")."',deal_userupdate='".$auth->auth["uid"]."' where deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - ParentDeal deletion query                               //
// Parameters:
//   - $p_id : parent_deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_parentdeal($p_id) {
  global $cdg_sql;

  $query = "delete from ParentDeal where parentdeal_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Update archive parentdeal query                                           //
// Parameters:
//   - $p_id : parent_deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_updatearchive_parentdeal($p_id) {
  global $cdg_sql;
  
  $query = "update ParentDeal set parentdeal_archive=1 where parentdeal_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Kind                                                   //
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function get_kind_label($id) {
  global $cdg_sql;

  $query = "select dealtype_label from DealType where dealtype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("dealtype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Status                                                 //
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function get_status_label($id) {
  global $cdg_sql;

  $query = "select dealstatus_label from DealStatus where dealstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
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
function get_parent_label($p_id) {
  global $cdg_sql;

  // We group by, to only have one row result
  $query = "select parentdeal_label from ParentDeal where parentdeal_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
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
function get_parent_status($p_id) {
  global $cdg_sql;

  // We order by dealstatus_id to get the min
  $query = "select dealstatus_order, dealstatus_label
    from DealStatus,Deal
    where deal_parentdeal_id='$p_id'
      and deal_status_id=dealstatus_id
    order by dealstatus_order";

  display_debug_msg($query, $cdg_sql);
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
function get_parent_dateproposal($p_id)  {
  global $cdg_sql;

  $query = "select deal_dateproposal from Deal
    where deal_parentdeal_id='$p_id' and deal_archive != '1'
      and deal_dateproposal is not null
    order by deal_dateproposal";

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
function get_parent_datealarm($p_id)  {
  global $cdg_sql;

  $query = "select deal_datealarm from Deal
    where deal_parentdeal_id='$p_id' and deal_archive != '1'
    order by deal_datealarm";

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
function get_parent_expense($p_id)  {
  global $cdg_sql;

  $query = "select sum(deal_amount) as res
    from Deal,DealType
    where deal_parentdeal_id='$p_id'
      and deal_hitrate = '100'
      and deal_type_id=dealtype_id
      and dealtype_inout='-'
    group by deal_parentdeal_id"; 

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
function get_parent_income($p_id)  {
  global $cdg_sql;

  $query = "select sum(deal_amount) as res
    from Deal,DealType
    where deal_parentdeal_id='$p_id'
      and deal_hitrate = '100'
      and deal_type_id=dealtype_id
      and dealtype_inout='+'
    group by deal_parentdeal_id"; 

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
function get_parent_potential($p_id)  {
  global $cdg_sql;

  $query = "select sum(deal_amount * deal_hitrate / 100) as res_balanced,
      sum(deal_amount) as res
    from Deal,DealType
    where deal_parentdeal_id='$p_id'
      and deal_hitrate < 100
      and deal_type_id=dealtype_id
      and dealtype_inout='+'
    group by deal_parentdeal_id"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return number_format($obm_q->f("res_balanced")) . " / " .$obm_q->f("res") ;
}


</script>
