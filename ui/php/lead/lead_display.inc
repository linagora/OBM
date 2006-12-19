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
    <form method=\"get\" name=\"f_search\" action=\"lead_index.php?action=search\" class=\"search\">

      <label>$l_text<br />
       <input name=\"tf_text\" size=\"24\" value=\"$text\">
      </label>

      <label>$l_company<br />
       <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
      </label>

      <label>$l_date $l_after<br />
        <input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
      </label>

      <label>$l_date $l_before<br />
         <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
      </label>

      <label>$l_source<br />
       $sel_source
      </label>

      <label>$l_manager<br />
       $sel_manager
      </label>

      <label><br />
       <input type=\"hidden\" name=\"action\" value=\"search\">
       <input type=\"submit\" name=\"submit\" value=\"$l_find\">  
      </label>
    <p class=\"CL\" />
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
  global $display, $l_no_found, $path, $l_err_reference;

  $view = $params["view"];
  $id = $params["lead_id"];

  if ($id > 0) {
    $l_q = run_query_lead_detail($id);
    $display["detailInfo"] = display_record_info($l_q);
    $block = html_lead_consult($l_q);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
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
  $source_label = $l_q->f("leadsource_label");
  $manager = $l_q->f("manager");
  $date = of_date_format($l_q->f("date"));
  $datealarm = of_date_format($l_q->f("datealarm"));
  $name = $l_q->f("lead_name");
  $archive = ($l_q->f("lead_archive") == 1 ? $c_yes : $c_no);
  $todo = $l_q->f("lead_todo");
  $comment = beautify_comment(nl2br($l_q->f("lead_comment")));
  $ad1 = $l_q->f("company_address1");
  $zip = $l_q->f("company_zipcode");
  $town = $l_q->f("company_town");

  $display["title"] = "<div class=\"title\">$l_lead : $date $c_name</div>";

  $block = "
<h1>$title</h1>
<div class=\"detail infos\"> 
  <h1>$l_lead</h1>
    <table class=\"detail\">
    <tr>
      <th>$l_company 
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
      </th>
      <td>$c_name<br />$ad1<br/>$zip $town</td>
    </tr>
    <tr>
      <th>$l_source</th>
      <td>$source_label</td>
    </tr>
    <tr>
      <th>$l_manager</th>
      <td>$manager</td>
    </tr>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>
    <tr>
      <th>$l_date</th>
      <td>$date</td>
    </tr>
    <tr>
      <th>$l_alarm</th>
      <td>$datealarm</td>
    </tr>
    <tr>
      <th>$l_todo</th>
      <td>$todo</td>
    </tr>
    </table>
</div>

<div class=\"detail extra\">

  <h1>$l_comment</h1>

   $comment
</div>
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
    $date = ($date != 0) ? of_date_format($date, 1) : "";
    $datealarm = $l_q->f("datealarm");
    $datealarm = ($datealarm != 0) ? of_date_format($datealarm, 1) : "";
    $todo = $l_q->f("lead_todo");
    $archive = $l_q->f("lead_archive");
    $comment = $l_q->f("lead_comment");
    $usercomment = $uid;
    $datecomment = of_isodate_format();

  // New form and first time
  } elseif ($action == "new") {
    $title = $l_header_new;
    $date = date("Y-m-d");
    $usercomment = $uid;
    $datecomment = of_isodate_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($params["company_id"])) { $c_id = $params["company_id"]; }
  if (isset($params["company_name"])) { $c_name = $params["company_name"]; }
  if (isset($params["company_new_id"])) { $c_new_id = $params["company_new_id"]; }
  if (isset($params["company_new_name"])) { $c_new_name = $params["company_new_name"]; }
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
        <th>$l_upd_comment</th>
        <td>&nbsp;</td>
      </tr><tr>
        <td colspan=\"2\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea></td>
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

<fieldset class=\"detail infos\">
    <legend>$l_lead</legend>
    <table>
    <tr>
      <th>$l_company</th>
      <td>$dis_company</td>
    </tr>
    $block_source
    <tr>
      <th>$l_manager</th>
      <td>$sel_manager</td>
    </tr>
    <tr>
      <th>$l_name</th>
      <td><input type=\"text\" name=\"tf_name\" size=\"32\" maxlength=\"64\" value=\"$name\" /></td>
    </tr>
    <tr>
      <th>$l_date</th>
     <td>
       <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
     </td>
    </tr>
    <tr>
      <th>$l_alarm</th>
      <td>
       <input type=\"text\" name=\"tf_datealarm\" value=\"$datealarm\" class=\"datePicker\"/>
      </td>
    </tr>
    <tr>
      <th>$l_todo</th>
      <td><input type=\"text\" name=\"tf_todo\" size=\"32\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr>
    </table>
</fieldset>

<fieldset class=\"detail extra\">
  <legend>$l_comment</legend>
      $l_add_comment
      <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
      $sel_usercomment
      $l_mail_comment
      
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group
      
   <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>
    $dis_comment


    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>
</fieldset>";

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
      <div class=\"buttons\">
        $dis_delete
        $dis_back
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the lead administration index
///////////////////////////////////////////////////////////////////////////////
function dis_lead_admin_index() {
  global $cgp_hide;

  $sources = of_category_get_ordered("lead", "source");
  $block .= of_category_dis_admin_form("source", $sources);

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
