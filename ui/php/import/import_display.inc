<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_display.php                                           //
//     - Desc : Import Display File                                          //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["import_name"] = $l_name;
$fieldnames["import_datasource"] = $l_datasource;
$fieldnames["import_separator"] = $l_csv_sep;
$fieldnames["import_enclosed"] = $l_enclosed;


///////////////////////////////////////////////////////////////////////////////
// Display Import specific dataset fields                                    //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_import(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme;

  if (($fieldname == "import_name")  && $link_ok) {
    $res["url"] = "import_index.php?action=detailconsult&amp;param_import=".$OD->data_set->f("import_id");
  }
  
  // For contacts urls (link):
  else if ($fieldname == "contact_lastname") {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters : 
//   - $import[] : default form values
//     keys used : name
//   - $dsrc_q   : database object with datasource list
///////////////////////////////////////////////////////////////////////////////
function html_import_search_form ($import, $dsrc_q) {
  global $l_name, $l_datasource, $l_find, $l_none;

  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($import["name"]);
  $dsrc = stripslashes($import["datasource"]);

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"0\">$l_none</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_import\"
    action=\"".url_prepare("import_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_datasource<br />
      $sel_dsrc
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
 }


///////////////////////////////////////////////////////////////////////////////
// Display the Import search result
// Parameters:
//   - $import[] : import hash values
//     keys used : name, subject, email
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_import_search_list($import, $popup=false) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "import");
  $obm_q = run_query_search($import, $new_order, $order_dir);
  $nb = $obm_q->num_rows();
  if ($nb == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_import_search_list($obm_q, $pref_q, $nb, $import, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Import search result
// Parameters : 
//   - $obm_q    : list of the imports to display 
//   - $pref_q   : fields that have to be displayed
//   - $nb       : nb imports returned by the search query 
//   - $import   : import search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_import_search_list($obm_q, $pref_q, $nb, $import, $popup) {
  global $display, $l_found, $l_add, $l_close;

  $name = urlencode(stripslashes($import["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  $display["msg"] .= display_info_msg("$nb $l_found");

  $url = url_prepare("import_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact");
  
  $od = new OBM_DISPLAY("DATA", $pref_q, "import");
  $od->data_set = $obm_q;
  $od->data_header = "both";
  $od->data_url = $url;
  $block .= $od->display("dis_data_import");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Consultation                                          //
// Parameters:
//   - $import_q : Import database result 
///////////////////////////////////////////////////////////////////////////////
function html_import_consult($import_q) {
  global $l_import, $l_name, $l_datasource,$l_csv_sep, $l_enclosed;
  global $l_file, $l_sample_file, $l_test_file;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $l_company, $l_number, $l_address, $l_postcode, $l_town;
  global $l_phone, $l_fax, $l_web, $l_email, $l_comment;
  global $l_contact, $l_lastname, $l_firstname, $l_title, $l_hphone, $l_mphone;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $display, $perm, $action;

  $id = $import_q->f("import_id");
  $usercreate = $import_q->f("import_usercreate");
  $userupdate = $import_q->f("import_userupdate");
  $timecreate = $import_q->f("timecreate");
  $timeupdate = $import_q->f("timeupdate");
  $name = $import_q->f("import_name");
  $dsrc = $import_q->f("import_datasource_id");
  $datasource = $import_q->f("datasource_name");
  $csv_sep = $import_q->f("import_separator");
  $enclosed = $import_q->f("import_enclosed");
  $desc = $import_q->f("import_desc");
  eval ($desc);

  if ($csv_sep == $ccsvd_sc) $csvd_sc = "checked";
  if ($csv_sep == $ccsvd_tab) $csvd_tab = "checked";

  while (list($field, $el) = each($comp)) {
    $label = ${$el["label"]};
    $value = $el["value"];
    $default = $el["default"];
    if (($label != "") && (($value != "") || ($default != ""))) {
      $mapping .= "
  <tr>
    <td class=\"detailLabel\">$label</td>
    <td class=\"detailText\">$value</td>
    <td class=\"detailText\">$default</td>
  </tr>";
      if ($value != "") {
	$map["$value"] = $field;
      }
    }
  }

  $mapping .= "
  <tr>
    <td class=\"detailHead\" colspan=\"3\">$l_contact</td>
  </tr>";

  while (list($field, $el) = each($con)) {
    $label = ${$el["label"]};
    $value = $el["value"];
    $default = $el["default"];
    if (($label != "") && (($value != "") || ($default != ""))) {
      $mapping .= "
  <tr>
    <td class=\"detailLabel\">$label</td>
    <td class=\"detailText\">$value</td>
    <td class=\"detailText\">$default</td>
  </tr>";
    }
  }

  $url = url_prepare("import_index.php");
  $display["detailInfo"] = display_record_info($import_q);
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
    <div class=\"detailHead\">$l_import</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_datasource</td>
      <td class=\"detailText\">$datasource</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_csv_sep</td>
      <td class=\"detailText\">$csv_sep</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_enclosed</td>
      <td class=\"detailText\">$enclosed</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_mapping</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailForm\">$l_field</td>
    <td class=\"detailForm\">$l_field_num</td>
    <td class=\"detailForm\">$l_default</td>
  </tr>
  <tr>
    <td class=\"detailHead\" colspan=\"3\">$l_company</td>
  </tr>
  $mapping
  </table>

    <div class=\"detailHead\">$l_file</div>
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_sample\" />
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_sample_file</td>
      <td class=\"detailForm\">
        <input type=\"file\" name=\"sample_file\" id=\"sample_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_test_file\" /></td>
    </tr>
    </table>
    </form>

    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_test\" />
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_test_file</td>
      <td class=\"detailForm\">
        <input type=\"file\" name=\"test_file\" id=\"test_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_test_file\" /></td>
    </tr>
    </table>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Sample File block
// Parameters:
//   - $import_q : Import database result 
//   - $import[] : import hash values
//   - $max_rows : max rows displayed from test file
///////////////////////////////////////////////////////////////////////////////
function html_import_file_sample($import_q, $import, $max_rows) {
  global $l_import, $l_name, $l_datasource, $l_file, $l_test_file, $l_csv_sep;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $display, $perm, $action;

  $file = $import["file"];
  $file_name = $import["file_name"];
  $file_size = $import["file_size"];
  $csv_sep = $import_q->f("import_separator");
  $enclosed = trim($import_q->f("import_enclosed"));

  $map = get_import_field_mapping($import_q);

  // Sample header
  $mapping_head .= "<tr>";
  while (list($num, $el) = each($map)) {
    $label = $el["label"];
    $mapping_head .= "<td class=\"detailLabel\">$num-$label</td>";
  }
  $mapping_head .= "</tr>";

  // Sample file display
  $regexp_sep = "/$enclosed$csv_sep$enclosed/";
  $r = file($file);
  while ( (list($num_line, $line) = each($r)) && ($num_line < $max_rows) ) {
    $rows = get_file_row_fields($line, $csv_sep, $enclosed);

    $mapping .= "<tr>";
    while (list($num, $value) = each($rows)) {
      if ($map[$num]["label"] != "") {
	$mapping .= "<td class=\"detailForm\">$value</td>";
      }
    }
    $mapping .= "</tr>";
  }

  $block = "
    <div class=\"detailHead\">$file_name</div>
    <table class=\"detail\">
    <tr>
      $mapping_head
    </tr>
      $mapping
    </table>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Test File block
// Parameters:
//   - $import_q : Import database result 
//   - $import[] : import hash values
///////////////////////////////////////////////////////////////////////////////
function html_import_file_test($import_q, $import) {
  global $l_import, $l_name, $l_datasource, $l_file, $l_test_file, $l_csv_sep;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $display, $perm, $action;

  $file = $import["file"];
  $file_name = $import["file_name"];
  $file_size = $import["file_size"];
  $csv_sep = $import_q->f("import_separator");
  $enclosed = trim($import_q->f("import_enclosed"));

  $map = get_import_field_mapping($import_q);

  // Sample header
  $mapping_head .= "<tr>";
  while (list($num, $el) = each($map)) {
    $label = $el["label"];
    $mapping_head .= "<td class=\"detailLabel\">$num-$label</td>";
  }
  $mapping_head .= "</tr>";

  // Sample file display
  $regexp_sep = "/$enclosed$csv_sep$enclosed/";
  $r = file($file);
  while ( (list($num_line, $line) = each($r)) && ($num_line < $max_rows) ) {
    $rows = get_file_row_fields($line, $csv_sep, $enclosed);

    $mapping .= "<tr>";
    while (list($num, $value) = each($rows)) {
      if ($map[$num]["label"] != "") {
	$mapping .= "<td class=\"detailForm\">$value</td>";
      }
    }
    $mapping .= "</tr>";
  }

  $block = "
    <div class=\"detailHead\">$file_name</div>
    <table class=\"detail\">
    <tr>
      $mapping_head
    </tr>
      $mapping
    </table>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import Form                                                       //
// Parameters :
//   - $action   : action called
//   - $import[] : default or transmitted values
//     keys used : name, subject, usercreate
//   - $import_q : DBO : information about the import (null for new import)
//   - $dsrc_q   : database object with datasource list
///////////////////////////////////////////////////////////////////////////////
function html_import_form($action, $import, $import_q, $dsrc_q) {
  global $display, $l_none, $l_insert, $l_update, $l_back;
  global $l_import, $l_name, $l_file, $l_datasource, $l_csv_sep, $l_enclosed;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $l_company, $l_number, $l_address, $l_postcode, $l_town;
  global $l_phone, $l_fax, $l_web, $l_email, $l_comment;
  global $l_contact, $l_lastname, $l_firstname, $l_title, $l_hphone, $l_mphone;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $import_q->f("import_id");
    $usercreate = $import_q->f("list_usercreate");
    $name = $import_q->f("import_name");
    $dsrc = $import_q->f("import_datasource_id");
    $sep = $import_q->f("import_separator");
    $enc = $import_q->f("import_enclosed");
    $desc = $import_q->f("import_desc");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($import["id"])) { $id = $import["id"]; }
  if (isset($import["name"])) { $name = stripslashes($import["name"]); }
  if (isset($import["datasource"])) { $dsrc = $import["datasource"]; }
  if (isset($import["sep"])) { $sep = $import["sep"]; }
  if (isset($import["enclosed"])) { $enc = $import["enclosed"]; }

  eval ($desc);
  $enc = htmlspecialchars(trim($enc));

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"0\">$l_none</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  if ($sep == $ccsvd_sc) $csvd_sc = "checked";
  if ($sep == $ccsvd_tab) $csvd_tab = "checked";

  $url = url_prepare("import_index.php");
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
  <form method=\"post\" name=\"f_new action=\"$url\"
    onSubmit=\"if (check_import_form(this)) return true; else return false;\">

  <div class=\"detailHead\">$l_import</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_name\" value=\"$name\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_datasource</td>
    <td class=\"detailForm\">$sel_dsrc</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_csv_sep</td>
    <td class=\"detailForm\">
      <input type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc
      <input type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_enclosed</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_enclosed\" size=\"1\" maxlength=\"1\" value=\"$enc\" /></td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_mapping</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailForm\">$l_field_num</td>
    <td class=\"detailForm\">$l_field</td>
    <td class=\"detailForm\">$l_default</td>
  </tr>
  <tr>
    <td class=\"detailHead\" colspan=\"3\">$l_company</td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_name\" value=\"$comp_name\" /></td>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_name_d\" value=\"$comp_name_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_num\" value=\"$comp_num\" /></td>
    <td class=\"detailLabel\">$l_number</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_num_d\" value=\"$comp_num_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad1\" value=\"$comp_ad1\" /></td>
    <td class=\"detailLabel\">$l_address 1</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_ad1_d\" value=\"$comp_ad1_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad2\" value=\"$comp_ad2\" /></td>
    <td class=\"detailLabel\">$l_address 2</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_ad2_d\" value=\"$comp_ad2_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad3\" value=\"$comp_ad3\" /></td>
    <td class=\"detailLabel\">$l_address 3</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_ad3_d\" value=\"$comp_ad3_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_zip\" value=\"$comp_zip\" /></td>
    <td class=\"detailLabel\">$l_postcode</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_zip_d\" value=\"$comp_zip_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_town\" value=\"$comp_town\" /></td>
    <td class=\"detailLabel\">$l_town</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_town_d\" value=\"$comp_town_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_pho\" value=\"$comp_pho\" /></td>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_pho_d\" value=\"$comp_pho_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_fax\" value=\"$comp_fax\" /></td>
    <td class=\"detailLabel\">$l_fax</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_fax_d\" value=\"$comp_fax_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_web\" value=\"$comp_web\" /></td>
    <td class=\"detailLabel\">$l_web</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_web_d\" value=\"$comp_web_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_mail\" value=\"$comp_mail\" /></td>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_mail_d\" value=\"$comp_mail_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_com\" value=\"$comp_com\" /></td>
    <td class=\"detailLabel\">$l_comment</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_com_d\" value=\"$comp_com_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailHead\" colspan=\"3\">$l_contact</td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_ln\" value=\"$con_ln\" /></td>
    <td class=\"detailLabel\">$l_lastname</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_ln_d\" value=\"$con_ln_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_fn\" value=\"$con_fn\" /></td>
    <td class=\"detailLabel\">$l_firstname</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_fn_d\" value=\"$con_fn_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_tit\" value=\"$con_tit\" /></td>
    <td class=\"detailLabel\">$l_title</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_tit_d\" value=\"$con_tit_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_ad1\" value=\"$con_ad1\" /></td>
    <td class=\"detailLabel\">$l_address 1</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_ad1_d\" value=\"$con_ad1_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_ad2\" value=\"$con_ad2\" /></td>
    <td class=\"detailLabel\">$l_address 2</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_ad2_d\" value=\"$con_ad2_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_ad3\" value=\"$con_ad3\" /></td>
    <td class=\"detailLabel\">$l_address 3</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_ad3_d\" value=\"$con_ad3_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_zip\" value=\"$con_zip\" /></td>
    <td class=\"detailLabel\">$l_postcode</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_zip_d\" value=\"$con_zip_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_town\" value=\"$con_town\" /></td>
    <td class=\"detailLabel\">$l_town</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_town_d\" value=\"$con_town_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_pho\" value=\"$con_pho\" /></td>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_pho_d\" value=\"$con_pho_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_hpho\" value=\"$con_hpho\" /></td>
    <td class=\"detailLabel\">$l_hphone</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_hpho_d\" value=\"$con_hpho_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_mpho\" value=\"$con_mpho\" /></td>
    <td class=\"detailLabel\">$l_mphone</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_mpho_d\" value=\"$con_mpho_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_fax\" value=\"$con_fax\" /></td>
    <td class=\"detailLabel\">$l_fax</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_fax_d\" value=\"$con_fax_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_mail\" value=\"$con_mail\" /></td>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_mail_d\" value=\"$con_mail_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_com\" value=\"$con_com\" /></td>
    <td class=\"detailLabel\">$l_comment</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_com_d\" value=\"$con_com_d\" /></td>
  </tr>
  </table>
";

  if (($action == "detailupdate") || ($action == "update")) {
     $dis_but .= "
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

       $dis_back = "<form method=\"get\" name=\"f_back\"
        action=\"".url_prepare("import_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>";
   } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_but
    </p>
    </div>
    </form>";

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

  $display["msg"] .= display_warn_msg("$l_warn_delete");

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form method=\"get\" name=\"form_delete\"
          action=\"" .url_prepare("import_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"delete\" />
        <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
        </form>
      </p>
      <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("import_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
        <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about an import insertion or update                   //
// When similar imports exist we show these and ask confirmation
// Parameters:
//   - $import_q : import database result (at least 1 row)
//   - $import[] : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_import_warn_insert($import_q, $import) {
  global $l_check_sameimport, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $name = $import["name"];
  $dsrc = $import["datasource"];

  $display["msg"] .= display_warn_msg($l_check_sameimport);
  while ($import_q->next_record()) {
    $id = $import_q->f("import_id");
    $samename = $import_q->f("import_name");
    $samedsrc = $import_q->f("datasource_name");
    $dis_same_import .= "
      <tr><td class=\"detailLabel\">
        <a class=\"detail\" href=\"" .url_prepare("import_index.php?action=detailconsult&amp;param_import=$id") . "\">$samename ($samedsrc)</a>
      </td></tr>";
  }

  $block = "<p />
  <table class=\"detail\">
  $dis_same_import
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("import_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back method=\"post\"
      action=\"" .url_prepare("import_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
      <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


</script>
