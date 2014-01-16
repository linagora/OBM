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
// OBM - File : list_js.inc                                                  //
//     - Desc : List javascript functions File                               //
// 2000-02-16 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function check_list(list) {
// return list.match($js_regexp_list_name) ;
return true;
}

function check_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_list\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}

function check_name_list(form) {
  name_list = form.tf_name.value ;
  subject_list = form.tf_subject.value ;

  if (trim(name_list) == \"\") {
     alert(\"$l_fill_name\") ;
     return false ;
  }

  if ( (name_list != \"\") && (check_list(name_list) == null)) {
    alert(\"$l_name_list_nomatch\");
    return false;
  }

  if (trim(subject_list) == \"\") {
     alert(\"$l_fill_subject\") ;
     return false ;
  }
  return true ;
}

NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	
my_url = '&action=new_criterion&popup=1&row_index=';


///////////////////////////////////////////////////////////////////////////////
// Add a criteria line in the list form
///////////////////////////////////////////////////////////////////////////////
function list_add_criteria_line(form,index) {

  target = window.opener.document;
  if (W3C) {
    criteria = target.getElementById('list_criterion');
  } else if (NS4) {
    criteria = target.layers['list_criterion'];
  } else {
    criteria = target.all['list_criterion'];
  }
  if (index == -1) {
    index = criteria.rows.length;
  } else {
    del_row(criteria.rows.item(index+1));
  }
  labels = criteria.insertRow(index);
  labels.className = 'head';
  values = criteria.insertRow(index+1);
  labels.insertCell(0);
  labels.insertCell(1);
  cell = labels.insertCell(2);
  cell = labels.insertCell(3);
  cell = labels.insertCell(4);
  values.insertCell(0);
  cell = values.insertCell(1);
  cell.innerHTML = '<a href=\"javascript:void(0);\" id=\"delete_img\" onclick=\"del_row(this.parentNode.parentNode);return false;\"><img src=\"$ico_delete\" alt=\"Delete\" /></a>';
  cell = values.insertCell(2);
  cell.style.borderRight = '1px solid #7B9EAD';
  cell.style.paddingRight = '5px';
  cell.innerHTML = '<a href=\"javascript:void(0);\" id=\"update_img\" onclick=\"open_window(this.parentNode.parentNode);return false;\"><img src=\"$ico_update\" alt=\"Update\" /></a>';
  cell = values.insertCell(3);
  cell.innerHTML = '<select name=\"sel_log_and[]\"><option value=\"OR\" selected=\"true\">$l_or</option><option value=\"AND\">$l_and</option></select>';
  cell = values.insertCell(4);
  cell.innerHTML += '<select name=\"sel_log_not[]\"><option value=\"\" selected=\"true\">$l_empty</option><option value=\"NOT\">$l_not</option><option value=\"Exclude\">$l_exclude</option></select>';";
if ($cgp_show['module']['company']) {
  $extra_js .= "
  i = list_add_company_criteria_line(form,labels,values,5);";
 } else {
  $extra_js .= "
  i = 5;";
 }
  
$extra_js .= "
  i = list_add_contact_criteria_line(form,labels,values,i);";
if ($cgp_show['module']['publication']) {
  $extra_js .= "
  i = list_add_publication_criteria_line(form,labels,values,i);";
}
$extra_js .= "
}


///////////////////////////////////////////////////////////////////////////////
// delete a criteria line in the list form
///////////////////////////////////////////////////////////////////////////////
function del_row(row) {

  i = row.rowIndex;
  table = row.parentNode;
  table.deleteRow(i-1);
  table.deleteRow(i-1);
}


///////////////////////////////////////////////////////////////////////////////
// Open the update criteria line form
///////////////////////////////////////////////////////////////////////////////
function open_window(row) {

  i = row.rowIndex-1;
  popup(window.location+my_url+i,'','650');
}


///////////////////////////////////////////////////////////////////////////////
// Get the values from the list form to initialize the update criterion form
// After displaying the form, values are picked up from the list query form
// List normal query form => Add criterion form
///////////////////////////////////////////////////////////////////////////////
function get_list_critria_from_query(form) {

  target = window.opener.document;
  if (W3C) {
    criteria = target.getElementById('list_criterion');
  } else if (NS4) {
    criteria = target.layers['list_criterion'];
  } else {
    criteria = target.all['list_criterion'];
  }
  row = criteria.rows.item(".($params["row_index"]+1).");
  cells = row.cells;
  for (i=5;i<cells.length;i++) {
    name = cells.item(i).lastChild.name;
    field = eval('form.'+name.substr(0,name.length-2));
    try {
      if (field.nodeName == 'SELECT') {
        values = cells.item(i).lastChild.value.split(',$separator');
        for(k=0; k < field.options.length; k++) {
          for(j=0; j < values.length; j++) {
            if(values[j] == field.options[k].value) {
              field.options[k].selected = true;
            }
          }
        }
      } else if (field.nodeName == 'INPUT') {
        field.value = cells.item(i).lastChild.value;
      }

    } catch (e) {
      // last child is an of_select : cell has a checbox + a select

      // handle the checkbox value
      cb = cells.item(i).firstChild.value;
      if (cb == 'true') {
        of_select_set_tree(window.document, name.substr(0,name.length-2), cb);
      }

      // of_select objects (field.nodeName undefined)
      values = cells.item(i).lastChild.value.split(',$separator');
      labels = cells.item(i).lastChild.title.split(',$separator');
      for (j=0; j < values.length; j++) {
        of_select_add_element(window.document, name.substr(0,name.length-2), values[j], labels[j]);
      }
    }
  }

}


///////////////////////////////////////////////////////////////////////////////
// Perform the Company Criteria's insertion
// Add criterion => List query form
// Parameters:
//   - i : column index
///////////////////////////////////////////////////////////////////////////////
function list_add_company_criteria_line(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];

  if (form.tf_company_name.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company';
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_name[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_name.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_company_name[]\" value=\"\" />';
  }

  if (form.sel_company_country_iso3166.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_country';
    cell = values.insertCell(i);
    field = form.sel_company_country_iso3166;
    cell.innerHTML = '<select name=\"sel_company_country_iso3166[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"sel_company_country_iso3166[]\" value=\"\" />';
  }

  if (form.tf_company_zipcode.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_postcode'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_zipcode.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_company_zipcode[]\" value=\"\" />';
  }

  if (form.tf_company_town.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_town'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_town.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_company_town[]\" value=\"\" />';
  }

  if (form.tf_company_timeafter.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_timeafter'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_timeafter[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_company_timeafter.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_company_timeafter[]\" value=\"\" />';
  }

  if (form.tf_company_timebefore.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_timebefore'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_timebefore[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_company_timebefore.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_company_timebefore[]\" value=\"\" />';
  }
  
  if (form.sel_company_marketingmanager_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_market'; 
    cell = values.insertCell(i);
    field = form.sel_company_marketingmanager_id;
    cell.innerHTML = '<select name=\"sel_company_marketingmanager_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"sel_company_marketingmanager_id[]\" value=\"\" />';
  }

  if (form.sel_company_datasource_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_datasource'; 
    cell = values.insertCell(i);
    field = form.sel_company_datasource_id;
    cell.innerHTML = '<select name=\"sel_company_datasource_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"sel_company_datasource_id[]\" value=\"\" />';
  }
";

// User data categories handling
if (is_array($cgp_user["company"]["category"])) {
  foreach($cgp_user["company"]["category"] as $cat_name => $one_cat) {
    $cat_label = ${"l_$cat_name"};

    $extra_js .= "
  el = of_select_get_element_array('sel_$cat_name');
  nb_el = el.length;
  el_title = '';
  el_id = '';
  cb_value = el[cb];
  if (nb_el > 0) {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $cat_label';
    cell = values.insertCell(i);
    // display the 1st value as selected
    id = el[0]['id'];
    label = el[0]['text'];
    // Construct the title and id with all the selection as coma sep values
    for (cp=0; cp < nb_el; cp++) {
      if ((el_title != '') || (el_id != '')) {
        el_title += ', ';
        el_id += ',';
      }
      el_id += el[cp]['id'];
      el_title += el[cp]['text'];
    }
    // add firstchild : cb input, lastchild : select
    cell.innerHTML = '<input type=\"text\" name=\"sel_${cat_name}_tree[]\" value=\"'+cb_value+'\"><select title=\"'+el_title+'\" name=\"sel_${cat_name}[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ el_id +'\">'+ label +'('+ nb_el +')</option></select>';
    i++;

  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"sel_${cat_name}_tree[]\" value=\"\"><input type=\"hidden\" name=\"sel_${cat_name}[]\" value=\"\" />';
  }
";
  }
}

$extra_js .= "
  return i;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the Contact Criteria's insertion
///////////////////////////////////////////////////////////////////////////////
function list_add_contact_criteria_line(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];

  if (form.tf_contact_lastname.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_lastname';    
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_lastname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_lastname.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_lastname[]\" value=\"\" />';
  }

  if (form.tf_contact_firstname.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_firstname';      
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_firstname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_firstname.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_firstname[]\" value=\"\" />';
  }

  if (form.sel_contact_country_iso3166.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_country';
    cell = values.insertCell(i);
    field = form.sel_contact_country_iso3166;
    cell.innerHTML = '<select name=\"sel_contact_country_iso3166[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_country_iso3166[]\" value=\"\" />';
  }

  if (form.tf_contact_zipcode.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_postcode';
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_zipcode.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_zipcode[]\" value=\"\" />';
  }

  if (form.tf_contact_town.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_town';
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_town.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_town[]\" value=\"\" />';
  }

  if (form.tf_contact_timeafter.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_timeafter';
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_timeafter[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_contact_timeafter.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_timeafter[]\" value=\"\" />';
  }

  if (form.tf_contact_timebefore.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_timebefore';
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_timebefore[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_contact_timebefore.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_timebefore[]\" value=\"\" />';
  }

  if (form.sel_contact_marketingmanager_id.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_market';
    cell = values.insertCell(i);
    field = form.sel_contact_marketingmanager_id;
    cell.innerHTML = '<select name=\"sel_contact_marketingmanager_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_marketingmanager_id[]\" value=\"\" />';
  }

  if (form.sel_contact_datasource_id.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_datasource';
    cell = values.insertCell(i);
    field = form.sel_contact_datasource_id;
    cell.innerHTML = '<select name=\"sel_contact_datasource_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_datasource_id[]\" value=\"\" />';
  }
  
  if (form.sel_contact_function_id.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_function';
    cell = values.insertCell(i);
    field = form.sel_contact_function_id;
    cell.innerHTML = '<select name=\"sel_contact_function_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_function_id[]\" value=\"\" />';
  }

  if (form.sel_kind_lang.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_lang';
    cell = values.insertCell(i);
    field = form.sel_kind_lang;
    cell.innerHTML = '<select name=\"sel_kind_lang[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_kind_lang[]\" value=\"\" />';
  }
";

// User data categories handling
if (is_array($cgp_user["contact"]["category"])) {
  foreach($cgp_user["contact"]["category"] as $cat_name => $one_cat) {
    $cat_label = ${"l_$cat_name"};

    $extra_js .= "
  el = of_select_get_element_array('sel_$cat_name');
  nb_el = el.length;
  el_title = '';
  el_id = '';
  cb_value = el[cb];
  if (nb_el > 0) {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $cat_label';
    cell = values.insertCell(i);
    // display the 1st value as selected
    id = el[0]['id'];
    label = el[0]['text'];
    // Construct the title and id with all the selection as coma sep values
    for (cp=0; cp < nb_el; cp++) {
      if ((el_title != '') || (el_id != '')) {
        el_title += ', ';
        el_id += ',';
      }
      el_id += el[cp]['id'];
      el_title += el[cp]['text'];
    }
    // add firstchild : cb input, lastchild : select
    cell.innerHTML = '<input type=\"text\" name=\"sel_${cat_name}_tree[]\" value=\"'+cb_value+'\"><select title=\"'+el_title+'\" name=\"sel_${cat_name}[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ el_id +'\">'+ label +'('+ nb_el +')</option></select>';
    i++;

  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"sel_${cat_name}_tree[]\" value=\"\"><input type=\"hidden\" name=\"sel_${cat_name}[]\" value=\"\" />';
  }
";
  }
}

$extra_js .= "
  return i;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the Publication Criteria's insertion
///////////////////////////////////////////////////////////////////////////////
function list_add_publication_criteria_line(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];

  if (form.sel_subscription_publication_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_title'; 
    cell = values.insertCell(i);
    field = form.sel_subscription_publication_id;
    cell.innerHTML = '<select name=\"sel_subscription_publication_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_subscription_publication_id[]\" value=\"\" />';
  }  

  if (form.tf_publication_lang.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_lang';      
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_publication_lang[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_publication_lang.value+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_publication_lang[]\" value=\"\" />';
  }

  if (form.tf_publication_year.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_year';  
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_publication_year[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_publication_year.value+'\" />';
    i++;    
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_publication_year[]\" value=\"\" />';
  }

  if (form.tf_subscription_renewal.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_renew';      
    cell = values.insertCell(i);
    field = form.tf_subscription_renewal;
    cell.innerHTML = '<select name=\"tf_subscription_renewal[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_subscription_renewal[]\" value=\"\" />';
  }  

  if (form.sel_subscription_reception_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_recept'; 
    cell = values.insertCell(i);
    field = form.sel_subscription_reception_id;
    cell.innerHTML = '<select name=\"sel_subscription_reception_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  } else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_subscription_reception_id[]\" value=\"\" />';
  }

  if (form.tf_subscription_timeafter.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_after'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_subscription_timeafter[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_subscription_timeafter.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_subscription_timeafter[]\" value=\"\" />';
  }

  if (form.tf_subscription_timebefore.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_before'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_subscription_timebefore[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+getFieldIsoDate(form.tf_subscription_timebefore.value)+'\" />';
    i++;
  } else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_subscription_timebefore[]\" value=\"\" />';
  }
  
  return i;
}


///////////////////////////////////////////////////////////////////////////////
// Show the manual query field
///////////////////////////////////////////////////////////////////////////////
function show_query_field(value_exp, value_nor, field) {
  ta = document.getElementById('ta_query');
  di = document.getElementById('di_query');
  ta_display = ta.style.display;
  di_display = di.style.display;

  exp_to_nor = '$l_warning_exp_to_nor';
  nor_to_exp = '$l_warning_nor_to_exp';

  if (value_nor == 'none') {
    txt = nor_to_exp;
  } else {
    txt = exp_to_nor;
  }

  if (ta_display != di_display && !confirm(txt)) {
    return false;
  }

  // We delete the query
  if (value_exp == 'none') {
    ta.value = '';
  }

  if (W3C) {
    table = document.getElementById('list_criterion');
  } else if (NS4) {
    table = document.layers['list_criterion'];
  } else {
    table = document.all['list_criterion'];
  }
  if (W3C) {
    ta.style.display = value_exp;
    di.style.display = value_nor;
  } else if (NS4) {
    document.layers['ta_query'].display = value_exp;
    di.style.display = value_nor;
  } else {
    document.all['ta_query'].style.display = value_exp;
    di.style.display = value_nor;
  }  
  if(value_nor == 'none') {
    table.innerHTML = '';
  }
}
";

?>
