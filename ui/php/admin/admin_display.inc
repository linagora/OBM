<script language="php">
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
function dis_help($mode) {

  if ($mode == "html")
    echo "<table class=\"details\">
      <tr>
        <td><pre>";

  include ("admin_help.inc");

  if ($mode == "html")
    echo "</pre>
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
function dis_index($mode, $lifetime) {

  switch ($mode) {
  case "txt":
    echo "try -h to help\n";
    break;
  case "html":
    $nb_user_ses = get_usersession_number();
    $obm_u = run_query_active_user($lifetime);
    html_admin_status($lifetime, $nb_user_ses, $obm_u);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Admin Status                                                      //
// Parameters
//   - $lifetime    : session lifetime
//   - $nb_user_ses : user session number
//   - $obm_u       : DBO : List of Active User Session
///////////////////////////////////////////////////////////////////////////////
function html_admin_status($lifetime, $nb_user_ses, $obm_u) {
  global $l_check, $l_database_server, $l_database,$l_last_page,$l_user_ip,$l_execute;
  global $l_session_stats, $l_session, $l_user_session, $l_lifetime, $l_active_user, $l_login_date, $l_last_acces,$l_nb_connec;
  global $obm_version, $l_version, $l_date, $l_host;

  $date = datetime_format();
  if ($lifetime > 0) {
    $nb_sess_to_clear = run_query_count_passive_sess($lifetime);
    $d_lifetime = $lifetime;
  } else {
    $nb_sess_to_clear = 0;
    $d_lifetime = "unlimited";
  }

  $nbua = $obm_u->nf();
  $list_user = "";
  while ($obm_u->next_record()) {
    $last_acces = datetime_format($obm_u->f("timeupdate"));
    $login = $obm_u->f("userobm_login");
    $login_date = datetime_format($obm_u->f("timecreate"));
    $nb_con = $obm_u->f("activeuserobm_nb_connexions");
    $lastpage = $obm_u->f("activeuserobm_lastpage");
    $ip = $obm_u->f("activeuserobm_ip");
    $list_user .= "
      <tr>
      <td class=\"adminText\">$login</td>
      <td class=\"adminText\">$login_date</td>
      <td class=\"adminText\">$last_acces</td>
      <td class=\"adminText\">$nb_con</td>
      <td class=\"adminText\">$lastpage</td>
      <td class=\"adminText\">$ip</td>
      </tr>";
  }

  echo "
    <br />
    <table class=\"admin\">
    <tr> 
      <td class=\"adminHead\" colspan=\"2\">$l_check</td>
    </tr><tr> 
      <td class=\"adminLabel\">$l_version</td>
      <td class=\"adminText\">$obm_version</td>
    </tr><tr> 
      <td class=\"adminLabel\">$l_database_server</td>
      <td class=\"adminText\">". strtoupper($obm_u->type). "</td>
    </tr><tr>
      <td class=\"adminLabel\">$l_database</td>
      <td class=\"adminText\">" . $obm_u->Database . "</td>
    </tr><tr>
      <td class=\"adminLabel\">$l_host</td>
      <td class=\"adminText\">" . $obm_u->Host . "</td>
    </tr>
    </table>

  <!--  Mini stats section -->

    <p />
    <table class=\"admin\">
    <tr> 
      <td class=\"adminHead\" colspan=\"6\">$l_session_stats</td>
    </tr><tr> 
      <td class=\"adminLabel\" colspan=\"4\">$l_date</td>
      <td class=\"adminText\" colspan=\"2\">$date</td>
    </tr><tr> 
      <td class=\"adminLabel\" colspan=\"4\">$l_lifetime</td>
      <td class=\"adminText\" colspan=\"2\">$lifetime</td>
    </tr><tr> 
      <td class=\"adminLabel\" colspan=\"4\">$l_user_session</td>
      <td class=\"adminText\" colspan=\"2\">$nb_user_ses</td>
    </tr><tr align=center> 
      <td class=\"adminHead\">$l_active_user</td>
      <td class=\"adminHead\">$l_login_date</td>
      <td class=\"adminHead\">$l_last_acces</td>
      <td class=\"adminHead\">$l_nb_connec</td>
      <td class=\"adminHead\">$l_last_page</td>
      <td class=\"adminHead\">$l_user_ip</td>

    </tr>
    $list_user
    </table>

    <br />
    <form method=\"get\" action=\"" . url_prepare("admin_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : clear_sess</td>
      <td class=\"adminHead\">Passive Sessions (delay > $d_lifetime)</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"clear_sess\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">Clear all passive users sessions</td>
      <td class=\"adminText\">$nb_sess_to_clear</td>
    </table>
    </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Clear the Sessions and file it       				     //
// Parameters:								     //
//   - $mode     : "txt" or "html"                                           //
//   - $lifetime : session lifetime
///////////////////////////////////////////////////////////////////////////////
function dis_clear_sess($mode, $lifetime) {
  global $cdg_sql;

  if ($lifetime > 0) {
    $date_limit = date("Y-m-d H:i:s",time() - $lifetime);
    $obm_q = run_query_passive_user($lifetime);

    while($obm_q->next_record()) {
      run_query_log_session($obm_q);
      run_query_activeuserobm_delete($obm_q->f('activeuserobm_sid'));
    }
  } else {
    echo "Sessions are unlimited (lifetime = 0) !";
  }

  if ($mode == "html")
    dis_index($mode, $lifetime);

}
