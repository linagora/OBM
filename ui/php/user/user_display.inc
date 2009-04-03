<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["userobm_login"] = $l_login;
$fieldnames["userobm_system"] = $l_system;
$fieldnames["domain_label"] = $l_domain;
$fieldnames["userobm_local"] = $l_local;
$fieldnames["userobm_ext_id"] = $l_ext_id;
$fieldnames["userobm_perms"] = $l_profile;
$fieldnames["userobm_delegation"] = $l_delegation;
$fieldnames["userobm_archive"] = $l_archive;
$fieldnames["userobm_lastname"] = $l_lastname;
$fieldnames["userobm_firstname"] = $l_firstname;
$fieldnames["userobm_title"] = $l_title;
$fieldnames["userobm_phone"] = $l_phone;
$fieldnames["userobm_phone2"] = $l_phone2;
$fieldnames["userobm_mobile"] = $l_mphone;
$fieldnames["userobm_fax"] = $l_fax;
$fieldnames["userobm_fax2"] = $l_fax2;
$fieldnames["datebegin"] = $l_datebegin;
$fieldnames["userobm_account_dateexp"] = $l_dateexp;
$fieldnames["timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;
$fieldnames["userobm_mail_quota"] = $l_quota;
$fieldnames["userobm_mail_login_date"] = $l_date;
$fieldnames["userobm_nomade"] = $l_access_nomade;
$fieldnames["userobm_samba_perms"] = $l_smb_perms;
$fieldnames["userobm_description"] = $l_desc;

///////////////////////////////////////////////////////////////////////////////
// Display User specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_user_data(&$OD, $fieldname, $link_ok) {
  global $path,  $ico_mail, $params,$obm,$cgp_use;
  global $l_enabled, $l_disabled, $l_forbidden, $l_perms;
  global $perm, $cright_write_admin, $module, $cright_read_admin;

  $ext_url = $params["ext_url"];
  $ext_element = $params["ext_element"];

  $status = $OD->data_set->f('userobm_status');

  if (($fieldname == "userobm_login")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_user_get_id(".$OD->data_set->f("userobm_id").",'".addslashes($OD->data_set->f("userobm_login"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_user_get_id_url('$ext_url',".$OD->data_set->f("userobm_id").");";
    } else if ($status != 'INIT') {
      if ($link_ok && $perm->check_right($module, $cright_read_admin))
        $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("userobm_id");
    }
  }

  if (($fieldname == "userobm_lastname") && ($status == 'INIT')){
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_user_get_id(".$OD->data_set->f("userobm_id").",'".addslashes($OD->data_set->f("userobm_lastname"))."');";
    } else {
      $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("userobm_id");
    }
  }

  if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-user-".$OD->data_set->f("userobm_id")."\" style=\"display:none;\">".
    $OD->data_set->f("userobm_lastname")." ".$OD->data_set->f("userobm_firstname").
    "</span>";
  }  

  else if ($fieldname == "userobm_system") {
    $res["align"] = "center";
    $system = $OD->data_set->f("userobm_system");
    if ($system) {
      $res["name"] = "X";
    } else {
      $res["name"] = " ";
    }
  }

  else if ($fieldname == "userobm_perms") {
    $profil = $OD->data_set->f($fieldname); 
    $res["name"] = isset($GLOBALS["l_perm_$profil"]) ? $GLOBALS["l_perm_$profil"] : $profil;
  }

  else if ($fieldname == "userobm_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }
  else if ($fieldname == "userobm_samba_perms") {
    $res["align"] = "center";
    $samba_perms = $OD->data_set->f("userobm_samba_perms");
    if ($samba_perms) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
    }
  } 
  else if ($fieldname == "userobm_email") {
    $email = $OD->data_set->f("userobm_email");
    if($OD->data_set->f("userobm_hidden") == 1 && !$perm->check_right($module, $cright_write_admin)) {
      $res["align"] = "center";
      $res['name'] = '-';
    } elseif (strcmp($email ,"") != 0) {
      $email = get_entity_email($email, $OD->data_set->f("domain_name"));
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res["txt_name"] = "$email";
    }
  } 
  else if ($fieldname == "userobm_nomade") {
    $nomade_perms = $OD->data_set->f("userobm_nomade_perms");
    $nomade_enable = $OD->data_set->f("userobm_nomade_enable");
    $email_nomade = $OD->data_set->f("userobm_email_nomade");

    if ($nomade_perms) {
      if ($nomade_enable) {
	$res["name"] = $l_enabled;
      } else {
	$res["name"] = $l_perms;
      }
    } else {
      $res["name"] = $l_forbidden;
    }
  } 

  else if ($fieldname == "userobm_local") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == 'userobm_mail_quota') {
    $res['align'] = 'center';
    $mq = $OD->data_set->f($fieldname);
    if (($mq == 0) || ($mq == '')) {
      $res['name'] = '-';
    } else {
      $mqu = $OD->data_set->f('userobm_mail_quota_use');
      $percent = number_format($mqu / $mq * 100);
      $res['name'] = "$percent % ($mqu/$mq)";
    }
  }

  else if ($fieldname == "timelastaccess") {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = of_datetime_format($date, 1);
  }
  else if ($fieldname == "datebegin") {
    $datebegin = $OD->data_set->f($fieldname);
    $res["name"] = of_date_format($datebegin, 1);
  }
  else if ($fieldname == "dateexp") {
    $dateexp = $OD->data_set->f($fieldname);
    $res["name"] = of_date_format($dateexp, 1);
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_login, $l_lastname, $l_profile, $l_domain, $l_archive, $l_add_user;
  global $l_group, $l_email, $l_phone, $l_desc, $l_find, $l_all, $l_firstname, $l_exp;
  global $display, $c_all, $l_recursive, $obm;
  
  $login = $user["login"];
  $profile = $user["profile"];
  $domain_id = $user["domain_id"];
  $lname = stripslashes($user["lastname"]);
  $fname = stripslashes($user["firstname"]);
  $email = $user["email"];
  $phone = $user["phone"];
  $desc = $user["desc"];
  $group = $user["group"];
  $delegation = stripslashes($user["delegation"]);
  $exp = $user['exp'];
  $archive = ($user["archive"] == "1" ? "checked" : "");
  $recursive = ($user["recursive"] == "1" ? "checked" : "");
  $popup = $user["popup"];
  $next_action = $user['next_action'];
  $user_action = $user['action'];
  
  if ($user["filter_entity"]) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$user[filter_entity]\" />";
    if($user['filter_pattern'])
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"$user[filter_pattern]\" />";
    else
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\" />";
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('user', $user);

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_title = stripslashes($user["ext_title"]);
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext_widget = $user["ext_widget"];
    $ext_element = $user["ext_element"];
    $ext_container = $user["ext_container"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\" />
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\" />
	          <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\" />
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />
            <input name=\"ext_container\" type=\"hidden\" value=\"$ext_container\" />";

    if ($ext_title == "") {
      $ext_title = $l_add_users;
    }
    $display["title"] = $ext_title;
  }

  if ($user_action == "search_batch_user" || $user_action == "sel_batch_users") {
    $form_action = "<input name=\"action\" type=\"hidden\" value=\"sel_batch_users\" />";
  } else {
    $form_action = "<input name=\"action\" type=\"hidden\" value=\"search\" />";
  }

  foreach ($user['profiles'] as $k_profile => $one_profile) {
    $profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
    $block_profile .= "<option value=\"$k_profile\">$profile</option>";
  }

  if ($obm['domain_global']) {
    $sel_domain = of_domain_dis_select($domain_id, "all");
    $dis_domain = "<label>$l_domain<br />
      $sel_domain
    </label>";
  }

  $dis_delegation = of_delegation_dis_search_form($delegation);

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("user_index.php")."\" class=\"search\"> 

    <label>$l_login<br />
      <input type=\"text\" name=\"tf_login\" size=\"16\" maxlength=\"32\"
      value=\"$login\" />
    </label>
    <label>$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"16\" maxlength=\"32\"
      value=\"$lname\" />
    </label>
    <label>$l_firstname<br />
      <input type=\"text\" name=\"tf_firstname\" size=\"16\" maxlength=\"32\"
      value=\"$fname\" />
    </label>
    $dis_domain
    <label>$l_profile<br />
      <select type=\"text\" name=\"sel_profile\">
        <option value=\"$c_all\"> $l_all</option>
        $block_profile
      </select>
    </label>
    $dis_delegation
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" maxlength=\"32\"
      value=\"$email\" />
    </label>
    <label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"16\" maxlength=\"16\"
      value=\"$phone\" />
    </label>
    <label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"16\" maxlength=\"32\"
      value=\"$desc\" />
    </label>
    <label>$l_exp<br />
      <input type=\"text\" name=\"tf_exp\" size=\"16\" maxlength=\"32\"
      value=\"$exp\" />
    </label>
    <label>$l_group<br />
      <input type=\"text\" name=\"tf_group\" size=\"16\" maxlength=\"32\"
      value=\"$group\" />
    </label>
    $block_userdata
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      $form_action
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_filter_entity
      $ext
    </label>
    <p class=\"CL\" />
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_user_search_list($user) {
  global $l_found, $l_no_found;
  global $display, $obm;

  $popup = $user["popup"];
  $filter_entity = $user["filter_entity"];
  
  $obm_q = run_query_user_search($user);

  $prefs = get_display_pref($obm["uid"], "user");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $l_found");
    $block = html_user_search_list($obm_q, $prefs, $user, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the User Search result
// Parameters:
//   - $obm_q    : database result (user list)
//   - $prefs    : the fields which have to be displayed
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_user_search_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add, $l_edit_users;

  $login = urlencode($user["login"]);
  $lname = urlencode($user["lastname"]);
  $fname = urlencode($user["firstname"]);
  $profile = $user["profile"];
  $email = $user["email"];
  $delegation = urlencode($user["delegation"]);
  $desc = urlencode($user["desc"]);
  $exp = urlencode($user['exp']);
  $group = urlencode($user["group"]);
  $archive = $user["archive"];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext_widget = $user["ext_widget"];
    $ext_widget_text = $user["ext_widget_text"];
    $ext_element = $user["ext_element"];
    $ext_container = $user["ext_container"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;ext_container=$ext_container";
  }

  $url = url_prepare("user_index.php?action=search&amp;tf_login=$login&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;sel_profile=$profile&amp;tf_email=$email&amp;tf_delegation=$delegation&amp;tf_desc=$desc&amp;tf_exp=$exp&amp;tf_group=$group&amp;cba_archive=$archive$url_userdata$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $prefs, "user");
  if ($popup) {
    //    if ($ext_action == "ext_get_ids") {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "data-u-";
    if ($ext_element != "") {
      $user_d->data_cb_name = "";
      $user_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\">";
    } elseif ($ext_widget != "") {
      $user_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $user_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }
    $user_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </div>
      </form>";

  /*  else if ($ext_action == "ext_get_id"){
    $user_d->display_link = false;
    if ($ext_url != "") {
      $user_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $user_d->display_ext = "get_id";
    }
  }*/

  $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
      
  } else if ($user['action'] == "sel_batch_users") {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "data-u-";
    
    $user_d->data_form_head = "
      <form method=\"post\" action=\"".url_prepare("user_index.php")."\">";
    $user_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_edit_users\" />
        <input type=\"hidden\" name=\"action\" value=\"edit_batch_values\" />
      </div>
      </form>";
    
  }
  
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_user_data");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Consultation
// Parameters:
//   - $user : user database result
///////////////////////////////////////////////////////////////////////////////
function dis_user_consult($user) {
  global $display, $l_err_reference;

  $id = $user["user_id"];

  if ($id > 0) {
    $obm_q = run_query_user_detail($id);
    if ($obm_q->num_rows() == 1) {
      $block = html_user_consult($obm_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q) {
  global $l_login, $l_password, $l_profile, $l_kind, $l_lastname, $l_firstname;
  global $l_domain, $l_user, $l_lastaccess, $l_archive, $l_datebegin, $l_dateexp, $l_desc;
  global $l_coord, $l_address, $l_postcode, $l_town, $l_expresspostal, $l_title;
  global $l_delegation_target, $l_delegation, $l_direction, $l_service;
  global $l_company, $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_email;
  global $l_group_member, $l_group, $l_local, $l_ext_id,$l_hidden;
  global $l_web, $l_web_mode, $l_web_all, $l_web_list_only;
  global $l_mail, $l_mail_ext, $l_email, $l_quota, $l_email_nomade, $l_local_copy;
  global $l_nomade, $l_perms, $l_access, $l_host, $l_ip, $l_empty;
  global $l_vacation, $l_vacation_message, $l_vacation_datebegin;
  global $l_vacation_dateend,$l_mail_server,$perm, $cright_write_admin;
  global $l_enabled, $l_disabled, $l_web_consult_list, $l_photo, $module;
  global $l_smb, $l_smb_perms, $l_smb_home, $l_smb_home_drive, $l_smb_logon;
  //global $obm, $cgp_use, $profiles, $path, $display, $l_yes, $l_no;
  global $obm, $cgp_use, $path, $display, $l_yes, $l_no;
  
  $profiles = get_all_profiles(false);

  $id = $obm_q->f("userobm_id");
  $domain_label = ($obm_q->f("domain_label") != "" ? $obm_q->f("domain_label") : "GLOBAL");
  $system = $obm_q->f("userobm_system");
  $login = $obm_q->f("userobm_login");
  $local = ($obm_q->f("userobm_local") == 1 ? $l_yes : $l_no);
  $ext_id = $obm_q->f("userobm_ext_id");
  $profile = $obm_q->f("userobm_perms");
  $dis_profile = isset($GLOBALS["l_perm_$profile"]) ? $GLOBALS["l_perm_$profile"] : $profile;
  $kind = $obm_q->f("userobm_kind");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $title = $obm_q->f("userobm_title");
  $delegation_target = $obm_q->f("userobm_delegation_target");
  $delegation = $obm_q->f("userobm_delegation");
  $desc = $obm_q->f("userobm_description");
  $datebegin = of_date_format($obm_q->f("datebegin"), true);
  $dateexp = of_date_format($obm_q->f("dateexp"), true);
  $lastaccess = of_datetime_format($obm_q->f("userobm_timelastaccess"), 1);
  $archive = ($obm_q->f("userobm_archive") == '1' ? $l_yes : $l_no);
  $hidden = ($obm_q->f("userobm_hidden") == '1' ? $l_yes : $l_no);
  $photo_id = $obm_q->f("userobm_photo_id");

  $phone = $obm_q->f("userobm_phone");
  $phone2 = $obm_q->f("userobm_phone2");
  $mobile = $obm_q->f("userobm_mobile");
  $fax = $obm_q->f("userobm_fax");
  $fax2 = $obm_q->f("userobm_fax2");
  $company = $obm_q->f("userobm_company");
  $direction = $obm_q->f("userobm_direction");
  $service = $obm_q->f("userobm_service");
  $ad1 = $obm_q->f("userobm_address1");
  $ad2 = $obm_q->f("userobm_address2");
  $ad3 = $obm_q->f("userobm_address3");
  $zip = $obm_q->f("userobm_zipcode");
  $town = $obm_q->f("userobm_town");
  $cdx = $obm_q->f("userobm_expresspostal");
  $ctry_name = $obm_q->f("country_name");

  $web_perms = $obm_q->f("userobm_web_perms");
  $web_all = $obm_q->f("userobm_web_all");
  $mail_perms = $obm_q->f("userobm_mail_perms");
  $mail_ext_perms = $obm_q->f("userobm_mail_ext_perms");
  //  $mail = get_mail_info();
  if( $obm_q->f("domain_name") != "") {
    $main_domain = $obm_q->f("domain_name");
  } else {
    $main_domain = $obm["domain_name"];
  }  
  if($obm_q->f('userobm_hidden') != 1 || $perm->check_right($module, $cright_write_admin)) {
    $email = get_entity_email($obm_q->f("userobm_email"), $main_domain, false);
  } else {
    $email = '';
  }
  $mail_server_id = $obm_q->f("userobm_mail_server_id");
  $mail_server_host_name = $obm_q->f("mailserver_hostname");
  $mail_quota = $obm_q->f("userobm_mail_quota");
  $email_nomade = $obm_q->f("userobm_email_nomade");
  $nomade_perms = $obm_q->f("userobm_nomade_perms");
  $nomade_enable = $obm_q->f("userobm_nomade_enable");
  $nomade_local_copy = $obm_q->f("userobm_nomade_local_copy");
  $vacation_enable = $obm_q->f("userobm_vacation_enable");
  $vacation_datebegin = of_datetime_format($obm_q->f("userobm_vacation_datebegin"), 1);
  $vacation_dateend = of_datetime_format($obm_q->f("userobm_vacation_dateend"), 1);
  $vacation_message = stripslashes($obm_q->f("userobm_vacation_message"));
  $smb_perms = $obm_q->f("userobm_samba_perms");
  $smb_home = $obm_q->f("userobm_samba_home");
  $smb_home_drive = $obm_q->f("userobm_samba_home_drive");
  $smb_logon = $obm_q->f("userobm_samba_logon_script");

  $nomade_perms = ($nomade_perms ? $l_yes : $l_no);
  $nomade_enable = ($nomade_enable ? $l_yes : $l_no);
  $nomade_local_copy = ($nomade_local_copy ? $l_yes : $l_no);
  $vacation_enable = ($vacation_enable ? $l_yes : $l_no);
  $vacation_message = ($vacation_enable ? nl2br($vacation_message) : "---");

  if ($host_name == '') {
    $dis_host = $l_empty;
  } else {
    $dis_host = "<a href=\"$path/host/host_index.php?action=detailconsult&amp;param_host=$host_id\">$host_name</a>";
  }
  if ($host_ip == '') $host_ip = $l_empty;
  $web_access = ($web_all ? $l_web_all : $l_web_list_only);
  $mail_state  = ($mail_perms ? $l_enabled : $l_disabled);
  $mail_ext_state  = ($mail_ext_perms ? $l_enabled : $l_disabled);
  $smb_state  = ($smb_perms ? $l_enabled : $l_disabled);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult("user", $id);

  // Liste des groupes de l'utilisateur
  $g_q = run_query_user_user_group($id);
  if ($g_q->num_rows() > 0) {
    $dis_group = "
    <div class=\"detail infos\">
    <h1>$l_group_member</h1>
    <table>
    <tr><td>";
    while ($g_q->next_record()) {
      $grp_id = $g_q->f("group_id");
      $grp_name = $g_q->f("group_name");
      $dis_group .= "
    </td></tr><tr>
      <th>$l_group</th>
      <td><a href=\"$path/group/group_index.php?action=detailconsult&amp;group_id=$grp_id\">$grp_name</a></td>";
    }
    $dis_group .= "
    </tr>
    </table></div>";
  }

  // Target Delegation handling (if user as delegation admin realm)
  if($cgp_use['property']['delegation']) {
    $dis_delegation_target = "
    <tr>
      <th>$l_delegation_target</th>
      <td>$delegation_target</td>
    </tr>";

    $dis_delegation = of_delegation_dis_block_consult($delegation);
  }

  // Section Web
  if ($cgp_use["service"]["web"]) {
    $web_section = "
    <div class=\"detail infos\">
    <h1>$l_access</h1>

    <table>
    <tr>
      <th>$l_web</th>
      <td>$web_state</td>
    </tr>
    $dis_access
    </table>
    </div>";
  }

  // Section Mail
  if ($cgp_use["service"]["mail"]) {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$l_mail</h1>

    <table>
    <tr>
      <th>$l_mail</th>
      <td>$mail_state</td>
    </tr>
    <tr>
      <th>$l_mail_server</th>
      <td>";
      if ($mail_server_id) {
        $mail_section .= "<a href=\"$path/host/host_index.php?action=detailconsult&host_id=".$mail_server_id."\">".$mail_server_host_name."</a>";
      } else {
        $mail_section .= "-";
      }
      $mail_section .= "</td>
    </tr>
    <tr>
      <th>$l_email</th>
      <td>$email </td>
    </tr>
    <tr>
      <th>$l_quota</th>
      <td>$mail_quota</td>
    </tr>
    </table>
    </div>";
  } else {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$l_mail</h1>

    <table>
    <tr>
      <th>$l_email</th>
      <td>$email</td>
    </tr>
    </table>
    </div>";
  }

  if ($cgp_use["service"]["mail_nomad"]) {
    $nomad_section = "
    <div class=\"detail infos\">
    <h1>$l_nomade</h1>

    <table>
    <tr>
      <th>$l_email_nomade</th>
      <td>$email_nomade</td>
    </tr>
    <tr>
      <th>$l_perms</th>
      <td>$nomade_perms</td>
    </tr>
    <tr>
      <th>$l_enabled</th>
      <td>$nomade_enable</td>
    </tr>
    <tr>
      <th>$l_local_copy</th>
      <td>$nomade_local_copy</td>
    </tr>
    </table>
    </div>";
  }

  if ($cgp_use["service"]["mail_vacation"]) {
    $vacation_section = "
    <div class=\"detail infos\">
    <h1>$l_vacation</h1>

    <table>
    <tr>
      <th>$l_enabled</th>
      <td>$vacation_enable</td>
    </tr>
    <tr>
      <th>$l_vacation_datebegin</th>
      <td>$vacation_datebegin</td>
    </tr>
    <tr>
      <th>$l_vacation_dateend</th>
      <td>$vacation_dateend</td>
    </tr>
    <tr>
      <th>$l_vacation_message</th>
      <td>$vacation_message</td>
    </tr>
    </table>
    </div>";
  }

  if ($cgp_use["service"]["samba"]) {
    $samba_section = "
    <div class=\"detail infos\">
    <h1>$l_smb</h1>

    <table>
    <tr>
      <th>$l_smb_perms</th>
      <td>$smb_state</td>
    </tr>
    ";
    if($smb_perms == '1') {
      $samba_section .= "      
      <tr>
        <th>$l_smb_home</th>
        <td>$smb_home</td>
      </tr>
      <tr>
        <th>$l_smb_home_drive</th>
        <td>$smb_home_drive</td>
      </tr>
      <tr>
        <th>$l_smb_logon</th>
        <td>$smb_logon</td>
      </tr>";
    }
    $samba_section .= "      
    </table>
    </div>";
  }

  // Photo
  if($photo_id > 0) {
    $block_photo = "<img src=\"$path/document/document_index.php?action=accessfile&document_id=$photo_id\" alt=\"[Photo]\"/ />";
  } else {
    $block_photo = "";
  }
  
  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "$fname $lname";

  $block = "
<div class=\"detail infos\">
  <h1>$l_user</h1>
 
  <table>
  <tr>
    <th>$l_kind</th>
    <td>$kind</td>
  </tr>
  <tr>
    <th>$l_lastname</th>
    <td>$lname</td>
  </tr>
  <tr>
    <th>$l_firstname</th>
    <td>$fname</td>
  </tr>
  <tr>
    <th>$l_login</th>
    <td>$login</td>
  </tr>
  <tr>
    <th>$l_domain</th>
    <td>$domain_label</td>
  </tr>
  <tr>
    <th>$l_local</th>
    <td>$local</td>
  </tr>
  <tr>
    <th>$l_archive</th>
    <td>$archive</td>
  </tr>    
  <tr>
    <th>$l_ext_id</th>
    <td>$ext_id</td>
  </tr>
  <tr>
    <th>$l_profile</th>
    <td>$dis_profile</td>
  </tr>
  $dis_delegation
  $dis_delegation_target
  <tr>
    <th>$l_title</th>
    <td>$title</td>
  </tr>
  <tr>
    <th>$l_datebegin</th>
    <td>$datebegin</td>
  </tr>
  <tr>
  <tr>
    <th>$l_dateexp</th>
    <td>$dateexp</td>
  </tr>
    <th>$l_lastaccess</th>
    <td>$lastaccess</td>
  </tr>
  <tr>
    <th>$l_photo</th>
    <td>$block_photo</td>
  </tr>
</table>
</div>

<div class=\"detail infos\">
  <h1>$l_coord</h1>
  <table>
  <tr>
    <th>$l_phone</th>
    <td>$phone</td>
  </tr>
  <tr>
    <th>$l_phone2</th>
    <td>$phone2</td>
  </tr>
  <tr>
    <th>$l_mphone</th>
    <td>$mobile</td>
  </tr>
  <tr>
    <th>$l_fax</th>
    <td>$fax</td>
  </tr>
  <tr>
    <th>$l_fax2</th>
    <td>$fax2</td>
  </tr>
  <tr>
    <th>$l_company</th>
    <td>$company</td>
  </tr>
  <tr>
    <th>$l_direction</th>
    <td>$direction</td>
  </tr>
  <tr>
    <th>$l_service</th>
    <td>$service</td>
  </tr>
  <tr>
    <th>$l_address 1</th>
    <td>$ad1</td>
  </tr>
  <tr>
    <th>$l_address 2</th>
    <td>$ad2</td>
  </tr>
  <tr>
    <th>$l_address 3</th>
    <td>$ad3</td>
  </tr>
  <tr>
    <th>$l_postcode</th>
    <td>$zip</td>
  </tr>
  <tr>
    <th>$l_town</th>
    <td>$town</td>
  </tr>
  <tr>
    <th>$l_expresspostal</th>
    <td>$cdx</td>
  </tr>
  <tr>
    <th>$l_country</th>
    <td>$ctry_name</td>
  </tr>
  </table>
</div>

<div class=\"detail infos\">
  <h1>$l_desc</h1>
  <table>
  <tr>
    <th>$l_desc</th>
    <td>$desc</td>
  </tr>
  </table>
</div>
$block_userdata

$web_section
$mail_section
$nomad_section
$vacation_section
$samba_section
$dis_group
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Group form
// Parameters:
//   - $usr_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_group_consult($usr_q) {
  global $l_update_group, $l_user, $l_group_member, $l_group;

  $id = $usr_q->f("userobm_id");
  $login = $usr_q->f("userobm_login");
  $lname = $usr_q->f("userobm_lastname");
  $fname = $usr_q->f("userobm_firstname");

  // Liste des groupes
  $g_q = run_query_user_public_owner_group($id);

  // Liste des groupes de l'utilisateur : on remplit le tableau $groups
  $gu_q = run_query_user_user_group($id);
  $groups = array();
  while ($gu_q->next_record()) {
    $g_id = $gu_q->f("userobmgroup_group_id");
    array_push($groups, $g_id);
  }

  while ($g_q->next_record()) {
    $grp_id = $g_q->f("group_id");
    $grp_name = $g_q->f("group_name");
    $checked = (in_array($grp_id, $groups) ? " checked" : "" );
    $dis_group .= "
    <tr>
      <th>$grp_name</th>
      <td><input name=\"data-g-$grp_id\" type=\"checkbox\" value=\"1\" $checked /></td>
    </tr>";
  }

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"" . url_prepare("user_index.php") . "\">
  <fieldset class=\"detail extra\">
  <legend>$l_user $login ($lname $fname) $l_group_member</legend>
  <table>
  $dis_group
  </table>
  </fieldset>
 
  <fieldset class=\"buttons\">
  <input type=\"hidden\" name=\"user_id\" value=\"$id\" />
  <input type=\"hidden\" name=\"action\" value=\"group_update\" />
  <input type=\"submit\" value=\"$l_update_group\" />
  </fieldset>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Form
// Parameters:
//   - $usr_q : user database result
//   - $user  : default values or updated values (if error)
//   - $field : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_user_form($usr_q, $user, $field='') {
  global $l_login, $l_password, $l_profile, $l_kind, $l_lastname, $l_firstname;
  global $l_user, $l_title, $l_archive, $l_datebegin, $l_dateexp, $l_noexperie, $l_insert, $l_update;
  global $l_update_valid, $l_validate, $l_change_password_confirm, $l_password_update;
  global $l_delegation_target, $l_hidden, $l_photo, $l_generate_passwd; 
  global $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_desc;
  global $l_coord, $l_company, $l_direction, $l_service, $l_address;
  global $l_postcode, $l_town, $l_expresspostal;
  global $l_web, $l_web_mode, $l_web_all, $l_web_list_only;
  global $l_mail, $l_mail_ext, $l_email, $l_quota, $l_email_nomade, $l_local_copy;
  global $l_nomade, $l_perms, $l_access, $l_host, $l_ip, $l_empty;
  global $l_vacation, $l_vacation_message, $l_vacation_datebegin;
  global $l_vacation_dateend, $l_mail_server;
  global $l_enabled, $l_disabled, $l_web_consult_list,$path;
  global $l_smb, $l_smb_perms, $l_smb_home, $l_smb_home_drive, $l_smb_logon;
  global $l_mailserver_auto;
  //global $path, $display, $cgp_use, $action, $profiles;
  global $path, $display, $cgp_use, $action;
  global $obm, $c_default_domain, $l_domain, $l_header_new, $l_hour;
  global $ccalendar_first_hour, $ccalendar_last_hour, $l_delete_userobmphoto;

  $noexperie = " checked=\"checked\" ";	
  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    // infos
    $id = $usr_q->f("userobm_id");
    $domain_id = $usr_q->f('userobm_domain_id');
    $login = $usr_q->f("userobm_login");
    $old_login = $login;
    $passwd = "";
    $profile = $usr_q->f("userobm_perms");
    $lastaccess = $usr_q->f("lastaccess");
    $kind = $usr_q->f("userobm_kind");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $title = $usr_q->f("userobm_title");
    $delegation_target = $usr_q->f("userobm_delegation_target");
    $delegation = $usr_q->f("userobm_delegation");
    $desc = $usr_q->f("userobm_description");
    $datebegin = of_date_upd_format($usr_q->f("datebegin"),true);
    $dateexp = of_date_upd_format($usr_q->f("dateexp"),true);
    if(isset($dateexp)){$noexperie = "";};
    $archive = ($usr_q->f("userobm_archive") == "1" ? " checked" : "");
    $hidden = ($usr_q->f("userobm_hidden") == "1" ? " checked" : "");
    $photo_id = $usr_q->f("userobm_photo_id");

    //Wait people
    $status = $usr_q->f("userobm_status");

    // coord
    $phone = $usr_q->f("userobm_phone");
    $phone2 = $usr_q->f("userobm_phone2");
    $mobile = $usr_q->f("userobm_mobile");
    $fax = $usr_q->f("userobm_fax");
    $fax2 = $usr_q->f("userobm_fax2");
    $company = $usr_q->f("userobm_company");
    $direction = $usr_q->f("userobm_direction");
    $service = $usr_q->f("userobm_service");
    $ad1 = $usr_q->f("userobm_address1");
    $ad2 = $usr_q->f("userobm_address2");
    $ad3 = $usr_q->f("userobm_address3");
    $zip = $usr_q->f("userobm_zipcode");
    $town = $usr_q->f("userobm_town");
    $cdx = $usr_q->f("userobm_expresspostal");
    $ctry_name = $usr_q->f("country_name");

    // web
    $h_name = $usr_q->f("host_name");
    $h_id = $usr_q->f("userobm_host_id");
    $web_perms = $usr_q->f("userobm_web_perms");
    $web_list = $usr_q->f("userobm_web_list");
    $web_all = $usr_q->f("userobm_web_all");

    // mail
    $mail_perms = $usr_q->f("userobm_mail_perms");
    $mail_ext_perms = $usr_q->f("userobm_mail_ext_perms");
    $mail_server_id = $usr_q->f("mailserver_id");
    $mail_server_host_name = $usr_q->f("mailserver_hostname");
    $email = $usr_q->f("userobm_email");
    $mail_quota = $usr_q->f("userobm_mail_quota");

    // nomad email
    $nomade_perms = $usr_q->f("userobm_nomade_perms");
    $nomade_enable = $usr_q->f("userobm_nomade_enable");
    $nomade_local_copy = $usr_q->f("userobm_nomade_local_copy");
    $email_nomade = $usr_q->f("userobm_email_nomade");

    // vacation
    $vacation_enable = $usr_q->f("userobm_vacation_enable");
    $vacation_message = stripslashes($usr_q->f("userobm_vacation_message"));
    $vacation_datebegin = $usr_q->f("userobm_vacation_datebegin");
    $vacation_dateend = $usr_q->f("userobm_vacation_dateend");
    $hourbegin = date("H",$vacation_datebegin);
    $minbegin = date("i",$vacation_datebegin);
    $hourend = date("H",$vacation_dateend);
    $minend = date("i",$vacation_dateend);
    $vacation_datebegin = of_date_upd_format($vacation_datebegin);
    $vacation_dateend = of_date_upd_format($vacation_dateend);

    // samba
    $smb_perms = $usr_q->f("userobm_samba_perms");
    $smb_home = $usr_q->f("userobm_samba_home");
    $smb_home_drive = trim($usr_q->f("userobm_samba_home_drive"));
    $smb_logon = $usr_q->f("userobm_samba_logon_script");

    $dis_title = "$fname $lname";
  } elseif ($action == 'new') {
    $dom_infos = of_domain_get_domain_infos($obm['domain_id'], true);
    $ad1 = $dom_infos['address1'];
    $ad2 = $dom_infos['address2'];
    $ad3 = $dom_infos['address3'];
    $zip = $dom_infos['postcode'];
    $town = $dom_infos['town'];
    $smb = get_samba_info();
    $smb_home = $smb['samba_home_def'];
    $smb_home_drive = $smb['samba_home_drive_def'];
  }

  // If parameters have been given, they supercede the default action value
  // infos
  if (isset($user["user_id"])) { $id = $user["user_id"]; }
  if (isset($user["domain_id"])) { $domain_id = $user["domain_id"]; }
  if (isset($user["login"])) { $login = strtolower($user["login"]); }
  if (isset($user["old_login"])) { $old_login = $user["old_login"]; }
  if (isset($user["passwd"])) { $passwd = $user["passwd"]; }
  if (isset($user["profile"])) { $profile = $user["profile"]; }
  if (isset($user["kind"])) { $kind = $user["kind"]; }
  if (isset($user["lastname"])) { $lname = stripslashes($user["lastname"]); }
  if (isset($user["firstname"])) { $fname = stripslashes($user["firstname"]); }
  if (isset($user["title"])) { $title = stripslashes($user["title"]); }
  if (isset($user["email"])) { $email = stripslashes($user["email"]); }
  if (isset($user["datebegin"])) { $datebegin = of_date_upd_format($user["datebegin"],true); }
  if (isset($user["dateexp"])) { $dateexp = of_date_upd_format($user["dateexp"],true); $noexperie = "";}
  if (isset($user["delegation_target"])) { $delegation_target = stripslashes($user["delegation_target"]); }
  if (isset($user["delegation"])) { $delegation = stripslashes($user["delegation"]); }
  if (isset($user["desc"])) { $desc = stripslashes($user["desc"]); }
  if (isset($user["archive"])) { $archive = ($user["archive"] == '1' ? "checked" : ""); }
  if (isset($user["hidden"])) { $hidden = ($user["hidden"] == '1' ? "checked" : ""); }
  if (isset($user["photo_id"])) { $photo_id = $user["photo_id"]; }

  // wait people
  if (isset($user["status"]) && $action != 'detailupdate') { $status = $user["status"]; }

  // coord
  if (isset($user["phone"])) { $phone = $user["phone"]; }
  if (isset($user["phone2"])) { $phone2 = $user["phone2"]; }
  if (isset($user["mobile"])) { $mobile = $user["mobile"]; }
  if (isset($user["fax"])) { $fax = $user["fax"]; }
  if (isset($user["fax2"])) { $fax2 = $user["fax2"]; }
  if (isset($user["company"])) { $company = stripslashes($user['company']); }
  if (isset($user["direction"])) { $direction = stripslashes($user['direction']); }
  if (isset($user["service"])) { $service = $user["service"]; }
  if (isset($user["ad1"])) { $ad1 = $user["ad1"]; }
  if (isset($user["ad2"])) { $ad2 = $user["ad2"]; }
  if (isset($user["ad3"])) { $ad3 = $user["ad3"]; }
  if (isset($user["zip"])) { $zip = $user["zip"]; }
  if (isset($user["town"])) { $town = $user["town"]; }
  if (isset($user["cdx"])) { $cdx = $user["cdx"]; }
  if (isset($user["ctry"])) { $ctry = $user["country"]; }

  // web
  if (isset($user["host_name"])) { $h_name = $user["host_name"]; }
  if (isset($user["host_id"])) { $h_id = $user["host_id"]; }
  if (isset($user["web_perms"])) { $web_perms = $user["web_perms"]; }
  if (isset($user["web_list"])) { $web_list = $user["web_list"]; }
  if (isset($user["web_all"])) { $web_all = $user["web_all"]; }

  // mail
  if (isset($user["mail_perms"])) { $mail_perms = $user["mail_perms"]; }
  if (isset($user["mail_ext_perms"])) { $mail_ext_perms = $user["mail_ext_perms"]; }
  if (isset($user["mail_server_id"])) { $mail_server_id = $user["mail_server_id"]; }
  if (isset($user["mail_server_hostname"])) { $mail_server_host_name = $user["mail_server_hostname"]; }
  if (isset($user["mail_quota"])) { $mail_quota = $user["mail_quota"]; }

  // nomad email
  if (isset($user["nomade_perms"])) { $nomade_perms = $user["nomade_perms"]; }
  if (isset($user["nomade_enable"])) { $nomade_enable = $user["nomade_enable"]; }
  if (isset($user["nomade_local_copy"])) { $nomade_local_copy = $user["nomade_local_copy"]; }
  if (isset($user["email_nomade"])) { $email_nomade = $user["email_nomade"]; }

  // vacation
  if (isset($user["vacation_enable"])) { $vacation_enable = $user["vacation_enable"]; }
  if (isset($user["vacation_datebegin"])) { $vacation_datebegin = $user["vacation_datebegin"]; }
  if (isset($user["vacation_dateend"])) { $vacation_dateend = $user["vacation_dateend"]; }
  if (isset($user["vacation_message"])) { $vacation_message = stripslashes( $user["vacation_message"] ); }
  if (isset($user["time_begin"])) { $hourbegin = $user["time_begin"]; }
  if (isset($user["min_begin"])) {$minbegin = $user["min_begin"]; }
  if (isset($user["time_end"])) {$hourend = $user["time_end"]; }
  if (isset($user["min_end"])) {$minend = $user["min_end"]; }
  // samba
  if (isset($user["smb_perms"])) { $smb_perms = $user["smb_perms"]; }
  if (isset($user["smb_home"])) { $smb_home = stripslashes($user["smb_home"]); }
  if (isset($user["smb_home_drive"])) { $smb_home_drive = $user['smb_home_drive']; }
  if (isset($user["smb_logon"])) { $smb_logon = $user["smb_logon"]; }

  $block_userdata = of_userdata_dis_entity_form("user", $id, $user);

  // Mark the error field
  if ($field != "") {
    $class[$field] = "error";
  }

  // Default exp is hidden
  if (!isset($dateexp))	{
    $showexp="display:none";
  }

  // login (can't be updated, so only on insert)
  if (($action == "new") || ($action == "insert")) {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id='userLogin' maxlength=\"32\" value=\"$login\" />";
  } else {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id='userLogin' maxlength=\"32\" value=\"$login\" />
                  <input type=\"hidden\" name=\"tf_old_login\" value=\"$old_login\" />";
  }

  // Domain (can't be updated, so only on insert)
  if (($action == 'new' || $action == 'insert')
      && $obm["domain_global"]) {
    $sel_domain = of_domain_dis_select($domain_id);
    $dis_domain = "<tr>
      <th>$l_domain</th>
      <td>$sel_domain</td>
  </tr>";
  }


  // Section Web
  if ($cgp_use["service"]["web"]) {
    // Web perms checkbox checked value
    $web_state  = ($web_perms ? "checked" : "");

    // Web mode Radio checked value
    if ($web_all == "0") $web_all_c0 = " checked";
    else $web_all_c1 = " checked";
    
    $web_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_access</legend>

  <table>
  <tr>
    <th class=\"$class[web_perms]\">$l_web</th>
    <td><input name=\"cb_web_perms\" type=\"checkbox\" value=\"1\" $web_state /></td>
  </tr>
  $dis_access
  </table>
  </fieldset>";
  }

  if ($cgp_use['service']['mail']) {
    $dis_mail = dis_user_mail_field($domain_id, $email, $class);
    
    // Mail perms checkbox checked values
    $mail_state  = ($mail_perms ? 'checked' : '');
    $mail_ext_state  = ($mail_ext_perms ? 'checked' : '');
    // Mail server can't be changed if set
    if (! $mail_server_host_name) {
      $dis_mailserver = of_domain_dis_mailserver_select('imap', $mail_server_id, $obm['domain_id']);
      
      // BEGIN - automatic mail server chooser hooks
      
      if (has_mail_server_auto()) {
        $dis_mailserver = preg_replace('/(<select [^>]+>)/',
          "\\1 <option value=\"auto\">$l_mailserver_auto</option>", $dis_mailserver);
      }
      
      // END - automatic mail server chooser hooks
    
    } else {
      $dis_mailserver = "
      <a href=\"$path/host/host_index.php?action=detailconsult&host_id=$mail_server_id\">$mail_server_host_name</a>
      <input type=\"hidden\" name=\"sel_mail_server_id\" value=\"$mail_server_id\" />
      <input type=\"hidden\" name=\"sel_mail_server_hostname\" value=\"$mail_server_host_name\" />";
    }
    if($mail_state) {
      $externalKlass = 'H';
      $externalEnabled = 'disabled="disabled"';
    } else {
      $internalKlass = 'H';
    }
    $main_domain = $obm["domain_name"];
    $mail_section = "
  <fieldset class=\"detail infos\" id=\"userMail\">
  <legend>$l_mail</legend>
  <table>
    <tr>
      <th class=\"$class[mail_perms]\">$l_mail</th>
      <td><input name=\"cb_mail_perms\" type=\"checkbox\" id=\"userMailActive\" onclick=\"switch_mail_mode();\" value=\"1\" $mail_state /></td>
    </tr>          
  </table>
  <table id=\"userInternalMail\" class='$internalKlass'>
    <tr id=\"user_mail\">
      <th class=\"$class[mail_server]\">$l_mail_server</th>
      <td>$dis_mailserver</td>
    </tr>
    <tr>
    $dis_mail
    </tr>        
    <tr id=\"user_mail\">
    <th class=\"$class[mail_quota]\">$l_quota</th>
    <td><input name=\"tf_mail_quota\" maxlength=\"6\" size=\"6\" value=\"$mail_quota\" /></td>
    </tr>      
  </table>
  <table id=\"userExternalMail\" class='$externalKlass'>
    <tr>
      <th class=\"$class[email]\">$l_email</th>
      <td><input type=\"text\" name=\"ta_email\" $externalEnabled value=\"$email\" /></td>
    </tr>  
  </table>
  <script type='text/javascript'>
    var internal = $('userInternalMail');
    var external = $('userExternalMail');
    if($('userMailActive').checked == true) {
      external.dispose();
    } else {
      internal.dispose();  
    }
  </script>
  </fieldset>";
  } else {
  $mail_section = "
    <fieldset class=\"detail infos\"  id=\"user_external_mail\">
    <legend>$l_mail</legend>
    <table>    
    <tr>
      <th class=\"$class[email]\">$l_email</th>
      <td><input type=\"text\" name=\"ta_email\" value=\"$email\" /></td>
    </tr>
    </table>
    </fieldset>";
  }

  if ($cgp_use["service"]["mail_nomad"]) {
  // Nomad chekbox checked value
    if ($nomade_perms) $nomade_perms_c = " checked";
    if ($nomade_enable) $nomade_enable_c = " checked";
    if ($nomade_local_copy) $nomade_local_copy_c = " checked";

    $nomad_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_nomade</legend>

  <table>
  <tr>
    <th class=\"$class[email_nomade]\">$l_email_nomade</th>
    <td><input name=\"tf_email_nomade\" maxlength=\"64\" size=\"32\" value=\"$email_nomade\" /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_perms]\">$l_perms</th>
    <td><input name=\"cb_nomade_perms\" type=\"checkbox\" value=\"1\" $nomade_perms_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_enable]\">$l_enabled</th>
    <td><input name=\"cb_nomade_enable\" type=\"checkbox\" value=\"1\" $nomade_enable_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_local_copy]\">$l_local_copy</th>
    <td><input name=\"cb_nomade_local_copy\" type=\"checkbox\" value=\"1\" $nomade_local_copy_c /></td>
  </tr>
  </table>
  </fieldset>";
  }

  if ($cgp_use["service"]["mail_vacation"]) {

    // hour begin
    $dis_hour_b = "<select name=\"sel_time_begin\" style=\"width:4em;\">";
    for ($i=$ccalendar_first_hour; $i<$ccalendar_last_hour; $i++) {
      $current_hour = substr("0$i",-2,2); 
      if ($current_hour == $hourbegin) {
        $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
      } else {
        $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_b .= "</select>";  
    $dis_min_b = "<select name=\"sel_min_begin\" style=\"width:4em;\">";
    if ($ccalendar_hour_fraction <= 60 && $ccalendar_hour_fraction > 0) {
      $hour_fraction = $ccalendar_hour_fraction;
    } else {
      $hour_fraction = 4;
    }
    $step = 60 / $hour_fraction;
    for ($i=$hour_fraction; $i>=1; $i--) {
      $current_min = substr("0".round((60 - ($step*$i))),-2);
      if ($current_min  == $minbegin) {
        $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
      } else {
        $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
      }
    }
    $dis_min_b .= "</select>";

    // hour end
    $dis_hour_e = "<select name=\"sel_time_end\" style=\"width:4em;\">";
    for ($i=$ccalendar_first_hour; $i<=$ccalendar_last_hour; $i++) {
      $current_hour = substr("0$i",-2,2); 
      if ($current_hour == $hourend) {
        $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
      } else {
        $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_e .= "</select>";  
    $dis_min_e = "<select name=\"sel_min_end\" style=\"width:4em;\">";
    for ($i=$hour_fraction; $i>=1; $i--) {
      $current_min = substr("0".round((60 - ($step*$i))),-2);
      if ($current_min  == $minend) {
        $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
      } else {
        $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
      }
    }
    $dis_min_e .= "</select>";

    // Vacation checkbox checked value
    $vacation_state = ($vacation_enable ? "checked" : "");
    $vacation_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_vacation</legend>

  <table>
  <tr>
    <th class=\"$class[vacation_enable]\">$l_enabled</th>
    <td><input name=\"cb_vacation_enable\" type=\"checkbox\" value=\"1\" $vacation_state /></td>
  </tr>
  <tr>
    <th class=\"$class[vacation_datebegin]\">$l_vacation_datebegin</th>
    <td>
      <input type=\"text\" name=\"tf_vacation_datebegin\" value=\"$vacation_datebegin\" class=\"datePicker\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr>
    <th class=\"$class[vacation_dateend]\">$l_vacation_dateend</th>
    <td>
      <input type=\"text\" name=\"tf_vacation_dateend\" value=\"$vacation_dateend\" class=\"datePicker\"/>
      <span id=\"hour_end\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>
  </tr>
  <tr>
    <th class=\"$class[vacation_message]\">$l_vacation_message</th>
    <td><textarea name=\"ta_vacation_message\" cols=\"32\" rows=\"3\">$vacation_message</textarea></td>
  </tr>
  </table>
  </fieldset>";
  }

  if ($cgp_use["service"]["samba"]) {
    $samba = run_query_user_samba_properties();
    $smb_home_drive = (isset($smb_home_drive))?$smb_home_drive:$samba['drive'];
    if (isset($smb_home_drive) && $smb_home_drive != '') $drive[$smb_home_drive] = 'selected="selected"';
    for($i='D'; $i < 'Z';$i++) {
      $dis_drive_sel .= "<option ".$drive["$i"]." value='$i'>$i:</option>";
    }
    $dis_drive_sel .= "<option ".$drive['Z']." value='Z'>Z:</option>";
    $dis_drive_sel = "<select style='width:4em;' name='sel_smb_home_drive'>$dis_drive_sel</select>";
    $smb_home = (isset($smb_home))?$smb_home:$samba['home'];
    if($login != '') {
      $smb_hidden_home = str_replace($login,'%u', $smb_home);
      $smb_home = str_replace('%u', $login, $smb_home);
    } else {
      $smb_hidden_home = $smb_home;
    }
    $smb_state  = ($smb_perms ? "checked" : "");
    if(!$smb_state) {
      $sambaKlass = 'H';
    }
    $samba_section = "
      <fieldset class=\"detail infos\" id='userSamba'>
      <legend>$l_smb</legend>

      <table>
      <tr>
        <th class=\"$class[smb_perms]\">$l_smb_perms</th>
        <td><input name=\"cb_smb_perms\" type=\"checkbox\" id=\"userSambaActive\" onclick=\"switch_samba_mode();\"  value=\"1\" $smb_state /></td>
      </tr>
      </table>
      <table  id=\"userSambaProperties\" class='$sambaKlass'>
      <tr>
        <th class=\"$class[smb_home]\">$l_smb_home</th>
        <td>
        <input name=\"tf_smb_home\" maxlength=\"255\" size=\"64\" id='userSambaHome'value=\"$smb_home\" />
        <input type=\"hidden\" id='userSambaHiddenHome'value=\"$smb_hidden_home\" />
        </td>
      </tr>
      <tr>
        <th class=\"$class[smb_home_drive]\">$l_smb_home_drive</th>
        <td>$dis_drive_sel</td>
      </tr>
      <tr>
        <th class=\"$class[smb_logon]\">$l_smb_logon</th>
        <td><input name=\"tf_smb_logon\" maxlength=\"128\" size=\"64\" value=\"$smb_logon\" /></td>
      </tr>
      </table>
      <script type='text/javascript'>
        var samba = $('userSambaProperties');
        $('userSambaHome').addEvent('focus', function () { $('userSambaHome').value = $('userSambaHiddenHome').value});
        $('userSambaHome').addEvent('blur', function () { $('userSambaHiddenHome').value = $('userSambaHome').value; sambaHomeCompute()});
        $('userSambaHome').addEvent((Browser.Engine.trident || Browser.Engine.webkit) ? 'keydown' : 'keypress', function (e) {
          if(e.key == 'enter') {
            sambaHomeCompute();
          }
        });
        $('userLogin').addEvent('keyup' , sambaHomeCompute);
      </script>      
      </fieldset>";
  }

  // Input file (photo)
  $block_photo = display_document_form($user, $l_photo);

  // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($action=="detailupdate") || ($action=="update") || ($action=="valid")) {
    $dis_button = "<input type=\"hidden\" id=\"user_action\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$id\" />";
    if ($status == 'INIT') {
        $dis_button .= "<input type=\"button\" onclick=\"obm.popup.show('idPopupValidUser')\" value=\"$l_update\" />";
    } else {
        $dis_button .= "<input type=\"submit\" value=\"$l_update\" />";
    }
  }

  //Popup password Generate
  $pop_generate = "
    <div id=\"popupModifPassword\" class=\"obmPopup\">
        <h1>
          <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
            <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
          </a>
          <span>$l_password_update</span>
          </h1>
          </br>
          $l_change_password_confirm
        <fieldset class=\"buttons\">
		    	<input type=\"button\" onclick=\"javascript:generate_passwd()\" id=\"popup_confirm\" class=\"closeSticky\" value=\"$GLOBALS[l_confirm]\" />
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$GLOBALS[l_cancel]\" />
				</fieldset>
    </div>
    ";

  // Status User
  if($status == 'INIT'){
    $dis_status = "
        <input type=\"hidden\" name=\"ta_status\" value=\"$status\" />
        ";
    $pop_up = "
      <div id=\"idPopupValidUser\" class=\"obmPopup\">
        <h1>
          <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
            <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
          </a>
          <span>Modif User</span>
        </h1>
        <input name=\"cba_modif\" type=\"radio\" onclick=\"$('user_action').value='valid'\"/><span>$l_update_valid</span>
        <input name=\"cba_modif\" type=\"radio\" checked=\"checked\" onclick=\"$('user_action').value='update'\"/><span>$l_update</span>
        <fieldset class=\"buttons\">
		    	<input type=\"button\" onclick=\"$('dataUser').submit()\" id=\"popup_manage\" class=\"closeSticky\" value=\"$l_validate\" />
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$GLOBALS[l_cancel]\" />
				</fieldset>
      </div>
      ";
    $dis_archive = "
      <tr>
        <td><input name=\"cba_archive\" type=\"hidden\" value=\"1\" $archive /></td>
      </tr>
    "; 
  } else {
    $dis_archive = "
      <tr>
        <th class=\"$class[archive]\">$l_archive</th>
        <td><input name=\"cba_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
      </tr>
    "; 
  }

  // Photo
  if($photo_id > 0) {
    $block_photo_detail = "
    <tr id=\"block_photo_detail\">
      <th>&nbsp;</th>
      <td>
        <img src=\"$path/document/document_index.php?action=accessfile&document_id=$photo_id\" alt=\"\"/ />
        <input type=\"hidden\" name=\"photo_id\" value=\"$photo_id\" />
        <a href=\"#\" onclick=\"javascript:delete_user_photo();\" >$l_delete_userobmphoto</a>
      </td>
    </tr>";
  } else {
    $block_photo_detail = "";
  }

  // Profiles
  $delegation_modules = array(
    'user' => $GLOBALS['cright_write_admin'],
    //'resource' => $GLOBALS['cright_write'], 
    'host' => $GLOBALS['cright_write'],
    'people' => $GLOBALS['cright_write'],
    'group' => $GLOBALS['cright_write_admin'],
    'mailshare' => $GLOBALS['cright_write']);
  foreach ($user['profiles'] as $k_profile => $one_profile) {
    $selected = "";
    foreach($delegation_modules as $mdl => $rgt) {
      $section = $GLOBALS['cgp_show']['modules'][$mdl];
      if(Perm::check_section_right($section, $k_profile) && Perm::check_module_right($mdl, $rgt, $k_profile)) {
        $onselect .= "profiles['$k_profile'] = true\n";
        if (!isset($delegation_class) && $profile == $k_profile){
          $delegation_class = '';
          $selected = " selected";
        } 
        break;
      }
    } 
    if (!isset($delegation_class) && $profile == $k_profile){
      $delegation_class = 'H';
      $selected = " selected";
    } 

    $dis_profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
    $block_profile .= "<option value=\"$k_profile\"$selected>$dis_profile</option>";

  }
  if($cgp_use['property']['delegation']) {
    if(!isset($delegation_target)) $delegation_target = $obm['delegation'];
    $dis_delegation_target = "
    <tr class='$delegation_class' id='delegationTargetBlock'>
      <th class=\"$class[delegation_target]\">$l_delegation_target</th>
      <td>
      <input id='delegationTargetField' name=\"tf_delegation_target\" size=\"32\" maxlength=\"64\" value=\"$delegation_target\" />
      <script type='text/javascript'>
        $('delegationField').addEvent('keyup' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })
        $('delegationField').addEvent('change' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })          
      </script>
      </td>
    </tr>";

    $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);
  }

  // some constants
  $csize_add = "48";
  $cmax_add = "64";
  $csize_phone = "24";

  $display["title"] = $dis_title;

  // --- HTML Template --------------------------------------------------------
  $block = "
    $pop_up
    $pop_generate
  <form id=\"dataUser\" method=\"post\" name=\"f_entity\"
    enctype=\"multipart/form-data\"
    onsubmit=\"return (check_phoneFax_number(this) && check_user(this));\"
    action=\"" . url_prepare("user_index.php") . "\">
  <fieldset class=\"detail infos\">
  <legend>$l_user</legend>
  <script type='text/javascript'>
    var profiles = new Object();
    $onselect
    function showDelegation(field) {
      var value = field.options[field.selectedIndex].value;
      if(profiles[value]) {
        $('delegationTargetBlock').removeClass('H');
      } else {
        $('delegationTargetBlock').addClass('H');
        $('delegationTargetField').value = '';
      }
    }
    function showExpiration()	{
      var Props = $('noexperie').getProperty('checked');			
      if(Props==false){
        $('exp').setProperty('style','');
      }else{
        $('exp').setProperty('style','display:none');
      }	
    }
  </script>
  <table>
  <tr>
    <th class=\"$class[kind]\">$l_kind</th>
    <td><input name=\"tf_kind\" size=\"32\" maxlength=\"32\" value=\"$kind\" /></td>
  </tr>
  <tr>
    <th class=\"$class[lastname]\">$l_lastname</th>
    <td><input name=\"tf_lastname\" size=\"32\" maxlength=\"32\" value=\"$lname\" /></td>
  </tr>
  <tr>
    <th class=\"$class[firstname]\">$l_firstname</th>
    <td><input name=\"tf_firstname\" size=\"32\" maxlength=\"32\" value=\"$fname\" /></td>
  </tr>
  <tr>
    <th class=\"$class[login]\">$l_login</th>
    <td>$dis_login</td>
  </tr>
  <tr>
    <th class=\"$class[password]\">$l_password</th>
    <td><input id=\"passwd\" type=\"password\" name=\"tf_passwd\" size=\"24\" maxlength=\"32\" value=\"$passwd\" />
        <button type=\"button\" onclick=\"javascript:modif_password()\">$l_generate_passwd</button>
    </td>
  </tr>
  $dis_status
  $dis_domain
  <tr>
    <th class=\"$class[hidden]\">$l_hidden</th>
    <td><input name=\"cba_hidden\" type=\"checkbox\" value=\"1\" $hidden /></td>
  </tr>
  $dis_archive
  <tr>
    <th class=\"$class[profile]\">$l_profile</th>
    <td><select name=\"sel_profile\" onchange='showDelegation(this);'>
      $block_profile
      </select>
    </td>
  </tr>
  $block_delegation
  $dis_delegation_target
  <tr>
    <th class=\"$class[title]\">$l_title</th>
    <td><input name=\"tf_title\" size=\"32\" maxlength=\"32\" value=\"$title\" /></td>
  </tr>
  <tr>
    <th class=\"$class[datebegin]\">$l_datebegin</th>
    <td><input type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/></td>
  </tr>
  <tr>
    <th class=\"$class[noexperie]\">$l_noexperie</th>
    <td><input id=\"noexperie\" name=\"cba_noexperie\" type=\"checkbox\" value=\"1\" $noexperie onclick='javascript:showExpiration();'/></td>
  </tr>
  <tr id=\"exp\" style=\"$showexp\">
    <th class=\"$class[dateexp]\">$l_dateexp</th>
    <td><input type=\"text\" name=\"tf_dateexp\" value=\"$dateexp\" class=\"datePicker\"/></td>
  </tr>
  $block_photo
  $block_photo_detail
  $block_userdata 
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_coord</legend>
  <table>
  <tr>
    <th class=\"$class[phone]\">$l_phone</th>
    <td><input name=\"tf_phone\" size=\"20\" maxlength=\"20\" value=\"$phone\" /></td>
  </tr>
  <tr>
    <th class=\"$class[phone2]\">$l_phone2</th>
    <td><input name=\"tf_phone2\" size=\"20\" maxlength=\"20\" value=\"$phone2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[mobile]\">$l_mphone</th>
    <td><input name=\"tf_mobile\" size=\"20\" maxlength=\"20\" value=\"$mobile\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax]\">$l_fax</th>
    <td><input name=\"tf_fax\" size=\"20\" maxlength=\"20\" value=\"$fax\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax2]\">$l_fax2</th>
    <td><input name=\"tf_fax2\" size=\"20\" maxlength=\"20\" value=\"$fax2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[company]\">$l_company</th>
    <td><input name=\"tf_company\" size=\"64\" maxlength=\"64\" value=\"$company\" /></td>
  </tr>
  <tr>
    <th class=\"$class[direction]\">$l_direction</th>
    <td><input name=\"tf_direction\" size=\"64\" maxlength=\"64\" value=\"$direction\" /></td>
  </tr>
  <tr>
    <th class=\"$class[service]\">$l_service</th>
    <td><input name=\"tf_service\" size=\"64\" maxlength=\"64\" value=\"$service\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address1]\">$l_address 1</th>
    <td><input name=\"tf_ad1\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address2]\">$l_address 2</th>
    <td><input name=\"tf_ad2\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address3]\">$l_address 3</th>
    <td><input name=\"tf_ad3\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad3\" /></td>
  </tr>
  <tr>
    <th class=\"$class[zip]\">$l_postcode</th>
    <td><input name=\"tf_zip\" size=\"8\" maxlength=\"8\" value=\"$zip\" /></td>
  </tr>
  <tr>
    <th class=\"$class[town]\">$l_town</th>
    <td><input name=\"tf_town\" size=\"24\" maxlength=\"24\" value=\"$town\" /></td>
  </tr>
  <tr>
    <th class=\"$class[expresspostal]\">$l_expresspostal</th>
    <td><input name=\"tf_cdx\" size=\"16\" maxlength=\"16\" value=\"$cdx\" /></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_desc</legend>
  <table>
  <tr>
    <th class=\"$class[desc]\">$l_desc</th>
    <td><input name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
  </tr>
  </table>
  </fieldset>
$web_section
$mail_section
$nomad_section
$vacation_section
$samba_section

  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a user insertion or update
// When similar users exists we show these and ask confirmation
// Parameters:
//   - $p_id     : user id
//   - $usr_q    : user database result (at least 1 row)
//   - $user[]   : default values
//     keys used : login, passwd, perms, lastname, firstname, archive, email
///////////////////////////////////////////////////////////////////////////////
function dis_user_warn_insert($p_id, $usr_q, $user) {
  global $l_check_sameuser, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  // infos
  $login = $user["login"];
  $passwd = $user["passwd"];
  $profile = $user["profile"];
  $hidden = $user["hidden"];
  $kind = $user["kind"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $title = $user["title"];
  $datebegin = $user["datebegin"];
  $dateexp = $user["dateexp"];
  $email = $user["email"];
  $desc = $user["desc"];
  $archive = $user["archive"];
  $domain_id = $user["domain_id"];
  $delegation = $user["delegation"];
  $delegation_target = $user["delegation_target"];

  // coord
  $phone = $user["phone"];
  $phone2 = $user["phone2"];
  $mobile = $user["mobile"];
  $fax = $user["fax"];
  $fax2 = $user["fax2"];
  $company = $user["company"];
  $direction = $user["direction"];
  $service = $user["service"];
  $ad1 = $user["ad1"];
  $ad2 = $user["ad2"];
  $ad3 = $user["ad3"];
  $zip = $user["zip"];
  $town = $user["town"];
  $cdx = $user["cdx"];
  $country = $user["country"];

  // web
  $host_name = $user["host_name"];
  $host_id = $user["host_id"];
  $web_perms = $user["web_perms"];
  $web_list = $user["web_list"];
  $web_all = $user["web_all"];

  // mail
  $mail_perms = $user["mail_perms"];
  $mail_ext_perms = $user["mail_ext_perms"];
  $mail_server_id = $user["mail_server_id"];
  $mail_quota = $user["mail_quota"];

  // mail nomad
  $nomade_perms = $user["nomade_perms"];
  $nomade_enable = $user["nomade_enable"];
  $nomade_local_copy = $user["nomade_local_copy"];
  $nomad_email = $user["email_nomade"];

  // vacation
  $vacation_enable = $user["vacation_enable"];
  $vacation_message = $user["vacation_message"];
  $vacation_datebegin = $user["vacation_datebegin"];
  $vacation_dateend = $user["vacation_dateend"];
  
  // samba
  $smb_perms = $user["smb_perms"];
  $smb_home = $user["smb_home"];
  $smb_home_drive = $user["smb_home_drive"];
  $smb_logon = $user["smb_logon"];

  $display["msg"] .= display_warn_msg($l_check_sameuser);
  while ($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $samename = $usr_q->f("userobm_login");
    $dis_same_user .= "
        <li>
          <a href=\"" . url_prepare("user_index.php?action=detailconsult&amp;user_id=$id") . "\">$samename</a>
        </li>";
  }

  $hidden_fields = "
  <input type=\"hidden\" name=\"tf_login\" value=\"$login\" />
  <input type=\"hidden\" name=\"tf_passwd\" value=\"$passwd\" />
  <input type=\"hidden\" name=\"sel_profile\" value=\"$profile\" />
  <input type=\"hidden\" name=\"tf_kind\" value=\"$kind\" />
  <input type=\"hidden\" name=\"tf_lastname\" value=\"$lname\" />
  <input type=\"hidden\" name=\"tf_firstname\" value=\"$fname\" />
  <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
  <input type=\"hidden\" name=\"tf_datebegin\" value=\"$datebegin\" />
  <input type=\"hidden\" name=\"tf_dateexp\" value=\"$dateexp\" />
  <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
  <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
  <input type=\"hidden\" name=\"cba_archive\" value=\"$archive\" />
  <input type=\"hidden\" name=\"cba_hidden\" value=\"$hidden\" />
  <input type=\"hidden\" name=\"sel_domain_id\" value=\"$domain_id\" />
  <input type=\"hidden\" name=\"delegation\" value=\"$delegation\" />
  <input type=\"hidden\" name=\"delegation_target\" value=\"$delegation_target\" />

  <input type=\"hidden\" name=\"phone\" value=\"$phone\" />
  <input type=\"hidden\" name=\"phone2\" value=\"$phone2\" />
  <input type=\"hidden\" name=\"mobile\" value=\"$mobile\" />
  <input type=\"hidden\" name=\"fax\" value=\"$fax\" />
  <input type=\"hidden\" name=\"fax2\" value=\"$fax2\" />
  <input type=\"hidden\" name=\"company\" value=\"$company\" />
  <input type=\"hidden\" name=\"direction\" value=\"$direction\" />
  <input type=\"hidden\" name=\"service\" value=\"$service\" />
  <input type=\"hidden\" name=\"ad1\" value=\"$ad1\" />
  <input type=\"hidden\" name=\"ad2\" value=\"$ad2\" />
  <input type=\"hidden\" name=\"ad3\" value=\"$ad3\" />
  <input type=\"hidden\" name=\"zip\" value=\"$zip\" />
  <input type=\"hidden\" name=\"town\" value=\"$town\" />
  <input type=\"hidden\" name=\"cdx\" value=\"$cdx\" />
  <input type=\"hidden\" name=\"country\" value=\"$country\" />

  <input type=\"hidden\" name=\"host_name\" value=\"$host_name\" />
  <input type=\"hidden\" name=\"host_id\" value=\"$host_id\" />
  <input type=\"hidden\" name=\"web_perms\" value=\"$web_perms\" />
  <input type=\"hidden\" name=\"web_list\" value=\"$web_list\" />
  <input type=\"hidden\" name=\"web_all\" value=\"$web_all\" />

  <input type=\"hidden\" name=\"mail_perms\" value=\"$mail_perms\" />
  <input type=\"hidden\" name=\"mail_ext_perms\" value=\"$mail_ext_perms\" />
  <input type=\"hidden\" name=\"mail_server_id\" value=\"$mail_server_id\" />
  <input type=\"hidden\" name=\"mail_quota\" value=\"$mail_quota\" />

  <input type=\"hidden\" name=\"nomade_perms\" value=\"$nomade_perms\" />
  <input type=\"hidden\" name=\"nomade_enable\" value=\"$nomade_enable\" />
  <input type=\"hidden\" name=\"nomade_local_copy\" value=\"$nomade_local_copy\" />
  <input type=\"hidden\" name=\"email_nomade\" value=\"$nomad_email\" />

  <input type=\"hidden\" name=\"vacation_enable\" value=\"$vacation_enable\" />
  <input type=\"hidden\" name=\"vacation_message\" value=\"$vacation_message\" />
  <input type=\"hidden\" name=\"vacation_datebegin\" value=\"$vacation_datebegin\" />
  <input type=\"hidden\" name=\"vacation_dateend\" value=\"$vacation_dateend\" />

  <input type=\"hidden\" name=\"smb_perms\" value=\"$smb_perms\" />
  <input type=\"hidden\" name=\"smb_home\" value=\"$smb_home\" />
  <input type=\"hidden\" name=\"smb_home_drive\" value=\"$smb_home_drive\" />
  <input type=\"hidden\" name=\"smb_logon\" value=\"$smb_logon\" />
";

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_user
    </ul>
  </div>
  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" . url_prepare("user_index.php") . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"post\"
    action=\"" . url_prepare("user_index.php") . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
 </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the user delete validation screen
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function dis_user_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $l_no_user, $l_check_usercreate, $l_check_userupdate;
  global $display, $l_user, $l_infos;

  $url = url_prepare("user_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\" onsubmit=\"return confirm_del(this)\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  // Display entities where the user is referenced
  $obm_q = new DB_OBM;
  $tables = $obm_q->table_names();

  $i = 0;
  while ($tables[$i]["table_name"]) {
    $table = $tables[$i]["table_name"];

    // We exclude POSTGRES meta table (sql_xxxx)
    if ((! (substr($table, 0, 4) == "sql_"))
	&& (! (substr($table, 0, 2) == "P_"))) {
      $usercreate = $table . "_usercreate";
      $userupdate = $table . "_userupdate";

      $obm_qm = new DB_OBM;
      $fields = $obm_qm->metadata($table, false);
      $fieldexist_uc = false;
      $fieldexist_uu = false;
      $j = 0;
      while ($fields[$j]["name"]) {
	if (strtoupper($fields[$j]["name"]) == strtoupper($usercreate)) {
	  $fieldexist_uc = true;
	}
	if (strtoupper($fields[$j]["name"]) == strtoupper($userupdate)) {
	  $fieldexist_uu = true;
	}
	$j++;
      }
      if ($fieldexist_uc) {
	$query = "SELECT count(*) FROM $table WHERE $usercreate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbc = $c_q->f("0");
      }
      if ($fieldexist_uu) {
	$query = "SELECT count(*) FROM $table WHERE $userupdate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbu = $c_q->f("0");
      }
      if ($fieldexist_uc || $fieldexist_uu) {
	$dis_user .= "
       <h1>$table</h1>
       <ul>
        <li><b>$nbc</b> $l_check_usercreate</li>
        <li><b>$nbu</b> $l_check_userupdate</li>
	</ul>";
      } else {
	$dis_user .= "
        <h1>$table</h1>
	<ul>
        <li>$l_no_user</li>
	</ul>";
      }
      $dis_user .= "<hr />";
    }
    $i++;
  }
 
  $block .= "
    <div class=\"buttons\">
    $dis_delete
    $dis_back
    </div>
    <br />
    <h1>$l_user : $l_infos</h1>

    <table>
      $dis_user
    </table>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index
///////////////////////////////////////////////////////////////////////////////
function dis_user_admin_index() {

  $block = of_userdata_dis_admin_form("user");

  return $block;
}


/**
 * Display the user csv import index
 **/
function dis_user_import_index() {
  global $display, $l_user, $l_header_import, $l_enabled, $l_kind, $l_profile;
  global $l_lastname, $l_firstname, $l_login, $l_password, $l_password_type;
  global $l_mail, $l_email, $l_quota, $l_email_nomade, $l_access_nomade;
  global $l_title, $l_phone, $l_mphone, $l_fax, $l_company, $l_direction;
  global $l_service, $l_address, $l_postcode, $l_town, $l_expresspostal;
  global $l_desc, $l_group, $l_delegation;
  global $l_import_msg, $l_file_format, $l_perms, $l_local_copy;

  $block = "
  <div class=\"detail extra\">
    <h1>$l_file_format</h1>

  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>1</th>
    <th>2</th>
    <th>3</th>
    <th>4</th>
    <th>5</th>
    <th>6</th>
    <th>7</th>
    <th>8</th>
    <th>9</th>
    <th>10</th>
    <th>11</th>
    <th>12</th>
    <th>13</th>
    <th>14</th>
    <th>15</th>
    <th>16</th>
    <th>17</th>
    <th>18</th>
    <th>19</th>
    <th>20</th>
    <th>21</th>
    <th>22</th>
    <th>23</th>
    <th>24</th>
    <th>25</th>
    <th>26</th>
    <th>27</th>
    <th>28</th>
    <th>29</th>
    <th>30</th>
    <th>31</th>
    <th>32</th>
  </tr>
  <tr>
    <th>$l_kind</th>
    <th>$l_lastname</th>
    <th>$l_firstname</th>
    <th>$l_profile</th>
    <th>$l_delegation</th>
    <th>$l_login</th>
    <th>$l_password</th>
    <th>$l_password_type</th>
    <th>$l_mail</th>
    <th>$l_email</th>
    <th>$l_quota</th>
    <th>$l_email_nomade</th>
    <th>$l_access_nomade $l_perms</th>
    <th>$l_access_nomade $l_enabled</th>
    <th>$l_access_nomade $l_local_copy</th>
    <th>$l_title</th>
    <th>$l_phone</th>
    <th>$l_phone 2</th>
    <th>$l_mphone</th>
    <th>$l_fax</th>
    <th>$l_fax 2</th>
    <th>$l_company</th>
    <th>$l_direction</th>
    <th>$l_service</th>
    <th>$l_address 1</th>
    <th>$l_address 2</th>
    <th>$l_address 3</th>
    <th>$l_postcode</th>
    <th>$l_town</th>
    <th>$l_expresspostal</th>
    <th>$l_desc</th>
    <th>$l_group</th>
  </tr>
  </thead>
  <tr>
    <td>M</td>
    <td>BAUD</td>
    <td>Pierre</td>
    <td>admin</td>
    <td>dele</td>
    <td>pierre</td>
    <td>mot</td>
    <td>PLAIN</td>
    <td>X</td>
    <td>pierre.baud</td>
    <td>300</td>
    <td>pb@mymail.org</td>
    <td>X</td>
    <td>X</td>
    <td></td>
    <td>Chief</td>
    <td>+33 0 11 22 33 44</td>
    <td></td>
    <td>06 06 06 06 06</td>
    <td>05 05 05 05 05</td>
    <td></td>
    <td>OBM corp</td>
    <td>IT</td>
    <td>SI</td>
    <td>rue de la paix</td>
    <td></td>
    <td></td>
    <td>31000</td>
    <td>Ramonville</td>
    <td>cedex 5</td>
    <td>nice guy</td>
    <td>Direction, Production</td>
  </tr>
  <tr>
    <td colspan=\"35\">$l_import_msg</td>
  </tr>
  </table>
  </div>";

  $block .= html_user_import_file();

  return $block;
}


/**
 * HTML Display Import Consultation File block
 **/
function html_user_import_file() {
  global $l_user, $l_header_import, $l_file, $l_import_file;
  global $display;

  $url = url_prepare('user_index.php');

  $block = "
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"import_file\" />
    <table>
    <tr>
      <th>$l_import_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
      </td>
    </tr>
    </table>
    <fieldset class=\"buttons\">
      <input type=\"submit\" name=\"submit\" value=\"$l_import_file\" />
    </fielset>
    </form>
    </div>";

  return $block;
}


/**
 * Process Import file
 **/
function dis_user_import_file_run($params) {
  global $obm, $display, $err, $ccscd_tab, $l_user, $l_header_import, $l_group;
  global $l_delegation, $l_login, $l_lastname, $l_firstname, $l_email;
  global $l_login_in_use, $l_file_error, $l_user_import_row_cols_error;

  $file = $params['file_saved'];
  if ($file == '') {
    $file = $params['file'];
  }
  $file_name = $params['file_name'];
  $file_size = $params['file_size'];
  $csv_sep = $_SESSION['set_csv_sep'];
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }

  $array_true = array('X', 'x', 'Y', 'YES', 'y', 'yes', 'O', 'OUI', 'o', 'oui', '1');

  $cpt = 0;

  $c_kind = $cpt++;
  $c_lastname = $cpt++;
  $c_firstname = $cpt++;
  $c_profile = $cpt++;
  $c_delegation = $cpt++;
  $c_login = $cpt++;
  $c_password = $cpt++;
  $c_password_type = $cpt++;
  $c_mail = $cpt++;
  $c_email = $cpt++;
  $c_mail_quota = $cpt++;
  $c_email_nomade = $cpt++;
  $c_nomade_perms = $cpt++;
  $c_nomade_enable = $cpt++;
  $c_nomade_local_copy = $cpt++;
  $c_title = $cpt++;
  $c_phone = $cpt++;
  $c_phone2 = $cpt++;
  $c_mobile = $cpt++;
  $c_fax = $cpt++;
  $c_fax2 = $cpt++;
  $c_company = $cpt++;
  $c_direction = $cpt++;
  $c_service = $cpt++;
  $c_ad1 = $cpt++;
  $c_ad2 = $cpt++;
  $c_ad3 = $cpt++;
  $c_postcode = $cpt++;
  $c_town = $cpt++;
  $c_cdx = $cpt++;
  $c_description = $cpt++;
  $c_group = $cpt++;

  $nb_col = $cpt;

  $domain_id = $obm['domain_id'];
  $d = of_domain_get_domain_infos($domain_id, true);

  $dis_import .= "
  <thead>
  <tr>
    <th>$l_login</th>
    <th>$l_delegation</th>
    <th>$l_lastname</th>
    <th>$l_firstname</th>
    <th>$l_email</th>
    <th># $l_group</th>
    <th></th>
  </tr>
  </thead>";

  $groups = get_user_group_public_hash();
  foreach($groups as $name => $id) {
    $groups[strtolower($name)] = $id;
  }
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('userobm');
  $num_line = 0;
  $fh = fopen($file, 'r');
  if (! $fh) {
    $display['msg'] .= display_err_msg($l_file_error);
    return;
  }

  while ( ($row = fgetcsv($fh, 2000, $csv_sep)) ) {
    $num_line++;
    $nb_group_ins = 0;
    $user['user_id'] = 0;
    $user['kind'] = addslashes(trim($row[$c_kind]));
    $user['lastname'] = addslashes(trim($row[$c_lastname]));
    $user['firstname'] = addslashes(trim($row[$c_firstname]));
    $user['delegation'] = addslashes(trim($row[$c_delegation]));
    $user['login'] = addslashes(trim($row[$c_login]));
    $user['passwd'] = addslashes(trim($row[$c_password]));
    if (trim($row[$c_password_type]) == 'CRYPT') {
      $user['password_type'] = 'CRYPT';
    } else if (trim($row[$c_password_type]) == 'MD5SUM') {
      $user['password_type'] = 'MD5SUM';
    } else {
      $user['password_type'] = 'PLAIN';
    }
    //$user['passwd'] = $user['password'];
    if (in_array(trim($row[$c_mail]), $array_true)) {
      $user['mail_perms'] = 1;
    }
    $user['email'] = addslashes(trim($row[$c_email]));

    // If quota no a number (and a correct one) get the domain default value
    if (is_numeric(addslashes(trim($row[$c_mail_quota])))
	&& (addslashes(trim($row[$c_mail_quota])) > 0)
	&& (addslashes(trim($row[$c_mail_quota])) < 100000)) {
      $user['mail_quota'] = addslashes(trim($row[$c_mail_quota]));
    } else {
      $user['mail_quota'] = $d['mail_quota'];
    }
    $user['profile'] = (trim($row[$c_profile]) != '' ? addslashes(trim($row[$c_profile])) : 'user');
    $user['title'] = addslashes($row[$c_title]);
    $user['phone'] = addslashes($row[$c_phone]);
    $user['phone2'] = addslashes($row[$c_phone2]);
    $user['mobile'] = addslashes($row[$c_mobile]);
    $user['fax'] = addslashes($row[$c_fax]);
    $user['fax2'] = addslashes($row[$c_fax2]);
    $user['company'] = addslashes($row[$c_company]);
    $user['direction'] = addslashes($row[$c_direction]);
    $user['service'] = addslashes($row[$c_service]);
    $user['ad1'] = addslashes($row[$c_ad1]);
    $user['ad2'] = addslashes($row[$c_ad2]);
    $user['ad3'] = addslashes($row[$c_ad3]);
    $user['zip'] = addslashes($row[$c_postcode]);
    $user['town'] = addslashes($row[$c_town]);
    $user['cdx'] = addslashes($row[$c_cdx]);
    $user['desc'] = addslashes($row[$c_description]);

    // Delegation
    $user['delegation'] = str_replace(';', '/', $row[$c_delegation]);
    
    // Email Nomade
    if (trim($row[$c_email_nomade]) != '') {
      $user['email_nomade'] = trim($row[$c_email_nomade]);
    }
    if (in_array(trim($row[$c_nomade_perms]), $array_true)) {
      $user['nomade_perms'] = 1;
    }
    if (in_array(trim($row[$c_nomade_enable]), $array_true)) {
      $user['nomade_enable'] = 1;
    }
    if (in_array(trim($row[$c_nomade_local_copy]), $array_true)) {
      $user['nomade_local_copy'] = 1;
    }
    if (count($row) != $nb_col) {
      // Row not OK
      $user['user_id'] = false;
      $res = $l_user_import_row_cols_error;
    } else if (check_user_login_in_use($domain_id, $user['login'])) {
      $res = "$user[login] : $l_login_in_use";
    } else {
      // Insert the user
      if (check_user_data_form('', $user)) {
	$user['user_id'] = run_query_user_insert($user);
	$res = '+';
	// handle groups membership
	$group = trim(strtok(strtolower($row[$c_group]),','));
	$cpt_g = 0;
	while ($group) {
	  if (array_key_exists($group, $groups)) {
	    $cpt_g++;
	    $user["group_$cpt_g"] = $groups[$group];
	  }
	  $group = trim(strtok(','));
	}
	$user['group_nb'] = $cpt_g;
	$nb_group_ins = run_query_user_update_user_group($user);
      } else {
	// User not ok, stop for this row
	$user['user_id'] = false;
	$res = "! $err[field] - $err[msg]";
      }
    }

    $dis_import .= "
    <tr>
      <td>$user[login]</td>
      <td>$user[delegation]</td>
      <td>$user[lastname]</td>
      <td>$user[firstname]</td>
      <td>$user[email]</td>
      <td>$nb_group_ins</td>
      <td>$res</td>
    </tr>";
  }
  fclose($fh);

  $block = "
  <table class=\"spreadSheet\">
  $dis_import
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the User Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_user_display_pref($prefs) {
  global $l_user_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "user");
  $dis_pref->pref_title = $l_user_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}

/**
 * dis_user_export_pdf 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function dis_user_export_pdf($id) {
  
  $user = run_query_user_detail($id);
  $login = $user->f('userobm_login');
  $password = $user->f('userobm_password');
  $domain = $user->f("domain_name");
  $email = get_entity_email($user->f("userobm_email"), $domain, false, null);
  $phone = $user->f("userobm_phone");
  $lname = $user->f("userobm_lastname");
  $fname = $user->f("userobm_firstname");
  $display_name = $user->f("userobm_firstname").' '.$user->f("userobm_lastname");
  if($user->f("userobm_address1")) $address[] = $user->f("userobm_address1");
  if($user->f("userobm_address2")) $address[] = $user->f("userobm_address2");
  if($user->f("userobm_address3")) $address[] = $user->f("userobm_address3");
  if($user->f("userobm_address3")) $address[] = $user->f("userobm_address3");
  if($user->f("userobm_zipcode" || $user->f("userobm_town"))) $address[] = $user->f("userobm_zipcode") . ' ' . $user->f("userobm_town");
  if($user->f("userobm_country_name")) $address[] = $user->f("userobm_country_name");

  $pdf = OBM_Pdf::load('user');
  $pdf->setFontSize(11);

  // Apply font and draw text
  $pdf->pages[0]->setFont($pdf->getFont(), $pdf->getFontSize());
  $pdf->pages[0]->setFillColor(new Zend_Pdf_Color_GrayScale(1));
  $pdf->pages[0]->setLineColor(new Zend_Pdf_Color_GrayScale(0.2));
  $pdf->pages[0]->setLineDashingPattern(array(3, 2, 3, 4), 1.6);
  $lineY = 700;
  $pdf->pages[0]->drawRectangle(260, $lineY, 538, 578);   
  $pdf->pages[0]->setFillColor(new Zend_Pdf_Color_GrayScale(0));
  $lineY -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText($display_name, 280, $lineY, 'UTF-8');
  foreach($address as $address_part) {
    $lineY -= $pdf->getLineHeight();  
    $pdf->pages[0]->drawText($address_part, 280, $lineY, 'UTF-8');
  }
  $lineY = 415;
  $pdf->pages[0]->setFillColor(new Zend_Pdf_Color_GrayScale(1));
  $pdf->pages[0]->drawRectangle(60, $lineY, 538, 250);
  $pdf->pages[0]->setFillColor(new Zend_Pdf_Color_GrayScale(0));
  $lineY -= $pdf->getLineHeight();  
  $lineY -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText("OBM : ". $GLOBALS['cgp_host'], 80, $lineY, 'UTF-8');
  $lineY -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText("$GLOBALS[l_login] : ". $login, 80, $lineY, 'UTF-8');
  $lineY -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText("$GLOBALS[l_password] : ". $password, 80, $lineY, 'UTF-8');
  for($i = 0; $i < 5 && $i < count($email); $i++) {
    $mail = $email[$i];
    $lineY -= $pdf->getLineHeight();  
    $pdf->pages[0]->drawText("$GLOBALS[l_email] : ". $mail, 80, $lineY, 'UTF-8');  
    if($i == 4 && $count($email) > 4) {
      $lineY -= $pdf->getLineHeight();  
      $pdf->pages[0]->drawText("$GLOBALS[l_email] : [...]", 80, $lineY, 'UTF-8');  
    }
  }
  $lineY -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText("$GLOBALS[l_phone] : ". $phone, 80, $lineY, 'UTF-8');    
  $pdf->download($login);
}
/**
 * Build a JSON array with all search results 
 * 
 * @param $results array of 'length' => DB user results nb, and 'datas' => DB User search query results
 * @access public
 * @return void
 */
function json_search_users($user,$results) {
  global $display, $module, $cright_write_admin, $perm;

  $user_q = $results['datas'];

  $users = array();
  while($user_q->next_record()) {
    $id = $user_q->f("userobm_id");
    $label = phpStringToJsString($user_q->f("userobm_lastname")." ".$user_q->f("userobm_firstname"));
    if($user_q->f('userobm_hidden') != 1 || $perm->check_right($module, $cright_write_admin)) {
      $extra = phpStringToJsString($user_q->f("userobm_email"));
    } else {
      $extra = '';
    }
    $users[] = "{id:'data-user-$id', label:'$label', extra:'$extra'}";
  }
  $display["json"] = "{length:".$results['length'].", datas:[".implode(",",$users)."]}";
}


/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_user_mail_field($domain_id, $values, $class) {

  if ($domain_id != '') {
    $d = of_domain_get_domain_infos($domain_id, true);
    if(!empty($d['alias'])) {
      $aliases = explode("\r\n",$d['alias']);
    } else {
      $aliases = array();
    }
    array_unshift($aliases,$d['name']);

  } else {
    // Creation from GLOBAL, do not propose aliases as we don't know the domain
    $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
    $aliases = array();
  }

  if(!empty($values)) {
    $emails = explode("\r\n",$values);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $sel_alias = '';
      list($mail,$domain) = explode('@',$email);  
      foreach($aliases as $alias) {
        if( $alias == $domain) {
          $sel_alias .= "<option selected='selected' value='$alias'>$alias</option>";
        } else {
          $sel_alias .= "<option value='$alias'>$alias</option>";
        } 
      }
      $sel_alias = "<select name='sel_aliases[]'>
        <option value=''>$GLOBALS[l_all_aliases]</option>
        $sel_alias
        </select>";

      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='tf_email[]' value=\"$mail\" /> @ $sel_alias
        </div>";
      $count ++;
    }
  }
  $sel_alias = '';
  foreach($aliases as $alias) {
    $sel_alias .= "<option value=\"$alias\">$alias</option>";
    $sel_js .= "aliasSelectTemplate.adopt(new Element('option').setProperty('value','$alias').appendText('$alias'));\n";
  }
  $sel_alias = "
    <select name=\"sel_aliases[]\">
    <option value=\"\">$GLOBALS[l_all_aliases]</option>
    $sel_alias
    </select>";
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $email_block .= "
      <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
      </a>        
      <input name='tf_email[]' value='' /> @ $sel_alias 
      </div>         
      ";
    $count++;
  } 
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $add_button = "<a id='addMailButton' href=\'\' onclick=\"add_email_field(aliasSelectTemplate);show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }
  
  $email_block = "
  <th class=\"$class[email]\" id='userMailLabel'>
  $GLOBALS[l_email]
  <script type='text/javascript'>
    var aliasSelectTemplate = new Element('select').setProperty('name','sel_aliases[]');
    aliasSelectTemplate.adopt(new Element('option').setProperty('value','').appendText('$GLOBALS[l_all_aliases]'));
    $sel_js
  </script>
  $add_button
  </th>
  <td id='userMailHome'>
  $email_block
  </td>
  ";

  return $email_block;
}

function html_user_batch_form($params) {
  // Globalize detail header
  global $l_user, $l_mail, $l_nomade, $l_coord;
  // Globalize form labels
  global $l_archive, $l_profile, $l_quota, $l_perms, $l_enabled, $l_local_copy;
  global $l_phone, $l_mphone, $l_fax, $l_company, $l_direction, $l_service, $l_address, $l_postcode, $l_town, $l_expresspostal;
  // other
  global $l_yes, $l_no, $l_do_batch_processing;
  
  $l_phone2 = $l_phone." 2";
  $l_fax2 = $l_fax." 2";
  $l_address2 = $l_address." 2";
  $l_address3 = $l_address." 3";
  
  $profiles = $params['profiles'];
  
  $sel_default = array('label' => '-----', 'value' => '');
  $selected = "selected=\"selected\"";
  
  $archive = $params['archive'];
  $profile = $params['profile_name'];
  $mail_perms = $params['mail_perms'];
  $mail_quota = $params['mail_quota'];
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  
  $archive_block = "
  <select name=\"sel_archive\">
  	<option value=\"$sel_default[value]\" ".(!isset($archive) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($archive=='0' ? $selected : "").">$l_no</option>
  	<option value=\"1\" ".($archive=='1' ? $selected : "").">$l_yes</option>
  </select>
  ";
  
  $profile_block = "
  <select name=\"sel_profile_name\">
  	<option value=\"$sel_default[value]\" ".(!isset($profile_id) ? $selected : "").">$sel_default[label]</option>
  ";
  foreach($profiles as $profile_name => $profile_value) {
    $profile_block .= "<option value=\"$profile_name\" ".($profile_name==$profile ? $selected : "").">$profile_name</option>";
  }
  
  $mail_perms_block = "
  <select name=\"sel_mail_perms\">
  	<option value=\"$sel_default[value]\" ".(!isset($mail_perms) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($mail_perms=='0' ? $selected : "").">$l_no</option>
  	<option value=\"1\" ".($mail_perms=='1' ? $selected : "").">$l_yes</option>
  </select>
  ";
  
  $mail_quota_block = "
  <input type=\"text\" name=\"tf_mail_quota\" value=\"$mail_quota\" size=\"6\" maxlength=\"6\" />
  ";
  
  $nomade_perms_block = "
  <select name=\"sel_nomade_perms\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_perms) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_perms=='0' ? $selected : "").">$l_no</option>
  	<option value=\"1\" ".($nomade_perms=='1' ? $selected : "").">$l_yes</option>
  </select>
  ";
  
  $nomade_enable_block = "
  <select name=\"sel_nomade_enable\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_enable) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_enable=='0' ? $selected : "").">$l_no</option>
  	<option value=\"1\" ".($nomade_enable=='1' ? $selected : "").">$l_yes</option>
  </select>
  ";
  
  $nomade_local_copy_block = "
  <select name=\"sel_nomade_local_copy\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_local_copy) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_local_copy=='0' ? $selected : "").">$l_no</option>
  	<option value=\"1\" ".($nomade_local_copy=='1' ? $selected : "").">$l_yes</option>
  </select>
  ";
  
  $buttons_block = "
    <input type=\"hidden\" name=\"action\" value=\"batch_processing\" />
  	<input type=\"submit\" value=\"$l_do_batch_processing\" />
  ";
  if (!isset($params['data-u-id'])) {
    foreach ($params as $params_name => $params_value) {
      if (! preg_match("/^data-u-[0-9]+$/", $params_name)) {
        continue;
      } else if ($params_value == 'on') {
        $user_id = substr($params_name, strlen('data-u-'));
        $buttons_block .= "
        <input type=\"hidden\" name=\"data-u-id"."[]\" value=\"$user_id\" />
        ";
      }
    }
  } else {
    foreach ($params['data-u-id'] as $u_id) {
      $buttons_block .= "
      <input type=\"hidden\" name=\"data-u-id"."[]\" value=\"$u_id\" />
      ";
    }
  }
  
  $form_block = "
  <fieldset class=\"detail infos\">
	<legend>$l_user</legend>
	<table>
	  <tr>
	  	<th>$l_archive</th>
	  	<td>$archive_block</td>
	  </tr><tr>
	    <th>$l_profile</th>
	    <td>$profile_block</td>
	  </tr>
	 </table>
  </fieldset>
  <fieldset class=\"detail infos\">
    <legend>$l_mail</legend>
    <table>
      <tr>
        <th>$l_mail</th>
        <td>$mail_perms_block</td>
      </tr><tr>
        <th>$l_quota</th>
        <td>$mail_quota_block</td>
      </tr>
    </table>
  </fieldset>
  <fieldset class=\"detail infos\">
    <legend>$l_nomade</legend>
    <table>
      <tr>
        <th>$l_perms</th>
        <td>$nomade_perms_block</td>
      </tr><tr>
        <th>$l_enabled</th>
        <td>$nomade_enable_block</td>
      </tr><tr>
        <th>$l_local_copy</th>
        <td>$nomade_local_copy_block</th>
      </tr>
    </table>
  </fieldset>
  <fieldset class=\"detail infos\">
    <legend>$l_coord</legend>
    <table id=\"coord\">
      <tr id=\"phone\">
        <th>$l_phone</th>
        <td>".disp_tf_coord('phone', 32, $params)."</td>
      </tr><tr id=\"phone2\">
        <th>$l_phone2</th>
        <td>".disp_tf_coord('phone2', 32, $params)."</td>
      </tr><tr id=\"mobile\">
        <th>$l_mphone</th>
        <td>".disp_tf_coord('mobile', 32, $params)."</td>
      </tr><tr id=\"fax\">
        <th>$l_fax</th>
        <td>".disp_tf_coord('fax', 32, $params)."</td>
      </tr><tr id=\"fax2\">
        <th>$l_fax2</th>
        <td>".disp_tf_coord('fax2', 32, $params)."</td>
      </tr><tr id=\"company\">
        <th>$l_company</th>
        <td>".disp_tf_coord('company', 64, $params)."</td>
      </tr><tr id=\"direction\">
        <th>$l_direction</th>
        <td>".disp_tf_coord('direction', 64, $params)."</td>
      </tr><tr id=\"service\">
        <th>$l_service</th>
        <td>".disp_tf_coord('service', 64, $params)."</td>
      </tr><tr id=\"address1\">
        <th>$l_address</th>
        <td>".disp_tf_coord('address1', 64, $params)."</td>
      </tr><tr id=\"address2\">
        <th>$l_address2</th>
        <td>".disp_tf_coord('address2', 64, $params)."</td>
      </tr><tr id=\"address3\">
        <th>$l_address3</th>
        <td>".disp_tf_coord('address3', 64, $params)."</td>
      </tr><tr id=\"zipcode\">
        <th>$l_postcode</th>
        <td>".disp_tf_coord('zipcode', 14, $params)."</td>
      </tr><tr id=\"town\">
        <th>$l_town</th>
        <td>".disp_tf_coord('town', 64, $params)."</td>
      </tr><tr id=\"expresspostal\">
        <th>$l_expresspostal</th>
        <td>".disp_tf_coord('expresspostal', 16, $params)."</td>
      </tr>
    </table>
  </fieldset>
  <fieldset class=\"buttons\">
  	$buttons_block
  </fieldset>
  ";
        
  $js_block = "
  <script type=\"text/javascript\">
  	fill_coords();
  </script>
  ";
  
  $style_block = "
  <style>
  	.icons {
  		padding-left: 0.3em;
  	}
  </style>
  ";
  
  $form_block = "
  $style_block
  <form method=\"post\">
  	$form_block
  	$js_block
  </form>
  ";
  
  return $form_block;
}

// Display tf_ coord function
function disp_tf_coord($name, $size, $params) {
  
  $field_to_update = "to_update_$name";
  if (!isset($params[$field_to_update])) {
    $value = 'false';
  } else {
    $value = $params[$field_to_update];
  }
  
  $block = "
  	<input type=\"hidden\" name=\"$field_to_update\" value=\"$value\" />
  	<input type=\"text\" name=\"tf_$name\" size=\"$size\" maxlengt=\"$size\" value=\"$params[$name]\" />
  ";
  
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_user_wait_list($user) {
  global $l_found, $l_no_found;
  global $display, $obm;
  
  $user['archive'] = 1;
  $user['status'] = "INIT";
  $user['delegation'] = $obm['delegation'];

  $popup = $user["popup"];
  $filter_entity = $user["filter_entity"];
  
  $obm_q = run_query_user_search($user);

  $prefs = get_display_pref($obm["uid"], "user");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $l_found");
    $block = html_user_wait_list($obm_q, $prefs, $user, $popup);
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Waiting people Search result
// Parameters:
//   - $obm_q    : database result (people list)
//   - $prefs    : the fields which have to be displayed
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
///////////////////////////////////////////////////////////////////////////////
function html_user_wait_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add;

  $lname = urlencode($user["lastname"]);
  $fname = urlencode($user["firstname"]);
  $desc = urlencode($user["desc"]);

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  $url = url_prepare("user_index.php?action=search&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;tf_email=$email&amp;tf_desc=$desc$url_userdata$url_ext");
  $user_d = new OBM_DISPLAY("DATA", $prefs, "user");

  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_user_data");
  $block .= $display_popup_end;

  return $block;
}
?>
