<SCRIPT language="php">
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
// Query execution - document list                                           //
///////////////////////////////////////////////////////////////////////////////
function get_document_list() {
  global $cdg_sql;

  $query = "select document_id,
      document_path,
      document_name,
      document_kind
      from Document order by
      BINARY CONCAT(document_path,
	     document_name
  )";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - document list                                           //
///////////////////////////////////////////////////////////////////////////////
function get_file_list($repository) {
  global $document_path;
  $root_handler = opendir($repository);
  $relative_path = substr($repository,strlen($document_path));
  while($file = readdir($root_handler)) {
    $path = "$repository$file";
    if($file != ".." && $file != ".") {
      $ret[$path] =$file;
    }
  }
  closedir($root_handler);
  if($ret) {
    ksort($ret);
    foreach($ret as $path=>$file) {
      $tree[$relative_path.$file] = array($relative_path,$file);;
      if(is_dir($path)) {
	  $result =  get_file_list("$path/");
	if($result) {
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

  $query = "update Company set
      company_aka='$aka',
      company_sound='$sound'
    where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


