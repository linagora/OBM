<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_display.php                                        //
//         - Desc : Mailshare Display File                                   //
// 2007-02-02 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["mailsharedir_name"] = $l_name;
$fieldnames["mailsharedir_email"] = $l_email;
$fieldnames["mailsharedir_quota"] = $l_quota;
$fieldnames["mailsharedir_description"] = $l_desc;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;


///////////////////////////////////////////////////////////////////////////////
// Display MailShare specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_mailshare(&$OD, $fieldname, $link_ok) {
  global $path, $ico_mail;

  if (($fieldname == "mailsharedir_name") && $link_ok) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("mailsharedir_id").",'".addslashes($OD->data_set->f("mailsharedir_name"))."');";
    } else {
     $res["url"] = "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=".$OD->data_set->f("mailsharedir_id");
    }
  }

  else if ($fieldname == "mailsharedir_quota") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname) == "0") {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = "";
    }    
  }

  else if ($fieldname == "mailsharedir_email") {
    $mail = get_mail_info();
    $main_domain = $mail["main_domain"];

    $email = $OD->data_set->f("mailsharedir_email");
    if (strcmp($email ,"") != 0) {
      $emails = explode("\n",$email);
      $email = trim($emails[0])."@$main_domain";
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\"/>$email";
      $res["txt_name"] = "$email";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare search Form
// Parameters : 
//   - $mailshare[]   : default form values
//     keys used : name, user
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_search_form($mailshare) {
  global $display, $l_name, $l_email, $l_quota, $l_desc, $l_find;

  $popup = $mailshare["popup"];

  if ($popup) {
    $ext_action = $mailshare["ext_action"];
    $ext_target = $mailshare["ext_target"];
    $ext_widget = $mailshare["ext_widget"];
    $ext_widget_text = $mailshare["ext_widget_text"];
    $ext_url = $mailshare["ext_url"];
    $ext_id = $mailshare["ext_id"];
    $ext_title = stripslashes($mailshare["ext_title"]);
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == "") {
      $ext_title = $l_add_mailshares;
    }
    $display["title"] = "<h1 class=\"title\">$ext_title</h1>";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($mailshare["name"]);
  $quota = stripslashes($mailshare["quota"]);
  $email = stripslashes($mailshare["email"]);
  $desc = stripslashes($mailshare["description"]);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"".url_prepare("mailshare_index.php")."\">

  <label>$l_name<br />
    <input name=\"tf_name\" size=\"24\" maxlength=\"32\" value=\"$name\" />
  </label>
  <label>$l_email<br />
    <input type=\"text\" name=\"tf_email\" size=\"24\" maxlength=\"32\" value=\"$email\" />
  </label>
  <label>$l_quota<br />
    <input name=\"tf_quota\" size=\"8\" maxlength=\"8\" value=\"$quota\" />
  </label>
  <label>$l_desc<br />
    <input name=\"tf_desc\" size=\"24\" maxlength=\"32\" value=\"$desc\" />
  </label>
  <label>&nbsp;<br />
    <input name=\"action\" type=\"hidden\" value=\"search\" />
    <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    $ext
  </label>
  <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Mailshare search result
// Parameters:
//   - $mailshare[]   : mailshare search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_search_list($mailshare) {
  global $l_no_found, $display, $obm;

  $prefs = get_display_pref($obm["uid"], "mailshare");
  $obm_q = run_query_mailshare_search($mailshare);

  $nb_mailshare = $obm_q->num_rows_total();
  if ($nb_mailshare == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_mailshare_search_list($obm_q, $prefs, $nb_mailshare, $mailshare);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Mailshare search result
// Parameters : 
//   - $obm_q     : DBO : mailshare list
//   - $pref_q    : fields that have to be displayed
//   - $nb_mailshare   : nb mailshares returned by the search query 
//   - $group[]   : mailshare search criteria
//     keys used  : name, ip, popup
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_search_list($obm_q, $pref_q, $nb_mailshare, $mailshare) {
  global $display, $l_found, $l_close, $module;

  $popup = $mailshare["popup"];

  if ($popup) {
    $ext_action = $mailshare["ext_action"];
    $ext_url = $mailshare["ext_url"];
    $ext_target = $mailshare["ext_target"];
    $ext_widget = $mailshare["ext_widget"];
    $ext_widget_text = $mailshare["ext_widget_text"];
    $ext_id = $mailshare["ext_id"];
    $ext_title = urlencode(stripslashes($mailshare["ext_title"]));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_title&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  $name = urlencode(stripslashes($mailshare["name"]));
  $email = urlencode(stripslashes($mailshare["email"]));

  $url = url_prepare("mailshare_index.php?action=search&amp;tf_name=$name&amp;tf_email=$email$url_ext");
  
  $dis_mailshare = new OBM_DISPLAY("DATA", $pref_q, $module);
  
  if ($popup) {
    if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $dis_mailshare->display_ext = "get_id";
    }
    $dis_mailshare->data_form_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_mailshare->data_set = $obm_q;
  $dis_mailshare->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_ok_msg("$nb_mailshare $l_found");
  $block .= $dis_mailshare->display("dis_data_mailshare");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare Consultation
// Parameters:
//   - $mailshare[] : mailshare parameters
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_consult($mailshare) {

  $mailshare_q = run_query_mailshare_detail($mailshare["mailshare_id"]);
  $block = html_mailshare_consult($mailshare_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Mailshare Consultation
// Parameters:
//   - $mailshare_q : mailshare database result 
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_consult($mailshare_q) {
  global $c_yes, $c_no;
  global $l_mailshare, $l_name, $l_quota, $l_desc, $l_email;
  global $display, $perm, $menu, $action, $cright_write;

  $name = $mailshare_q->f("mailsharedir_name");
  $quota = $mailshare_q->f("mailsharedir_quota");
  $desc = $mailshare_q->f("mailsharedir_description");
  $email = $mailshare_q->f("mailsharedir_email");

  $display["detailInfo"] = display_record_info($mailshare_q);
  $display["title"] = "<div class=\"title\">$l_mailshare : $name</div>";

  $block = "
  <div class=\"detail extra\">
  <h1>$l_mailshare</h1>

  <table>
  <tr>
    <th>$l_name</th>
    <td>$name</td>
  </tr>
  <tr>
    <th>$l_quota</th>
    <td>$quota</td>
  </tr>
  <tr>
    <th>$l_email</th>
    <td>$email</td>
  </tr>
  <tr>
    <th>$l_desc</th>
    <td>$desc</td>
  </tr>
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare Form
// Parameters :
//   - $action      : action called
//   - $mailshare_q : DBO : information about the mailshare (null for new mailshare)
//   - $mailshare[] : default or transmitted values
//   - $field       : field in error
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_form($action, $mailshare_q, $mailshare, $field="") {
  global $display, $l_insert, $l_update, $l_back,$l_web_list;
  global $l_mailshare, $l_name, $l_desc, $l_quota, $l_email;
  global $cgp_use, $cmailshare_default_quota;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $mailshare_q->f("mailsharedir_id");
    $name = $mailshare_q->f("mailsharedir_name");
    $quota = $mailshare_q->f("mailsharedir_quota");
    $email = $mailshare_q->f("mailsharedir_email");
    $desc = $mailshare_q->f("mailsharedir_description");

  } else {
    // Values are taken from parameters
    $id = $mailshare["mailshare_id"];
    $name = stripslashes($mailshare["name"]);
    $quota = $mailshare["quota"];
    $email = $mailshare["email"];
    $desc = stripslashes($mailshare["desc"]);
    if ($action == "new") {
      $quota = $cmailshare_default_quota;
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($mailshare["name"])) { $name = $mailshare["name"]; }
  if (isset($mailshare["quota"])) { $quota = $mailshare["quota"]; }
  if (isset($mailshare["email"])) { $email = $mailshare["email"]; }

  $class_name = ($field == "name") ? "detailError" : "detailLabel";
  $class_quota = ($field == "quota") ? "detailError" : "detailLabel";
  $class_email =  ($field == "email") ? "detailError" : "detailLabel";

  $mail = get_mail_info();
  $main_domain = $mail["main_domain"];

  $display["detailInfo"] = display_record_info($mailshare_q);
  $display["title"] = "<div class=\"title\">$l_mailshare : $name</div>";

  if (($action == "new") || ($action == "insert")) {
    $dis_name = "<input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";

    $dis_button .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($action == "detailupdate") || ($action == "update")) {
    $dis_name = "$name <input name=\"tf_name\" type=\"hidden\" value=\"$name\" />";

    $dis_button = "
      <input type=\"hidden\" name=\"mailshare_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";
  }

  $block = "
  <form method=\"post\" name=\"f_mailshare\" action=\"".url_prepare("mailshare_index.php")."\">

  <fieldset class=\"detail extra\">
  <legend>$l_mailshare</legend>

  <table>
  <tr>
    <th>$l_name</th>
    <td>$dis_name</td>
  </tr>
  <tr>
    <th>$l_email</th>
    <td><textarea name=\"ta_email\" cols=\"32\" rows=\"3\">$email</textarea>@$main_domain</td>
  </tr>
  <tr>
    <th>$l_quota</th>
    <td><input name=\"tf_quota\" maxlength=\"8\" size=\"8\" value=\"$quota\" />M</td>
  </tr>
  <tr>
    <th>$l_desc</th>
    <td><input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"128\" /></td>
  </tr>
  </table>

  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Mailshare Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_display_pref($prefs) {
  global $l_mailshare_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
  $dis_pref->display_entity = "mailshare"; 
  $dis_pref->pref_title = $l_mailshare_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the mailshare delete validation screen
// Parameters:
//   - $p_id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_mailshare($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"get\"
    action=\"".url_prepare("mailshare_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"mailshare_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"form\"
      action=\"" . url_prepare("mailshare_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"mailshare_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
  <fieldset class=\"buttons\">
  $dis_delete
  $dis_back
  </fieldset>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a mailshare insertion or update
// When similar mailshares exist we show these and ask confirmation
// Parameters:
//   - $id       : mailshare id
//   - $h_q      : mailshare database result (at least 1 row)
//   - $mailshare[]  : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_mailshare_warn_insert($id, $h_q, $mailshare) {
  global $display, $l_check_samemailshare, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $mailshare["name"];
  $desc = $mailshare["desc"];
  $email = $mailshare["email"];
  $quota = $mailshare["quota"];

  $display["msg"] .= display_warn_msg($l_check_samemailshare);
  while ($h_q->next_record()) {
    $id = $h_q->f("mailsharedir_id");
    $samename = $h_q->f("mailsharedir_name");
    $samedesc = $h_q->f("mailsharedir_description");
    $dis_same_mailshare .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("mailshare_index.php?action=detailconsult&amp;mailshare_id=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_mailshare
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("mailshare_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"ta_email\" value=\"$email\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"hidden\" name=\"tf_quota\" value=\"$quota\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("mailshare_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"ta_email\" value=\"$email\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"hidden\" name=\"tf_quota\" value=\"$quota\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  return $block;
}

</script>
