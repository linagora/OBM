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
// OBM - File : document_query.inc                                           //
//     - Desc : document query File                                          //
// 2003-08-21 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

class OverQuotaDocumentException extends Exception {}

///////////////////////////////////////////////////////////////////////////////
// Document search query execution
// Parametes:
//   - $document[]    : document search criteria
//     keys used       archive, name, phone, kind, zip
///////////////////////////////////////////////////////////////////////////////
function run_query_document_search($document) {
  global $c_all, $cdg_sql, $obm, $ctu_sql_limit, $cdoc_kind_dir;
  $obm_q = new DB_OBM;

  $sql_order_dir = $document["sql_order_dir"];
  $sql_order_field = $document["sql_order_field"];

  $title = sql_search_text_parse($obm_q->escape($document["title"]));
  $author = sql_search_text_parse($obm_q->escape($document["author"]));
  $mime = $document["mime"];
  $name = sql_search_text_parse($obm_q->escape($document["name"]));
  $entity = $document["entity"];
  $entity_id = $document["entity_id"];  

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timecreate = sql_date_format($db_type, "document_timecreate", "timecreate");
  $timeupdate = sql_date_format($db_type, "document_timeupdate", "timeupdate");
  $multidomain = sql_multidomain("document");
  
  if (!isset($document["private"]) || $document["private"] == 1) {
    $where .= "(document_privacy = '1' AND document_usercreate = '$obm[uid]')";
  } else {
    $where .= sql_obm_entity_privacy("document");
  }
  $where .= " AND document_kind != $cdoc_kind_dir";

  if (($entity != "") && ($entity != "document")) {
    $entity_id = of_entity_get($entity, $entity_id);
    $join_ent = "LEFT JOIN DocumentLink on document_id = documentlink_document_id";
    $where .= " AND documentlink_entity_id = $entity_id ";
  }

  if ($title != "") {
    $where .= " AND document_title $like '%".$obm_q->escape($title)."%'";
  }
  
  if ($name != "") {
    $where .= " AND document_name $like '%".$obm_q->escape($name)."%'";
  }

  if (($author != $c_all) && ($author != "")) { 
    $where .= " AND document_author $like '%".$obm_q->escape($author)."%'";
  }
  
  if (($mime != $c_all) && ($mime != "")) { 
    $mime = sql_parse_id($mime, true);
    $where .= " AND document_mimetype_id $mime ";
  }

  // User defined data
  $userdata = of_userdata_query_search('Document', $document);
  if ($userdata['where'] != '') {
    $where .= " AND ". $userdata['where'];
    $join_userdata = $userdata['join'];
  }
  
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "document_title";
  $orderq = "ORDER BY $order $sql_order_dir";
  
  $select = "SELECT
      document_id as id,
      document_id,
      document_title,
      document_name,
      document_author,
      document_size,
      $timecreate,
      $timeupdate,
      documentmimetype_label
    FROM Document
    INNER JOIN DocumentEntity ON documententity_document_id = document_id
      ";

  $join = "
      $join_userdata
      LEFT JOIN DocumentMimeType on document_mimetype_id = documentmimetype_id
      $join_ent";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Document $join $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    $query = "$select $join $whereq $orderq $limit";
    display_debug_msg($query, $cdg_sql, "run_query_document_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}

/**
 * Search document from a single field
 *
 * @param mixed $params
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_document_ext_search($params) {
  global $c_all, $cdg_sql, $obm, $ctu_sql_limit, $cdoc_kind_dir;
  
  $pattern = $params['pattern'];
  $limit = $params['limit'];
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  if (isset($params['first_row'])) $first_row = $params['first_row'] - 1;
  else $first_row = 0;
  $multidomain = sql_multidomain("document");
  
  $where = "(document_title $like '$pattern%' OR document_name $like '$pattern%')";
  $where.= ' AND '.sql_obm_entity_privacy("document");
  $where.= " AND document_kind != $cdoc_kind_dir";
  
  $query = "SELECT
    count(*) as resultscount
  FROM Document
  WHERE
    $where
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_document_ext_search()');
  $obm_q->query($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f('resultscount');
  
  $query = "SELECT
    document_id as id,
    document_id,
    document_title,
    document_name
  FROM Document
  WHERE
    $where
    $multidomain
  ORDER BY document_title
  LIMIT $limit OFFSET $first_row";

  display_debug_msg($query, $cdg_sql, 'run_query_document_ext_search()');
  $obm_q->query($query);

  return array('length' => $resultsCount, 'datas' => $obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_document_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timecreate = sql_date_format($db_type, "document_timecreate", "timecreate");
  $timeupdate = sql_date_format($db_type, "document_timeupdate", "timeupdate");
  $multidomain = sql_multidomain("document");
  $p_id = sql_parse_id($p_id, true);	
  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Document
      LEFT JOIN DocumentMimeType on document_mimetype_id = documentmimetype_id
      LEFT JOIN UserObm as c on document_usercreate=c.userobm_id
      LEFT JOIN UserObm as u on document_userupdate=u.userobm_id
    WHERE document_id $p_id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution²
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_document_insert(&$document) {
  global $cdg_sql, $c_auto, $obm;
  global $err, $l_create_file_error;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  $title = $document["title"];
  $author = $document["author"];
  $mime = $document["mime"];
  $name = $document["name"];
  $path = $document["path"];
  $size = $document["size"];
  $kind = sql_parse_int($document["kind"]);
  $url = $document["url"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');
  $entity = $document["entity"];
  
  if ($privacy != 0) {
    check_document_user_quota($size, $uid);
  }

  if ($size == "") $size = 0;
  
  // If Mime type set to Auto, try to determine it
  if ($mime == $c_auto) {
    $mime = get_document_auto_mime_type($document);
    $document["mime"] = $mime;
  }
  $mime = sql_parse_id($mime);

  // If document is a link, set the name as the url
  if ($kind == $cdoc_kind_link) {
    $name = $url;
    $document["name"] = $name;
  }
  $query = "INSERT INTO Document (
    document_timeupdate,
    document_timecreate,
    document_userupdate,
    document_usercreate,
    document_domain_id,
    document_title,
    document_author,
    document_name,
    document_path,
    document_kind,
    document_size,
    document_mimetype_id,
    document_privacy
  ) VALUES (
    null,
    NOW(),
    null,
    $uid,
    $domain_id,
    '".$obm_q->escape($title)."',
    '".$obm_q->escape($author)."',
    '".$obm_q->escape($name)."',
    '".$obm_q->escape($path)."',
    $kind,
    '".$obm_q->escape($size)."',
    $mime,
    $privacy
  )";

  display_debug_msg($query, $cdg_sql, "run_query_document_insert(1)");
  $retour = $obm_q->query($query);

  $doc_id = $obm_q->lastid();
  $entity_id = of_entity_insert('document', $doc_id);  
  $document["document_id"] = $doc_id;

  // If the document is correctly inserted in db
  if ($doc_id > 0) {

    $ret = of_userdata_query_update("document", $doc_id, $document);

    // We create the document on disk
    if ($kind == $cdoc_kind_file) {
      if (! run_query_document_create_file($document)) {
        $err["msg"] = $l_create_file_error;
        // If error, we delete The document infos to keep bd and rep on sync
        run_query_document_delete($doc_id);
        return false;
      }
    }

    // We insert links if entity given
    if ($entity != "") {
      $link_id = sql_parse_id($document['entity_id']);
      $link_entity_id = of_entity_get($entity, $link_id);
      $query = "INSERT INTO DocumentLink (
        documentlink_entity_id,
        documentlink_document_id
      ) VALUES (
        $link_entity_id,
        $doc_id
      )";
      display_debug_msg($query, $cdg_sql, "run_query_document_insert(links)");
      $obm_q->query($query);
    }
  }

  return $doc_id;
}


///////////////////////////////////////////////////////////////////////////////
// Document ID : Get the id of the document inserted.
// Parameters:
//   - $document[] : document hash info
// Returns:
//   document Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_document_id($document) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $title = $document["title"];
  $author = $document["author"];
  $name = $document["name"];
  $path = $document["path"];
  $size = $document["size"];
  $multidomain = sql_multidomain("document");
	
  $query = "SELECT MAX(document_id) as document_id
    FROM Document
    WHERE 
      document_title='".$obm_q->escape($title)."'
      AND document_author='".$obm_q->escape($author)."'
      AND document_name='".$obm_q->escape($name)."'
      AND document_path='".$obm_q->escape($path)."'
      AND document_size='".$obm_q->escape($size)."'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_id()");
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f("document_id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $document[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_document_update(&$document) {
  global $obm, $cdg_sql, $c_auto;
  global $err, $l_delete_file_error, $l_create_file_error;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = $obm["uid"];

  $id = sql_parse_id($document["document_id"], true);
  $title = $document["title"];
  $author = $document["author"];
  $mime = $document["mime"];
  $path = $document["path"];
  $size = $document["size"];
  $name = $document["name"];
  $kind = sql_parse_int($document["kind"]);
  $url = $document["url"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');
  $file_update = $document["file_update"];
  $multidomain = sql_multidomain("document");
	
  if ($size == "") { $size = 0; }
  
  if (($mime == $c_auto) && ($file_update == "yes")) {
    $mime = get_document_auto_mime_type($document);
    $document["mime"] = $mime;
  }
  $mime = sql_parse_id($mime); 
  $mime = sql_parse_id($mime);
  if ($kind == $cdoc_kind_link) {
    $name = $url;
    $document["name"] = $name;
  }

  if ($file_update == "yes") {
    $query_file = ",
    document_path='".$obm_q->escape($path)."',
    document_size='".$obm_q->escape($size)."',
    document_name='".$obm_q->escape($name)."',
    document_mimetype_id=$mime";
  }
  
  $query = "UPDATE Document SET
    document_timeupdate=NOW(),
    document_userupdate=$uid,
    document_title='".$obm_q->escape($title)."',
    document_kind=$kind,
    document_author='".$obm_q->escape($author)."',
    document_privacy='".$obm_q->escape($privacy)."'
    $query_file
  WHERE document_id $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_update()");
  $retour = $obm_q->query($query);

  if (($kind == $cdoc_kind_file) && ($file_update == "yes")) {

    if (! run_query_document_delete_file($document["document_id"])) { 
      $err["msg"] = $l_delete_file_error;
      return false;
    }
    if (! run_query_document_create_file($document)) {
      $err["msg"] = $l_create_file_error;
      return false;
    }
  }

  if ($retour) {
    $retour = of_userdata_query_update("document", $document["document_id"], $document);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_document_delete($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("document");

  // Delete all links to documents
  run_query_global_delete_document_links("", "", $p_id);

  // Delete the file
  run_query_document_delete_file($p_id);
  of_entity_delete($GLOBALS['module'], $p_id);

  // Delete User data
  $ret = of_userdata_query_delete("document", $p_id);
  $p_id = sql_parse_id($p_id, true);
  // Delete the database entry
  $query = "DELETE FROM Document 
		WHERE document_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk document path for a document
// Parameters:
//   - $id   : document id
// Returns:
//   real disk path
///////////////////////////////////////////////////////////////////////////////
function get_document_disk_path($id) {
  global $cdocument_root;

  // The document disk path set in the subdirectory named "last id number"
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = $cdocument_root . "/" . $rel . "/";
  
  return $disk_path; 
}


///////////////////////////////////////////////////////////////////////////////
// Create File query execution
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_document_create_file($document) {

  $id = $document["document_id"];
  $file = $document["file_tmp"];

  $disk_path = get_document_disk_path($id);
  $disk_fullname = $disk_path . $id; 

  // Ensure that the final path is a directory
  if (! is_dir($disk_path)) {
    return false;
  }

  if (is_writable($disk_path)) {
    if (!move_uploaded_file($file, $disk_fullname)) {
      return false;
    }
  } else {
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Delete File from Disk
// Parameters:
//   - $id : id of file to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_document_delete_file($id) {

  $disk_path = get_document_disk_path($id);
  $disk_fullname = $disk_path . $id; 

  @rmdir($disk_fullname);
  @unlink($disk_fullname);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of an uploaded file from file infos
// Parameters:
//   - $document[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function get_document_auto_mime_type($document) {
  global $default_mime;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  $name = $document["name"];
  $kind = $document["kind"];

  // If doc is a link
  if ($kind == $cdoc_kind_link) {

    // If name ended by / we assume mime type is html
    if (strrpos($name,'/') == (strlen($name) -1)) {
      $html_mime = run_query_document_get_type_by_ext("html");
      if ($html_mime) {
	return $html_mime;
      }

      // If named not ended by / we try to resolve the mime from extension
    } else {
      $ext = strtolower(substr(strrchr($name,'.'), 1));
      $guess_mime = run_query_document_get_type_by_ext($ext);
      if ($guess_mime) {
	return $guess_mime;
      }
    }
 
  // If document is a file, try to get the mime type from its file extension
  } elseif ($kind == $cdoc_kind_file) {
  
    $ext = strtolower(substr(strrchr($name,'.'), 1));
    $guess_mime = run_query_document_get_type_by_ext($ext);
    if ($guess_mime) {
      return $guess_mime;
    }
  }

  // If mime not guessed, return default mime
  $return = run_query_document_get_type_by_mime($default_mime);

  return $return ;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $type : Mime-Type
///////////////////////////////////////////////////////////////////////////////
function run_query_document_get_type_by_mime($type) {
  global $cdg_sql;

  $multidomain = sql_multidomain("documentmimetype");
  $type = sql_parse_id($type, true);
  $query = "SELECT *
    FROM DocumentMimeType
    WHERE documentmimetype_mime $type 
      $multidomain";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_document_get_type_by_mime()");
  if ($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_id");
  } else { 
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $ext : Extension 
///////////////////////////////////////////////////////////////////////////////
function run_query_document_get_type_by_ext($ext) {
  global $cdg_sql;

  $multidomain = sql_multidomain("documentmimetype");

  $query = "SELECT *
    FROM DocumentMimeType
    WHERE documentmimetype_extension ='$ext'
      $multidomain";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_document_get_type_by_ext()");
  if ($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_id");
  } else { 
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of entities linked to a document
// Parameters:
//   - $document_id
///////////////////////////////////////////////////////////////////////////////
function get_document_link_nb($document_id) {
  global $cdg_sql;

  $document_id = sql_parse_id($document_id, true);
  $query = "SELECT count(*) as nb
    FROM DocumentLink
    WHERE documentlink_document_id $document_id ";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "get_document_link_nb()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("nb");
}

///////////////////////////////////////////////////////////////////////////////
// Get events linked to a document
// Parameters:
//   - $document_id
///////////////////////////////////////////////////////////////////////////////
function get_document_linked_events($document_id) {
  global $cdg_sql;

  $document_id = sql_parse_id($document_id, true);
  $query = "SELECT event_id, event_title
    FROM Event 
    INNER JOIN EventEntity ON evententity_event_id = event_id 
    INNER JOIN DocumentLink ON evententity_entity_id = documentlink_entity_id
    WHERE documentlink_document_id $document_id ";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "get_document_linked_events()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the document number in a directory
// Parameters:
//   - $dpath : directory full name (path+name)
//   - $kind  : Limit the search to document kind
///////////////////////////////////////////////////////////////////////////////
function get_dir_document_nb($dpath, $kind) {
  global $cdg_sql, $cdoc_kind_dir, $c_all;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("document");
  if ($kind != $c_all) {
    $kind = sql_parse_int($kind);
    $where_kind = "AND document_kind=$kind ";
  } 

  $query = "SELECT count(document_id) as nb
    FROM Document 
    WHERE document_path = '".$obm_q->escape($dpath)."'
      $where_kind
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_dir_document_nb()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Document Mime Types query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime() {
  global $cdg_sql;

  $multidomain = sql_multidomain("documentmimetype");

  $query = "SELECT * 
    FROM DocumentMimeType
    WHERE 1=1
      $multidomain
    ORDER BY documentmimetype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_document_mime()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a path exists in DB (a directory)
// Parameters:
//   - $dpath : path to check
// Returns:
//   true if a directory for that path exists, else false
///////////////////////////////////////////////////////////////////////////////
function document_path_exists($dpath) {
  global $cdg_sql, $cdoc_kind_dir;
  // Root is a valid path
  if ($dpath == "/") {
    return true;
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $cpath[] = array("type" => "field", "value" => "document_path");
  $cpath[] = array("type" => "field", "value" => "document_name");
  $cpath[] = array("type" => "string", "value" => "/");
  $concat_path = sql_string_concat($db_type, $cpath);

  $multidomain = sql_multidomain("document");

  $query = "SELECT count(document_id) as nb
    FROM Document
    WHERE document_kind = $cdoc_kind_dir
      AND $concat_path = '".$obm_q->escape($dpath)."'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "document_path_exists()");
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("nb");

  if ($nb > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Document Form Data checking and formatting
// Parameters:
//   - $cid   : document id
//   - $document[] : values checked
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_document_data_form($cid, $document) {
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  global $l_fill_title, $l_fill_author, $l_fill_path;
  global $l_invalid_file, $l_invalid_existing_file, $l_invalid_url;
  global $err, $l_invalid_link_file, $l_invalid_dir_path, $l_dir_error;

  $title = $document["title"];
  $author = $document["author"];
  $path = $document["path"];
  $name = $document["name"];
  $size = $document["size"];
  $kind = $document["kind"];
  $url = $document["url"];
  $file_update = $document["file_update"];

  // MANDATORY: Document Title 
  if (trim($title) == "") {
    $err["msg"] = $l_fill_title;
    return false;
  }

  // MANDATORY: Document Author 
  if (trim($author) == "") {
    $err["msg"] = $l_fill_author;
    return false;
  }

  // If doc is a file, check that name and size are not empty
  if (($kind == $cdoc_kind_file) && $file_update != "no" && ($name == "" || $size == "")) {
    $err["msg"] = $l_invalid_file;
    return false;
  }
  
  // If doc is a file, check the path exists
  if ( ($kind == $cdoc_kind_file) && $file_update != "no"
       && (! document_path_exists($path)) ) {
    $err["msg"] = "$l_invalid_dir_path ($path)";
    return false;
  }

  // If document is a link, check that the url is filled
  if (($kind == $cdoc_kind_link) && ($url == "") && ($file_update != "no")) {
    $err["msg"] = $l_invalid_url;
    return false;
  } 

  // If document is a directory, check that name and path are ok
  if (($kind == $cdoc_kind_dir) && ($name == "") && ($file_update != "no")
       && (! document_path_exists($path)) ) {
    $err["msg"] = $l_invalid_dir;
    return false;
  } 

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Mime Type insertion query construction and execution
// Parameters:
//   - $document : document hash info
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime_insert($document) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = sql_parse_id($obm["uid"]);
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  if($mime == "" || $extension == "" || $mimetype == "") {
    return false;
  }
  $query = "INSERT INTO DocumentMimeType (
    documentmimetype_domain_id,
    documentmimetype_timecreate,
    documentmimetype_usercreate,
    documentmimetype_label,
    documentmimetype_extension,
    documentmimetype_mime)
  VALUES (
    $domain_id,
    '$timecreate',
    $usercreate,
    '".$obm_q->escape($mime)."',
    '".$obm_q->escape($extension)."',
    '".$obm_q->escape($mimetype)."')";

  display_debug_msg($query, $cdg_sql, "run_query_document_mime_insert()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// MimeType update query execution
// Parameters:
//   - $document : document hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime_update($document) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = sql_parse_id($obm["uid"]);
  $id = sql_parse_id($document["mime"], true);
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];
  $multidomain = sql_multidomain("documentmimetype");
	
  $query = "UPDATE DocumentMimeType SET
      documentmimetype_label='".$obm_q->escape($mime)."',
      documentmimetype_extension='".$obm_q->escape($extension)."',
      documentmimetype_mime='".$obm_q->escape($mimetype)."',
      documentmimetype_timeupdate='$timeupdate',
      documentmimetype_userupdate=$userupdate
    WHERE
      documentmimetype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_mime_update()");
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a mime type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_document_mime_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("documentmimetype");
  $id = sql_parse_id($id, true);
  $query = "SELECT documentmimetype_label
    FROM DocumentMimeType
    WHERE documentmimetype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_document_mime_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("documentmimetype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Document links query execution
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime_links($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("document");
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT document_title, document_id, document_mimetype_id
    FROM Document
    WHERE document_mimetype_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_mime_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Mime Type deletion query execution
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("documentmimetype");
  $id = sql_parse_id($id, true);
  $query = "DELETE FROM DocumentMimeType 
		WHERE documentmimetype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_mime_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Dir Creation
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_document_insert_dir($document) {
  global $cdg_sql, $cdoc_kind_dir, $obm;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  $path = $document["path"];
  $name = $document["name"];

  $query = "INSERT INTO Document (
    document_timeupdate,
    document_timecreate,
    document_userupdate,
    document_usercreate,
    document_domain_id,
    document_title,
    document_author,
    document_name,
    document_path,
    document_kind,
    document_size,
    document_mimetype_id,
    document_privacy
  ) VALUES (null,
    NOW(),
    null,
    $uid,
    $domain_id,
    '',
    '',
    '".$obm_q->escape($name)."',
    '".$obm_q->escape($path)."',
    $cdoc_kind_dir,
    0,
    NULL,
    0)";

  display_debug_msg($query, $cdg_sql, "run_query_document_insert_dir()");
  $obm_q->query($query);
  $id = $obm_q->lastid();
  of_entity_insert('document', $id);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Dir Creation
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_document_update_dir($document) {
  global $cdg_sql, $obm, $cdoc_kind_dir;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $path = $document["path"].$document["name"];
  $old_path = $document["path"].$document["old_name"];
  $name = $document["name"];
  $id = sql_parse_id($document["document_id"], true);

  $multidomain = sql_multidomain("document");

  $query = "UPDATE Document SET
      document_timeupdate = NOW(),
      document_userupdate = $uid,
      document_name = '".$obm_q->escape($name)."'
    WHERE 
      document_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_document_update_dir(1)");
  $obm_q->query($query);
  $query = "SELECT *
    FROM Document 
    WHERE document_path like '".$obm_q->escape($old_path)."/%'
      $multidomain" ;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_document_update_dir(2)");
  $obm2_q = new DB_OBM;
  $size = strlen($old_path);
  while($obm_q->next_record()) {
    $new_path = $path.substr($obm_q->f("document_path"),$size);
    $d_id = sql_parse_id($obm_q->f("document_id"), true);
    $query = "UPDATE Document SET
        document_timeupdate = document_timeupdate,
        document_path = '".$obm_q->escape($new_path)."'
      WHERE
        document_id $d_id 
        $multidomain";

    display_debug_msg($query, $cdg_sql, "run_query_document_update_dir(3)");
    $obm2_q->query($query);
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Document Form Data checking and formatting
// Parameters:
//   - $document[] : values checked
//     keys used  : num, name, zip, phone, fax, web
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_document_dir_data_form($document) {
  global $err, $l_invalid_dir_path, $l_fill_dir_name;

  $name = $document["name"];
  $path = $document["path"];

  // MANDATORY: Document Title 
  if (trim($name) == "") {
    $err["msg"] = $l_fill_dir_name;
    return false;
  }

  // If doc is a file, check the path exists
  if (! document_path_exists($path)) {
    $err["msg"] = "$l_invalid_dir_path ($path)";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check the document can be deleted
// Parameters:
//   - $p_id : document id
// Returns:
//   true if document can be deleted (for now always), else false
///////////////////////////////////////////////////////////////////////////////
function check_document_can_delete($p_id) {
  global $err, $l_have_link, $l_have_no_link;

  $delete_ok = true;

  // Links to entities
  $nb = get_document_link_nb($p_id);

  // Warn if document has links
  if ($nb > 0) {
    $err["msg"] = $l_have_link;
    $delete_ok = false;
  } else {
    $err["msg"] = $l_have_no_link;
  }
 
  return $delete_ok;
}

///////////////////////////////////////////////////////////////////////////////
// Check user quota when uploading a new private document
// Parameters:
//   - $size : size of the newly uploaded document
//   - $u_id : user ID
// Throws an exception if the user is over quota
///////////////////////////////////////////////////////////////////////////////
function check_document_user_quota($size, $u_id) {
  global $c_quota_private_document;
  
  if ($c_quota_private_document == 0) return;
  
  $user_quota = run_query_document_user_quota($u_id);
  if ($user_quota + $size > $c_quota_private_document) {
    throw new OverQuotaDocumentException();
  }
}

///////////////////////////////////////////////////////////////////////////////
// Retrieve user quota
///////////////////////////////////////////////////////////////////////////////
function run_query_document_user_quota($u_id) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $query = "SELECT SUM(document_size) AS sum_size FROM Document WHERE 
  document_usercreate = '".$obm_q->escape($u_id)."' AND 
  document_privacy = 1";
  display_debug_msg($query, $cdg_sql, 'run_query_document_user_quota()');
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f('sum_size');
}

///////////////////////////////////////////////////////////////////////////////
// Check if a directory can be deleted
// Parameters:
//   - $id : document id
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_document_can_delete_dir($id) {
  global $cdoc_kind_dir, $c_all, $default_path;
  global $err, $l_err_reference, $l_err_system_path, $l_err_dir_no_empty;

  $delete_ok = true;

  $obm_q = run_query_document_detail($id);
  $db_id = $obm_q->f("document_id");
  $kind = $obm_q->f("document_kind");
  $dpath = $obm_q->f("document_path");
  $name = $obm_q->f("document_name");
  $fullname = $dpath . $name . "/";

  // Forbid to delete Root or path defined as default
  if (($fullname == "/") || ($fullname == $default_path)) {
    $err["msg"] .= $l_err_system_path;
    return false;
  }

  // Check that document really exists !
  if ($db_id != $id) {
    $err["msg"] .= $l_err_reference;
    $delete_ok = false;
    return false;
  }

  // Check that document is really a directory
  if ($kind != $cdoc_kind_dir) {
    $err["msg"] .= $l_cant_delete;
    $delete_ok = false;
    return false;
  }

  // Check that directory is empty
  $nb = get_dir_document_nb($fullname, $c_all);
  if ($nb > 0) {
    $err["msg"] .= $l_err_dir_no_empty;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Query to build the file tree
// Parameters:
//   - $display_file : bolean
///////////////////////////////////////////////////////////////////////////////
function run_query_document_get_tree($display_file) {
  global $cdg_sql, $cdoc_kind_dir;

  $where .= sql_obm_entity_privacy("document");
  $multidomain = sql_multidomain("document");
	
  if ($display_file != "true") {
    $where .= " AND document_kind = $cdoc_kind_dir ";
  }
  $query = "SELECT *
    FROM Document
    WHERE $where
      $multidomain
    ORDER BY document_name";

  display_debug_msg($query, $cdg_sql, "run_query_document_get_tree()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;  
}

?>
