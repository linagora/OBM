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
// OBM - File : document_display.inc                                         //
//     - Desc : Document Display File                                        //
//  2003-08-21 Mehdi Rande                                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["document_title"] = $l_title;
$fieldnames["document_name"] = $l_name;
$fieldnames["document_author"] = $l_author;
$fieldnames["document_size"] = $l_size;
$fieldnames["timecreate"] = $l_createtime;
$fieldnames["timeupdate"] = $l_updatetime;
// Calculated or indirect fields
$fieldnames["documentmimetype_label"] = $l_mimetype;


///////////////////////////////////////////////////////////////////////////////
// Display user quota message
///////////////////////////////////////////////////////////////////////////////
function dis_document_quota() {
  global $obm, $c_quota_private_document, $l_quota_msg;
  
  if ($c_quota_private_document == 0) return;
  
  $user_quota = run_query_document_user_quota($obm['uid']);
  $percent = round(($user_quota * 100) / $c_quota_private_document);
  $msg = sprintf($l_quota_msg, dis_filesize($user_quota), $percent.'%', dis_filesize($c_quota_private_document));
  if ($percent > 80) {
    return display_warn_msg($msg);
  }
  return display_ok_msg($msg);
}

///////////////////////////////////////////////////////////////////////////////
// Process a user-friendly file size
///////////////////////////////////////////////////////////////////////////////
function dis_filesize($filesize) {
    global $l_filesize_units;
    
    $decr = 1024; 
    $step = 0;
    while (($filesize / $decr) > 0.9) {
        $filesize = $filesize / $decr;
        $step++;
    } 
    return round($filesize, 2).' '.$l_filesize_units[$step];
}

function upload_max_filesize_to_bytes($upload_max_filesize) {
	switch(strtolower(substr ($upload_max_filesize, -1))) {
		case 'g':
			return (int)$upload_max_filesize*1024*1024*1024;
		case 'm':
			return (int)$upload_max_filesize*1024*1024;
		case 'k':
			return (int)$upload_max_filesize*1024;
		default:
			return $upload_max_filesize;
	}
}

///////////////////////////////////////////////////////////////////////////////
// Display Document specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_document(&$OD, $fieldname, $link_ok) {
  global $path, $params;

  if (($fieldname == "document_title") && $link_ok) {
    $res["url"] = "$path/document/document_index.php?action=detailconsult&amp;document_id=".$OD->data_set->f("document_id");
  }
  
  if (($fieldname == 'data_element') && $params['ext_element'] != '') {
    $res['name'] = "<span id=\"data-document-".$OD->data_set->f('document_id')."\" style=\"display:none;\">".
    $OD->data_set->f('document_name').'</span>';
  }

  return $res;
}

///////////////////////////////////////////////////////////////////////////////
// Display Document search form
// Parameters:
//   - $document[] : hash with document values
///////////////////////////////////////////////////////////////////////////////
function dis_document_search_form($document="") {

  $mime_q = run_query_document_mime();

  $block .= html_document_search_form($document, $mime_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Document search Form
// Parameters:
//   - $document : default form values
//   - $mime_q   : mime type list
///////////////////////////////////////////////////////////////////////////////
function html_document_search_form ($document, $mime_q) {
  global $display, $l_title, $l_mimetype, $l_name, $l_private;
  global $c_all, $l_all, $l_find, $l_author, $l_document_add; 

  $title = stripslashes($document["title"]);
  $author = stripslashes($document["author"]);
  $mime = $document["mime"];
  $name = stripslashes($document["name"]);
  $popup = $document["popup"];
  if ($document["private"] !== 0) {
    $private = 'checked="checked"';
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search("document", $document);

  if ($popup) {
    $ext_action = $document["ext_action"];
    $ext_title = ($document["ext_title"] ? $document["ext_title"] : $l_document_add);
    $ext_url = $document["ext_url"];
    $ext_id = $document["ext_id"];
    $ext_target = $document["ext_target"];
    $ext_element =  $document["ext_element"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
            <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">";
    $display["title"] = $ext_title;
  }
  
  // Mime-type select
  $sel_mime = "<select id=\"sel_mime\" name=\"sel_mime\">
    <option value=\"$c_all\">$l_all</option>";
  while($mime_q->next_record()) {
    $type_id = $mime_q->f("documentmimetype_id");
    $sel_mime .= "\n<option value=\"$type_id\"";
    if ($type_id == $mime) { $sel_mime .= " selected=\"selected\""; }
    $sel_mime .= ">".$mime_q->f("documentmimetype_label")."</option>\n";
  }
  $sel_mime .= "</select>";

  $url = url_prepare("document_index.php");

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" class=\"search\" name=\"f_search\" id=\"f_search\" action=\"$url\">
      <label>$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"16\" value=\"$title\" />
    </label>
    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"16\" value=\"$name\" />
    </label>
    $block_userdata
    <label>$l_author<br />
      <input type=\"text\" name=\"tf_author\" id=\"tf_author\" size=\"8\" value=\"$author\" />
    </label>
    <label>$l_mimetype<br />
      $sel_mime
    </label>
    <label>
      &nbsp;<br />
      <input type=\"hidden\" name=\"cba_private\" value=\"0\" />
      <input type=\"checkbox\" name=\"cba_private\" value=\"1\" $private/>
      $l_private 
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp; 
    </label>
    <p class=\"CL\"></p>
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Document search result
// Parameters:
//   - $document[] : document search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_document_search_list($document) {
  global $obm, $display, $l_found, $l_no_found;

  $prefs = get_display_pref($obm["uid"], "document");
  $obm_q = run_query_document_search($document);
  $nb_document = $obm_q->num_rows_total();
  if ($nb_document == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_document $l_found");
    $block = html_document_search_list($obm_q, $prefs, $document);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $doc_q     : list of documents
//   - $prefs     : the fields which have to be displayed
//   - $document[]  : document search criteria
//     keys used   : archive, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_document_search_list($doc_q, $prefs, $document) {
  global $l_add, $l_close;

  // we urlencode to avoid breakage for space char
  $title = stripslashes($document["title"]);
  $author = stripslashes($document["author"]);
  $mime = $document["mime"];
  $name = stripslashes($document["name"]);
  $entity = $document["entity"];
  $entity_id = $document["entity_id"];
  $popup = $document["popup"];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params("document", $document);
  
 if ($popup) {
    $ext_action = $document["ext_action"];
    $ext_url = $document["ext_url"];
    $ext_id = $document["ext_id"];
    $ext_target = $document["ext_target"];
    $ext_element = $document["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }
 if ($entity != "document") {
   $url_ent = "&amp;entity=$entity&amp;entity_id=$entity_id";
 }
   
  $url = url_prepare("document_index.php?action=search$url_userdata&amp;tf_title=$title&tf_author=$author&tf_name=$name&sel_mime=$mime$url_ent$url_ext");

  $doc_d = new OBM_DISPLAY("DATA", $prefs, "document");

  if ($popup) {
    $doc_d->display_link = false;
    $doc_d->data_cb_text = "X";
    $doc_d->data_idfield = "document_id";
    
    if ($ext_element != '') {
      $doc_d->data_cb_name = '';
      $doc_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\" > ";
    } else {
      $doc_d->data_cb_name = "cb_d";
      $doc_d->data_form_head = "
        <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }
    
    $doc_d->data_form_end = "
      <fieldset class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </fieldset>
      </form>";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    }
    
  $doc_d->data_set = $doc_q;
  $doc_d->data_url = $url;
  $doc_d->data_header = "both";
  $block .= $doc_d->display("dis_data_document");
  $block .= $display_popup_end;

  return $block;
}

/**
 * Build a JSON array with all search results 
 * 
 * @param $params POST params
 * @param $results array of 'length' => DB query results nb, and 'datas' => DB Document search query results
 * @access public
 * @return void
 */
function json_search_document($params, $results) {
  global $display, $l_title;

  $doc_q = $results['datas'];

  $docs = array();
  while ($doc_q->next_record()) {
    $id = $doc_q->f('document_id');
    $label = phpStringToJsString($doc_q->f('document_name'));
    $extra = phpStringToJsString("$l_title : ".$doc_q->f('document_title'));
    $docs[] = "{id:'data-document-$id', label:'$label', extra:'$extra'}";
  }
  $display['json'] = '{length:'.$results['length'].', datas:['.implode(',',$docs).']}';
}


///////////////////////////////////////////////////////////////////////////////
// Display Document Detail
// Parameters:
//   - $action     : action called
//   - $document[] : hash values
//   - $doc_q      : document database result 
///////////////////////////////////////////////////////////////////////////////
function dis_document_consult($document) {
  global $display, $l_err_reference;

  if ($document["document_id"] > 0) {
    $doc_q = run_query_document_detail($document["document_id"]);
    if ($doc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($doc_q);
      $block = html_document_consult($doc_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }  
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Document Consultation
// Parameters:
//   - $co_q   : document database result 
///////////////////////////////////////////////////////////////////////////////
function html_document_consult($doc_q) {
  global $display, $path ;
  global $l_document, $l_size, $l_bytes, $l_title, $l_mimetype, $l_private;
  global $l_author, $l_path, $l_name; 
  global $l_file, $l_consult, $l_insert, $l_update, $l_checkdelete;
  global $l_yes, $l_no;

  $id = $doc_q->f("document_id");
  $title = $doc_q->f("document_title");
  $author = $doc_q->f("document_author");
  $privacy = ($doc_q->f("document_privacy") == 1 ? $l_yes : $l_no );
  $mimetype = $doc_q->f("documentmimetype_label");
  $docpath = $doc_q->f("document_path");
  $size = $doc_q->f("document_size");
  $name = $doc_q->f("document_name");

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult("document", $id);

  $url = "$path/document/document_index.php?action=accessfile&amp;document_id=$id&amp;popup=1";

  $dname =  ($name != '') ? "($name)" : ''; 
  $display["title"] = "$title $dname";

  // --- HTML Template --------------------------------------------------------

  $block = "
   <div class=\"detail infos\">
   <h1>$l_document</h1>
   <table>
   <tr>
     <th>$l_title</th>
     <td>$title</td>
   </tr>
   <tr>
     <th>$l_name</th>
     <td>$name</td>
   </tr>
   <tr>
     <th>$l_author</th>
     <td>$author</td>
   </tr>
   <tr>
     <th>$l_private</th>
     <td>$privacy</td>
   </tr>
   </table>
   </div>

   $block_userdata
   <div class=\"detail infos\"> 
   <h1>$l_file</h1>

   <table>
   <tr>
     <th>$l_mimetype</th>
     <td>$mimetype</td>
   </tr><tr>
     <th>$l_path</th>
     <td>$docpath</td>
   </tr>    
   <tr>
     <th>$l_size</th>
     <td>$size $l_bytes</td>
    </tr>    
   </table>
   </div>
   
   <div class=\"detail extra\">
   <h1>
   <a target=\"_blank\" href=\"$url\">
    $l_consult
   </a>
   </h1>
   </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Document Form
// Parameters:
//   - $action     : action called
//   - $document[] : hash values
//   - $doc_q      : document database result 
///////////////////////////////////////////////////////////////////////////////
function dis_document_form($action, $document, $doc_q) {

  $mime_q = run_query_document_mime();

  $block = html_document_form($action, $document, $doc_q, $mime_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Document Form
// Parameters:
//   - $action     : action called
//   - $document[] : Document hash values
//   - $doc_q      : document database result 
//   - $mime_q     : Mime types database result 
///////////////////////////////////////////////////////////////////////////////
function html_document_form($action, $document, $doc_q, $mime_q) {
  global $obm, $display, $path, $default_path;
  global $l_document,$l_new_file,$l_link,$l_noupdate_file,$l_update_file;
  global $l_title, $l_mimetype, $l_private, $ico_browse, $l_url, $l_links;
  global $l_name, $l_author, $l_path, $l_kind, $l_browse, $l_header_new;
  global $l_file,$l_insert,$l_update,$l_checkdelete,$l_auto,$c_auto, $l_file_maxsize;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $doc_q->f("document_id");
    $title = $doc_q->f("document_title");
    $name = $doc_q->f("document_name");
    $kind = $doc_q->f("document_kind");
    $author = $doc_q->f("document_author");
    $privacy = ($doc_q->f("document_privacy") == 1 ? " checked" : "");
    $mime = $doc_q->f("document_mimetype_id");
    $dis_title = $title;
    if ($kind == $cdoc_kind_link) {
      $fi_path = $doc_q->f("document_path");
      $url_parse = parse_url($name);
      $proto = $url_parse["scheme"];
      $url = $url_parse["host"].$url_parse["path"];
      if (strrpos($fi_path,'/') != strlen($fi_path) -1)  {
	$fi_path = $doc_q->f("document_path").'/';
      }
    } else {
      $kind = $cdoc_kind_file;
      $fi_path = $doc_q->f("document_path");
    }
    $update_file = "
    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>
    <tr>
      <th colspan=\"2\">
       <input checked=\"checked\" onclick=\"basic_hide('file_update')\" type=\"radio\" name=\"rd_file_update\" id=\"no\" value=\"no\"/><label for=\"no\">$l_noupdate_file</label>
       <input onclick=\"basic_show('file_update')\" type=\"radio\" name=\"rd_file_update\" id=\"yes\" value=\"yes\"/><label for=\"yes\">$l_update_file</label>
      </th>
    </tr>
    </table>
    <div id=\"file_update\" style=\"display: none\">";
    $update_file_end = "</div>";
  } elseif($action == "new") {
    $dis_title = $l_header_new;
    $usr_q = get_user_info($obm["uid"]);
    $author = $usr_q["lastname"]." ".$usr_q["firstname"];
  }

  // If parameters have been given, they supercede the default action value
  if (isset($document["document_id"])) { $id = $document["document_id"]; }
  if (isset($document["title"])) { $title = stripslashes($document["title"]); }
  if (isset($document["privacy"])) { $privacy = ($document["privacy"] == 1 ? "checked=\"checked\"" : ""); }
  if (isset($document["author"])) { $author = stripslashes($document["author"]); }
  if (isset($document["mime"])) { $mime = $document["mime"]; }
  if (isset($document["path"])) { $fi_path = stripslashes($document["path"]); }
  if (isset($document["file"])) { $file = stripslashes($document["file"]); } 
  if (isset($document["kind"])) { $kind = $document["kind"]; }
  if (isset($document["entity"])) { $entity = $document["entity"]; }
  if (isset($document["entity_id"])) { $entity_id = $document["entity_id"]; }
  if (isset($document["url"])) {
    $url = stripslashes($document["url"]); 
    $url_parse = parse_url($url);
    $proto = $url_parse["scheme"];
    $url = $url_parse["host"].$url_parse["path"];
  }

  if ($fi_path == "") $fi_path = $default_path;

  // User defined data
  $block_userdata = of_userdata_dis_entity_form("document", $id, $document);

  // Mime-type select
  $sel_mime = "<select id=\"sel_mime\" name=\"sel_mime\">";
  $sel_mime .= "<option value=\"$c_auto\">$l_auto</option>";
  while($mime_q->next_record()) {
    $type_id = $mime_q->f("documentmimetype_id");
    $sel_mime .= "\n<option value=\"$type_id\"";
    if ($type_id == $mime) { $sel_mime .= " selected=\"selected\""; }
    $sel_mime .= ">".$mime_q->f("documentmimetype_label")."</option>\n";
  }
  $sel_mime .= "</select>";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"document_id\" id=\"document_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  if (($kind == $cdoc_kind_file) || ($kind == "")) { 
    $checked = "checked=\"checked\"";
    $dis_img2 = " style=\"display: none;\" ";
    $dis_url = " style=\"display: none;\" ";
  } else { 
    $checked = "";
  }

  $rd_kind .= "<input onclick=\"show_hide_field()\" type=\"radio\" name=\"rd_kind\" id=\"$cdoc_kind_file\" value=\"$cdoc_kind_file\" $checked /><label for=\"$cdoc_kind_file\">$l_new_file</label> &nbsp;";

  if (($kind == $cdoc_kind_link) && ($proto == "http")) {
    $dis_field = " style=\"display: none;\" ";
    $dis_img2 = " style=\"display: none;\" ";
    $checked = "checked=\"checked\"";
    $tp = "http://";
  } else { 
    $checked = "";
  }

   $rd_kind .= "<input onclick=\"show_hide_field()\" type=\"radio\" name=\"rd_kind\" id=\"$cdoc_kind_link\" value=\"$cdoc_kind_link\" $checked /><label for=\"$cdoc_kind_link\">$l_link</label> &nbsp;";

  if (($kind == $cdoc_kind_link) && ($proto == "https")) {
    $dis_field = " style=\"display: none;\" ";
    $dis_img2 = " style=\"display: none;\" ";
    $checked = "checked=\"checked\"";
    $tp = "https://";
  } else { 
    $checked = "";
  }

  $rd_kind .= "<input onclick=\"show_hide_field()\"type=\"radio\" name=\"rd_kind\" id=\"https\" value=\"$cdoc_kind_link\" $checked/><label for=\"https\">$l_links</label>";

  $dname =  ($name != '') ? "($name)" : ''; 
  $display["title"] = "$dis_title $dname";
  $url1 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=false"; 
  $url2 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=true"; 

  // Private block
  $block_private = "<input type=\"checkbox\" id=\"cba_privacy\" name=\"cba_privacy\" value=\"1\" $privacy />";

  // --- HTML Template --------------------------------------------------------
  $max_filesize = ini_get("upload_max_filesize");
  $block = "
  <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_entity\"
    onsubmit=\"if (check_document(this)){this.tf_url.value= this.preurl.value + this.tf_url.value; return true;} else return false;\"
    action=\"document_index.php\">
   
  <fieldset class=\"detail infos\">
    <legend>$l_document</legend>

    <table>
    <tr>
      <th>$l_title</th>
      <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"30\" value=\"$title\" /></td>
    </tr><tr>
      <th>$l_author</th>
      <td><input type=\"text\" id=\"tf_author\" name=\"tf_author\" value=\"$author\" size=\"50\"/ /></td>
    </tr><tr>
      <th><label for=\"cba_privacy\">$l_private</label></th>
      <td>$block_private</td> 
    </tr>
    $block_userdata
    </table>
  </fieldset>

  <fieldset class=\"detail infos\">
    <legend>$l_file</legend>
    $update_file

    <table>
    <tr>
      <th>$l_mimetype</th>
      <td>$sel_mime</td>
    </tr>
    <tr>
      <th>$l_kind</th>
      <td>$rd_kind</td>
    </tr> 
    <tr>
      <th>$l_path</th>
      <td>
      <input type=\"text\" id=\"tf_path\" name=\"tf_path\" size=\"30\" value=\"$fi_path\" />
      <span id=\"browse_img\" $dis_img>
      <input type=\"button\" value=\"$l_browse\" onclick=\"popup('$url1',''); return false;\">
      </span>
      </td>
    </tr> 
    <tr id=\"browse_field\" $dis_field>
      <th>$l_file ($l_file_maxsize $max_filesize)</th>
      <td><input type=\"file\" id=\"fi_file\" name=\"fi_file\" size=\"30\" value=\"$file\" /></td>
    </tr>
    <tr id=\"url_field\" $dis_url>
      <th>$l_url</th>
      <td><input type=\"text\" disabled=\"true\" name=\"preurl\" value=\"$tp\" size=\"5\" /><input type=\"text\" id=\"tf_url\" name=\"tf_url\" size=\"50\" value=\"$url\" /></td>
    </tr> 
    </table>
    $update_file_end
  </fieldset>
  <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"3000000\" />
    <input type=\"hidden\" name=\"upload\" value=\"yes\" />
    $dis_button
    <input type=\"hidden\" name=\"entity_id\" value=\"$entity_id\" />
    <input type=\"hidden\" name=\"entity\" value=\"$entity\" />
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Directory Form
// Parameters:
//   - $document[] : default values
//     keys used  : uri, name
///////////////////////////////////////////////////////////////////////////////
function html_document_dir_form($action, $document,$doc_q=null) {
  global $display, $l_document;
  global $l_path, $l_name,$path,$l_browse;
  global $l_dir, $l_add_dir,$l_update_dir;

  if ($action == "detailupdate_dir") {
    $old_name = $doc_q->f("document_name");
    $name = $doc_q->f("document_name");
    $re_path = $doc_q->f("document_path");
  }
  $id = $document["document_id"];
  if (isset($document["name"])) $name = $document["name"];
  if (isset($document["path"])) $re_path = $document["path"];
  if (isset($document["old_name"])) $old_name = $document["old_name"];

  
  $url = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].elements[2]&amp;ext_disp_file=false"; 
  if($action == "new_dir" || $action == "insert_dir") {
    $dis_button = "
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_dir\" />
    <input type=\"submit\" value=\"$l_add_dir\" />
    ";    
    $dis_path = "
     <input type=\"text\" id=\"tf_path\" name=\"tf_path\" value=\"$re_path\" size=\"50\"/>
      <input type=\"button\" value=\"$l_browse\" onclick=\"popup('$url',''); return false;\">
     ";
  } else {
    $dis_button = "
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"update_dir\" />
    <input type=\"submit\" value=\"$l_update_dir\" />
    <input type=\"hidden\" id=\"hd_path\" name=\"hd_path\" value=\"$re_path\" size=\"50\"/>
    <input type=\"hidden\" id=\"hd_old_name\" name=\"hd_old_name\" value=\"$old_name\" size=\"50\"/>
    ";    
    
    $dis_path = $re_path;
  }

  $block = "
    <form method=\"get\" name=\"f_new_dir\"
      onsubmit=\"if (check_dir(this)) return true; else return false;\"
      action=\"".url_prepare("document_index.php")."\">
    
    <fieldset class=\"detail extra\">
    <legend>$l_dir</legend>

    <table>
    <tr>
      <th>$l_name</th>
      <td><input type=\"text\" id=\"tf_name\" name=\"tf_name\" maxlength=\"50\" size=\"30\" value=\"$name\" /></td>
    </tr><tr>
      <th>$l_path</th>
      <td>
      $dis_path
      </td>
    </tr>
    </table>
    </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    <input type=\"hidden\" name=\"document_id\" value=\"$id\" />
    </fieldset>
    </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the document can be deleted, and the form
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_document_can_delete($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back,$path;

  $display["msg"] .= display_ok_msg($l_can_delete);
  
  $block .="
  <div class=\"buttons\">
  <a href=\"$path/document/document_index.php?action=delete&amp;document_id=$p_id\" >$l_delete</a>
  <a href=\"$path/document/document_index.php?action=detailconsult&amp;document_id=$p_id\">$l_back</a>
  </div>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the list of events linked to a document
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_document_force_delete($p_id) {
  global $l_force_delete, $l_back, $path;
  
  $events_q = get_document_linked_events($p_id);
  
  $block = "<div class=\"detail admin\"><table class=\"L\">
    <thead>
      <tr><td>$GLOBALS[l_events]</td></tr>
    </thead>
    <tbody>";
  
  while ($events_q->next_record()) {
    $id = $events_q->f('event_id');
    $title = $events_q->f('event_title');
    $block.= "<tr><td><a href=\"$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=$id\">$title</a></td></tr>";
  }
  
  $block .="</tbody></table></div>
  <div class=\"buttons\">
  <a href=\"$path/document/document_index.php?action=delete&amp;force=true&amp;document_id=$p_id\" >$l_force_delete</a>
  <a href=\"$path/document/document_index.php?action=detailconsult&amp;document_id=$p_id\">$l_back</a>
  </div>";

  return $block;
}



///////////////////////////////////////////////////////////////////////////////
// Display: Display and check the directory links (and delete form if ok)
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_document_can_delete_dir($p_id) {
  global $display,$path,$l_dir_can_delete,$l_dir_delete,$l_back;

  $display["msg"] .= display_ok_msg($l_dir_can_delete);

  $block .="
  <div class=\"buttons\">
    <a href=\"$path/document/document_index.php?action=dir_delete&amp;document_id=$p_id\" >$l_dir_delete</a>
     <a href=\"$path/document/document_index.php?action=tree\">$l_back</a>
   </div>";
	      
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the document administration index
///////////////////////////////////////////////////////////////////////////////
function dis_document_admin_index() {

  $mime_q = run_query_document_mime();
  $block .= html_document_mime_form($mime_q);

  // User defined data
  $block .= of_userdata_dis_admin_form("document");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Mime section
// Parameters:
//   - $mime_q : mimetype list database object
///////////////////////////////////////////////////////////////////////////////
function html_document_mime_form($mime_q) {
  global $l_mime_manage,$l_mime_exist,$l_label,$l_extension,$l_mimetype;
  global $l_mime_checkdelete,$l_mime_update,$l_mime_new,$l_mime_insert,$l_mime_label,$l_new,$l_c_insert;
  global $ico_ok, $ico_delete;

  $char_title = '=';
  $key = 0;
  while ($mime_q->next_record()) {
    $id = $mime_q->f("documentmimetype_id");
    $extension = $mime_q->f("documentmimetype_extension");
    $label = $mime_q->f("documentmimetype_label");
    $type = $mime_q->f("documentmimetype_mime");

    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = "";
    }
    $c_label = htmlentities($one_cat["label"], ENT_COMPAT, 'UTF-8');
    $mime_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"mime_$id\" value=\"$id\" />
    <a href=\"?action=mime_checklink&amp;category=mime&amp;mime=$id\">
    <img src=\"$ico_delete\" alt=\"$l_mime_checkdelete\" title=\"$l_mime_checkdelete\" />
    </a></td>
    <td> <input type=\"text\" id=\"tf_mime_label_$id\" value=\"$label\" size=\"14\"/></td>
    <td> <input type=\"text\" id=\"tf_extension_$id\" value=\"$extension\" size=\"6\"/></td>
    <td> <input type=\"text\" id=\"tf_mimetype_$id\" value=\"$type\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'mime');\">
    <img src=\"$ico_ok\" alt=\"$l_mime_update\" title=\"$l_mime_update\" />
    </a>
    </td>
    </tr>";
  }


  // --- HTML Template --------------------------------------------------------

  $block = "
 <div class=\"detail admin\" >
  <form id=\"form_mime_update\" action=\"?action=mime_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_mime_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_extension\" value=\"\" />
  <input type=\"hidden\" name=\"tf_mimetype\" value=\"\" />
  <input type=\"hidden\" name=\"mime\" value=\"\" />
  </form>    
  <h1>$l_mime_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_mime_label</td>
  <td>$l_extension</td>
  <td>$l_mimetype</td>
  <td>&nbsp;</td>        
  </tr>    
  </thead>
  <tbody>
  $mime_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_mime_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_mime_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_mime_label</td>    
  <td>$l_extension</td>
  <td>$l_mime_new</td>    
  </tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_mime_label\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_extension\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_mimetype\" value=\"\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_c_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"mime_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"mime\" />
  </form>
  </div>  
   
";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: the mime links
// Parameters:
//   - $document : document hash info : keys used : mime
///////////////////////////////////////////////////////////////////////////////
function dis_document_mime_links($document) {
  global $l_mime_link_document, $l_mime_link_document_no;
  global $l_mime_delete, $l_mime_can_delete, $l_mime_cant_delete;
  global $l_back, $display, $path;

  $delete_ok = true;
  $id = $document["mime"];
  $label = get_document_mime_label($id);
  $obm_q = run_query_document_mime_links($id);

  $nb_mime = $obm_q->num_rows();
  if ($nb_mime > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1>$label : $l_mime_link_document ($nb_mime)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f("document_id");
      $dtitle = $obm_q->f("document_title");
      $dis_link .= "<li><a  href=\"" .url_prepare("document_index.php?action=detailconsult&amp;document_id=$did") . "\">$dtitle</a></li>";
    }
    if ($cpt < $nb_mime) {
      $dis_link .= "<li>...</li>";
    }

  } else {
    $dis_link_head = "<h1>$label : $l_mime_link_document_no</h1>";
  }

  $block .= "
    <div class=\"detail\">
    $dis_link_head
    <ul>
    $dis_link
    </ul>
    </div>";

  $dis_back = "<a href=\"$path/document/document_index.php?action=admin\">$l_back</a>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_mime_can_delete);
    $dis_del = "<a href=\"$path/document/document_index.php?action=mime_delete&amp;sel_mime=$id\" >$l_mime_delete</a>";
    $block .= "
      <div class=\"buttons\">
      $dis_del 
      $dis_back
      </div>";
     } else {
     $display["msg"] .= display_warn_msg($l_mime_cant_delete);
     $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Document Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_document_display_pref($prefs) {
  global $l_document_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "document");
  $dis_pref->pref_title = $l_document_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_document_tree($document, $display_file) {
  global $l_close, $path;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  
  $popup = $document["popup"];
  $obm_d = run_query_document_get_tree($display_file);
  if ($popup) {
    $link = "<a class=\"documentDir\" href=\"\" onclick=\"fill_ext_form('/');return false;\">";
    $link_end ="</a>";
  }
  $name = '/';
  $kind = $cdoc_kind_dir;
  $realpath = $path.$name;
  $block.= "
  <div class=\"documentTree\">
  ";
  if ($popup) {
    $fill = " onclick=\"fill_ext_form('$realpath');return false;\"";
  }
  if ($display_file == "true") {
    while($obm_d->next_record()) {
      $id = $obm_d->f("document_id");
      $kind = $obm_d->f("document_kind");
      if ($kind == $cdoc_kind_link) {
	$kind = $cdoc_kind_file;
      }
      $doc_info = array("name" =>$obm_d->f("document_name"),
                      "title" =>$obm_d->f("document_title"),
		      "author" => $obm_d->f("document_author"));
      $tree[$obm_d->f("document_path")][$kind][$id] = $doc_info;
    }
  }
  else {
    while($obm_d->next_record()) {
      $id = $obm_d->f("document_id");
      $kind = $obm_d->f("document_kind");
      if ($kind == $cdoc_kind_dir) {
       	$doc_info = array("name" =>$obm_d->f("document_name"),
                      "title" =>$obm_d->f("document_title"),
		      "author" => $obm_d->f("document_author"));
        $tree[$obm_d->f("document_path")][$id] = $doc_info;
      }
    }
  }
  if ($display_file == "true") {
   $block .= @display_document_tree($tree,'/','/',0,0);
  } else {
    $block .= @display_document_popup_tree($tree,'/','/',0);
  }
$block.= "
  </div>
  ";

  return $block;
}




function display_document_tree($tree,$current_directory,$name,$indent,$d_id) {
  global $ico_file,$ico_folder_close,$ico_folder;
  global $ico_folder_empty,$popup, $action, $path;

  $directories = $tree[$current_directory][0];
  $files = $tree[$current_directory][1];
  $indent_file = $indent + 15;
  foreach($directories as $id => $info) {
    $content.= display_document_tree($tree,$current_directory.$info["name"].'/',$info["name"],$indent_file,$id);
    $bool = true;
  }
  $current_dir_id = str_replace("'","_",$current_directory);
  $current_dir_id = str_replace("\"","-",$current_dir_id);
  foreach($files as $id => $info) {
    $realpath = $current_directory.$info["name"];
    $bool = true;
    $content.= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentFile\" style=\"padding-left:".$indent_file."px;text-align:left;\">";
  if ($popup) {
    if ($action == "ext_get_path"){
      $fill_ext = "ext_get_path('".addslashes(get_document_disk_path($id).$id)."')";
    }
    else if ($action == "ext_get_id_from_path"){
      $fill_ext = "fill_ext_get_id_from_path($id,'".$info["title"]."')";
    }
    $content.= "
     <a class=\"documentFile\" href=\"\" onclick=\"$fill_ext;return false;\" > ".$info["name"]." </a>";
  }
  else {
   $content.= "
	 
	<td class=\"documentFile\" style=\"text-align:left;\">
	 <a  href=\"document_index.php?action=detailconsult&amp;document_id=$id\" >

	  <img src=\"$ico_file\" alt=\"\" />
	 </a>
	 <a class=\"documentFile\" 
	  href=\"document_index.php?action=accessfile&amp;document_id=$id&amp;popup=1\" 
	  target=\"_blank\" >
	  ".$info["name"]."
	 </a>
  ";
  }
  $content.= "
	</td>
	<td class=\"documentInfo\">&nbsp;".$info["title"]."</td>
	<td class=\"documentInfo\">&nbsp;".$info["author"]."</td>
       </tr>
      </table>";
  }
  if($d_id) {
    $dis_name = "
       <a href=\"$path/document/document_index.php?action=detailupdate_dir&amp;document_id=$d_id\"> 
       $name
       </a>        
      ";
  } else {
    $dis_name = $name;
  }  
  if($bool) {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	 <a href=\"\" onclick=\"show_hide_dir('$current_dir_id');return false;\"> 
	  <img id=\"img_$current_dir_id\"  src=\"$ico_folder_close\" alt=\"[Dir]\"/>
         </a>
         $dis_name
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>
      <div id=\"div_$current_dir_id\" style=\"display:none;\" >
       $content
      </div>";

  }
  else {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	 <a href=\"document_index.php?action=dir_check_delete&amp;document_id=$d_id\"> 
	  <img id=\"img_$current_dir_id\"  src=\"$ico_folder_empty\" alt=\"[File]\"/>
	 </a>
	 $dis_name
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>";
  }
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function display_document_popup_tree($tree,$current_directory,$name,$indent) {
  global $ico_file,$ico_folder_close,$ico_folder;
  global $popup;
  
  $indent_file = $indent + 15;
  $directories = $tree[$current_directory];
  foreach($directories as $id => $info) {
    $content.= display_document_popup_tree($tree,$current_directory.$info["name"].'/',$info["name"],$indent_file);
    $bool = true;
  }
  $current_dir_id = str_replace("'","_",$current_directory);
  $current_dir_id = str_replace("\"","-",$current_directory);
  if($bool == true) {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	<a href=\"\" onclick=\"show_hide_dir('$current_dir_id');return false;\"> 
	  <img id=\"img_$current_dir_id\"  src=\"$ico_folder_close\" alt=\"\"/>
	 </a>
	 <a class=\"documentDir\" href=\"\"  onclick=\"fill_ext_form('".addslashes($current_directory)."');return false;\">
	  $name
	 </a>	 
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>
      <div id='div_$current_dir_id' style=\"display:none;\" >
       $content
      </div>";
  }
  else {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	  <img id=\"img_$current_dir_id\"  src=\"$ico_folder\" alt=\"\"/>
	 <a class=\"documentDir\" href=\"\"  onclick=\"fill_ext_form('".addslashes($current_directory)."');return false;\">
	  $name
	 </a>
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>";
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Consult File
///////////////////////////////////////////////////////////////////////////////
function dis_document_file($doc_q) {
  global $cdoc_kind_link, $display,$l_no_document;

  $name = $doc_q->f("document_name");  
  $kind = $doc_q->f("document_kind");

  ob_end_clean();

  // If document is a link we transfer to the link
  if ($kind == $cdoc_kind_link) {
    header("location: $name");
    exit;

  } else {

    $id = $doc_q->f("document_id");
    $mime = $doc_q->f("documentmimetype_mime");  
    $disk_path = get_document_disk_path($id);
    $disk_fullname = $disk_path . $id; 

    // Else Document is a file, we send the file
    
    if(file_exists($disk_fullname))$handle = fopen ($disk_fullname, "r");
    if($handle) {
      header("Content-Type: $mime");
      if (isset($GLOBALS['c_document_display_inline']) && $GLOBALS['c_document_display_inline'] === true) {
        header("Content-Disposition: inline; filename=".addslashes(str_replace(" ", "_", $name)));
      } else {
        header("Content-Disposition: attachment; filename=".addslashes(str_replace(" ", "_", $name)));
      }
      header("Cache-Control: maxage=3600");
      header("Pragma: public");
      
      while ( !feof($handle) ) {
        echo fread($handle, 4096);
      } 
      fclose ($handle); 
      exit();
    } else {
      $display["msg"] .= display_err_msg($l_no_document); 
    }
  }

}

?>
