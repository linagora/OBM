<?php
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
//     keys used       archive, name, phone, type, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_company_search($company, $p_new_order, $p_order_dir) {
  global $c_all, $c_none, $cdg_sql, $ctu_sql_limit;

  $fuzzy = $company["fuzzy"];
  $archive = $company["archive"];
  $name = sql_search_text_parse($company["name"]);
  $phone = sql_search_text_parse($company["phone"]);
  $town = sql_search_text_parse($company["town"]);
  $ctry = $company["country"];
  $type = $company["type"];
  $category1 = $company["category1"];
  $category1_tree = $company["category1_tree"];
  $category1_code = $company["category1_code"];
  $act = $company["activity"];
  $naf = $company["nafcode"];
  $market = $company["marketing_manager"];
  $dsrc = $company["datasource"];
  $zip = sql_search_text_parse($company["zip"]);
  $dateafter = $company["dateafter"];
  $datebefore = $company["datebefore"];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $and = "";
  if ($name != "") {
    $where_name = " (company_name $like '$name%' OR company_aka like '$name%')";
    $and = "AND";
  }
  if (($type != $c_all) && ($type != "")) { 
    $where .= " $and company_type_id='$type'";
    $and = "and";
  }
  if (($category1 != $c_all) && ($category1 != $c_none) && ($category1 != "")) {
    $join_category1 = "LEFT JOIN CompanyCategory1Link ON company_id=companycategory1link_company_id";
    if ($category1_tree) {
      $join_category1 .= " LEFT JOIN CompanyCategory1 ON companycategory1link_category_id=companycategory1_id";
      $where_category1 = " OR companycategory1_code like '$category1_code%'";
    }
    $where .= " $and (companycategory1link_category_id='$category1'$where_category1)";
    $and = "AND";
  } else if ($category1 == $c_none) {
    $join_category1 = "LEFT JOIN CompanyCategory1Link ON company_id=companycategory1link_company_id";
    $where .= " $and companycategory1link_category_id IS NULL";
    $and = "AND";
  }
  if (($act != $c_all) && ($act != "")) { 
    $where .= " $and company_activity_id='$act'";
    $and = "AND";
  }
  if (($naf != $c_all) && ($naf != "")) { 
    $where .= " $and company_nafcode_id='$naf'";
    $and = "AND";
  }
  if (($market != $c_all) && ($market != "")) { 
    $where .= " $and company_marketingmanager_id='$market'";
    $and = "AND";
  }
  if (($dsrc != $c_all) && ($dsrc != "")) { 
    $where .= " $and company_datasource_id='$dsrc'";
    $and = "AND";
  }
  if ($phone != "") {
    $where .= " $and company_phone $like '$phone%'";
    $and = "AND";
  }
  if ($town != "") {
    $where .= " $and company_town $like '$town%'";
    $and = "AND";
  }
  if (($ctry != $c_all) && ($ctry != "")) { 
    $where .= " $and company_country_iso3166='$ctry'";
    $and = "AND";
  }
  if ($zip != "") {
    $where .= " $and company_zipcode $like '$zip%'";
    $and = "AND";
  }
  if ($dateafter != "") {
    $where .= " $and (company_timeupdate > '$dateafter'
                     OR company_timecreate > '$dateafter')";
    $and = "AND";
  }
  if ($datebefore != "") {
    $where .= " $and (company_timeupdate < '$datebefore'
                     AND company_timecreate < '$datebefore')";
    $and = "AND";
  }

  // Get only not archived by default
  if ($archive != "1") {
    $where .= " $and company_archive=0";
    $and = "AND";
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
  $orderq = " ORDER BY $order $p_order_dir $order_next";


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
      companytype_label as type_label,
      companyactivity_label as activity_label,
      companynafcode_code as nafcode_code,
      companynafcode_label as nafcode_label,
      country_name
    FROM Company";

  $left_join = "
         LEFT JOIN CompanyType ON company_type_id=companytype_id
         $join_category1
         LEFT JOIN CompanyActivity ON company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode ON company_nafcode_id=companynafcode_id
         LEFT JOIN Country ON company_country_iso3166=country_iso3166
                   AND country_lang='$lang'";

  if ((! $fuzzy) || ($name == "")) {
    // If result limited, we get the global rows number
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct company_id) FROM Company $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    // If any results (from limited) or result not limited, we get the data
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, "company_search(1)");
      $obm_q->query($query);
      if (! $ctu_sql_limit) {
	$count = $obm_q->num_rows();
      }
    }
  }

  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($name != "") ) {
    $auto_aka = format_name($name, 0, true, true);
    $sound = phonetic_key($name);
    $where_name = " ($where_name OR company_aka $like '%$auto_aka%'
                 OR company_sound = '$sound')";
    $whereq = " WHERE $where_name $where";
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct company_id) FROM Company $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
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
function run_query_company_detail($p_id) {
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
         LEFT JOIN CompanyType ON company_type_id=companytype_id
         LEFT JOIN CompanyActivity ON company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode ON company_nafcode_id=companynafcode_id
         LEFT JOIN UserObm as m ON company_marketingmanager_id=m.userobm_id
         LEFT JOIN Country ON company_country_iso3166=country_iso3166
                   AND country_lang='$lang'
         LEFT JOIN DataSource ON company_datasource_id=datasource_id
         LEFT JOIN UserObm as c ON company_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON company_userupdate=u.userobm_id
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
//     keys used  : num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com, category1
///////////////////////////////////////////////////////////////////////////////
function run_query_company_insert($company) {
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
  $type = ($company["type"] != "" ? $company["type"] : "0");
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
  $category1 = $company["category1"];
  
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
    '$type',
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

  if ($type != 0) {
    $type_sql = "AND company_type_id='$type'";
  }
  if ($market != 0) {
    $marquet_sql = "AND company_marketingmanager_id='$market'";
  }
  if ($act != 0) {
    $act_sql = "AND company_activity_id='$act'";
  }

  // Get the company id
  $query = "SELECT company_id as id
    FROM Company
    WHERE company_name = '$name'
      AND company_timecreate='$now'
      $type_sql
      $marquet_sql
      $act_sql
      AND company_phone = '$phone'
      AND company_zipcode = '$zip'";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("id");

  if ($retour && is_array($category1)) {
    foreach($category1 as $categoryid) {
      $retour = of_category_query_insert_link("company", "category1", $id, $categoryid);
    }
  }
  
  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $cid       : company id
//   - $company[] : Entry's values
//     keys used  : num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_company_update($cid, $company) {
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
  $type = $company["type"]; 
  if ($type != "") $upd_type = "company_type_id='$type',";
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
  $category1 = $company["category1"];

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
    $upd_type
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

  display_debug_msg($query, $cdg_sql, "run_query_company_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $retour = of_category_query_delete_link("company", "category1", $cid);
    if (is_array($category1)) {
      foreach($category1 as $categoryid) {
        $retour = of_category_query_insert_link("company", "category1", $cid, $categoryid);
      }
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  run_query_global_delete_document_links($p_id, "company");

  display_debug_msg($query, $cdg_sql, "run_query_company_delete(cat1 links)");
  $query = "DELETE FROM CompanyCategory1Link
    WHERE companycategory1link_company_id='$p_id'";
  $obm_q->query($query);

  $query = "DELETE FROM Company WHERE company_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_company_delete()");
  $retour = $obm_q->query($query);

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
// Naf Code company query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companynafcode() {
  global $cdg_sql, $cgp_hide;

  $show_naf = (! $cgp_hide["company"]["nafcode"]);
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
function run_query_company_nafcode_insert($company) {
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
function run_query_company_nafcode_update($company) {
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
function run_query_company_nafcode_delete($id) {
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
function run_query_company_nafcode_links($p_id) {
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
// Get the label of an Naf Code from its id (code + label)
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_company_nafcode_label($id) {
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
function check_company_data_form($cid, $company) {
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
  $nb = get_global_linked_contact_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_contact";
  } else {
    $ok_msg .= "$l_link_contact_no";
  }

  // Links from deals
  $nb = get_global_linked_deal_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_deal";
  } else {
    $ok_msg .= "<br />$l_link_deal_no";
  }

  // Links from Contracts
  $nb = get_global_linked_contract_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_contract";
  } else {
    $ok_msg .= "<br />$l_link_contract_no";
  }

  // Links from Project
  $nb = get_global_linked_project_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_project";
  } else {
    $ok_msg .= "<br />$l_link_project_no";
  }

  // Links from Invoice
  $nb = get_global_linked_invoice_nb($p_id, "company", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_invoice";
  } else {
    $ok_msg .= "<br />$l_link_invoice_no";
  }

  return $delete_ok;
}


?>
