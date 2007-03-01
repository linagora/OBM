<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_category.inc                                              //
//     - Desc : OBM Framework Category (mono and muli) handling              //
// 2005-08-27 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//--- Multi Categories with tree --------------------------------------------//
//
// Tables
// EntityCategoryN (id, time and user * update and create, code, label)
// EntityCategoryNLink (category_id, entity_id)

require_once("$obminclude/javascript/category_js.inc");
require_once("$obminclude/of/of_select.inc");

$extra_js .= "
///////////////////////////////////////////////////////////////////////////////
// Category Functions
///////////////////////////////////////////////////////////////////////////////
NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	

function show_hide_dir(name) {

  if (W3C) {
    if(document.getElementById('div_'+name).style.display != \"none\") {
      hide(name);
    } else {
      show(name);
    }
  } else if (NS4) {
    if(document.layers['div_'+name].display != \"none\") {
      hide(name);
    } else{
      show(name);
    }
  } else {
    if (document.all['div_'+name].style.display != \"none\") {
      hide(name);
    } else{
      show(name);
    }
  }

}

function hide ( name ) {
  if (W3C) {
    document.getElementById('div_'+name).style.display = \"none\";
    document.getElementById('div_'+name).style.height = \"0px\";
    document.getElementById('img_'+name).src = \"$ico_folder_close\";    
  } else if (NS4) {
    document.layers['div_'+name].display = \"hide\";
    document.layers['div_'+name].height = \"0px\";
    document.layers['img_'+name].src = \"$ico_folder_close\";    
  } else {
    document.all['div_'+name].style.display = \"none\";
    document.all['div_'+name].style.height = \"0px\";
    document.all['img_'+name].src = \"$ico_folder_close\";
  }
}

function show ( name ) {
  if (W3C) {
    document.getElementById('div_'+name).style.display = \"\";
    document.getElementById('div_'+name).style.height = \"auto\"; 
    document.getElementById('img_'+name).src = \"$ico_folder\";
  } else if (NS4) {
    document.layers['div_'+name].display = \"\";
    document.layers['div_'+name].height = \"auto\";
    document.layers['img_'+name].src = \"$ico_folder\";
  } else {
    document.all['div_'+name].style.display = \"\";
    document.all['div_'+name].style.height = \"auto\";
    document.all['img_'+name].src = \"$ico_folder\";
  }
}

";

if (isset($params["ext_element"]) && $params["ext_element"]!="") {
  $extra_js .= "

/*function fill_ext_form(dir_path) {
  ext_field = window.opener.document.$params[ext_element];
  ext_field.value = dir_path;
  window.close();
} */

function fill_ext_cat_element(int_form) {
  size = int_form.length;
  for (i=0; i <size; i++) {
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
	elemid = '$params[ext_element]-data-'+id;
        if (!get_elem(window.opener.document, elemid)) {
          elemid = 'data'+id;
	  span = get_elem(window.document,elemid);
	  of_select_add_element(window.opener.document, '$params[ext_element]', id, span.innerHTML);
        }
      }
    }
  }
}


// Select the category with the code given
/*
function fill_category_ext_code_form(value) {
  ext_field = window.opener.document.$params[ext_element];
  ext_size = ext_field.length;
  ext_field = window.opener.document.$params[ext_element];
  bool= false;
  for(j=0; j< ext_size && bool == false; j++) {
    if (ext_field.options[j].value == value) {
      ext_field.options[j].selected = true;
      bool=true;
    }
  }
  window.close();
}
*/

";
}


//=============================================================================
// OF Category API
//=============================================================================

// Display functions ----------------------------------------------------------

///////////////////////////////////////////////////////////////////////////////
// Display: category search block 
// Parameters:
//   - $entity        : target entity
//   - $cat           : target category
//   - $cats          : Category array
//   - $cat_id        : selected target category id
//   - $first_element : scalar or array : (["all"|"none"])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_search_select($entity, $cat, $cats, $cat_id, $first_element="all", $sel_append="") {
  global $cgp_hide, $l_all;

  $l_cat = "l_${cat}"; 
  global $$l_cat;

  if (! ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"])) {
    $sel_cat = of_category_html_select($entity, $cat, $cats, $cat_id, $first_element, $sel_append);

    $block_cat = "
    <label>${$l_cat}<br />
      $sel_cat
    </label>";
  }

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display the category select (add and delete) for an entity form
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $cats   : category to display
//   - $mode   : ([mono|multi])
//   - $cat_id : (mono mode) category selected
//   - $first  : (mono mode) first element, scalar or array : (["all"|"none"])
// Returns
//   XHTML display of category form part
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_entity_form($entity, $cat, $cats, $mode="multi", $cat_id="", $first="") {
  global  $ico_add, $ico_delete, $popup_height, $popup_width;
  global $cgp_hide, $l_none;
  
  $show_cat = (! $cgp_hide["$entity"]["${cat}"]);

  // Category select field
  if (! ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"])) {

    if ($mode == "mono") {
      $sel_cat = of_category_html_select($entity, $cat, $cats, $cat_id, $first);
      $class = "detailForm";
    } else {
      if (is_array($cats)) {
        foreach ($cats as $one_cat) {
	  $c_id = $one_cat["id"];
          $c_code = $one_cat["code"];
	  $c_label = htmlentities($one_cat["label"]);
	  $sel_name = "sel_${cat}";
	  $div_id = "$sel_name-data-$c_id";
	  $sel_cat .= "<div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','$sel_name');\">
          <img src=\"$ico_delete\">
          </a>
          $c_code $c_label
          <input value=\"$c_id\" name=\"${sel_name}[]\" type=\"hidden\" />
          </div>";
        }
      }
      $url = "${entity}_index.php?action=ext_get_${cat}_ids&amp;popup=1&amp;ext_element=sel_${cat}&amp;ext_title=${$l_cat}";
      $link="<a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
      $class = "detail";
    }

    $l_cat = "l_${cat}"; 
    global $$l_cat;

    $block_cat = "
    <tr>
      <th>$link ${$l_cat} :</th>    
      <td id=\"sel_${cat}\">$sel_cat</td>
    </tr>";
  }

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display: category html select component
// Parameters:
//   - $entity        : target entity
//   - $cats          : Category array
//   - $cat           : target category
//   - $cat_id        : selected target category
//   - $first_element : scalar or array : (["all"|"none"])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_category_html_select($entity, $cat, $cats, $cat_id, $first_element, $sel_append="") {
  global $cgp_hide;

  if (! ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"])) {

    if (is_array($first_element)) {
      foreach($first_element as $element) {
	$l_element = "l_${element}";
	$c_element = "c_${element}"; 
	global $$l_element, $$c_element;
	if ($$c_element == $cat_id) {
	  $first_option .= "
	<option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
	} else {
	  $first_option .= "
	<option value=\"${$c_element}\">${$l_element}</option>";
	}
      }
    } else if ($first_element != "") {
      $l_element = "l_${first_element}";
      $c_element = "c_${first_element}"; 
      global $$l_element, $$c_element;
      if ($$c_element == $cat_id) {
	$first_option .= "
	<option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
      } else {
	$first_option .= "
	<option value=\"${$c_element}\">${$l_element}</option>";
      }
    }

    // Category select
    $sel_cat = "<select id=\"sel_${cat}\" name=\"sel_${cat}\" $sel_append>
      $first_option";
    if (is_array($cats)) {
      $has_code = false;
      foreach ($cats as $one_cat) {
        if ($one_cat["code"] != "") {
	  $has_code = true;
	  break;
	}
      }
      foreach ($cats as $one_cat) {
        $c_id = $one_cat["id"];
	if ($has_code) {
	  $c_code = str_pad($one_cat["code"] . " ", 10, ".");
	}
        $c_label = htmlentities($one_cat["label"]);
        $sel_cat .= "\n<option value=\"$c_id\"";
        if ($c_id == $cat_id) { $sel_cat .= " selected=\"selected\""; }
        $sel_cat .= ">$c_code $c_label</option>";
      }
    }
    $sel_cat .= "</select>";
  }

  return $sel_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display: category consult block
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $cats   : category to display
//   - $mode   : ([mono|multi])
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_block_consult($entity, $cat, $cats, $mode="multi") {
  global $cgp_hide;

  $l_cat = "l_${cat}";
  global $$l_cat;

  if (! ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"])) {

    // Mode mono
    if ($mode == "mono") {
      // Mono Link
      $label = htmlentities($cats[0]["label"]); 
      $block_cat = "
      <tr><th>".ucfirst(${$l_cat})." </th><td>$label</td></tr>
      ";

    } else {
      // Multi Link
      $nb_col_cat = 3;

      $i = 0;
      if (is_array($cats)) {
        foreach ($cats as $one_cat) {
          if ($i==0) {
            $dis_cat .= "<tr>";
          }
          $label = htmlentities($one_cat["label"]);
          $code = $one_cat["code"];
          $i++;
          $dis_cat .= "<td>$code $label </td>";
          if ($i==$nb_col_cat) {
            $dis_cat .= "</tr>";
            $i = 0;
          }
        }
      }
      $block_cat = "
      <div class=\"detail infos\">
      <h1>".ucfirst(${$l_cat})."</h1>
      <table>
      $dis_cat
      </table>
      </div>";
    }

  }

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display a category Tree (category with code like 1.4.10) with input
// Input type is checkbox for ext_get_cat_ids else link
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $params : hash of module values
//   - $action : action called
// Returns
//   XHTML display of category tree with form if needed
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_tree($entity, $cat, $params, $action) {
  global $display, $l_close, $l_add, $l_cat_no;

  $popup = $params["popup"];
  $ext_title = $params["ext_title"];
  $cats = of_category_get_ordered($entity, $cat);
  $display["title"] = "<div class=\"title\">$ext_title</div>";

  $block .= "
  <div class=\"categoryTree\">";

  $multi_select = false;
  if (preg_match("/ext_get_category[1-9]?_ids/", $action) > 0) {
    $multi_select = true;
  }

  // For multiple select we set a form
  if ($multi_select) {
    $block .= "
    <form onsubmit=\"fill_ext_cat_element(this); window.close(); return false;\">";
  }

  if (is_array($cats)) {

    // Check if code are there
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }
    $cpt_no_code = 1;
    foreach ($cats as $cat_info) {
      if ($cat_info["code"] != "") {
	$hash_code = explode(".",$cat_info["code"]);
	$deep = count($hash_code);
	switch ($deep) {
        case 1:
	  $tree[$hash_code[0]] = $cat_info;
	  break;
        case 2:
	  $tree[$hash_code[0]][$hash_code[1]] = $cat_info;
	  break;
        case 3:
	  $tree[$hash_code[0]][$hash_code[1]][$hash_code[2]] = $cat_info;
	  break;
	}
      } else {
	// Handle categories where code is not filled (automatic numbering)
	$no_code = "X";
	$tree["$no_code"][$cpt_no_code] = $cat_info;
	$tree["$no_code"]["code"] = "$no_code";
	$tree["$no_code"]["label"] = "No code";
	$tree["$no_code"][$cpt_no_code]["code"] = "$no_code.$cpt_no_code";
	$cpt_no_code++;
      }
    }
    if (is_array($tree)) {
      $block .= _of_category_dis_tree_node($tree, 0, $multi_select);
    }
  } else {
    $block .= "$l_cat_no";
  }

  if ($multi_select) {
    $block .= "
    <br />
    <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
    <input type=\"submit\" value=\"$l_add\" /> &nbsp;&nbsp;&nbsp;
    <input type=\"button\" value=\"$l_close\" onclick=\"window.close();\"/>   
    </form>";
  }

  $block .= "</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Admin Category section
// Parameters:
//   - $catu   : target category (string) can have Uppercase eg: CompanyRole
//   - $cats   : Category array
//   - $user   : User Category ? default false
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_admin_form($catu, $cats, $user=false) {
  global $l_c_manage, $l_c_update, $l_c_new, $l_code, $l_label, $ico_delete;
  global   $l_c_insert, $l_c_j_create, $l_c_j_insert, $l_c_j_update, $ico_ok;

  $cat_name = strtolower($catu);
  $l_cat = "l_${cat_name}";
  global $$l_cat;

  // cat_name is used for form names (to have differents ones)
  // cat is used for fields (no problemes if 2 tf_category_label in 2 forms)
  if ($user) {
    $cat = "category";
  } else {
    $cat = $cat_name;
  }

  $jslabel = addslashes(ucfirst(${$l_cat}));
  if (is_array($cats)) {
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }
    foreach ($cats as $key => $one_cat) {
      $c_id = $one_cat["id"];
      if ($has_code) {
	$c_code = $one_cat["code"];
      }
      if($key%2 == 0) {
        $class = "class=\"pair\"";
      } else {
        $class = "";
      }
      $c_label = htmlentities($one_cat["label"]);
      $cat_list .= "
        <tr $class>
        <td>
        <input type=\"hidden\" id=\"${cat}_id_$c_id\" value=\"$c_id\" />
        <a href=\"?action=${cat}_checklink&amp;category=$cat_name&amp;${cat}_id=$c_id\">
        <img src=\"$ico_delete\" alt=\"$l_c_checkdelete\" title=\"$l_c_checkdelete\" />
        </a></td>
        <td> <input type=\"text\" id=\"tf_${cat}_code_$c_id\" value=\"$c_code\" size=\"6\"/></td>
        <td> <input type=\"text\" id=\"tf_${cat}_label_$c_id\" value=\"$c_label\" /></td>
        <td>
        <a href=\"#\" onclick=\"updateLine($c_id,'$cat_name')\">
        <img src=\"$ico_ok\" alt=\"$l_c_update\" title=\"$l_c_update\" />
        </a>
	</td>
        </tr>
        ";
    }
  }
  $sel_cat .= "</select>";


  $block = "
    <div class=\"detail admin\" >
    <form id=\"form_".$cat_name."_update\" action=\"?action=${cat}_update\" method=\"post\"
     onsubmit=\"return check_category(this.tf_${cat}_label,'$jslabel')\">
    <input type=\"hidden\" name=\"tf_${cat}_code\" value=\"\" size=\"6\"/>
    <input type=\"hidden\" name=\"tf_${cat}_label\" value=\"\" />
    <input type=\"hidden\" name=\"${cat}_id\" value=\"\" />
    <input type=\"hidden\" name=\"category\" value=\"$cat_name\" />
    </form>    
    <h1>$l_c_manage ${$l_cat}</h1>
    <div class=\"overflow\">
    <table>
    <thead>
    <tr>
    <td>&nbsp;</td>
    <td>$l_code</td>
    <td>$l_label</td>
    <td>&nbsp;</td>        
    </tr>    
    </thead>
    <tbody>
    $cat_list
    </tbody>
    </table>
    </div>
    <form id=\"form_".$cat_name."_new\" action=\"\" method=\"post\"
    onsubmit=\"return check_category(this.tf_${cat}_label,'$jslabel')\">
    <table>
    <thead>
    <tr>
    <td colspan=\"2\">$l_c_new</td>
    </tr>
    <tbody>   
    <tr>
    <td>$l_code</td>
    <td>$l_label</td>    
    </tr>       
    <tr>
    <td><input type=\"text\" name=\"tf_${cat}_code\" value=\"\" size=\"6\"/></td>
    <td><input type=\"text\" name=\"tf_${cat}_label\" value=\"\" /></td>    
    </tr>  
    </tbody>
    <tfoot>
    <tr>
    <td colspan=\"2\"><input type=\"submit\" value=\"$l_c_insert\" /></td>
    </tr>
    </tfoot>
    </table>
    <input type=\"hidden\" name=\"action\" value=\"${cat}_insert\" />
    <input type=\"hidden\" name=\"category\" value=\"$cat_name\" />
    </form>
    </div>
      ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the cat links
// Parameters:
//   - $entities   : target entity : if an array, check for each entities
//   - $cat        : target category
//   - $params     : Entity parameters array
//   - $mode       : ([mono|multi])
//   - $link_table : [optionnal] to handle special
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_links($entities, $cat, $params, $mode="multi") {
  global $l_c_link, $l_c_link_no;
  global $l_c_delete, $l_c_can_delete, $l_c_cant_delete;
  global $l_back, $display;

  $l_cat = "l_${cat}";
  global $$l_cat;

  $has_links = false;
  $id = $params["${cat}_id"];
  if (! ($id > 0)) {
    $id = $params["${cat}"];
  }

  // Category is multi-entities (check for each entity)
  if (is_array($entities)) {
    $label = of_category_get_label("", $cat, $id);
    foreach($entities as $entity) {
      $l_entity = strtolower("l_${entity}");
      global $$l_entity;

      $obm_q = of_category_query_links($entity, $cat, $id, $mode);

      $nb_cat = $obm_q->num_rows();
      if ($nb_cat > 0) {
	$has_links = true;
        $url = url_prepare("?action=search&amp;sel_${cat}=$id&amp;cba_archive=1");

	$dis_link .= "<h1>".ucfirst(${$l_cat})."($label) : $l_c_link ${$l_entity} <a href=\"$url\">($nb_cat)</a></h1>";
      } else {
	$dis_link .= "<h1>".ucfirst(${$l_cat})."($label) : $l_c_link_no (${$l_entity}).</h1>";
      }
    }

    // Category is dedicated to an entity
  } else {
    $entity = $entities;
    $l_entity = strtolower("l_${entity}");
    global $$l_entity;
    $label = of_category_get_label($entity, $cat, $id);
    $obm_q = of_category_query_links($entity, $cat, $id, $mode);

    $nb_cat = $obm_q->num_rows();
    if ($nb_cat > 0) {
      $has_links = true;
      $url = url_prepare("?action=search&amp;sel_${cat}=$id&amp;cba_archive=1");

      $dis_link .= "<h1>".ucfirst(${$l_cat})."($label) : $l_c_link ${$l_entity} <a href=\"$url\">($nb_cat)</a></h1>";
    } else {
      $dis_link .= "<h1>".ucfirst(${$l_cat})."($label) : $l_c_link_no (${$l_entity})</h1>";
    }
  }

  if ($has_links) {
    $display["msg"] .= display_warn_msg(ucfirst(${$l_cat}).": $l_c_cant_delete.");
  } else {
    $display["msg"] .= display_ok_msg(ucfirst(${$l_cat}).": $l_c_can_delete.");

    $dis_buttons .= "<a href=\"?action=${cat}_delete&amp;sel_${cat}=$id\">$l_c_delete</a>";
  }

  $dis_buttons .= "<a href=\"?action=admin\">$l_back</a>";

  $block = "
  <div class=\"detail\">
  $dis_link
  </div>
  <div class=\"buttons\">
    $dis_buttons
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the category for one entity 
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $cats   : array for category stats
//   - $order  : column to sort results
// Returns:
//   - $block  : html block
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_entity_stats($entity, $cat, $cats, $order) {
  global $l_totals;

  $l_cat = "l_${cat}"; $l_entity = "l_${entity}";
  global $$l_cat,$$l_entity;

  $title = ucfirst(${$l_cat});

  if (is_array($cats)) {
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }

    if ($order == "code") {
      if ($has_code) {
	usort($cats, "_of_category_compare_code");
      } else {
	usort($cats, "_of_category_compare_label");
      }
    } else {
      usort($cats, "_of_category_compare_nb");
    }

    $count_entity = _of_category_get_entity_count($entity);
    $max = 1;
    foreach($cats as $cat_id=>$one_cat) {
      $stat[$cat_id]["$cat"] = $one_cat["nb"];
      if ($has_code) {
	$stat[$cat_id]["name"] = $one_cat["code"]." - ".$one_cat["label"];
      } else {
	$stat[$cat_id]["name"] = $one_cat["label"];
      }
      if ($one_cat["nb"] > $max) {
	$max = $one_cat["nb"];
      }
    }
  }

  $block .= "<div class=\"detailHead\">$title</div>
  <table class=\"detail\" >
  <tr>
    <td class=\"detailLabel\" class=\"detailLabel\"><a href=\"?action=statistics&amp;stat=$cat&amp;order=code\">$title</a><br/></td>
    <td class=\"detailForm\">
      <table>
      <tr><td style=\"width:20px;background-color:#DE5B5B;\">&nbsp;</td>
        <td><a href=\"?action=statistics&amp;stat=$cat&amp;order=nb\">${$l_entity}</a> ($l_totals : $count_entity)</td>
      </tr>
      </table>
      <br/>
    </td>
  </tr>";

  if (is_array($stat)) {
    foreach($stat as $id => $value) {
      $ents = round(($value["$cat"]/$max)*90);
      $block .= "<tr>
    <td class=\"detailLabel\">".$value["name"]."</td>
    <td class=\"detailForm\">
     <table style=\"width:100%;\">
      <tr><td style=\"width:".$ents."%; background-color:#DE5B5B;align:left\"></td><td>".$value["$cat"]."</td></tr>
     </table>
    </td>
   </tr>";
    }
  }

  $block .= "
  <tr>
    <td class=\"detailLabel\" class=\"detailLabel\"><br/>$title</td>
    <td class=\"detailForm\">
     <br/>
     <table>
      <tr><td style=\"width:20px;background-color:#DE5B5B;\">&nbsp;</td><td>${$l_entity} ($l_totals : $count_entity)</td></tr>
     </table>
    </td>
  </tr>
  </table>";

  return $block;
}


// Data functions -------------------------------------------------------------


///////////////////////////////////////////////////////////////////////////////
// Category query execution
// Parameters:
//   - $entity    : target entity
//   - $catu      : target category (string, may have upper case)
//   - $mode      : ([mono|multi]) : to retrieve cats when restricted to entry 
//   - $entity_id : if not 0, get only cat referenced by this entry
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_category_get_ordered($entity, $catu, $mode="multi", $entity_id=0) {
  global $cdg_sql, $cgp_hide;

  $cat = strtolower($catu);
  $cats = array();
  if ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"]) {
    return $cats;
  }

  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = strtolower($entity.$cat."_");
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";
  $multidomain = sql_multidomain(strtolower($table));

  // If an entity id is given, restrict categories to those of this entry
  if ( ($mode == "multi") && ($entity_id > 0)) {
    $query = "SELECT $table.*
      FROM $linktable
        LEFT JOIN $table ON $linkfield_cat = $field_id
      WHERE $linkfield_ent = '$entity_id' $multidomain
      ORDER BY $field_code, $field_label";
  } else {
    $query = "SELECT * FROM $table WHERE 1=1 $multidomain ORDER BY $field_code, $field_label";
  }
  $cat_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "of_category_get_ordered()");
  $cat_q->query($query);
  
  while ($cat_q->next_record()) {
    $id = $cat_q->f($field_id);
    $code = $cat_q->f($field_code);
    $label = $cat_q->f($field_label);
    $cat_info = array("id" => $id,
		      "label" => $label,
		      "code" => $code);
    $cats[] = $cat_info;
  }

  if (is_array($cats)) {
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }

    if ($has_code) {
      if (count($cats) > 0) {
	usort($cats, "_of_category_compare_code");
      }
    }
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Category insertion query construction and execution
// Parameters:
//   - $entity  : target entity
//   - $catu    : target category (string, may have upper case)
//   - $params  : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_insert($entity, $catu, $params) {
  global $obm, $cdg_sql, $obm;

  $cat = strtolower($catu);
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $cat_label = $params["${cat}_label"];
  $cat_code = $params["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = strtolower($entity.$cat."_");
  $field_timecreate = $field_prefix."timecreate";
  $field_usercreate = $field_prefix."usercreate";
  $field_label = $field_prefix."label";
  $field_code = $field_prefix."code";
  $field_domain = $field_prefix."domain_id";
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO $table (
    $field_timecreate,
    $field_usercreate,
    $field_domain,
    $field_label,
    $field_code)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$domain_id',
    '$cat_label',
    '$cat_code')";

  display_debug_msg($query, $cdg_sql, "of_category_query_insert()");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Category link insertion query construction and execution
// Parameters:
//   - $entity_id : entity id 
//   - $cat_id    : category id 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_insert_link($entity, $cat, $entity_id, $cat_id) {
  global $cdg_sql;

  if ( ($entity_id > 0) && ($cat_id > 0) ) {
    $linktable = ucfirst($entity) . ucfirst($cat)."Link";
    $linkfield_ent = $entity.$cat."link_".$entity."_id";
    $linkfield_cat = $entity.$cat."link_category_id";
		
    $query = "INSERT INTO $linktable (
       $linkfield_ent,
       $linkfield_cat)
     VALUES (
       '$entity_id',
       '$cat_id')";
    display_debug_msg($query, $cdg_sql, "of_category_query_insert_link()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category update query execution
// Parameters:
//   - $entity : target entity
//   - $catu   : target category (string, may have upper case)
//   - $params : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_update($entity, $catu, $params) {
  global $obm, $cdg_sql;

  $cat = strtolower($catu);
  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $cat_id = $params["${cat}_id"];
  if (! ($cat_id > 0)) {
    $cat_id = $params["${cat}"];
  }
  $cat_label = $params["${cat}_label"];
  $cat_code = $params["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = $entity.$cat."_";
  $field_timeupdate = $field_prefix."timeupdate";
  $field_userupdate = $field_prefix."userupdate";
  $field_id = $field_prefix."id";
  $field_label = $field_prefix."label";
  $field_code = $field_prefix."code";
  $multidomain = sql_multidomain(strtolower($table));
	
  $query = "UPDATE $table SET
    $field_timeupdate = '$timeupdate',
    $field_userupdate = '$userupdate',
    $field_label = '$cat_label',
    $field_code = '$cat_code'
  WHERE
    $field_id='$cat_id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "of_category_query_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category deletion query execution
// Parameters:
//   - $entity  : target entity
//   - $catu    : target category (string, may have upper case)
//   - $params  : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_delete($entity, $catu, $params) {
  global $cdg_sql;

  $cat = strtolower($catu);
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $id = $params["${cat}_id"];
  if (! ($id > 0)) {
    $id = $params["${cat}"];
  }

  $multidomain = sql_multidomain(strtolower($table));

  if ($id != '') {
    $query = "DELETE FROM $table WHERE $field_id ='$id' $multidomain";

    display_debug_msg($query, $cdg_sql, "of_category_query_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  } else {
    $retour = 1;
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category link deletion query construction and execution
// Parameters:
//   - $entity    : target entity
//   - $cat       : target category
//   - $entity_id : entity id 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_delete_link($entity, $cat, $entity_id) {
  global $cdg_sql;

  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";

  $query = "DELETE
    FROM $linktable 
    WHERE $linkfield_ent = '$entity_id'";

  display_debug_msg($query, $cdg_sql, "of_category_query_delete_link()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category - Entity links query execution
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $p_id   : category id
//   - $mode   : ([mono|multi]) 
// Return:
//   - database handle
///////////////////////////////////////////////////////////////////////////////
function of_category_query_links($entity, $cat, $p_id, $mode="multi") {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $linkfield_prefix = strtolower($entity.$cat."link_");
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $linkfield_prefix.$entity."_id";
  $linkfield_cat = $linkfield_prefix."category_id";
  if ($entity == "calendar") {
    $entitytable = "CalendarEvent";
    $entityfield_prefix = "calendarevent_";
  } else {
    $entitytable = ucfirst($entity);
    $entityfield_prefix = strtolower($entity."_");
  }
  $entityfield_id = $entityfield_prefix."id";
  $entityfield_cat = $entityfield_prefix.$cat."_id";
	
  if ($mode == "mono") {
    $query = "SELECT $entityfield_id
      FROM $entitytable
      WHERE $entityfield_cat = '$p_id' $multidomain";
  } else {
    $query = "SELECT $linkfield_ent
      FROM $linktable
      WHERE $linkfield_cat = '$p_id'";
  }

  display_debug_msg($query, $cdg_sql, "of_category_query_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the entity
// Parameters:
//   - $entity    : target entity
//   - $cat       : target category
//   - $entity_id : entity id 
//   - $mode   : ([mono|multi]) 
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_category_get_entitycategories($entity, $cat, $entity_id, $mode="multi") {
  global $cdg_sql, $cgp_hide;

  $cats = array();
  if ($cgp_hide["$entity"]["$cat"] || $cgp_hide["$entity"]["$entity$cat"]) {
    return $cats;
  }

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";
  if ($entity == "calendar") {
    $entitytable = "CalendarEvent";
    $entityfield_prefix = "calendarevent_";
  } else {
    $entitytable = ucfirst($entity);
    $entityfield_prefix = $entity."_";
  }
  $entityfield_id = $entityfield_prefix."id";
  $entityfield_cat = $entityfield_prefix.$cat."_id";
  $multidomain = sql_multidomain(strtolower($table));
	
  if ($mode == "mono") {
    $query = "SELECT $field_id, $field_label, $field_code 
      FROM $entitytable, $table 
      WHERE $entityfield_cat = $field_id
        AND $entityfield_id = '$entity_id' $multidomain";
  } else {
    $query = "SELECT $field_id, $field_label, $field_code 
      FROM $linktable, $table 
      WHERE $linkfield_cat = $field_id 
        AND $linkfield_ent = '$entity_id' $multidomain
      ORDER BY $field_id";
  }

  display_debug_msg($query, $cdg_sql, "of_category_get_entitycategories()");
  $cat_q = new DB_OBM;
  if ($entity_id != "") {
    $cat_q->query($query);
  }

  while ($cat_q->next_record()) {
    $id = $cat_q->f($field_id);
    $code = $cat_q->f($field_code);
    $label = $cat_q->f($field_label);
    $cat_info = array("id" => $id,
                      "label" => $label,
                      "code" => $code);
    $cats[] = $cat_info;
  }

  if (count($cats) > 0) {
    usort($cats, "_of_category_compare_code");
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Category from its id
// Parameters:
//   - $entity    : target entity
//   - $cat       : target category
//   - $id        : category id 
// Return:
//   - category label 
///////////////////////////////////////////////////////////////////////////////
function of_category_get_label($entity, $cat, $id) {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = strtolower($entity.$cat."_");
  $field_id = $field_prefix."id";
  $field_label = $field_prefix."label";
  $multidomain = sql_multidomain(strtolower($table));
	
  $query = "SELECT $field_label 
    FROM $table 
    WHERE $field_id = '$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "of_category_get_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $retour = $obm_q->f("$field_label");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Statistic Category Per Entity query execution
// Parameters:
//   - $entity       : target entity
//   - $cat          : target category
//   - $mode   : ([mono|multi])
///////////////////////////////////////////////////////////////////////////////
function of_category_query_category_per_entity($entity, $cat, $mode="multi") {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";
  if ($entity == "calendar") {
    $entitytable = "CalendarEvent";
    $entityfield_prefix = "calendarevent_";
  } else {
    $entitytable = ucfirst($entity);
    $entityfield_prefix = $entity."_";
  }
  $entityfield_id = $entityfield_prefix."id";
  $entityfield_cat = $entityfield_prefix.$cat."_id";
  $multidomain = sql_multidomain(strtolower($table));
	
  if ($mode == "mono") {
    $query = "SELECT $field_id, $field_code, $field_label, count($entityfield_id) as nb_category
      FROM $table
        LEFT JOIN $entitytable ON $entityfield_cat = $field_id
      WHERE 1=1 $multidomain
      GROUP BY $entityfield_cat, $field_id, $field_code, $field_label
      ORDER BY $field_code, $field_label";
  } else {
    $query = "SELECT $field_id, $field_code, $field_label, count($entityfield_id) as nb_category
      FROM $table
        LEFT JOIN $linktable ON $linkfield_cat = $field_id
        LEFT JOIN $entitytable ON $entityfield_id = $linkfield_ent
      WHERE 1=1 $multidomain
      GROUP BY $linkfield_cat, $field_id, $field_code, $field_label
      ORDER BY $field_code, $field_label";
  }

  display_debug_msg($query, $cdg_sql, "of_category_query_category_per_entity()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cats = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f($field_id);
    $code = $obm_q->f($field_code);
    $label = $obm_q->f($field_label);
    $nb = $obm_q->f("nb_category");
    $cats[$id] = array("code" => $code, "label" => $label, "nb" => $nb);
  }

  return $cats;
}


//=============================================================================
// OF Category internal helper API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
//   - $tree   : array with categories
//   - $indent : indent size
//   - $multi  : tell if multi selection enabled
///////////////////////////////////////////////////////////////////////////////
function _of_category_dis_tree_node($tree, $indent, $multi=false) {
  global $ico_folder_close, $ico_folder;

  $code = $tree["code"];
  $label = $tree["label"];
  $id = $tree["id"];
  if ($code != "") {
    $node_to_display = true;
    $indent_file = $indent + 15;
  } else {
    $node_to_display = false;
    $indent_file = $indent;
  }

  // Display the node children if it has any
  if (is_array($tree)) {
    foreach ($tree as $level => $info) {
      if ("$level" != "code" && "$level" != "label" && "$level" != "id") {
	$content .= _of_category_dis_tree_node($info, $indent_file, $multi);
	$has_child = true;
      }
    }
  }

  // Display the node if it has values
  if ($node_to_display) {

    if ($multi) {
      $dis_cat = "
        <input type=\"checkbox\" name=\"$id\" />
        <span id=\"data$id\">$code - $label</span>";
    } else {
      $dis_cat = "
        <a href=\"javascript: void(0);\" onclick=\"fill_category_ext_code_form('$code'); return false;\"><span id=\"data$id\">$code - $label</span></a>";
    }

    // If the node has children : link to open it, and icon is different
    if ($has_child == true) {
      $block = "
      <table class=\"category\">
      <tr>
	<td class=\"categoryDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	  <a href=\"\" onclick=\"show_hide_dir('$code'); return false;\"> 
	  <img id=\"img_$code\" src=\"$ico_folder_close\" alt=\"\"/>
	  </a>
          $dis_cat
	</td>
      </tr>
      </table>
      <div id='div_$code' style=\"display:none;\" >
      $content
      </div>";
    } else {
      $block = "
      <table class=\"category\">
      <tr>
        <td class=\"categoryDir\" style=\"padding-left:".$indent."px;text-align:left;\">
          <img id=\"img_$code\" src=\"$ico_folder\" alt=\"\"/>
          $dis_cat
        </td>
      </tr>
      </table>";
    }
  } else {
    $block = $content;
  }
    
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Compare 2 categories by their occurence number
// Parameters: 
//   - $m   : index of first category
//   - $n   : index of second category
///////////////////////////////////////////////////////////////////////////////
function _of_category_compare_nb($m, $n) {

  if ($m["nb"] >= $n["nb"]) {
    return -1;
  } else {
    return 1;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Compare 2 categories by their label
// Parameters: 
//   - $m   : index of first category
//   - $n   : index of second category
///////////////////////////////////////////////////////////////////////////////
function _of_category_compare_label($m, $n) {

  if ($m["label"] >= $n["label"]) {
    return 1;
  } else {
    return -1;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Compare 2 categories with their code. Order 1.15 ans 1.2 correctly
// Parameters: 
//   - $m : index of first category
//   - $n : index of second category
///////////////////////////////////////////////////////////////////////////////
function _of_category_compare_code($m, $n) {

  $code_1 = explode(".",$m["code"]);
  $code_2 = explode(".",$n["code"]);
  $i=0;
  while (isset($code_1[$i]) || isset($code_2[$i])) {
    if ($code_1[$i] > $code_2[$i]) {
      return 1;
    } elseif ($code_1[$i] < $code_2[$i]) {
      return -1;
    }
    $i++;
  }

  return 0;
}


///////////////////////////////////////////////////////////////////////////////
// Entity count query execution
// Parameters:
//   - $entity       : target entity
///////////////////////////////////////////////////////////////////////////////
function _of_category_get_entity_count($entity) {
  global $cdg_sql;

  if ($entity == "calendar") {
    $entitytable = "CalendarEvent";
  } else {
    $entitytable = ucfirst($entity);
  }

  $multidomain = sql_multidomain(strtolower($entitytable));

  $query = "SELECT count(*) as count_entity FROM $entitytable WHERE 1=1 $multidomain";

  display_debug_msg($query, $cdg_sql, "_of_category_get_entity_count()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("count_entity");
}


//=============================================================================
// OF USERDATA API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: Global User data search block
// Parameters:
//   - $entity : entity
//   - $params : parameters with search data
///////////////////////////////////////////////////////////////////////////////
function of_userdata_dis_search($entity, $params) {

  $block_user .= of_category_user_dis_search($entity, $params);

  return $block_user;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Global User data search block
// Parameters:
//   - $entity : entity
//   - $params : parameters with search data
///////////////////////////////////////////////////////////////////////////////
function of_userdata_get_url_search_params($entity, $params) {
  global $cgp_user;

  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $url .= "&amp;sel_$cat_name=".$params[$cat_name]."&amp;${cat_name}_code=".$params["${cat_name}_code"]."&amp;cba_${cat_name}_tree=".$params["${cat_name}_tree"];
    }
  }

  return $url;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Global User data for an entity in consult mode
// Parameters:
//   - $entity : entity
//   - $id     : entity id
///////////////////////////////////////////////////////////////////////////////
function of_userdata_dis_entity_consult($entity, $ent_id) {
  global $cgp_user,$l_properties;

  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cats = of_category_user_get_entitycategories($entity, "$cat_name", $ent_id);
      if ($one_cat["mode"] == "mono") {
        $mono .= of_category_dis_block_consult($entity, $cat_name, $cats, $one_cat["mode"]);
      } else {
        $multi .= of_category_dis_block_consult($entity, $cat_name, $cats, $one_cat["mode"]);
      }
    }
    if ($mono != "") {;
      $block_user .= "
        <div class=\"detail infos\">
        <h1>$l_properties</h1>
        <table>
        $mono
        </table>
        </div>";
    }
    $block_user .= $multi;
  }

  return $block_user;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Global User data form for an entity
// Parameters:
//   - $entity : entity
//   - $id     : entity id
//   - $params : parameters hash
///////////////////////////////////////////////////////////////////////////////
function of_userdata_dis_entity_form($entity, $ent_id, $params) {
  global $cgp_user;

  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cats = array();
      $mode = $one_cat["mode"];
      if ($mode == "mono") {
	$cats = of_category_user_get_ordered($cat_name, $entity);
	if ($ent_id > 0) {
	  if (isset($params["$cat_name"])) {
	    $cats_sel = of_category_user_get_infos($params["$cat_name"]);
	  } else {
	    $cats_sel = of_category_user_get_ordered($cat_name, $entity, $ent_id);
	  }
	  $cat_id = $cats_sel[$params["$cat_name"]]["id"];
	} else {
	  if (isset($params["$cat_name"])) {
	    $cats_sel = of_category_user_get_infos($params["$cat_name"]);
	    $cat_id = $cats_sel[$params["$cat_name"]]["id"];
	  } else {
	    $cats_sel = of_category_user_get_ordered($cat_name, $entity, $ent_id);
	    $cat_id = "";
	  }
	}
      } else {
	if ($ent_id > 0) {
	  if (isset($params["$cat_name"])) {
	    $cats = of_category_user_get_infos($params["$cat_name"]);
	  } else {
	    $cats = of_category_user_get_ordered($cat_name, $entity, $ent_id);
	  }
	} else {
	  if (isset($params["$cat_name"])) {
	    $cats = of_category_user_get_infos($params["$cat_name"]);
	  }
	}
      }
      $block_user .= of_category_user_dis_entity_form($entity, $cat_name, $cats, $one_cat["mode"], $cat_id, "undef");
    }
  }

  return $block_user;
}


///////////////////////////////////////////////////////////////////////////////
// Display: hidden form fields for each userdata
// Parameters:
//   - $entity : entity
//   - $id     : entity id
///////////////////////////////////////////////////////////////////////////////
function of_userdata_dis_hidden_fields($entity, $params) {
  global $cgp_user;

  $cats = array();
  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      if (is_array($params[$cat_name])) {
	foreach ($params[$cat_name] as $cat) {
	  $block .= "<input type=\"hidden\" name=\"sel_${cat_name}[]\" value=\"$cat\" />";
	}
      } else {
	$cat = $params[$cat_name];
	$block .= "<input type=\"hidden\" name=\"sel_${cat_name}[]\" value=\"$cat\" />";
      }
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Global User data admin form
// Parameters:
//   - $entity : entity
///////////////////////////////////////////////////////////////////////////////
function of_userdata_dis_admin_form($entity) {
  global $cgp_user;

  // User categories form
  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cats = of_category_user_get_ordered("$cat_name");
      $block_user .= of_category_dis_admin_form($cat_name, $cats, true);
    }
  }

  return $block_user;
}


// Data functions -------------------------------------------------------------


///////////////////////////////////////////////////////////////////////////////
// User Data search query execution
// Parameters:
//   - $entity : user category for this entity
//   - $params : array with parameters
///////////////////////////////////////////////////////////////////////////////
function of_userdata_query_search($entity, $params) {
  global $cgp_user, $c_all, $c_none;

  // User defined categories
  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cat = $params[$cat_name];
      $cat_tree = $params["${cat_name}_tree"];
      $cat_code = $params["${cat_name}_code"];
      $tlink = "${cat_name}_link";

      if (($cat != $c_all) && ($cat != $c_none) && ($cat != "")) {
	$where_cat_user = "";
	$join_cat_user .= "LEFT JOIN CategoryLink AS $tlink
          ON ${entity}_id=$tlink.categorylink_entity_id
          AND $tlink.categorylink_entity='$entity'
          AND $tlink.categorylink_category='$cat_name'";
	if ($cat_tree) {
	  $join_cat_user .= " LEFT JOIN Category AS $cat_name ON $tlink.categorylink_category_id=$cat_name.category_id";
	  $where_cat_user = " OR $cat_name.category_code like '$cat_code%'";
	}
	$where .= " $and ($tlink.categorylink_category_id='$cat' $where_cat_user)";
	$and = "AND";
      } else if ($cat == $c_none) {
	$join_cat_user .= " LEFT JOIN CategoryLink AS $tlink
          ON ${entity}_id=$tlink.categorylink_entity_id
          AND $tlink.categorylink_entity='$entity'
          AND $tlink.categorylink_category='$cat_name'";
	$where .= " $and $tlink.categorylink_category_id IS NULL";
	$and = "AND";
      }
    }
  }
  $ret["where"] = $where;
  $ret["join"] = $join_cat_user;

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// User Data update query execution
// Parameters:
//   - $entity : user category for this entity
//   - $ent_id : entity id
//   - $params : userdata parameters
//   - $clear  : if true, clear previous link to the entity
///////////////////////////////////////////////////////////////////////////////
function of_userdata_query_update($entity, $ent_id, $params, $clear=true) {
  global $cgp_user;

  $check = ! $clear;

  // User categories update
  if ($clear) {
    $ret = of_category_user_query_delete_link($entity, $ent_id);
  }

  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cats = $params["$cat_name"];
      if (is_array($cats)) {
	foreach($cats as $cat_id) {
	  $ret = of_category_user_query_insert_link($entity, $cat_name, $ent_id, $cat_id, $check);
	}
      } else if ($cats > 0) {
	$ret = of_category_user_query_insert_link($entity, $cat_name, $ent_id, $cats, $check);
      }
    }
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// User Data delete query execution
// Parameters:
//   - $entity : entity
//   - $ent_id : entity id
///////////////////////////////////////////////////////////////////////////////
function of_userdata_query_delete($entity, $ent_id) {
  global $cgp_user;

  // User categories update
  $ret = of_category_user_query_delete_link($entity, $ent_id);

  return $ret;
}


//=============================================================================
// OF User Category API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// User category actions switch
// Parameters:
//   - $module : calling module
//   - $action : action
//   - $params : parameters hash
///////////////////////////////////////////////////////////////////////////////
function of_category_user_action_switch($module, $action, $params) {
  global $display, $extra_css, $l_insert_ok, $l_insert_error;
  global $l_update_ok,$l_update_error, $l_delete_ok, $l_delete_error;

  $category = $params["category"];
  global ${"l_$category"};
  $l_cat = ${"l_$category"};

  if ($action == "category_insert") {
    $retour = of_category_user_query_insert($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_cat : $l_insert_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_cat : $l_insert_error");
    }
    $func = "dis_${module}_admin_index";
    $display["detail"] .= @$func();

  } elseif ($action == "category_update") {
    $retour = of_category_user_query_update($params); 
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_cat : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_cat : $l_update_error");
    }
    $func = "dis_${module}_admin_index";
    $display["detail"] .= @$func();

  } elseif ($action == "category_checklink") {
    $display["detail"] .= of_category_user_dis_links($params);

  } elseif ($action == "category_delete") {
    $retour = of_category_user_query_delete($params); 
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_cat : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_cat : $l_delete_error");
    }
    $func = "dis_${module}_admin_index";
    $display["detail"] .= @$func();

  } elseif ($action == "ext_get_category_ids") {
    $extra_css = "category.css";
    $display["detail"] = of_category_user_dis_tree($module, $params["category"], $params, $action);

  } elseif ( ($action == "ext_get_category_code")
	     || ($action == "ext_get_category_codes") ) {
    $extra_css = "category.css";
    $display["detail"] = of_category_user_dis_tree($module, $params["category"], $params, $action);
  }

}


///////////////////////////////////////////////////////////////////////////////
// User category actions definition
// Parameters:
//   - $module : calling module
///////////////////////////////////////////////////////////////////////////////
function of_category_user_module_action($module) {
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $actions;

// Category Insert
  $actions[$module]["category_insert"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions[$module]["category_update"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions[$module]["category_checklink"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions[$module]["category_delete"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Select
  $actions[$module]["ext_get_category_ids"] = array (
    'Url'      => "$path/$module/${module}_index.php?action=ext_get_category_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Category Select
  $actions[$module]["ext_get_category_code"] = array (
    'Url'      => "$path/$module/${module}_index.php?action=ext_get_category_code",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Category Select
  $actions[$module]["ext_get_category_codes"] = array (
    'Url'      => "$path/$module/${module}_index.php?action=ext_get_category_codes",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

}


// Display functions ----------------------------------------------------------


///////////////////////////////////////////////////////////////////////////////
// Display: Global User category search block 
// Parameters:
//   - $entity : user category for this entity
//   - $params : parameters with search data
///////////////////////////////////////////////////////////////////////////////
function of_category_user_dis_search($entity, $params) {
  global $cgp_user, $l_all, $l_cat_tree;

  if (is_array($cgp_user[$entity]["category"])) {
    foreach($cgp_user[$entity]["category"] as $cat_name => $one_cat) {
      $cats = of_category_user_get_ordered("$cat_name");
      $cat_mode = $onet_cat["mode"];
      $cat = $params["$cat_name"];
      $cat_code = $params["${cat_name}_code"];
      $cat_tree = ($params["${cat_name}_tree"] == "1" ? "checked = \"checked\"" : "");

      $sel_append = "onchange=\"if (this.options[this.selectedIndex].value != '$c_all') {
          mytext = this.options[this.selectedIndex].text;
          pos = mytext.indexOf(' ');
          code = mytext.substr(0, pos);
          this.form.tf_${cat_name}_code.value = code + '.';
       } \"";

      $block_user .= of_category_user_dis_search_select($cat_name, $cats, $cat, array("all", "none"), $sel_append);

      $block_user .= "
    <label>$l_cat_tree<br />
      <input type=\"checkbox\" name=\"cba_${cat_name}_tree\" value=\"1\" $cat_tree />
      <input type=\"hidden\" name=\"tf_${cat_name}_code\" value=\"$cat_code\" />
    </label>";
    }
  }

  return $block_user;
}


///////////////////////////////////////////////////////////////////////////////
// Display: One User category search block 
// Parameters:
//   - $cat           : target category
//   - $cats          : Category array
//   - $cat_id        : selected target category id
//   - $first_element : scalar or array : (["all"|"none"])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_category_user_dis_search_select($cat, $cats, $cat_id, $first_element="all", $sel_append="") {
  global $l_all;

  $l_cat = "l_${cat}"; 
  global $$l_cat;

  $sel_cat = of_category_user_html_select($cat, $cats, $cat_id, $first_element, $sel_append);

  $block_cat = "
    <label>${$l_cat}<br />
      $sel_cat
    </label";

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display: User category html select component
// Parameters:
//   - $cat           : target category
//   - $cats          : Category array
//   - $cat_id        : selected target category
//   - $first_element : scalar or array : (["all"|"none"])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_category_user_html_select($cat, $cats, $cat_id, $first_element, $sel_append="") {

  if (is_array($first_element)) {
    foreach($first_element as $element) {
      $l_element = "l_${element}";
      $c_element = "c_${element}"; 
      global $$l_element, $$c_element;
      if ($$c_element == $cat_id) {
	$first_option .= "
	<option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
      } else {
	$first_option .= "
	<option value=\"${$c_element}\">${$l_element}</option>";
      }
    }
  } else if ($first_element != "") {
    $l_element = "l_${first_element}";
    $c_element = "c_${first_element}"; 
    global $$l_element, $$c_element;
    if ($$c_element == $cat_id) {
      $first_option .= "
	<option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
    } else {
      $first_option .= "
	<option value=\"${$c_element}\">${$l_element}</option>";
    }
  }

  // Category select
  $sel_cat = "<select id=\"sel_${cat}\" name=\"sel_${cat}\" $sel_append>
      $first_option";
  if (is_array($cats)) {
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }
    foreach ($cats as $one_cat) {
      $c_id = $one_cat["id"];
      if ($has_code) {
	$c_code = str_pad($one_cat["code"] . " ", 10, ".");
      }
      $c_label = htmlentities($one_cat["label"]);
      $sel_cat .= "\n<option value=\"$c_id\"";
      if ($c_id == $cat_id) { $sel_cat .= " selected=\"selected\""; }
      $sel_cat .= ">$c_code $c_label</option>";
    }
  }
  $sel_cat .= "</select>";

  return $sel_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display the USER category select (add and delete) for an entity form
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $cats   : category to display
//   - $mode   : ([mono|multi])
//   - $cat_id : (mono mode) category selected
//   - $first  : (mono mode) first element, scalar or array : (["all"|"none"])
// Returns
//   XHTML display of category form part
///////////////////////////////////////////////////////////////////////////////
function of_category_user_dis_entity_form($entity, $cat, $cats, $mode="multi", $cat_id="", $first="") {
  global  $ico_add, $ico_delete, $popup_height, $popup_width, $l_none;
  
  if ($mode == "mono") {
    $sel_cat = of_category_user_html_select($cat, $cats, $cat_id, $first);
    $class = "detailForm";
  } else {
    if (is_array($cats)) {
      foreach ($cats as $one_cat) {
	$c_id = $one_cat["id"];
	$c_code = $one_cat["code"];
	$c_label = htmlentities($one_cat["label"]);
	$sel_name = "sel_${cat}";
	$div_id = "$sel_name-data-$c_id";
	$sel_cat .= "<div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','$sel_name');\">
          <img src=\"$ico_delete\">
          </a>
          $c_code $c_label
          <input value=\"$c_id\" name=\"${sel_name}[]\" type=\"hidden\" />
          </div>";
      }
    }
    $url = "../$entity/${entity}_index.php?action=ext_get_category_ids&amp;category=$cat&amp;popup=1&amp;ext_element=sel_${cat}&amp;ext_title=${$l_cat}";
    $link = "<a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
    $class = "detail";
  }

  $l_cat = "l_${cat}"; 
  global $$l_cat;

  $block_cat = "
    <tr>
      <th>$link ${$l_cat} :</th>    
      <td id=\"sel_${cat}\">$sel_cat</td>
    </tr>";

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display a USER category Tree (category with code like 1.4.10) with input
// Input type is checkbox for ext_get_cat_ids else link
// Parameters:
//   - $entity : target entity
//   - $cat    : target category
//   - $params : hash of module values
//   - $action : action called
// Returns
//   XHTML display of category tree with form if needed
///////////////////////////////////////////////////////////////////////////////
function of_category_user_dis_tree($entity, $cat, $params, $action) {
  global $display, $l_close, $l_add, $l_cat_no;

  $popup = $params["popup"];
  $title = $params["ext_title"];
  if ($title == "") {
    $label = "l_$cat";
    global $$label;
    $title = $$label;
  }
  $cats = of_category_user_get_ordered($cat);
  $display["title"] = "<div class=\"title\">$title</div>";

  $block .= "
  <div class=\"categoryTree\">";

  $multi_select = false;
  if ( (preg_match("/ext_get_category[1-9]?_ids/", $action) > 0)
       || (preg_match("/ext_get_category_codes/", $action) > 0) ) {
    $multi_select = true;
  }

  echo "of_category_user_dis_tree";
  // For multiple select we set a form
  if ($multi_select) {
    $block .= "
    <form onsubmit=\"fill_ext_cat_element(this); window.close(); return false;\">";
  }

  if (is_array($cats)) {

    // Check if code are there
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }
    $cpt_no_code = 1;
    foreach ($cats as $cat_info) {
      if ($cat_info["code"] != "") {
	$hash_code = explode(".",$cat_info["code"]);
	$deep = count($hash_code);
	switch ($deep) {
        case 1:
	  $tree[$hash_code[0]] = $cat_info;
	  break;
        case 2:
	  $tree[$hash_code[0]][$hash_code[1]] = $cat_info;
	  break;
        case 3:
	  $tree[$hash_code[0]][$hash_code[1]][$hash_code[2]] = $cat_info;
	  break;
	}
      } else {
	// Handle categories where code is not filled (automatic numbering)
	$no_code = "X";
	$tree["$no_code"][$cpt_no_code] = $cat_info;
	$tree["$no_code"]["code"] = "$no_code";
	$tree["$no_code"]["label"] = "No code";
	$tree["$no_code"][$cpt_no_code]["code"] = "$no_code.$cpt_no_code";
	$cpt_no_code++;
      }
    }
    if (is_array($tree)) {
      $block .= _of_category_dis_tree_node($tree, 0, $multi_select);
    }
  } else {
    $block .= "$l_cat_no";
  }

  if ($multi_select) {
    $block .= "
    <br />
    <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
    <input type=\"submit\" value=\"$l_add\" /> &nbsp;&nbsp;&nbsp;
    <input type=\"button\" value=\"$l_close\" onclick=\"window.close();\"/>
    </form>";
  }

  $block .= "</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the USER category links
// Parameters:
//   - $params  : parameters array
///////////////////////////////////////////////////////////////////////////////
function of_category_user_dis_links($params) {
  global $l_c_link, $l_c_link_no;
  global $l_c_delete, $l_c_can_delete, $l_c_cant_delete;
  global $l_back, $display;

  $category = $params["category"]; 
  $cat_id = $params["category_id"];

  $l_cat = "l_${category}";
  global $$l_cat;

  $has_links = false;
  $cat = of_category_user_get_infos($cat_id);
  $cat_label = $cat[$cat_id]["label"];

  $cats = of_category_user_get_links($category, $cat_id);

  if (is_array($cats)) {

    foreach($cats as $entity => $one_cat) {
      $l_entity = strtolower("l_${entity}");
      global $$l_entity;
      $nb_links = count($one_cat);
      if ($nb_links > 0) {
	$has_links = true;
        $url = url_prepare("?action=search&amp;sel_${category}=$cat_id&amp;cba_archive=1");

	$dis_link .= "
    <tr>
      <td class=\"detailHead\">".ucfirst(${$l_cat})." ($cat_label) : $l_c_link : ${$l_entity} <a href=\"$url\">($nb_links)</a>.</td>
    </tr>";
      } else {
	$dis_link .= "
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        ".ucfirst(${$l_cat})."($cat_label) : $l_c_link_no (${$l_entity}). </td>
    </tr>";
      }
    }
  }

  if ($has_links) {
    $display["msg"] .= display_warn_msg(ucfirst(${$l_cat}).": $cat_label : $l_c_cant_delete.");
  } else {
    $display["msg"] .= display_ok_msg(ucfirst(${$l_cat}).": $cat_label : $l_c_can_delete.");

    $dis_buttons .= "
      <form name=\"form_cat_delete\"
        method=post action=\"" . url_prepare("") . "\">
      <input type=\"hidden\" name=\"action\" value=\"category_delete\">
      <input type=\"hidden\" name=\"sel_category_id\" value=\"$cat_id\">
      <input type=\"hidden\" name=\"category\" value=\"$category\">
      <input type=\"submit\" name=\"sub_cat\" value=\"$l_c_delete\">
      </form>";
  }

  $dis_buttons .= "
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  $block = "
    <table class=\"detail\">
      $dis_link
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      $dis_buttons
    </p>
  </div>
";

  return $block;
}



// Data functions -------------------------------------------------------------


///////////////////////////////////////////////////////////////////////////////
// Category query execution
// Parameters:
//   - $cat       : category name
//   - $entity    : target entity
//   - $entity_id : if not 0, get only cat referenced by this entry
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_ordered($cat, $entity="", $entity_id=0) {
  global $cdg_sql;

  $multidomain = sql_multidomain("Category");

  // If an entity id is given, restrict categories to those of this entry
  if ( $entity_id > 0) {
    $query = "SELECT Category.*
      FROM CategoryLink
        LEFT JOIN Category ON categorylink_category_id = category_id
      WHERE categorylink_entity_id = '$entity_id'
        AND categorylink_entity = '$entity'
        AND categorylink_category = '$cat'
        $multidomain
      ORDER BY category_code, category_label";
  } else {
    $query = "SELECT *
      FROM Category
      WHERE
        category_category = '$cat'
        $multidomain
      ORDER BY category_code, category_label";
  }
  $cat_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "of_category_user_get_ordered()");
  $cat_q->query($query);

  $cats = array();
  while ($cat_q->next_record()) {
    $id = $cat_q->f("category_id");
    $code = $cat_q->f("category_code");
    $label = $cat_q->f("category_label");
    $cat_info = array("id" => $id,
		      "label" => $label,
		      "code" => $code);
    $cats[$id] = $cat_info;
  }

  if (is_array($cats)) {
    $has_code = false;
    foreach ($cats as $one_cat) {
      if ($one_cat["code"] != "") {
	$has_code = true;
	break;
      }
    }

    if ($has_code) {
      if (count($cats) > 0) {
	usort($cats, "_of_category_compare_code");
      }
    }
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the User categories attached to the entity
// Parameters:
//   - $entity    : target entity
//   - $category  : target category
//   - $entity_id : entity id 
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_entitycategories($entity, $category, $entity_id) {
  global $cdg_sql;

  $cats = array();
  $multidomain = sql_multidomain("Category");

  $query = "SELECT category_id, category_code, category_label 
      FROM CategoryLink
        JOIN Category ON categorylink_category_id=category_id
      WHERE categorylink_entity='$entity'
        AND categorylink_category='$category'
        AND categorylink_entity_id='$entity_id'
        $multidomain
      ORDER BY category_id";

  display_debug_msg($query, $cdg_sql, "of_category_user_get_entitycategories()");
  $cat_q = new DB_OBM;
  if ($entity_id != "") {
    $cat_q->query($query);
  }

  while ($cat_q->next_record()) {
    $id = $cat_q->f("category_id");
    $code = $cat_q->f("category_code");
    $label = $cat_q->f("category_label");
    $cat_info = array("id" => $id,
                      "label" => $label,
                      "code" => $code);
    $cats[] = $cat_info;
  }

  if (count($cats) > 0) {
    usort($cats, "_of_category_compare_code");
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// User Category insertion query construction and execution
// Parameters:
//   - $params : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_user_query_insert($params) {
  global $obm, $cdg_sql, $obm;

  $category = $params["category"];
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $obm["uid"];
  $cat_label = $params["category_label"];
  $cat_code = $params["category_code"];
  $domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO Category (
    category_domain_id,
    Category_timeupdate,
    Category_timecreate,
    Category_userupdate,
    Category_usercreate,
    Category_category,
    Category_code,
    Category_label)
  VALUES (
    '$domain_id',
    null,
    '$timecreate',
    '$usercreate',
    '$usercreate',
    '$category',
    '$cat_code',
    '$cat_label')";

  display_debug_msg($query, $cdg_sql, "of_category_user_query_insert()");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// User Category update query execution
// Parameters:
//   - $param  : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_user_query_update($params) {
  global $obm, $cdg_sql, $obm;

  $category = $params["category"];
  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $obm["uid"];
  $cat_id = $params["category_id"];
  $cat_label = $params["category_label"];
  $cat_code = $params["category_code"];
  $multidomain = sql_multidomain("Category");
	
  $query = "UPDATE Category SET
    category_timeupdate = '$timeupdate',
    category_userupdate = '$userupdate',
    category_label = '$cat_label',
    category_code = '$cat_code'
  WHERE
    category_id='$cat_id'
    AND category_category = '$category'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "of_category_user_query_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category deletion query execution
// Parameters:
//   - $params : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_user_query_delete($params) {
  global $cdg_sql;

  $category_id = $params["category_id"];
  $category = $params["category"];
  $multidomain = sql_multidomain("Category");

  if ($category_id > 0) {
    $query = "DELETE FROM Category
    WHERE category_id ='$category_id'
      AND category_category = '$category'
      $multidomain";

    display_debug_msg($query, $cdg_sql, "of_category_user_query_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  } else {
    $retour = 1;
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// USER Category link insertion query construction and execution
// Parameters:
//   - $entity    : target entity
//   - $cat       : target category
//   - $entity_id : entity id 
//   - $cat_id    : category id 
//   - $check     : if true, check if link already exists first
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_user_query_insert_link($entity, $cat, $entity_id, $cat_id, $check=false) {
  global $cdg_sql;

  if ( ($entity_id > 0) && ($cat_id > 0) ) {
    $obm_q = new DB_OBM;

    if ($check) {
      $nb = 0;
      $query = "SELECT count(*) as nb
       FROM CategoryLink
       WHERE categorylink_category_id='$cat_id'
         AND categorylink_entity_id='$entity_id'
         AND categorylink_category='$cat'
         AND categorylink_entity='$entity'";

      display_debug_msg($query, $cdg_sql, "of_category_user_query_insert_link(check)");
      $obm_q->query($query);
      $obm_q->next_record();
      $nb = $obm_q->f("nb");
      if ($nb > 0) {
	return $nb;
      }
    }

    $query = "INSERT INTO CategoryLink (
       categorylink_category_id,
       categorylink_entity_id,
       categorylink_category,
       categorylink_entity
     ) VALUES (
       '$cat_id',
       '$entity_id',
       '$cat',
       '$entity')";
    display_debug_msg($query, $cdg_sql, "of_category_user_query_insert_link()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete ALL USER categories link for the entity
// Parameters:
//   - $entity    : target entity
//   - $entity_id : entity id 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_user_query_delete_link($entity, $entity_id) {
  global $cdg_sql;

  $query = "DELETE
    FROM CategoryLink
    WHERE
      categorylink_entity='$entity'
      AND categorylink_entity_id='$entity_id'";

  display_debug_msg($query, $cdg_sql, "of_category_user_query_delete_link()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// USER Category - Entity links query execution
// Parameters:
//   - $cat    : target category
//   - $p_id   : category id
// Return:
//   - database handle
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_links($category, $p_id) {
  global $cdg_sql;

  $query = "SELECT categorylink_entity_id,
        categorylink_entity
      FROM CategoryLink
      WHERE categorylink_category_id = '$p_id'
        AND categorylink_category = '$category'
      ORDER by categorylink_entity";

  display_debug_msg($query, $cdg_sql, "of_category_user_get_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $cats = array();
  while ($obm_q->next_record()) {
    $ent = $obm_q->f("categorylink_entity");
    $ent_id = $obm_q->f("categorylink_entity_id");
    $cats[$ent][] = $ent_id;
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// USER Category - get category infos
// Parameters:
//   - $cat_id : category id || array of category ids
// Return:
//   - array with category infos
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_infos($cat_id) {
  global $cdg_sql;

  $cat = array();

  if (is_array($cat_id)) {
    if (count($cat_id) > 0) {
      $or = "";
      foreach ($cat_id as $one_id) {
	$where_cat .= " $or category_id = '$one_id'";
	$or = "OR";
      }
    } else {
      return $cat;
    }
  } else {
    if ($cat_id > 0) {
      $where_cat .= "category_id = '$cat_id'";
    } else {
      return $cat;
    }
  }

  $query = "SELECT *
    FROM Category
    WHERE $where_cat";

  display_debug_msg($query, $cdg_sql, "of_category_user_get_infos()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while($obm_q->next_record() ) {
    $id = $obm_q->f("category_id");
    $cat[$id] = array (
		    "category" => $obm_q->f("category_category"),
		    "id" => $id,
		    "code" => $obm_q->f("category_code"),
		    "label" => $obm_q->f("category_label"));
  }

  return $cat;
}


///////////////////////////////////////////////////////////////////////////////
// USER Category - get category hash [id]->code
// Parameters:
//   - $category : category
// Return:
//   - array with category hash
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_code_hash($category) {
  global $cdg_sql;

  $multidomain = sql_multidomain("Category");

  $query = "SELECT category_id, category_code
      FROM Category
      WHERE category_category = '$category'
        $multidomain";

  display_debug_msg($query, $cdg_sql, "of_category_user_get_code_hash()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cat = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f("category_id");
    $code = $obm_q->f("category_code");
    $cats[$id] = $code;
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Statistic Category Per Entity query execution
// Parameters:
//   - $entity    : target entity
//   - $category  : target category
///////////////////////////////////////////////////////////////////////////////
function of_category_user_get_category_distribution($entity, $category) {
  global $cdg_sql;

  $multidomain = sql_multidomain("Category");
	
  $query = "SELECT
      category_id,
      category_code,
      category_label,
      count(category_id) as nb_category
    FROM CategoryLink
      LEFT JOIN Category ON categorylink_category_id = category_id
    WHERE categorylink_entity='$entity'
      AND categorylink_category='$category'
      $multidomain
    GROUP BY category_id, category_code, category_label
    ORDER BY category_code, category_label";

  display_debug_msg($query, $cdg_sql, "of_category_user_category_per_entity()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cats = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f("category_id");
    $code = $obm_q->f("category_code");
    $label = $obm_q->f("category_label");
    $nb = $obm_q->f("nb_category");
    $cats[$id] = array("code" => $code, "label" => $label, "nb" => $nb);
  }

  return $cats;
}


?>
