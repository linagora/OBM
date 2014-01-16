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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : people_display.inc
//     - Desc : People Display functions File
// 2008-10-08 : Vincent Bernard
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
$fieldnames["userobm_direction"] = $l_direction;
$fieldnames["userobm_service"] = $l_service;
$fieldnames["userobm_address"] = $l_address;
$fieldnames["userobm_zipcode"] = $l_postcode;
$fieldnames["userobm_cedex"] = $l_expresspostal;
$fieldnames["userobm_town"] = $l_town;
$fieldnames['userobm_delegation'] = $l_delegation;
$fieldnames['userobm_vacation'] = $l_vacation;

///////////////////////////////////////////////////////////////////////////////
// Display User specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_people_data(&$OD, $fieldname, $link_ok) {
  global $path,  $ico_mail, $params,$obm,$cgp_use;
  global $l_enabled, $l_disabled, $l_forbidden, $l_perms;
  global $perm, $cright_write_admin, $module, $cright_read_admin, $cright_read;

  $ext_url = $params["ext_url"];
  $ext_element = $params["ext_element"];

  if (($fieldname == "userobm_lastname")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_user_get_id(".$OD->data_set->f("id").",'".addslashes($OD->data_set->f("userobm_lastname"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_user_get_id_url('$ext_url',".$OD->data_set->f("userobm_id").");";
    } else {
      if ($link_ok && $perm->check_right($module, $cright_read)) {
        if ($OD->data_set->f("type") == "user") {
          $res["url"] = "$path/people/people_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("userobm_id");
        } else {
          $res["url"] = "$path/contact/contact_index.php?action=consult&amp;id=".$OD->data_set->f("userobm_id");
        }
      }
    }
    $res["name"] = addslashes($OD->data_set->f("userobm_lastname"));
  }

  if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-".$OD->data_set->f("type")."-".$OD->data_set->f("userobm_id")."\" style=\"display:none;\">".
    $OD->data_set->f("userobm_lastname")." ".$OD->data_set->f("userobm_firstname")."</span>";
  }

  if ($fieldname == "userobm_email") {
    $email = $OD->data_set->f("userobm_email");
    if($OD->data_set->f("hidden") == 1 && !$perm->check_right($module, $cright_write_admin)) {
      $res["align"] = "center";
      $res['name'] = '-';
    } elseif (strcmp($email ,"") != 0) {
      $email = get_entity_email($email, $OD->data_set->f("domain_name"));
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res["txt_name"] = "$email";
    }
  } 

  if ($fieldname == "userobm_vacation") {
    $vacation = $OD->data_set->f("userobm_vacation");
    $datebegin = $OD->data_set->f('userobm_vacation_datebegin');
    $dateend = $OD->data_set->f('userobm_vacation_dateend');
    $res['align'] = 'center';
    if ($vacation) {
      $res['name'] = 'X';
    } elseif($datebegin) {
      $res['name'] = "$datebegin - $dateend";
    } else {
      $res['name'] = '';
    } 
  } 
  
  if ($fieldname == "userobm_address") {
      $res['name'] = join(' ', array($OD->data_set->f('userobm_address1'),
      	$OD->data_set->f('userobm_address2'),
      	$OD->data_set->f('userobm_address3'),
      ));
  	
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
///////////////////////////////////////////////////////////////////////////////
function html_people_search_form($user) {
  global $display,$obm;
  
  $lname = stripslashes($user["lastname"]);
  $fname = stripslashes($user["firstname"]);
  $email = $user["email"];
  $phone = $user["phone"];
  $desc = $user["desc"];
  $popup = $user["popup"];
  $direction = $user["direction"];
  $service = $user["service"];
  $address = $user["address"];
  $zipcode = $user["zipcode"];
  $town = $user["town"];
  $company = $user["company"];
  
  if ($user["filter_entity"]) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$user[filter_entity]\" />";
    if($user['filter_pattern'])
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"$user[filter_pattern]\" />";
    else
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\" />";

  }

  // User defined data : XXX to do userdata from 'user' or 'people'
  $block_userdata .= of_userdata_dis_search('user', $user);
  $display_pref = get_display_pref($obm['uid'],'people');

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
      $ext_title = $GLOBALS['l_add_users'];
    }
    $display["title"] = $ext_title;
  }

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("people_index.php")."\" class=\"search\">";
  
  if ($display_pref['userobm_lastname']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_lastname]<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"16\" maxlength=\"32\"
      value=\"$lname\" />
    </label>";
  }
  if ($display_pref['userobm_firstname']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_firstname]<br />
      <input type=\"text\" name=\"tf_firstname\" size=\"16\" maxlength=\"32\"
      value=\"$fname\" />
    </label>";
  }
  if ($display_pref['userobm_email']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_email]<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" maxlength=\"32\"
      value=\"$email\" />
    </label>";
  }
  if ($display_pref['userobm_phone']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_phone]<br />
      <input type=\"text\" name=\"tf_phone\" size=\"16\" maxlength=\"16\"
      value=\"$phone\" />
    </label>";
  }
  if ($display_pref['userobm_description']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_desc]<br />
      <input type=\"text\" name=\"tf_desc\" size=\"16\" maxlength=\"32\"
      value=\"$desc\" />
    </label>";
  }
  if ($display_pref['userobm_direction']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_direction]<br />
      <input type=\"text\" name=\"tf_direction\" size=\"16\" maxlength=\"32\"
      value=\"$direction\" />
    </label>";
  }
  if ($display_pref['userobm_service']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_service]<br />
      <input type=\"text\" name=\"tf_service\" size=\"16\" maxlength=\"32\"
      value=\"$service\" />
    </label>";
  }
  if ($display_pref['userobm_address']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_address]<br />
      <input type=\"text\" name=\"tf_address\" size=\"16\" maxlength=\"32\"
      value=\"$address\" />
    </label>";
  }
  if ($display_pref['userobm_zipcode']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_postcode]<br />
      <input type=\"text\" name=\"tf_zipcode\" size=\"16\" maxlength=\"32\"
      value=\"$zipcode\" />
    </label>";
  }
  if ($display_pref['userobm_town']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_town]<br />
      <input type=\"text\" name=\"tf_town\" size=\"16\" maxlength=\"32\"
      value=\"$town\" />
    </label>";
  }
  if ($display_pref['userobm_company']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_company]<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" maxlength=\"32\"
      value=\"$company\" />
    </label>";
  }
    $block .= "
    $block_userdata
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$GLOBALS[l_find]\" />
			<input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_filter_entity
      $ext
    </label>
    <p class=\"CL\"></p>
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_people_search_list($user) {
  global $display, $obm;

  $popup = $user["popup"];

  $obm_q = run_query_people_search($user);

  $prefs = get_display_pref($obm["uid"], "people");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($GLOBALS['l_no_found']);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $GLOBALS[l_found]");
    $block = html_people_search_list($obm_q, $prefs, $user, $popup);
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
function html_people_search_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add;

  $lname = urlencode($user["lastname"]);
  $fname = urlencode($user["firstname"]);
  $email = urlencode($user["email"]);
  $desc = urlencode($user["desc"]);
  $phone = urlencode($user['phone']);
  $direction = urlencode($user['direction']);
  $service = urlencode($user['service']);
  $address = urlencode($user['address']);
  $zipcode = urlencode($user['zipcode']);
  $town = urlencode($user['town']);


  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  if ($popup) {
    $filter_entity = $user["filter_entity"];
    $filter_pattern = $user["filter_pattern"];
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext_widget = $user["ext_widget"];
    $ext_widget_text = $user["ext_widget_text"];
    $ext_element = $user["ext_element"];
    $ext_container = $user["ext_container"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;filter_entity=$filter_entity&amp;filter_pattern=$filter_pattern&amp;ext_widget_text=$ext_widget_text&amp;ext_container=$ext_container";
  }
  $url = url_prepare("people_index.php?action=search&amp;tf_firstname=$fname&amp;tf_phone=$phone&amp;tf_lastname=$lname&amp;tf_email=$email&amp;tf_direction=$direction&amp;tf_service=$service&amp;tf_address=$adress&amp;tf_zipcode=$zipcode&amp;tf_town=$town&amp;tf_desc=$desc$url_userdata$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $prefs, "people");

  if ($popup) {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "id";
    $user_d->data_cb_name = "data-user-";
    if ($ext_element != "") {
      $user_d->data_cb_name = "";
      $user_d->data_form_head = "
      <form onsubmit=\"select_fill_from_checkbox(this); return false;\">";
    } elseif ($ext_widget != "") {
      $user_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $user_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    if ($ext_element == "sel_attendees_id") {
      $user_d->data_cb_name = "";
      $user_d->data_form_head = "
      <form onsubmit=\"addFreeBusyEntities(this); return false;\">";
    }

    $user_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </div>
      </form>";

  $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
      
  }
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_people_data");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Consultation
// Parameters:
//   - $user : user database result
///////////////////////////////////////////////////////////////////////////////
function dis_people_consult($user) {
  global $display, $l_err_reference;

  $id = $user["user_id"];
  if ($id > 0) {
    $obm_q = run_query_people_detail($id);
    if ($obm_q->num_rows() == 1) {
      $block = html_people_consult($obm_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
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
function dis_people_warn_insert($p_id, $peo_q, $people) {
  global $l_check_sameuser, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  // infos
  $login = $people["login"];
  $passwd = $people["passwd"];
  $profile = $people["profile"];
  $hidden = $people["hidden"];
  $kind = $people["kind"];
  $lname = $people["lastname"];
  $fname = $people["firstname"];
  $title = $people["title"];
  $datebegin = $people["datebegin"];
  $dateexp = $people["dateexp"];
  $email = $people["email"];
  $desc = $people["desc"];
  $archive = $people["archive"];
  $domain_id = $people["domain_id"];
  $delegation = $people["delegation"];
  $delegation_target = $people["delegation_target"];

  // coord
  $phone = $people["phone"];
  $phone2 = $people["phone2"];
  $mobile = $people["mobile"];
  $fax = $people["fax"];
  $fax2 = $people["fax2"];
  $company = $people["company"];
  $direction = $people["direction"];
  $service = $people["service"];
  $ad1 = $people["ad1"];
  $ad2 = $people["ad2"];
  $ad3 = $people["ad3"];
  $zip = $people["zip"];
  $town = $people["town"];
  $cdx = $people["cdx"];
  $country = $people["country"];

  $display["msg"] .= display_warn_msg($l_check_sameuser);
  while ($peo_q->next_record()) {
    $id = $peo_q->f("userobm_id");
    $samename = $peo_q->f("userobm_login");
    $dis_same_people .= "
        <li>
          <a href=\"" . url_prepare("people_index.php?action=detailconsult&amp;user_id=$id") . "\">$samename</a>
        </li>";
  }


  $hidden_fields = "
  <input type=\"hidden\" name=\"tf_login\" value=\"$login\" />
  <input type=\"hidden\" name=\"sel_profile\" value=\"$profile\" />
  <input type=\"hidden\" name=\"tf_kind\" value=\"$kind\" />
  <input type=\"hidden\" name=\"tf_lastname\" value=\"$lname\" />
  <input type=\"hidden\" name=\"tf_firstname\" value=\"$fname\" />
  <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
  <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
  <input type=\"hidden\" name=\"sel_domain_id\" value=\"$domain_id\" />
  <input type=\"hidden\" name=\"delegation\" value=\"$delegation\" />

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

  ";

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_people
    </ul>
  </div>
  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" . url_prepare("people_index.php") . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"post\"
    action=\"" . url_prepare("people_index.php") . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
 </div>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_people_consult($obm_q) {
  global $l_kind, $l_lastname, $l_firstname;
  global $l_people, $l_desc;
  global $l_coord, $l_address, $l_postcode, $l_town, $l_expresspostal, $l_title;
  global $l_direction, $l_service;
  global $l_company, $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_email;
  global $l_mail, $l_mail_ext, $l_email;
  global $l_perms, $l_empty;
  global $perm, $cright_write_admin;
  global $l_enabled, $l_disabled, $module, $l_group,$l_group_member;
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

  $email = get_entity_email($obm_q->f("userobm_email"),$obm_q->f('domain_name'));

  // Liste des groupes de l'utilisateur
  $g_q = run_query_people_user_group($id);
  if ($g_q->num_rows() > 0) {
    $dis_group = "
    <div class=\"detail infos\">
    <h1>$l_group_member</h1>
    <table>
    <tr><td>";
    while ($g_q->next_record()) {
      $grp_id = $g_q->f('group_id');
      $grp_name = $g_q->f('group_name');
      $dis_group .= "
    </td></tr><tr>
      <th>$l_group</th>
      <td><a href=\"$path/group/group_index.php?action=detailconsult&amp;group_id=$grp_id\">$grp_name</a></td>";
    }
    $dis_group .= "
    </tr>
    </table></div>";
  }

  
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
$dis_group
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
function html_people_form($usr_q, $user, $field='') {
  global $l_kind, $l_lastname, $l_firstname, $l_login;
  global $l_people, $l_title, $l_insert, $l_update; 
  global $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_desc;
  global $l_coord, $l_company, $l_direction, $l_service, $l_address;
  global $l_postcode, $l_town, $l_expresspostal;
  global $l_perms, $l_access, $l_empty;
  global $l_enabled, $l_disabled, $path;
  global $path, $display, $cgp_use, $action;
  global $obm, $c_default_domain, $l_header_new, $l_hour;
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
  } elseif ($action == 'new'){
    $dom_infos = of_domain_get_domain_infos($obm['domain_id'],true);
    $delegation = $obm['delegation_target'];
  }

  // If parameters have been given, they supercede the default action value
  // infos
  if (isset($user["user_id"])) { $id = $user["user_id"]; }
  if (isset($user["kind"])) { $kind = $user["kind"]; }
  if (isset($user["lastname"])) { $lname = stripslashes($user["lastname"]); }
  if (isset($user["firstname"])) { $fname = stripslashes($user["firstname"]); }
  if (isset($user["title"])) { $title = stripslashes($user["title"]); }
  if (isset($user["delegation"])) { $delegation = stripslashes($user["delegation"]); }
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

/*  // login (can't be updated, so only on insert)
  if (($action == "new") || ($action == "insert")) {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id='userLogin' maxlength=\"32\" value=\"$login\" />";
  } else {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id='userLogin' maxlength=\"32\" value=\"$login\" />
                  <input type=\"hidden\" name=\"tf_old_login\" value=\"$old_login\" />";
  }*/

 // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";
  }

  $csize_phone = "24";

  $display["title"] = $dis_title;

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
    action=\"" . url_prepare("people_index.php") . "\">
  <fieldset class=\"detail infos\">
  <legend>$l_people</legend>
  
  <table>
  <tr>
    <th class=\"$class[kind]\">$l_kind</th>
    <td><input name=\"tf_kind\" size=\"32\" maxlength=\"64\" value=\"$kind\" /></td>
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
function dis_people_display_pref($prefs) {
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

  $people = $results['datas'];

  $users = array();
  foreach($people as $name => $data) {
    $id = $data["id"];
    $label = phpStringToJsString($name);
    if($data['userobm_hidden'] != 1 || $perm->check_right($module, $cright_write_admin)) {
      $extra = phpStringToJsString(get_entity_email($data['email']));
    } else {
      $extra = '';
    }
    $users[] = "{id:'data-".$data['type']."-$id', label:'$label', extra:'$extra'}";
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


///////////////////////////////////////////////////////////////////////////////
// Display: the User Display preference screen
// Parameters:
//   - $$params[] : user params
///////////////////////////////////////////////////////////////////////////////
function dis_people_wait_list($params) {
  global $display, $obm;

  $filter_entity = $params["filter_entity"];
  $popup = $params["popup"];
  $params['delegation'] = $obm['delegation_target'];
  $params["status"] = "INIT";

  $obm_q = run_query_people_search($params);

  $prefs = get_display_pref($obm["uid"], "people");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($GLOBALS['l_no_found']);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $GLOBALS[l_found]");
    $block = html_people_wait_list($obm_q, $prefs, $user, $popup);
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
function html_people_wait_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add;

  $lname = urlencode($user["lastname"]);
  $fname = urlencode($user["firstname"]);
  $desc = urlencode($user["desc"]);

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  $url = url_prepare("people_index.php?action=wait&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;tf_email=$email&amp;tf_desc=$desc$url_userdata$url_ext");
  $user_d = new OBM_DISPLAY("DATA", $prefs, "people");

  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_people_data");
  $block .= $display_popup_end;

  return $block;
}

?>
