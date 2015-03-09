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
$fieldnames['host_imap'] = $l_imap;
$fieldnames['host_imap_archive'] = $l_imap_archive;
$fieldnames['host_smtp_in'] = $l_smtp_in;
$fieldnames['host_smtp_out'] = $l_smtp_out;
$fieldnames['host_obm_sync'] = $l_obm_sync;
$fieldnames['host_description'] = $l_desc;
$fieldnames['host_provisioning'] = $l_provisioning;
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

  else if ($fieldname == 'host_smtp_in') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'host_smtp_out') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'host_obm_sync') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'host_imap') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'host_imap_archive') {
  	$res['align'] = 'center';
  	if ($OD->data_set->f($fieldname)) {
  		$res['name'] = 'X';
  	} else {
  		$res['name'] = ' ';
  	}
  }
  
  else if ($fieldname == 'host_samba') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'host_provisioning') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
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
  global $cgp_use, $obm;

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

  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($host['name']);
  $delegation = stripslashes($host['delegation']);
  $ip = stripslashes($host['ip']);
  $smb = ($host['samba'] == '1' ? "checked = \"checked\"" : '');
  $smtp_in = ($host['smtp_in'] == '1' ? "checked = \"checked\"" : '');
  $smtp_out = ($host['smtp_out'] == '1' ? "checked = \"checked\"" : '');
  $obm_sync = ($host['obm_sync'] == '1' ? "checked = \"checked\"" : '');
  $solr = ($host['solr'] == '1' ? "checked = \"checked\"" : '');
  $opush = ($host['opush'] == '1' ? "checked = \"checked\"" : '');
  $opush_frontend = ($host['opush_frontend'] == '1' ? "checked = \"checked\"" : '');
  $ldap = ($host['ldap'] == '1' ? "checked = \"checked\"" : '');
  $imap = ($host['imap'] == '1' ? "checked = \"checked\"" : '');
  $imap_archive = ($host['imap_archive'] == '1' ? "checked = \"checked\"" : '');
  $imap_frontend = ($host['imap_frontend'] == '1' ? "checked = \"checked\"" : '');
  $backup_ftp = ($host['backup_ftp'] == '1' ? "checked = \"checked\"" : '');
  $provisioning = ($host['provisioning'] == '1' ? "checked = \"checked\"" : '');


  $dis_delegation = of_delegation_dis_search_form($delegation);

  // --- HTML Page display ----------------------------------------------------

  if (($cgp_use['service']['samba'])  && ($samba_domain)) {
    $block_samba = "
    <label>$l_smb_only<br />
      <input type=\"checkbox\" name=\"cb_samba\" value=\"1\" $smb />
    </label>";
  }
  if ($cgp_use['service']['mail']) {
    $block_mail = "
    <label>$GLOBALS[l_imap]<br />
      <input type=\"checkbox\" name=\"cb_imap\" value=\"1\" $imap />
    </label>
    <label>$GLOBALS[l_imap_archive]<br />
      <input type=\"checkbox\" name=\"cb_imap_archive\" value=\"1\" $imap_archive />
    </label>
    <label>$GLOBALS[l_smtp_in]<br />
      <input type=\"checkbox\" name=\"cb_smtp_in\" value=\"1\" $smtp_in />
    </label>
    <label>$GLOBALS[l_smtp_out]<br />
      <input type=\"checkbox\" name=\"cb_smtp_out\" value=\"1\" $smtp_out />
    </label>
    <label>$GLOBALS[l_imap_frontend]<br />
      <input type=\"checkbox\" name=\"cb_imap_frontend\" value=\"1\" $imap_frontend />
    </label>";
  }
    
  $block_sync = "
  <label>$GLOBALS[l_obm_sync]<br />
    <input type=\"checkbox\" name=\"cb_obm_sync\" value=\"1\" $obm_sync />
  </label>";

  $block_solr = "
  <label>$GLOBALS[l_solr]<br />
    <input type=\"checkbox\" name=\"cb_solr\" value=\"1\" $solr />
  </label>";

  $block_opush = "
  <label>$GLOBALS[l_opush]<br />
  <input type=\"checkbox\" name=\"cb_opush\" value=\"1\" $opush />
  </label>";

  $block_opush_frontend = "
  <label>$GLOBALS[l_opush_frontend]<br />
  <input type=\"checkbox\" name=\"cb_opush_frontend\" value=\"1\" $opush_frontend />
  </label>";

  $block_ldap = "
  <label>$GLOBALS[l_ldap]<br />
  <input type=\"checkbox\" name=\"cb_ldap\" value=\"1\" $ldap />
  </label>";

  $block_backup_ftp = "
  <label>$GLOBALS[l_backup_ftp]<br />
    <input type=\"checkbox\" name=\"cb_backup_ftp\" value=\"1\" $backup_ftp />
  </label>";

  $block_provisioning = "
  <label>$GLOBALS[l_provisioning]<br />
    <input type=\"checkbox\" name=\"cb_provisioning\" value=\"1\" $provisioning />
  </label>";

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
      $block_mail
      $block_sync
      $block_solr
      $block_opush
      $block_opush_frontend
      $block_ldap
      $block_backup_ftp
      $block_provisioning
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
  $imap = $host['imap'];
  $imap_archive = $host['imap_archive'];
  $smtp_in = $host['smtp_in'];
  $smtp_out = $host['smtp_out'];
  $obm_sync = $host['obm_sync'];
  $solr = $host['solr'];
  $provisioning = $host['provisioning'];


  $url = url_prepare("host_index.php?action=search&amp;tf_name=$name&amp;tf_delegation=$delegation&amp;tf_ip=$ip&amp;cb_samba=$smb&amp;cb_imap=$imap&amp;cb_imap_archive=$imap_archive&amp;cb_smtp_in=$smtp_in&amp;cb_smtp_out=$smtp_out&amp;cb_obm_sync=$obm_sync&amp;cb_solr=$solr&amp;cb_provisioning=$provisioning$url_ext");

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
  global $cgp_use, $obm, $display, $perm, $menu, $action, $cright_write;
  global $l_yes, $l_no, $l_module_host, $l_name, $l_ip, $l_desc, $l_smb;
  global $l_perms, $l_enable, $l_disable, $l_empty, $l_not_assigned;
  global $l_user, $l_fqdn, $l_system, $l_help_system,$l_domain;
  global $l_day, $l_week, $l_month, $l_year;

  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  $id = $host_q->f('host_id');
  $entity_id = $host_q->f('host_entity_id');
  $delegation = $host_q->f('host_delegation');
  $opush = $host_q->f('host_opush');
  $opush_frontend = $host_q->f('host_opush_frontend');
  $ldap = $host_q->f('host_ldap');
  $samba = $host_q->f('host_samba');
  $imap = $host_q->f('host_imap');
  $imap_archive = $host_q->f('host_imap_archive');
  $smtp_out = $host_q->f('host_smtp_out');
  $obm_sync = $host_q->f('host_obm_sync');
  $solr = $host_q->f('host_solr');
  $imap_frontend = $host_q->f('host_imap_frontend');
  $smtp_in = $host_q->f('host_smtp_in');
  $backup_ftp = $host_q->f('host_backup_ftp');
  $provisioning = $host_q->f('host_provisioning');
  $name = $host_q->f('host_name');
  $desc = $host_q->f('host_description');
  $ip = $host_q->f('host_ip');
  $fqdn = $host_q->f('host_fqdn');
  $user_id = $host_q->f('userobm_id');
  $user_login = $host_q->f('userobm_login');
  $domain_name = ($host_q->f('domain_name') != '' ? $host_q->f('domain_name') : 'GLOBAL');

  $backup_ftp_properties = of_service_properties('backup_ftp',$entity_id);
  $backup_ftp_login = $backup_ftp_properties['login'];
  $backup_ftp_password = $backup_ftp_properties['password'];
  $backup_ftp_root = $backup_ftp_properties['root'];

  $dis_user =   
    "<tr>
      <th>$l_user</th>
      <td>$dis_user</td>
    </tr>";

  // Formatage des informations de la BD
  if ($opush) $opush = $l_yes; else $opush = $l_no;
  if ($opush_frontend) $opush_frontend = $l_yes; else $opush_frontend = $l_no;
  if ($ldap) $ldap = $l_yes; else $ldap = $l_no;
  if ($samba) $samba = $l_yes; else $samba = $l_no;
  if ($imap) $imap = $l_yes; else $imap = $l_no;
  if ($imap_archive) $imap_archive = $l_yes; else $imap_archive = $l_no;
  if ($smtp_in) $smtp_in = $l_yes; else $smtp_in = $l_no;
  if ($smtp_out) $smtp_out = $l_yes; else $smtp_out = $l_no;
  if ($obm_sync) $obm_sync = $l_yes; else $obm_sync = $l_no;
  if ($solr) $solr = $l_yes; else $solr = $l_no;
  if ($imap_frontend) $imap_frontend = $l_yes; else $imap_frontend = $l_no;
  if ($provisioning) $provisioning = $l_yes; else $provisioning = $l_no;
  if ($backup_ftp) {
    $backup_ftp = $l_yes;
    $block_backup_ftp = "
    <tr>
      <th>$GLOBALS[l_backup_ftp]</th>
      <td>$backup_ftp</td>
    </tr>";
    $block_backup_ftp .= "
    <tr>
      <th>$GLOBALS[l_backup_ftp_login]</th>
      <td>$backup_ftp_login</td>
    </tr>";
    //Do not display the password on consult form
    //$block_backup_ftp .= "
    //<tr>
    //  <th>$GLOBALS[l_backup_ftp_password]</th>
    //  <td>$backup_ftp_password</td>
    //</tr>";
    $block_backup_ftp .= "
    <tr>
      <th>$GLOBALS[l_backup_ftp_root]</th>
      <td>$backup_ftp_root</td>
    </tr>";

  } else {
    $backup_ftp = $l_no;
    $block_backup_ftp = "
    <tr>
      <th>$GLOBALS[l_backup_ftp]</th>
      <td>$backup_ftp</td>
    </tr>";
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

  if (($cgp_use['service']['samba']) && ($samba_domain)) {
    $block_samba = "
    <tr>
      <th>$l_smb</th>
      <td>$samba</td>
    </tr>";
  }

  if ($cgp_use['service']['mail']) {
    $block_mail = "
    <tr>
      <th>$GLOBALS[l_imap]</th>
      <td>$imap</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_imap_archive]</th>
      <td>$imap_archive</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_smtp_in]</th>
      <td>$smtp_in</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_smtp_out]</th>
      <td>$smtp_out</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_imap_frontend]</th>
      <td>$imap_frontend</td>
    </tr>
      ";    
  }

  $block_sync = "
  <tr>
    <th>$GLOBALS[l_obm_sync]</th>
    <td>$obm_sync</td>
  </tr>";    

  $block_solr = "
  <tr>
    <th>$GLOBALS[l_solr]</th>
    <td>$solr</td>
  </tr>";    

  $block_opush = "
  <tr>
  <th>$GLOBALS[l_opush]</th>
  <td>$opush</td>
  </tr>";

  $block_opush_frontend = "
  <tr>
  <th>$GLOBALS[l_opush_frontend]</th>
  <td>$opush_frontend</td>
  </tr>";

  $block_ldap = "
  <tr>
  <th>$GLOBALS[l_ldap]</th>
  <td>$ldap</td>
  </tr>";

  $block_provisioning = "
  <tr>
  <th>$GLOBALS[l_provisioning]</th>
  <td>$provisioning</td>
  </tr>";

  $host_domain_explode = explode('.', $fqdn, 2 );
  $host_domain = $host_domain_explode[1];

  $block = "
    <div class=\"detail extras\" style=\"overflow:hidden\">
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
      <th>$l_fqdn</th>
      <td>$fqdn</td>
    </tr>    
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
    $block_sync
    $block_solr
    $block_opush
    $block_opush_frontend
    $block_ldap
    $block_backup_ftp
    $block_provisioning
    </table>
    $block_stat
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
  global $cgp_use, $l_domain, $l_insert, $l_update, $l_back;
  global $l_module_host, $l_name, $l_desc, $l_ip, $l_smb;
  global $l_perms, $l_header_new, $l_fqdn;
  global $l_host_select_user, $l_user, $l_clear;
  global $ico_user, $display, $obm, $c_services;
  
  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $host_q->f('host_id');
    $entity_id = $host_q->f('host_entity_id');
    $name = $host_q->f('host_name');
    $delegation = $host_q->f('host_delegation');
    $ip = $host_q->f('host_ip');
    $fqdn = $host_q->f('host_fqdn');
    $samba = $host_q->f('host_samba');
    $imap = $host_q->f('host_imap');
    $imap_archive = $host_q->f('host_imap_archive');
    $smtp_in = $host_q->f('host_smtp_in');
    $smtp_out = $host_q->f('host_smtp_out');
    $obm_sync = $host_q->f('host_obm_sync');
    $solr = $host_q->f('host_solr');
    $opush = $host_q->f('host_opush');
    $opush_frontend = $host_q->f('host_opush_frontend');
    $ldap = $host_q->f('host_ldap');
    $imap_frontend = $host_q->f('host_imap_frontend');
    $backup_ftp = $host_q->f('host_backup_ftp');
    $provisioning = $host_q->f('host_provisioning');
    $desc = $host_q->f('host_description');
    $u_login = $host_q->f('userobm_login');
    $u_id = $host_q->f('userobm_id');
    $dis_title = $name;

    $backup_ftp_properties = of_service_properties('backup_ftp',$entity_id);
    $backup_ftp_login = $backup_ftp_properties['login'];
    $backup_ftp_password = $backup_ftp_properties['password'];
    $backup_ftp_root = $backup_ftp_properties['root'];
  } else if ($action == 'new') {
    // If windows domain, default check the windows checkbox
    if ($samba_domain) {
      $samba = 1;
    }
  }
  $domain_id = $obm['domain_id'];
  // If parameters have been given, they supercede the default action value
  if (isset($host['host_id'])) { $id = $host['host_id']; }
  if (isset($host['name'])) { $name = stripslashes($host['name']); }
  if (isset($host['delegation'])) { $delegation = stripslashes($host['delegation']); }
  if (isset($host['ip'])) { $ip = $host['ip']; }
  if (isset($host['fqdn'])) { $fqdn = $host['fqdn']; }
  if (isset($host['samba'])) { $samba = $host['samba']; }
  if (isset($host['imap'])) { $imap = $host['imap']; }
  if (isset($host['imap_archive'])) { $imap = $host['imap_archive']; }
  if (isset($host['smtp_in'])) { $smtp_in = $host['smtp_in']; }
  if (isset($host['smtp_out'])) { $smtp_out = $host['smtp_out']; }
  if (isset($host['obm_sync'])) { $obm_sync = $host['obm_sync']; }
  if (isset($host['solr'])) { $solr = $host['solr']; }
  if (isset($host['opush'])) { $opush = $host['opush']; }
  if (isset($host['opush_frontend'])) { $opush_frontend = $host['opush_frontend']; }
  if (isset($host['ldap'])) { $ldap = $host['ldap']; }
  if (isset($host['imap_frontend'])) { $imap_frontend = $host['imap_frontend']; }
  if (isset($host['provisioning'])) { $provisioning = $host['provisioning']; }
  if (isset($host['backup_ftp'])) { $backup_ftp = $host['backup_ftp']; }
  if (isset($host['backup_ftp_login'])) { $backup_ftp_login = $host['backup_ftp_login']; }
  if (isset($host['backup_ftp_password'])) { $backup_ftp_password = $host['backup_ftp_password']; }
  if (isset($host['backup_ftp_root'])) { $backup_ftp_root = $host['backup_ftp_root']; }
  if (isset($host['desc'])) { $desc = stripslashes($host['desc']); }
  if (isset($host['domain_id'])) { $domain_id = $host['domain_id']; }

  if (empty($backup_ftp_root)) $backup_ftp_root = '/';

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
  $imap_state  = ($imap ? 'checked' : '');
  $imap_archive_state  = ($imap_archive ? 'checked' : '');
  $smtp_in_state  = ($smtp_in ? 'checked' : '');
  $smtp_out_state  = ($smtp_out ? 'checked' : '');
  $obm_sync_state  = ($obm_sync ? 'checked' : '');
  $solr_state  = ($solr ? 'checked' : '');
  $opush_state  = ($opush ? 'checked' : '');
  $opush_frontend_state  = ($opush_frontend ? 'checked' : '');
  $ldap_state  = ($ldap ? 'checked' : '');
  $imap_frontend_state  = ($imap_frontend ? 'checked' : '');
  $backup_ftp_state  = ($backup_ftp ? 'checked' : '');
  $provisioning_state  = ($provisioning ? 'checked' : '');

  $display_backup_ftp_data = '';
  if ($backup_ftp_state=='') {
    $display_backup_ftp_data = 'style="display: none;"';
  }

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  $display['detailInfo'] = display_record_info($host_q);
  $display['title'] = $dis_title;
  if ($action != 'insert' && $action !='new') {
    $dis_name = "$name <input type=\"hidden\" name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";
  } else {
    $dis_name = "<input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";
  }

  // Display windows info only if samba is active and domainis samba
  if (($cgp_use['service']['samba']) && ($samba_domain)) {
    $block_samba = "
    <tr>
      <th class=\"$class[samba]\"><label for=\"cb_samba\">$l_smb</label></th>
      <td>
        <input id=\"cb_samba\" name=\"cb_samba\" type=\"checkbox\" value=\"1\" $samba_state>
      </td>
    </tr>";
  }
  if ($cgp_use['service']['mail']) {
    $block_mail = "
    <tr>
      <th class=\"$class[imap]\"><label for=\"cb_imap\">$GLOBALS[l_imap]</label></th>
      <td>
        <input id=\"cb_imap\" name=\"cb_imap\" type=\"checkbox\" value=\"1\" $imap_state>
      </td>
    </tr><tr>
      <th class=\"$class[imap_archive]\"><label for=\"cb_imap_archive\">$GLOBALS[l_imap_archive]</label></th>
      <td>
        <input id=\"cb_imap_archive\" name=\"cb_imap_archive\" type=\"checkbox\" value=\"1\" $imap_archive_state>
      </td>
    </tr><tr>
      <th class=\"$class[smtp_in]\"><label for=\"cb_smtp_in\">$GLOBALS[l_smtp_in]</label></th>
      <td>
        <input id=\"cb_smtp_in\" name=\"cb_smtp_in\" type=\"checkbox\" value=\"1\" $smtp_in_state>
      </td>
    </tr><tr>
      <th class=\"$class[smtp_out]\"><label for=\"cb_smtp_out\">$GLOBALS[l_smtp_out]</label></th>
      <td>
        <input id=\"cb_smtp_out\" name=\"cb_smtp_out\" type=\"checkbox\" value=\"1\" $smtp_out_state>
      </td>
    </tr><tr>
      <th class=\"$class[imap_frontend]\"><label for=\"cb_imap_frontend\">$GLOBALS[l_imap_frontend]</label></th>
      <td>
        <input id=\"cb_imap_frontend\" name=\"cb_imap_frontend\" type=\"checkbox\" value=\"1\" $imap_frontend_state>
      </td>
    </tr>";
  }
    
  $block_sync = "
  <tr>
    <th class=\"$class[obm_sync]\"><label for=\"cb_obm_sync\">$GLOBALS[l_obm_sync]</label></th>
    <td>
      <input id=\"cb_obm_sync\" name=\"cb_obm_sync\" type=\"checkbox\" value=\"1\" $obm_sync_state>
    </td>
  </tr>";

  $block_solr = "
  <tr>
    <th class=\"$class[solr]\"><label for=\"cb_solr\">$GLOBALS[l_solr]</label></th>
    <td>
      <input id=\"cb_solr\" name=\"cb_solr\" type=\"checkbox\" value=\"1\" $solr_state>
    </td>
  </tr>";

  $block_opush = "
  <tr>
  <th class=\"$class[opush]\"><label for=\"cb_opush\">$GLOBALS[l_opush]</label></th>
  <td>
  <input id=\"cb_opush\" name=\"cb_opush\" type=\"checkbox\" value=\"1\" $opush_state>
  </td>
  </tr>";

  $block_opush_frontend = "
  <tr>
  <th class=\"$class[opush_frontend]\"><label for=\"cb_opush_frontend\">$GLOBALS[l_opush_frontend]</label></th>
  <td>
  <input id=\"cb_opush_frontend\" name=\"cb_opush_frontend\" type=\"checkbox\" value=\"1\" $opush_frontend_state>
  </td>
  </tr>";

  $block_ldap= "
  <tr>
  <th class=\"$class[ldap]\"><label for=\"cb_ldap\">$GLOBALS[l_ldap]</label></th>
  <td>
  <input id=\"cb_ldap\" name=\"cb_ldap\" type=\"checkbox\" value=\"1\" $ldap_state>
  </td>
  </tr>";

  $block_provisioning= "
  <tr>
  <th class=\"$class[provisioning]\"><label for=\"cb_provisioning\">$GLOBALS[l_provisioning]</label></th>
  <td>
  <input id=\"cb_provisioning\" name=\"cb_provisioning\" type=\"checkbox\" value=\"1\" $provisioning_state>
  </td>
  </tr>";

  $block_backup_ftp = "
  <tr>
    <th class=\"$class[backup_ftp]\"><label for=\"cb_backup_ftp\">$GLOBALS[l_backup_ftp]</label></th>
    <td>
      <input id=\"cb_backup_ftp\" name=\"cb_backup_ftp\" type=\"checkbox\" value=\"1\" $backup_ftp_state onClick=\"on_backup_ftp_click();\" />
    </td>
  </tr>
  <tr id=\"row_backup_ftp_login\" $display_backup_ftp_data>
    <th class=\"$class[backup_ftp_login]\"><label for=\"tf_backup_ftp_login\">$GLOBALS[l_backup_ftp_login]</label></th>
    <td>
      <input id=\"tf_backup_ftp_login\" name=\"tf_backup_ftp_login\" value=\"$backup_ftp_login\" />
    </td>
  </tr>
  <tr id=\"row_backup_ftp_password\" $display_backup_ftp_data>
    <th class=\"$class[backup_ftp_password]\"><label for=\"tf_backup_ftp_password\">$GLOBALS[l_backup_ftp_password]</label></th>
    <td>
      <input id=\"tf_backup_ftp_password\" name=\"tf_backup_ftp_password\" type=\"password\" value=\"$backup_ftp_password\" />
    </td>
  </tr>
  <tr id=\"row_backup_ftp_root\" $display_backup_ftp_data>
    <th class=\"$class[backup_ftp_root]\"><label for=\"tf_backup_ftp_root\">$GLOBALS[l_backup_ftp_root]</label></th>
    <td>
      <input id=\"tf_backup_ftp_root\" name=\"tf_backup_ftp_root\" value=\"$backup_ftp_root\" />
    </td>
  </tr>";

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
  <legend>$l_module_host</legend>

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
  <tr>
    <th class=\"$class[fqdn]\">$l_fqdn</th>
    <td>
      <input name=\"tf_fqdn\" value=\"$fqdn\" /></td>
  </tr>  
  $block_samba
  $block_mail
  $block_sync
  $block_solr
  $block_opush
  $block_opush_frontend
  $block_ldap
  $block_backup_ftp
  $block_provisioning
  <tr>
    <th class=\"$class[desc]\">$l_desc</th>
    <td>
      <input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"128\" />
    </td>
  </tr>

  </table>
  </fieldset>";

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

  $lhost = get_host_info($id);
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
  $fqdn = $host['fqdn'];
  $imap = $host['imap'];
  $imap_archive = $host['imap_archive'];
  $smtp_in = $host['smtp_in'];
  $smtp_out = $host['smtp_out'];
  $obm_sync = $host['obm_sync'];
  $solr = $host['solr'];
  $imap_frontend = $host['imap_frontend'];
  $samba = $host['samba'];
  $provisioning = $host['provisioning'];

  $display['msg'] .= display_warn_msg($l_check_samehost);
  while ($h_q->next_record()) {
    $id = $h_q->f('host_id');
    $samename = $h_q->f('host_name');
    $samedesc = $h_q->f('host_description');
    $dis_same_host .= "
      <li>
        <a href=\"" .url_prepare("host_index.php?action=detailconsult&amp;host_id=$id") . "\">$samename ($samedesc)</a>
      </li>";
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"tf_delegation\" value=\"$delegation\" />
    <input type=\"hidden\" name=\"tf_ip\" value=\"$ip\" />
    <input type=\"hidden\" name=\"cb_imap\" value=\"$imap\" />
    <input type=\"hidden\" name=\"cb_imap_archive\" value=\"$imap_archive\" />
    <input type=\"hidden\" name=\"cb_smtp_in\" value=\"$smtp_in\" />
    <input type=\"hidden\" name=\"cb_smtp_out\" value=\"$smtp_out\" />
    <input type=\"hidden\" name=\"cb_obm_sync\" value=\"$obm_sync\" />
    <input type=\"hidden\" name=\"cb_solr\" value=\"$solr\" />
    <input type=\"hidden\" name=\"cb_imap_frontend\" value=\"$imap_frontend\" />
    <input type=\"hidden\" name=\"cb_provisioning\" value=\"$provisioning\" />
    <input type=\"hidden\" name=\"cb_samba\" value=\"$samba\" />
    <input type=\"hidden\" name=\"tf_fqdn\" value=\"$fqdn\" />
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

