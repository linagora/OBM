<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : deal_query.inc                                               //
//     - Desc : Deal query File                                              //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
//-----------------------------------------------------------------
// 2001-07-19 / Fink / single quotes for SQL
// 2001-07-20 / Fink / handling of NULL values and quotes on ParentDeal
//-----------------------------------------------------------------

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
  global $auth, $c_all, $cdg_sql;

  $parent = $deal["parent"];
  $label = $deal["label"];
  $company_id = $deal["company"];
  $name = $deal["company_name"];
  $zip = $deal["company_zip"];
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = $deal["dateafter"];
  $datebefore = $deal["datebefore"];

  $query = "select 
      deal_id as Id,
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
      deal_visibility,
      dealtype_label,
      company_name as deal_company_name,
      company_zipcode as deal_company_zipcode,
      tasktype_label,
      dealstatus_label,
      userobm_lastname as deal_marketingmanager,
      UNIX_TIMESTAMP(deal_datealarm) as deal_datealarm
    from Deal, DealType, TaskType, Company, DealStatus, UserObm
    where deal_type_id=dealtype_id
     and tasktype_id=deal_tasktype_id
     and deal_company_id=company_id
     and deal_status_id=dealstatus_id
     and deal_marketingmanager_id=UserObm.userobm_id";

  if ($all == 0) {
    $query .= " and (deal_visibility=0
                      or deal_usercreate='" . $auth->auth["uid"] . "')";
  }
  if ($label != "") {
    $query .= " and deal_label like '$label%'";
  }
  if (($kind != $c_all) && ($kind != "")) {
    $query .= " and dealtype_id='$kind'";
  }
  if (($cat != $c_all) && ($cat != "")) {
    $query .= " and deal_tasktype_id='$cat'";
  }
  if ($name != "") {
    $query .= " and company_name like '$name%'";
  }
  if ($zip != "") {
    $query .= " and company_zipcode like '$zip%'";
  }
  if ($company_id != "") {
    $query .= " and company_id='$company_id'";
  }
  if (($state != $c_all) && ($state != "")) {
    $query .= " and dealstatus_id='$state'";
  }
  if ($dateafter != "") {
    $query .= " and deal_datealarm > '$dateafter'";
  }
  if ($datebefore != "") {
    $query .= " and deal_datealarm < '$datebefore'";
  }
  if ($archive != "1") {
    $query .= " and deal_archive=0";
  }
  if ($param_company != "") {
    $query .= " and deal_company_id='$param_company'";
  }
  if (($manager != $c_all) && ($manager != "")) {
    $query .= " and (deal_technicalmanager_id='$manager'
                    or deal_marketingmanager_id='$manager') ";
  } 
  if ($parent != "") {
    $query .= " and deal_parentdeal_id='$parent'";
  }

  // Order by clause
  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "deal_datealarm";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;

}


///////////////////////////////////////////////////////////////////////////////
// Deal detail query execution                                               //
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $obm_q = new DB_OBM;
  if ($obm_q->type == $db_type_mysql) {
    $query = "select Deal.*,
        dealtype_label,
        tasktype_label,
        dealstatus_label,
        company_name, company_address1, company_zipcode, company_town,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        c1.contact_lastname as lnamec1, c1.contact_firstname as fnamec1,
        c1.contact_phone as phonec1, c2.contact_phone as phonec2,
        c2.contact_lastname as lnamec2, c2.contact_firstname as fnamec2,
        UNIX_TIMESTAMP(deal_datebegin) as datebegin,
        UNIX_TIMESTAMP(deal_datealarm) as datealarm,
        UNIX_TIMESTAMP(deal_timeupdate) as datemodif,
        UNIX_TIMESTAMP(deal_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(deal_timecreate) as timecreate
      from
        (((Deal left join UserObm u1 on deal_marketingmanager_id=u1.userobm_id)
           left join UserObm u2 on deal_technicalmanager_id=u2.userobm_id)
           left join Contact c1 on deal_contact1_id=c1.contact_id)
           left join Contact c2 on deal_contact2_id=c2.contact_id,
         DealType, TaskType, DealStatus, Company
      where deal_id='$id'
        and dealtype_id=deal_type_id
        and tasktype_id=deal_tasktype_id
        and dealstatus_id=deal_status_id
        and company_id=deal_company_id";

  } else if ($obm_q->type == $db_type_pgsql) {
    // require at list Postgresql 7.1 (left join)
    $query = "select Deal.*,
        dealtype_label,
        tasktype_label,
        dealstatus_label,
        company_name, company_address1, company_zipcode, company_town,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        deal_timeupdate as datemodif,
        deal_timeupdate as timeupdate,
        deal_timecreate as timecreate
      from
        (Deal left join UserObm u1 on deal_marketingmanager_id=u1.userobm_id)
              left join UserObm u2 on deal_technicalmanager_id=u2.userobm_id,
         DealType TaskType, DealStatus, Company
      where deal_id='$id'
        and dealtype_id=deal_type_id
        and tasktype_id=deal_tasktype_id
        and dealstatus_id=deal_status_id
        and company_id=deal_company_id";
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : deal insertion                                          //
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($deal) {
  global $auth, $cdg_sql;

  $id = $deal["id"];
  $pid = ($deal["parent"] ? $deal["parent"] : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $vis = $deal["vis"];
  $datebegin = ($deal["datebegin"] ? $deal["datebegin"] : "null");
  $kind = $deal["kind"]; 
  $cat = $deal["cat"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? $deal["market"] : "null");
  $tech = ($deal["tech"] ? $deal["tech"] : "null");
  $dateprop = ($deal["dateprop"] ? $deal["dateprop"] : "null");
  $amount = $deal["amount"];
  $hitrate = $deal["hitrate"];
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "null");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "null");
  $datealarm = ($deal["datealarm"] ? $deal["datealarm"] : "null");
  $status = $deal["state"];
  $archive = $deal["archive"];
  $todo = $deal["todo"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  $query = "insert into Deal (deal_timeupdate,
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
    deal_visibility,
    deal_parentdeal_id,
    deal_todo)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$num',
    '$label',
    '$datebegin',
    '$kind',
    '$cat',
    '$comp',
    '$con1',
    '$con2',
    '$market',
    '$tech',
    '$dateprop',
    '$amount',
    '$hitrate',
    '$status',
    '$datealarm',
    '$comment',
    '$archive',
    '$vis',
    '$pid',
    '$todo')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // if the deal inserted is not archived, we increment the company deal number
  if (($retour) && ($archive == '0')) {
    run_query_company_active_deal_inc($comp);
  }

  // we increment the company total deal number
  if ($retour) {
    run_query_company_total_deal_inc($comp);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Update query execution                                               //
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($deal) {
  global $auth, $cdg_sql;

  $id = $deal["id"];
  $pid = ($deal["parent"] ? $deal["parent"] : "null");
  $num = $deal["num"];
  $label = $deal["label"];
  $vis = $deal["vis"];
  $datebegin = ($deal["datebegin"] ? $deal["datebegin"] : "null");
  $kind = $deal["kind"]; 
  $cat = $deal["cat"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? $deal["market"] : "null");
  $tech = ($deal["tech"] ? $deal["tech"] : "null");
  $dateprop = ($deal["dateprop"] ? $deal["dateprop"] : "null");
  $amount = $deal["amount"];
  $hitrate = $deal["hitrate"];
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "null");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "null");
  $datealarm = ($deal["datealarm"] ? $deal["datealarm"] : "null");
  $status = $deal["state"];
  $archive = $deal["archive"];
  $todo = $deal["todo"];
  $comment = $deal["com"];
  $add_comment = $deal["add_comment"];
  if ($add_comment != "") {
    $datecomment = $deal["datecomment"];
    $usercomment = $deal["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  
  $query = "update Deal set deal_timeupdate='". date("Y-m-d H:i:s")."',
    deal_userupdate='".$auth->auth["uid"]."',
    deal_number='$num',
    deal_label='$label',
    deal_datebegin='$datebegin',
    deal_type_id='$kind',
    deal_tasktype_id='$cat',
    deal_company_id='$comp',
    deal_contact1_id='$con1',
    deal_contact2_id='$con2',
    deal_marketingmanager_id='$market',
    deal_technicalmanager_id='$tech',
    deal_dateproposal='$dateprop',
    deal_amount='$amount',
    deal_hitrate='$hitrate',
    deal_status_id='$status',
    deal_datealarm='$datealarm',
    deal_comment='$comment',
    deal_archive='$archive',
    deal_visibility='$vis',
    deal_todo='$todo'
  where deal_id='$id'";

  $archive_old = get_deal_archive($id);

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

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
// Deal ID : Get the id of the deal inserted.                                //
// Parameters:
//   - $deal[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_id($deal) {
  global $cdg_sql;

  $num = $deal["num"];
  $label = $deal["label"];
  $vis = $deal["vis"];
  $datebegin = ($deal["datebegin"] ? $deal["datebegin"] : "null");
  $kind = $deal["kind"]; 
  $cat = $deal["cat"];
  $comp = $deal["company"];
  $market = ($deal["market"] ? $deal["market"] : "null");
  $tech = ($deal["tech"] ? $deal["tech"] : "null");
  $dateprop = ($deal["dateprop"] ? $deal["dateprop"] : "null");
  $amount = $deal["amount"];
  $hitrate = $deal["hitrate"];
  $con1 = ($deal["contact1"] ? $deal["contact1"] : "null");
  $con2 = ($deal["contact2"] ? $deal["contact2"] : "null");
  $datealarm = ($deal["datealarm"] ? $deal["datealarm"] : "null");
  $status = $deal["state"];
  $archive = $deal["archive"];
  $todo = $deal["todo"];
  $comment = $deal["com"];

  $query = "select deal_id from Deal Where 
    deal_number='$num' and
    deal_label='$label' and
    deal_datebegin='$datebegin' and
    deal_type_id='$kind' and
    deal_tasktype_id='$cat' and
    deal_company_id='$comp' and
    deal_contact1_id='$con1' and
    deal_contact2_id='$con2' and
    deal_marketingmanager_id='$market' and
    deal_technicalmanager_id='$tech' and
    deal_dateproposal='$dateprop' and
    deal_amount='$amount' and
    deal_hitrate='$hitrate' and
    deal_status_id='$status' and
    deal_datealarm='$datealarm' and
    deal_comment='$comment' and
    deal_archive='$archive' and
    deal_visibility='$vis' and
    deal_todo='$todo'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return  $obm_q->f("deal_id");
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction                                                 //
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

  $query = "delete from Deal where deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal Kind list                                          //
///////////////////////////////////////////////////////////////////////////////
function run_query_dealtype() {
  global $cdg_sql;

  $query = "select * from DealType";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Deal TaskType list                                      //
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_tasktype() {
  global $cdg_sql;

  $query = "select * from TaskType where tasktype_internal!='1' order by tasktype_internal";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
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
// Contact list from the company :  Query execution                          //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_deal($p_id) {
  global $cdg_sql;

  $query = "select contact_id,contact_lastname,contact_firstname,contact_phone from Contact where contact_company_id='$p_id'";

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
// Query execution : Status insert                                           //
// Parameters:
//   - $deal[] : deal hash info : keys used : status_label, status_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_insert($deal) {
  global $auth, $cdg_sql;
	
  $label = $deal["status_label"];
  $order = $deal["status_order"];

  $query = "insert into DealStatus (dealstatus_timeupdate,
    dealstatus_timecreate,
    dealstatus_usercreate,
    dealstatus_label,dealstatus_order)
  values ('null',
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$label',
    '$order')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status update                                           //
// Parameters:
//   - $deal[] : deal hash info : keys used : state, status_label, status_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_update($deal) {
  global $auth, $cdg_sql;

  $status = $deal["state"];
  $label = $deal["status_label"];
  $order = $deal["status_order"];

  $query = "update DealStatus set dealstatus_label='$label',
    dealstatus_timeupdate='".date("Y-m-d H:i:s")."',
    dealstatus_userupdate='".$auth->auth["uid"]."',
    dealstatus_order='$order'
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
// Query execution : Category insert                                         //
// Parameters:
//   - $deal[] : deal hash info : keys used : cat_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_insert($deal) {
  global $auth, $cdg_sql;
	
  $label = $deal["cat_label"];

  $query = "insert into TaskType (tasktype_timeupdate,
    tasktype_timecreate,
    tasktype_usercreate,
    tasktype_internal,
    tasktype_label)
  values ('null',
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '0',
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Category update                                         //
// Parameters:
//   - $deal[] : deal hash info : keys used : cat, cat_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_update($deal) {
  global $auth, $cdg_sql;

  $cat = $deal["cat"];
  $label = $deal["cat_label"];

  $query = "update TaskType set tasktype_label='$label',
      tasktype_timeupdate='".date("Y-m-d H:i:s")."',
      tasktype_userupdate='".$auth->auth["uid"]."'
    where tasktype_id='$cat'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Category deletion                                       //
// Parameters:
//   - $id : Category id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_delete($id) {
  global $cdg_sql;

  $query = "delete from TaskType where tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $cat_q = new DB_OBM;
  $retour = $cat_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Category given                             //
// Parameters:
//   - $c_id : category id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_links($c_id) {
  global $cdg_sql;

  $query = "select deal_id, deal_label from Deal where deal_tasktype_id='$c_id'";

  display_debug_msg($query, $cdg_sql);
  $cat_q = new DB_OBM;
  $cat_q->query($query);

  return $cat_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Form Data checking and formatting
// Parameters:
//   - $did : deal id : (empty on insertion)
//   - $deal[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_deal_form($did, $deal) {
  global $php_regexp_isodate, $l_invalid_date, $l_date_init;
  global $err_msg, $l_err_label_empty, $l_err_hitrate;
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
  if ($deal["cat"] < 1) {
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
  if ( ($deal["amount"]) && !($deal["amount"] == 0)) {
    if ( ($deal["hitrate"] == "")
	 || !( ($deal["hitrate"] >= 0) && ($deal["hitrate"] <= 100)) ) {
      $err_msg = $l_err_hitrate;
      return false;
    }
  }

  $datebegin = $deal["datebegin"];

  // Begin Date check
  if (($datebegin != "") && (ereg($php_regexp_isodate, $datebegin) == false)) {
    $err_msg = "$l_date_init : $l_invalid_date"; 
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
  global $auth, $c_all, $cdg_sql;

  $plabel = $deal["plabel"];
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];

  $query = "select
      parentdeal_id as Id,
      parentdeal_id,
      parentdeal_label,
      parentdeal_marketingmanager_id,
      parentdeal_technicalmanager_id,
      parentdeal_archive,
      Marketing.userobm_lastname as parentdeal_marketing_lastname,
      Technical.userobm_lastname as parentdeal_technical_lastname
    from ParentDeal,
      UserObm Marketing, 
      UserObm Technical
    where parentdeal_technicalmanager_id=Technical.userobm_id
      and parentdeal_marketingmanager_id=Marketing.userobm_id";
   
  if ($plabel != "") {
    $query .= " and parentdeal_label like '$plabel%'";
  }
  if ($parchive != 1) {
    $query .= " and parentdeal_archive=0";
  }
  if (($pmanager != $c_all) && ($pmanager != "")) { 
     $query .= " and (parentdeal_technicalmanager_id='$pmanager'
		    or parentdeal_marketingmanager_id='$pmanager') ";
  } 

  // Order by clause
  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "parentdeal_timecreate"; 
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);

  $obm_q = new DB_OBM;
  $obm_q->query($query);
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
  
  if ($obm_q->type == $db_type_mysql) {
    $query = "select ParentDeal.*,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        UNIX_TIMESTAMP(parentdeal_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(parentdeal_timecreate) as timecreate
      from (ParentDeal
          left join UserObm u1 on parentdeal_marketingmanager_id=u1.userobm_id)
          left join UserObm u2 on parentdeal_technicalmanager_id=u2.userobm_id
      where parentdeal_id='$pid'";
  } else if ($obm_q->type == $db_type_pgsql) {
    $query = "select ParentDeal.*,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        parentdeal_timeupdate as timeupdate,
        parentdeal_timecreate as timecreate
      from (ParentDeal
          left join UserObm u1 on parentdeal_marketingmanager_id=u1.userobm_id)
          left join UserObm u2 on parentdeal_technicalmanager_id=u2.userobm_id
      where parentdeal_id='$pid'";
  }

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
// Get the label of a Category                                               //
// Parameters:
//   - $id : category id
///////////////////////////////////////////////////////////////////////////////
function get_cat_label($id) {
  global $cdg_sql;

  $query="select tasktype_label from TaskType where tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("tasktype_label");
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
      and deal_dateproposal != '0000-00-00'
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

  return $obm_q->f("res_balanced") . " / " .$obm_q->f("res") ;
}


//
////
//////
//////////////////////////////////////////////////////////////////////////////
// Invoice stuff below :
//////////////////////////////////////////////////////////////////////////////
//////
////
//


//////////////////////////////////////////////////////////////////////////////
// returns all invoices connected to the $p_deal_id deal :
// if $incl_arch = 1, we include archived invoices as well
//////////////////////////////////////////////////////////////////////////////
function run_query_search_connected_invoices ($p_deal_id, $incl_arch = 0) {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select invoice_id, invoice_inout ".
     ", invoice_label ".
     ", invoice_number ".
     ", invoice_amount_HT ".
     ", invoice_amount_TTC ".
     ", invoicestatus_label as invoice_status ".
     ", invoice_date as date".
     ", deal_label as invoice_deal ".
     ", company_name as invoice_company ".
     ", company_id".
     ", deal_id ".
     " from Deal, DealInvoice, Invoice, InvoiceStatus, Company ".
     " where deal_id = dealinvoice_deal_id ".
     " and dealinvoice_invoice_id = invoice_id ".
     " and invoice_invoicestatus_id = invoicestatus_id ".
     " and deal_company_id = company_id ".
     " and deal_id = '$p_deal_id'";
  if ($incl_arch == 0) {
    $query .= " and invoice_archive = '0' ";
  }

  display_debug_msg ("<b>run_query_search_connected_invoices () : </b>".$query, $cdg_sql);
  
  $go->query($query);

  return $go;
}


//////////////////////////////////////////////////////////////////////////////
// returns the number of archived invoices connected to a deal
//////////////////////////////////////////////////////////////////////////////
function run_query_nb_archived_invoices ($p_deal_id) {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select invoice_id from DealInvoice, Invoice
    where dealinvoice_invoice_id = invoice_id
      and dealinvoice_deal_id = '$p_deal_id'
      and invoice_archive = '1'";

  display_debug_msg ("<b> run_query_nb_archived_invoices() : </b>".$query, $cdg_sql);
  $go->query ($query);

  return $go->nf();
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Selection of the Deal according to Company id           //
///////////////////////////////////////////////////////////////////////////////
function run_query_deal($comp_id = "0") {
  global $cdg_sql;
  if ($comp_id == "0")
   $query = "select * from Deal ";
  else
   $query = "select * from Deal Where deal_company_id = $comp_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Search for contact linked to this Deal                  //
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_contract($deal_id) {
  global $cdg_sql;
  if($deal_id) {
    $query = "select count(*) as Num from Contract Where contract_deal_id = $deal_id";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("Num");
  }
  else
    return 0;
}


</SCRIPT>
