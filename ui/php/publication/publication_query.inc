<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_query.inc                                            //
//     - Desc : publication query File                                           //
// 2004-01-28 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Publication search query execution                                            //
// Parametes:
//   - $publication[]    : publication search criteria
//     keys used       archive, title, phone, type, zip
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($publication, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql;  

  $archive = $publication["archive"];
  $title = $publication["title"];
  $lang = $publication["lang"];
  $type = $publication["type"];
  $year = $publication["year"];
  $market = $publication[""];
  $zip = $publication["zip"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  // If a title has been given we search its sound too
  $req = "select
      publication_id as Id,
      publication_id,
      publication_title,
      publication_type_id,
      publication_year,
      publication_lang,
      publicationtype_label
    from Publication
         LEFT JOIN PublicationType on publication_type_id=publicationtype_id";

  $and = "";
  if ($title != "") {
    $where_title = " publication_title $like '$title%'";
    $and = "and";
  }
  if (($type != $c_all) && ($type != "")) { 
    $where .= " $and publication_type_id='$type'";
    $and = "and";
  }

  if ($year != "") {
    $where .= " $and publication_year $like '$year%'";
    $and = "and";
  }
  
  if ($lang != "") {
    $where .= " $and publication_lang $like '$lang%'";
    $and = "and";
  }

  if (($where != "") || ($where_title != "")) {
    $whereq = " where $where_title $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : " publication_year DESC,publication_title";

    
  $query = "$req $whereq order by $order $p_order_dir";

  display_debug_msg("publication_search(1) : $query", $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;

  $timeupdate = sql_date_format($type, "publication_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "publication_timecreate", "timecreate");

  $query = "select *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from Publication
         LEFT JOIN PublicationType on publication_type_id=publicationtype_id
         LEFT JOIN UserObm as c on publication_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on publication_userupdate=u.userobm_id
    where publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : num, archive, title, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($publication) {
  global $auth, $cdg_sql;

  $title = $publication["title"];
  $type = $publication["type"];
  $lang = $publication["lang"];
  $year = $publication["year"];
  $desc = $publication["desc"];
  
  $query = "insert into Publication (
    publication_timeupdate,
    publication_timecreate,
    publication_userupdate,
    publication_usercreate,
    publication_title,
    publication_type_id,
    publication_lang,
    publication_year,
    publication_desc)
  values (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    " . $auth->auth["uid"] . ",
    '$title',
    '$type',
    '$lang',
    '$year',
    '$desc'
    )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}
///////////////////////////////////////////////////////////////////////////////
// Subscription Insertion query execution                                                 //
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_subscription($publication) {
  global $auth, $cdg_sql;

  $c_id = $publication["contact_id"];
  $quantity = $publication["quantity"];
  $p_id = $publication["id"];
  $renew = $publication["renew"];
  $recept = $publication["recept"];
  
  $query = "insert into Subscription (
    subscription_timeupdate,
    subscription_timecreate,
    subscription_userupdate,
    subscription_usercreate,
    subscription_contact_id,
    subscription_publication_id,
    subscription_renewal_id,
    subscription_reception_id,
    subscription_quantity)
  values (null,
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
// Subscription Update query execution                                                 //
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_update_subscription($publication) {
  global $auth, $cdg_sql;

  $c_id = $publication["contact_id"];
  $quantity = $publication["quantity"];
  $p_id = $publication["id"];
  $renew = $publication["renew"];
  $recept = $publication["recept"];
  
  $query = "Update Subscription 
    set subscription_timeupdate = '". date("Y-m-d H:i:s") ."',
        subscription_userupdate='". $auth->auth["uid"] ."',
        subscription_renewal_id='$renew',
        subscription_reception_id='$recept',
        subscription_quantity='$quantity'
    Where	
        subscription_contact_id = '$c_id' and
	subscription_publication_id = '$p_id'";
    
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $cid       : publication id
//   - $publication[] : Entry's values
//     keys used  : num, archive, title, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
///////////////////////////////////////////////////////////////////////////////
function run_query_update($pid, $publication) {
  global $auth, $cdg_sql;

  $title = $publication["title"];
  $type = $publication["type"];
  $lang = $publication["lang"];
  $year = $publication["year"];
  $desc = $publication["desc"];

  $query = "update Publication set
    publication_timeupdate='". date("Y-m-d H:i:s") ."',
    publication_userupdate='". $auth->auth["uid"] ."',
    publication_title='$title',
    publication_lang='$lang',
    publication_year='$year',
    publication_desc='$desc',
    publication_type_id='$type'
    where publication_id='$pid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  $query = "delete from Publication where publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  run_query_delete_document($p_id,"Publication");
  $query = "delete from PublicationCategoryLink
    where publicationcategorylink_publication_id='$p_id'";
  $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Publication select query execution                                   //
// Returns : DB object result with all publications
///////////////////////////////////////////////////////////////////////////////
function run_query_active_publication() {
  global $cdg_sql;

  $query = "select publication_id, publication_title, publication_town
            from Publication
            where publication_archive='0'
            order by publication_title";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contacts attached to the publication
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_contact_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct contact_id, contact_lasttitle, contact_firsttitle 
            from Contact
            where contact_publication_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the deals attached to the publication
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_deal_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct deal_id, deal_label
    from Deal
    where deal_publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contracts attached to the publication
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_contract_links($p_id) {
  global $cdg_sql;

  $query = "select distinct contract_id, contract_label 
            from Contract, Contact
            where contract_publication_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
function run_query_check_publication($p_id, $title, $lang) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $like = sql_casei_like($type);

  $where_id = "";
  if ($p_id != "") {
    $where_id = "and publication_id!='$p_id'";
  }
  $query = "select distinct publication_id, publication_title, publication_lang
    from Publication
    where publication_title $like '$title%'and publication_lang like '$lang%'
          $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Type publication query execution                                              //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publicationtype() {
  global $cdg_sql;

  $query = "select * from PublicationType order by publicationtype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Renewal mode subscription query execution                                 //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_subscriptionrenewal() {
  global $cdg_sql;

  $query = "select * from SubscriptionRenewal order by subscriptionrenewal_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Reception mode subscription query execution                                 //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_subscriptionreception() {
  global $cdg_sql;

  $query = "select * from SubscriptionReception order by subscriptionreception_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Type insertion query construction and execution                           //
// Parameters:
//   - $publication : publication hash info : keys used : type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_type_insert($publication) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $type = $publication["type_label"];

  $query = "insert into PublicationType (
    publicationtype_timecreate,
    publicationtype_usercreate,
    publicationtype_label)
  values (
    '$timecreate',
    '$usercreate',
    '$type')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Renewal mode insertion query construction and execution                           //
// Parameters:
//   - $publication : publication hash info : keys used : renew_label
///////////////////////////////////////////////////////////////////////////////
function run_query_renew_insert($publication) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $renewal = $publication["renew_label"];

  $query = "insert into SubscriptionRenewal (
    subscriptionrenewal_timecreate,
    subscriptionrenewal_usercreate,
    subscriptionrenewal_label)
  values (
    '$timecreate',
    '$usercreate',
    '$renewal')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Reception mode insertion query construction and execution                           //
// Parameters:
//   - $publication : publication hash info : keys used : recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_recept_insert($publication) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $recept = $publication["recept_label"];

  $query = "insert into SubscriptionReception (
    subscriptionreception_timecreate,
    subscriptionreception_usercreate,
    subscriptionreception_label)
  values (
    '$timecreate',
    '$usercreate',
    '$recept')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Type update query execution                                               //
// Parameters:
//   - $publication : publication hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_type_update($publication) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $publication["type"];
  $type = $publication["type_label"];

  $query = "update PublicationType set
    publicationtype_label='$type',
    publicationtype_timeupdate='$timeupdate',
    publicationtype_userupdate='$userupdate'
      where
    publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Renewal update query execution                                               //
// Parameters:
//   - $publication : publication hash info : keys used : renew, renew_label
///////////////////////////////////////////////////////////////////////////////
function run_query_renew_update($publication) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $publication["renew"];
  $renew = $publication["renew_label"];

  $query = "update SubscriptionRenewal set
    subscriptionrenewal_label='$renew',
    subscriptionrenewal_timeupdate='$timeupdate',
    subscriptionrenewal_userupdate='$userupdate'
      where
    subscriptionrenewal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Reception update query execution                                               //
// Parameters:
//   - $publication : publication hash info : keys used : recept, recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_recept_update($publication) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $id = $publication["recept"];
  $recept = $publication["recept_label"];

  $query = "update SubscriptionReception set
    subscriptionreception_label='$recept',
    subscriptionreception_timeupdate='$timeupdate',
    subscriptionreception_userupdate='$userupdate'
      where
    subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type deletion query execution                                             //
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_type_delete($id) {
  global $cdg_sql;

  $query = "delete from PublicationType where publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Renewal deletion query execution                                          //
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_renew_delete($id) {
  global $cdg_sql;

  $query = "delete from SubscriptionRenewal where subscriptionrenewal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Reception deletion query execution                                        //
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_recept_delete($id) {
  global $cdg_sql;

  $query = "delete from SubscriptionReception where subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Type - Publication links query execution                                      //
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_type_links($p_id) {
  global $cdg_sql;

  $query = "select publication_title, publication_id, publication_type_id from Publication where publication_type_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Renew - Subscription links query execution                                      //
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_renew_links($p_id) {
  global $cdg_sql;

  $query = "select publication_title, publication_id, subscription_renewal_id, contact_firstname, contact_id  
            from (Subscription LEFT JOIN Publication ON publication_id = subscription_publication_id) 
	    LEFT JOIN Contact ON contact_id = subscription_contact_id
	    where subscription_renewal_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Reception - Subscription links query execution                                      //
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_recept_links($p_id) {
  global $cdg_sql;

  $query = "select publication_title, publication_id, subscription_reception_id, contact_firstname, contact_id  
            from (Subscription LEFT JOIN Publication ON publication_id = subscription_publication_id) 
	    LEFT JOIN Contact ON contact_id = subscription_contact_id
	    where subscription_reception_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Get the label of a Type from its id                                       //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_type_label($id) {
  global $cdg_sql;

  $query = "select publicationtype_label
    from PublicationType
    where publicationtype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("publicationtype_label");
  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Get the label of a Renewal Mode from its id                               //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_renew_label($id) {
  global $cdg_sql;

  $query = "select subscriptionrenewal_label
    from SubscriptionRenewal 
    where subscriptionrenewal_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("subscriptionrenewal_label");
  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Get the label of a Reception Mode from its id                             //
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_recept_label($id) {
  global $cdg_sql;

  $query = "select subscriptionreception_label
    from SubscriptionReception 
    where subscriptionreception_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("subscriptionreception_label");
  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Publication environment checking (same publications exists ?)                    //
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
  $co_q = run_query_check_publication($cid, $title, $lang);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting                                 //
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($cid, $publication) {
  global $l_fill_title, $l_j_check_year,$l_j_fill_lang ;
  global $err_msg;

  $title = $publication["title"];
  $year = $publication["year"];
  $lang = $publication["lang"];
  $desc = $publication["desc"];
  $type = $publication["type"];

  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }

  if (($year != "") && ($year < 1900 || $year > 2200)) {
    $err_msg = $l_j_check_year. " : $year";
    return false;
  }

  if ($lang == "") {
    $err_msg = $l_j_fill_lang;
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting                                 //
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_subscription_form($cid, $publication) {
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
// Contact detail query execution                                            //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_contact_lang($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
    $query = "select kind_lang from
        Contact LEFT JOIN Kind ON contact_kind_id = kind_id
        where
        contact_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("kind_lang");
}

///////////////////////////////////////////////////////////////////////////////
//                                       //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_subscription_external_search($hash_table) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;
  $contact = $hash_table["id"];
  $obm_q = new DB_OBM;
  $type = $obm_q->type;     
  $timeupdate = sql_date_format($type, "subscription_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "subscription_timecreate", "timecreate");

    $query = "
        SELECT
	      $timeupdate,$timecreate,subscription_contact_id,subscription_publication_id,
	      publication_title,publication_year, subscriptionrenewal_label,
	      subscriptionreception_label, subscription_quantity 
        FROM Subscription 
	LEFT JOIN Publication ON publication_id = subscription_publication_id
	LEFT JOIN SubscriptionRenewal ON subscriptionrenewal_id = subscription_renewal_id 
	LEFT JOIN SubscriptionReception ON subscriptionreception_id = subscription_reception_id 	
        where
        subscription_contact_id='$contact'
	Order by  subscription_timecreate desc";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}
///////////////////////////////////////////////////////////////////////////////
//                                       //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_subscription_detail($c_id,$p_id){
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;
  $obm_q = new DB_OBM;

    $query = "
        SELECT
	      subscription_contact_id,subscription_publication_id,
	      publication_title, subscription_renewal_id,
	      subscription_reception_id, subscription_quantity 
        FROM Subscription 
	LEFT JOIN Publication ON publication_id = subscription_publication_id
	LEFT JOIN SubscriptionRenewal ON subscriptionrenewal_id = subscription_renewal_id 
	LEFT JOIN SubscriptionReception ON subscriptionreception_id = subscription_reception_id 	
        where
        subscription_contact_id='$c_id' and subscription_publication_id='$p_id'
	";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}

</script>
