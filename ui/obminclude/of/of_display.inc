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
  var $display_name;      // Display Name (to differentiate same displays)
  var $display_module;    // module where to find the entity in use
  var $display_entity;    // entity used (deal, contact, parentdeal,...)
  var $display_pref;      // User preferences : Array[fieldname] order,status
  var $display_link = 1;  // If false, extern links aren't displayed
  var $display_ext = '';  // External action called (allow Special handling)

  //----------- TYPE SECTION : PREFERENCES --------//
  var $pref_title;       // title of the preference panel
  var $pref_dis_help;    // flag to tell if the pref help screen shoulg be dis.

  //----------- TYPE SECTION : DATA -----------//
  var $data_set;              // Data set :DB_OBM: data rows to be displayed
  var $data_header = 'top';   // Table header location (top|both) (+bottom)
  var $data_page = true;      // Page management (true | false)
  var $data_url = '';         // Base URL (with search) used for links in Index
  var $data_order = true;     // Order can be changed (click on column title)
  var $data_cb_text = '';     // Checkbox column text
  var $data_cb_side = 'left'; // Checkbox position in the row (left|right)
  var $data_cb_name = '';     // Base name of the checkbox element (+id)
  var $data_cb_field = '';    // Field from which cb is filled (1=selected)
  var $data_idfield = '_id';  // Index field for the data row
  var $data_cb_show = 1;      // To set visible
                              //   0 -> not visible (else visible)
                              //   'string' if ->f['string'] == 0 -> visible
  var $data_form_head = '';   // Form header
  var $data_form_end = '';    // Form end
  var $delimiter = '"';       // CVS field delimiter (enclosed by)
  var $sql_order_dir = 'ASC'; // SQL Sorting direction
  var $dis_data_footer = null;
  var $footer_data = array();

  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY Constructor
  // Parameters:
  //   - $type   : display type
  //   - $pref   : display pref : DBO
  //   - $module : calling module
  //   - $entity : entity to display
  //   - $name   : dataset name
  /////////////////////////////////////////////////////////////////////////////
  function OBM_DISPLAY($type, $pref, $module, $entity='', $name='') {
    global $params;

    if ( ($type != 'PREFERENCES') && 
	($type != 'DATA') ) {
      die('ERROR: the type MUST be PREFERENCES or DATA');
    } else {
      $this->display_type = $type;
    }
    if (isset($pref)) {
      $this->display_pref = $pref;
    }
    if (isset($module)) {
      $this->display_module = $module;
    }
    if (isset($entity) && ($entity != '')) {
      $this->display_entity = $entity;
    } else {
      $this->display_entity = $this->display_module;
    }
    if (isset($name)) {
      $this->display_name = $name;
    }
    $this->sql_order_dir = ($params['sql_order_dir'] == 'DESC' ? 'DESC' : 'ASC');
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY main display function
  /////////////////////////////////////////////////////////////////////////////
  function display($dis_data_entity='') {
    if ($this->display_type == 'PREFERENCES') {
      $this->check_preferences_vars();
      return $this->dis_preferences();
    } else if ($this->display_type == 'DATA') {
      $this->check_data_vars();
      return $this->dis_data($dis_data_entity);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if DATA variables are set, if not die               //
  /////////////////////////////////////////////////////////////////////////////
  function check_data_vars() {
    if (!is_array($this->display_pref)) {
      die('THE display_pref VARIABLE MUST BE SET');
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if PREFERENCES variables are set, if not die        //
  /////////////////////////////////////////////////////////////////////////////
  function check_preferences_vars() {
    if (! is_array($this->display_pref)) {
      die('THE display_pref VARIABLE MUST BE SET');
    }
    if (!isset($this->display_entity)) {
      die('THE display_entity VARIABLE MUST BE SET');
    }
    if (!isset($this->pref_title)) {
      die('THE pref_title VARIABLE MUST BE SET');
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preferences screen for the dataset            //
  /////////////////////////////////////////////////////////////////////////////
  function dis_preferences() {
    global $l_default, $l_displayed, $l_order, $l_field, $l_move, $l_mandatory;
    global $l_display_no, $l_display_yes, $l_down, $l_up;
    // Images
    global $ico_arrow_mount, $ico_arrow_umount, $ico_nok;
    global $fieldnames, $cgp_hide, $ico_ok, $ico_lock;
    
    $nb_field = count($this->display_pref) - 1;

    // actions names
    $action_display = 'dispref_display';
    $action_level = 'dispref_level';

    $entity = $this->display_entity;

    if ($this->display_module != '') {
      $url = $this->display_module . '_index.php';
    } else {
      $url = $this->display_entity . '_index.php';
    }

    $block = "
    <div class=\"displayPref\">
    <h1>".$this->pref_title."</h1>
    <table >
    <thead>
    <tr>
      <td>$l_default</td>
      <td>$l_displayed</td>
      <td>$l_order</td>
      <td>$l_field</td>
      <td>$l_move</td>
    </tr>
    </thead>";

    $i = 0;
    $nb_field_hidden = 0;
    while ( list($fieldname, $fieldinfo) = each($this->display_pref) ) {
      $fieldstatus = $fieldinfo['status'];
      $fieldorder = $fieldinfo['order'];
      $fielddefault = (! $fieldinfo['notdefault']);

      if ($fielddefault) {
        $dis_default = '=';
      } else {
        $dis_default = "&nbsp;";
      }

      // Check if field (or field family has to be displayed)
      // If type is hidden, hide all type_* fields
      $pos_base = strpos($fieldname, '_');
      if ($pos_base > 0) {
	$fieldname_base = (substr($fieldname, 0, $pos_base));
      } else {
	$fieldname_base = $fieldname;
      }
      if ($cgp_hide[$entity][$fieldname]
	  || $cgp_hide[$entity][$fieldname_base] || !isset($fieldnames[$fieldname])) {
	$td_class = 'resultPrefHide';
      } 
      if($i%2 == 0) {
        $class = "class=\"pair\"";
      } else {
        $class = '';
      }
      $block .= "
    <tr $class>
    <td class=\"$td_class\">$dis_default</td>
    <td class=\"$td_class\">";

      if ($fieldstatus == 0) {
	$new_status = 1;
	$nb_field_hidden++;
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldstatus=$new_status")."\">
      <img alt=\"$l_display_yes\" src=\"$ico_nok\" /></a>";
      } else if ($fieldstatus == 1) {
	$new_status = 0;
	$block .= "
      <a class=\"$td_class\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldstatus=$new_status")."\">
      <img alt=\"$l_display_no\" src=\"$ico_ok\" /></a>";
      } else if ($fieldstatus == 2) {
	$block .= "
      <img alt=\"$l_mandatory\" src=\"$ico_lock\" />";
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
    <td class=\"$td_class\">";
      // If last field we doesn't display the down arrow
      if ($i != $nb_field) {
        $sql_order_field = $fieldorder + 1;
	$block .= "
        <a href=\"$url?action=$action_level&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldorder=$sql_order_field\">
        <img alt=\"$l_down\" src=\"$ico_arrow_umount\" /></a>";
      }
      
      // If first field we don't display the up arrow
      if ($i != 0) {
        $sql_order_field = $fieldorder - 1;
	$block .= "
          <a href=\"$url?action=$action_level&amp;entity=$entity&amp;fieldname=$fieldname&amp;fieldorder=$sql_order_field\">
          <img alt=\"$l_up\" src=\"$ico_arrow_mount\" /></a>";
      }
      $block .= "
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
    $block .= '</div>';

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preference help screen
  /////////////////////////////////////////////////////////////////////////////
  function dis_pref_help() {
    global $l_help, $l_help_display_yes, $l_help_display_no, $l_help_mandatory;
    global $l_help_down, $l_help_up;
    // Images
    global $ico_arrow_mount, $ico_arrow_umount, $ico_ok;
    global $ico_nok, $ico_lock;

    $block = "
    <h1>$l_help</h1>
    <p>
    <img src=\"$ico_ok\" alt=\"$l_help_display_yes\" /> $l_help_display_yes<br />
    <img src=\"$ico_nok\" alt=\"$l_help_display_no\" /> $l_help_display_no<br />
    <img src=\"$ico_lock\" alt=\"$l_help_mandatory\" /> $l_help_mandatory<br />
    <img src=\"$ico_arrow_mount\" alt=\"$l_help_down\" /> $l_help_down<br />
    <img src=\"$ico_arrow_umount\" alt=\"$l_help_down\" /> $l_help_up<br />
    </p>";

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Display: Returns the dataset display
  // Parameters:
  //   - $dis_data_entity : function to call
  /////////////////////////////////////////////////////////////////////////////
  function dis_data($dis_data_entity) {
    global $path, $cgp_hide, $cdg_id, $fieldnames;

    $link_ok = $this->display_link;
    $entity = $this->display_entity;
    $block .= "<div class=\"result\">";

    $module = $this->display_module;

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != '')) {
      $block_page = $this->dis_data_index($this->data_url, $dis_data_entity);
      $block .= $block_page;
    }

    $block .= $this->data_form_head;
    $block .= '<table>';
    $block_header = $this->dis_data_header($this->data_url);
    $block .= $block_header;
    $block .= '<tbody>';
    reset($this->display_pref);
    $cpt = 0;

    // Iteration on each data row of dataset
    while ( ($this->data_set->next_record() )
            && ( ($cpt < $_SESSION['set_rows']) || ( ! $this->data_page) ) ) {

      if (function_exists($dis_data_entity)) {
	$res = $dis_data_entity($this, 'row_style', $link_ok);
	$rowstyle = $res['style'];
      }
      if (!$rowstyle && $cpt%2 == 0) {
        $rowstyle = "class='pair'";
      } 
      
      $cpt++;
      $block .= "<tr $rowstyle>";

      // If debug level Id is set, display the entity id
      if (($_SESSION['set_debug'] > 0) && (($_SESSION['set_debug'] & $cdg_id) == $cdg_id)) {
	$dg_id = $this->data_set->f('id');
	$block .= "<td>$dg_id</td>";
      }

      // If a left checkbox has to be displayed
      if (($this->data_cb_side == 'left') && ($this->data_cb_text != '')) {
	$col_show = $this->data_cb_show;
        // If cb are to be displayed or column telling visibility is true
        if (($col_show == '1') || ($this->data_set->f($col_show))) {
	  $name = $this->data_cb_name;
	  $name .= $this->data_set->f($this->data_idfield);
	  if ($this->data_set->f($this->data_cb_field)) {
	    $cb_check = 'checked';
	  } else {
	    $cb_check = '';
	  }
	  if (function_exists($dis_data_entity)) {
	    $data_element = $dis_data_entity($this, 'data_element', $link_ok);
	    $data_element = $data_element['name'];
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

	// Check if field (or field family has to be displayed)
	// If type is hidden, hide all type_* fields
	$pos_base = strpos($fieldname, '_');
	if ($pos_base > 0) {
	  $fieldname_base = (substr($fieldname, 0, $pos_base));
	} else {
	  $fieldname_base = $fieldname;
	}

	if (! ($cgp_hide[$entity][$fieldname]
	       || $cgp_hide[$entity][$fieldname_base]) && isset($fieldnames[$fieldname])) {

	  // Default values
	  $name = $this->data_set->f($fieldname);
	  $url = '';
	  $popup = false;
	  $popup_width = 600;
	  $popup_height = 400;
	  $align = 'left';

	  //--------------------------------------------------
	  // Global field display disposition
	  //--------------------------------------------------
	  // Format dates
	  if (substr($fieldname,0,4) == 'date') {
	    $name = of_date_format($this->data_set->f($fieldname), 1);
	  }
	  elseif ($fieldname == 'timeupdate') {
	    $name = of_datetime_format($this->data_set->f($fieldname), 1);
	  }
	  else if ($fieldname == 'timecreate') {
	    $name = of_datetime_format($this->data_set->f($fieldname), 1);
	  }
	  
	  //--------------------------------------------------
	  // We call module specific field display disposition
	  //--------------------------------------------------
	  if (function_exists($dis_data_entity)) {
	    $res = $dis_data_entity($this, $fieldname, $link_ok);
	    if ($res['name'] != '') $name = $res['name'];
	    $url = $res['url'];
	    $align = $res['align'];
	    $style = $res['style'];
	    $window = $res['window'];
	    $popup = $res['popup'];
	    $popup_width = $res['popup_width'];
	    $popup_height = $res['popup_height'];
	  }
	  if (function_exists($this->dis_data_footer)) {
            if(!$this->footer_data[$fieldname]) $this->footer_data[$fieldname] = '';
            $func = $this->dis_data_footer;
            $func($this, $fieldname, $link_ok);
	  }	 
	  //--------------------------------------------------
	  // Column display
	  //--------------------------------------------------
	  $content = '';
	  
	  // Set the style
	  if ($style != '') {
	    $td = "<td $style>";
	  } else if ($align == 'center') {
	    $td = "<td class=\"C\">";
	  } else if ($align == 'right') {
	    $td = "<td class=\"R\">";
	  } else {
	    $td = '<td>';
	  }
	  
	  // Pre cell display (open popup, link)
	  if ($popup) {
	    $content = "<a href=\"#\" onclick=\"window.name='root';popup('$url',''); return false;\">";
	  } else if ($window) {
	    $content = "<a href=\"#\" onclick=\"window.name='root';popup('$url',''); return false;\">";
	  } else if ($url != '') {
	    $content = "<a href=\"".url_prepare("$url")."\">";
	  }
	
	  // Cell display
	  $content .= $name;
	  
	  // Post cell display (close link, popup)
	  if ($popup || ($url != '')) {
	    $content .= '</a>';
	  }
	  
	  // Cell writing
	  $block .= "$td $content </td>\n";
	}
      }

      // If a right checkbox has to be displayed
      if (($this->data_cb_side == 'right') && ($this->data_cb_text != '')) {
	$col_show = $this->data_cb_show;
        // If cb are to be displayed or column telling visibility is true
        if (($col_show == '1') || ($this->data_set->f($col_show))) {
	  $name = $this->data_cb_name;
	  $name .= $this->data_set->f($this->data_idfield);
	  if ($this->data_set->f($this->data_cb_field)) {
	    $cb_check = 'checked';
	  } else {
	    $cb_check = '';
	  }
	  $block .= "<td>
            <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  $block .= "<td>&nbsp;</td>";
      }

      reset($this->display_pref);
      $block .= '</tr>';
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    $block .= '</tbody>';
    if (function_exists($this->dis_data_footer)) {
      $block .= "<tfoot><tr>";
      foreach($this->footer_data as $fieldname => $value) {
        $block .= '<td>'.$value.'</td>' ;
      }
      $block .= "</tr></tfoot>";
    }
    if ($this->data_header == 'both') {
      $block .= $block_header;
    }
    $block .= "</table>
      " . $this->data_form_end . "
      ";
    if ($this->data_header == 'both') {
      $block .= $block_page;
    }
    $block .= '</div>';
    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Display : Returns the dataset index display (page management)
  // Parameters:
  //   - $url             : URL with search fields to link to index pages
  //   - $dis_data_entity : function to call (module formatting fields)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_index($url, $dis_data_entity) {
    global $path, $params, $ctu_sql_limit, $obm;
    global $cak_prev, $cak_next, $cak_page_prev, $cak_page_next, $cak_begin, $cak_end;
    global $set_rows_default,$module, $l_select_all, $l_unselect_all;
    global $l_entries, $l_page_res, $l_export, $l_export_all;
    global $ico_export, $ico_export_all, $ico_begin, $ico_end, $ico_previous, $ico_next;

    $nb_page_dis = 10;
    $this_entity = $this->display_entity;
    $this_name = $this->display_name;
    $sql_order_dir = $this->sql_order_dir;
    $query = $this->data_set->obm_query;
    if ($this->data_set->obm_query_exportall != '') {
      $query = $this->data_set->obm_query_exportall;
    }
    $have_cb = ($this->data_cb_text != '');
    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }
    // Page display management
    // If page not set or page not for this display (this entity)
    if ((!isset($params['page'])) || ($params['page'] == '') || ($params['of_display_name'] != $this_name)) {
      $lpage = 1;
    } else {
      $lpage = $params['page'];
    }

    if ($_SESSION['set_rows'] < 1) $_SESSION['set_rows'] = $set_rows_default;
    $nb_rows = 0;
    if (function_exists('get_user_results_limit')) {
      $user_results_limit = get_user_results_limit($obm['profile']);
      $nb_rows = min($user_results_limit, $this->data_set->num_rows_total());
    } else {
      $nb_rows = $this->data_set->num_rows_total(); 
    }
    // If we delete all contacts of the last page, we must go to $page-1 (list)
    if ($lpage > 1) {
      if ($nb_rows < (($lpage-1)*$_SESSION['set_rows'])+1) {
         $lpage-- ;
      }
    }

    $nb_page = floor(($nb_rows-1) / $_SESSION['set_rows']) + 1;
    $first_row = ($lpage - 1) * $_SESSION['set_rows'];
    $start_page =  (floor(($lpage - 1) / $nb_page_dis) * $nb_page_dis) + 1;
    $end_page = $start_page + $nb_page_dis - 1;
    $end_page = min ($end_page, $nb_page); 

    if ($nb_rows < $_SESSION['set_rows']) {
      $export_rows = $nb_rows;
    } else {
      $export_rows = $_SESSION['set_rows'];
    }
    if($have_cb) {
      $cb_links = "
        <span>
        <a href=\"\" onclick=\"selectAllCb(this.parentNode.parentNode.parentNode);return false;\">$l_select_all</a>
        <a href=\"\" onclick=\"unSelectAllCb(this.parentNode.parentNode.parentNode);return false;\">$l_unselect_all</a>
        </span>";
    }
    $export_url = "$path/exportcsv/exportcsv_index.php";
    $content = "<div>
      $cb_links
      <script type=\"text/javascript\">
        function exportResultSet_{$this_entity}_{$this_name}(firstRow,nbRows) {
          form = new Element('form');
          form.setProperties({action:'$export_url', method:'POST'});
          form.adopt(new Element('input').setProperties({type:'hidden', name:'action', value:'export_page'}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'call_module', value:'$module'}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'of_display_name', value:'$this_name'}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'entity', value:'$this_entity'}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'first_row', value:firstRow}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'nb_rows', value:nbRows}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'query', value:'".phpStringToJsString($query)."'}));
          form.adopt(new Element('input').setProperties({type:'hidden', name:'func_data', value:'$dis_data_entity'}));
          form.injectInside($(document.body));
          form.submit();
          return false;
        }
      </script>";

    if ($module != 'people') {
      $content .="
        <a href=\"#\" onclick=\"return exportResultSet_{$this_entity}_{$this_name}('$first_row','$export_rows');\" >$l_export</a>      
        <a href=\"#\" onclick=\"return exportResultSet_{$this_entity}_{$this_name}('0','$nb_rows');\" >$l_export_all</a>";
    } 

    $content .= "$l_entries $nb_rows | $l_page_res : ($nb_page) &nbsp;&nbsp;&nbsp;&nbsp;";

    // Rewind to 1st page
    if ($lpage > 1) {
      $content .= "<a accesskey=\"$cak_begin\" href=\"$url&amp;page=1&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">|&lt;&lt;</a>&nbsp;&nbsp;";
    }

    // Rewind nb_page_dis page
    if ($start_page > 1) {
      $i = $start_page - $nb_page_dis;
      $content .= "<a accesskey=\"$cak_page_prev\" href=\"$url&amp;page=$i&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">-$nb_page_dis</a>&nbsp;&nbsp;";
    }

    // Previous page
    if ($lpage > 1) {
      $tmppage = $lpage - 1;
      $content .= "<a accesskey=\"$cak_prev\" href=\"$url&amp;page=$tmppage&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">&lt;</a>&nbsp;&nbsp;";
    }

    $i = $start_page;
    // Pages display
    while ($i <= $end_page) {
      if ($i != $lpage) {
	$content .= "<a href=\"$url&amp;page=$i&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">$i</a> | \n";
      } else {
	$content .= "$i | \n";
      }
      $i++;
    }

    // Next page
    if ($lpage < $nb_page) {
      $tmppage = $lpage + 1;
      $content .= "&nbsp;&nbsp;<a accesskey=\"$cak_next\" href=\"$url&amp;page=$tmppage&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">&gt;</a>";
    }

    // Go to next $nb_page_dis pages
    if ($end_page < $nb_page) {
      $i = $end_page + 1;
      $content .= "&nbsp;&nbsp;<a accesskey=\"$cak_page_next\" href=\"$url&amp;page=$i&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">+$nb_page_dis</a>";
    }

    // Go to last page
    if ($lpage < $nb_page) {
      $content .= "&nbsp;&nbsp;
      <a accesskey=\"$cak_end\" href=\"$url&amp;page=$nb_page&amp;sql_order_field=$params[sql_order_field]&amp;sql_order_dir=$sql_order_dir&amp;of_display_name=$this_name&amp;entity=$this_entity\">&gt;&gt;|</a>";
    }

    // Export - Uses forms to allow big queries to be transmitted
    $module = $this->display_module;

    $content .= "
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
    global $params;
    global $fieldnames, $cgp_hide, $cdg_id, $l_id;
    global $ico_arrow_up, $ico_arrow_down, $ico_arrow_off;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    $entity = $this->display_entity;
    $this_name = $this->display_name;
    $sql_order_dir = $this->sql_order_dir;
    $new_sql_order_dir = ($sql_order_dir == 'ASC' ? 'DESC' : 'ASC'); 
    $block = "
      <thead>
    <tr>";
    
    // If debug level Id is set, display the entity id
    if (($_SESSION['set_debug'] > 0) && (($_SESSION['set_debug'] & $cdg_id) == $cdg_id)) {
      $block .= "<td>$l_id</td>";
    }

    // Display the checkbox title if needed (exists and left side)
    if (($this->data_cb_side == 'left')
          && ($this->data_cb_text != '')) {
      $block .= '<td>'.$this->data_cb_text.'</td>';
    }

    reset($this->display_pref);
    while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {

      // Check if field (or field family has to be displayed)
      // If moduletype is hidden, hide all moduletype_* fields
      $pos_base = strpos($fieldname, '_');
      if ($pos_base > 0) {
	$fieldname_base = (substr($fieldname, 0, $pos_base));
      } else {
	$fieldname_base = $fieldname;
      }

      if (! ($cgp_hide[$entity][$fieldname]
	     || $cgp_hide[$entity][$fieldname_base]) && isset($fieldnames[$fieldname])) {
	$label = $fieldnames[$fieldname];
  
        $img = '';
        $onclick = ''; 
	if ($this->data_order) {
	  $furl = $url;
          if ($url != '') {
            if ($params['sql_order_field'] == $fieldname) {
              if ($sql_order_dir == 'DESC') {
                $img = "<img src='$ico_arrow_up' alt='sort' />";
              } else {
                $img = "<img src='$ico_arrow_down' alt='sort' />";
              }
            }
	    // If action on this dataset
	    if ($this_name == $params['of_display_name']) {
	      $furl .= "&amp;page=$params[page]&amp;sql_order_field=$fieldname&amp;sql_order_dir=$new_sql_order_dir&amp;of_display_name=$this_name&amp;entity=$entity";
	    } else {
	      $furl .= "&amp;page=1&amp;sql_order_field=$fieldname&amp;sql_order_dir=$new_sql_order_dir&amp;of_display_name=$this_name&amp;entity=$entity";
	    }
          }
          $onclick = "onclick=\"window.location.href='$furl'\" onmouseover=\"this.className='hover'\" onmouseout=\"this.className=''\" ";
	}
	$block .= "<td $onclick>$label $img</td>";
      }
    }

    // Display the checkbox title if needed (exists and right side)
    if (($this->data_cb_side == 'right')
         && ($this->data_cb_text != '')) {
      $block .= '<td>'.$this->data_cb_text.'</td>';
    }
    $block .= '</tr></thead>';

    return $block;
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  // for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header_file($sep) {
    global $cgp_hide, $fieldnames, $cdg_id;

    $entity = $this->display_entity;

    reset($this->display_pref);
    while ( list($fieldname, $fieldinfo) = each ($this->display_pref) ) {

      // Check if field (or field family has to be displayed)
      // If type is hidden, hide all type_* fields
      $pos_base = strpos($fieldname, '_');
      if ($pos_base > 0) {
	$fieldname_base = (substr($fieldname, 0, $pos_base));
      } else {
	$fieldname_base = $fieldname;
      }

      if (! ($cgp_hide[$entity][$fieldname]
	  || $cgp_hide[$entity][$fieldname_base]) && isset($fieldnames[$fieldname])) {
	$label = $fieldnames[$fieldname];
	// if label empty or is an image or a link, we display the column name
	if ( ($label == '') || (preg_match("/.*<.*>.*/", $label) ) ) {
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
    global $cgp_hide, $cdg_id, $ctu_sql_limit, $fieldnames;

    $entity = $this->display_entity;
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

	// Check if field (or field family has to be displayed)
	// If type is hidden, hide all type_* fields
	$pos_base = strpos($fieldname, '_');
	if ($pos_base > 0) {
	  $fieldname_base = (substr($fieldname, 0, $pos_base));
	} else {
	  $fieldname_base = $fieldname;
	}

	if (! ($cgp_hide[$entity][$fieldname]
	       || $cgp_hide[$entity][$fieldname_base]) && isset($fieldnames[$fieldname])) {
	  $txt_name = $this->data_set->f($fieldname);

	  //--------------------------------------------------
	  // Global field display disposition
	  //--------------------------------------------------
	  // Format dates
	  if (substr($fieldname,0,4) == 'date') {
	    $txt_name = of_date_format($this->data_set->f($fieldname));
	  }
	  elseif ($fieldname == 'timeupdate') {
	    $txt_name = of_datetime_format($this->data_set->f($fieldname));
	  }
	  else if ($fieldname == 'timecreate') {
	    $txt_name = of_datetime_format($this->data_set->f($fieldname));
	  }
	  
	  //--------------------------------------------------
	  // We call module specific field display disposition
	  //--------------------------------------------------
	  if (function_exists($dis_data_entity)) {
	    $res = $dis_data_entity($this, $fieldname, $link_ok);
	  }	  

	  if ($res['txt_name'] != '') {
	    $name = $res['txt_name'];
	  } else if ($res['name'] != '') {
	    $name = $res['name'];
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
	    if ($delimiter == '') {
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
// Display : Function that display each page
// Parameters:
//   - $display : (by ref) $display hash with each content object
///////////////////////////////////////////////////////////////////////////////
function display_page(&$display) {

  // If a theme specif function is provided
  if (function_exists('display_page_theme')) {
    return display_page_theme($display);
  }  
  
  global $output_target, $ico_nav_close;
  global $ico_nav_open, $l_hide, $l_show;
  global $l_print,  $params;


  if (count($_POST)) {
    $redirection = $GLOBALS['actions'][$GLOBALS['module']][$params['action']]['Redirection'];
    if ($redirection && $params['redirection']) {
      header( "Status: 301 Moved Permanently");
      header("location: $redirection");
      die();
    }
  }
  
  // If header is null, display only the detail (Popup windows)
  if ($display['header'] == '') {
    echo $display['head'] . 
      "<span class='RF' id='spinner' style='display:none' ><img src='$GLOBALS[ico_spinner]' alt='' /></span>".
      $display['title'] .
      $display['msg'] . 
      "
      <div id=\"mainContent\">
      <div id=\"ajaxMessage\"></div>".
      $display['search'] .
      $display['detail']." 
       <p class=\"LC\"></p>
       </div>
      ".
      $display['result'] . 
      $display['end'];

    return;
  }

  echo $display['head'] . $display['header'] . "
    <!-- left panel -->
    <div id=\"portletsPanel\"> 
      <div id='portletsHandler'>
       <span>
        <img src='$ico_nav_close' alt='Hide this bar' />
       </span>
       <span id='portletsHandlerShow'>
        <img src='$ico_nav_open' alt='Show this bar' />
       </span>
      </div>         
     <div id=\"portlets\">          
      $display[todo]
      $display[features] 
      $display[link] 
      $display[detailInfo] 
      $display[last]
     </div>
    </div>
    <!-- left panel end -->
    <div id=\"mainPanel\" >  ";

echo "$display[title]
    $display[action]
    $display[msg]" ;
    if ($display['search'] != '' || $display['detail'] != '') {
      echo  
        "<div id=\"mainContent\">
         <div id=\"ajaxMessage\"></div>
        ".
      $display['search'] .
      $display['detail'] . "
      <p class=\"LC\"></p>
      </div>";
    }
    echo $display['result'] ."
    </div>
    <script type=\"text/javascript\">
      obm.portlets = new Obm.Portlets();
      obm.tip = new Obm.Tip($$('.obmTip'));
    </script>  
    <!-- center zone end -->  
    ".$display['end'];

  if (isset($_SESSION['obm_message'])) {
    echo $_SESSION['obm_message'];
    unset($_SESSION['obm_message']);
  }
}

/**
 *
 */
function redirect_to($url, $msg = null) {
  if (!is_null($msg)) {
    $_SESSION['obm_message'] = "<script text='language/javascript'>showOkMessage(\"".phpStringToJsString($msg)."\")</script>";
  }
  header( "Status: 301 Moved Permanently");
  header("location: $url");
  die();
}

/**
 *
 */
function redirect_ok(&$params, $msg) {
  $params['redirection'] = true;
  $_SESSION['obm_message'] = "<script text='language/javascript'>showOkMessage(\"".phpStringToJsString($msg)."\")</script>";
}

/**
 *
 */
function redirect_warn(&$params, $msg) {
  $params['redirection'] = true;
  $_SESSION['obm_message'] = "<script text='language/javascript'>showWarnMessage(\"".phpStringToJsString($msg)."\")</script>";
}

/**
 *
 */
function redirect_err(&$params, $msg) {
  $params['redirection'] = true;
  $_SESSION['obm_message'] = "<script text='language/javascript'>showErrorMessage(\"".phpStringToJsString($msg)."\")</script>";
}


///////////////////////////////////////////////////////////////////////////////
// Display : Function that display each page
// Parameters:
//   - $display : (by ref) $display hash with each content object
///////////////////////////////////////////////////////////////////////////////
function display_outframe(&$display) {
  // If a theme specif function is provided
  if (function_exists('display_outframe_theme')) {
    return display_outframe_theme($display);
  }  
  echo $display['head'] . $display['header'] .$display['detail'];
}

///////////////////////////////////////////////////////////////////////////////
// Function used to know if we have to show the topmenu popin
// Parameters:
//   none
///////////////////////////////////////////////////////////////////////////////

function show_topmenupopin() {
  if (is_global_admin()) {
    return false;
  }

	if ($_SESSION['set_top_bar'] != 'yes') {
		if (!isset($_SESSION['displayed_popin']) OR $_SESSION['displayed_popin'] != 1) {
				return true;
		}
	} 
	return false;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Head
// Parameters:
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_head($p_module, $standard=true) {
	if (isset($GLOBALS['_GET']['action_popin']) AND !empty($GLOBALS['_GET']['action_popin'])) {
		if ($GLOBALS['_GET']['action_popin'] == 'stop_popin') {
			update_user_pref($_SESSION['obm']['uid'], "displayed_popin", "1");
			$_SESSION['displayed_popin'] = 1;
		} else if ($GLOBALS['_GET']['action_popin'] == 'change_topmenu') {
			update_user_pref($_SESSION['obm']['uid'], "displayed_popin", "1");
			update_user_pref($_SESSION['obm']['uid'], "set_top_bar", "yes");
			$_SESSION['displayed_popin'] = 1;
			$_SESSION['set_top_bar'] = 'yes';
		}
	}
	
  // If a theme specif function is provided
  if (function_exists('display_head_theme')) {
    return display_head_theme($module);
  }
  global $l_obm_title, $extra_css, $extra_js, $extra_js_include,$ico_minus, $ico_add;
  global $ico_datepicker,$l_monthsofyear,$l_daysofweekfirst,$ico_colorpicker;
  global $l_monthsofyearshort,$l_daysofweek,$l_daysofweekshort,$ico_plus, $c_week_first_day;
  global $l_today, $l_close, $css_obm, $popup_height,$popup_width,$resources_path;
  global $l_popin_message, $l_popin_activation_button, $l_close_popin_button;
  global $css_print_obm, $ico_delete, $ico_update, $output_target,$l_transparent;
  global $l_autocomplete_field, $l_fatal_server_err, $gwt_js_include, $path, $params;
  global $ico_html, $ico_ical;
  global $obm_version, $obm;

  $css = $css_obm;

  if ($params['output_target'] == 'print') {
    $extra_css[] = $css_print_obm;
  }

  $monthShort =  phpArrayToJsArray($l_monthsofyearshort);
  $monthLong = phpArrayToJsArray($l_monthsofyear);
  $daysShort = phpArrayToJsArray($l_daysofweekfirst);
  $dayMedium = phpArrayToJsArray($l_daysofweekshort);
  $dayLong =   phpArrayToJsArray($l_daysofweek);

  $headers_e = '';

  if (is_array($extra_css)) {
    foreach($extra_css as $css_file) {
      $dis_extra_css .= "<link rel=\"stylesheet\" type=\"text/css\" href=\"$css_file?version=$obm_version\" />";
    }
  } elseif (isset($extra_css)) {
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"$extra_css?version=$obm_version\" />";
  }

  if (is_array($extra_js_include)) {
    foreach($extra_js_include as $js_file) {
      $js_include .= "<script type=\"text/javascript\" src=\"$resources_path/js/$js_file?version=$obm_version\" ></script>\n";
    }
  }
  if(is_array($GLOBALS['js']['vars'])) {
    foreach($GLOBALS['js']['vars'] as $kind => $values) {
      foreach($values as $label => $value) {
        if(is_numeric($value)) {
          $obm_vars .= "obm.vars.$kind.$label = $value;\n";
        } elseif(is_string($value)) {
          $obm_vars .= "obm.vars.$kind.$label = '".phpStringToJsString($value)."';\n";
        } elseif(is_array($value)) {
          $obm_vars .= "obm.vars.$kind.$label = new Object();\n";
          foreach($value as $key => $string) {
            $obm_vars .= "obm.vars.$kind.$label.$key = '".phpStringToJsString($string)."';\n";
          }
        } else {
          $obm_vars .= "obm.vars.$kind.$label = '$value';\n";
        }
      }
    }
  }

  if ( $_SESSION['set_top_bar'] == 'yes' && $_SESSION['set_theme'] == "default") {
    $stylecss .= "<link rel=\"stylesheet\" type=\"text/css\" href=\"$resources_path/themes/default/styles/topbar.css\" />";
  } else {
    $stylecss .= "<link rel=\"stylesheet\" type=\"text/css\" href=\"$css?version=$obm_version\" />";
  }
  $set_top_bar = ($_SESSION['set_top_bar'] == 'yes' && $_SESSION['set_theme'] == 'default');

  if($standard) {
    $headers_e .= '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">';
  } else {
    $headers_e .= '<!DOCTYPE html>';
  }

  if (!$params['popup']) {
    if (show_topmenupopin()) {
      $supportOBMBanner .= "
        <div class=\"popintopbar\">
        <form class=\"search\" id=\"popintopbar-form\" method=\"get\" action=\"\">
          <input type=\"hidden\" name=\"action_popin\" value=\"change_topmenu\" />
          <label><span class=\"popintopbar-message\">$l_popin_message &nbsp;&nbsp;</span></label>
          <label><input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_popin_activation_button\" />&nbsp;&nbsp;</label>
          <label><span class=\"popintopbar-message\"><a href=\"../obm.php?action_popin=stop_popin\"><img src=\"../images/themes/default/images/ico_close.gif\" alt=\"close\" title=\"$l_close_popin_button\" /></a></span></label>
        </form>
      </div>
      ";
    } else {
      $supportOBMBanner = "<h2 class=\"spreadLove\"><div>"
      .__("You are using the open-source and free version of <a href=\"http://www.obm.org/\">OBM</a> developped and supported by <a href=\"http://www.linagora.com/\">Linagora</a>.")
      ."</div><div class=\"supportUs\">"
      .__("Contribute to the product R&amp;D by subscribing to an <a href=\"http://pro.obm.org/\">enterprise offer</a>.")
      ."</div></h2>";
    }
  }

  return "$headers_e   
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />
  <meta http-equiv=\"X-UA-Compatible\" content=\"chrome=1\" />
  <title>$l_obm_title - $p_module</title>
  $stylecss
  <link rel=\"stylesheet\" type=\"text/css\" href=\"$css_print_obm?version=$obm_version\" media=\"print\" />
  <link rel=\"stylesheet\" type=\"text/css\" href=\"$resources_path/js/introjs/introjs.css\" />
  $dis_extra_css
  <script type=\"text/javascript\" src=\"$resources_path/js/q.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/mootools/mootools-1.2.1-core.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/mootools/plugins/mootools-1.2-more.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/mootools/plugins/clientcide.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/css3-mediaqueries.js\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/introjs/intro.js\" ></script>
  <script type=\"text/javascript\">
  //<![CDATA[
  var obm = Obm = {};
  obm.vars = new Object();
  obm.vars.labels = new Object();
  obm.vars.labels.intro = new Object();
  obm.vars.images = new Object();
  obm.vars.regexp = new Object();
  obm.vars.consts = new Object();
  obm.vars.conf = new Object();
  obm.initialize = new Chain();
  obm.resize = new Chain();
  obm.vars.newTopbar = ".($set_top_bar ? 'true' : 'false').";
  obm.vars.images.minus = '$ico_minus';
  obm.vars.images.plus = '$ico_plus';
  obm.vars.images.datePicker = '$ico_datepicker';
  obm.vars.images.colorPicker = '$ico_colorpicker';
  obm.vars.images.update = '$ico_update';
  obm.vars.images.add = '$ico_add';
  obm.vars.images.del = '$ico_delete';
  obm.vars.images.html = '$ico_html';
  obm.vars.images.ical = '$ico_ical';
  obm.vars.regexp.dateFormat = '$_SESSION[set_date_upd]';
  obm.vars.regexp.dispDateFormat = '$_SESSION[set_date]';
  obm.vars.regexp.dispTimeFormat = '".Of_Date::getTimeFormat()."';
  obm.vars.consts.popupHeight = '$popup_height';
  obm.vars.consts.popupWidth = '$popup_width';
  obm.vars.consts.weekStart = $c_week_first_day;
  obm.vars.consts.resourcePath = '$resources_path';
  obm.vars.consts.timeZone = '".Of_Date::getOption('timezone')."';
  obm.vars.consts.obmUrl = '$path';
  obm.vars.consts.firstHour = $GLOBALS[ccalendar_first_hour];
  obm.vars.consts.lastHour = $GLOBALS[ccalendar_last_hour];
  obm.vars.consts.charLimit = 100;
  obm.vars.labels.monthsShort = $monthShort;
  obm.vars.labels.months = $monthLong;
  obm.vars.labels.dayShort = $daysShort;
  obm.vars.labels.dayMedium = $dayMedium;
  obm.vars.labels.dayLong = $dayLong;
  obm.vars.labels.today = '".phpStringToJsString($l_today)."';
  obm.vars.labels.transparent = '".phpStringToJsString($l_transparent)."';
  obm.vars.labels.autocompleteField = '".phpStringToJsString($l_autocomplete_field)."'
  obm.vars.labels.fatalServerErr = '".phpStringToJsString($l_fatal_server_err)."';
  obm.vars.labels.permsError = '".phpStringToJsString($GLOBALS['l_error_permission'])."';
  obm.vars.labels.insertOk = '".phpStringToJsString($GLOBALS['l_insert_ok'])."';
  obm.vars.labels.updateOk = '".phpStringToJsString($GLOBALS['l_update_ok'])."';
  obm.vars.labels.deleteOk = '".phpStringToJsString($GLOBALS['l_delete_ok'])."';
  obm.vars.labels.insertError = '".phpStringToJsString($GLOBALS['l_insert_error'])."';
  obm.vars.labels.updateError = '".phpStringToJsString($GLOBALS['l_update_error'])."';
  obm.vars.labels.deleteError = '".phpStringToJsString($GLOBALS['l_delete_error'])."';
  obm.vars.labels.details = '".phpStringToJsString($GLOBALS['l_details'])."';
  obm.vars.labels.intro.skip = '".phpStringToJsString($GLOBALS['l_intro_skip'])."';
  obm.vars.labels.intro.done = '".phpStringToJsString($GLOBALS['l_intro_done'])."';
  obm.vars.labels.intro.next = '".phpStringToJsString($GLOBALS['l_intro_next'])."';
  obm.vars.labels.intro.previous = '".phpStringToJsString($GLOBALS['l_intro_previous'])."';
  $obm_vars
  // ]]>
  </script> 
  <script type=\"text/javascript\" src=\"$resources_path/js/datepicker.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/utils.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/obm.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/autocomplete.js?version=$obm_version\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/dropdown.js?version=$obm_version\" ></script>
  $js_include

  <script type=\"text/javascript\">
  //<![CDATA[
  obm.popup = new Obm.Popup();
  window.onresize= function() {
    while(obm.resize.\$chain && obm.resize.\$chain.length > 0) {
      obm.resize.callChain();
    }
  }

  obm.initialize.chain(datePickerGenerator);
  obm.initialize.chain(refreshWaitEvent);
  obm.initialize.chain(refreshUnreadMail);
  setInterval(refreshUnreadMail,300000);
  setInterval(refreshWaitEvent,300000);
  window.addEvent('load', function () {
    while(obm.initialize.\$chain && obm.initialize.\$chain.length > 0) {
      obm.initialize.callChain();
    }
  });

  $extra_js
  // ]]>
  </script>
  <script type=\"text/javascript\">
    window.addEvent('load', function() {
      $('logout').addEvent('click', function(event) {
        event.stop();

        new Request({
          method: 'get',
          url: '/webmail/obm.php?_task=logout',
          onComplete: function() { window.location.href = $('logout').href; }
        }).send();
      });
    });
  </script>
</head>

<body onload=\"if (document.f_search && document.f_search.elements[0]) {
    document.f_search.elements[0].focus();
  } else if (document.f_entity && document.f_entity.elements[0]) {
    document.f_entity.elements[0].focus();
  } \">
$supportOBMBanner

"
.display_debug()
;
}



///////////////////////////////////////////////////////////////////////////////
// Display : Function that display the page menu
// Parameters:
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function display_menu($module) {
  global $path, $obm, $display, $obm_version, $cgp_show, $modules;
  global $ico_logout, $ico_login, $ico_obm, $ico_print, $ico_scope;
  global $l_logout, $l_profile, $l_scope_update, $l_waiting_events;
  global $perm, $cright_write_admin, $conf_obm3_topbar;

  // If a new topbar specif function is provided
  if ( $_SESSION['set_top_bar'] == "yes" && $_SESSION['set_theme'] == "default") {
    return display_unified_menu($module);
  }

  // If a theme specif function is provided
  if (function_exists('display_menu_theme')) {
    return display_menu_theme($module);
  }

  $obm['uid'];
  $login = $obm['login'];
  $profil = $obm['profile'];
  $p = isset($GLOBALS["l_perm_$profil"]) ? $GLOBALS["l_perm_$profil"] : $profil;
  $domain = $obm['domain_label'];
  $domain_id = $obm['domain_id'];
  $section = $cgp_show['module'][$module];
  if (empty($section)) {
    $section = $module;
  }
  $display['section'] = display_sections($section);
  $display['last'] = display_last_visited();
  $display['todo'] = display_todos();
  $display['action'] = display_actions($section, $module);
  $update_state = get_update_state($domain_id);
  if (($update_state) && $cgp_show['module']['tools'] && ($perm->check_right('tools', $cright_write_admin))) {
    $dis_scope = "<li><a id=\"tools_update_index\" href=\"$path/tools/tools_index.php?action=update_index\" ><img src=\"$ico_scope\" title=\"$l_scope_update\" alt=\"Commit update\" /></a></li>";
  }

  $wait_event = "<li><a id=\"bannerWaitingEvent\" href='".$path."/calendar/calendar_index.php?action=waiting_events'></a></li>";

  $block = "
    <!-- Bandeau -->
    <div id='banner'>

    <div style='float:left;'>
      <a href=\"$path/obm.php\" id=\"obmLogo\"><img src='$ico_obm' alt='Logo OBM'/></a>
      ".$display['section']."
    </div>

    <div style='float:right;'>
    <ul id='information'>
      <li id='spinner' style='display:none' ><img src='$GLOBALS[ico_spinner]' alt='' /></li>
      $dis_scope
      $wait_event
      <li id='informationUser'>$login ($domain)</li>
      <li id='informationProfile'>$l_profile: $p</li>
      <li><a id='logout' href='".$path."/obm.php?action=logout'><img src='$ico_logout' alt='$l_logout' /> $l_logout</a></li>
      $dis_printer
    </ul>
    </div>

    </div>
    ".$display['module'];

  return $block;
}

function display_unified_menu($module) {
  global $path, $obm, $display, $obm_version, $cgp_show, $modules;
  global $ico_logout, $ico_login, $ico_obm, $ico_print, $ico_scope, $ico_big_user;
  global $l_logout, $l_profile, $l_scope_update, $l_waiting_events, $l_section_webmail, $l_module_calendar, $l_module_contact, $l_section_more;
  global $perm, $cright_write_admin;
  global $l_intro_webmail, $l_intro_calendar, $l_intro_contacts, $l_intro_moremenu, $l_intro_user;

  $uid = $obm["uid"];
  $login = $obm['login'];
  $profil = $obm['profile'];
  $p = isset($GLOBALS["l_perm_$profil"]) ? $GLOBALS["l_perm_$profil"] : $profil;
  $domain = $obm['domain_label'];
  $domain_id = $obm['domain_id'];
  $section = $cgp_show['module'][$module];
  if (empty($section)) {
    $section = $module;
  }
  $display['section'] = display_sections($section);
  $display['last'] = display_last_visited();
  $display['todo'] = display_todos();
  $display['action'] = display_actions($section, $module);
  $dis_scope = false;
  $update_state = get_update_state($domain_id);
  if (($update_state) && $cgp_show['module']['tools'] && ($perm->check_right('tools', $cright_write_admin))) {
    $dis_scope = true;
  }

  if(!isset($user_photo_id)){
    $user_infos = get_user_info($uid);
    $user_photo_id = $user_infos["photo_id"];
    $common_name = $user_infos["commonname"];
    $email = $user_infos["email"];
  }

  $m = new Mustache_Engine(array(
    'loader' => new Mustache_Loader_FilesystemLoader(dirname(__FILE__) . "/mustache"),
    'partials_loader' => new Mustache_Loader_FilesystemLoader(dirname(__FILE__) . '/mustache/partials'),
  ));

  $data = array("template");
  $data["template"]["logo_ico"] = $ico_obm;
  $data["template"]["logo_url"] = "$path/obm.php";

  $data["template"]["ico_obm"] = $ico_obm;
  $data["template"]["ico_spinner"] = $GLOBALS[ico_spinner];

  $data[] = array("user_infos");
  $data["user_infos"]["login"] = $login;
  $data["user_infos"]["commonname"] = $common_name;
  $data["user_infos"]["email"] = $email;
  $data["user_infos"]["profil"] = $profil;
  $data["user_infos"]["profil_lang"] = $l_profile;
  $data["user_infos"]["domain"] = $domain;
  $data["user_infos"]["intro-element-id"] = "user";
  $data["user_infos"]["lastname"] = $user_infos["lastname"];
  $data["user_infos"]["firstname"] =  $user_infos["firstname"];

  $data["user_infos"]["l_logout"] = $l_logout;

  if ($user_photo_id > 0) {
    $data["user_infos"]["photo_url"] = "$path/document/document_index.php?action=accessfile&document_id=$user_photo_id";
  } else {
    $data["user_infos"]["photo_url"] = $ico_big_user;
  }

  $data[] = array("scope");
  $data["scope"]["dis_scope"] = $dis_scope;
  $data["scope"]["url"] = "$path/tools/tools_index.php?action=update_index";
  $data["scope"]["ico"] = $ico_scope;
  $data["scope"]["title"] =$l_scope_update;

  $data[] = array("buttons");
  $data["buttons"] = array();

  if (isset($email) && !empty($email)) {
    array_push( $data["buttons"],
      array(
        "name" => $l_section_webmail,
        "url" => "$path/webmail/webmail_index.php?_task=mail&_filter=ALL",
        "notifier" => true,
        "notifier_url" => "$path/webmail/webmail_index.php?_task=mail&_filter=UNSEEN&_mbox=INBOX",
        "notifier_id" => "bannerUnreadMail",
      	"intro-element-id" => "webmail"
      )
    );
  }

  array_push( $data["buttons"],
    array(
      "name" => $l_module_calendar,
      "url" => "$path/calendar/calendar_index.php",
      "notifier" => true,
      "notifier_url" => "$path/calendar/calendar_index.php?action=waiting_events",
      "notifier_id" => "bannerWaitingEvent",
      "intro-element-id" => "calendar"
    )
  );

  array_push( $data["buttons"],
    array(
      "name" => $l_module_contact,
      "url" => "$path/contact/contact_index.php",
      "notifier" => false,
      "intro-element-id" => "contacts"
    )
  );

  array_push( $data["buttons"],
    array(
      "name" => $l_section_more, 
      "idmore" => "moremenu_button",
      "url" => "#",
      "notifier" => false,
      "intro-element-id" => "moremenu"
    )
  );

  $seenTopbarIntro = get_one_user_pref($uid, "seen_topbar_intro");

  if (!isset($seenTopbarIntro[$uid]) || $seenTopbarIntro[$uid]["value"] != "yes") {
    update_user_pref($uid, "seen_topbar_intro", "yes");

    $data["show_intro"] = true;
    $data["intro-steps"] = array();

    if ( !empty($data["user_infos"]["email"]) ) {
      $data["intro-steps"][] = array(
        "id" => "webmail",
        "position" => "right",
        "text" => $l_intro_webmail
      );
    }

    $data["intro-steps"][] = array(
      "id" => "calendar",
      "position" => "right",
      "text" => $l_intro_calendar
    );
    $data["intro-steps"][] = array(
      "id" => "contacts",
      "position" => "right",
      "text" => $l_intro_contacts
    );
    $data["intro-steps"][] = array(
      "id" => "moremenu",
      "position" => "right",
      "text" => $l_intro_moremenu
    );
    $data["intro-steps"][] = array(
      "id" => "user",
      "position" => "left",
      "text" => $l_intro_user
    );
  }

  $data["sections"] = get_sections_data();

  $data["user_settings"] = get_module_data('my', null);


  $block = $m->render('topbar', $data);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {

  // If a theme specif function is provided
  if (function_exists('display_sections_theme')) {
    return display_sections_theme($section);
  }
  global $cgp_show, $sections, $perm;

  $ret = "<ul id='section'>\n";
  foreach ($cgp_show['section'] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value['Name'];
      $section_target = $value['Target'];
      $section_right = $perm->get_section_rights($key);
      if ($section_right) {
        $section_modules = array_keys($cgp_show['module'],$key);
        if (count($section_modules) > 0) {
	  $module_block = display_modules($key, null);
	  $mods .= $module_block;
	  $js .= "obm.menu.addItem('section-$key');";
	} else {
	  if (isset($section_target)) {
	    $target = "target='$section_target'";
	  } else {
            $target = '';
          }
          $section_name = "<a $target href=\"$show[url]\">$section_name</a>";
        }
        if ($key == $section) {
          $ret .= "<li class=\"current\" id=\"section-$key\">$section_name</li>\n";
        } else {
          $ret .= "<li class=\"sectionItem\" id=\"section-$key\">$section_name</li>\n";
        }
      }
    }
  }
  $ret .= '</ul>';
  $ret .= "
    $mods
    <script type=\"text/javascript\">
      obm.menu = new Obm.Menu();
      $js
    </script>
";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section menus display block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_modules($section, $module, $portlet = false) {

  // If a theme specif function is provided
  if (function_exists('display_modules_theme')) {
    return display_modules_theme($section, $module);
  }
  global $cme_txt, $cme_ico, $cme_both;
  global $path, $perm, $modules, $cgp_show;

  if (($_SESSION['set_menu'] == $cme_ico) || ($_SESSION['set_menu'] == $cme_both))
    $fico = true;
  else $fico = false;

  if (($_SESSION['set_menu'] == $cme_txt) || ($_SESSION['set_menu'] == $cme_both))
    $ftext = true;
  else $ftext = false;

  $section_link = array_keys($cgp_show['module'],$section);
  if (is_array($section_link)) {
    $id = $section.'Items';
    $block .= ( $portlet ) ? "<ul id='section-$section-items' style='list-style-type:none;'>" : "<div class='module' id='section-$section-items-wrapper'><ul id='section-$section-items'>";
    foreach ($section_link as $mod) {
      $module_name = $modules[$mod]['Name'];
      $module_tooltip = $modules[$mod]['Tooltip'];
      $module_right = $perm->get_module_rights($mod); 
      if (($module_right & $modules[$mod]['Right']) == $modules[$mod]['Right']) {
	// XXX specifier une target au lien du module (non configurable
	// actuellement car cgp_show[module][webmail] = valeur et non tableau
	// Si tableau pb avec section_link = array_keys(..) 10 l plus haut
	$module_target = $modules[$mod]['Target'];
	if (isset($module_target)) {
	  $target = " target='$module_target'";
	} else {
          $target = '';
        }
        $block .= '<li>';
        if ($fico) {
          $ico = $modules[$mod]['Ico'];
          if ($ico) {
            $block .= "<a$target id=\"".$modules[$mod]['Id']."\" href='".$modules[$mod]['Url']."' class='ico' title='$module_tooltip'><img src='$ico' alt='$module_name' /></a>";
          }
        }
        $block .= "<a$target id=\"".$modules[$mod]['Id']."\" href='".$modules[$mod]['Url']."' title='$module_tooltip'>";
        if ($ftext || $portlet) $block .= $module_name;
        $block .= "</a></li>";
      }
    }
    $block .= ( $portlet ) ? '</ul>' : '</ul></div>';
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

  // If a theme specif function is provided
  if (function_exists('display_actions_theme')) {
    return display_actions_theme($section, $module);
  }
  global $modules, $action, $display;
  global $actions, $perm, $display;

  beautify_title($module, $action);

  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    $block = "<ul id=\"action\">"; 
    foreach ($actions[$module] as $key => $value) {
      // If the current action has this action in target
      if (!in_array("!$action",$value['Condition']) 
        && (in_array('all', $value['Condition'])
	|| in_array($action, $value['Condition']))) {
	if (($module_right & $value['Right']) == $value['Right']) {

          $tooltip = $value['Tooltip'];

	  if ($value['Popup'] == 1) {
	    if ($value['Target']) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $content = "
              <a href=\"\" 
              title=\"".$value['tooltip']."\" 
              onclick=\"$wtarget popup('".$value['Url']."',''); return false;\">
	      ".$value['Name']."</a> &nbsp;";
	  } else {
            $id = isset($value['Id']) ? 'id="' . $value['Id'] . '"' : '';
            $content = "
              <a $id href=\"".$value['Url']."\" title=\"".$tooltip."\" >".$value['Name']."</a> &nbsp;";
          }
          $block .= "<li onmouseover=\"this.className='hover'\" onmouseout=\"this.className=''\" >$content</li>";
	}
      }
    }
    $block .= "</ul>"; 
    
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_last_visited() {

  // If a theme specif function is provided
  if (function_exists('display_last_visited_theme')) {
    return display_last_visited_theme();
  }
  global $obm, $cdg_sql, $path, $cgp_show, $last_v, $l_last_visit, $modules, $perm;

  $uid = $obm['uid'];

  foreach ($cgp_show['module'] as $module => $val) {

    if (($cgp_show['module'][$module]) && ($_SESSION['last_v'][$module]['text'] != '')) {
      $id = $_SESSION['last_v'][$module]['id'];
      $text = substr($_SESSION['last_v'][$module]['text'], 0, 30);
      if(isset($GLOBALS['modules'][$module]['Ico'])) {
        $l_ico = $GLOBALS['modules'][$module]['Ico'];
      } else {
        $l_ico = $GLOBALS["ico_$module"];
      }
      if($l_ico != '') {
        $mod_url = url_prepare("$path/$module/${module}_index.php?action=detailconsult&amp;${module}_id=$id");
        $dis_last .= "
          <li><a href=\"$mod_url\"><img src=\"$l_ico\" alt=\"$module\" /> $text</a></li>";
      } else {
        $mod_url = url_prepare("$path/$module/${module}_index.php?action=detailconsult&amp;${module}_id=$id");
        $dis_last .= "
          <li><a href=\"$mod_url\"> $text</li>";
      }
    }
  }

  // Add Calendar items
  // Last created|updated event
  $obm_q = new DB_OBM;
  $cal_url = "$path/calendar/calendar_index.php?action=detailconsult&calendar_id=";
  $query = "SELECT event_id, event_timeupdate, event_title 
    FROM Event 
    WHERE event_usercreate='$uid' 
      AND event_timeupdate = (SELECT max(event_timeupdate) FROM Event WHERE event_usercreate='$uid' 
      AND event_type='VEVENT')";
  display_debug_msg($query, $cdg_sql, 'display_last_visited(Last created|updated event)');
  $obm_q->query($query);
  if($obm_q->next_record()) {
    $id = $obm_q->f('event_id');
    $title = htmlspecialchars($obm_q->f('event_title'));
    $dis_last .= "
      <li>
        <a id=\"last_visit_calendar_event_a\" href=\"$cal_url$id\">
         <img src=\"$GLOBALS[ico_time]\" alt=\"Calendar\" />
         <span id=\"last_visit_calendar_event_title\">$title</span>
        </a>
      </li>";
  } else {
    $dis_last .= "
      <li style=\"display:none;\">
        <a id=\"last_visit_calendar_event_a\" href=\"\">
         <img src=\"$GLOBALS[ico_time]\" alt=\"Calendar\" />
         <span id=\"last_visit_calendar_event_title\"></span>
        </a>
      </li>";

  }

  // Last calendar view
  if ($perm->get_module_rights("calendar")) {
    $dis_last .= "
      <li><a href=\"$GLOBALS[path]/calendar/calendar_index.php\" >
      <img src=\"$GLOBALS[ico_calendar]\" alt=\"Calendar\" />$GLOBALS[l_module_calendar]</a></li>";
  }
  $block .= "
  <div class=\"portlet\" id=\"lastVisitPortlet\"> 
    <h1>$l_last_visit</h1>
    <ul>
    $dis_last
    </ul>
  </div>";

  return '';//$block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the Todo display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_todos($path='..') {

  if (! $GLOBALS['cgp_show']['module']['todo']) {
    return '';
  }

  // If a theme specif function is provided
  if (function_exists('display_todos_theme')) {
    return display_todos_theme($path);
  }

  global $l_module_todo;

  $nb = count($_SESSION['todos']);

  for ($i = 1; $i <= $nb; $i++) {
    $t_id = $_SESSION['todos'][$i]['id'];
    $t_title = $_SESSION['todos'][$i]['title'];
    $todo_url = url_prepare("$GLOBALS[path]/todo/todo_index.php?action=detailconsult&amp;todo_id=$t_id");
    $todo_title = substr($t_title, 0, 30);
    $block_todo .= "
      <li><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($nb >= 1) {
    $block = "<div class=\"portlet\" id=\"todoPortlet\">
      <h1>$l_module_todo</h1>
      <ul>
      $block_todo
     </ul>
      </div>";
  } else {
    $block = '';
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : end of an HTML page
///////////////////////////////////////////////////////////////////////////////
function display_end() { 

  // If a theme specif function is provided
  if (function_exists('display_end_theme')) {
    return display_end_theme();
  }

  return "
    </body>
    </html>
";
}

///////////////////////////////////////////////////////////////////////////////
// Display an entity dates and users creation / update infos
// Parameters:
//   - $obm_q : DBO object : used fields
//          usercreate userupdate timecreate timeupdate datasource_name privacy
///////////////////////////////////////////////////////////////////////////////
function display_record_info($obm_q) {
  global  $l_infos , $l_public, $l_private, $ico_data;
  global  $ico_privacy, $ico_new ,$ico_update;

  if (is_object($obm_q)) {
    $usercreate = $obm_q->f('usercreate');
    $userupdate = $obm_q->f('userupdate');
    $timecreate = new Of_Date($obm_q->f('timecreate'), 'GMT');
    $timeupdate = new Of_Date($obm_q->f('timeupdate'), 'GMT');
    $datasource = $obm_q->f('datasource_name');
    $privacy = ($obm_q->f('privacy') == 0 ? $l_public : $l_private);
    $created = "$usercreate : ".$timecreate->getOutputDateTime();
    $updated = "$userupdate : ".$timeupdate->getOutputDateTime();

    if ($datasource != '') {
      $dis_dsrc = "<li><img src=\"$ico_data\" alt=\"Datasource\" /> : $datasource</li>";
    } else {
      $dis_dsrc = '';
    }
    
    $block = "<div class=\"portlet\" id=\"recordPortlet\"><h1>$l_infos</h1>
      <ul>
      <li><img src=\"$ico_new\" alt=\"created by\"/> $created</li>
      <li><img src=\"$ico_update\" alt=\"updated by\"/> $updated </li>
      <li><img src=\"$ico_privacy\" alt=\"privacy\" /> : $privacy</li>
      $dis_dsrc
      </ul>
  </div>";
  }

  return $block;
}



///////////////////////////////////////////////////////////////////////////////
// Display : debug
///////////////////////////////////////////////////////////////////////////////
function display_debug() {
  global $l_dg_sql, $l_dg_param, $l_dg_sess, $l_dg_solr, $global_dis_debug_custom;
  global $global_dis_debug_msg, $global_dis_debug_solr, $global_dis_debug_param, $dis_session;
  global $cdg_param, $cdg_sql, $cdg_exe, $cdg_sess, $cdg_solr;

  if (debug_level_isset($cdg_sql) || debug_level_isset($cdg_exe)) {
    $dis_debug_sql = "
      <h1>$l_dg_sql</h1>
      $global_dis_debug_msg";
  }

  if (debug_level_isset($cdg_solr)) {
    $dis_debug_solr = "
      <h1>$l_dg_solr</h1>
      $global_dis_debug_solr";
  }

    
  if (debug_level_isset($cdg_param)) {
    $dis_debug_param = "
      <h1>$l_dg_param</h1>
      $global_dis_debug_param";
  }
    
  if (debug_level_isset($cdg_sess)) {
    $dis_session_content = "
      <h1>$l_dg_sess</h1>
      $dis_session";
  }
  if ($_SESSION['set_debug'] > 0) {
    $dis_debug_custom = "
      <h1>$l_dg_custom</h1>
      $global_dis_debug_custom";
  }
  
  if ($_SESSION['set_debug'] > 0) {
    return "
      <div class=\"debug\">".
      $dis_session_content.
      $dis_debug_sql.
      $dis_debug_solr.
      $dis_debug_param.
      $dis_debug_custom.
      "</div>";
  } else { 
    return '';
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display : title
///////////////////////////////////////////////////////////////////////////////
function display_title($title) { 

  return "<div class=\"title\">$title</div>";
}

///////////////////////////////////////////////////////////////////////////////
// No comment...
///////////////////////////////////////////////////////////////////////////////
function beautify_title($module, $action) {
  global $actions, $display;
  
  $mod_title = $GLOBALS["l_module_$module"];
  if(!empty($actions[$module][$action]['Title'])) {
    $act_title = '- '.$actions[$module][$action]['Title'].' ';
  } elseif(!empty($actions[$module][$action]['Name'])) {
    $act_title = '- '.$actions[$module][$action]['Name'].' ';
  }

  if (empty($display['title'])) {
    $display['title'] = "<h1 class=\"title\">$mod_title $act_title</h1>";
  } else if ($display['title'] == strip_tags($display['title'])) {
    $display['title'] = "<h1 class=\"title\">$mod_title $act_title: $display[title]</h1>";
  } 
}


///////////////////////////////////////////////////////////////////////////////
// Display an OK message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg, $special=true) {

  if ($msg != '') {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p id=\"message_ok\" name=\"displayMessageOk\" class=\"message ok\">$msg</p>";
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

  if ($msg != '') {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p name=\"displayMessageInfo\" class=\"message info\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display a warning message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg, $special=true, $warning_class=NULL) {
  if ($msg != '') {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p name=\"displayMessageWarning\" class=\"message warning";
    if(!is_null($warning_class)) {
    	$ret .= " ".$warning_class;
    }
    $ret .= "\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display an error message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg, $special=true, $msg_class=NULL) {

  if ($msg != '') {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p id=\"message_ko\" name=\"displayMessageError\" class=\"message error";
    if(!is_null($msg_class)) {
    	$ret .= " ".$msg_class;
    }
    $ret .= "\">$msg</p>";
  }
  
  return $ret;
}

///////////////////////////////////////////////////////////////////////////////
// Display document form
// Parameters:
//   - $params[] : 
//   - $label    :  
///////////////////////////////////////////////////////////////////////////////
function display_document_form($params, $label='') {
  global $l_document;

  if($label == '') {
    $label = $l_document;
  }

  if (isset($params['file'])) { $file = $params['file']; }

  $block = "<tr>
    <th class=\"$class[file]\">$label</th>
    <td>
      <input type=\"file\" name=\"fi_file\" size=\"30\" value=\"$file\" /></td>
  </tr>";

  return $block;

}

function get_sections_data() {
  global $cgp_show, $perm;

  $sections_data = array();
  $exclusions = array("my", "webmail");

  foreach($cgp_show['section'] as $section => $value){
    $section_right = $perm->get_section_rights($section);
    $in_more_menu = !in_array($section, $exclusions);
    $have_content = $value && isset($value['url']);
    if ( $section_right && $in_more_menu && $have_content ) {
      $sections_data[] = get_module_data($section, null);
    }
  }

  return $sections_data;
}

/*//////////////////////////////////////////////////////////////////////////////
// Templating : Returns the section menus
// Parameters:
//   - $section : selected section
//   - $module  : selected module

  $data[] = array(
    "section_name" => "section-key",
    "name" => "section-name",
    "links" => array( 
      array( 'url' => "uri", "ico" => src_to_ico, "title" => "TxT", "tooltip" => "TxT"),
      array( 'url' => "uri", "ico" => src_to_ico, "title" => "TxT",  "tooltip" => "TxT"),
    )
  );
//////////////////////////////////////////////////////////////////////////////*/
function get_module_data($section, $module) {
  global $cme_txt, $cme_ico, $cme_both;
  global $path, $perm, $modules, $cgp_show, $sections;
  global $obm;

  $userInfo = get_user_info($obm['uid']);
  $modulesRequiringEmail = array("webmail_pref", "mailforward", "vacation", "mailbox");
  $userHasEmail = $userInfo["mail_perms"] == 1 && $userInfo["email"];

  if (($_SESSION['set_menu'] == $cme_ico) || ($_SESSION['set_menu'] == $cme_both))
    $fico = true;
  else $fico = false;

  if (($_SESSION['set_menu'] == $cme_txt) || ($_SESSION['set_menu'] == $cme_both))
    $ftext = true;
  else $ftext = false;

  $section_link = array_keys($cgp_show['module'],$section);
  if (is_array($section_link) ) {
    $id = $section.'Items';

    $module_data = array();

    $l_section = 'l_section_'.$section;

    $module_data["name"] = $sections[$section]['Name'] != "" ? $sections[$section]['Name'] : $GLOBALS[$l_section];
    $module_data["section_name"] = $section;

    $module_data["links"] = array();
    $exclusions = array($GLOBALS['l_module_calendar'], $GLOBALS['l_module_contact'], $GLOBALS['l_module_planning'], $GLOBALS['l_module_webmail']);

    foreach ($section_link as $mod) {
      $link_name = $modules[$mod]['Name'];

      if (!in_array($link_name, $exclusions) && ($userHasEmail || !in_array($mod, $modulesRequiringEmail))) {
        $link_tooltip = ( !empty($modules[$mod]['Tooltip']) ) ? $modules[$mod]['Tooltip'] : $modules[$mod]['Name'];
        $link_url = $modules[$mod]['Url'];
        $link_id = $modules[$mod]['Id'];

        $link_right = $perm->get_module_rights($mod);
        if (($link_right & $modules[$mod]['Right']) == $modules[$mod]['Right']) {

          $link_target = $modules[$mod]['Target'];
          if (!isset($link_target)) {
            $link_target = "_self";
          }

          if ($fico) {
            $link_ico = $modules[$mod]['Ico'];
          }

          $module_data["links"][] = array(
            "url"     => $link_url,
            "target"  => $link_target,
            "ico"     => $link_ico,
            "title"   => $link_name,
            "tooltip" => $link_tooltip,
            "id" => $link_id
          );
        }
      }
    }
    return $module_data;
  }
}

?>
