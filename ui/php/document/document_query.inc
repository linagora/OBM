<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : document_query.inc                                            //
//     - Desc : document query File                                           //
//  2003-08-21 Mehdi Rande                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  Category 1 query execution                                              //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_documentcategory1() {
  global $cdg_sql;

  $query = "select * from DocumentCategory1 order by documentcategory1_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
//  Category 2 query execution                                              //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_documentcategory2() {
  global $cdg_sql;

  $query = "select * from DocumentCategory2 order by documentcategory2_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
//  Category 2 query execution                                              //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_documentmime() {
  global $cdg_sql;

  $query = "select * from DocumentMimeType order by documentmimetype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Document search query execution                                            //
// Parametes:
//   - $document[]    : document search criteria
//     keys used       archive, name, phone, kind, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($document, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql,$auth;  

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $name = $document["filename"];
  $entity =$document["entity"];
  $entity_id = $document["entity_id"];  


  if ($entity != "") {
    $leftjoin = "LEFT JOIN DocumentEntity on document_id = documententity_documentid";
    $where .= " and documententity_entity = '$entity' and documententity_entityid = '$entity_id' ";
  }
  
  $req = "select
      document_id as Id,
      document_id,
      document_title,
      document_name,
      document_author,
      document_size,
      UNIX_TIMESTAMP(document_timecreate) as timecreate,
      UNIX_TIMESTAMP(document_timeupdate) as timeupdate,
      documentcategory1_label,
      documentcategory2_label,
      documentmimetype_label
      from (((Document
         LEFT JOIN DocumentCategory1 on document_category1 = documentcategory1_id)
         LEFT JOIN DocumentCategory2 on document_category2 = documentcategory2_id)
	 LEFT JOIN DocumentMimeType on document_mimetype = documentmimetype_id)
	 $leftjoin";
      

  if ($title != "") {
    $where .= "and document_title like '%$title%'";
  }
  
  if ($name != "") {
    $where .= "and document_name like '%$name%'";
  }

  if (($cat1 != $c_all) && ($cat1 != "")) { 
    $where .= " and document_category1 = '$cat1'";
  }
  
  if (($cat2 != $c_all) && ($cat2 != "")) { 
    $where .= " and document_category2 = '$cat2'";
  }
  
  if (($author != $c_all) && ($author != "")) { 
    $where .= " and document_author like '%$author%'";
  }
  
  if (($mime != $c_all) && ($mime != "")) { 
    $where .= " and document_mimetype = '$mime'";
  }
  
  $where = " where document_kind != '0' AND 
  (document_private = '0' OR (document_private = '1' AND document_usercreate = ".$auth->auth["uid"].")) $where";

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "document_title";

  $req .= "$where order by $order $p_order_dir";

  display_debug_msg($req, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($req);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_insert(&$document) {
  global $auth, $cdg_sql,$c_auto,$document_path,$err_msg,$l_already_exist_error,$l_creation_error;

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
  $kind = $document["kind"];  
  $entity = $document["entity"];
  $entity_id = $document["entity_id"];
  if(strrpos($path,'/') != strlen($path) -1) {
    $path = $path.'/';
  }

  if(strpos($path,'/') === 0) {
    $final_path = $document_path.$path.$name;
  }
  else {
    $final_path = $document_path.'/'.$path.$name;
  }

  if($kind == "link") {
    $name = $url;
    $document["name"]=$name;
    $file_kind = 2;
  }
  elseif(file_exists($final_path)) {
    $err_msg = $l_already_exist_error;
    return false;
  }

  if($size == "") $size = 0;
  
  if($mime == $c_auto) {
    $mime = run_query_get_mime($document);
    $document["mime"] = $mime;
  }

  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');

  if($kind == "new_file") {
    $file_kind = 1;
    if(!run_query_create_file($document)){
      $err_msg = $l_creation_error;
      return false;
    }
  }

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
    " . $auth->auth["uid"] . ",
    '$title',
    '$author',
    '$name',
    '$path',
    '$file_kind',
    '$size',
    '$cat1',
    '$cat2',
    '$mime',
    '$privacy'
  )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  if($entity != "") {
    $document["entity_id"] = "";
    $document["entity"] = "";
    $new_doc = run_query_search ($document, $new_order, $order_dir);
    $new_doc->next_record();
    $new_doc_id = $new_doc->f("document_id");
    
    $query = "insert into DocumentEntity (
      documententity_entity,
      documententity_entityid,
      documententity_documentid
    )
    values (    
      '$entity',
      '$entity_id',
      '$new_doc_id'
      )";
    $obm_q->query($query);

  }
  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of an uploaded file
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////

function run_query_get_mime($document){
  global $default_mime;
  $path = $document["path"];
  $file = $document["name"];
  $mime = $document["mime_file"];
  $kind = $document["kind"];
  var_dump($document); 
  if($mime != "") {
    $return = run_query_get_type_by_mime($mime);
    if($return) {
      return $return;
    }
  }
  if(strrpos($file,'/') == (strlen($file) -1) && $kind == "link") {
    $return = run_query_get_type_by_ext("html");
    if($return) {
      return $return;
    }
  }
  
  if($kind == "file") {
    $ext = substr($path,strrpos($path,'/'));
    $ext = substr(strrchr($path,'.'),1);
    $return = run_query_get_type_by_ext($ext);
    if($return) {
      return $return;
    }
  }
  else{
    $ext = substr($name,strrpos($name,'/'));
    $ext = substr(strrchr($name,'.'),1);
    $return = run_query_get_type_by_ext($ext);
    if($return) {
      return $return;
    }
  }

  $return = run_query_get_type_by_mime($default_mime);
  return $return ;
}

///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a disk file
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////

function run_query_get_mimetype($path){
  global $default_mime;
  $ext = substr(strrchr($path,'.'),1);
  $return = run_query_get_type_mime_by_ext($ext);
  if($return) {
    return $return;
  }
  $return = run_query_get_type_mime_by_mime($default_mime);
  return $return ;
}

///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql,$link_to;
  $query = "select * from Document where document_id ='$p_id'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $path = $obm_q->f("document_path");
  $name = $obm_q->f("document_name");
  if(islink($name)) {
    $truename = $link_to.$obm_q->f("document_title");
  }  
  $query = "select count(*) as link from Document where document_path =
           '$path' and document_name ='$name'";
  $obm_q->query($query);
  $obm_q->next_record();
  if($truename != "") {
    $name = $truename;
  }
  if($obm_q->f("link") == 1) {
    run_query_delete_file($path,$name);
 
  }
  $query = "delete from Document where document_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}
///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $type : Mime-Type
///////////////////////////////////////////////////////////////////////////////

function run_query_document_links($document_id) {
  $obm_q = new DB_OBM;
  $obm_q2 = new DB_OBM;

  $query = "select * from DocumentEntity where documententity_documentid =
           '$document_id'";
  $obm_q->query($query);
  return $obm_q->nf();

}
///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $type : Mime-Type
///////////////////////////////////////////////////////////////////////////////

function run_query_get_type_by_mime($type) {
  global $auth, $cdg_sql;

  $query = "select * from DocumentMimeType where documentmimetype_mime ='$type'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  if($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_id");
  }
  else 
    return false;
}                   
///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $ext : Extension 
///////////////////////////////////////////////////////////////////////////////

function run_query_get_type_by_ext($ext) {
  global $auth, $cdg_sql;

  $query = "select * from DocumentMimeType where documentmimetype_extension ='$ext'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  if($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_id");
  }
  else 
    return false;
}   

///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $ext : Extension 
///////////////////////////////////////////////////////////////////////////////

function run_query_get_type_mime_by_ext($ext) {
  global $auth, $cdg_sql;

  $query = "select * from DocumentMimeType where documentmimetype_extension ='$ext'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  if($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_mime");
  }
  else 
    return false;
}   
///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $ext : Extension 
///////////////////////////////////////////////////////////////////////////////

function run_query_get_type_mime_by_mime($mime) {
  global $auth, $cdg_sql;

  $query = "select * from DocumentMimeType where documentmimetype_mime ='$mime'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  if($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_mime");
  }
  else 
    return false;
}   


///////////////////////////////////////////////////////////////////////////////
// Delete File query execution                                                 //
// Parameters:
///////////////////////////////////////////////////////////////////////////////

function run_query_delete_file($path,$name) {
  global $document_path;
  unlink($document_path.'/'.$path.'/'.$name);
}

///////////////////////////////////////////////////////////////////////////////
// Create File query execution                                                 //
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////

function run_query_create_file($document) {
  global $document_path;
  $path = $document["path"];
  $name = $document["name"];
  $file = $document["file"];
  
  if(strpos($path,'/') === 0) {
    $final_path = $document_path.$path;
  }
  else {
    $final_path = $document_path.'/'.$path;
  }
  
  
  if(!is_dir($final_path)) {
    return false;
  }

  if(strrpos($finalpath,'/') == (strlen($finalpath) -1)) {
    $directory = $final_path.$name;
  }
  else {
    $directory = $final_path.'/'.$name;
  }
  if(!move_uploaded_file($file,$directory)) {
    return false;
  }
  return true; 
  

}



///////////////////////////////////////////////////////////////////////////////
// Document Form Data checking and formatting                                 //
// Parameters:
//   - $cid   : document id
//   - $document[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($cid, $document) {
  global $l_fill_title, $l_fill_author, $l_fill_path, $l_invalid_repository_path;
  global $l_invalid_file,$l_invalid_existing_file,$l_invalid_url;
  global $err_msg,$document_path,$l_invalid_link_file;

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
  
  if(strpos($path,'/') === 0) {
    $final_path = $document_path.$path;
  }
  else {
    $final_path = $document_path.'/'.$path;
  }
   
  if($kind == "new_file" && !is_dir($final_path) && $file_update != "no") {
    $err_msg = $l_invalid_repository_path;
    return false;
  }
  
  $realpath = realpath($final_path);
  if(substr($realpath,0,strlen($document_path)) != $document_path) {
    $err_msg = $l_invalid_repository_path;
    return false;
  }

  if($kind == "new_file"  && $file_update != "no" && ($name == "" || $size == "")) {
    $err_msg = $l_invalid_file;
    return false;

  }
  
  if($kind == "link" && $url == "" && $file_update != "no") {
      $err_msg = $l_invalid_url;
      return false;
  } 


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

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($document) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;
  $p_id = $document["id"];
  $name = $document["name"];
  $path = $document["path"];
  if(islink($name)) {
    $name = getlink($name,$path);
  }
  $obm_q = new DB_OBM;
  if ($obm_q->type == $db_type_mysql) {
    $query = "select *,
        UNIX_TIMESTAMP(document_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(document_timecreate) as timecreate
      from ((Document
         LEFT JOIN DocumentCategory1 on document_category1 = documentcategory1_id)
         LEFT JOIN DocumentCategory2 on document_category2 = documentcategory2_id)
	 LEFT JOIN DocumentMimeType on document_mimetype = documentmimetype_id
	 where document_id='$p_id'";
  } else if ($obm_q->type == $db_type_pgsql) {
    $query = "select *,document_timeupdate as timeupdate, document_timecreate as timecreate from Document where document_id='$p_id'";
  }
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail_by_path($path,$name) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select *,
      UNIX_TIMESTAMP(document_timeupdate) as timeupdate,
      UNIX_TIMESTAMP(document_timecreate) as timecreate
    from ((Document
       LEFT JOIN DocumentCategory1 on document_category1 = documentcategory1_id)
       LEFT JOIN DocumentCategory2 on document_category2 = documentcategory2_id)
       LEFT JOIN DocumentMimeType on document_mimetype = documentmimetype_id
       where document_name='$name' and (document_path='$path' OR  document_path='$path/'
       OR  document_path='/$path 'OR  document_path='/$path/')";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  if($obm_q->nf()>0) {
    $obm_q->next_record();
    return $obm_q;
  }
  return false;
}

///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $cid       : document id
//   - $document[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($did, &$document) {
  global $auth, $cdg_sql,$document_path,$c_auto;
  global $l_already_exist_error,$l_creation_error,$err_msg;
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
  $id = $document["id"];
  $file_update = $document["file_update"];
  if(strrpos($path,'/') != strlen($path) -1) {
    $path = $path.'/';
  }

  if(strpos($path,'/') === 0) {
    $final_path = $document_path.$path.$name;
  }
  else {
    $final_path = $document_path.'/'.$path.$name;
  }

  if($kind == "link") {
    $name = $url;
    $document["name"]=$name;
    $file_kind = 2;
  }
  elseif(file_exists($final_path) && $file_update=="yes") {
    $err_msg = $l_already_exist_error;
    return false;
  }

  if($size == "") $size = 0;
  
  if($mime == $c_auto && $file_update=="yes") {
    $mime = run_query_get_mime($document);
    $document["mime"] = $mime;
  }

  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');

  if($kind == "new_file" && $file_update=="yes") {
    $file_kind = 1;
    $obm_q = run_query_detail($document);
    if(!run_query_delete_file($obm_q->f("document_path"),$obm_q->f("document_name"))){ 
      $err_msg = $l_creation_error;
      return false;
    }
    if(!run_query_create_file($document)) {
      $err_msg = $l_creation_error;
      return false;
    }
  }
  
  //////
  

  $query = "update Document set
    document_timeupdate='". date("Y-m-d H:i:s") ."',
    document_userupdate='". $auth->auth["uid"] ."',
    document_title='$title',
    document_author='$author',
    document_private='$privacy'
";
  if($file_update=="yes") {
    $query .=" ,document_path='$path',
    document_size='$size',
    document_name='$name',
    document_category1='$cat1',
    document_category2='$cat2',
    document_mimetype='$mime'
    ";
  }
  $query .="    where document_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category1 insertion query construction and execution                           //
// Parameters:
//   - $document : document hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_insert($document) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat1 = $document["cat1_label"];

  $query = "insert into DocumentCategory1 (
    documentcategory1_timecreate,
    documentcategory1_usercreate,
    documentcategory1_label)
  values (
    '$timecreate',
    '$usercreate',
    '$cat1')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category2 insertion query construction and execution                           //
// Parameters:
//   - $document : document hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_insert($document) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat2 = $document["cat2_label"];

  $query = "insert into DocumentCategory2 (
    documentcategory2_timecreate,
    documentcategory2_usercreate,
    documentcategory2_label)
  values (
    '$timecreate',
    '$usercreate',
    '$cat2')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Mime Type insertion query construction and execution                           //
// Parameters:
//   - $document : document hash info : keys used : kind_label
///////////////////////////////////////////////////////////////////////////////
function run_query_mime_insert($document) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];

  $query = "insert into DocumentMimeType (
    documentmimetype_timecreate,
    documentmimetype_usercreate,
    documentmimetype_label,
    documentmimetype_extension,
    documentmimetype_mime)
  values (
    '$timecreate',
    '$usercreate',
    '$mime',
    '$extension',
    '$mimetype'
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Category 1  update query execution                                           //
// Parameters:
//   - $document : document hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_update($document) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $document["category2"];
  $cat2 = $document["cat2_label"];

  $query = "update DocumentCategory2 set
      documentcategory2_label='$cat2',
      documentcategory2_timeupdate='$timeupdate',
      documentcategory2_userupdate='$userupdate'
    where
      documentcategory2_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Category 1 update query execution                                           //
// Parameters:
//   - $document : document hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_update($document) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $document["category1"];
  $cat1 = $document["cat1_label"];

  $query = "update DocumentCategory1 set
      documentcategory1_label='$cat1',
      documentcategory1_timeupdate='$timeupdate',
      documentcategory1_userupdate='$userupdate'
    where
      documentcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Category 2 update query execution                                           //
// Parameters:
//   - $document : document hash info : keys used : act, act_label
///////////////////////////////////////////////////////////////////////////////
function run_query_mime_update($document) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $document["mime"];
  $mime = $document["mime_label"];
  $extension = $document["extension"];
  $mimetype = $document["mimetype"];

  $query = "update DocumentMimeType set
      documentmimetype_label='$mime',
      documentmimetype_extension='$extension',
      documentmimetype_mime='$mimetype',
      documentmimetype_timeupdate='$timeupdate',
      documentmimetype_userupdate='$userupdate'
    where
      documentmimetype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id                                  //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat1_label($id) {
  global $cdg_sql;

  $query = "select documentcategory1_label
    from DocumentCategory1
    where documentcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("documentcategory1_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Document links query execution                                  //
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_links($p_id) {
  global $cdg_sql;

  $query = "select document_title, document_id, document_category1
    from Document where document_category1='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id                                  //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_cat2_label($id) {
  global $cdg_sql;

  $query = "select documentcategory2_label
    from DocumentCategory2
    where documentcategory2_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("documentcategory2_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Document links query execution                                  //
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_links($p_id) {
  global $cdg_sql;

  $query = "select document_title, document_id, document_category2
    from Document where document_category2='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Get the label of an Category from its id                                  //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_mime_label($id) {
  global $cdg_sql;

  $query = "select documentmimetype_label
    from DocumentMimeType
    where documentmimetype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("documentmimetype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Document links query execution                                  //
// Parameters:
//   - $p_id : activity id
///////////////////////////////////////////////////////////////////////////////
function run_query_mime_links($p_id) {
  global $cdg_sql;

  $query = "select document_title, document_id, document_mimetype
    from Document where document_mimetype='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Category1 deletion query execution                                             //
// Parameters:
//   - $id : Category1 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_delete($id) {
  global $cdg_sql;

  $query = "delete from DocumentCategory1 where documentcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Category2 deletion query execution                                             //
// Parameters:
//   - $id : Category2 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat2_delete($id) {
  global $cdg_sql;

  $query = "delete from DocumentCategory2 where documentcategory2_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Mime Type deletion query execution                                             //
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_mime_delete($id) {
  global $cdg_sql;

  $query = "delete from DocumentMimeType where documentmimetype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}
///////////////////////////////////////////////////////////////////////////////
//Repository Creation                                             //
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////

function run_query_insert_repository($document) {
  global $cdg_sql;  
  global $document_path,$auth;
  $path = $document["repository_path"];
  $name = $document["repository_name"];
  if(strrpos($path,'/') != strlen($path) -1) {
    $path = $path.'/';
  }  
  $final_path = $document_path.'/'.$path.$name;
  if(file_exists($final_path)) {
    return false;
  }
  run_query_create_repository($document);

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
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '" . $auth->auth["uid"] . "',
    '',
    '',
    '$name',
    '$path',
    '0',
    '0',
    '',
    '',
    '',
    ''
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return true;

}

///////////////////////////////////////////////////////////////////////////////
//Repository Creation                                             //
// Parameters:
//   - $id : Mime Type id to delete
///////////////////////////////////////////////////////////////////////////////

function run_query_create_repository($document) {
  global $document_path;
  $path = $document["repository_path"];
  $name = $document["repository_name"];
  if(strpos($path,'/') === 0) {
    $final_path = $document_path.$path;
  }
  else {
    $final_path = $document_path.'/'.$path;
  }
  
  
  if(!is_dir($final_path)) {
    return false;
  }

  if(strrpos($finalpath,'/') == (strlen($finalpath) -1)) {
    $directory = $final_path.$name;
  }
  else {
    $directory = $final_path.'/'.$name;
  }
  if(!@mkdir($directory,0755))
    return false;
  return true; 

}
///////////////////////////////////////////////////////////////////////////////
// Document Form Data checking and formatting                                 //
// Parameters:
//   - $document[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_repository_data_form($document) {
  global $document_path,$err_msg;
  global $l_invalid_repository_path,$l_fill_repository_name;
  $name = $document["repository_name"];
  $path = $document["repository_path"];
  // MANDATORY: Document Title 
  if (trim($name) == "") {
    $err_msg = $l_fill_repository_name;
    return false;
  }
  $final_path = $document_path.$path;
  $realpath = realpath($final_path);
  if(substr($realpath,0,strlen($document_path)) != $document_path) {
    $err_msg = $l_invalid_repository_path;
    return false;
  }


  // MANDATORY: Document Title 
  if (!@opendir($final_path)) {
    $err_msg = $l_invalid_repository_path;
    return false;
  }

  return true;

}
///////////////////////////////////////////////////////////////////////////////
// Tell if the file is a link or not.
// return true if it is false else
///////////////////////////////////////////////////////////////////////////////

function islink($filename) {

  if(substr_count($filename,'/')>0) {
    return true;
  }
  return false;
}

///////////////////////////////////////////////////////////////////////////////
// Tell if the file is a link or not.
// return true if it is false else
///////////////////////////////////////////////////////////////////////////////

function getlink($name,$path) {
  return $name;
}

///////////////////////////////////////////////////////////////////////////////
// Query to build the file tree                                //
// Parameters:
//   - $display_file : bolean

///////////////////////////////////////////////////////////////////////////////


function run_query_get_tree($display_file) {
  global $cdg_sql,$auth;
  $where = "where (document_private = '0' OR (document_private = '1' AND document_usercreate = ".$auth->auth["uid"]."))";
  if($display_file != "true") {
    $where .= " AND document_kind = '0'";
  }
  $query = "select *  from Document $where 
  order by CONCAT(document_path,document_name)
";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;  

  /*
  Pour avoir un joli arbre
  CONCAT(
    SUBSTRING(
      REPLACE(
	document_path,
	'/',
	'/0'
      ),
      1,
      LENGTH(
	REPLACE(
      	  document_path,
	  '/',
	  '/0'
	) 
      )-1
    ),
      document_kind ,
      document_name
    )
  */
}
