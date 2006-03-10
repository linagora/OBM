<?php
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
function dis_backup_index($backup="") {
  global $display;

  $display["search"] = dis_backup_search_form($backup);
  $display["result"] = dis_backup_search_list($backup);

  return $true;
}


///////////////////////////////////////////////////////////////////////////////
// Display Backup search form
// Parameters:
//   - $backup[] : hash with backup values
///////////////////////////////////////////////////////////////////////////////
function dis_backup_search_form($backup="") {

  $block .= html_backup_search_form($backup);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Backup search Form
// Parameters:
//   - $backup[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_backup_search_form($backup) {
  global $display, $l_name, $l_name;
  global $c_all, $l_all, $l_find, $l_author;

  $filename = stripslashes($backup["filename"]);
  $author = stripslashes($document["author"]);

  $url = url_prepare("backup_index.php");

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"32\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_filename\" id=\"tf_title\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp; 
    </div>
    <div class=\"searchEnd\"></div>
  </div>
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
  global $auth;

  $files = get_backup_list($backup);
  $nb_files = count($files);
  if ($nb_files == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
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
  global $display, $path, $l_found, $l_file, $l_date, $l_size, $l_version;
  global $l_bytes, $l_restore, $l_delete;

  $author = stripslashes($document["author"]);
  $filename = stripslashes($backup["filename"]);

  foreach ($files as $file => $one_file) {
    $cpt++;
    $data = "data" . ($cpt % 2);
    $date = datetime_format($one_file["date"]);
    $version = $one_file["version"];
    $size = $one_file["size"];

    $dis_files .= "
      <tr class=\"$data\">
        <td style=\"text-align: center;\">$file</td>
        <td style=\"text-align: right;\">$date &nbsp;</td>
        <td style=\"text-align: right;\">$version &nbsp;</td>
        <td style=\"text-align: right;\">$size &nbsp;</td>
        <td style=\"text-align: center;\"><a href=\"$path/backup/backup_index.php?action=restore&amp;param_backup=$file\">$l_restore</a></td>
        <td style=\"text-align: center;\"><a href=\"$path/backup/backup_index.php?action=check_delete&amp;param_backup=$file\">$l_delete</a></td>
      </tr>";
  }

  $display["msg"] .= display_info_msg("$nb_files $l_found");

  $block = "
    <table class=\"resultTable\">
    <tr>
      <td class=\"resultHead\">$l_file</td>
      <td class=\"resultHead\">$l_date</td>
      <td class=\"resultHead\">$l_version</td>
      <td class=\"resultHead\">$l_size ($l_bytes)</td>
      <td class=\"resultHead\">$l_restore</td>
      <td class=\"resultHead\">$l_delete</td>
    </tr>
    $dis_files
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation form to create a backup
///////////////////////////////////////////////////////////////////////////////
function dis_backup_form() {
  global $display, $l_back, $l_confirm, $l_can_create;

  $url = url_prepare("backup_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"index\" />
    <input type=\"hidden\" name=\"param_backup\" value=\"$filename\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_ok = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"submit\" value=\"$l_confirm\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_create);

  $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_ok</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the backup can be deleted, and the form
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_backup($filename) {
  global $display, $l_can_delete, $l_cant_delete, $l_back, $l_delete;

  $url = url_prepare("backup_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"index\" />
    <input type=\"hidden\" name=\"param_backup\" value=\"$filename\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_backup\" value=\"$filename\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_delete</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Consult File
///////////////////////////////////////////////////////////////////////////////
function dis_backup_file($doc_q) {
  global $cdoc_kind_link;

  $name = $doc_q->f("document_name");  
  $kind = $doc_q->f("document_kind");

  // If document is a link we transfer to the link
  if ($kind == $cdoc_kind_link) {
    header("location: $name");
    exit;

  } else {

    $id = $doc_q->f("document_id");
    $mime = $doc_q->f("documentmimetype_mime");  
    $disk_path = get_document_disk_path($id);
    $disk_fullname = $disk_path . $id; 

    // Else Document is a file, we send the file
    $handle = fopen ($disk_fullname, "r");
    header("Content-Type: $mime");
    header("Content-Disposition: inline; filename=$name");
    echo fread($handle, filesize ($disk_fullname));
    fclose ($handle); 
  
  }

}

?>
