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
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];
  $archive = $contact["archive"];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $if_country = sql_if($db_type, "(ctry1.country_name != '')", "ctry1.country_name", "ctry2.country_name");

  // only the one which are allowed (ie. publics )
  $where .= sql_obm_entity_privacy("contact");

  // If company module used (external from Contact)
  if ($cgp_show["module"]["company"]) {
    $join_comp = "LEFT JOIN Company on contact_company_id=company_id";
    $join_ctry2 = "LEFT JOIN Country as ctry2 ON
                  Company.company_country_iso3166 = ctry2.country_iso3166
                  and ctry2.country_lang='$lang'";
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
    $where .= " and contact_lastname $like '$lname%'";
  }
  // If a firstname has been specified, get it 
  if ($fname != "") {
    $where .= " and contact_firstname $like '$fname%'";
  }
  // If a phone number has been specified, get it 
  if ($phone != "") {
    $where .= " and (contact_phone $like '$phone%' or
                 contact_homephone $like '$phone%' or
                 contact_mobilephone $like '$phone%')";
  }
  // If an email indication has been specified, get it 
  if ($email != "") {
    $where .= " and (contact_email $like '$email%' or
                     contact_email2 $like '$email%')";
  }
  // If a company name indication has been specified (without id) get it 
  if (($company != "") && ($company_id == "")) {
    if ($cgp_show["module"]["company"]) {
      $where .= " and company_name $like '$company%'";
      $join_comp_count = "left join Company on contact_company_id=company_id";
    } else {
      $where .= " and contact_company $like '$company%'";
    }
  }
  // If a company was sent as parameter, get it
  if ($company_id != "") {
    $where .= " and contact_company_id='$company_id'";
  }
  // If a post code has been specified, get it 
  if ($zip != "") {
    $where .= " and contact_zipcode $like '$zip%'";
  }
  // If a town has been specified, get it 
  if ($town != "") {
    $where .= " and contact_town $like '$town%'";
  }
  // If a country has been set
  if (($ctry != $c_all) && ($ctry != "")) {
    if ($cgp_show["module"]["company"]) {
      $where .= " and (contact_country_iso3166='$ctry' or
       contact_country_iso3166='' and company_country_iso3166='$ctry')";
      $join_comp_count = "left join Company on contact_company_id=company_id";
    } else {
      $where .= " and contact_country_iso3166='$ctry'";
    }
  }

  // If a person in charge has been set
  if (($market != $c_all) && ($market != "")) { 
    $where .= " and contact_marketingmanager_id='$market'";
  }
  // If a function has been set
  if (($func != $c_all) && ($func != "")) { 
    $where .= " and contact_function_id='$func'";
  }
  // If a title has been specified, get it 
  if ($title != "") {
    $where .= " and contact_title $like '$title%'";
  }
  // If a Category 1 has been set
  if (($cat1 != $c_all) && ($cat1 != "")) {
    $join_cat1 = "left join ContactCategory1Link on contact_id=contactcategory1link_contact_id";
    $where .= " and contactcategory1link_category_id='$cat1'";
  }
  // If a Category 2 has been set
  if (($cat2 != $c_all) && ($cat2 != "")) {
    $join_cat2 = "left join ContactCategory2Link on contact_id=contactcategory2link_contact_id";
    $where .= " and contactcategory2link_category_id='$cat2'";
  }
  // Get only not archived by default
  if ($archive != "1") {
    $where .= " and contact_archive=0";
  }
  
  $whereq = "WHERE $where";

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

  $query = "SELECT
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
      $company_fields
      function_label as contact_function,
      $if_country as country_name
    FROM Contact
        $join_comp
        left join Kind on contact_kind_id = kind_id
        left join Function on contact_function_id=function_id
        left join Country as ctry1 ON
                  contact_country_iso3166 = ctry1.country_iso3166
                  and ctry1.country_lang='$lang'
        $join_ctry2
        $join_cat1
        $join_cat2
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Contact $join_comp_count $join_cat1 $join_cat2 $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "contact_search()");
    $obm_q->query($query);
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

  if ($cgp_show["module"]["company"]) {
    $join_comp = "LEFT JOIN Company on contact_company_id=company_id";
    $join_ctry2 = "LEFT JOIN Country as C2
                   on company_country_iso3166=C2.country_iso3166
                    and C2.country_lang='$lang'";
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
    $company_fields = "contact_company";
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
        function_label,
        C1.country_name,
        datasource_name,
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      FROM
        Contact
        $join_comp
        LEFT JOIN Kind on kind_id=contact_kind_id
        LEFT JOIN UserObm as m on contact_marketingmanager_id=m.userobm_id
        LEFT JOIN Function on contact_function_id=function_id
        LEFT JOIN Country as C1 on contact_country_iso3166=C1.country_iso3166
                  and C1.country_lang='$lang'
        $join_ctry2
        LEFT JOIN DataSource on contact_datasource_id=datasource_id
        LEFT JOIN UserObm as c on contact_usercreate=c.userobm_id
        LEFT JOIN UserObm as u on contact_userupdate=u.userobm_id
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
    LEFT JOIN Country on company_country_iso3166 = country_iso3166
                         and country_lang='$lang'
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
// Category 1 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_contactcategory1() {
  global $cdg_sql, $cgp_hide;

  if (! $cgp_hide["contact"]["category1"]) {
    $query = "SELECT * FROM ContactCategory1 ORDER BY contactcategory1_code"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Category 2 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_contactcategory2() {
  global $cdg_sql, $cgp_hide;

  if (! $cgp_hide["contact"]["category2"]) {
    $query = "SELECT * FROM ContactCategory2 ORDER BY contactcategory2_code"; 
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Insertion query execution
// Parameters:
//   - $contact[]: Entry's values
//     keys used : company_id, kind, lname, fname ad1, ad2, ad3, zip, town, cdx
//               : ctry, func, phone, hphone, mphone, fax, email, com, priv
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
  $service = $contact["service"];
  $ad1 = $contact["ad1"];
  $ad2 = $contact["ad2"];
  $ad3 = $contact["ad3"];
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];  
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
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contact["datecomment"];
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
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
    contact_comment)
  VALUES (null,
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
    '$comment')";

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

  if ($retour && is_array($cat1)) {
    foreach($cat1 as $categoryid) {
      $query = "INSERT INTO ContactCategory1Link (
	  contactcategory1link_contact_id,
	  contactcategory1link_category_id)
	VALUES (
          '$id',
          '$categoryid')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
    }
  }
  if ($retour && is_array($cat2)) {
    foreach($cat2 as $categoryid) {
      $query = "INSERT INTO ContactCategory2Link (
	  contactcategory2link_contact_id,
	  contactcategory2link_category_id)
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
// Contact update query execution
// Parameters:
//   - $contact[] : Entry's values
//     keys used  : id, company_id, kind, lname, fname ad1, ad2, ad2, zip, town
//                : cdx, ctry, func, phone, hphone, mphone, fax, email, com
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
  $comment = $contact["com"];
  $add_comment = $contact["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contact["datecomment"];
    $usercomment = $contact["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $arch = (isset($contact["archive"]) ? $contact["archive"] : '0');
  $priv = (isset($contact["priv"]) ? $contact["priv"] : '0');
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];
  
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
    contact_comment='$comment'
  WHERE contact_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $query = "DELETE
      FROM ContactCategory1Link
      WHERE contactcategory1link_contact_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    if (is_array($cat1)) {
      foreach($cat1 as $categoryid) {
	$query = "INSERT INTO ContactCategory1Link (
	  contactcategory1link_contact_id,
	  contactcategory1link_category_id)
	VALUES (
          '$id',
          '$categoryid')";
	display_debug_msg($query, $cdg_sql);
	$obm_q->query($query);
      }
    }
  }

  if ($retour) {
    $query = "delete
    from ContactCategory2Link
    where contactcategory2link_contact_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);    
    if (is_array($cat2)) {
      foreach($cat2 as $categoryid) {
	$query = "INSERT INTO ContactCategory2Link (
	  contactcategory2link_contact_id,
	  contactcategory2link_category_id)
	VALUES (
          '$id',
          '$categoryid')";
	display_debug_msg($query, $cdg_sql);
	$obm_q->query($query);
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
// Return all the categories attached to the contact
// Parameters:
//   - $c_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_contactcategory1($c_id) {
  global $cdg_sql;

  $cats = array();

  $query = "SELECT contactcategory1_id
    FROM ContactCategory1Link, ContactCategory1
    WHERE contactcategory1link_category_id = contactcategory1_id
      AND contactcategory1link_contact_id = '$c_id'
    ORDER BY contactcategory1_id";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $id = $obm_q->f("contactcategory1_id");
    $cats[] = $id;
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the contact
// Parameters:
//   - $c_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_contactcategory1_label($c_id) {
    global $cdg_sql;

  $query = "SELECT contactcategory1_label, contactcategory1_code
    FROM ContactCategory1Link, ContactCategory1
    WHERE contactcategory1link_category_id = contactcategory1_id
      AND contactcategory1link_contact_id = '$c_id'
    ORDER BY contactcategory1_id";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  if ($c_id != "") {
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the contact
// Parameters:
//   - $c_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_contactcategory2_label($c_id) {
  global $cdg_sql;

  $query = "SELECT contactcategory2_label, contactcategory2_code
    FROM ContactCategory2Link, ContactCategory2
    WHERE contactcategory2link_category_id = contactcategory2_id
      AND contactcategory2link_contact_id = '$c_id'
    ORDER BY contactcategory2_id";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  if ($c_id != "") {
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the contact
// Parameters:
//   - $c_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_contactcategory2($c_id) {
  global $cdg_sql;

  $cats = array();

  $query = "select contactcategory2_id
    from ContactCategory2Link, ContactCategory2
    where contactcategory2link_category_id = contactcategory2_id
      and contactcategory2link_contact_id = '$c_id'
    order by contactcategory2_id";     

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while($obm_q->next_record()) {
    $id = $obm_q->f("contactcategory2_id");
    $cats[] = $id;
  }

  return $cats;
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
      LEFT JOIN Company on contact_company_id=company_id
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
// Function insertion query construction and execution                       //
// Parameters:
//   - $contact : contact hash info : keys used : func_label
///////////////////////////////////////////////////////////////////////////////
function run_query_function_insert($contact) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $function = $contact["func_label"];

  $query = "insert into Function (
    function_timecreate,
    function_usercreate,
    function_label)
  values (
    '$timecreate',
    '$usercreate',
    '$function')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Function update query execution                                           //
// Parameters:
//   - $contact : contact hash info : keys used : function, func_label
///////////////////////////////////////////////////////////////////////////////
function run_query_function_update($contact) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $contact["function"];
  $function = $contact["func_label"];

  $query = "update Function set
      function_label='$function',
      function_timeupdate='$timeupdate',
      function_userupdate='$userupdate'
    where
      function_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Function deletion query execution                                         //
// Parameters:
//   - $id : Function id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_function_delete($id) {
  global $cdg_sql;

  $query = "delete from Function where function_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Fuction - Contact links query execution                                   //
// Parameters:
//   - $id : function id
///////////////////////////////////////////////////////////////////////////////
function run_query_function_links($id) {
  global $cdg_sql;

  $query = "select contact_lastname,
      contact_firstname,
      contact_id,
      contact_function_id
    from Contact where contact_function_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a function from its id
// Parameters:
//   - $id : function id
///////////////////////////////////////////////////////////////////////////////
function get_function_label($id) {
  global $cdg_sql;

  $query = "select function_label
    from Function
    where function_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("function_label");
  return $retour;
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

  $query = "insert into Kind (
    kind_timecreate,
    kind_usercreate,
    kind_minilabel,
    kind_header,
    kind_lang,
    kind_default)
  values (
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

  $query = "update Kind
    set kind_default=0
    where kind_lang = '$lang'";

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

  $query = "update Kind set
      kind_timeupdate='$timeupdate',
      kind_userupdate='$userupdate',
      kind_minilabel='$label',
      kind_header='$header',
      kind_lang='$lang',
      kind_default='$default'
    where
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

  $query = "delete from Kind where kind_id='$id'";

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

  $query = "select contact_lastname,
      contact_firstname,
      contact_id,
      contact_kind_id
    from Contact where contact_kind_id='$id'";

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

  $query = "select kind_lang, kind_minilabel, kind_header
    from Kind
    where kind_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("kind_lang") . " " . $obm_q->f("kind_minilabel") . " " .
            $obm_q->f("kind_header");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category1 insertion query construction and execution
// Parameters:
//   - $contact : contact hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_insert($contact) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat1 = $contact["cat1_label"];
  $cat1_code = $contact["cat1_code"];

  $query = "insert into ContactCategory1 (
    contactcategory1_timecreate,
    contactcategory1_usercreate,
    contactcategory1_label,
    contactcategory1_code)
  values (
    '$timecreate',
    '$usercreate',
    '$cat1',
    '$cat1_code')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category2 insertion query construction and execution
// Parameters:
//   - $contact : contact hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_insert($contact) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat2 = $contact["cat2_label"];
  $cat2_code = $contact["cat2_code"];
  

  $query = "insert into ContactCategory2 (
    contactcategory2_timecreate,
    contactcategory2_usercreate,
    contactcategory2_label,
    contactcategory2_code)
  values (
    '$timecreate',
    '$usercreate',
    '$cat2',
    '$cat2_code')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category 1  update query execution
// Parameters:
//   - $contact : contact hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_update($contact) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $contact["category2"];
  $cat2 = $contact["cat2_label"];
  $cat2_code = $contact["cat2_code"];

  $query = "update ContactCategory2 set
      contactcategory2_label='$cat2',
      contactcategory2_code='$cat2_code',
      contactcategory2_timeupdate='$timeupdate',
      contactcategory2_userupdate='$userupdate'
    where
      contactcategory2_id='$id'";

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
function run_query_cat1_update($contact) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $contact["category1"];
  $cat1 = $contact["cat1_label"];
  $cat1_code = $contact["cat1_code"];
  
  $query = "update ContactCategory1 set
      contactcategory1_label='$cat1',
      contactcategory1_code='$cat1_code',
      contactcategory1_timeupdate='$timeupdate',
      contactcategory1_userupdate='$userupdate'
    where
      contactcategory1_id='$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id                                  //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat1_label($id) {
  global $cdg_sql;

  $query = "select contactcategory1_label
    from ContactCategory1
    where contactcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contactcategory1_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Contact links query execution
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_links($p_id) {
  global $cdg_sql;

  $query = "select contact_title, contact_id, '$p_id' as contact_category1
    from Contact,ContactCategory1Link
    where contact_id = contactcategory1link_contact_id
      and contactcategory1link_category_id = '$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Category from its id                                   //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat2_label($id) {
  global $cdg_sql;

  $query = "select contactcategory2_label
    from ContactCategory2
    where contactcategory2_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contactcategory2_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category - Contact links query execution                                 //
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_links($p_id) {
  global $cdg_sql;

  $query = "select contact_title, contact_id, '$p_id' as contact_category2
    from Contact, ContactCategory2Link
    where contact_id = contactcategory2link_contact_id
      and contactcategory2link_category_id = '$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Category1 deletion query execution
// Parameters:
//   - $id : Category1 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_delete($id) {
  global $cdg_sql;

  if ($id != '') {
    $query = "delete from ContactCategory1 where contactcategory1_id='$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }
  else 
    $retour = 1;
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category2 deletion query execution
// Parameters:
//   - $id : Category2 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_delete($id) {
  global $cdg_sql;

  if ($id != '') {

    $query = "delete from ContactCategory2 where contactcategory2_id='$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  } else { 
    $retour = 1;
  }

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
    $query = "select count(*) as nb from Subscription where subscription_contact_id = '$id'";

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
