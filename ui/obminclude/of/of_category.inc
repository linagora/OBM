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
    document.getElementById('img_'+name).src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";    
  } else if (NS4) {
    document.layers['div_'+name].display = \"hide\";
    document.layers['div_'+name].height = \"0px\";
    document.layers['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";    
  } else {
    document.all['div_'+name].style.display = \"none\";
    document.all['div_'+name].style.height = \"0px\";
    document.all['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\";
  }
}

function show ( name ) {
  if (W3C) {
    document.getElementById('div_'+name).style.display = \"\";
    document.getElementById('div_'+name).style.height = \"auto\"; 
    document.getElementById('img_'+name).src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  } else if (NS4) {
    document.layers['div_'+name].display = \"\";
    document.layers['div_'+name].height = \"auto\";
    document.layers['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  } else {
    document.all['div_'+name].style.display = \"\";
    document.all['div_'+name].style.height = \"auto\";
    document.all['img_'+name].src = \"".C_IMAGE_PATH."/$set_theme/$ico_directory_open\";
  }
}

";

if (isset($ext_target) && $ext_target!="") {
  $extra_js .= "

function fill_ext_form(dir_path) {
  ext_field = window.opener.document.$ext_target;
  ext_field.value = dir_path;
  window.close();
} 
  
function fill_ext_cat_element(int_form) {
  size = int_form.length;
  for (i=0; i <size; i++) {
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
	elemid = '$ext_target-data-'+id;
        if (!get_elem(window.opener.document, elemid )) {
          elemid = 'data'+id;
	  span = get_elem(window.document,elemid);
	  add_element('$ext_target' + '[]', id, span.innerHTML);
        }
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Add an element line in the list form
///////////////////////////////////////////////////////////////////////////////
function add_element(fieldName,fieldValue,fieldLabel) {
  target = window.opener.document;
  container = get_elem(target, '$ext_target' );
  row = target.createElement('div');
  row.id = '$ext_target'+fieldValue;
  row.className = 'elementRow';
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = fieldName;
  field.value = fieldValue;
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\'$ext_target'+fieldValue+'\',\'$ext_target\');';
  link.onclick = '';
  image = target.createElement('img');
  image.src = '".C_IMAGE_PATH."/$set_theme/$ico_crow';
  link.appendChild(image);
  content = target.createTextNode(fieldLabel);
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);
  container.appendChild(row);
} 
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
    <div class=\"searchLabel\">${$l_cat}<br />
      $sel_cat
    </div>";
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
  global $set_theme, $ico_add, $ico_crow, $popup_height, $popup_width;
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
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_crow\">
          </a>
          $c_code $c_label
          <input value=\"$c_id\" name=\"${sel_name}[]\" type=\"hidden\" />
          </div>";
        }
      }
      $url = "${entity}_index.php?action=ext_get_${cat}_ids&amp;popup=1&amp;ext_target=sel_${cat}&amp;ext_title=${$l_cat}";
      $link="<a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" />
      </a>";
      $class = "detail";
    }

    $l_cat = "l_${cat}"; 
    global $$l_cat;

    $block_cat = "
    <tr>
      <td class=\"detailLabel\">$link ${$l_cat} </td>    
      <td class=\"$class\" id=\"sel_${cat}\">$sel_cat</td>
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
        <tr>
          <td class=\"detailLabel\">".ucfirst(${$l_cat})."</td>
          <td class=\"detailText\">$label</td>
         </tr>";

    } else {
      // Multi Link
      $nb_col_cat = 3;
      $dis_cat = "<tr>";
      for ($i=0; $i<$nb_col_cat; $i++) {
        $dis_cat .= "<td>&nbsp;</td>";
      }
      $dis_cat .= "</tr>";
      $dis_cat = "";

      $i = 0;
      if (is_array($cats)) {
        foreach ($cats as $one_cat) {
          if ($i==0) {
            $dis_cat .= "<tr>";
          }
          $label = htmlentities($one_cat["label"]);
          $code = $one_cat["code"];
          $i++;
          $dis_cat .= "<td style=\"border: 1px solid\" class=\"detailText\">$code $label </td>";
          if ($i==$nb_col_cat) {
            $dis_cat .= "</tr>";
            $i = 0;
          }
        }
      }
      $block_cat = "
      <div class=\"detailHead\">".ucfirst(${$l_cat})."</div>
      <table class=\"detail\">
      $dis_cat
      </table>";
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
      print_r($tree);
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
//   - $entity : target entity
//   - $catu   : target category (string) can have Uppercase eg: CompanyRole
//   - $cats   : Category array
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_admin_form($entity, $catu, $cats) {
  global $l_c_manage, $l_code, $l_label;
  global $l_c_checkdelete, $l_c_update, $l_c_upd, $l_c_new, $l_c_insert;
  global $l_c_j_create, $l_c_j_insert, $l_c_j_update;

  $cat = strtolower($catu);
  $l_cat = "l_${cat}";
  global $$l_cat;

  $indent = 10;
  $flabel = $indent;
  $sel_cat = "<select name=\"sel_${cat}\" onchange=\"
    mytext = this.options[this.selectedIndex].text;
    mytext = mytext.substr(0, mytext.length);
    pos = mytext.indexOf('...');
    if (pos != -1) {
      ext = trim(mytext.substr(0, pos));
      mytext = mytext.substr($flabel);
    }
    forms.form_${cat}_update.tf_${cat}_label.value=mytext;
    forms.form_${cat}_update.tf_${cat}_code.value=ext;
    \"
    size=\"8\" style=\"font-family:monospace;\">";
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
	$c_code = str_pad($one_cat["code"] . " ", $indent, ".");
      }
      $c_label = htmlentities($one_cat["label"]);
      $sel_cat .= "\n<option value=\"$c_id\">$c_code $c_label</option>";
    }
  }
  $sel_cat .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"admin\">
    <div class=\"adminTitle\">:: $l_c_manage ${$l_cat}</div>
    <table class=\"admin\" width=\"100%\">
    <tr>
      <td>&nbsp;</td>
      <td class=\"adminLabel\">$l_c_upd</td>
      <td class=\"adminLabel\">$l_c_new</td>
    </tr>
    <tr>
      <td>
<!-- Category Check Delete Section -->
        <form name=\"form_${cat}_delete\" method=\"post\"
          action=\"" . url_prepare("") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">".ucfirst(${$l_cat})."</td>
        </tr>
        <tr>
          <td class=\"adminText\">$sel_cat</td>
        </tr>
        <tr>
          <td>
          <input type=\"hidden\" name=\"action\" value=\"${cat}_checklink\" />
          <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_checkdelete\" onclick=\"if (check_category_checkdel(this.form.sel_${cat},'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
          </td>
        </tr> 
        </table>
        </form>
      </td>
      <td>

<!-- Category Update Section -->

          <form name=\"form_${cat}_update\" method=\"get\" action=\"" . url_prepare("") . "\">
        <table heigth=\"100%\">
        <tr>
          <td class=\"adminLabel\">$l_code :</td>
          <td><input type=\"text\" maxlength=\"10\" size=\"10\" name=\"tf_${cat}_code\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_label :</td>
          <td><input type=\"text\" name=\"tf_${cat}_label\" /></td>
        </tr><tr>
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"sel_${cat}\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"${cat}_update\" />
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_update\"
              onclick=\"if (check_category_upd(this.form.tf_${cat}_label,this.form.sel_${cat},document.form_${cat}_delete.sel_${cat},'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
          </td>
        </tr>
        </table>
          </form>
      </td>
      <td>
<!-- New Category Section -->
        <form name=\"form_${cat}_new\" method=\"get\" action=\"" . url_prepare("") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_code :</td>
          <td ><input type=\"text\" maxlength=\"10\" size=\"10\" name=\"tf_${cat}_code\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_label :</td>
          <td ><input type=\"text\" name=\"tf_${cat}_label\" /></td>
        </tr><tr>
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"action\" value=\"${cat}_insert\" />
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_insert\"
              onclick=\"if (check_category_new(this.form.tf_${cat}_label,'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>

      </td>
    </tr>
    </table>

<!--
    <table class=\"admin\">
    <tr>
      <td>

        <table border=\"1\">
        <tr>
          <td colspan=\"3\">
            <form name=\"form_${cat}_delete\" method=\"post\"
              action=\"" . url_prepare("") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">".ucfirst(${$l_cat})."</td>
              <td class=\"adminLabel\">$sel_cat</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"${cat}_checklink\" />
              <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_checkdelete\" onclick=\"if (check_category_checkdel(this.form.sel_${cat},'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_${cat}_update\" method=\"get\"
              action=\"" . url_prepare("") . "\">
            <input type=\"hidden\" name=\"sel_${cat}\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"${cat}_update\" />
            <input type=\"text\" maxlength=\"10\" size=\"10\" name=\"tf_${cat}_code\" />
            <input type=\"text\" name=\"tf_${cat}_label\" />
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_update\"
              onclick=\"if (check_category_upd(this.form.tf_${cat}_label,this.form.sel_${cat},document.form_${cat}_delete.sel_${cat},'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>

      </td>
      <td>

        <form name=\"form_${cat}_new\" method=\"get\"
          action=\"" . url_prepare("") . "\">
        <table>
        <tr>
        <td class=\"adminLabel\" colspan=\"2\">$l_c_new</td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_code :</td>
          <td ><input type=\"text\" maxlength=\"10\" size=\"10\" name=\"tf_${cat}_code\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_label :</td>
          <td ><input type=\"text\" name=\"tf_${cat}_label\" /></td>
        </tr><tr>
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"action\" value=\"${cat}_insert\" />
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_c_insert\"
              onclick=\"if (check_category_new(this.form.tf_${cat}_label,'".addslashes(ucfirst(${$l_cat}))."')) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
-->

    </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the cat links
// Parameters:
//   - $entities   : target entity : if an array, check for each entities
//   - $cat        : target category
//   - $param      : Entity parameters array
//   - $mode       : ([mono|multi])
//   - $link_table : [optionnal] to handle special
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_links($entities, $cat, $param, $mode="multi") {
  global $l_c_link, $l_c_link_no;
  global $l_c_delete, $l_c_can_delete, $l_c_cant_delete;
  global $l_back, $display;

  $l_cat = "l_${cat}";
  global $$l_cat;

  $has_links = false;
  $id = $param["${cat}_id"];
  if (! ($id > 0)) {
    $id = $param["${cat}"];
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
        $url = url_prepare("?action=search&amp;sel_${cat}=$id");

	$dis_link .= "
    <tr>
      <td class=\"detailHead\">".ucfirst(${$l_cat})."($label) : $l_c_link ${$l_entity} <a href=\"$url\">($nb_cat)</a>.</td>
    </tr>";
      }
    }
  } else {
    $entity = $entities;
    $l_entity = strtolower("l_${entity}");
    global $$l_entity;
    $label = of_category_get_label($entity, $cat, $id);
    $obm_q = of_category_query_links($entity, $cat, $id, $mode);

    $nb_cat = $obm_q->num_rows();
    if ($nb_cat > 0) {
      $has_links = true;
      $url = url_prepare("?action=search&amp;sel_${cat}=$id");

    $dis_link .= "
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        ".ucfirst(${$l_cat})."($label) : $l_c_link_no (${$l_entity}). </td>
    </tr>";
    }
  }

  if ($has_links) {
    $display["msg"] .= display_warn_msg(ucfirst(${$l_cat}).": $l_c_cant_delete.");
  } else {
    $display["msg"] .= display_ok_msg(ucfirst(${$l_cat}).": $l_c_can_delete.");

    $dis_buttons .= "
      <form name=\"form_cat_delete\"
        method=post action=\"" . url_prepare("") . "\">
      <input type=\"hidden\" name=\"action\" value=\"${cat}_delete\">
      <input type=\"hidden\" name=\"sel_${cat}\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_c_delete\">
      </form>";
  }

  $dis_buttons .= "
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("") . "\">
        <input type=\"hidden\" name=\"action\" value=\"region\" />
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


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About the category for one entity 
// Parameters:
//   - $entity       : target entity
//   - $cat          : target category
//   - $cat_q        : DB Object for category stats
// Returns:
//   - $block        : html block
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_entity_stats($entity, $cat, $cat_q) {
  global $l_key, $l_totals;

  $l_cat = "l_${cat}"; $l_entity = "l_${entity}";
  global $$l_cat,$$l_entity;

  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $entityfield_prefix = $entity."_";
  $entityfield_id = $entityfield_prefix."id";

  $count_entity = _of_category_get_entity_count($entity);
  $max = 1;
  while($cat_q->next_record()) {
    $stat[$cat_q->f("$field_id")]["$cat"] = $cat_q->f("count_entity");
     $stat[$cat_q->f("$field_id")]["name"] = $cat_q->f("$field_code")." - ".$cat_q->f("$field_label");
    if ($cat_q->f("count_entity") > $max) {
      $max = $cat_q->f("count_entity");
    }
  }


  $block .= "<div class=\"detailHead\">".ucfirst(${$l_cat})."</div>
   <table class=\"detail\" >";
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
    <td class=\"detailLabel\" class=\"detailLabel\"><br/>$l_key</td>
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

  // If an entity id is given, restrict categories to those of this entry
  if ( ($mode == "multi") && ($entity_id > 0)) {
    $query = "SELECT $table.*
      FROM $linktable
        LEFT JOIN $table ON $linkfield_cat = $field_id
      WHERE $linkfield_ent = '$entity_id'
      ORDER BY $field_code, $field_label";
  } else {
    $query = "SELECT * FROM $table ORDER BY $field_code, $field_label";
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

  if (count($cats) > 0) {
    usort($cats, "_of_category_compare");
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Category insertion query construction and execution
// Parameters:
//   - $entity  : target entity
//   - $catu    : target category (string, may have upper case)
//   - $param   : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_insert($entity, $catu, $param) {
  global $auth, $cdg_sql;

  $cat = strtolower($catu);
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat_label = $param["${cat}_label"];
  $cat_code = $param["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = strtolower($entity.$cat."_");
  $field_timecreate = $field_prefix."timecreate";
  $field_usercreate = $field_prefix."usercreate";
  $field_label = $field_prefix."label";
  $field_code = $field_prefix."code";

  $query = "INSERT INTO $table (
    $field_timecreate,
    $field_usercreate,
    $field_label,
    $field_code)
  VALUES (
    '$timecreate',
    '$usercreate',
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
//   - $param  : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_update($entity, $catu, $param) {
  global $auth, $cdg_sql;

  $cat = strtolower($catu);
  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $cat_id = $param["${cat}_id"];
  if (! ($cat_id > 0)) {
    $cat_id = $param["${cat}"];
  }
  $cat_label = $param["${cat}_label"];
  $cat_code = $param["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = $entity.$cat."_";
  $field_timeupdate = $field_prefix."timeupdate";
  $field_userupdate = $field_prefix."userupdate";
  $field_id = $field_prefix."id";
  $field_label = $field_prefix."label";
  $field_code = $field_prefix."code";

  $query = "UPDATE $table SET
    $field_timeupdate = '$timeupdate',
    $field_userupdate = '$userupdate',
    $field_label = '$cat_label',
    $field_code = '$cat_code'
    WHERE
      $field_id='$cat_id'";

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
//   - $param   : parameters 
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_category_query_delete($entity, $catu, $param) {
  global $cdg_sql;

  $cat = strtolower($catu);
  $table = ucfirst($entity) . ucfirst($catu);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $id = $param["${cat}_id"];
  if (! ($id > 0)) {
    $id = $param["${cat}"];
  }

  if ($id != '') {
    $query = "DELETE FROM $table WHERE $field_id ='$id'";

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

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Activity - Entity links query execution
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
      WHERE $entityfield_cat = '$p_id'";
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

  if ($mode == "mono") {
    $query = "SELECT $field_id, $field_label, $field_code 
      FROM $entitytable, $table 
      WHERE $entityfield_cat = $field_id
        AND $entityfield_id = '$entity_id' 
      ";
  } else {
    $query = "SELECT $field_id, $field_label, $field_code 
      FROM $linktable, $table 
      WHERE $linkfield_cat = $field_id 
        AND $linkfield_ent = '$entity_id'
      ORDER BY $field_id";
  }

  display_debug_msg($query, $cdg_sql);
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
    usort($cats, "_of_category_compare");
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

  $query = "SELECT $field_label 
    FROM $table 
    WHERE $field_id = '$id'";

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

  if ($mode == "mono") {
    $query = "SELECT $field_id, $field_code, $field_label, count($entityfield_id) as count_entity
      FROM $table
        LEFT JOIN $entitytable ON $entityfield_cat = $field_id
      GROUP BY $entityfield_cat, $field_id, $field_code, $field_label
      ORDER BY $field_code, $field_label";
  } else {
    $query = "SELECT $field_id, $field_code, $field_label, count($entityfield_id) as count_entity
      FROM $table
        LEFT JOIN $linktable ON $linkfield_cat = $field_id
        LEFT JOIN $entitytable ON $entityfield_id = $linkfield_ent
      GROUP BY $linkfield_cat, $field_id, $field_code, $field_label
      ORDER BY $field_code, $field_label";
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
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
  global $ico_directory_close, $ico_directory, $set_theme;

  $code = $tree["code"];
  $label = $tree["label"];
  $id = $tree["id"];
  if ($code != "") {
    $node_to_display = true;
    $indent_file = $indent + 15;
  } else {
    $node_to_display = false;
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
        <a href=\"javascript: void(0);\" onclick=\"fill_company_ext_code_form('$code'); return false;\"><span id=\"data$id\">$code - $label</span></a>";
    }

    // If the node has children : link to open it, and icon is different
    if ($has_child == true) {
      $block = "
      <table class=\"category\">
      <tr>
	<td class=\"categoryDir\" style=\"padding-left:".$indent."px;text-align:left;\">
	  <a href=\"\" onclick=\"show_hide_dir('$code'); return false;\"> 
	  <img id=\"img_$code\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_directory_close\" alt=\"\"/>
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
          <img id=\"img_$code\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_directory\" alt=\"\"/>
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
// Compare 2 categories with their code. Order 1.15 ans 1.2 correctly
// Parameters: 
//   - $m : index of first category
//   - $n : index of second category
///////////////////////////////////////////////////////////////////////////////
function _of_category_compare($m, $n) {

  $code_1 = explode(".",$m["code"]);
  $code_2 = explode(".",$n["code"]);
  $i=0;
  while (isset($code_1[$i]) || isset($code_2[$i])) {
    if ($code_1[$i] > $code_2[$i]){
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

  $query = "SELECT count(*) as count_entity FROM $entitytable";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("count_entity");
}


?>
