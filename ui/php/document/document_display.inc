<script language="php">
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
$fieldnames["documentcategory1_label"] = $l_category1;
$fieldnames["documentcategory2_label"] = $l_category2;
$fieldnames["documentmimetype_label"] = $l_mimetype;


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
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  if (($fieldname == "document_title") && $link_ok) {
    $res["url"] = "$path/document/document_index.php?action=detailconsult&amp;param_document=".$OD->data_set->f("document_id");
  }

  return $res;
}

///////////////////////////////////////////////////////////////////////////////
// Display Document search form
// Parameters:
//   - $document[] : hash with document values
///////////////////////////////////////////////////////////////////////////////
function dis_document_search_form($document="") {

  $cats1 = of_category_get_ordered("document", "category1");
  $cats2 = of_category_get_ordered("document", "category2");
  $mime_q = run_query_documentmime();

  $block .= html_document_search_form($document, $cats1, $cats2, $mime_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Document search Form
// Parameters:
//   - $cats1      : array with document category 1 list
//   - $cats2      : array with document category 2 list
//   - $document[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_document_search_form ($document, $cats1, $cats2, $mime_q) {
  global $display, $l_title, $l_mimetype, $l_name;
  global $l_all, $l_find, $l_author, $l_document_add; 
  global $c_all;

  $title = stripslashes($document["title"]);
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
  $author = stripslashes($document["author"]);
  $mime = $document["mime"];
  $name = stripslashes($document["filename"]);
  $popup = $document["popup"];

  if ($popup) {
    $ext_action = $document["ext_action"];
    $ext_title = ($document["ext_title"] ? $document["ext_title"] : $l_document_add);
    $ext_url = $document["ext_url"];
    $ext_id = $document["ext_id"];
    $ext_target = $document["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }
  
  $block_category1 = of_category_dis_search_select("document", "category1", $cats1, $cat1);
  $block_category2 = of_category_dis_search_select("document", "category2", $cats2, $cat2);

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
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"16\" value=\"$title\" />
    </div>
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_filename\" id=\"tf_title\" size=\"16\" value=\"$name\" />
    </div>
    $block_category1 
    $block_category2
    <div class=\"searchLabel\">$l_author<br />
      <input type=\"text\" name=\"tf_author\" id=\"tf_author\" size=\"8\" value=\"$author\" />
    </div>
    <div class=\"searchLabel\">$l_mimetype<br />
      $sel_mime
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp; 
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Document search result
// Parameters:
//   - $document[] : document search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_document_search_list($document) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $prefs = get_display_pref($auth->auth["uid"], "document");
  $obm_q = run_query_search($document, $new_order, $order_dir);
  $nb_document = $obm_q->num_rows_total();
  if ($nb_document == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_document_search_list($obm_q,$prefs,$nb_document,$document);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $doc_q     : list of documents
//   - $prefs     : the fields which have to be displayed
//   - $nb_document : nb documents returned by the search query
//   - $document[]  : document search criteria
//     keys used   : archive, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_document_search_list($doc_q, $prefs, $nb_document,$document) {
  global $display, $l_found, $l_add,$l_close;

  // we urlencode to avoid breakage for space char
  $title = stripslashes($document["title"]);
  $category1 = $document["category1"];
  $category2 = $document["category2"];
  $author = stripslashes($document["author"]);
  $mime = $document["mime"];
  $name = stripslashes($document["name"]);
  $entity =$document["entity"];
  $entity_id = $document["entity_id"];
  $popup = $document["popup"];
  
 if ($popup) {
    $ext_action = $document["ext_action"];
    $ext_url = $document["ext_url"];
    $ext_id = $document["ext_id"];
    $ext_target = $document["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }  
  $url = url_prepare("document_index.php?tf_title=$title&sel_category1=$category1&sel_category2=$category2&tf_author=$author&tf_name=$name&sel_mime=$mime&entity=$entity&amp;param_entity=$entity_id&amp;action=search$url_ext");


  $doc_q->seek($first_row);

  $doc_d = new OBM_DISPLAY("DATA", $prefs, "document");

  if ($popup) {
    $doc_d->display_link = false;
    $doc_d->data_cb_text = "X";
    $doc_d->data_idfield = "document_id";
    $doc_d->data_cb_name = "cb_d";
    $doc_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $doc_d->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>";
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    }
    
  $doc_d->data_set = $doc_q;
  $doc_d->data_url = $url;
  $doc_d->data_header = "both";
  $display["msg"] .= display_info_msg("$nb_document $l_found");
  $block .= $doc_d->display("dis_data_document");
  $block .= $display_popup_end;
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Document Detail
// Parameters:
//   - $action     : action called
//   - $document[] : hash values
//   - $doc_q      : document database result 
///////////////////////////////////////////////////////////////////////////////
function dis_document_consult($document) {
  global $display, $l_no_document;

  if ($document["id"] > 0) {
    $doc_q = run_query_detail($document["id"]);
    if ($doc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($doc_q);
      $block = html_document_consult($doc_q);
    } else {
      $display["msg"] .= display_err_msg("$l_no_document !");
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Document Consultation
// Parameters:
//   - $co_q   : document database result 
///////////////////////////////////////////////////////////////////////////////
function html_document_consult($doc_q) {
  global $display, $set_theme;
  global $l_document, $l_size, $l_bytes, $l_title, $l_mimetype, $l_private;
  global $l_author, $l_path, $l_name; 
  global $l_file, $l_consult, $l_insert, $l_update, $l_checkdelete;
  global $c_yes, $c_no;

  // if update mode and first display values are taken from database
  $id = $doc_q->f("document_id");
  $title = $doc_q->f("document_title");
  $cats1 = of_entitycategories_get($doc_q->f("document_id"), "document", "category1", "mono");
  $cats2 = of_entitycategories_get($doc_q->f("document_id"), "document", "category2", "mono");
  $author = $doc_q->f("document_author");
  $privacy = ($doc_q->f("document_privacy") == 1 ? $c_yes : $c_no );
  $mimetype = $doc_q->f("documentmimetype_label");
  $path = $doc_q->f("document_path");
  $size = $doc_q->f("document_size");
  $name = $doc_q->f("document_name");

  // Category 1 & 2 
  $block_category1 = of_category_dis_block_consult("document", "category1", $cats1, "mono");
  $block_category2 = of_category_dis_block_consult("document", "category2", $cats2, "mono");

  $url = "document_index.php?action=accessfile&amp;tf_path=$path&amp;fi_file_name=$name&amp;popup=1";
  $url = "document_index.php?action=accessfile&amp;param_document=$id&amp;popup=1";

  $display["title"] = "<div class=\"title\">$l_document : $title</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detailHead\">$l_document</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailText\">$title</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_author</td>
      <td class=\"detailText\">$author</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailText\">$privacy</td>
    </tr>
    $block_category1
    $block_category2
    </table>

    <div class=\"detailHead\">$l_file</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_mimetype</td>
      <td class=\"detailText\">$mimetype</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_path</td>
      <td class=\"detailText\">$path</td>
    </tr>    
    <tr>
      <td class=\"detailLabel\">$l_size</td>
      <td class=\"detailText\">$size $l_bytes</td>
     </tr>    
    </table>
    <div class=\"detailHead\">
    <a target=\"_blank\" href=\"$url\">
     $l_consult
    </a>
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

  $cats1 = of_category_get_ordered("document", "category1");
  $cats2 = of_category_get_ordered("document", "category2");
  $mime_q = run_query_documentmime();

  $block = html_document_form($action, $document, $doc_q, $cats1, $cats2, $mime_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Document Form
// Parameters:
//   - $action     : action called
//   - $document[] : Document hash values
//   - $doc_q      : document database result 
//   - $cats1      : Document category1 array
//   - $cats2      : Document category2 array
//   - $mime_q     : Mime types database result 
///////////////////////////////////////////////////////////////////////////////
function html_document_form($action, $document, $doc_q, $cats1, $cats2, $mime_q) {
  global $display, $path, $default_path, $set_theme;
  global $l_document,$l_new_file,$l_link,$l_noupdate_file,$l_update_file;
  global $l_title, $l_mimetype, $l_private, $ico_browse, $l_url, $l_links;
  global $l_name, $l_author, $l_path, $l_kind; 
  global $l_file,$l_insert,$l_update,$l_checkdelete,$l_auto,$c_auto;
  global $popup_width, $popup_height;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $doc_q->f("document_id");
    $title = $doc_q->f("document_title");
    $name = $doc_q->f("document_name");
    $kind = $doc_q->f("document_kind");
    $cat1 = $doc_q->f("document_category1_id");
    $cat2 = $doc_q->f("document_category2_id");
    $author = $doc_q->f("document_author");
    $privacy = ($doc_q->f("document_privacy") == 1 ? " checked" : "");
    $mime = $doc_q->f("document_mimetype_id");
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
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\">$name</td>
    </tr>
    <tr>
      <td class=\"detailLabel\" colspan=\"2\">
       <input checked=\"checked\" onclick=\"basic_hide('file_update')\" type=\"radio\" name=\"rd_file_update\" id=\"no\" value=\"no\"/><label for=\"no\">$l_noupdate_file</label>
       <input onclick=\"basic_show('file_update')\" type=\"radio\" name=\"rd_file_update\" id=\"yes\" value=\"yes\"/><label for=\"yes\">$l_update_file</label>
      </td>
    </tr>
    </table>
    <div id=\"file_update\" style=\"display: none\">";
    $update_file_end = "</div>";
  }
  // If parameters have been given, they supercede the default action value
  if (isset($document["id"])) { $id = $document["id"]; }
  if (isset($document["title"])) { $title = stripslashes($document["title"]); }
  if (isset($document["name"])) { $title = stripslashes($document["name"]); }
  if (isset($document["privacy"])) { $privacy = ($document["privacy"] == 1 ? "checked=\"checked\"" : ""); }
  if (isset($document["category1"])) { $cat1 = $document["category1"]; }
  if (isset($document["category2"])) { $cat2 = $document["category2"]; }
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

  if ($fi_path =="") $fi_path = $default_path;

  $block_category1 = of_category_dis_entity_form("document", "category1", $cats1, "mono", $cat1);
  $block_category2 = of_category_dis_entity_form("document", "category2", $cats2, "mono", $cat2);

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
      <!-- Update button -->
      <input type=\"hidden\" name=\"param_document\" id=\"param_document\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
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

  $dname =  ($name != "") ? "($name)" : ""; 
  $display["title"] = "<div class=\"title\">$l_document : $title $dname</div>";
  $url1 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=false"; 
  $url2 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=true"; 

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_entity\"
      onsubmit=\"if (check_document(this)){this.tf_url.value= this.preurl.value + this.tf_url.value; return true;} else return false;\"
      action=\"document_index.php\">

    <div class=\"detailHead\">$l_document</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"30\" value=\"$title\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_author</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_author\" name=\"tf_author\" value=\"$author\" size=\"50\"/ /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_privacy\" value=\"1\" $privacy /></td> 
    </tr>
    $block_category1
    $block_category2
    </table>

    <div class=\"detailHead\">$l_file</div>
    $update_file

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_mimetype</td>
      <td class=\"detailForm\">$sel_mime</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\">$rd_kind</td>
    </tr> 
    <tr>
      <td class=\"detailLabel\">$l_path</td>
      <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_path\" name=\"tf_path\" size=\"30\" value=\"$fi_path\" />
      <span id=\"browse_img\" $dis_img>
      <a href=\"javascript:return false;\" onclick=\"window.open('$url1','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_browse\" alt=\"\" /></a></span>
      </td>
    </tr> 
    <tr id=\"browse_field\" $dis_field>
      <td class=\"detailLabel\">$l_file</td>
      <td class=\"detailForm\"><input type=\"file\" id=\"fi_file
\" name=\"fi_file\" size=\"30\" value=\"$file\" /></td>
    </tr>
    <tr id=\"url_field\" $dis_url>
      <td class=\"detailLabel\">$l_url</td>
      <td class=\"detailForm\"><input type=\"text\" disabled=\"true\" name=\"preurl\" value=\"$tp\" size=\"5\" /><input type=\"text\" id=\"tf_url\" name=\"tf_url\" size=\"50\" value=\"$url\" /></td>
    </tr> 
   </table>
   </div>
   $update_file_ende
   <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"3000000\">
    <input type=\"hidden\" name=\"upload\" value=\"yes\">
    $dis_button
    <input type=\"hidden\" name=\"param_entity\" value=\"$entity_id\" />
    <input type=\"hidden\" name=\"entity\" value=\"$entity\" />
    </p>
    </div>
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
function html_dir_form($action, $document) {
  global $display,$popup_height,$popup_width;
  global $set_theme,$l_document;
  global $l_path, $l_name,$path,$ico_browse;
  global $l_dir, $l_add_dir;

  $name = $document["name"];
  $re_path = $document["path"];
  $dis_button = "
  <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_dir\" />
  <input type=\"submit\" value=\"$l_add_dir\" />
  ";
  $url = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].elements[1]&amp;ext_disp_file=false"; 

  $block = "
    <form method=\"get\" name=\"f_new_dir\"
      onsubmit=\"if (check_dir(this)) return true; else return false;\"
      action=\"".url_prepare("document_index.php")."\">

    <div class=\"detailHead\">$l_dir</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_name\" name=\"tf_name\" maxlength=\"50\" size=\"30\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_path</td>
      <td class=\"detailForm\">
     <input type=\"text\" id=\"tf_path\" name=\"tf_path\" value=\"$re_path\" size=\"50\"/>
       <a href=\"javascript:return false;\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_browse\" alt=\"\" /></a>
      </td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the document can be deleted, and the form
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_document($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back;

  $url = url_prepare("document_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_document\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_document\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
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
// Display: Display and check the directory links (and delete form if ok)
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_dir($p_id) {
  global $display, $l_dir_can_delete, $l_dir_delete, $l_back;

  $url = url_prepare("document_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"tree\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_dir_can_delete);

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form name=\"form_delete\" method=\"post\" action=\"$url\">
        <input type=\"hidden\" name=\"action\" value=\"dir_delete\" />
        <input type=\"hidden\" name=\"param_document\" value=\"$p_id\" />
        <input type=\"submit\" value=\"$l_dir_delete\" />
        </form>
      </p>
      <p class=\"detailButtons\">$dis_back</p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the document administration index
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $cats1 = of_category_get_ordered("document", "category1");
  $cats2 = of_category_get_ordered("document", "category2");
  $mime_q = run_query_documentmime();

  $block = of_category_dis_admin_form("document", "category1", $cats1);
  $block .= "<hr />";
  $block .= of_category_dis_admin_form("document", "category2", $cats2);
  $block .= "<hr />";
  $block .= html_document_mime_form($mime_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Mime section
// Parameters:
//   - $mime_q : mimetype list database object
///////////////////////////////////////////////////////////////////////////////
function html_document_mime_form($mime_q) {
  global $l_mime_manage,$l_mime_exist,$l_label,$l_extension,$l_mimetype;
  global $l_mime_checkdelete,$l_mime_update,$l_mime_new,$l_mime_insert,$l_mime_label;

  $sel_mime = "<select name=\"sel_mime\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('-');
    ext = mytext.substr(pos + 2);
    mytext = mytext.substr(0, pos-1); 
    pos = mytext.indexOf('(');
    hit = mytext.substr(pos+1);
    pos2 = hit.indexOf(')');
    hit = hit.substr(0,pos2);
    forms.form_mime_update.tf_mime.value=mytext.substr(pos2+3);
    forms.form_mime_update.tf_mimetype.value=ext;
    forms.form_mime_update.tf_extension.value=hit;
    \"
    size=\"4\" >";
  while ($mime_q->next_record()) {
    $id = $mime_q->f("documentmimetype_id");
    $label = $mime_q->f("documentmimetype_label");
    $extends = $mime_q->f("documentmimetype_extension");
    $mime = $mime_q->f("documentmimetype_mime");
    $sel_mime .= "\n<option value=\"$id\">($extends) $label - $mime</option>";
  }
  $sel_mime .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_mime_manage
      </td>
    </tr><tr>
      <td>

<!-- Mime-type Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_mime_delete\" method=\"post\"
              action=\"" . url_prepare("document_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_mime_exist</td>
              <td class=\"adminLabel\">$sel_mime</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"mime_checklink\" />
              <input type=\"submit\" name=\"sub_mime\" value=\"$l_mime_checkdelete\" onclick=\"if (check_mime_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Mime-type Update Section -->
        <tr>
	  <form name=\"form_mime_update\" method=\"get\"
              action=\"" . url_prepare("document_index.php") . "\">	
          <td class=\"adminLabel\">$l_mime_label :</td>
            <td><input type=\"text\" name=\"tf_mime\" /></td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_extension :</td>
            <td><input type=\"text\" name=\"tf_extension\" /></td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_mimetype :</td>
            <td><input type=\"text\" name=\"tf_mimetype\" /></td>
        </tr><tr>	
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <input type=\"hidden\" name=\"sel_mime\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"mime_update\" />
            <input type=\"submit\" name=\"sub_mime\" value=\"$l_mime_update\"
              onclick=\"if (check_mime_upd(this.form,document.form_mime_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Mime-type Section -->

        <form name=\"form_mime_new\" method=\"get\"
          action=\"" . url_prepare("document_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_mime_new :</td>
            <td><input type=\"text\" name=\"tf_mime\" /></td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_extension :</td>
            <td><input type=\"text\" name=\"tf_extension\" /></td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_mimetype :</td>
            <td><input type=\"text\" name=\"tf_mimetype\" /></td>
        </tr><tr>	
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"action\" value=\"mime_insert\" />
            <input type=\"submit\" name=\"sub_mime\" value=\"$l_mime_insert\"
              onclick=\"if (check_mime_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: the mime links
// Parameters:
//   - $document : document hash info : keys used : mime
///////////////////////////////////////////////////////////////////////////////
function dis_mime_links($document) {
  global $l_mime_link_document, $l_mime_link_document_no;
  global $l_mime_delete, $l_mime_can_delete, $l_mime_cant_delete;
  global $l_back,$display;

  $id = $document["mime"];
  $label = get_mime_label($id);
  $obm_q = run_query_mime_links($id);

  $nb_mime = $obm_q->num_rows();
  if ($nb_mime > 0) {
    $display["msg"] .= display_warn_msg($l_mime_cant_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_mime_link_document ($nb_mime)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("document_id");
      $cname = $obm_q->f("document_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("document_index.php?action=detailconsult&amp;param_document=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_mime) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .="
    </table>
      <div class=\"detailButton\">
        <p class=\"detailButtons\">
	  <form name=\"form_back\" method=\"get\"
	    action=\"" .url_prepare("document_index.php") . "\">
	  <input type=\"hidden\" name=\"action\" value=\"admin\" />
	  <input type=\"submit\" value=\"$l_back\" />
	  </form>
	</p>
      </div>";

  } else {
    $display["msg"] .= display_ok_msg($l_mime_can_delete);

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $label : $l_mime_link_document_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">    
      <form name=\"form_act_delete\"
	method=post action=\"" . url_prepare("document_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"mime_delete\">
      <input type=\"hidden\" name=\"sel_mime\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_mime_delete\">
      </form>
      
      <form name=\"form_back\" method=\"get\"
	action=\"" .url_prepare("document_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"admin\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  }

  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Document Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_document_display_pref($prefs) {
  global $l_document_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
  $dis_pref->display_entity = "document"; 
  $dis_pref->pref_title = $l_document_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_documents_tree($document,$display_file) {
  global $l_close;
  global $cdoc_kind_dir, $cdoc_kind_file, $cdoc_kind_link;
  
  $popup = $document["popup"];
  $obm_d = run_query_get_tree($display_file);
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
    $block .= @display_tree($tree,'/','/',0,0);
  } else {
    $block .= @display_popup_tree($tree,'/','/',0);
  }
$block.= "
  </div>
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function display_tree($tree,$current_directory,$name,$indent,$d_id) {
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory,$set_theme;
  global $ico_del_directory,$popup;

  $directories = $tree[$current_directory][0];
  $files = $tree[$current_directory][1];
  $indent_file = $indent + 15;
  foreach($directories as $id => $info) {
    $content.= display_tree($tree,$current_directory.$info["name"].'/',$info["name"],$indent_file,$id);
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
	<td class=\"documentFile\" style=\"padding-left:".$indent_file."px;text-align:left;\">
	 <a  href=\"document_index.php?action=detailconsult&amp;param_document=$id\" >
	  <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_file\" alt=\"\" />
	 </a>
	 <a class=\"documentFile\" 
	  href=\"document_index.php?action=accessfile&amp;param_document=$id&amp;popup=1\" 
	  target=\"_blank\" >
	  ".$info["name"]."
	 </a>
	</td>
	<td class=\"documentInfo\">&nbsp;".$info["title"]."</td>
	<td class=\"documentInfo\">&nbsp;".$info["author"]."</td>
       </tr>
      </table>";
  }
  if($bool) {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	 <a href=\"\" onclick=\"show_hide_dir('$current_dir_id');return false;\"> 
	  <img id=\"img_$current_dir_id\"  src=\"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\" alt=\"[Dir]\"/>
	 </a>
	 ".$name."
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>
      <div id=\"$current_dir_id\" style=\"display:none;\" >
       $content
      </div>";

  }
  else {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	 <a href=\"document_index.php?action=dir_check_delete&amp;param_document=$d_id\"> 
	  <img id=\"img_$current_dir_id\"  src=\"".C_IMAGE_PATH."/$set_theme/$ico_del_directory\" alt=\"[File]\"/>
	 </a>
	 ".$name."
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
function display_popup_tree($tree,$current_directory,$name,$indent) {
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory,$set_theme;
  global $popup;
  $indent_file = $indent + 15;
  $directories = $tree[$current_directory];
  foreach($directories as $id => $info) {
    $content.= display_popup_tree($tree,$current_directory.$info["name"].'/',$info["name"],$indent_file);
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
	  <img id=\"img_$current_dir_id\"  src=\"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\" alt=\"\"/>
	 </a>
	 <a class=\"documentDir\" href=\"\"  onclick=\"fill_ext_form('".addslashes($current_directory)."');return false;\">
	  $name
	 </a>	 
	</td>
	<td class=\"documentInfo\">&nbsp;</td>
	<td class=\"documentInfo\">&nbsp;</td>
       </tr>
      </table>
      <div id='$current_dir_id' style=\"display:none;\" >
       $content
      </div>";
  }
  else {
    $block= 
    "<table class=\"document\">
       <tr>
	<td class=\"documentDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	  <img id=\"img_$current_dir_id\"  src=\"".C_IMAGE_PATH."/$set_theme/$ico_directory\" alt=\"\"/>
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
function dis_file($doc_q) {
  global $cdoc_kind_link;

  $name = $doc_q->f("document_name");  
  $kind = $doc_q->f("document_kind");

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
    $handle = fopen ($disk_fullname, "r");
    header("Content-Type: $mime");
    header("Content-Disposition: inline; filename=$name");
    echo fread($handle, filesize ($disk_fullname));
    fclose ($handle); 
  
  }

}
