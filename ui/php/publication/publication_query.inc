<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_query.inc                                        //
//     - Desc : publication query File                                       //
// 2004-01-28 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Publication search query execution
// Parametes:
//   - $publication[]    : publication search criteria
//     keys used       archive, title, phone, type, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_search($publication, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $archive = $publication["archive"];
  $title = sql_search_text_parse($publication["title"]);
  $lang = sql_search_text_parse($publication["lang"]);
  $type = $publication["type"];
  $year = sql_search_text_parse($publication["year"]);
  $market = $publication[""];
  $zip = $publication["zip"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $and = "";
  if ($title != "") {
    $where .= " publication_title $like '$title%'";
    $and = "AND";
  }
  if (($type != $c_all) && ($type != "")) { 
    $where .= " $and publication_type_id='$type'";
    $and = "AND";
  }
  if ($year != "") {
    $where .= " $and publication_year $like '$year%'";
    $and = "AND";
  }
  if ($lang != "") {
    $where .= " $and publication_lang $like '%$lang%'";
    $and = "AND";
  }
  if ($where != "") {
    $whereq = "WHERE $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : " publication_year DESC,publication_title";
  $orderq = "ORDER BY $order $p_order_dir";

  $query = "SELECT
      publication_id as id,
      publication_id,
      publication_title,
      publication_type_id,
      publication_year,
      publication_lang,
      publicationtype_label
    FROM Publication
         LEFT JOIN PublicationType ON publication_type_id=publicationtype_id
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Publication $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_publication_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_detail($p_id) {
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;

  $timeupdate = sql_date_format($type, "publication_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "publication_timecreate", "timecreate");

  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Publication
         LEFT JOIN PublicationType on publication_type_id=publicationtype_id
         LEFT JOIN UserObm as c on publication_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on publication_userupdate=u.userobm_id
    WHERE publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Category 1 query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_contactcategory1() {
  global $cdg_sql;

  $query = "SELECT * FROM ContactCategory1 ORDER BY contactcategory1_code"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_publication_contactcategory1()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// List query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_list() {
  global $cdg_sql;

  $query = "SELECT list_id,
      list_name,
      userobm_login
    FROM List LEFT JOIN UserObm ON list_usercreate=userobm_id
    ORDER BY list_name"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_publication_list()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : num, archive, title, type, ad1, ad2, ad3, zip, town, cdx
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_insert($publication) {
  global $auth, $cdg_sql;

  $title = $publication["title"];
  $type = $publication["type"];
  $lang = $publication["lang"];
  $year = $publication["year"];
  $desc = $publication["desc"];
  $concat1 = $publication["concat1"];
  $list_sub = $publication["list_sub"];
  $time = date("Y-m-d H:i:s");
  
  $query = "INSERT INTO Publication (
    publication_timeupdate,
    publication_timecreate,
    publication_userupdate,
    publication_usercreate,
    publication_title,
    publication_type_id,
    publication_lang,
    publication_year,
    publication_desc)
  VALUES (null,
    '$time',
    null,
    '" . $auth->auth["uid"] . "',
    '$title',
    '$type',
    '$lang',
    '$year',
    '$desc'
    )";

  display_debug_msg($query, $cdg_sql, "run_query_publication_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  $query = "SELECT publication_id
    FROM Publication
    WHERE publication_timecreate = '$time'
      AND publication_usercreate = '" . $auth->auth["uid"] . "'
      AND publication_title = '$title'
      AND publication_year = '$year'";
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  $obm_q->next_record();
  $p_id = $obm_q->f("publication_id");
  
  if ( (is_array($concat1) && (count($concat1)>0) )
       || (is_array($list_sub) && (count($list_sub)>0) ) ) {
    run_query_publication_auto_subscription($publication, $p_id);
  }
  return $p_id;
}


///////////////////////////////////////////////////////////////////////////////
// Auto Subscription Insertion query execution                                 
// Parameters:
//   - $publication[] : List of contact_type to subscribe
//   -p_id :  Id of the subscription
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_auto_insert($publication){
  global $auth, $cdg_sql;
  
  $id = $publication["id"];
  $id_orig = $publication["id_orig"];
  $recept = $publication["recept"];
  
  $query = "INSERT INTO Subscription (
    subscription_timeupdate,
    subscription_timecreate,
    subscription_userupdate,
    subscription_usercreate,
    subscription_contact_id,
    subscription_publication_id,
    subscription_renewal,
    subscription_reception_id,
    subscription_quantity)
  SELECT 
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '" . $auth->auth["uid"] . "',
    subscription_contact_id,
    '$id',
    '1',
    '$recept',
    subscription_quantity  
  FROM Subscription 
  WHERE subscription_renewal = '1' 
    AND subscription_reception_id = '$recept'
    AND subscription_publication_id = '$id_orig'";
  
  display_debug_msg($query, $cdg_sql);

  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Auto Subscription Insertion query execution                                 
// Parameters:
//   - $publication[] : pub hash [concat1] and [list_sub] of interest
//   - $p_id       :  Id of the subscription
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_auto_subscription($publication, $p_id){
  global $auth, $cdg_sql, $c_focsys;
  
  $concat1 = $publication["concat1"];
  $list_sub = $publication["list_sub"];
  $recept = $c_focsys;
  $lang = $publication["lang"];

  // Subscription according to contact category 1
  if (is_array($concat1) && (count($concat1)>0)) {
    $obm_q = new DB_OBM;
    $type = $obm_q->type;  
    $like = sql_casei_like($type);     
    $lang_array = split("-",$lang);
    $query = "INSERT INTO Subscription (
      subscription_timeupdate,
      subscription_timecreate,
      subscription_userupdate,
      subscription_usercreate,
      subscription_contact_id,
      subscription_publication_id,
      subscription_renewal,
      subscription_reception_id,
      subscription_quantity)
    SELECT 
      null,
      '" . date("Y-m-d H:i:s") . "',
      null,
      '" . $auth->auth["uid"] . "',
      contact_id,
      '$p_id',
      '1',
      '$recept',
      '1'  
    FROM Contact
      LEFT JOIN ContactCategory1Link
           ON contactcategory1link_contact_id = contact_id
      LEFT JOIN Kind ON kind_id = contact_kind_id
    WHERE contactcategory1link_category_id IN (";

    foreach($concat1 as $category_id) {
      $query .= "$coma '$category_id'";
      $coma =',';
    }
    $query .= ") 
  AND (";
    $coma = "";
    foreach($lang_array as $lang_id) {
      $query .= "$coma kind_lang $like '$lang_id' ";
      $coma ='OR';
    }
    $query .= ") 
  group by contact_id";

    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }


  // Subscription according to contact list
  if (is_array($list_sub) && (count($list_sub)>0)) {
    $obm_q = new DB_OBM;
    $contacts = array();

    // Loop for each list
    foreach ($list_sub as $list_id) {
      $c_q = ext_list_get_contact_ids($list_id);

      // Process eacg list contact (if not already done)
      while ($c_q->next_record()) {
	$c_id = $c_q->f("contact_id");

	// If subscription not already set for this contact
	if (! in_array($c_id, $contacts)) {
	  $contacts[] = $c_id;
	  $query = "INSERT INTO Subscription (
          subscription_timeupdate,
          subscription_timecreate,
          subscription_userupdate,
          subscription_usercreate,
          subscription_contact_id,
          subscription_publication_id,
          subscription_renewal,
          subscription_reception_id,
          subscription_quantity)
        VALUES ( 
          null,
          '" . date("Y-m-d H:i:s") . "',
          null,
          '" . $auth->auth["uid"] . "',
          '$c_id',
          '$p_id',
          '1',
          '$recept',
          '1')";
	  display_debug_msg($query, $cdg_sql, "run_query_publication_auto_subscription(from list : contact : $c_id)");
	  $retour = $obm_q->query($query);
	}
      }
    }
  }

  return $retour;
}

 
///////////////////////////////////////////////////////////////////////////////
// Subscription Insertion query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_insert_subscription($publication) {
  global $auth, $cdg_sql;

  $c_id = $publication["contact_id"];
  $quantity = $publication["quantity"];
  $p_id = $publication["id"];
  $renew = $publication["renew"];
  $recept = $publication["recept"];
/*  $query = "select count(subscription_contact_id) as num from
  Subscription where subscription_contact_id = '$c_id' and
  subscription_publication_id = '$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  $obm_q->next_record();
  if($obm_q->f("num") != 0) {
    return true;
  }*/  
  $query = "INSERT INTO Subscription (
    subscription_timeupdate,
    subscription_timecreate,
    subscription_userupdate,
    subscription_usercreate,
    subscription_contact_id,
    subscription_publication_id,
    subscription_renewal,
    subscription_reception_id,
    subscription_quantity)
  VALUES (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    " . $auth->auth["uid"] . ",
    '$c_id',
    '$p_id',
    '$renew',
    '$recept',
    '$quantity'
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Subscription Update query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_update_subscription($publication) {
  global $auth, $cdg_sql;

  $s_id = $publication["subscription_id"];
  $quantity = $publication["quantity"];
  $renew = $publication["renew"];
  $recept = $publication["recept"];
  
  $query = "UPDATE Subscription SET 
      subscription_timeupdate = '". date("Y-m-d H:i:s") ."',
      subscription_userupdate='". $auth->auth["uid"] ."',
      subscription_renewal='$renew',
      subscription_reception_id='$recept',
      subscription_quantity='$quantity'
    WHERE	
      subscription_id = '$s_id'";
    
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $cid       : publication id
//   - $publication[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_update($pid, $publication) {
  global $auth, $cdg_sql;

  $title = $publication["title"];
  $type = $publication["type"];
  $lang = $publication["lang"];
  $year = $publication["year"];
  $desc = $publication["desc"];

  $query = "UPDATE Publication SET
      publication_timeupdate='". date("Y-m-d H:i:s") ."',
      publication_userupdate='". $auth->auth["uid"] ."',
      publication_title='$title',
      publication_lang='$lang',
      publication_year='$year',
      publication_desc='$desc',
      publication_type_id='$type'
    WHERE publication_id='$pid'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_delete($p_id) {
  global $cdg_sql;

  $query = "DELETE FROM Publication WHERE publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : publication id
//   - $c_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_delete_subscription($s_id) {
  global $cdg_sql;

  $query = "delete from Subscription where subscription_id='$s_id'";
  display_debug_msg($query, $cdg_sql, "run_query_publication_delete_subscription()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contacts attached to the publication
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_contact_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "SELECT DISTINCT contact_id, contact_lastname, contact_firstname 
    FROM Contact LEFT JOIN Subscription ON subscription_contact_id = contact_id
    WHERE subscription_publication_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_publication_contact_links()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the publications with this title and zip (not with this id if set)
// Parameters:
//   - $p_id : publication Id not to return
//   - $title : title
//   - $zip  : zip
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_check($p_id, $title, $lang) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $like = sql_casei_like($type);

  $where_id = "";
  if ($p_id != "") {
    $where_id = "AND publication_id!='$p_id'";
  }
  $query = "SELECT distinct publication_id, publication_title, publication_lang
    FROM Publication
    WHERE publication_title $like '$title%'
      AND publication_lang $like '$lang%'
      $where_id";

  display_debug_msg($query, $cdg_sql, "run_query_publication_check()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Type publication query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type() {
  global $cdg_sql;

  $query = "SELECT * FROM PublicationType ORDER BY publicationtype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_publication_type()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode subscription query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_reception() {
  global $cdg_sql, $c_focsys;

  $query = "SELECT *
    FROM SubscriptionReception
    WHERE subscriptionreception_id != '$c_focsys'
    ORDER BY subscriptionreception_label DESC "; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_publication_subscription_reception()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Type insertion query construction and execution
// Parameters:
//   - $publication : publication hash info : keys used : type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_insert($publication) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $type = $publication["type_label"];

  $query = "INSERT INTO PublicationType (
    publicationtype_timecreate,
    publicationtype_usercreate,
    publicationtype_label)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$type')";

  display_debug_msg($query, $cdg_sql, "run_query_publication_type_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode insertion query construction and execution
// Parameters:
//   - $publication : publication hash info : keys used : recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_insert($publication) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $recept = $publication["recept_label"];

  $query = "INSERT INTO SubscriptionReception (
    subscriptionreception_timecreate,
    subscriptionreception_usercreate,
    subscriptionreception_label)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$recept')";

  display_debug_msg($query, $cdg_sql, "run_query_publication_recept_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type update query execution
// Parameters:
//   - $publication : publication hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_update($publication) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $publication["type"];
  $type = $publication["type_label"];

  $query = "UPDATE PublicationType SET
      publicationtype_label='$type',
      publicationtype_timeupdate='$timeupdate',
      publicationtype_userupdate='$userupdate'
    WHERE
      publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_type_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Reception update query execution
// Parameters:
//   - $publication : publication hash info : keys used : recept, recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_update($publication) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $publication["recept"];
  $recept = $publication["recept_label"];

  $query = "UPDATE SubscriptionReception SET
      subscriptionreception_label='$recept',
      subscriptionreception_timeupdate='$timeupdate',
      subscriptionreception_userupdate='$userupdate'
    WHERE
      subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_recept_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type deletion query execution
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM PublicationType WHERE publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_type_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Reception deletion query execution
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_delete($id) {
  global $cdg_sql;

  $query = "DELETE
    FROM SubscriptionReception
    WHERE subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_recept_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type - Publication links query execution
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_links($p_id) {
  global $cdg_sql;

  $query = "SELECT publication_title, publication_id, publication_type_id
    FROM Publication
    WHERE publication_type_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_type_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception - Subscription links query execution
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_links($p_id) {
  global $cdg_sql;

  $query = "SELECT publication_title,
      publication_id,
      subscription_reception_id,
      contact_firstname,
      contact_id  
    FROM Subscription
      LEFT JOIN Publication ON publication_id = subscription_publication_id
      LEFT JOIN Contact ON contact_id = subscription_contact_id
    WHERE subscription_reception_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_recept_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_publication_type_label($id) {
  global $cdg_sql;

  $query = "SELECT publicationtype_label
    FROM PublicationType
    WHERE publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_publication_type_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("publicationtype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Reception Mode from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_publication_recept_label($id) {
  global $cdg_sql;

  $query = "SELECT subscriptionreception_label
    FROM SubscriptionReception 
    WHERE subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_publication_recept_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("subscriptionreception_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Publication environment checking (same publications exists ?)
// Parameters: :/ Tous les champs ici
//   - $cid       : publication id
//   - $publication[] : publication's values
// Returns:
//   - Publication Database object with list of similar publications
///////////////////////////////////////////////////////////////////////////////
function check_publication_context($cid, $publication) {

  $title = $publication["title"];
  $lang = $publication["lang"];

  // if a publication with same title and zip exists, return false
  $co_q = run_query_publication_check($cid, $title, $lang);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_publication_data($cid, $publication) {
  global $l_fill_title, $l_j_check_year,$l_j_fill_lang, $php_regexp_pub_lang;
  global $err_msg, $l_j_check_lang;

  $title = $publication["title"];
  $year = $publication["year"];
  $lang = $publication["lang"];
  $desc = $publication["desc"];
  $type = $publication["type"];

  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }

  if (($year == "") || ($year < 1900 || $year > 2200)) {
    $err_msg = $l_j_check_year. " : $year";
    return false;
  }

  if ($lang == "") {
    $err_msg = $l_j_fill_lang;
    return false;
  } else if (preg_match($php_regexp_pub_lang, $lang) == 0) {
    $err_msg = $l_j_check_lang . " : $lang";
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_publication_data_subscription_form($cid, $publication) {
  global $l_j_check_quantity,$l_j_select_publication ;
  global $err_msg;

  $quantity = $publication["quantity"];
  $p_id = $publication["id"];

  if (($p_id == "") || !($p_id >= 0) ) {
    $err_msg = $l_j_select_publication;
    return false;
  }
  if (($quantity == "")) {
    $err_msg = $l_j_check_quantity;
    return false;
  }
  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Check if the publication can be deleted
// Parameters:
//   - $p_id : publication id
// Returns:
//   true if the publication can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_publication_can_delete($p_id) {
  global $err_msg, $ok_msg, $block;
  global $path;
  global $l_contact, $l_link_contact, $l_link_contact_no;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_publication_contact_links($p_id);
  $nb = $obm_q->num_rows();

  if ($nb > 0) {
    $delete_ok = false;
    $err_msg = $l_link_contact;
    $display_linkc ="
      <tr>
        <td class=\"detailHead\">$l_contact</td>
      </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname") . " " . $obm_q->f("contact_firstname");
      $display_linkc .= "
      <tr>
        <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
        </td>
      </tr>";
    }
  } else {
    $ok_msg = "$l_link_contact_no";
  }

  $block .= "
  <table class=\"detail\">
    $display_linkc
  </table>";


  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Contact detail query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_get_contact_lang($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
    $query = "SELECT kind_lang
      FROM Contact LEFT JOIN Kind ON contact_kind_id = kind_id
      WHERE contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_get_contact_lang()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("kind_lang");
}


///////////////////////////////////////////////////////////////////////////////
//
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_external_search($hash_table) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $contact = $hash_table["id"];
  $obm_q = new DB_OBM;
  $type = $obm_q->type;     
  $timeupdate = sql_date_format($type, "subscription_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "subscription_timecreate", "timecreate");

  $query = "SELECT
      $timeupdate,
      $timecreate,
      subscription_id,
      subscription_contact_id,
      subscription_publication_id,
      publication_title,
      publication_year,
      publication_lang,
      subscription_renewal,
      subscriptionreception_label,
      subscription_quantity 
    FROM Subscription 
      LEFT JOIN Publication ON publication_id = subscription_publication_id
      LEFT JOIN SubscriptionReception ON subscriptionreception_id = subscription_reception_id 	
    WHERE subscription_contact_id='$contact'
    ORDER BY subscription_timecreate desc";

  display_debug_msg($query, $cdg_sql, "run_query_publication_subscription_external_search()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
//
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_detail($s_id){
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;

  $query = "SELECT
      subscription_contact_id,
      subscription_publication_id,
      publication_title,
      subscription_renewal,
      subscription_reception_id,
      subscription_quantity 
    FROM Subscription 
	LEFT JOIN Publication ON publication_id = subscription_publication_id
    WHERE subscription_id='$s_id'";

  display_debug_msg($query, $cdg_sql, "run_query_publication_subscription_detail()");
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}

?>