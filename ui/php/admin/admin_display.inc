<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
//   - $mode   : 'txt' ou 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_admin_help($mode) {

  if ($mode == 'html')
    echo "<table class=\"details\">
      <tr>
        <td><pre>";

  include ('admin_help.inc');

  if ($mode == 'html')
    echo "</pre></td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode     : 'txt' ou 'html'
//   - $lifetime : session lifetime
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index($mode, $lifetime) {

  switch ($mode) {
  case 'txt':
    echo "try -h to help\n";
    break;
  case 'html':
    $nb_user_ses = get_admin_usersession_number();
    $obm_u = run_query_admin_active_user($lifetime);
    $block = html_admin_status($lifetime, $nb_user_ses, $obm_u);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

  return $block;
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
  global $l_session_stats, $l_session, $l_user_session, $l_lifetime, $l_active_user, $l_login_date;
  global $l_last_access,$l_nb_connec, $l_clear_passive_session, $l_action, $l_clear_sess, $l_passive_session, $l_activeUser_toDisplay;
  global $obm_version, $l_version, $l_date, $l_host, $l_domain, $c_singleNameSpace;

  $date = of_datetime_format();
  if ($lifetime > 0) {
    $nb_sess_to_clear = run_query_admin_count_passive_sess($lifetime);
    $d_lifetime = $lifetime;
  } else {
    $nb_sess_to_clear = 0;
    $d_lifetime = 'unlimited';
  }

  $nbua = $obm_u->nf();
  $list_user = '';

  while ($obm_u->next_record()) {
    $last_access = of_datetime_format($obm_u->f('timeupdate'));
    $login = $obm_u->f('userobm_login');
    $login_date = of_datetime_format($obm_u->f('timecreate'));
    $nb_con = $obm_u->f('activeuserobm_nb_connexions');
    $lastpage = $obm_u->f('activeuserobm_lastpage');
    $ip = $obm_u->f('activeuserobm_ip');
    $list_user .= "
      <tr>
      <td>$login</td>
      <td>$login_date</td>
      <td>$last_access</td>
      <td>$nb_con</td>
      <td>$lastpage</td>
      <td>$ip</td>
      </tr>";
  }
  
  $multidomain = $c_singleNameSpace == false ? 'Multi' : 'Single';
	 
  $block = "
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

    <form method=\"get\" action=\"" . url_prepare('admin_index.php'). "\">
    <div class=\"detail infos\">
    <h1>Sessions</h1>
    <table>
    <tr>
      <th>$l_action : $l_clear_sess</th>
      <td>$l_passive_session (delay > $d_lifetime)</td>
    </tr><tr>
      <th>$l_clear_passive_session</th>
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
    <td>".$obm_u->nf()." ".$l_activeUser_toDisplay." ".$nb_user_ses."</td>
    </tr>
    </tbody>
    <thead>
    <tr> 
      <th>$l_active_user</th>
      <th>$l_login_date</th>
      <th>$l_last_access</th>
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

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Clear the Sessions and file it
// Parameters:
//   - $mode     : 'txt' or 'html'
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

  run_query_admin_activeuserobm_purge($ltime);

  $block = "";
  if ($mode == 'html') {
    $block .= dis_admin_index($mode, $ltime);
  }
  
  return $block;
}
?>
