<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_pref_query.inc                                         //
//     - Desc : Pref admin Query File                                        //
// 2002-08-10 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Global Pref updates                                                       //
// Parameters:
//   - $pref : hash of values
///////////////////////////////////////////////////////////////////////////////
function update_global_pref($pref) {

  // Lifetime update
  $lifetime = $pref["lifetime"];
  $retour = run_query_lifetime_update($lifetime);

  // Session Cookie flag update
  $session_cookie = ($pref["session_cookie"] ? 1 : 0 );
  $retour = run_query_session_cookie_update($session_cookie);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Global Pref lifetime update                                               //
// Parameters:
//   - $lifetime : new lifetime to set
///////////////////////////////////////////////////////////////////////////////
function run_query_lifetime_update($lifetime) {
  global $auth, $cdg_sql;

  $query = "update GlobalPref set
              globalpref_value = '$lifetime'
            where
              globalpref_option = 'lifetime'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Global Pref session_cookie update                                         //
// Parameters:
//   - $session_cookie : new session_cookie flag to set (1 true, 0 false)
///////////////////////////////////////////////////////////////////////////////
function run_query_session_cookie_update($session_cookie) {
  global $auth, $cdg_sql;

  $query = "update GlobalPref set
              globalpref_value = '$session_cookie'
            where
              globalpref_option = 'session_cookie'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the lifetime global pref value                                        //
// Returns:
//   - $lifetime value
///////////////////////////////////////////////////////////////////////////////
function get_global_pref_lifetime() {
  global $cdg_sql;

  $query = "select globalpref_value
            from GlobalPref
            where globalpref_option = 'lifetime'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("globalpref_value");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the session_cookie flag global pref value                             //
// Returns:
//   - $session_cookie flag value (1 | 0)
///////////////////////////////////////////////////////////////////////////////
function get_global_pref_session_cookie() {
  global $cdg_sql;

  $query = "select globalpref_value
            from GlobalPref
            where globalpref_option = 'session_cookie'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("globalpref_value");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Admin Preferences Form Data checking and formatting                       //
// Parameters:
//   - $pref[]   : values checked
//     keys used : lifetime
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($pref) {
  global $l_fill_lifetime, $l_j_check_lifetime, $php_regexp_lifetime;
  global $err_msg;

  $lifetime = $pref["lifetime"];

  // MANDATORY: Session lifetime
  if (trim($lifetime) == "") {
    $err_msg = $l_fill_lifetime;
    return false;
  } else if (ereg($php_regexp_lifetime, $lifetime) == false) {
    $err_msg = $l_j_check_lifetime . " : $lifetime";
    return false;
  }

  return true; 
}
