<script language="php">
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
// Country update query execution
// Parameters:
//   - $ctry : data source hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_country_update($ctry) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $name = $ctry["name"];
  $iso = $ctry["iso"];
  $lang = $ctry["lang"];
  $phone = $ctry["phone"];
  $old_iso = $ctry["old_iso"];
  $old_lang = $ctry["old_lang"];

  $query = "update Country set
      country_iso3166='$iso',
      country_name='$name',
      country_lang='$lang',
      country_phone='$phone',
      country_timeupdate='$timeupdate',
      country_userupdate='$userupdate'
    where
      country_iso3166='$old_iso'
      and country_lang='$old_lang'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country deletion query execution
// Parameters:
//   - $ctry : data source hash info : keys used : old_iso, old_lang
///////////////////////////////////////////////////////////////////////////////
function run_query_country_delete($ctry) {
  global $cdg_sql;

  $iso = $ctry["iso"];
  $lang = $ctry["lang"];

  $query = "delete from Country
    where country_iso3166='$iso'
      and country_lang='$lang'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Company links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_country_company_links($iso) {
  global $cdg_sql;

  $query = "select company_id,
      company_name,
      company_country_iso3166
    from Company
    where company_country_iso3166='$iso'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Contact links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_country_contact_links($iso) {
  global $cdg_sql;

  $query = "select contact_id,
      contact_lastname,
      contact_firstname,
      contact_country_iso3166
    from Contact where contact_country_iso3166='$iso'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Country from its iso, lang
// Parameters:
//   - $iso  : country_iso3166
//   - $lang : country_lang
///////////////////////////////////////////////////////////////////////////////
function get_country_name($iso, $lang) {
  global $cdg_sql;

  $query = "select country_name
    from Country
    where country_iso3166='$iso'
      and country_lang='$lang'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("country_name");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source insertion query construction and execution
// Parameters:
//   - $dsrc : data source hash info : keys used : name
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_insert($dsrc) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $name = $dsrc["name"];

  $query = "INSERT INTO DataSource (
      datasource_timecreate,
      datasource_usercreate,
      datasource_name)
    VALUES (
      '$timecreate',
      '$usercreate',
      '$name')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source update query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : act, name
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_update($dsrc) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $dsrc["datasource"];
  $name = $dsrc["name"];

  $query = "UPDATE DataSource SET
      datasource_name='$name',
      datasource_timeupdate='$timeupdate',
      datasource_userupdate='$userupdate'
    WHERE
      datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source deletion query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_delete($dsrc) {
  global $cdg_sql;

  $id = $dsrc["datasource"];

  $query = "DELETE FROM DataSource WHERE datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Company links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_company_links($dsrc) {
  global $cdg_sql;

  $id = $dsrc["datasource"];

  $query = "SELECT company_id,
      company_name,
      company_datasource_id
    FROM Company
    WHERE company_datasource_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Contact links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource_contact_links($dsrc) {
  global $cdg_sql;

  $id = $dsrc["datasource"];

  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_datasource_id
    FROM Contact
    WHERE contact_datasource_id='$id'";

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


///////////////////////////////////////////////////////////////////////////////
// Task Type insertion query construction and execution
// Parameters:
//   - $tt : task type hash info : keys used : label, internal
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_insert($tt) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $tt["label"];
  $int = $tt["internal"];

  $query = "insert into TaskType (
      tasktype_timecreate,
      tasktype_usercreate,
      tasktype_label,
      tasktype_internal)
    values (
      '$timecreate',
      '$usercreate',
      '$label',
      '$int')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type update query execution
// Parameters:
//   - $tt : task type hash info : keys used : tasktype, label, int
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_update($tt) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $tt["tasktype"];
  $label = $tt["label"];
  $int = $tt["internal"];

  $query = "update TaskType set
      tasktype_label='$label',
      tasktype_timeupdate='$timeupdate',
      tasktype_userupdate='$userupdate'
    where
      tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type deletion query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_delete($tt) {
  global $cdg_sql;

  $id = $tt["tasktype"];

  $query = "delete from TaskType where tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Deal links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_deal_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype"];

  $query = "select deal_id,
      deal_label,
      deal_tasktype_id
    from Deal
    where deal_tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Project links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_project_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype"];

  $query = "select project_id,
      project_name,
      project_tasktype_id
    from Project where project_tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - TimeTask links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_timetask_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timedate = sql_date_format($db_type, "timetask_date", "date");

  $query = "select timetask_id,
      timetask_user_id,
      $timedate,
      userobm_lastname as lastname,
      userobm_firstname as firstname,
      timetask_tasktype_id
    from TimeTask
         left join UserObm on timetask_user_id=userobm_id
    where timetask_tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Task Type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_tasktype_label($id) {
  global $cdg_sql;

  $query = "select tasktype_label
    from TaskType
    where tasktype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("tasktype_label");
  return $retour;
}
