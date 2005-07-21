<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_query.inc                                                 //
//     - Desc : OBM Framework SQL and query Objects                          //
// 2005-06-29 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//--- SQL handling ----------------------------------------------------------//

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


//--- User and Display preferences handling----------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Query Execution - get the User's entity display preferences
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $all    : if not 0, get all fields (even those marked not to display)
// Returns:
//   - DBO : ordered list of entity fields to display
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref($u_id, $entity, $all=0) {
  global $cdg_sql;
  
  $query = "SELECT *
    FROM DisplayPref
    WHERE display_user_id='$u_id'
      AND display_entity='$entity'";
  if ($all == 0) { 
    $query .= " AND display_display != 0";
  }
  $query .= " ORDER BY display_fieldorder"; 

  display_debug_msg($query, $cdg_sql, "run_query_display_pref()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;	
}      


///////////////////////////////////////////////////////////////////////////////
// Get the User's entity display preferences
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $all    : if not 0, get all fields (even those marked not to display)
// Returns:
//   - $prefs  : prefs[$fieldname] : ["order"], ["status"], ["notdefault"]
///////////////////////////////////////////////////////////////////////////////
function get_display_pref($u_id, $entity, $all=0) {
  global $cdg_sql;

  if ($all == 0) {
    $hide = true;
  }
  
  $query = "SELECT *
    FROM DisplayPref
    WHERE (display_user_id='0' OR display_user_id='$u_id')
      AND display_entity='$entity'
    ORDER BY display_user_id, display_fieldorder"; 

  display_debug_msg($query, $cdg_sql, "run_query_display_pref()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f("display_user_id");
    $fieldname = $obm_q->f("display_fieldname");
    $fieldorder = $obm_q->f("display_fieldorder");
    $fieldstatus = $obm_q->f("display_display");

    // For specific user values, get them only if defined
    if ($user_id > 0) {
      if (isset($prefs[$fieldname])) {
	// Note if user value different from default value
        if ( ($prefs[$fieldname]["order"] != $fieldorder)
             || ($prefs[$fieldname]["status"] != $fieldstatus) ) {
          $prefs[$fieldname]["notdefault"] = true;
        }
	// If get only not hidden fields
        if ($hide) {
          if ($fieldstatus == 0) {
            $hidden[$fieldname] = true;
	    // If user has set the field visible, show it
          } elseif (($hidden[$fieldname] == true) && ($fieldstatus != 0)) {
            unset($hidden[$fieldname]);
          }
        }
        $index_sort[$prefs[$fieldname]["order"]] = $fieldorder;
        $prefs[$fieldname]["order"] = $fieldorder;
        $prefs[$fieldname]["status"] = $fieldstatus;
      }
      // Default values (user id = 0)
    } else {
      $prefs[$fieldname]["order"] = $fieldorder;
      $prefs[$fieldname]["status"] = $fieldstatus;
      $index_sort[$fieldorder] = $fieldorder;
      if (($hide) && ($fieldstatus == 0)) {
        $hidden[$fieldname] = true;
      }
    }
  }

  // Sort the $prefs array according to the final fieldorder
  array_multisort($index_sort, $prefs);

  // If we do not show hidden fields, we remove these from result
  if ($hide) {
    while (list($key, $value) = each($hidden)) {
      unset($prefs[$key]);
    }
  }

  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Get One User's entity field display preference
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $field  : field
// Returns:
//   - $prefs  : prefs[$fieldname] : ["order"], ["status"], ["notdefault"]
///////////////////////////////////////////////////////////////////////////////
function get_one_display_pref($u_id, $entity, $field) {
  global $cdg_sql;
  
  $query = "SELECT *
    FROM DisplayPref
    WHERE (display_user_id='0' OR display_user_id='$u_id')
      AND display_entity='$entity'
      AND display_fieldname='$field'
    ORDER BY display_user_id"; 

  display_debug_msg($query, $cdg_sql, "get_one_display_pref()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f("display_user_id");
    $fieldorder = $obm_q->f("display_fieldorder");
    $fieldstatus = $obm_q->f("display_display");

    $prefs[$user_id]["order"] = $fieldorder;
    $prefs[$user_id]["status"] = $fieldstatus;
  }

  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new display value for the entity, field
// Parameters:
//   - $entity     : entity affected
//   - $fieldname  : fieldname to change the display value
//   - $new_status : new display status value; "" if not updated
//   - $new_order  : new display order; "" if not updated
///////////////////////////////////////////////////////////////////////////////
function update_display_pref($entity, $fieldname, $new_status, $new_order="") {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $prefs = get_one_display_pref($uid, $entity, $fieldname);
  $default_order = $prefs[0]["order"];
  $default_status = $prefs[0]["status"];
  $user_order = $prefs[$uid]["order"];
  $current_order = ($user_order > 0 ? $user_order : $default_order);

  // if order is updated, update other field concerned too (with current order)
  if ($new_order != "") {
    run_query_display_pref_level_update($uid, $entity, $new_order, $current_order);
  } else {
    $new_order = $current_order;
  }

  // if new field values = default values and user value exists, erase it
  if ($new_status == $default_status) {
    if ($new_order == $default_order) {
      
      $query = "DELETE FROM DisplayPref
      WHERE display_user_id='$uid'
        AND display_entity='$entity'
        AND display_fieldname='$fieldname'";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM; 
      $obm_q->query($query);
    } else {
      
      // if user entry does not exist
      if (! isset($prefs[$uid])) {
	run_query_display_pref_insert($uid, $entity, $fieldname, $new_status, $new_order);
      } else {
	// user entry is different (=> update it)
	run_query_display_pref_update($uid, $entity, $fieldname, $new_status, $new_order);
      }
    }
    
  } else {  // $new status != default status
    
    if (isset($prefs[$uid])) {
      // if user entry exists, we update it
      run_query_display_pref_update($uid, $entity, $fieldname, $new_status, $new_order);
    } else {
      // user entry does not exist, we create it
      run_query_display_pref_insert($uid, $entity, $fieldname, $new_status, $default_order);
    }
  }
  
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Update a display value for the entity, user, field
// Parameters:
//   - $u_id      : user id
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $status    : field status to set
//   - $order     : [opt] field order to set
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_update($u_id, $entity, $fieldname, $status, $order="") {
  global $auth, $cdg_sql;

  $coma = "";
  if ($status != "") {
    $update_status = "$coma
      display_display='$status'";
    $coma = ",";
  }
  if ($order != "") {
    $update_order = "$coma
      display_fieldorder='$order'";
    $coma = ",";
  }
  
  if ($coma != "") {
    $query = "UPDATE DisplayPref SET
      $update_status
      $update_order
    WHERE display_user_id='$u_id'
      AND display_entity='$entity'
      AND display_fieldname='$fieldname'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM; 
    $obm_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Create a display value for the entity, user, field
// Parameters:
//   - $u_id      : user id
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $status    : field status to set
//   - $order     : field order to set
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_insert($u_id, $entity, $fieldname, $status, $order) {
  global $auth, $cdg_sql;

  $query = "INSERT INTO DisplayPref (
      display_user_id,
      display_entity,
      display_fieldname,
      display_fieldorder,
      display_display)
    VALUES (
      '$u_id',
      '$entity',
      '$fieldname',
      '$order',
      '$status')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM; 
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new display value for the entity, field
// Parameters:
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $disstatus : current display value
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_update_old($entity, $fieldname, $disstatus) {
  global $auth, $cdg_sql;

  // Invert the display value (0,1)  
  $new_display = ($disstatus == 0) ? 1 : 0;
  $query = "UPDATE DisplayPref SET
      display_display='$new_display'
    WHERE display_user_id='" . $auth->auth["uid"] . "'
      AND display_entity='$entity'
      AND display_fieldname='$fieldname'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ; 
  $obm_q->query($query) ;
}


///////////////////////////////////////////////////////////////////////////////
// Update the level of the field in the entity given identified by the order
// Parameters:
//   - $u_id       : user id
//   - $entity     : entity affected
//   - $fieldorder : actual position
//   - $new_level  : new position
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_level_update($u_id, $entity, $fieldorder, $new_level) {
  global $cdg_sql;

  $query = "UPDATE DisplayPref
    SET display_fieldorder='$new_level'
    WHERE display_user_id='$u_id'
      AND display_entity='$entity'
      AND display_fieldorder='$fieldorder'";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Insert (or Reset) User parameters to default values
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
  $query = "DELETE FROM UserObmPref WHERE userobmpref_user_id='$user_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //-- We get default parameters (user id 0)
  $query = "SELECT * from UserObmPref WHERE userobmpref_user_id='0'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $line_q = new DB_OBM;
  while ($obm_q->next_record()) {
    $option = $obm_q->f("userobmpref_option");
    $value = $obm_q->f("userobmpref_value");

    $query = "INSERT INTO UserObmPref
        (userobmpref_user_id, userobmpref_option, userobmpref_value)
      VALUES
        ('$user_id', '$option', '$value')";
    display_debug_msg($query, $cdg_sql);
    $line_q->query($query);
  }

  //---------------------------------------------------------------------------
  // Default Display Parameters
  // We copy each entry with user id = 0 to the new user
  //---------------------------------------------------------------------------

  $obm_q = new DB_OBM;
  //-- Drop current user preferences
  $query = "DELETE FROM DisplayPref WHERE display_user_id='$user_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //-- We get default parameters (user id 0)
  $query = "SELECT * FROM DisplayPref WHERE display_user_id='0'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $line_q = new DB_OBM;
  while ($obm_q->next_record()) {
    $entity = $obm_q->f("display_entity");
    $fieldname = $obm_q->f("display_fieldname");
    $fieldorder = $obm_q->f("display_fieldorder");
    $disstatus = $obm_q->f("display_display");

    $query = "INSERT INTO DisplayPref (
        display_user_id,
        display_entity,
        display_fieldname,
        display_fieldorder,
        display_display)
      VALUES
        ('$user_id', '$entity', '$fieldname', '$fieldorder', '$disstatus')";
    display_debug_msg($query, $cdg_sql);
    $line_q->query($query);
  }

  return true;
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
  $query = "DELETE FROM UserObmPref
    WHERE userobmpref_user_id ='$user_id'
      AND userobmpref_option = '$option'";   

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ;
  $ret = $obm_q->query($query);

  // Insert new value
  $query = "INSERT INTO UserObmPref
      (userobmpref_user_id, userobmpref_option, userobmpref_value)
    VALUES
      ('$user_id', '$option', '$value')";
  display_debug_msg($query, $cdg_sql);
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the preferences of the specified user
///////////////////////////////////////////////////////////////////////////////
function run_query_get_user_prefs($p_user_id) {
  global $cdg_sql;

  $query = "SELECT * FROM UserObmPref WHERE userobmpref_user_id='$p_user_id'";
  // XXX Problem because called before include global.inc in setting module
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


//--- Login / Logout global functions ---------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Update the login date of the user
// Parameters:
//   - $id : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_update_lastaccess($id) {
  global $cdg_sql;

  $query = "UPDATE UserObm
    SET userobm_timelastaccess='".date("Y-m-d H:i:s")."'
    WHERE userobm_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Action on ActiveUserObm and UserObm_Session Log during  //
// the logout
///////////////////////////////////////////////////////////////////////////////
function run_query_logout() {
  global $sess, $cdg_sql;

  // get the Active connexion
  $obm_q = new DB_OBM ; 
  $query = "SELECT *
    FROM ActiveUserObm 
    WHERE activeuserobm_sid = '".$sess->id."'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;

  if($obm_q->next_record()) {
    // Log the connexion in the session log
    run_query_log_session($obm_q);

    // drop the connexion from the active user session table
    $query = "DELETE FROM ActiveUserObm
      WHERE activeuserobm_sid = '".$obm_q->f('activeuserobm_sid')."'";
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
// Get the user text from the user Id
// Parameters:
//   - $id : user id 
///////////////////////////////////////////////////////////////////////////////
function get_last_user_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT userobm_login FROM UserObm WHERE userobm_id='$id'";
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
    $query = "SELECT group_name FROM UGroup WHERE group_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("group_name");
  }
  return;
}


//--- User and group global functions ---------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Get users infos from the uid array given
// Parameters:
//   - users : array of users id
// Used by: Agenda, todo
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
//   - $login : login
// Returns:
//   - user's Id
// Used by: user, client import
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
// Used by: time only !!
///////////////////////////////////////////////////////////////////////////////
function get_user_info($id="") {
  global $cdg_sql, $auth;

  $obm_q = new DB_OBM ;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, "userobm_datebegin", "datebegin");

  if ($id == "") $id = $auth->auth["uid"];

  $query = "SELECT userobm_login,
      userobm_perms,
      userobm_lastname,
      userobm_firstname,
      userobm_datebegin,
      $datebegin
    FROM UserObm
    WHERE userobm_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record() ;
  $u["login"] = $obm_q->f("userobm_login");
  $u["perms"] = $obm_q->f("userobm_perms");
  $u["lastname"] = $obm_q->f("userobm_lastname");
  $u["firstname"] = $obm_q->f("userobm_firstname");
  $u["datebegin"]["iso"] = $obm_q->f("userobm_datebegin");
  $u["datebegin"]["unix_timestamp"] = $obm_q->f("datebegin");

  return $u;
}


///////////////////////////////////////////////////////////////////////////////
// Userobm query execution : All users
// Parameters:
//   - $id (optionnal) : UserObm Id 
// Return:
//   - Database Object : userobm list
// Used by: deal, contract, incident
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm($id="") {
  global $cdg_sql;

  if ($id) $where = "AND userobm_id='$id'";
  $query = "SELECT *
    FROM UserObm
    WHERE userobm_archive = 0
      $where
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_userobm()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Active Userobm query execution : All users not archived
// Parameters:
//   - $ids (optionnal) : array of UserObm Id to add to the result
// Return:
//   - Database Object : userobm list
// Used by: company, contact, deal, list, invoice, incident
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_active($ids="") {
  global $cdg_sql;

  if (is_array($ids)) {
    $where_add = "";
    $nb_add = count($ids);
    for ($i=0; $i<$nb_add; $i++) {
      if ($ids[$i] != "") {
        $where_add .= " OR userobm_id = '$ids[$i]'";
      }
    }
  }

  $query = "SELECT * FROM UserObm
    WHERE userobm_archive != '1'
      $where_add
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg("run_query_userobm_active() : $query", $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the users list from the given group
// Recursive search to handle group trees
// Parameters:
//   - $g_id : Group id
//   - $ids (optionnal) : array of UserObm Id to add to the result
// Used by: company, contact, deal, project, contract, import
///////////////////////////////////////////////////////////////////////////////
function run_query_all_users_from_group($g_id, $ids="") {
  global $cdg_sql;

  $gusers = get_all_users_from_group($g_id);

  if (is_array($gusers)) {
    $where_gusers = "";
    $nb_add = count($gusers);
    for ($i=0; $i<$nb_add; $i++) {
      if ($gusers[$i] != "") {
        $where_gusers .= " OR userobm_id = '$gusers[$i]'";
      }
    }
  }

  if (is_array($ids)) {
    $where_ids = "";
    $nb_add = count($ids);
    for ($i=0; $i<$nb_add; $i++) {
      if ($ids[$i] != "") {
        $where_ids .= " OR userobm_id = '$ids[$i]'";
      }
    }
  }

  $query = "SELECT userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM UserObm
    WHERE (userobm_archive != '1' and (1=0 $where_gusers))
      $where_ids
    ORDER BY userobm_lastname"; 
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
// Used by: agenda, deal
///////////////////////////////////////////////////////////////////////////////
function get_all_users_from_group($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $users = array();

  // Groups members of this group
  $query = "SELECT groupgroup_child_id as child_id
      FROM GroupGroup
      WHERE groupgroup_parent_id='$g_id'";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $child_id = $obm_q->f("child_id");
    $gusers = get_all_users_from_group($child_id);
    $users = array_merge($users, $gusers);
  }

  // Users members of this group
  $query = "SELECT userobmgroup_userobm_id as user_id
    FROM UserObmGroup
    WHERE userobmgroup_group_id='$g_id'
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


//--- Mail global functions -------------------------------------------------//


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
function send_mail($subject, $message, $recipients, $r_addr, $force="") {
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
  if ($aliasuite_enabled) {
    $domain_q = run_query_get_domain();
    $local_domain = $domain_q->f("mail_value");
  }

  // Construct recipient list
  if (is_object($reci_q)) {
    while ($reci_q->next_record()) {
      $reci = $reci_q->f("userobm_email");
    
      // If AliaSuite, construct recipient address.
      if ($aliasuite_enabled) {
	$reci = strtok($reci, "\r\n")."@".$local_domain;
      }
    
      if ($reci_list == "") {
	$reci_list = $reci;
      } else {
	$reci_list .= " , $reci";
      }
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
  } else {
    $headers .= $send_q->f("userobm_email");
  }
  
  if ($reci_list != "") {
    // echo "<p>mail: $reci_list<br>$subject<br>$message<br>$headers";
    mail ($reci_list, $subject, $message, $headers);      
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get recipients mail addresses
// If no recipient given, no list returned
// Parameters:
//  - $recipients : Id Recipients array
//  - $force      : Mail should be forced (all user even with no set_mail)
// Returns: DB with email recipients, or false if no valid recipients
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
    if ($recipient) {
      $user_list .= $coma.$recipient;
      $coma = ",";
    }
  }
  if ($user_list != "") {
    $user_in = "userobm_id IN ($user_list) AND";
  } else {
    return false;
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
// Get sender mail address
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
// Get domain name (aliasuite mode)
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


//--- Others global functions -----------------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Get the module list
// Return : $modules : array of modules
// Used by: admin_code, admin_lang
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


 
?>
