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
$fieldnames['import_name'] = $l_name;
$fieldnames['import_datasource'] = $l_datasource;
$fieldnames['import_market'] = $l_market;
$fieldnames['import_separator'] = $l_csv_sep;
$fieldnames['import_enclosed'] = $l_enclosed;


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
  global $path;

  if (($fieldname == 'import_name')  && $link_ok) {
    $res['url'] = "import_index.php?action=detailconsult&amp;import_id=".$OD->data_set->f('import_id');
  }
  
  // For contacts urls (link):
  else if ($fieldname == 'import_market') {
    $res['name'] = $OD->data_set->f('market_lastname') . ' ' . $OD->data_set->f('market_firstname');
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters:
//   - $import[] : hash with import values
///////////////////////////////////////////////////////////////////////////////
function dis_import_search_form($import='') {
  global $obm;

  $dsrc_q = run_query_global_datasource();
  $users = of_usergroup_get_group_users($obm['group_admin'], true);
  $block = html_import_search_form($import, $dsrc_q, $users);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters : 
//   - $import[] : default form values
//     keys used : name
//   - $dsrc_q   : database object with datasource list
//   - $users    : array with userobm list
///////////////////////////////////////////////////////////////////////////////
function html_import_search_form ($import, $dsrc_q, $users) {
  global $l_name, $l_datasource, $l_market, $l_find;
  global $c_all, $l_all;

  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($import['name']);
  $dsrc = stripslashes($import['datasource']);
  $market = $import['market'];

  // data source select
  $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f('datasource_id');
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f('datasource_name') . "</option>\n";
  }
  $sel_dsrc .= '</select>';

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  if (is_array($users) && (count($users) > 0)) {
    foreach($users as $u_id => $one_user) {
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $market) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= '>'. $one_user['lastname'].' '.$one_user['firstname'] . "</option>\n";
    }
  }
  $sel_market .= '</select>';

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" class=\"search\"
    action=\"".url_prepare("import_index.php")."\">

    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </label>
    <label>$l_datasource<br />
      $sel_dsrc
    </label>
    <label>$l_market<br />
      $sel_market
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\" />
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
  global $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'import');
  $obm_q = run_query_import_search($import);
  $nb = $obm_q->num_rows_total();
  if ($nb == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
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

  $name = urlencode(stripslashes($import['name']));
  $contact = urlencode(stripslashes($list['contact']));

  $display['msg'] .= display_info_msg("$nb $l_found");

  $url = url_prepare("import_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact");

  $od = new OBM_DISPLAY('DATA', $prefs, 'import');
  $od->data_set = $obm_q;
  $od->data_header = 'both';
  $od->data_url = $url;
  $block .= $od->display('dis_data_import');

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import detail page
// Parameters:
//   - $id : import id
///////////////////////////////////////////////////////////////////////////////
function dis_import_consult($params) {
  global $display, $l_err_reference;

  $id = $params['import_id'];
  if ($id > 0) {
    $import_q = run_query_import_detail($id);
    if ($import_q->num_rows() != 1) {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
    $display['detailInfo'] = display_record_info($import_q);
    $block = html_import_consult($import_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

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

  $id = $import_q->f('import_id');
  $usercreate = $import_q->f('import_usercreate');
  $userupdate = $import_q->f('import_userupdate');
  $timecreate = $import_q->f('timecreate');
  $timeupdate = $import_q->f('timeupdate');
  $name = $import_q->f('import_name');
  $dsrc = $import_q->f('import_datasource_id');
  $datasource = $import_q->f('datasource_name');
  $market = $import_q->f('market_lastname') . ' ' . $import_q->f('market_firstname');
  $csv_sep = $import_q->f('import_separator');
  $enclosed = $import_q->f('import_enclosed');
  $desc = $import_q->f('import_desc');
  eval ($desc);

  if ($csv_sep == $ccsvd_sc) $csvd_sc = 'checked';
  if ($csv_sep == $ccsvd_tab) $csvd_tab = 'checked';

  while (list($field, $el) = each($comp)) {
    global ${$el['label']};
    $label = ${$el['label']};
    $value = $el['value'];
    $default = $el['default'];
    if (($label != '') && (($value != '') || ($default != ''))) {
      $mapping .= "
  <tr>
    <th>$label</th>
    <td>$value</td>
    <td>$default</td>
  </tr>";
      if ($value != '') {
	$map["$value"] = $field;
      }
    }
  }

  $mapping .= "
  <tr>
    <td colspan=\"3\"><h1 class=\"title\">$l_contact</h1></td>
  </tr>";

  while (list($field, $el) = each($con)) {
    global ${$el['label']};
    $label = ${$el['label']};
    $value = $el['value'];
    $default = $el['default'];
    if (($label != '') && (($value != '') || ($default != ''))) {
      $mapping .= "
  <tr>
    <th>$label</th>
    <td>$value</td>
    <td>$default</td>
  </tr>";
    }
  }

  $display['detailInfo'] = display_record_info($import_q);
  $display['title'] = $name;

  $block = "
  <div class=\"detail extra\">
    <h1>$l_import</h1>
    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr><tr>
      <th>$l_datasource</th>
      <td>$datasource</td>
    </tr><tr>
      <th>$l_market</th>
      <td>$market</td>
    </tr><tr>
      <th>$l_csv_sep</th>
      <td>$csv_sep</td>
    </tr><tr>
      <th>$l_enclosed</th>
      <td>$enclosed</td>
    </tr>
    </table>
    </div>

  <div class=\"detail extra\">
  <h1>$l_mapping</h1>
  <table>
  <tr>
    <td>$l_field</td>
    <td>$l_field_num</td>
    <td>$l_default</td>
  </tr>
  <tr>
    <td colspan=\"3\"><h1 class=\"title\">$l_company</h1></td>
  </tr>
  $mapping
  </table>
  </div>
";

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

  $id = $import_q->f('import_id');
  $name = $import_q->f('import_name');

  $url = url_prepare('import_index.php');
  $display['title'] = $name;

  $block = "
    <div class=\"detail extra\">
    <h1>$l_file</h1>
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_sample\" />
      <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
    <table>
    <tr>
      <th>$l_sample_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_check_file\" /></td>
    </tr>
    </table>
    </form>

    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_test\" />
      <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
    
    <table>
    <tr>
      <th>$l_test_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_test_file\" /></td>
    </tr>
    </table>
    </form>

    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"file_import\" />
      <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
    <table>
    <tr>
      <th>$l_import_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_import_file\" /></td>
    </tr>
    </table>
    </form>
    </div>";

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

  $file = $import['file'];
  $file_name = $import['file_name'];
  $file_size = $import['file_size'];
  $csv_sep = $import_q->f('import_separator');
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }
  $enclosed = $import_q->f('import_enclosed');
  if($enclosed == '') {
    $enclosed = ' ';
  } else {
    $enclosed = trim($enclosed);
  }
  $map = get_import_field_mapping($import_q);

  // Sample header
  $mapping_head .= '<tr>';
  while (list($num, $el) = each($map)) {
    if (($num != 'default') && ($num != 'field')) {
      $label = $el['label'];
      $mapping_head .= "<th>$num-$label</th>";
    }
  }
  $mapping_head .= '</tr>';

  // Sample file display
  if (! file_exists($file)) {
    $display['msg'] .= display_err_msg("$file : $l_file_error");
    return;
  }
 
  $num_line = 0;
  $fh = fopen($file, 'r');
  while ( ($row = fgetcsv($fh, 4000, $csv_sep, $enclosed)) && ($num_line < $max_rows) ) {
    $num_line++;

    $mapping .= '<tr>';
    while (list($num, $value) = each($row)) {
      $num++; // $map mapping start at 1 and $row at 0
      if ($map[$num]['label'] != '') {
	$mapping .= "<td>$value</td>";
      }
    }
    $mapping .= "</tr>";
  }
  fclose($fh);

  $block = "
    <div class=\"detail extra\">
    <h1>$l_sample_file : $file_name</h1>
    <table>
    <tr>
      $mapping_head
    </tr>
      $mapping
    </table>
    </form>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import File Core (Test and Import)
// Parameters:
//   - $import_q : Import database result 
//   - $import[] : import hash values
///////////////////////////////////////////////////////////////////////////////
function html_import_file_import($import_q, $import) {
  global $cgp_user, $ccsvd_tab, $l_test_file, $l_import_file;
  global $l_company, $l_contact, $l_info, $l_result, $l_used_company;
  global $l_similar_company, $l_similar_contact, $l_contact_defaultname;
  global $l_company_defaultname, $ccsvd_tab;
  global $l_invalid_row, $l_invalid_rows, $l_conflict_rows;
  global $l_import_end,$l_file_error, $l_lines_processed, $l_conflict_detected;
  global $l_module_company, $l_module_contact, $l_nb_insert, $l_nb_update;
  global $display;

  $file = $import['file_saved'];
  if ($file == '') {
    $file = $import['file'];
  }
  $file_name = $import['file_name'];
  $file_size = $import['file_size'];
  $csv_sep = $import_q->f('import_separator');
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }
  $enclosed = $import_q->f('import_enclosed');
  if ($enclosed == '') {
    $enclosed = ' ';
  } else {
    $enclosed = trim($enclosed);  
  }
  $run_mode = $import['run_mode'];
  $row_start = ($import['row_start'] ? $import['row_start'] : 1);
  $conflict_action = $import['conflict_action'];
  $company_id = $import['company_id'];
  $contact_id = $import['contact_id'];
  $auto_conflict = $import['auto_conflict'];
  if ($run_mode == 'test') {
    $title = "$l_test_file : $file_name";
    $countries = get_import_country_hash();
  } else {
    $title = "$l_import_file : $file_name";
    // Get the country hash : countries['name'] = $id
    $countries = get_import_country_hash();
    $kinds = get_import_kind_hash();
    $functions = get_import_function_hash();

    // User data categories handling
    if (is_array($cgp_user['company']['category'])) {
      foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
	$hash = get_import_category_user_hash($cat_name);
	$userdata['company']['category'][$cat_name] = $hash;
      }
    }
    if (is_array($cgp_user['contact']['category'])) {
      foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
	$hash = get_import_category_user_hash($cat_name);
	$userdata['contact']['category'][$cat_name] = $hash;
      }
    }
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
    $display['msg'] .= display_err_msg("$file : $l_file_error");
    return;
  }

  $cpt_line = 1;
  $fh = fopen($file, 'r');
  while ($cpt_line < $row_start) {
    $cpt_line++;
    $tmp = fgets($fh, 5000);
  }

  // Company & Contact Conflict Management ------------------------------------
  if ($conflict_action != '') {
    $log = '';
    $log['num_line'] = $row_start;
    $nb_line++;

    $cpt_line++;
    $row = fgetcsv($fh, 4000, $csv_sep, $enclosed);
    $fields = get_import_row_mapping($row, $map);

    // Test user decision
    // Insert the new company
    if ($conflict_action == 'company_insert') {
      $cid = run_query_import_company_insert($import_q, $fields, $map['default'], $countries, $userdata);
      $nb_comp_ins++;

      // If contact, insert
      if (import_row_has_contact($fields)) {
	run_query_import_contact_insert($import_q, $cid, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	$nb_con_ins++;
      }

      // Others company choices (update or keep) need then contact choice
    } else if (($conflict_action == 'company_nothing')
              || ($conflict_action == 'company_update')) {

      if ($conflict_action == 'company_update') {
	run_query_import_company_update($import_q, $fields, $map['default'], $countries, $userdata, $company_id);
	$nb_comp_upd++;
      }

      // test for contact
      if ( (import_row_has_contact($fields)) &&
	   ( ($fields['con_ln'] != '') || ($map['default']['con_ln'] != ''))) {
	
	// Test if similar contact exist --------------------------------------
	$conts = get_import_similar_contact($fields, $company_id);
	if (is_array($conts)) {
	  $log['display'] = true;
	  $log['res'] = $l_similar_contact;
	  // Automatic mode
	  if ($run_mode == 'auto') {
	    // XXXX action auto
	    
	    // Test mode (no database updates) Should never happens here
	  } else if ($run_mode == 'test') {
	    // Normal mode (Interactive)
	  } else {
	    $stop = true;
	    $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	    $display['msg'] .= display_warn_msg($l_conflict_detected);
	  }
	} else {
	  // No similar contacts exists ---------------------------------------
	  if ($fields['con_ln'] == '') {
	    $log['res'] .= $l_contact_defaultname;
	  }
	  run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	  $nb_con_ins++;
	}

      } else {
	// Row has no contact : End row Conflict
      }

      // Update the existing contact
    } else if ($conflict_action == 'contact_update') {
      run_query_import_contact_update($import_q, $fields, $map['default'], $countries, $kinds, $functions, $userdata, $contact_id);
      $nb_con_upd++;

    } else if ($conflict_action == 'contact_insert') {
      run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
      $nb_con_ins++;

    } else if ($conflict_action == 'contact_nothing') {
      // Nothing to do
    }
  }


  // Loop through the file : Run until conflict or End ------------------------
  while ( ($row = fgetcsv($fh, 4000, $csv_sep, $enclosed))
	  && ($stop == false) ) {
    $log = '';
    $log['num_line'] = $cpt_line;
    $nb_line++;
    $cpt_line++;

    $fields = get_import_row_mapping($row, $map);

    // Test if company is valid
    if (import_is_row_valid($fields, $map)) {

      if ($fields['comp_name'] == '') {
	$log['res'] = $l_company_defaultname;
      }

      $comps = get_import_similar_company($fields, $map,$countries);

      // If auto conflict management is enabled
      // If same company do not update it
      if ($auto_conflict == 1 && is_array($comps['same'])) {
	$company_id = $comps['same'][0]['comp_id'];
	$log['res'] = $l_used_company;

	// test for contact
	if ( (import_row_has_contact($fields)) &&
	     ( ($fields['con_ln'] != '') || ($map['default']['con_ln'] != ''))) {
	
	  // Test if similar contact exist ------------------------------------
	  $conts = get_import_similar_contact($fields, $company_id);
	  if (is_array($conts)) {
	    $log['display'] = true;
	    $log['res'] = $l_similar_contact;
	    // Automatic mode
	    if ($run_mode == 'auto') {
	      // XXXX action auto
	      
	      // Test mode (no database updates) Should never happens here
	    } else if ($run_mode == 'test') {
	      // Normal mode (Interactive)
	    } else {
	      $stop = true;
	      $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	      $display['msg'] .= display_warn_msg($l_conflict_detected);
	    }
	  } else {
	    // Test mode (no database updates) Do nothing : only log
	    if ($run_mode == 'test') {

	      // Auto or Normal mode (Interactive)
	    } else {
	      // No similar contacts exists -----------------------------------
	      if ($fields['con_ln'] == '') {
		$log['res'] .= $l_contact_defaultname;
	      }
	      run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	      $nb_con_ins++;
	    }
	  }

	} else {
	// Row has no contact
	}
        
      // If same company update it
      } elseif (($auto_conflict == 2 || $auto_conflict == 3) && is_array($comps['same'])) {
	$company_id = $comps['same'][0]['comp_id'];
	$log['res'] = $l_used_company;
	run_query_import_company_update($import_q, $fields, $map['default'], $countries, $userdata, $company_id);
	$nb_comp_upd++;

	// test for contact
	if ( (import_row_has_contact($fields)) &&
	     ( ($fields['con_ln'] != '') || ($map['default']['con_ln'] != ''))) {
	
	  // Test if similar contact exist ------------------------------------
	  $conts = get_import_similar_contact($fields, $company_id);
	  if (is_array($conts)) {
	    $log['display'] = true;
	    $log['res'] = $l_similar_contact;
	    // Automatic mode
	    if ($run_mode == 'auto') {
	      // XXXX action auto
	      
	      // Test mode (no database updates) Should never happens here
	    } else if ($run_mode == 'test') {
	      // Normal mode (Interactive)
	    } else {
	      $stop = true;
	      $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	      $display['msg'] .= display_warn_msg($l_conflict_detected);
	    }
	  } else {
	    // Test mode (no database updates) Do nothing : only log
	    if ($run_mode == 'test') {

	      // Auto or Normal mode (Interactive)
	    } else {
	      // No similar contacts exists -----------------------------------
	      if ($fields['con_ln'] == '') {
		$log['res'] .= $l_contact_defaultname;
	      }
	      run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	      $nb_con_ins++;
	    }
          }
	} else {
	// Row has no contact
	}   
      // If same company update it
      } elseif ($auto_conflict == 3 && is_array($comps['similar']) && count($comps['similar']) == 1) {
	$company_id = $comps['similar'][0]['comp_id'];
	$log['res'] = $l_used_company;
	run_query_import_company_update($import_q, $fields, $map['default'], $countries, $userdata, $company_id);
	$nb_comp_upd++;

	// test for contact
	if ( (import_row_has_contact($fields)) &&
	     ( ($fields['con_ln'] != '') || ($map['default']['con_ln'] != ''))) {
	
	  // Test if similar contact exist ------------------------------------
	  $conts = get_import_similar_contact($fields, $company_id);
	  if (is_array($conts)) {
	    $log['display'] = true;
	    $log['res'] = $l_similar_contact;
	    // Automatic mode
	    if ($run_mode == 'auto') {
	      // XXXX action auto
	      
	      // Test mode (no database updates) Should never happens here
	    } else if ($run_mode == 'test') {
	      // Normal mode (Interactive)
	    } else {
	      $stop = true;
	      $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	      $display['msg'] .= display_warn_msg($l_conflict_detected);
	    }
	  } else {
	    // Test mode (no database updates) Do nothing : only log
	    if ($run_mode == 'test') {

	      // Auto or Normal mode (Interactive)
	    } else {
	      // No similar contacts exists -----------------------------------
	      if ($fields['con_ln'] == '') {
		$log['res'] .= $l_contact_defaultname;
	      }
	      run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	      $nb_con_ins++;
	    }
          }
	} else {
	// Row has no contact
	}   
      } elseif ($auto_conflict == 4 && !is_array($comps['same'])) {
        $company_id = run_query_import_company_insert($import_q, $fields, $map['default'], $countries, $userdata);
        $nb_comp_ins++;        

	// test for contact
	if ( (import_row_has_contact($fields)) &&
	     ( ($fields['con_ln'] != '') || ($map['default']['con_ln'] != ''))) {
	
	  // Test if similar contact exist ------------------------------------
	  $conts = get_import_similar_contact($fields, $company_id);
	  if (is_array($conts)) {
	    $log['display'] = true;
	    $log['res'] = $l_similar_contact;
	    // Automatic mode
	    if ($run_mode == 'auto') {
	      // XXXX action auto
	      
	      // Test mode (no database updates) Should never happens here
	    } else if ($run_mode == 'test') {
	      // Normal mode (Interactive)
	    } else {
	      $stop = true;
	      $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps, $conts);
	      $display['msg'] .= display_warn_msg($l_conflict_detected);
	    }
	  } else {
	    // Test mode (no database updates) Do nothing : only log
	    if ($run_mode == 'test') {

	      // Auto or Normal mode (Interactive)
	    } else {
	      // No similar contacts exists -----------------------------------
	      if ($fields['con_ln'] == '') {
		$log['res'] .= $l_contact_defaultname;
	      }
	      run_query_import_contact_insert($import_q, $company_id, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	      $nb_con_ins++;
	    }
          }
	} else {
	// Row has no contact
	}   
      // Similar company exists : conflict ====================================
      }	else if (is_array($comps['similar']) || is_array($comps['same'])) {
	$nb_line_conf++;
	$log['display'] = true;
	$log['res'] .= $l_similar_company;

	// Automatic mode
	if ($run_mode == 'auto') {
	  // XXXX action auto

	  // Test mode (no database updates) Do nothing : only log
	} else if ($run_mode == 'test') {

	  // Normal mode (Interactive)
	} else {
	  $stop = true;
	  $conflict_block = display_import_conflict_block($import, $log, $log_head, $fields, $comps);
	  $display['msg'] .= display_warn_msg($l_conflict_detected);
	}
	
	// No similar companies exists, we proceed ============================
      } else {

	// Test mode (no database updates) Do nothing : only log
	if ($run_mode == 'test') {

	  // Auto or Normal mode (Interactive)
	} else {
	  // Insert the company
	  $cid = run_query_import_company_insert($import_q, $fields, $map['default'], $countries, $userdata);
	  $nb_comp_ins++;
	  // If row has a contact
	  if (import_row_has_contact($fields)) {
	    // We insert If lastname filled, or if lastname empty but default
	    if ($fields['con_ln'] != '') {
	      run_query_import_contact_insert($import_q, $cid, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
	      $nb_con_ins++;
	    } else if ($fields['con_ln'] == '') {
	      if ($map['default']['con_ln'] != '') {
		$log['res'] .= $l_contact_defaultname;
		run_query_import_contact_insert($import_q, $cid, $fields, $map['default'], $countries, $kinds, $functions, $userdata);
		$nb_con_ins++;
	      }
	    }
	  }
	}
      }

      // Company row not valid
    } else {
      $nb_line_err++;
      $log['res'] = $l_invalid_row;
    }

    $log_block .= display_import_log_line($log, $fields, $comps);
  }

  fclose($fh);

  $msg = "$nb_line $l_lines_processed";
  if (isset($log['num_line'])) {
    $msg .= " ($row_start - " . $log['num_line'] .")";
  }
  if (! $stop) {
    $msg .= ". $l_import_end";
  }
  $msg .= "<br />$l_module_company : $nb_comp_ins $l_nb_insert, $nb_comp_upd $l_nb_update";
  $msg .= "<br />$l_module_contact : $nb_con_ins $l_nb_insert, $nb_con_upd $l_nb_update";

  $display['msg'] .= display_ok_msg("$msg");
  if (($nb_line_err > 0) || ($nb_line_conf > 0)) {
    $display['msg'] .= display_info_msg("$nb_line_err $l_invalid_rows, $nb_line_conf $l_conflict_rows");
  }

  // Test header
  $log_head .= "<tr>
    <th>#</th>
    <th>$l_company</th>
    <th>$l_info</th>
    <th>$l_contact</th>
    <th>$l_info</th>
    <th>$l_result</th>
  </tr>";

  $block = "
  <div class=\"detail extra\">
    $conflict_block

    <h1>$title</h1>
    <table>
    <tr>
      $log_head
    </tr>
      $log_block
    </table>
    </div>";

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
function display_import_conflict_block($import, $log, &$log_head, $fields, $comps, $cons='') {
  global $path, $l_validate;
  global $l_conflict, $l_comp_nothing, $l_comp_insert, $l_comp_update;
  global $l_con_nothing, $l_con_insert, $l_con_update,$l_same_company;
  global $l_auto_resolv, $l_always_ask, $l_always_do_nothing;
  global $l_always_update_same, $l_always_update_similar, $l_always_insert;

  $id = $import['import_id'];
  if(isset($import['auto_conflict'])) $auto_conflict = $import['auto_conflict'];
  else $auto_conflict = 0;
  $file = $import['file'];
  $file_name = $import['file_name'];
  $file_saved = $import['file_saved'];
  $comp_id = $import['company_id'];
  // Case same company was found, id is in $comps array
  if ($comp_id == '') {
    $comp_id = $comps['same'][0]['comp_id'];
  }

  $num_line = $log['num_line'];
  
  $name = $fields['comp_name'];
  $info = $fields['comp_zip'] . ' ' . $fields['comp_town'] . ' ' . $fields['comp_cdx'];
  $con = $fields['con_ln'] . ' ' . $fields['con_fn'];
  $con_info = $fields['con_func'] . ' ' . $fields['con_tit'] . ' ' . $fields['con_pho'];
  $res = $log['res'];
  $line_log .= "<tr>
      <td class=\"detailForm\">$num_line</td>
      <td class=\"detailForm\">$name</td>
      <td class=\"detailForm\">$info</td>
      <td class=\"detailForm\">$con</td>
      <td class=\"detailForm\">$con_info</td>
      <td class=\"detailForm\">$res";
  $checked = false;
  if (is_array($comps['same'])) {
    while ( list($num, $comp) = each($comps['same']) ) {
      $rd_check = ($checked) ? '' : 'checked';
      $checked = true;
      $comp_id = $comp['comp_id'];
      $url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id");
      $line_log .= "<br />
        <input title=\"$l_same_company\" type=\"radio\" name=\"company_id\" value=\"$comp_id\" $rd_check />
        <a title=\"$l_same_company\" target=\"New\" href=\"$url\"><em>".$comp['comp_name'] . "</em></a> - " . $comp['comp_zip'];
    }
    $lab_nothing = $l_comp_nothing;
    $lab_insert = $l_comp_insert;
    $lab_update = $l_comp_update;
    $act_nothing = 'company_nothing';
    $act_insert = 'company_insert';
    $act_update = 'company_update';

  }
  if (is_array($comps['similar'])) {
    while ( list($num, $comp) = each($comps['similar']) ) {
      $rd_check = ($checked) ? '' : 'checked';
      $checked = true;
      $comp_id = $comp['comp_id'];
      $url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id");
      $line_log .= "<br />
        <input type=\"radio\" name=\"company_id\" value=\"$comp_id\" $rd_check />
        <a target=\"New\" href=\"$url\">".$comp['comp_name'] . "</a> - " . $comp['comp_zip'];
    }
    $line_log .= "</td>
      </tr>";
    $lab_nothing = $l_comp_nothing;
    $lab_insert = $l_comp_insert;
    $lab_update = $l_comp_update;
    $act_nothing = 'company_nothing';
    $act_insert = 'company_insert';
    $act_update = 'company_update';
  }

  if (is_array($cons)) {
    $checked = false;
    while ( list($num, $con) = each($cons) ) {
      $rd_check = ($checked) ? '' : 'checked';
      $checked = true;
      $con_id = $con['con_id'];
      $url = url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con_id");
      $line_log .= "<br />
        <input type=\"radio\" name=\"contact_id\" value=\"$con_id\" $rd_check />
        <a target=\"New\" href=\"$url\">".$con['con_lname'] . ' '.$con['con_fname'] ."</a>";
    }
    $line_log .= "</td>
      </tr>";
    $lab_nothing = $l_con_nothing;
    $lab_insert = $l_con_insert;
    $lab_update = $l_con_update;
    $act_nothing = 'contact_nothing';
    $act_insert = 'contact_insert';
    $act_update = 'contact_update';
  }

  $url = url_prepare('import_index.php');
  ${"auto_check_$auto_conflict"} = "checked=\"checked\"";
  $block = "
    <form method=\"post\" name=\"f_file\" action=\"$url\">
    <fieldset class=\"detail extra\">
    <legend>$l_auto_resolv</legend>
    <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
    <input $auto_check_0 type=\"radio\" name=\"rd_auto_conflict\" id=\"auto_conflict_0\" value=\"0\" />
    <label for=\"auto_conflict_0\">$l_always_ask</label><br />
    <input $auto_check_1 type=\"radio\" name=\"rd_auto_conflict\" id=\"auto_conflict_1\" value=\"1\" />
    <label for=\"auto_conflict_1\">$l_always_do_nothing</label><br />
    <input $auto_check_2 type=\"radio\" name=\"rd_auto_conflict\" id=\"auto_conflict_2\" value=\"2\" />
    <label for=\"auto_conflict_2\">$l_always_update_same</label><br />
    <input $auto_check_3 type=\"radio\" name=\"rd_auto_conflict\" id=\"auto_conflict_3\" value=\"3\" />
    <label for=\"auto_conflict_3\">$l_always_update_similar</label><br />
    <input $auto_check_4 type=\"radio\" name=\"rd_auto_conflict\" id=\"auto_conflict_4\" value=\"4\" />
    <label for=\"auto_conflict_4\">$l_always_insert</label>
    </fieldset>
    <fieldset class=\"detail extra\">
    <legend>$l_conflict </legend>
    <table class=\"detail\">
    <tr>
      $log_head
    </tr>
      $line_log
    </table>

    <table class=\"detail\">
    <tr>    
      <td class=\"detailLabel\">
      <input type=\"radio\" name=\"rd_conflict_action\" id=\"$act_nothing\" value=\"$act_nothing\" checked />
      <label accesskey=\"K\" for=\"$act_nothing\">$lab_nothing</label>
      <input type=\"radio\" name=\"rd_conflict_action\" id=\"$act_insert\" value=\"$act_insert\" />
      <label accesskey=\"I\" for=\"$act_insert\">$lab_insert</label>
      <input type=\"radio\" name=\"rd_conflict_action\" id=\"$act_update\" value=\"$act_update\" />
      <label accesskey=\"U\" for=\"$act_update\">$lab_update</label>
      </td>
    </tr>
    <tr colspan=\"3\">
      <td class=\"detailLabel\">
        <input type=\"hidden\" name=\"import_file\" value=\"$file\" />
        <input type=\"hidden\" name=\"import_file_name\" value=\"$file_name\" />
        <input type=\"hidden\" name=\"file_saved\" value=\"$file_saved\" />
        <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
        <input type=\"hidden\" name=\"row_start\" value=\"$num_line\" />
        <input type=\"hidden\" name=\"action\" value=\"file_import\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_validate\" />
      </td> 
    </table>
    </fieldset>
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
  $num_line = $log['num_line'];
  $display_log = false;

  // Test if similar companies exist
  if ( ($log['display']) || (($num_line % $step) == 0) ) {
    $display_log = true;
  }

  if ($display_log) {
    $name = $fields['comp_name'];
    $info = $fields['comp_zip'] . ' ' . $fields['comp_town'] . ' ' . $fields['comp_cdx'];
    $con = $fields['con_ln'] . ' ' . $fields['con_fn'];
    $con_info = $fields['con_func'] . ' ' . $fields['con_tit'] . ' ' . $fields['con_pho'];
    $res = $log['res'];
    $line_log .= "<tr>
      <td class=\"detailForm\">$num_line</td>
      <td class=\"detailForm\">$name</td>
      <td class=\"detailForm\">$info</td>
      <td class=\"detailForm\">$con</td>
      <td class=\"detailForm\">$con_info</td>
      <td class=\"detailForm\">$res";

    if (is_array($comps['same'])) {
      list($num, $comp) = each($comps['same']);
      $cid = $comp['comp_id'];
      $url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$cid");
      $line_log .= "<br /><a target=\"New\" href=\"$url\">".$comp['comp_name'] . "</a> - " . $comp['comp_zip'] . "</td>
      </tr>";
    } else if (is_array($comps['similar'])) {
      while ( list($num, $comp) = each($comps['similar']) ) {
	$cid = $comp['comp_id'];
	$url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$cid");
	$line_log .= "<br /><a target=\"New\" href=\"$url\">".$comp['comp_name'] . "</a> - " . $comp['comp_zip'];
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
//   - $users    : array with userobm list
///////////////////////////////////////////////////////////////////////////////
function html_import_form($action, $import, $import_q, $dsrc_q, $users) {
  global $cgp_user, $display, $c_undef, $l_undef;
  global $l_insert, $l_update, $l_back, $l_header_new;
  global $l_import, $l_name, $l_datasource, $l_market, $l_csv_sep, $l_enclosed;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $l_mapping, $l_field_num, $l_field, $l_default;
  global $l_company, $l_number, $l_address, $l_postcode, $l_town;
  global $l_expresspostal, $l_lang, $l_country, $l_mailing_ok_msg;
  global $l_phone, $l_fax, $l_web, $l_email, $l_mailing_ok, $l_comment;
  global $l_contact, $l_lastname, $l_firstname, $l_function, $l_title, $l_hphone, $l_mphone;

  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $import_q->f('import_id');
    $usercreate = $import_q->f('list_usercreate');
    $name = $import_q->f('import_name');
    $dsrc = $import_q->f('import_datasource_id');
    $market = $import_q->f('import_marketingmanager_id');
    $sep = $import_q->f('import_separator');
    $enc = $import_q->f('import_enclosed');
    $desc = $import_q->f('import_desc');
    $dis_title = $name;
  } else if ($action == 'new') {
    $dis_title = $l_header_new;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($import['import_id'])) { $id = $import['import_id']; }
  if (isset($import['name'])) { $name = stripslashes($import['name']); }
  if (isset($import['datasource'])) { $dsrc = $import['datasource']; }
  if (isset($import['market'])) { $market = $import['market']; }
  if (isset($import['sep'])) { $sep = $import['sep']; }
  if (isset($import['enclosed'])) { $enc = $import['enclosed']; }

  eval ($desc);
  $enc = htmlspecialchars(trim($enc));

  // data source select
  $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f('datasource_id');
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f('datasource_name') . "</option>\n";
  }
  $sel_dsrc .= '</select>';

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($users)) {
    foreach($users as $u_id => $one_user) {
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $market) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= '>'. $one_user['lastname'].' '.$one_user['firstname'] . "</option>\n";
    }
  }
  $sel_market .= '</select>';

  if ($sep == $ccsvd_tab) $csvd_tab = 'checked';
  else $csvd_sc = 'checked';

  // User data categories handling
  if (is_array($cgp_user['company']['category'])) {
    foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
      $field = "comp_${cat_name}";
      $value = $$field;
      $field_d = "comp_${cat_name}_d";
      $value_d = $$field_d;
      global ${"l_$cat_name"};
      $cat_label = ${"l_$cat_name"};

      $dis_userdata_company .= "
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_$field\" value=\"$value\" /></td>
    <td>$cat_label</td>
    <td>
      <input type=\"text\" name=\"tf_${field}_d\" value=\"$value_d\" /></td>
  </tr>";
    }
  }

  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      $field = "con_${cat_name}";
      $value = $$field;
      $field_d = "con_${cat_name}_d";
      $value_d = $$field_d;
      global ${"l_$cat_name"};
      $cat_label = ${"l_$cat_name"};

      $dis_userdata_contact .= "
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_$field\" value=\"$value\" /></td>
    <td>$cat_label</td>
    <td>
      <input type=\"text\" name=\"tf_${field}_d\" value=\"$value_d\" /></td>
  </tr>";
    }
  }

  $url = url_prepare('import_index.php');
  $display['title'] = $dis_title;

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"$url\"
    onSubmit=\"if (check_import_form(this)) return true; else return false;\">
  
  <fieldset class=\"detail infos\">
  <legend>$l_import</legend>
  <table>
  <tr>
    <th>$l_name</th>
    <td>
      <input type=\"text\" name=\"tf_name\" value=\"$name\" /></td>
  </tr>
  <tr>
    <th>$l_datasource</th>
    <td>$sel_dsrc</td>
  </tr>
  <tr>
    <th>$l_market</th>
    <td>$sel_market</td>
  </tr>
  <tr>
    <th>$l_csv_sep</th>
    <td>
      <label><input type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc</label>
      <label><input type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab</label>
    </td>
  </tr><tr>
    <th>$l_enclosed</th>
    <td>
      <input type=\"text\" name=\"tf_enclosed\" size=\"1\" maxlength=\"1\" value=\"$enc\" /></td>
  </tr>
  </table>
  </fieldset>


  <fieldset class=\"detail infos\">
  <legend>$l_mapping</legend>
  <table>
  <tr>
    <td>$l_field_num</td>
    <td>$l_field</td>
    <td>$l_default</td>
  </tr>

  <tr>
    <td colspan=\"3\"><h1 class=\"title\">$l_company</h1></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_name\" value=\"$comp_name\" /></td>
    <td>$l_name</td>
    <td>
      <input type=\"text\" name=\"tf_comp_name_d\" value=\"$comp_name_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_num\" value=\"$comp_num\" /></td>
    <td>$l_number</td>
    <td>
      <input type=\"text\" name=\"tf_comp_num_d\" value=\"$comp_num_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad1\" value=\"$comp_ad1\" /></td>
    <td>$l_address 1</td>
    <td>
      <input type=\"text\" name=\"tf_comp_ad1_d\" value=\"$comp_ad1_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad2\" value=\"$comp_ad2\" /></td>
    <td>$l_address 2</td>
    <td>
      <input type=\"text\" name=\"tf_comp_ad2_d\" value=\"$comp_ad2_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_ad3\" value=\"$comp_ad3\" /></td>
    <td>$l_address 3</td>
    <td>
      <input type=\"text\" name=\"tf_comp_ad3_d\" value=\"$comp_ad3_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_zip\" value=\"$comp_zip\" /></td>
    <td>$l_postcode</td>
    <td>
      <input type=\"text\" name=\"tf_comp_zip_d\" value=\"$comp_zip_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_town\" value=\"$comp_town\" /></td>
    <td>$l_town</td>
    <td>
      <input type=\"text\" name=\"tf_comp_town_d\" value=\"$comp_town_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_cdx\" value=\"$comp_cdx\" /></td>
    <td>$l_expresspostal</td>
    <td>
      <input type=\"text\" name=\"tf_comp_cdx_d\" value=\"$comp_cdx_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_ctry\" value=\"$comp_ctry\" /></td>
    <td>$l_country</td>
    <td>
      <input type=\"text\" name=\"tf_comp_ctry_d\" value=\"$comp_ctry_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_pho\" value=\"$comp_pho\" /></td>
    <td>$l_phone</td>
    <td>
      <input type=\"text\" name=\"tf_comp_pho_d\" value=\"$comp_pho_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_fax\" value=\"$comp_fax\" /></td>
    <td>$l_fax</td>
    <td>
      <input type=\"text\" name=\"tf_comp_fax_d\" value=\"$comp_fax_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_web\" value=\"$comp_web\" /></td>
    <td>$l_web</td>
    <td>
      <input type=\"text\" name=\"tf_comp_web_d\" value=\"$comp_web_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_mail\" value=\"$comp_mail\" /></td>
    <td>$l_email</td>
    <td>
      <input type=\"text\" name=\"tf_comp_mail_d\" value=\"$comp_mail_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_comp_com\" value=\"$comp_com\" /></td>
    <td>$l_comment</td>
    <td>
      <input type=\"text\" name=\"tf_comp_com_d\" value=\"$comp_com_d\" /></td>
  </tr>
  $dis_userdata_company

  <tr>
    <td colspan=\"3\"><h1 class=\"title\">$l_contact</h1></td>
  </tr>

  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_ln\" value=\"$con_ln\" /></td>
    <td>$l_lastname</td>
    <td>
      <input type=\"text\" name=\"tf_con_ln_d\" value=\"$con_ln_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_fn\" value=\"$con_fn\" /></td>
    <td>$l_firstname</td>
    <td>
      <input type=\"text\" name=\"tf_con_fn_d\" value=\"$con_fn_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_lang\" value=\"$con_lang\" /></td>
    <td>$l_lang</td>
    <td>
      <input type=\"text\" name=\"tf_con_lang_d\" value=\"$con_lang_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_func\" value=\"$con_func\" /></td>
    <td>$l_function</td>
    <td>
      <input type=\"text\" name=\"tf_con_func_d\" value=\"$con_func_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_tit\" value=\"$con_tit\" /></td>
    <td>$l_title</td>
    <td>
      <input type=\"text\" name=\"tf_con_tit_d\" value=\"$con_tit_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_ad1\" value=\"$con_ad1\" /></td>
    <td>$l_address 1</td>
    <td>
      <input type=\"text\" name=\"tf_con_ad1_d\" value=\"$con_ad1_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_ad2\" value=\"$con_ad2\" /></td>
    <td>$l_address 2</td>
    <td>
      <input type=\"text\" name=\"tf_con_ad2_d\" value=\"$con_ad2_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_ad3\" value=\"$con_ad3\" /></td>
    <td>$l_address 3</td>
    <td>
      <input type=\"text\" name=\"tf_con_ad3_d\" value=\"$con_ad3_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_zip\" value=\"$con_zip\" /></td>
    <td>$l_postcode</td>
    <td>
      <input type=\"text\" name=\"tf_con_zip_d\" value=\"$con_zip_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_town\" value=\"$con_town\" /></td>
    <td>$l_town</td>
    <td>
      <input type=\"text\" name=\"tf_con_town_d\" value=\"$con_town_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_cdx\" value=\"$con_cdx\" /></td>
    <td>$l_expresspostal</td>
    <td>
      <input type=\"text\" name=\"tf_con_cdx_d\" value=\"$con_cdx_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_ctry\" value=\"$con_ctry\" /></td>
    <td>$l_country</td>
    <td>
      <input type=\"text\" name=\"tf_con_ctry_d\" value=\"$con_ctry_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_pho\" value=\"$con_pho\" /></td>
    <td>$l_phone</td>
    <td>
      <input type=\"text\" name=\"tf_con_pho_d\" value=\"$con_pho_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_hpho\" value=\"$con_hpho\" /></td>
    <td>$l_hphone</td>
    <td>
      <input type=\"text\" name=\"tf_con_hpho_d\" value=\"$con_hpho_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_mpho\" value=\"$con_mpho\" /></td>
    <td>$l_mphone</td>
    <td>
      <input type=\"text\" name=\"tf_con_mpho_d\" value=\"$con_mpho_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_fax\" value=\"$con_fax\" /></td>
    <td>$l_fax</td>
    <td>
      <input type=\"text\" name=\"tf_con_fax_d\" value=\"$con_fax_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_mail\" value=\"$con_mail\" /></td>
    <td>$l_email</td>
    <td>
      <input type=\"text\" name=\"tf_con_mail_d\" value=\"$con_mail_d\" /></td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_mailok\" value=\"$con_mailok\" /></td>
    <td>$l_mailing_ok</td>
    <td>
      <input type=\"text\" name=\"tf_con_mailok_d\" maxlength=\"1\" value=\"$con_mailok_d\" /> $l_mailing_ok_msg</td>
  </tr>
  <tr>
    <td>
      <input type=\"text\" size=\"3\" name=\"tf_con_com\" value=\"$con_com\" /></td>
    <td>$l_comment</td>
    <td>
      <input type=\"text\" name=\"tf_con_com_d\" value=\"$con_com_d\" /></td>
  </tr>
  $dis_userdata_contact
  </table>
  </fieldset>
";

  if (($action == 'detailupdate') || ($action == 'update')) {
     $dis_but .= "
      <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

       $dis_back = "<form method=\"get\" name=\"f_back\"
        action=\"".url_prepare('import_index.php')."\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"import_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>";
   } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<fieldset class=\"buttons\">
    $dis_but
    </fieldset>
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
  global $display, $l_warn_delete, $l_delete, $l_back,$path;

  $display['msg'] .= display_warn_msg("$l_warn_delete");

  $block .= "
    <div class=\"buttons\">
    <a href=\"$path/import/import_index.php?action=delete&amp;import_id=$id\">
    $l_delete 
    </a>
    <a href=\"$path/import/import_index.php?action=detailconsult&amp;import_id=$id\">$l_back</a>
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
  global $display,$path,$c_yes, $c_no;

  $name = $import['name'];
  $dsrc = $import['datasource'];

  $display['msg'] .= display_warn_msg($l_check_sameimport);
  while ($import_q->next_record()) {
    $id = $import_q->f('import_id');
    $samename = $import_q->f('import_name');
    $samedsrc = $import_q->f('datasource_name');
    $dis_same_import .= "
      <tr><th>
        <a class=\"detail\" href=\"" .url_prepare("import_index.php?action=detailconsult&amp;import_id=$id") . "\">$samename ($samedsrc)</a>
      </th></tr>";
  }

  $block = "
  <table>
  $dis_same_import
  </table>
  <div class=\"buttons\">
 <a href=\"$path/import/import_index.php?action=insert&amp;hd_confirm=$c_yes&amp;tf_name=$name&amp;sel_datasource=$dsrc\">
    $l_confirm 
 </a>
 <a href=\"$path/import/import_index.php?action=new&amp;tf_name=$name&amp;sel_datasource=$dsrc\">
    $l_back
 </a>
  </div>";

  return $block;
}


?>
