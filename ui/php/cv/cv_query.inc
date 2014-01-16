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



/**
* @filesource cv_query.inc CV query File 
* @author 2006-05-22  Vincent Coulette    
*/
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


/**
* CV Search query 
* @param array $cv[] cv search criteria
* @return DB Object $obm_q
*/
function run_query_cv_search($cv) {
  global $cdg_sql, $c_all, $ctu_sql_limit;
  $obm_q = new DB_OBM;

  $sql_order_dir = $cv["sql_order_dir"];
  $sql_order_field = $cv["sql_order_field"];

  $lastname = sql_search_text_parse($obm_q->escape($cv["lastname"]));
  $firstname = sql_search_text_parse($obm_q->escape($cv["firstname"]));
  $keyword_title = sql_search_text_parse($obm_q->escape($cv["keyword_title"]));
  $keyword_education = sql_search_text_parse($obm_q->escape($cv["keyword_education"]));
  $project_name = sql_search_text_parse($obm_q->escape($cv["project_name"]));
  $deal_name = sql_search_text_parse($obm_q->escape($cv["deal_name"]));

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
 // $where .= sql_obm_entity_privacy("cv");

  if ($keyword_title != "") {
    $where .= " AND cv_title $like '%$keyword_title%'";
  }

  $whereq = "WHERE 1=1 $where";
  
  $joinq = "";
  
  if ($lastname != "") {
    $joinq .= " AND userobm_lastname = '$lastname' ";
  }
  
  if ($firstname != "") {
    $joinq .= " AND userobm_firstname = '$firstname' ";
  }
        
  if ($keyword_education != "") {
    $joinq .= " AND userobm_education $like '%$keyword_education%' ";
  }
            
  if ($project_name != "" || $deal_name != "") {
    $joinq .= " JOIN ProjectCV ON cv_id = projectcv_cv_id
                JOIN Project ON projectcv_project_id = project_id ";
    if ($project_name != "") {
      $joinq .= " AND project_name $like '%$project_name%'";
    }
    if ($deal_name != "") {
      $joinq .= " JOIN Deal ON project_deal_id = deal_id AND deal_label $like '%$deal_name%'";
    }    
  }

  // order
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "userobm_lastname";

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field, "firstname") == 0) {
    $order = "userobm_firstname";
  } else if (strcmp($sql_order_field, "timeupdate") == 0) {
    $order = "cv_timeupdate";
  } else if (strcmp($sql_order_field, "timecreate") == 0) {
    $order = "cv_timecreate";
  }

  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $multidomain = sql_multidomain("cv"); 
  
  $timeupdate = sql_date_format($db_type, "cv_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "cv_timecreate", "timecreate");
  
  $query = "SELECT cv_title,
      cv_id,
      cv_id as id,
      cv_userobm_id,
      userobm_lastname,
      userobm_firstname,
      $timeupdate,
      $timecreate
    FROM CV
      JOIN UserObm ON cv_userobm_id = userobm_id
      $joinq
    $whereq
      $multidomain
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM CV $join_comp $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_cv_search()");
    $obm_q->query($query);
  }

  return($obm_q);
}


/**
CV detail query execution
@param integer $id cv id
@return DB Object $obm_q
*/
function run_query_cv_detail($id) {
  global $cdg_sql;  

  $id = sql_parse_id($id, true);
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timeupdate = sql_date_format($db_type, "cv_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "cv_timecreate", "timecreate");

  $multidomain = sql_multidomain("cv"); 
  $query = "SELECT * FROM CV WHERE cv_id $id $multidomain";

  /*SELECT *,
      p.userobm_lastname as lastname,
      p.userobm_firstname as firstname,
      p.userobm_location as location,
      p.userobm_phone as phone,
      p.userobm_education as education,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM CV
         JOIN UserObm as p on cv_userobm_id=userobm_id
         LEFT JOIN UserObm as c on cv_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on cv_userupdate=u.userobm_id
    WHERE cv_id = '$id'";*/

  display_debug_msg($query, $cdg_sql, "run_query_cv_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


/**
* CV refs detail query execution
* @param  integer $id cv id
* @return DB Object $obm_q
*/
function run_query_cv_refs_detail($id) {
  global $cdg_sql;  

  $sql_order_dir = $cv["sql_order_dir"];
  $sql_order_field = $cv["sql_order_field"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $id = sql_parse_id($id, true);
  // order
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "project_name";

  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $multidomain = sql_multidomain("cv"); 
  $query = "SELECT cv_id,
    project_id, project_name, project_reference_desc, project_reference_tech,
    project_reference_duration, project_reference_date,
    projectcv_role
  FROM CV
    JOIN ProjectCV ON cv_id=projectcv_cv_id
    JOIN Project ON projectcv_project_id=project_id
  WHERE cv_id $id 
    $multidomain
  $order_q";

  display_debug_msg($query, $cdg_sql, "run_query_cv_refs_detail()");
  $obm_q->query($query);

  return $obm_q;
}


/**
* Insertion query construction
* @param integer $cv
* @return integer $cv_id
*/
function run_query_cv_insert($cv) {
  global $display, $cdg_sql, $obm, $cgp_show;
  $obm_q = new DB_OBM;

  // CV insertion
  
  $uid = sql_parse_id($obm["uid"]);
  $title = $cv["title"];
  $user_id = sql_parse_id($cv["user_id"]);
  $additionalrefs = $cv["additionalrefs"];
  $comment = $cv["comment"];
  $timecreate = date("Y-m-d H:i:s");
  $query = "INSERT INTO CV
     (cv_timeupdate,
      cv_timecreate,
      cv_userupdate,
      cv_usercreate,
      cv_title,
      cv_userobm_id,
      cv_additionnalrefs,
      cv_comment,
      cv_domain_id  
    )
    VALUES (
      null,
      '$timecreate', 
      $uid,
      $uid,
      '".$obm_q->escape($title)."',
      $user_id,
      '".$obm_q->escape($additionalrefs)."',
      '".$obm_q->escape($comment)."',
      $obm[domain_id]
    )";

  display_debug_msg($query, $cdg_sql, "run_query_cv_insert()");
  $retour = $obm_q->query($query);

  $cv_id = $obm_q->lastid(); 
  $entity_id = of_entity_insert($GLOBALS['module'], $id);  
  
  // ProjectCV insertion
  
  if ($cv_id > 0 ) {
    $projects_id = $cv["project_id"];
    $projects_role = $cv["project_role"];
    if (is_array($projects_id) && count($projects_id) > 0){
      foreach ($projects_id as $project_id){
        $role = $projects_role[$project_id];
        $ret = run_query_cv_insert_projectcv($project_id, $cv_id, $role);
        if (!$ret)
          return false;
      }   
    }
    return $cv_id;
  }
  else
    return -1;
}


/**
* ProjectCV Insertion query construction
* @param integer $project_id id of project
* @param integer $cv_id id of CV
* @param string $role role of the member working on the project 
* @return string $retour insertion into table ProjectCV
*/
function run_query_cv_insert_projectcv($project_id, $cv_id, $role) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $project_id = sql_parse_id($project_id);
  $cv_id = sql_parse_id($cv_id);

  $query = "INSERT INTO ProjectCV
     (projectcv_project_id,
      projectcv_cv_id,
      projectcv_role)
    VALUES (
      $project_id,
      $cv_id, 
      '".$obm_q->escape($role)."')";

  display_debug_msg($query, $cdg_sql, "run_query_cv_insert_projectcv()");
  $retour = $obm_q->query($query);

  return $retour;
}


/**
* Update query construction
* @param integer $cv
* @return bool true or false
*/
function run_query_cv_update($cv) {
  global $obm, $cdg_sql, $ccf_ticket, $ccf_duration;
  $obm_q = new DB_OBM;

  $id = sql_parse_id($cv["cv_id"], true);
  $uid = sql_parse_id($obm["uid"]);
  $title = $cv["title"];
  $user_id = sql_parse_id($cv["user_id"]);
  $additionalrefs = $cv["additionalrefs"];
  $comment = $cv["comment"];
  $multidomain = sql_multidomain("cv"); 
  
  $query = "UPDATE CV SET
      cv_timeupdate = '". date("Y-m-d H:i:s")."',
      cv_userupdate = $uid,
      cv_title = '".$obm_q->escape($title)."',
      cv_additionnalrefs = '$additionalrefs',
      cv_comment = '".$obm_q->escape($comment)."'
    WHERE cv_id = $id
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_cv_update(1)");
  $ret = $obm_q->query($query);  
  
  // ProjectCV are deleted and reinserted
  $query = "DELETE FROM ProjectCV WHERE projectcv_cv_id = '$id'";
  display_debug_msg($query, $cdg_sql, "run_query_cv_update(2)");
  $obm_q->query($query); 
  
  $projects_id = $cv["project_id"];
  $projects_role = $cv["project_role"];
  if(is_array($projects_id))
  foreach ($projects_id as $key => $project_id){
    $role = $projects_role[$project_id];
    $ret = run_query_cv_insert_projectcv($project_id, $id, $role);
    if (!$ret)
      return false;
  }   
  return true;
}


/**
* Delete query execution
* @param $p_id cv id
* @return string $ret delete the CV from CV and ProjectCV table
*/
function run_query_cv_delete($p_id) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true); 
  // delete the projectcv
  $query = "DELETE FROM ProjectCV WHERE projectcv_cv_id $p_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_cv_delete(1)");
  $ret = $obm_q->query($query);
  
  // delete the CV
  $query = "DELETE FROM CV WHERE cv_id $p_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_cv_delete(2)");
  $ret = $obm_q->query($query);

  return $ret;
}


/**
* CV ID : Get the id of the cv inserted
* @param array $cv[] cv hash info
* @param date $timecreate creation time
* @param integer $uid creator
* @return integer $id if found else false
*/
function run_query_cv_id($cv, $timecreate, $uid) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($uid, true);
  $title = $cv["title"];
  $user_id = sql_parse_id($cv["user_id"], true);
  $additionalrefs = $cv["additionalrefs"];
  $comment = $cv["comment"];

  $multidomain = sql_multidomain("cv"); 
  // comment and additionalrefs not searched because processed before stored in db
  $query = "SELECT
      cv_id
    FROM CV
    WHERE cv_title = '".$obm_q->escape($title)."'
      AND cv_userobm_id $user_id 
      AND cv_timecreate = '".$obm_q->escape($timecreate)."'
      AND cv_usercreate $uid 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_cv_id()");
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
   $obm_q->next_record();
    $id = $obm_q->f("cv_id");
  } else {
    $id = false;
  }

  return $id;
}


/**
* CV Form Data checking and formatting
* @param integer $cid  cv id : (empty on insertion)
* @param array $cv[] values to check
* @return bool true if check ok, else false
*/
function check_cv_form($cid, $cv) {
  global $err, $l_err_title_empty;

  // Check if the title is filled
  if (trim($cv["title"]) == "") {
    $err["msg"] = $l_err_title_empty;
    return false;
  }

  return true;
}


/**
* Check if the cv can be deleted
* @param integer $p_id cv id
* @return bool true if the cv can be deleted, else false
*/
function check_can_delete_cv($p_id) {
  global $ok_msg;

  $delete_ok = true;
  
  // no links

  return $delete_ok;
}


/**
* Detail query execution
* @param integer $user_id user id
* @return DB object $obm_q
*/
function run_query_user_detail($user_id){
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $user_id = sql_parse_id($user_id, true); 
  $multidomain = sql_multidomain("userobm"); 
  $query = "SELECT
    userobm_login,
    userobm_lastname,
    userobm_firstname,
    userobm_location,
    userobm_phone,
    userobm_education
  FROM UserObm
  WHERE userobm_id $user_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_user_detail()");
  $obm_q->query($query);
  $obm_q->next_record();
  
  return $obm_q;
}


/**
* Default Odt template insert query execution
* @param $cv cv hash info
* keys used : defaultofttemplate_label, defaultofttemplate_document_id
* @return string $retour
*/
function run_query_defaultodttemplate_insert($cv) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $entity = "cv";
  $document_id = sql_parse_id($cv["defaultodttemplate_document_id"]);
  $label = $cv["defaultodttemplate_label"];

  $query = "INSERT INTO DefaultOdtTemplate (
    defaultodttemplate_entity,
    defaultodttemplate_document_id,
    defaultodttemplate_label,
    defaultodttemplate_domain_id
  ) VALUES (
    '".$obm_q->escape($entity)."',
    $document_id,
    '".$obm_q->escape($label)."',
    $obm[domain_id])";

  display_debug_msg($query, $cdg_sql, "run_query_defaultodttemplate_insert()");
  $retour = $obm_q->query($query);
  return $retour;
}


/**
* Default Odt template update query execution
* @param array $cv cv hash info : keys used : defaultofttemplate_label
* @return string $retour 
*/
function run_query_defaultodttemplate_update($cv) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $label = $cv["defaultodttemplate_label"];
  $id = sql_parse_id($cv["defaultodttemplate"], true);
  $multidomain = sql_multidomain("defaultodttemplate");

  $query = "UPDATE DefaultOdtTemplate SET
      defaultodttemplate_label='".$obm_q->escape($label)."'
    WHERE
      defaultodttemplate_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_defaultodttemplate_update()");
  $retour = $obm_q->query($query);
  return $retour;
}


/**
* Default Odt template deletion query execution
* @param integer $id defaultodttemplate id
* @return string $retour
*/
function run_query_defaultodttemplate_delete($id) {
  global $cdg_sql;

  $id = sql_parse_id($id, true);
  $multidomain = sql_multidomain("defaultodttemplate");
  $query = "DELETE
    FROM DefaultOdtTemplate
    WHERE defaultodttemplate_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_defaultodttemplate_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


/**
* Format CV data for export
* @param object $cv_q 
* @param object $refs_q 
* @param $anonymous flag to hide personal details (just name initials)
* @return string $data
*/
function format_cv_data_export($cv_q, $refs_q, $anonymous) {
  global $l_refs, $l_comment, $l_education, $l_personal;
  
  $data_vars = array();
  $data_blocks = array();
  
  // comment
  $data_vars["title"] = $cv_q->f("cv_title");
  
  // personal details
  $data_vars["l_personal"] = $l_personal;
  
  $lastname = $cv_q->f("lastname");
  $firstname = $cv_q->f("firstname");
  
  $data_vars["lastname"] = $lastname;
  $data_vars["firstname"] = $firstname;
  $data_vars["address"] = "N/A"; // $cv_q->f("location");
  $data_vars["phone"] = "N/A"; //$cv_q->f("phone");
  
  $the_personals[] = $a_personal;
  $data_blocks["personal"] = $the_personals;

  $initials = "";
  if ($firstname != "" && $lastname != "") $initials .= $firstname{0}.".".$lastname{0}.".";
  
  $a_anonymous["initials"] = $initials;
  $the_anonymous[] = $a_anonymous;
  $data_blocks["anonymous"] = $the_anonymous;
  
  if ($anonymous) {
    $data_vars["anonymous"] = 1;
  }
  else {
    $data_vars["anonymous"] = 0;
  }
    
  
  $data_vars["l_education"] =  $l_education;
  $data_vars["education"] = "N/A"; // $cv_q->f("education");
  
  // references
  $data_vars["l_refs"] = $l_refs;
  while ($refs_q->next_record()) {
    $a_ref["date"] = $refs_q->f("project_reference_date");
    $a_ref["duration"] = $refs_q->f("project_reference_duration");
    $a_ref["role"] = $refs_q->f("projectcv_role");
    $a_ref["project"] = $refs_q->f("project_name");
    $a_ref["desc"] = $refs_q->f("project_reference_desc");
    $a_ref["tech"] = $refs_q->f("project_reference_tech");
    $the_refs[] = $a_ref; 
  }
  // additional refs
  $additionalrefs = split_additionalrefs($cv_q->f("cv_additionalrefs"));
  foreach($additionalrefs as $index => $ref) {
    $a_ref["date"] = $ref[0];
    $a_ref["duration"] = $ref[1];
    $a_ref["role"] = $ref[2];
    $a_ref["project"] = $ref[3];
    $a_ref["desc"] = $ref[4];
    $a_ref["tech"] = $ref[5];
    $the_refs[] = $a_ref;
  }
  $data_blocks["refs"] = $the_refs;
  
  // comment
  $data_vars["l_comment"] = $l_comment;
  $data_vars["comment"] = $cv_q->f("cv_comment");
  
  $data = array("data_vars" => $data_vars, "data_blocks" => $data_blocks);

  return $data;
}


/**
* Split Additional refs text block into arrays (one array by ref)
* @param array $additionalrefs
* @return string $ret
*/
function split_additionalrefs($additionalrefs) {

  $sep = "[|-sep-|]";
  $bigsep = "[|-bigsep-|]";
  $refs = explode($bigsep, $additionalrefs);
  $ret = array();
  for ($i = 0; $i < count($refs); $i++){
    $one_ref = explode($sep, $refs[$i]);
    $ret[] = $one_ref;
  }
  return $ret;
}


/**
* Additionnalrefs DB text formatting 
* @param string $dates date of project
* @param string $durations duration of project
* @param string $projects name of project
* @param string $roles role of member
* @param string $descs description of tasks member
* @param string $techs technical description of tasks member
* @return string $additionalrefs
*/
function format_additionalrefs($dates, $durations, $projects, $roles, $descs, $techs){
  $additionalrefs = "";
  $sep = "[|-sep-|]";
  $bigsep = "[|-bigsep-|]";
  if ($dates != "" && is_array($dates)) { 
    foreach ($dates as $key => $value){
      if ($dates[$key] != "" || $durations[$key] != "" || $projects[$key] != "" || $roles[$key] != "" || $descs[$key] != "" || $techs[$key] != ""){
        if ($additionalrefs != "")
          $additionalrefs .= $bigsep;
        $additionalrefs .= $dates[$key].$sep.$durations[$key].$sep.$projects[$key].$sep.$roles[$key].$sep.$descs[$key].$sep.$techs[$key];
      }
    }
  }
  return $additionalrefs;
}

?>
