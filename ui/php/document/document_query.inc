<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : document_query.inc                                           //
//     - Desc : document query File                                          //
// 2003-08-21 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Document search query execution
// Parametes:
//   - $document[]    : document search criteria
//     keys used       archive, name, phone, kind, zip
///////////////////////////////////////////////////////////////////////////////
function run_query_document_search($document) {
  global $c_all, $cdg_sql, $auth, $ctu_sql_limit, $cdoc_kind_dir;

  $uid = $auth->auth["uid"];

  $sql_order_dir = $document["sql_order_dir"];
  $sql_order_field = $document["sql_order_field"];

  $title = sql_search_text_parse($document["title"]);
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = sql_search_text_parse($document["author"]);
  $mime = $document["mime"];
  $name = sql_search_text_parse($document["name"]);
  $entity = $document["entity"];
  $entity_id = $document["entity_id"];  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timecreate = sql_date_format($db_type, "document_timecreate", "timecreate");
  $timeupdate = sql_date_format($db_type, "document_timeupdate", "timeupdate");
	$multidomain = sql_multidomain("document");
	
  $where .= sql_obm_entity_privacy("document");
  $where .= " and document_kind != '$cdoc_kind_dir'";

  if ($entity != "") {
    $join_ent = "LEFT JOIN DocumentEntity on document_id = documententity_document_id";
    $where .= " AND documententity_entity = '$entity'
                AND documententity_entity_id = '$entity_id'";
  }

  if ($title != "") {
    $where .= " and document_title $like '%$title%'";
  }
  
  if ($name != "") {
    $where .= " and document_name $like '%$name%'";
  }

  $join_category1 = "LEFT JOIN DocumentCategory1 ON document_category1_id=documentcategory1_id";
  if (($cat1 != $c_all) && ($cat1 != "")) { 
    $where .= " and document_category1_id = '$cat1'";
  }
  
  $join_category2 = "LEFT JOIN DocumentCategory2 ON document_category2_id=documentcategory2_id";
  if (($cat2 != $c_all) && ($cat2 != "")) { 
    $where .= " and document_category2_id = '$cat2'";
  }
  
  if (($author != $c_all) && ($author != "")) { 
    $where .= " and document_author $like '%$author%'";
  }
  
  if (($mime != $c_all) && ($mime != "")) { 
    $where .= " and document_mimetype_id = '$mime'";
  }
  
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "document_title";
  $orderq = "ORDER BY $order $sql_order_dir";
  
  $req = "SELECT
      document_id as id,
      document_id,
      document_title,
      document_name,
      document_author,
      document_size,
      $timecreate,
      $timeupdate,
      documentcategory1_label,
      documentcategory2_label,
      documentmimetype_label
    FROM Document
      $join_category1
      $join_category2
      LEFT JOIN DocumentMimeType on document_mimetype_id = documentmimetype_id
      $join_ent
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Document $join_ent $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($req, $cdg_sql, "run_query_document_search()");
    $obm_q->query($req);
  }

  return $obm_q;
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
	
  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Document
      LEFT JOIN DocumentCategory1 on document_category1_id=documentcategory1_id
      LEFT JOIN DocumentCategory2 on document_category2_id=documentcategory2_id
      LEFT JOIN DocumentMimeType on document_mimetype_id = documentmimetype_id
      LEFT JOIN UserObm as c on document_usercreate=c.userobm_id
      LEFT JOIN UserObm as u on document_userupdate=u.userobm_id
    WHERE document_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_document_insert(&$document) {
  global $auth, $cdg_sql, $c_auto, $obm;
  global $err_msg, $l_create_file_error;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");
	$domain_id = $obm["domain_id"];
	
  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $name = $document["name"];
  $path = $document["path"];
  $size = $document["size"];
  $kind = $document["kind"];
  $url = $document["url"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');
  $entity = $document["entity"];
  $entity_id = $document["entity_id"];

  if ($size == "") $size = 0;
  
  // If Mime type set to Auto, try to determine it
  if ($mime == $c_auto) {
    $mime = get_document_auto_mime_type($document);
    $document["mime"] = $mime;
  }

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
    document_category1_id,
    document_category2_id,
    document_mimetype_id,
    document_privacy
  ) VALUES (
    null,
    '$now',
    null,
    '$uid',
		'$domain_id',
    '$title',
    '$author',
    '$name',
    '$path',
    '$kind',
    '$size',
    '$cat1',
    '$cat2',
    '$mime',
    '$privacy'
  )";

  display_debug_msg($query, $cdg_sql, "run_query_document_insert(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $doc_id = run_query_document_id($document);
  $document["document_id"] = $doc_id;

  // If the document is correctly inserted in db
  if ($doc_id > 0) {

    // We create the document on disk
    if ($kind == $cdoc_kind_file) {
      if (! run_query_document_create_file($document)) {
	$err_msg = $l_create_file_error;
	// If error, we delete The document infos to keep bd and rep on sync
	run_query_document_delete($doc_id);
	return false;
      }
    }

    // We insert links if entity given
    if ($entity != "") {
      $query = "INSERT INTO DocumentEntity (
        documententity_entity,
        documententity_entity_id,
				documententity_domain_id,
        documententity_document_id
      ) VALUES (
        '$entity',
        '$entity_id',
				'$domain_id',
        '$doc_id'
      )";
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

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $name = $document["name"];
  $path = $document["path"];
  $size = $document["size"];
	$multidomain = sql_multidomain("document");
	
  $query = "SELECT document_id
    FROM Document
    WHERE 
      document_title='$title'
      AND document_category1_id='$cat1'
      AND document_category2_id='$cat2'
      AND document_author='$author'
      AND document_name='$name'
      AND document_path='$path'
      AND document_size='$size'
			$multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
//   - $did        : document id
//   - $document[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_document_update($did, &$document) {
  global $auth, $cdg_sql, $c_auto;
  global $err_msg, $l_delete_file_error, $l_create_file_error;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");

  $id = $document["document_id"];
  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $path = $document["path"];
  $size = $document["size"];
  $name = $document["name"];
  $kind = $document["kind"];
  $url = $document["url"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');
  $file_update = $document["file_update"];
	$multidomain = sql_multidomain("document");
	
  if ($size == "") { $size = 0; }
  
  if (($mime == $c_auto) && ($file_update == "yes")) {
    $mime = get_document_auto_mime_type($document);
    $document["mime"] = $mime;
  }

  if ($kind == $cdoc_kind_link) {
    $name = $url;
    $document["name"] = $name;
  }

  if ($file_update == "yes") {
    $query_file = ",
    document_path='$path',
    document_size='$size',
    document_name='$name',
    document_mimetype_id='$mime'";
  }
  
  $query = "UPDATE Document SET
    document_timeupdate='$now',
    document_userupdate='$uid',
    document_title='$title',
    document_kind='$kind',
    document_author='$author',
    document_category1_id='$cat1',
    document_category2_id='$cat2',
    document_privacy='$privacy'
    $query_file
  WHERE document_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if (($kind == $cdoc_kind_file) && ($file_update == "yes")) {

    if (! run_query_document_delete_file($id)) { 
      $err_msg = $l_delete_file_error;
      return false;
    }
    if (! run_query_document_create_file($document)) {
      $err_msg = $l_create_file_error;
      return false;
    }
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

  // Delete the database entry
  $query = "DELETE FROM Document 
		WHERE document_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT *
    FROM DocumentMimeType
    WHERE documentmimetype_mime ='$type' $multidomain";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
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
    WHERE documentmimetype_extension ='$ext' $multidomain";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
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
//   - $type : Mime-Type
///////////////////////////////////////////////////////////////////////////////
function get_document_link_nb($document_id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("documententity");

  $query = "SELECT count(*) as nb
    FROM DocumentEntity
    WHERE documententity_document_id = '$document_id' 
		$multidomain";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Get the document number in a directory
// Parameters:
//   - $dpath : directory full name (path+name)
//   - $kind  : Limit the search to document kind
///////////////////////////////////////////////////////////////////////////////
function get_dir_document_nb($dpath, $kind) {
  global $cdg_sql, $cdoc_kind_dir, $c_all;

	$multidomain = sql_multidomain("document");

  if ($kind != $c_all) {
    $where_kind = "AND document_kind = '$kind'";
  } 

  $query = "SELECT count(document_id) as nb
    FROM Document 
    WHERE document_path = '$dpath'
      $where_kind $multidomain";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
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
		WHERE 1=1 $multidomain
		ORDER BY documentmimetype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

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
  $ctt[] = array("type" => "field", "value" => "document_path");
  $ctt[] = array("type" => "field", "value" => "document_name");
  $ctt[] = array("type" => "string", "value" => "/");
  $concat = sql_string_concat($db_type, $ctt);

	$multidomain = sql_multidomain("document");

  $query = "SELECT count(document_id) as nb
    FROM Document
    WHERE document_kind = '$cdoc_kind_dir'
      AND $concat = '$dpath' $multidomain"; 

  display_debug_msg($query, $cdg_sql, "path_exists");
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
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_document_data_form($cid, $document) {
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  global $l_fill_title, $l_fill_author, $l_fill_path;
  global $l_invalid_file, $l_invalid_existing_file, $l_invalid_url;
  global $err_msg, $l_invalid_link_file, $l_invalid_dir_path, $l_dir_error;

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $path = $document["path"];
  $name = $document["name"];
  $size = $document["size"];
  $kind = $document["kind"];
  $url = $document["url"];
  $file_update = $document["file_update"];

  // MANDATORY: Document Title 
  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }

  // MANDATORY: Document Author 
  if (trim($author) == "") {
    $err_msg = $l_fill_author;
    return false;
  }

  // If doc is a file, check that name and size are not empty
  if (($kind == $cdoc_kind_file) && $file_update != "no" && ($name == "" || $size == "")) {
    $err_msg = $l_invalid_file;
    return false;
  }
  
  // If doc is a file, check the path exists
  if ( ($kind == $cdoc_kind_file) && $file_update != "no"
       && (! document_path_exists($path)) ) {
    $err_msg = "$l_invalid_dir_path ($path)";
    return false;
  }

  // If document is a link, check that the url is filled
  if (($kind == $cdoc_kind_link) && ($url == "") && ($file_update != "no")) {
    $err_msg = $l_invalid_url;
    return false;
  } 

  // If document is a directory, check that name and path are ok
  if (($kind == $cdoc_kind_dir) && ($name == "") && ($file_update != "no")
       && (! document_path_exists($path)) ) {
    $err_msg = $l_invalid_dir;
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
  global $auth, $cdg_sql, $obm;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];
	$domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO DocumentMimeType (
    documentmimetype_timecreate,
    documentmimetype_usercreate,
    documentmimetype_label,
    documentmimetype_extension,
		documentmimetype_domain_id,
    documentmimetype_mime)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$mime',
    '$extension',
		'$domain_id',
    '$mimetype'
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// MimeType update query execution
// Parameters:
//   - $document : document hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_document_mime_update($document) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $document["mime"];
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];
	$multidomain = sql_multidomain("documentmimetype");
	
  $query = "UPDATE DocumentMimeType SET
      documentmimetype_label='$mime',
      documentmimetype_extension='$extension',
      documentmimetype_mime='$mimetype',
      documentmimetype_timeupdate='$timeupdate',
      documentmimetype_userupdate='$userupdate'
    WHERE
      documentmimetype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_document_mime_label($id) {
  global $cdg_sql;

	$multidomain = sql_multidomain("documentmimetype");

  $query = "SELECT documentmimetype_label
    FROM DocumentMimeType
    WHERE documentmimetype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT document_title, document_id, document_mimetype_id
    FROM Document
    WHERE document_mimetype_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
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

  $query = "DELETE FROM DocumentMimeType 
		WHERE documentmimetype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql);
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
  global $cdg_sql, $auth, $cdoc_kind_dir, $obm;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");
	$domain_id = $obm["domain_id"];
	
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
    document_category1_id,
    document_category2_id,
    document_mimetype_id,
    document_privacy
  ) VALUES (null,
    '$now',
    null,
    '$uid',
		'$domain_id',
    '',
    '',
    '$name',
    '$path',
    '$cdoc_kind_dir',
    '0',
    '0',
    '0',
    '0',
    '0'
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Dir Creation
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_document_update_dir($document) {
  global $cdg_sql, $auth, $cdoc_kind_dir;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");
  $path = $document["path"].$document["name"];
  $old_path = $document["path"].$document["old_name"];
  $name = $document["name"];
  $id = $document["document_id"];

	$multidomain = sql_multidomain("document");

  $query = "UPDATE Document SET
    document_timeupdate = NOW(),
    document_userupdate = '$uid',
    document_name = '$name'
    WHERE 
    document_id = '$id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $query = "SELECT * FROM Document 
		WHERE document_path  like '$old_path/%' $multidomain" ;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm2_q = new DB_OBM;
  $size = strlen($old_path);
  while($obm_q->next_record()) {
    $new_path = $path.substr($obm_q->f("document_path"),$size);
    $query = "UPDATE Document SET
      document_timeupdate = document_timeupdate,
      document_path = '$new_path'
      WHERE 
      document_id = '".$obm_q->f("document_id")."' $multidomain";

    display_debug_msg($query, $cdg_sql);
    $obm2_q->query($query);
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Document Form Data checking and formatting
// Parameters:
//   - $document[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_document_dir_data_form($document) {
  global $err_msg, $l_invalid_dir_path, $l_fill_dir_name;

  $name = $document["name"];
  $path = $document["path"];

  // MANDATORY: Document Title 
  if (trim($name) == "") {
    $err_msg = $l_fill_dir_name;
    return false;
  }

  // If doc is a file, check the path exists
  if (! document_path_exists($path)) {
    $err_msg = "$l_invalid_dir_path ($path)";
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
  global $err_msg, $l_have_link, $l_have_no_link;

  $delete_ok = true;

  // Links to entities
  $nb = get_document_link_nb($p_id);

  // Warn if document has links
  if ($nb > 0) {
    $err_msg = $l_have_link;
  } else {
    $err_msg = $l_have_no_link;
  }
 
  return $delete_ok;
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
  global $err_msg, $l_err_reference, $l_err_system_path, $l_err_dir_no_empty;

  $delete_ok = true;

  $obm_q = run_query_document_detail($id);
  $db_id = $obm_q->f("document_id");
  $kind = $obm_q->f("document_kind");
  $dpath = $obm_q->f("document_path");
  $name = $obm_q->f("document_name");
  $fullname = $dpath . $name . "/";

  // Forbid to delete Root or path defined as default
  if (($fullname == "/") || ($fullname == $default_path)) {
    $err_msg .= $l_err_system_path;
    return false;
  }

  // Check that document really exists !
  if ($db_id != $id) {
    $err_msg .= $l_err_reference;
    $delete_ok = false;
    return false;
  }

  // Check that document is really a directory
  if ($kind != $cdoc_kind_dir) {
    $err_msg .= $l_cant_delete;
    $delete_ok = false;
    return false;
  }

  // Check that directory is empty
  $nb = get_dir_document_nb($fullname, $c_all);
  if ($nb > 0) {
    $err_msg .= $l_err_dir_no_empty;
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
  global $cdg_sql, $auth, $cdoc_kind_dir;

  $where .= sql_obm_entity_privacy("document");
	$multidomain = sql_multidomain("document");
	
  if ($display_file != "true") {
    $where .= " AND document_kind = '$cdoc_kind_dir' ";
  }
  $query = "SELECT *
    FROM Document
    WHERE $where $multidomain
    ORDER BY document_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;  
}

?>
