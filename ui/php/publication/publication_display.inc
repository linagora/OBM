<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_display.inc                                      //
//     - Desc : Publication Display File                                     //
// 2004-01-28 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["publication_title"] = $l_publication_title;
$fieldnames["publication_year"] = $l_year;
$fieldnames["publication_lang"] = $l_lang;
// Calculated or indirect fields
$fieldnames["publicationtype_label"] = $l_type;


///////////////////////////////////////////////////////////////////////////////
// Display Publication specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_publication(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;
  
  if ($fieldname == "publication_title") {
    if($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("publication_id").",'".addslashes($OD->data_set->f("publication_title"))."');";
    }
    else {    
      $res["url"] = "$path/publication/publication_index.php?action=detailconsult&amp;publication_id=".$OD->data_set->f("publication_id");
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Publication search Form
// Parameters:
//   - $type_q    : database object with type list
//   - $act_q     : database object with activity list
//   - $usr_q     : database object with userobm list
//   - $publication[] : default form values
//     keys used  : archive, name, type, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_publication_search_form ($type_q,$publication) {
  global $l_publication_title, $l_publication_type, $l_lang, $l_year;
  global $l_all, $l_find,$popup; 
  global $c_all;
  
  $title = stripslashes($publication["title"]);
  $lang = stripslashes($publication["lang"]);
  $type = $publication["type"];
  $year = $publication["year"];

  if (!$popup) {
    $dis_title = "
    <div class=\"searchLabel\">$l_publication_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"24\" value=\"$title\" />
    </div>";

    $dis_year = "
    <div class=\"searchLabel\">$l_year<br />
      <input type=\"text\" name=\"tf_year\" id=\"tf_year\" size=\"4\" value=\"$year\" />
    </div>";

    $dis_button = "    
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      </div>";
  }
  else {
    $sel_js = " onchange=\"this.form.submit();\" ";
    $ext_action = $publication["ext_action"];
    $ext_url = $publication["ext_url"];
    $ext_id = $publication["ext_id"];
    $ext_title = $publication["ext_title"];
    $ext_target = $publication["ext_target"];
    $dis_button = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
	    <input name=\"popup\" type=\"hidden\" value=\"$popup\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
	    <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }
    
  // type select
  $sel_type = "<select id=\"sel_type\" name=\"sel_type\" $sel_js>
    <option value=\"$c_all\">$l_all</option>";
  while ($type_q->next_record()) {
    $t_id = $type_q->f("publicationtype_id");
    $sel_type .= "\n<option value=\"$t_id\"";
    if ($t_id == $type) { $sel_type .= " selected=\"selected\""; }
    $sel_type .= ">". $type_q->f("publicationtype_label") . "</option>\n";
  }
  $sel_type .= "</select>";

  $url = url_prepare("publication_index.php");
  
  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
  <div class=\"search\">
    $dis_title
    <div class=\"searchLabel\">$l_publication_type<br />
      $sel_type
    </div>
    $dis_year
    <div class=\"searchLabel\">$l_lang<br />
      <input type=\"text\" name=\"tf_lang\" id=\"tf_lang\" size=\"15\" 
      value=\"$lang\" />
    </div>
    $dis_button
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Publication search result
// Parameters:
//   - $publication[] : publication search criteria
//     keys used  : name, type, zip
///////////////////////////////////////////////////////////////////////////////
function dis_publication_search_list($publication) {
  global $display, $l_found, $l_no_found;
  global $auth;

  $popup = $publication["popup"];

  $prefs = get_display_pref($auth->auth["uid"], "publication");
  $obm_q = run_query_publication_search($publication);
  $nb_pub = $obm_q->num_rows_total();
  if ($nb_pub == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $display["msg"] = display_info_msg("$nb_pub $l_found");
    $block = html_publication_search_list($obm_q,$prefs,$publication,$popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $pub_q      : publication list
//   - $prefs      : the fields which have to be displayed
//   - $publication[]  : publication search criteria
//     keys used   : archive, name, type, zip
///////////////////////////////////////////////////////////////////////////////
function html_publication_search_list($pub_q, $prefs, $publication, $popup) {
  global $l_close;

  if ($popup) {
    $ext_action = $publication["ext_action"];
    $ext_url = $publication["ext_url"];
    $ext_id = $publication["ext_id"];
    $ext_target = $publication["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  // we urlencode to avoid breakage for space char
  $title = urlencode(stripslashes($publication["title"]));
  $type = $publication["type"];
  $year = $publication["year"]; 
  $lang = $publication["lang"];
 
  $url = url_prepare("publication_index.php?action=search&amp;tf_title=$title&amp;sel_type=$type&amp;tf_year=$year&amp;tf_lang=$lang$url_ext");

  $pub_d = new OBM_DISPLAY("DATA", $prefs, "publication");
  if ($popup) {
    $pub_d->display_link = false;
    $pub_d->display_ext = "get_id";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $pub_d->data_set = $pub_q;
  $pub_d->data_url = $url;
  $pub_d->data_header = "both";
  $block = $pub_d->display("dis_data_publication");
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $pub_q      : publication list
//   - $prefs      : the fields which have to be displayed
//   - $nb_pub     : nb publications returned by the search query
//   - $publication[]  : publication search criteria
//     keys used   : archive, name, type, zip
///////////////////////////////////////////////////////////////////////////////
function html_publication_subscription_search_list($pub_q, $prefs, $nb_pub,$publication,$popup) {
  global $display, $l_found,$l_close;

  // we urlencode to avoid breakage for space char
  $archive = $publication["archive"];
  $title = urlencode(stripslashes($publication["name"]));
  $phone = urlencode(stripslashes($publication["phone"]));
  $type = $publication["type"];
  $act = $publication["activity"];
  $zip = urlencode(stripslashes($publication["zip"]));
  $market = $publication["marketing_manager"];
  $popup = $publication["popup"]; 
  
  $url = url_prepare("publication_index.php?action=search&amp;tf_title=$title&amp;tf_phone=$phone&amp;sel_type=$type&amp;sel_act=$act&amp;tf_zip=$zip&amp;cba_archive=$archive&amp;sel_market=$market$url_ext");

  $pub_q->seek($first_row);

  $pub_d = new OBM_DISPLAY("DATA", $prefs, "publication");
  if ($popup) {
    $pub_d->display_ext = "get_id";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $pub_d->data_set = $pub_q;
  $pub_d->data_url = $url;
  $pub_d->data_header = "both";
  $display["msg"] = display_info_msg("$nb_pub $l_found");
  $block = $pub_d->display("dis_data_publication");
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Publication Detail screen
// Parameters:
//   - $publication[] : publication values
///////////////////////////////////////////////////////////////////////////////
function dis_publication_consult($publication) {
  global $display, $l_err_reference;

  if ($publication["publication_id"] > 0) {
    $pub_q = run_query_publication_detail($publication["publication_id"]);
    if ($pub_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($pub_q);
      $block = html_publication_consult($pub_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Publication Consultation
// Parameters:
//   - $pub_q : publication database result 
///////////////////////////////////////////////////////////////////////////////
function html_publication_consult($pub_q) {
  global $l_publication_title, $l_type, $l_year, $l_lang, $l_publication;
  global $display, $c_yes, $c_no,$l_desc;

  $id = $pub_q->f("publication_id");
  $title = $pub_q->f("publication_title");
  $type = $pub_q->f("publicationtype_label");
  $year = $pub_q->f("publication_year");
  $lang = $pub_q->f("publication_lang");
  $desc = $pub_q->f("publication_desc");

  $ldesc = nl2br($desc);
  $display["title"] = "<div class=\"title\">$l_publication : $title</div>";


  // --- HTML Template --------------------------------------------------------
  $block .= "<div class=\"detailHead\">$l_publication</div>

<table class=\"detail\">
  <tr> 
      <td class=\"detailLabel\">$l_publication_title</td>
      <td class=\"detailText\">$title</td>
    </tr>
    <tr> 
      <td class=\"detailLabel\">$l_year</td>
      <td class=\"detailText\">$year</td>
    </tr>
    <tr> 
      <td class=\"detailLabel\">$l_lang</td>
      <td class=\"detailText\">$lang</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailText\">$type</td>
    </tr>
  </table>

<div class=\"detailHead\">$l_desc</div>
  
  <table class=\"detail\">
    <tr> 
      <td class=\"detailText\">$ldesc
      </td>
    </tr>
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Publication Form
// Parameters:
//   - $action      : action called
//   - $publication : hash values
///////////////////////////////////////////////////////////////////////////////
function dis_publication_form($action, $publication) {
  global $display, $l_err_reference;

  if ($publication["publication_id"] > 0) {
    $pub_q = run_query_publication_detail($publication["publication_id"]);
    if ($pub_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($pub_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
    $concat1_q = "";
  } else {
    $pub_q = "";
    // We retrieve contact category 1 and list only on new
    // auto subscription to all contact from a category or list only on insert
    $concat1_q = run_query_publication_contactcategory1();
    $list_q = run_query_publication_list();
  }

  $type_q = run_query_publication_type();
  $recept_q = run_query_publication_subscription_reception(); 

  $block = html_publication_form($action, $pub_q, $type_q, $recept_q, $publication, $concat1_q, $list_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Publication Form
// Parameters:
//   - $action    : action called
//   - $pub_q     : publication database result 
//   - $type_q    : type database result 
//   - $recept_q  : reception modes database result 
//   - $publication[] : default values
//   - $cat1_q    : contact category
//   - $list_q    : lists
///////////////////////////////////////////////////////////////////////////////
function html_publication_form($action, $pub_q,$type_q,$recept_q, $publication,$cat1_q, $list_q) {
  global $display, $auth, $set_dsrc, $l_none, $ico_add;
  global $set_theme,$ico_web,$ico_mail,$l_category,$popup_height,$popup_width;
  global $l_publication_title, $l_type, $l_year, $l_lang, $l_desc;
  global $l_insert,$l_update,$l_checkdelete, $l_publication, $l_list;
  global $l_contact_category1,$l_auto_sub;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $pub_q->f("publication_id");
    $title = $pub_q->f("publication_title");
    $type = $pub_q->f("publication_type_id");
    $year = $pub_q->f("publication_year");
    $desc = $pub_q->f("publication_desc");
    $lang = $pub_q->f("publication_lang");    
  }
  $concat1 = array();
  $list_sub = array();
  // If parameters have been given, they supercede the default action value
  if (isset($publication["publication_id"])) { $id = $publication["publication_id"]; }
  if (isset($publication["title"])) { $title = stripslashes($publication["title"]); }
  if (isset($publication["type"])) { $type = $publication["type"]; }
  if (isset($publication["year"])) { $year = stripslashes($publication["year"]); }
  if (isset($publication["lang"])) { $lang = stripslashes($publication["lang"]); }
  if (isset($publication["desc"])) { $desc = stripslashes($publication["desc"]); }
  if (isset($publication["concat1"])) { $concat1 = $publication["concat1"]; }
  if (isset($publication["list_sub"])) { $list_sub = $publication["list_sub"]; }
 

  // type select
  $sel_type = "<select name=\"sel_type\" id=\"sel_type\">";
  while ($type_q->next_record()) {
    $k_id = $type_q->f("publicationtype_id");
    $sel_type .= "\n<option value=\"$k_id\"";
    if ($k_id == $type) { $sel_type .= " selected=\"selected\""; }
    $sel_type .= ">". $type_q->f("publicationtype_label") . "</option>\n";
  }
  $sel_type .= "</select>";
  
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"publication_id\" id=\"publication_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

    $sel_contcat1 = "<select name=\"sel_contactcategory1[]\" multiple=\"multiple\">";
    while($cat1_q->next_record()) {
      $cat_id = $cat1_q->f("contactcategory1_id");
      $cat_label = $cat1_q->f("contactcategory1_label");
      $sel_contcat1 .= "<option value=\"".$cat_id ."\" ";
      if (in_array($cat_id,$concat1)) {
	$sel_contcat1 .= "selected=\"selected\" ";
      }
      $sel_contcat1 .= ">$cat_label</option>";
    }
    $sel_contcat1 .= "</select>";

    $sel_list = "<select name=\"sel_list[]\" multiple=\"multiple\">";
    while($list_q->next_record()) {
      $l_id = $list_q->f("list_id");
      $l_name = $list_q->f("list_name");
      $l_login = $list_q->f("userobm_login");
      $sel_list .= "<option value=\"$l_id\" ";
      if (in_array($l_id, $list_sub)) {
	$sel_list .= "selected=\"selected\" ";
      }
      $sel_list .= ">$l_login : $l_name</option>";
    }
    $sel_list .= "</select>";

    $dis_auto_sub = "<div class=\"detailHead\">$l_auto_sub</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_contact_category1</td>
      <td class=\"detailForm\">
       $sel_contcat1
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_list</td>
      <td class=\"detailForm\">
       $sel_list
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\"></td>
      <td class=\"detailForm\">
      $sel_recept
      </td>
    </tr>
    </table>";      
  }

  $url = "publication_index.php?action=ext_get_cat_ids&amp;popup=1&amp;ext_target=forms[0].elements[6]"; 

  $display["title"] = "<div class=\"title\">$l_publication : $title</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_publication(this)) return true; else return false;\"
      action=\"".url_prepare("publication_index.php")."\">

    <div class=\"detailHead\">$l_publication</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_publication_title</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"30\" size=\"30\" value=\"$title\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailForm\">$sel_type</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lang</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_lang\" name=\"tf_lang\" maxlength=\"20\" size=\"20\" value=\"$lang\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_year</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_year\" name=\"tf_year\" maxlength=\"4\" size=\"4\" value=\"$year\" /></td>
    </tr>
    </table>
    
    $dis_auto_sub
    
    <div class=\"detailHead\">$l_desc</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea id=\"ta_desc\" name=\"ta_desc\" rows=\"8\" cols=\"80\">$desc</textarea></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Publication subscription Form
// Parameters:
//   - $action    : action called
//   - $pub_q      : publication database result 
//   - $dsrc_q    : datasource database result 
//   - $type_q    : type database result 
//   - $usr_q     : userobm database result 
//   - $ctry_q    : country database result 
//   - $publication[] : default values
//     keys used  : id, num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_publication_group_subscription_form($action,$pub_q, $cat1_q, $publication) {
  global $display, $auth, $set_dsrc;
  global $set_theme,$l_category;
  global $l_publication, $l_contact_category1, $l_auto_sub;

  $concat1 = array();
  $title = $pub_q->f("publication_title");
  // If parameters have been given, they supercede the default action value
  if (isset($publication["publication_id"])) { $id = $publication["publication_id"]; }
  if (isset($publication["concat1"])) { $concat1 = $publication["concat1"]; }
 

  $dis_button = "
    <input type=\"hidden\" name=\"publication_id\" id=\"publication_id\" value=\"$id\" />  
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_group_subscription\" />
    <input type=\"submit\" value=\"$l_auto_sub\" />
    ";
  $sel_contcat1 = "<select name=\"sel_contactcategory1[]\" multiple=\"multiple\"> "; 
  while($cat1_q->next_record()) {
    $cat_id = $cat1_q->f("contactcategory1_id");
    $cat_label = $cat1_q->f("contactcategory1_label");
    $sel_contcat1 .= "<option value=\"".$cat_id ."\" ";
    if ($cat_id = current($concat1)) {
      $sel_contcat1 .= "selected=\"selected\" ";
      next($concat1);
    }
    $sel_contcat1 .= ">$cat_label</option>";
  }
  $sel_contcat1 .= "</select>";

  $display["title"] = "<div class=\"title\">$l_publication : $title</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_publication(this)) return true; else return false;\"
      action=\"".url_prepare("publication_index.php")."\">

    <div class=\"detailHead\">$l_publication</div>

    <div class=\"detailHead\">$l_auto_sub</div>
   
    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_contact_category1</td>
      <td class=\"detailForm\">
       $sel_contcat1
      </td>
     </tr>   
     <tr>
      <td class=\"detailLabel\"></td>
      <td class=\"detailForm\">
      $sel_recept
      </td>
     </tr>       
    </table>    

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Subscription Form
// Parameters:
//   - $action    : action called
//   - $pub_q      : publication database result 
//   - $dsrc_q    : datasource database result 
//   - $type_q    : type database result 
//   - $usr_q     : userobm database result 
//   - $ctry_q    : country database result 
//   - $publication[] : default values
//     keys used  : id, num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_publication_subscription_form($action,$sub_q, $recept_q, $publication) {
  global $display, $authd;
  global $set_theme,$ico_publication_new,$popup_height,$popup_width;
  global $l_subscription,$l_quantity, $l_renew, $l_recept, $l_lang, $l_desc;
  global $l_insert_sub,$l_update_sub,$l_checkdelete_sub, $l_publication;

  $popup = $publication["popup"];
  $ext_url = $publication["ext_url"];

  if ($action == "detailupdate_subscription") {
   $sub_id = $sub_q->f("subscription_id");
   $pub_id = $sub_q->f("subscription_publication_id");
   $title = $sub_q->f("publication_title");
   $cont_id = $sub_q->f("subscription_contact_id");
   $quantity = $sub_q->f("subscription_quantity");
   if ($sub_q->f("subscription_renewal") == 1) $renew = " checked=\"checked\"";
   $recept = $sub_q->f("subscription_reception_id");
   $lang = $sub_q->f("publication_lang");
  }
  // If parameters have been given, they supercede the default action value
  if (isset($publication["publication_id"])) { $pub_id = $publication["publication_id"]; }
  if (isset($publication["title"])) { $title = stripslashes($publication["title"]); }
  if (isset($publication["quantity"])) { $quantity = $publication["quantity"]; }
  if (isset($publication["subscription_id"])) { $sub_id = $publication["subscription_id"]; }
  if (isset($publication["contact_id"])) { $cont_id = $publication["contact_id"]; }
  if ($publication["renew"] == 1) { $renew = " checked=\"checked\""; }
  if (isset($publication["lang"])) { $lang = stripslashes($publication["lang"]); }
  if (isset($publication["recept"])) { $recept = stripslashes($publication["recept"]); }
  

  // reception mode select
  $sel_recept = "<select name=\"sel_recept\" id=\"sel_recept\">";
  while ($recept_q->next_record()) {
    $k_id = $recept_q->f("subscriptionreception_id");
    $sel_recept .= "\n<option value=\"$k_id\"";
    if ($k_id == $recept) { $sel_recept .= " selected=\"selected\""; }
    $sel_recept .= ">". $recept_q->f("subscriptionreception_label") . "</option>\n";
  }
  $sel_recept .= "</select>";

  $url = "publication_index.php?action=ext_get_id&amp;popup=1&amp;ext_target=forms[0]&amp;tf_lang=$lang"; 
  if (($action == "detailupdate_subscription") || ($action == "update_subscription")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"subscription_id\" id=\"subscription_id\" value=\"$sub_id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update_subscription\" />
      <input type=\"submit\" value=\"$l_update_sub\" />";
     $block_publi = "$title<input type=\"hidden\" name=\"publication_id\" id=\"publication_id\" value=\"$pub_id\" />";
     	
  } elseif (($action == "new_subscription") || ($action == "insert_subscription")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_subscription\" />
      <input type=\"submit\" value=\"$l_insert_sub\" />";
    $block_publi = "      
    <a href=\"\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_publication_new\" alt=\"[New Publication]\" /> 
    </a>
    <input type=\"hidden\" name=\"publication_id\" id=\"publication_id\" value=\"$pub_id\" />
    <input readonly=\"true\" type=\"text\" name=\"tf_title\" id=\"tf_title\" value=\"$title\" />
    ";     
  }
  $dis_button .= "
      <input type=\"hidden\" id=\"contact_id\" name=\"contact_id\" value=\"$cont_id\" />
      <input type=\"hidden\" id=\"ext_url\" name=\"ext_url\" value=\"$ext_url\" />
      <input type=\"hidden\" id=\"popup\" name=\"popup\" value=\"$popup\" />";

  $display["title"] = "<div class=\"title\">$l_subscription : $contact_firstname  $pub_title</div>";

  // --- HTML Template --------------------------------------------------------
  if (!isset($quantity)) $quantity = 1;
  $block = "
    $js_ctry

    <form method=\"get\" name=\"form_subscription\"
      onsubmit=\"if (check_subscription(this)) return true; else return false;\"
      action=\"".url_prepare("publication_index.php")."\">

    <div class=\"detailHead\">$l_subscription</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
      $l_publication 
      </td>
      <td class=\"detailForm\">
	$block_publi
      </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_quantity</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_quantity\" name=\"tf_quantity\" maxlength=\"8\" size=\"4\" value=\"$quantity\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_renew</td>
      <td class=\"detailForm\">
      <input type=\"checkbox\" $renew name=\"cba_renewal\" value=\"1\" />
      </td>      
    </tr><tr>
      <td class=\"detailLabel\">$l_recept</td>
      <td class=\"detailForm\">$sel_recept</td>
    </tr>
    </table>
   <!-- 
    <div class=\"detailHead\" style=\"display:none;\">$l_subscription_dates</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\"></td>
      <td class=\"detailForm\"></td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$</td>
      <td class=\"detailForm\"></td>
    </tr>    
    </table>
   -->
    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Subscription Form
// Parameters:
//   - $action    : action called
//   - $pub_q      : publication database result 
//   - $type_q    : type database result 
//   - $usr_q     : userobm database result 
//   - $ctry_q    : country database result 
//   - $publication[] : default values
//     keys used  : id, num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_publication_auto_subscription_form($action,$pub_q,$recept_q, $publication) {
  global $display,$ico_publication_new;
  global $set_theme,$popup_height,$popup_width,$l_insert;
  global $l_publication,$l_quantity, $l_renew, $l_recept,$l_year;
  global $l_publication_dest,$l_publication_orig;
  
  $popup = $publication["popup"];

  $pub_id = $pub_q->f("publication_id"); 
  $title = $pub_q->f("publication_title"); 
  

  // reception mode select
  $sel_recept = "<select name=\"sel_recept\" id=\"sel_recept\">";
  while ($recept_q->next_record()) {
    $k_id = $recept_q->f("subscriptionreception_id");
    $sel_recept .= "\n<option value=\"$k_id\"";
    if ($k_id == $recept) { $sel_recept .= " selected=\"selected\""; }
    $sel_recept .= ">". $recept_q->f("subscriptionreception_label") . "</option>\n";
  }
  $sel_recept .= "</select>";
  $url = "publication_index.php?action=ext_get_id&amp;popup=1&amp;ext_target=forms[0]&amp;tf_lang=$lang";   
 $block_publi_dest = "      
    <a href=\"\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_publication_new\" alt=\"[New Publication]\" /> 
    </a>
    <input type=\"hidden\" name=\"publication_id\" id=\"publication_id\" value=\"\" />
    <input readonly=\"true\" type=\"text\" name=\"tf_title\" id=\"tf_title\" value=\"\" />
    ";       
  $block_publi_orig = "$title<input type=\"hidden\" name=\"param_publication_orig\" id=\"param_publication_orig\" value=\"$pub_id\" />";
   $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_auto\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
     	

  $display["title"] = "<div class=\"title\">$l_renew : $title</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    $js_ctry

    <form method=\"get\" name=\"form_auto_subscription\"
            action=\"".url_prepare("publication_index.php")."\">

    <div class=\"detailHead\">$l_subscription</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
      $l_publication_orig 
      </td>
      <td class=\"detailForm\">
	$block_publi_orig
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">
      $l_publication_dest 
      </td>
      <td class=\"detailForm\">
      $block_publi_dest
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_recept</td>
      <td class=\"detailForm\">$sel_recept</td>
    </tr><tr>
    </table>
    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the publication delete validation screen
// Parameters:
//   - $p_id : publication id
///////////////////////////////////////////////////////////////////////////////
function dis_publication_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("publication_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"publication_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"publication_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
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
// Display the context about a publication insertion or update
// When similar companies exists we show these and ask confirmation
// Parameters:
//   - $cid       : publication id
//   - $pub_q      : publication database result (at least 1 row)
//   - $publication[] : default values
/////////////////////////////////////////////////////////////////////////////
function dis_publication_warn_insert($cid, $pub_q, $publication) {
  global $display, $l_check_samepublication, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $title = $publication["title"];
  $type = $publication["type"];
  $year = $publication["year"];
  $lang = $publication["lang"];
  $desc = $publication["desc"];

  $display["msg"] .= display_warn_msg($l_check_samepublication);

  while ($pub_q->next_record()) {
    $id = $pub_q->f("publication_id");
    $sametitle = $pub_q->f("publication_title");
    $samelang = $pub_q->f("publication_lang");
    $dis_same_pub .= "
      <tr><td colspan =\"2\" class=\"detailText\">
         <a class=\"detail\" href=\"" .url_prepare("publication_index.php?action=detailconsult&amp;publication_id=$id") . "\">
          $sametitle ($samelang)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_pub
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" .url_prepare("publication_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"hidden\" name=\"tf-title\" value=\"$title\" />
    <input type=\"hidden\" name=\"sel_type\" value=\"$type\" />
    <input type=\"hidden\" name=\"tf_year\" value=\"$year\" />
    <input type=\"hidden\" name=\"tf_lang\" value=\"$lang\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("publication_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"hidden\" name=\"tf-title\" value=\"$title\" />
    <input type=\"hidden\" name=\"sel_type\" value=\"$type\" />
    <input type=\"hidden\" name=\"tf_year\" value=\"$year\" />
    <input type=\"hidden\" name=\"tf_lang\" value=\"$lang\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the publication administration index
///////////////////////////////////////////////////////////////////////////////
function dis_publication_admin_index() {

  $type_q = run_query_publication_type();
  $block = html_publication_type_form($type_q);
  $block .= "<hr />";
  $recept_q = run_query_publication_subscription_reception();
  $block .= html_publication_subscription_reception_form($recept_q);  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Publication Kind section
// Parameters:
//   - $type_q : Kind list database object
///////////////////////////////////////////////////////////////////////////////
function html_publication_type_form($type_q) {
  global $l_type_manage,$l_type_exist;
  global $l_type_checkdelete,$l_type_update,$l_type_new,$l_type_insert;

  $sel_type = "<select name=\"sel_type\" onChange=\"
    forms.form_type_update.tf_type.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($type_q->next_record()) {
    $id = $type_q->f("publicationtype_id");
    $label = $type_q->f("publicationtype_label");
    $sel_type .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_type .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_type_manage
      </td>
    </tr><tr>
      <td>

<!-- Kind Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_type_delete\" method=\"post\"
              action=\"" . url_prepare("publication_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_type_exist</td>
              <td class=\"adminLabel\">$sel_type</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"type_checklink\" />
              <input type=\"submit\" name=\"sub_type\" value=\"$l_type_checkdelete\" onclick=\"if (check_type_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Kind Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_type_update\" method=\"get\"
              action=\"" . url_prepare("publication_index.php") . "\">
            <input type=\"hidden\" name=\"sel_type\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"type_update\" />
            <input type=\"text\" name=\"tf_type\" />
            <input type=\"submit\" name=\"sub_type\" value=\"$l_type_update\"
              onclick=\"if (check_type_upd(this.form,document.form_type_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Kind Section -->

        <form name=\"form_type_new\" method=\"get\"
          action=\"" . url_prepare("publication_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_type_new :
            <input name=\"tf_type\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"type_insert\" />
            <input type=\"submit\" name=\"sub_type\" value=\"$l_type_insert\"
              onclick=\"if (check_type_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the type links
// Parameters:
//   - $publication : publication hash info : keys used : type
///////////////////////////////////////////////////////////////////////////////
function dis_publication_type_links($publication) {
  global $display, $l_back, $l_type_link_publication, $l_type_link_publication_no;
  global $l_type_delete, $l_type_can_delete, $l_type_cant_delete;

  $delete_ok = true;
  $id = $publication["type"];
  $label = get_publication_type_label($id);
  $obm_q = run_query_publication_type_links($id);

  $nb_type = $obm_q->num_rows();
  if ($nb_type > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_type_link_publication ($nb_type)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("publication_id");
      $cname = $obm_q->f("publication_title");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("publication_index.php?action=detailconsult&amp;publication_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_type) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_type_link_publication_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("publication_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_type_can_delete);
    $dis_del = "<form name=\"form_type_delete\".
          method=\"post\" action=\"" . url_prepare("publication_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"type_delete\" />
        <input type=\"hidden\" name=\"sel_type\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_type_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Subscription Reception section
// Parameters:
//   - $recept_q : Renewal list database object
///////////////////////////////////////////////////////////////////////////////
function html_publication_subscription_reception_form($recept_q) {
  global $l_recept_manage,$l_recept_exist;
  global $l_recept_checkdelete,$l_recept_update,$l_recept_new,$l_recept_insert;

  $sel_recept = "<select name=\"sel_recept\" onChange=\"
    forms.form_recept_update.tf_recept.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($recept_q->next_record()) {
    $id = $recept_q->f("subscriptionreception_id");
    $label = $recept_q->f("subscriptionreception_label");
    $sel_recept .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_recept .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_recept_manage
      </td>
    </tr><tr>
      <td>

<!-- Kind Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_recept_delete\" method=\"post\"
              action=\"" . url_prepare("publication_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_recept_exist</td>
              <td class=\"adminLabel\">$sel_recept</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"recept_checklink\" />
              <input type=\"submit\" name=\"sub_recept\" value=\"$l_recept_checkdelete\" onclick=\"if (check_recept_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Kind Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_recept_update\" method=\"get\"
              action=\"" . url_prepare("publication_index.php") . "\">
            <input type=\"hidden\" name=\"sel_recept\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"recept_update\" />
            <input type=\"text\" name=\"tf_recept\" />
            <input type=\"submit\" name=\"sub_recept\" value=\"$l_recept_update\"
              onclick=\"if (check_recept_upd(this.form,document.form_recept_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Kind Section -->

        <form name=\"form_recept_new\" method=\"get\"
          action=\"" . url_prepare("publication_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_recept_new :
            <input name=\"tf_recept\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"recept_insert\" />
            <input type=\"submit\" name=\"sub_recept\" value=\"$l_recept_insert\"
              onclick=\"if (check_recept_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the recept links
// Parameters:
//   - $publication : publication hash info : keys used : recept
///////////////////////////////////////////////////////////////////////////////
function dis_publication_recept_links($publication) {
  global $display, $l_back, $l_recept_link_publication, $l_recept_link_publication_no;
  global $l_recept_delete, $l_recept_can_delete, $l_recept_cant_delete;

  $delete_ok = true;
  $id = $publication["recept"];
  $label = get_publication_recept_label($id);
  $obm_q = run_query_publication_recept_links($id);

  $nb_recept = $obm_q->num_rows();
  if ($nb_recept > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_recept_link_publication ($nb_recept)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("publication_id");
      $cname = $obm_q->f("publication_title");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("publication_index.php?action=detailconsult&amp;publication_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_recept) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_recept_link_publication_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("publication_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_recept_can_delete);
    $dis_del = "<form name=\"form_recept_delete\".
          method=\"post\" action=\"" . url_prepare("publication_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"recept_delete\" />
        <input type=\"hidden\" name=\"sel_recept\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_recept\" value=\"$l_recept_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_recept_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Publication Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_publication_display_pref($prefs) {
  global $l_publication_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "publication");
  $dis_pref->pref_title = $l_publication_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Subscription search result in an external module
// Parameters:
//   - $publication[] : publication search criteria
//     keys used  : name, type, zip
///////////////////////////////////////////////////////////////////////////////
function dis_publication_subscription_external_list($hash_table) {
  global $display,$l_subscription, $l_no_found_subscription;
  global $auth;
  
  $obm_q = run_query_publication_subscription_external_search($hash_table);
  $nb_subscription = $obm_q->num_rows();
  if ($nb_subscription == 0) {
    $display["msg"] = display_warn_msg($l_no_found_subscription);
  } else {
    $block .= "<div class=\"detailHead\">$l_subscription</div>";
    $block .= html_publication_subscription_list_result($obm_q,$nb_subscription);
  }

  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Task  result
// Parameters : 
//   - $sub_q   : subscription list
//   - $nb_sub  : nb subscriptions
///////////////////////////////////////////////////////////////////////////////
function html_publication_subscription_list_result($sub_q,$nb_subscription) {
  global $set_theme, $ico_update, $ico_delete, $ico_crow;
  global $l_yes,$l_no, $l_timecreate, $l_timeupdate,$l_publication_title;
  global $l_year, $l_renewal,$l_recept,$l_quantity,$l_lang;
  global $path, $auth, $popup_height, $popup_width;
  global $cright_write, $module, $perm;

  if ($perm->check_right($module, $cright_write)) {  
    $has_write_right = true;
    $display_write .= "<td class=\"resultHead\"></td>
    <td class=\"resultHead\"></td>";
  }
  
  $block = " 
  <div class=\"result\"><table class=\"resultTable\">
   <tr>
    <td class=\"resultHead\">$l_publication_title</td>
    <td class=\"resultHead\">$l_year</td>
    <td class=\"resultHead\">$l_lang</td>
    <td class=\"resultHead\">$l_renewal</td>
    <td class=\"resultHead\">$l_recept</td>
    <td class=\"resultHead\">$l_quantity</td>
    <td class=\"resultHead\">$l_timecreate</td>    
    <td class=\"resultHead\">$l_timeupdate</td>
    $display_write
   </tr>
   ";
  while($sub_q->next_record()) {
    $c_id = $sub_q->f("subscription_contact_id");
    $p_id = $sub_q->f("subscription_publication_id");
    $s_id = $sub_q->f("subscription_id");
    $title = $sub_q->f("publication_title");
    $year = $sub_q->f("publication_year");
    $lang = $sub_q->f("publication_lang");
    $rec_label = $sub_q->f("subscriptionreception_label");
    $quantity = $sub_q->f("subscription_quantity");
    $timecreate = datetime_format($sub_q->f("timecreate"));
    if ($sub_q->f("timeupdate") != "") {
      $timeupdate = datetime_format($sub_q->f("timeupdate"));
    } else {
      $timeupdate = "&nbsp;";
    }
    $url_publication_consult = url_prepare("$path/publication/publication_index.php?action=detailconsult&amp;publication_id=$p_id");
    $ext_url = urlencode(url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;contact_id=$c_id&amp;view=publication"));

    if ($has_write_right) {
      $url_subscription_update = url_prepare("$path/publication/publication_index.php?action=detailupdate_subscription&amp;contact_id=$c_id&amp;param_subscription=$s_id&amp;popup=1&amp;ext_url=$ext_url");
      $url_subscription_delete = url_prepare("$path/publication/publication_index.php?action=delete_subscription&amp;param_subscription=$s_id&amp;popup=1");

      $display_write = "
    <td class=\"resultText\">
      <a href=\"javascript: void(0);\" onclick=\"window.name='$l_subscription'; window.open('$url_subscription_update','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\" alt=\"Update Subscription\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_update\" />
      </a>
    </td>    
    <td class=\"resultText\">
      <a href=\"javascript: void(0);\" onclick=\"window.name='$l_subscription'; window.open('$url_subscription_delete','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\" alt=\"Update Subscription\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_crow\" />
      </a>
      </td>";
    }

    if ($sub_q->f("subscription_renewal") == "1") {
      $dis_renew = $l_yes;
    } else {
      $dis_renew = $l_no;
    }
    $block .= "
    <tr>
    <td class=\"resultText\"><a href=\"$url_publication_consult\">$title</a></td>
    <td class=\"resultText\">$year</td>
    <td class=\"resultText\">$lang</td>
    <td class=\"resultText\">$dis_renew</td>
    <td class=\"resultText\">$rec_label</td>
    <td class=\"resultText\">$quantity</td>
    <td class=\"resultText\">$timecreate</td>
    <td class=\"resultText\">$timeupdate</td>    
    $display_write
    </tr>";
  }
  return $block;
}


?>