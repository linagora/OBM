<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : organizationalchart_display.inc                              //
//     - Desc : Organizational Chart Display File                            //
// 2007-02-26 David PHAN                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["organizationalchart_name"] = $l_name;
$fieldnames["organizationalchart_description"] = $l_desc;
$fieldnames["organizationalchart_archive"] = $l_archive;


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_organizationalchart(&$OD, $fieldname, $link_ok) {

  if ($fieldname == "organizationalchart_name") {
    $res["url"] = "$path/organizationalchart/organizationalchart_index.php";
    $res["url"] .= "?action=detailconsult&amp;organizationalchart_id=".$OD->data_set->f("organizationalchart_id");
  }  else if ($fieldname == "organizationalchart_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart search form
// Parameters:
//   - $organizationalchart[] : hash with company values
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_search_form($organizationalchart) {

  $block = html_organizationalchart_search_form($organizationalchart);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Organizational Chart search Form
// Parameters:
//   - $organizationalchart[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_search_form($organizationalchart) {
  global $action, $display;
  global $l_find, $l_name, $l_user, $l_desc, $l_archive;

  $popup = $organizationalchart["popup"];
  $name = stripslashes($organizationalchart["name"]);
  $user = stripslashes($organizationalchart["user"]);
  $desc = stripslashes($organizationalchart["desc"]);
  $archive = ($organizationalchart["archive"] == "1" ? "checked = \"checked\"" : "");

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"$url\">
    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"24\"  value=\"$name\" />
    </label>
    <label>$l_user<br />
      <input type=\"text\" name=\"tf_user\" size=\"24\"  value=\"$user\" />
    </label>
    <label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"24\"  value=\"$desc\" />
    </label>
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Organizational Chart search result
// Parameters:
//   - $organizationalchart[] : company search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_search_list($organizationalchart) {
  global $display, $l_found, $l_no_found, $obm;

  $popup = $organizationalchart["popup"];
  $prefs = get_display_pref($obm["uid"], "organizationalchart");
  $obm_q = run_query_organizationalchart_search($organizationalchart);
  $nb_organizationalchart = $obm_q->num_rows_total();
  if ($nb_organizationalchart == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $display["msg"] = display_info_msg("$nb_organizationalchart $l_found");
    $block = html_organizationalchart_search_list($obm_q, $prefs, $organizationalchart, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $organizationalchart_q    : list of organizational chart
//   - $prefs     : Display preferences
//   - $organizationalchart[] : company search criteria
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_search_list($organizationalchart_q, $prefs, $organizationalchart, $popup) {

  if ($popup) {
    $ext_action = $organizationalchart["ext_action"];
    $ext_url = urlencode($organizationalchart["ext_url"]);
    $ext_id = $organizationalchart["ext_id"];
    $ext_target = $organizationalchart["ext_target"];
    $ext_widget = $organizationalchart["ext_widget"];
    $ext_widget_text = $organizationalchart["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  // we urlencode to avoid breakage for space char
  $name = urlencode(stripslashes($organizationalchart["name"]));
  $archive = $organizationalchart["archive"];
  $user = stripslashes($organizationalchart["user"]);

  $url = url_prepare("organizationalchart_index.php?action=search&amp;tf_name=$name&amp;tf_user=$user&amp;cb_archive=$archive$url_userdata$url_ext");
  $orgchart_d = new OBM_DISPLAY("DATA", $prefs, "organizationalchart");
  if ($popup) {
    $orgchart_d->display_link = false;
    if ($ext_url != "") {
      $orgchart_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $orgchart_d->display_ext = "get_id";
    }
    $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
  }
  
  $orgchart_d->data_set = $organizationalchart_q;
  $orgchart_d->data_url = $url;
  $orgchart_d->data_header = "both";
  $block = $orgchart_d->display("dis_data_organizationalchart");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart Form
// Parameters:
//   - $action    : action called
//   - $organizationalchart[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_form($action, $organizationalchart) {
  global $display, $obm, $l_err_reference;

  $id = $organizationalchart["organizationalchart_id"];
  if ($id > 0) {
    $oc_q = run_query_organizationalchart_detail($id);
    $root_node = run_query_organizationalchart_root_node($id);
    if ($oc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($oc_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  }
  
  $block = html_organizationalchart_form($action, $oc_q, $organizationalchart, $root_node);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Organizational Chart Form
// Parameters:
//   - $action    : action called
//   - $organizationalchart : default values
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_form($action, $oc_q, $organizationalchart, $root_node) {
  global $obm, $display, $popup_height, $popup_width;
  global $ico_user, $ico_group, $ico_add, $ico_delete;
  global $l_organizationalchart, $l_name, $l_desc, $l_archive, $l_update, $l_insert;
  global $l_detail;

  if ($action == "detailupdate" || $action == "update") {
    $id = $oc_q->f("organizationalchart_id");
    $name = $oc_q->f("organizationalchart_name");
    $desc = $oc_q->f("organizationalchart_description");
    $archive = $oc_q->f("organizationalchart_archive") == "1" ? "checked" : "";
    $nb_child = ($oc_q->f("node") + 1);
    $block_detail = draw_form($root_node);
    $block_detail .= "<input type=\"hidden\" id=\"nb_child\" name=\"nb_child\" value=\"$nb_child\" />
      <input type=\"hidden\" id=\"organizationalchart_id\" name=\"organizationalchart_id\" value=\"$id\" /";
  } else {
    $url_user = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent1";
    $url_group = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent1";
    $block_detail = "<div id=\"1\" class=\"organizational_chart_node\">
        <div class=\"organizational_chart_wrap\">
        <input type=\"text\" id=\"tf_ogroup[1]\" name=\"tf_ogroup[1]\" maxlength=\"16\" size=\"24\" value=\"$ogroup\" />
        <input type=\"hidden\" id=\"tf_parent_level[1]\" name=\"tf_parent_level[1]\" value=\"0\" />
        <!-- user extmod -->
        <a href=\"javascript: return false;\"
          onclick=\"window.open('$url_user','','height=$popup_height,width=$popup_width,scrollbars=yes');return false;\">
          <img src=\"$ico_user\" alt=\"[User]\" /></a>
        <!-- group extmod -->
          <a href=\"javascript: return false;\"
          onclick=\"window.open('$url_group','','height=$popup_height,width=$popup_width,scrollbars=yes');return false;\">
          <img src=\"$ico_group\" alt=\"[Group]\" /></a>
        <a href=\"javascript: add_entity('1');\"> <img src=\"$ico_add\" alt=\"[Add]\" /> </a>
        <input type=\"hidden\" name=\"nb_child\" id=\"nb_child\" value=\"1\" />
      <div class=\"organizational_chart_entities\" id=\"sel_ent1\">
        <!-- Entities -->
      </div>
      </div>
      </div>";
  }

  // If parameters have been given, they supercede the default action value
  if (isset($organizationalchart["oc_name"])) { $name = stripslashes($organizationalchart["oc_name"]); }
  if (isset($organizationalchart["desc"])) { $desc = stripslashes($organizationalchart["desc"]); }
  if (isset($organizationalchart["archive"])) { $archive = ($organizationalchart["archive"] == 1 ? "checked" : ""); }

  // Buttons
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
    <!-- Update button -->
    <input type=\"hidden\" name=\"organizationalchart_id\" id=\"organizationalchart_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
    <input type=\"submit\" value=\"$l_update\" />";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
    <!-- Insert button -->
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
    <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display["title"] = "<h1 class=\"title\">$l_organizationalchart : $oc_name</h1>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_form(this)) return true; else return false;\"
    action=\"".url_prepare("organizationalchart_index.php")."\">
    <fieldset class=\"detail extra\">  
      <legend>$l_organizationalchart</legend>
      <table>
        <tr>
          <th>$l_name :</th>
				  <td><input type=\"text\" id=\"tf_oc_name\" name=\"tf_oc_name\" maxlength=\"32\" size=\"50\" value=\"$name\" /></td>
        </tr><tr>
          <th>$l_desc :</th>
          <td><input type=\"text\" id=\"tf_desc\" name=\"tf_desc\" maxlength=\"255\" size=\"50\" value=\"$desc\" /></td>
        </tr><tr>
          <th>$l_archive :</th>
          <td><input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /></td>
        </tr>
      </table>
    </fieldset>
    <!-- Organizational Chart -->
    <fieldset class=\"detail extra\">  
      <legend>$l_detail</legend>
        $block_detail
    </fieldset>
    <fieldset class=\"buttons\">$dis_button</fieldset>
  </form>";
/*
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_ent1', 'user');
    });
  </script>"; 
*/
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart Detail
// Parameters:
//   - $organizationalchart[] : criteria
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_consult($organizationalchart) {

  $id = $organizationalchart["organizationalchart_id"];
  if ($id) {
    $oc_q = run_query_organizationalchart_detail($id);
    $root_node = run_query_organizationalchart_root_node($id);
    $block = html_organizationalchart_consult($organizationalchart, $oc_q, $root_node); 
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Organizational Chart Detail
// Parameters:
//   - $oc_q : ogroup database result 
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_consult($organizationalchart, $oc_q, $root_node) {
  global $display;
  global $l_organizationalchart, $l_detail;
  global $l_name, $l_desc, $l_archive, $l_yes, $l_no;

  $oc_name = $oc_q->f("organizationalchart_name");
  $desc = $oc_q->f("organizationalchart_description");
  $archive = $oc_q->f("organizationalchart_archive") == 1 ? $l_yes : $l_no;
  $block_detail = draw_consult($root_node);

  $display["detailInfo"] = display_record_info($oc_q);
  $display["title"] = "<h1 class=\"title\">$l_organizationalchart : $oc_name</h1>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <div class=\"detail extra\">
    <h1>$l_organizationalchart</h1>
      <table>
        <tr>
          <th>$l_name :</th>
          <td>$oc_name</td>
        </tr>
        <tr>
          <th>$l_desc :</th>
          <td>$desc</td>
        </tr>
        <tr>
          <th>$l_archive :</th>
          <td>$archive</td>
        </tr>
       </table>
    <h1>$l_detail</h1>
    <div id=\"container\" class=\"container\">
      $block_detail
    </div>
  </div>";
 
  $block .= dis_organizationalchart_userdetail();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the organizational chart delete validation screen
// Parameters:
//   - $id : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_organizationalchart($id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display,$path;
  
  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
  <div class=\"buttons\">
  <a href=\"$path/organizationalchart/organizationalchart_index.php?action=delete&amp;organizationalchart_id=$id\" 
    onclick=\"return confirm_company_del(this.form);\">
  $l_delete 
  </a>
  <a href=\"$path/organizationalchart/organizationalchart_index.php?action=detailconsult&amp;organizationalchart_id=$id\">$l_back</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the OrganizationalChart Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_display_pref($prefs) {
  global $l_organizationalchart_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "organizationalchart");
  $dis_pref->pref_title = $l_organizationalchart_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Draw a node
// Parameters:
//    - $node_q : node detail
///////////////////////////////////////////////////////////////////////////////
function draw_consult($node_q) {
  global $f, $l; 
  global $l_title, $l_phone, $l_mphone, $l_email;
  global $ico_manager, $ico_nophoto;

  $node_name = $node_q->f("ogroup_name");
  $node_id = $node_q->f("ogroup_id");
  $parent_node_id = $node_q->f("ogroup_parent_id");
  $level = $node_q->f("ogroup_level");

  if (!isset($l)) {
    $l = array();
  }

  if (!isset($f)) {
    $f = array();
  }

  // Get node detail
  $ogroupentity_q = run_query_organizationalchart_ogroupentity_detail($node_id);
  while($ogroupentity_q->next_record()) {
    $entity = $ogroupentity_q->f("ogroupentity_entity");
    $entity_id = $ogroupentity_q->f("ogroupentity_entity_id");
    switch($entity) {
      case "group":
        $usr_q = run_query_all_users_from_group($entity_id);
        $g_manager = run_query_global_group_manager($entity_id);
        break;
      case "user":
       $usr_q = run_query_userobm($entity_id);
       break;
    }
    while($usr_q->next_record()) {
      $userobm_id = $usr_q->f("userobm_id");
      $userobm_url = "$path/user/user_index.php?action=detailconsult&amp;user_id=$userobm_id";
      $userobm_name = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
      $manager = $g_manager== $userobm_id ? "<img src=\"$ico_manager\" />" : "";
      $item_id = "item-${level}-${userobm_id}";
      $block_detail_entities .= "<li id=\"item-${level}-${userobm_id}\">
        <a href=\"$userobm_url\" 
			    onmouseover=\"obm.userDetail.compute($userobm_id, event);return false;\"
          onmouseout=\"obm.userDetail.compute($userobm_id, event);return false;\">$manager$userobm_name</a>
        </li>";
    }
  }
 
  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  $nb_leaves = run_query_organizationalchart_get_leaves($node_id, true);
  if ($nb_children > 0) {
    $wrap_width = $nb_leaves * 150;
    $node_queue = "<div class=\"node_tail\"></div>";
	  foreach($t_child as $key => $child_id){
      if ($nb_children >= 2) {
        if ($key == 0){
          // first child
          $f[] = $child_id;
        } else if ($key == ($nb_children-1)){
          // last child
          $l[] = $child_id;
        }
      }
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = draw_consult($child_q);
      $block_detail_child .= $t_draw;
    }
  } else {
    $wrap_width = 150;
  }

  if ($parent_node_id > 0) {
    $node_head = "<div class=\"node_tail\"></div>";
    if (in_array($node_id, $f)) {  // is first child ?
      $node_edge = "<div class=\"node_start\" style=\"width: ".($wrap_width/2)."px;
        margin-left: ".($wrap_width/2)."px\"></div>";
    } else if (in_array($node_id, $l)) {  // is last child ?
      $node_edge = "<div class=\"node_end\" style=\"width: ".($wrap_width/2)."px;
        margin-right: ".($wrap_width/2)."px\"\"></div>";
    } else {  // not first or last child
      $t = array_merge($f, $l);
      if (!in_array($node_id, $t)) {
        $t_child = run_query_organizationalchart_node_child($parent_node_id);
        $nb_neighbour = sizeof($t_child);
        if ($nb_neighbour > 1) {    
          $border_top = "border-top: 1px solid #646567;";
        } else {
          $border_top = "border-top: 0.1px solid #eff0f2;";
        }
      }
    }
  } else {
    $node_head = "";
    $wrap_width = $nb_leaves * 150;
  }

  $block = "
    <div id=\"node-wrap-$node_id\" class=\"node_wrap\"
      style=\"width: ${wrap_width}px; $border_top\">
      $node_edge
      $node_head
      <div id=\"node-$node_id\" class=\"node toggle\">
        <h1>$node_name</h1>
        <ul>$block_detail_entities</ul>
      </div>
      $node_queue
      $block_detail_child
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Draw a node form
// Parameters:
//    - $node_q : node detail
///////////////////////////////////////////////////////////////////////////////
function draw_form($node_q) {
  global $f, $l; 
  global $ico_delete, $ico_add, $ico_user, $ico_group;
  global $popup_height, $popup_width;

  $node_name = $node_q->f("ogroup_name");
  $node_id = $node_q->f("ogroup_id");
  $parent_node_id = $node_q->f("ogroup_parent_id");
  $parent_level = $node_q->f("parent_level");
  $level = $node_q->f("ogroup_level");

  if (!isset($l)) {
    $l = array();
  }

  if (!isset($f)) {
    $f = array();
  }

  // Get node detail
  $ogroupentity_q = run_query_organizationalchart_ogroupentity_detail($node_id);
  while($ogroupentity_q->next_record()) {
     $entity = $ogroupentity_q->f("ogroupentity_entity");
     $entity_id = $ogroupentity_q->f("ogroupentity_entity_id");
     switch($entity) {
       case "user":
         $name_q = run_query_userobm($entity_id);
         $name_q->next_record();
         $name = $name_q->f("userobm_lastname")." ".$name_q->f("userobm_firstname");
       break;
       case "group":
         $name = get_last_group_text($entity_id);
       break;
     }
     $block_detail_entities .= "<div class=\"elementRow\" id=\"sel_ent${entity}${level}data-${entity}-${entity_id}\">
       <a href=\"javascript: remove_element('sel_ent${entity}${level}data-${entity}-${entity_id}','sel_ent${level}');\">
        <img src=\"$ico_delete\">
       </a>
       $name
       <input value=\"data-${entity}-${entity_id}\" name=\"sel_ent${level}[]\" type=\"hidden\"></div>";
  }

  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  if ($nb_children > 0) {
	  foreach($t_child as $key => $child_id){
      if ($nb_children >= 2) {
        if ($key == 0){
          // first child
          $f[] = $child_id;
        } else if ($key == ($nb_children-1)){
          // last child
          $l[] = $child_id;
        }
      }
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = draw_form($child_q);
      $block_detail_child .= $t_draw;
    }
  } 

  if ($level > 1) {
    $ico_del_entity = "<a href='javascript: del_entity(\"$level\");'><img src=\"$ico_delete\"></a>";
    $stage = strlen($level) - strlen(str_replace(".", "", $level));  
    $last_stage = strlen($parent_level) - strlen(str_replace(".", "", $parent_level));  
    if ($last_stage < $stage) {
      $margin = "style=\"margin-left: 30px;\"";
    } else if ($last_stage > $stage) {
      $margin = "style=\"margin-left: -30px;\"";
    } else {
      $margin = "";
    }
    $color = "color".($stage);
  } else {  
    $ico_del_entity = "";
    $color = "";
  }

  // Urls for extmod
  $url_user = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent$level";
  $url_group = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent$level";

  $block = "<div id=\"$level\" class=\"organizational_chart_node\" $margin>
    <div class=\"organizational_chart_wrap $color\">
      $ico_del_entity
      <input type=\"text\" id=\"tf_ogroup[$level]\" name=\"tf_ogroup[$level]\" 
        maxlength=\"32\" size=\"24\" value=\"$node_name\" />
      <input type=\"hidden\" id=\"tf_parent_level[$level]\" name=\"tf_parent_level[$level]\"
        value=\"$parent_level\" />
      <!-- user extmod -->
      <a href=\"javascript: return false;\"
        onclick=\"window.open('$url_user','','height=$popup_height,width=$popup_width,scrollbars=yes');
        return false;\">
        <img src=\"$ico_user\" alt=\"[User]\" />
      </a>
      <!-- group extmod -->
      <a href=\"javascript: return false;\"
        onclick=\"window.open('$url_group','','height=$popup_height,width=$popup_width,scrollbars=yes');
        return false;\">
       <img src=\"$ico_group\" alt=\"[Group]\" />
      </a>
      <a href=\"javascript: add_entity('$level');\"> <img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <div class=\"organizational_chart_entities\" id=\"sel_ent$level\">
        <!-- Entities -->
        $block_detail_entities
      </div>
    </div>
    $block_detail_child
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// User detail popup 
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_userdetail() {
  global $l_title, $l_phone, $l_mphone, $l_email;
  global $ico_nophoto, $ico_load, $path; 

  $block = "
    <div id=\"userdetail\" class=\"userdetail\">
      <div id=\"userdetail_photo\" class=\"userphoto\">$block_photo</div>
      <div id=\"userdetail_name\" class=\"userdetail_name\"></div>
      <table>
        <tr class=\"blockItem\">
          <th>$l_title</th>
          <td id=\"userdetail_title\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_phone</th>
          <td id=\"userdetail_phone\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_mphone</th>
          <td id=\"userdetail_mphone\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_email</th>
          <td id=\"userdetail_email\"></td>
        </tr>
      </table>
    </div>
    <script type=\"text/javascript\">
      obm.userDetail = new Obm.UserDetail();
      obm.vars.path = '$path';
      obm.vars.images.nophoto = '$ico_nophoto';
      obm.vars.images.load = '$ico_load';
    </script>";

  return $block;
}


/**
 * Build a JSON message with all the event data
 * the respone is a table of event objects.
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function organizationalchart_json_event($usr_q) {
  global $display;

  $userobm_email = $usr_q->f("userobm_email");
  if($userobm_email !=  "") {
    $email = $userobm_email."@".get_last_domain_text($usr_q->f("userobm_domain_id"));
  } else {
    $email = "";
  }

  $info[] = "id:".$usr_q->f("userobm_id");
  $info[] = "name:'".$usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname")."'";
  $info[] = "photo:'".$usr_q->f("userobm_photo_id")."'";
  $info[] = "title:'".$usr_q->f("userobm_title")."'";
  $info[] = "phone:'".$usr_q->f("userobm_phone")."'";
  $info[] = "mphone:'".$usr_q->f("userobm_mobile")."'";
  $info[] = "email:'$email'";

  $json = implode(",",$info);
  $display['json'] =  "$json,error:0";
}

?>
