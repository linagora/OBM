<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_display.inc                                               //
//     - Desc : OBM Framework Display Objects and functions                  //
// 2005-07-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

class OBM_DISPLAY {
  var $display_type;      // determines the type of display. possible values
    // - PREFERENCES  : to set the display preference
    // - DATA         : to display all elements, for exemple all contacts
  var $display_module;    // module where to find the entity in use
  var $display_entity;    // entity used (deal, contact, parentdeal,...)
  var $display_pref;      // User preferences : Array[fieldname] order,status
  var $display_link = 1;  // If false, extern links aren't displayed
  var $display_ext = "";  // External action called (allow Special handling)

  //----------- TYPE SECTION : PREFERENCES --------//
  var $pref_title;       // title of the preference panel
  var $pref_dis_help;    // flag to tell if the pref help screen shoulg be dis.

  //----------- TYPE SECTION : DATA -----------//
  var $data_set;              // Data set :DB_OBM: data rows to be displayed
  var $data_header = "top";   // Table header location (top|both) (+bottom)
  var $data_page = true;      // Page management (true | false)
  var $data_url = "";         // Base URL (with search) used for links in Index
  var $data_order = true;     // Order can be changed (click on column title)
  var $data_cb_text = "";     // Checkbox column text
  var $data_cb_side = "left"; // Checkbox position in the row (left|right)
  var $data_cb_name = "";     // Base name of the checkbox element (+id)
  var $data_cb_field = "";    // Field from which cb is filled (1=selected)
  var $data_idfield = "_id";  // Index field for the data row
  var $data_cb_show = 1;      // To set visible
                              //   1 -> visible
                              //   "string" if ->f["string"] == 0 -> visible
  var $data_form_head = "";   // Form header
  var $data_form_end = "";    // Form end
  var $delimiter = '"';        // CVS field delimiter (enclosed by)

  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY Constructor
  // Parameters:
  //   - $type   : display type
  //   - $pref   : display pref : DBO
  //   - $module : calling module
  /////////////////////////////////////////////////////////////////////////////
  function OBM_DISPLAY($type, $pref, $module="") {
    if ( ($type != "PREFERENCES") && 
	($type != "DATA") ) {
      die("ERROR: the type MUST be PREFERENCES or DATA");
    } else {
      $this->display_type = $type;
    }
    if (isset($pref)) {
      $this->display_pref = $pref;
    }
    if (isset($module)) {
      $this->display_module = $module;
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY main display function
  /////////////////////////////////////////////////////////////////////////////
  function display($dis_data_entity="") {
    if ($this->display_type == "PREFERENCES") {
      $this->check_preferences_vars();
      return $this->dis_preferences();
    } else if ($this->display_type == "DATA") {
      $this->check_data_vars();
      return $this->dis_data($dis_data_entity);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if DATA variables are set, if not die               //
  /////////////////////////////////////////////////////////////////////////////
  function check_data_vars() {
    if (!is_array($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if PREFERENCES variables are set, if not die        //
  /////////////////////////////////////////////////////////////////////////////
  function check_preferences_vars() {
    if (! is_array($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
    if (!isset($this->display_entity)) {
      die("THE display_entity VARIABLE MUST BE SET");
    }
    if (!isset($this->pref_title)) {
      die("THE pref_title VARIABLE MUST BE SET");
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preferences screen for the dataset            //
  /////////////////////////////////////////////////////////////////////////////
  function dis_preferences() {
    global $l_default, $l_displayed, $l_order, $l_field, $l_move, $l_mandatory;
    global $l_display_no, $l_display_yes, $l_down, $l_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;
    global $fieldnames, $cgp_hide;
    
    $nb_field = count($this->display_pref) - 1;

    // actions names
    $action_display = "dispref_display";
    $action_level = "dispref_level";

    $entity = $this->display_entity;

    if ($this->display_module != "") {
      $url = $this->display_module . "_index.php";
    } else {
      $url = $this->display_entity . "_index.php";
    }

    $block = "
    <table class=\"resultPref\" >
    <tr>
      <td class=\"resultHead\" colspan=\"5\">".$this->pref_title."</td>
    </tr>
    <tr>
      <td class=\"resultLabel\">$l_default</td>
      <td class=\"resultLabel\">$l_displayed</td>
      <td class=\"resultLabel\">$l_order</td>
      <td class=\"resultLabel\">$l_field</td>
      <td class=\"resultLabel\">$l_move</td>
    </tr>";

    $i = 0;
    $nb_field_hidden = 0;
    while ( list($fieldname, $fieldinfo) = each($this->display_pref) ) {
      $entity = $this->display_entity;
      $fieldstatus = $fieldinfo["status"];
      $fieldorder = $fieldinfo["order"];
      $fielddefault = (! $fieldinfo["notdefault"]);

      if ($fielddefault) {
        $dis_default = "X";
      } else {
        $dis_default = "&nbsp;";
      }

      if ($cgp_hide[$entity][$fieldname]) {
	$td_class = "resultPrefHide";
      } else {
	$td_class = "resultTextC";
      }

      $block .= "
    <tr>
    <td class=\"$td_class\">$dis_default</td>
    <td class=\"$td_class\">";

      if ($fieldstatus == 0) {
	$new_status = 1;
	$nb_field_hidden++;
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldstatus=$new_status")."\">
      <img alt=\"$l_display_yes\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_crow\" /></a>";
      } else if ($fieldstatus == 1) {
	$new_status = 0;
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldstatus=$new_status")."\">
      <img alt=\"$l_display_no\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_ball\" /></a>";
      } else if ($fieldstatus == 2) {
	$block .= "
      <img alt=\"$l_mandatory\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_forbidden\" />";
      }

      $block .= "
      </td>
      <td class=\"$td_class\">";

      if ($fieldstatus == 0) {
	$block .= "&nbsp;";
      } else {
	$pos = $fieldorder - $nb_field_hidden;
	$block .= "$pos";
      }
      $block .= "</td>
    <td class=\"$td_class\">".$fieldnames[$fieldname]."</td>
    <td class=\"$td_class\">
      <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">
      <tr>";
      
      // If last field we doesn't display the down arrow
      if ($i == $nb_field) {
	$block .= "
        <td>&nbsp;</td>";
      } else {
        $new_order = $fieldorder + 1;
	$block .= "
        <td><a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldorder=$new_order")."\">
          <img alt=\"$l_down\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_arrow_down\" /></a></td>";
      }
      
      // If first field we doesn't display the up arrow
      if ($i == 0) {
	$block .= "
        <td >&nbsp;</td>";
      } else {
        $new_order = $fieldorder - 1;
	$block .= "
        <td>
          <a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldorder=$new_order")."\">
          <img alt=\"$l_up\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_arrow_up\" /></a></td>";
      }
      $block .= "
      </tr>
      </table>   
    </td>
  </tr>";
      $i++;
    } // While end

    $block .= "
    </table>";

    // If help is set, we display the help panel
    if ($this->pref_dis_help == 1) {
      $block .= $this->dis_pref_help();
    }

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preference help screen
  /////////////////////////////////////////////////////////////////////////////
  function dis_pref_help() {
    global $l_help, $l_help_display_yes, $l_help_display_no, $l_help_mandatory, $l_help_down, $l_help_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;

    $block = "<hr />
      <table class=\"resultPref\">
      <tr>
        <td class=\"resultHead\" colspan=\"2\">
        $l_help</td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_ball\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_display_yes
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_crow\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_display_no
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
         <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_forbidden\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_mandatory
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
         <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_arrow_down\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_down
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_arrow_up\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_up
        </td>
      </tr>
    </table>";

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Display: Returns the dataset display
  // Parameters:
  //   - $dis_data_entity : function to call
  /////////////////////////////////////////////////////////////////////////////
  function dis_data($dis_data_entity) {
    global $path, $cgp_hide, $cdg_id, $set_debug, $set_theme, $set_rows;

    $link_ok = $this->display_link;
    $module = $this->display_module;
    $entity = $this->display_entity;
    if ($entity == "") {
      $entity = $module;
    }
    $block .= "<div class=\"result\">";

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != "")) {
      $block_page = $this->dis_data_index($this->data_url, $dis_data_entity);
      $block .= $block_page;
    }

    $block .= $this->data_form_head;
    $block .= "<table class=\"resultTable\">";
    $block_header = $this->dis_data_header($this->data_url);
    $block .= $block_header;

    reset($this->display_pref);
    $cpt = 0;

    // Iteration on each data row of dataset
    while ( ($this->data_set->next_record() )
            && ( ($cpt < $set_rows) || ( ! $this->data_page) ) ) {

      if (function_exists($dis_data_entity)) {
	$res = $dis_data_entity($this, "row_style", $link_ok);
	$rowstyle = $res["style"];
      }	  
      
      $cpt++;
      $block .= "<tr $rowstyle>";

      // If debug level Id is set, display the entity id
      if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
	$dg_id = $this->data_set->f("id");
	$block .= "<td class=\"resultText\">$dg_id</td>";
      }

      if ($rowstyle == "")
	$style = "class=\"resultText\"";

      // If a left checkbox has to be displayed
      if (($this->data_cb_side == "left") && ($this->data_cb_text != "")) {
	$col_show = $this->data_cb_show;
        // If cb are to be displayed or column telling visibility is true
        if (($col_show == "1") || ($this->data_set->f($col_show))) {
	  $name = $this->data_cb_name;
	  $name .= $this->data_set->f($this->data_idfield);
	  if ($this->data_set->f($this->data_cb_field)) {
	    $cb_check = "checked";
	  } else {
	    $cb_check = "";
	  }
	  if (function_exists($dis_data_entity)) {
	    $data_element = $dis_data_entity($this, "data_element", $link_ok);
	    $data_element = $data_element["name"];
	  }	  
	  $block .= "<td $style>
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check />$data_element </td>";
	}
	else
	  $block .= "<td $style>&nbsp;</td>";
      }


      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {
	// Default values

	if (! $cgp_hide[$entity][$fieldname]) {

	  $name = $this->data_set->f($fieldname);
	  $url = "";
	  $popup = false;
	  $popup_width = 600;
	  $popup_height = 400;
	  $align = "left";

	  //--------------------------------------------------
	  // Global field display disposition
	  //--------------------------------------------------
	  // Format dates
	  if (substr($fieldname,0,5) == "date_") {
	    $name = date_format($this->data_set->f($fieldname));
	  }
	  elseif ($fieldname == "timeupdate") {
	    $name = datetime_format($this->data_set->f($fieldname));
	  }
	  else if ($fieldname == "timecreate") {
	    $name = datetime_format($this->data_set->f($fieldname));
	  }
	  
	  //--------------------------------------------------
	  // We call module specific field display disposition
	  //--------------------------------------------------
	  if (function_exists($dis_data_entity)) {
	    $res = $dis_data_entity($this, $fieldname, $link_ok);
	    if ($res["name"] != "") $name = $res["name"];
	    $url = $res["url"];
	    $align = $res["align"];
	    $style = $res["style"];
	    $window = $res["window"];
	    $popup = $res["popup"];
	    $popup_width = $res["popup_width"];
	    $popup_height = $res["popup_height"];
	  }

	  //--------------------------------------------------
	  // Column display
	  //--------------------------------------------------
	  $content = "";
	  
	  // Set the style
	  if ($rowstyle != "") {
	    $td = "<td>";
	  } else if ($style != "") {
	    $td = "<td $style>";
	  } else if ($align == "center") {
	    $td = "<td class=\"resultTextC\">";
	  } else {
	    $td = "<td class=\"resultText\">";
	  }
	  
	  // Pre cell display (open popup, link)
	  if ($popup) {
	    $content = "<a class=\"resultText\" href=\"\" onclick=\"window.name='root';window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=no,toolbar=no,menubar=no,resizable=no'); return false;\">";
	  } else if ($window) {
	    $content = "<a class=\"resultText\" href=\"\" onclick=\"window.name='root';window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes'); return false;\">";
	  } else if ($url != "") {
	    $content = "<a class=\"resultText\" href=\"".url_prepare("$url")."\">";
	  }
	
	  // Cell display
	  $content .= $name;
	  
	  // Post cell display (close link, popup)
	  if ($popup || ($url != "")) {
	    $content .= "</a>";
	  }
	  
	  // Cell writing
	  $block .= "$td $content </td>\n";
	}
      }

      // If a right checkbox has to be displayed
      if (($this->data_cb_side == "right") && ($this->data_cb_text != "")) {
	$col_show = $this->data_cb_show;
        // If cb are to be displayed or column telling visibility is true
        if (($col_show == "1") || ($this->data_set->f($col_show))) {
	  $name = $this->data_cb_name;
	  $name .= $this->data_set->f($this->data_idfield);
	  if ($this->data_set->f($this->data_cb_field)) {
	    $cb_check = "checked";
	  } else {
	    $cb_check = "";
	  }
	  $block .= "<td class=\"resultText\">
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  $block .= "<td class=\"resultText\">&nbsp;</td>";
      }

      reset($this->display_pref);
      $block .= "</tr>";
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    if ($this->data_header == "both") {
      $block .= $block_header;
    }
    $block .= "</table>
      " . $this->data_form_end . "
      </div>";
    if ($this->data_header == "both") {
      $block .= $block_page;
    }

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Display : Returns the dataset index display (page management)
  // Parameters:
  //   - $url             : URL with search fields to link to index pages
  //   - $dis_data_entity : function to call (module formatting fields)
  /////////////////////////////////////////////////////////////////////////////
    function dis_data_index($url, $dis_data_entity) {
    global $path, $page, $new_order, $order_dir, $entity, $ctu_sql_limit;
    global $cak_prev, $cak_next, $cak_page_prev, $cak_page_next, $cak_begin, $cak_end;
    global $set_theme, $set_rows, $set_rows_default;
    global $l_entries, $l_page_res, $l_export, $l_export_all;
    global $ico_export, $ico_export_all, $ico_begin, $ico_end, $ico_previous, $ico_next;

    $nb_page_dis = 10;
    $this_entity = $this->display_entity;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    // Page display management
    // If page not set or page not for this display (this entity)
    if ((!isset($page)) || ($page == "") || ($entity != $this_entity)) {
      $lpage = 1;
    } else {
      $lpage = $page;
    }

    if ($set_rows < 1) $set_rows = $set_rows_default;
    $nb_rows = $this->data_set->num_rows_total();
    // If we delete all contacts of the last page, we must go to $page-1 (list)
    if ($lpage > 1) {
      if ($nb_rows < (($lpage-1)*$set_rows)+1) {
         $lpage-- ;
      }
    }

    $nb_page = floor(($nb_rows-1) / $set_rows) + 1;
    $first_row = ($lpage - 1) * $set_rows;
    $start_page =  (floor(($lpage - 1) / $nb_page_dis) * $nb_page_dis) + 1;
    $end_page = $start_page + $nb_page_dis - 1;
    $end_page = min ($end_page, $nb_page); 

    $content = "<div class=\"resultIndex\">$l_entries # $nb_rows &nbsp; &nbsp; &nbsp; $l_page_res : ($nb_page) &nbsp;&nbsp;&nbsp;&nbsp;";

    // Rewind to 1st page
    if ($lpage > 1) {
      $content .= "<a accesskey=\"$cak_begin\" href=\"$url&amp;page=1&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_begin\" class=\"resultIndexIcon\" alt=\"|<\"></a>&nbsp;&nbsp;";
    }

    // Rewind nb_page_dis page
    if ($start_page > 1) {
      $i = $start_page - $nb_page_dis;
      $content .= "<a accesskey=\"$cak_page_prev\" href=\"$url&amp;page=$i&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\">-$nb_page_dis</a>&nbsp;&nbsp;";
    }

    // Previous page
    if ($lpage > 1) {
      $tmppage = $lpage - 1;
      $content .= "<a accesskey=\"$cak_prev\" href=\"$url&amp;page=$tmppage&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_previous\" class=\"resultIndexIcon\" alt=\"|<\"></a>&nbsp;&nbsp;";
    }

    $i = $start_page;
    // Pages display
    while ($i <= $end_page) {
      if ($i != $lpage) {
	$content .= "<a href=\"$url&amp;page=$i&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\">$i</a> | \n";
      } else {
	$content .= "$i | \n";
      }
      $i++;
    }

    // Next page
    if ($lpage < $nb_page) {
      $tmppage = $lpage + 1;
      $content .= "&nbsp;&nbsp;<a accesskey=\"$cak_next\" href=\"$url&amp;page=$tmppage&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_next\" class=\"resultIndexIcon\" alt=\"|<\"></a>";
    }

    // Go to next $nb_page_dis pages
    if ($end_page < $nb_page) {
      $i = $end_page + 1;
      $content .= "&nbsp;&nbsp;<a accesskey=\"$cak_page_next\" href=\"$url&amp;page=$i&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\">+$nb_page_dis</a>";
    }

    // Go to last page
    if ($lpage < $nb_page) {
      $content .= "&nbsp;&nbsp;
      <a accesskey=\"$cak_end\" href=\"$url&amp;page=$nb_page&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_end\" class=\"resultIndexIcon\" alt=\">| $nb_page\"></a>";
    }

    // Export - Uses forms to allow big queries to be transmitted
    $module = $this->display_module;
    $query = $this->data_set->obm_query;
    $export_url = "$path/exportcsv/exportcsv_index.php";

    $content .= "&nbsp;&nbsp;&nbsp;&nbsp;
      <form style=\"display: inline;\" name=\"export_page\" method=\"post\" action=\"$export_url\">
      <input type=\"hidden\" name=\"action\" value=\"export_page\" />
      <input type=\"hidden\" name=\"call_module\" value=\"$module\" />
      <input type=\"hidden\" name=\"entity\" value=\"$this_entity\" />
      <input type=\"hidden\" name=\"first_row\" value=\"$first_row\" />
      <input type=\"hidden\" name=\"nb_rows\" value=\"$set_rows\" />
      <input type=\"hidden\" name=\"query\" value=\"$query\" />
      <input type=\"hidden\" name=\"func_data\" value=\"$dis_data_entity\" />
      <input type=\"image\" style=\"border: 0px; vertical-align: bottom;\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_export\" alt=\"$l_export\" />
      </form>

      <form style=\"display: inline;\" name=\"export_page\" method=\"post\" action=\"$export_url\">
      <input type=\"hidden\" name=\"action\" value=\"export_page\" />
      <input type=\"hidden\" name=\"call_module\" value=\"$module\" />
      <input type=\"hidden\" name=\"entity\" value=\"$this_entity\" />
      <input type=\"hidden\" name=\"first_row\" value=\"0\" />
      <input type=\"hidden\" name=\"nb_rows\" value=\"$nb_rows\" />
      <input type=\"hidden\" name=\"query\" value=\"$query\" />
      <input type=\"hidden\" name=\"func_data\" value=\"$dis_data_entity\" />
      <input type=\"image\" style=\"border: 0px; vertical-align: bottom;\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_export_all\" alt=\"$l_export_all\" />
      </form>
    </div>
";

    // If limit is not used, we seek to the first row to display
    if (! $ctu_sql_limit) {
      $this->data_set->seek($first_row);
    }

    return $content;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Display : Returns the dataset header display (Columns names)
  // Parameters:
  //   - $url : URL with search fields to link to index pages
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header($url) {
    global $page, $new_order, $order_dir;
    global $fieldnames, $cgp_hide, $cdg_id, $set_debug;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    $module = $this->display_module;
    $entity = $this->display_entity;
    if ($entity == "") {
      $entity = $module;
    }
    $block = "
    <tr>";
    
    // If debug level Id is set, display the entity id
    if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
      $block .= "<td class=\"resultHead\">Id</td>";
    }

    // Display the checkbox title if needed (exists and left side)
    if (($this->data_cb_side == "left")
          && ($this->data_cb_text != "")) {
      $block .= "<td class=\"resultHead\">".$this->data_cb_text."</td>";
    }

    reset($this->display_pref);
    while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {

      if (! $cgp_hide[$entity][$fieldname]) {
	$label = $fieldnames[$fieldname];
	$order = ($order_dir == "DESC" ? "ASC" : "DESC");

	$block .= "
        <td class=\"resultHead\">";
	if ($this->data_order) {
	  $block .= "<a class=\"resultHead\" href=\"".$url.
	    (($url != "") ? "&amp;page=$page" : "" ) .
	    "&amp;new_order=$fieldname&amp;order_dir=$order&amp;entity=$entity\">";
	}
	$block .= $label;
	if ($this->data_order) {
	  $block .= "</a>";
	}
	$block .= "</td>";
      }
    }

    // Display the checkbox title if needed (exists and right side)
    if (($this->data_cb_side == "right")
         && ($this->data_cb_text != "")) {
      $block .= "<td class=\"resultHead\">".$this->data_cb_text."</td>";
    }
    $block .= "</tr>";

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  // for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header_file($sep) {
    global $cgp_hide, $fieldnames, $cdg_id, $set_debug;

    $module = $this->display_module;
    $entity = $this->display_entity;
    if ($entity == "") {
      $entity = $module;
    }

    reset($this->display_pref);
    while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {
      if (! $cgp_hide[$entity][$fieldname]) {
	$label = $fieldnames[$fieldname];
	// if label empty or is an image or a link, we display the column name
	if ( ($label == "") || (preg_match("/.*<.*>.*/", $label) ) ) {
	  $label = $fieldname;
	}
	echo "$label$sep";
      }
    }
    echo "\n";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_file($first_row, $nb_rows, $sep, $dis_data_entity) {
    global $cgp_hide, $cdg_id, $set_debug, $ctu_sql_limit;

    $module = $this->display_module;
    $entity = $this->display_entity;
    if ($entity == "") {
      $entity = $module;
    }
    $this->dis_data_header_file($sep);

    // If limit is not used, we seek to the first row to display
    if (! $ctu_sql_limit) {
      $this->data_set->seek($first_row);
    }

    // Iteration on each data row of dataset
    $cpt = 0;
    while (($this->data_set->next_record()) && ($cpt < $nb_rows)) {

      // For each field in the data row we walk through the pref array
      // and for each entry (field) we set special parameters and display it
      reset($this->display_pref);
      while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {

	if (! $cgp_hide[$entity][$fieldname]) {
	  $txt_name = $this->data_set->f($fieldname);

	  //--------------------------------------------------
	  // Global field display disposition
	  //--------------------------------------------------
	  // Format dates
	  if (substr($fieldname,0,5) == "date_") {
	    $txt_name = date_format($this->data_set->f($fieldname));
	  }
	  elseif ($fieldname == "timeupdate") {
	    $txt_name = datetime_format($this->data_set->f($fieldname));
	  }
	  else if ($fieldname == "timecreate") {
	    $txt_name = datetime_format($this->data_set->f($fieldname));
	  }
	  
	  //--------------------------------------------------
	  // We call module specific field display disposition
	  //--------------------------------------------------
	  if (function_exists($dis_data_entity)) {
	    $res = $dis_data_entity($this, $fieldname, $link_ok);
	  }	  

	  if ($res["txt_name"] != "") {
	    $name = $res["txt_name"];
	  } else if ($res["name"] != ""){
	    $name = $res["name"];
	  } else {
	    $name = $txt_name;
	  }
	  $delimiter = $this->delimiter;
	  // If double quote in field, we double these and set field delimiter
	  if ( (($delimiter == '"') || ($delimiter == ''))
               && (strstr($name, '"'))) {
	    $name = str_replace('"', '""', $name);
	    $delimiter = '"';
	  } else if (($sep != '') && (strstr($name, $sep))) {
	    if ($delimiter == "") {
	      $delimiter = '"';
	    }
	  }
	  echo "${delimiter}${name}${delimiter}$sep";

	}
      }

      echo "\n";
      $cpt++;
    }
    echo "\n\n";
  }

}


// Themes generic display functions -----------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Head
// Parameters:
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_head($p_module) { 
  global $l_obm_title, $set_theme, $extra_css, $extra_js, $extra_file_js;

  // If a theme specif function is provided
  if (function_exists("display_head_theme")) {
    return display_head_theme($p_module);
  }

  // Else
  if (isset($extra_css)) {
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"".C_IMAGE_PATH."/$set_theme/$extra_css\" />";
  }

  $headers_e = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  //  if ($extra_file_js) {
  //    $file_js = "<script language=\"JavaScript\" type=\"text/javascript\" src=\"$extra_file_js\" />";
  //  }

  return "$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />
  <title>$l_obm_title - $p_module</title>
  <link rel=\"stylesheet\" type=\"text/css\" href=\"".C_IMAGE_PATH."/$set_theme/style.css\" />
  $dis_extra_css
  $file_js
<script type=\"text/javascript\">
  $extra_js
</script>
</head>

<body onload=\"if (document.f_search) { document.f_search.submit.focus();} \">
";
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {
  global $cgp_show, $sections, $perm;
  
  // If a theme specif function is provided
  if (function_exists("display_sections_theme")) {
    return display_sections_theme($p_module);
  }

  // Else
  $ret = "<div class='section'>\n";
  foreach ($cgp_show["section"] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value["Name"];
      $section_right = $perm->get_section_rights($key);
      if (($section_right & $value["Right"]) == $value["Right"]) {
        if ($key == $section) {
          $ret .= "<p class='actif'>$section_name</p>\n";
        } else {
	  $ret .= "<p class='inactif'><a href='".$value["Url"]."'>$section_name</a></p>\n";
        }
      }
    }
  }
  $ret .= "</div>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section menus display block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_modules($section, $module) {
  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $perm, $modules, $cgp_show;

  // If a theme specif function is provided
  if (function_exists("display_modules_theme")) {
    return display_modules_theme($section, $module);
  }

  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $modules, $perm, $cgp_show;

  if (($set_menu == $cme_ico) || ($set_menu == $cme_both))
    $fico = true;
  else $fico = false;

  if (($set_menu == $cme_txt) || ($set_menu == $cme_both))
    $ftext = true;
  else $ftext = false;

  if ($section != "") {
    $section_link = array_keys($cgp_show["module"],$section);
    if (is_array($section_link)) {
      $block .= "<ul class='module'>\n";

      foreach ($section_link as $mod) {
        $module_name = $modules[$mod]["Name"];
        $module_right = $perm->get_module_rights($mod); 
        if (($module_right & $modules[$mod]["Right"]) == $modules[$mod]["Right"]) {
          if ($mod == $module) {
            $block .= "<li class=\"activeModule\">";
	  } else {
	    $block .= "<li class=\"inactiveModule\">";
	  }
          if ($fico) {
	    $ico = $modules[$mod]["Ico"];
	    if ($ico) {
	      $block .= "<a href='".$modules[$mod]["Url"]."' class='ico'><img src='".C_IMAGE_PATH."/".$set_theme."/".$ico."' alt='$module_name' /></a> ";
	    }
	  }
	  $block .= "<a href='".$modules[$mod]["Url"]."'>";
	  if ($ftext) $block .= $module_name;
          $block .= "</a></li>\n";
        }
      }
      $block .= "</ul>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the action display block of the selected module
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions($section, $module) {
  global $set_theme, $modules, $action, $popup_height;
  global $actions, $popup_width, $perm;

  // If a theme specif function is provided
  if (function_exists("display_actions_theme")) {
    return display_actions_theme($section, $module);
  }

  $name = $modules[$module]["Name"];
  $ico = $modules[$module]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico\" alt=\"\" />&nbsp;";
  else
    $dico = "";

  $ret = "
  <ul class=\"action\">
  ";
  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module);

    foreach ($actions[$module] as $key => $value) {

      // If the current action has this action in target
      if (in_array("all", $value["Condition"])
	  || in_array($action, $value["Condition"])) {

	if (($module_right & $value["Right"]) == $value["Right"]) {

	  $ret .= "<li>";
	  if ($value["Popup"] == 1) {
	    if ($value["Target"]) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $ret .= "
              <a href=\"\" class=\"action\" 
              onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
	  } else {
	    $ret .= "
               <a class=\"action\" href=\"".$value["Url"]."\">".$value["Name"]."</a> &nbsp;";
	  }
	  $ret .= "</li>";
	}
      }
    }
  }
   
  $ret .= "
  </ul>
  ";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_last_visited($path="..") {
  global $path, $cgp_show, $last_v, $set_theme, $l_last_visit;

  // If a theme specif function is provided
  if (function_exists("display_last_visited_theme")) {
    return display_last_visited_theme($path);
  }

  foreach ($cgp_show["module"] as $module => $val) {

    if (($cgp_show["module"][$module]) && ($last_v[$module]["text"] != "")) {
      $id = $last_v[$module]["id"];
      $text = substr($last_v[$module]["text"], 0, 30);
      $l_ico = "ico_$module";
      global ${$l_ico};
      $mod_url = url_prepare("$path/$module/${module}_index.php?action=detailconsult&amp;param_${module}=$id");
      $dis_last .= "
        <li class=\"blockItem; lastItem\"><a href=\"$mod_url\"><img src=\"".C_IMAGE_PATH."/$set_theme/${$l_ico}\" alt=\"\" /></a>
          <a href=\"$mod_url\">$text</a></li>";
    }
  }

  $block .= "
  <div class=\"block\" class=\"last\"> 
  <div class=\"blockTitle\" class=\"lastTitle\">:: $l_last_visit</div>
    <ul class=\"blockItem\" class=\"lastItem\">
    $dis_last
    </ul>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the Todo display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_todos($path="..") {
  global $cgp_show, $todos, $l_module_todo;

  if (! $cgp_show["module"]["todo"]) {
    return "";
  }

  // If a theme specif function is provided
  if (function_exists("display_todos_theme")) {
    return display_todos_theme($path);
  }

  $nb = count($todos);

  for ($i = 1; $i <= $nb; $i++) {
    $t_id = $todos[$i]["id"];
    $t_title = $todos[$i]["title"];
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=$t_id");
    $todo_title = substr($t_title, 0, 30);
    $block_todo .= "
      <li class=\"blockItem\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($nb >= 1) {
    $block = "
    <div class=\"block\"> 
    <div class=\"blockTitle\">:: $l_module_todo</div>
      <ul class=\"blockItem\">
      $block_todo
     </ul>
    </div>";
  } else {
    $block = "";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : end of an HTML page
///////////////////////////////////////////////////////////////////////////////
function display_end() { 
  return "
    </body>
    </html>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display : title
///////////////////////////////////////////////////////////////////////////////
function display_title($title) { 

  return "<div class=\"title\">$title</div>";
}


///////////////////////////////////////////////////////////////////////////////
// Display an OK message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageOk\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display an Info message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_info_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageInfo\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display a warning message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg, $special=true) {
  
  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageWarning\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display an error message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageError\">$msg</p>";
  }
  
  return $ret;
}


?>
