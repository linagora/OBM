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
// OBM - File : userbackup_display.inc                                       //
//     - Desc : Backup/restore Display functions File                        //
// 2012-07-17 Alexis Gavoty                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

include_once("$obminclude/global.inc");

///////////////////////////////////////////////////////////////////////////////
// Display the user backup and restore form
///////////////////////////////////////////////////////////////////////////////

function dis_user_backup_form($backup, $params) {
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $user_id = $params['user_id'];
  $obm_q = run_query_user_detail($user_id);
  $url = url_prepare('userbackup_index.php');

  //New backup form
  if (check_user_can_backup($user_id)) {
    $dis_new_backup = "
    <form method=\"post\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"backup\" />
      <input type=\"hidden\" name=\"execute\" value=\"1\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">

      <fieldset class=\"buttons\">
        <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_do_backup']}\" />
      </fielset>
    </form>";
  } else {
    $dis_new_backup = "<p>{$GLOBALS['l_cant_backup_pending_modifications']}</p>";
  }

  try {
    $backups = $backup->availableBackups();
    foreach ($backups as $record) {
      $date = new Of_Date($record['datetime']);
      $dis_date = $date->getOutputDateTime();
      $dis_buttons = '';
      foreach (OBM_Satellite_RestoreEntity::$restoreData as $method) {
        $dis_buttons .= "
        <form method=\"post\" action=\"$url\">
          <input type=\"hidden\" name=\"action\" value=\"restore\" />
          <input type=\"hidden\" name=\"execute\" value=\"1\" />
          <input type=\"hidden\" name=\"filename\" value=\"{$record['filename']}\">
          <input type=\"hidden\" name=\"method\" value=\"$method\">
          <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">
          <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_restore_'.$method]}\" />
        </form>";
      }
      $dis_backups .= "<tr><td>$dis_date</td><td>{$record['filename']}</td>";

      if (Perm::check_right('userbackup', $cright_write)) 
        $dis_backups .= "<td><div class=\"buttons\">$dis_buttons</div></td>";

      $dis_backups .= "</tr>";
    }

    if (!empty($dis_backups)) {
      $dis_backups_content = $dis_backups;
      $dis_backups = "
        <table class=\"spreadSheet\">
          <thead><tr><th>{$GLOBALS['l_date']}</th><th>{$GLOBALS['l_file']}</th>";
          if (Perm::check_right('userbackup', $cright_write)) 
            $dis_backups .= "<th>{$GLOBALS['l_restore']}</th>";

          $dis_backups .= "</tr></thead>
          <tbody>$dis_backups_content</tbody>
        </table>";
    }

  } catch (Exception $e) {
    $display['msg'] .= display_err_msg($e->getMessage());
  }

  if (empty($dis_backups)) {
    $dis_backups = "<p>{$GLOBALS['l_no_backup_available']}</p>";
  }

  $backupftpservers = of_domain_get_domain_backupftpserver($obm_q->f('userobm_domain_id'));
  if (!empty($backupftpservers)) {
    //FTP backups
    $ftp_desc = sprintf($GLOBALS['l_retrieve_from_backup_ftp'],$obm_q->f('userobm_login').'@'.$obm_q->f('domain_name'));
    $dis_ftp_backups = "
      <form method=\"post\" action=\"$url\">
        <input type=\"hidden\" name=\"action\" value=\"backup\" />
        <input type=\"hidden\" name=\"retrieveAll\" value=\"1\" />
        <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">

        <p>$ftp_desc</p>

        <fieldset class=\"buttons\">
          <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_retrieve_from_ftp']}\" />
        </fieldset>
      </form>";
  } else {
    $dis_ftp_backups = "
      <p>{$GLOBALS['l_no_backup_ftp_specified']}</p>";
  }

  $block = "";

  if (Perm::check_right('userbackup', $cright_write_admin)) {
    $block .= "
      <div class=\"detail extra\">
        <h1>{$GLOBALS['l_new_backup']}</h1>
        $dis_new_backup
      </div>
      ";
  }

  $block .= "<div class=\"detail extra\">
      <h1>{$GLOBALS['l_available_backups']}</h1>
      $dis_backups
    </div>
  ";

  return $block;
}

 ?>
