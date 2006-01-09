<script language="php">
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
//     keys used       company_id, name, phone, company
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_search($contact, $p_new_order, $p_order_dir) {
  global $cgp_show, $cdg_sql, $c_all, $ctu_sql_limit;

  $company_id = $contact["company_id"];
  $fuzzy = $contact["fuzzy"];
  $lname = sql_search_text_parse($contact["lname"]);
  $fname = sql_search_text_parse($contact["fname"]);
  $phone = sql_search_text_parse($contact["phone"]);
  $email = sql_search_text_parse($contact["email"]);
  $company = sql_search_text_parse($contact["company"]);
  $market = $contact["marketing_manager"];
  $func = $contact["function"];
  $title = $contact["title"];
  $zip = sql_search_text_parse($contact["zip"]);
  $town = sql_search_text_parse($contact["town"]);
  $ctry = $contact["country"];
  $category1 = $contact["category1"];
  $category2 = $contact["category2"];
  $category3 = $contact["category3"];
  $category4 = $contact["category4"];
  $category5 = $contact["category5"];
  $archive = $contact["archive"];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  // only the one which are allowed (ie. publics )
  $where .= sql_obm_entity_privacy("contact");

  // If company module used (external from Contact)
  if ($cgp_show["module"]["company"]) {
    $if_ctry = sql_if($db_type, "(ctry1.country_name != '')", "ctry1.country_name", "ctry2.country_name");
    $if_country = ",
      $if_ctry as country_name";
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
      $where .= sql_company_name_advanced_search($company, $like);
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
    $where .= " AND contact_zipcode $like '$zip%'";
  }
  // If a town has been specified, get it 
  if ($town != "") {
    $where .= " AND contact_town $like '$town%'";
  }
  // If a country has been set
  if (($ctry != $c_all) && ($ctry != "")) {
    if ($cgp_show["module"]["company"]) {
      $where .= " AND (contact_country_iso3166='$ctry' OR
       contact_country_iso3166='' AND company_country_iso3166='$ctry')";
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
  // If a Category 1 has been set
  if (($category1 != $c_all) && ($category1 != "")) {
    $join_category1 = "LEFT JOIN ContactCategory1Link ON contact_id=contactcategory1link_contact_id";
    $where .= " AND contactcategory1link_category_id='$category1'";
  }
  // If a Category 2 has been set
  if (($category2 != $c_all) && ($category2 != "")) {
    $join_category2 = "LEFT JOIN ContactCategory2Link ON contact_id=contactcategory2link_contact_id";
    $where .= " AND contactcategory2link_category_id='$category2'";
  }
  // If a Category 3 has been set
  if (($category3 != $c_all) && ($category3 != "")) {
    $join_category3 = "LEFT JOIN ContactCategory3Link ON contact_id=contactcategory3link_contact_id";
    $where .= " AND contactcategory3link_category_id='$category3'";
  }
  // If a Category 4 has been set
  if (($category4 != $c_all) && ($category4 != "")) {
    $join_category4 = "LEFT JOIN ContactCategory4Link ON contact_id=contactcategory4link_contact_id";
    $where .= " AND contactcategory4link_category_id='$category4'";
  }
  // If a Category 5 has been set
  if (($category5 != $c_all) && ($category5 != "")) {
    $join_category5 = "LEFT JOIN ContactCategory5 ON contact_category5_id=contactcategory5_id";
    $where .= " AND contactcategory5_id='$category5'";
  }
  // Date
  $date = sql_date_format($db_type, "contact_date", "contact_date");

  // Get only not archived by default
  if ($archive != "1") {
    $where .= " AND contact_archive=0";
  }
  
  $whereq = "WHERE $where";
  if ($where_name != "") {
    $whereq .= " AND $where_name";
  }

  // order
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "contact_lastname";
  if (($order == "contact_lastname") || ($order == "contact_firstname")) {
    $order = sql_casei_sort($db_type, $order);
  }

  // Order exceptions (order on calculated rows)
  if (strcmp($p_new_order,"contact_address") == 0) {
    $order = "contact_address1";
    $order_next = ", contact_address2, contact_address3, contact_zipcode, contact_town";
  } else if (strcmp($p_new_order,"country_name") == 0) {
    $order = "country_name";
    if ($cgp_show["module"]["company"]) {
      $order_next = ", company_country";
    }
  }
  $orderq = " ORDER BY $order $p_order_dir $order_next";

  $select = "SELECT
      contact_id as id,
      contact_id,
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
      contact_address1,
      contact_address2,
      contact_address3,
      contact_zipcode,
      contact_town,
      contact_expresspostal,
      contact_country_iso3166,
      ctry1.country_name as country_name,
      contact_phone,
      contact_homephone,
      contact_mobilephone,
      contact_fax,
      contact_email,
      contact_archive,
      contact_privacy,
      $date,
      $company_fields
      contactfunction_label
      $if_country
    FROM Contact";

  $left_join = "$join_comp
        LEFT JOIN Kind ON contact_kind_id = kind_id
        LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
        LEFT JOIN Country as ctry1 ON
                  contact_country_iso3166 = ctry1.country_iso3166
                  AND ctry1.country_lang='$lang'
        $join_ctry2
        $join_category1
        $join_category2
        $join_category3
        $join_category4
        $join_category5";

  if ((! $fuzzy) || ($lname == "")) {
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM Contact $left_join $whereq");
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
        contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
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
    WHERE company_id='$c_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind select query execution
// Returns : DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_kind() {
  global $cdg_sql;

  $query = "SELECT kind_id,
      kind_minilabel,
      kind_header,
      kind_lang,
      kind_default
    FROM Kind
    ORDER BY kind_lang, kind_minilabel, kind_header";

  display_debug_msg($query, $cdg_sql);
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
  global $auth, $cgp_show, $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $uid = $auth->auth["uid"];

  $comp_id = $contact["comp_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }
  // In case company module not used, to avoid postgres error
  if ($comp_id == "") {
    $comp_id = "0";
  }

  $company = $contact["company"];
  $dsrc = $contact["datasource"];
  $kind = $contact["kind"];
  $market = ($contact["marketing_manager"] ? $contact["marketing_manager"] : 0);
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
  $category1 = $contact["category1"];
  $category2 = $contact["category2"];  
  $category3 = $contact["category3"];  
  $category4 = $contact["category4"];  
  $category5 = ($contact["category5"] > 0 ? $contact["category5"] : '0');  
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
  $mailok = $contact["mailok"];
  $date = $contact["date"];
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contact["datecomment"];
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $add_comment2 = $contact["add_comment"];
  if ($add_comment2 != "") {
    $datecomment2 = $contact["datecomment2"];
    $usercomment2 = $contact["usercomment2"];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $add_comment3 = $contact["add_comment3"];
  if ($add_comment3 != "") {
    $datecomment3 = $contact["datecomment3"];
    $usercomment3 = $contact["usercomment3"];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }

  $arch = (isset($contact["archive"]) ? $contact["archive"] : '0');
  $priv = (isset($contact["priv"]) ? $contact["priv"] : '0');

  $query = "INSERT INTO Contact (contact_timeupdate,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
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
    contact_archive,
    contact_privacy,
    contact_date,
    contact_comment,
    contact_comment2,
    contact_comment3,
    contact_category5_id
  ) VALUES (null,
    '$now',
    null,
    '$uid',
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
    '$arch',
    '$priv',
    '$date',
    '$comment',
    '$comment2',
    '$comment3',
    '$category5'
  )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if (($cgp_show["module"]["company"]) && ($retour)) {
    run_query_company_contact_number_update($comp_id);
  }

  $query = "SELECT contact_id
    FROM Contact
    WHERE contact_lastname = '$lname'
      AND contact_kind_id ='$kind'
      AND contact_company_id ='$comp_id'
      AND contact_email ='$email'
      AND contact_phone = '$phone'
      AND contact_zipcode = '$zip'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("contact_id");

  if ($retour && is_array($category1)) {
    foreach($category1 as $categoryid) {
      $retour = of_category_query_insert_link("contact", "category1", $id, $categoryid);
    }
  }
  if ($retour && is_array($category2)) {
    foreach($category2 as $categoryid) {
      $retour = of_category_query_insert_link("contact", "category2", $id, $categoryid);
    }
  }
  if ($retour && is_array($category3)) {
    foreach($category3 as $categoryid) {
      $retour = of_category_query_insert_link("contact", "category3", $id, $categoryid);
    }
  }
  if ($retour && is_array($category4)) {
    foreach($category4 as $categoryid) {
      $retour = of_category_query_insert_link("contact", "category4", $id, $categoryid);
    }
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
  global $auth, $cgp_show, $cdg_sql;

  $id = $contact["id"];
  $comp_old_id = $contact["company_id"];
  $comp_new_id = $contact["comp_new_id"];

  $comp_id = $contact["comp_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }
  // In case company module not used, to avoid postgres error
  if ($comp_id == "") {
    $comp_id = "0";
  }
  $company = $contact["company"];
  $dsrc = $contact["datasource"];
  $kind = $contact["kind"];
  $market = ($contact["marketing_manager"] ? $contact["marketing_manager"] : 0);
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
  $mailok = $contact["mailok"];
  $date = $contact["date"];
  $comment = $contact["com"];
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contact["datecomment"];
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comment2 = $contact["com2"];
  $add_comment2 = $contact["add_comment2"];
  if ($add_comment2 != "") {
    $datecomment2 = $contact["datecomment2"];
    $usercomment2 = $contact["usercomment2"];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $comment3 = $contact["com3"];
  $add_comment3 = $contact["add_comment3"];
  if ($add_comment3 != "") {
    $datecomment3 = $contact["datecomment3"];
    $usercomment3 = $contact["usercomment3"];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }

  $arch = (isset($contact["archive"]) ? $contact["archive"] : '0');
  $priv = (isset($contact["priv"]) ? $contact["priv"] : '0');
  $category1 = $contact["category1"];
  $category2 = $contact["category2"];
  $category3 = $contact["category3"];
  $category4 = $contact["category4"];
  $category5 = ($contact["category5"] > 0 ? $contact["category5"] : '0');  
  
  $query = "UPDATE Contact SET
    contact_timeupdate='".date("Y-m-d H:i:s")."',
    contact_userupdate='".$auth->auth["uid"]."',
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
    contact_archive='$arch',
    contact_privacy='$priv',
    contact_date='$date',
    contact_comment='$comment',
    contact_comment2='$comment2',
    contact_comment3='$comment3',
    contact_category5_id='$category5'
  WHERE contact_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $retour = of_category_query_delete_link("contact", "category1", $id);
    if (is_array($category1)) {
      foreach($category1 as $categoryid) {
        $retour = of_category_query_insert_link("contact", "category1", $id, $categoryid);
      }
    }
  }

  if ($retour) {
    $retour = of_category_query_delete_link("contact", "category2", $id);
    if (is_array($category2)) {
      foreach($category2 as $categoryid) {
        $retour = of_category_query_insert_link("contact", "category2", $id, $categoryid);
      }
    }
  }

  if ($retour) {
    $retour = of_category_query_delete_link("contact", "category3", $id);
    if (is_array($category3)) {
      foreach($category3 as $categoryid) {
        $retour = of_category_query_insert_link("contact", "category3", $id, $categoryid);
      }
    }
  }

  if ($retour) {
    $retour = of_category_query_delete_link("contact", "category4", $id);
    if (is_array($category4)) {
      foreach($category4 as $categoryid) {
        $retour = of_category_query_insert_link("contact", "category4", $id, $categoryid);
      }
    }
  }

  if ($cgp_show["module"]["company"]) {
    // If company has changed, update the companies contact number
    if (($retour) && ($comp_new_id > 0) && ($comp_new_id != $comp_old_id)) {
      run_query_company_contact_number_update($comp_new_id);
      run_query_company_contact_number_update($comp_old_id);
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

  display_debug_msg($query, $cdg_sql);
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

  display_debug_msg($query, $cdg_sql);
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
  run_query_delete_document_links($p_id, "contact");    
  run_query_delete_contact_subscription($p_id);    

  $query = "DELETE FROM Contact WHERE contact_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    $query = "INSERT INTO
      DeletedContact (deletedcontact_contact_id, deletedcontact_timestamp)
      VALUES ('$p_id', '$now')";
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }

  // After contact deletion to get correct number
  run_query_company_contact_number_update($comp_id);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Subscription deletion query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_contact_subscription($p_id) {
  global $cdg_sql;

  $query = "delete from Subscription where subscription_contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_check_contact($cid, $comp_id, $lname, $fname) {
  global $cdg_sql;

  $where_id = "";
  if ($cid != "") {
    $where_id = "contact_id!='$cid' AND";
  }
  $query = "SELECT DISTINCT contact_id,
      contact_company_id,
      contact_lastname,
      contact_firstname,
      company_name 
    FROM Contact
      LEFT JOIN Company ON contact_company_id=company_id
    WHERE $where_id
      ((contact_lastname='$lname' AND contact_firstname='$fname')
        OR (contact_company_id='$comp_id' AND contact_lastname='$lname'))";

  display_debug_msg($query, $cdg_sql);
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
  $comp_new_id = $contact["comp_new_id"];
  $comp_id = $contact["company_id"];
  $comp = ($comp_new_id != "") ? $comp_new_id : $comp_id;

  // return contacts with same lastname and firstname or
  // within the same company with same lname
  if ($cgp_show["module"]["company"]) {
    $co_q = run_query_check_contact($cid, $comp, $lname, $fname);
  }

  return $co_q;
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
  global $cgp_show, $php_regexp_phone,$php_regexp_fax,$php_regexp_email;
  global $l_fill_lastname, $l_fill_company, $l_j_check_phone;
  global $l_j_check_hphone,$l_j_check_mphone, $l_j_check_fax, $l_j_check_email;
  global $err_msg, $l_exist_error;

  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = stripslashes($contact["email"]);
  $email2 = stripslashes($contact["email2"]);
  $c_id = $contact["company_id"];
  $c_new_id = $contact["comp_new_id"];
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];
  $cat3 = $contact["category3"];
  $cat4 = $contact["category4"];
  
  // MANDATORY: Contact name
  $lname = strtoupper($lname);
  if (trim($lname) == "") {
    $err_msg = $l_fill_lastname;
    return false;
  }

  if ($cgp_show["module"]["company"]) {
    // MANDATORY: a company must be set
    if (($c_id < 1) && ($c_new_id < 1)) {
      $err_msg = $l_fill_company;
      return false;
    }
  }

  // Contact Firstname
  $fname = ucfirst($fname);

  // Contact phone
  if (($phone != "") && (preg_match($php_regexp_phone, $phone) == 0)) {
    $err_msg = $l_j_check_phone . " : $phone";
    return false;
  }

  // Contact mobile or personal phone
  if (($hphone != "") && (preg_match($php_regexp_phone, $hphone) == 0)) {
    $err_msg = $l_j_check_hphone . " : $hphone";
    return false;
  }
  if (($mphone != "") && (preg_match($php_regexp_phone, $mphone) == 0)) {
    $err_msg = $l_j_check_mphone . " : $mphone";
    return false;
  }
  // Contact fax
  if (($fax != "") && (preg_match($php_regexp_fax, $fax) == 0)) {
    $err_msg = $l_j_check_fax . " : $fax";
    return false;
  }

  // Contact email
  if (($email != "") && (preg_match($php_regexp_email, $email) == 0)) {
    $err_msg = $l_j_check_email . " : $email";
    return false;
  }

  // Contact email
  if (($email2 != "") && (preg_match($php_regexp_email, $email2) == 0)) {
    $err_msg = $l_j_check_email . " : $email2";
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
  global $err_msg, $ok_msg;
  global $l_link_deal, $l_link_deal_no, $l_link_contract, $l_link_contract_no;
  global $l_link_list, $l_link_list_no, $l_link_publication, $l_link_publication_no;

  $delete_ok = true;

  // Links from deals
  $nb = get_linked_deal_nb($p_id, "contact1", true, "contact2");
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_deal";
  } else {
    $ok_msg .= "$l_link_deal_no";
  }

  // Links from lists
  $nb = get_linked_contact_list_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_list";
  } else {
    $ok_msg .= "<br />$l_link_list_no";
  }

  // Links from Contract
  $nb = get_linked_contract_nb($p_id, "contact1", true, "contact2");
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_contract";
  } else {
    $ok_msg .= "<br />$l_link_contract_no";
  }

  // Links from Publications (Subscriptions)
  $nb = get_linked_contact_publication_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "<br />$l_link_publication";
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

  $query = "SELECT
      contact_company_id
    FROM Contact
    WHERE contact_id='$con_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $c["company_id"] = $obm_q->f("contact_company_id");

  return $c;
}


///////////////////////////////////////////////////////////////////////////////
// Kind insertion query construction and execution
// Parameters:
//   - $contact : contact hash info
//                keys used : kind_label, kind_lang, kind_header
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_insert($contact) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $contact["kind_label"];
  $lang = $contact["kind_lang"];
  $header = $contact["kind_header"];
  $default = ($contact["kind_default"] == 1 ? 1 : 0);

  // If kind is new default for this lang, we cancel other defaults
  if ($default == 1) {
    run_query_kind_clear_default($lang);
  }

  $query = "INSERT INTO Kind (
    kind_timecreate,
    kind_usercreate,
    kind_minilabel,
    kind_header,
    kind_lang,
    kind_default)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$label',
    '$header',
    '$lang',
    '$default')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Clear the default kind for the given lang
// Parameters:
//   - $lang : lang to clear default kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_clear_default($lang) {
  global $cdg_sql;

  $query = "UPDATE Kind
    SET kind_default=0
    WHERE kind_lang = '$lang'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_kind_update($contact) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $contact["kind"];
  $label = $contact["kind_label"];
  $lang = $contact["kind_lang"];
  $header = $contact["kind_header"];
  $default = ($contact["kind_default"] == 1 ? 1 : 0);

  // If kind is new default for this lang, we cancel other defaults
  if ($default == 1) {
    run_query_kind_clear_default($lang);
  }

  $query = "UPDATE Kind SET
      kind_timeupdate='$timeupdate',
      kind_userupdate='$userupdate',
      kind_minilabel='$label',
      kind_header='$header',
      kind_lang='$lang',
      kind_default='$default'
    WHERE
      kind_id='$id'";

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

  $query = "DELETE FROM Kind WHERE kind_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Kind - Contact links query execution
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function run_query_kind_links($id) {
  global $cdg_sql;

  $query = "SELECT contact_lastname,
      contact_firstname,
      contact_id,
      contact_kind_id
    FROM Contact
    WHERE contact_kind_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a kind from its id
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function get_kind_label($id) {
  global $cdg_sql;

  $query = "SELECT kind_lang, kind_minilabel, kind_header
    FROM Kind
    WHERE kind_id='$id'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_subscription_nb($id) {
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


</script>
