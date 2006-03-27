<?php
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
$fieldnames["import_market"] = $l_market;
$fieldnames["import_separator"] = $l_csv_sep;
$fieldnames["import_enclosed"] = $l_enclosed;


///////////////////////////////////////////////////////////////////////////////
// Display Import specific dataset fields
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
  else if ($fieldname == "import_market") {
    $res["name"] = $OD->data_set->f("market_lastname") . " " . $OD->data_set->f("market_firstname");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters:
//   - $import[] : hash with import values
///////////////////////////////////////////////////////////////////////////////
function dis_import_search_form($import="") {
  global $cg_adm;

  $dsrc_q = run_query_global_datasource();
  $usr_q = run_query_all_users_from_group($cg_adm);
  $block = html_import_search_form($import, $dsrc_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters : 
//   - $import[] : default form values
//     keys used : name
//   - $dsrc_q   : database object with datasource list
//   - $usr_q    : database object with userobm list from Com group
///////////////////////////////////////////////////////////////////////////////
function html_import_search_form ($import, $dsrc_q, $usr_q) {
  global $l_name, $l_datasource, $l_market, $l_find;
  global $c_all, $l_all;

  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($import["name"]);
  $dsrc = stripslashes($import["datasource"]);
  $market = $import["market"];

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $market) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"".url_prepare("import_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_datasource<br />
      $sel_dsrc
    </div>
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
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

  $prefs = get_display_pref($auth->auth["uid"], "import");
  $obm_q = run_query_import_search($import, $new_order, $order_dir);
  $nb = $obm_q->num_rows_total();
  if ($nb == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_import_search_list($obm_q, $prefs, $nb, $import, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Import search result
// Parameters : 
//   - $obm_q    : list of the imports to display 
//   - $prefs    : fields that have to be displayed
//   - $nb       : nb imports returned by the search query 
//   - $import   : import search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_import_search_list($obm_q, $prefs, $nb, $import, $popup) {
  global $display, $l_found, $l_add, $l_close;

  $name = urlencode(stripslashes($import["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  $display["msg"] .= display_info_msg("$nb $l_found");

  $url = url_prepare("import_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact");

  $od = new OBM_DISPLAY("DATA", $prefs, "import");
  $od->data_set = $obm_q;
  $od->data_header = "both";
  $od->data_url = $url;
  $block .= $od->display("dis_data_import");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Consultation
// Parameters:
//   - $import_q : Import database result 
///////////////////////////////////////////////////////////////////////////////
function html_import_consult($import_q) {
  global $l_import, $l_name, $l_datasource, $l_market, $l_csv_sep, $l_enclosed;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $l_company, $l_contact;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $display;

  $id = $import_q->f("import_id");
  $usercreate = $import_q->f("import_usercreate");
  $userupdate = $import_q->f("import_userupdate");
  $timecreate = $import_q->f("timecreate");
  $timeupdate = $import_q->f("timeupdate");
  $name = $import_q->f("import_name");
  $dsrc = $import_q->f("import_datasource_id");
  $datasource = $import_q->f("datasource_name");
  $market = $import_q->f("market_lastname") . " " . $import_q->f("market_firstname");
  $csv_sep = $import_q->f("import_separator");
  $enclosed = $import_q->f("import_enclosed");
  $desc = $import_q->f("import_desc");
  eval ($desc);

  if ($csv_sep == $ccsvd_sc) $csvd_sc = "checked";
  if ($csv_sep == $ccsvd_tab) $csvd_tab = "checked";

  while (list($field, $el) = each($comp)) {
    global ${$el["label"]};
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
    global ${$el["label"]};
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
      <td class=\"detailLabel\">$l_market</td>
      <td class=\"detailText\">$market</td>
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
  </table>";

  $block .= html_import_consult_file($import_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Consultation File block
// Parameters:
//   - $import_q : Import database result 
///////////////////////////////////////////////////////////////////////////////
function html_import_consult_file($import_q) {
  global $l_import, $l_file, $l_sample_file, $l_check_file, $l_test_file;
  global $l_import_file;
  global $display;

  $id = $import_q->f("import_id");
  $name = $import_q->f("import_name");

  $url = url_prepare("import_index.php");
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
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
        <input type=\"submit\" name=\"submit\" value=\"$l_check_file\" /></td>
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
    </form>

    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_import\" />
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_import_file</td>
      <td class=\"detailForm\">
        <input type=\"file\" name=\"import_file\" id=\"import_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_import_file\" /></td>
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
  global $ccsvd_tab, $l_sample_file, $l_file_error;
  global $display;

  $file = $import["file"];
  $file_name = $import["file_name"];
  $file_size = $import["file_size"];
  $csv_sep = $import_q->f("import_separator");
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }
  $enclosed = trim($import_q->f("import_enclosed"));

  $map = get_import_field_mapping($import_q);

  // Sample header
  $mapping_head .= "<tr>";
  while (list($num, $el) = each($map)) {
    if (($num != "default") && ($num != "field")) {
      $label = $el["label"];
      $mapping_head .= "<td class=\"detailLabel\">$num-$label</td>";
    }
  }
  $mapping_head .= "</tr>";

  // Sample file display
  if (! file_exists($file)) {
    $display["msg"] .= display_err_msg("$file : $l_file_error");
    return;
  }
 
  $num_line = 0;
  $fh = fopen($file, 'r');
  while ( ($row = fgetcsv($fh, 4000, $csv_sep, $enclosed)) && ($num_line < $max_rows) ) {
    $num_line++;

    $mapping .= "<tr>";
    while (list($num, $value) = each($row)) {
      $num++; // $map mapping start at 1 and $row at 0
      if ($map[$num]["label"] != "") {
	$mapping .= "<td class=\"detailForm\">$value</td>";
      }
    }
    $mapping .= "</tr>";
  }
  fclose($fh);

  $block = "
    <div class=\"detailHead\">$l_sample_file : $file_name</div>
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
// HTML Display Import File Core (Test and Import)
// Parameters:
//   - $import_q : Import database result 
//   - $import[] : import hash values
///////////////////////////////////////////////////////////////////////////////
function html_import_file_import($import_q, $import) {
  global $ccsvd_tab, $l_test_file, $l_import_file;
  global $l_company, $l_contact, $l_info, $l_result;
  global $l_similar_company, $l_similar_contact, $l_contact_defaultname;
  global $l_company_defaultname, $ccsvd_tab;
  global $l_invalid_row, $l_invalid_rows, $l_conflict_rows;
  global $l_import_end,$l_file_error, $l_lines_processed, $l_conflict_detected;
  global $l_module_company, $l_module_contact, $l_nb_insert, $l_nb_update;
  global $display;

  $file = $import["file_saved"];
  if ($file == "") {
    $file = $import["file"];
  }
  $file_name = $import["file_name"];
  $file_size = $import["file_size"];
  $csv_sep = $import_q->f("import_separator");
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }
  $enclosed = trim($import_q->f("import_enclosed"));
  $run_mode = $import["run_mode"];
  $row_start = ($import["row_start"] ? $import["row_start"] : 1);
  $conflict_action = $import["conflict_action"];
  $company_id = $import["company_id"];
  $contact_id = $import["contact_id"];

  if ($run_mode == "test") {
    $title = "$l_test_file : $file_name";
  } else {
    $title = "$l_import_file : $file_name";
    // Get the country hash : countries["name"] = $id
    $countries = get_import_country_hash();
    $cats1 = get_import_companycategory1_hash();
    $kinds = get_import_kind_hash();
    $functions = get_import_function_hash();
  }
    
  $map = get_import_field_mapping($import_q);

  // File processing
  $stop = false;
  $nb_line = 0;
  $nb_line_err = 0;
  $nb_line_conf = 0;
  $nb_comp_ins = 0;
  $nb_comp_upd = 0;
  $nb_con_ins = 0;
  $nb_con_upd = 0;
  if (! file_exists($file)) {
    $display["msg"] .= display_err_msg("$file : $l_file_error");
    return;
  }

  $cpt_line = 1;
  $fh = fopen($file, 'r');
  while ($cpt_line < $row_start) {
    $cpt_line++;
    $tmp = fgets($fh, 5000);
  }

  // Company & Contact Conflict Management ------------------------------------
  if ($conflict_action != "") {
    $log = "";
    $log["num_line"] = $row_start;
    $nb_line++;

    $cpt_line++;
    $row = fgetcsv($fh, 4000, $csv_sep, $enclosed);
    $fields = get_import_row_mapping($row, $map);

    // Test user decision
    // Insert the new company
    if ($conflict_action == "company_insert") {
      $cid = run_query_import_company_insert($import_q, $fields, $map["default"], $countries, $cats1);
      $nb_comp_ins++;

      // If contact, insert
      if (import_row_has_contact($fields)) {
	run_query_import_contact_insert($import_q, $cid, $fields, $map["default"], $countries, $kinds, $functions);
	$nb_con_ins++;
      }

      // Others company choices (update or keep) need then contact choice
    } else if (($conflict_action == "company_nothing")
              || ($conflict_action == "company_update")) {

      if ($conflict_action == "company_update") {
	run_query_import_company_update($import_q, $fields, $map["default"], $countries, $cats1, $company_id);
	$nb_comp_upd++;
      }

      // test for contact
      if ( (import_row_has_contact($fields)) &&
	   ( ($fields["con_ln"] != "") || ($map["default"]["con_ln"] != ""))) {
	
	// Test if similar contact exist --------------------------------------
	$conts = get_import_similar_contact($fields, $company_id);
	if (is_array($conts)) {
	  $log["display"] = true;
	  $log["res"] = $l_similar_contact;
	  // Automatic mode
	  if ($run_mode == "auto") {
	    // XXXX action auto
	    
	    // Test mode (no database updates) Should never happens here
	  } else if ($run_mode == "test") {
	    // Normal mode (Interactive)
	  } else {
	    $stop = true;
	    $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	    $display["msg"] .= display_warn_msg($l_conflict_detected);
	  }
	} else {
	  // No similar contacts exists ---------------------------------------
	  if ($fields["con_ln"] == "") {
	    $log["res"] .= $l_contact_defaultname;
	  }
	  run_query_import_contact_insert($import_q, $company_id, $fields, $map["default"], $countries, $kinds, $functions);
	  $nb_con_ins++;
	}

      } else {
	// Row has no contact : End row Conflict
      }

      // Update the existing contact
    } else if ($conflict_action == "contact_update") {
      run_query_import_contact_update($import_q, $fields, $map["default"], $countries, $kinds, $functions, $contact_id);
      $nb_con_upd++;

    } else if ($conflict_action == "contact_insert") {
      run_query_import_contact_insert($import_q, $company_id, $fields, $map["default"], $countries, $kinds, $functions);
      $nb_con_ins++;

    } else if ($conflict_action == "contact_nothing") {
      // Nothing to do
    }
  }


  // Loop through the file ----------------------------------------------------
  while ( ($row = fgetcsv($fh, 4000, $csv_sep, $enclosed))
	  && ($stop == false) ) {
    $log = "";
    $log["num_line"] = $cpt_line;
    $nb_line++;
    $cpt_line++;

    $fields = get_import_row_mapping($row, $map);

    // Test if company is valid
    if (import_is_row_valid($fields, $map)) {

      if ($fields["comp_name"] == "") {
	$log["res"] = $l_company_defaultname;
      }

      // Test if similar companies exist --------------------------------------
      $comps = get_import_similar_company($fields, $map);
      if (is_array($comps)) {
	$nb_line_conf++;
	$log["display"] = true;
	$log["res"] .= $l_similar_company;

	// Automatic mode
	if ($run_mode == "auto") {
	  // XXXX action auto

	  // Test mode (no database updates) Do nothing : only log
	} else if ($run_mode == "test") {

	  // Normal mode (Interactive)
	} else {
	  $stop = true;
	  $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps);
	  $display["msg"] .= display_warn_msg($l_conflict_detected);
	}
	
	// No similar companies exists ----------------------------------------
      } else {

	// Test mode (no database updates) Do nothing : only log
	if ($run_mode == "test") {

	  // Auto or Normal mode (Interactive)
	} else {
	  // Insert the company
	  $cid = run_query_import_company_insert($import_q, $fields, $map["default"], $countries, $cats1);
	  $nb_comp_ins++;
	  // If row has a contact
	  if (import_row_has_contact($fields)) {
	    // We insert If lastname filled, or if lastname empty but default
	    if ($fields["con_ln"] != "") {
	      run_query_import_contact_insert($import_q, $cid, $fields, $map["default"], $countries, $kinds, $functions);
	      $nb_con_ins++;
	    } else if ($fields["con_ln"] == "") {
	      if ($map["default"]["con_ln"] != "") {
		$log["res"] .= $l_contact_defaultname;
		run_query_import_contact_insert($import_q, $cid, $fields, $map["default"], $countries, $kinds, $functions);
		$nb_con_ins++;
	      }
	    }
	  }
	}
      }

      // Company row not valid
    } else {
      $nb_line_err++;
      $log["res"] = $l_invalid_row;
    }

    $log_block .= display_import_log_line($log, $fields, $comps);
  }

  fclose($fh);

  $msg = "$nb_line $l_lines_processed";
  if (isset($log["num_line"])) {
    $msg .= " ($row_start - " . $log["num_line"] .")";
  }
  if (! $stop) {
    $msg .= ". $l_import_end";
  }
  $msg .= "<br />$l_module_company : $nb_comp_ins $l_nb_insert, $nb_comp_upd $l_nb_update";
  $msg .= "<br />$l_module_contact : $nb_con_ins $l_nb_insert, $nb_con_upd $l_nb_update";

  $display["msg"] .= display_ok_msg("$msg");
  if (($nb_line_err > 0) || ($nb_line_conf > 0)) {
    $display["msg"] .= display_info_msg("$nb_line_err $l_invalid_rows, $nb_line_conf $l_conflict_rows");
  }

  // Test header
  $log_head .= "<tr>
    <td class=\"detailLabel\">#</td>
    <td class=\"detailLabel\">$l_company</td>
    <td class=\"detailLabel\">$l_info</td>
    <td class=\"detailLabel\">$l_contact</td>
    <td class=\"detailLabel\">$l_info</td>
    <td class=\"detailLabel\">$l_result</td>
  </tr>";

  $block = "
    $conflict_block

    <div class=\"detailHead\">$title</div>
    <table class=\"detail\">
    <tr>
      $log_head
    </tr>
      $log_block
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import Form
// Parameters :
//   - $log      : log line info array
//   - $log_head : log line header
//   - $fields   : company row info array
//   - $comps    : similar company info array
//   - $cons     : similar contact info array
///////////////////////////////////////////////////////////////////////////////
function display_import_conflict_block($import, $log, &$log_head, $fields, $comps, $cons="") {
  global $path, $l_validate;
  global $l_conflict, $l_comp_nothing, $l_comp_insert, $l_comp_update;
  global $l_con_nothing, $l_con_insert, $l_con_update;

  $id = $import["id"];
  $file = $import["file"];
  $file_name = $import["file_name"];
  $file_saved = $import["file_saved"];
  $comp_id = $import["company_id"];

  $num_line = $log["num_line"];

  $name = $fields["comp_name"];
  $info = $fields["comp_zip"] . " " . $fields["comp_town"] . " " . $fields["comp_cdx"];
  $con = $fields["con_ln"] . " " . $fields["con_fn"];
  $con_info = $fields["con_func"] . " " . $fields["con_tit"] . " " . $fields["con_pho"];
  $res = $log["res"];
  $line_log .= "<tr>
      <td class=\"detailForm\">$num_line</td>
      <td class=\"detailForm\">$name</td>
      <td class=\"detailForm\">$info</td>
      <td class=\"detailForm\">$con</td>
      <td class=\"detailForm\">$con_info</td>
      <td class=\"detailForm\">$res";

  if (is_array($comps)) {
    $checked = false;
    while ( list($num, $comp) = each($comps) ) {
      $rd_check = ($checked) ? "" : "checked";
      $checked = true;
      $comp_id = $comp["comp_id"];
      $url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id");
      $line_log .= "<br />
        <input type=\"radio\" name=\"rd_comp\" value=\"$comp_id\" $rd_check />
        <a target=\"New\" href=\"$url\">".$comp["comp_name"] . "</a> - " . $comp["comp_zip"];
    }
    $line_log .= "</td>
      </tr>";
    $lab_nothing = $l_comp_nothing;
    $lab_insert = $l_comp_insert;
    $lab_update = $l_comp_update;
    $act_nothing = "company_nothing";
    $act_insert = "company_insert";
    $act_update = "company_update";
  }

  if (is_array($cons)) {
    $checked = false;
    while ( list($num, $con) = each($cons) ) {
      $rd_check = ($checked) ? "" : "checked";
      $checked = true;
      $con_id = $con["con_id"];
      $url = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con_id");
      $line_log .= "<br />
        <input type=\"radio\" name=\"rd_con\" value=\"$con_id\" $rd_check />
        <a target=\"New\" href=\"$url\">".$con["con_lname"] . " ".$con["con_fname"] ."</a>";
    }
    $line_log .= "</td>
      </tr>";
    $lab_nothing = $l_con_nothing;
    $lab_insert = $l_con_insert;
    $lab_update = $l_con_update;
    $act_nothing = "contact_nothing";
    $act_insert = "contact_insert";
    $act_update = "contact_update";
  }

  $url = url_prepare("import_index.php");

  $block = "
    <form method=\"post\" name=\"f_file\" action=\"$url\">

    <div class=\"detailHead\">$l_conflict</div>
    <table class=\"detail\">
    <tr>
      $log_head
    </tr>
      $line_log
    </table>

    <table class=\"detail\">
    <tr>    
      <td class=\"detailLabel\">
        <input type=\"radio\" name=\"rd_conflict\" value=\"$act_nothing\" checked />$lab_nothing
        <input type=\"radio\" name=\"rd_conflict\" value=\"$act_insert\" />$lab_insert
        <input type=\"radio\" name=\"rd_conflict\" value=\"$act_update\" />$lab_update
      </td>
    </tr>
    <tr colspan=\"3\">
      <td class=\"detailLabel\">
        <input type=\"hidden\" name=\"import_file\" value=\"$file\" />
        <input type=\"hidden\" name=\"import_file_name\" value=\"$file_name\" />
        <input type=\"hidden\" name=\"file_saved\" value=\"$file_saved\" />
        <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
        <input type=\"hidden\" name=\"row_start\" value=\"$num_line\" />
        <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
        <input type=\"hidden\" name=\"action\" value=\"file_import\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_validate\" />
      </td> 
    </table>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import log line
// Parameters :
//   - $log    : log line info array
//   - $fields : company row info array
//   - $comps  : similar company info array
///////////////////////////////////////////////////////////////////////////////
function display_import_log_line($log, $fields, $comps) {
  global $path;

  $step = 1;
  $num_line = $log["num_line"];
  $display_log = false;

  // Test if similar companies exist
  if ( ($log["display"]) || (($num_line % $step) == 0) ) {
    $display_log = true;
  }

  if ($display_log) {
    $name = $fields["comp_name"];
    $info = $fields["comp_zip"] . " " . $fields["comp_town"] . " " . $fields["comp_cdx"];
    $con = $fields["con_ln"] . " " . $fields["con_fn"];
    $con_info = $fields["con_func"] . " " . $fields["con_tit"] . " " . $fields["con_pho"];
    $res = $log["res"];
    $line_log .= "<tr>
      <td class=\"detailForm\">$num_line</td>
      <td class=\"detailForm\">$name</td>
      <td class=\"detailForm\">$info</td>
      <td class=\"detailForm\">$con</td>
      <td class=\"detailForm\">$con_info</td>
      <td class=\"detailForm\">$res";

    if (is_array($comps)) {
      while ( list($num, $comp) = each($comps) ) {
	$cid = $comp["comp_id"];
	$url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$cid");
	$line_log .= "<br />";
	$line_log .= "<a target=\"New\" href=\"$url\">".$comp["comp_name"] . "</a> - " . $comp["comp_zip"];
      }
      $line_log .= "</td>
      </tr>";
    }
  }

  return $line_log;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import Form
// Parameters :
//   - $action   : action called
//   - $import[] : default or transmitted values
//     keys used : name, subject, usercreate
//   - $import_q : DBO : information about the import (null for new import)
//   - $dsrc_q   : database object with datasource list
//   - $usr_q    : database object with userobm list
///////////////////////////////////////////////////////////////////////////////
function html_import_form($action, $import, $import_q, $dsrc_q, $usr_q) {
  global $display, $c_undef, $l_undef, $l_insert, $l_update, $l_back;
  global $l_import, $l_name, $l_datasource, $l_market, $l_csv_sep, $l_enclosed;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $l_company, $l_number, $l_category1, $l_address, $l_postcode, $l_town;
  global $l_expresspostal, $l_lang, $l_country, $l_mailing_ok_msg;
  global $l_phone, $l_fax, $l_web, $l_email, $l_mailing_ok, $l_comment;
  global $l_contact, $l_lastname, $l_firstname, $l_function, $l_title, $l_hphone, $l_mphone;

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $import_q->f("import_id");
    $usercreate = $import_q->f("list_usercreate");
    $name = $import_q->f("import_name");
    $dsrc = $import_q->f("import_datasource_id");
    $market = $import_q->f("import_marketingmanager_id");
    $sep = $import_q->f("import_separator");
    $enc = $import_q->f("import_enclosed");
    $desc = $import_q->f("import_desc");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($import["id"])) { $id = $import["id"]; }
  if (isset($import["name"])) { $name = stripslashes($import["name"]); }
  if (isset($import["datasource"])) { $dsrc = $import["datasource"]; }
  if (isset($import["market"])) { $market = $import["market"]; }
  if (isset($import["sep"])) { $sep = $import["sep"]; }
  if (isset($import["enclosed"])) { $enc = $import["enclosed"]; }

  eval ($desc);
  $enc = htmlspecialchars(trim($enc));

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $market) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  if ($sep == $ccsvd_sc) $csvd_sc = "checked";
  if ($sep == $ccsvd_tab) $csvd_tab = "checked";

  $url = url_prepare("import_index.php");
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"$url\"
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
    <td class=\"detailLabel\">$l_market</td>
    <td class=\"detailForm\">$sel_market</td>
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
      <input type=\"text\" size=\"3\" name=\"tf_comp_cat1\" value=\"$comp_cat1\" /></td>
    <td class=\"detailLabel\">$l_category1</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_cat1_d\" value=\"$comp_cat1_d\" /></td>
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
      <input type=\"text\" size=\"3\" name=\"tf_comp_cdx\" value=\"$comp_cdx\" /></td>
    <td class=\"detailLabel\">$l_expresspostal</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_cdx_d\" value=\"$comp_cdx_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_comp_ctry\" value=\"$comp_ctry\" /></td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_comp_ctry_d\" value=\"$comp_ctry_d\" /></td>
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
      <input type=\"text\" size=\"3\" name=\"tf_con_lang\" value=\"$con_lang\" /></td>
    <td class=\"detailLabel\">$l_lang</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_lang_d\" value=\"$con_lang_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_func\" value=\"$con_func\" /></td>
    <td class=\"detailLabel\">$l_function</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_func_d\" value=\"$con_func_d\" /></td>
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
      <input type=\"text\" size=\"3\" name=\"tf_con_cdx\" value=\"$con_cdx\" /></td>
    <td class=\"detailLabel\">$l_expresspostal</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_cdx_d\" value=\"$con_cdx_d\" /></td>
  </tr>
  <tr>
    <td class=\"detailForm\">
      <input type=\"text\" size=\"3\" name=\"tf_con_ctry\" value=\"$con_ctry\" /></td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_ctry_d\" value=\"$con_ctry_d\" /></td>
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
      <input type=\"text\" size=\"3\" name=\"tf_con_mailok\" value=\"$con_mailok\" /></td>
    <td class=\"detailLabel\">$l_mailing_ok</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_con_mailok_d\" maxlength=\"1\" value=\"$con_mailok_d\" /> $l_mailing_ok_msg</td>
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
// Display warning before an import deletion
// We ask confirmation or cancel
// Parameters:
//   - $id : import id
///////////////////////////////////////////////////////////////////////////////
function dis_import_warn_delete($id) {
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
// Display the context about an import insertion or update
// When similar imports exist we show these and ask confirmation
// Parameters:
//   - $import_q : import database result (at least 1 row)
//   - $import[] : values for insertion/update (if confirmation)
//     keys used : name, subject
///////////////////////////////////////////////////////////////////////////////
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


?>
