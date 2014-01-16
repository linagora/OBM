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
// OBM - File : backup_display.inc                                           //
//     - Desc : Backup Display File                                          //
//  2005-08-22 Aliacom - Pierre Baudracco                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Backup index
// Parameters:
//   - $backup[] : hash with backup values
///////////////////////////////////////////////////////////////////////////////
function dis_backup_index($backup='') {
  global $display;

  $display['search'] = dis_backup_search_form($backup);
  $display['result'] = dis_backup_search_list($backup);

  return $true;
}


///////////////////////////////////////////////////////////////////////////////
// Display Backup search form
// Parameters:
//   - $backup[] : hash with backup values
///////////////////////////////////////////////////////////////////////////////
function dis_backup_search_form($backup='') {

  $block .= html_backup_search_form($backup);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Backup search Form
// Parameters:
//   - $backup[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_backup_search_form($backup) {
  global $display, $l_find, $l_date, $l_version;

  $filename = stripslashes($backup['filename']);
  $version = stripslashes($backup['version']);
  $date = of_date_upd_format(stripslashes($backup['date']));

  $url = url_prepare('backup_index.php');

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
    <label>$l_version
      <input type=\"text\" name=\"tf_version\" id=\"tf_version\" size=\"8\" maxlength=\"8\" value=\"$version\" />
    </label>
    <label>$l_date
    <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
    </label>
    <label>
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp; 
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Backup search result
// Parameters:
//   - $backup[] : backup search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_backup_search_list($backup) {
  global $display, $l_no_found;

  $files = get_backup_list($backup);
  $nb_files = count($files);
  if ($nb_files == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $block = html_backup_search_list($files, $nb_files);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters :
//   - $files    : array of backup files
//   - $nb_files : nb backup files
///////////////////////////////////////////////////////////////////////////////
function html_backup_search_list($files, $nb_files) {
  global $display, $path, $l_backup_found, $l_file, $l_date, $l_size, $l_version;
  global $l_bytes, $l_restore, $l_delete;

  foreach ($files as $file => $one_file) {
    $cpt++;
    $data = 'data' . ($cpt % 2);
    $date = of_datetime_format($one_file['date']);
    $version = $one_file['version'];
    $size = $one_file['size'];

    $dis_files .= "
      <tr class=\"$data\">
        <td style=\"text-align: center;\">$file</td>
        <td style=\"text-align: right;\">$date &nbsp;</td>
        <td style=\"text-align: right;\">$version &nbsp;</td>
        <td style=\"text-align: right;\">$size &nbsp;</td>
        <td style=\"text-align: center;\"><a href=\"$path/backup/backup_index.php?action=restore&amp;tf_filename=$file\">$l_restore</a></td>
        <td style=\"text-align: center;\"><a href=\"$path/backup/backup_index.php?action=check_delete&amp;tf_filename=$file\">$l_delete</a></td>
      </tr>";
  }

  $display['msg'] .= display_info_msg("$nb_files $l_backup_found");

  $block = "
  <div class=\"result\">
    <table>
    <thead>
    <tr>
      <td class=\"resultHead\">$l_file</td>
      <td class=\"resultHead\">$l_date</td>
      <td class=\"resultHead\">$l_version</td>
      <td class=\"resultHead\">$l_size ($l_bytes)</td>
      <td class=\"resultHead\">$l_restore</td>
      <td class=\"resultHead\">$l_delete</td>
    </tr>
    </thead>
    $dis_files
    </table>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation form to create a backup
///////////////////////////////////////////////////////////////////////////////
function dis_backup_form() {
  global $path, $display, $l_back, $l_confirm, $l_can_create;

  $dis_back = "<a href=\"$path/backup/backup_index.php?action=index\">$l_back</a>";

  $dis_ok = "<a href=\"$path/backup/backup_index.php?action=insert\">$l_confirm </a>";

  $display['msg'] .= display_ok_msg($l_can_create);

  $block .= "<div class=\"buttons\">
             $dis_ok
             $dis_back
             </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the backup can be deleted, and the form
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_backup($filename) {
  global $path, $display, $l_can_delete, $l_cant_delete, $l_back, $l_delete;


  $dis_back = "<a href=\"$path/backup/backup_index.php?action=index&amp;tf_filename=$filename\">$l_back</a>";

  $dis_delete = "<a href=\"$path/backup/backup_index.php?action=delete&amp;tf_filename=$filename\">$l_delete</a>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
      <div class=\"buttons\">
        $dis_delete
        $dis_back
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Consult File
///////////////////////////////////////////////////////////////////////////////
function dis_backup_file($doc_q) {
  global $cdoc_kind_link;

  $name = $doc_q->f('document_name');  
  $kind = $doc_q->f('document_kind');

  // If document is a link we transfer to the link
  if ($kind == $cdoc_kind_link) {
    header("location: $name");
    exit;

  } else {

    $id = $doc_q->f('document_id');
    $mime = $doc_q->f('documentmimetype_mime');  
    $disk_path = get_document_disk_path($id);
    $disk_fullname = $disk_path . $id; 

    // Else Document is a file, we send the file
    $handle = fopen ($disk_fullname, 'r');
    header("Content-Type: $mime");
    header("Content-Disposition: inline; filename=$name");
    echo fread($handle, filesize ($disk_fullname));
    fclose ($handle); 
  
  }

}

?>
