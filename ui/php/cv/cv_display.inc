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



/**
* OBM - File : cv_display.php 
* cv Display File
* @author Vincent Coulette
*
* @param integer $Id cv_display.inc,v 1.66 2006/05/15 07:56:29 pierre Exp $
*/

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//

//cv
$fieldnames["cv_title"] = $l_title;
$fieldnames["lastname"] = $l_lastname;
$fieldnames["firstname"] = $l_firstname;
$fieldnames["timeupdate"] = $l_timeupdate;
$fieldnames["timecreate"] = $l_timecreate;

//cv_reference
$fieldnames["project_name"] = $l_ref;
$fieldnames["projectcv_role"] = $l_role;
$fieldnames["project_reference_date"] = $l_project_date;
$fieldnames["project_reference_duration"] = $l_project_duration;
$fieldnames["project_reference_desc"] = $l_project_desc;
$fieldnames["project_reference_tech"] = $l_project_tech;

/**
* Display cv specific dataset fields
* @ param object $OD  OBM_DISPLAY object (passed by reference)
* @ param string $fieldname field to display
* @ param bool $link_ok true if links must be displayed
* @ return array $res  hash with 'name', 'url', 'align' values
*/

function dis_data_cv(&$OD, $fieldname, $link_ok) {
  global $path, $params;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  global $cck_customer, $cck_supplier, $l_cck_customer, $l_cck_supplier;

  $ext_url = $params["ext_url"];

//cv


  if ($fieldname == "cv_title") {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_cv_get_id(".$OD->data_set->f("cv_id").",'".addslashes($OD->data_set->f("cv_title"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_cv_get_id_url('$ext_url',".$OD->data_set->f("cv_title").");";
    } else {
      $res["url"] = "$path/cv/cv_index.php?action=detailconsult&amp;cv_id=".$OD->data_set->f("cv_id");
    }
  }

  else if ($fieldname == "lastname") {
    $res["name"] = $OD->data_set->f("userobm_lastname");
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("cv_userobm_id");
  }
  
  else if ($fieldname == "firstname") {
    $res["name"] = $OD->data_set->f("userobm_firstname");
  }

  else if ($fieldname == "timeupdate") {
    $res["name"] = of_date_format($OD->data_set->f("userobm_education"));
  }

  else if ($fieldname == "timecreate") {
    $res["name"] = of_date_format($OD->data_set->f("userobm_timecreate"));
  }
  

  // Cv_reference
  else if ($fieldname == "project_name") {
    $res["name"] = $OD->data_set->f("project_name");
    $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;project_id=".$OD->data_set->f("project_id");
  }
  
  return $res;
}


/**
* Display cv search form
* @param array $cv[]
* @return string $block display html search form
*/

function dis_cv_search_form($cv) {

  $block .= html_cv_search_form($cv);

  return $block;
}


/**
* Display cv search form
* @param array $cv[] default form values
* @return string $block html search form to display
*/


function html_cv_search_form($cv) {
  global $l_find, $l_lastname, $l_firstname, $l_keyword_title, $l_keyword_education, $l_project_name, $l_deal_name, $l_select_cv;
  
  $popup = $cv["popup"];
  $lastname = $cv["lastname"];
  $firstname = $cv["firstname"];
  $keyword_title = $cv["keyword_title"];
  $keyword_education = $cv["keyword_education"];
  $project_name = $cv["project_name"];
  $deal_name = $cv["deal_name"];

  if ($popup) {
    $ext_action = $cv["ext_action"];
    $ext_url = $cv["ext_url"];
    $ext_id = $cv["ext_id"];
    $ext_title = ($cv["ext_title"] ? $cv["ext_title"] : $l_select_cv);
    $ext_target = $cv["ext_target"];
    $ext_widget = $cv["ext_widget"];
    $ext_widget_text = $cv["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
            <input name=\"popup\" type=\"hidden\" value=\"$popup\" />";
    $display["title"] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" class=\"search\" name=\"f_search\" action=\"cv_index.php\">
   <label>$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"20\" maxlength=\"30\" value=\"$lastname\" />
    </label>
    <label>$l_firstname<br />
      <input name=\"tf_firstname\" size=\"20\" maxlength=\"30\" value=\"$firstname\" />
    </label>
    <label>$l_keyword_title<br />
      <input type=\"text\" name=\"tf_keyword_title\" size=\"20\" value=\"$keyword_title\" />
    </label>
    <label>$l_keyword_education<br />
      <input type=\"text\" name=\"tf_keyword_education\" size=\"20\" value=\"$keyword_education\" />
    </label>
    <label>$l_project_name<br />
      <input name=\"tf_project_name\" size=\"20\" maxlength=\"30\" value=\"$project_name\" />
   </label>
    <label>$l_deal_name<br />
      <input name=\"tf_deal_name\" size=\"20\" maxlength=\"30\" value=\"$deal_name\" />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\"></p>
 </form>";
  return $block;
  
}


/**
* Display the cv search result
* @param array $cv[] cv search criteria
* keys used   : label,...
* @return string $block html cv search list to display
*/

function dis_cv_search_list($cv) {
 global $l_found, $l_no_found;
  global $display, $obm;

  $popup = $cv["popup"];
  $prefs = get_display_pref($obm["uid"], "cv");
  $obm_q = run_query_cv_search($cv);
  $nb_cv = $obm_q->num_rows_total();
  if ($nb_cv == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg($nb_cv." ".$l_found);
    $block = html_cv_search_list($obm_q, $prefs, $cv, $popup);
  }

  return $block;
}


/**
* XHTML Display the cv Search result
* @param object $cv_q cvs list
* @param string $prefs fields to display in the cvs list
* @param array $cv[]  cv search criteria
* keys used label, type, dateafter, datebefore, company_name, market
* @param $popup popup flag tell if external parameters to handle
* @return string $block html cv search list to display
*/

function html_cv_search_list($cv_q, $prefs, $cv, $popup) {
  global $l_close;
  
  if ($popup) {
    $ext_action = $cv["ext_action"];
    $ext_id = $cv["ext_id"];
    $ext_target = $cv["ext_target"];
    $ext_widget = $cv["ext_widget"];
    $ext_widget_text = $cv["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $lastname = urlencode($cv["lastname"]);
  $firstname = urlencode($cv["firstname"]);
  $keyword_title = urlencode($cv["keyword_title"]);
  $keyword_education = urlencode($cv["keyword_education"]);
  $project_name = urlencode($cv["project_name"]);
  $deal_name = urlencode($cv["deal_name"]);

  $url = url_prepare("cv_index.php?action=search&amp;tf_lastname=$lastname&amp;tf_firstname=$firstname&amp;tf_keyword_title=$keyword_title&amp;tf_keyword_education=$keyword_education&amp;tf_project_name=$project_name&amp;tf_deal_name=$deal_namer$url_ext");
  
  $cv_q->seek($first_row);
  $cv_d = new OBM_DISPLAY("DATA", $prefs, "cv");
  if ($popup) {
    $cv_d->display_link = false;
    if ($ext_url != "") {
      $cv_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $cv_d->display_ext = "get_id";
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }

  $cv_d->data_set = $cv_q;
  $cv_d->data_url = $url;
  $cv_d->data_header = "both";

  $block = $cv_d->display("dis_data_cv");

  return $block;
}


/**
* Display the CV consult
* @param array $cv[]    
* @return string $block html cv consult form to display
*/
function dis_cv_consult($params) {
  global $display, $obm;

  $id = $params["cv_id"];
  if ($id > 0) {
    $cv_q = run_query_cv_detail($id);
    $display["detailInfo"] = display_record_info($cv_q);
    $refs_q = run_query_cv_refs_detail($id);
    $prefs = get_display_pref($obm["uid"], "cv_reference");
    $block = html_cv_consult($cv, $cv_q, $refs_q, $prefs);
  }

  return $block;
}

/**
* Display CV Consult
* @param object $cv_q information about the cv
* @param array $cv CV hash infos
* @param object $refs_q information about the cv refs
* @param string $prefs user display prefs
* @return string $block html cv consult form to display
*/

function html_cv_consult($cv, $cv_q, $refs_q, $prefs){
  global $path, $display;
  global $l_yes, $l_no, $l_cv;
  global $l_personal, $l_lastname, $l_firstname, $l_education, $l_location, $l_phone, $l_additionalrefs, $l_comment;
  global $l_ref, $l_refs,  $l_project_date, $l_project_duration, $l_project_desc, $l_project_tech, $l_role, $l_project ,$l_project_duration;
  
  $id = $cv_q->f("cv_id");
  $title = $cv_q->f("cv_title");
  $additionalrefs = $cv_q->f("cv_additionnalrefs");
  $array_additionalrefs = split_additionalrefs($additionalrefs);
  $com = beautify_comment(nl2br($cv_q->f("cv_comment")));
 
  $lastname = $cv_q->f("lastname");
  $firstname = $cv_q->f("firstname");
  $location = $cv_q->f("location");
  $phone = $cv_q->f("phone");
  $education = $cv_q->f("education");

  $block_references = "";
  while ($refs_q->next_record()){
    $name = $refs_q->f("project_name");
    $role = $refs_q->f("projectcv_role");
    $desc = $refs_q->f("project_reference_desc");
    $tech = $refs_q->f("project_reference_tech");
    $date = $refs_q->f("project_reference_date");
    $duration = $refs_q->f("project_reference_duration");
    $block_references .= "
                          <div class=\"detail extra\">  
                          <h1>$l_ref : $name</h1>
                          <table>
                          <tr>
                            <th>$l_role</th>
                            <td>$role</td>
                          </tr><tr>
                            <th>$l_project_date</th>
                            <td>$duration</td>
                          </tr><tr>
                            <th>$l_project_duration</th>
                            <td>$duration</td>
                          </tr><tr>
                            <th>$l_project_desc</th>
                            <td>$desc</td>
                          </tr><tr>
                            <th>$l_project_tech</th>
                            <td>$tech</td>
                          </tr>
                          </table></div>";
  }      
  $block_additional_references = "";
   if (is_array($array_additionalrefs)) {
     $display_additional_references = false;
     $block_additional_references .="
       <div class=\"detail extra\">
       <h1>$l_additionalrefs</h1>";
     for ($i=0; $i<count($array_additionalrefs); $i++){
       $adref = $array_additionalrefs[$i];
       if (is_array($adref) && count($adref) == 6) {
         $display_additional_references = true;
         $block_additional_references.= "
         <table class=\"spreadSheet\">
         <thead>
         <tr>
           <th>$l_project_date</th>
           <th>$l_project_duration</th>
           <th>$l_project</th>
           <th>$l_role</th>
           <th>$l_project_desc</th>
            <th>$l_project_tech</th>
          </tr>
          <tbody>
          <tr>
          <td> ".$adref[0]."</td>
          <td> ".$adref[1]."</td>
          <td> ".$adref[2]."</td>
          <td> ".$adref[3]."</td>
          <td> ".nl2br($adref[4])."</td>
          <td> ".nl2br($adref[5])."</td>
         </tr>
         </tbody>
         </table></div>";
       }
     }  
    if (!$display_additional_references)
      $block_additional_references = "";
  }
  
  // --- HTML Template --------------------------------------------------------

  //$display["link"] = html_cv_links($id, $label, $tech_id, $deal_id, $inc_nb, $last_inc);
  $display["title"] = $title;


  $block = "
  <!--<div class=\"detailHead\">$l_personal</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_lastname</td>
    <td class=\"detailText\">$lastname</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_firstname</td>
    <td class=\"detailText\">$firstname</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_location</td>
    <td class=\"detailText\">$location</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailText\">$phone</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_education</td>
    <td class=\"detailText\">$education</td>
  </tr>
  </table>    -->  ";

  //$block_references
  
  // Static Contacts
  $nb_ref = $refs_q->num_rows_total();
  if ($nb_ref == 0) {
    $message = $l_refs;
  } else {
    $message = "$nb_ref $l_refs";
  }

  $block .= display_info_msg($message);
  
  $block .= "<div class=\"detail extra\"><h1>$l_refs</h1></div>";

  if ($nb_ref != 0) {

    $url=url_prepare("cv_index.php?action=detailconsult&amp;cv_id=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $prefs, "cv", "cv_reference", "reference");
    $dis_infos->data_set = $refs_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";
    $dis_infos->data_form_end = "";

    $block .= $dis_infos->display("dis_data_cv");

  }

  $block .= "

  $block_additional_references

  <div class=\"detail extra\">
  <h1>$l_comment</h1>
  $com
  </div>";

  return $block;
}

/**
* Display Publication Form
* @param string $action action called
* @param array $cv hash values
* @return string $block html cv form to display
*/

function dis_cv_form($action, $cv) {
  global $display, $l_query_error, $obm;

  // detailupdate
  if ($cv["cv_id"] > 0) {
    $cv_q = run_query_cv_detail($cv["cv_id"]);
    $users = array($contract["market"], $contract["tech"]);
    $users_c = of_usergroup_get_group_users($obm['group_com'], true, $users);
    if ($cv_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($cv_q);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $cv_q->query . " !");
    }
    $refs_q = run_query_cv_refs_detail($cv["cv_id"]);
  }
  // new
  else {
    $cv_q = "";
    $refs_q = "";
    $users = array($contract["market"], $contract["tech"]);
    $users_c = of_usergroup_get_group_users($obm['group_com'], true, $users);
  }

  $block = html_cv_form($action, $cv, $cv_q, $refs_q, $users_c);

  return $block;
}


/**
* XHTML Display CV Form
* @param string $action action called
* @param array $cv[] default values
* @param array $users_c[] users
* @return string $block html cv form to display
*/
function html_cv_form($action, $cv, $cv_q="", $refs_q="", $users_c) {
  global $display, $l_none,  $path;
  global $l_update, $l_insert, $l_delete;
  global $l_cv, $l_title, $l_comment, $l_user, $l_select_user, $l_duplicate;
  global $l_lastname, $l_firstname, $l_location, $l_phone, $l_education, $l_personal;
  global $l_refs, $l_add_ref, $l_add_additionalref, $l_select_ref, $l_project, $l_role, $l_additionalrefs;
  global $l_project_desc, $l_project_tech, $l_project_date, $l_project_duration;


  // if update or duplicate mode and first display values are taken from database (except user for duplicate)
  if ($action == "detailupdate" || $action == "duplicate") {
    if ($action == "detailupdate") {
    $user_id = $cv_q->f("cv_userobm_id");
    }
    $id = $cv_q->f("cv_id");
    $title = $cv_q->f("cv_title");
    $additionalrefs = $cv_q->f("cv_additionnalrefs");
    $array_additionalrefs = split_additionalrefs($additionalrefs);
    $comment = $cv_q->f("cv_comment");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($cv["id"])) { $id = $cv["cv_id"]; }
  if (isset($cv["title"])) { $title = stripslashes($cv["title"]); }
  if (isset($cv["comment"])) { $comment = stripslashes($cv["comment"]); }
  if (isset($cv["user_id"])) { $user_id = $cv["user_id"]; }
  
 
  // retrieve the selected user infos
  if ($user_id > 0) {
    $usr_q = run_query_user_detail($user_id); 
    $lastname =  $usr_q->f("userobm_lastname");
    $firstname =  $usr_q->f("userobm_firstname");
    $location =  $usr_q->f("userobm_location");
    $phone =  $usr_q->f("userobm_phone");
    $education =  $usr_q->f("userobm_education");
  }


  if (($action == "detailupdate") || ($action == "update") || ($action == "duplicate")) {
    // buttons
    if ($action == "duplicate") {
      $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" id=\"tf_user_id\" name=\"tf_user_id\" value=\"$user_id\" />
      <input type=\"submit\" value=\"$l_duplicate\" />";
    }
    else {
      $dis_button = "
        <!-- Update button -->
        <input type=\"hidden\" name=\"cv_id\" id=\"cv_id\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
        <input type=\"submit\" value=\"$l_update\" />";
    }
      
   // refs taken from database for duplicate and update
   $block_refs = "";
    while ($refs_q->next_record()){
      $project_id = $refs_q->f("project_id");
      $name = $refs_q->f("project_name");
      $role = $refs_q->f("projectcv_role");
      $block_refs .= "<tr>
                        <td>$name</td>
                        <td><input name=\"tf_project_role[$project_id]\" type=\"text\" size=\"64\" maxlength=\"128\" value=\"$role\" />
                         <input name=\"project_id[]\" type=\"hidden\" value=\"$project_id\" /></td>
                        <td><a href=\"\" onclick=\"deleteRow(this) ;return false;\">$l_delete</a></td>
                      </tr>";
    } 
   // aditionalrefs formatting
   $block_additionalrefs = "";
   if (is_array($array_additionalrefs)) {
     for ($i=0; $i<count($array_additionalrefs); $i++){
       $adref = $array_additionalrefs[$i];
       if (is_array($adref) && count($adref) == 6) {
         $block_additionalrefs.= "<tr>
                                    <td><input name=\"tf_additionalrefs_date[]\" type=\"text\" size=\"8\"  value=\"".$adref[0]."\" /></td>
                                    <td><input name=\"tf_additionalrefs_duration[]\" type=\"text\" size=\"8\"  value=\"".$adref[1]."\" /></td>
                                    <td><input name=\"tf_additionalrefs_project[]\" type=\"text\" size=\"32\"  value=\"".$adref[2]."\" /></td>
                                    <td><input name=\"tf_additionalrefs_role[]\" type=\"text\" size=\"32\"  value=\"".$adref[3]."\" /></td>
                                    <td><textarea name=\"ta_additionalrefs_desc[]\">".$adref[4]."</textarea></td>
                                    <td><textarea name=\"ta_additionalrefs_tech[]\">".$adref[5]."</textarea></td>
                                    <td><a href=\"\" onclick=\"deleteRow(this) ;return false;\">$l_delete</a></td>
                                  </tr>";
       }
     }  
   }
   
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" id=\"user_id\" name=\"user_id\" value=\"$user_id\" />
      <input type=\"submit\" value=\"$l_insert\" />";
      
  }

  $display["title"] = $title;

  
  // Marketing manager select
  $sel_user = "<select name=\"sel_user_id\">
    <option value=\"$c_undef\">$l_undef</option>";
  foreach ($users_c as $u_id => $one_user) {
    $sel_user .= "\n<option value=\"$u_id\"";
    if ($u_id == $market_id) 
      $sel_user.= " selected=\"selected\"";
    $sel_user .= ">". $one_user['lastname']." ".$one_user['firstname']."</option>";
  }
  $sel_market.="</select>";


  // --- HTML Template --------------------------------------------------------
  
  $url = "$path/project/project_index.php?action=ext_get_id_cv&amp;ext_action=ext_get_id_cv&amp;popup=1";
  $js_project = "popup('$url','$l_select_ref'); return false;";

  $block = "
    <form method=\"post\" name=\"f_cv\"
      onsubmit=\"if (check_cv(this)) return true; else return false;\"
      action=\"".url_prepare("cv_index.php")."\">
    
    <fieldset class=\"detail extra\">
    <legend>$l_personal</legend>
     <table>
     <tr>
     <th>
     $l_select_user
     </th>
     <td>
     $sel_user
     </td>
     </tr>
     </table>
    </fieldset>

    <fieldset class=\"detail extra\">
    <legend>$l_title</legend>
    <table>
    <tr>
      <th>$l_title</th>
      <td><input type=\"text\" name=\"tf_title\" maxlength=\"255\" size=\"70\" value=\"$title\" /></td>
    </tr>
    </table>
    </fieldset>


    <fieldset class=\"detail extra\">
    <legend>$l_refs</legend>
    <table class=\"spreadSheet\" id=\"table_refs\">
      <thead>
      <tr>
        <th> $l_project </th>
        <th> $l_role </th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      $block_refs
      </tbody>
      </table>

      <fieldset class=\"buttons\">
        <input onclick=\"$js_project\" value=\"$l_add_ref\"  type=\"button\" />
      </fieldset>
    </fieldset>

    <fieldset class=\"detail extra\">
    <legend>$l_additionalrefs</legend>
    <table class=\"spreadSheet\" id=\"table_additionalrefs\">
      <thead>
      <tr>
        <th>$l_project_date</th>
        <th>$l_project_duration</th>
        <th>$l_project</th>
        <th>$l_role</th>
        <th>$l_project_desc</th>
        <th>$l_project_tech</th>
        <th></th>
       </tr>
       </thead>
       <tbody>
       $block_additionalrefs
      </tbody>
      </table>
      <fieldset class=\"buttons\">
        <input onclick=\"newRow2(); return false;\" value=\"$l_add_additionalref\"  type=\"button\" />
      </fieldset>
    </table>
    </fieldset>

    <fieldset class=\"detail extra\">
    <legend>$l_comment</legend>
      <textarea id=\"ta_comment\" name=\"ta_comment\" rows=\"8\" cols=\"80\">$comment</textarea>
    </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>";

  return $block;
}


/**
* Display the cv delete validation screen
* @param integer $p_id cv id
* @return string $block html cv form delete to display
*/
function dis_can_delete_cv($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("cv_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"cv_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"cv_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_cv_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_delete</p>
    <p class=\"detailButtons\">$dis_back</p>
    </div>";

  return $block;
}


/**
* Display the CV Display preference screen
* @param $prefs Display preferences
* @return string $block html cv form preferences to display
*/
function dis_cv_display_pref($prefs) {
  global $l_cv_display;
  
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "cv");
  $dis_pref->pref_title = $l_cv_display;
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $block = $dis_pref->display();
  
  return $block;
}


/**
* Display the CV Export parameters screen
* @param array $params
* @return string $block html cv export form to display
*/
function dis_cv_export($params) {

  $defaults_q = of_get_defaultodttemplates("cv");
  $block = html_cv_export($params["cv_id"], $defaults_q);
  return $block;
}


/**
* Display the CV Export parameters screen (html)
* @param  integer $cv_id cv id
* @param object $default_q DB OBM object
* @return string $block html cv export form to display
*/
function html_cv_export($cv_id, $defaults_q) {
  global $path;
  global $l_export, $l_anonymous, $patch, $l_template, $l_select_template, $l_template_selection;
  global $l_template_kind, $l_default_template, $l_other_template, $l_unreferenced_template;
  
  $url_sel_document = "$path/document/document_index.php?action=ext_get_id_from_path&amp;popup=1&amp;ext_widget=f_export.tf_other_template&amp;ext_widget_text=f_export.tf_other_template_name&amp;ext_disp_file=true&amp;ext_title=".urlencode($l_select_template);
  $ext_sel_document = "<a href=\"\" onclick=\"popup('$url_sel_document',''); return false;\">$l_select_template</a>";
  
  $sel_default_template = "<select name=\"sel_default_template\">";
  while ($defaults_q->next_record()){
    $id = $defaults_q->f("document_id");
    $label = $defaults_q->f("defaultodttemplate_label");
    $sel_default_template .= "<option value=\"$id\">$label</option>";
  }
  $sel_default_template .= "</select>";
  
  $block = "<fieldset class=\"detail extra\">
            <legend>$l_export</legend>
            <form name=\"f_export\" method=\"post\" action=\"$path/cv/cv_index.php\">
            <table>
            <tr>
              <th>$l_anonymous</th>
              <td><input type=\"checkbox\" name=\"cb_anonymous\" value=\"1\"></input></td>
            </tr>
            <tr>
              <th> $l_template_kind </th>
              <td> ";
              
  if ($defaults_q->num_rows() > 0) {
    $block .= " <label> <input onclick=\"showHide(0)\" type=\"radio\" value=\"0\" name=\"rd_template_kind\" checked=\"checked\"/> $l_default_template </label> &nbsp;";
    $checked = "";
  }
  else {
    $checked = "checked=\"checked\"";
  }
  
  $block .= "   <label> <input onclick=\"showHide(1)\" type=\"radio\" value=\"1\" name=\"rd_template_kind\" $checked /> $l_other_template </label> &nbsp;";
                /* <label> <input onclick=\"showHide(2)\" type=\"radio\" value=\"2\" name=\"rd_template_kind\" /> $l_unreferenced_template </label> */
  $block .= "   </td>
            </tr>";
            
  if ($defaults_q->num_rows() > 0){
    $block .= "
      <tr id=\"default_template\">
        <th>$l_template_selection</th>
        <td>$sel_default_template</td>              
      </tr>";
      $style_other_template = "style=\"display: none;\"";
  }
  
  $block .= "<tr $style_other_template id =\"other_template\">
              <th>$l_template_selection</th>
              <td>
                <input readonly=\"readonly\" type=\"text\" name=\"tf_other_template_name\" size=\"50\" maxlength=\"255\" value=\"\" /> 
                <input type=\"hidden\" name=\"tf_other_template\" value=\"\"/>  
                &nbsp; $ext_sel_document
              </td>
            </tr>";
/*
            <tr style=\"display: none;\" id =\"unreferenced_template\">
              <td class=\"detailLabel\">$l_template_selection</td>
              <td class=\"detailForm\"><input type=\"text\" name=\"tf_unreferenced_template\" size=\"50\" maxlength=\"255\" /></td>
            </tr>
*/
  $block .= "            
            </table>
            </fieldset>
            <fieldset class=\"buttons\">
                <input type=\"submit\" value=\"$l_export\"/>
                <input type=\"hidden\" name=\"action\" value=\"export\"/>
                <input type=\"hidden\" name=\"cv_id\" value=\"$cv_id\" />
            </fieldset>
            </form>";
	    
  return $block;
}


/**
* Display the cv administration index
* @return string $block html admin form to display
*/
function dis_admin_index() {

  $defaults_q = of_get_defaultodttemplates("cv");
  $block = of_defaultodttemplates_dis_admin_form("cv", $defaults_q);
  return $block;
}


/**
* Display the CV Hidden Export screen
* @param array $params
* @return string $block html export form to display 
*/
function dis_cv_hidden_export($params) {
  
  $id = $params["cv_id"];
  $anonymous = $params["anonymous"];
  $template = $params["default_template"];
  $cv_q = run_query_cv_detail($id);
  $refs_q = run_query_cv_refs_detail($id);
  $data = urlencode(serialize(format_cv_data_export($cv_q, $refs_q,  $anonymous)));
  $block = html_cv_hidden_export($template, $data);
  return $block;
}


/**
* Display the CV Hidden Export screen (html call to the odt module)
* @param integer template id of template choosen
* @param string $data
* @return string $block html export form to display
*/
function html_cv_hidden_export($template, $data) {
  global $path;
  global $l_loading, $l_loaded;
  
  $submiter = "<script type=\"text/javascript\">
     window.onload = function() {
       document.f_hidden_export.submit();
       document.getElementById('message').innerHTML = '$l_loaded !';
     }
     </script>";
  
  $block = "<div class=\"messageInfo\" id=\"message\"> $l_loading </div>
     <form name=\"f_hidden_export\" method=\"post\" action=\"$path/odt/odt_index.php\">
     <input type=\"hidden\" name=\"template\" value=\"$template\"/>
     <input type=\"hidden\" name=\"data\" value=\"$data\"/>
     <input type=\"hidden\" name=\"action\" value=\"export\"/>
     </form>
     $submiter";

  return $block;
}

?>
