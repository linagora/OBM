<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_display.inc                                           //
//     - Desc : Global Display File (OBM_DISPLAY)                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

class OBM_DISPLAY {
  var $display_type;     // determines the type of display. possible values
    // - PREFERENCES  : to set the display preference
    // - DATA         : to display all elements, for exemple all contacts
  var $display_module;   // module where to find the entity in use
  var $display_entity;   // entity used (deal, contact, parentdeal,...)
  var $display_pref;     // User preferences : DB_OBM : query from PrefDisplay
  var $display_link = 1; // If false, extern links aren't displayed

  //----------- TYPE SECTION : PREFERENCES --------//
  var $pref_title;       // title of the preference panel
  var $pref_dis_help;    // flag to tell if the pref help screen shoulg be dis.

  //----------- TYPE SECTION : DATA -----------//
  var $data_set;              // Data set :DB_OBM: data rows to be displayed
  var $data_header = "top";   // Table header location (top|both) (+bottom)
  var $data_page = "true";    // Page management (true | false)
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
  var $data_specialfield = "";// Special field to signal special treatments which
                              // haven't been perform here 
  var $separator = ";";       // for file generation : .csv by default

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
    if(!is_object($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if PREFERENCES variables are set, if not die        //
  /////////////////////////////////////////////////////////////////////////////
  function check_preferences_vars() {
    if (!is_object($this->display_pref)) {
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
    global $l_displayed, $l_order, $l_field, $l_move, $l_mandatory;
    global $l_display_no, $l_display_yes, $l_down, $l_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;
    global $fieldnames, $cgp_hide;
    
    $nb_field = $this->display_pref->nf() - 1;

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
      <td class=\"resultHead\" colspan=\"4\">".$this->pref_title."</td>
    </tr>
    <tr>
      <td class=\"resultLabel\">$l_displayed</td>
      <td class=\"resultLabel\">$l_order</td>
      <td class=\"resultLabel\">$l_field</td>
      <td class=\"resultLabel\">$l_move</td>
    </tr>";

    $i = 0;
    $nb_field_hidden = 0;
    while ($this->display_pref->next_record()) {
      $entity = $this->display_entity;
      $fieldname = $this->display_pref->f("display_fieldname");
      $disstatus = $this->display_pref->f("display_display");

      if ($cgp_hide[$entity][$fieldname]) {
	$td_class = "resultPrefHide";
      } else {
	$td_class = "resultTextC";
      }

      $block .= "
    <tr>
    <td class=\"$td_class\">";

      if ($disstatus == 0) {
	$nb_field_hidden++;
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;disstatus=$disstatus")."\">
      <img alt=\"$l_display_yes\" src=\"/images/$set_theme/$ico_crow\" /></a>";
      } else if ($disstatus == 1) {
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;disstatus=$disstatus")."\">
      <img alt=\"$l_display_no\" src=\"/images/$set_theme/$ico_ball\" /></a>";
      } else if ($disstatus == 2) {
	$block .= "
      <img alt=\"$l_mandatory\" src=\"/images/$set_theme/$ico_forbidden\" />";
      }

      $block .= "
      </td>
      <td class=\"$td_class\">";

      if ($disstatus == 0) {
	$block .= "&nbsp;";
      } else {
	$pos = $this->display_pref->f("display_fieldorder") - $nb_field_hidden;
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
	$block .= "
        <td><a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=down&amp;fieldorder=".$this->display_pref->f("display_fieldorder"))."\">
          <img alt=\"$l_down\" src=\"/images/$set_theme/$ico_arrow_down\" /></a></td>";
      }
      
      // If first field we doesn't display the up arrow
      if ($i == 0) {
	$block .= "
        <td >&nbsp;</td>";
      } else {
	$block .= "
        <td>
          <a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=up&amp;fieldorder=".$this->display_pref->f("display_fieldorder"))."\">
          <img alt=\"$l_up\" src=\"/images/$set_theme/$ico_arrow_up\" /></a></td>";
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
          <img src=\"/images/$set_theme/$ico_ball\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_display_yes
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
          <img src=\"/images/$set_theme/$ico_crow\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_display_no
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
         <img src=\"/images/$set_theme/$ico_forbidden\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_mandatory
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
         <img src=\"/images/$set_theme/$ico_arrow_down\" alt=\"\" />
        </td>
        <td class=\"resultText\">
          $l_help_down
        </td>
      </tr>
      <tr>
        <td class=\"resultLabel\">
          <img src=\"/images/$set_theme/$ico_arrow_up\" alt=\"\" />
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

    $block .= "<div class=\"result\">";

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != "")) {
      $block_page = $this->dis_data_index($this->data_url, $dis_data_entity);
      $block .= $block_page;
    }

    $block .= "<table class=\"resultTable\">";
    $block_header = $this->dis_data_header($this->data_url);
    $block .= $block_header;

    $this->display_pref->seek(0);
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
	$dg_id = $this->data_set->f("Id");
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

	  $block .= "<td $style>
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  $block .= "<td $style>&nbsp;</td>";
      }


      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ($this->display_pref->next_record()) {
	// Default values
	$fieldname = $this->display_pref->f("display_fieldname");

	if (! $cgp_hide[$module][$fieldname]) {

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

      $this->display_pref->seek(0);
      $block .= "</tr>";
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    if ($this->data_header == "both") {
      $block .= $block_header;
    }
    $block .= "</table>
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
    global $path, $page, $new_order, $order_dir, $entity;
    global $set_theme, $set_rows, $set_rows_default;
    global $l_entries, $l_page_res, $l_export, $l_export_all;
    global $ico_export, $ico_export_all, $ico_begin, $ico_end;

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
    $nb_rows = $this->data_set->num_rows();
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
      $content .= "<a href=\"$url&amp;page=1&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"/images/$set_theme/$ico_begin\" class=\"resultIndexIcon\" alt=\"|<\"></a>&nbsp;&nbsp;&nbsp;";
    }

    // Rewind nb_page_dis page
    if ($start_page > 1) {
      $i = $start_page - $nb_page_dis;
      $content .= "<a href=\"$url&amp;page=$i&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\">-$nb_page_dis</a>&nbsp;&nbsp;&nbsp;";
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

    // Go to next $nb_page_dis pages
    if ($end_page < $nb_page) {
      $i = $end_page + 1;
      $content .= "&nbsp;&nbsp;&nbsp;<a href=\"$url&amp;page=$i&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\">+$nb_page_dis</a>";
    }

    // Go to last page
    if ($lpage < $nb_page) {
      $content .= "&nbsp;&nbsp;
      <a href=\"$url&amp;page=$nb_page&amp;new_order=$new_order&amp;order_dir=$order_dir&amp;entity=$this_entity\"><img src=\"/images/$set_theme/$ico_end\" class=\"resultIndexIcon\" alt=\">| $nb_page\"></a>";
    }

    // Export
    $module = $this->display_module;
    $query = urlencode($this->data_set->obm_query);
    $query_pref = urlencode($this->display_pref->obm_query);
    $export_page_url = "$path/exportcsv/exportcsv_index.php?action=export_page&amp;call_module=$module&amp;first_row=$first_row&amp;nb_rows=$set_rows&amp;query=$query&amp;query_pref=$query_pref&amp;func_data=$dis_data_entity";
    $export_all_url = "$path/exportcsv/exportcsv_index.php?action=export_page&amp;call_module=$module&amp;first_row=0&amp;nb_rows=$nb_rows&amp;query=$query&amp;query_pref=$query_pref&amp;func_data=$dis_data_entity";

    $content .= "&nbsp;&nbsp;&nbsp;&nbsp;
      <a href=\"$export_page_url\"><img src=\"/images/$set_theme/$ico_export\" class=\"resultIndexIcon\" alt=\"$l_export\"/></a>
      <a href=\"$export_all_url\"><img src=\"/images/$set_theme/$ico_export_all\" class=\"resultIndexIcon\" alt=\"$l_export_all\"/></a>
    </div>
";

    $this->data_set->seek($first_row);

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

    $this->display_pref->seek(0);
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

    while ($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");

      if (! $cgp_hide[$module][$fieldname]) {
	$label = $fieldnames[$fieldname];
	$order = ($order_dir == "ASC" ? "DESC" : "ASC");

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
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header_file($sep) {
    global $fieldnames, $cdg_id, $set_debug;

    $this->display_pref->seek(0);
    
    while ($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");
      $label = $fieldnames[$fieldname];
      // if label empty or is an image or a link, we display the column name
      if ( ($label == "") || (preg_match("/.*<.*>.*/", $label) ) ) {
	$label = $fieldname;
      }
      echo "$label$sep";
    }
    echo "\n";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
    function dis_data_file($first_row, $nb_rows, $sep, $dis_data_entity) {
    global $cdg_id, $set_debug;

    $this->dis_data_header_file($sep);

    $this->data_set->seek($first_row);

    // Iteration on each data row of dataset
    $cpt = 0;
    while (($this->data_set->next_record()) && ($cpt < $nb_rows)) {

      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      $this->display_pref->seek(0);
      while ($this->display_pref->next_record()) {
	$fieldname = $this->display_pref->f("display_fieldname");
        //--------------------------------------------------
        // We call module specific field display disposition
        //--------------------------------------------------
	if (function_exists($dis_data_entity)) {
	  $res = $dis_data_entity($this, $fieldname, $link_ok);
	}	  
	if ($res["txt_name"] != "") {
	  echo $res["txt_name"] . $sep;
	} else {
	  echo $this->data_set->f($fieldname) . $sep;
	}
      }

      echo "\n";
      $cpt++;
    }
    echo "\n\n";
  }

}

?>
