<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_query.inc                                            //
//     - Desc : company query File                                           //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Company search query execution                                            //
// Parametes:
//   - $company[]    : company search criteria
//     keys used       archive, name, phone, kind, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($company, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql;  

  $archive = $company["archive"];
  $name = $company["name"];
  $phone = $company["phone"];
  $kind = $company["kind"];
  $act = $company["activity"];
  $market = $company["marketing_manager"];
  $zip = $company["zip"];

  $req = "select
      company_id as Id,
      company_id,
      company_name,
      company_archive,
      company_type_id,
      company_activity_id,
      company_marketingmanager_id,
      company_address1,
      company_address2,
      company_zipcode,
      company_town,
      company_expresspostal,
      company_country,
      company_phone,
      company_fax,
      company_web,
      company_email,
      company_contact_number,
      company_deal_number,
      company_deal_total,
      companytype_label,
      companyactivity_label
    from (Company
         LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id)
         LEFT JOIN CompanyType on company_type_id=companytype_id";

  $and = "";
  if ($name != "") {
    $where .= " company_name like '$name%'";
    $and = "and";
  }
  if (($kind != $c_all) && ($kind != "")) { 
    $where .= " $and company_type_id='$kind'";
    $and = "and";
  }
  if (($act != $c_all) && ($act != "")) { 
    $where .= " $and company_activity_id='$act'";
    $and = "and";
  }
  if (($market != $c_all) && ($market != "")) { 
    $where .= " $and company_marketingmanager_id='$market'";
    $and = "and";
  }
  if ($phone != "") {
    $where .= " $and company_phone like '$phone%'";
    $and = "and";
  }
  if ($zip != "") {
    $where .= " $and company_zipcode like '$zip%'";
    $and = "and";
  }
  // Get only not archived by default
  if ($archive != "1") {
    $where .= " $and company_archive=0";
    $and = "and";
  }

  if ($where != "") {
    $where = " where $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "company_name";

  // Contact list and new contact column haven't any meaning for sorting
  if ( (strcmp($p_new_order,"company_contacts")==0) || (strcmp($p_new_order,"company_new_contact") == 0) ) {
    $order = "company_name";
  }

  $req .= "$where order by $order $p_order_dir";

  display_debug_msg($req, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($req);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  if ($obm_q->type == $db_type_mysql) {
    $query = "select *,
        UNIX_TIMESTAMP(company_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(company_timecreate) as timecreate
      from (Company LEFT JOIN CompanyType on company_type_id=companytype_id)
           LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id
      where company_id='$p_id'";
  } else if ($obm_q->type == $db_type_pgsql) {
    $query = "select *,company_timeupdate as timeupdate, company_timecreate as timecreate from Company where company_id=".$p_id;
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($company) {
  global $auth, $cdg_sql;

  $num = $company["num"];
  $archive = $company["archive"];
  $name = $company["name"];
  $kind = $company["kind"];
  $act = $company["activity"];
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["ctry"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["com"];

  $query = "insert into Company (company_timeupdate,
    company_timecreate,
    company_userupdate,
    company_usercreate,
    company_number,
    company_archive,
    company_name,
    company_type_id,
    company_activity_id,
    company_marketingmanager_id,
    company_address1,
    company_address2,
    company_zipcode,
    company_town,
    company_expresspostal,
    company_country,
    company_phone,
    company_fax,
    company_web,
    company_email,
    company_comment)
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    " . $auth->auth["uid"] . ",
    '$num',
    '$archive',
    '$name',
    '$kind',
    '$act',
    '$market',
    '$ad1',
    '$ad2',
    '$zip',
    '$town',
    '$cdx',
    '$ctry',
    '$phone',
    '$fax',
    '$web',
    '$email',
    '$com')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $cid       : company id
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($cid, $company) {
  global $auth, $cdg_sql;

  $num = $company["num"];
  $archive = $company["archive"];
  $name = $company["name"];
  $kind = $company["kind"];
  $act = $company["activity"];
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["ctry"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["com"];

  $query = "update Company set
    company_timeupdate='". date("Y-m-d H:i:s") ."',
    company_userupdate='". $auth->auth["uid"] ."',
    company_number='$num',
    company_archive='$archive',
    company_name='$name',
    company_type_id='$kind',
    company_activity_id='$act',
    company_marketingmanager_id='$market',
    company_address1='$ad1',
    company_address2='$ad2',
    company_zipcode='$zip',
    company_town='$town',
    company_expresspostal='$cdx',
    company_country='$ctry',
    company_phone='$phone',
    company_fax='$fax',
    company_web='$web',
    company_email='$email',
    company_comment='$com'
  where company_id='$cid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  $query = "delete from Company where company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Company select query execution                                   //
// Returns : DB object result with all companies
///////////////////////////////////////////////////////////////////////////////
function run_query_company() {
  global $cdg_sql;

  $query = "select company_id, company_name, company_town
            from Company order by company_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the deals attached to the company
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_deal_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct deal_id,deal_label 
            from Deal,Contact
            where
              deal_company_id='$p_id'
              or
              (contact_company_id='$p_id'
                and (deal_contact1_id=contact_id 
                  or deal_contact2_id=contact_id
                  or deal_marketingmanager_id=contact_id
                  or deal_technicalmanager_id=contact_id))";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contacts attached to the company
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contact_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct contact_id, contact_lastname, contact_firstname 
            from Contact
            where contact_company_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the companies with this name and zip (not with this id if set)
// Parameters:
//   - $p_id : company Id not to return
//   - $name : name
//   - $zip  : zip
///////////////////////////////////////////////////////////////////////////////
function run_query_check_company($p_id, $name, $zip) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct company_id, company_name, company_zipcode 
            from Company
            where company_id!='$p_id'
              and company_name like '$name%'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Kind company query execution                                              //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companytype() {
  global $cdg_sql;

  $query = "select * from CompanyType order by companytype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Kind insertion query construction and execution                           //
// Parameters:
//   - $company : company hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $kind = $company["kind_label"];

  $query = "insert into CompanyType (
    companytype_timecreate,
    companytype_usercreate,
    companytype_label)
  values (
    '$timecreate',
    '$usercreate',
    '$kind')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind update query execution                                               //
// Parameters:
//   - $company : company hash info : keys used : kind, kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["kind"];
  $kind = $company["kind_label"];

  $query = "update CompanyType set
    companytype_label='$kind',
    companytype_timeupdate='$timeupdate',
    companytype_userupdate='$userupdate'
      where
    companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind deletion query execution                                             //
// Parameters:
//   - $id : Kind id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_delete($id) {
  global $cdg_sql;

  $query = "delete from CompanyType where companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind - Company links query execution                                      //
// Parameters:
//   - $p_id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_links($p_id) {
  global $cdg_sql;

  $query = "select company_name, company_id, company_type_id from Company where company_type_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Activity company query execution                                          //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companyactivity() {
  global $cdg_sql;

  $query = "select * from CompanyActivity order by companyactivity_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Activity insertion query construction and execution                       //
// Parameters:
//   - $company : company hash info : keys used : act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $act = $company["act_label"];

  $query = "insert into CompanyActivity (
      companyactivity_timecreate,
      companyactivity_usercreate,
      companyactivity_label)
    values (
      '$timecreate',
      '$usercreate',
      '$act')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity update query execution                                           //
// Parameters:
//   - $company : company hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["activity"];
  $activity = $company["act_label"];

  $query = "update CompanyActivity set
      companyactivity_label='$activity',
      companyactivity_timeupdate='$timeupdate',
      companyactivity_userupdate='$userupdate'
    where
      companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity deletion query execution                                         //
// Parameters:
//   - $id : Activity id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_delete($id) {
  global $cdg_sql;

  $query = "delete from CompanyActivity where companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Company links query execution                                  //
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_links($p_id) {
  global $cdg_sql;

  $query = "select company_name, company_id, company_activity_id
    from Company where company_activity_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Kind from its id                                       //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_kind_label($id) {
  global $cdg_sql;

  $query = "select companytype_label
    from CompanyType
    where companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("companytype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Activity from its id                                  //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_activity_label($id) {
  global $cdg_sql;

  $query = "select companyactivity_label
    from CompanyActivity
    where companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("companyactivity_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name (lastname firstname) of the userobm from his Id              //
// Parameters:
//   - $id : userobm Id
///////////////////////////////////////////////////////////////////////////////
function get_userobm_name($id) {
  global $cdg_sql;

  $query = "select userobm_lastname, userobm_firstname
            from UserObm
            where userobm_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Company environment checking (same companies exists ?)                    //
// Parameters: :/ Tous les champs ici
//   - $cid       : company id
//   - $company[] : company's values
// Returns:
//   - Company Database object with list of similar companies
///////////////////////////////////////////////////////////////////////////////
function check_company_context($cid, $company) {

  $num = $company["num"];
  $name = $company["name"];
  $zip = $company["zip"];

  // if a company with same name and zip exists, return false
  $co_q = run_query_check_company($cid, $name, $zip);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Company Form Data checking and formatting                                 //
// Parameters:
//   - $cid   : company id
//   - $company[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($cid, $company) {
  global $l_fill_name, $l_j_check_phone, $l_j_check_zip, $l_j_check_fax;
  global $l_j_check_email, $l_j_check_web;
  global $err_msg;
  global $php_regexp_zip,$php_regexp_phone,$php_regexp_fax,$php_regexp_email,$php_regexp_web;

  $num = $company["num"];
  $name = $company["name"];
  $zip = $company["zip"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];

  // MANDATORY: Company name
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // Company Zip code
  if (($zip != "") && (ereg($php_regexp_zip, $zip) == false)) {
    $err_msg = $l_j_check_zip . " : $zip";
    return false;
  }

  // Company phone
  if (($phone != "") && (ereg($php_regexp_phone, $phone) == false)) {
    $err_msg = $l_j_check_phone . " : $phone";
    return false;
  }

  // Company fax
  if (($fax != "") && (ereg($php_regexp_fax, $fax) == false)) {
    $err_msg = $l_j_check_fax . " : $fax";
    return false;
  }

  // Company email
  if (($mail != "") && (eregi($php_regexp_email, $email) == false)) {
    $err_msg = $l_j_check_email . " : $email";
    return false;
  }

  // Company web
  if (($web != "") && (eregi($php_regexp_web, $web) == false)) {
    $err_msg = $l_j_check_web . " : $web";
    return false;
  }

  return true; 
}


</SCRIPT>
