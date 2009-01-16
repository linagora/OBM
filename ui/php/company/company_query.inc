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
  $archive = ( $company['archive']);
  $name = sql_search_text_parse($company['name']);
  $number = sql_search_text_parse($company['number']);
  $phone = sql_search_text_parse($company['phone']);
  $town = sql_search_text_parse($company['town']);
  $ctry = $company['country'];
  $type = ( $company['type']);
  $act = ( $company['activity']);
  $naf = ( $company['naf']);
  $market = ( $company['market']);
  $dsrc = ( $company['datasource']);
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
    $where .= " $and company_type_id=$type";
    $and = 'and';
  }
  if (($act != $c_all) && ($act != '')) { 
    $where .= " $and company_activity_id=$act";
    $and = 'AND';
  }
  if (($naf != $c_all) && ($naf != '')) { 
    $where .= " $and company_nafcode_id=$naf";
    $and = 'AND';
  }
  if (($market != $c_all) && ($market != '')) { 
    $where .= " $and company_marketingmanager_id=$market";
    $and = 'AND';
  }
  if (($dsrc != $c_all) && ($dsrc != '')) { 
    $where .= " $and company_datasource_id=$dsrc";
    $and = 'AND';
  }
  // If a phone number has been specified, get it 
  $where .= " $and (phone_id IN (SELECT MIN(phone_id) FROM Phone WHERE phone_entity_id = companyentity_entity_id)";
  $and = 'AND';
  if ($phone != '') {
    $where .= "AND companyentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE companyentity_entity_id = phone_entity_id AND phone_number $like '$phone%'))";
  } else {
    $where .= " OR phone_id IS NULL)";
  }
  // If an email indication has been specified, get it 
  $where .= 'AND (email_id IN (SELECT MIN(email_id) FROM Email WHERE email_entity_id = companyentity_entity_id)';
  if ($email != '') {
    $where .= " AND companyentity_entity_id IN (SELECT email_entity_id FROM Email WHERE companyentity_entity_id = email_entity_id AND email_address $like '$email%'))";
  } else {
    $where .= " OR email_id IS NULL)";
  }  
  // Address criteria
  $addr = '';
  if ($town != '') {
    $addr .= " AND address_town $like '$town%'";
  }
  if (($ctry != $c_all) && ($ctry != '')) { 
    $addr .= " AND address_country='$ctry'";
  }
  if ($zip != '') {
    $addr .= " AND address_zipcode $like '$zip%'";
  }

  $where .= 'AND (address_id IN (SELECT MIN(address_id) FROM Address WHERE address_entity_id = companyentity_entity_id)';
  if($addr != '') {
    $where .= " AND companyentity_entity_id IN (SELECT address_entity_id FROM Address WHERE companyentity_entity_id = address_entity_id ".$addr."))";
  } else {
    $where .= " OR address_id IS NULL)";
  }  
  $where .= 'AND (website_id IN (SELECT MIN(website_id) FROM Website WHERE website_entity_id = companyentity_entity_id) OR website_id IS NULL)';
  
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
    $where .= " $and company_archive=0";
    $and = 'AND';
  } elseif($cgp_archive_only) {
    $where .= " $and company_archive=1";
    $and = 'AND';
  }

  // User defined data
  $userdata = of_userdata_query_search('Company', $company);
  if ($userdata['where'] != '') {
    $where .= " $and ". $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  if (($where != '') || ($where_name != '')) {
    $whereq = " WHERE $where_name $where $multidomain";
  } else {
    $whereq = " WHERE 1=1 $multidomain";
  }

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'company_name';

  // Order exceptions (order on calculated rows)
  // Contact list and new contact column haven't any meaning for sorting
  if ( (strcmp($sql_order_field,'company_contacts')==0)
      || (strcmp($sql_order_field,'company_new_contact') == 0) ) {
    $order = 'company_name';
  } else if (strcmp($sql_order_field,'company_address')==0) {
    $order = 'address_street';
    $order_next = ', address_zipcode, address_town';    
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $select = "SELECT distinct
      company_id as id,
      company_id,
      company_name,
      company_number,
      address_street,
      address_zipcode,
      address_town,
      address_expresspostal,
      phone_number,
      email_address,
      website_url,
      country_name,      
      company_vat,
      company_siret,
      company_archive,
      company_type_id,
      company_activity_id,
      company_nafcode_id,
      company_marketingmanager_id,
      company_contact_number,
      company_deal_number,
      company_deal_total,
      companytype_label as type_label,
      companyactivity_label as activity_label,
      companynafcode_code as nafcode_code,
      companynafcode_label as nafcode_label
    FROM Company
    INNER JOIN CompanyEntity ON companyentity_company_id = company_id
    LEFT JOIN Address ON address_entity_id = companyentity_entity_id
    LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='FR' 
    LEFT JOIN Phone ON phone_entity_id = companyentity_entity_id
    LEFT JOIN Email ON email_entity_id = companyentity_entity_id        
    LEFT JOIN Website ON website_entity_id = companyentity_entity_id        
      ";

  $left_join = "
         LEFT JOIN CompanyType ON company_type_id=companytype_id
         $join_userdata
         LEFT JOIN CompanyActivity ON company_activity_id=companyactivity_id
         LEFT JOIN CompanyNafCode ON company_nafcode_id=companynafcode_id";

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
    $whereq = " WHERE $where_name $where $multidomain";
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
  $p_id = sql_parse_id( $p_id, true);

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
         LEFT JOIN DataSource ON company_datasource_id=datasource_id
         LEFT JOIN UserObm as c ON company_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON company_userupdate=u.userobm_id
    WHERE company_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}

/**
 * run_query_company_coords
 * 
 * @param mixed $id
 * @access public
 * @return void
 */
function run_query_company_coords($id, $update=false) {
  global $cdg_sql;
  $obm_q = new DB_OBM;
  $coords = array();
  $query = "SELECT phone_label, phone_number FROM Phone INNER JOIN CompanyEntity ON phone_entity_id = companyentity_entity_id WHERE companyentity_company_id = $id ORDER BY phone_label";
  display_debug_msg($query, $cdg_sql, 'run_query_company_coords(Phone)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('phone_label'));
    array_pop($label);
    $label = implode('_',$label);
    $coords['phone'][] = array('label' => $label, 'number' => $obm_q->f('phone_number'));
  }
  $query = "SELECT email_label, email_address FROM Email INNER JOIN CompanyEntity ON email_entity_id = companyentity_entity_id WHERE companyentity_company_id = $id ORDER BY email_label";
  display_debug_msg($query, $cdg_sql, 'run_query_company_coords(Email)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('email_label'));
    array_pop($label);
    $label = implode('_',$label);
    $coords['email'][] = array('label' => $label, 'address' => $obm_q->f('email_address'));
  }
  
  $query = "SELECT website_label, website_url FROM Website INNER JOIN CompanyEntity ON website_entity_id = companyentity_entity_id WHERE companyentity_company_id = $id ORDER BY website_label";
  display_debug_msg($query, $cdg_sql, 'run_query_company_coords(Website)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('website_label'));
    array_pop($label);
    $label = implode('_',$label);
    $coords['website'][] = array('label' => $label, 'url' => $obm_q->f('website_url'));
  }
  $lang = get_lang();
  $query = "SELECT 
    address_label, address_street, address_zipcode, address_expresspostal, address_town, address_country, country_name
    FROM Address INNER JOIN CompanyEntity ON address_entity_id = companyentity_entity_id 
    LEFT JOIN Country ON country_iso3166 = address_country AND country_lang = '$lang' WHERE companyentity_company_id = $id ORDER BY address_label";
  display_debug_msg($query, $cdg_sql, 'run_query_company_coords(Address)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('address_label'));
    array_pop($label);
    $label = implode('_',$label);
    if($update) {
      $country =  $obm_q->f('address_country');
    } else {
      $country = $obm_q->f('country_name');
    }
    $coords['address'][] = array(
      'label' => $label, 'street' => $obm_q->f('address_street'), 'zipcode' => $obm_q->f('address_zipcode'),
      'expresspostal' => $obm_q->f('address_expresspostal'), 'town' => $obm_q->f('address_town'), 'country' => $country);
  }

  return $coords;
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

  $uid = sql_parse_id( $obm['uid']);
  $now = date('Y-m-d H:i:s');
  $domain_id = sql_parse_id( $obm['domain_id']);
	
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
  $dsrc = sql_parse_id( $company['datasource']); 
  $type = sql_parse_id( $company['type']); 
  $act = sql_parse_id( $company['activity']); 
  $naf = sql_parse_id( $company['naf']); 
  $market = sql_parse_id( $company['market']);
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
    company_comment)
  VALUES (
    $domain_id,
    null,
    '$now',
    null,
    $uid,
    $dsrc,
    '$number',
    '$vat',
    '$siret',
    '$archive',
    '$name',
    '$aka',
    '$sound',
    $type,
    $act,
    $naf,
    $market,
    '$comment')";

  display_debug_msg($query, $cdg_sql, 'run_query_company_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  display_debug_msg($query, $cdg_sql, 'run_query_company_insert(2)');
  $id = $obm_q->lastid();

  if ($id > 0) {
    $entity_id = of_entity_insert($GLOBALS['module'], $id);
    run_query_contact_coords_update($entity_id, $company);
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
  $dsrc = sql_parse_id( $company['datasource']);
  $type = sql_parse_id( $company['type']); 
  $upd_type = "company_type_id=$type,";
  $act = sql_parse_id( $company['activity']);
  $upd_act = "company_activity_id=$act,";
  $naf = sql_parse_id( $company['naf']);
  $upd_naf = "company_nafcode_id=$naf,";
  $market = sql_parse_id( $company['market']);
  $comment = $company['comment'];
  $add_comment = $company['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($company['datecomment']);
    $usercomment = $company['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $multidomain = sql_multidomain('company');
  $cid_sql = sql_parse_id($cid, true);  
  $query = "UPDATE Company SET
    company_timeupdate='". date('Y-m-d H:i:s') ."',
    company_userupdate=". $obm['uid'] .",
    company_datasource_id=$dsrc,
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
    company_marketingmanager_id=$market,
    company_comment='$comment'
  WHERE company_id $cid_sql
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_company_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $ret = of_userdata_query_update('company', $cid, $company);
    $entityid = of_entity_get('company', $cid);
    run_query_contact_coords_update($entityid, $company);
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

  of_entity_delete($GLOBALS['module'], $p_id);
  
  $p_id = sql_parse_id( $p_id, true);  
  $query = "DELETE FROM Company
    WHERE company_id $p_id 
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
///////////////////////////////////////////////////////////////////////////////
function run_query_check_company($p_id, $name) {
  global $cdg_sql;

  $where_id = '';
  if ($p_id != '') {
    $p_id = sql_parse_id( $p_id, true, true);
    $where_id = "AND company_id $p_id"; 
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('company');
	
  $query = "SELECT DISTINCT company_id, company_name 
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
  $usercreate = sql_parse_id( $obm['uid']);
  $title = (isset($company['naf_title']) ? $company['naf_title'] : '0');
  $code = $company['naf_code'];
  $label = $company['naf_label'];
  $domain_id = sql_parse_id( $obm['domain_id']);

  $query = "INSERT INTO CompanyNafCode (
      companynafcode_timecreate,
      companynafcode_usercreate,
      companynafcode_domain_id,
      companynafcode_title,
      companynafcode_code,
      companynafcode_label)
    VALUES (
      '$timecreate',
      $usercreate,
      $domain_id,
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
  $userupdate = sql_parse_id( $obm['uid']);
  $id = sql_parse_id( $company['naf'], true);
  $code = $company['naf_code'];
  $title = (isset($company['naf_title']) ? $company['naf_title'] : '0');
  $label = $company['naf_label'];
  $multidomain = sql_multidomain('companynafcode');
	
  $query = "UPDATE CompanyNafCode SET
      companynafcode_code='$code',
      companynafcode_label='$label',
      companynafcode_timeupdate='$timeupdate',
      companynafcode_userupdate=$userupdate,
      companynafcode_title=$title
    WHERE
      companynafcode_id $id $multidomain";

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
  $id = sql_parse_id( $id, true);	

  $query = "DELETE FROM CompanyNafCode WHERE companynafcode_id $id $multidomain";
	
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

  $p_id = sql_parse_id( $p_id, true);
  $query = "SELECT company_name, company_id, company_nafcode_id
    FROM Company
    WHERE company_nafcode_id $p_id $multidomain";

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
  $id = sql_parse_id( $id, true);
  $query = "SELECT companynafcode_code,
      companynafcode_label
    FROM CompanyNafCode
    WHERE companynafcode_id $id $multidomain";

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

  // if a company with same name exists, return false
  $co_q = run_query_check_company($cid, $name);
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
  $phones = $company['phones'];
  $emails = $company['emails'];
  $addresses = $company['addresses'];

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
  if(is_array($addresses)) {
    foreach($addresses as $address) {
      if (($address['zipcode'] != '') && (preg_match($php_regexp_zip, $address['zipcode']) == 0)) {
        $err['msg'] = $l_j_check_zip . " : $address[zipcode]";
        return false;
      }
    }
  }
  
  // Company phone
  if(is_array($phones)) {
    foreach($phones as $phone) {
      if (($phone['number'] != '') && (preg_match($php_regexp_phone, $phone['number']) == 0)) {
        $err['msg'] = $l_j_check_phone . " : $phone[number]";
        return false;
      }
    }
  }

  // Company email
  if(is_array($emails)) {
    foreach($emails as $email) {
      if (($email['address'] != '') && (preg_match($php_regexp_email, $email['address']) == 0)) {
        $err['msg'] = $l_j_check_email . " : $email[address]";
        return false;
      }  
    }
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
