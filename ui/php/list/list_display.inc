<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_display.php                                             //
//     - Desc : List Display File                                            //
// 2000-01-20 Vincent MARGUERIT                                              //
// 2001-08-23 jlb : add list_comment, add export for letter mailing
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["list_name"] = $l_name;
$fieldnames["list_subject"] = $l_subject;
$fieldnames["list_email"] = $l_email;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["list_nb_contact"] = "$l_nb_contact / $l_query";
$fieldnames["contact_lastname"] = $l_lastname;
$fieldnames["contact_firstname"] = $l_firstname;
$fieldnames["contact_function"] = $l_function;
$fieldnames["company_name"] = $l_company;
$fieldnames["company_address1"] = $l_address;
$fieldnames["company_zipcode"] = $l_postcode;
$fieldnames["company_town"] = $l_town;
$fieldnames["contact_phone"] = $l_phone;
$fieldnames["contact_mobilephone"] = $l_hphone;
$fieldnames["contact_email"] = $l_email;


///////////////////////////////////////////////////////////////////////////////
// Display List specific dataset fields                                      //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_list(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme;

  if (($fieldname == "list_name")  && $link_ok) {
    $res["url"] = "list_index.php?action=detailconsult&amp;param_list=".$OD->data_set->f("list_id");
  }
  
  else if ($fieldname == "list_nb_contact") {
    $nb_q = new DB_OBM;
    $id = $OD->data_set->f("list_id");
    $query = "select count(*) from ContactList where ContactList_listid='$id'";
    $nb_q->query($query);
    $nb_q->next_record();
    $res["name"] = $nb_q->f(0) . " / " .$OD->data_set->f("list_query_nb");
    $res["align"] = "center";
  }
  
  // For contacts urls (link):
  else if ($fieldname == "contact_lastname") {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  else if ($fieldname == "company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
  }

  else if ($fieldname == "company_address") {
    if ($OD->data_set->f("company_address1") != "") {
      $res["name"] .= $OD->data_set->f("company_address1")."<br />\n";
    }
    if ($OD->data_set->f("company_address2") != "") {
      $res["name"] .= $OD->data_set->f("company_address2")."<br />\n";
    }
    if ($OD->data_set->f("company_address3") != "") {
      $res["name"] .= $OD->data_set->f("company_address3")."<br />\n";
    }
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display List search Form                                                  //
// Parameters : 
//   - $list[]        : default form values
//     keys used      : name, contact
///////////////////////////////////////////////////////////////////////////////
function html_list_search_form ($list) {
  global $l_name, $l_list_contact, $l_email, $l_find;
  global $popup;

  if ($popup) {
    $title = $list["title"];
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($list["name"]);
  $email = stripslashes($list["email"]);
  $contact = stripslashes($list["contact"]);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_list\"
    action=\"".url_prepare("list_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_list_contact<br />
      <input type=\"text\" name=\"tf_contact\" size=\"16\" value=\"$contact\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"20\" value=\"$email\" />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
 }


///////////////////////////////////////////////////////////////////////////////
// Display the List search result                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_list_search_list($list, $popup=false) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "list");
  $obm_q = run_query_search($list, $new_order, $order_dir);
  $nb_list = $obm_q->num_rows();
  if ($nb_list == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the List search result                                       //
// Parameters : 
//   - $obm_q    : list of the lists to display 
//   - $pref_q   : fields that have to be displayed
//   - $nb_list  : nb lists returned by the search query 
//   - $list[ ]  : list search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup) {
  global $display, $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }

  $name = urlencode(stripslashes($list["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  $display["msg"] .= display_info_msg("$nb_list $l_found");

  $url = url_prepare("list_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact$url_ext");
  
  $dis_list = new OBM_DISPLAY("DATA", $pref_q, "list");
  if ($popup) {
    $dis_list->display_link = false;
    $dis_list->data_cb_text = "X";
    $dis_list->data_idfield = "list_id";
    $dis_list->data_cb_name = "cb_list";
    $block .= "<form target=\"$ext_target\" method=\"get\" action=\"$ext_url\">";
  }
  $dis_list->data_set = $obm_q;
  $dis_list->data_header = "both";
  $dis_list->data_url = $url;
  $block .= $dis_list->display("dis_data_list");

  if ($popup) {
    $block .= "
    <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <input type=\"submit\" value=\"$l_add\" /> 
      <input type=\"hidden\" name=\"param_list\" value=\"$ext_id\" /> 
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" /> 
    </p>
    </p>
    </form>
    <p align=\"right\">
    <a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display List Consultation                                            //
// Parameters:
//   - $list_q     : list database result 
//   - $pref_con_q : contact display preferences database result 
//   - $con_q      : contact database result 
///////////////////////////////////////////////////////////////////////////////
function html_list_consult($list_q, $pref_con_q, $con_q) {
  global $l_list, $l_name, $l_subject, $l_email, $l_query;
  global $display, $perm, $action;
  global $l_nb_contact, $l_no_static_contact, $l_static_con_found;
  global $l_no_dyn_contact, $l_dyn_con_found, $l_invalid_query;
  global $l_del_contact_sel, $nb_contact_added, $nb_contact_deleted;

  $id = $list_q->f("list_id");
  $usercreate = $list_q->f("list_usercreate");
  $userupdate = $list_q->f("list_userupdate");
  $timecreate = $list_q->f("timecreate");
  $timeupdate = $list_q->f("timeupdate");
  $name = $list_q->f("list_name");
  $subject = $list_q->f("list_subject");
  $email = $list_q->f("list_email");
  $query = $list_q->f("list_query");
  $dis_query = nl2br($query);
  $query_nb = get_query_num_rows($query);
  if ($query_nb >= 0) {
    run_query_update_num_rows($id, $query_nb);
  } else {
    $query_nb = $l_invalid_query;
  }

  $display["detailInfo"] = display_record_info($list_q);
  $display["title"] = "<div class=\"title\">$l_list : $name</div>";

  $block = "
    <div class=\"detailHead\">$l_list</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_subject</td>
      <td class=\"detailText\">$subject</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_query</td>
      <td class=\"detailText\">$query</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_query $l_nb_contact</td>
      <td class=\"detailText\">$query_nb</td>
    </tr>
    </table>";

  // Static Contacts
  $nb_con = $con_q->num_rows();
  if ($nb_con == 0) {
    $message = $l_no_static_contact;
  } else {
    $message = "$nb_con $l_static_con_found";
  }

  $block .= display_info_msg($message);

  if ($nb_con != 0) {

    $url=url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_con_q, "list");
    $dis_infos->data_set = $con_q;
    $dis_infos->data_url = $url;
    $dis_infos->display_entity = "list_contact";
    $dis_infos->data_header = "both";

    if ($perm->have_perm("editor")) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "contact_id";
      $dis_infos->data_cb_name = "cb_con";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"list_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_list");

    if ($perm->have_perm("editor")) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_contact_sel\" />
        <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"contact_del\" />
        </p>
      </p>
      </form>";
    }
  }


  // Dynamic Contacts
  if ($query_nb != 0) {
    $message = "$nb_conq $l_dyn_con_found";
    $block .= display_info_msg($message);

    $url=url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $conq_q = new DB_OBM;
    $conq_q->query($query);
    $dis_infos = new OBM_DISPLAY("DATA", $pref_con_q, "list");
    $dis_infos->data_set = $conq_q;
    $dis_infos->data_url = $url;
    $dis_infos->display_entity = "list_contact_2";
    $dis_infos->data_order = false;
    $dis_infos->data_header = "both";
    $block .= $dis_infos->display("dis_data_list");
  } else {
    $message = $l_no_dyn_contact;
    $block .= display_info_msg($message);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display List Form (graphical or not)                                     //
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function dis_list_form($action, $list_q, $list) {
  global $l_insert, $l_update, $l_back;
  if(is_object($list_q)){
    $crit_q = $list_q->f("list_structure");
  }
  $url = url_prepare("list_index.php");
  $block = "<form method=\"get\" name=\"f_new\" action=\"$url\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">";

  $block = html_list_form($action, $list_q, $list);
  if($action == "insert" || $action == "new" || $list["criteria"] != "" ||  $crit_q !="") {
     $block .= html_list_graphical_form($action, $list_q, $list);
  }

  if (($action == "detailupdate") || ($action == "update")) {
     $dis_but .= "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

   } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\"/>";
  }

  $block .= "<div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_but 
    </p>
    </div>
    </form>";
  $block .= "</form>";
  return $block; 
}
///////////////////////////////////////////////////////////////////////////////
// Display List Form                                                         //
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_form($action, $list_q, $list) {
  global $l_name, $l_subject, $l_email, $l_query,$l_list,$display;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $list_q->f("list_id");
    $usercreate = $list_q->f("list_usercreate");
    $name = $list_q->f("list_name");
    $subject = $list_q->f("list_subject");
    $email = $list_q->f("list_email");
    $crit_q = $list_q->f("list_structure");
    $query = $list_q->f("list_query");
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($list["id"])) { $id = $list["id"]; }
  if (isset($list["name"])) { $name = stripslashes($list["name"]); }
  if (isset($list["subject"])) { $subject = stripslashes($list["subject"]); }
  if (isset($list["email"])) { $email = stripslashes($list["email"]); }
  if (isset($list["query"])) { $query = stripslashes($list["query"]); }
  $url = url_prepare("list_index.php");
  $display["title"] = "<div class=\"title\">$l_list : $name</div>";

  $block = "
  <form method=\"get\" name=\"f_new\" action=\"$url\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">
  <div class=\"detailHead\">$l_list</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_name\" value=\"$name\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_subject</td>
    <td class=\"detailForm\">
      <input name=\"tf_subject\" size=\"30\" value=\"$subject\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input name=\"tf_email\" size=\"30\" value=\"$email\" /></td>
  </tr>";
  if($action == "insert" || $action == "new" || ($list["criteria"] == "" &&  $crit_q =="")) {
  $block .=  "
    <tr>
      <td class=\"detailLabel\">$l_query</td>
      <td class=\"detailForm\">
      <textarea name=\"ta_query\" cols=\"68\" rows=\"8\">$query</textarea></td>
    </tr>
    </table>";
  }
  if (($action == "detailupdate") || ($action == "update")) {
    $block .= "<input type=\"hidden\" name=\"param_list\" value=\"$id\" />";
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Graphical List Form                                               //
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_graphical_form($action, $list_q, $list) {
  global $l_name, $l_subject, $l_email, $l_query,$l_insert, $l_update, $l_back,$l_add_criterion;
  global $popup_height,$popup_width,$set_theme,$ico_updated,$ico_crow,$l_criterion;
  global $l_company, $l_country,$l_zipcode,$l_town,$l_contact,$l_lastname,$l_firstname;
  global $l_update,$l_delete;

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $list_q->f("list_id");
    $usercreate = $list_q->f("list_usercreate");
    $name = $list_q->f("list_name");
    $subject = $list_q->f("list_subject");
    $email = $list_q->f("list_email");
    $criteria = unserialize($list_q->f("list_structure"));

  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($list["id"])) { $id = $list["id"]; }
  if (isset($list["name"])) { $name = stripslashes($list["name"]); }
  if (isset($list["subject"])) { $subject = stripslashes($list["subject"]); }
  if (isset($list["email"])) { $email = stripslashes($list["email"]); }
  if (isset($list["criteria"])) { $criteria = $list["criteria"]; }
  if(!is_array($criteria)) {
    $criteria = unserialize($criteria);
  }
  if(is_array($criteria)) {
    foreach($criteria as $module)
    foreach($module as $field => $criterion) {
      foreach($criterion as $line => $value) {
        if($field == "cs1.country_name") {
          $field= "company_country";
        }elseif($field == "cs2.country_name") {
          $field= "contact_country";
        }
        if($value != "") {
          $input = "<input type=\"text\" name=\"tf_".$field."[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"$value\" />";
        }
        else {
          $input = "<input type=\"hidden\" name=\"tf_".$field."[]\" value=\"\" />";
        }
        $structure[$line] .= "<td>$input</td>";
      }
    }
    $del_cell= "<td><a href=\"javascript:void(0);\" onclick=\"del_row(this.parentNode.parentNode);return false;\">
    <img src=\"/images/$set_theme/$ico_crow\" alt=\"Delete\" /></a></td>";
 
    $upd_cell = "<td><a href=\"javascript:void(0);\" onclick=\"open_window(this.parentNode.parentNode);return false;\">
    <img src=\"/images/$set_theme/$ico_updated\" alt=\"Update\" /></a></td>";  
    foreach($structure as $line) {
      $dis_criteria .="<tr>$del_cell$upd_cell$line</tr>";
    }
  }

  $url2 = "list_index.php?action=new_criterion&amp;popup=1&amp;ext_target=forms[0].elements[5]"; 

  $block = "
   <div class=\"detailHead\">$l_criterion</div> 
  <table class=\"search\" id=\"list_criterion\">
  <tr>
    <td class=\"detailLabel\" style=\"width:5%;\" >$l_delete</td>
    <td class=\"detailLabel\" style=\"width:5%;\">$l_update</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_company</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_company: $l_country</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_company: $l_zipcode</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_company: $l_town</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_contact: $l_lastname</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_contact: $l_firstname</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_contact: $l_country</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_contact: $l_zipcode</td>
    <td class=\"detailLabel\" style=\"width:9%;\">$l_contact: $l_town</td>    
  </tr>  
      $dis_criteria
  </table>
  ";

  $block .= "<input type=\"submit\" value=\"$l_add_criterion\" 
  onclick=\"window.open('$url2','','height=$popup_height,width=$popup_width,scrollbars=yes,status=yes'); return false;\"/>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the Form to make a Criterion //
// Parameters:
//   - $pref_list_q    : DBO : List Field list to display
//   - $pref_contact_q : DBO : Contact Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_add_criterion_form($list) {
  global $l_list_display, $l_contact_display,$l_add_criterion;
  $popup = $list["popup"];
  if(isset($list["row_index"]))$row_index = $list["row_index"];else $row_index =-1;
  $block .= "<form method=\"get\" name=\"f_new_criterion\" action=\"$url\" onSubmit=\"add_criterion(this,$row_index);window.close();return false;\">";
  $block .= html_company_criterion();
  $block .= html_contact_criterion();
  $block .= "
  <div class=\"detailButton\">
  <p class=\"detailButtons\">
  <input type=\"submit\" value=\"$l_add_criterion\"/>
  </p>
  </div>
";
  $block.="<input type=\"hidden\" name=\"ext_target\" value=\"$ext_target\" />";
  $block .= "</form>";
  if($row_index != -1) {
    $block .= "
    <script type=\"text/javascript\">
      get_values(window.document.f_new_criterion);
    </script>
    ";
  }
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the Company Criterion Form //
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_company_criterion(){
  global $l_company,$l_country,$l_zipcode,$l_town;
  $block .= "
  <div class=\"detailHead\">$l_company</div>    
  <table class=\"search\">
  <tr>
    <td class=\"detailLabel\">$l_company</td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailLabel\">$l_zipcode</td>
    <td class=\"detailLabel\">$l_town</td>
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_name\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_country\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_zipcode\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_town\" value=\"\" /></td>
  </tr>
  </table>
  ";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the Contact Criterion Form //
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_contact_criterion(){
  global $l_lastname,$l_firstname,$l_country,$l_zipcode,$l_town,$l_contact;
  $block = "
  <div class=\"detailHead\">$l_contact</div>    
  <table class=\"search\">
  <tr>
    <td class=\"detailLabel\">$l_lastname</td>
    <td class=\"detailLabel\">$l_firstname</td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailLabel\">$l_zipcode</td>
    <td class=\"detailLabel\">$l_town</td>    
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_lname\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_fname\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_country\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_zipcode\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_town\" value=\"\" /></td>    
  </tr>
  </table>
  ";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the List Display preference screen                                //
// Parameters:
//   - $pref_list_q    : DBO : List Field list to display
//   - $pref_contact_q : DBO : Contact Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_list_display_pref($pref_list_q, $pref_contact_q) {
  global $l_list_display, $l_contact_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_list_q);
  $dis_pref->display_entity = "list"; 
  $dis_pref->pref_title = $l_list_display;
  $dis_pref->pref_dis_help = 0;

  $block = "<center><table><tr><td>";
  $block .= $dis_pref->display();

  $dis_pref->display_pref = $pref_contact_q;
  $dis_pref->display_module = "list";
  $dis_pref->display_entity = "list_contact";
  $dis_pref->pref_title = $l_contact_display;

  $block .= "</td><td>";
  $block .= $dis_pref->display();

  $block .= "</td></tr></table>";
  $block .= $dis_pref->dis_pref_help();
  $block .= "</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a list deletion                                    //
// We ask confirmation or cancel
// Parameters:
//   - $id : list id
/////////////////////////////////////////////////////////////////////////////
function dis_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_back;

  $nb_con = get_list_nb_contact($id);

  $display["msg"] .= display_warn_msg("$nb_con $l_warn_delete");

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form method=\"get\" name=\"form_delete\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"delete\" />
        <input type=\"hidden\" name=\"hd_list_id\" value=\"$id\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
        </form>
      </p>
      <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
        <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a list insertion or update                      //
// When similar lists exist we show these and ask confirmation
// Parameters:
//   - $list_q   : list database result (at least 1 row)
//   - $list[]   : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_list_warn_insert($list_q, $list) {
  global $l_check_samelist, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $name = $list["name"];
  $subject = $list["subject"];
  $query = stripslashes($list["query"]);
  if(isset($list["criteria"])) {
    $criteria = urlencode(serialize($list["criteria"]));
  }
  $display["msg"] .= display_warn_msg($l_check_samelist);
  while ($list_q->next_record()) {
    $id = $list_q->f("list_id");
    $samename = $list_q->f("list_name");
    $samesubject = $list_q->f("list_subject");
    $dis_same_list .= "
      <tr><td class=\"detailLabel\">
        <a class=\"detail\" href=\"" .url_prepare("list_index.php?action=detailconsult&amp;param_list=$id") . "\">$samename ($samesubject)</a>
      </td></tr>";
  }

  $block = "<p />
  <table class=\"detail\">
  $dis_same_list
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("list_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
      <input type=\"hidden\" name=\"ta_query\" value=\"$query\" />
      <input type=\"hidden\" name=\"se_criteria\" value=\"$criteria\" /
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back method=\"post\"
      action=\"" .url_prepare("list_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
      <input type=\"hidden\" name=\"ta_query\" value=\"$query\" />
      <input type=\"hidden\" name=\"se_criteria\" value=\"$criteria\" /
      <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the export format form                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function dis_export_form($list) {
  global $c_export_format, $l_no_found, $l_list_set, $l_export_format;
  global $l_export_intro, $l_name, $l_desc, $l_help;
  global $display, $set_theme, $ses_list;

  $display["msg"] .= display_info_msg($l_export_intro);

  $nb_list = $list["list_nb"];
  $cpt = 1;
  $d_list = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$l_list_set</td>
    </tr>";
  $param_list = "&amp;list=";
  $first = true;
  while ( list( $key, $val ) = each( $ses_list ) ) {
    if ($first == true) {
      $param_list .= $val;
    } else {
      $param_list .= ",$val";
    }
    $first = false;
    $name = get_list_name($val);
    $d_list .= "
    <tr>
      <td class=\"detailText\">
        <a href=\"list_index.php?action=detailconsult&amp;param_list=$val\">$name</a></td>
    </tr>";
  }

  $d_list .= "</table>";

  $cpt = 0;
  $d_format = "
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"4\">$l_export_format</td>
    </tr><tr>
      <td class=\"adminHead\" colspan=\"2\">$l_name</td>
      <td class=\"adminHead\">$l_desc</td>
      <td class=\"adminHead\">$l_help</td>
    </tr>";
  while ($c_export_format[$cpt]["name"]) {
    $name = $c_export_format[$cpt]["name"];
    $info = $c_export_format[$cpt]["info"];
    $url = url_prepare($c_export_format[$cpt]["url"] . $param_list);
    $icon = $c_export_format[$cpt]["icon"];
    $url_help = "export_format_$cpt.html";
    $d_format .= "<tr>
      <td class=\"adminLabel\"><a target=\"export\" href=\"$url\">$name</a></td>
      <td class=\"adminDetail\"><img src=\"/images/$set_theme/$icon\" alt=\"I\" /></td>
      <td class=\"adminDetail\">$info</td>
      <td class=\"adminDetail\"><a target=\"doc\" href=\"$url_help\">X</a></td>
      </tr>";
    $cpt++;
  }
  $d_format .= "</table>";

  $block = "<table class=\"detail\">
    <tr>
      <td>$d_list
      </td>
      <td>$d_format
      </td>
    </tr>
    </table>";

  return $block;
}


</script>
