<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_query.inc                                             //
//     - Desc : Global Query File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the Id of one User from its login / pass
// Parameters:
//   - $login  : login
//   - $passwd : password
// Returns:
//   - user's Id
///////////////////////////////////////////////////////////////////////////////
function run_query_id_user($login, $passwd) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  if (isset($login) && isset($passwd)) {
    $query = "select userobm_id from UserObm where userobm_login='$login' and userobm_password='$passwd' " ;
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query) ;
    $connect_db->next_record() ;
    return $connect_db->f("userobm_id") ;
  } else {
    return "" ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Userobm query execution                                                   //
// Parameters:
//   - $id (optionnal) : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm($id="") {
  global $cdg_sql;

  if ($id) $where = "where userobm_id='$id'";
  $query = "select * from UserObm $where order by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg("run_query_userobm() : $query", $cdg_sql);

  return $obm_q;
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

  $query = "update UserObmPref
            set userobmpref_value='$value'
            where userobmpref_user_id ='$user_id'
              and userobmpref_option = '$option'";   

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM ;
  $obm_q->query($query);
}


/////////////////////////////////////////////////////////////////////////////
// Return all the preferences of the specified user
/////////////////////////////////////////////////////////////////////////////
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
  $obm_q_name_var = run_query_get_global_prefs();
  while ($obm_q_name_var->next_record()) {
    $tab[0]=$obm_q_name_var->f("globalpref_option");
    $tab[1]=$obm_q_name_var->f("globalpref_value");

    // Define the variable in the database as global ($tab[0] contain the
    // variable name so ${$tab[0]} will define the var
    global ${$tab[0]};
    ${$tab[0]}=$tab[1];
    $sess->register("$tab[0]");
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's preferences into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_prefs() {
  global $sess, $auth;
  global $last_company_name,$last_contact_name,$last_deal_name,$last_list_name;
  global $last_contract_name, $last_incident_name;
  global $last_account_name, $last_invoice_name, $last_payment_name;

  $obm_q_name_var = run_query_get_user_prefs($auth->auth["uid"]);
  while ($obm_q_name_var->next_record()) {
    $tab[0]=$obm_q_name_var->f("userobmpref_option");
    $tab[1]=$obm_q_name_var->f("userobmpref_value");
    global ${$tab[0]};
    $$tab[0]=$tab[1];
    $sess->register("$tab[0]");
  }
  $last_company_name = run_query_global_company_name($last_company);
  $sess->register("last_company_name");
  $last_contact_name = run_query_global_contact_name($last_contact);
  $sess->register("last_contact_name");
  $last_deal_name = run_query_global_deal_label($last_deal);
  $sess->register("last_deal_name");
  $last_list_name = run_query_global_list_name ($last_list); 
  $sess->register("last_list_name");
  $last_account_name = run_query_global_account_label($last_account);
  $sess->register("last_account_name");
  $last_invoice_name = run_query_global_invoice_label ($last_invoice);
  $sess->register("last_invoice_name");
  $last_payment_name = run_query_global_payment_label ($last_payment);
  $sess->register("last_payment_name");
  $last_contract_name = run_query_global_contract_label ($last_contract); 
  $sess->register("last_contract_name");
  $last_incident_name = run_query_global_incident_label ($last_incident); 
  $sess->register("last_incident_name");

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
// Get the company name from the company Id
// Parameters:
//   - $c_id : company id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_company_name($c_id) {
  global $cdg_sql;
  
  $query = "select company_name from Company where company_id='$c_id'";
  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("company_name");
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact name from the contact Id
// Parameters:
//   - $c_id : contact id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_contact_name($c_id) {
  global $cdg_sql;
  
  $query = "select contact_lastname from Contact where contact_id='$c_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("contact_lastname");
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal label from the deal Id
// Parameters:
//   - $d_id : deal id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_deal_label($d_id) {
  global $cdg_sql;

  $query = "select deal_label from Deal where deal_id='$d_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("deal_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the list name from the list Id
// Parameters:
//   - $l_id : list id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_list_name($l_id) {
  global $cdg_sql;
  
  $query = "select list_name from List where list_id='$l_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("list_name");
}


///////////////////////////////////////////////////////////////////////////////
// Get the account label from the account Id
// Parameters:
//   - $a_id : account id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_account_label($a_id) {
  global $cdg_sql;

  $query = "select account_label from Account where account_id='$a_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("account_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the invoice label from the invoice Id
// Parameters:
//   - $a_id : invoice id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_invoice_label($i_id) {
  global $cdg_sql;
  
  $query = "select invoice_label from Invoice where invoice_id='$i_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("invoice_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the payment label from the payment Id
// Parameters:
//   - $p_id : payment id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_payment_label($p_id) {
  global $cdg_sql;
  
  $query = "select payment_label from Payment where payment_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("payment_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract label from the contract Id
// Parameters:
//   - $c_id : contract id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_contract_label($c_id) {
  global $cdg_sql;
  
  $query = "select contract_label from Contract where contract_id='$c_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("contract_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the incident label from the incident Id
// Parameters:
//   - $i_id : incident id 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_incident_label($i_id) {
  global $cdg_sql;
  
  $query = "select incident_label from Incident where incident_id='$i_id'";
  // Problem because called before include global.inc in setting module
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("incident_label");
}


///////////////////////////////////////////////////////////////////////////////
// Get the company contact number                                            //
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function get_company_contact_number($id) {
  global $cdg_sql;

  $query = "select count(contact_id) from Contact
            where contact_company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("count(contact_id)");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company active (non archived) deal number                         //
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_company_active_deal_number($id) {
  global $cdg_sql;

  $query = "select count(deal_id) from Deal
            where deal_company_id='$id' and deal_archive='0'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("count(deal_id)");
  return $retour;
}
///////////////////////////////////////////////////////////////////////////////
// Get the configuration variable in the database this variable could be set
// in the administration module.
// Parameters:
//   - none   
///////////////////////////////////////////////////////////////////////////////

function run_query_get_config_var() {
  global $cdg_sql;

  $query = "select * from ConfigObmVars";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

?>
