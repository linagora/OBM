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
  global $cgp_archive_only,$obm;
  global $c_public, $c_private;

  $sql_order_dir = $contact['sql_order_dir'];
  $sql_order_field = $contact['sql_order_field'];

  $company_id = $contact['company_id'];
  $fuzzy = $contact['fuzzy'];
  $lname = sql_search_text_parse($contact['lname']);
  $fname = sql_search_text_parse($contact['fname']);
  $phone = sql_search_text_parse($contact['phone']);
  $email = sql_search_text_parse($contact['email']);
  $company = sql_search_text_parse($contact['company']);
  $market = $contact['market'];
  $func = $contact['function'];
  $title = $contact['title'];
  $zip = sql_search_text_parse($contact['zip']);
  $town = sql_search_text_parse($contact['town']);
  $ctry = $contact['country'];
  $dsrc = $contact['datasource'];
  $date_field = ($contact['date_field'] ? $contact['date_field'] : 'timeupdate');
  $dateafter = of_isodate_convert($contact['date_after'],true);
  $datebefore = of_isodate_convert($contact['date_before'],true);
  $mailing_ok_only = $contact['mailing_ok_only'];
  $privacy = $contact["privacy"];
  $newsletter = $contact['newsletter'];
  $archive = $contact['archive'];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, 'contact_date', 'contact_date');
  $multidomain = sql_multidomain('contact');

  // only the one which are allowed (ie. publics )
  $where .= sql_obm_entity_privacy('contact');

  // If company module used (external from Contact)
  if ($cgp_show['module']['company']) {

    $join_comp = 'LEFT JOIN Company ON contact_company_id=company_id LEFT JOIN CompanyEntity ON companyentity_company_id = company_id';
    $company_fields = "company_name,
      company_aka,";

  } else {
    $company_fields = 'contact_company as company_name,';
  }

  // If a lastname has been specified, get it 
  if ($lname != '') {
    $where_name .= "(contact_lastname $like '$lname%' or contact_aka like '$lname%')";
  }
  // If a firstname has been specified, get it 
  if ($fname != '') {
    $where .= " AND contact_firstname $like '$fname%'";
  }
  // If a phone number has been specified, get it 
  if ($phone != '') {
    $where .= " AND (contactentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number $like '$phone%')";
    if ($cgp_show['module']['company']) {
      $where .= "OR companyentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number $like '$phone%')";
    }
    $where .= ')';
  }
  // If an email indication has been specified, get it 
  if ($email != '') {
    $where .= " AND (contactentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address $like '$email%')";
    if ($cgp_show['module']['company']) {
      $where .= " OR companyentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address $like '$email%')";
    }
    $where .= ')';
  }
  // If a post code has been specified, get it
  if ($zip != '') {
    $addr .= " AND address_zipcode $like '$zip%'";
  }
  // If a town has been specified, get it 
  if ($town != '') {
    $addr .= " AND address_town $like '$town%'";
  }
  // If a country has been set
  if (($ctry != $c_all) && ($ctry != '')) {
    $addr .= " AND address_country='$ctry'";
  }
  if ($addr != '') {
    $where .= " AND (contactentity_entity_id IN (SELECT address_entity_id FROM Address WHERE 1=1 ".$addr.")";
    if ($cgp_show['module']['company']) {
      $where .= " OR companyentity_entity_id IN (SELECT address_entity_id FROM Address WHERE 1=1 ".$addr.")";
    }
    $where .= ')';
  }

  // If a company name indication has been specified (without id) get it 
  if (($company != '') && ($company_id == '')) {
    if ($cgp_show['module']['company']) {
      $where .= sql_global_company_name_advanced_search($company, $like);
    } else {
      $where .= " AND contact_company $like '$company%'";
    }
  }
  // If a company was sent as parameter, get it
  if ($company_id != '') {
    $company_id = sql_parse_id($company_id, true);
    $where .= " AND contact_company_id $company_id"; 
  }

  // If a person in charge has been set
  if (($market != $c_all) && ($market != '')) { 
    $market = sql_parse_id($market, true);
    $where .= " AND contact_marketingmanager_id $market"; 
  }
  // If a function has been set
  if (($func != $c_all) && ($func != '')) { 
    $func = sql_parse_id($func, true);
    $where .= " AND contact_function_id $func"; 
  }
  // If a title has been specified, get it 
  if ($title != '') {
    $where .= " AND contact_title $like '$title%'";
  }
  // If a datasource has been specified, get it 
  if (($dsrc != $c_all) && ($dsrc != '')) { 
    $dsrc = sql_parse_id($dsrc, true);
    $where .= " AND contact_datasource_id $dsrc"; 
  }
  if ($dateafter != '') {
    $where .= " AND contact_$date_field > '$dateafter'";
  }
  if ($datebefore != '') {
    $where .= " AND contact_$date_field < '$datebefore'";
  }
  // Get all by default 
  if ($mailing_ok_only == '1') {
    $where .= ' AND contact_mailing_ok=1';
  }
  if ($privacy == $c_public) {
    $where .= ' AND contact_privacy = 0';
  } elseif ($privacy == $c_private) { 
    $where .= ' AND contact_privacy = 1';
  }  
  if ($newsletter == '1') {
    $where .= ' AND contact_newsletter=1';
  }
  // Get only not archived by default
  if ($archive != '1') {
    $where .= ' AND contact_archive=0';
  } elseif ($cgp_archive_only) {
    $where .= ' AND contact_archive=1';
  }

  // User defined data
  $userdata = of_userdata_query_search('Contact', $contact);
  if ($userdata['where'] != '') {
    $where .= ' AND '. $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  $whereq = "WHERE $where $multidomain";
  if ($where_name != '') {
    $whereq .= " AND $where_name";
  }

  // order
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'contact_lastname';
  if (($order == 'contact_lastname') || ($order == 'contact_firstname')) {
    $order = sql_casei_sort($db_type, $order);
  }

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field,'contact_address') == 0) {
    $order = 'address_street';
    $order_next = ', address_zipcode, address_town';
  } else if (strcmp($sql_order_field,'country_name') == 0) {
    $order = 'country_name';
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";
  $selectFields = "
";

  $select = "SELECT
    contact_id as id,
    contact_id,
    address_street,
    address_zipcode,
    address_town,
    address_expresspostal,
    phone_number,
    email_address,
    country_name,
    contact_usercreate,
    contact_company_id,
    contact_kind_id,
    kind_minilabel,
    kind_header,
    kind_lang,
    contact_marketingmanager_id,
    contact_lastname,
    contact_firstname,
    contact_aka,
    contact_function_id,
    contact_title,
    contact_service,
    contact_mailing_ok,
    contact_newsletter,
    contact_archive,
    contact_privacy,
    $date,
    $company_fields
    contactfunction_label as function_label
    FROM Contact
    $join_comp
";

  // XXX Really strange Postgres (8.1.5) behaviour here !
  // if $join_userdata after join_comp, query is really slow (30s) with only
  // criteria : contactcategory1 selected
  $left_join = "
    $join_userdata
    LEFT JOIN Kind ON contact_kind_id = kind_id
    LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id 
    LEFT JOIN Address ON address_entity_id = contactentity_entity_id  AND address_label like 'PREF;%'
    LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='FR' 
    LEFT JOIN Phone ON phone_entity_id = contactentity_entity_id AND phone_label like 'PREF;%'
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label like 'PREF;%'";


  if ((! $fuzzy) || ($lname == '')) {
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct contact_id) FROM Contact $join_comp $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    // If any results (from limited) or result not limited, we get the data
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, 'contact_search(1)');
      $obm_q->query($query);
      if (! $ctu_sql_limit) {
        $count = $obm_q->num_rows();
      }
    }
  }

  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($lname != '') ) {
    $auto_aka = format_name($lname, 0, true, true);
    $sound = phonetic_key($lname);
    $where_name = " AND ($where_name OR contact_aka $like '%$auto_aka%'
      OR contact_sound = '$sound')";
    $whereq = " WHERE $where $where_name";
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(distinct contact_id) FROM Contact $join_comp $left_join $whereq");
      $obm_q->set_num_rows_total($count);
    }
    if (($count > 0) || (! $ctu_sql_limit)) {
      $query = "$select $left_join $whereq $orderq $limit";
      display_debug_msg($query, $cdg_sql, 'contact_search(2)');
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
  $p_id = sql_parse_id($p_id, true);
  $timeupdate = sql_date_format($db_type, 'contact_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'contact_timecreate', 'timecreate');
  $lang = get_lang();
  $date = sql_date_format($db_type, 'contact_date', 'contact_date');
  $multidomain = sql_multidomain('contact');

  if ($cgp_show['module']['company']) {
    $join_comp = 'LEFT JOIN Company ON contact_company_id=company_id';
    $company_fields = "company_id,company_name";
  } else {
    $company_fields = 'contact_company as company_name';
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
    datasource_name,
    $date,
    bd.event_date as contact_birthday,
    an.event_date as contact_anniversary,
    contact_birthday_id,
    contact_anniversary_id,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate
    FROM
    Contact
    $join_comp
    LEFT JOIN Kind ON kind_id=contact_kind_id
    LEFT JOIN UserObm as m ON contact_marketingmanager_id=m.userobm_id
    LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
    LEFT JOIN DataSource ON contact_datasource_id=datasource_id
    LEFT JOIN UserObm as c ON contact_usercreate=c.userobm_id
    LEFT JOIN UserObm as u ON contact_userupdate=u.userobm_id
    LEFT JOIN Event as bd ON contact_birthday_id = bd.event_id
    LEFT JOIN Event as an ON contact_anniversary_id = an.event_id
    WHERE
    contact_id $p_id
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_detail()');
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
  $multidomain = sql_multidomain('company');

  $query = "SELECT company_id,
    company_name
    FROM Company
    WHERE company_id='$c_id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_company()');
  $obm_q = new DB_OBM;
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

  $multidomain = sql_multidomain('kind');

  $query = "SELECT kind_id,
    kind_minilabel,
    kind_header,
    kind_lang,
    kind_default
    FROM Kind
    WHERE 1=1
    $multidomain
    ORDER BY kind_lang, kind_minilabel, kind_header";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Insertion query execution
// Parameters:
//   - $contact[]: Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_insert($contact) {
  global $cgp_show, $cdg_sql, $obm;

  $uid = sql_parse_id($obm['uid']);

  $comp_id = $contact['company_new_id'];
  if ($comp_id < 1) {
    $comp_id = $contact['company_id'];
  }
  // In case company module not used, to avoid postgres error
  $comp_id = sql_parse_id($comp_id);

  $domain_id = sql_parse_id($obm['domain_id']);
  $dsrc = sql_parse_id($contact['datasource'] );
  $company = $contact['company'];
  $kind = sql_parse_id($contact['kind']);
  $market = sql_parse_id($contact['market']);
  $lname = $contact['lname'];
  $fname = $contact['fname'];
  $mname = $contact['mname'];
  $suffix = $contact['suffix'];
  $aka = trim($contact['aka']);
  // If aka is empty we auto fill it
  if ($aka == '') {
    $auto_aka = format_name($lname, 0, true, true);
    if ($auto_aka != $lname) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($lname);
  $service = $contact['service'];
  $func = sql_parse_id($contact['function']);
  $title = $contact['title'];
  $manager = $contact['manager'];
  $assistant = $contact['assistant'];
  $spouse = $contact['spouse'];
  $category = $contact['category'];
  $date = ($contact['date'] ? "'".of_isodate_convert($contact['date'])."'" : 'null');
  $mailok = ($contact['mailok'] == '1' ? '1' : '0');
  $newsletter = ($contact['newsletter'] == '1' ? '1' : '0');

  $add_comment = $contact['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($contact['datecomment']);
    $usercomment = $contact['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $add_comment2 = $contact['add_comment'];
  if ($add_comment2 != '') {
    $datecomment2 = of_isodate_convert($contact['datecomment2']);
    $usercomment2 = $contact['usercomment2'];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $add_comment3 = $contact['add_comment3'];
  if ($add_comment3 != '') {
    $datecomment3 = of_isodate_convert($contact['datecomment3']);
    $usercomment3 = $contact['usercomment3'];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }

  $archive = ($contact['archive'] == '1' ? '1' : '0');
  $priv = (isset($contact['privacy']) ? $contact['privacy'] : '0');

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
    contact_middlename,
    contact_suffix,
    contact_aka,
    contact_sound,
    contact_manager,
    contact_assistant,
    contact_spouse,
    contact_category,
    contact_service,
    contact_function_id,
    contact_title,
    contact_mailing_ok,
    contact_newsletter,
    contact_archive,
    contact_privacy,
    contact_date,
    contact_comment,
    contact_comment2,
    contact_comment3,
    contact_origin
  ) VALUES (
    NOW(),
    NOW(),
    $uid,
    $uid,
    $domain_id,
    $dsrc,
    $comp_id,
    '$company',
    $kind,
    $market,
    '$lname',
    '$fname',
    '$mname',
    '$suffix',
    '$aka',
    '$sound',
    '$manager',
    '$assistant',
    '$spouse',
    '$category',
    '$service',
    $func,
    '$title',
    $mailok,
    $newsletter,
    $archive,
    $priv,
    $date,
    '$comment',
    '$comment2',
    '$comment3',
    '$GLOBALS[c_origin_web]'
  )";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $id = $obm_q->lastid();
  if ($id > 0) {
    if (($cgp_show['module']['company']) && ($retour)) {
      run_query_global_company_contact_number_update($comp_id);
    }    
    $entityid = of_entity_insert($GLOBALS['module'],$id);
    $contact['contact_id'] = $id;

    if($priv == 1) {
      // Subscription support
      $query = "INSERT INTO SynchedContact (
          synchedcontact_user_id, 
          synchedcontact_contact_id,
          synchedcontact_timestamp)
          VALUES ($uid, $id, NOW()
      )";
      display_debug_msg($query, $cdg_sql, 'run_query_contact_insert(1)');
      $obm_q->query($query);
    }

    // Birthday support
    $birthday = of_isodate_convert($contact['birthday'], true);
    if (!empty($birthday))
      $birthday = new Of_Date($birthday);
    else
      $birthday = null;
    run_query_contact_birthday_update('birthday', $id, $uid, null, "$fname $lname", null, $birthday);

    // Anniversary support
    $anniversary = of_isodate_convert($contact['anniversary'], true);
    if (!empty($anniversary))
      $anniversary = new Of_Date($anniversary);
    else
      $anniversary = null;
    run_query_contact_birthday_update('anniversary', $id, $uid, null, "$fname $lname", null, $anniversary);

    run_query_contact_coords_update($entityid, $contact);
    $ret = of_userdata_query_update('contact', $id, $contact);

    // Hook : Post
    if (function_exists('hook_post_run_query_contact_insert')) {
      if (! hook_post_run_query_contact_insert($contact)) {
        return false;
      }
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
  global $obm, $cgp_show, $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $id =  $contact['contact_id'];
  $comp_old_id =  $contact['company_id'];
  $comp_new_id =  $contact['company_new_id'];

  $comp_id = $contact['company_new_id'];
  if ($comp_id < 1) {
    $comp_id = $contact['company_id'];
  }
  // In case company module not used, to avoid postgres error
  $comp_id = sql_parse_id($comp_id);
  $dsrc = sql_parse_id($contact['datasource']);
  $company = $contact['company'];
  $kind = sql_parse_id($contact['kind']);
  $market = sql_parse_id($contact['market']);
  $lname = $contact['lname'];
  $fname = $contact['fname'];
  $mname = $contact['mname'];
  $suffix = $contact['suffix'];
  $aka = trim($contact['aka']);
  // If aka is empty we auto fill it
  if ($aka == '') {
    $auto_aka = format_name($lname, 0, true, true);
    if ($auto_aka != $lname) {
      $aka = $auto_aka;
    }
  }
  $sound = phonetic_key($lname);
  $service = $contact['service'];
  $func = sql_parse_id($contact['function']);
  $title = $contact['title'];
  $manager = $contact['manager'];
  $assistant = $contact['assistant'];
  $spouse = $contact['spouse'];
  $category = $contact['category'];
  $mailok = ($contact['mailok'] == '1' ? '1' : '0');
  $newsletter = ($contact['newsletter'] == '1' ? '1' : '0');
  if ($contact['date'] != '') {
    $query_date = "contact_date = '".of_isodate_convert($contact['date'])."',";
  } else {
    $query_date = 'contact_date = null,';
  }

  $comment = $contact['comment'];
  $add_comment = $contact['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($contact['datecomment']);
    $usercomment = $contact['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comment2 = $contact['comment2'];
  $add_comment2 = $contact['add_comment2'];
  if ($add_comment2 != '') {
    $datecomment2 = of_isodate_convert($contact['datecomment2']);
    $usercomment2 = $contact['usercomment2'];
    $comment2 .= "\n$datecomment2:$usercomment2:$add_comment2";
  }
  $comment3 = $contact['comment3'];
  $add_comment3 = $contact['add_comment3'];
  if ($add_comment3 != '') {
    $datecomment3 = of_isodate_convert($contact['datecomment3']);
    $usercomment3 = $contact['usercomment3'];
    $comment3 .= "\n$datecomment3:$usercomment3:$add_comment3";
  }
  $archive = ($contact['archive'] == '1' ? '1' : '0');
  $priv = (isset($contact['privacy']) ? $contact['privacy'] : '0');

  // Archived contact unsubscribed from mailings
  if ($archive == '1') {
    $mailok = '0';
    $newsletter = '0';
  }
  $sql_id = sql_parse_id($id, true);
  $query = "UPDATE Contact SET
    contact_timeupdate='".date('Y-m-d H:i:s')."',
    contact_userupdate='".$obm['uid']."',
    contact_datasource_id=$dsrc,
    contact_company_id=$comp_id,
    contact_company='$company',
    contact_kind_id=$kind,
    contact_marketingmanager_id=$market,
    contact_lastname='$lname',
    contact_firstname='$fname',
    contact_middlename='$mname',
    contact_suffix='$suffix',
    contact_aka='$aka',
    contact_sound='$sound',
    contact_manager='$manager',
    contact_assistant='$assistant',
    contact_spouse='$spouse',
    contact_category='$category',
    contact_service='$service',
    contact_function_id=$func,
    contact_title='$title',
    contact_mailing_ok=$mailok,
    contact_newsletter=$newsletter,
    contact_archive=$archive,
    contact_privacy=$priv,
    $query_date
    contact_comment='$comment',
    contact_comment2='$comment2',
    contact_comment3='$comment3',
    contact_origin='$GLOBALS[c_origin_web]'
  WHERE contact_id $sql_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($cgp_show['module']['company']) {
    // If company has changed, update the companies contact number
    if (($retour) && ($comp_new_id > 0) && ($comp_new_id != $comp_old_id)) {
      run_query_global_company_contact_number_update($comp_new_id);
      run_query_global_company_contact_number_update($comp_old_id);
    }
  }

  // BEGIN birthday support

  $birthday = of_isodate_convert($contact['birthday'], true);
  if (!empty($birthday)) {
    $birthday = new Of_Date($birthday);
  } else {
    $birthday = null;
  }
  $old_contact = run_query_contact_detail($id);
  $old_birthday = $old_contact->f('contact_birthday');
  if ($old_birthday) {
    $old_birthday = new Of_Date($old_birthday);
  } else {
    $old_birthday = null;
  }
  run_query_contact_birthday_update('birthday', $id, $old_contact->f('contact_usercreate'),
    $old_contact->f('contact_birthday_id'), "$fname $lname", $old_birthday, $birthday);

  // BEGIN anniversary support

  $anniversary = of_isodate_convert($contact['anniversary'], true);
  if (!empty($anniversary)) {
    $anniversary = new Of_Date($anniversary);
  } else {
    $anniversary = null;
  }
  $old_anniversary = $old_contact->f('contact_anniversary');
  if ($old_anniversary) {
    $old_anniversary = new Of_Date($old_anniversary);
  } else {
    $old_anniversary = null;
  }
  run_query_contact_birthday_update('anniversary', $id, $old_contact->f('contact_usercreate'),
    $old_contact->f('contact_anniversary_id'), "$fname $lname", $old_anniversary, $anniversary);

  if ($retour) {
    $ret = of_userdata_query_update('contact', $id, $contact);
    $entityid = of_entity_get('contact', $id);
    run_query_contact_coords_update($entityid, $contact);

    // Hook : Post
    if (function_exists('hook_post_run_query_contact_update')) {
      if (! hook_post_run_query_contact_update($contact)) {
        return false;
      }
    }
  }

  return $retour;
}


/**
 * run_query_contact_coords_update 
 * 
 * @param mixed $id 
 * @param mixed $contact 
 * @access public
 * @return void
 */
function run_query_contact_coords_update($id, $contact) {
  global $cdg_sql;

  $phones = $contact['phones'];
  $addresses = $contact['addresses'];
  $ims = $contact['ims'];
  $websites = $contact['websites'];
  $emails = $contact['emails'];
  $obm_q = new DB_OBM;

  $query = "DELETE FROM Phone WHERE phone_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(phone)');
  $obm_q->query($query);
  if(is_array($phones)) {
    $cpt = array();
    $pref = 'PREF;';
    foreach($phones as $phone) {
      if(trim($phone['number']) != '' ) {
        $phone['label'] = str_replace('_', ';', $phone['label']);
        $cpt[$phone['label']]++;
        $query = "INSERT INTO Phone (phone_entity_id, phone_number, phone_label) VALUES ($id, '$phone[number]', '$pref$phone[label];X-OBM-Ref".$cpt[$phone['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(phone)');
        $obm_q->query($query);
	$pref = '';
      }
    }
  }

  $query = "DELETE FROM Address WHERE address_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(address)');
  $obm_q->query($query);
  if(is_array($addresses)) {
    $cpt = array();
    $pref = 'PREF;';
    foreach($addresses as $address) {
      if(trim($address['street']) != '' || (trim($address['country']) != '' && trim($address['country']) != 'none') || trim($address['zipcode']) != ''
         || trim($address['expresspostal']) != '') {
        if(trim($address['country']) == 'none') $address['country'] = '';
        $address['label'] = str_replace('_', ';', $address['label']);
        $cpt[$address['label']]++;
        $query = "INSERT INTO Address (
          address_entity_id,
          address_street,
          address_zipcode,
          address_town,
          address_expresspostal,
          address_country,
          address_label
        ) VALUES (
          $id, 
          '$address[street]',
          '$address[zipcode]',
          '$address[town]',
          '$address[expresspostal]',
          '$address[country]',
          '$pref$address[label];X-OBM-Ref".$cpt[$address['label']]."'
        )";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(address)');
        $obm_q->query($query);
	$pref = '';
      }
    }
  }

  $query = "DELETE FROM Website WHERE website_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(website)');
  $obm_q->query($query);

  if(is_array($websites)) {
    $cpt = array();
    $pref = 'PREF;';
    foreach($websites as $website) {
      if(trim($website['url']) != '' ) {
        $website['label'] = str_replace('_', ';', $website['label']);
        $cpt[$website['label']]++;
        $query = "INSERT INTO Website (website_entity_id, website_url, website_label) VALUES ($id, '$website[url]', '$pref$website[label];X-OBM-Ref".$cpt[$website['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(website)');
        $obm_q->query($query);
	$pref = '';
      }
    }
  }

  $query = "DELETE FROM IM WHERE im_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(IM)');
  $obm_q->query($query);

  if(is_array($ims)) {
    $cpt = array();
    $pref = 'PREF;';
    foreach($ims as $im) {
      if(trim($im['address']) != '' ) {
        $query = "INSERT INTO IM (im_entity_id, im_address, im_protocol, im_label) VALUES ($id, '$im[address]', '$im[protocol]', '$pref$im[label]')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(IM)');
        $obm_q->query($query);
	$pref = '';
      }
    }
  }

  $query = "DELETE FROM Email WHERE email_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(Email)');
  $obm_q->query($query);

  if(is_array($emails)) {
    $cpt = array();
    $pref = 'PREF;';
    foreach($emails as $email) {
      if(trim($email['address']) != '' ) {
        $email['label'] = str_replace('_', ';', $email['label']);
        $cpt[$email['label']]++;
        $query = "INSERT INTO Email (email_entity_id, email_address, email_label) VALUES ($id, '$email[address]', '$pref$email[label];X-OBM-Ref".$cpt[$email['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(Email)');
        $obm_q->query($query);
	$pref = '';
      }
    }
  }
}


/**
 * run_query_contact_coords
 * 
 * @param mixed $id
 * @param mixed $inherit
 * @param mixed $update
 * @access public
 * @return void
 */
function run_query_contact_coords($id, $inherit=false, $update=false) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $coords = array();

  // Phone
  $query = "SELECT phone_id, phone_label, phone_number
    FROM Phone
      INNER JOIN ContactEntity ON phone_entity_id = contactentity_entity_id
    WHERE contactentity_contact_id = $id";
  if ($inherit) {
    $query .= " UNION 
      SELECT phone_id, 'COMPANY;X-OBM-REF1' as phone_label, phone_number
      FROM Phone 
        INNER JOIN CompanyEntity ON phone_entity_id = companyentity_entity_id 
        INNER JOIN Contact ON contact_company_id = companyentity_company_id
      WHERE contact_id = $id";
  }
  $query .= ' ORDER BY phone_id';
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords(Phone)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('phone_label'));
    // Remove the X-OBM mark
    array_pop($label);
    // Remove the PREF mark
    if ($label[0] == 'PREF') {
      unset($label[0]);
    }
    $label = implode('_',$label);
    $coords['phone'][] = array('label' => $label, 'number' => $obm_q->f('phone_number'));
  }

  // Email
  $query = "SELECT email_id, email_label, email_address
    FROM Email
      INNER JOIN ContactEntity ON email_entity_id = contactentity_entity_id
    WHERE contactentity_contact_id = $id";
  if ($inherit) {
    $query .= " UNION 
      SELECT email_id, 'COMPANY;X-OBM-REF1' as email_label, email_address
      FROM Email 
        INNER JOIN CompanyEntity ON email_entity_id = companyentity_entity_id 
        INNER JOIN Contact ON contact_company_id = companyentity_company_id
      WHERE contact_id = $id";
  }  
  $query .= ' ORDER BY email_id';
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords(Email)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('email_label'));
    array_pop($label);
    // Remove the PREF mark
    if ($label[0] == 'PREF') {
      unset($label[0]);
    }
    $label = implode('_',$label);
    $coords['email'][] = array('label' => $label, 'address' => $obm_q->f('email_address'));
  }

  // Address
  $lang = get_lang();
  $query = "SELECT address_id,
      address_label, address_street, address_zipcode, address_expresspostal,
      address_town, address_country, country_name
    FROM Address
      INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id 
      LEFT JOIN Country ON country_iso3166 = address_country
        AND country_lang = '$lang' WHERE contactentity_contact_id = $id";
  if ($inherit) {
    $query .= " UNION
      SELECT address_id, 'COMPANY;X-OBM-REF1' as address_label, address_street,
        address_zipcode, address_expresspostal, address_town, address_country,
        country_name 
      FROM Address
        INNER JOIN CompanyEntity ON address_entity_id = companyentity_entity_id 
        INNER JOIN Contact ON contact_company_id = companyentity_company_id
        LEFT JOIN Country ON country_iso3166 = address_country
          AND country_lang = '$lang'
      WHERE contact_id = $id";
  }  
  $query .= ' ORDER BY address_id';
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords(Address)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('address_label'));
    array_pop($label);
    // Remove the PREF mark
    if ($label[0] == 'PREF') {
      unset($label[0]);
    }
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

  // IM
  $query = "SELECT *
    FROM IM
      INNER JOIN ContactEntity ON im_entity_id = contactentity_entity_id
    WHERE contactentity_contact_id = $id
    ORDER BY im_id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords(IM)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $coords['im'][] = array('protocol' => $obm_q->f('im_protocol'),'address' => $obm_q->f('im_address'));
  }

  // Website
  $query = "SELECT website_id, website_label, website_url
    FROM Website
      INNER JOIN ContactEntity ON website_entity_id = contactentity_entity_id
    WHERE contactentity_contact_id = $id";
  if($inherit) {
    $query .= " UNION 
      SELECT website_id, 'COMPANY;X-OBM-REF1' as website_label, website_url
      FROM Website
        INNER JOIN CompanyEntity ON website_entity_id = companyentity_entity_id 
        INNER JOIN Contact ON contact_company_id = companyentity_company_id
      WHERE contact_id = $id";
  }  
  $query .= ' ORDER BY website_id';
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords(Website)');
  $obm_q->query($query);        
  while($obm_q->next_record()) {
    $label = explode(';',$obm_q->f('website_label'));
    array_pop($label);
    // Remove the PREF mark
    if ($label[0] == 'PREF') {
      unset($label[0]);
    }
    $label = implode('_',$label);
    $coords['website'][] = array('label' => $label, 'url' => $obm_q->f('website_url'));
  }

  return $coords;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of static lists where the contact is registered
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_contact_list_nb($p_id) {
  global $cdg_sql;

  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT count(DISTINCT contactlist_list_id) as nb
    FROM ContactList
    WHERE contactlist_contact_id $p_id"; 

  display_debug_msg($query, $cdg_sql, 'get_linked_contact_list_nb()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of publications where the contact has subscribed
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_contact_publication_nb($p_id) {
  global $cdg_sql;

  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT DISTINCT subscription_publication_id as nb
    FROM Subscription 
    WHERE subscription_contact_id $p_id"; 

  display_debug_msg($query, $cdg_sql, 'get_linked_contact_publication_nb()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Deletion query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_delete($p_id) {
  global $obm, $cdg_sql, $c_use_connectors;

  // Hook : Pre
  if (function_exists('hook_pre_run_query_contact_delete')) {
    if (! hook_pre_run_query_contact_delete($p_id)) {
      return false;
    }
  }

  $obm_q = new DB_OBM;

  $sql_id = sql_parse_id($p_id, true);
  $multidomain = sql_multidomain('contact');

  $query = "SELECT contact_company_id,
      contact_birthday_id,
      contact_anniversary_id
    FROM Contact
    WHERE contact_id $sql_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_delete(select)');
  $retour = $obm_q->query($query);
  $obm_q->next_record();

  $comp_id =  $obm_q->f('contact_company_id');
  $birthday_id =  $obm_q->f('contact_birthday_id');
  $anniversary_id =  $obm_q->f('contact_anniversary_id');

  run_query_global_delete_document_links($p_id, 'contact');    
  $ret = of_userdata_query_delete('contact', $p_id);

  // BEGIN birthday and anniversary support
  run_query_contact_birthday_update('birthday', null, null, $birthday_id, null, null, null);
  run_query_contact_birthday_update('anniversary', null, null, $anniversary_id, null, null, null);
  // END birthday and anniversary support

  of_entity_delete($GLOBALS['module'], $p_id);

  $query = "DELETE FROM Contact WHERE contact_id $sql_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_delete(1)');
  $retour = $obm_q->query($query);

  // If connectors in use

  if ($c_use_connectors) {
    $uid = sql_parse_id($obm['uid']);
    $query = "INSERT INTO DeletedContact (
        deletedcontact_contact_id,
        deletedcontact_user_id,
        deletedcontact_timestamp,
        deletedcontact_origin)
      VALUES (
        $p_id,
        $uid,
        NOW(),
        '$GLOBALS[c_origin_web]')";
    display_debug_msg($query, $cdg_sql, 'run_query_contact_delete(2)');
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

  $p_id = sql_parse_id( $p_id, true);
  $query = "DELETE FROM Subscription WHERE subscription_contact_id $p_id"; 

  display_debug_msg($query, $cdg_sql, 'run_query_contact_delete_subscription()');
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
  global $cdg_sql, $cgp_show, $obm;

  $uid = sql_parse_id($obm['uid'], true);
  $multidomain = sql_multidomain('contact');	
  $where_id = '';
  if ($cid != '') {
    $cid = sql_parse_id($cid, true, true);
    $where_id = "contact_id $cid AND";
  }

  if ($cgp_show['module']['company']) {
    $comp_id = sql_parse_id($comp_id, true);
    $join_comp = 'LEFT JOIN Company ON contact_company_id=company_id';
    $where_comp = "OR (contact_company_id $comp_id AND contact_lastname='$lname')";
    $select_comp = ', company_name';
  }

  $query = "SELECT DISTINCT contact_id,
    contact_company_id,
    contact_lastname,
    contact_firstname
    $select_comp
    FROM Contact
    $join_comp
    WHERE $where_id
    ((contact_lastname='$lname' AND contact_firstname='$fname') $where_comp)
    AND ((contact_privacy = 1 AND contact_usercreate $uid) OR contact_privacy = 0)
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_check()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insert a contact imported from a VCard
// Parameters:
//   - $params : form values
///////////////////////////////////////////////////////////////////////////////
function run_query_vcard_insert($params) {
  $file_content = file_get_contents($params['vcard_tmp']);
  try {
    $vcards = Vpdi::decode($file_content, 'VCARD');
  } catch (Exception $e) {
    return false;
  }
  $ids = array();
  foreach ($vcards as $vcard) {
    $contact = OBM_Contact::import($vcard);
    if (isset($contact['contact_id']) && run_query_check_contact_existence($contact['contact_id'])) {
      if (run_query_contact_update($contact)) {
        $ids[] = $contact['contact_id'];
      }
    } else {
      $id = run_query_contact_insert($contact);
      if ($id > 0) {
        $ids[] = $id;
      }
    }
  }
  if (count($ids) != count($vcards)) {
    return false;
  }
  return $ids;
}


///////////////////////////////////////////////////////////////////////////////
// Check contact existence
///////////////////////////////////////////////////////////////////////////////
function run_query_check_contact_existence($contact_id) {
  $obm_q = new DB_OBM;
  $obm_q->query("SELECT 1 FROM Contact WHERE contact_id='{$contact_id}'");
  return $obm_q->next_record() !== false;
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

  $lname = $contact['lname'];
  $fname = $contact['fname'];
  $comp_new_id = $contact['company_new_id'];
  $comp_id = $contact['company_id'];
  $comp = ($comp_new_id != '') ? $comp_new_id : $comp_id;

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
  }

  // Hook : Pre
  if (function_exists('hook_pre_check_contact_update_rights')) {
    if (! hook_pre_check_contact_update_rights($params, $c)) {
      return false;
    }
  }

  if ($id > 0) {

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
function check_contact_data_form($cid, &$contact) {
  global $perm, $cgp_show, $cright_write_admin;
  global $php_regexp_phone,$php_regexp_fax,$php_regexp_email;
  global $l_fill_lastname, $l_fill_company, $l_j_check_phone;
  global $l_j_check_hphone,$l_j_check_mphone, $l_j_check_fax, $l_j_check_email;
  global $err, $l_exist_error, $l_err_public_rights;

  if (!check_user_defined_rules('contact', $contact)) {
    return false;
  }

  $lname = $contact['lname'];
  $fname = $contact['fname'];
  $priv = $contact['privacy'];
  $c_id = $contact['company_id'];
  $c_new_id = $contact['company_new_id'];
  $phones = $contact['phones'];
  $emails = $contact['emails'];

  // MANDATORY: Contact name
  $lname = strtoupper($lname);
  if (trim($lname) == '') {
    $err['msg'] = $l_fill_lastname;
    return false;
  }

  /*if ($cgp_show['module']['company']) {
    // MANDATORY: a company must be set
    if (($c_id < 1) && ($c_new_id < 1)) {
      $err['msg'] = $l_fill_company;
      return false;
    }
  }*/

  // Contact Firstname
  $fname = ucfirst($fname);

  // Contact phone
  if(is_array($phones)) {
    foreach($phones as $phone) {
      if (($phone['number'] != '') && (preg_match($php_regexp_phone, $phone['number']) == 0)) {
        $err['msg'] = $l_j_check_phone . " : $phone[number]";
        return false;
      }
    }
  }

  // Contact email
  if(is_array($emails)) {
    foreach($emails as $email) {
      if (($email['address'] != '') && (preg_match($php_regexp_email, $email['address']) == 0)) {
        $err['msg'] = $l_j_check_email . " : $email[address]";
        return false;
      }  
    }
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
  global $l_err_public_rights, $cgp_show;

  $delete_ok = true;

  // Hook : Pre
  if (function_exists('hook_pre_check_can_delete_contact')) {
    if (! hook_pre_check_can_delete_contact($p_id)) {
      return false;
    }
  }

  // Allow public contact handling only if write_admin right
  $lc = get_contact_info($p_id);
  if (($lc['privacy'] == '0') &&
    (! $perm->check_right('contact', $cright_write_admin))) {
      $err['msg'] .= $l_err_public_rights;
      $delete_ok = false;
      return false;
    }

  // Links from deals
  if ($cgp_show['module']['deal']) {
    $nb = get_global_linked_deal_nb($p_id, 'contact1', true, 'contact2');
    if ($nb > 0) {
      $delete_ok = false;
      $err['msg'] .= "$l_link_deal";
    } else {
      $ok_msg .= "$l_link_deal_no";
    }
  }

  // Links from lists
  if ($cgp_show['module']['list']) {
    $nb = get_linked_contact_list_nb($p_id);
    if ($nb > 0) {
      $delete_ok = false;
      $err['msg'] .= "<br />$l_link_list";
    } else {
      $ok_msg .= "<br />$l_link_list_no";
    }
  }

  // Links from Contract
  if ($cgp_show['module']['contract']) {
    $nb = get_global_linked_contract_nb($p_id, 'contact1', true, 'contact2');
    if ($nb > 0) {
      $delete_ok = false;
      $err['msg'] .= "<br />$l_link_contract";
    } else {
      $ok_msg .= "<br />$l_link_contract_no";
    }
  }
  if ($cgp_show['module']['publication']) {
    // Links from Publications (Subscriptions)
    $nb = get_linked_contact_publication_nb($p_id);
    if ($nb > 0) {
      $delete_ok = false;
      $err['msg'] .= "<br />$l_link_publication";
    } else {
      $ok_msg .= "<br />$l_link_publication_no";
    }
  }
  return $delete_ok;
}


/**
 * Get the contact infos
 * @param $con_id : contact id
 * @return : $c hash with contact infos
 **/
function get_contact_info($con_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $con_id = sql_parse_id($con_id, true);
  $query = "SELECT
    contact_company_id,
    contact_privacy
    FROM Contact
    WHERE contact_id $con_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_contact_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $c['company_id'] = $obm_q->f('contact_company_id');
  $c['privacy'] = $obm_q->f('contact_privacy');

  return $c;
}


/**
 * Get the contact kind default value
 * @return : $kind_id
 **/
function get_contact_kind_default() {
  global $cdg_sql;

  $multidomain = sql_multidomain('kind');

  $query = "SELECT kind_id FROM Kind WHERE kind_default=1 $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_contact_kind_default()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $kind_id = $obm_q->f('kind_id');

  return $kind_id;
}


///////////////////////////////////////////////////////////////////////////////
// Kind insertion query construction and execution
// Parameters:
//   - $contact : contact hash info
//                keys used : kind_label, kind_lang, kind_header
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_kind_insert($contact) {
  global $cdg_sql, $obm;

  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id($obm['uid']);
  $label = $contact['kind_label'];
  $lang = $contact['kind_lang'];
  $header = $contact['kind_header'];
  //$default = ($contact['kind_default'] == 1 ? 1 : 0);
  $default = sql_parse_int($contact['kind_default']);
  $domain_id = sql_parse_id($obm['domain_id']);

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
      $usercreate,
      $domain_id,
      '$label',
      '$header',
      '$lang',
      $default)";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind_insert()');
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

  $multidomain = sql_multidomain('kind');

  $query = "UPDATE Kind
    SET kind_default=0
    WHERE kind_lang = '$lang' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind_clear_default()');
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

  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id($obm['uid']);
  $id = sql_parse_id($contact['kind'], true);
  $label = $contact['kind_label'];
  $lang = $contact['kind_lang'];
  $header = $contact['kind_header'];
  $default = sql_parse_int($contact['kind_default'] );
  $multidomain = sql_multidomain('kind');

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
    kind_default=$default
    WHERE
    kind_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind_update()');
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

  $multidomain = sql_multidomain('kind');
  $id = sql_parse_id($id, true);
  $query = "DELETE FROM Kind WHERE kind_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind_delete()');
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

  $multidomain = sql_multidomain('contact');
  $id = sql_parse_id($id, true);
  $query = "SELECT contact_lastname,
    contact_firstname,
    contact_id,
    contact_kind_id
    FROM Contact 
    WHERE contact_kind_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_kind_links()');
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

  $multidomain = sql_multidomain('kind');	
  $id = sql_parse_id($id, true);
  $query = "SELECT kind_lang, kind_minilabel, kind_header
    FROM Kind
    WHERE kind_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_contact_kind_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $retour = $obm_q->f('kind_lang') . ' ' . $obm_q->f('kind_minilabel') . ' ' .
    $obm_q->f('kind_header');
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
    $id = sql_parse_id($id, true);
    $query = "SELECT count(*) as nb
      FROM Subscription
      WHERE subscription_contact_id $id"; 

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    $retour = $obm_q->f('nb');
  } else { 
    $retour = 0;
  }

  return $retour;
}


/**
 * Update contact birthday or anniversary
 *
 * When new need : - $contact_id
 *                 - $contact_usercreate
 *                 - $contact_fullname
 *                 - $new_value
 *                 - set $event_id to null
 * 
 * When exists need : - $event_id
 *                    - $old_value
 *                    - $new_value
 * 
 * When delete : - need $event_id
 *               - set $new_value to null
 * 
 * @param Integer $date ('birthday' || 'anniversary')
 * @param Integer $contact_id
 * @param Integer $contact_usercreate
 * @param Integer $event_id
 * @param String $title
 * @param Of_Date $old_value
 * @param Of_Date $new_value
 */
function run_query_contact_birthday_update ($date='birthday', $contact_id, $contact_usercreate, $event_id, $contact_fullname, $old_value, $new_value) {
    global $cdg_sql, $obm;
    global $l_birthday_event_title, $l_anniversary_event_title;

    list($nope_event, $insert_event, $update_event, $delete_event) = array(0,1,2,3);
    if ($event_id == null) {
      if ($new_value != null) {
        $do = $insert_event;
      }
    } else {
      if ($new_value == null) {
        $do = $delete_event;
      } else if ($new_value->compare($old_value) != 0) {
        $do = $update_event;
      }
    }

    $obm_q = new DB_OBM;

    $multidomain_contact = sql_multidomain('contact');
    $multidomain_event = sql_multidomain('event');

    switch ($do) {
    case $insert_event:
      $duration = 3600*24;
      $label = ${"l_${date}_event_title"};
      $title = str_replace('\'', '\\\'', sprintf($label, $contact_fullname));

      $query = "INSERT INTO Event
        (event_timeupdate,
        event_timecreate,
        event_usercreate,
        event_origin,
        event_owner,
        event_timezone,
        event_title,
        event_date,
        event_description,
        event_properties,
        event_location,
        event_category1_id,
        event_priority,
        event_privacy,
        event_duration,
        event_repeatkind,
        event_repeatfrequence,
        event_repeatdays,
        event_allday,
        event_color,
        event_endrepeat,
        event_domain_id)
        VALUES
        (
         NOW(),
         NOW(),
        '$contact_usercreate',
        '$GLOBALS[c_origin_web]',
        '$contact_usercreate',
        '".Of_Date::getOption('timezone')."',
        '$title',
        '$new_value',
        '',
        '',
        '',
        NULL,
        '2',
        '0',
        '$duration',
        'yearly',
        '1',
        '0000000',
        '1',
        '',
        NULL,
        '$obm[domain_id]')";

      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(insert event)');

      $insert_event_id = $obm_q->lastid();
      if ($insert_event_id) {
        of_entity_insert('event', $insert_event_id);
        $sql_id = sql_parse_id($contact_id);
        $query = "UPDATE Contact
          SET contact_${date}_id = $insert_event_id
          WHERE
          contact_id = $sql_id
          $multidomain_contact";

        $obm_q->query($query);
        display_debug_msg($query, $cdg_sql, "run_query_contact_birthday_update(update birthday id)");
        $entity_id = of_entity_get('user', $contact_usercreate);
        $query = "INSERT INTO EventLink (
          eventlink_timecreate,
          eventlink_usercreate,
          eventlink_event_id, 
          eventlink_entity_id,
          eventlink_state) 
        VALUES (
          NOW(),
          $contact_usercreate,
          $insert_event_id,
          $entity_id,
          'ACCEPTED')";

        $obm_q->query($query);
        display_debug_msg($query, $cdg_sql, "run_query_contact_birthday_update(insert entity)");
      }

      break;

    case $update_event:
      $sql_id = sql_parse_id($event_id);
      $query = "UPDATE Event SET
        event_date = '$new_value',
        event_origin = '$GLOBALS[c_origin_web]'
      WHERE
        event_id = $sql_id
        $multidomain_event";

      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(update event)');

      break;

    case $delete_event:
      of_entity_delete('event',$event_id);
      $sql_id = sql_parse_id($event_id);
      $query = "DELETE FROM Event WHERE event_id = $sql_id
        $multidomain_event";
      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(delete event)');

      break;
    }
  }

?>
