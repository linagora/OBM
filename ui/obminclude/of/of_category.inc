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
// EntityCategory (id, time and user * update and create, code, label)
// EntityCategoryLink (category_id, entity_id)

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


///////////////////////////////////////////////////////////////////////////////
// Display the category select (add and delete) for an entity
// Parameters:
//   - $cats    : category to display
//   - $cat_num : category number (eg: 1 => sel_cat1)
//   - $entity  : target entity
// Returns
//   XHTML display of category form part
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_entity_form($cats, $cat_num="", $entity) {
  global $set_theme, $ico_add, $ico_crow, $popup_height, $popup_width;

  $show_cat = (! $cgp_hide["$entity"]["category$cat_num"]);

  // Category select field
  if ($show_cat) {
    if (is_array($cats)) {
      foreach ($cats as $one_cat) {
	$c_id = $one_cat["id"];
	$c_code = $one_cat["code"];
	$c_label = htmlentities($one_cat["label"]);
	$sel_name = "sel_cat$cat_num";
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

    $label = "l_category${cat_num}";
    global $$label;
    $url = "${entity}_index.php?action=ext_get_cat${cat_num}_ids&amp;popup=1&amp;ext_target=sel_cat$cat_num&amp;ext_title=${$label}";

    $block_cat = "
  <tr>
    <td class=\"detailLabel\">
      <a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" />
      </a>
      ${$label}
    </td>    
    <td class=\"detail\" id=\"sel_cat${cat_num}\">
      $sel_cat
    </td>
  </tr>";
  }

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display a category Tree (category with code like 1.4.10) with input
// Input type is checkbox for ext_get_cat_ids else link
// Parameters:
//   - $params : hash of module values
//   - $action : action called
//   - $entity : target entity
//   - $cat    : target category
// Returns
//   XHTML display of category tree with form if needed
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_tree($params, $action, $entity, $cat) {
  global $display, $l_close, $l_add, $l_cat_no;

  $popup = $params["popup"];
  $ext_title = $params["ext_title"];
  $cats = of_category_get_ordered($entity, $cat);
  $display["title"] = "<div class=\"title\">$ext_title</div>";

  $block .= "
  <div class=\"categoryTree\">";

  $multi_select = false;
  if (preg_match("/ext_get_cat[1-9]?_ids/", $action) > 0) {
    $multi_select = true;
  }

  // For multiple select we set a form
  if ($multi_select) {
    $block .= "
    <form onsubmit=\"fill_ext_cat_element(this); window.close(); return false;\">";
  }

  if (is_array($cats)) {
    foreach ($cats as $cat_info) {
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
    }
    $block .= of_category_dis_tree_node($tree, 0, $multi_select);

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
// Display: Document Tree
// Parameters:
//   - $tree   : array with categories
//   - $indent : indent size
//   - $multi  : tell if multi selection enabled
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_tree_node($tree, $indent, $multi=false) {
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
  foreach ($tree as $level => $info) {
    if ("$level" != "code" && "$level" != "label" && "$level" != "id") {    
      $content .= of_category_dis_tree_node($info, $indent_file, $multi);
      $has_child = true;
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
        <a href=\"javascript: void(0);\" onclick=\"fill_ext_code_form('$code'); return false;\"><span id=\"data$id\">$code - $label</span></a>";
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
// Category query execution
// Parameters:
//   - $entity    : target entity
//   - $cat       : target category
//   - $entity_id : if not c_all, get only cat referenced by this entry
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_category_get_ordered($entity, $cat, $entity_id=0) {
  global $cdg_sql, $cgp_hide;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";

  $show_cat = (! $cgp_hide["$entity"]["$cat"]);

  if ($show_cat) {
    // If an entity id is given, restrict categories tothose of this entry
    if ($entity_id > 0) {
      $query = "SELECT
          $table.*
        FROM $linktable
          LEFT JOIN $table ON $linkfield_cat = $field_id
        WHERE $linkfield_ent = '$entity_id'
        ORDER BY $field_code, $field_label";
    } else {
      $query = "SELECT * FROM $table ORDER BY $field_code, $field_label";
    }
    $cat_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, "of_get_category_ordered()");
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
      usort($cats, "of_category_compare");
    }

  } else {
    $cats = "";
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Compare 2 categories with their code. Order 1.15 ans 1.2 correctly
// Parameters: 
//   - $m      : index of first category
//   - $n      : index of second category
///////////////////////////////////////////////////////////////////////////////
function of_category_compare($m, $n) {

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


?>
