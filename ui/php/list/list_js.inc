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
  if(index > 1) {
    cell.innerHTML = '<select name=\"sel_log_and[]\"><option value=\"OR\" selected=\"true\">$l_or</option><option value=\"AND\">$l_and</option></select>';
  } 
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"sel_log_and[]\" value=\"\" />';
  }
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
  length = row.cells.length;
  for(j=0;j++;j<length) {
    row.deleteCell(j);
  }
  table = row.parentNode;
  table.deleteRow(i-1);
  table.deleteRow(i-1);
  if(i==1 && table.rows.length >= 1) {
    row = table.rows[i];
    row.cells[3].innerHTML = '<select name=\"sel_log_not[]\"><option value=\"\" selected=\"true\">$l_empty</option><option value=\"NOT\">$l_not</option></select>';
    row.cells[3].innerHTML += '<input type=\"hidden\" name=\"sel_log_and[]\" value=\"\" />';

  }
}

///////////////////////////////////////////////////////////////////////////////
//  Open the update criteria line form                                  //
///////////////////////////////////////////////////////////////////////////////

function open_window(row) {
  i = row.rowIndex-1;
  window.open(window.location+my_url+i,'','height=$popup_height,width=$popup_width,scrollbars=yes,status=yes');
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
    form.elements[i-4].value = cells.item(i).lastChild.value;
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

  if(form.tf_company_country.value != '') {  
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_company: $l_country'; 
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_country[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_country.value+'\" />';
    i++;  
  }
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_company_country[]\" value=\"\" />';
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
  return i;
}

///////////////////////////////////////////////////////////////////////////////
// Perform the Contact Criteria's insertion                              //
///////////////////////////////////////////////////////////////////////////////

function contact_criterion(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];
  if(form.tf_contact_lname.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_lastname';    
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_lastname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_lname.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_lastname[]\" value=\"\" />';
  }

  if(form.tf_contact_fname.value != '') {
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_firstname';      
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_firstname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_fname.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_contact_firstname[]\" value=\"\" />';
  }

  if(form.tf_contact_country.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_contact: $l_country';  
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_country[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_country.value+'\" />';
    i++;    
  }  
  else {
    defaultCell.innerHTML +=  '<input type=\"hidden\" name=\"tf_contact_country[]\" value=\"\" />';
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

  return i;
}

///////////////////////////////////////////////////////////////////////////////
// Perform the Publication Criteria's insertion                              //
///////////////////////////////////////////////////////////////////////////////

function publication_criterion(form,labels,values,i) {
  values.className = 'detailForm';
  defaultCell = values.cells[0];
  if(form.tf_publication_title.value != '') {    
    cell = labels.insertCell(i);
    cell.innerHTML = '$l_publication: $l_title';    
    cell = values.insertCell(i);
    cell.innerHTML = '<input type=\"text\" name=\"tf_publication_title[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_publication_title.value+'\" />';
    i++;
  }
  else {
    defaultCell.innerHTML += '<input type=\"hidden\" name=\"tf_publication_title[]\" value=\"\" />';
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
  return i;
  }


</script>";
