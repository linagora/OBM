<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_query.inc                                               //
//     - Desc : list query File                                              //
// 2000-06-07 Vincent MARGUERIT                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//NOTA:    $connect_db->free() DOES NOT WORK WITH PGSQL

///////////////////////////////////////////////////////////////////////////////
// List Search query execution 
// Parameters :
//   - $list[]       : list search criteria
//     keys used     : name, contact
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($list, $p_new_order, $p_order_dir) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;
  
  $name = $list["name"];
  $email = $list["email"];
  $contact = $list["contact"];

  $obm_q = new DB_OBM;

  if ($obm_q->type == $db_type_mysql) {
    if ($contact != '') {
      $where .= " and contact_lastname like '$contact%'";
    }
    if ($email != '') {
      $where .= " and list_email like '%$email%'";
    }

    $query = "select distinct List.*, list_id as Id,
      UNIX_TIMESTAMP(List.list_timecreate) as timecreate,
      UNIX_TIMESTAMP(List.list_timeupdate) as timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from 
      (List LEFT JOIN ContactList ON list_id=ContactList_listid)
      LEFT JOIN Contact ON ContactList.ContactList_contactid=Contact.contact_id
      LEFT JOIN UserObm as A ON List.list_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON List.list_userupdate=B.userobm_id
    where list_name like '$name%'
      $where";
  }

  // Postgres doesn't support Left join until 7.1 !
  else if ($obm_q->type == $db_type_pgsql) {
    if ($email != '') {
      $where .= " and list_email like '%$email%'";
    }
    $query = "select distinct List.*, list_id as Id,
                List.list_timecreate as timecreate,
                List.list_timeupdate as timeupdate
              from List, ContactList, Contact
              where list_name like '$name%' $where";

    if ($contact != "") {
      $query .= " or (contact_lastname like '$contact%' and ContactList_contactid in (select contact_id from Contact where contact_lastname like '$contact%') and List.list_id = ContactList_listid)";
    }
  }

  // ORDER construction

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "list_name";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deal detail query execution                                               //
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  if ($obm_q->type == $db_type_mysql) {
    $query = "select *,
      UNIX_TIMESTAMP(List.list_timecreate) as timecreate,
      UNIX_TIMESTAMP(List.list_timeupdate) as timeupdate
    from List where list_id = '$id'";
  } else if ($obm_q->type == $db_type_pgsql) {
    $query = "select List.* from List where list_id = '$id'";
  }
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : list insertion                                          //
// Parameters:
//   - $list[] : deal hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];

  $query = "insert into List (list_timeupdate,
    list_timecreate,
    list_userupdate,
    list_usercreate,
    list_name,
    list_subject,
    list_email)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$name',
    '$subject',
    '$email')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// List Update query execution                                               //
// Parameters:
//   - $list[] : list hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];
  
  $query = "update List set list_timeupdate='". date("Y-m-d H:i:s")."',
    list_userupdate='".$auth->auth["uid"]."',
    list_name='$name',
    list_subject='$subject',
    list_email='$email'
  where list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete all contacts registration to this list
  $query = "delete from ContactList where ContactList_listid='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete the List
  $query = "delete from List where list_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList conditionnal insertion                      //
// Parameters:
//   - $list[] : deal hash info : keys used : id, con_nb, conX
// Return: number of contact inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_contactlist_insert($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $list["con_nb"]) {
    $cpt++;
    $con_id = $list["con$cpt"];

    $query = "select * from ContactList
      where ContactList_listid='$id'
        and ContactList_contactid='$con_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into ContactList (ContactList_listid,
        ContactList_contactid) values ($id, $con_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList deletion                                    //
// Parameters:
//   - $list[] : deal hash info : keys used : id, con_nb, conX
///////////////////////////////////////////////////////////////////////////////
function run_query_contactlist_delete($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $list["con_nb"]) {
    $cpt++;
    $con_id = $list["con$cpt"];

    $query = "delete from ContactList
      where ContactList_listid='$id' and ContactList_contactid='$con_id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the contacts of the given list                      //
// Parameters:
//   - $list_id   : List id
// -- Optionnals
//   - $new_order : field for the older clause
//   - $order_dir : direction for the older clause
///////////////////////////////////////////////////////////////////////////////
function run_query_contacts_list($list_id, $new_order="", $order_dir="") {
  global $cdg_sql;

  if (trim($new_order) != "") {
    $order = "order by $new_order $order_dir";
  } else {
    $order = "order by contact_lastname";
  }

  $query = "select ContactList_contactid as list_contact_id,
         ContactList_contactid as Id,
         contact_timeupdate as list_contact_timeupdate,
         contact_timecreate as list_contact_timecreate,
         contact_userupdate as list_contact_userupdate,
         contact_usercreate as list_contact_usercreate,
         contact_company_id as list_contact_company_id,
         contact_kind_id as list_contact_kind_id,
         contact_lastname as list_contact_lastname,
         contact_firstname as list_contact_firstname,
         contact_address1 as list_contact_address1,
         contact_address2 as list_contact_address2,
         contact_zipcode as list_contact_zipcode,
         contact_town as list_contact_town,
         contact_expresspostal as list_contact_expresspostal,
         contact_country as list_contact_country,
         contact_function as list_contact_function,
         contact_phone as list_contact_phone,
         contact_homephone as list_contact_homephone,
         contact_mobilephone as list_contact_mobilephone,
         contact_fax as list_contact_fax,
         contact_email as list_contact_email,
         contact_comment as list_contact_comment,
         contact_visibility as list_contact_visibility,
         company_name as list_contact_company
      from (ContactList LEFT JOIN Contact ON ContactList_contactid=contact_id)
         LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
      where ContactList_listid='$list_id'
         $order";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered contact in the list specified
// Parameters:
//   - $l_id    : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_nb_contact($l_id) {
  global $cdg_sql;

  $query = "select count(*) from ContactList where ContactList_listid='$l_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Return the lists which matches the name or the subject
// except the one given (update mode)
// Parameters:
//   - $l_id    : list id
//   - $name    : list name
//   - $subject : list subject
///////////////////////////////////////////////////////////////////////////////
function run_query_check_list($l_id, $name, $subject) {
  global $cdg_sql;

  // If name is short, we test equality, else similarity
  if (strlen($name)  > 2) {
    $wname = "list_name like '%$name%'";
  } else {
    $wname = "list_name = '$name'";
  }

  // If subject is short, we test equality, else similarity
  if (strlen($subject)  > 2) {
    $wsubject = "list_subject like '%$subject%'";
  } else {
    $wsubject = "list_subject = '$subject'";
  }

  $query = "select distinct list_id, list_name, list_subject
     from List
     where list_id!='$l_id'
       and ($wname or $wsubject)";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// List context checking (same lists exists ?)
// Parameters:
//   - $l_id     : list id
//   - $list[]   : list values
//     keys used : name, subject, email
// Returns:
//   - List Database object with list of similar lists
///////////////////////////////////////////////////////////////////////////////
function check_list_context($l_id, $list) {
  global $cdg_sql;

  $name = $list["name"];
  $subject = $list["subject"];

  // return the lists with same name or subject
  $l_q = run_query_check_list($l_id, $name, $subject);

  return $l_q;
}


///////////////////////////////////////////////////////////////////////////////
// List Form Data checking and formatting
// Parameters:
//   - $l_id     : list id  (empty on insertion)
//   - $list[]   : values checked
//     keys used : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function check_data_form($l_id, $list) {
  global $php_regexp_email, $l_fill_name;
  global $err_msg, $l_j_check_email;

  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];

  // MANDATORY: List name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // List email
  if (($email != "") && (eregi($php_regexp_email, $email) == false)) {
    $err_msg = " $email : $l_j_check_email";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Name of a List from its Id                                        //
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_name($id) {
  global $cdg_sql;

  $query = "select list_name from List where list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("list_name");
  return $retour;
}





///////////////////////////////////////////////////////////////////////////////
// Query Execution : Distincts contacts from several lists for export        //
// Parameters:
//   - $list_ids : comma separated list of list id
// Returns:
//   - database result set
///////////////////////////////////////////////////////////////////////////////
function run_query_contacts_for_letter($list_ids) {
  global $db_type_mysql,$db_type_pgsql, $auth, $cdg_sql;

  $connect_db = new DB_OBM;
  if($list_ids != "") {
    $query = "select DISTINCT contact_id as list_contact_id,
                 contact_timeupdate as list_contact_timeupdate,
                 contact_timecreate as list_contact_timecreate,
                 contact_userupdate as list_contact_userupdate,
                 contact_usercreate as list_contact_usercreate,
                 contact_company_id as list_contact_company_id,
                 contact_lastname as list_contact_lastname,
                 contact_firstname as list_contact_firstname,
                 contact_address1 as list_contact_address1,
                 contact_address2 as list_contact_address2,
                 contact_zipcode as list_contact_zipcode,
                 contact_town as list_contact_town,
                 contact_expresspostal as list_contact_expresspostal,
                 contact_country as list_contact_country,
                 contact_function as list_contact_function,
                 contact_phone as list_contact_phone,
                 contact_homephone as list_contact_homephone,
                 contact_mobilephone as list_contact_mobilephone,
                 contact_fax as list_contact_fax,
                 contact_email as list_contact_email,
                 contact_comment as list_contact_comment,
                 contact_visibility as list_contact_visibility,
                 company_name as contact_company_name,
                 company_address1 as contact_company_address1,
                 company_address2 as contact_company_address2,
                 company_zipcode as contact_company_zipcode,
                 company_town as contact_company_town,
                 company_expresspostal as contact_company_expresspostal,
                 company_country as contact_company_country,
                 kind_label as list_contact_kind
              from Contact, Company, ContactList, Kind
              where contactlist_listid in (";
    $query .= $list_ids;
    $query .= ") and contactlist_contactid = contact_id and company_id = contact_company_id and contact_kind_id = kind_id";
    $query .= " order by ";
    $query .= (strcmp($options_order,"") != 0) ? $options_order : "contact_lastname";
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query);
  }
  return $connect_db;
}


</script>
