<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contact_query.inc                                            //
//     - Desc : contact query File                                           //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Contact search query execution                                            //
// Parametes:
//   - $contact[]    : contact search criteria
//     keys used       company_id, name, phone, company
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($contact, $p_new_order, $p_order_dir) {
  global $auth, $cdg_sql;

  $company_id = $contact["company_id"];
  $lname = $contact["lname"];
  $phone = stripslashes($contact["phone"]);
  $email = $contact["email"];
  $company = $contact["company"];
  $archive = $contact["archive"];

  $query = "select
      contact_id as Id,
      contact_id,
      contact_usercreate,
      contact_company_id,
      contact_kind_id,
      contact_lastname,
      contact_firstname,
      contact_function,
      contact_phone,
      contact_homephone,
      contact_mobilephone,
      contact_email,
      contact_archive,
      contact_visibility,
      company_name as contact_company_name, company_phone
    from Contact, Company
    where";

  // If a contact indication has been specified, get it 
  if ($lname != "") {
    $query .= " contact_lastname like '$lname%' and";
  }
  // If a phone number hes been specified, get it 
  if ($phone != "") {
    $query .= " (contact_phone like '$phone%' or
                 contact_homephone like '$phone%' or
                 contact_mobilephone like '$phone%') and";
  }
  // If an email indication has been specified, get it 
  if ($email != "") {
    $query .= " contact_email like '$email%' and";
  }
  // If a company name indication has been specified (without id) get it 
  if (($company != "") && ($company_id == "")) {
    $query .= " company_name like '$company%' and";
  }
  // If a company was sent as parameter, get it
  if ($company_id != "") {
    $query .= " company_id='$company_id' and";
  }
  // Get only not archived by default
  if ($archive != "1") {
    $query .= " contact_archive=0 and";
  }

  // only the one which are allowed (ie. publics )
  $query .= " (contact_visibility='0' or contact_usercreate='".$auth->auth["uid"]."') and ";
  
  // join
  $query .= " contact_company_id=company_id";

  // order
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "contact_lastname";
  $query .= " order by $order $p_order_dir";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}



///////////////////////////////////////////////////////////////////////////////
// Contact Company query execution                                           //
// Parameters:
//   - $c_id : company id
// Returns : DB result object with the company's name and id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_company($c_id) {
  global $cdg_sql;

  $query = "select company_id, company_name, company_phone from Company where company_id='$c_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind select query execution                                      //
// Returns : DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_kind() {
  global $cdg_sql;

  $query = "select kind_id, kind_minilabel, kind_label from Kind";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact detail query execution                                            //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q= new DB_OBM;
  if ($obm_q->type == $db_type_mysql) {
    $query = "select Contact.*,
        UNIX_TIMESTAMP(contact_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(contact_timecreate) as timecreate,
        company_id, company_name, company_phone,
        kind_minilabel
      from
        Contact left join Kind on kind_id=contact_kind_id,
        Company
      where
        contact_id='$p_id'
        and company_id=contact_company_id";

  } else if ($obm_q->type == $db_type_pgsql) {
    $query = "select Contact.*,
                contact_timeupdate as timeupdate,
                contact_timecreate as timecreate,
                company_id, company_name, company_phone,
                kind_minilabel
              from
                Contact left join Kind on kind_id=contact_kind_id,
                Company
              where
                contact_id='$p_id'
                and company_id=contact_company_id";
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Insertion query execution                                         //
// Parameters:
//   - $contact[] : Entry's values
//     keys used  : company_id, kind, lname, fname ad1, ad2, zip, town, cdx
//                : ctry, func, phone, hphone, mphone, fax, email, com, vis
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($contact) {
  global $auth, $cdg_sql;

  $comp_id = $contact["comp_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }

  $kind = $contact["kind"];
  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $ad1 = $contact["ad1"];
  $ad2 = $contact["ad2"];
  $zip = $contact["zip"];
  $town = $contact["town"];
  $cdx = $contact["cdx"];
  $ctry = $contact["ctry"];
  $func = $contact["func"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = $contact["email"];
  $com = $contact["com"];
  $arch = $contact["archive"];
  $vis = $contact["vis"];

  $query = "insert into Contact (contact_timeupdate,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
    contact_company_id,
    contact_kind_id,
    contact_lastname,
    contact_firstname,
    contact_address1,
    contact_address2,
    contact_zipcode,
    contact_town,
    contact_expresspostal,
    contact_country,
    contact_function,
    contact_phone,
    contact_homephone,
    contact_mobilephone,
    contact_fax,
    contact_email,
    contact_archive,
    contact_visibility,
    contact_comment)
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '" . $auth->auth["uid"] . "',
    '$comp_id',
    '$kind',
    '$lname',
    '$fname',
    '$ad1',
    '$ad2',
    '$zip',
    '$town',
    '$cdx',
    '$ctry',
    '$func',
    '$phone',
    '$hphone',
    '$mphone',
    '$fax',
    '$email',
    '$arch',
    '$vis',
    '$com')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    run_query_company_contact_inc($comp_id);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact update query execution                                            //
// Parameters:
//   - $contact[] : Entry's values
//     keys used  : id, company_id, kind, lname, fname ad1, ad2, zip, town, cdx
//                : ctry, func, phone, hphone, mphone, fax, email, com, vis
///////////////////////////////////////////////////////////////////////////////
function run_query_update($contact) {
  global $auth, $cdg_sql;

  $id = $contact["id"];
  $comp_old_id = $contact["company_id"];
  $comp_new_id = $contact["comp_new_id"];

  $comp_id = $contact["comp_new_id"];
  if ($comp_id < 1) {
    $comp_id = $contact["company_id"];
  }
  $kind = $contact["kind"];
  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $ad1 = $contact["ad1"];
  $ad2 = $contact["ad2"];
  $zip = $contact["zip"];
  $town = $contact["town"];
  $cdx = $contact["cdx"];
  $ctry = $contact["ctry"];
  $func = $contact["func"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = $contact["email"];
  $com = $contact["com"];
  $arch = $contact["archive"];
  $vis = $contact["vis"];

  $query = "update Contact set
    contact_timeupdate='".date("Y-m-d H:i:s")."',
    contact_userupdate='".$auth->auth["uid"]."',
    contact_company_id='$comp_id',
    contact_kind_id='$kind',
    contact_lastname='$lname',
    contact_firstname='$fname',
    contact_address1='$ad1',
    contact_address2='$ad2',
    contact_zipcode='$zip',
    contact_town='$town',
    contact_expresspostal='$cdx',
    contact_country='$ctry',
    contact_function='$func',
    contact_phone='$phone',
    contact_homephone='$hphone',
    contact_mobilephone='$mphone',
    contact_fax='$fax',
    contact_email='$email',
    contact_archive='$arch',
    contact_visibility='$vis',
    contact_comment='$com'
  where contact_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If company has changed, update the companies contact number
  if (($retour) && ($comp_new_id > 0) && ($comp_new_id != $comp_old_id)) {
    run_query_company_contact_inc($comp_new_id);
    run_query_company_contact_dec($comp_old_id);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the deals attached to the contact
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_deal_links($p_id) {
  global $cdg_sql;

  $query = "select distinct deal_id,deal_label 
            from Deal,Contact
            where  deal_contact1_id='$p_id'
              or deal_contact2_id='$p_id'
              or deal_marketingmanager_id='$p_id'
              or deal_technicalmanager_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the lists where the contact is registered
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_list_links($p_id) {
  global $cdg_sql;

  $query = "select distinct list_id, list_name, ContactList_contactid, ContactList_listid
            from List, ContactList
            where list_id=ContactList_listid
              and ContactList_contactid='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Deletion query execution                                          //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  $comp_id = get_contact_company_id($p_id);

  $query = "delete from Contact where contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    run_query_company_contact_dec($comp_id);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company contact number increment                        //
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contact_inc($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_contact_number=company_contact_number+1
              where company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company contact number decrement                        //
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contact_dec($comp_id) {
  global $cdg_sql;

  $query = "update Company
              set company_contact_number=company_contact_number-1
              where company_id='$comp_id'";

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

  $query = "select distinct contact_id, contact_company_id, contact_lastname, contact_firstname, company_name 
      from Contact, Company
      where contact_company_id=company_id
        and contact_id!='$cid'
        and ((contact_lastname='$lname' and contact_firstname='$fname')
          or (contact_company_id='$comp_id' and contact_lastname='$lname'))";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact environment checking (same contacts exists ?)                     //
// Parameters:
//   - $cid       : contact id
//   - $contact[] : contact's values
//     keys used  : lname, fname
// Returns:
//   - Contact Database object with list of similar contacts
///////////////////////////////////////////////////////////////////////////////
function check_contact_context($cid, $contact) {

  $comp_id = $contact["company_id"];
  $lname = $contact["lname"];
  $fname = $contact["fname"];

  // return contacts with same lastname and firstname or
  // within the same company with same lname
  $co_q = run_query_check_contact($cid, $comp_id, $lname, $fname);

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
function check_data_form($cid, $contact) {
  global $php_regexp_phone,$php_regexp_fax,$php_regexp_email;
  global $l_fill_lastname, $l_fill_company, $l_j_check_phone;
  global $l_j_check_hphone,$l_j_check_mphone, $l_j_check_fax, $l_j_check_email;
  global $err_msg, $l_exist_error;

  $lname = $contact["lname"];
  $fname = $contact["fname"];
  $phone = $contact["phone"];
  $hphone = $contact["hphone"];
  $mphone = $contact["mphone"];
  $fax = $contact["fax"];
  $email = $contact["email"];
  $c_id = $contact["company_id"];
  $c_new_id = $contact["comp_new_id"];

  // MANDATORY: Contact name
  $lname = strtoupper($lname);
  if (trim($lname) == "") {
    $err_msg = $l_fill_lastname;
    return false;
  }

  // MANDATORY: a company must be set
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err_msg = $l_fill_company;
    return false;
  }

  // Contact Firstname
  $fname = ucfirst($fname);

  // Contact phone
  if (($phone != "") && (ereg($php_regexp_phone, $phone) == false)) {
    $err_msg = $l_j_check_phone . " : $phone";
    return false;
  }

  // Contact mobile or personal phone
  if (($hphone != "") && (ereg($php_regexp_phone, $hphone) == false)) {
    $err_msg = $l_j_check_hphone . " : $hphone";
    return false;
  }
  if (($mphone != "") && (ereg($php_regexp_phone, $mphone) == false)) {
    $err_msg = $l_j_check_mphone . " : $mphone";
    return false;
  }
  // Contact fax
  if (($fax != "") && (ereg($php_regexp_fax, $fax) == false)) {
    $err_msg = $l_j_check_fax . " : $fax";
    return false;
  }

  // Contact email
  if (($email != "") && (eregi($php_regexp_email, $email) == false)) {
    $err_msg = $l_j_check_email . " : $email";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact's company id                                              //
// Parameters:
//   - $con_id : contact id
// Returns: company id
///////////////////////////////////////////////////////////////////////////////
function get_contact_company_id($con_id) {
  global $cdg_sql;

  $query = "select contact_company_id from Contact where contact_id='$con_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $retour = $obm_q->f("contact_company_id");

  return $retour;
}


</SCRIPT>

