<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contact_query.inc                                            //
//     - Desc : contact query File                                           //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Contact search query execution
// Parametes:
//   - $contact[]    : contact search criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_search($contact) {
  global $cgp_show, $cdg_sql, $c_all, $ctu_sql_limit;
  global $cgp_archive_only;

  $sql_order_dir = $contact["sql_order_dir"];
  $sql_order_field = $contact["sql_order_field"];

  $company_id = $contact["company_id"];
  $fuzzy = $contact["fuzzy"];
  $lname = sql_search_text_parse($contact["lname"]);
  $fname = sql_search_text_parse($contact["fname"]);
  $phone = sql_search_text_parse($contact["phone"]);
  $email = sql_search_text_parse($contact["email"]);
  $company = sql_search_text_parse($contact["company"]);
  $market = $contact["market"];
  $func = $contact["function"];
  $title = $contact["title"];
  $zip = sql_search_text_parse($contact["zip"]);
  $town = sql_search_text_parse($contact["town"]);
  $ctry = $contact["country"];
  $dsrc = $contact["datasource"];
  $date_field = ($contact['date_field'] ? $contact['date_field'] : 'timeupdate');
  $dateafter = of_isodate_convert($contact["date_after"],true);
  $datebefore = of_isodate_convert($contact["date_before"],true);
  $mailing_ok_only = $contact["mailing_ok_only"];
  $newsletter = $contact["newsletter"];
  $archive = $contact["archive"];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "contact_date", "contact_date");
  $multidomain = sql_multidomain("contact");

  // only the one which are allowed (ie. publics )
  $where .= sql_obm_entity_privacy("contact");

  // If company module used (external from Contact)
  if ($cgp_show["module"]["company"]) {

   // To allow sorting on contact and company addresses infos
   $if_condition = "(
      (contact_address1 != '') OR
      (contact_address2 != '') OR 
      (contact_address3 != '') OR 
      (contact_zipcode != '') OR 
      (contact_town != '') OR 
      (contact_expresspostal != ''))";

    $if_ad1 = sql_if($db_type, $if_condition, "contact_address1 ", "company_address1") . " as contact_address1,";
    $if_ad2 = sql_if($db_type, $if_condition, "contact_address2 ", "company_address2") . " as contact_address2,";
    $if_ad3 = sql_if($db_type, $if_condition, "contact_address3 ", "company_address3") . " as contact_address3,";
    $if_zip = sql_if($db_type, $if_condition, "contact_zipcode ", "company_zipcode") . " as contact_zipcode,";
    $where_zip = "(contact_zipcode $like '$zip%' OR contact_zipcode='' AND company_zipcode $like '$zip%')";
    $if_town = sql_if($db_type, $if_condition, "contact_town ", "company_town") . " as contact_town,";
    $where_town = "(contact_town $like '$town%' OR contact_town='' AND company_town $like '$town%')";
    $if_express = sql_if($db_type, $if_condition, "contact_expresspostal ", "company_expresspostal") . " as contact_expresspostal,";
    $if_express = sql_if($db_type, $if_condition, "ctry1.country_name ", "ctry2.country_name") . " as country_name,";
    $join_comp = "LEFT JOIN Company ON contact_company_id=company_id";
    $join_ctry2 = "LEFT JOIN Country as ctry2 ON
                  Company.company_country_iso3166 = ctry2.country_iso3166
                  AND ctry2.country_lang='$lang'";

    $company_fields = "company_name,
      company_phone,
      company_fax,
      company_address1,
      company_address2,
      company_address3,
      company_zipcode,
      company_town,
      company_expresspostal,
      ctry2.country_name as company_country,";

  } else {
    $if_ad1 = "contact_address1,";
    $if_ad2 = "contact_address2,";
    $if_ad3 = "contact_address3,";
    $if_zip = "contact_zipcode,";
    $where_zip = "(contact_zipcode $like '$zip%')";
    $if_town = "contact_town,";
    $where_town = "(contact_town $like '$town%')";
    $if_express = "contact_expresspostal,";
    $if_country = "ctry1.country_name as country_name,";
    $company_fields = "contact_company as company_name,";
  }

  // If a lastname has been specified, get it 
  if ($lname != "") {
    $where_name .= "(contact_lastname $like '$lname%' or contact_aka like '$lname%')";
  }
  // If a firstname has been specified, get it 
  if ($fname != "") {
    $where .= " AND contact_firstname $like '$fname%'";
  }
  // If a phone number has been specified, get it 
  if ($phone != "") {
    $where .= " AND (contact_phone $like '$phone%' OR
                 contact_homephone $like '$phone%' OR
                 contact_mobilephone $like '$phone%')";
  }
  // If an email indication has been specified, get it 
  if ($email != "") {
    $where .= " AND (contact_email $like '$email%' OR
                     contact_email2 $like '$email%')";
  }
  // If a company name indication has been specified (without id) get it 
  if (($company != "") && ($company_id == "")) {
    if ($cgp_show["module"]["company"]) {
      $where .= sql_global_company_name_advanced_search($company, $like);
      $join_comp_count = "LEFT JOIN Company ON contact_company_id=company_id";
    } else {
      $where .= " AND contact_company $like '$company%'";
    }
  }
  // If a company was sent as parameter, get it
  if ($company_id != "") {
    $where .= " AND contact_company_id='$company_id'";
  }
  // If a post code has been specified, get it
  if ($zip != "") {
    $where .= " AND $where_zip";
  }
  // If a town has been specified, get it 
  if ($town != "") {
    $where .= " AND $where_town";
  }
  // If a country has been set
  if (($ctry != $c_all) && ($ctry != "")) {
    if ($cgp_show["module"]["company"]) {
      $where .= " AND (contact_country_iso3166='$ctry' OR
       contact_country_iso3166=0 AND company_country_iso3166='$ctry')";
      $join_comp_count = "LEFT JOIN Company ON contact_company_id=company_id";
    } else {
      $where .= " AND contact_country_iso3166='$ctry'";
    }
  }

  // If a person in charge has been set
  if (($market != $c_all) && ($market != "")) { 
    $where .= " AND contact_marketingmanager_id='$market'";
  }
  // If a function has been set
  if (($func != $c_all) && ($func != "")) { 
    $where .= " AND contact_function_id='$func'";
  }
  // If a title has been specified, get it 
  if ($title != "") {
    $where .= " AND contact_title $like '$title%'";
  }
  // If a datasource has been specified, get it 
  if (($dsrc != $c_all) && ($dsrc != "")) { 
    $where .= " AND contact_datasource_id='$dsrc'";
  }
  if ($dateafter != "") {
    $where .= " AND contact_$date_field > '$dateafter'";
  }
  if ($datebefore != "") {
    $where .= " AND contact_$date_field < '$datebefore'";
  }
  // Get all by default 
  if ($mailing_ok_only == "1") {
    $where .= " AND contact_mailing_ok=1";
  }
  if ($newsletter == "1") {
    $where .= " AND contact_newsletter=1";
  }
  // Get only not archived by default
  if ($archive != "1") {
    $where .= " AND contact_archive=0";
  } elseif ($cgp_archive_only) {
    $where .= " AND contact_archive=1";
  }

  // User defined data
  $userdata = of_userdata_query_search("contact", $contact);
  if ($userdata["where"] != "") {
    $where .= " AND ". $userdata["where"];
    $join_userdata = $userdata["join"];
  }
  
  $whereq = "WHERE $where $multidomain";
  if ($where_name != "") {
    $whereq .= " AND $where_name";
  }

  // order
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "contact_lastname";
  if (($order == "contact_lastname") || ($order == "contact_firstname")) {
    $order = sql_casei_sort($db_type, $order);
  }

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field,"contact_address") == 0) {
    $order = "contact_address1";
    $order_next = ", contact_address2, contact_address3, contact_zipcode, contact_town";
  } else if (strcmp($sql_order_field,"country_name") == 0) {
    $order = "country_name";
    if ($cgp_show["module"]["company"]) {
      $order_next = ", company_country";
    }
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $select = "SELECT
      contact_id as id,
      contact_id,
      $if_ad1
      $if_ad2
      $if_ad3
      $if_zip
      $if_town
      $if_express
      $if_country
      contact_usercreate,
      contact_company_id,
      contact_kind_id,
      kind_minilabel,
      kind_header,
      kind_lang,
      contact_marketingmanager_id,
      contact_lastname,
      contact_firstname,
      contact_function_id,
      contact_title,
      contact_service,
      contact_country_iso3166,
      contact_phone,
      contact_homephone,
      contact_mobilephone,
      contact_fax,
      contact_email,
      contact_mailing_ok,
      contact_newsletter,
      contact_archive,
      contact_privacy,
      $date,
      $company_fields
      contactfunction_label as function_label
    FROM Contact";

  // XXX Really strange Postgres (8.1.5) behaviour here !
  // if $join_userdata after join_comp, query is really slow (30s) with only
  // criteria : contactcategory1 selected
  $left_join = "
        $join_userdata
        $join_comp
        LEFT JOIN Kind ON contact_kind_id = kind_id
        LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
        LEFT JOIN Country as ctry1 ON
                  contact_country_iso3166 = ctry1.country_iso3166
                  AND ctry1.country_lang='$lang'
        $join_ctry2";


  if ((! $fuzzy) || ($lname == "")) {
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct contact_id) FROM Contact $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    // If any results (from limited) or result not limited, we get the data
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, "contact_search(1)");
      $obm_q->query($query);
      if (! $ctu_sql_limit) {
	$count = $obm_q->num_rows();
      }
    }
  }

  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($lname != "") ) {
    $auto_aka = format_name($lname, 0, true, true);
    $sound = phonetic_key($lname);
    $where_name = " AND ($where_name OR contact_aka $like '%$auto_aka%'
                 OR contact_sound = '$sound')";
    $whereq = " WHERE $where $where_name";
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct contact_id) FROM Contact $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, "contact_search(2)");
      $obm_q->query($query);
    }
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact detail query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_detail($p_id) {
  global $cgp_show, $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "contact_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "contact_timecreate", "timecreate");
  $lang = get_lang();
  $date = sql_date_format($db_type, "contact_date", "contact_date");
  $multidomain = sql_multidomain("contact");

  if ($cgp_show["module"]["company"]) {
    $join_comp = "LEFT JOIN Company ON contact_company_id=company_id";
    $join_ctry2 = "LEFT JOIN Country as C2
                   ON company_country_iso3166=C2.country_iso3166
                    AND C2.country_lang='$lang'";
    $company_fields = "company_id,
        company_name,
        company_phone,
        company_fax,
        company_address1,
        company_address2,
        company_address3,
        company_zipcode,
        company_town,
        company_expresspostal,
        company_country_iso3166,
        C2.country_name as company_country_name";
  } else {
    $company_fields = "contact_company as company_name";
  }

  $query = "SELECT Contact.*,
        contact_privacy as privacy,
        $timeupdate,
        $timecreate,
        $company_fields,
        kind_lang,
        kind_minilabel,
        kind_header,
        m.userobm_lastname as market_lname,
        m.userobm_firstname as market_fname,
        contactfunction_label,
        C1.country_name,
        datasource_name,
        $date,
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      FROM
        Contact
        $join_comp
        LEFT JOIN Kind ON kind_id=contact_kind_id
        LEFT JOIN UserObm as m ON contact_marketingmanager_id=m.userobm_id
        LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
        LEFT JOIN Country as C1 ON contact_country_iso3166=C1.country_iso3166
                  AND C1.country_lang='$lang'
        $join_ctry2
        LEFT JOIN DataSource ON contact_datasource_id=datasource_id
        LEFT JOIN UserObm as c ON contact_usercreate=c.userobm_id
        LEFT JOIN UserObm as u ON contact_userupdate=u.userobm_id
      WHERE
        contact_id='$p_id'
        $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Company query execution
// Parameters:
//   - $c_id : company id
// Returns : DB result object with the company's name and id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_company($c_id) {
  global $cdg_sql;

  $lang = get_lang();
  $multidomain = sql_multidomain("company");
	
  $query = "SELECT company_id,
      company_name,
      company_phone,
      company_fax,
      company_address1,
      company_address2,
      company_address3,
      company_zipcode,
      company_town,
      company_expresspostal,
      company_country_iso3166,
      country_name as company_country_name
    FROM Company
    LEFT JOIN Country ON company_country_iso3166 = country_iso3166
                         AND country_lang='$lang'
    WHERE company_id='$c_id'
      $multidomain";
	
  display_debug_msg($query, $cdg_sql, "run_query_contact_company()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind select query execution
// Returns : DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind() {
  global $cdg_sql;
  
  $multidomain = sql_multidomain("kind");
	
  $query = "SELECT kind_id,
      kind_minilabel,
      kind_header,
      kind_lang,
      kind_default
    FROM Kind
    WHERE 1=1
      $multidomain
    ORDER BY kind_lang, kind_minilabel, kind_header";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Insertion query execution
// Parameters:
//   - $contact[]: Entry's values
//     keys used : company_id, kind, lname, fname ad1, ad2, ad3, zip, town, cdx
//               : ctry, func, phone, hphone, mphone, fax, email, priv
//               : com, com2, com3 
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_insert($contact) {
  global $cgp_show, $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $uid = $obm["uid"];
	
  $comp_id = $contact["company_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }
  // In case company module not used, to avoid postgres error
  if ($comp_id == "") {
    $comp_id = "0";
  }
  
  $domain_id = $obm["domain_id"];
  $dsrc = ($contact["datasource"] ? $contact["datasource"] : 0);
  $company = $contact["company"];
  $kind = $contact["kind"];
  $market = ($contact["market"] ? $contact["market"] : 0);
  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $aka = trim($contact["aka"]);
  // If aka is empty we auto fill it
  if ($aka == "") {
    $auto_aka = format_name($lname, 0, true, true);
    if ($auto_aka != $lname) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($lname);
  $service = $contact["service"];
  $ad1 = $contact["ad1"];
  $ad2 = $contact["ad2"];
  $ad3 = $contact["ad3"];
  $zip = $contact["zip"];
  $town = $contact["town"];
  $cdx = $contact["cdx"];
  $ctry = $contact["country"];
  $func = ($contact["function"] ? $contact["function"] : 0);
  $title = $contact["title"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = $contact["email"];
  $email2 = $contact["email2"];
  $mailok = ($contact["mailok"] == "1" ? "1" : "0");
  $newsletter = ($contact["newsletter"] == "1" ? "1" : "0");
  $date = ($contact["date"] ? "'".of_isodate_convert($contact["date"])."'" : "null");
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($contact["datecomment"]);
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $add_comment2 = $contact["add_comment"];
  if ($add_comment2 != "") {
    $datecomment2 = of_isodate_convert($contact["datecomment2"]);
    $usercomment2 = $contact["usercomment2"];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $add_comment3 = $contact["add_comment3"];
  if ($add_comment3 != "") {
    $datecomment3 = of_isodate_convert($contact["datecomment3"]);
    $usercomment3 = $contact["usercomment3"];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }

  $archive = ($contact["archive"] == "1" ? "1" : "0");
  $priv = (isset($contact["privacy"]) ? $contact["privacy"] : '0');

  $query = "INSERT INTO Contact (contact_timeupdate,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
    contact_domain_id,
    contact_datasource_id,
    contact_company_id,
    contact_company,
    contact_kind_id,
    contact_marketingmanager_id,
    contact_lastname,
    contact_firstname,
    contact_aka,
    contact_sound,
    contact_service,
    contact_address1,
    contact_address2,
    contact_address3,
    contact_zipcode,
    contact_town,
    contact_expresspostal,
    contact_country_iso3166,
    contact_function_id,
    contact_title,
    contact_phone,
    contact_homephone,
    contact_mobilephone,
    contact_fax,
    contact_email,
    contact_email2,
    contact_mailing_ok,
    contact_newsletter,
    contact_archive,
    contact_privacy,
    contact_date,
    contact_comment,
    contact_comment2,
    contact_comment3
  ) VALUES ('$now',
    '$now',
    '$uid',
    '$uid',
    '$domain_id',
    '$dsrc',
    '$comp_id',
    '$company',
    '$kind',
    '$market',
    '$lname',
    '$fname',
    '$aka',
    '$sound',
    '$service',
    '$ad1',
    '$ad2',
    '$ad3',
    '$zip',
    '$town',
    '$cdx',
    '$ctry',
    '$func',
    '$title',
    '$phone',
    '$hphone',
    '$mphone',
    '$fax',
    '$email',
    '$email2',
    '$mailok',
    '$newsletter',
    '$archive',
    '$priv',
    $date,
    '$comment',
    '$comment2',
    '$comment3'
  )";

  display_debug_msg($query, $cdg_sql, "run_query_contact_insert(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if (($cgp_show["module"]["company"]) && ($retour)) {
    run_query_global_company_contact_number_update($comp_id);
  }

  $query = "SELECT contact_id
    FROM Contact
    WHERE contact_lastname = '$lname'
      AND contact_timecreate = '$now'
      AND contact_kind_id ='$kind'
      AND contact_company_id ='$comp_id'
      AND contact_email ='$email'
      AND contact_phone = '$phone'
      AND contact_zipcode = '$zip'
      AND contact_domain_id='$domain_id'";

  display_debug_msg($query, $cdg_sql, "run_query_contact_insert(2)");
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("contact_id");

  if ($id > 0) {
    $ret = of_userdata_query_update("contact", $id, $contact);
  }
  
  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Contact update query execution
// Parameters:
//   - $contact[] : Entry's values
//     keys used  : id, company_id, kind, lname, fname ad1, ad2, ad2, zip, town
//                : cdx, ctry, func, phone, hphone, mphone, fax, email
//                : com, com2, com3
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_update($contact) {
  global $obm, $cgp_show, $cdg_sql;

  $multidomain = sql_multidomain("contact");
  $id = $contact["contact_id"];
  $comp_old_id = $contact["company_id"];
  $comp_new_id = $contact["company_new_id"];

  $comp_id = $contact["company_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }
  // In case company module not used, to avoid postgres error
  if ($comp_id == "") {
    $comp_id = "0";
  }
  $dsrc = ($contact["datasource"] ? $contact["datasource"] : 0);
  $company = $contact["company"];
  $kind = $contact["kind"];
  $market = ($contact["market"] ? $contact["market"] : 0);
  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $aka = trim($contact["aka"]);
  // If aka is empty we auto fill it
  if ($aka == "") {
    $auto_aka = format_name($lname, 0, true, true);
    if ($auto_aka != $lname) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($lname);
  $service = $contact["service"];
  $ad1 = $contact["ad1"];
  $ad2 = $contact["ad2"];
  $ad3 = $contact["ad3"];
  $zip = $contact["zip"];
  $town = $contact["town"];
  $cdx = $contact["cdx"];
  $ctry = $contact["country"];
  $func = ($contact["function"] ? $contact["function"] : 0);
  $title = $contact["title"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = $contact["email"];
  $email2 = $contact["email2"];
  $mailok = ($contact["mailok"] == "1" ? "1" : "0");
  $newsletter = ($contact["newsletter"] == "1" ? "1" : "0");
  $date = ($contact["date"] ? "'".of_isodate_convert($contact["date"])."'" : "null");
  $comment = $contact["comment"];
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($contact["datecomment"]);
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comment2 = $contact["comment2"];
  $add_comment2 = $contact["add_comment2"];
  if ($add_comment2 != "") {
    $datecomment2 = of_isodate_convert($contact["datecomment2"]);
    $usercomment2 = $contact["usercomment2"];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $comment3 = $contact["comment3"];
  $add_comment3 = $contact["add_comment3"];
  if ($add_comment3 != "") {
    $datecomment3 = of_isodate_convert($contact["datecomment3"]);
    $usercomment3 = $contact["usercomment3"];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }
  $archive = ($contact["archive"] == "1" ? "1" : "0");
  $priv = (isset($contact["privacy"]) ? $contact["privacy"] : '0');
  $dsrc = ($contact["datasource"] ? $contact["datasource"] : 0);

  $query = "UPDATE Contact SET
    contact_timeupdate='".date("Y-m-d H:i:s")."',
    contact_userupdate='".$obm["uid"]."',
    contact_datasource_id='$dsrc',
    contact_company_id='$comp_id',
    contact_company='$company',
    contact_kind_id='$kind',
    contact_marketingmanager_id='$market',
    contact_lastname='$lname',
    contact_firstname='$fname',
    contact_aka='$aka',
    contact_sound='$sound',
    contact_service='$service',
    contact_address1='$ad1',
    contact_address2='$ad2',
    contact_address3='$ad3',
    contact_zipcode='$zip',
    contact_town='$town',
    contact_expresspostal='$cdx',
    contact_country_iso3166='$ctry',
    contact_function_id='$func',
    contact_title='$title',
    contact_phone='$phone',
    contact_homephone='$hphone',
    contact_mobilephone='$mphone',
    contact_fax='$fax',
    contact_email='$email',
    contact_email2='$email2',
    contact_mailing_ok='$mailok',
    contact_newsletter='$newsletter',
    contact_archive='$archive',
    contact_privacy='$priv',
    contact_date=$date,
    contact_comment='$comment',
    contact_comment2='$comment2',
    contact_comment3='$comment3'
  WHERE contact_id='$id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $ret = of_userdata_query_update("contact", $id, $contact);
  }

  if ($cgp_show["module"]["company"]) {
    // If company has changed, update the companies contact number
    if (($retour) && ($comp_new_id > 0) && ($comp_new_id != $comp_old_id)) {
      run_query_global_company_contact_number_update($comp_new_id);
      run_query_global_company_contact_number_update($comp_old_id);
    }
  }

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Get the number of static lists where the contact is registered
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_contact_list_nb($p_id) {
  global $cdg_sql;

  $query = "SELECT count(DISTINCT contactlist_list_id) as nb
    FROM ContactList
    WHERE contactlist_contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "get_linked_contact_list_nb()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("nb");

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of publications where the contact has subscribed
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_contact_publication_nb($p_id) {
  global $cdg_sql;

  $query = "SELECT DISTINCT subscription_publication_id as nb
    FROM Subscription 
    WHERE subscription_contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "get_linked_contact_publication_nb()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("nb");

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Deletion query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_delete($p_id) {
  global $cdg_sql, $c_use_connectors;

  $c = get_contact_info($p_id);
  $comp_id = $c["company_id"];
  $multidomain = sql_multidomain("contact");

  run_query_global_delete_document_links($p_id, "contact");    
  run_query_contact_delete_subscription($p_id);    
  $ret = of_userdata_query_delete("contact", $p_id);

  $query = "DELETE FROM Contact WHERE contact_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_contact_delete(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    $query = "INSERT INTO
      DeletedContact (deletedcontact_contact_id, deletedcontact_timestamp)
      VALUES ('$p_id', '$now')";
    display_debug_msg($query, $cdg_sql, "run_query_contact_delete(2)");
    $retour = $obm_q->query($query);
  }

  // After contact deletion to get correct number
  run_query_global_company_contact_number_update($comp_id);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Subscription deletion query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_delete_subscription($p_id) {
  global $cdg_sql;

  $query = "DELETE FROM Subscription WHERE subscription_contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_contact_delete_subscription()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the contacts which matches the lastname and firstname or which
// matches the lastname and company except the one given (update mode)
// Parameters:
//   - $cid     : contact id
//   - $comp_id : company id
//   - $lname   : lastname
//   - $fname   : firstname
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_check($cid, $comp_id, $lname, $fname) {
  global $cdg_sql, $cgp_show;

  $multidomain = sql_multidomain("contact");
	
  $where_id = "";
  if ($cid != "") {
    $where_id = "contact_id!='$cid' AND";
  }

  if ($cgp_show["module"]["company"]) {
    $join_comp = "LEFT JOIN Company ON contact_company_id=company_id";
    $where_comp = "OR (contact_company_id='$comp_id' AND contact_lastname='$lname')";
    $select_comp = ", company_name";
  }

  $query = "SELECT DISTINCT contact_id,
      contact_company_id,
      contact_lastname,
      contact_firstname
      $select_comp
    FROM Contact
      $join_comp
    WHERE $where_id
      ((contact_lastname='$lname' AND contact_firstname='$fname')
      $where_comp)
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_check()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact environment checking (same contacts exists ?)
// Parameters:
//   - $cid       : contact id
//   - $contact[] : contact's values
//     keys used  : lname, fname
// Returns:
//   - Contact Database object with list of similar contacts
///////////////////////////////////////////////////////////////////////////////
function check_contact_context($cid, $contact) {
  global $cgp_show;

  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $comp_new_id = $contact["company_new_id"];
  $comp_id = $contact["company_id"];
  $comp = ($comp_new_id != "") ? $comp_new_id : $comp_id;

  // return contacts with same lastname and firstname or
  // within the same company with same lname
  $co_q = run_query_contact_check($cid, $comp, $lname, $fname);

  return $co_q;
}


/*
 * Contact specific rights checking (eg : public contact needs write_admin)
 * Parameters:
 * @param $params : parameters values
 * @param $c[]    : current contact values
 * @return true if OK, else false
 */
function check_contact_update_rights($params, $c='') {
  global $actions, $perm, $err, $cright_write_admin;
  global $l_err_public_rights, $l_error_permission, $l_error_delegation;

  $id = $params['contact_id'];
  if ($id > 0) {
    // Get current contact info if not given
    if (! is_array($c)) {
      $c = get_contact_info($id);
    }

    /*    // Check delegation update right
    if (! of_delegation_check_update_rights($params, $g['delegation'])) {
      $err['msg'] .= $l_error_delegation;
      return false;
    }*/

    // Check update right is set
    if (! $perm->check_right('contact', $actions['contact']['update']['Right'])) {
      $err['msg'] .= $l_error_permission;
      return false;
    }

    // Allow public contact handling only if write_admin right
    if (($c['privacy'] != 1)
	&& (! $perm->check_right('contact', $cright_write_admin))) {
      $err['msg'] .= $l_err_public_rights;
      return false;
    }
  
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Form Data checking and formatting
// Parameters:
//   - $cid       : contact id  (empty on insertion)
//   - $contact[] : values checked
//     keys used  : lname, fname, phone, hphone, mphone, fax, email
//                  company_id, comp_new_id
///////////////////////////////////////////////////////////////////////////////
function check_contact_data_form($cid, $contact) {
  global $perm, $cgp_show, $cright_write_admin;
  global $php_regexp_phone,$php_regexp_fax,$php_regexp_email;
  global $l_fill_lastname, $l_fill_company, $l_j_check_phone;
  global $l_j_check_hphone,$l_j_check_mphone, $l_j_check_fax, $l_j_check_email;
  global $err, $l_exist_error, $l_err_public_rights;

  $lname = $contact['lname'];
  $fname = $contact['fname'];
  $phone = $contact['phone'];
  $hphone = $contact['hphone'];
  $mphone = $contact['mphone'];
  $fax = $contact['fax'];
  $email = stripslashes($contact['email']);
  $email2 = stripslashes($contact['email2']);
  $priv = $contact['privacy'];
  $c_id = $contact['company_id'];
  $c_new_id = $contact['company_new_id'];
  
  // MANDATORY: Contact name
  $lname = strtoupper($lname);
  if (trim($lname) == '') {
    $err['msg'] = $l_fill_lastname;
    return false;
  }

  if ($cgp_show['module']['company']) {
    // MANDATORY: a company must be set
    if (($c_id < 1) && ($c_new_id < 1)) {
      $err['msg'] = $l_fill_company;
      return false;
    }
  }

  // Contact Firstname
  $fname = ucfirst($fname);

  // Contact phone
  if (($phone != '') && (preg_match($php_regexp_phone, $phone) == 0)) {
    $err['msg'] = $l_j_check_phone . " : $phone";
    return false;
  }

  // Contact mobile or personal phone
  if (($hphone != '') && (preg_match($php_regexp_phone, $hphone) == 0)) {
    $err['msg'] = $l_j_check_hphone . " : $hphone";
    return false;
  }
  if (($mphone != '') && (preg_match($php_regexp_phone, $mphone) == 0)) {
    $err['msg'] = $l_j_check_mphone . " : $mphone";
    return false;
  }
  // Contact fax
  if (($fax != '') && (preg_match($php_regexp_fax, $fax) == 0)) {
    $err['msg'] = $l_j_check_fax . " : $fax";
    return false;
  }

  // Contact email
  if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
    $err['msg'] = $l_j_check_email . " : $email";
    return false;
  }

  // Contact email
  if (($email2 != '') && (preg_match($php_regexp_email, $email2) == 0)) {
    $err['msg'] = $l_j_check_email . " : $email2";
    return false;
  }

  // Allow public contact handling only if write_admin right
  if (($priv != 1) && (! $perm->check_right('contact', $cright_write_admin))) {
    $err['msg'] = $l_err_public_rights;
    $err['field'] = 'privacy';
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the contact can be deleted
// Parameters:
//   - $p_id : contact id
// Returns:
//   true if the contact can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_contact($p_id) {
  global $err, $perm, $cright_write_admin, $ok_msg;
  global $l_link_deal, $l_link_deal_no, $l_link_contract, $l_link_contract_no;
  global $l_link_list, $l_link_list_no, $l_link_publication, $l_link_publication_no;
  global $l_err_public_rights;

  $delete_ok = true;

  // Allow public contact handling only if write_admin right
  $lc = get_contact_info($p_id);
  if (($lc['privacy'] == '0') &&
      (! $perm->check_right('contact', $cright_write_admin))) {
    $err['msg'] .= $l_err_public_rights;
    $delete_ok = false;
    return false;
  }

  // Links from deals
  $nb = get_global_linked_deal_nb($p_id, 'contact1', true, 'contact2');
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_deal";
  } else {
    $ok_msg .= "$l_link_deal_no";
  }

  // Links from lists
  $nb = get_linked_contact_list_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_list";
  } else {
    $ok_msg .= "<br />$l_link_list_no";
  }

  // Links from Contract
  $nb = get_global_linked_contract_nb($p_id, 'contact1', true, 'contact2');
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_contract";
  } else {
    $ok_msg .= "<br />$l_link_contract_no";
  }

  // Links from Publications (Subscriptions)
  $nb = get_linked_contact_publication_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_publication";
  } else {
    $ok_msg .= "<br />$l_link_publication_no";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact infos
// Parameters:
//   - $con_id : contact id
// Returns: $c hash with contact infos
///////////////////////////////////////////////////////////////////////////////
function get_contact_info($con_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');

  $query = "SELECT
      contact_company_id,
      contact_privacy
    FROM Contact
    WHERE contact_id='$con_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_contact_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $c['company_id'] = $obm_q->f('contact_company_id');
  $c['privacy'] = $obm_q->f('contact_privacy');

  return $c;
}


///////////////////////////////////////////////////////////////////////////////
// Kind insertion query construction and execution
// Parameters:
//   - $contact : contact hash info
//                keys used : kind_label, kind_lang, kind_header
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_insert($contact) {
  global $cdg_sql, $obm;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $label = $contact["kind_label"];
  $lang = $contact["kind_lang"];
  $header = $contact["kind_header"];
  //$default = ($contact["kind_default"] == 1 ? 1 : 0);
  $default = (isset($contact["kind_default"]) ? $contact["kind_default"] : '0');
  $domain_id = $obm["domain_id"];
	
  // If kind is new default for this lang, we cancel other defaults
  if ($default == 1) {
    run_query_contact_kind_clear_default($lang);
  }

  $query = "INSERT INTO Kind (
    kind_timecreate,
    kind_usercreate,
		kind_domain_id,
    kind_minilabel,
    kind_header,
    kind_lang,
    kind_default)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$domain_id',
    '$label',
    '$header',
    '$lang',
    '$default')";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Clear the default kind for the given lang
// Parameters:
//   - $lang : lang to clear default kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_clear_default($lang) {
  global $cdg_sql;

  $multidomain = sql_multidomain("kind");

  $query = "UPDATE Kind
    SET kind_default=0
    WHERE kind_lang = '$lang' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind_clear_default()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind update query execution
// Parameters:
//   - $contact : contact hash info
//                keys used : kind_label, kind_lang, kind_header
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_update($contact) {
  global $obm, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $id = $contact["kind"];
  $label = $contact["kind_label"];
  $lang = $contact["kind_lang"];
  $header = $contact["kind_header"];
  $default = (isset($contact["kind_default"]) ? $contact["kind_default"] : '0');
  $multidomain = sql_multidomain("kind");
	
  // If kind is new default for this lang, we cancel other defaults
  if ($default == 1) {
    run_query_contact_kind_clear_default($lang);
  }

  $query = "UPDATE Kind SET
      kind_timeupdate='$timeupdate',
      kind_userupdate='$userupdate',
      kind_minilabel='$label',
      kind_header='$header',
      kind_lang='$lang',
      kind_default='$default'
    WHERE
      kind_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind deletion query execution
// Parameters:
//   - $id : Kind id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_delete($id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("kind");

  $query = "DELETE FROM Kind WHERE kind_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind - Contact links query execution
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_links($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contact");

  $query = "SELECT contact_lastname,
      contact_firstname,
      contact_id,
      contact_kind_id
    FROM Contact 
    WHERE contact_kind_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contact_kind_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a kind from its id
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function get_contact_kind_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("kind");	

  $query = "SELECT kind_lang, kind_minilabel, kind_header
    FROM Kind
    WHERE kind_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_contact_kind_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("kind_lang") . " " . $obm_q->f("kind_minilabel") . " " .
            $obm_q->f("kind_header");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Count the number of subscription
// Parameters:
//   - $id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_subscription_nb($id) {
  global $cdg_sql;

  if ($id != '') {
    $query = "SELECT count(*) as nb
      FROM Subscription
      WHERE subscription_contact_id = '$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    $retour = $obm_q->f("nb");
  } else { 
    $retour = 0;
  }

  return $retour;
}

?>
