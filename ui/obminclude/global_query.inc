<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_query.inc                                             //
//     - Desc : Global Query File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get an OBM info
// Parameters:
//   - $info : info name
///////////////////////////////////////////////////////////////////////////////
function get_obm_info($info) {
  global $cdg_sql;

  $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='$info'";

  display_debug_msg($query, $cdg_sql, "get_obm_info($info)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("obminfo_value");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// CompanyCategory1 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_companycategory() {
  global $cdg_sql;

  $query = "SELECT * FROM CompanyCategory1 ORDER BY companycategory1_code"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;  
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Company contact update
// Parameters:
//   - $comp_id : company id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contact_number_update($comp_id) {
  global $cdg_sql;

  $query = "SELECT count(*) as nb
      FROM Contact
      WHERE contact_company_id='$comp_id'
        AND contact_archive != '1'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("nb");

  $query = "UPDATE Company
    SET company_contact_number='$nb'
    WHERE company_id='$comp_id'";

  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_datasource() {
  global $cdg_sql;

  $query = "SELECT * FROM DataSource ORDER BY datasource_name"; 
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the country list filled for the current lang
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_country_for_lang() {
  global $cdg_sql;

  $lang = get_lang();
  $query = "SELECT * FROM Country
    WHERE country_lang='$lang'
    ORDER BY country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_country() {
  global $cdg_sql;

  $query = "SELECT * FROM Country
     ORDER BY country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Function select query execution
// Returns:
//   DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_function() {
  global $cdg_sql, $cgp_hide;

  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  if ($show_func) {
    $query = "SELECT function_id, function_label
    FROM Function
    ORDER by function_label";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// TaskType list query
// Parameters :
//   - $tt_cat : tasktype category
//               "all" or ""  return all tasktype
//               "project" "  return tasktype from $ctt_sales and $ctt_research
//               one category return the category
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype($tt_cat="") {
  global $cdg_sql, $ctt_sales, $ctt_research;

  $query = "
    SELECT
      tasktype_id,
      tasktype_label, 
      tasktype_internal
    FROM TaskType ";

  if (($tt_cat == "") || ($tt_cat == "all")) {
    $query .= "";
  } elseif ($tt_cat == "project") {
    $query .= "WHERE (tasktype_internal = '$ctt_sales'
                      or tasktype_internal = '$ctt_research')";
  } else {
    $query .= "WHERE tasktype_internal = '$tt_cat'";
  }

  $query .= " ORDER BY tasktype_internal, tasktype_label";

  display_debug_msg($query, $cdg_sql, "run_query_tasktype");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of contacts linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (company)
//   - $arch   : flag to tell if archived contacts should be counted (if true)
// Return:
//   - $nb     : number of Contact
///////////////////////////////////////////////////////////////////////////////
function get_linked_contact_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(contact_id) as nb
    FROM Contact
    WHERE contact_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " AND contact_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of deals linked to an entity
// Parameters:
//   - $id      : Id of entity
//   - $entity  : entity (company, contact)
//   - $arch    : flag to tell if archived deal should be counted (if true)
//   - $entity2 : entity 2 (for contact as deal can have 2 contacts)
// Return:
//   - $nb      : number of Deal
///////////////////////////////////////////////////////////////////////////////
function get_linked_deal_nb ($id, $entity, $arch="", $entity2="") {
  global $cdg_sql;

  if ($entity2 != "") {
    $where_ent = "(deal_${entity}_id = '$id' OR deal_${entity2}_id = '$id')";
  } else {
    $where_ent = "deal_${entity}_id = '$id'";
  }

  $query = "SELECT count(deal_id) as nb
    FROM Deal
    WHERE $where_ent";

  if (! $arch) {
    $query .= " AND deal_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of projects linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company)
//   - $arch   : flag to tell if archived projects should be counted (if true)
// Return:
//   - $nb     : number of Project
///////////////////////////////////////////////////////////////////////////////
function get_linked_project_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(project_id) as nb
    FROM Project
    WHERE project_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " AND project_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of contract linked to an entity
// Parameters:
//   - $id      : Id of entity
//   - $entity  : entity (deal, company)
//   - $arch    : flag to tell if archived contract should be counted (if true)
//   - $entity2 : entity 2 (for contact as contract can have 2 contacts)
// Return:
//   - $nb      : number of contract
///////////////////////////////////////////////////////////////////////////////
function get_linked_contract_nb ($id, $entity, $arch="", $entity2="") {
  global $cdg_sql;

  if ($entity2 != "") {
    $where_ent = "(contract_${entity}_id = '$id' OR contract_${entity2}_id = '$id')";
  } else {
    $where_ent = "contract_${entity}_id = '$id'";
  }

  $query = "SELECT count(contract_id) as nb
    FROM Contract
    WHERE $where_ent";

  if (! $arch) {
    $query .= " AND contract_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of incidents linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (contract))
//   - $arch   : flag to tell if archived incidents should be counted (if true)
// Return:
//   - $nb     : # Incident
///////////////////////////////////////////////////////////////////////////////
function get_linked_incident_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(incident_id) as nb
    FROM Incident
    WHERE incident_{$entity}_id = '$id'";

  if (! $arch) {
    $query .= " AND incident_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of invoice linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, project)
//   - $arch   : flag to tell if archived invoices should be counted (if true)
// Return:
//   - $nb     : number of invoice
///////////////////////////////////////////////////////////////////////////////
function get_linked_invoice_nb ($id, $entity, $arch="") {
  global $cdg_sql;

  $query = "SELECT count(invoice_id) as nb
    FROM Invoice
    WHERE invoice_${entity}_id = '$id'";

  if (! $arch) {
    $query .= " AND invoice_archive='0'";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of documents linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, contact..)
// Return:
//   - $nb : document number
///////////////////////////////////////////////////////////////////////////////
function run_query_document_nb ($id,$entity) {
  global $cdg_sql;

  $query = "SELECT count(*) as nb
    FROM DocumentEntity
    WHERE documententity_entity = '$entity'
      AND documententity_entity_id = '$id'"; 

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Delete document linked with an entity
// Parameters:
//   - $id     : entity Id
//   - $entity : Type of entity (deal, company..)
//   - $doc_id : [optionnal] document Id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_document_links($ent_id, $entity, $doc_id="") {
  global $cdg_sql;

  $where = "1=1";

  if ($ent_id != "") {
    $where .= " AND documententity_entity_id = '$ent_id'";
  }
  if ($entity != "") {
    $where .= " AND documententity_entity = '$entity'";
  }
  if ($doc_id != "") {
    $where .= " AND documententity_document_id = '$doc_id'";
  }

  $query = "DELETE FROM DocumentEntity WHERE $where";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Insert document linked with a entity
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - $cpt_ins : # documents inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_documents($ids, $entity) {
  global $cdg_sql;

  $id = $ids["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $ids["doc_nb"]) {
    $cpt++;
    $d_id = $ids["doc$cpt"];
    
    $query = "SELECT *
      FROM DocumentEntity
      WHERE documententity_entity='$entity'
        AND documententity_entity_id='$id'
        AND documententity_document_id = '$d_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "INSERT INTO DocumentEntity (
       	documententity_entity,
	documententity_entity_id,
	documententity_document_id
      )
      VALUES (    
	'$entity',
	'$id',
	'$d_id'
      )";
      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's todo into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_todos($set_todo="todo_priority") {
  global $cdg_sql, $cgp_todo_nb, $todos, $sess, $auth;

  $uid = $auth->auth["uid"];

  $limit = sql_limit($db_type, $cgp_todo_nb, 0);
  $order = ($set_todo != "" ? $set_todo : "todo_priority");
  if ($order == "todo_priority") {
    $order .= " desc";
  }

  // create the todo list
  $query = "SELECT
      todo_id,
      todo_title
    FROM Todo
    WHERE todo_user = '$uid'
    ORDER BY $order
    $limit";

  display_debug_msg($query, $cdg_sql);
  $todolist_q = new DB_OBM;
  $todolist_q->query($query);

  $nb_todo = $todolist_q->num_rows();
  $max = min ($cgp_todo_nb, $nb_todo);

  $todos = "";

  for ($rank = 1; $rank <= $max; $rank++) {
    $todolist_q->next_record();

    $option = "todo_$rank";
    $t_id = $todolist_q->f("todo_id");
    $t_title = $todolist_q->f("todo_title");

    $todos[$rank]["id"] = $t_id; 
    $todos[$rank]["title"] = $t_title; 
  }

  $sess->register("todos");
}


///////////////////////////////////////////////////////////////////////////////
// Get the company name from the company Id
// Parameters:
//   - $id : company id 
///////////////////////////////////////////////////////////////////////////////
function get_last_company_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT company_name FROM Company WHERE company_id='$id'";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("company_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact name from the contact Id
// Parameters:
//   - $id : contact id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contact_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT contact_lastname FROM Contact WHERE contact_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("contact_lastname");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal label from the deal Id
// Parameters:
//   - $id : deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_deal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT deal_label FROM Deal WHERE deal_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
    $obm_db->next_record();
    return $obm_db->f("deal_label");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Parent deal label from the parent deal Id
// Parameters:
//   - $id : parent deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_parentdeal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT parentdeal_label
    FROM ParentDeal
    WHERE parentdeal_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("parentdeal_label");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list name from the list Id
// Parameters:
//   - $id : list id 
///////////////////////////////////////////////////////////////////////////////
function get_last_list_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT list_name FROM List WHERE list_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db = new DB_OBM;
    $obm_db->query($query);
    $obm_db->next_record();
    return $obm_db->f("list_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the publication name from the publication Id
// Parameters:
//   - $id : publication id 
///////////////////////////////////////////////////////////////////////////////
function get_last_publication_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT publication_title
      FROM Publication
      WHERE publication_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("publication_title");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal project from the project Id
// Parameters:
//   - $id : project id 
///////////////////////////////////////////////////////////////////////////////
function get_last_project_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT project_name FROM Project WHERE project_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("project_name");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract label from the contract Id
// Parameters:
//   - $id : contract id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contract_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT contract_label FROM Contract WHERE contract_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("contract_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the incident label from the incident Id
// Parameters:
//   - $id : incident id 
///////////////////////////////////////////////////////////////////////////////
function get_last_incident_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT incident_label FROM Incident WHERE incident_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("incident_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Document name from the document Id
// Parameters:
//   - $id : document id 
///////////////////////////////////////////////////////////////////////////////
function get_last_document_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT document_name FROM Document WHERE document_id='$id'";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("document_name");
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the account label from the account Id
// Parameters:
//   - $id : account id
///////////////////////////////////////////////////////////////////////////////
function get_last_account_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT account_label FROM Account WHERE account_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("account_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the invoice label from the invoice Id
// Parameters:
//   - $id : invoice id 
///////////////////////////////////////////////////////////////////////////////
function get_last_invoice_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT invoice_label FROM Invoice WHERE invoice_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("invoice_label");
  }
  return;
}

///////////////////////////////////////////////////////////////////////////////
// Get the resource label from the resource Id
// Parameters:
//   - $id : resource id
///////////////////////////////////////////////////////////////////////////////
function get_last_resource_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT resource_name FROM Resource WHERE resource_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("resource_name");
  }
  return;
}

///////////////////////////////////////////////////////////////////////////////
// Get the resourcegroup label from the resourcegroup Id
// Parameters:
//   - $id : resourcegroup id
///////////////////////////////////////////////////////////////////////////////
function get_last_resourcegroup_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT rgroup_name FROM RGroup WHERE rgroup_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("rgroup_name");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the payment label from the payment Id
// Parameters:
//   - $id : payment id 
///////////////////////////////////////////////////////////////////////////////
function get_last_payment_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $query = "SELECT payment_label FROM Payment WHERE payment_id='$id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("payment_label");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Return an array of all used emails (users, groups)
// Parameters:
//   - $id  : User id to exclude from the result
//   - $gid : Group id to exclude from the result
//   - $lid : List id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_email_used($id="", $gid="", $lid="") {
  global $cdg_sql, $php_regexp_email_name;

  $mails = array();
  if ($id != "") {
    $where_id = "WHERE userobm_id != '$id'";
  }
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_email
    FROM UserObm
    $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $email = $obm_q->f("userobm_email");
    $name = "Utilisateur : " . $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
    $em = strtok($email, "\r\n");
    while ($em) {
      $mails["$em"] = "$name";
      $em = strtok("\r\n");
    }
  }

  if ($gid != "") {
    $where_gid = "WHERE group_id != '$gid'";
  }
  $query = "SELECT group_name, group_email
    FROM UGroup
    $where_gid";

  display_debug_msg($query, $cdg_sql);
  $g_q = new DB_OBM;
  $g_q->query($query);

  while ($g_q->next_record()) {
    $email = $g_q->f("group_email");
    $name = "Groupe : " . $g_q->f("group_name");
    $mails["$email"] = "$name";
  }

  if ($lid != "") {
    $where_lid = "WHERE list_id != '$lid'";
  }
  $query = "SELECT list_name, list_email
    FROM List
    $where_lid";

  display_debug_msg($query, $cdg_sql);
  $l_q = new DB_OBM;
  $l_q->query($query);

  while ($l_q->next_record()) {
    $email = $l_q->f("list_email");
    $name = "List : " . $l_q->f("list_name");
    $mails["$email"] = "$name";
  }

  return $mails;
}


///////////////////////////////////////////////////////////////////////////////
// Return the SQL advanced search clause on Company name
// Parameters:
//   - $name : company name to search
//   - $like : like clause (can be ilike for postgres)
// Returns:
//   - $sql : sql criteria
///////////////////////////////////////////////////////////////////////////////
function sql_company_name_advanced_search($name, $like="like") {

  $sound = phonetic_key($name);
  $sql = " AND (company_name $like '$name%'
                  OR company_aka $like '%$name%'
                  OR company_sound = '$sound')";

  return $sql;
}

?>
