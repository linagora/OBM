<SCRIPT language="php">
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

  if ($mode == "html") echo "<table border=1><tr><td><pre>\n";
  include ("admin_help.inc");
  if ($mode == "html") echo "\n</pre></td></tr></table>";

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
    print_r($obm_u);
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
  global $col_textem, $col_a_table, $col_a_tableh, $col_a_text, $l_help;
  global $l_check, $l_database_server, $l_database,$l_last_page,$l_user_ip,$l_execute;
  global $l_session_stats, $l_session, $l_user_session, $l_lifetime, $l_active_user, $l_login_date, $l_last_acces,$l_nb_connec;
  global $sess, $obm_version, $l_version, $l_date, $l_host;

  $date = date("Y-m-d H:i:s");
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
    $a = $obm_u->f("activeuserobm_timecreate");
    $last_acces = $obm_u->f("timeupdate");
    $login = $obm_u->f("userobm_login");
    $nb_con = $obm_u->f("activeuserobm_nb_connexions");
    $lastpage = $obm_u->f("activeuserobm_lastpage");
    $ip = $obm_u->f("activeuserobm_ip");
    $login_date = substr($a,0,4)."-".substr($a,4,2)."-".substr($a,6,2)." ".substr($a,8,2).":".substr($a,10,2).":".substr($a,12,2);
    $list_user .= "<tr align=center>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$login</font></td>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$login_date</font></td>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$last_acces</font></td>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$nb_con</font></td>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$lastpage</font></td>
      <td bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$ip</font></td>
   </tr>";
}

  echo "
   <center>

    <a href=\"" . $sess->url("admin_index.php?action=help&amp;mode=html"). "\">$l_help</a>
    <p>
        <table border=1 width=50%>
    <tr align=center> 
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">&nbsp;$l_check&nbsp;</font></td>
    </tr><tr align=center> 
      <td width=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_version</font></td>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$obm_version</font></td>
    </tr><tr align=center> 
      <td width=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_database_server</font></td>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">". strtoupper($obm_u->type). "</font></td>
    </tr><tr align=center>
      <td width=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_database</font></td>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">" . $obm_u->Database . "</font></td>
    </tr><tr align=center>
      <td width=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_host</font></td>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">" . $obm_u->Host . "</font></td>
    </tr></table>
    </center>

  <!--  Mini stats section -->

    <p><center>
    <table border=1 >
    <tr align=center> 
      <td colspan=6 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">&nbsp;$l_session_stats&nbsp;</font></td>
    </tr><tr align=center> 
      <td colspan=4 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_date</font></td>
      <td colspan=2 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$date</font></td>
    </tr><tr align=center> 
      <td colspan=4 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_lifetime</font></td>
      <td colspan=2 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$lifetime</font></td>
    </tr><tr align=center> 
      <td colspan=4 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_user_session</font></td>
      <td colspan=2 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$nb_user_ses</font></td>
    </tr><tr align=center> 
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_active_user</font></td>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_login_date</font></td>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_last_acces</font></td>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_nb_connec</font></td>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_last_page</font></td>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_user_ip</font></td>

    </tr>
    $list_user
    </table>
    </center>
    <br>
    <form method=get action=\"" . $sess->url("admin_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : clear_sess<br>
        <font color=\"#$col_textem\">Clear all passive users sessions</font></td>
      <td>Passive Sessions (delay > $d_lifetime) :
        <br><center>$nb_sess_to_clear</center></td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=clear_sess>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
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
    $date_limit = date("YmdHis",time() - $lifetime);
    $obm_q = run_query_passive_user($lifetime);

    while($obm_q->next_record()) {
      run_query_log_session($obm_q);
      run_query_activeuserobm_delete($obm_q->f('activeuserobm_sid'));
    }
  } else {
    echo "Sessions are unlimited (lifetime = 0) !";
  }

  if($mode == "html")
    dis_index($mode, $lifetime);

}
