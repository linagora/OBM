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
// OBM - File : odt_index.inc
//     - Desc : odt Main file
// 2006-02-04 Aliacom - Pierre Baudracco
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    
// - export                --             -- display a odt
// - export_multiple       --                -- display a multiple bar odt
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "odt";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

if ($action == "") $action = "index";
$odt = get_param_odt();

///////////////////////////////////////////////////////////////////////////////
// Main program
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "export") {
///////////////////////////////////////////////////////////////////////////////
  include_once("$obminclude/lib/TBS/tbs_class.php");
  include_once("$obminclude/lib/TBS/tbsooo_class.php");
  $template_full_path = get_document_disk_full_path($odt["template"]);
  
  
  $template_mime_type = get_document_mime_type($odt["template"]);
  export2odt($template_full_path, $template_mime_type, $odt["data"], true);

}



//////////////////////////////////////////////////////////////////////////////
// Create OpenDocument from template and data
// Parameters:
//   - $template_full_path : path to the template
//   - $data : associative array containing data :  variables (TBS 'var') and blocks (TBS 'block')
//   - $display : boolean TRUE if odt created must be displayed, FALSE if not
//   - $save_path : location where odt must be saved (if $display == FALSE)
// Returns
//   - TRUE if successfull
//   - FALSE if it fails (with error ???) 
//////////////////////////////////////////////////////////////////////////////
function export2odt($template_full_path, $template_mime_type, $data, $display, $save_path=""){
  
  $data_vars = $data["data_vars"];
  $data_blocks = $data["data_blocks"];
 
  // instantiate a TBS OOo class
  $OOo = new clsTinyButStrongOOo; 
  
  // setting the object
  if ($OOo->SetZipBinary('zip', true) == false) {
    // erreur zip non présent
  }
  if ($OOo->SetUnzipBinary('unzip', true) == false) {
    // erreur unzip non présent
  }
  if ($OOo->SetProcessDir('/tmp/') == false) {
    // erreur /tmp inaccessible
  }

  // create a new openoffice document from the template with an unique id
 
 $OOo->NewDocFromTpl($template_full_path);
 
 // merge data with OOo file content.xml
  $OOo->LoadXmlFromDoc('content.xml');
  
  
  // load Data
  foreach($data_vars as $key => $value){
    $GLOBALS[$key] = $value;
  }

  
  // Merge Data
  foreach ($data_blocks as $key => $value) {
    $OOo->MergeBlock($key, $value);
  }
  $OOo->SaveXmlToDoc();
  
  if ($display){
   
      if ($template_mime_type == "")
        $template_mime_type = $OOo->GetMimetypeDoc();
      if ($template_mime_type == "")
        $template_mime_type = "sxw";
     
      header('Content-type: '.$template_mime_type);
      header('Content-Length: '.filesize($OOo->GetPathnameDoc())); 
      header('Content-Disposition: inline; filename=cv.'.$template_mime_type);
      $OOo->FlushDoc();
      $OOo->RemoveDoc(); 
  }
  else {
    if ($save_path != "") {
      rename($OOo->GetPathnameDoc(), $save_path);
      // affichage des document sauvés ?????
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk path of a document 
// Parameters:
//   - $id   : document id
// Returns:
//   real full disk path
///////////////////////////////////////////////////////////////////////////////
function get_document_disk_full_path($id) {
  global $cdocument_root;

  // The document disk path set in the subdirectory named "last id number"
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = $cdocument_root . "/" . $rel . "/" . $id;
  
  return $disk_path; 
}


/////////////////////////////////////////////////////////////////////////////////
// Returns a document mime type extension
// Parameter :
//   - $id : document id
// Returns :
//    document mime type if ok, null string if undefined
/////////////////////////////////////////////////////////////////////////////////

function get_document_mime_type($id) {
  global $cdg_sql;
  
  $query = "SELECT documentmimetype_extension
            FROM Document
            JOIN DocumentMimeType ON document_mimetype_id = documentmimetype_id
            WHERE document_id = '$id'";
            
  
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  if ($obm_q->num_rows() == 1){
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_extension");
  }
  else 
    return "";
}

///////////////////////////////////////////////////////////////////////////////
// Stores Odt parameters transmitted in $odt hash
// returns : $odthash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_odt() {
  $odt = array();

  $odt["template"] = $_POST["template"];
  $odt["save_path"] = stripslashes($_POST["save_path"]);
  $odt["data"] = unserialize(urldecode(stripslashes($_POST["data"])));
  $odt["action"] = $_POST["action"];
  
  display_debug_param($odt);

  return $odt;
}
?>
