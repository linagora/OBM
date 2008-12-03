<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : host_display.php                                             //
//     - Desc : Host Display File                                            //
// 2004-09-09 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames['host_name'] = $l_name;
$fieldnames['host_delegation'] = $l_delegation;
$fieldnames['host_ip'] = $l_ip;
$fieldnames['host_samba'] = $l_smb;
$fieldnames['host_description'] = $l_desc;
$fieldnames['usercreate'] = $l_creator;
$fieldnames['userupdate'] = $l_updater;
$fieldnames['timecreate'] = $l_date_creation;
$fieldnames['timeupdate'] = $l_date_last_update;


///////////////////////////////////////////////////////////////////////////////
// Display Host specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_host(&$OD, $fieldname, $link_ok) {
  global $path ;

  if (($fieldname == 'host_name') && $link_ok) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_get_id(".$OD->data_set->f('host_id').",'".addslashes($OD->data_set->f('host_name'))."');";
    } else {
     $res['url'] = "$path/host/host_index.php?action=detailconsult&amp;host_id=".$OD->data_set->f('host_id');
    }
  }

  else if ($fieldname == 'host_samba') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname) == '1') {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Host search Form
// Parameters : 
//   - $host[]   : default form values
//     keys used : name, user
///////////////////////////////////////////////////////////////////////////////
function html_host_search_form($host) {
  global $display, $l_name, $l_ip, $l_smb_only, $l_find;
  global $cgp_use;

  $popup = $host['popup'];

  if ($popup) {
    $ext_action = $host['ext_action'];
    $ext_target = $host['ext_target'];
    $ext_widget = $host['ext_widget'];
    $ext_widget_text = $host['ext_widget_text'];
    $ext_url = $host['ext_url'];
    $ext_id = $host['ext_id'];
    $ext_title = $host['ext_title'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == '') {
      $ext_title = $l_add_hosts;
    }
    $display['title'] = $ext_title;
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($host['name']);
  $delegation = stripslashes($host['delegation']);
  $ip = stripslashes($host['ip']);
  $smb = ($host['samba'] == '1' ? "checked = \"checked\"" : '');

  $dis_delegation = of_delegation_dis_search_form($delegation);

  // --- HTML Page display ----------------------------------------------------

  if ($cgp_use['service']['samba']) {
    $block_samba = "
    <label>$l_smb_only<br />
      <input type=\"checkbox\" name=\"cb_samba\" value=\"1\" $smb />
    </label>";
  }

  $block = "
    <form method=\"get\" name=\"f_search\" class=\"search\" action=\"".url_prepare("host_index.php")."\">
    <div class=\"search\">
      <label>$l_name<br />
        <input name=\"tf_name\" size=\"24\" maxlength=\"32\" value=\"$name\" />
      </label>
      $dis_delegation
      <label>$l_ip<br />
        <input name=\"tf_ip\" size=\"16\" maxlength=\"16\" value=\"$ip\" />
      </label>
      $block_samba
      <label>&nbsp;<br />
        <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
        $ext
      </label>
      <p class=\"CL\" />
    </div>
  </form>";

  return $block;
}


/**
 * Display the Host search result
 *
 * @param mixed $host[] : host search criteria
 * @return Search result display
 */
function dis_host_search_list($host) {
  global $l_found, $l_no_found, $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'host');
  $obm_q = run_query_host_search($host);

  $nb_host = $obm_q->num_rows_total();
  if ($nb_host == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_ok_msg("$nb_host $l_found");
    $block = html_host_search_list($obm_q, $prefs, $host);
  }

  return $block;
}


/**
 * XHTML Display the Host search result
 *
 * @param mixed $obm_q  : database object with host list
 * @param mixed $pref_q : database object with user display preferences
 * @param mixed $host[] : host parameters
 * @return XHTML Display Search result
 */
function html_host_search_list($obm_q, $pref_q, $host) {
  global $display, $l_found, $l_close, $module;

  $popup = $host['popup'];

  if ($popup) {
    $ext_action = $host['ext_action'];
    $ext_url = $host['ext_url'];
    $ext_target = $host['ext_target'];
    $ext_widget = $host['ext_widget'];
    $ext_widget_text = $host['ext_widget_text'];
    $ext_id = $host['ext_id'];
    $ext_title = urlencode(stripslashes($host['ext_title']));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_title&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $name = urlencode(stripslashes($host['name']));
  $delegation = urlencode(stripslashes($host['delegation']));
  $ip = urlencode(stripslashes($host['ip']));
  $smb = $host['samba'];

  $url = url_prepare("host_index.php?action=search&amp;tf_name=$namet&amp;tf_delegation=$delegation&amp;tf_ip=$ip&amp;cb_smb=$smb$url_ext");

  $dis_host = new OBM_DISPLAY('DATA', $pref_q, $module);

  if ($popup) {
    if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $dis_host->display_ext = 'get_id';
    }
    $dis_host->data_form_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_host->data_set = $obm_q;
  $dis_host->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_host->display('dis_data_host');

  return $block;
}


/**
 * Display Host Detail
 *
 * @param mixed $host[] : host parameters
 * @return Display block
 */
function dis_host_consult($host) {
  global $display, $l_err_reference;

  $host_q = run_query_host_detail($host['host_id']);
  if ($host_q->num_rows() == 1) {
    $block = html_host_consult($host_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}


/**
 * XHTML Display Host Detail
 *
 * @param mixed $host_q : host database data
 * @return XHTML Display block
 */
function html_host_consult($host_q) {
  global $cgp_use, $l_yes, $l_no;
  global $l_module_host, $l_name, $l_ip, $l_desc, $l_smb;
  global $display, $perm, $menu, $action, $cright_write;
  global $l_perms, $l_web, $l_ftp, $l_firewall,$l_web_mode;
  global $l_enable, $l_disable, $l_empty, $l_web_all, $l_web_list_only, $l_not_assigned;
  global $l_user, $l_consult_web_list, $l_web_access, $l_mailserver;
  global $l_system, $l_help_system,$l_domain;


  $id = $host_q->f('host_id');
  $delegation = $host_q->f('host_delegation');
  $samba = $host_q->f('host_samba');
  $name = $host_q->f('host_name');
  $desc = $host_q->f('host_description');
  $ip = $host_q->f('host_ip');
  $web_perms = $host_q->f('host_web_perms');
  $web_all = $host_q->f('host_web_all');
  $ftp_perms = $host_q->f('host_ftp_perms');
  $firewall_perms = $host_q->f('host_firewall_perms');
  $user_id = $host_q->f('userobm_id');
  $user_login = $host_q->f('userobm_login');
  $mail_server = $host_q->f('mailserver_id');
  $domain_name = ($host_q->f('domain_name') != '' ? $host_q->f('domain_name') : 'GLOBAL');

  $dis_user =   
    "<tr>
      <th>$l_user</th>
      <td>$dis_user</td>
    </tr>";

  // Formatage des informations de la BD
  if ($samba) $samba = $l_yes; else $samba = $l_no;
  $web_state  = ($web_perms ? $l_enable : $l_disable);
  $web_access = ($web_all ? $l_web_all : $l_web_list_only);
  $ftp_state  = ($ftp_perms ? $l_enable : $l_disable);
  if (trim($firewall_perms) != '') {
    $firewall_str = nl2br($firewall_perms);
  } else {
    $firewall_str = $l_empty;
  }
  if ($user_login == '') {
    $dis_user = $l_not_assigned;
  } else {
    $dis_user = "<a href=\"$path/user/user_index.php?action=detailconsult&amp;user_id=$user_id\">$user_login</a>";
  }
  if ($mail_server) $mail_server = $l_yes; else $mail_server = $l_no;

  $block_delegation = of_delegation_dis_block_consult($delegation);
    

  $display['detailInfo'] = display_record_info($host_q);
  $display['title'] = $name;

  // Section Web, seulement si squid en local pour les machines car 
  // acces geres dans les ACL donc dans squid.conf
  if ($cgp_use['service']['web_local']) {
    // Affichage uniquement si acces web ET mode web_local
    // sinon il n'y a pas generation de squid.conf donc pas d'ACL
    if ($web_perms == 1) {
      $dis_access = "
      <tr>
        <th>$l_web_mode</th>
	<td>$web_access (<a href=\"$path/host/host_index.php?action=showlist&amp;host_id=$id\" >$l_consult_web_list</a>)</td>
      </tr>";
    }
    $web_section = "
    <div class=\"detail infos\">
      <h1>$l_web_access</h1>
      <table>
      <tr>
        <th>$l_web</th>
        <td>$web_state</td>
      </tr>
      $dis_access
      </table>
    </div>";
  }
  
  if ($cgp_use['service']['samba']) {
    $block_samba = "
    <tr>
      <th>$l_smb</th>
      <td>$samba</td>
    </tr>";
  }

  if ($cgp_use['service']['mail']) {
    $block_mail = "
    <tr>
      <th>".$l_mailserver."</th>
      <td>".$mail_server."</td>
    </tr>";
  }

  $block = "
    <div class=\"detail extras\">
    <h1>$l_module_host</h1>

    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>
    <tr>
      <th>$l_domain</th>
      <td>$domain_name</td>
    </tr>
    $block_delegation
    <tr>
      <th>$l_ip</th>
      <td>$ip</td>
    </tr>
    $block_samba
    <tr>
      <th>$l_desc</th>
      <td>$desc</td>
    </tr>

    $block_mail
    </table>
    </div>
    $web_section";

  if ($cgp_use['service']['firewall']) {
    $block .= "
    <div class=\"detailHead\">$l_perms</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_ftp</td>
      <td class=\"detailText\">$ftp_state</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_firewall</td>
      <td class=\"detailText\">$firewall_str</td>
    </tr>
    </table>";
  
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Web restriction screen
// Parameters:
//   - $usr_q     : user database result
///////////////////////////////////////////////////////////////////////////////
function html_host_consult_web($host_q) {
  global $l_lastname, $l_firstname, $l_restrict;
  global $l_disable;
  global $action, $c_yes, $c_no;
  global $l_web, $l_web_mode, $l_web_all, $l_web_list_only;

  $height_sep = '10';

  $id = $host_q->f('host_id');
  $name = $host_q->f('host_name');
  $ip = $host_q->f('host_ip');
  $web_all = $host_q->f('host_web_all');
  $list = nl2br($host_q->f('host_web_list'));

  $web_access = ($web_all ? $l_web_all : $l_web_list_only);

  $block = "
  <div>
    <h1>$l_restrict $l_web : $name ($ip)</h1>
  <table class=\"detail\">
    <tr>
      <th>$l_web_mode</th>
      <td>$web_access</td>
    </tr>
    <tr>
      <td colspan=\"2\">$list</td>
    </tr>
  </table>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Host Form
// Parameters :
//   - $action   : action called
//   - $host_q   : DBO : information about the host (null for new host)
//   - $host[]   : default or transmitted values
//     keys used : name, desc, usercreate
//   - $field    : field in error
///////////////////////////////////////////////////////////////////////////////
function html_host_form($action, $host_q, $host, $field='') {
  global $cgp_use, $l_domain, $l_insert, $l_update, $l_back,$l_web_list;
  global $l_module_host, $l_name, $l_desc, $l_ip, $l_smb,$l_web_mode;
  global $l_perms, $l_web, $l_ftp, $l_firewall,$l_web_access, $l_header_new;
  global $l_host_select_user, $l_user, $l_clear,$l_web_all, $l_web_list_only;
  global $ico_user, $display, $obm;
  
  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $host_q->f('host_id');
    $name = $host_q->f('host_name');
    $delegation = $host_q->f('host_delegation');
    $ip = $host_q->f('host_ip');
    $samba = $host_q->f('host_samba');
    $desc = $host_q->f('host_description');
    $web_perms = $host_q->f('host_web_perms');
    $web_list = $host_q->f('host_web_list');
    $web_all = $host_q->f('host_web_all');
    $ftp_perms = $host_q->f('host_ftp_perms');
    $firewall_perms = $host_q->f('host_firewall_perms');
    $u_login = $host_q->f('userobm_login');
    $u_id = $host_q->f('userobm_id');
    $dis_title = $name;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($host['host_id'])) { $id = $host['host_id']; }
  if (isset($host['name'])) { $name = stripslashes($host['name']); }
  if (isset($host['delegation'])) { $delegation = stripslashes($host['delegation']); }
  if (isset($host['ip'])) { $ip = $host['ip']; }
  if (isset($host['samba'])) { $samba = $host['samba']; }
  if (isset($host['desc'])) { $desc = stripslashes($host['desc']); }
  if (isset($host['web_perms'])) { $web_perms = $host['web_perms']; }
  if (isset($host['web_list'])) { $web_list = $host['web_list']; }
  if (isset($host['web_all'])) { $web_all = $host['web_all']; }
  if (isset($host['ftp_perms'])) { $ftp_perms = $host['ftp_perms']; }
  if (isset($host['firewall_perms'])) { $firewall_perms = $host['firewall_perms']; }
  if (isset($host['domain_id'])) { $domain_id = $host['domain_id']; }

  // Domain (can't be updated, so only on insert)
  if (($action == 'new' || $action == 'insert')
      && $obm['domain_global']) {
    $sel_domain_id = of_domain_dis_select($domain_id);
    $dis_domain = "<tr>
      <th>$l_domain</th>
      <td>$sel_domain_id</td>
    </tr>";
  }
  // Samba host checkbox checked value
  $samba_state  = ($samba ? 'checked' : '');

  // Web mode Radio checked value
  if ($web_all == '0') $web_all_c0 = ' checked';
  else $web_all_c1 = ' checked';

  // Web/FTP checkbox checked value
  $web_state  = ($web_perms ? 'checked' : '');
  $ftp_state  = ($ftp_perms ? 'checked' : '');

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  $display['detailInfo'] = display_record_info($host_q);
  $display['title'] = $dis_title;
  if($action != 'insert' && $action !='new') {
    $dis_name = "$name <input type=\"hidden\" name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";
  } else {
    $dis_name = "<input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";
  }
  // Section Web, seulement si squid en local pour les machines car 
  // acces geres dans les ACL donc dans squid.conf
  if ($cgp_use['service']['web_local']) {

    $block_web_local = "
    <tr>
      <th class=\"$class[web_mode]\">$l_web_mode</th>
      <td>
        <input type=\"radio\" name=\"rd_web_all\" value=\"1\" $web_all_c1>
        $l_web_all
        <input type=\"radio\" name=\"rd_web_all\" value=\"0\" $web_all_c0>
        $l_web_list_only
      </td>
    </tr>    
    <tr>
      <th class=\"$class[web_list]\">$l_web_list</th>
      <td>
        <textarea name=\"ta_web_list\" cols=\"40\" rows=\"6\">$web_list</textarea>
      </td>
    </tr>";

    $dis_web = "
    <fieldset class=\"detail info\">
    <legend>$l_web_access</legend>
    <table>
    <tr>
      <th class=\"$class[web_perms]\">$l_web</th>
      <td>
        <input name=\"cb_web_perms\" type=\"checkbox\" value=\"1\" $web_state>
      </td>
      </tr>
    $block_web_local
      </table>
    </fieldset>";
  }

  if ($cgp_use['service']['samba']) {
    $block_samba = "
  <tr>
    <th class=\"$class[samba]\">$l_smb</th>
    <td>
      <input name=\"cb_samba\" type=\"checkbox\" value=\"1\" $samba_state>
    </td>
  </tr>";
  }

  $dis_user = "
  <tr>
  <th class=\"$class[user]\">
  $l_user
  <a href=\"\" onclick=\"popup('$path/user/user_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_host_select_user)."&amp;ext_widget=f_host.user_id&amp;ext_widget_text=f_host.tf_user_login','User'); return false;\">
   <img src=\"$ico_user\" alt=\"\" /></a>
  </th>
   
    <td>
    <input type=\"hidden\" name=\"user_id\" value=\"$u_id\" />
    <input type=\"text\" name=\"tf_user_login\" value=\"$u_login\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    <input type=\"button\" value=\"$l_clear\" onclick=\"this.form.user_id.value='';this.form.tf_user_login.value='';\" />
    </td>
  </tr>";

  $block = "
  <form method=\"get\" name=\"f_host\" action=\"host_index.php\">

  <fieldset class=\"detail extras\">
  <legend>$l_module_host</h1>

  <table>
  <tr>
    <th class=\"$class[name]\">$l_name</th>
    <td>
    $dis_name
    </td>
  </tr>
  $dis_domain
  $block_delegation
  <tr>
    <th class=\"$class[ip]\">$l_ip</th>
    <td>
      <input name=\"tf_ip\" maxlength=\"16\" size=\"16\" value=\"$ip\" /></td>
  </tr>
  $block_samba
  <tr>
    <th class=\"$class[desc]\">$l_desc</th>
    <td>
      <input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"128\" />
    </td>
  </tr>

  </table>
  </fieldset>
  $dis_web";

  if ($cgp_use['service']['firewall']) {
    $block .= "
  <fieldset class=\"detail infos\">
  <h1>$l_perms</h1>

  <table>
  <tr>
    <th class=\"$class[ftp_perms]\">$l_ftp</td>
    <td>
      <input name=\"cb_ftp_perms\" type=\"checkbox\" value=\"1\" $ftp_state>
    </td>
  </tr>
  <tr>
    <th class=\"$class[firewall_perms]\">$l_firewall</td>
    <td>
      <textarea name=\"ta_firewall_perms\" cols=\"32\" rows=\"3\">$firewall_perms</textarea>
    </td>
  </tr>
  </table>
  </fieldset>";
  }


  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_but = "
      <input type=\"hidden\" name=\"host_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<fieldset class=\"buttons\">
    $dis_but
    </fieldset>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Host Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_host_display_pref($prefs) {
  global $l_host_display, $module;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, $module);
  $dis_pref->display_entity = 'host'; 
  $dis_pref->pref_title = $l_host_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a host deletion
// We ask confirmation or cancel
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function dis_host_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_can_delete, $l_back;
  global $l_cant_delete_mailserver;

  $lhost = get_host_info($id);
  if ($lhost['mailserver'] != '') {
    $display['msg'] .= display_warn_msg($l_cant_delete_mailserver);
    $dis_delete = '';

  } else {
    $display['msg'] .= display_info_msg("$l_can_delete");

    $dis_delete = "
    <p class=\"detailButtons\">
      <form method=\"get\" name=\"form_delete\"
      action=\"" .url_prepare("host_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"host_id\" value=\"$id\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
      </form>
    </p>";
  }

  $dis_back = "
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("host_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"host_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>";

  $block = "
  <div class=\"detailButton\">
    $dis_delete
    $dis_back
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a host insertion or update
// When similar hosts exist we show these and ask confirmation
// Parameters:
//   - $id       : host id
//   - $h_q      : host database result (at least 1 row)
//   - $host[]  : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_host_warn_insert($id, $h_q, $host) {
  global $display, $l_check_samehost, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $host['name'];
  $delegation = $host['delegation'];
  $ip = $host['ip'];
  $desc = $host['desc'];

  $display['msg'] .= display_warn_msg($l_check_samehost);
  while ($h_q->next_record()) {
    $id = $h_q->f('host_id');
    $samename = $h_q->f('host_name');
    $samedesc = $h_q->f('host_description');
    $dis_same_host .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("host_index.php?action=detailconsult&amp;host_id=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"tf_delegation\" value=\"$delegation\" />
    <input type=\"hidden\" name=\"tf_ip\" value=\"$ip\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
";

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_host
    </ul>
  </div>

  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" .url_prepare("host_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden_fields
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>

    <form name=\"form_back\" method=\"get\"
    action=\"" .url_prepare("host_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden_fields
    <input type=\"submit\" value=\"$l_back\" />
    </form>
  </div>";

  return $block;
}

</script>
