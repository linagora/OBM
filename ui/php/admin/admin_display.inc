<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_display.inc                                            //
//     - Desc : Admin Display File                                           //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_help($mode) {

  if ($mode == "html")
    echo "<table class=\"details\">
      <tr>
        <td>";

  include ("admin_help.inc");

  if ($mode == "html")
    echo "
        </td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode     : "txt" ou "html"
//   - $lifetime : session lifetime
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index($mode, $lifetime) {

  switch ($mode) {
  case "txt":
    echo "try -h to help\n";
    break;
  case "html":
    $nb_user_ses = get_admin_usersession_number();
    $obm_u = run_query_admin_active_user($lifetime);
    html_admin_status($lifetime, $nb_user_ses, $obm_u);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Admin Status
// Parameters
//   - $lifetime    : session lifetime
//   - $nb_user_ses : user session number
//   - $obm_u       : DBO : List of Active User Session
///////////////////////////////////////////////////////////////////////////////
function html_admin_status($lifetime, $nb_user_ses, $obm_u) {
  global $l_check, $l_database_server, $l_database,$l_last_page,$l_user_ip,$l_execute;
  global $l_session_stats, $l_session, $l_user_session, $l_lifetime, $l_active_user, $l_login_date, $l_last_acces,$l_nb_connec;
  global $obm_version, $l_version, $l_date, $l_host, $l_domain, $c_multidomain;

  $date = of_datetime_format();
  if ($lifetime > 0) {
    $nb_sess_to_clear = run_query_admin_count_passive_sess($lifetime);
    $d_lifetime = $lifetime;
  } else {
    $nb_sess_to_clear = 0;
    $d_lifetime = "unlimited";
  }

  $nbua = $obm_u->nf();
  $list_user = "";
  while ($obm_u->next_record()) {
    $last_acces = of_datetime_format($obm_u->f("timeupdate"));
    $login = $obm_u->f("userobm_login");
    $login_date = of_datetime_format($obm_u->f("timecreate"));
    $nb_con = $obm_u->f("activeuserobm_nb_connexions");
    $lastpage = $obm_u->f("activeuserobm_lastpage");
    $ip = $obm_u->f("activeuserobm_ip");
    $list_user .= "
      <tr>
      <td>$login</td>
      <td>$login_date</td>
      <td>$last_acces</td>
      <td>$nb_con</td>
      <td>$lastpage</td>
      <td>$ip</td>
      </tr>";
  }
  
	$multidomain = $c_multidomain == true ? "Multi" : "Single";
	 
  echo "
    <div class=\"detail infos\">
    <h1>$l_check</h1>
    <table>
    <tr> 
      <th>$l_version</th>
      <td>$obm_version</td>
    </tr><tr>
      <th>$l_database_server</th>
      <td>". strtoupper($obm_u->type). "</td>
    </tr><tr>
      <th>$l_database</th>
      <td>" . $obm_u->Database . "</td>
    </tr><tr>
      <th>$l_host</th>
      <td>" . $obm_u->Host . "</td>
    </tr><tr>
      <th>$l_domain</th>
      <td>$multidomain</td>
    </tr>
    </table>
    </div>

    <form method=\"get\" action=\"" . url_prepare("admin_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Sessions</h1>
    <table>
    <tr>
      <th>Action : clear_sess</th>
      <td>Passive Sessions (delay > $d_lifetime)</td>
    </tr><tr>
      <th>Clear all passive users sessions</th>
      <td>$nb_sess_to_clear</td>
    
    </table>
    <div class=\"buttons\">  
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"clear_sess\" />
        <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>
    </form>
       
       
 

    <div class=\"detail extra\">
    <h1>$l_session_stats</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr> 
      <td colspan=\"6\">$l_session_stats</td>
    </tr>
    <tr> 
      <th>$l_date</th>
      <th>$l_lifetime</th>
      <th colspan=\"4\">$l_user_session</th>
    </tr>
    </thead>
    <tbody>
    <tr> 
    <td>$date</td>
    <td>$lifetime</td>
    <td>$nb_user_ses</td>
    </tr>
    </tbody>
    <thead>
    <tr> 
      <th>$l_active_user</th>
      <th>$l_login_date</th>
      <th>$l_last_acces</th>
      <th>$l_nb_connec</th>
      <th>$l_last_page</th>
      <th>$l_user_ip</th>
    </tr>
    </thead>
    <tbody>
    $list_user
    </tbody>
    </table>
    </div>

";
}


///////////////////////////////////////////////////////////////////////////////
// Clear the Sessions and file it
// Parameters:
//   - $mode     : "txt" or "html"
//   - $lifetime : session lifetime
///////////////////////////////////////////////////////////////////////////////
function dis_admin_clear_sess($mode, $lifetime) {
  global $cdg_sql;

  // If session lifetime not set, clear all sessions older than 1 day
  if ($lifetime > 0) {
    $ltime = $lifetime;
  } else {
    $ltime = 86400;
  }

  $date_limit = date("Y-m-d H:i:s",time() - $ltime);
  $obm_q = run_query_admin_passive_user($ltime);

  while($obm_q->next_record()) {
    run_query_log_session($obm_q);
    run_query_admin_activeuserobm_delete($obm_q->f('activeuserobm_sid'));
  }

  if ($mode == "html") {
    dis_admin_index($mode, $ltime);
  }
}
?>
