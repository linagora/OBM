<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_query.inc                                               //
//     - Desc : list query File                                              //
// 2000-06-07 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// List Search query execution 
// Parameters :
//   - $list[]       : list search criteria
//     keys used     : name, contact
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($list, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  
  $name = sql_search_text_parse($list["name"]);
  $email = sql_search_text_parse($list["email"]);
  $contact = sql_search_text_parse($list["contact"]);
  $market = $list["marketing_manager"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type, "list_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "list_timecreate", "timecreate");

  $where .= sql_obm_entity_privacy("list");

  if ($name != '') {
    $where .= " and list_name $like '$name%'";
  }
  if ($contact != '') {
    $where .= " and contact_lastname $like '$contact%'";
    $join_con = "LEFT JOIN ContactList ON list_id=contactlist_list_id
    LEFT JOIN Contact ON contactlist_contact_id=contact_id";
  }
  if ($email != '') {
    $where .= " and list_email $like '%$email%'";
  }
  if (($market != $c_all) && ($market != "")) { 
    $where .= " and (list_usercreate='$market')";
  }

  $whereq = "where $where";

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "list_name";

  $orderq .= " order by $order $p_order_dir $order_next";

  $query = "select distinct List.*, list_id as id,
    $timecreate,
    $timeupdate,
    A.userobm_login as usercreate,
    B.userobm_login as userupdate
  from List
    LEFT JOIN ContactList ON list_id=contactlist_list_id
    LEFT JOIN Contact ON ContactList.contactlist_contact_id=Contact.contact_id
    LEFT JOIN UserObm as A ON List.list_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON List.list_userupdate=B.userobm_id
  $whereq
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from List $join_con $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "list_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// List detail query execution
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "list_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "list_timecreate", "timecreate");

  $query = "select *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
  from List
       LEFT JOIN UserObm as c on list_usercreate=c.userobm_id
       LEFT JOIN UserObm as u on list_userupdate=u.userobm_id
  where list_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Stripslashes of all elements of an array (walk_array_recursive() only PHP5)
// Parameters:
//   - $item : array or value
//   - $key  : element key
///////////////////////////////////////////////////////////////////////////////
function stripslashes_array(&$item, $key) {

  if (is_array($item)) {
    array_walk($item, 'stripslashes_array');
  } else if ($item != "") {
    $item = stripslashes($item);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Addslashes of all elements of an array (walk_array_recursive() only PHP5)
// Parameters:
//   - $item : array or value
//   - $key  : element key
///////////////////////////////////////////////////////////////////////////////
function addslashes_array(&$item, $key) {

  if (is_array($item)) {
    array_walk($item, 'addslashes_array');
  } else if ($item != "") {
    $item = addslashes($item);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : list insertion
// Parameters:
//   - $list[] : list hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($list) {
  global $auth, $cdg_sql;

  $priv = ($list["priv"]==1) ? 1 : 0;
  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];
  $list_query = addslashes($list["query"]);
  $mailing_ok = ($list["mailing_ok"]==1)?1:0;
  $contact_arch = ($list["contact_arch"]==1)?1:0;
  $info_pub = ($list["info_pub"]==1)?1:0;
  if (isset($list["criteria"])) {
    $ss_criteria = $list["criteria"];
    array_walk($ss_criteria, 'stripslashes_array');
    $list_critery = addslashes(serialize($ss_criteria));
  }
  $query = "insert into List (list_timeupdate,
    list_timecreate,
    list_userupdate,
    list_usercreate,
    list_privacy,
    list_name,
    list_subject,
    list_email,
    list_mailing_ok,
    list_contact_archive,
    list_info_publication,
    list_query,
    list_structure)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$priv',
    '$name',
    '$subject',
    '$email',
    '$mailing_ok',
    '$contact_arch',
    '$info_pub',
    '$list_query',
    '$list_critery')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $id = get_list_id($list);
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list id from the list Infos
// Parameters:
//   - $list : list infos
///////////////////////////////////////////////////////////////////////////////
function get_list_id($list) {
  global $cdg_sql;

  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];
  $list_query = addslashes($list["query"]);

  $query = "select list_id
    from List
    where list_name='$name'
      and list_subject='$subject'
      and list_email='$email'
      and list_query='$list_query'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res = $obm_q->f("list_id");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// List Update query execution
// Parameters:
//   - $list[] : list hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $priv = ($list["priv"]==1) ? 1 : 0;
  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];
  $list_query = addslashes($list["query"]);
  $mailing_ok = ($list["mailing_ok"]==1)?1:0;
  $contact_arch = ($list["contact_arch"]==1)?1:0;
  $info_pub = ($list["info_pub"]==1)?1:0;
  if (isset($list["criteria"])) {
    $ss_criteria = $list["criteria"];
    array_walk($ss_criteria, 'stripslashes_array');
    $list_critery = addslashes(serialize($ss_criteria));
  }
  $query = "update List set
    list_timeupdate='". date("Y-m-d H:i:s")."',
    list_userupdate='".$auth->auth["uid"]."',
    list_privacy='$priv',
    list_name='$name',
    list_subject='$subject',
    list_email='$email',
    list_mailing_ok='$mailing_ok',
    list_contact_archive='$contact_arch',
    list_info_publication='$info_pub',
    list_query='$list_query',
    list_structure='$list_critery'
  where list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update the SQL for a List
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update_sql($id) {
  global $auth, $cdg_sql;

  // Check that list has really a query (if not we do nothing)
  $query = "select list_structure from List where list_id='$id'";
  display_debug_msg($query, $cdg_sql);
  $list_q = new DB_OBM;
  $retour = $list_q->query($query);
  $list_q->next_record();

  $list_query = addslashes(make_query_from_db($list_q));

  if ($list_query != "") {

    $query = "update List set
      list_timeupdate='". date("Y-m-d H:i:s")."',
      list_userupdate='".$auth->auth["uid"]."',
      list_query='$list_query'
    where list_id='$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete all contacts registration to this list
  $query = "delete from ContactList where contactlist_list_id='$p_id'";
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
// Query execution : ContactList conditionnal insertion
// Parameters:
//   - $list[] : list hash info : keys used : id, con_nb, conX
// Return: number of contact inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_contactlist_insert($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];

  // We check the list still exist (in case popup windows here for long...)
  $query = "select list_id from List where list_id = '$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() != 1) {
    return -1;
  }

  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $list["con_nb"]) {
    $cpt++;
    $con_id = $list["con$cpt"];

    $query = "select * from ContactList
      where contactlist_list_id='$id'
        and contactlist_contact_id='$con_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into ContactList (contactlist_list_id,
        contactlist_contact_id) values ($id, $con_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);

      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList deletion
// Parameters:
//   - $list[] : list hash info : keys used : id, con_nb, conX
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
      where contactlist_list_id='$id' and contactlist_contact_id='$con_id'";
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
// Query Execution : Get the static contacts of the given list
// Parameters:
//   - $list   : List hash
// -- Optionnals
//   - $entity : dataset entity to order (to handle multiple Display in 1 p)
///////////////////////////////////////////////////////////////////////////////
function run_query_contacts_list($list, $entity="") {
  global $cdg_sql;

  $id = $list["id"];
  $new_order = $list["new_order"];
  $order_dir = $list["order_dir"];
  $lang = get_lang();

  // Order exceptions (order on calculated rows)
  // Addresses are taken from contact or from company (if empty for contact)

  if ( (strcmp($new_order,"address")==0) ) {
    $order = "contact_address1";
    $order_next = ", contact_address2, contact_address3, contact_zipcode, contact_town, company_address1, company_address2, company_address3, company_zipcode, company_town";
  } else if (strcmp($new_order,"address1")==0) {
    $order = "contact_address1";
    $order_next = ", company_address1";
  } else if (strcmp($new_order,"address2")==0) {
    $order = "contact_address2";
    $order_next = ", company_address2";
  } else if (strcmp($new_order,"address3")==0) {
    $order = "contact_address3";
    $order_next = ", company_address3";
  } else if (strcmp($new_order,"zipcode")==0) {
    $order = "contact_zipcode";
    $order_next = ", company_zipcode";
  } else if (strcmp($new_order,"town")==0) {
    $order = "contact_town";
    $order_next = ", company_town";
  } else if (trim($new_order) == "") {
    $order = "contact_lastname";
  } else {
    $order = $new_order;
  }

  if ($entity != "list_contact") {
    $order = "contact_lastname";
  }

  $orderq = "order by $order $order_dir $order_next";

  $query = "select contactlist_contact_id as contact_id,
         contactlist_contact_id as id,
         contact_timeupdate,
         contact_timecreate,
         contact_userupdate,
         contact_usercreate,
         contact_company_id,
         contact_kind_id,
         kind_minilabel,
         kind_header,
         kind_lang,
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
         ctry1.country_name as country_name,
         contact_function_id,
         function_label,
         contact_title,
         contact_phone,
         contact_homephone,
         contact_mobilephone,
         contact_fax,
         contact_email,
         contact_comment,
         contact_privacy,
         company_id,
         company_name,
         company_address1,
         company_address2,
         company_address3,
         company_zipcode,
         company_town,
         company_expresspostal,
         ctry2.country_name as company_country
      from ContactList
         LEFT JOIN Contact ON contactlist_contact_id=contact_id
         LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
         LEFT JOIN Country as ctry1 ON Contact.contact_country_iso3166 = ctry1.country_iso3166
                              and ctry1.country_lang='$lang'
         LEFT JOIN Country as ctry2 ON Company.company_country_iso3166 = ctry2.country_iso3166
                              and ctry2.country_lang='$lang'
         LEFT JOIN Function ON Contact.contact_function_id = function_id
         LEFT JOIN Kind ON Contact.contact_kind_id = kind_id
      where contactlist_list_id='$id'
      $orderq";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered contact in the list specified
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_static_contact_nb($id) {
  global $cdg_sql;

  $query = "select count(*) from ContactList where contactlist_list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Update the dynamic rows number for the list
// Parameters:
//   - $id : list_id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update_static_nb($id) {
  global $cdg_sql;

  if ($id == "") {
    return 0;
  }

  $nb = get_list_static_contact_nb($id);

  $query = "update List set list_static_nb='$nb' where list_id='$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a list (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : list id to exclude
// Returns:
//   - true (if a list exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_list_name_exists($name, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "and list_id != '$id'";
  }

  $query = "select list_id, list_name
    from List
    where list_name='$name'
      $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the lists which matches the name or the subject
// except the one given (update mode)
// Parameters:
//   - $id      : list id
//   - $name    : list name
//   - $subject : list subject
///////////////////////////////////////////////////////////////////////////////
function run_query_check_list($id, $name, $subject) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "list_id != '$id' and";
  }

  // We search for a similar name (as same name is already excluded)
  $wname = "list_name $like '%$name%'";

  // If subject is short, we test equality, else similarity
  if (strlen($subject)  > 2) {
    $wsubject = "list_subject $like '%$subject%'";
  } else {
    $wsubject = "list_subject = '$subject'";
  }

  $query = "select distinct list_id, list_name, list_subject
     from List
     where $where_id
       ($wname or $wsubject)";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Update the dynamic rows number for the list
// Parameters:
//   - $id : list_id
//   - $nb : new rows number
///////////////////////////////////////////////////////////////////////////////
function run_query_update_num_rows($id, $nb) {
  global $cdg_sql;

  if ($id == "") {
    return 0;
  }
  if ($nb == "") {
    $nb = "0";
  }
  $query = "update List set list_query_nb='$nb' where list_id='$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// List context checking (same lists exists ?)
// Parameters:
//   - $id       : list id
//   - $list[]   : list values
//     keys used : name, subject, email
// Returns:
//   - List Database object with list of similar lists
///////////////////////////////////////////////////////////////////////////////
function check_list_context($id, $list) {
  global $cdg_sql;

  $name = $list["name"];
  $subject = $list["subject"];

  // return the lists with same name or subject
  $list_q = run_query_check_list($id, $name, $subject);

  return $list_q;
}


///////////////////////////////////////////////////////////////////////////////
// List Form Data checking and formatting
// Parameters:
//   - $id       : list id  (empty on insertion)
//   - $list[]   : values checked
//     keys used : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function check_data_form($id, $list) {
  global $php_regexp_email, $l_j_check_email, $l_list_exists, $l_fill_name;
  global $l_invalid_query, $l_banned_word, $l_hungry_query;
  global $err_msg, $cql_max_row, $cql_max_cost;

  $name = $list["name"];
  $subject = $list["subject"];
  $email = $list["email"];
  $query = $list["query"];

  // MANDATORY: List name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: List name unique
  if (get_list_name_exists($name, $id)) {
    $err_msg = "$l_list_exists ($name)";
    return false;
  }

  // List email
  if (($email != "") && (preg_match($php_regexp_email, $email) == 0)) {
    $err_msg = " $email : $l_j_check_email";
    return false;
  }

  // If a query has been given, check if it is correct
  if (trim($query) != "") {
    // Check if banned words are included
    $excluded_words = array ('insert', 'update', 'delete', 'create', 'alter', 'drop', 'userobm_password');
    while ( list($key, $value) = each($excluded_words) ) {
      if (preg_match("/\b$value\b/i", $query)) {
	$err_msg = "$l_invalid_query $l_banned_word : $value";
	return false;
      }
    }

    // Check if query is syntaxically correct
    display_debug_msg("explain $query", $cdg_sql);
    $obm_q = new DB_OBM;
    // 1 indicate to PHPLIB not to report the error if any
    $ret = $obm_q->query("explain $query", 1);
    if (substr($ret, 0,4) == "obm:") {
      $err_msg = "$l_invalid_query : " . substr($ret,4);
      return false;
    } else {
      $ret = sql_query_resource_high($query);
      if ($ret["ret"]) {
	$rows = $ret["rows"];
	$cost = $ret["cost"];
	$err_msg = "$l_hungry_query (rows=$rows [max=$cql_max_row] cost=$cost [max=$cql_max_cost])";
	return false;
      }
    }
  }
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the infos of a List from its Id
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_info($id) {
  global $cdg_sql;

  $query = "select list_id,
      list_name,
      list_structure,
      list_mailing_ok,
      list_contact_archive,
      list_info_publication
    from List
    where list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res["id"] = $obm_q->f("list_id");
  $res["name"] = $obm_q->f("list_name");
  $res["structure"] = $obm_q->f("list_structure");
  $res["mailing_ok"] = $obm_q->f("list_mailing_ok");
  $res["contact_arch"] = $obm_q->f("list_contact_archive");
  $res["info_pub"] = $obm_q->f("list_info_publication");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the rows number for the query given
// Parameters:
//   - $query : query to run
///////////////////////////////////////////////////////////////////////////////
function get_list_query_num_rows($query="") {
  global $cdg_sql;

  // Construct count query to optimize performance (instead of counting rows)
  // Get the From ... Where clauses
  // IF 'Group by' present, take between From and group by (ungreedy)
  // else get between From and the end
  if (preg_match('/(FROM (.*))($)/Uis', $query, $matches)) {
    $query_c = "select contact_id " . $matches[1];
    display_debug_msg($query_c, $cdg_sql);
    $obm_q = new DB_OBM;
    $r = $obm_q->query($query_c, 1);
    if (is_string($r)) {
      $ret = -1;
    } else {
      $obm_q->next_record();
      $ret = $obm_q->nf();
    }
  } else {
    $ret = 0;
  }  

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the current mode of a given list according to parameters
// Parameters:
//   - $query    : list query
//   - $criteria : list criteria (unserialized from structureà or parameter
// Returns:
//   $clist_mode_expert || $clist_mode_normal
///////////////////////////////////////////////////////////////////////////////
function check_list_mode($query, $criteria) {
  global $clist_mode_expert, $clist_mode_normal;

  if ($criteria == "" && $query != "") {
    $mode = $clist_mode_expert;
  } else {
    $mode = $clist_mode_normal;
  }

  return $mode;
}


///////////////////////////////////////////////////////////////////////////////
// Evaluate if a query can be accepted based on resource consumption
// Evaluation done specificaly for each database backend supported
// Parameters:
//   - $db_type : DB type
//   - $query   : query to evaluate
// Returns:
//   - true if resource consumption is high for this query, else false
///////////////////////////////////////////////////////////////////////////////
function sql_query_resource_high($query) {
  global $db_type_mysql, $db_type_pgsql;
  global $cql_max_row, $cql_max_cost;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  if ($db_type == $db_type_mysql) {
    $q = "explain $query";
    $obm_q->query($q);
    $cpt = 1;
    while ($obm_q->next_record()) {
      $num = $obm_q->f("rows");
      $cpt *= $num;
    }
    $ret["rows"] = $cpt;
    if ($cpt > $cql_max_row) {
      $ret["ret"] = true;
      return $ret;
    } else {
      $ret["ret"] = false;
      return $ret;
    }

    // Postgres Estimate
  } elseif ($db_type == $db_type_pgsql) {
    $q = "explain $query";
    $obm_q->query($q, 1);
    // We read the first row of explain command
    $obm_q->next_record();
    $line = $obm_q->f(0);
    $pattern = "/cost=[0-9]*\.[0-9]*\.\.([0-9]*\.[0-9]*) rows=([0-9]*) /";
    if (preg_match($pattern, $line, $match)) {
      $cost = $match[1];
      $rows = $match[2];
      $ret["cost"] = $cost;
      $ret["rows"] = $rows;
    }

    if (($cost > $cql_max_cost) || ($rows > $cql_max_row)) {
      $ret["ret"] = true;
      return $ret;
    } else {
      $ret["ret"] = false;
      return $ret;
    }

    // Other DB backends
  } else {
    $ret["ret"] = false;
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Category 1 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_contactcategory1() {
  global $cdg_sql;

  $query = "select * from ContactCategory1 order by contactcategory1_code"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Category 2 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_contactcategory2() {
  global $cdg_sql;

  $query = "select * from ContactCategory2 order by contactcategory2_code"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Function select query execution
// Returns:
//   DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_function() {
  global $cdg_sql;

  $query = "select function_id, function_label
    from Function
    order by function_label";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind lang select query 
// Returns:
//   DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_language() {
  global $cdg_sql;

  $query = "select kind_lang from Kind
    group by kind_lang";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Publication: publication select query 
// Returns:
//   DB object result with all publication
///////////////////////////////////////////////////////////////////////////////
function run_query_publication() {
  global $cdg_sql;

  $query = "select publication_title, publication_id
    from Publication
    order by publication_year desc, publication_type_id, publication_title";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode subscription query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_subscriptionreception() {
  global $cdg_sql;

  $query = "select * from SubscriptionReception order by subscriptionreception_label DESC"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Generate the SQL Query from a List given as a DBO
// Parameters:
//   - $list_q : DBO with list info
///////////////////////////////////////////////////////////////////////////////
function make_query_from_db(&$list_q) {
  global $auth, $cdg_sql;

  if (is_object($list_q)) {
    $id = $list_q->f("list_id");
    $structure = $list_q->f("list_structure");
    $mail_ok = $list_q->f("list_mailing_ok");
    $contact_arch = $list_q->f("list_contact_archive");
    $info_pub = $list_q->f("list_info_publication");
    
    if ($structure != "") {
      $criteria = unserialize($structure);
    } else {
      $criteria = "";
    }
    
    if ($criteria != "") {
      // Addslashes to all criteria elements
      $as_criteria = $criteria;
      array_walk($as_criteria, 'addslashes_array');
      $list_info["criteria"] = $as_criteria;
      $list_info["id"] = $id;
      $list_info["mailing_ok"] = $mail_ok;
      $list_info["contact_arch"] = $contact_arch;
      $list_info["info_pub"] = $info_pub;
      $query = make_query_from_criteria($list_info);
    }
  }

  return $query;
}


///////////////////////////////////////////////////////////////////////////////
// Make an SQL Query with the list of criteria
// Parameters:
//   - $list[]   : list search criteria
//     keys used : $criteria.
///////////////////////////////////////////////////////////////////////////////
function make_query_from_criteria($list) {
  global $auth, $cdg_sql;

  $id = $list["id"];
  $criteria = $list["criteria"]["modules"];
  $boulean_criterion = $list["criteria"]["logical"]["NOT"];
  $logical_criterion = $list["criteria"]["logical"]["AND"];
  $mailing_ok = $list["mailing_ok"];
  $contact_arch = $list["contact_arch"];
  $info_pub = $list["info_pub"];
  $lang = get_lang();
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  
  $common_like = "%f $like '%v%'";
  $common_equal = "%f = '%v'";
  $category_equal = "(cc%j.%f $like '%v.%' OR cc%j.%f = '%v')";
  $connection_equal = "%j.%f = '%v'";
  $pub_lang_like = "%f $like '%%v%'";
  $comp_time_sup = "(company_timecreate >= '%v' OR company_timeupdate >= '%v')";
  $comp_time_inf = "(company_timecreate <= '%v' OR company_timeupdate <= '%v')";
  $cont_time_sup = "(contact_timecreate >= '%v' OR contact_timeupdate >= '%v')";
  $cont_time_inf = "(contact_timecreate <= '%v' OR contact_timeupdate <= '%v')";
  
  $operateurs = array(
    "company_name" => $common_like,
    "company_country_iso3166" => $common_equal,
    "company_timeafter"  => $comp_time_sup,
    "company_timebefore" => $comp_time_inf,
    "company_zipcode"  => $common_like,
    "company_marketingmanager_id" => $common_equal,
    "company_town"  => $common_like,
    "company_datasource_id" => $common_equal,
    "companycategory_code" => $category_equal,
    "contact_firstname"  => $common_like,
    "contact_country_iso3166" => $common_equal,
    "contact_timeafter"  => $cont_time_sup,
    "contact_timebefore"  => $cont_time_inf,
    "contact_lastname"  => $common_like,
    "contact_marketingmanager_id" => $common_equal,
    "contact_datasource_id" => $common_equal,
    "contact_town"  => $common_like,
    "contact_zipcode"  => $common_like,
    "contactcategory1link_category_id" => "cc1l".$connection_equal,
    "contactcategory2link_category_id" => "cc2l".$connection_equal,
    "contact_function_id" => $common_equal,
    "subscription_publication_id"  => $common_equal,
    "publication_lang"  => $pub_lang_like,
    "publication_year"  => $common_like,
    "subscription_renewal"  => $common_equal,
    "subscription_reception_id"  => $common_equal,
    "kind_lang"  => $common_equal
  );
    
  $add_join = array (
    "publication" => " left join Subscription on subscription_contact_id = contact_id
                       left join Publication on subscription_publication_id = publication_id ",
    "companycategory_code" => " left join CompanyCategoryLink ccl%j on company_id = ccl%j.companycategorylink_company_id 
          	                left join CompanyCategory cc%j on cc%j.companycategory_id = ccl%j.companycategorylink_category_id ",
    "contactcategory1link_category_id" => " left join ContactCategory1Link cc1l%j on contact_id = cc1l%j.contactcategory1link_contact_id ",
    "contactcategory2link_category_id" => " left join ContactCategory2Link cc2l%j on contact_id = cc2l%j.contactcategory2link_contact_id ",
  );

  //If publication info must be displayed we add these fields
  if ($info_pub == 1) {
    $add_select = array (
      "publication" => ", SUM(subscription_quantity) as subscription_quantity
                        ,publication_lang
                        ,publication_title"
      );
  
    $add_group = array (
      "publication" => ",publication_lang
                        ,publication_title"
      );
  }

  $select = "
        contact_id,
        contact_lastname,
        contact_firstname,
        kind_minilabel,
        kind_header,
        kind_lang,
        function_label,
        contact_title,
        contact_service,
        contact_address1,
        contact_address2,
        contact_address3,
        contact_zipcode,
        contact_town,
        contact_expresspostal,
        contact_country_iso3166,
        ctry1.country_name,
        contact_phone,
        contact_homephone,
        contact_fax,
        contact_email,
        contact_archive,
        company_id,
        company_name,
        company_address1,
        company_address2,
        company_address3,
        company_zipcode,
        company_town,
        company_expresspostal,
        ctry2.country_name as company_country";

  $group = "
        contact_id,
        contact_lastname,
        contact_firstname,
        kind_minilabel,
        kind_header,
        kind_lang,
        function_label,
        contact_title,
        contact_service,
        contact_address1,
        contact_address2,
        contact_address3,
        contact_zipcode,
        contact_town,
        contact_expresspostal,
        contact_country_iso3166,
        ctry1.country_name,
        contact_phone,
        contact_homephone,
        contact_fax,
        contact_email,
        contact_archive,
        company_id,
        company_name,
        company_address1,
        company_address2,
        company_address3,
        company_zipcode,
        company_town,
        company_expresspostal,
        ctry2.country_name";
	
  $join = " Contact left join Company on contact_company_id=company_id
                    left join Country as ctry1 on Contact.contact_country_iso3166=ctry1.country_iso3166 and ctry1.country_lang='$lang'
                    left join Country as ctry2 on Company.company_country_iso3166=ctry2.country_iso3166 and ctry2.country_lang='$lang'
                    left join Kind on contact_kind_id = kind_id
                    left join Function on contact_function_id = function_id";

  if (is_array($criteria)) {
    foreach($criteria as $module => $module_criteria) {
      if (is_array($module_criteria)) {
        foreach($module_criteria as $field => $criterion) {
          if (is_array($criterion)) {
            foreach($criterion as $line => $value) {
              if ($value != "") {
		$select .= $add_select[$field];
		$group .= $add_group[$field];
		if(!isset($join_name[$field])) {
		  $join_name[$field] = 0;
		  $join .= str_replace('%j',$join_name[$field],$add_join[$field]);
		}
		elseif($logical_criterion[$line] == "AND" && $line == $old_line + 1) {
		  $join_name[$field] ++;
		  $join .= str_replace('%j',$join_name[$field],$add_join[$field]);
		}
		$module_fill = 1;
		$operation = $operateurs[$field];
		$operation = str_replace('%j',$join_name[$field],$operation);
		$operation = str_replace('%f',$field,$operation);
		$operation = str_replace('%v',$value,$operation);
		$condition[$line] .= $and[$line]." ". $operation;
		$and[$line] = " AND";	
	      }
	      else {
		$condition[$line] .= "";
	      }
	      $old_line = $line;
            }
          }
        }
       	if ($module_fill == 1) {
      	  $join .= $add_join[$module];
	  $select .= $add_select[$module];
	  $group .= $add_group[$module];
	  $module_fill = 0;
	}
      }
    }
    foreach($condition as $line=>$value) {
      if ($line == 0) {
	$logical_criterion[$line] = "";
      }
      $where .= " " . $logical_criterion[$line]." ".$boulean_criterion[$line]." ($value)";
    }
  }


  // Handle the Mailing enable flag
  if ($mailing_ok == 1) {
    $where = " contact_mailing_ok = 1 AND ($where) ";
  }

  // Handle the contact archive flag
  if ($contact_arch == 0) {
    $where = " contact_archive != 1 AND ($where) ";
  }

  // If any (not in insert mode), add statics contacts to the query
  if ($id > 0) {
    $con_q = run_query_list_static_contact_id($id);
    if ($con_q->num_rows() > 0) {
      $first = true;
      $where_static = "(";
      while ($con_q->next_record()) {
	$c_id = $con_q->f("contactlist_contact_id");
	if ($first) {
	  $where_static .= "contact_id='$c_id'";
	} else {
	  $where_static .= " or contact_id='$c_id'";
	}
	$first = false;
      }
      $where_static .= ")";
    }
  }

  if ($where != "") {
    if ($where_static != "") {
      $where = "WHERE ($where) or $where_static";
    } else {
      $where = "WHERE $where";
    }
  }

  $query = trim("SELECT $select FROM $join $where GROUP BY $group ORDER BY contact_lastname");
  return $query;
}


///////////////////////////////////////////////////////////////////////////////
// Staticaly registered contacts ids to the list 
// Parameters:
//   - $id : List id
// Returns:
//   DB object result with statically registered contact
///////////////////////////////////////////////////////////////////////////////
function run_query_list_static_contact_id($id) {
  global $cdg_sql;

  $query = "select contactlist_contact_id
    from ContactList
    where contactlist_list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}




// Utilise que par adresses.php XXXX
///////////////////////////////////////////////////////////////////////////////
// Query Execution : Distincts contacts from several lists for export        //
// Parameters:
//   - $list_ids : comma separated list of list id
// Returns:
//   - database result set
///////////////////////////////////////////////////////////////////////////////
function run_query_contacts_for_letter($list_ids) {
  global $auth, $cdg_sql;

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
        c1.country_name as list_contact_country,
        function_label as list_contact_function,
        contact_phone as list_contact_phone,
        contact_homephone as list_contact_homephone,
        contact_mobilephone as list_contact_mobilephone,
        contact_fax as list_contact_fax,
        contact_email as list_contact_email,
        contact_comment as list_contact_comment,
        contact_privacy as list_contact_privacy,
        company_name as contact_company_name,
        company_address1 as contact_company_address1,
        company_address2 as contact_company_address2,
        company_zipcode as contact_company_zipcode,
        company_town as contact_company_town,
        company_expresspostal as contact_company_expresspostal,
        c2.country_name as contact_company_country,
        kind_minilabel as list_contact_kind
      from Contact
           left join Company on contact_company_id=company_id
           left join Kind on contact_kind_id=kind_id
           left join Function on contact_function_id=function_id
           left join Country as c1 on contact_country_id=c1.country_id
           left join Country as c2 on company_country_id=c2.country_id,
           ContactList
      where contactlist_list_id in (";
    $query .= $list_ids;
    $query .= ") and contactlist_contact_id = contact_id and company_id = contact_company_id and contact_kind_id = kind_id";
    $query .= " order by ";
    $query .= (strcmp($options_order,"") != 0) ? $options_order : "contact_lastname";
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query);
  }
  return $connect_db;
}

///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the number of company from a dynamic query.       //
// Parameters:
//   - $query : the dynamic query
// Returns:
//   - amount of companies
///////////////////////////////////////////////////////////////////////////////
function run_query_get_dynamic_company($query) {
  global $cdg_sql;
  if (preg_match('/(FROM (.*))( GROUP BY)?(?(3)|$)/Uis', $query, $matches)) { 
   $query_f = "select count(distinct(company_id)) as nb ". $matches[1];
  }
  display_debug_msg($query_f, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query_f);
  $obm_q->next_record();
  return $obm_q->f("nb");

}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the number of company from the static contact.      //
// of a list
// Parameters:
//   - $id : Id of the list
// Returns:
//   - amount of companies
///////////////////////////////////////////////////////////////////////////////
function run_query_get_static_company($id) {

  $query_c = "select count(distinct(company_id)) as nb from ContactList
         LEFT JOIN Contact ON contactlist_contact_id=contact_id
         LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
	 where
         contactlist_list_id='$id'";
  display_debug_msg($query_f, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query_c);
  $obm_q->next_record();
  return $obm_q->f("nb");
	 
}

</script>

