<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_display.inc                                          //
//     - Desc : Publication Display File                                         //
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
// Display Publication specific dataset fields                                   //
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
    if($OD->data_specialfield == "ext_get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("publication_id").",'".$OD->data_set->f("publication_title")."');";
    }
    else {    
      $res["url"] = "$path/publication/publication_index.php?action=detailconsult&amp;param_publication=".$OD->data_set->f("publication_id");
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Publication search Form                                               //
// Parameters:
//   - $type_q    : database object with type list
//   - $act_q     : database object with activity list
//   - $usr_q     : database object with userobm list
//   - $publication[] : default form values
//     keys used  : archive, name, type, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_publication_search_form ($type_q, $publication) {
  global $l_publication_title, $l_publication_type, $l_lang, $l_year;
  global $l_all, $l_find; 
  global $c_all;
  
  $title = stripslashes($publication["title"]);
  $lang = stripslashes($publication["lang"]);
  $type = $publication["type"];
  $year = $publication["year"];

  // type select
  $sel_type = "<select id=\"sel_type\" name=\"sel_type\">
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
  <form method=\"get\" name=\"f_publication\" id=\"f_publication\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_publication_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"24\"
      value=\"$title\" />
    </div>
    <div class=\"searchLabel\">$l_publication_type<br />
      $sel_type
    </div>
    <div class=\"searchLabel\">$l_year<br />
      <input type=\"text\" name=\"tf_year\" id=\"tf_year\" size=\"4\" 
      value=\"$year\" />
    </div>
    <div class=\"searchLabel\">$l_lang<br />
      <input type=\"text\" name=\"tf_lang\" id=\"tf_lang\" size=\"4\" 
      value=\"$lang\" />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Publication search result                                         //
// Parameters:
//   - $publication[] : publication search criteria
//     keys used  : name, type, zip
///////////////////////////////////////////////////////////////////////////////
function dis_publication_search_list($publication) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "publication");
  $obm_q = run_query_search($publication, $new_order, $order_dir);
  $nb_publication = $obm_q->num_rows();
  if ($nb_publication == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_publication_search_list($obm_q,$pref_q,$nb_publication,$publication,$popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $comp_q     : list of companies
//   - $pref_q     : the fields which have to be displayed
//   - $nb_publication : nb companies returned by the search query
//   - $publication[]  : publication search criteria
//     keys used   : archive, name, type, zip
///////////////////////////////////////////////////////////////////////////////
function html_publication_search_list($comp_q, $pref_q, $nb_publication,$publication,$popup) {
  global $display, $l_found,$l_close;

  // we urlencode to avoid breakage for space char
  $archive = $publication["archive"];
  $name = urlencode(stripslashes($publication["name"]));
  $phone = urlencode(stripslashes($publication["phone"]));
  $type = $publication["type"];
  $act = $publication["activity"];
  $zip = urlencode(stripslashes($publication["zip"]));
  $market = $publication["marketing_manager"];
  
  $url = url_prepare("publication_index.php?action=search&amp;tf_name=$name&amp;tf_phone=$phone&amp;sel_type=$type&amp;sel_act=$act&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_market=$market$url_ext");

  $comp_q->seek($first_row);

  $comp_d = new OBM_DISPLAY("DATA", $pref_q, "publication");
  if ($popup) {
    $comp_d->data_specialfield = "ext_get_id";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $comp_d->data_set = $comp_q;
  $comp_d->data_url = $url;
  $comp_d->data_header = "both";
  $display["msg"] = display_info_msg("$nb_publication $l_found");
  $block = $comp_d->display("dis_data_publication");
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////// Display: Publication links menu                                               //
// Parameters:
//   - $pub_q : publication database result 
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_publication_links($pub_q) {
  global $set_theme,$ico_contact,$ico_contact_new,$ico_deal,$ico_deal_new;
  global $ico_document,$ico_document_new, $ico_project, $ico_contract;
  global $l_header_contact, $l_contact_new, $l_header_project, $l_project_new;
  global $l_header_deal, $l_deal_new, $l_header_contract, $l_contract_new;
  global $l_header_document, $l_document_new, $l_document_add;
  global $perms_user,$l_publication, $path, $auth,$popup_height,$popup_width;

  $id = $pub_q->f("publication_id");
  $name = $pub_q->f("publication_name");
  $market = $pub_q->f("publication_marketingmanager_id");
  $ctry = $pub_q->f("publication_country_id");
  $con_num = $pub_q->f("publication_contact_number");
  $deal_num = $pub_q->f("publication_deal_number");
  $deal_total = $pub_q->f("publication_deal_total");

  $uname = urlencode($name);
  $url_con_list = url_prepare("$path/contact/contact_index.php?action=search&amp;param_publication=$id&amp;tf_publication=$uname");
  $url_con_new = url_prepare("$path/contact/contact_index.php?action=new&amp;param_publication=$id&amp;sel_market=$market&amp;sel_ctry=$ctry");
  $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;param_publication=$id&amp;tf_publication_name=$uname");
  $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;param_publication=$id&amp;tf_publication_name=$uname&amp;cb_archive=1");
  $url_deal_new = url_prepare("$path/deal/deal_index.php?action=new&amp;param_publication=$id");
  $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=Publication");
  $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=Publication");
  $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_document)."&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/publication/publication_index.php")."&amp;ext_id=$id&amp;ext_target=$l_publication";
  $nb_document = run_query_document_nb ($id,"Publication");
  $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;param_publication=$id");
  $nb_pro = run_query_linked_project_nb ($id,"publication");
  $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;param_publication=$id&amp;tf_publication_name=$uname");
  $nb_ctra = run_query_linked_contract_nb ($id,"publication");
  $url_ctra_new = url_prepare("$path/contract/contract_index.php?action=new&amp;param_publication=$id&amp;tf_publication_name=$uname");
  
  $block = "<div class=\"link\"> 
  <div class=\"linkTitle\">:: $l_header_contact</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_con_list\"><img src=\"/images/$set_theme/$ico_contact\" /></a>
        <a href=\"$url_con_list\">$l_header_contact ($con_num)</a></li>
    <li><a href=\"$url_con_new\"><img src=\"/images/$set_theme/$ico_contact_new\" /></a>
        <a href=\"$url_con_new\">$l_contact_new</a></li>
  </ul>

  <div class=\"linkTitle\">:: $l_header_deal</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_deal_list\"><img src=\"/images/$set_theme/$ico_deal\" /></a>
        $l_header_deal (
        <a href=\"$url_deal_list\">$deal_num</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
    <li><a href=\"$url_deal_new\"><img src=\"/images/$set_theme/$ico_deal_new\" /></a>
        <a href=\"$url_deal_new\">$l_deal_new</a></li>
  </ul>

  <div class=\"linkTitle\">:: $l_header_project</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_pro\"><img src=\"/images/$set_theme/$ico_project\" /></a>
        <a href=\"$url_pro\">$l_header_project ($nb_pro)</a></li>
    <li><a href=\"$url_pro_new\"><img src=\"/images/$set_theme/$ico_project\" /></a>
        <a href=\"$url_pro_new\">$l_project_new</a></li>
  </ul>

  <div class=\"linkTitle\">:: $l_header_contract</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_ctra\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_ctra\">$l_header_contract ($nb_ctra)</a></li>
    <li><a href=\"$url_ctra_new\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_ctra_new\">$l_contract_new</a></li>
  </ul>

  <div class=\"linkTitle\">:: $l_header_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"/images/$set_theme/$ico_document\" /></a>
        <a href=\"$url_doc\">$l_header_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
    <li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_publication'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>       
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Publication Consultation                                         //
// Parameters:
//   - $pub_q : publication database result 
///////////////////////////////////////////////////////////////////////////////
function html_publication_consult($pub_q) {
  // - Themes
  global $l_publication_title, $l_type, $l_year, $l_lang, $l_publication;
  global $display, $c_yes, $c_no;

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
// Display Publication Form                                                      //
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
function html_publication_form($action, $pub_q,$type_q, $publication) {
  global $display, $auth, $set_dsrc, $l_none, $ico_add;
  global $set_theme,$ico_web,$ico_mail,$l_category,$popup_height,$popup_width;
  global $l_publication_title, $l_type, $l_year, $l_lang, $l_desc;
  global $l_insert,$l_update,$l_checkdelete, $l_publication;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $pub_q->f("publication_id");
    $title = $pub_q->f("publication_title");
    $type = $pub_q->f("publication_type_id");
    $year = $pub_q->f("publication_year");
    $desc = $pub_q->f("publication_desc");
    $lang = $pub_q->f("publication_lang");    
  }

  // If parameters have been given, they supercede the default action value
  if (isset($publication["id"])) { $id = $publication["id"]; }
  if (isset($publication["title"])) { $name = stripslashes($publication["title"]); }
  if (isset($publication["type"])) { $type = $publication["type"]; }
  if (isset($publication["year"])) { $ad3 = stripslashes($publication["year"]); }
  if (isset($publication["lang"])) { $zip = stripslashes($publication["lang"]); }
  if (isset($publication["desc"])) { $com = stripslashes($publication["desc"]); }

  // type select
  $sel_type = "<select name=\"sel_type\" id=\"sel_type\">
    <option value=\"0\">$l_none</option>";
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
      <input type=\"hidden\" name=\"param_publication\" id=\"param_publication\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
  }

  $url = "publication_index.php?action=ext_get_cat_ids&amp;popup=1&amp;ext_target=forms[0].elements[6]"; 

  $display["title"] = "<div class=\"title\">$l_publication : $name</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    $js_ctry

    <form method=\"post\" name=\"form_publication\"
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
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_lang\" name=\"tf_lang\" maxlength=\"2\" size=\"2\" value=\"$lang\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_year</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_year\" name=\"tf_year\" maxlength=\"4\" size=\"4\" value=\"$year\" /></td>
    </tr>
    </table>

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
// Display: Display and check the publication links (and delete form if ok)
// Parameters:
//   - $p_id : publication id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $l_link_contract, $l_link_contract_no, $l_link_project, $l_link_project_no;
  global $path, $display;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_publication_contact_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkc ="
      <tr>
        <td class=\"detailHead\">$l_link_contact</td>
      </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname") . " " . $obm_q->f("contact_firstname");
      $display_linkc .= "
      <tr>
        <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
        </td>
      </tr>";
    }
  } else {
    $display_linkc = "
      <tr>
        <td class=\"detailHead\">$l_link_contact_no</td>
      </tr>";
  }

  // Links from deals
  $obm_q = run_query_publication_deal_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkd = "
      <tr>
        <td class=\"detailHead\">$l_link_deal</td>
      </tr>";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $display_linkd .="<tr>
        <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" .url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_linkd = "
      <tr>
        <td class=\"detailHead\">$l_link_deal_no</td>
      </tr>";
  }

  // Links from Contracts
  $obm_q = run_query_publication_contract_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_cr = "<tr><td class=\"detailHead\">$l_link_contract</td></tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $display_link_cr .= "
      <tr>
        <td class=\"detailLabel\">
          <a class=\"detailLabel\" href=\"".url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$cid")."\">
          $clabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_link_cr = "<tr><td class=\"detailHead\">$l_link_contract_no</td></tr>";
  }

  // Links from Project
  $obm_q = run_query_publication_project_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkp ="<tr><td class=\"detailHead\">$l_link_project</td></tr>";
    while ($obm_q->next_record()) {
      $pid = $obm_q->f("project_id");
      $pname = $obm_q->f("project_name");
      $display_linkp .= "
    <tr>
      <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" . url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=$pid") . "\">$pname</a>
      </td>
    </tr>";
    }
  } else {
    $display_linkp = "
    <tr>
      <td class=\"detailHead\">$l_link_project_no</td>
    </tr>";
  }

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("publication_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_publication\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $block .= "<table class=\"detail\">
   $display_linkc
   $display_linkd
   $display_link_cr
   $display_linkp
   </table>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_can_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">" . dis_delete_form($p_id) . "</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a publication insertion or update                   //
// When similar companies exists we show these and ask confirmation
// Parameters:
//   - $cid       : publication id
//   - $pub_q      : publication database result (at least 1 row)
//   - $publication[] : default values
//     keys used  : num, archive, name, type, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
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
         <a class=\"detail\" href=\"" .url_prepare("publication_index.php?action=detailconsult&amp;param_publication=$id") . "\">
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
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("publication_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"hd_publication_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display the publication administration index                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $type_q = run_query_publicationtype();
  $block = html_publication_type_form($type_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Publication Kind section                                             //
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
// Displays the type links                                                   //
// Parameters:
//   - $publication : publication hash info : keys used : type
///////////////////////////////////////////////////////////////////////////////
function dis_type_links($publication) {
  global $display, $l_back, $l_type_link_publication, $l_type_link_publication_no;
  global $l_type_delete, $l_type_can_delete, $l_type_cant_delete;

  $delete_ok = true;
  $id = $publication["type"];
  $label = get_type_label($id);
  $obm_q = run_query_type_links($id);

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
        <a href=\"" .url_prepare("publication_index.php?action=detailconsult&amp;param_publication=$cid") . "\">$cname</a>
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
// Display: the Publication Display preference screen                            //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_publication_display_pref($display_pref_q) {
  global $l_publication_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "publication"; 
  $dis_pref->pref_title = $l_publication_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}

</SCRIPT>
