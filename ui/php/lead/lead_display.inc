<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : lead_index.php                                               //
//     - Desc : lead display File                                            //
// 2006-05-19 Aliacom - PB
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["lead_name"] = $l_name;
$fieldnames["leadsource_label"] = $l_source;
$fieldnames["date"] = $l_date;
$fieldnames["datealarm"] = $l_alarm;
$fieldnames["company_name"] = $l_company;
$fieldnames["company_zipcode"] = $l_postcode;
$fieldnames["lead_todo"] = $l_todo;
$fieldnames["manager"] = $l_manager;


///////////////////////////////////////////////////////////////////////////////
// Display Lead specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_lead(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_frs, $col_client;

  if ($fieldname == "lead_name") {
    $name = $OD->data_set->f($fieldname);
    $res["url"] = "$path/lead/lead_index.php?action=detailconsult&amp;lead_id=".$OD->data_set->f("lead_id");
    $res["name"] = $name;
  }

  elseif ($fieldname == "lead_comment") {
    $res["url"] = "$path/lead/lead_index.php?action=detailconsult&amp;lead_id=".$OD->data_set->f("lead_id");
  }

  elseif ($fieldname == "company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f("lead_company_id");
  }

  else if ($fieldname == "lead_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Lead search form
// Parameters:
//   - $params[] : hash with parameters values
///////////////////////////////////////////////////////////////////////////////
function dis_lead_search_form($params="") {
  global $cgp_hide;

  $sources = get_global_lead_sources();
  $managers = get_lead_managers();
  $block .= html_lead_search_form($params, $sources, $managers);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Lead search Form
// Parameters:
//   - $params[] : default form values
//   - $sources  : sources infos
//   - $managers : managers infos
///////////////////////////////////////////////////////////////////////////////
function html_lead_search_form($params, $sources, $managers) {
  global $l_find, $l_all, $c_all;
  global $l_text, $l_manager, $l_source, $l_company;
  global $l_date, $l_after, $l_before;
  global $l_both, $l_received, $l_emitted;

  $source_id = $params["source"];
  $manager_id = $params["manager"];
  $company = $params["company"];
  $archive = ($params["archive"] == "1" ? "checked = \"checked\"" : "");
  $text = stripslashes($params["text"]);
  $date_after = $params["date_after"];
  $date_before = $params["date_before"];

  // Sources select
  $sel_source = "
    <select name=\"sel_source\">
     <option value=\"$c_all\">$l_all</option>\n";
  if (is_array($sources) && (count($sources) > 0)) {
    foreach($sources as $s_id => $one_source) {
      $slabel = $one_source["label"];
      $sselect = ($s_id == $source_id) ? "selected" : "";
      $sel_source .= "<option value=\"$s_id\" $sselect>$slabel</option>";
    }
  }
  $sel_source .= "</select>";

  // Manager select
  $sel_manager = "
    <select name=\"sel_manager\">
     <option value=\"$c_all\">$l_all</option>";
  if (is_array($managers) && (count($managers) > 0)) {
    foreach($managers as $m_id => $one_manager) {
      $mname = $one_manager["name"];
      $mselect = ($m_id == $manager_id) ? "selected" : "";
      $sel_manager .= "<option value=\"$m_id\" $mselect>$mname</option>";
    }
  }
  $sel_manager .= "</select>";

  $block = "
    <form method=\"get\" name=\"f_search\" action=\"lead_index.php?action=search\">
     <div class=\"search\">

      <div class=\"searchLabel\">$l_text<br />
       <input name=\"tf_text\" size=\"24\" value=\"$text\">
      </div>

      <div class=\"searchLabel\">$l_company<br />
       <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
      </div>

      <div class=\"searchLabel\">$l_date $l_after<br />
        <script>calendar('tf_date_after','$date_after')</script>
      </div>

      <div class=\"searchLabel\">$l_date $l_before<br />
        <script>calendar('tf_date_before','$date_before')</script>
      </div>

      <div class=\"searchLabel\">$l_source<br />
       $sel_source
      </div>

      <div class=\"searchLabel\">$l_manager<br />
       $sel_manager
      </div>

      <div class=\"searchLabel\"><br />
       <input type=\"hidden\" name=\"action\" value=\"search\">
       <input type=\"submit\" name=\"submit\" value=\"$l_find\">  
      </div>

      <div class=\"searchEnd\"></div>
     </div>
    </form>
";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Lead search result
// Parameters:
//   - $params[] : params (search criteria)
///////////////////////////////////////////////////////////////////////////////
function dis_lead_search_list($params) {
  global $display, $l_found, $l_no_found;
  global $auth;

  $obm_q = run_query_lead_search($params);
  $nb = $obm_q->num_rows_total();
  if ($nb == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb $l_found");
    $prefs = get_display_pref($auth->auth["uid"], "lead");
    $block = html_lead_search_list($obm_q, $prefs, $params);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Lead search result
// Parameters : 
//   - $obm_q     : list of leads to display 
//   - $prefs     : the fields which have to be displayed
//   - $params[]  : parameters (search criteria)
///////////////////////////////////////////////////////////////////////////////
function html_lead_search_list($obm_q, $prefs, $params) {
  global $l_close;

  $popup = $params["popup"];

  $text = urlencode($params["text"]);
  $company = $params["company"];
  $date_after = $params["date_after"];
  $date_before = $params["date_before"];
  $source = $params["source"];
  $manager = $params["manager"];
  $archive = $params["archive"];

  if ($popup) {
    $ext_action = $params["ext_action"];
    $ext_url = $params["ext_url"];
    $ext_id = $params["ext_id"];
    $ext_target = $params["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  $url = url_prepare("lead_index.php?action=search".
		    "&amp;tf_text=$text".
		    "&amp;tf_company=$company".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_date_after=$date_after".
		    "&amp;sel_source=$source".
		    "&amp;sel_manager=$manager".
		    "$url_ext");

  $dis_p = new OBM_DISPLAY("DATA", $prefs, "lead");
  if ($popup) {
    $dis_p->display_link = false;
    $dis_p->data_cb_text = "X";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $dis_p->data_set = $obm_q;
  $dis_p->data_url = $url;
  $dis_p->data_header = "both";

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_p->display("dis_data_lead");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Lead detail
// Parameters:
//   - $params[] : lead values (id)
///////////////////////////////////////////////////////////////////////////////
function dis_lead_consult($params) {
  global $display, $l_no_found, $path, $l_query_error;

  $view = $params["view"];
  $id = $params["lead_id"];

  if ($id > 0) {
    $l_q = run_query_lead_detail($id);
    $display["detailInfo"] = display_record_info($l_q);
    $block = html_lead_consult($l_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Lead Consultation
// Parameters:
//   - $l_q  : lead database result
///////////////////////////////////////////////////////////////////////////////
function html_lead_consult($l_q) {
  global $path, $display, $set_theme, $ico_company, $c_yes, $c_no;
  global $l_company, $l_comment;
  global $l_lead, $l_source, $l_manager, $l_name, $l_date, $l_alarm, $l_todo;

  $id = $l_q->f("lead_id");
  $c_id = $l_q->f("lead_company_id");
  $c_name = $l_q->f("company_name");
  $source = $l_q->f("leadsource_label");
  $manager = $l_q->f("manager");
  $date = date_format($l_q->f("date"));
  $datealarm = date_format($l_q->f("datealarm"));
  $name = $l_q->f("lead_name");
  $archive = ($l_q->f("lead_archive") == 1 ? $c_yes : $c_no);
  $todo = $l_q->f("lead_todo");
  $comment = beautify_comment(nl2br($l_q->f("lead_comment")));
  $ad1 = $l_q->f("company_address1");
  $zip = $l_q->f("company_zipcode");
  $town = $l_q->f("company_town");

  $display["title"] = "<div class=\"title\">$l_lead : $date $c_name</div>";

  $block = "
<div class=\"detailHead\">$title</div>

  <div class=\"detailHead\">$l_lead</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company 
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
      </td>
      <td class=\"detailText\">$c_name<br />$ad1<br/>$zip $town</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_source</td>
      <td class=\"detailText\">$source</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_manager</td>
      <td class=\"detailText\">$manager</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_alarm</td>
      <td class=\"detailText\">$datealarm</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_todo</td>
      <td class=\"detailText\">$todo</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailText\">$comment</td>
    </tr>
   </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Lead Form
// Parameters:
//   - $action    : action called
//   - $params[]  : parameters : default values
///////////////////////////////////////////////////////////////////////////////
function dis_lead_form($action, $params) {
  global $display, $uid, $cg_com;

  $p_id = $params["lead_id"];
  if ($p_id > 0) {
    $l_q = run_query_lead_detail($p_id);
    $users = array($uid, $l_q->f("lead_manager_id"));
  } else {
    $users = array($uid);
  }

  $sources = get_global_lead_sources();
  $managers = get_all_users_from_group($cg_com, $users);

  $block = html_lead_form($action, $l_q, $sources, $managers, $params);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Lead Form
// Parameters:
//   - $action    : action called
//   - $l_q       : lead database result
//   - $sources   : Lead sources array 
//   - $managers  : Managers array 
//   - $params[]  : parameters : form values
///////////////////////////////////////////////////////////////////////////////
function html_lead_form($action, $l_q, $sources, $managers, $params) {
  global $display, $path, $auth, $c_undef, $l_undef;
  global $set_theme, $ico_company, $l_company,$ico_add,$ico_crow;
  global $l_lead, $l_source, $l_manager, $l_name, $l_date, $l_alarm, $l_todo;
  global $l_update, $l_checkdelete, $l_insert;
  global $l_comment,$l_add_comment,$l_upd_comment, $l_mail_comment;
  global $l_no, $l_members, $l_group;
  global $popup_width, $popup_height, $l_header_new, $ico_mini_cal;

  $uid = $auth->auth["uid"];

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $id = $l_q->f("lead_id");
    $source_id = $l_q->f("lead_source_id");
    $manager_id = $l_q->f("lead_manager_id");
    $c_id = $l_q->f("lead_company_id");
    $c_name = $l_q->f("company_name");
    $name = $l_q->f("lead_name");
    $date = $l_q->f("date");
    $date = ($date != 0) ? date_format($date, 1) : "";
    $datealarm = $l_q->f("datealarm");
    $datealarm = ($datealarm != 0) ? date_format($datealarm, 1) : "";
    $todo = $l_q->f("lead_todo");
    $archive = $l_q->f("lead_archive");
    $comment = $l_q->f("lead_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();

  // New form and first time
  } elseif ($action == "new") {
    $title = $l_header_new;
    $date = date("Y-m-d");
    $usercomment = $uid;
    $datecomment = isodate_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($params["company_id"])) { $c_id = $params["company_id"]; }
  if (isset($params["company_name"])) { $c_name = $params["company_name"]; }
  if (isset($params["comp_new_id"])) { $c_new_id = $params["comp_new_id"]; }
  if (isset($params["comp_new_name"])) { $c_new_name = $params["comp_new_name"]; }
  if (isset($params["lead_id"])) { $id = $params["lead_id"]; }
  if (isset($params["source"])) { $source_id = $params["source"]; }
  if (isset($params["manager"])) { $manager_id = $params["manager"]; }
  if (isset($params["name"])) { $name = $params["name"]; }
  if (isset($params["date"])) { $date = $params["date"]; }
  if (isset($params["datealarm"])) { $datealarm = $params["datealarm"]; }
  if (isset($params["todo"])) { $todo = stripslashes($params["todo"]); }
  if (isset($params["comment"])) { $comment = stripslashes($params["comment"]); }
  if (isset($params["add_comment"])) { $add_comment = stripslashes($params["add_comment"]); }
  if (isset($params["usercomment"])) { $usercomment = $params["usercomment"]; }
  if (isset($params["datecomment"])) { $datecomment = $params["datecomment"]; }
  if (isset($params["mail_comment"])) { $mail_comment = $params["mail_comment"]; }


  // Lead kind field
  $block_source = of_category_dis_entity_form("lead", "source", $sources, "mono", $source_id, "none");

  // Manager select
  $sel_manager = "
    <select name=\"sel_manager\">
     <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($managers) && (count($managers) > 0)) {
    foreach($managers as $m_id => $one_manager) {
      $mname = $one_manager["lastname"] . " " . $one_manager["firstname"];
      $mselect = ($m_id == $manager_id) ? "selected" : "";
      $sel_manager .= "<option value=\"$m_id\" $mselect>$mname</option>";
    }
  }
  $sel_manager .= "</select>";

  // Company Display
  $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"". C_IMAGE_PATH . "/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";

  // User comment select construction
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  if (is_array($managers) && (count($managers) > 0)) {
    foreach($managers as $cid => $one_manager) {
      $cname = $one_manager["lastname"] . " " . $one_manager["firstname"];
      $cselect = "";
      if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
	$cselect = " selected = \"selected\"";
      }
      $sel_usercomment .= "<option value=\"$cname\" $cselect>$cname</option>";
    }
  }
  $sel_usercomment .= "</select>";

  // Mail comment radio
  if (($mail_comment == "") || ($mail_comment == "$l_no")) {
    $rd_mail_no_c = "checked";
  } else if ($mail_comment == "$l_members") {
    $rd_mail_members_c = "checked";
  } else if ($mail_comment == "$l_group") {
    $rd_mail_group_c = "checked";
  }

  // UPDATE
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      <tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea></td>
      </tr>";
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"lead_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display["title"] = "<div class=\"title\">$l_lead : $title</div>";

  // --- HTML Template --------------------------------------------------------
  $block .= "
    <form method=\"get\" name=\"f_entity\" onsubmit=\"if (check_lead(this)) return true; else return false;\" action=\"".url_prepare("lead_index.php")."\">

    <div class=\"detailHead\">$l_lead</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailForm\">$dis_company</td>
    </tr>
    $block_source
    <tr>
      <td class=\"detailLabel\">$l_manager</td>
      <td class=\"detailForm\">$sel_manager</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_name\" size=\"32\" maxlength=\"64\" value=\"$name\" /></td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailForm\">
        <script>calendar('tf_date','$date')</script>
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_alarm</td>
      <td class=\"detailForm\">
        <script>calendar('tf_datealarm','$datealarm')</script>
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_todo</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_todo\" size=\"32\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\">
        <script>calendar('tf_datecomment','$datecomment')</script>$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_mail_comment</td>
      <td class=\"detailForm\">
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group
      </td>
    </tr>
    <tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
    </tr>
    $dis_comment
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the lead can be deleted, and the form
// Parameters:
//   - $p_id : lead id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_lead($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back;

  $url = url_prepare("lead_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"lead_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"lead_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);
  $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_delete</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the lead administration index
///////////////////////////////////////////////////////////////////////////////
function dis_lead_admin_index() {
  global $cgp_hide;

  $sources = of_category_get_ordered("lead", "source");
  $block .= "<hr />";
  $block .= of_category_dis_admin_form("lead", "source", $sources);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Lead Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_lead_display_pref ($prefs) {
  global $l_lead_display;
  global $set_theme ;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "lead");
  $dis_pref->pref_title = $l_lead_display;
  $dis_pref->pref_dis_help = 1;

  $block .= $dis_pref->display();

  return $block;
}


?>