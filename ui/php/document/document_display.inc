<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : document_display.inc                                          //
//     - Desc : Document Display File                                         //
//  2003-08-21 Mehdi Rande                                               //
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
// Display Document specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_document(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  if ($fieldname == "document_title") {
    $res["url"] = "$path/document/document_index.php?action=detailconsult&amp;param_document=".$OD->data_set->f("document_id");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Document search Form                                               //
// Parameters:
//   - $cat1_q    : database object with cat1 list
//   - $cat2_q     : database object with cat2ivity list
//   - $document[] : default form values
//     keys used  : archive, name, cat1, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_document_search_form ($cat1_q, $cat2_q,$mime_q, $document) {
  global $l_title, $l_category1, $l_mimetype,$l_name;
  global $l_all, $l_find, $l_author, $l_category2; 
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
    $ext_title = $document["ext_title"];
    $ext_url = $document["ext_url"];
    $ext_id = $document["ext_id"];
    $ext_target = $document["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["msg"] .= display_info_msg($ext_title);
  }
  
  // category 1 select
  $sel_cat1 = "<select id=\"sel_cat1\" name=\"sel_cat1\">
    <option value=\"$c_all\">$l_all</option>";
  while($cat1_q->next_record()) {
    $c1_id = $cat1_q->f("documentcategory1_id");
    $sel_cat1 .= "\n<option value=\"$c1_id\"";
    if ($c1_id == $cat1) { $sel_cat1 .= " selected=\"selected\""; }
    $sel_cat1 .= ">". $cat1_q->f("documentcategory1_label") . "</option>\n";
  }
  $sel_cat1 .= "</select>";

  // category 2 select
  $sel_cat2 = "<select id=\"sel_cat2\" name=\"sel_cat2\">
    <option value=\"$c_all\">$l_all</option>";
  while ($cat2_q->next_record()) {
    $c2_id = $cat2_q->f("documentcategory2_id");
    $sel_cat2 .= "\n<option value=\"$c2_id\"";
    if ($c2_id == $cat2) { $sel_cat2 .= " selected=\"selected\""; }
    $sel_cat2 .= ">". $cat2_q->f("documentcategory2_label") . "</option>\n";
  }
  $sel_cat2 .= "</select>";


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
  <form method=\"get\" name=\"f_document\" id=\"f_document\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"16\" value=\"$title\" />
    </div>
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_filename\" id=\"tf_title\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_category1<br />
      $sel_cat1
    </div>
    <div class=\"searchLabel\">$l_category2<br />
      $sel_cat2
    </div>
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
// Display the Document search result                                         //
// Parameters:
//   - $document[] : document search criteria
//     keys used  : name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_document_search_list($document) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "document");
  $obm_q = run_query_search($document, $new_order, $order_dir);
  $nb_document = $obm_q->num_rows();
  if ($nb_document == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_document_search_list($obm_q,$pref_q,$nb_document,$document);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $doc_q     : list of documents
//   - $pref_q     : the fields which have to be displayed
//   - $nb_document : nb documents returned by the search query
//   - $document[]  : document search criteria
//     keys used   : archive, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_document_search_list($doc_q, $pref_q, $nb_document,$document) {
  global $display, $l_found, $l_add,$l_close;

  // we urlencode to avoid breakage for space char
  $title = stripslashes($document["title"]);
  $cat1 = $document["category1"];
  $cat2 = $document["category2"];
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
  $url = url_prepare("document_index.php?tf_title=$title&sel_cat1=$cat1&sel_cat2=$cat2&tf_author=$author&tf_name=$name&sel_mime=$mime&entity=$entity&amp;param_entity=$entity_id&amp;action=search$url_ext");


  $doc_q->seek($first_row);

  $doc_d = new OBM_DISPLAY("DATA", $pref_q, "document");

  if ($popup) {
    $doc_d->display_link = false;
    $doc_d->data_cb_text = "X";
    $doc_d->data_idfield = "document_id";
    $doc_d->data_cb_name = "cb_d";
    $display_popup_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $display_popup_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    }
    
  $doc_d->data_set = $doc_q;
  $doc_d->data_url = $url;
  $doc_d->data_header = "both";
  $display["msg"] .= display_info_msg("$nb_document $l_found");
  $block = $display_popup_head;
  $block .= $doc_d->display("dis_data_document");
  $block .= $display_popup_end;
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Document Form                                                      //
// Parameters:
//   - $action    : action called
//   - $doc_q      : document database result 
//   - $kind_q    : kind database result 
//   - $document[] : default values
//     keys used  : id, num, archive, name, kind, ad1, ad2, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
//////////////////////////////popup/////////////////////////////////////////////////
function html_document_form($action, $doc_q, $cat1_q, $cat2_q,$mime_q, $document) {
  global $display;
  global $set_theme,$l_document,$l_new_file,$l_link,$l_existing_file,$path,$ico_browse_file;
  global $l_title, $l_category1, $l_mimetype,$l_private,$ico_browse,$set_theme,$l_url;
  global $l_find, $l_author, $l_category2,$l_path,$l_kind,$popup_width,$document_path; 
  global $l_file,$l_insert,$l_update,$l_checkdelete,$popup_height,$l_auto,$c_auto,$default_path;


  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $doc_q->f("document_id");
    $title = $doc_q->f("document_title");
    $cat1 = $doc_q->f("document_category1");
    $cat2 = $doc_q->f("document_category2");
    $author = $doc_q->f("document_author");
    $privacy = ($doc_q->f("document_privacy") == 1 ? " checked" : "");
    $mime = $doc_q->f("document_mimetype");
    if(islink($doc_q->f("document_name"))) {
      $kind = "link";
      $fi_path = $doc_q->f("document_path");
      $url = $doc_q->f("document_name");
      if(strrpos($fi_path,'/') != strlen($fi_path) -1)  {
	$fi_path = $doc_q->f("document_path").'/';
      }
    }
    else {
      $kind = "file";
      $fi_path = $doc_q->f("document_path").'/'.$doc_q->f("document_name");
    }
  }
  // If parameters have been given, they supercede the default action value
  if (isset($document["id"])) { $id = $document["id"]; }
  if (isset($document["title"])) { $title = stripslashes($document["title"]); }
  if (isset($document["privacy"])) { $privacy = ($document["privacy"] == 1 ? "checked" : ""); }
  if (isset($document["category1"])) { $cat1 = $document["category1"]; }
  if (isset($document["category2"])) { $cat2 = $document["category2"]; }
  if (isset($document["author"])) { $author = stripslashes($document["author"]); }
  if (isset($document["mime"])) { $mime = $document["mime"]; }
  if (isset($document["path"])) { $fi_path = stripslashes($document["path"]); }
  if (isset($document["file"])) { $file = stripslashes($document["file"]); } 
  if (isset($document["kind"])) { $kind = $document["kind"]; }
  if (isset($document["entity"])) { $entity = $document["entity"]; }
  if (isset($document["entity_id"])) { $entity_id = $document["entity_id"]; }
  if ($fi_path =="") $fi_path = $default_path;
  // category 1 select
  $sel_cat1 = "<select id=\"sel_cat1\" name=\"sel_cat1\">";
  while($cat1_q->next_record()) {
    $c1_id = $cat1_q->f("documentcategory1_id");
    $sel_cat1 .= "\n<option value=\"$c1_id\"";
    if ($c1_id == $cat1) { $sel_cat1 .= " selected=\"selected\""; }
    $sel_cat1 .= ">". $cat1_q->f("documentcategory1_label") . "</option>\n";
  }
  $sel_cat1 .= "</select>";

  // category 2 select
  $sel_cat2 = "<select id=\"sel_cat2\" name=\"sel_cat2\">";
  while ($cat2_q->next_record()) {
    $c2_id = $cat2_q->f("documentcategory2_id");
    $sel_cat2 .= "\n<option value=\"$c2_id\"";
    if ($c2_id == $cat2) { $sel_cat2 .= " selected=\"selected\""; }
    $sel_cat2 .= ">". $cat2_q->f("documentcategory2_label") . "</option>\n";
  }
  $sel_cat2 .= "</select>";


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
  
   if($kind == "new_file" || $kind == "") { 
     $checked = "checked=\"checked\"";
     $dis_img2 = " style=\"display: none;\" ";
     $dis_url = " style=\"display: none;\" ";
   }
   else 
     $checked = "";
   $rd_kind .= "<input onclick=\"show_hide_field()\" type=\"radio\" name=\"rd_kind\" value=\"new_file\" $checked/> $l_new_file<br /> ";

  if($kind == "file") {
     $dis_field = " style=\"display: none;\" ";
     $checked = "checked=\"checked\"";
     $dis_img = " style=\"display: none;\" ";
     $dis_url = " style=\"display: none;\" ";
   }
   else 
     $checked = "";

   $rd_kind .= "<input onclick=\"show_hide_field()\"type=\"radio\" name=\"rd_kind\" value=\"file\" $checked/> $l_existing_file<br />";
  

  if($kind == "link") {
     $dis_field = " style=\"display: none;\" ";
     $dis_img2 = " style=\"display: none;\" ";
     $checked = "checked=\"checked\"";
     
   }else 
     $checked = "";

   $rd_kind .= "<input onclick=\"show_hide_field()\"type=\"radio\" name=\"rd_kind\" value=\"link\" $checked/> $l_link<br />";

  $display["title"] = "<div class=\"title\">$l_document : $title</div>";
  $url1 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=false"; 
  $url2 = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].tf_path&amp;ext_disp_file=true"; 

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\"  enctype=\"multipart/form-data\" name=\"f_mod_document\"
      onsubmit=\"if (check_document(this)) return true; else return false;\"
      action=\"document_index.php\">

    <div class=\"detailHead\">$l_document</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"30\" value=\"$title\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_author</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_author\" name=\"tf_author\" value=\"$author\" size=\"50\"/ /></td>
<!--    </tr><tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_privacy\" value=\"1\" $privacy /></td> -->
    </tr><tr>
      <td class=\"detailLabel\">$l_category1</td>
      <td class=\"detailForm\">$sel_cat1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_category2</td>
      <td class=\"detailForm\">$sel_cat2</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_file</div>

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
      <a href=\"javascript:return false;\" onclick=\"window.open('$url1','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_browse\" alt=\"\" /></a></span>
     <span id=\"browse_img_file\" $dis_img2>
      <a href=\"javascript:return false;\" onclick=\"window.open('$url2','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_browse_file\" alt=\"\" /></a></span>
      </td>
    </tr> 
    <tr id=\"browse_field\" $dis_field>
      <td class=\"detailLabel\">$l_file</td>
      <td class=\"detailForm\"><input type=\"file\" id=\"fi_file\" name=\"fi_file\" size=\"30\" value=\"$file\" /></td>
    </tr>
    <tr id=\"url_field\" $dis_url>
      <td class=\"detailLabel\">$l_url</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_url\" name=\"tf_url\" size=\"50\" value=\"$url\" /></td>
    </tr> 
   </table>
   <div class=\"detailButton\">
    <p class=\"detailButtons\">
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
// Display Repository Form                                                      //
// Parameters:
//   - $document[] : default values
//     keys used  : uri, name
///////////////////////////////////////////////////////////////////////////////
function html_repository_form($action, $document) {
  global $display,$popup_height,$popup_width;
  global $set_theme,$l_document;
  global $l_path, $l_name,$path,$ico_browse;
  global $l_repository,$l_add_repos;
  $name = $document["repository_name"];
  $re_path = $document["repository_path"];
  $dis_button = "
  <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert_repository\" />
  <input type=\"submit\" value=\"$l_add_repos\" />
  ";
  $url = "$path/document/document_index.php?action=ext_get_path&amp;popup=1&amp;ext_target=forms[0].elements[1]&amp;ext_disp_file=false"; 

  $block = "
    <form method=\"get\" name=\"f_new_repository\"
      onsubmit=\"if (check_repository(this)) return true; else return false;\"
      action=\"".url_prepare("document_index.php")."\">

    <div class=\"detailHead\">$l_repository</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_repository_name\" name=\"tf_repository_name\" maxlength=\"50\" size=\"30\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_path</td>
      <td class=\"detailForm\">
     <input type=\"text\" id=\"tf_repository_path\" name=\"tf_repository_path\" value=\"$re_path\" size=\"50\"/>
       <a href=\"javascript:return false;\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_browse\" alt=\"\" /></a>
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
// HTML Display Document Consultation                                         //
// Parameters:
//   - $co_q   : document database result 
///////////////////////////////////////////////////////////////////////////////
function html_document_consult($doc_q) {
  global $display,$link_to;
  global $set_theme,$l_document,$l_size,$l_bytes;
  global $l_title, $l_category1, $l_mimetype,$l_private;
  global $l_find, $l_author, $l_category2,$l_path,$l_name; 
  global $l_file,$l_insert,$l_update,$l_checkdelete;
  global $c_yes, $c_no,$l_consult;
  // if update mode and first display values are taken from database
  $id = $doc_q->f("document_id");
  $title = $doc_q->f("document_title");
  $cat1 = $doc_q->f("documentcategory1_label");
  $cat2 = $doc_q->f("documentcategory2_label");
  $author = $doc_q->f("document_author");
  $privacy = ($doc_q->f("document_privacy") == 1 ? $c_yes : $c_no );
  $mimetype = $doc_q->f("documentmimetype_label");
  $path = $doc_q->f("document_path");
  $size = $doc_q->f("document_size");
  $name = $doc_q->f("document_name");

  $display["title"] = "<div class=\"title\">$l_document : $title</div>";

  // --- HTML Template --------------------------------------------------------
  if(islink($name)) {
    $url = "document_index.php?action=accessfile&amp;tf_path=$path&amp;fi_file_name=$link_to$title&amp;popup=1";
  }
  else {
    $url = "document_index.php?action=accessfile&amp;tf_path=$path&amp;fi_file_name=$name&amp;popup=1";
  }
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
    </tr><tr>
      <td class=\"detailLabel\">$l_category1</td>
      <td class=\"detailText\">$cat1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_category2</td>
      <td class=\"detailText\">$cat2</td>
    </tr>
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
// Display: Display and check the document links (and delete form if ok)
// Parameters:
//   - $p_id : document id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_have_link,$l_have_no_link;
  global $path, $display;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_document_links($p_id);

  if ($obm_q > 0) {
    $delete_ok = false;
    $display_linkc ="
      <tr>
        <td class=\"detailHead\">$l_have_link</td>
      </tr>";
  } else {
    $display_linkc = "
      <tr>
        <td class=\"detailHead\">$l_have_no_link</td>
      </tr>";
  }

 
  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("document_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_document\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $block .= "<table class=\"detail\">
   $display_linkc
   $display_linkd
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
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : document Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("document_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"hd_document_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}

///////////////////////////////////////////////////////////////////////////////
// Display the document administration index                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $cat1_q = run_query_documentcategory1();
  $cat2_q = run_query_documentcategory2();
  $mime_q = run_query_documentmime();



  $block = html_document_cat1_form($cat1_q);
  $block .= "<hr />";
  $block .= html_document_cat2_form($cat2_q);
  $block .= "<hr />";
  $block .= html_document_mime_form($mime_q);

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: Document Category1 section                                             //
// Parameters:
//   - $cat1_q : Category1 list database object
///////////////////////////////////////////////////////////////////////////////
function html_document_cat1_form($cat1_q) {
  global $l_cat1_manage,$l_cat1_exist;
  global $l_cat1_checkdelete,$l_cat1_update,$l_cat1_new,$l_cat1_insert;

  $sel_cat1 = "<select name=\"sel_cat1\" onChange=\"
    forms.form_cat1_update.tf_cat1.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($cat1_q->next_record()) {
    $id = $cat1_q->f("documentcategory1_id");
    $label = $cat1_q->f("documentcategory1_label");
    $sel_cat1 .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_cat1 .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_cat1_manage
      </td>
    </tr><tr>
      <td>

<!-- Category1 Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_cat1_delete\" method=\"post\"
              action=\"" . url_prepare("document_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_cat1_exist</td>
              <td class=\"adminLabel\">$sel_cat1</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"cat1_checklink\" />
              <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_checkdelete\" onclick=\"if (check_cat1_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category1 Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_cat1_update\" method=\"get\"
              action=\"" . url_prepare("document_index.php") . "\">
            <input type=\"hidden\" name=\"sel_cat1\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"cat1_update\" />
            <input type=\"text\" name=\"tf_cat1\" />
            <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_update\"
              onclick=\"if (check_cat1_upd(this.form,document.form_cat1_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category1 Section -->

        <form name=\"form_cat1_new\" method=\"get\"
          action=\"" . url_prepare("document_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_cat1_new :
            <input name=\"tf_cat1\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"cat1_insert\" />
            <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_insert\"
              onclick=\"if (check_cat1_new(this.form)) return true; else return false;\" />
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
// Display: Document Category1 section                                             //
// Parameters:
//   - $cat2_q : Category2 list database object
///////////////////////////////////////////////////////////////////////////////
function html_document_cat2_form($cat2_q) {
  global $l_cat2_manage,$l_cat2_exist;
  global $l_cat2_checkdelete,$l_cat2_update,$l_cat2_new,$l_cat2_insert;

  $sel_cat2 = "<select name=\"sel_cat2\" onChange=\"
    forms.form_cat2_update.tf_cat2.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($cat2_q->next_record()) {
    $id = $cat2_q->f("documentcategory2_id");
    $label = $cat2_q->f("documentcategory2_label");
    $sel_cat2 .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_cat2 .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_cat2_manage
      </td>
    </tr><tr>
      <td>

<!-- Category2 Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_cat2_delete\" method=\"post\"
              action=\"" . url_prepare("document_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_cat2_exist</td>
              <td class=\"adminLabel\">$sel_cat2</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"cat2_checklink\" />
              <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_checkdelete\" onclick=\"if (check_cat2_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category2 Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_cat2_update\" method=\"get\"
              action=\"" . url_prepare("document_index.php") . "\">
            <input type=\"hidden\" name=\"sel_cat2\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"cat2_update\" />
            <input type=\"text\" name=\"tf_cat2\" />
            <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_update\"
              onclick=\"if (check_cat2_upd(this.form,document.form_cat2_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category2 Section -->

        <form name=\"form_cat2_new\" method=\"get\"
          action=\"" . url_prepare("document_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_cat2_new :
            <input name=\"tf_cat2\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"cat2_insert\" />
            <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_insert\"
              onclick=\"if (check_cat2_new(this.form)) return true; else return false;\" />
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
// Display: Document Category2 section                                             //
// Parameters:
//   - $mime_q : Category2 list database object
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
// Display: the cat1 links                                               //
// Parameters:
//   - $document : document hash info : keys used : cat1
///////////////////////////////////////////////////////////////////////////////
function dis_cat1_links($document) {
  global $l_cat1_link_document, $l_cat1_link_document_no;
  global $l_cat1_delete, $l_cat1_can_delete, $l_cat1_cant_delete;
  global $l_back,$display;

  $id = $document["category1"];
  $label = get_cat1_label($id);
  $obm_q = run_query_cat1_links($id);
  if ($obm_q->num_rows() > 0) {
    $display["msg"] .= display_warn_msg($l_cat1_cant_delete."aaé");
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat1_link_document</td>
    </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("document_id");
      $cname = $obm_q->f("document_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("document_index.php?action=detailconsult&amp;param_document=$cid") . "\">$cname</a>
      </td>
    </tr>
    </table>";
    }
    $dis_link .="
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
    $display["msg"] .= display_ok_msg($l_cat1_can_delete);

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $label : $l_cat1_link_document_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">    
      <form name=\"form_act_delete\"
	method=post action=\"" . url_prepare("document_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"cat1_delete\">
      <input type=\"hidden\" name=\"sel_cat1\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_cat1_delete\">
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
// Display: the cat2 links                                               //
// Parameters:
//   - $document : document hash info : keys used : cat2
///////////////////////////////////////////////////////////////////////////////
function dis_cat2_links($document) {
  global $l_cat2_link_document, $l_cat2_link_document_no;
  global $l_cat2_delete, $l_cat2_can_delete, $l_cat2_cant_delete;
  global $l_back,$display;

  $id = $document["category2"];
  $label = get_cat2_label($id);
  $obm_q = run_query_cat2_links($id);
  if ($obm_q->num_rows() > 0) {
    $display["msg"] .= display_warn_msg($l_cat2_cant_delete."aaé");
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat2_link_document</td>
    </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("document_id");
      $cname = $obm_q->f("document_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("document_index.php?action=detailconsult&amp;param_document=$cid") . "\">$cname</a>
      </td>
    </tr>
    </table>";
    }
    $dis_link .="
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
    $display["msg"] .= display_ok_msg($l_cat2_can_delete);

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $label : $l_cat2_link_document_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">    
      <form name=\"form_act_delete\"
	method=post action=\"" . url_prepare("document_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"cat2_delete\">
      <input type=\"hidden\" name=\"sel_cat2\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_cat2_delete\">
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
// Display: the mime links                                               //
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
  if ($obm_q->num_rows() > 0) {
    $display["msg"] .= display_warn_msg($l_mime_cant_delete."aaé");
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_mime_link_document</td>
    </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("document_id");
      $cname = $obm_q->f("document_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("document_index.php?action=detailconsult&amp;param_document=$cid") . "\">$cname</a>
      </td>
    </tr>
    </table>";
    }
    $dis_link .="
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
// Display: the Document Display preference screen                            //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_document_display_pref($display_pref_q) {
  global $l_document_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
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
  global $document_path,$l_close;
  $popup = $document["popup"];
  $block = dis_repository_rec($document,$document_path,$display_file);
  if($popup) {
    $block.= "<a href=\"javascript:window.close();\">$l_close</a>";
  }
  return $block;
  
}

///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function dis_repository_rec($document,$repository,$display_file) {
  global $document_path;
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory,$set_theme;
  $root_handler = opendir($repository);
  $relative_path = substr($repository,strlen($document_path)); 
  $popup = $document["popup"];
  
  if($repository == $document_path) {
    $name = "/";
    $relative_path = $name;
  }
  else {
    $name = substr($repository,strrpos($repository,'/')+1);
  }
  if($popup) {
   if($display_file == "false") {
     $js = "onclick=\"fill_ext_form('$relative_path');return false;\"";
     $link_repo = "<a href=\"\" $js>";
     $link_repo_end ="</a>";
   }
   $close = "<a href=\"javascript:window.close();\">$l_close</a>";
  }

  $files = array();
  $repos = array();

  while($file = readdir($root_handler)) {
    $path = "$repository/$file";
    if($file != ".." && $file != ".") {
      if(is_dir($path)) {
	$repos[] = $path;
      }
      elseif($display_file == "true") {
	$files[] = $file;
      }
    }
    
  }
  closedir($root_handler);
  
  if(count($repos) == 0 && (count($files) == 0 || $display_file == "false")) {
     $img = "/images/$set_theme/$ico_directory";
  }
  else {
     $img = "/images/$set_theme/$ico_directory_close";
     $js_int = "<a href=\"\" onclick=\"show_hide_repo('$relative_path');return false;\"\">";
     $js_int2 = " </a>";
  }
  
  $block = "
  <div class=\"documentRepository\">
   $js_int <img id=\"img_$relative_path\" src=\"$img\" alt=\"\" />$js_int2 
   $link_repo $ind $name $link_repo_end
  </div>";

  $block .= "<div id=\"$relative_path\" style=\"display:none;\" class=\"documentBox\">";

   foreach($repos as $path) {
    $block .= dis_repository_rec($document,$path,$display_file);
  }
  
  foreach($files as $file) {
    if($popup) {
      $js = "onclick=\"fill_ext_form('$relative_path/$file');return false;\"";
    }
    
    $file = stripslashes($file);
    $block .= "<div class=\"documentFile\">
     <a href=\"document_index.php?action=detailconsult&amp;name_document=$file&amp;tf_path=$relative_path\" $js><img src=\"/images/$set_theme/$ico_file\" alt=\"\" /></a>
     <a target=\"_blank\" href=\"document_index.php?action=accessfile&amp;tf_path=$relative_path&amp;fi_file_name=$file&amp;popup=1\" $js>
      $file</a><br /></div>";
  }
  $block .= "</div>";
  return $block;
      
  
  
}

///////////////////////////////////////////////////////////////////////////////
// Consult File
/////////////////////////////////////////////////////////////////////////////

function dis_file($document) {
  global $document_path,$link_to;
  global $obm_version,$set_weekstart_default;
  $path = $document["path"];
  $name = $document["name"];
  if(islink($name)) {
    $link = getlink($name,$path);
    header("location: $link");
    exit;
  }

  $doc_q = run_query_detail_by_path($path,$name);
  if(!$doc_q) {
    $mime = run_query_get_mimetype($name);
  }
  else {
    $mime = $doc_q->f("documentmimetype_mime");  
  }
  $final_path = $document_path.'/'.$path.'/'.$name; 
  $handle = fopen ($final_path, "r");
  header("Content-Type: $mime");
  header("Content-Disposition: inline; filename=$name");
  echo  fread($handle, filesize ($final_path));
  fclose ($handle); 
  
}
