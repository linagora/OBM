<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_js.inc                                                  //
//     - Desc : List javascript functions File                               //
// 200-16-02 Vincent MARGUERIT                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script language=\"JavaScript\">" ;

require("$obminclude//javascript/check_js.inc");

$extra_js .= "
function check_list(list) {
// return list.match($js_regexp_list_name) ;
return true;
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
//  Add a criteria line in the list form                                   //
///////////////////////////////////////////////////////////////////////////////

function add_criterion(form,index) {
  target = window.opener.document;
  if (W3C) {
    criteria = target.getElementById('list_criterion');
  } else if (NS4) {
    criteria = target.layers['list_criterion'];
  } else {
    criteria = target.all['list_criterion'];
  }
  if(index == -1) {
    index = criteria.rows.length;
  }
  else{
    del_row(criteria.rows.item(index+1));
  }
  labels = criteria.insertRow(index); 
  labels.className = 'detailLabel'; 
  values = criteria.insertRow(index+1); 
  labels.insertCell(0);
  labels.insertCell(1); 
  cell = labels.insertCell(2); 
  cell = labels.insertCell(3); 
  values.insertCell(0);
  cell = values.insertCell(1);
  cell.innerHTML = '<a href=\"javascript:void(0);\" id=\"delete_img\" onclick=\"del_row(this.parentNode.parentNode);return false;\"><img src=\"/images/$set_theme/$ico_crow\" alt=\"Delete\" /></a>';
  cell = values.insertCell(2);  
  cell.style.borderRight = '1px solid #7B9EAD';
  cell.style.paddingRight = '5px';
  cell.innerHTML = '<a href=\"javascript:void(0);\" id=\"update_img\" onclick=\"open_window(this.parentNode.parentNode);return false;\"><img src=\"/images/$set_theme/$ico_updated\" alt=\"Update\" /></a>';
  cell = values.insertCell(3);
  cell.innerHTML = '<select name=\"sel_log_and[]\"><option value=\"OR\" selected=\"true\">$l_or</option><option value=\"AND\">$l_and</option></select>';
  cell.innerHTML += '<select name=\"sel_log_not[]\"><option value=\"\" selected=\"true\">$l_empty</option><option value=\"NOT\">$l_not</option></select>';
  i=company_criterion(form,labels,values,4);
  i=contact_criterion(form,labels,values,i);
  i=publication_criterion(form,labels,values,i);
}

///////////////////////////////////////////////////////////////////////////////
//  delete a criteria line in the list form                                  //
///////////////////////////////////////////////////////////////////////////////

function del_row(row) {
  i = row.rowIndex;
  table = row.parentNode;
  table.deleteRow(i-1);
  table.deleteRow(i-1);
}

///////////////////////////////////////////////////////////////////////////////
//  Open the update criteria line form                                  //
///////////////////////////////////////////////////////////////////////////////

function open_window(row) {
  i = row.rowIndex-1;
  window.open(window.location+my_url+i,'','height=650,width=$popup_width,scrollbars=yes,status=yes');
}

///////////////////////////////////////////////////////////////////////////////
// Get the values to update into the update form                             //
///////////////////////////////////////////////////////////////////////////////

function get_values(form) {
  target = window.opener.document;
  if (W3C) {
    criteria = target.getElementById('list_criterion');
  } else if (NS4) {
    criteria = target.layers['list_criterion'];
  } else {
    criteria = target.all['list_criterion'];
  }
  row = criteria.rows.item(".($row_index+1).");
  cells = row.cells;
  for(i=4;i<cells.length;i++) {
    name = cells.item(i).lastChild.name;
    field = eval('form.'+name.substr(0,name.length-2));
    field.value = cells.item(i).lastChild.value;
  }

}

///////////////////////////////////////////////////////////////////////////////
// Perform the Company Criteria's insertion                              //
///////////////////////////////////////////////////////////////////////////////

function company_criterion(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];
  if(form.tf_company_name.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_name[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_name.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +='<input type=\"hidden\" name=\"tf_company_name[]\" value=\"\" />';
  }

  if(form.sel_company_country_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_country'; 
    cell = values.insertCell(i);
    field = form.sel_company_country_id;
    cell.innerHTML = '<select name=\"sel_company_country_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_company_country_id[]\" value=\"\" />';
  }

  if(form.tf_company_zipcode.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_postcode'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_zipcode.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_company_zipcode[]\" value=\"\" />';
  }

  if(form.tf_company_town.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_town'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_town.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_company_town[]\" value=\"\" />';
  }

  if(form.tf_company_timeafter.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_timeafter'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_timeafter[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_timeafter.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_company_timeafter[]\" value=\"\" />';
  }

  if(form.tf_company_timebefore.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_timebefore'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_timebefore[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_timebefore.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_company_timebefore[]\" value=\"\" />';
  }
  
  if(form.sel_company_marketingmanager_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_market'; 
    cell = values.insertCell(i);
    field = form.sel_company_marketingmanager_id;
    cell.innerHTML = '<select name=\"sel_company_marketingmanager_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_company_marketingmanager_id[]\" value=\"\" />';
  }
   
  if(form.sel_companycategory_code.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_category'; 
    cell = values.insertCell(i);
    field = form.sel_companycategory_code;
    cell.innerHTML = '<select name=\"sel_companycategory_code[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_companycategory_code[]\" value=\"\" />';
  }   
  
  if(form.sel_company_datasource_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_datasource'; 
    cell = values.insertCell(i);
    field = form.sel_company_datasource_id;
    cell.innerHTML = '<select name=\"sel_company_datasource_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_company_datasource_id[]\" value=\"\" />';
  }   
  return i;
}

///////////////////////////////////////////////////////////////////////////////
// Perform the Contact Criteria's insertion                              //
///////////////////////////////////////////////////////////////////////////////

function contact_criterion(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];
  if(form.tf_contact_lastname.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_lastname';    
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_lastname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_lastname.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_lastname[]\" value=\"\" />';
  }

  if(form.tf_contact_firstname.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_firstname';      
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_firstname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_firstname.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_firstname[]\" value=\"\" />';
  }

  if(form.sel_contact_country_id.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_country';  
    cell = values.insertCell(i);
    field = form.sel_contact_country_id;
    cell.innerHTML = '<select name=\"sel_contact_country_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;    
  }  
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_country_id[]\" value=\"\" />';
  }

  if(form.tf_contact_zipcode.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_postcode';  
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_zipcode.value+'\" />';
    i++;
  } 
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_zipcode[]\" value=\"\" />';
  }

  if(form.tf_contact_town.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_town';  
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_town.value+'\" />';
    i++;
  }  
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_town[]\" value=\"\" />';
  }

  if(form.tf_contact_timeafter.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_timeafter'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_timeafter[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_timeafter.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_timeafter[]\" value=\"\" />';
  }

  if(form.tf_contact_timebefore.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_timebefore'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_timebefore[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_timebefore.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_timebefore[]\" value=\"\" />';
  }

  if(form.sel_contact_marketingmanager_id.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_market';  
    cell = values.insertCell(i);
    field = form.sel_contact_marketingmanager_id;
    cell.innerHTML = '<select name=\"sel_contact_marketingmanager_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;    
  }  
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_marketingmanager_id[]\" value=\"\" />';
  }

  if(form.sel_contact_datasource_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_datasource'; 
    cell = values.insertCell(i);
    field = form.sel_contact_datasource_id;
    cell.innerHTML = '<select name=\"sel_contact_datasource_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_datasource_id[]\" value=\"\" />';
  }   
  
  if(form.sel_contactcategory1link_category_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_contact_cat1'; 
    cell = values.insertCell(i);
    field = form.sel_contactcategory1link_category_id;
    cell.innerHTML = '<select name=\"sel_contactcategory1link_category_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contactcategory1link_category_id[]\" value=\"\" />';
  }  

  
  if(form.sel_contactcategory2link_category_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_contact_cat2'; 
    cell = values.insertCell(i);
    field = form.sel_contactcategory2link_category_id;
    cell.innerHTML = '<select name=\"sel_contactcategory2link_category_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contactcategory2link_category_id[]\" value=\"\" />';
  }  
  

  if(form.sel_contact_function_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_function'; 
    cell = values.insertCell(i);
    field = form.sel_contact_function_id;
    cell.innerHTML = '<select name=\"sel_contact_function_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_contact_function_id[]\" value=\"\" />';
  }   

  if(form.sel_kind_lang.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_lang'; 
    cell = values.insertCell(i);
    field = form.sel_kind_lang;
    cell.innerHTML = '<select name=\"sel_kind_lang[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_kind_lang[]\" value=\"\" />';
  }    

  if(form.tf_contact_mailing_ok.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_mailing';      
    cell = values.insertCell(i);
    field = form.tf_contact_mailing_ok;
    cell.innerHTML = '<select name=\"tf_contact_mailing_ok[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_mailing_ok[]\" value=\"\" />';
  }  
  
  return i;
}

///////////////////////////////////////////////////////////////////////////////
// Perform the Publication Criteria's insertion                              //
///////////////////////////////////////////////////////////////////////////////

function publication_criterion(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];

  if(form.sel_subscription_publication_id.value != '') {  
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


  
  if(form.tf_publication_lang.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_lang';      
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_publication_lang[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_publication_lang.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_publication_lang[]\" value=\"\" />';
  }

  if(form.tf_publication_year.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_year';  
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_publication_year[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_publication_year.value+'\" />';
    i++;    
  }  
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_publication_year[]\" value=\"\" />';
  }

  if(form.tf_subscription_renewal.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_renew';      
    cell = values.insertCell(i);
    field = form.tf_subscription_renewal;
    cell.innerHTML = '<select name=\"tf_subscription_renewal[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_subscription_renewal[]\" value=\"\" />';
  }  


  if(form.sel_subscription_reception_id.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_recept'; 
    cell = values.insertCell(i);
    field = form.sel_subscription_reception_id;
    cell.innerHTML = '<select name=\"sel_subscription_reception_id[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\"><option value=\"'+ field.options[field.selectedIndex].value+'\">'+ field.options[field.selectedIndex].text+'</option></select>';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"sel_subscription_reception_id[]\" value=\"\" />';
  }  
  
  return i;
  }
///////////////////////////////////////////////////////////////////////////////
// Show the manual query field                              //
///////////////////////////////////////////////////////////////////////////////

function show_query_field(value1,value2,field) {
  ta_display = document.getElementById('ta_query').style.display;
  di_display = document.getElementById('di_query').style.display;
  if(ta_display != di_display && !confirm('$l_warning_lose_query')) {
    return false;
  }
  if (W3C) {
    table = document.getElementById('list_criterion');
  } else if (NS4) {
    table = document.layers['list_criterion'];
  } else {
    table = document.all['list_criterion'];
  }
  if (W3C) {
    document.getElementById('ta_query').style.display = value1;
    document.getElementById('di_query').style.display = value2;
  } else if (NS4) {
    document.layers['ta_query'].display = value1;
    document.getElementById('di_query').style.display = value2;
  } else {
    document.all['ta_query'].style.display = value1;
    document.getElementById('di_query').style.display = value2;
  }  
  len = table.rows.length
  for(i=0;i<len;i++) 
    table.deleteRow(0);  
}
</script>";
