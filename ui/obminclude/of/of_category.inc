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
//   - $cats     : category to display
//   - $entity   : target entity
//   - $category : target category 
// Returns
//   XHTML display of category form part
///////////////////////////////////////////////////////////////////////////////
function of_category_dis_entity_form($cats, $entity, $cat="") {
  global $set_theme, $ico_add, $ico_crow, $popup_height, $popup_width;

  $show_cat = (! $cgp_hide["$entity"]["${cat}"]);

  // Category select field
  if ($show_cat) {
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

    $l_cat = "l_${cat}"; 
    global $$l_cat;
    $url = "${entity}_index.php?action=ext_get_${cat}_ids&amp;popup=1&amp;ext_target=sel_${cat}&amp;ext_title=${$l_cat}";

    $block_cat = "
  <tr>
    <td class=\"detailLabel\">
      <a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" />
      </a>
      ${$l_cat}
    </td>    
    <td class=\"detail\" id=\"sel_${cat}\">
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
  if (preg_match("/ext_get_category[1-9]?_ids/", $action) > 0) {
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

  $cats = array();
  $show_cat = (! $cgp_hide["$entity"]["$cat"]);

  if ($show_cat) {
    // If an entity id is given, restrict categories to those of this entry
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
  }

  return $cats;
}

///////////////////////////////////////////////////////////////////////////////
// Return all the categories attached to the entity
// Parameters:
//   - $entity_id : entity id 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   Array with orderer categories
///////////////////////////////////////////////////////////////////////////////
function of_entitycategories_get($entity_id, $entity, $cat) {
  global $cdg_sql, $cgp_hide;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";

  $query = "SELECT $field_id, $field_label, $field_code 
    FROM $linktable, $table 
    WHERE $linkfield_cat = $field_id 
      AND $linkfield_ent = '$entity_id'
    ORDER BY $field_id";

  display_debug_msg($query, $cdg_sql);
  $cat_q = new DB_OBM;
  if ($entity_id != "") {
    $cat_q->query($query);
  }

  $cats=array();
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

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Category insertion query construction and execution
// Parameters:
//   - $param     : parameters 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_run_query_category_insert($param, $entity, $cat) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $cat_label = $param["${cat}_label"];
  $cat_code = $param["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
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

  display_debug_msg($query, $cdg_sql);
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
function of_run_query_category_insert_link($entity_id, $cat_id, $entity, $cat) {
  global $cdg_sql;

  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";

  $query = "INSERT INTO $linktable (
       $linkfield_ent,
       $linkfield_cat)
     VALUES (
       '$entity_id',
       '$cat_id')";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Category update query execution
// Parameters:
//   - $param     : parameters 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_run_query_category_update($param, $entity, $cat) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $cat_id = $param["${cat}"];
  $cat_label = $param["${cat}_label"];
  $cat_code = $param["${cat}_code"];
  $table = ucfirst($entity) . ucfirst($cat);
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

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Category deletion query execution
// Parameters:
//   - $param     : parameters 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_run_query_category_delete($param, $entity, $cat) {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $id = $param["${cat}"];

  if ($id != '') {
    $query = "DELETE FROM $table WHERE $field_id ='$id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  } else 
    $retour = 1;

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Category link deletion query construction and execution
// Parameters:
//   - $entity_id : entity id 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function of_run_query_category_delete_link($entity_id, $entity, $cat) {
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
// Get the label of a Category from its id
// Category deletion query execution
// Parameters:
//   - $id        : category id 
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - category label 
///////////////////////////////////////////////////////////////////////////////
function of_get_category_label($id, $entity, $cat) {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_label = $field_prefix."label";

  $query = "SELECT $field_label 
    FROM $table 
    WHERE $field_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $retour = $obm_q->f("contactcategory1_label");

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Activity - Entity links query execution
// Parameters:
//   - $p_id      : activity(category) id
//   - $entity    : target entity
//   - $cat       : target category
// Return:
//   - database handle
///////////////////////////////////////////////////////////////////////////////
function of_run_query_category_links($p_id, $entity, $cat) {
  global $cdg_sql;

  $table = ucfirst($entity) . ucfirst($cat);
  $field_prefix = $entity.$cat."_";
  $field_id = $field_prefix."id";
  $field_code = $field_prefix."code";
  $field_label = $field_prefix."label";
  $linktable = ucfirst($entity) . ucfirst($cat)."Link";
  $linkfield_ent = $entity.$cat."link_".$entity."_id";
  $linkfield_cat = $entity.$cat."link_category_id";
  $entitytable = ucfirst($entity);
  $entityfield_prefix = $entity."_";
  $entitityfield_id = $entityfield_prefix."id";

  $query = "SELECT $entitityfield_id, '$p_id' as contact_category1
    FROM $entitytable, $linktable
    WHERE $entitityfield_id = $linkfield_ent 
      AND $linkfield_cat = '$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
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

///////////////////////////////////////////////////////////////////////////////
// Display: Admin  Category section
// Parameters:
//   - $cats      : Category array
//   - $entity    : target entity
//   - $cat       : target category
///////////////////////////////////////////////////////////////////////////////
function of_html_admin_cat_form($cats, $entity, $cat) {
  global $obminclude;
  global $l_code,$l_label;
  global $l_manage,$l_exist, $l_checkdelete, $l_update, $l_new, $l_insert;

  $l_cat = "l_${cat}"; $l_cat_s = "l_${cat}_s"; 
  $l_cat_new = "l_${cat}_new";$l_cat_exist = "l_${cat}_exist";
  global $$l_cat,$$l_cat_s;
  global $$l_cat_new,$$l_cat_exist;


  $indent = 14;
  $flabel = $indent;
  $sel_cat = "<select name=\"sel_${cat}\" onchange=\"
    mytext = this.options[this.selectedIndex].text;
    mytext = mytext.substr(0, mytext.length);
    pos = mytext.indexOf('...');
    ext = trim(mytext.substr(0, pos));
    mytext = mytext.substr($flabel);
    forms.form_${cat}_update.tf_${cat}_label.value=mytext;
    forms.form_${cat}_update.tf_${cat}_code.value=ext;
    \"
    size=\"6\" >";
  if (is_array($cats)) {
    foreach ($cats as $one_cat) {
      $c_id = $one_cat["id"];
      $c_code = str_pad($one_cat["code"] . " ", $indent, ".");
      $c_label = htmlentities($one_cat["label"]);
      $sel_cat .= "\n<option value=\"$c_id\">$c_code $c_label</option>";
    }
  }
  $sel_cat .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_manage ${$l_cat_s}
      </td>
    </tr><tr>
      <td>

<!-- Category Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_${cat}_delete\" method=\"post\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">".ucfirst(${$l_cat_s})." ${$l_cat_exist}</td>
              <td class=\"adminLabel\">$sel_cat</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"${cat}_checklink\" />
              <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_checkdelete\" onclick=\"if (check_category_checkdel(this.form.sel_${cat})) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_${cat}_update\" method=\"get\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <input type=\"hidden\" name=\"sel_${cat}\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"${cat}_update\" />
            <input type=\"text\" maxlength=\"10\" size=\"10\" name=\"tf_${cat}_code\" />
            <input type=\"text\" name=\"tf_${cat}_label\" />
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_update\"
              onclick=\"if (check_category_upd(this.form.tf_${cat}_label,this.form.sel_${cat},document.form_${cat}_delete.sel_${cat})) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category Section -->

        <form name=\"form_${cat}_new\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <table>
        <tr>
        <td class=\"adminLabel\" colspan=\"2\">${$l_cat_new} ${$l_cat}</td>
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
            <input type=\"submit\" name=\"sub_${cat}\" value=\"$l_insert\"
              onclick=\"if (check_category_new(this.form.tf_${cat}_label)) return true; else return false;\" />
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
// Display: the cat1 links
// Parameters:
//   - $contact : contact hash info : keys used : cat1
// Display: Admin  Category section
// Parameters:
//   - $cats      : Category array
//   - $entity    : target entity
//   - $cat       : target category
///////////////////////////////////////////////////////////////////////////////
function of_dis_category_links($param, $entity, $cat) {
  global $l_cat1_link_contact, $l_cat1_link_contact_no;
  global $l_cat1_delete, $l_cat1_can_delete, $l_cat1_cant_delete;
  global $l_back,$display;

  $id = $param["${cat}"];
  $label = of_get_category_label($id, $entity, $cat);
  $obm_q = of_run_query_category_links($id, $entity, $cat);

  $nb_cat = $obm_q->num_rows();
  if ($nb_cat > 0) {
    $display["msg"] .= display_warn_msg($l_cat1_cant_delete);
    $url = url_prepare("${entity}_index.php?action=search&amp;sel_${cat}=$id");

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat1_link_contact <a href=\"$url\">($nb_cat)</a></td>
    </tr>";

    $dis_link .="
    </table>
      <div class=\"detailButton\">
        <p class=\"detailButtons\">
          <form name=\"form_back\" method=\"get\"
            action=\"" .url_prepare("contact_index.php") . "\">
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
        $label : $l_cat1_link_contact_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form name=\"form_act_delete\"
        method=post action=\"" . url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"${cat}_delete\">
      <input type=\"hidden\" name=\"sel_${cat}\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_cat1_delete\">
      </form>

      <form name=\"form_back\" method=\"get\"
        action=\"" .url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"admin\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  }

  return $dis_link;
}

///////////////////////////////////////////////////////////////////////////////
// Display: category select block 
// Parameters:
//   - $cats         : Category array
//   - $cat_selected : selected target category 
//   - $entity       : target entity 
//   - $cat          : target category
///////////////////////////////////////////////////////////////////////////////
function of_dis_block_select_category($cats, $cat_selected, $entity, $cat) {
  global $c_all, $l_all;
  $l_cat = "l_${cat}"; 
  global $$l_cat;

  $show_cat = (! $cgp_hide["$entity"]["$cat"]);

  if ($show_cat) {
    // Category select
    $sel_cat = "<select id=\"sel_${cat}\" name=\"sel_${cat}\">
    <option value=\"$c_all\">$l_all</option>";
    if (is_array($cats)) {
      foreach ($cats as $one_cat) {
        $c_id = $one_cat["id"];
        $c_code = str_pad($one_cat["code"] . " ", 10, ".");
        $c_label = htmlentities($one_cat["label"]);
        $sel_cat .= "\n<option value=\"$c_id\"";
        if ($c_id == $cat_selected) { $sel_cat .= " selected=\"selected\""; }
        $sel_cat .= ">$c_code $c_label</option>";
      }
    }
    $sel_cat .= "</select>";

    $block_cat = "
    <div class=\"searchLabel\">${$l_cat}<br />
      $sel_cat
    </div>";
  }

  return $block_cat;
}

///////////////////////////////////////////////////////////////////////////////
// Display: category consult block
// Parameters:
//   - $cats         : Category array
//   - $entity       : target entity 
//   - $cat          : target category
///////////////////////////////////////////////////////////////////////////////
function of_dis_block_consult_category($cats, $entity, $cat) {
  $l_cat = "l_${cat}"; 
  global $$l_cat;

  $show_cat = (! $cgp_hide["$entity"]["$cat"]);

  if ($show_cat) {
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
        $i++;
        $dis_cat .= "<td style=\"border: 1px solid\" class=\"detailText\"> $label </td>";
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

  return $block_cat;
}


?>
