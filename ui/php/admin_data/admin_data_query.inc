<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_data_query.inc                                         //
//     - Desc : Data admin Query File                                        //
// 2003-05-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Query execution - company list                                            //
///////////////////////////////////////////////////////////////////////////////
function get_company_list() {
  global $cdg_sql;

  $query = "select company_id,
      company_name,
      company_aka,
      company_sound,
      company_contact_number,
      company_deal_number,
      company_deal_total
    from Company";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company contact number
// Parameters:
//   - $id : company id
///////////////////////////////////////////////////////////////////////////////
function get_company_contact_number($id) {
  global $cdg_sql;

  $query = "select count(contact_id) as nb from Contact
            where contact_company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company active (non archived) deal number
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_company_active_deal_number($id) {
  global $cdg_sql;

  $query = "select count(deal_id) as nb from Deal
            where deal_company_id='$id' and deal_archive='0'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company total deal number
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function get_company_total_deal_number($id) {
  global $cdg_sql;

  $query = "select count(deal_id) as nb from Deal
            where deal_company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("nb");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Update
// Parametres:
//   - $id         : company id
//   - $con_num    : contact number
//   - $deal_num   : active deal number
//   - $deal_total : total deal number
///////////////////////////////////////////////////////////////////////////////
function update_one_company($id, $con_num, $deal_num, $deal_total) {
  global $cdg_sql;

  $query = "update Company set
      company_contact_number='$con_num',
      company_deal_number='$deal_num',
      company_deal_total='$deal_total'
    where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_deal_list() {
  global $cdg_sql;

  $query = "select deal_id,
      deal_hitrate,
      deal_status_id,
      deal_archive,
      dealstatus_label,
      dealstatus_hitrate
    from Deal
      left join DealStatus on deal_status_id = dealstatus_id";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal hitrate Update
// Parametres:
//   - $id      : deal id
//   - $hitrate : deal new hitrate
///////////////////////////////////////////////////////////////////////////////
function update_one_deal($id, $hitrate) {
  global $cdg_sql;

  $query = "update Deal set
      deal_hitrate='$hitrate'
    where deal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - List list
///////////////////////////////////////////////////////////////////////////////
function get_list_list() {
  global $cdg_sql;

  $query = "select list_id,
      list_name,
      list_subject,
      list_email,
      list_mailing_ok,
      list_static_nb,
      list_query_nb,
      list_query,
      list_structure
    from List
    order by list_name";

  display_debug_msg($query, $cdg_sql);

  $l_q = new DB_OBM;
  $l_q->query($query);
  return $l_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - List Update
// Parametres:
//   - $id        : list id
//   - $sta_nb    : static contact number
//   - $dyn_nb    : dynamic contact number
//   - $new_query : query
///////////////////////////////////////////////////////////////////////////////
function update_one_list($id, $sta_nb, $dyn_nb, $new_query) {
  global $cdg_sql;

  $query = "update List set
      list_static_nb='$sta_nb',
      list_query_nb='$dyn_nb',
      list_query='$new_query'
    where list_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - document list                                           //
///////////////////////////////////////////////////////////////////////////////
function get_document_list() {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $ctt[0]["type"] = "field";
  $ctt[0]["value"] = "document_path";
  $ctt[1]["type"] = "field";
  $ctt[1]["value"] = "document_name";
  $concat = sql_string_concat($db_type, $ctt);

  $query = "SELECT document_id,
      document_path,
      document_name,
      document_kind
    FROM Document
    ORDER BY $concat";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk document path
// Parameters:
//   - $id   : document id
// Returns:
//   real disk path
///////////////////////////////////////////////////////////////////////////////
function get_document_disk_path($id) {
  global $document_path;

  // The document disk path set in the subdirectory named "last id number"
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = $document_path . "/" . $rel . "/";
  
  return $disk_path; 
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk document path
// Parameters:
//   - $id   : document id
// Returns:
//   real disk path
///////////////////////////////////////////////////////////////////////////////
function get_document_disk_relative_path($id) {
  global $document_path;

  // The document disk path set in the subdirectory named "last id number"
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = "/" . $rel . "/" . $id;
  
  return $disk_path; 
}


///////////////////////////////////////////////////////////////////////////////
// Get file list from Filesystem. includes Files + directories
// Returns:
// Array( [$fullname] => array([0]=>$path [1]=>$name), ..)
///////////////////////////////////////////////////////////////////////////////
function get_file_list($repository) {
  global $document_path;

  $root_handler = opendir($repository);
  $relative_path = substr($repository,strlen($document_path));
  while ($file = readdir($root_handler)) {
    $path = "$repository$file";
    if ($file != ".." && $file != ".") {
      $ret[$path] = $file;
    }
  }
  closedir($root_handler);
  if ($ret) {
    ksort($ret);
    foreach($ret as $path=>$file) {
      $tree[$relative_path.$file] = array($relative_path,$file);;
      if (is_dir($path)) {
	$result =  get_file_list("$path/");
	if ($result) {
	  $tree = array_merge($tree,$result);
	}
      }
    }
    return $tree;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Sound Update
// Parametres:
//   - $id    : company id
//   - $aka   : aka to insert
//   - $sound : sound key to insert
///////////////////////////////////////////////////////////////////////////////
function update_sound_aka_one_company($id, $aka, $sound) {
  global $cdg_sql;

  $aka_s = addslashes($aka);

  $query = "update Company set
      company_aka='$aka_s',
      company_sound='$sound'
    where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);

  return $retour;
}


