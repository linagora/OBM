<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_query.inc                                          //
//     - Desc : mailshare query File                                         //
// 2007-02-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Mailshare Search query execution 
// Parameters :
//   - $mailshare[] : mailshare search criteria
//     keys used    : name
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_search($mailshare) {
  global $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $user["sql_order_dir"];
  $sql_order_field = $user["sql_order_field"];

  $name = sql_search_text_parse($mailshare["name"]);
  $email = sql_search_text_parse($mailshare["email"]);
  $quota = sql_search_text_parse($mailshare["quota"]);
  $desc = sql_search_text_parse($mailshare["desc"]);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"mailsharedir_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"mailsharedir_timecreate", "timecreate");
  $multidomain = sql_multidomain("mailsharedir");

  $where = "(1=1)";
  if ($name != '') {
    $where .= " AND mailsharedir_name $like '$name%'";
  }
  if ($email != '') {
    $where .= " AND mailsharedir_email $like '%$email%'";
  }
  if ($quota > 0) {
    $where .= " AND mailsharedir_quota ='$quota'";
  }
  if ($desc != '') {
    $where .= " AND mailsharedir_description $like '%$desc%'";
  }
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "mailsharedir_name";
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT DISTINCT MailShareDir.*,
      mailsharedir_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM MailShareDir
      LEFT JOIN UserObm as A ON mailsharedir_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON mailsharedir_userupdate=B.userobm_id
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM MailShareDir $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_mailshare_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// MailShare detail query execution
// Parameters:
//   - $id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,"mailsharedir_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"mailsharedir_timecreate", "timecreate");
  $multidomain = sql_multidomain("mailsharedir");

  $query = "SELECT *,
      host_name as mailserver_hostname,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM MailShareDir
         LEFT JOIN MailServer ON mailsharedir_mail_server_id = mailserver_id
         LEFT JOIN Host ON mailserver_host_id = host_id
         LEFT JOIN UserObm as c ON mailsharedir_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON mailsharedir_userupdate=u.userobm_id
    WHERE mailsharedir_id = '$id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailshare_detail()");
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : mailshare insertion
// Parameters:
//   - $mailshare[] : mailshare hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_insert($mailshare) {
  global $cdg_sql, $obm;

  $name = $mailshare["name"];
  $quota = $mailshare["quota"];
  $email = $mailshare["email"];
  $desc = $mailshare["desc"];
  $mail_server = $mailshare["mail_server"];
  $now = date("Y-m-d H:i:s");
  $domain_id = $obm["domain_id"];

  $query = "INSERT INTO MailShareDir (
    mailsharedir_domain_id,
    mailsharedir_timeupdate,
    mailsharedir_timecreate,
    mailsharedir_userupdate,
    mailsharedir_usercreate,
    mailsharedir_name,
    mailsharedir_mail_server_id,
    mailsharedir_quota,
    mailsharedir_description,
    mailsharedir_email)
  VALUES (
    '$domain_id',
    null,
    '$now',
    null,
    '$obm[uid]',
    '$name',
    '$mail_server',
    '$quota',
    '$desc',
    '$email')";

  display_debug_msg($query, $cdg_sql, "run_query_mailshare_insert(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {

    // On recupere l'ID de l'hôte qu'on vient d'ajouter
    $query = "SELECT mailsharedir_id as id
    FROM MailShareDir
    WHERE
      mailsharedir_timecreate = '$now'
      AND mailsharedir_userupdate is null
      AND mailsharedir_usercreate = '$obm[uid]'
      AND mailsharedir_domain_id = '$domain_id'
      AND mailsharedir_name = '$name'
      AND mailsharedir_quota = '$quota'
      AND mailsharedir_email = '$email'
      AND mailsharedir_description = '$desc'";

    display_debug_msg($query, $cdg_sql, "run_query_mailshare_insert(2)");
    $obm_q->query($query);
    $obm_q->next_record();
    $id = $obm_q->f("id");

    return $id;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare Update query execution
// Parameters:
//   - $mailshare[] : mailshare hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_update($mailshare) {
  global $obm, $cdg_sql;

  $id = $mailshare["mailshare_id"];
  $name = $mailshare["name"];
  $quota = $mailshare["quota"];
  $email = $mailshare["email"];
  $desc = $mailshare["desc"];
  $mail_server = $mailshare["mail_server"];
  $now = date("Y-m-d H:i:s");
  $multidomain = sql_multidomain("mailsharedir");
  
  $query = "UPDATE MailShareDir set
    mailsharedir_timeupdate = '$now',
    mailsharedir_userupdate = '$obm[uid]',
    mailsharedir_name = '$name',
    mailsharedir_quota = '$quota',
    mailsharedir_mail_server_id = '$mail_server',
    mailsharedir_description = '$desc',
    mailsharedir_email = '$email'
  WHERE mailsharedir_id='$id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_mailshare_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_delete($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("mailsharedir");

  // Delete ACL for this mailshare
  $query = "DELETE FROM EntityRight
    WHERE entityright_entity='mailshare'
      AND entityright_entity_id='$p_id'
      $multidomain";
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_mailshare_delete(1)");
  $obm_q->query($query);

  // Delete the Mailshare
  $query = "DELETE FROM MailShareDir
    WHERE mailsharedir_id='$p_id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailshare_delete(2)");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Check if Mailshare (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : mailshare id to exclude
// Returns:
//   - true (if a mailshare exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_name_exists($name, $id="") {
  global $cdg_sql;

  $multidomain = sql_multidomain("mailsharedir");

  if ($id != "") {
    $where_id = "AND mailsharedir_id != '$id'";
  }

  $query = "SELECT mailsharedir_id, mailsharedir_name
    FROM MailShareDir
    WHERE
      mailsharedir_name = '$name'
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_mailshare_name_exists()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the mailshares which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : mailshare id
//   - $name : mailshare name
//   - $desc : mailshare desc
///////////////////////////////////////////////////////////////////////////////
function run_query_check_mailshare($id, $name, $desc) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain("mailsharedir");

  if ($id != "") {
    $where_id = "AND mailsharedir_id!='$id'";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "mailsharedir_name $like '%$name%'";
  } else {
    $wname = "mailsharedir_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != "") {
    if (strlen($desc) > 2) {
      $wdesc = "mailsharedir_description $like '%$desc%'";
    } else {
      $wdesc = "mailsharedir_description = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "OR $wdesc";

  $query = "SELECT DISTINCT mailsharedir_id,
      mailsharedir_name,
      mailsharedir_description
    FROM MailShareDir
    WHERE
      ($wname $wdesc)
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_check_mailshare()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare context checking (same mailshare exists ?)
// Parameters:
//   - $id          : mailshare id
//   - $mailshare[] : mailshare values
// Returns:
//   - Mailshare Database object with mailshare of similar mailshares
///////////////////////////////////////////////////////////////////////////////
function check_mailshare_context($id, $mailshare) {
  global $cdg_sql;

  $name = $mailshare["name"];
  $desc = $mailshare["desc"];

  // return the mailshares with same name or desc
  $m_q = run_query_check_mailshare($id, $name, $desc);

  return $m_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the mailshare can be deleted
// Parameters:
//   - $p_id : mailshare id
// Returns:
//   true if the mailshare can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_mailshare($p_id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare Form Data checking and formatting
// Parameters:
//   - $mailshare[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_mailshare_data_form($mailshare) {
  global $err, $l_err_mailshare_name_empty, $l_err_mailshare_name_exist;
  global $php_regexp_login, $l_name_error, $l_name_in_use;
  global $php_regexp_email_name, $php_regexp_quota, $l_err_mailshare_email;
  global $l_err_mailshare_email_empty, $l_err_mailshare_email_exist;
  global $l_err_mailshare_uemail_exist, $l_err_mailshare_quota;

  $id = $mailshare["mailshare_id"];
  $name = $mailshare["name"];
  $quota = $mailshare["quota"];
  $email = $mailshare["email"];
  $desc = $mailshare["desc"];

  // MANDATORY: Mailshare name not empty
  if (trim($name) == "") {
    $err["msg"] = $l_err_mailshare_name_empty;
    $err["field"] = "name";
    return false;
  } else {
    if (! preg_match($php_regexp_login, $name)) {
      $err["msg"] .= "$name : $l_name_error";
      $err["field"] = "name";
      return false;
    }
    // Name not already in use
    if (name_in_use($name, $id)) {
      $err["msg"] = "$name : $l_name_in_use";
      $err["field"] = "name";
      return false;
    }
  }

  // MANDATORY: Host name unique
  if (get_mailshare_name_exists($name, $id)) {
    $err["msg"] = "$l_err_mailshare_name_exist ($name)";
    $err["field"] = "name";
    return false;
  }

  // If quota set, must be numerical
  if ($quota != "") {
    if (! preg_match($php_regexp_quota, $quota)) {
      $err["msg"] = " $quota : $l_err_mailshare_quota";
      $err["field"] = "quota";
      return false;
    }
  }

  // MANDATORY: Email filled
  if (trim($email) == "") {
    $err["msg"] = "$l_err_mailshare_email_empty";
    $err["field"] = "email";
    return false;
  }

  // User email addresses (only the name before @)
  if ($email != "") {
    $mails = get_email_used("", "", $id);
    $um = array();

    // All email addresses given are valid and not already used
    $em = strtok($email, "\r\n");
    while ($em) {
      if (! preg_match($php_regexp_email_name, $em)) {
        $err["msg"] .= "$em : $l_err_mailshare_email";
	$err["field"] = "email";
        return false;
      }
      // Email address not already used
      if (array_key_exists($em, $mails)) {
	$err["msg"] = "$l_err_mailshare_email_exist : $em (". $mails["$em"] .")";
	$err["field"] = "email";
        return false;
      }
      // Email address not already used by this user
      if (in_array($em, $um)) {
	$err["msg"] = "$l_err_mailshare_uemail_exist : $em";
	$err["field"] = "email";
        return false;
      }
      array_push($um, $em);
      $em = strtok("\r\n");
    }
  }
 
  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Check if a login is present in the user table
// Parameters:
//   - $login : Login to test
//   - $u_id  : user Id to ignore (not to test current user in update)
// Returns:
//   - true if found, else false
///////////////////////////////////////////////////////////////////////////////
function name_in_use($name, $u_id="") {
  global $cdg_sql;

  $multidomain = sql_multidomain("mailsharedir");

  $query = "SELECT
    mailsharedir_id,
    mailsharedir_name
  FROM MailShareDir
  WHERE mailsharedir_name = '$name'
    AND mailsharedir_id != '$u_id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "name_in_use()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}
///////////////////////////////////////////////////////////////////////////////
// Available mailservers
// Parameters:
///////////////////////////////////////////////////////////////////////////////

function run_query_mailshare_get_mailserver() {
  global $cdg_sql;

  $multidomain = sql_multidomain("host",true,true);
  $query = "SELECT
    mailserver_id,
    ms.host_name as host_name
    FROM MailServer 
    JOIN Host ms ON mailserver_host_id = ms.host_id
    WHERE 1=1 $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailshare_get_mailserver())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;    
}
</script>
