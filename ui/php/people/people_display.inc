<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : people_display.inc                                             //
//     - Desc : People Display functions File                                  //
// 2008-10-08 : Vincent Bernard                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id: people_display.inc 3103 2008-09-30 09:19:00Z benoitc $ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["userobm_lastname"] = $l_lastname;
$fieldnames["userobm_firstname"] = $l_firstname;
$fieldnames["userobm_title"] = $l_title;
$fieldnames["userobm_phone"] = $l_phone;
$fieldnames["userobm_phone2"] = $l_phone2;
$fieldnames["userobm_mobile"] = $l_mphone;
$fieldnames["userobm_fax"] = $l_fax;
$fieldnames["userobm_fax2"] = $l_fax2;
$fieldnames["userobm_email"] = $l_email;
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
  global $perm, $cright_write_admin, $module, $cright_read_admin, $cright_read;

  $ext_url = $params["ext_url"];
  $ext_element = $params["ext_element"];

  if (($fieldname == "userobm_lastname")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_user_get_id(".$OD->data_set->f("userobm_id").",'".addslashes($OD->data_set->f("userobm_lastname"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_user_get_id_url('$ext_url',".$OD->data_set->f("userobm_id").");";
    } else {
      if ($link_ok && $perm->check_right($module, $cright_read))
        $res["url"] = "$path/people/people_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("userobm_id");
    }
  }

  if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-user-".$OD->data_set->f("userobm_id")."\" style=\"display:none;\">".
    $OD->data_set->f("userobm_lastname")." ".$OD->data_set->f("userobm_firstname").
    "</span>";
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

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_lastname;
  global $l_email, $l_phone, $l_desc, $l_find, $l_all, $l_firstname;
  global $display, $c_all, $obm;
  
  $lname = stripslashes($user["lastname"]);
  $fname = stripslashes($user["firstname"]);
  $email = $user["email"];
  $phone = $user["phone"];
  $desc = $user["desc"];
  
  if ($user["filter_entity"]) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$user[filter_entity]\" />";
    if($user['filter_pattern'])
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"$user[filter_pattern]\" />";
    else
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\" />";

  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('user', $user);

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("people_index.php")."\" class=\"search\"> 

    <label>$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"16\" maxlength=\"32\"
      value=\"$lname\" />
    </label>
    <label>$l_firstname<br />
      <input type=\"text\" name=\"tf_firstname\" size=\"16\" maxlength=\"32\"
      value=\"$fname\" />
    </label>
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
    $block_userdata
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
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

  $filter_entity = $user["filter_entity"];
  $obm_q = run_query_user_search($user);

  $prefs = get_display_pref($obm["uid"], "people");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $l_found");
    $block = html_user_search_list($obm_q, $prefs, $user);
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
///////////////////////////////////////////////////////////////////////////////
function html_user_search_list($obm_q, $prefs, $user) {
  global $l_close, $l_add;

  $lname = urlencode($user["lastname"]);
  $fname = urlencode($user["firstname"]);
  $email = urlencode($user["email"]);
  $desc = urlencode($user["desc"]);

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  $url = url_prepare("people_index.php?action=search&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;tf_email=$email&amp;tf_desc=$desc$url_userdata$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $prefs, "people");
  
  $user_d->data_export = false;
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_user_data");

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
  global $l_kind, $l_lastname, $l_firstname;
  global $l_people, $l_desc;
  global $l_coord, $l_address, $l_postcode, $l_town, $l_expresspostal, $l_title;
  global $l_direction, $l_service;
  global $l_company, $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_email;
  global $l_mail, $l_mail_ext, $l_email;
  global $l_perms, $l_empty;
  global $perm, $cright_write_admin;
  global $l_enabled, $l_disabled, $module;
  global $obm, $cgp_use, $path, $display, $l_yes, $l_no;
  

  $id = $obm_q->f("userobm_id");
  $kind = $obm_q->f("userobm_kind");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $title = $obm_q->f("userobm_title");
  $desc = $obm_q->f("userobm_description");

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

  $email = $obm_q->f("userobm_email");
  
  $block_userdata .= of_userdata_dis_entity_consult('user', $id);

 
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
 
  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "$fname $lname";

  $block = "
<div class=\"detail infos\">
  <h1>$l_people</h1>
 
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
    <th>$l_title</th>
    <td>$title</td>
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
$mail_section
";

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
  global $l_kind, $l_lastname, $l_firstname;
  global $l_people, $l_title, $l_update; 
  global $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_desc;
  global $l_coord, $l_company, $l_direction, $l_service, $l_address;
  global $l_postcode, $l_town, $l_expresspostal;
  global $l_perms, $l_access, $l_empty;
  global $l_enabled, $l_disabled, $path;
  global $path, $display, $cgp_use, $action;
  global $obm, $c_default_domain, $c_renameUserMailbox, $l_header_new, $l_hour;
  global $ccalendar_first_hour, $ccalendar_last_hour;
	
  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    // infos
    $id = $usr_q->f("userobm_id");
    $lastaccess = $usr_q->f("lastaccess");
    $kind = $usr_q->f("userobm_kind");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $title = $usr_q->f("userobm_title");
    $desc = $usr_q->f("userobm_description");

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

    $dis_title = "$fname $lname";
  }

  // If parameters have been given, they supercede the default action value
  // infos
  if (isset($user["user_id"])) { $id = $user["user_id"]; }
  if (isset($user["kind"])) { $kind = $user["kind"]; }
  if (isset($user["lastname"])) { $lname = stripslashes($user["lastname"]); }
  if (isset($user["firstname"])) { $fname = stripslashes($user["firstname"]); }
  if (isset($user["title"])) { $title = stripslashes($user["title"]); }
  if (isset($user["desc"])) { $desc = $user["desc"]; }

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

  $block_userdata = of_userdata_dis_entity_form('user', $id, $user);

  // Mark the error field
  if ($field != "") {
    $class[$field] = "error";
  }

  if (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";
  }

  $csize_phone = "24";

  $display["title"] = $dis_title;

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
    enctype=\"multipart/form-data\"
    onsubmit=\"if (check_user(this)) return true; else return false;\"
    action=\"" . url_prepare("people_index.php") . "\">
  <fieldset class=\"detail infos\">
  <legend>$l_people</legend>
  
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
    <th class=\"$class[title]\">$l_title</th>
    <td><input name=\"tf_title\" size=\"32\" maxlength=\"32\" value=\"$title\" /></td>
  </tr>
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

  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  </form>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: the User Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_user_display_pref($prefs) {
  global $l_people_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "people");
  $dis_pref->pref_title = $l_people_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
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
function dis_user_mail_field($values,$class) {
  $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
  if(!empty($d['alias'])) {
    $aliases = explode("\r\n",$d['alias']);
  } else {
    $aliases = array();
  }
  array_unshift($aliases,$d['name']);
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
        <input name='tf_email[]' value='$mail' /> @ $sel_alias
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



?>
