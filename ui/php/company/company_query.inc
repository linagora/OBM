<script language="php">
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

  // If a name has been given we search its sound too
  if ($name != "") {
    $auto_aka = reduce_name($name);
    $sound = phonetic_key($name);
  }

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
      company_address3,
      company_zipcode,
      company_town,
      company_expresspostal,
      company_country_id,
      company_phone,
      company_fax,
      company_web,
      company_email,
      company_contact_number,
      company_deal_number,
      company_deal_total,
      companytype_label,
      companyactivity_label
    from Company
         LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id
         LEFT JOIN CompanyType on company_type_id=companytype_id";

  $and = "";
  if ($name != "") {
    $where_name = " company_name like '$name%'";
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

  if (($where != "") || ($where_name != "")) {
    $whereq = " where $where_name $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "company_name";

  // Contact list and new contact column haven't any meaning for sorting
  if ( (strcmp($p_new_order,"company_contacts")==0) || (strcmp($p_new_order,"company_new_contact") == 0) ) {
    $order = "company_name";
  }

  $query = "$req $whereq order by $order $p_order_dir";

  display_debug_msg("company_search(1) : $query", $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  if ( ($obm_db->num_rows() == 0) && ($name != "") ) {
    $where_name = " (company_aka like '%$auto_aka%'
                 or company_sound = '$sound')";
    $whereq = " where $where_name $where";
    $query = "$req $whereq order by $order $p_order_dir";
    display_debug_msg("company_search(2) : $query", $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
  }

  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;

  $timeupdate = sql_date_format($type, "company_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "company_timecreate", "timecreate");

  $query = "select *,
      $timeupdate,
      $timecreate,
      m.userobm_lastname as market_lname,
      m.userobm_firstname as market_fname,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from Company
         LEFT JOIN CompanyType on company_type_id=companytype_id
         LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id
         LEFT JOIN UserObm as m on company_marketingmanager_id=m.userobm_id
         LEFT JOIN Country on company_country_id=country_id
         LEFT JOIN DataSource on company_datasource_id=datasource_id
         LEFT JOIN UserObm as c on company_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on company_userupdate=u.userobm_id
    where company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($company) {
  global $auth, $cdg_sql;

  $num = $company["num"];
  $archive = (isset($company["archive"]) ? $company["archive"] : '0');
  $name = $company["name"];
  $aka = $company["aka"];
  // If reduced version of name not in aka (and not same as name), we add it
  $auto_aka = reduce_name($name);
  if ( ($auto_aka != $name) && (strpos($aka, $auto_aka) === false) ) {
    $aka .= " $auto_aka";
  }
  $sound = phonetic_key($name);
  $dsrc = $company["datasource"];
  $kind = $company["kind"];
  $act = $company["activity"];
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $ad3 = $company["ad3"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["ctry"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["com"];
  $compcat = $company["cat"];
  
  $query = "insert into Company (company_timeupdate,
    company_timecreate,
    company_userupdate,
    company_usercreate,
    company_datasource_id,
    company_number,
    company_archive,
    company_name,
    company_aka,
    company_sound,
    company_type_id,
    company_activity_id,
    company_marketingmanager_id,
    company_address1,
    company_address2,
    company_address3,
    company_zipcode,
    company_town,
    company_expresspostal,
    company_country_id,
    company_phone,
    company_fax,
    company_web,
    company_email,
    company_comment)
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    " . $auth->auth["uid"] . ",
    '$dsrc',
    '$num',
    '$archive',
    '$name',
    '$aka',
    '$sound',
    '$kind',
    '$act',
    $market,
    '$ad1',
    '$ad2',
    '$ad3',
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

  $query = "select company_id as Id
    from Company
    where company_name = '$name'
      and company_type_id='$kind'
      and company_activity_id='$act'
      and company_marketingmanager_id='$market'
      and company_phone = '$phone'
      and company_zipcode = '$zip'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("Id");

  if ($retour && is_array($compcat)) {
    foreach($compcat as $categoryid) {
      $query = "insert into CompanyCategoryLink (
	  companycategorylink_company_id,
	  companycategorylink_category_id)
	values (
          '$id',
          '$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $cid       : company id
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($cid, $company) {
  global $auth, $cdg_sql;

  $num = $company["num"];
  $archive = (isset($company["archive"]) ? $company["archive"] : '0');
  $name = $company["name"];
  $aka = $company["aka"];
  // If reduced version of name not in aka (and not same as name), we add it
  $auto_aka = reduce_name($name);
  if ( ($auto_aka != $name) && (strpos($aka, $auto_aka) === false) ) {
    $aka .= " $auto_aka";
  }
  $sound = phonetic_key($name);
  $dsrc = $company["datasource"];
  $kind = $company["kind"];
  $act = $company["activity"];
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $ad3 = $company["ad3"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["ctry"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["com"];
  $compcat = $company["cat"];

  $query = "update Company set
    company_timeupdate='". date("Y-m-d H:i:s") ."',
    company_userupdate='". $auth->auth["uid"] ."',
    company_datasource_id='$dsrc',
    company_number='$num',
    company_archive='$archive',
    company_name='$name',
    company_aka='$aka',
    company_sound='$sound',
    company_type_id='$kind',
    company_activity_id='$act',
    company_marketingmanager_id='$market',
    company_address1='$ad1',
    company_address2='$ad2',
    company_address3='$ad3',
    company_zipcode='$zip',
    company_town='$town',
    company_expresspostal='$cdx',
    company_country_id='$ctry',
    company_phone='$phone',
    company_fax='$fax',
    company_web='$web',
    company_email='$email',
    company_comment='$com'
  where company_id='$cid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $query = "delete
    from CompanyCategoryLink
    where companycategorylink_company_id='$cid'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  if($retour && is_array($compcat)) {
    foreach($compcat as $categoryid) {
      $query = "insert into CompanyCategoryLink (
	companycategorylink_company_id,
	companycategorylink_category_id)
	values
	('$cid','$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }


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
  run_query_delete_document($p_id,"Company");
  $query = "delete from CompanyCategoryLink
    where companycategorylink_company_id='$p_id'";
  $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Company select query execution                                   //
// Returns : DB object result with all companies
///////////////////////////////////////////////////////////////////////////////
function run_query_active_company() {
  global $cdg_sql;

  $query = "select company_id, company_name, company_town
            from Company
            where company_archive='0'
            order by company_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
// Return all the deals attached to the company
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_deal_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct deal_id, deal_label
    from Deal
    where deal_company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contracts attached to the company
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contract_links($p_id) {
  global $cdg_sql;

  $query = "select distinct contract_id, contract_label 
            from Contract, Contact
            where contract_company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the projects attached to the company
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_project_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct project_id, project_name 
            from Project
            where project_company_id='$p_id'";

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

  $where_id = "";
  if ($p_id != "") {
    $where_id = "and company_id!='$p_id'";
  }
  $obm_q = new DB_OBM;
  $query = "select distinct company_id, company_name, company_zipcode 
            from Company
            where company_name like '$name%'
              $where_id";

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
// Category company query execution                                          //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companycat() {
  global $cdg_sql;

  $query = "select * from CompanyCategory"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// get detail from the Category of a company and sort it as a table
// Parameters:
//   - $id : Company ID
///////////////////////////////////////////////////////////////////////////////
function run_query_get_companycat($id) { 
  global $cdg_sql;

  $query = "select CompanyCategory.*
    from CompanyCategoryLink, CompanyCategory
    where companycategorylink_company_id = '$id' 
      and companycategorylink_category_id = companycategory_id
    order by  companycategory_code"; 

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $i=0;
  $links = run_query_ordered_category($obm_q);
  return $links;
}

///////////////////////////////////////////////////////////////////////////////
// get detail from the Category of a company 
// Parameters:
//   - $id : Company ID
///////////////////////////////////////////////////////////////////////////////
function run_query_get_companycat_name($id) { 
  global $cdg_sql;

  $query = "select *
    from CompanyCategoryLink,
      CompanyCategory
    where companycategorylink_company_id = '$id' 
      and companycategorylink_category_id = companycategory_id
    order by  companycategory_code"; 

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie insertion query construction and execution                      //
// Parameters:
//   - $company : company hash info : keys used : cat_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat = $company["cat_label"];
  $code = $company["cat_code"];

  $query = "insert into CompanyCategory (
    companycategory_timecreate,
    companycategory_usercreate,
    companycategory_label,
    companycategory_code)
  values (
    '$timecreate',
    '$usercreate',
    '$cat',
    '$code')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie update query execution                                          //
// Parameters:
//   - $company : company hash info : keys used : cat, cat_label,cat_code
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["cat"];
  $cat = $company["cat_label"];
  $code = $company["cat_code"];

  $query = "update CompanyCategory set
    companycategory_label='$cat',
    companycategory_code='$code',
    companycategory_timeupdate='$timeupdate',
    companycategory_userupdate='$userupdate'
      where
    companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie deletion query execution                                        //
// Parameters:
//   - $id : Categorie id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_delete($id) {
  global $cdg_sql;

  $query = "delete from CompanyCategory where companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie - Company links query execution                                 //
// Parameters:
//   - $p_id : cat id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_links($p_id) {
  global $cdg_sql;

  $query = "select company_name,
      company_id,
      '$p_id' as company_category_id
    from Company,
      CompanyCategoryLink
    where company_id=companycategorylink_company_id
      and companycategorylink_category_id = '$p_id'";

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
// Get the label of a Category from its id                                       //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat_label($id) {
  global $cdg_sql;

  $query = "select companycategory_label
    from CompanyCategory
    where companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $retour = $obm_q->f("companycategory_label");
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
  global $l_j_check_email, $l_j_check_web, $l_err_act_empty;
  global $err_msg;
  global $php_regexp_zip,$php_regexp_phone,$php_regexp_fax,$php_regexp_email,$php_regexp_web;

  $num = $company["num"];
  $name = $company["name"];
  $act = $company["activity"];
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

  // Company activity filled
  //  if ($company["activity"] < 1) {
  //    $err_msg = $l_err_act_empty;
  //    return false;
  //  }


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

///////////////////////////////////////////////////////////////////////////////
// Query to build the category tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_get_tree() {
  global $cdg_sql,$auth;

  $query = "select *  from CompanyCategory $where 
  order by  companycategory_code
";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;  

}

///////////////////////////////////////////////////////////////////////////////
// place Category in the correct order
// Parameters: DB object of categories.
///////////////////////////////////////////////////////////////////////////////
function run_query_ordered_category($cat_q) {
  $wc =0; 
  while($cat_q->next_record()) {
    $id = $cat_q->f("companycategory_id");
    $code = $cat_q->f("companycategory_code");
    $label = $cat_q->f("companycategory_label");
    $doc_info = array(
      "id" =>$id,
      "label" =>$cat_q->f("companycategory_label"),
      "code" => $code);
    $categories[] = $doc_info;
  }

  usort($categories,"category_compare");
  return $categories;  

}

///////////////////////////////////////////////////////////////////////////////
// Compare 2 categories with their code
// Parameters: 
//   - $cats   : categories array
//   - $m      : index of first category
//   - $n      : index of second category
///////////////////////////////////////////////////////////////////////////////

function category_compare($m, $n) {
  global $wc;
  $wc++;
  $code_1 = explode(".",$m["code"]);
  $code_2 = explode(".",$n["code"]);
  $i=0;
  while(isset($code_1[$i]) || isset($code_2[$i])) {
    if($code_1[$i] > $code_2[$i]){
      return 1;
    }
    elseif($code_1[$i] < $code_2[$i]) {
      return -1;
    }
    $i++;
  }
  return 0;
}
</script>
