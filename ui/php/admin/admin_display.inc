<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_display.inc                                           //
//     - Desc  : Admin Display File                                          //
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
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_index($mode) {
  global $time_active_session;

  switch ($mode) {
  case "txt":
    echo "try -h to help\n";
    break;
  case "html":
    $nb_user_ses = get_usersession_number();
    $obm_u = run_query_active_user($time_active_session);
    html_admin_status( $nb_user_ses, $obm_u);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Admin Status                                                      //
// Parameters
//   - $nb_ses      : web session number
//   - $nb_user_ses : user session number
//   - $obm_u       : DBO : List of Active User Session
///////////////////////////////////////////////////////////////////////////////
function html_admin_status( $nb_user_ses, $obm_u) {
  global $col_textem, $col_a_table, $col_a_tableh, $col_a_text, $l_help;
  global $l_check, $l_database_server, $l_database,$l_last_page,$l_user_ip,$l_execute;
  global $l_session_stats, $l_session, $l_user_session, $l_active_user, $l_last_acces,$l_nb_connec,$sess;
  global $obm_version, $l_version, $time_active_session;

  $nb_sess_to_clear = run_query_count_passive_sess($time_active_session);
  $nbua= $obm_u->nf();
  $list_user="";
  while($obm_u->next_record()) {
    $a = $obm_u->f("userobm_timelastaccess");
    $username = $obm_u->f("userobm_username");
    $nb_con = $obm_u->f("activeuserobm_nb_connexions");
    $lastpage = $obm_u->f("activeuserobm_lastpage");
    $ip = $obm_u->f("activeuserobm_ip");
    $lastaccess = substr($a,0,4)."/".substr($a,4,2)."/".substr($a,6,2)." ".substr($a,8,2).":".substr($a,10,2).":".substr($a,12,2);
    $list_user .= "<TR ALIGN=center>
      <TD bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$username</font></TD>
      <TD bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$lastaccess</font></TD>
      <TD bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$nb_con</font></TD>
      <TD bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$lastpage</font></TD>
      <TD bgcolor=\"#$col_a_table\">
        <font color=\"#$col_a_text\">$ip</font></TD>
   </TR>";
}

  echo "
   <CENTER>

    <a href=\"" . $sess->url("admin_index.php?action=help&amp;mode=html"). "\">$l_help</a>
    <p>
        <TABLE BORDER=1 WIDTH=50%>
    <TR ALIGN=center> 
      <TD COLSPAN=2 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">&nbsp;$l_check&nbsp;</font></TD>
    </TR><TR ALIGN=center> 
      <TD WIDTH=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_version</font></TD>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$obm_version</font></TD>
    </TR><TR ALIGN=center> 
      <TD WIDTH=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_database_server</font></TD>
      <td bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">". strtoupper($obm_u->type). "</font></TD>
    </TR><TR ALIGN=center>
      <TD WIDTH=50% bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_database</font></TD>
      <TD bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">" . $obm_u->Database . "</font></TD>
    </TR></TABLE>
    </CENTER>

  <!--  Mini stats section -->

    <P><CENTER>
    <TABLE BORDER=1 >
    <TR ALIGN=center> 
      <TD COLSPAN=5 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">&nbsp;$l_session_stats&nbsp;</font></TD>
    </TR><TR ALIGN=center> 
      <TD COLSPAN=3 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$l_user_session</font></TD>
      <TD COLSPAN=2 bgcolor=\"#$col_a_table\">
      <font color=\"#$col_a_text\">$nb_user_ses</font></TD>
    </TR><TR ALIGN=center> 
      <TD bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_active_user</font></TD>
      <td bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_last_acces</font></TD>
      <TD bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_nb_connec</font></TD>
      <TD bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_last_page</font></TD>
      <TD bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_user_ip</font></TD>

    </TR>
    $list_user
    </TABLE>
    </CENTER>
    <BR>
    <FORM method=get action=\"" . $sess->url("admin_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : clear_sess<br>
        <font color=\"#$col_textem\">Clear all passive users sessions</font></td>
      <td>Passive Sessions :<br><center>$nb_sess_to_clear</center></td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=clear_sess>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>";
}


///////////////////////////////////////////////////////////////////////////////
// Clear the Sessions and file it       				     //
// Parameters:								     //
//   - $mode : "txt" or "html" 						     //
///////////////////////////////////////////////////////////////////////////////
function dis_clear_sess($mode) {
  global $cdg_sql;
  global $time_active_session;

  $date_limit = date("YmdHis",time()-$time_active_session);

  $obm_q = run_query_passive_user($time_active_session);

  while($obm_q->next_record()) {
    run_query_store($obm_q);
    run_query_activeuserobm_delete($obm_q->f('activeuserobm_sid'));
  }

  if($mode == "html")
    dis_index($mode);

}

