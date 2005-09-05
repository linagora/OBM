<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_query.inc                                            //
//     - Desc : company query File                                           //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Company search query execution
// Parametes:
//   - $company[]    : company search criteria
//     keys used       archive, name, phone, kind, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($company, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $fuzzy = $company["fuzzy"];
  $archive = $company["archive"];
  $name = sql_search_text_parse($company["name"]);
  $phone = sql_search_text_parse($company["phone"]);
  $town = sql_search_text_parse($company["town"]);
  $ctry = $company["country"];
  $kind = $company["kind"];
  $cat = $company["cat"];
  $cat_tree = $company["cat_tree"];
  $cat_code = $company["cat_code"];
  $act = $company["activity"];
  $naf = $company["nafcode"];
  $market = $company["marketing_manager"];
  $dsrc = $company["datasource"];
  $zip = sql_search_text_parse($company["zip"]);
  $dateafter = $company["dateafter"];
  $datebefore = $company["datebefore"];
  $lang = get_lang();

  // If a name has been given we search its sound too
  if ($name != "") {
    $auto_aka = format_name($name, 0, true, true);
    $sound = phonetic_key($name);
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $and = "";
  if ($name != "") {
    $where_name = " (company_name $like '$name%' or company_aka like '$name%')";
    $and = "and";
  }
  if (($kind != $c_all) && ($kind != "")) { 
    $where .= " $and company_type_id='$kind'";
    $and = "and";
  }
  if (($cat != $c_all) && ($cat != "")) {
    $join_cat = "LEFT JOIN CompanyCategoryLink on company_id=companycategorylink_company_id";
    if ($cat_tree) {
      $join_cat .= " LEFT JOIN CompanyCategory on companycategorylink_category_id=companycategory_id";
      $where_cat = " or companycategory_code like '$cat_code%'";
    }
    $where .= " $and (companycategorylink_category_id='$cat'$where_cat)";
    $and = "and";
  }
  if (($act != $c_all) && ($act != "")) { 
    $where .= " $and company_activity_id='$act'";
    $and = "and";
  }
  if (($naf != $c_all) && ($naf != "")) { 
    $where .= " $and company_nafcode_id='$naf'";
    $and = "and";
  }
  if (($market != $c_all) && ($market != "")) { 
    $where .= " $and company_marketingmanager_id='$market'";
    $and = "and";
  }
  if (($dsrc != $c_all) && ($dsrc != "")) { 
    $where .= " $and company_datasource_id='$dsrc'";
    $and = "and";
  }
  if ($phone != "") {
    $where .= " $and company_phone $like '$phone%'";
    $and = "and";
  }
  if ($town != "") {
    $where .= " $and company_town $like '$town%'";
    $and = "and";
  }
  if (($ctry != $c_all) && ($ctry != "")) { 
    $where .= " $and company_country_iso3166='$ctry'";
    $and = "and";
  }
  if ($zip != "") {
    $where .= " $and company_zipcode $like '$zip%'";
    $and = "and";
  }
  if ($dateafter != "") {
    $where .= " $and (company_timeupdate > '$dateafter'
                     or company_timecreate > '$dateafter')";
    $and = "and";
  }
  if ($datebefore != "") {
    $where .= " $and (company_timeupdate < '$datebefore'
                     and company_timecreate < '$datebefore')";
    $and = "and";
  }

  // Get only not archived by default
  if ($archive != "1") {
    $where .= " $and company_archive=0";
    $and = "and";
  }

  if (($where != "") || ($where_name != "")) {
    $whereq = " WHERE $where_name $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "company_name";

  // Order exceptions (order on calculated rows)
  // Contact list and new contact column haven't any meaning for sorting
  if ( (strcmp($p_new_order,"company_contacts")==0)
      || (strcmp($p_new_order,"company_new_contact") == 0) ) {
    $order = "company_name";
  } else if (strcmp($p_new_order,"company_address")==0) {
    $order = "company_address1";
    $order_next = ", company_address2, company_address3, company_zipcode, company_town";
  }

  $select = "SELECT distinct
      company_id as id,
      company_id,
      company_name,
      company_number,
      company_vat,
      company_archive,
      company_type_id,
      company_activity_id,
      company_nafcode_id,
      company_marketingmanager_id,
      company_address1,
      company_address2,
      company_address3,
      company_zipcode,
      company_town,
      company_expresspostal,
      company_country_iso3166,
      company_phone,
      company_fax,
      company_web,
      company_email,
      company_contact_number,
      company_deal_number,
      company_deal_total,
      companytype_label,
      companyactivity_label,
      companynafcode_code,
      companynafcode_label,
      country_name
    FROM Company";

  $left_join = "
         LEFT JOIN CompanyType on company_type_id=companytype_id
         $join_cat
         LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode on company_nafcode_id=companynafcode_id
         LEFT JOIN Country on company_country_iso3166=country_iso3166
                   and country_lang='$lang'";

  if ((! $fuzzy) || ($name == "")) {
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct company_id) FROM Company $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq ORDER BY $order $p_order_dir $order_next $limit";
      display_debug_msg($query, $cdg_sql, "company_search(1)");
      $obm_q->query($query);
      if (! $ctu_sql_limit) {
	$count = $obm_q->num_rows();
      }
    }
  }

  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($name != "") ) {
    $where_name = " ($where_name or company_aka $like '%$auto_aka%'
                 or company_sound = '$sound')";
    $whereq = " WHERE $where_name $where";
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct company_id) FROM Company $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    if (($count > 0)  || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq ORDER BY $order $p_order_dir $order_next $limit";
      display_debug_msg($query, $cdg_sql, "company_search(2)");
      $obm_q->query($query);
    }
  }
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "company_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "company_timecreate", "timecreate");
  $lang = get_lang();

  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      m.userobm_lastname as market_lname,
      m.userobm_firstname as market_fname,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Company
         LEFT JOIN CompanyType on company_type_id=companytype_id
         LEFT JOIN CompanyActivity on company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode on company_nafcode_id=companynafcode_id
         LEFT JOIN UserObm as m on company_marketingmanager_id=m.userobm_id
         LEFT JOIN Country on company_country_iso3166=country_iso3166
                   and country_lang='$lang'
         LEFT JOIN DataSource on company_datasource_id=datasource_id
         LEFT JOIN UserObm as c on company_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on company_userupdate=u.userobm_id
    WHERE company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($company) {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");

  $num = $company["num"];
  $vat = $company["vat"];
  $archive = (isset($company["archive"]) ? $company["archive"] : '0');
  $name = $company["name"];
  $aka = trim($company["aka"]);
  // If aka is empty we auto fill it
  if ($aka == "") {
    $auto_aka = format_name($name, 0, true, true);
    if ($auto_aka != $name) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($name);
  $dsrc = $company["datasource"];
  $kind = ($company["kind"] != "" ? $company["kind"] : "0");
  $act = ($company["activity"] != "" ? $company["activity"] : "0");
  $naf = ($company["nafcode"] != "" ? $company["nafcode"] : "0");
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $ad3 = $company["ad3"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["country"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $add_comment = $company["add_comment"];
  if ($add_comment != "") {
    $datecomment = $company["datecomment"];
    $usercomment = $company["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $compcat = $company["cat"];
  
  $query = "INSERT INTO Company (company_timeupdate,
    company_timecreate,
    company_userupdate,
    company_usercreate,
    company_datasource_id,
    company_number,
    company_vat,
    company_archive,
    company_name,
    company_aka,
    company_sound,
    company_type_id,
    company_activity_id,
    company_nafcode_id,
    company_marketingmanager_id,
    company_address1,
    company_address2,
    company_address3,
    company_zipcode,
    company_town,
    company_expresspostal,
    company_country_iso3166,
    company_phone,
    company_fax,
    company_web,
    company_email,
    company_comment)
  VALUES (null,
    '$now',
    null,
    '$uid',
    '$dsrc',
    '$num',
    '$vat',
    '$archive',
    '$name',
    '$aka',
    '$sound',
    '$kind',
    '$act',
    '$naf',
    '$market',
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
    '$comment')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($kind != 0) {
    $kind_sql = "AND company_type_id='$kind'";
  }
  if ($market != 0) {
    $marquet_sql = "AND company_marketingmanager_id='$market'";
  }
  if ($act != 0) {
    $act_sql = "AND company_activity_id='$act'";
  }

  $query = "SELECT company_id as id
    FROM Company
    WHERE company_name = '$name'
      AND company_timecreate='$now'
      $kind_sql
      $marquet_sql
      $act_sql
      AND company_phone = '$phone'
      AND company_zipcode = '$zip'";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("id");

  if ($retour && is_array($compcat)) {
    foreach($compcat as $categoryid) {
      $query = "INSERT INTO CompanyCategoryLink (
	  companycategorylink_company_id,
	  companycategorylink_category_id)
	VALUES (
          '$id',
          '$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }
  
  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $cid       : company id
//   - $company[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($cid, $company) {
  global $auth, $cdg_sql, $cgp_hide;

  $num = $company["num"];
  $vat = $company["vat"];
  $archive = (isset($company["archive"]) ? $company["archive"] : '0');
  $name = $company["name"];
  $aka = trim($company["aka"]);
  // If aka is empty we auto fill it
  if ($aka == "") {
    $auto_aka = format_name($name, 0, true, true);
    if ($auto_aka != $name) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($name);
  $dsrc = $company["datasource"];
  $kind = $company["kind"]; 
  if ($kind != "") $upd_kind = "company_type_id='$kind',";
  $act = $company["activity"];
  if ($act != "") $upd_act = "company_activity_id='$act',";
  $naf = $company["nafcode"];
  if ($naf != "") $upd_naf = "company_nafcode_id='$naf',";
  $market = $company["marketing_manager"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $ad3 = $company["ad3"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["country"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $comment = $company["comment"];
  $add_comment = $company["add_comment"];
  if ($add_comment != "") {
    $datecomment = $company["datecomment"];
    $usercomment = $company["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $compcat = $company["cat"];

  $query = "UPDATE Company SET
    company_timeupdate='". date("Y-m-d H:i:s") ."',
    company_userupdate='". $auth->auth["uid"] ."',
    company_datasource_id='$dsrc',
    company_number='$num',
    company_vat='$vat',
    company_archive='$archive',
    company_name='$name',
    company_aka='$aka',
    company_sound='$sound',
    $upd_kind
    $upd_act
    $upd_naf
    company_marketingmanager_id='$market',
    company_address1='$ad1',
    company_address2='$ad2',
    company_address3='$ad3',
    company_zipcode='$zip',
    company_town='$town',
    company_expresspostal='$cdx',
    company_country_iso3166='$ctry',
    company_phone='$phone',
    company_fax='$fax',
    company_web='$web',
    company_email='$email',
    company_comment='$comment'
  WHERE company_id='$cid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $query = "DELETE
    FROM CompanyCategoryLink
    WHERE companycategorylink_company_id='$cid'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  if ($retour && is_array($compcat)) {
    foreach($compcat as $categoryid) {
      $query = "INSERT INTO CompanyCategoryLink (
	companycategorylink_company_id,
	companycategorylink_category_id)
	VALUES
	('$cid','$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }


  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  $query = "DELETE FROM Company WHERE company_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  run_query_delete_document_links($p_id, "company");

  $query = "DELETE FROM CompanyCategoryLink
    WHERE companycategorylink_company_id='$p_id'";
  $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Auto transform company name (could become a HOOK)
// Parameters:
//   - $name : company name
// Returns:
//   $name formatted
///////////////////////////////////////////////////////////////////////////////
function get_format_company_name($name) {
  global $caf_company_name;

  $res = $name;
  if ($caf_company_name) {
    $res = format_name($res, 2, true, false);
  }

  return $res;
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
    $where_id = "AND company_id!='$p_id'";
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $query = "SELECT DISTINCT company_id, company_name, company_zipcode 
            FROM Company
            WHERE company_name $like '$name%'
              $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Kind company query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companytype() {
  global $cdg_sql, $cgp_hide;

  $show_kind = (! $cgp_hide["company"]["companytype_label"]);
  if ($show_kind) {
    $query = "SELECT * FROM CompanyType ORDER BY companytype_label"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Kind insertion query construction and execution
// Parameters:
//   - $company : company hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $kind = $company["kind_label"];

  $query = "INSERT INTO CompanyType (
    companytype_timecreate,
    companytype_usercreate,
    companytype_label)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$kind')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind update query execution
// Parameters:
//   - $company : company hash info : keys used : kind, kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["kind"];
  $kind = $company["kind_label"];

  $query = "UPDATE CompanyType SET
    companytype_label='$kind',
    companytype_timeupdate='$timeupdate',
    companytype_userupdate='$userupdate'
  WHERE
    companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind deletion query execution
// Parameters:
//   - $id : Kind id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM CompanyType WHERE companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind - Company links query execution
// Parameters:
//   - $p_id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_links($p_id) {
  global $cdg_sql;

  $query = "SELECT company_name, company_id, company_type_id
    FROM Company
    WHERE company_type_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie insertion query construction and execution
// Parameters:
//   - $company : company hash info : keys used : cat_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat = $company["cat_label"];
  $code = $company["cat_code"];

  $query = "INSERT INTO CompanyCategory (
    companycategory_timecreate,
    companycategory_usercreate,
    companycategory_label,
    companycategory_code)
  VALUES (
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
// Categorie update query execution
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

  $query = "UPDATE CompanyCategory SET
    companycategory_label='$cat',
    companycategory_code='$code',
    companycategory_timeupdate='$timeupdate',
    companycategory_userupdate='$userupdate'
  WHERE
    companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie deletion query execution
// Parameters:
//   - $id : Categorie id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM CompanyCategory WHERE companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Categorie - Company links query execution
// Parameters:
//   - $p_id : cat id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat_links($p_id) {
  global $cdg_sql;

  $query = "SELECT company_name,
      company_id,
      '$p_id' as company_category_id
    FROM Company,
      CompanyCategoryLink
    WHERE company_id=companycategorylink_company_id
      AND companycategorylink_category_id = '$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Activity company query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companyactivity() {
  global $cdg_sql, $cgp_hide;

  $show_act = (! $cgp_hide["company"]["companyactivity_label"]);
  if ($show_act) {
    $query = "SELECT * FROM CompanyActivity ORDER BY companyactivity_label"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Activity insertion query construction and execution
// Parameters:
//   - $company : company hash info : keys used : act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $act = $company["act_label"];

  $query = "INSERT INTO CompanyActivity (
      companyactivity_timecreate,
      companyactivity_usercreate,
      companyactivity_label)
    VALUES (
      '$timecreate',
      '$usercreate',
      '$act')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity update query execution
// Parameters:
//   - $company : company hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["activity"];
  $activity = $company["act_label"];

  $query = "UPDATE CompanyActivity SET
      companyactivity_label='$activity',
      companyactivity_timeupdate='$timeupdate',
      companyactivity_userupdate='$userupdate'
    WHERE
      companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity deletion query execution
// Parameters:
//   - $id : Activity id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM CompanyActivity WHERE companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Company links query execution
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_activity_links($p_id) {
  global $cdg_sql;

  $query = "SELECT company_name, company_id, company_activity_id
    FROM Company
    WHERE company_activity_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code company query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companynafcode() {
  global $cdg_sql, $cgp_hide;

  $show_naf = (! $cgp_hide["company"]["companynafcode_code"]);
  if ($show_naf) {
    $query = "SELECT * FROM CompanyNafCode ORDER BY companynafcode_code"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code insertion query construction and execution
// Parameters:
//   - $company : company hash info : keys used : naf_code, naf_label
///////////////////////////////////////////////////////////////////////////////
function run_query_nafcode_insert($company) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $title = (isset($company["naf_title"]) ? $company["naf_title"] : '0');
  $code = $company["naf_code"];
  $label = $company["naf_label"];

  $query = "INSERT INTO CompanyNafCode (
      companynafcode_timecreate,
      companynafcode_usercreate,
      companynafcode_title,
      companynafcode_code,
      companynafcode_label)
    VALUES (
      '$timecreate',
      '$usercreate',
      '$title',
      '$code',
      '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code update query execution
// Parameters:
//   - $company : company hash info : keys used : naf_code, naf_label
///////////////////////////////////////////////////////////////////////////////
function run_query_nafcode_update($company) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $company["nafcode"];
  $code = $company["naf_code"];
  $label = $company["naf_label"];

  $query = "UPDATE CompanyNafCode SET
      companynafcode_code='$code',
      companynafcode_label='$label',
      companynafcode_timeupdate='$timeupdate',
      companynafcode_userupdate='$userupdate'
    WHERE
      companynafcode_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code deletion query execution
// Parameters:
//   - $id : Naf code id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_nafcode_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM CompanyNafCode WHERE companynafcode_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code - Company links query execution
// Parameters:
//   - $p_id : naf code id
///////////////////////////////////////////////////////////////////////////////
function run_query_nafcode_links($p_id) {
  global $cdg_sql;

  $query = "SELECT company_name, company_id, company_nafcode_id
    FROM Company
    WHERE company_nafcode_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Kind from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_kind_label($id) {
  global $cdg_sql;

  $query = "SELECT companytype_label
    FROM CompanyType
    WHERE companytype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("companytype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Category from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat_label($id) {
  global $cdg_sql;

  $query = "SELECT companycategory_label
    FROM CompanyCategory
    WHERE companycategory_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $retour = $obm_q->f("companycategory_label");
  return $retour;
}      


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Activity from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_activity_label($id) {
  global $cdg_sql;

  $query = "SELECT companyactivity_label
    FROM CompanyActivity
    WHERE companyactivity_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("companyactivity_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Naf Code from its id (code + label)
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_nafcode_label($id) {
  global $cdg_sql;

  $query = "SELECT companynafcode_code,
      companynafcode_label
    FROM CompanyNafCode
    WHERE companynafcode_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("companynafcode_code") . " - " . $obm_q->f("companynafcode_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Company environment checking (same companies exists ?)
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
// Company Form Data checking and formatting
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
  if (($zip != "") && (preg_match($php_regexp_zip, $zip) == 0)) {
    $err_msg = $l_j_check_zip . " : $zip";
    return false;
  }

  // Company phone
  if (($phone != "") && (preg_match($php_regexp_phone, $phone) == 0)) {
    $err_msg = $l_j_check_phone . " : $phone";
    return false;
  }

  // Company fax
  if (($fax != "") && (preg_match($php_regexp_fax, $fax) == 0)) {
    $err_msg = $l_j_check_fax . " : $fax";
    return false;
  }

  // Company email
  if (($email != "") && (preg_match($php_regexp_email, $email) == 0)) {
    $err_msg = $l_j_check_email . " : $email";
    return false;
  }

  // Company web
  if (($web != "") && (preg_match($php_regexp_web, $web) == 0)) {
    $err_msg = $l_j_check_web . " : $web";
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Check if the company can be deleted
// Parameters:
//   - $p_id : company id
// Returns:
//   true if the company can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_company($p_id) {
  global $err_msg, $ok_msg;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $l_link_contract, $l_link_contract_no, $l_link_project;
  global $l_link_invoice, $l_link_invoice_no, $l_link_project_no;

  $delete_ok = true;

  // Links from Contact
  $nb = get_linked_contact_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_contact";
  } else {
    $ok_msg .= "$l_link_contact_no";
  }

  // Links from deals
  $nb = get_linked_deal_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_deal";
  } else {
    $ok_msg .= "<br />$l_link_deal_no";
  }

  // Links from Contracts
  $nb = get_linked_contract_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_contract";
  } else {
    $ok_msg .= "<br />$l_link_contract_no";
  }

  // Links from Project
  $nb = get_linked_project_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_project";
  } else {
    $ok_msg .= "<br />$l_link_project_no";
  }

  // Links from Invoice
  $nb = get_linked_invoice_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_invoice";
  } else {
    $ok_msg .= "<br />$l_link_invoice_no";
  }

  return $delete_ok;
}


</script>
