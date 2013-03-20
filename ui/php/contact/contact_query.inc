<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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

  $ctu_sql_limit = true;
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $uid = sql_parse_id($obm['uid']);
  $search_action = OBM_Acl::ACCESS;

  $sql_order_dir = $contact['sql_order_dir'];
  $sql_order_field = $contact['sql_order_field'];

  $company_id = $contact['company_id'];
  $fuzzy = $contact['fuzzy'];
  $lastname = sql_search_text_parse($contact['lastname']);
  $firstname = sql_search_text_parse($contact['firstname']);
  $commonname = sql_search_text_parse($contact['commonname']);
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
  $newsletter = $contact['newsletter'];
  $archive = $contact['archive'];
  $lang = get_lang();

  //$limit = sql_limit($db_type, '', '', '', true);
  $date = sql_date_format($db_type, 'contact_date', 'contact_date');
  $multidomain = sql_multidomain('contact');

  // XXXXXX Migrate the access right to the Addressbook
  if ($privacy != $c_public) {
    // Now we play with ACLs
    $allowed_contacts_query = "
      SELECT contactentity_contact_id AS id
      FROM UserEntity
      INNER JOIN EntityRight ON userentity_entity_id = entityright_consumer_id
      INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
      WHERE userentity_user_id = $uid
        AND entityright_$search_action = 1
      UNION ALL SELECT contactentity_contact_id AS id FROM UserEntity
      INNER JOIN of_usergroup ON userentity_user_id = of_usergroup_user_id
      INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id
      INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id
      INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
      WHERE userentity_user_id = $uid
        AND entityright_$search_action = 1
      UNION ALL SELECT contact_id as id FROM Contact 
      WHERE contact_usercreate = $uid";
    display_debug_msg($allowed_contacts_query, $cdg_sql, 'contact_search(0)');
    $obm_q->query($allowed_contacts_query);
    $f_contact_ids = array();
    while ($obm_q->next_record()) {
      $f_contact_ids[] = sql_parse_id($obm_q->f('id'));
    }
    $where = "((1=1 $multidomain) ";
    if (count($f_contact_ids) > 0) {
      $where .= " OR contact_id IN (".implode(',',$f_contact_ids).")";
    }
    $where.= ')';
  } else {
    $where = ' 1=1 ';
  }

  // If company module used (external from Contact)
  if ($cgp_show['module']['company']) {

    $join_comp = 'LEFT JOIN Company ON contact_company_id=company_id LEFT JOIN CompanyEntity ON companyentity_company_id = company_id';
    $company_fields = "company_name,
      company_aka,";

  } else {
    $company_fields = "contact_company as company_name,
      '' as company_aka,";
  }

  // If a lastname has been specified, get it 
  if ($lastname != '') {
    $where_name .= "(contact_lastname #LIKE '$lastname%' or contact_aka #LIKE '$lastname%')";
  }
  // If a firstname has been specified, get it 
  if ($firstname != '') {
    $where .= " AND contact_firstname #LIKE '$firstname%'";
  }
  // If a commonname has been specified, get it 
  if ($commonname != '') {
    $where .= " AND contact_commonname #LIKE '$commonname%'";
  } 
  // If a phone number has been specified, get it 
  if ($phone != '') {
    $where .= " AND (contactentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number #LIKE '$phone%')";
    if ($cgp_show['module']['company']) {
      $where .= "OR companyentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number #LIKE '$phone%')";
    }
    $where .= ')';
  }
  // If an email indication has been specified, get it 
  if ($email != '') {
    $where .= " AND (contactentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address #LIKE '$email%')";
    if ($cgp_show['module']['company']) {
      $where .= " OR companyentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address #LIKE '$email%')";
    }
    $where .= ')';
  }
  // If a post code has been specified, get it
  if ($zip != '') {
    $addr .= " AND address_zipcode #LIKE '$zip%'";
  }
  // If a town has been specified, get it 
  if ($town != '') {
    $addr .= " AND address_town #LIKE '$town%'";
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
      $where .= sql_global_company_name_advanced_search($company, '#LIKE');
    } else {
      $where .= " AND contact_company #LIKE '$company%'";
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
    $where .= " AND contact_title #LIKE '$title%'";
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
  if ($newsletter == '1') {
    $where .= ' AND contact_newsletter=1';
  }
  // Get only not archived by default
  if ($archive != '1') {
    $where .= ' AND contact_archive=0';
  } elseif ($cgp_archive_only) {
    $where .= ' AND contact_archive=0';
  }

  // User defined data
  $userdata = of_userdata_query_search('Contact', $contact);
  if ($userdata['where'] != '') {
    $where .= ' AND '. $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  // Construct the where statement
  $whereq = "WHERE $where";
  if ($where_name != '') {
    $whereq .= " AND $where_name";
  }

  // order
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'contact_lastname';
  if (($order == 'contact_lastname') || ($order == 'contact_firstname')) {
    $order = sql_casei_sort($db_type, $order);
  }
  if (strcmp($sql_order_field,'contact_address') == 0) {
    $order = 'address_street';
    $order_next = ', address_zipcode, address_town';
  } else if (strcmp($sql_order_field,'country_name') == 0) {
    $order = 'country_name';
  } else if (strcmp($sql_order_field,'contact_displayname') == 0) {
    $order = sql_casei_sort($db_type, 'contact_firstname');
    $order_next_field = sql_casei_sort($db_type, 'contact_lastname');
    $order_next = ", $order_next_field $sql_order_dir";
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
    WorkPhone.phone_number as \"WorkPhone.phone_number\",
    MobilePhone.phone_number as \"MobilePhone.phone_number\",
    FaxPhone.phone_number as \"FaxPhone.phone_number\",
    HomePhone.phone_number as \"HomePhone.phone_number\",
    email_address,
    country_name,
    contact_usercreate,
    contact_company_id,
    contact_kind_id,
    kind_minilabel,
    kind_header,
    kind_lang,
    contact_marketingmanager_id,
    #CONCAT(contact_firstname,' ',contact_lastname) as contact_displayname,
    contact_lastname,
    contact_firstname,
    contact_commonname,
    contact_aka,
    contact_function_id,
    contact_title,
    contact_service,
    contact_mailing_ok,
    contact_newsletter,
    contact_archive,
    $date,
    $company_fields
    contactfunction_label as function_label
    FROM Contact
    $join_comp
";

  // XXX Really strange Postgres (8.1.5) behaviour here !
  // if $join_userdata after join_comp, query is really slow (30s) with only
  // criteria : contactcategory1 selected
  $coords_join = "
    LEFT JOIN Phone as WorkPhone ON WorkPhone.phone_entity_id = contactentity_entity_id AND WorkPhone.phone_label = 'WORK;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as MobilePhone ON MobilePhone.phone_entity_id = contactentity_entity_id  AND MobilePhone.phone_label = 'CELL;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as FaxPhone ON FaxPhone.phone_entity_id = contactentity_entity_id  AND FaxPhone.phone_label = 'WORK;FAX;X-OBM-Ref1'
    LEFT JOIN Phone as HomePhone ON HomePhone.phone_entity_id = contactentity_entity_id  AND HomePhone.phone_label = 'HOME;VOICE;X-OBM-Ref1'
    LEFT JOIN Address ON address_entity_id = contactentity_entity_id AND address_label = 'WORK;X-OBM-Ref1'
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
";
  $left_join = "
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id 
    $join_userdata
    $coords_join
    LEFT JOIN Kind ON contact_kind_id = kind_id
    LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
    LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='FR'
";


  if ((! $fuzzy) || ($lastname == '')) {
    if ($_SESSION['set_rows'] != '') {
      $limit = $_SESSION['set_rows'];
    } else {
      $limit = $set_rows_default;
    }
    if (($contact['page'] > 0) && ($name == $contact['of_display_name'])) {
      $off = ($contact['page'] - 1) * $limit;
    } else {
      $off = '0';
    }
    $query = "SELECT contact_id FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id $join_userdata $coords_join $join_comp $whereq $multidomain $orderq";
    display_debug_msg($query, $cdg_sql, 'contact_search(1)');
    $obm_q->xquery($query);
    $count = $obm_q->nf();
    $obm_q->set_num_rows_total($count);
    if($count != 0) {
      $ids = array();
      $obm_q->seek($off);
      while($obm_q->next_record() && $i < $limit) {
        $ids[] = $obm_q->f('contact_id');
        $i++;
      }
      $ids = array_unique($ids);
      $whereq = " WHERE contact_id IN (".implode(',',$ids).")";
      $query = "$select $left_join $whereq $orderq";
      display_debug_msg($query, $cdg_sql, 'contact_search(2)');
      $obm_q->xquery($query);
    }
  }
  // if fuzzy or unsuccessful search (meaningful only if name has been entered)
  if ( ( ($fuzzy) || ($count == 0) ) && ($lastname != '') ) {
    $auto_aka = format_name($lastname, 0, true, true);
    $sound = phonetic_key($lastname);
    $where_name = " AND ($where_name OR contact_aka #LIKE '%$auto_aka%'
      OR contact_sound = '$sound')";
    $whereq = " WHERE $where $where_name";
    $query = "SELECT contact_id FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id $join_comp $whereq $multidomain $orderq";
    $obm_q->xquery($query);
    $count = $obm_q->nf();
    $obm_q->set_num_rows_total($count);    
    if ($count != 0) {
      $ids = array();
      $obm_q->seek($off);
      while($obm_q->next_record() && $i < $limit) {
        $ids[] = $obm_q->f('contact_id');
        $i++;
      }
      $whereq = " WHERE contact_id IN (".implode(',',$ids).")";
      $query = "$select $left_join $whereq $orderq";
      display_debug_msg($query, $cdg_sql, 'contact_search(2)');
      $obm_q->xquery($query);
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
  $birthday = sql_date_format($db_type, 'bd.event_date', 'contact_birthday');
  $anniversary = sql_date_format($db_type, 'an.event_date', 'contact_anniversary');
  $multidomain = sql_multidomain('contact');

  if ($cgp_show['module']['company']) {
    $join_comp = 'LEFT JOIN Company ON contact_company_id=company_id';
    $company_fields = "company_id,company_name";
  } else {
    $company_fields = 'contact_company as company_name';
  }

  $query = "SELECT Contact.*,
    $timeupdate,
    $timecreate,
    $company_fields,
    kind_lang,
    kind_minilabel,
    kind_header,
    m.userobm_lastname as market_lastname,
    m.userobm_firstname as market_firstname,
    contactfunction_label,
    datasource_name,
    contact_date,
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
function run_query_contact_kind($language = null) {
  global $cdg_sql;
  $multidomain = sql_multidomain('kind');

  $languageCondition = is_null($language) ?
                       '' : 
                       ' AND kind_lang = \''.$language.'\'';

  $query = "SELECT kind_id,
    kind_minilabel,
    kind_header,
    kind_lang,
    kind_default
    FROM Kind
    WHERE 1=1
    $multidomain $languageCondition
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
  $c = OBM_Contact::create($contact, $contact['addressbook']);
  return $c->id;
}


///////////////////////////////////////////////////////////////////////////////
// Set initial rights when switching from public to private (or when inserting
// new contact)
// Parameters:
//   - $contact_id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_remove_rights($contact_id) {
  OBM_Acl::denyAll('contact', $contact_id);
}


///////////////////////////////////////////////////////////////////////////////
// Contact update query execution
// Parameters:
//   - $contact[] : Entry's values
//     keys used  : id, company_id, kind, lastname, firstname ad1, ad2, ad2, zip, town
//                : cdx, ctry, func, phone, hphone, mphone, fax, email, commonname
//                : com, com2, com3
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_update($contact) {
  global $obm, $cgp_show, $cdg_sql;
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('contact');
  $id =  $contact['contact_id'];
  $comp_old_id =  $contact['company_old_id'];
  $comp_new_id =  $contact['company_id'];

  $comp_id = $contact['company_new_id'];
  if ($comp_id < 1) {
    $comp_id = $contact['company_id'];
  }
  // In case company module not used, to avoid postgres error
  $comp_id = sql_parse_id($comp_id);
  $addressbook= sql_parse_id($contact['addressbook']);
  $dsrc = sql_parse_id($contact['datasource']);
  $company = $contact['company'];
  $kind = sql_parse_id($contact['kind']);
  $market = sql_parse_id($contact['market']);
  $lastname = $contact['lastname'];
  $firstname = $contact['firstname'];
  $commonname = $contact['commonname'];
  $mname = $contact['mname'];
  $suffix = $contact['suffix'];
  $aka = trim($contact['aka']);
  $sound = phonetic_key($lastname);
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

  // Archived contact unsubscribed from mailings
  if ($archive == '1') {
    $mailok = '0';
    $newsletter = '0';
  }
  $sql_id = sql_parse_id($id, true);
  $query = "UPDATE Contact SET
    contact_timeupdate=NOW(),
    contact_userupdate='".$obm['uid']."',
    contact_datasource_id=".$obm_q->escape($dsrc).",
    contact_addressbook_id=".$obm_q->escape($addressbook).",
    contact_company_id=".$obm_q->escape($comp_id).",
    contact_company='".$obm_q->escape($company)."',
    contact_kind_id=".$obm_q->escape($kind).",
    contact_marketingmanager_id=".$obm_q->escape($market).",
    contact_lastname='".$obm_q->escape($lastname)."',
    contact_firstname='".$obm_q->escape($firstname)."',
    contact_commonname='".$obm_q->escape($commonname)."',
    contact_middlename='".$obm_q->escape($mname)."',
    contact_suffix='".$obm_q->escape($suffix)."',
    contact_aka='".$obm_q->escape($aka)."',
    contact_sound='".$obm_q->escape($sound)."',
    contact_manager='".$obm_q->escape($manager)."',
    contact_assistant='".$obm_q->escape($assistant)."',
    contact_spouse='".$obm_q->escape($spouse)."',
    contact_category='".$obm_q->escape($category)."',
    contact_service='".$obm_q->escape($service)."',
    contact_function_id=".$obm_q->escape($func).",
    contact_title='".$obm_q->escape($title)."',
    contact_mailing_ok=".$obm_q->escape($mailok).",
    contact_newsletter=".$obm_q->escape($newsletter).",
    contact_archive=".$obm_q->escape($archive).",
    $query_date
    contact_comment='".$obm_q->escape($comment)."',
    contact_comment2='".$obm_q->escape($comment2)."',
    contact_comment3='".$obm_q->escape($comment3)."',
    contact_origin='$GLOBALS[c_origin_web]'
  WHERE contact_id $sql_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_update()');
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
  if ($old_contact->f('contact_birthday')) {
    $old_birthday = new Of_Date($old_contact->f('contact_birthday'),'GMT');
  } else {
    $old_birthday = null;
  }
  run_query_contact_birthday_update('birthday', $id, $old_contact->f('contact_usercreate'),
    $old_contact->f('contact_birthday_id'), "$firstname $lastname", $old_birthday, $birthday);

  // BEGIN anniversary support

  $anniversary = of_isodate_convert($contact['anniversary'], true);
  if (!empty($anniversary)) {
    $anniversary = new Of_Date($anniversary);
  } else {
    $anniversary = null;
  }
  if ($old_contact->f('contact_anniversary')) {
    $old_anniversary = new Of_Date($old_contact->f('contact_anniversary'),'GMT');
  } else {
    $old_anniversary = null;
  }
  run_query_contact_birthday_update('anniversary', $id, $old_contact->f('contact_usercreate'),
    $old_contact->f('contact_anniversary_id'), "$firstname $lastname", $old_anniversary, $anniversary);

  if ($retour) {
    $ret = of_userdata_query_update('contact', $id, $contact);
    $entityid = of_entity_get('contact', $id);
    run_query_contact_coords_update($entityid, $contact, $id);

    // Hook : Post
    if (function_exists('hook_post_run_query_contact_update')) {
      if (! hook_post_run_query_contact_update($contact)) {
        return false;
      }
    }
  }

  OBM_AddressBook::timestamp($contact['addressbook']);

  // Indexing Contact
  OBM_Contact::solrStore(OBM_Contact::get($id));

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
function run_query_contact_coords_update($id, $contact,$contactId=NULL) {
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
    foreach($phones as $phone) {
      if(trim($phone['number']) != '' ) {
        $phone['label'] = str_replace('_', ';', $phone['label']);
        $cpt[$phone['label']]++;
        $query = "INSERT INTO Phone (phone_entity_id, phone_number, phone_label) VALUES ($id, '$phone[number]', '$phone[label];X-OBM-Ref".$cpt[$phone['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(phone)');
        $obm_q->query($query);
      }
    }
  }

  $query = "DELETE FROM Address WHERE address_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(address)');
  $obm_q->query($query);
  if(is_array($addresses)) {
    $cpt = array();
    foreach($addresses as $address) {
      if(trim($address['country_iso3166']) == 'none') $address['country_iso3166'] = '';
      if(trim($address['country_iso3166']) == '' &&  trim($address['country']) != '') {
        $address['country_iso3166'] = OBM_Contact::getCountryIso3166($address['country']);
      }      
      if(trim($address['street']) != '' || trim($address['country_iso3166']) != ''  || trim($address['zipcode']) != ''
         || trim($address['expresspostal']) != '' || trim($address['town']) != '') {
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
          '".$obm_q->escape($address[street])."',
          '".$obm_q->escape($address[zipcode])."',
          '".$obm_q->escape($address[town])."',
          '".$obm_q->escape($address[expresspostal])."',
          '".$obm_q->escape($address[country_iso3166])."',
          '".$obm_q->escape($address[label]).";X-OBM-Ref".$cpt[$address['label']]."'
        )";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(address)');
        $obm_q->query($query);
      }
    }
  }

  $query = "DELETE FROM Website WHERE website_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(website)');
  $obm_q->query($query);

  if(is_array($websites)) {
    $cpt = array();
    foreach($websites as $website) {
      if(trim($website['url']) != '' ) {
        $website['label'] = str_replace('_', ';', $website['label']);
        $cpt[$website['label']]++;
        $query = "INSERT INTO Website (website_entity_id, website_url, website_label) VALUES ($id, '".$obm_q->escape($website[url])."', '".$obm_q->escape($website[label]).";X-OBM-Ref".$cpt[$website['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(website)');
        $obm_q->query($query);
      }
    }
  }

  $query = "DELETE FROM IM WHERE im_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(IM)');
  $obm_q->query($query);

  if(is_array($ims)) {
    $cpt = array();
    foreach($ims as $im) {
      if(trim($im['address']) != '' ) {
        $cpt[$im['protocol']]++;          
        $query = "INSERT INTO IM (im_entity_id, im_address, im_protocol, im_label) VALUES ($id, '".$obm_q->escape($im[address])."', '".$obm_q->escape($im[protocol])."', '".$obm_q->escape($im[protocol]).";X-OBM-Ref".$cpt[$im['protocol']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(IM)');
        $obm_q->query($query);
      }
    }
  }

  $query = "DELETE FROM Email WHERE email_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(Email)');
  $obm_q->query($query);
  if($obm_q->affected_rows() > 0) $updateGroup = true;
  if(is_array($emails)) {
    $cpt = array();
    foreach($emails as $email) {
      if(trim($email['address']) != '' ) {
        $email['label'] = str_replace('_', ';', $email['label']);
        $cpt[$email['label']]++;
        $query = "INSERT INTO Email (email_entity_id, email_address, email_label) VALUES ($id, '".$obm_q->escape(trim($email['address']))."', '".$obm_q->escape($email['label']).";X-OBM-Ref".$cpt[$email['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_contact_coords_update(Email)');
        $obm_q->query($query);
      }
    }
  }
  if($updateGroup && $contactId != NULL) {
    $obm_q->query("UPDATE UGroup SET group_timeupdate = NOW() WHERE group_id IN (SELECT group_id FROM _contactgroup WHERE contact_id = ".$contactId.")");
    if($obm_q->affected_rows() > 0) set_update_state();
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

  of_entity_delete('contact', $p_id);

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
//   - $lastname   : lastname
//   - $firstname   : firstname
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_check($cid, $comp_id, $lastname, $firstname) {
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
    $where_comp = "OR (contact_company_id $comp_id AND contact_lastname #LIKE '$lastname')";
    $select_comp = ', company_name';
  }

  // XXXXX check access to the contact addressbook ?
  $query = "SELECT DISTINCT contact_id,
    contact_company_id,
    contact_lastname,
    contact_firstname
    $select_comp
    FROM Contact
    $join_comp
    WHERE $where_id
    ((contact_lastname #LIKE '$lastname' AND contact_firstname #LIKE '$firstname') $where_comp)
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_contact_check()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insert a contact imported from a VCard
// Parameters:
//   - $params : form values
///////////////////////////////////////////////////////////////////////////////
function run_query_vcard_insert($params, $addressbook) {
  if ($params['vcard_tmp']) {
    $file_content = file_get_contents($params['vcard_tmp']);
  } else {
    $file_content = stream_get_contents($params['vcard_fd']);
  }
  try {
    $vcards = Vpdi::decode($file_content, Vpdi::VCARD);
  } catch (Exception $e) {
    return false;
  }
  $skipped = 0;
  $ids = array();
  foreach ($vcards as $vcard) {
    if (!$vcard->isValid()) {
      error_log("Skipping invalid VCARD: " . $vcard);
      
      $skipped++;
      continue;
    }
    
    $contact = OBM_Contact::import($vcard);
    if (isset($contact['contact_id']) && run_query_check_contact_existence($contact['contact_id'], $addressbook)) {
      $contact['addressbook']=$addressbook->id;
      if (run_query_contact_update($contact)) {
        $ids[] = $contact['contact_id'];
      }
    } else {
      $c = OBM_Contact::create($contact, $addressbook);
      $id = $c->id; 
      if ($id > 0) {
        $ids[] = $id;
      }
    }
  }
  if (count($ids) != (count($vcards) - $skipped)) {
    return false;
  }
  OBM_IndexingService::commit('contact');
  return $ids;
}


///////////////////////////////////////////////////////////////////////////////
// Check contact existence
///////////////////////////////////////////////////////////////////////////////
function run_query_check_contact_existence($contact_id, $addressbook) {
  $obm_q = new DB_OBM;
  $obm_q->query("SELECT 1 FROM Contact WHERE contact_id='{$contact_id}' and contact_addressbook_id = $addressbook->id ");
  return $obm_q->next_record() !== false;
}


///////////////////////////////////////////////////////////////////////////////
// Contact environment checking (same contacts exists ?)
// Parameters:
//   - $cid       : contact id
//   - $contact[] : contact's values
//     keys used  : lastname, firstname
// Returns:
//   - Contact Database object with list of similar contacts
///////////////////////////////////////////////////////////////////////////////
function check_contact_context($cid, $contact) {
  global $cgp_show;

  $lastname = $contact['lastname'];
  $firstname = $contact['firstname'];
  $comp_new_id = $contact['company_new_id'];
  $comp_id = $contact['company_id'];
  $comp = ($comp_new_id != '') ? $comp_new_id : $comp_id;

  // return contacts with same lastname and firstname or
  // within the same company with same lastname
  $co_q = run_query_contact_check($cid, $comp, $lastname, $firstname);

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

    // XXXXX Check addressbook write_access ?
    $contact = OBM_Contact::get($id);
    $addressbook = OBM_AddressBook::get($contact->addressbook_id);
    if (!$addressbook || !$addressbook->write) {
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
//     keys used  : lastname, firstname, phone, hphone, mphone, fax, email
//                  company_id, comp_new_id
///////////////////////////////////////////////////////////////////////////////
function check_contact_data_form($cid, &$contact) {
  global $perm, $obm, $cgp_show, $cright_write_admin, $action;
  global $php_regexp_phone,$php_regexp_fax,$php_regexp_email;
  global $l_fill_lastname, $l_fill_company, $l_j_check_phone;
  global $l_j_check_hphone,$l_j_check_mphone, $l_j_check_fax, $l_j_check_email;
  global $err, $l_exist_error, $l_err_public_rights, $l_err_length_express_postal;

  $user_id = $obm['uid'];

  if (!check_user_defined_rules('contact', $contact)) {
    return false;
  }

  $lastname = $contact['lastname'];
  $firstname = $contact['firstname'];
  $c_id = $contact['company_id'];
  $c_new_id = $contact['company_new_id'];
  $phones = $contact['phones'];
  $emails = $contact['emails'];
  $addresses = $contact['addresses'];

  // MANDATORY: Contact name
  $lastname = strtoupper($lastname);
  if (trim($lastname) == '') {
    $err['msg'] = $l_fill_lastname;
    OBM_Error::getInstance()->addError('lastname', $l_fill_lastname);
  }

  // Contact Firstname
  $firstname = ucfirst($firstname);

  // Contact phone
  if(is_array($phones)) {
    foreach($phones as $i => $phone) {
      if ((trim($phone['number']) != '') && (preg_match($php_regexp_phone, trim($phone['number'])) == 0)) {
        $err['msg'] = $l_j_check_phone;
        OBM_Error::getInstance()->addError('phones['.$i.']', $err['msg']);
      }
    }
  }

  // Contact email
  if(is_array($emails)) {
    foreach($emails as $i => $email) {
      $email['address'] = stripslashes($email['address']);
      if ((trim($email['address']) != '') && (preg_match($php_regexp_email, trim($email['address'])) == 0)) {
        $err['msg'] = $l_j_check_email;
        OBM_Error::getInstance()->addError('emails['.$i.']', $err['msg']);
      }  
    }
  }

  // Check if express postal is not too long
  if (is_array($addresses)){
    foreach ($addresses as $i => $addresse){
      $express_postal = $addresse['expresspostal'];
      if(strlen($express_postal)>16){
        $err['msg'] = $l_err_length_express_postal;
        OBM_Error::getInstance()->addError('addresses['.$i.']', $err['msg']);
      }
    }
  } 
  return !OBM_Error::getInstance()->inError();
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
  return $delete_ok;
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
      '".$obm_q->escape($label)."',
      '".$obm_q->escape($header)."',
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
    kind_minilabel='".$obm_q->escape($label)."',
    kind_header='".$obm_q->escape($header)."',
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
 *                 - $contact_fullastname
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
function run_query_contact_birthday_update ($date='birthday', $contact_id, $contact_usercreate, $event_id, $contact_fullastname, $old_value, $new_value) {
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
      $title = sprintf($label, $contact_fullastname);
      $ext_id = generateUniqueKey();

      $query = "INSERT INTO Event
        (event_timeupdate,
        event_timecreate,
        event_usercreate,
        event_origin,
        event_ext_id,
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
        event_opacity,
        event_color,
        event_endrepeat,
        event_domain_id)
        VALUES
        (
         NOW(),
         NOW(),
        '$contact_usercreate',
        '$GLOBALS[c_origin_web]',
        '$ext_id',
        '$contact_usercreate',
        '".Of_Date::getOption('timezone')."',
        '".$obm_q->escape($title)."',
        '".$obm_q->escape($new_value)."',
        '',
        '',
        '',
        NULL,
        '2',
        1,
        '$duration',
        'yearly',
        '1',
        '0000000',
        '1',
        'TRANSPARENT',
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
        event_date = '".$obm_q->escape($new_value)."',
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


/**
 * Search contacts from a single field 
 * 
 * @param mixed $contact 
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_contact_ext_search($contact) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = '('.$contact['pattern']."* OR "."email:".$contact['pattern']."*) AND -is:archive ";
  if($contact["withCalendar"]){
    $pattern .= " AND hasACalendar:true";
  }
  if($contact["fromMyContacts"]){
    $pattern .= " AND -in:collected_contacts AND -in:public_contacts";
  }
  $addressbook_id = $contact['addressbook_id'];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('contact');
  if (is_null($addressbook_id)) {
    $addressbooks = OBM_AddressBook::search();
    $contacts = $addressbooks->searchContacts($pattern);
  } else {
    $addressbooks = OBM_AddressBook::get($contact['addressbook_id']);
    $contacts = $addressbooks->getContacts($pattern);
  }

  return $contacts;
}


/**
 * Check if a contact (its addressbook) is synchronized
 * @param Integer $ab_id addressbook_id
 * @return true or false
 **/
// XXXX Try to check only once for an addressbook
function get_contact_is_synced($ab_id) {
  global $cdg_sql, $obm;

  $uid = sql_parse_id($obm['uid'], true);
  $cid = sql_parse_id($cid, true);
  $query = "SELECT addressbook_id FROM SyncedAddressbook
        WHERE user_id $uid
        AND addressbook_id $ab_id";

  display_debug_msg($query, $cdg_sql, 'get_contact_is_synced()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q->next_record();
}

function run_query_contact_get_datasources() {
  $datasources = array();
  $dsrc_q = run_query_global_datasource();
  while($dsrc_q->next_record()) {
    $datasources[$dsrc_q->f('datasource_id')] = $dsrc_q->f('datasource_name');
  }
  return $datasources;
}

function run_query_contact_get_functions() {
  $functions = array();
  $funcs = of_category_get_ordered('contact', 'function');
  foreach($funcs as $function_data) {
    $functions[$function_data['id']]  = $function_data['label'];
  }
  return $functions;
}

function run_query_contact_get_markets($id) {
  $markets = array();
  $uids = array($GLOBALS['obm']['uid']);
  if($id) $uids[] = $id;
  $users = of_usergroup_get_group_users($GLOBALS['obm']['group_com'], true, $uids, 'userobm_lastname');
  foreach($users as $user) {
    $markets[$user['id']] = $user['lastname'].' '.$user['firstname'];
  }
  return $markets;
}

///////////////////////////////////////////////////////////////////////////////
// Contact: Kind select query execution
// Returns : DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_get_kinds($language = null) {
  $db = run_query_contact_kind($language);
  $kinds = array();
  while($db->next_record()) {
    $kinds[$db->f('kind_id')] = $db->f('kind_header');
  }
  return $kinds;
}
///////////////////////////////////////////////////////////////////////////////
// Synchronise or desyncronise a user from a contact 
// Parameters:
//   -    $cid : Contact id
//   - $desync : Boolean, true if we sync, false if we desync
///////////////////////////////////////////////////////////////////////////////
function run_query_do_addressbook_sync($ab_id, $sync = true) {
  global $cdg_sql, $obm;

  $uid = sql_parse_id($obm['uid']);
  $ab_id = sql_parse_id($ab_id);

  // XXXXX change contact to addressbook
  if (get_contact_is_synced($ab_id) XOR !$sync) {

  }

  if ($sync) {
    $query = "INSERT INTO SyncedAddressbook (
        user_id, 
        addressbook_id,
        timestamp)
        VALUES ($uid, $ab_id, NOW())";
  } else {
    $query = "DELETE FROM SyncedAddressbook
        WHERE user_id = $uid
        AND addressbook_id = $ab_id";
  }

  display_debug_msg($query, $cdg_sql, "run_query_do_addressbook_sync($ab_id, $sync)");
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
}

?>
