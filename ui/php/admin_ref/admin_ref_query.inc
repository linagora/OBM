<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_query.inc                                          //
//     - Desc : Referential Data Query File                                  //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Data Source query execution                                               //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource() {
  global $cdg_sql;

  $query = "select * from DataSource order by datasource_name"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source insertion query construction and execution                    //
// Parameters:
//   - $dsrc : data source hash info : keys used : name
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_insert($dsrc) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $name = $dsrc["name"];

  $query = "insert into DataSource (
      datasource_timecreate,
      datasource_usercreate,
      datasource_name)
    values (
      '$timecreate',
      '$usercreate',
      '$name')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source update query execution                                        //
// Parameters:
//   - $dsrc : data source hash info : keys used : act, name
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_update($dsrc) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $dsrc["id"];
  $name = $dsrc["name"];

  $query = "update DataSource set
      datasource_name='$name',
      datasource_timeupdate='$timeupdate',
      datasource_userupdate='$userupdate'
    where
      datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source deletion query execution                                      //
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_delete($dsrc) {
  global $cdg_sql;

  $id = $dsrc["id"];

  $query = "delete from DataSource where datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Company links query execution                               //
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_company_links($dsrc) {
  global $cdg_sql;

  $id = $dsrc["id"];

  $query = "select company_id,
      company_name,
      company_datasource_id
    from Company
    where company_datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Contact links query execution                               //
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_contact_links($dsrc) {
  global $cdg_sql;

  $id = $dsrc["id"];

  $query = "select contact_id,
      contact_lastname,
      contact_firstname,
      contact_datasource_id
    from Company where contact_datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}
