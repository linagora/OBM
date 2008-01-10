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
///////////////////////////////////////////////////////////////////////////////
function run_query_company_search($company) {
  global $cgp_user, $c_all, $c_none, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;
  
  $sql_order_dir = $company['sql_order_dir'];
  $sql_order_field = $company['sql_order_field'];
  
  $fuzzy = $company['fuzzy'];
  $archive = $company['archive'];
  $name = sql_search_text_parse($company['name']);
  $number = sql_search_text_parse($company['number']);
  $phone = sql_search_text_parse($company['phone']);
  $town = sql_search_text_parse($company['town']);
  $ctry = $company['country'];
  $type = $company['type'];
  $act = $company['activity'];
  $naf = $company['naf'];
  $market = $company['market'];
  $dsrc = $company['datasource'];
  $zip = sql_search_text_parse($company['zip']);
  $dateafter = of_isodate_convert($company['date_after'],true);
  $datebefore = of_isodate_convert($company['date_before'],true);
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain('company');
	
  $and = '';
  if ($name != '') {
    $where_name = " (company_name $like '$name%' OR company_aka like '$name%')";
    $and = 'AND';
  }
  if ($number != '') {
    $where .= " $and company_number $like '$number%'";
    $and = 'AND';
  }
  if (($type != $c_all) && ($type != '')) { 
    $where .= " $and company_type_id='$type'";
    $and = 'and';
  }
  if (($act != $c_all) && ($act != '')) { 
    $where .= " $and company_activity_id='$act'";
    $and = 'AND';
  }
  if (($naf != $c_all) && ($naf != '')) { 
    $where .= " $and company_nafcode_id='$naf'";
    $and = 'AND';
  }
  if (($market != $c_all) && ($market != '')) { 
    $where .= " $and company_marketingmanager_id='$market'";
    $and = 'AND';
  }
  if (($dsrc != $c_all) && ($dsrc != '')) { 
    $where .= " $and company_datasource_id='$dsrc'";
    $and = 'AND';
  }
  if ($phone != '') {
    $where .= " $and company_phone $like '$phone%'";
    $and = 'AND';
  }
  if ($town != '') {
    $where .= " $and company_town $like '$town%'";
    $and = 'AND';
  }
  if (($ctry != $c_all) && ($ctry != '')) { 
    $where .= " $and company_country_iso3166='$ctry'";
    $and = 'AND';
  }
  if ($zip != '') {
    $where .= " $and company_zipcode $like '$zip%'";
    $and = 'AND';
  }
  if ($dateafter != '') {
    $where .= " $and (company_timeupdate > '$dateafter'
                     OR company_timecreate > '$dateafter')";
    $and = 'AND';
  }
  if ($datebefore != '') {
    $where .= " $and (company_timeupdate < '$datebefore'
                     AND company_timecreate < '$datebefore')";
    $and = 'AND';
  }

  // Get only not archived by default
  if ($archive != '1') {
    $where .= " $and company_archive='0'";
    $and = 'AND';
  } elseif($cgp_archive_only) {
    $where .= " $and company_archive='1'";
    $and = 'AND';
  }

  // User defined data
  $userdata = of_userdata_query_search('company', $company);
  if ($userdata['where'] != '') {
    $where .= " $and ". $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  if (($where != '') || ($where_name != '')) {
    $whereq = " WHERE $where_name $where $multidomain";
  }

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'company_name';

  // Order exceptions (order on calculated rows)
  // Contact list and new contact column haven't any meaning for sorting
  if ( (strcmp($sql_order_field,'company_contacts')==0)
      || (strcmp($sql_order_field,'company_new_contact') == 0) ) {
    $order = 'company_name';
  } else if (strcmp($sql_order_field,'company_address')==0) {
    $order = 'company_address1';
    $order_next = ', company_address2, company_address3, company_zipcode, company_town';
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $select = "SELECT distinct
      company_id as id,
      company_id,
      company_name,
      company_number,
      company_vat,
      company_siret,
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
         $join_userdata
         LEFT JOIN CompanyActivity ON company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode ON company_nafcode_id=companynafcode_id
         LEFT JOIN Country ON company_country_iso3166=country_iso3166
                   AND country_lang='$lang'";

  if ((! $fuzzy) || ($name == '')) {
    // If result limited, we get the global rows number
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct company_id) FROM Company $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    // If any results (from limited) or result not limited, we get the data
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, 'company_search(1)');
      $obm_q->query($query);
      if (! $ctu_sql_limit) {
	$count = $obm_q->num_rows();
      }
    }
  }

  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($name != '') ) {
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
      display_debug_msg($query, $cdg_sql, 'company_search(2)');
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
  
  $multidomain = sql_multidomain('company');
  $timeupdate = sql_date_format($db_type, 'company_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'company_timecreate', 'timecreate');
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
    WHERE company_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $company[] : Entry's values
//     keys used  : num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_company_insert($company) {
  global $obm, $cdg_sql;

  $uid = $obm['uid'];
  $now = date('Y-m-d H:i:s');
  $domain_id = $obm['domain_id'];
	
  $number = $company['number'];
  $vat = $company['vat'];
  $siret = $company['siret'];
  $archive = ($company['archive'] == '1' ? '1' : '0');
  $name = $company['name'];
  $aka = trim($company['aka']);
  // If aka is empty we auto fill it
  if ($aka == '') {
    $auto_aka = format_name($name, 0, true, true);
    if ($auto_aka != $name) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($name);
  $dsrc = ($company['datasource'] != '' ? $company['datasource'] : '0');
  $type = ($company['type'] != '' ? $company['type'] : '0');
  $act = ($company['activity'] != '' ? $company['activity'] : '0');
  $naf = ($company['naf'] != '' ? $company['naf'] : '0');
  $market = $company['market'];
  $ad1 = $company['ad1'];
  $ad2 = $company['ad2'];
  $ad3 = $company['ad3'];
  $zip = $company['zip'];
  $town = $company['town'];
  $cdx = $company['cdx'];
  $ctry = $company['country'];
  $phone = $company['phone'];
  $fax = $company['fax'];
  $web = trim($company['web']);
  $email = trim($company['email']);
  $comment = $company['comment'];
  $add_comment = $company['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($company['datecomment']);
    $today = date('Y-m-d');
    if ($today == $datecomment) {
      $datecomment = $now;
    }
    $usercomment = $company['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  
  $query = "INSERT INTO Company (
    company_domain_id,
    company_timeupdate,
    company_timecreate,
    company_userupdate,
    company_usercreate,
    company_datasource_id,
    company_number,
    company_vat,
    company_siret,
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
  VALUES (
    '$domain_id',
    null,
    '$now',
    null,
    '$uid',
    '$dsrc',
    '$number',
    '$vat',
    '$siret',
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

  display_debug_msg($query, $cdg_sql, 'run_query_company_insert(1)');
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
  
  display_debug_msg($query, $cdg_sql, 'run_query_company_insert(2)');
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f('id');

  if ($id > 0) {
    $ret = of_userdata_query_update('company', $id, $company);
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
  global $obm, $cdg_sql, $cgp_hide;

  $number = $company['number'];
  $vat = $company['vat'];
  $siret = $company['siret'];
  $archive = ($company['archive'] == '1' ? '1' : '0');
  $name = $company['name'];
  $aka = trim($company['aka']);
  // If aka is empty we auto fill it
  if ($aka == '') {
    $auto_aka = format_name($name, 0, true, true);
    if ($auto_aka != $name) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($name);
  $dsrc = $company['datasource'];
  $type = $company['type']; 
  if ($type != '') $upd_type = "company_type_id='$type',";
  $act = $company['activity'];
  if ($act != '') $upd_act = "company_activity_id='$act',";
  $naf = $company['naf'];
  if ($naf != '') $upd_naf = "company_nafcode_id='$naf',";
  $market = $company['market'];
  $ad1 = $company['ad1'];
  $ad2 = $company['ad2'];
  $ad3 = $company['ad3'];
  $zip = $company['zip'];
  $town = $company['town'];
  $cdx = $company['cdx'];
  $ctry = $company['country'];
  $phone = $company['phone'];
  $fax = $company['fax'];
  $web = trim($company['web']);
  $email = trim($company['email']);
  $comment = $company['comment'];
  $add_comment = $company['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($company['datecomment']);
    $usercomment = $company['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $multidomain = sql_multidomain('company');
	
  $query = "UPDATE Company SET
    company_timeupdate='". date('Y-m-d H:i:s') ."',
    company_userupdate='". $obm['uid'] ."',
    company_datasource_id='$dsrc',
    company_number='$number',
    company_vat='$vat',
    company_siret='$siret',
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
  WHERE company_id='$cid'
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $ret = of_userdata_query_update('company', $cid, $company);
  }

  return $retour;
}


/**
 * Deletion query execution
 *
 * @param mixed $p_id : company id
 * @return true is query is ok
 **/
function run_query_company_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('company');
	
  run_query_global_delete_document_links($p_id, 'company');

  $ret = of_userdata_query_delete('company', $p_id);

  $query = "DELETE FROM Company
    WHERE company_id='$p_id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_company_delete()');
  $retour = $obm_q->query($query);

  return $retour;
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

  $where_id = '';
  if ($p_id != '') {
    $where_id = "AND company_id!='$p_id'";
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('company');
	
  $query = "SELECT DISTINCT company_id, company_name, company_zipcode 
    FROM Company
    WHERE company_name $like '$name%'
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_check_company()');
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

  $show_naf = (! $cgp_hide['company']['nafcode']);
  if ($show_naf) {
    $multidomain = sql_multidomain('companynafcode');
  	
    $query = "SELECT * FROM CompanyNafCode WHERE 1=1 $multidomain ORDER BY companynafcode_code"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql, 'run_query_companynafcode()');
  } else {
    $obm_q = '';
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Naf Code insertion query construction and execution
// Parameters:
//   - $company : company hash info : keys used : naf_code, naf_label
///////////////////////////////////////////////////////////////////////////////
function run_query_company_nafcode_insert($company) {
  global $cdg_sql, $obm;

  $timecreate = date('Y-m-d H:i:s');
  $usercreate = $obm['uid'];
  $title = (isset($company['naf_title']) ? $company['naf_title'] : '0');
  $code = $company['naf_code'];
  $label = $company['naf_label'];
  $domain_id = $obm['domain_id'];

  $query = "INSERT INTO CompanyNafCode (
      companynafcode_timecreate,
      companynafcode_usercreate,
      companynafcode_domain_id,
      companynafcode_title,
      companynafcode_code,
      companynafcode_label)
    VALUES (
      '$timecreate',
      '$usercreate',
      '$domain_id',
      '$title',
      '$code',
      '$label')";

  display_debug_msg($query, $cdg_sql, 'run_query_company_nafcode_insert()');
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
  global $obm, $cdg_sql;

  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = $obm['uid'];
  $id = $company['naf'];
  $code = $company['naf_code'];
  $title = (isset($company['naf_title']) ? $company['naf_title'] : '0');
  $label = $company['naf_label'];
  $multidomain = sql_multidomain('companynafcode');
	
  $query = "UPDATE CompanyNafCode SET
      companynafcode_code='$code',
      companynafcode_label='$label',
      companynafcode_timeupdate='$timeupdate',
      companynafcode_userupdate='$userupdate',
      companynafcode_title=$title
    WHERE
      companynafcode_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_nafcode_update()');
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

  $multidomain = sql_multidomain('companynafcode');
	
  $query = "DELETE FROM CompanyNafCode WHERE companynafcode_id='$id' $multidomain";
	
  display_debug_msg($query, $cdg_sql, 'run_query_company_nafcode_delete()');
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

  $multidomain = sql_multidomain('company');

  $query = "SELECT company_name, company_id, company_nafcode_id
    FROM Company
    WHERE company_nafcode_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_nafcode_links()');
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

  $multidomain = sql_multidomain('companynafcode');

  $query = "SELECT companynafcode_code,
      companynafcode_label
    FROM CompanyNafCode
    WHERE companynafcode_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_company_nafcode_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('companynafcode_code') . ' - ' . $obm_q->f('companynafcode_label');
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

  $name = $company['name'];
  $zip = $company['zip'];

  // if a company with same name and zip exists, return false
  $co_q = run_query_check_company($cid, $name, $zip);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Company Form Data checking and formatting
// Parameters:
//   - $cid   : company id
//   - $company[] : values checked
//     keys used  : number, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_company_data_form($cid, $company) {
  global $l_fill_name, $l_j_check_phone, $l_j_check_zip, $l_j_check_fax;
  global $l_j_check_email, $l_j_check_web, $l_err_act_empty;
  global $err;
  global $php_regexp_zip,$php_regexp_phone,$php_regexp_fax,$php_regexp_email,$php_regexp_web;

  $number = $company['number'];
  $name = $company['name'];
  $act = $company['activity'];
  $zip = $company['zip'];
  $phone = $company['phone'];
  $fax = $company['fax'];
  $web = trim($company['web']);
  $email = trim($company['email']);

  // MANDATORY: Company name
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    return false;
  }

  // Company activity filled
  //  if ($company['activity'] < 1) {
  //    $err['msg'] = $l_err_act_empty;
  //    return false;
  //  }

  // Company Zip code
  if (($zip != '') && (preg_match($php_regexp_zip, $zip) == 0)) {
    $err['msg'] = $l_j_check_zip . " : $zip";
    return false;
  }

  // Company phone
  if (($phone != '') && (preg_match($php_regexp_phone, $phone) == 0)) {
    $err['msg'] = $l_j_check_phone . " : $phone";
    return false;
  }

  // Company fax
  if (($fax != '') && (preg_match($php_regexp_fax, $fax) == 0)) {
    $err['msg'] = $l_j_check_fax . " : $fax";
    return false;
  }

  // Company email
  if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
    $err['msg'] = $l_j_check_email . " : $email";
    return false;
  }

  // Company web
  if (($web != '') && (preg_match($php_regexp_web, $web) == 0)) {
    $err['msg'] = $l_j_check_web . " : $web";
    return false;
  }

  return true; 
}


/**
 * Check if the company can be deleted
 *
 * @param mixed $p_id : company id
 * @return true if the company can be deleted, else false
 */
function check_can_delete_company($p_id) {
  global $err, $ok_msg;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $l_link_contract, $l_link_contract_no, $l_link_project;
  global $l_link_invoice, $l_link_invoice_no, $l_link_project_no;

  $delete_ok = true;

  // Links from Contact
  $nb = get_global_linked_element_nb('Contact', $p_id, 'company', true);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_contact";
  } else {
    $ok_msg .= "$l_link_contact_no";
  }

  // Links from deals
  $nb = get_global_linked_element_nb('Deal', $p_id, 'company', true);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_deal";
  } else {
    $ok_msg .= "<br />$l_link_deal_no";
  }

  // Links from Contracts
  $nb = get_global_linked_element_nb('Contract', $p_id, 'company', true);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_contract";
  } else {
    $ok_msg .= "<br />$l_link_contract_no";
  }

  // Links from Project
  $nb = get_global_linked_element_nb('Project', $p_id, 'company', true);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_project";
  } else {
    $ok_msg .= "<br />$l_link_project_no";
  }

  // Links from Invoice
  $nb = get_global_linked_element_nb('Invoice', $p_id, 'company', true);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_invoice";
  } else {
    $ok_msg .= "<br />$l_link_invoice_no";
  }

  return $delete_ok;
}


?>
