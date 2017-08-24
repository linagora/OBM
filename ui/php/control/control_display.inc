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
// OBM - File : control_display.inc                                          //
//     - Desc : Control panel Display functions File                         //
// 2002-09-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Control panel Consultation
// Parameters:
//   - $sys_q     : system database result
//   - $domain_id : domain id to process
///////////////////////////////////////////////////////////////////////////////
function html_control_consult($domain_id="") {
  global $l_control, $l_system, $l_client, $l_model, $l_version;
  global $gp_client, $gp_model, $obm_version;
  global $l_network, $l_domain, $l_mail_domain, $l_mail_alias;
  global $l_mail_mode, $l_relay, $l_server;
  global $l_global_stats, $l_private, $l_public, $l_archive;
  global $l_nb_user, $l_nb_group, $l_nb_mailshare, $l_disk_space;
  global $l_mail_stats, $l_mail_sent, $l_mail_recv, $l_mail_kb_sent, $l_mail_kb_recv;
  global $l_maintenance, $l_sys_update;
  global $c_yes, $c_no;
  global $obm, $cgp_use, $display;

  $display['title'] = $l_control;

  if ($cgp_use['service']['mail']) {

    $domain_label = $obm['domain_label'];
    $domain_name = $obm['domain_name'];
    $domain = of_domain_get_domain_infos($obm['domain_id']);
    $domain_alias = $domain['alias'];

    $mode_mail = ($mail_relay ? $l_relay : $l_server);
    if (! $mail_relay) {
      $mail_server = '';
    }
    if ($mail_relay) $c_mail_relay = 'checked';
    if ($mail_server) $c_mail_server = 'checked';

    $block_mail_info = "
    <div class=\"detail infos\">
    <h1>$l_network</h1>

    <table>
    <tr>
      <th>$l_domain</th>
      <td>$domain_label</td>
    </tr>
    <tr>
      <th>$l_mail_domain</th>
      <td>$domain_name</td>
    </tr>
    <tr>
      <th>$l_mail_mode</th>
      <td>$mode_mail</td>
    </tr>
    <tr>
      <th>$l_mail_alias</th>
      <td>$domain_alias</td>
    </tr>
    </table>

    </div>
";
  
    $mail_stats = get_mail_stats();
    $mail_sdate = $mail_stats['mail_start_date'];
    $mail_edate = $mail_stats['mail_end_date'];
    $mail_sent = $mail_stats['mail_sent'];
    $mail_recv = $mail_stats['mail_recv'];
    $mail_kb_sent = $mail_stats['mail_kb_sent'];
    $mail_kb_recv = $mail_stats['mail_kb_recv'];
      
    $block_mail_stats = "
    <div class=\"detail infos\">
    <h1>$l_mail_stats du $mail_sdate au $mail_edate</h1>

    <table>
    <tr>
      <th>$l_mail_sent</th>
      <td>$mail_sent</td>
    </tr>
    <tr>
      <th>$l_mail_recv</th>
      <td>$mail_recv</td>
    </tr>
    <tr>
      <th>$l_mail_kb_sent</th>
      <td>$mail_kb_sent</td>
    </tr>
    <tr>
      <th>$l_mail_kb_recv</th>
      <td>$mail_kb_recv</td>
    </tr>
    </table>
    </div>
";
  }

  $u = get_control_user_infos($domain_id);
  $g = get_control_group_infos($domain_id);
  $g_total = $g['nb_public'] + $g['nb_private'];
  $nb_mailshare = get_control_mailshare_infos($domain_id);
  $disk_use = get_sys_disk_use();
  
  //  $system_update = get_update_state();
  $sys_update = ($system_update ? $c_yes : $c_no);

  $block = "
    <div class=\"detail infos\">
    <h1>$l_system</h1>

    <table>
    <tr>
      <th>$l_client</th>
      <td>$gp_client</td>
    </tr>
    <tr>
      <th>$l_model</th>
      <td>$gp_model</td>
    </tr>
    <tr>
      <th>$l_version</th>
      <td>$obm_version</td>
    </tr>
    </table>
    </div>

    $block_mail_info
    
    <div class=\"detail extra\">
    <h1>$l_global_stats</h1>

    <table>
    <tr>
      <th>$l_nb_user ($l_archive)</th>
      <td>$u[nb] ($u[nb_archive])</td>
    </tr>
    <tr>
      <th>$l_nb_group ($l_public / $l_private)</th>
      <td>$g_total ($g[nb_public] / $g[nb_private])</td>
    </tr>
    <tr>
      <th>$l_nb_mailshare</th>
      <td>$nb_mailshare</td>
    </tr>
    <tr>
      <th>$l_disk_space</th>
      <td>$disk_use</td>
    </tr>
    </table>
    </div>

    $block_mail_stats
    
    <div class=\"detail infos\">
    <h1>$l_maintenance</h1>

    <table>
    <tr>
      <th>$l_sys_update</th>
      <td>$sys_update</td>
    </tr>
    </table>
    </div>";

  return $block;
}


?>
