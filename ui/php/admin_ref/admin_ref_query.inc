<?php
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
function run_query_admin_ref_country_insert($ctry) {
  global $obm, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $name = $ctry["ctry_name"];
  $iso = $ctry["ctry_code"];
  $lang = $ctry["ctry_lang"];
  $phone = $ctry["ctry_phone"];

  $query = "INSERT INTO Country (
      country_timecreate,
      country_usercreate,
      country_iso3166,
      country_name,
      country_lang,
      country_phone,
      country_domain_id
    ) VALUES (
     '$timecreate',
     '$usercreate',
     '$iso',
     '$name',
     '$lang',
     '$phone',
     '$obm[domain_id]')";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_country_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country update query execution
// Parameters:
//   - $ctry : data source hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_update($ctry) {
  global $obm, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $name = $ctry["ctry_name"];
  $iso = $ctry["ctry_code"];
  $lang = $ctry["ctry_lang"];
  $phone = $ctry["ctry_phone"];
  $old_iso = $ctry["ctry_old_code"];
  $old_lang = $ctry["ctry_old_lang"];

  $multidomain = sql_multidomain("country");

  $query = "UPDATE Country SET
      country_iso3166='$iso',
      country_name='$name',
      country_lang='$lang',
      country_phone='$phone',
      country_timeupdate='$timeupdate',
      country_userupdate='$userupdate'
    WHERE
      country_iso3166='$old_iso'
      AND country_lang='$old_lang'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_country_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country deletion query execution
// Parameters:
//   - $ctry : data source hash info : keys used : old_iso, old_lang
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_delete($ctry) {
  global $cdg_sql;

  $iso = $ctry["ctry_code"];
  $lang = $ctry["ctry_lang"];
  $multidomain = sql_multidomain("country");

  $query = "DELETE FROM Country
    WHERE country_iso3166='$iso'
      AND country_lang='$lang'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_country_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Company links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_company_links($iso) {
  global $cdg_sql;

  $multidomain = sql_multidomain("company");
  $query = "SELECT company_id,
      company_name,
      company_country_iso3166
    FROM Company
    WHERE company_country_iso3166='$iso'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_country_company_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Contact links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_contact_links($iso) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contact");
  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_country_iso3166
    FROM Contact
    WHERE contact_country_iso3166='$iso'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_country_contact_links()");
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
function get_admin_ref_country_name($iso, $lang) {
  global $cdg_sql;

  $multidomain = sql_multidomain("country");
  $query = "SELECT country_name
    FROM Country
    WHERE country_iso3166='$iso'
      AND country_lang='$lang'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_admin_ref_country_name()");
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
function run_query_admin_ref_datasource_insert($dsrc) {
  global $obm, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $name = $dsrc["dsrc_name"];

  $query = "INSERT INTO DataSource (
      datasource_timecreate,
      datasource_usercreate,
      datasource_name,
      datasource_domain_id
    )
    VALUES (
      '$timecreate',
      '$usercreate',
      '$name',
      '$obm[domain_id]'
    )";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_datasource_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source update query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : act, name
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_update($dsrc) {
  global $obm, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $id = $dsrc["dsrc"];
  $name = $dsrc["dsrc_name"];
  $multidomain = sql_multidomain("datasource");

  $query = "UPDATE DataSource SET
      datasource_name='$name',
      datasource_timeupdate='$timeupdate',
      datasource_userupdate='$userupdate'
    WHERE
      datasource_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_datasource_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source deletion query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_delete($dsrc) {
  global $cdg_sql;
  $multidomain = sql_multidomain("datasource");

  $id = $dsrc["dsrc"];

  $query = "DELETE FROM DataSource WHERE datasource_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_datasource_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Company links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_company_links($dsrc) {
  global $cdg_sql;

  $id = $dsrc["dsrc"];
  $multidomain = sql_multidomain("company");

  $query = "SELECT company_id,
      company_name,
      company_datasource_id
    FROM Company
    WHERE company_datasource_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_datasource_company_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Contact links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_contact_links($dsrc) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contact");
  $id = $dsrc["dsrc"];

  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_datasource_id
    FROM Contact
    WHERE contact_datasource_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_datasource_contact_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Data Source from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_datasource_name($id) {
  global $cdg_sql;
  $multidomain = sql_multidomain("datasource");

  $query = "SELECT datasource_name
    FROM DataSource
    WHERE datasource_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_admin_ref_datasource_name()");
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
function run_query_admin_ref_tasktype_insert($tt) {
  global $obm, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $label = $tt["tt_label"];
  $int = $tt["internal"];

  $query = "INSERT INTO TaskType (
      tasktype_timecreate,
      tasktype_usercreate,
      tasktype_label,
      tasktype_internal,
      tasktype_domain_id
    ) VALUES (
      '$timecreate',
      '$usercreate',
      '$label',
      '$int',
      '$obm[domain_id]'
    )";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type update query execution
// Parameters:
//   - $tt : task type hash info : keys used : tasktype, label, int
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_update($tt) {
  global $obm, $cdg_sql;

  $multidomain = sql_multidomain("tasktype");
  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $id = $tt["tasktype"];
  $label = $tt["tt_label"];
  $int = $tt["internal"];

  $query = "UPDATE TaskType SET
      tasktype_label='$label',
      tasktype_timeupdate='$timeupdate',
      tasktype_userupdate='$userupdate'
    WHERE
      tasktype_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type deletion query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_delete($tt) {
  global $cdg_sql;

  $id = $tt["tasktype"];
  $multidomain = sql_multidomain("tasktype");

  $query = "DELETE FROM TaskType WHERE tasktype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Deal links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_deal_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype_id"];
  $multidomain = sql_multidomain("deal");

  $query = "SELECT deal_id,
      deal_label,
      deal_tasktype_id
    FROM Deal
    WHERE deal_tasktype_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_deal_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Project links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_project_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype_id"];
  $multidomain = sql_multidomain("project");

  $query = "SELECT project_id,
      project_name,
      project_tasktype_id
    FROM Project
    WHERE project_tasktype_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_project_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - TimeTask links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_timetask_links($tt) {
  global $cdg_sql;

  $id = $tt["tasktype_id"];
  $multidomain = sql_multidomain("userobm");

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timedate = sql_date_format($db_type, "timetask_date", "date");

  $query = "SELECT timetask_id,
      timetask_user_id,
      $timedate,
      userobm_lastname as lastname,
      userobm_firstname as firstname,
      timetask_tasktype_id
    FROM TimeTask
         LEFT JOIN UserObm ON timetask_user_id=userobm_id
    WHERE timetask_tasktype_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_admin_ref_tasktype_timetask_links()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Task Type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_tasktype_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("tasktype");

  $query = "SELECT tasktype_label
    FROM TaskType
    WHERE tasktype_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_admin_ref_tasktype_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("tasktype_label");
  return $retour;
}

?>
