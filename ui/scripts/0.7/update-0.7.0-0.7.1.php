<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-0.7.0-0.7.1.php                                       //
// 2003-09-18 Mehdi Rande                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");
include("php/document/document_query.inc");
global $document_path;
$db = new DB_OBM;
$query = "select globalpref_value from GlobalPref where
          globalpref_option ='document_path'";
$db->query($query);
$db->next_record();

$document_path = $db->f("globalpref_value");	 
update_document();

///////////////////////////////////////////////////////////////////////////////
// Update the documents database. 
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function update_document() {
  global $document_path;
  echo " Updating Documents :\n";
  echo " It while store all youre unstored documents into Obm database\n";
  echo " After this we advise you to supress all alias \n";
  echo " For example if you have two documents which referes to the same file \n";
  echo " You should supress one. \n";
  sleep(5);
  echo "Begin\n";
  echo "Updating path\n";
  $query = "select *from Document";
  $db = new DB_OBM;
  $db2 = new DB_OBM;
  $db->query($query);
  while($db->next_record()) {
    $path = $db->f("document_path");
    $name = $db->f("document_name");
    $id = $db->f("document_id");
    $kind = $db->f("document_kind");
    if($kind == "") $kind = 1;
    if(strrpos($path,'/') != strlen($path) -1) {
      $path = $path.'/';
    }
    if(substr_count("$name",'/')>0) {
      $kind = 2;
    }
    $query = "update Document set document_path = '$path', document_kind = '$kind '
    where document_id = '$id'";
    $db2->query($query);
    $kind ="";
  }
  echo "End updating path\n";
  echo "Updating Database\n";

  dis_repository_rec($document_path);
  echo "End updating Database\n";
}

///////////////////////////////////////////////////////////////////////////////
// Perform all repository in the obm file system and add the files and 
// repository which are not already store
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function dis_repository_rec($repository) {
  global $document_path,$link_to;
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory,$set_theme;

  echo "Processing $repository \n";
  $db = new DB_OBM;
  $root_handler = opendir($repository);
  $relative_path = substr($repository,strlen($document_path)).'/'; 
  
  if($repository == $document_path) {
    $name = "/";
    $relative_path = $name;
  }
  else {
    $name = substr($repository,strrpos($repository,'/')+1);
  }

  while($file = readdir($root_handler)) {
    $path = "$repository/$file";
    if($file != ".." && $file != ".") {
      $query = "select count(*) as exist from Document
      where document_path ='$relative_path' and document_name = '$file'";
      $db->query($query);
      $db->next_record();
      if(is_dir($path)) {
	if($db->f("exist") == 0) {
	  echo "Unstored Repository : $path\n";
	  $query = "insert into Document (
	    document_timeupdate,
	    document_timecreate,
	    document_userupdate,
	    document_usercreate,
	    document_title,
	    document_author,
	    document_name,
	    document_path,
	    document_kind,
	    document_size,
	    document_category1,
	    document_category2,
	    document_mimetype,
	    document_private
	  )
	  values (
	    null,
	    '" . date("Y-m-d H:i:s") . "',
	    null,
	    '0',
	    '',
	    '',
	    '$file',
	    '$relative_path',
	    '0',
	    '',
	    '',
	    '',
	    '',
	    '0'
	  )";
	  $db->query($query);
	}
	dis_repository_rec($path);
      }
      else {
	if(substr_count($file,$link_to) == 1) {
	  echo "Removing Link : $path\n";
	  unlink($path);
	}
      	elseif($db->f("exist") == 0) {
	  echo "Unstored File : $path\n";
	  $ext = substr($path,strrpos($path,'/'));
	  $ext = substr(strrchr($path,'.'),1);
	  $mime = run_query_get_type_by_ext($ext);
	  $stat = stat($path);
	  $query = "insert into Document (
	    document_timeupdate,
	    document_timecreate,
	    document_userupdate,
	    document_usercreate,
	    document_title,
	    document_author,
	    document_name,
	    document_path,
	    document_kind,
	    document_size,
	    document_category1,
	    document_category2,
	    document_mimetype,
	    document_private
	  )
	  values (
	    null,
	    '" . date("Y-m-d H:i:s") . "',
	    null,
	    '0',
	    '$file',
	    'System',
	    '$file',
	    '$relative_path',
	    '1',
	    '".$stat["size"]."',
	    '0',
	    '0',
	    '$mime',
	    '0'
	  )";
	  $db->query($query);
	  echo "File stored\n";
	}
      }
    }
  }
  closedir($root_handler);
}

