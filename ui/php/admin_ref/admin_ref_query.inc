<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_query.inc                                          //
//     - Desc : Referential Data Query File                                  //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Country insertion query construction and execution
// Parameters:
//   - $ctry : data source hash info : keys used : name
///////////////////////////////////////////////////////////////////////////////
function run_query_country_insert($ctry) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $name = $ctry["name"];
  $iso = $ctry["iso"];
  $lang = $ctry["lang"];
  $phone = $ctry["phone"];

  $query = "insert into Country (
      country_timecreate,
      country_usercreate,
      country_iso3166,
      country_name,
      country_lang,
      country_phone)
    values (
      '$timecreate',
      '$usercreate',
      '$iso',
      '$name',
      '$lang',
      '$phone')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country update query execution                                        //
// Parameters:
//   - $ctry : data source hash info : keys used : act, name
///////////////////////////////////////////////////////////////////////////////
function run_query_country_update($ctry) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $ctry["country"];
  $name = $ctry["name"];
  $iso = $ctry["iso"];
  $lang = $ctry["lang"];
  $phone = $ctry["phone"];

  $query = "update Country set
      country_iso3166='$iso',
      country_name='$name',
      country_lang='$lang',
      country_phone='$phone',
      country_timeupdate='$timeupdate',
      country_userupdate='$userupdate'
    where
      country_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country deletion query execution                                      //
// Parameters:
//   - $ctry : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_country_delete($ctry) {
  global $cdg_sql;

  $id = $ctry["country"];

  $query = "delete from Country where country_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Company links query execution                               //
// Parameters:
//   - $id : country_id
///////////////////////////////////////////////////////////////////////////////
function run_query_country_company_links($id) {
  global $cdg_sql;

  $query = "select company_id,
      company_name,
      company_country_id
    from Company
    where company_country_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Contact links query execution                               //
// Parameters:
//   - $id : country_id
///////////////////////////////////////////////////////////////////////////////
function run_query_country_contact_links($id) {
  global $cdg_sql;

  $query = "select contact_id,
      contact_lastname,
      contact_firstname,
      contact_country_id
    from Contact where contact_country_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Country from its id                                 //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_country_name($id) {
  global $cdg_sql;

  $query = "select country_name
    from Country
    where country_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("country_name");
  return $retour;
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
  $id = $dsrc["datasource"];
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

  $id = $dsrc["datasource"];

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

  $id = $dsrc["datasource"];

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

  $id = $dsrc["datasource"];

  $query = "select contact_id,
      contact_lastname,
      contact_firstname,
      contact_datasource_id
    from Contact where contact_datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Data Source from its id                                 //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_datasource_name($id) {
  global $cdg_sql;

  $query = "select datasource_name
    from DataSource
    where datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("datasource_name");
  return $retour;
}
