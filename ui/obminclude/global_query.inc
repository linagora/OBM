<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_query.inc                                             //
//     - Desc : Global Query File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Format a Date field query according to Database backend
// Parameters:
//   - $db_type : DB type
//   - $field   : date field name
//   - $as      : (Optionnal) alias fieldname to return
// Returns:
//   - Field query string
///////////////////////////////////////////////////////////////////////////////
function sql_date_format($db_type, $field, $as="") {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "UNIX_TIMESTAMP($field)";
    if ($as != "") {
      $ret .= " as $as";
    }
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "EXTRACT (EPOCH from $field)";
    if ($as != "") {
      $ret .= " as $as";
    }
  } else {
    $ret = $field;
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql word corresponding to case incensitive like
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word "ilike"
///////////////////////////////////////////////////////////////////////////////
function sql_casei_like($db_type) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "like";
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "ilike";
  } else {
    $ret = "like";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Transform and Return a searched string (allow global processing)
// Parameters:
//   - $text : Text o parse or transform
// Returns:
//   - updated text
///////////////////////////////////////////////////////////////////////////////
function sql_search_text_parse($text) {
  global $cgp_sql_star;

  $mytext = $text;

  if ($cgp_sql_star) {
    $mytext = preg_replace("/\*/", "%", $text);
  }

  return $mytext;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql order clause handling case insensitive sorting
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word "ilike"
///////////////////////////////////////////////////////////////////////////////
function sql_casei_sort($db_type, $field) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "$field";
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "UPPER($field)";
  } else {
    $ret = "$field";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql corresponding to concat function
// Parameters:
//   - $db_type : DB type
//   - $ctt[]   : array of values ctt[0][type] = field | string, ctt[0][value]
// Returns:
//   - sql concatenation string
///////////////////////////////////////////////////////////////////////////////
function sql_string_concat($db_type, $ctt) {
  global $db_type_mysql, $db_type_pgsql;
  global $cmy_character_set, $cmy_charset_collation;

  if ($db_type == $db_type_mysql) {
    $start = "concat(";
    $end = ")";
    $sep = ",";
    $intro = $cmy_character_set;
    if ($cmy_charset_collation) {
      $collation = " COLLATE $cmy_charset_collation";
    }
  } elseif ($db_type == $db_type_pgsql) {
    $start = "(";
    $end = ")";
    $sep = "||";
  } else {
    $start = "(";
    $end = ")";
    $sep = "||";
  }

  $ret = $start;
  if ($ctt[0]["type"] == "field") {
    $ret .= $ctt[0]["value"];
  } else {
    $ret .= "'" . $ctt[0]["value"] . "'";
  }
  $i = 1;
  while ($ctt[$i]) {
    $ret .= " $sep ";
    if ($ctt[$i]["type"] == "field") {
      $ret .= $ctt[$i]["value"];
    } else {
      $ret .= "$intro'" . $ctt[$i]["value"] . "'$collation";
    }
    $i++;
  }
  $ret .= $end;

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql string corresponding to if then else
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word "ilike"
///////////////////////////////////////////////////////////////////////////////
function sql_if($db_type, $expr, $iftrue, $iffalse) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "if ($expr, $iftrue, $iffalse)";
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "case $expr when true then $iftrue else $iffalse end";
  } else {
    $ret = "if ($expr, $iftrue, $iffalse)";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql string corresponding to the limit clause
// If number and count not given, user preferences values are used
// Parameters:
//   - $db_type  : DB type
//   - $number   : number of rows to return (set_rows by default)
//   - $p_offset : #rows to skip (start at offset + 1) (default from page)
// Returns:
//   - limit clause
///////////////////////////////////////////////////////////////////////////////
function sql_limit($db_type, $number="", $p_offset="") {
  global $db_type_mysql, $db_type_pgsql, $ctu_sql_limit;
  global $set_rows, $set_rows_default, $page;

  // If limit should not be used, return empty clause
  if (! $ctu_sql_limit) { return ""; }

  if ($number != "") {
    $num = $number;
  } else if ($set_rows != "") {
    $num = $set_rows;
  } else {
    $num = $set_rows_default;
  }

  if ($p_offset != "") {
    $off = $p_offset;
  } else if ($page > 0) {
    $off = ($page - 1) * $num;
  } else {
    $off = "0";
  }

  if (($db_type == $db_type_mysql) || ($db_type == $db_type_pgsql)) {
    $ret = "limit $num offset $off";
  } else {
    $ret = "limit $num offset $off";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check Privacy access
// Parameters:
//   - $entity : entity to check
//   - $p_uid  : user id to check access
// Returns true if entity is visible, else false
///////////////////////////////////////////////////////////////////////////////
function sql_obm_entity_privacy($entity, $p_uid="") {
  global $auth, $cdg_sql;

  // Set uid if not given
  if ($p_uid == "") {
    $uid = $auth->auth["uid"];
  } else {
    $uid = $p_uid;
  }

  $field_pri = "${entity}_privacy";
  $field_uc = "${entity}_usercreate";

  $where = "($field_pri = '0' or $field_uc = '$uid')";

  return $where;
}


///////////////////////////////////////////////////////////////////////////////
// Return the count(*) of the query (the 1fst field)
// Parameters:
//   - $db_type  : DB type
// Returns:
//   - limit clause
///////////////////////////////////////////////////////////////////////////////
function get_query_count($query) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  display_debug_msg("get_query_count() : $query", $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $count = $obm_q->f(0);

  return $count;
}


///////////////////////////////////////////////////////////////////////////////
// CompanyCategory query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companycategory() {
  global $cdg_sql;

  $query = "select * from CompanyCategory order by companycategory_code"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;  
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company contact number increment
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
// Data Source query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource() {
  global $cdg_sql;

  $query = "select * from DataSource order by datasource_name"; 
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the country list filled for the current lang
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_country_for_lang() {
  global $cdg_sql;

  $lang = get_lang();
  $query = "select * from Country
    where country_lang='$lang'
    order by country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_country() {
  global $cdg_sql;

  $query = "select * from Country
     order by country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// TaskType list query
// Parameters :
//   - $tt_cat : tasktype category
//               "all" or ""  return all tasktype
//               "project" "  return tasktype from $ctt_sales and $ctt_research
//               one category return the category
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype($tt_cat="") {
  global $cdg_sql, $ctt_sales, $ctt_research;

  $query = "
    select
      tasktype_id,
      tasktype_label, 
      tasktype_internal
    from TaskType ";

  if (($tt_cat == "") || ($tt_cat == "all")) {
    $query .= "";
  } elseif ($tt_cat == "project") {
    $query .= "where (tasktype_internal = '$ctt_sales'
                      or tasktype_internal = '$ctt_research')";
  } else {
    $query .= "where tasktype_internal = '$tt_cat'";
  }

  $query .= " order by tasktype_internal, tasktype_label";

  display_debug_msg($query, $cdg_sql, "run_query_tasktype");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of projects linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company)
//   - $arch   : flag to tell if archived projects should be counted (if true)
// Return:
//   - $nb     : number of Project
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_project_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(project_id) as nb
    FROM Project
    WHERE project_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " and project_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of contract linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company)
//   - $arch   : flag to tell if archived contract should be counted (if true)
// Return:
//   - $nb     : number of contract
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_contract_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(contract_id) as nb
    FROM Contract
    WHERE contract_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " and contract_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of invoice linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, project)
//   - $arch   : flag to tell if archived invoices should be counted (if true)
// Return:
//   - $nb     : number of invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_invoice_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "select count(invoice_id) as nb from Invoice
    where invoice_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " and invoice_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of documents linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, contact..)
// Return:
//   - $nb : document number
///////////////////////////////////////////////////////////////////////////////
function run_query_document_nb ($id,$entity) {
  global $cdg_sql;

  $query = "select count(*) as nb
    from DocumentEntity
    where documententity_entity = '$entity'
      and documententity_entity_id = '$id'"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Delete document linked with a entity
// Parameters:
//   - $id     : entity Id
//   - $entity : Type of entity (deal, company..)
//   - $doc_id : [optionnal] document Id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_document_links($ent_id, $entity, $doc_id="") {
  global $cdg_sql;

  $where = "1=1";

  if ($ent_id != "") {
    $where .= " and documententity_entity_id = '$ent_id'";
  }
  if ($entity != "") {
    $where .= " and documententity_entity = '$entity'";
  }
  if ($doc_id != "") {
    $where .= " and documententity_document_id = '$doc_id'";
  }

  $query = "Delete from DocumentEntity where $where";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Insert document linked with a entity
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - $cpt_ins : # documents inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_documents($ids, $entity) {
  global $cdg_sql;

  $id = $ids["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $ids["doc_nb"]) {
    $cpt++;
    $d_id = $ids["doc$cpt"];
    
    $query = "select * from DocumentEntity
      where documententity_entity='$entity'
      and documententity_entity_id='$id'
      and documententity_document_id = '$d_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into DocumentEntity (
       	documententity_entity,
	documententity_entity_id,
	documententity_document_id
      )
      values (    
	'$entity',
	'$id',
	'$d_id'
      )";
      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Get users infos from the uid array given
// Parameters:
//   - users : array of users id
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_in($users) {
  global $cdg_sql;

  $where = "(0";
  foreach($users as $user) {
    $where .= ", $user";
  }
  $where .= ")";
  $query = "SELECT *
    FROM UserObm
    WHERE userobm_archive = 0
      AND userobm_id IN $where 
    ORDER by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_userobm()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the Id of one User from its login
// Parameters:
//   - $login  : login
// Returns:
//   - user's Id
///////////////////////////////////////////////////////////////////////////////
function get_user_id($login) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  if (isset($login)) {
    $query = "select userobm_id from UserObm where userobm_login='$login'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query) ;
    $obm_q->next_record() ;
    return $obm_q->f("userobm_id") ;
  } else {
    return "" ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get User infos
// Parameters:
//   - $id  : user id
// Returns:
//   - user hash values
///////////////////////////////////////////////////////////////////////////////
function get_user_info($id="") {
  global $cdg_sql, $auth;

  $obm_q = new DB_OBM ;
  if ($id == "") $id = $auth->auth["uid"];

  $query = "SELECT userobm_login,
      userobm_perms,
      userobm_lastname,
      userobm_firstname
    FROM UserObm
    WHERE userobm_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record() ;
  $u["login"] = $obm_q->f("userobm_login");
  $u["perms"] = $obm_q->f("userobm_perms");
  $u["lastname"] = $obm_q->f("userobm_lastname");
  $u["firstname"] = $obm_q->f("userobm_firstname");

  return $u;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the entry date of one User from its id
// Parameters:
//   - $id : id
// Returns:
//   - begin date
///////////////////////////////////////////////////////////////////////////////
function get_user_entrydate($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, "userobm_datebegin", "datebegin");

  if (isset($id)) {
    $query = "select userobm_datebegin,
        $datebegin
      from UserObm
      where userobm_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
    $date["iso"] = $obm_q->f("userobm_datebegin");
    $date["unix_timestamp"] = $obm_q->f("datebegin");
    return $date;
  } else {
    return "" ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Userobm query execution : All users                                       //
// Parameters:
//   - $id (optionnal) : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm($id="") {
  global $cdg_sql;

  if ($id) $where = "and userobm_id='$id'";
  $query = "select *
    from UserObm
    where userobm_archive = 0
      $where
    order by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_userobm()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Active Userobm query execution : All users not archived                   //
// Parameters:
//   - $ids (optionnal) : array of UserObm Id to add to the result
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_active($ids="") {
  global $cdg_sql;

  if (is_array($ids)) {
    $where_add = "";
    $nb_add = count($ids);
    for ($i=0; $i<$nb_add; $i++) {
      if ($ids[$i] != "") {
        $where_add .= " or userobm_id = '$ids[$i]'";
      }
    }
  }

  $query = "select * from UserObm
    where userobm_archive != '1'
      $where_add
    order by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg("run_query_userobm_active() : $query", $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the users list from the given group
// Recursive search to handle group trees
// Parameters:
//   - $group_id : Group id
//   - $ids (optionnal) : array of UserObm Id to add to the result
///////////////////////////////////////////////////////////////////////////////
function run_query_all_users_from_group($g_id, $ids="") {
  global $cdg_sql;

  $gusers = get_all_users_from_group($g_id);

  if (is_array($gusers)) {
    $where_gusers = "";
    $nb_add = count($gusers);
    for ($i=0; $i<$nb_add; $i++) {
      if ($gusers[$i] != "") {
        $where_gusers .= " or userobm_id = '$gusers[$i]'";
      }
    }
  }

  if (is_array($ids)) {
    $where_ids = "";
    $nb_add = count($ids);
    for ($i=0; $i<$nb_add; $i++) {
      if ($ids[$i] != "") {
        $where_ids .= " or userobm_id = '$ids[$i]'";
      }
    }
  }

  $query = "select userobm_id,
      userobm_lastname,
      userobm_firstname
    from UserObm
    where (userobm_archive != '1' and (1=0 $where_gusers))
      $where_ids
    order by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg("run_query_all_users_from_group() : $query", $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the users list from the given group
// Recursive search to handle group trees
// Parameters:
//   - $group_id : Group id
///////////////////////////////////////////////////////////////////////////////
function get_all_users_from_group($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $users = array();

  // Groups members of this group
  $query = "select groupgroup_child_id as child_id
      from GroupGroup
      where groupgroup_parent_id='$g_id'";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $child_id = $obm_q->f("child_id");
    $gusers = get_all_users_from_group($child_id);
    $users = array_merge($users, $gusers);
  }

  // Users members of this group
  $query = "select userobmgroup_userobm_id as user_id
    from UserObmGroup
    where userobmgroup_group_id='$g_id'
  ";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("user_id");
    if (! in_array($u_id, $users)) {
      array_push($users, $u_id);
    }
  }

  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Update the login date of the user                                         //
// Parameters:
//   - $id : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_update_lastaccess($id) {
  global $cdg_sql;

  $query = "update UserObm
    set userobm_timelastaccess='".date("Y-m-d H:i:s")."'
    where userobm_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Insert (or Reset) User parameters to default values     //
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_default_preferences_insert($user_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  //---------------------------------------------------------------------------
  // Default User Preferences (tableUserObmPref)
  // We copy each entry with user id = 0 to the new user
  //---------------------------------------------------------------------------

  //-- Drop current user preferences
  $query = "delete from UserObmPref where userobmpref_user_id='$user_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //-- We get default parameters (user id 0)
  $query = "select * from UserObmPref where userobmpref_user_id='0'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $line_q = new DB_OBM;
  while ($obm_q->next_record()) {
    $option = $obm_q->f("userobmpref_option");
    $value = $obm_q->f("userobmpref_value");

    $query = "insert into UserObmPref (userobmpref_user_id, userobmpref_option, userobmpref_value) values ('$user_id', '$option', '$value')";
    display_debug_msg($query, $cdg_sql);
    $line_q->query($query);
  }

  //---------------------------------------------------------------------------
  // Default Display Parameters
  // We copy each entry with user id = 0 to the new user
  //---------------------------------------------------------------------------

  $obm_q = new DB_OBM;
  //-- Drop current user preferences
  $query = "delete from DisplayPref where display_user_id='$user_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //-- We get default parameters (user id 0)
  $query = "select * from DisplayPref where display_user_id='0'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $line_q = new DB_OBM;
  while ($obm_q->next_record()) {
    $entity = $obm_q->f("display_entity");
    $fieldname = $obm_q->f("display_fieldname");
    $fieldorder = $obm_q->f("display_fieldorder");
    $disstatus = $obm_q->f("display_display");

    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$user_id', '$entity', '$fieldname', '$fieldorder', '$disstatus')";
    display_debug_msg($query, $cdg_sql);
    $line_q->query($query);
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - get the User's entity display preferences
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $all    : if not 0, get all fields (even those marked not to display)
// Returns:
//   - DBO : ordered list of deal fields to display
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref($u_id, $entity, $all=0)  {
  global $cdg_sql;
  
  $query = "select *
            from DisplayPref
            where display_user_id='$u_id' and display_entity='$entity'";
  if ($all == 0) { 
    $query.=" and display_display != 0";
  }
  $query.=" order by display_fieldorder"; 

  display_debug_msg($query, $cdg_sql, "run_query_display_pref()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;	
}      


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new display value for the entity, field       //
// Parameters:
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $disstatus : current display value
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_update($entity, $fieldname, $disstatus) {
  global $auth, $cdg_sql;

  // Invert the display value (0,1)  
  $new_display = ($disstatus == 0) ? 1 : 0;
  $query = "update DisplayPref set
      display_display='$new_display'
    where display_user_id='" . $auth->auth["uid"] . "'
      and display_entity='$entity'
      and display_fieldname='$fieldname'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ; 
  $obm_q->query($query) ;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Update the level of the field in the entity given       //
// Parameters:
//   - $entity     : entity affected
//   - $fieldname  : fieldname to change the display value
//   - $fieldorder : position of field
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_level_update($entity, $new_level,$fieldorder) {
  global $auth, $cdg_sql;

  $num_new_level = ($new_level == "up") ? $fieldorder-1 : $fieldorder+1;
  $uid = $auth->auth["uid"];
  $obm_q = new DB_OBM;
  
  //Aim: To know which fieldname  will be changed
  $query = "select display_fieldname
    from DisplayPref
    where display_user_id='$uid'
      and display_entity='$entity'
      and display_fieldorder='$num_new_level'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $fieldname_near_change = $obm_q->f("display_fieldname");

  //Aim: To change the first field's level
  $query = "update DisplayPref
    set display_fieldorder='$num_new_level'
    where display_user_id='$uid'
      and display_entity='$entity'
      and display_fieldorder='$fieldorder'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //Aim: To change the second field's level of $fieldname_near_change
  $query = "update DisplayPref
    set display_fieldorder='$fieldorder'
    where display_user_id='$uid'
      and display_entity='$entity'
      and display_fieldname='$fieldname_near_change'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Update an user preference
// Parameters:
//   - $user_id  : user Id
//   - $option   : preference name
//   - $value    : new preference value
///////////////////////////////////////////////////////////////////////////////
function run_query_set_user_pref($user_id, $option, $value) {
  global $cdg_sql;

  // Delete actual value
  $query = "delete from UserObmPref
            where userobmpref_user_id ='$user_id'
              and userobmpref_option = '$option'";   

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ;
  $ret = $obm_q->query($query);

  // Insert new value
  $query = "insert into UserObmPref
            (userobmpref_user_id, userobmpref_option, userobmpref_value)
            values ('$user_id', '$option', '$value')";
  display_debug_msg($query, $cdg_sql);
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the preferences of the specified user
///////////////////////////////////////////////////////////////////////////////
function run_query_get_user_prefs($p_user_id) {
  global $cdg_sql;

  $query = "select * from UserObmPref where userobmpref_user_id='$p_user_id'";
  // Problem because called before include global.inc in setting module
  display_debug_msg($query, $cdg_sql);
  $obm_q_pref = new DB_OBM;
  $obm_q_pref->query($query);

  return $obm_q_pref;
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's preferences into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_prefs() {
  global $sess, $auth, $last_v;

  // We reset $last_v in case of session persistence (changing user)
  $last_v = "";

  $obm_q = run_query_get_user_prefs($auth->auth["uid"]);
  while ($obm_q->next_record()) {
    $option = $obm_q->f("userobmpref_option");
    $value = $obm_q->f("userobmpref_value");
    global $$option;
    $$option = $value;

    // Last visit management (if last_visit entry, get the associated text)
    if (substr($option, 0, 5) == "last_") {
      $entity = substr($option, 5);
      $function_get_last_text = "get_last_${entity}_text";
      $last_v[$entity]["id"] = $$option;
      $last_v[$entity]["text"] = $function_get_last_text($$option);

      // Other preferences
    } else {
      $sess->register("$option");
    }
  }

  $sess->register("last_v");
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's todo into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_todos($set_todo="todo_priority") {
  global $cdg_sql, $cgp_todo_nb, $todos, $sess, $auth;

  $uid = $auth->auth["uid"];

  $limit = sql_limit($db_type, $cgp_todo_nb, 0);
  $order = ($set_todo != "" ? $set_todo : "todo_priority");
  if ($order == "todo_priority") {
    $order .= " desc";
  }

  // create the todo list
  $query = "SELECT
      todo_id,
      todo_title
    FROM Todo
    WHERE todo_user = '$uid'
    ORDER BY $order
    $limit";

  display_debug_msg($query, $cdg_sql);
  $todolist_q = new DB_OBM;
  $todolist_q->query($query);

  $nb_todo = $todolist_q->num_rows();
  $max = min ($cgp_todo_nb, $nb_todo);

  $todos = "";

  for ($rank = 1; $rank <= $max; $rank++) {
    $todolist_q->next_record();

    $option = "todo_$rank";
    $t_id = $todolist_q->f("todo_id");
    $t_title = $todolist_q->f("todo_title");

    $todos[$rank]["id"] = $t_id; 
    $todos[$rank]["title"] = $t_title; 
  }

  $sess->register("todos");
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Action on ActiveUserObm and UserObm_Session Log during  //
// the logout
///////////////////////////////////////////////////////////////////////////////
function run_query_logout() {
  global $sess, $cdg_sql;

  // get the Active connexion
  $obm_q = new DB_OBM ; 
  $query = "Select * from ActiveUserObm 
            where activeuserobm_sid = '".$sess->id."'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;

  if($obm_q->next_record()) {
    // Log the connexion in the session log
    run_query_log_session($obm_q);

    // drop the connexion from the active user session table
    $query = "delete from ActiveUserObm where 
              activeuserobm_sid = '".$obm_q->f('activeuserobm_sid')."'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query) ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Insertion or update of the Session logs		     //
// during the logout						  	     //
///////////////////////////////////////////////////////////////////////////////
function run_query_log_session($obm_q) {
  global $cdg_sql;

  $uquery = sprintf("
    update UserObm_SessionLog set 
    userobm_sessionlog_session_name = '%s', 
    userobm_sessionlog_userobm_id = '%s', userobm_sessionlog_timeupdate = '%s',
    userobm_sessionlog_timecreate = '%s', 
    userobm_sessionlog_nb_connexions = userobm_sessionlog_nb_connexions + %d,
    userobm_sessionlog_lastpage = '%s', userobm_sessionlog_ip  = '%s' where
    userobm_sessionlog_sid='%s'",
    $obm_q->f('activeuserobm_session_name'), 
    $obm_q->f('activeuserobm_userobm_id'), $obm_q->f('activeuserobm_timeupdate'),
    $obm_q->f('activeuserobm_timecreate'), 
    $obm_q->f('activeuserobm_nb_connexions'),
    $obm_q->f('activeuserobm_lastpage'), $obm_q->f('activeuserobm_ip'),
    $obm_q->f('activeuserobm_sid'));

  $squery = sprintf("
    select count(*) from UserObm_SessionLog where userobm_sessionlog_sid='%s'",
    $obm_q->f('activeuserobm_sid'));

  $iquery = sprintf("
    insert into UserObm_SessionLog ( userobm_sessionlog_sid,
    userobm_sessionlog_session_name, 
    userobm_sessionlog_userobm_id, userobm_sessionlog_timeupdate,
    userobm_sessionlog_timecreate, userobm_sessionlog_nb_connexions,
    userobm_sessionlog_lastpage, userobm_sessionlog_ip ) 
    values ('%s', '%s', '%d', '%s','%s', '%d', '%s', '%s')",
    $obm_q->f('activeuserobm_sid'), $obm_q->f('activeuserobm_session_name'), 
    $obm_q->f('activeuserobm_userobm_id'), $obm_q->f('activeuserobm_timeupdate'),
    $obm_q->f('activeuserobm_timecreate'), 
    $obm_q->f('activeuserobm_nb_connexions'),
    $obm_q->f('activeuserobm_lastpage'), $obm_q->f('activeuserobm_ip'));

  $obm_q2 = new DB_OBM ;
  display_debug_msg($uquery, $cdg_sql);
  $obm_q2->query($uquery);
  if ( $obm_q2->affected_rows() == 0) {
    display_debug_msg($squery, $cdg_sql);
    if ( $obm_q2->query($squery)
         && $obm_q2->next_record() && $obm_q2->f(0) == 0 ) {
      display_debug_msg($iquery, $cdg_sql);
      $obm_q2->query($iquery);
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Function that Update a Last visited entry if needed
// Parameters:
//   - $entity : entity
//   - $id     : entity id
//   - $action : action called 
///////////////////////////////////////////////////////////////////////////////
function update_last_visit($entity, $id, $action) {
  global $auth, $last_v;

  if ( ($id == $last_v[$entity]["id"]) && (strcmp($action,"delete")==0) ) {
    $last_v[$entity]["id"] = 0;
    $last_v[$entity]["text"] = 0;
  } else if ( ($id > 0 ) && ($last_v[$entity]["id"] != $id) ) {
    $last_v[$entity]["id"] = $id;
    run_query_set_user_pref($auth->auth["uid"], "last_$entity", $id);
    $function_get_last_text = "get_last_${entity}_text";
    $last_v[$entity]["text"] = $function_get_last_text($id);
  }
  
}


///////////////////////////////////////////////////////////////////////////////
// Get the company name from the company Id
// Parameters:
//   - $id : company id 
///////////////////////////////////////////////////////////////////////////////
function get_last_company_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select company_name from Company where company_id='$id'";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("company_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact name from the contact Id
// Parameters:
//   - $id : contact id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contact_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select contact_lastname from Contact where contact_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("contact_lastname");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal label from the deal Id
// Parameters:
//   - $id : deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_deal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select deal_label from Deal where deal_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
    $obm_db->next_record();
    return $obm_db->f("deal_label");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Parent deal label from the parent deal Id
// Parameters:
//   - $id : parent deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_parentdeal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select parentdeal_label
    from ParentDeal
    where parentdeal_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("parentdeal_label");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list name from the list Id
// Parameters:
//   - $id : list id 
///////////////////////////////////////////////////////////////////////////////
function get_last_list_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select list_name from List where list_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
    $obm_db->next_record();
    return $obm_db->f("list_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the publication name from the publication Id
// Parameters:
//   - $id : publication id 
///////////////////////////////////////////////////////////////////////////////
function get_last_publication_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select publication_title from Publication
     where publication_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("publication_title");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal project from the project Id
// Parameters:
//   - $id : project id 
///////////////////////////////////////////////////////////////////////////////
function get_last_project_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select project_name from Project where project_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("project_name");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract label from the contract Id
// Parameters:
//   - $id : contract id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contract_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select contract_label from Contract where contract_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("contract_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the incident label from the incident Id
// Parameters:
//   - $id : incident id 
///////////////////////////////////////////////////////////////////////////////
function get_last_incident_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select incident_label from Incident where incident_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("incident_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Document name from the document Id
// Parameters:
//   - $id : document id 
///////////////////////////////////////////////////////////////////////////////
function get_last_document_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select document_name from Document where document_id='$id'";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("document_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the account label from the account Id
// Parameters:
//   - $id : account id
///////////////////////////////////////////////////////////////////////////////
function get_last_account_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select account_label from Account where account_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("account_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the invoice label from the invoice Id
// Parameters:
//   - $id : invoice id 
///////////////////////////////////////////////////////////////////////////////
function get_last_invoice_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select invoice_label from Invoice where invoice_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("invoice_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the payment label from the payment Id
// Parameters:
//   - $id : payment id 
///////////////////////////////////////////////////////////////////////////////
function get_last_payment_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select payment_label from Payment where payment_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("payment_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract label from the contract Id
// Parameters:
//   - $id : user id 
///////////////////////////////////////////////////////////////////////////////
function get_last_user_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "select userobm_login from UserObm where userobm_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("userobm_login");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Group name from the group Id
// Parameters:
//   - $id : group id 
///////////////////////////////////////////////////////////////////////////////
function get_last_group_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select group_name from UGroup where group_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("group_name");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the module list                                                       //
// Return : $modules : array of modules
///////////////////////////////////////////////////////////////////////////////
function get_modules_array() {
  global $path;

  // Module dirs to exclude
  $exclude = array('.', '..', 'CVS');
  $modules = array();

  $mod_dir = dir("$path");
  while ($entry=$mod_dir->read()) {
    if (! in_array ($entry, $exclude) && is_dir($mod_dir->path."/".$entry)) {
      array_push($modules, $entry);
    }
  }
  $mod_dir->close();
  sort($modules);

  return $modules;
}


///////////////////////////////////////////////////////////////////////////////
// Return an array of all used emails (users, groups)
// Parameters:
//   - $id  : User id to exclude from the result
//   - $gid : Group id to exclude from the result
//   - $lid : List id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_email_used($id="", $gid="", $lid="") {
  global $cdg_sql, $php_regexp_email_name;

  $mails = array();
  if ($id != "") {
    $where_id = "WHERE userobm_id != '$id'";
  }
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_email
    FROM UserObm
    $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $email = $obm_q->f("userobm_email");
    $name = "Utilisateur : " . $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
    $em = strtok($email, "\r\n");
    while ($em) {
      $mails["$em"] = "$name";
      $em = strtok("\r\n");
    }
  }

  if ($gid != "") {
    $where_gid = "WHERE group_id != '$gid'";
  }
  $query = "SELECT group_name, group_email
    FROM UGroup
    $where_gid";

  display_debug_msg($query, $cdg_sql);
  $g_q = new DB_OBM;
  $g_q->query($query);

  while ($g_q->next_record()) {
    $email = $g_q->f("group_email");
    $name = "Groupe : " . $g_q->f("group_name");
    $mails["$email"] = "$name";
  }

  if ($lid != "") {
    $where_lid = "WHERE list_id != '$lid'";
  }
  $query = "SELECT list_name, list_email
    FROM List
    $where_lid";

  display_debug_msg($query, $cdg_sql);
  $l_q = new DB_OBM;
  $l_q->query($query);

  while ($l_q->next_record()) {
    $email = $l_q->f("list_email");
    $name = "List : " . $l_q->f("list_name");
    $mails["$email"] = "$name";
  }

  return $mails;
}


///////////////////////////////////////////////////////////////////////////////
// Send a Mail to an user set
// If recipient list is empty, no mail is sent
// Parameters:
//  - $subject    : mail subject
//  - $message    : mail content
//  - $recipients : Id Recipients array
//  - $r_addr     : adresses array to add to recipients
//  - $force      : Mail should be forced (all user even with no set_mail)
///////////////////////////////////////////////////////////////////////////////
function send_mail($subject, $message, $recipients, $r_addr, $force) {
  global $cgp_mail_enabled;
  global $aliasuite_enabled;  

  $message = stripslashes($message);
  if (! $cgp_mail_enabled) {
    return 0;
  }

  if (count ($recipients) < 1) {
    return 0;
  }

  $send_q = run_query_get_sender();
  $reci_q = run_query_get_recipients($recipients, $force);

  // If AliaSuite, get local main mail domain.
  if( $aliasuite_enabled ) {
      $domain_q = run_query_get_domain();
      $local_domain = $domain_q->f("mail_value");
  }

  // Construct recipient list
  while ($reci_q->next_record()) {
    $reci = $reci_q->f("userobm_email");
    
    // If AliaSuite, construct recipient address.
    if( $aliasuite_enabled ) {
        $reci = strtok($reci, "\r\n")."@".$local_domain;
    }
    
    if ($reci_list == "") {
      $reci_list = $reci;
    } else {
      $reci_list .= " , $reci";
    }
  }

  // Add addresses given to recipient list
  if (is_array($r_addr)) {
    foreach($r_addr as $reci) {
      if ($reci_list == "") {
	$reci_list = $reci;
      } else {
	$reci_list .= " , $reci";
      }
    }
  }

  $headers .= "From: ";
  // If AliaSuite, construct sender address.
  if( $aliasuite_enabled ) {
    $headers .= strtok($send_q->f("userobm_email"), "\r\n")."@".$local_domain;
  }else {
    $headers .= $send_q->f("userobm_email");
  }
  
  if ($reci_list != "") {
    mail ($reci_list, $subject, $message, $headers);      
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get recipients mail addresses
// Parameters:
//  - $recipients : Id Recipients array
//  - $force      : Mail should be forced (all user even with no set_mail)
///////////////////////////////////////////////////////////////////////////////
function run_query_get_recipients($recipients, $force) {
  global $cdg_sql;
 
  if ($force != 1) {
    $mail_filter = "AND userobm_id = userobmpref_user_id
      AND userobmpref_option = 'set_mail'
      AND userobmpref_value = 'yes'";
    $join = ", UserObmPref";
  }  
  $coma = "";
  foreach($recipients as $recipient) {
    $user_list .= $coma.$recipient;
    $coma = ",";
  }
  if ($user_list != "") {
    $user_in = "userobm_id IN ($user_list) AND";
  }

  $query = "SELECT userobm_email
    FROM UserObm $join
    WHERE $user_in
      userobm_email != ''
      $mail_filter";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get sender mail address                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_get_sender() {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $query = "SELECT userobm_email
    FROM UserObm
    WHERE userobm_id = '$uid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get sender mail address                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_get_domain() {
  global $cdg_sql;

  $query = "SELECT mail_value
    FROM Mail
    WHERE mail_name = 'main_domain'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}
  
?>
