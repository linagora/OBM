<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_display.inc                                           //
//     - Desc : Global Display File (OBM_DISPLAY)                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

class OBM_DISPLAY {
  var $display_type;      // determines the type of display. possible values
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
  var $separator = ";";       // for file generation : .csv by default

  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY Constructor
  // Parameters:
  //   - $type : display type
  //   - $pref : display pref : DBO
  /////////////////////////////////////////////////////////////////////////////
  function OBM_DISPLAY($type, $pref) {
    if( ($type != "PREFERENCES") && 
	($type != "DATA") ) {
      die("ERROR: the type MUST be PREFERENCES or DATA");
    } else {
      $this->display_type = $type;
    }
    if(isset($pref)) {
      $this->display_pref = $pref;
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY main display function
  /////////////////////////////////////////////////////////////////////////////
  function display($dis_data_entity="") {
    if ($this->display_type == "PREFERENCES") {
      $this->check_preferences_vars();
      $this->dis_preferences();
    } else if ($this->display_type == "DATA") {
      $this->check_data_vars();
      $this->dis_data($dis_data_entity);
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
    if(!is_object($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
    if(!isset($this->display_entity)) {
      die("THE display_entity VARIABLE MUST BE SET");
    }
    if(!isset($this->pref_title)) {
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
    global $fieldnames;
    
    $nb_field = $this->display_pref->nf() - 1;

    // actions names
    $action_display = "dispref_display";
    $action_level = "dispref_level";

    if ($this->display_module != "") {
      $url = $this->display_module . "_index.php";
    } else {
      $url = $this->display_entity . "_index.php";
    }

    echo "
    <table class=\"OBMDisplayPref\" >
       <tr>
         <td class=\"OBMDisplayHead\" colspan=\"4\">
          ".$this->pref_title."</td>
       </tr>
       <tr>
         <td class=\"OBMDisplayLabel\">
            $l_displayed</td>
         <td class=\"OBMDisplayLabel\">
            $l_order</td>
        <td class=\"OBMDisplayLabel\">
            $l_field</td>
         <td class=\"OBMDisplayLabel\">
            $l_move</td>
       </tr>";

    $i = 0;
    $nb_field_hidden = 0;
    while ($this->display_pref->next_record()) {
      $entity = $this->display_entity;
      $fieldname = $this->display_pref->f("display_fieldname");
      $display = $this->display_pref->f("display_display");
      echo "
          <tr>
            <td class=\"OBMDisplayTextCentered\">";
      if ($display == 1) {
	echo "<a class=\"OBMDisplayText\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
        <img alt=\"$l_display_no\" src=\"/images/$set_theme/$ico_ball\" /></a>";
      } else if ($display == 0) {
	$nb_field_hidden++;
	echo "<a class=\"OBMDisplayTextCentered\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
          <img alt=\"$l_display_yes\" src=\"/images/$set_theme/$ico_crow\" /></a>";
      } else if ($display == 2) {
	echo "<img alt=\"$l_mandatory\" src=\"/images/$set_theme/$ico_forbidden\" />";
      }
      echo "
            </td>
            <td class=\"OBMDisplayTextCentered\">
              ";
      if($display == 0) {
	echo "&nbsp;";
      } else {
	$pos = $this->display_pref->f("display_fieldorder") - $nb_field_hidden;
	echo "$pos";
      }
      echo "
             </td>
            <td class=\"OBMDisplayTextCentered\">
                   ".$fieldnames[$fieldname]."
            </td>
            <td class=\"OBMDisplayText\">
              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">
                <tr>";

      // If last field we doesn't display the down arrow
      if ($i == $nb_field) {
	echo "<td>&nbsp;</td>";
      } else {
	echo "<td><a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=down&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
           <img alt=\"$l_down\" src=\"/images/$set_theme/$ico_arrow_down\" /></a></td>";
      }

      // If first field we doesn't display the up arrow
      if ($i == 0) {
	echo "<td >&nbsp;</td>";
      } else {
	echo "<td>
              <a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=up&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
            <img alt=\"$l_up\" src=\"/images/$set_theme/$ico_arrow_up\" /></a></td>";
      }
      echo "
                </tr>
              </table>   
            </td>
          </tr>";
      $i++;
    }
    echo "
        </table>";

    // If help is set, we display the help panel
    if($this->pref_dis_help == 1) {
      $this->dis_pref_help();
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preference help screen
  /////////////////////////////////////////////////////////////////////////////
  function dis_pref_help() {
    global $l_help, $l_help_display_yes, $l_help_display_no, $l_help_mandatory, $l_help_down, $l_help_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;

    echo "<hr />
      <table class=\"OBMDisplayPref\">
      <tr>
        <td class=\"OBMDisplayHead\" colspan=\"2\">
        $l_help</td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_ball\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_display_yes
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_crow\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_display_no
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
         <img src=\"/images/$set_theme/$ico_forbidden\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_mandatory
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
         <img src=\"/images/$set_theme/$ico_arrow_down\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_down
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_arrow_up\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_up
        </td>
      </tr>
    </table>";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  // Parameters:
  //   - $dis_data_entity : function to call
  /////////////////////////////////////////////////////////////////////////////
  function dis_data($dis_data_entity) {
    global $cdg_id, $set_debug, $set_theme, $set_rows;
    global $path, $time;

    $link_ok = $this->display_link;

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != "")) {
      $this->dis_data_index($this->data_url);
    }

    echo "<table class=\"OBMDisplayData\">";
    $this->dis_data_header($this->data_url);

    $this->display_pref->seek(0);
    $cpt = 0;

    // Iteration on each data row of dataset
    while ( ($this->data_set->next_record() )
            && ( ($cpt < $set_rows) || ( ! $this->data_page) ) ) {
      $cpt++;
      echo "<tr>";

      // If debug level Id is set, display the entity id
      if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
	$dg_id = $this->data_set->f("Id");
	echo "<td class=\"OBMDisplayText\">$dg_id</td>";
      }

      // If a left checkbox has to be displayed
      if (($this->data_cb_side == "left") && ($this->data_cb_text != "")){
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
	  echo "<td class=\"OBMDisplayText\">
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  echo "<td class=\"OBMDisplayText\">&nbsp;</td>";
      }


      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ($this->display_pref->next_record()) {
	$name = "";
	$url = "";
	$popup = false;
	$popup_width = 600;
	$popup_height = 400;
	$align = "left";
	$fieldname = $this->display_pref->f("display_fieldname");

        //--------------------------------------------------
        // Global field display disposition
        //--------------------------------------------------
	// Format dates
	if (substr($fieldname,0,5) == "date_") {
	  $name =date_format($this->data_set->f($fieldname));
	}
	elseif ($fieldname == "timeupdate") {
	  $name = datetime_format($this->data_set->f($fieldname));
	}
	else if ($fieldname == "timecreate") {
	  $name = datetime_format($this->data_set->f($fieldname));
	}


        //------------------------------
        // For the time management
        //------------------------------
        else if (($fieldname == "task_id")
                  && ($this->data_set->f("task_status")==0) ) {
	  $popup = true;
	  $popup_width = 970;
	  $popup_height = 270;
	  $url = "time_index.php?action=detailupdate&amp;popup=1&amp;task_id=".$this->data_set->f("task_id")."&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"];
	  $name = "<img border=0 src=\"/images/$set_theme/modify.png\">";
	}
	else if ($fieldname == "task_id")
           $name = " ";
 


        //--------------------------------------------------
        // We call module specific field display disposition
        //--------------------------------------------------
	if (function_exists($dis_data_entity)) {
	  $res = $dis_data_entity($this, $fieldname, $link_ok);
	  $align = $res["align"];
	  if ($res["name"] != "") $name = $res["name"];
	  $url = $res["url"];
	  $style = $res["style"];
	} else {
	  $name = $this->data_set->f($fieldname);
	}	  

	// Set the style
	if ($style != "") {
	  $td = "<td $style>";
	} elseif ($align == "center") {
	  $td = "<td class=\"OBMDisplayTextCentered\">";
	} else {
	  $td = "<td class=\"OBMDisplayText\">";
	}
        $content = "";
	
	// Pre cell display (open popup, link)
	if ($popup) {
	  $content = "<a class=\"OBMDisplayText\" href=\"\" OnClick=\"window.name='root';window.open('$url','',config='height=$popup_height,width=$popup_width,scrollbars=no,toolbar=no,menubar=no,resizable=no'); return false;\">";
	} else if ($url != "") {
	  $content = "<a class=\"OBMDisplayText\" href=\"".url_prepare("$url")."\">";
	}
	
	// Cell display
	if ($name == "") {
	  $content .= $this->data_set->f($fieldname);
	} else {
	  $content .= $name;
	}
	
	// Post cell display (close link, popup)
	if ($popup || ($url != "")) {
	  $content .= "</a>";
	}
	
	// Cell writing
	echo "$td $content </td>\n";
      }
      

      // If a left checkbox has to be displayed
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
	  echo "<td class=\"OBMDisplayText\">
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  echo "<td class=\"OBMDisplayText\">&nbsp;</td>";
      }

      $this->display_pref->seek(0);
      echo "</tr>";
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    if ($this->data_header == "both") {
      $this->dis_data_header($this->data_url);
    }
    echo "</table>";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset index (page management)
  // Parameters:
  //   - $url : URL with search fields to link to index pages
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_index($url) {
    global $page, $new_order;
    global $set_rows, $set_rows_default;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    // Page display management
    if (!isset($page)) {
      $page=0;
    }
    if ($set_rows < 1) $set_rows = $set_rows_default;
    $nb_list = $this->data_set->num_rows();
    // If we delete all contacts of the last page, we must go to $page-1
    if($page != 0) {
      if($nb_list < ($page*$set_rows)+1) {
         $page-- ;
      }
    }

    $nb_page = floor(($nb_list-1) / $set_rows);
    $first_row = $page * $set_rows;
    // skip if display possible on a single page if nb_page==0
    if ($nb_page > 0) {
    echo "<p class=\"messageInfo\">";
      // Table of content (pages)
      $i=0;
      while ($i <= ($nb_page)) {
        if ($i != 0) echo " | ";
        if ($i != $page) {
          echo "<a href=\"$url&amp;page=$i&amp;new_order=$new_order\">\n";
	}
	echo ($i + 1);
	if ($i != $page) echo "</a>\n";
	$i++;
      }
      echo "</p>";
      $this->data_set->seek($first_row);
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header($url) {
    global $page, $new_order, $order_dir;
    global $fieldnames, $cdg_id, $set_debug;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    $this->display_pref->seek(0);
    echo "
    <tr>";
    
    // If debug level Id is set, display the entity id
    if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
      echo "<td class=\"OBMDisplayHead\">Id</td>";
    }

    // Display the checkbox title if needed (exists and left side)
    if (($this->data_cb_side == "left")
          && ($this->data_cb_text != "")) {
      echo "<td class=\"OBMDisplayHead\">".$this->data_cb_text."</td>";
    }

    while($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");
      $label = $fieldnames[$fieldname];
      $order = ($order_dir == "ASC" ? "DESC" : "ASC");

      echo "
        <td class=\"OBMDisplayHead\">";
      if ($this->data_order) {
        echo "<a class=\"OBMDisplayHead\" href=\"".$url.
          (($url != "") ? "&amp;page=$page" : "" ) .
          "&amp;new_order=$fieldname&amp;order_dir=$order\">";
      }
      echo $label;
      if ($this->data_order) {
        echo "</a>";
      }

      echo "</td>";
    }

    // Display the checkbox title if needed (exists and right side)
    if (($this->data_cb_side == "right")
         && ($this->data_cb_text != "")) {
      echo "<td class=\"OBMDisplayHead\">".$this->data_cb_text."</td>";
    }
    echo "</tr>";
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

      echo " $label $sep ";
    }
    echo "\n";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_file($sep) {
    global $cdg_id, $set_debug;
    global $time;

    $this->dis_data_header_file($sep);

    //    echo "nb lignes : ". $this->data_set->num_rows() . "\n";
    // Iteration on each data row of dataset
    while ($this->data_set->next_record()) {
      $this->display_pref->seek(0);

      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ($this->display_pref->next_record()) {
	$fieldname = $this->display_pref->f("display_fieldname");
        echo $this->data_set->f($fieldname) . $sep;
      }

      echo "\n";
    }
    echo "\n\n\n";
    
  }


} // End of the object OBM_DISPLAY



?>
