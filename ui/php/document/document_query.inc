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
  global $c_all, $cdg_sql;  

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $name = $document["filename"];
  
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
      from ((Document
         LEFT JOIN DocumentCategory1 on document_category1 = documentcategory1_id)
         LEFT JOIN DocumentCategory2 on document_category2 = documentcategory2_id)
	 LEFT JOIN DocumentMimeType on document_mimetype = documentmimetype_id";
      

  $and = "";
  if ($title != "") {
    $where .= " document_title like '%$title%'";
    $and = "and";
  }
  
  if ($name != "") {
    $where .= " document_name like '%$name%'";
    $and = "and";
  }


  if (($cat1 != $c_all) && ($cat1 != "")) { 
    $where .= " $and document_category1 = $cat1";
    $and = "and";
  }
  
  if (($cat2 != $c_all) && ($cat2 != "")) { 
    $where .= " $and document_category2 = $cat2";
    $and = "and";
  }
  
  if (($author != $c_all) && ($author != "")) { 
    $where .= " $and document_author like '%$author%'";
    $and = "and";
  }
  
  if (($mime != $c_all) && ($mime != "")) { 
    $where .= " $and document_mimetype = $mime";
    $and = "and";
  }
  
  if ($where != "") {
    $where = " where $where";
  }

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
function run_query_insert($document) {
  global $auth, $cdg_sql;

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $path = $document["path"];
  $size = $document["size"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');

  $query = "insert into Document (
    document_timeupdate,
    document_timecreate,
    document_userupdate,
    document_usercreate,
    document_title,
    document_author,
    document_path,
    document_size,
    document_category1,
    document_category2,
    document_mimetype,
    document_private
)
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    " . $auth->auth["uid"] . ",
    '$title',
    '$author',
    '$path',
    $size,
    $cat1,
    $cat2,
    $mime,
    $privacy
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
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
  global $l_fill_title, $l_fill_author, $l_fill_path;
  global $err_msg;

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $path = $document["path"];
  


  // MANDATORY: Document Title 
  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }

  // MANDATORY: Document Title 
  if (trim($author) == "") {
    $err_msg = $l_fill_author;
    return false;
  }

  // MANDATORY: Document Title 
  if (trim($path) == "") {
    $err_msg = $l_fill_path;
    return false;
  }


  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : document id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

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
    $query = "select *,document_timeupdate as timeupdate, document_timecreate as timecreate from Document where document_id=".$p_id;
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $cid       : document id
//   - $document[] : Entry's values
//     keys used  : num, archive, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($did, $document) {
  global $auth, $cdg_sql;

  $title = $document["title"];
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = $document["author"];
  $mime = $document["mime"];
  $path = $document["path"];
  $size = $document["size"];
  $name = $document["name"];
  $privacy = (isset($document["privacy"]) ? $document["privacy"] : '0');


  $query = "update Document set
    document_timeupdate='". date("Y-m-d H:i:s") ."',
    document_userupdate='". $auth->auth["uid"] ."',
    document_title='$title',
    document_author='$author',
    document_path='$path',
    document_size='$size',
    document_bame='$name'
    document_category1=$cat1,
    document_category2=$cat2,
    document_mimetype='$mime',
    document_private=$privacy
  where document_id='$did'";

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


