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
// OBM - File : domain_display.inc                                           //
//     - Desc : domain Display functions File                                //
// 2000-05-17 Phan David                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['domain_label'] = $l_label;
$fieldnames['domain_description'] = $l_desc;
$fieldnames['domain_name'] = $l_domain_name;
$fieldnames['domain_alias'] = $l_alias;
$fieldnames['domain_timecreate'] = $l_timecreate;


///////////////////////////////////////////////////////////////////////////////
// Display domain specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_domain_data(&$OD, $fieldname, $link_ok) {
  global $path, $ico_mail;

  if (($fieldname == 'domain_label') && $link_ok) {
    $res['url'] = "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=".$OD->data_set->f('domain_id');
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain search form
// Parameters:
//   - $domain[]   : default form values
//     keys used : login, lastname, perms, archive, popup
///////////////////////////////////////////////////////////////////////////////
function html_domain_search_form($domain) {
  global $l_text, $l_find;

  $text = $domain['text'];
  
  $block = "
  <form method=\"get\" name=\"f_search\" class=\"search\" action=\"". url_prepare('domain_index.php')."\">

    <label>$l_text<br />
      <input type=\"text\" name=\"tf_text\" size=\"16\" maxlength=\"32\"
      value=\"$text\" />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the domain search result
// Parameters:
//   - $domain[]   : domain search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_domain_search_list($domain) {
  global $l_found, $l_no_found;
  global $display, $obm;

  $obm_q = run_query_domain_search($domain);

  $prefs = get_display_pref($obm['uid'], 'domain');
  $nb_domain = $obm_q->num_rows_total();
  if ($nb_domain == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_domain $l_found");
    $block = html_domain_search_list($obm_q, $prefs, $domain, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the domain Search result
// Parameters:
//   - $obm_q    : database result (domain list)
//   - $prefs    : the fields which have to be displayed
//   - $domain[]   : domain search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_domain_search_list($obm_q, $prefs, $domain, $popup) {
  global $l_close, $l_add;

  $text = urlencode($domain['text']);

  $url = url_prepare("domain_index.php?action=search&amp;tf_text=$text");

  $domain_d = new OBM_DISPLAY('DATA', $prefs, 'domain');
  $domain_d->data_set = $obm_q;
  $domain_d->data_header = 'both';
  $domain_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $domain_d->display('dis_domain_data');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain Consultation
// Parameters:
//   - $domain : domain database result
///////////////////////////////////////////////////////////////////////////////
function dis_domain_consult($domain) {
  global $display, $l_query_error;

  $id = $domain['domain_id'];

  $obm_q = run_query_domain_detail($id);
  $properties_q = run_query_domain_properties($id);
  $mailservers = of_domain_get_domain_mailserver('', $id);
  $syncservers = of_domain_get_domain_syncserver($id);
  $solrservers = of_domain_get_domain_solrserver($id);
  $opushservers = of_domain_get_domain_opushserver($id);
  $opushfrontendservers = of_domain_get_domain_opushfrontendserver($id);
  $ldapservers = of_domain_get_domain_ldapserver($id);
  $imapfrontendservers = of_domain_get_domain_imapfrontendserver($id);
  $backupftpservers = of_domain_get_domain_backupftpserver($id);
  $samba = run_query_domain_samba_properties($id);
  if ($obm_q->num_rows() == 1) {
    $block = html_domain_consult($obm_q, $mailservers, $samba, $properties_q, $syncservers, $solrservers, $imapfrontendservers, $backupftpservers, $opushservers, $opushfrontendservers, $ldapservers);
  } else {
    $display['msg'] .= display_err_msg($l_query_error);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display domain Consultation
// Parameters:
//   - $obm_q       : domain database result
//   - $mailservers : domain mailservers array
///////////////////////////////////////////////////////////////////////////////
function html_domain_consult($obm_q, $mailservers, $samba, $properties_q, $syncservers=NULL,$solrservers=NULL, $imapfrontendservers=NULL, $backupftpservers=NULL, $opushservers = NULL, $opushfrontendservers=NULL, $ldapservers=NULL) {
  global $display, $ico_delete, $l_mail_server, $l_sync_servers, $cgp_use, $l_properties, $l_solr_servers, $l_ldap_servers;
  global $l_label, $l_domain, $l_desc, $l_domain_name, $l_alias;
  global $l_host, $l_ip, $l_role, $l_mail_servers, $l_header_delete;

  // GLOBAL - automatic mail server chooser hooks
  global $c_mailchooser_global_hooks;
  
  $id = $obm_q->f('domain_id');
  $label = $obm_q->f('domain_label');
  $desc = $obm_q->f('domain_description');
  $domain_name = $obm_q->f('domain_name');
  $alias = $obm_q->f('domain_alias');
  $mail_server_auto = $obm_q->f('domain_mail_server_auto');
  $mail_server_host_name = $obm_q->f('mailserver_hostname');
  
  if ($cgp_use['service']['mail']) {
    $dis_mailserver = "<tr>
      <th>$l_mail_server</th>
      <td>";
    if ($mail_server_id) {
      $dis_mailserver .= "<a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&mailserver_id=$mail_server_id\">$mail_server_host_name</a>";
    } else {
      $no_mailserver = '-' ;
      // BEGIN - automatic mail server chooser hooks
        
      $hooks = $c_mailchooser_global_hooks;
      
      if (file_exists("$path/../conf/hooks/user/mailchooser.inc")) {
        require_once("$path/../conf/hooks/user/mailchooser.inc");
        $hooks = array_merge($hooks, $c_mailchooser_hooks);
      }
      
      if ($mail_server_auto) {
        foreach ($hooks as $hook) {
          $hook_name = $hook['name'];
          if (isset($GLOBALS[$hook_name])) $hook_name = $GLOBALS[$hook_name];
          
          if ($hook['id'] == $mail_server_auto)
            $no_mailserver = $hook_name;
        }
      }
      // END - automatic mail server chooser hooks
      
      $dis_mailserver .= $no_mailserver;
    }
    $dis_mailserver .= '</td>
    </tr>';
    
    // Domain mailservers
    // Get the domain mailservers
    $block_m = '';
    if (is_array($mailservers)) {
      foreach ($mailservers as $m_id => $mailserver_info) {
	// $mailserver_info is an array of info rows about this mail server
	foreach ($mailserver_info as $server_info) {
	  $name = $server_info['name'];
	  $ip = $server_info['ip'];
	  $role = $server_info['role'];
	  $block_m .= "
        <tr>
        <td>$name</td>
        <td>$ip</td>
        <td>$role</td>
        </tr>";
	}
      }
      if ($block_m != '') {
	$block_ms = "
      <div class=\"detail infos\">
      <h1>$l_mail_servers</h1>

      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_host</th>
        <th>$l_ip</th>
        <th>$l_role</th>
      </tr>
      </thead>
      $block_m
      </table>
      </div>";
      }
    }
  }

  // Domain LDAP servers
  $block_s = '';
  if (is_array($ldapservers)) {
    foreach ($ldapservers as $s_id => $ldapserver_info) {
      foreach ($ldapserver_info as $server_info) {
        $name = $server_info['name'];
        $ip = $server_info['ip'];
        $role = $server_info['role'];
        $block_ldapservers .= "
        <tr>
        <td>$name</td>
        <td>$ip</td>
        <td>$role</td>
        </tr>";
      }
    }
    if ($block_ldapservers != '') {
    $block_ldap = "
    <div class=\"detail infos\">
    <h1>$l_ldap_servers</h1>
  
    <table class=\"spreadSheet\">
    <thead>
    <tr>
    <th>$l_host</th>
    <th>$l_ip</th>
    <th>$l_role</th>
    </tr>
    </thead>
    $block_ldapservers
    </table>
    </div>";
    }
  }

    // Domain syncservers
    // Get the domain syncservers
  $block_s = '';
  if (is_array($syncservers)) {
    if (is_array($opushservers)) {
      $syncservers = array_merge($syncservers, $opushservers);
    }
    if (is_array($opushfrontendservers)) {
      $syncservers = array_merge($syncservers, $opushfrontendservers);
    }
    
    foreach ($syncservers as $s_id => $syncserver_info) {
            // $mailserver_info is an array of info rows about this mail server
            foreach ($syncserver_info as $server_info) {
              $name = $server_info['name'];
              $ip = $server_info['ip'];
              $role = $server_info['role'];
              $block_s .= "
            <tr>
            <td>$name</td>
            <td>$ip</td>
            <td>$role</td>
            </tr>";
            }
    }
    if ($block_s != '') {
            $block_sync = "
          <div class=\"detail infos\">
          <h1>$l_sync_servers</h1>

          <table class=\"spreadSheet\">
          <thead>
          <tr>
            <th>$l_host</th>
            <th>$l_ip</th>
            <th>$l_role</th>
          </tr>
          </thead>
          $block_s
          </table>
          </div>";
    }
  }


  // Domain solrservers
  // Get the domain solrservers
  $block_s = '';
  if (is_array($solrservers)) {
    foreach ($solrservers as $s_id => $solrserver_info) {
	    foreach ($solrserver_info as $server_info) {
	      $name = $server_info['name'];
	      $ip = $server_info['ip'];
	      $role = $server_info['role'];
	      $block_solrservers .= "
            <tr>
            <td>$name</td>
            <td>$ip</td>
            <td>$role</td>
            </tr>";
	    }
    }
    if ($block_solrservers != '') {
	    $block_solr = "
          <div class=\"detail infos\">
          <h1>$l_solr_servers</h1>

          <table class=\"spreadSheet\">
          <thead>
          <tr>
            <th>$l_host</th>
            <th>$l_ip</th>
            <th>$l_role</th>
          </tr>
          </thead>
          $block_solrservers
          </table>
          </div>";
    }
  }

  // Domain imap frontend server 
  // Get the domain imapfrontendservers
  $block_s = '';
  if (is_array($imapfrontendservers)) {
    foreach ($imapfrontendservers as $s_id => $imapfrontendserver_info) {
	    foreach ($imapfrontendserver_info as $server_info) {
	      $name = $server_info['name'];
	      $ip = $server_info['ip'];
	      $role = $server_info['role'];
	      $block_imapfrontendservers .= "
            <tr>
            <td>$name</td>
            <td>$ip</td>
            <td>$role</td>
            </tr>";
	    }
    }
    if ($block_imapfrontendservers != '') {
	    $block_imapfrontend = "
          <div class=\"detail infos\">
          <h1>$GLOBALS[l_imap_frontend_servers]</h1>

          <table class=\"spreadSheet\">
          <thead>
          <tr>
            <th>$l_host</th>
            <th>$l_ip</th>
            <th>$l_role</th>
          </tr>
          </thead>
          $block_imapfrontendservers
          </table>
          </div>";
    }
  }

  // Domain backup_ftp server 
  // Get the domain backupftpservers
  $block_s = '';
  if (is_array($backupftpservers)) {
    foreach ($backupftpservers as $s_id => $backupftpserver_info) {
	    foreach ($backupftpserver_info as $server_info) {
	      $name = $server_info['name'];
	      $ip = $server_info['ip'];
	      $role = $server_info['role'];
	      $block_backupftpservers .= "
            <tr>
            <td>$name</td>
            <td>$ip</td>
            <td>$role</td>
            </tr>";
	    }
    }
    if ($block_backupftpservers != '') {
	    $block_backupftp = "
          <div class=\"detail infos\">
          <h1>$GLOBALS[l_backup_ftp_servers]</h1>

          <table class=\"spreadSheet\">
          <thead>
          <tr>
            <th>$l_host</th>
            <th>$l_ip</th>
            <th>$l_role</th>
          </tr>
          </thead>
          $block_backupftpservers
          </table>
          </div>";
    }
  }


  if ($cgp_use['service']['samba'] && ($samba['drive'] || $samba['domain'] || $samba['sid'] || $samba['profile'] || $samba['home'])) {
    $dis_samba = "
      <div class=\"detail infos\">
      <h1>$GLOBALS[l_samba]</h1>
      <table>
       <tr>
         <th>$GLOBALS[l_samba_domain]</th><td>$samba[domain]</td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_sid]</th><td>$samba[sid]</td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_profile]</th><td>$samba[profile]</td>
       </tr>        
       <tr>
         <th>$GLOBALS[l_samba_home]</th><td>$samba[home]</td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_drive]</th><td>$samba[drive]</td>
       </tr>
      </table>
      </div>";
  }

  // Domain properties
  while ($properties_q->next_record()) {
    $plabel = $GLOBALS['l_'.$properties_q->f('domainproperty_key')];
    $value = $properties_q->f('domainpropertyvalue_value');
    $properties_block .= "
      <tr>
      <th>$plabel</th>
      <td>$value</td>
      </tr>";
  }
  if ($properties_block != '') {
    $properties_block = "
     <div class=\"detail infos\">
     <h1>$l_properties</h1>
     <table>
     $properties_block
     </table>
     </div>";
  }
  
  $display['detailInfo'] = display_record_info($obm_q);
  $display['title'] = $label;

  $block = "
  <div class=\"detail infos\">
  <h1>$l_domain</h1>
    <table>
    <tr>
      <th>$l_label</th>
      <td>$label</td>
    </tr>
    <tr>
      <th>$l_desc</th>
      <td>$desc</td>
    </tr>
    <tr>
      <th>$l_domain_name</th>
      <td>$domain_name</td>
    </tr>
    $dis_mailserver
    </table>
  </div>

  <div class=\"detail infos\">
  <h1>$l_alias</h1>
  <table>
  <tr>
    <th>$l_alias</th>
    <td>$alias</td>
  </tr>
  </table>
  </div>
$block_ms
$block_imapfrontend
$block_ldap
$block_sync
$block_solr
$block_backupftp
$dis_samba
$properties_block    
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain Form
// Parameters:
//   - $usr_q     : domain database result
//   - $domain      : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_domain_form($dom_q, $mailservers, $syncservers = NULL, $solrservers = NULL, $imapfrontendservers, $backupftpservers=NULL, $samba, $properties_q, $domain, $opushservers = NULL, $opushfrontendservers = NULL, $ldapservers = NULL) {
  global $l_label, $l_desc, $l_domain, $l_archive, $l_domain_name, $l_alias;
  global $l_insert, $l_update,$l_mail_server, $l_header_add_mailserver;
  global $display, $cgp_use, $c_none, $l_none, $l_properties;
  
  // if update mode and first time value are taken from database
  if ($domain['action'] == 'detailupdate') {
    $id = $dom_q->f('domain_id');
    $label = $dom_q->f('domain_label');
    $desc = $dom_q->f('domain_description');
    $domain_name = $dom_q->f('domain_name');
    $mail_server_auto = $dom_q->f('domain_mail_server_auto');
    $alias = $dom_q->f('domain_alias');
    while($properties_q->next_record()) {
      ${$properties_q->f('domainproperty_key')} = $properties_q->f('domainpropertyvalue_value');
    }
    $properties_q->seek(0);
    foreach ($mailservers as $mailserver_id => $mailserver) {
      foreach($mailserver as $server) {
        ${$server['role']}[] = $mailserver_id;
      }
    }
    foreach($syncservers as $syncserver_id => $syncserver) {
      $obm_sync[] = $syncserver_id;
    }
    foreach($solrservers as $solrserver_id => $solrserver) {
      $solr[] = $solrserver_id;
    }
    foreach($opushservers as $opushserver_id => $opushserver) {
      $opush[] = $opushserver_id;
    }
    foreach($opushfrontendservers as $opushfrontendserver_id => $opushfrontendserver) {
      $opush_frontend[] = $opushfrontendserver_id;
    }
    foreach($ldapservers as $ldapserver_id => $ldapserver) {
      $ldap[] = $ldapserver_id;
    }
    foreach($imapfrontendservers as $imapfrontendserver_id => $imapfrontendserver) {
      $imap_frontend[] = $imapfrontendserver_id;
    }
    foreach($backupftpservers as $backupftpserver_id => $backupftpserver) {
      $backup_ftp[] = $backupftpserver_id;
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($domain['domain_id'])) { $id = $domain['domain_id']; }
  if (isset($domain['label'])) { $label = $domain['label']; }
  if (isset($domain['desc'])) { $desc = $domain['desc']; }
  if (isset($domain['domain_name'])) { $domain_name = $domain['domain_name']; }
  if (isset($domain['mail_server_auto'])) { $mail_server_auto = $domain['mail_server_auto']; }
  if (isset($domain['alias'])) { $alias = $domain['alias']; }
  if (isset($domain['imap'])) { $imap = $domain['imap'];}
  if (isset($domain['smtp_in'])) { $smtp_in = $domain['smtp_in'];}
  if (isset($domain['smtp_out'])) { $smtp_out = $domain['smtp_out'];}
  if (isset($domain['obm_sync'])) { $obm_sync = $domain['obm_sync'];}
  if (isset($domain['solr'])) { $solr = $domain['solr'];}
  if (isset($domain['opush'])) { $opush = $domain['opush'];}
  if (isset($domain['opush_frontend'])) { $opush_frontend = $domain['opush_frontend'];}
  if (isset($domain['ldap'])) { $ldap = $domain['ldap'];}
  if (isset($domain['imap_frontend'])) { $imap_frontend = $domain['imap_frontend'];}
  if (isset($domain['backup_ftp'])) { $backup_ftp = $domain['backup_ftp'];}
  if (isset($domain['samba'])) { $samba = $domain['samba'];}

  while($properties_q->next_record()) {
    $readonly = $properties_q->f('domainproperty_readonly');
    if ($readonly != '1') {
      $name = $properties_q->f('domainproperty_key');
      $plabel = $GLOBALS["l_$name"];
      if (isset($domain[$name])) { 
	      ${$name} = $domain[$name]; 
      }
      $properties_block .= "
      <tr>
      <th><dfn title='".$GLOBALS["l_help_$name"]."' class='helpTooltip'>".$plabel."</dfn></th>
      <td><input type=\"text\" name=\"$name\" value=\"".${$name}."\" /></td>
      </tr>";
    }
  }
  if ($properties_block != '') {
    $properties_block = "
     <fieldset class=\"detail infos\">
     <legend>$l_properties</legend>
     <table>
     $properties_block
     </table>
     </fieldset>";
  }
  // Buttons
  if (($domain['action'] == 'new') || ($domain['action'] == 'insert')) {
    $dis_domain_name = "<input name=\"tf_domain_name\" size=\"48\" maxlength=\"128\" value=\"$domain_name\" />";
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
          <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($domain['action'] == 'detailupdate') || ($domain['action'] == 'update')) {
    $dis_domain_name = $domain_name;
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\">
      <input type=\"hidden\"  name=\"tf_domain_name\"  value=\"$domain_name\">
          <input type=\"hidden\" name=\"domain_id\" value=\"$id\">
          <input type=\"submit\" value=\"$l_update\">";
  }
  
  if ($cgp_use['service']['mail']) {
    $mail_server_selected = $mail_server_id;
    
    // BEGIN - automatic mail server chooser hooks
    
    if ($mail_server_auto)
      $mail_server_selected = "a$mail_server_auto";
      
    // END - automatic mail server chooser hooks
    
    // Mail server selection
    $dis_mailserver_auto = "
    <tr>
      <th class=\"$class[mail_server]\">$l_mail_server</th>
      <td>
      ".dis_domain_mailserver_select('imap', "$mail_server_selected", ($id == '')?'0':$id)."
      </td>
    </tr>";
    $serverlist = get_domain_mailservers(NULL, $id);
    if (is_array($imap)) {
      foreach($imap as $imapserver_id) {
        $imap_servers .= "
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'imapMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          ".$serverlist[$imapserver_id]['name']."<input type=\"hidden\" name=\"sel_imap[]\" value=\"$imapserver_id\" />  
          </div>";
      }
    }
    if (is_array($smtp_in)) {
      foreach($smtp_in as $smtp_inserver_id) {
        $smtp_in_servers .= "
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'smtp_inMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          ".$serverlist[$smtp_inserver_id]['name']."<input type=\"hidden\" name=\"sel_smtp_in[]\" value=\"$smtp_inserver_id\" />  
          </div>";
      }
    }
    if (is_array($smtp_out)) {
      foreach($smtp_out as $smtp_outserver_id) {
        $smtp_out_servers .= "
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'smtp_outMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          ".$serverlist[$smtp_outserver_id]['name']."<input type=\"hidden\" name=\"sel_smtp_out[]\" value=\"$smtp_outserver_id\" />  
          </div>";
      }
    }
    $imap_list = '<option value="">---</option>';
    $smtp_in_list = '<option value="">---</option>';
    $smtp_out_list = '<option value="">---</option>';
    foreach($serverlist as $host_id => $serverinfos) {
      if ($serverinfos['imap'] > 0) {
        $have_imap = true;
        $imap_list .= "<option value=\"$host_id\">$serverinfos[name] ($GLOBALS[l_imap])</option>";
      }
      if ($serverinfos['smtp_in'] > 0) {
        $have_smtp_in = true;
        $smtp_in_list .= "<option value=\"$host_id\">$serverinfos[name] ($GLOBALS[l_smtp_in])</option>";
      }
      if ($serverinfos['smtp_out'] > 0) {
        $have_smtp_out = true;
        $smtp_out_list .= "<option value=\"$host_id\">$serverinfos[name] ($GLOBALS[l_smtp_out])</option>";
      }  
    }
    if ($have_imap) {
      $dis_imap = "
        <fieldset class=\"detail infos\">
          <legend>$GLOBALS[l_imap] $l_mailserver</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_add_mailserver] <a href=\'\' onclick=\"copyField($('imapMailServerHome').getLast(), $('imapMailServerHome'));return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add imap mailserver]\" /></a></th>
        <td id='imapMailServerHome'>
          $imap_servers
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'imapMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          <select name='sel_imap[]'>$imap_list</select> 
          </div>              
        </td>
        </tr>
        </table>
        </fieldset>";
    } else {
      $dis_imap = "
        <fieldset class=\"detail infos\">
          <legend class='warning'>$GLOBALS[l_imap] $l_mailserver : $GLOBALS[l_mailserver_warn]</legend>
        </fieldset>";
    }
    if ($have_smtp_in) {
      $dis_smtp_in = "
        <fieldset class=\"detail infos\">
          <legend>$GLOBALS[l_smtp_in] $l_mailserver</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_add_mailserver] <a href=\'\' onclick=\"copyField($('smtp_inMailServerHome').getLast(), $('smtp_inMailServerHome'));return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add smtp_in mailserver]\" /></a></th>
        <td id='smtp_inMailServerHome'>
          $smtp_in_servers
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'smtp_inMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          <select name='sel_smtp_in[]'>$smtp_in_list</select> 
          </div>              
        </td>
        </tr>
        </table>
        </fieldset>";
    } else {
      $dis_smtp_in = "
        <fieldset class=\"detail infos\">
          <legend class='warning'>$GLOBALS[l_smtp_in] $l_mailserver : $GLOBALS[l_mailserver_warn]</legend>
        </fieldset>";
    }
    if ($have_smtp_out) {
      $dis_smtp_out = "
        <fieldset class=\"detail infos\">
          <legend>$GLOBALS[l_smtp_out] $l_mailserver</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_add_mailserver] <a href=\'\' onclick=\"copyField($('smtp_outMailServerHome').getLast(), $('smtp_outMailServerHome'));return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add smtp_out mailserver]\" /></a></th>
        <td id='smtp_outMailServerHome'>
          $smtp_out_servers
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'smtp_outMailServerHome');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          <select name='sel_smtp_out[]'>$smtp_out_list</select> 
          </div>              
        </td>
        </tr>
        </table>
        </fieldset>";
    } else {
      $dis_smtp_out = "
        <fieldset class=\"detail infos\">
          <legend class='warning'>$GLOBALS[l_smtp_out] $l_mailserver : $GLOBALS[l_mailserver_warn]</legend>
        </fieldset>";
    }

    $dis_mailserver = "
    $dis_imap
    $dis_smtp_in
    $dis_smtp_out";
    $dis_alias = "
     <fieldset class=\"detail infos\">
    <legend>$l_alias</legend>
    <table>
    <tr>".dis_domain_mail_field($alias,$class)."</tr>
    </table>
    </fieldset>";
  }

  if ($cgp_use['service']['samba']) {
    if (isset($samba['drive']) && $samba['drive'] != '') $drive[$samba['drive']] = 'selected="selected"';
    if(!($samba['drive'] || $samba['domain'] || $samba['sid'] || $samba['profile'] || $samba['home'])) {
      $disabled_s = 'disabled="true"';
      $style_s = 'style="display:none;"';
    } else {
      $checked_s = 'checked="checked"';
    }
    $dis_drive_sel .= "<option ".$drive["$c_none"]." value=''>$l_none</option>";
    for($i='D'; $i< 'Z';$i++) {
      $dis_drive_sel .= "<option ".$drive["$i"]." value='$i'>$i:</option>";
    }
    $dis_drive_sel .= "<option ".$drive["Z"]." value='Z'>Z:</option>";
    $dis_drive_sel = "<select $disabled_s style='width:4em;' name='samba[drive]'>$dis_drive_sel</select>";

    $dis_samba = "
      <fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_samba] <input type='checkbox' $checked_s onchange=\"$('sambaHome').getElements('input, select').setProperty('disabled', !this.checked); $('sambaHome').setStyle('display', (!this.checked)?'none':'');\"</legend>
      <table $style_s id='sambaHome'>
       <tr>
         <th>$GLOBALS[l_samba_domain]</th><td><input type='text' $disabled_s name='samba[domain]' value='$samba[domain]'/></td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_sid]</th><td><input type='text' $disabled_s name='samba[sid]' id='sambaSID' value='$samba[sid]'/>&nbsp;<input type='button' onclick='autoGenSid()' value='$GLOBALS[l_autogen_sid]' /></td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_profile]</th><td><input type='text' $disabled_s name='samba[profile]' value='$samba[profile]'/></td>
       </tr>        
       <tr>
         <th>$GLOBALS[l_samba_home]</th><td><input type='text' $disabled_s name='samba[home]' value='$samba[home]'/></td>
       </tr>
       <tr>
         <th>$GLOBALS[l_samba_drive]</th><td>$dis_drive_sel</td>
       </tr>
      </table>
      </fieldset>
      ";
  }

  $syncservers = get_domain_syncservers($id);
  if (is_array($obm_sync)) {
    foreach($obm_sync as $obm_sync_id) {
      $obm_sync_sel = $obm_sync_id;
    }
  }
  if (sizeof($syncservers) > 0) {
    foreach($syncservers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($obm_sync_sel) && $obm_sync_sel == $host_id) $selected = "selected='selected'";        
      $obm_sync_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_obm_sync = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_obm_sync]</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_select_syncserver]</th>
        <td id='obm_syncServer'>
          <div class=\"multiple\">
          <select name='sel_obm_sync[]'>
            <option value=''>---</option>
            $obm_sync_list
          </select> 
          </div>              
        </td>
        </tr>
        </table>
    </fieldset>";
  } else {
    $dis_obm_sync = "
      <fieldset class=\"detail infos\">
        <legend class='warning'>$GLOBALS[l_obm_sync] : $GLOBALS[l_syncserver_warn]</legend>
      </fieldset>";
  } 



  $solrservers = get_domain_solrservers($id);
  if (is_array($solr)) {
    foreach($solr as $solr_id) {
      $solr_sel = $solr_id;
    }
  }
  if (sizeof($solrservers) > 0) {
    foreach($solrservers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($solr_sel) && $solr_sel == $host_id) $selected = "selected='selected'";        
      $solr_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_solr = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_solr_servers]</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_select_solrserver]</th>
        <td id='obm_syncServer'>
          <div class=\"multiple\">
          <select name='sel_solr[]'>
            <option value=''>---</option>
            $solr_list
          </select> 
          </div>              
        </td>
        </tr>
        </table>
    </fieldset>";
  } else {
    $dis_solr = "
      <fieldset class=\"detail infos\">
        <legend class='warning'>$GLOBALS[l_solr_servers] : $GLOBALS[l_solrserver_warn]</legend>
      </fieldset>";
  } 

  $opushservers = get_domain_opushservers($id);
  if (is_array($opush)) {
    foreach($opush as $opush_id) {
      $opush_sel = $opush_id;
    }
  }
  if (sizeof($opushservers) > 0) {
    foreach($opushservers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($opush_sel) && $opush_sel == $host_id) $selected = "selected='selected'";
      $opush_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_opush = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_opush]</legend>
      <table>
      <tr>
      <th>$GLOBALS[l_header_select_opushserver]</th>
      <td id='obm_opushServer'>
      <div class=\"multiple\">
      <select name='sel_opush[]'>
      <option value=''>---</option>
      $opush_list
      </select>
      </div>
      </td>
      </tr>
      </table>
      </fieldset>";
  } else {
    $dis_opush = "
      <fieldset class=\"detail infos\">
      <legend class='warning'>$GLOBALS[l_opush] : $GLOBALS[l_opushserver_warn]</legend>
      </fieldset>";
  }

  $opush_frontend_servers = get_domain_opushfrontendservers($id);
  if (is_array($opush_frontend)) {
    foreach($opush_frontend as $opush_frontend_id) {
      $opush_frontend_sel = $opush_frontend_id;
    }
  }
  if (sizeof($opush_frontend_servers) > 0) {
    foreach($opush_frontend_servers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($opush_frontend_sel) && $opush_frontend_sel == $host_id) $selected = "selected='selected'";
      $opush_frontend_servers_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_opush_frontend = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_opush_frontend]</legend>
      <table>
      <tr>
      <th>$GLOBALS[l_header_select_opushfrontendserver]</th>
      <td id='obm_opushFrontendServer'>
      <div class=\"multiple\">
      <select name='sel_opush_frontend[]'>
      <option value=''>---</option>
      $opush_frontend_servers_list
      </select>
      </div>
      </td>
      </tr>
      </table>
      </fieldset>";
  } else {
    $dis_opush_frontend = "
      <fieldset class=\"detail infos\">
      <legend class='warning'>$GLOBALS[l_opush_frontend_servers] : $GLOBALS[l_opushfrontendserver_warn]</legend>
      </fieldset>";
  }

  $ldapservers = get_domain_ldapservers($id);
  if (is_array($ldap)) {
    foreach($ldap as $ldap_id) {
      $ldap_sel = $ldap_id;
    }
  }
  if (sizeof($ldapservers) > 0) {
    foreach($ldapservers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($ldap_sel) && $ldap_sel == $host_id) $selected = "selected='selected'";
      $ldap_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_ldap = "<fieldset class=\"detail infos\">
    <legend>$GLOBALS[l_ldap]</legend>
    <table>
    <tr>
    <th>$GLOBALS[l_header_select_ldapserver]</th>
    <td id='obm_ldapServer'>
    <div class=\"multiple\">
    <select name='sel_ldap[]'>
    <option value=''>---</option>
    $ldap_list
    </select>
    </div>
    </td>
    </tr>
    </table>
    </fieldset>";
  } else {
    $dis_ldap = "
    <fieldset class=\"detail infos\">
    <legend class='warning'>$GLOBALS[l_ldap] : $GLOBALS[l_ldapserver_warn]</legend>
    </fieldset>";
    }
  
  $imap_frontend_servers = get_domain_frontendservers($id);
  if (is_array($imap_frontend)) {
    foreach($imap_frontend as $imap_frontend_id) {
      $imap_frontend_sel = $imap_frontend_id;
    }
  }
  if (sizeof($imap_frontend_servers) > 0) {
    foreach($imap_frontend_servers as $host_id => $serverinfos) {
      $selected = '';
      if(isset($imap_frontend_sel) && $imap_frontend_sel == $host_id) $selected = "selected='selected'";        
      $imap_frontend_servers_list .= "<option value=\"$host_id\" $selected>$serverinfos[name]</option>";
    }
    $dis_imap_frontend = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_imap_frontend_servers]</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_select_imapfrontendserver]</th>
        <td id='obm_imapFrontendServer'>
          <div class=\"multiple\">
          <select name='sel_imap_frontend[]'>
            <option value=''>---</option>
            $imap_frontend_servers_list
          </select> 
          </div>              
        </td>
        </tr>
        </table>
    </fieldset>";
  } else {
    $dis_imap_frontend = "
      <fieldset class=\"detail infos\">
        <legend class='warning'>$GLOBALS[l_imap_frontend_servers] : $GLOBALS[l_imapfrontendserver_warn]</legend>
      </fieldset>";
  } 


  $backup_ftp_servers = get_domain_backupftpservers($id);
  if (sizeof($backup_ftp_servers) > 0) {
    if (is_array($backup_ftp)) {
      foreach($backup_ftp as $backup_ftp_id) {
        //$backup_ftp_sel = $backup_ftp_id;
        $backup_ftp_list .= "
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'ftp_server');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a>        
          ".$backup_ftp_servers[$backup_ftp_id]['name']."<input type=\"hidden\" name=\"sel_backup_ftp[]\" value=\"$backup_ftp_id\" />  
          </div>";
      }
    }
    $backup_ftp_servers_list = '<option value="">---</option>';
    foreach($backup_ftp_servers as $host_id => $serverinfos) {
      $backup_ftp_servers_list .= "<option value=\"$host_id\">$serverinfos[name]</option>";
    }
    $dis_backup_ftp = "<fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_backup_ftp_servers]</legend>
        <table>
        <tr>
        <th>$GLOBALS[l_header_select_backupftpserver]<a href=\'\' onclick=\"copyField($('obm_backupFtpServer').getLast(), $('obm_backupFtpServer'));return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add ftpserver]\" /></a></th>
        <td id='obm_backupFtpServer'>
          $backup_ftp_list
          <div class=\"multiple\">
          <a onclick=\"remove_element(this.parentNode,'ftp_server');show_hide_add_button();return false\" href=\"\">
           <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
          </a> 
          <select name='sel_backup_ftp[]'>
            $backup_ftp_servers_list
          </select> 
          </div>              
        </td>
        </tr>
        </table>
    </fieldset>";
  } else {
    $dis_backup_ftp = "
      <fieldset class=\"detail infos\">
        <legend class='warning'>$GLOBALS[l_backup_ftp_servers] : $GLOBALS[l_backupftpserver_warn]</legend>
      </fieldset>";
  } 


  $display['title'] = $label;

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_domain(this)) return true; else return false;\"
      action=\"" . url_prepare('domain_index.php') . "\">

    <fieldset class=\"detail infos\">
    <legend>$l_domain</legend>
    <table>
    <tr>
      <th>$l_label</th>
      <td><input name=\"tf_label\" size=\"24\" maxlength=\"32\" value=\"$label\" /></td>
    </tr>
    <tr>
      <th>$l_desc</th>
      <td><input name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
    </tr>
    <tr>
      <th>$l_domain_name</th>
      <td>$dis_domain_name</td>
    </tr>
    $dis_mailserver_auto
    </table>
    </fieldset>

    $dis_alias

    $dis_mailserver
    $dis_imap_frontend
    $dis_backup_ftp
    $dis_samba
    $dis_ldap
    $dis_obm_sync
    $dis_solr
    $dis_opush
    $dis_opush_frontend
    $properties_block
    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>";

  return $block;
}

/**
 * Display : domain applicable mailserver select (host from domain or global)
 * @param $role : restrict list to mail server role
 * @param $sel_id : selected entry
 * @param $domain_id : restrict list to mail server in this domain
 **/
function dis_domain_mailserver_select($role, $sel_id='', $domain_id='') {
  // GLOBAL - automatic mail server chooser hooks
  global $c_mailchooser_global_hooks;
  
  // BEGIN - automatic mail server chooser hooks
  
  $hooks = $c_mailchooser_global_hooks;
  
  if (file_exists("$path/../conf/hooks/user/mailchooser.inc")) {
    require_once("$path/../conf/hooks/user/mailchooser.inc");
    $hooks = array_merge($hooks, $c_mailchooser_hooks);
  }
  
  $selected = " selected=\"selected\"";
  
  $block_mailchooser = '';
  foreach ($hooks as $hook) {
    $hook_name = $hook['name'];
    if (isset($GLOBALS[$hook_name])) $hook_name = $GLOBALS[$hook_name];
    
    $block_mailchooser .= "<option value=\"a$hook[id]\"";
    if ("a$hook[id]" == $sel_id)
      { $block_mailchooser .= $selected; }
    $block_mailchooser .= ">$hook_name</option>";
  }
  
  // END - automatic mail server chooser hooks
  
  // select
  $sel = "<select id=\"sel_mail_server_id\" name=\"sel_mail_server_id\">";
  $sel .= "$block_mailchooser</select>";

  return $sel;
}


///////////////////////////////////////////////////////////////////////////////
// Display the domain delete validation screen
// Parameters:
//   - $p_id : domain id
//   - $warn_delete : entity link on domain
///////////////////////////////////////////////////////////////////////////////
function dis_domain_can_delete($p_id,$warn_delete) {
  global $l_can_delete, $l_back, $l_delete, $l_warn_delete;
  global $path, $display, $l_domain;

  $url = url_prepare('domain_index.php');

  $dis_back = "<a href=\"$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$p_id\">$l_back</a>";

  $dis_delete = "
  <input type=\"submit\" value=\"$l_delete\" />
  ";

  $infos_q = run_query_domain_detail($p_id);
  $title = $infos_q->f('domain_label');
  $display['title'] = $title;
  if($warn_delete)
    $display['msg'] .= display_warn_msg($l_warn_delete);
  else
    $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
  <form action=\"$path/domain/domain_index.php\" method=\"post\" onsubmit=\"return confirm_del(this)\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"domain_id\" value=\"$p_id\" />
    <div id='domain_spinner' style='display:none;' 'class=\"C\">
      <img src='$GLOBALS[ico_spinner]' />
    </div>
    <div id='buttons' class=\"buttons\">
      $dis_delete
      $dis_back
    </div>
  </form>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the domain Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_domain_display_pref($prefs) {
  global $l_domain_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'domain');
  $dis_pref->pref_title = $l_domain_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}

/**
 * Generate alias field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_domain_mail_field($values,$class) {
  if(!empty($values)) {
    $aliases = explode("\r\n",$values);
  }
  $count = 0;
  if(is_array($aliases)) {
    foreach($aliases as $key => $alias) {
      $alias_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'domainDomainHome');show_hide_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='tf_alias[]' value='$alias' />
        </div>";
      $count ++;
    }
  }
  if($GLOBALS['c_max_domain_alias'] ==0 || $count < $GLOBALS['c_max_domain_alias']) {
    $alias_block .= "
      <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'domainDomainHome');show_hide_add_button();return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
      </a>        
      <input name='tf_alias[]' value='' /> 
      </div>         
      ";
    $count++;
  } 
  if($GLOBALS['c_max_domain_alias'] ==0 || $count < $GLOBALS['c_max_domain_alias']) {
    $add_button = "<a id='addDomainButton' href=\'\' onclick=\"add_alias_field();show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add alias field]\" /></a>";
  }
  
  $alias_block = "
  <th class=\"$class[alias]\" id='domainDomainLabel'>
  $GLOBALS[l_alias]
  $add_button
  </th>
  <td id='domainDomainHome'>
  $alias_block
  </td>
  ";

  return $alias_block;
}

?>
