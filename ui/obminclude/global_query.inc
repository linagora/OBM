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

  if ($db_type == $db_type_mysql) {
    $start = "concat(";
    $end = ")";
    $sep = ",";
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
      $ret .= "'" . $ctt[$i]["value"] . "'";
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
// Data Source query execution                                               //
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
// Country query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_country() {
  global $cdg_sql;

  $query = "select * from Country order by country_name, country_iso3166"; 
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
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_project_nb ($id, $entity) {
  global $cdg_sql;

  $query = "select count(*) as nb from Project
    where project_${entity}_id = '$id'";

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
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_contract_nb ($id, $entity) {
  global $cdg_sql;

  $query = "select count(*) as nb from Contract
    where contract_${entity}_id = '$id'";

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
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_document_nb ($id,$entity) {
  global $cdg_sql;

  $query = "select count(*) as nb
    from DocumentEntity
    where documententity_entity = '$entity'
      and documententity_entityid = '$id'"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Delete document linked with a entity
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_document($id,$entity) {
  global $cdg_sql;

  $query = "Delete from DocumentEntity where documententity_entity = '$entity'
            and documententity_entityid = '$id'"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Insert document linked with a entity      //
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_documents($ids,$entity) {
  global $cdg_sql,$auth;

  $id = $ids["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $ids["doc_nb"]) {
    $cpt++;
    $d_id = $ids["doc$cpt"];
    
    $query = "select * from DocumentEntity
      where documententity_entity='$entity'
      and documententity_entityid='$id'
      and documententity_documentid = '$d_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into DocumentEntity (
       	documententity_entity,
	documententity_entityid,
	documententity_documentid
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
  display_debug_msg("run_query_userobm() : $query", $cdg_sql);

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
  $query = "select groupgroup_childid as child_id
      from GroupGroup
      where groupgroup_parentid='$g_id'";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $child_id = $obm_q->f("child_id");
    $gusers = get_all_users_from_group($child_id);
    $users = array_merge($users, $gusers);
  }

  // Users members of this group
  $query = "select userobmgroup_userobmid as user_id
    from UserObmGroup
    where userobmgroup_groupid='$g_id'
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

  $obm_q = new DB_OBM;
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
    $display = $obm_q->f("display_display");

    $query = "insert into DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values ('$user_id', '$entity', '$fieldname', '$fieldorder', '$display')";
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

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;	
}      


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new display value for the entity, field       //
// Parameters:
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $display   : current display value
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_update($entity, $fieldname, $display) {
  global $auth, $cdg_sql;

  // Invert the display value (0,1)  
  $new_display = ($display == 0) ? 1 : 0;
  $query = "update DisplayPref set display_display='$new_display' where display_user_id='" . $auth->auth["uid"] . "' and display_entity='$entity' and display_fieldname='$fieldname'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ; 
  $obm_q->query($query) ;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Update the level of the field in the entity given       //
// Parameters:
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $display   : current display value
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_level_update($entity, $new_level,$fieldorder) {
  global $auth, $cdg_sql;

  $num_new_level = ($new_level == "up") ? $fieldorder-1 : $fieldorder+1;
  
  $obm_q = new DB_OBM;
  
  //Aim: To know which fieldname  will be changed
  $query = "select display_fieldname from DisplayPref where display_user_id='" . $auth->auth["uid"] . "' and display_entity='$entity' and display_fieldorder='$num_new_level'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $fieldname_near_change = $obm_q->f("display_fieldname");

  //Aim: To change the first field's level
  $query = "update DisplayPref set display_fieldorder='$num_new_level' where display_user_id='" . $auth->auth["uid"] . "' and display_entity='$entity' and display_fieldorder='$fieldorder'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //Aim: To change the second field's level of $fieldname_near_change
  $query = "update DisplayPref set display_fieldorder='$fieldorder' where display_user_id='" . $auth->auth["uid"] . "' and display_entity='$entity' and display_fieldname='$fieldname_near_change'";
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


/////////////////////////////////////////////////////////////////////////////
// Return all the global preferences
/////////////////////////////////////////////////////////////////////////////
function run_query_get_global_prefs() {
  global $cdg_sql;

  $query = "select * from GlobalPref";
  display_debug_msg($query, $cdg_sql);
  $obm_q_pref = new DB_OBM;
  $obm_q_pref->query($query);

  return $obm_q_pref;
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the global preferences into session at login 
// This must be called in before page_open 
///////////////////////////////////////////////////////////////////////////////
function session_load_global_prefs() {
  global $sess;

  $obm_q = run_query_get_global_prefs();
  while ($obm_q->next_record()) {
    $option = $obm_q->f("globalpref_option");
    $value = $obm_q->f("globalpref_value");

    // Define the variable in the database as global ($tab[0] contain the
    // variable name so ${$tab[0]} will define the var
    global $$option;
    $$option = $value;
    $sess->register("$option");
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's preferences into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_prefs() {
  global $sess, $auth, $last_v;
  global $todo_1_id, $todo_1_title, $todo_2_id, $todo_2_title;
  global $todo_3_id, $todo_3_title, $todo_4_id, $todo_4_title;
  global $todo_5_id, $todo_5_title;

  $obm_q = run_query_get_user_prefs($auth->auth["uid"]);
  while ($obm_q->next_record()) {
    $option = $obm_q->f("userobmpref_option");
    $value = $obm_q->f("userobmpref_value");
    global $$option;
    $$option = $value;
    $sess->register("$option");
    // Last visit management (if last_visit entry, get the associated text)
    if (substr($option, 0, 5) == "last_") {
      $entity = substr($option, 5);
      $function_get_last_text = "get_last_${entity}_text";
      $last_v[$entity]["id"] = $$option;
      $last_v[$entity]["text"] = $function_get_last_text($$option);
    }
  }

  $sess->register("last_v");

  $todo_1_title = run_query_global_todo_title($todo_1_id);
  $sess->register("todo_1_title");
  $todo_2_title = run_query_global_todo_title($todo_2_id);
  $sess->register("todo_2_title");
  $todo_3_title = run_query_global_todo_title($todo_3_id);
  $sess->register("todo_3_title");
  $todo_4_title = run_query_global_todo_title($todo_4_id);
  $sess->register("todo_4_title");
  $todo_5_title = run_query_global_todo_title($todo_5_id);
  $sess->register("todo_5_title");
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
// Get the Todo label from the contract Id
// Parameters:
//   - $id : user id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_todo_title($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "select todo_title from Todo where todo_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
    $obm_db->next_record();
    return $obm_db->f("todo_title");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company contact number                                            //
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function get_company_contact_number($id) {
  global $cdg_sql;

  $query = "select count(contact_id) as nb from Contact
            where contact_company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company active (non archived) deal number                         //
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_company_active_deal_number($id) {
  global $cdg_sql;

  $query = "select count(deal_id) as nb from Deal
            where deal_company_id='$id' and deal_archive='0'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company total deal number
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_company_total_deal_number($id) {
  global $cdg_sql;

  $query = "select count(deal_id) as nb from Deal
            where deal_company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
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
  $query = "select userobm_lastname, userobm_firstname, userobm_email
    from UserObm
    where userobm_id != '$id'";

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

  $query = "select group_name, group_email
    from UGroup
    where group_id != '$gid'";

  display_debug_msg($query, $cdg_sql);
  $g_q = new DB_OBM;
  $g_q->query($query);

  while ($g_q->next_record()) {
    $email = $g_q->f("group_email");
    $name = "Groupe : " . $g_q->f("group_name");
    $mails["$email"] = "$name";
  }


  $query = "select list_name, list_email
    from List
    where list_id != '$lid'";

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
// Send a Mail to an user                                                    //
// Parameters:
//  - $subject    : mail subject
//  - $message    : mail content
//  - $recipients : Id Recipients array
//  - $force      : Mail should be forced (all user even with no set_mail)
///////////////////////////////////////////////////////////////////////////////
function send_mail($subject, $message, $recipients, $force) {
  global $cgp_mail_enabled;
  $message = stripslashes($message);
  if (! $cgp_mail_enabled) {
    return 0;
  }

  $send_q = run_query_get_sender();
  $reci_q = run_query_get_recipients($recipients, $force);

  while ($reci_q->next_record()) {
    $reci = $reci_q->f("userobm_email");
    if ($reci_list == "") {
      $reci_list = $reci;
    } else {
      $reci_list .= " , $reci";
    }
  }
  $headers .= "From: ".$send_q->f("userobm_email");
  if ($reci_list != "") {
    mail ($reci_list, $subject, $message, $headers);      
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get recipients mail addresses                                             //
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
  $query = "SELECT userobm_email FROM UserObm $join WHERE userobm_id IN (";
  $coma = "";
  foreach($recipients as $recipient) {
    $query.= $coma.$recipient;
    $coma = ",";
  }
  $query.=") AND userobm_email != '' $mail_filter";
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
  
?>
