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
    del_row(criteria.rows.item(index));
  }
  values = criteria.insertRow(index); 
  cell = values.insertCell(0);
  cell.innerHTML = '<a href=\"javascript:void(0);\" onclick=\"del_row(this.parentNode.parentNode);return false;\"><img src=\"/images/$set_theme/$ico_crow\" alt=\"Delete\" /></a>';
  cell = values.insertCell(1);  
  cell.innerHTML = '<a href=\"javascript:void(0);\" onclick=\"open_window(this.parentNode.parentNode);return false;\"><img src=\"/images/$set_theme/$ico_updated\" alt=\"Update\" /></a>';
  i=company_criterion(form,values,2);
  i=contact_criterion(form,values,i);
}

function del_row(row) {
  i = row.rowIndex;
  row.parentNode.deleteRow(i);
  
}

function open_window(row) {
  i = row.rowIndex;
  window.open(window.location+my_url+i,'','height=$popup_height,width=$popup_width,scrollbars=yes,status=yes');
}

function get_values(form) {
  target = window.opener.document;
  if (W3C) {
    criteria = target.getElementById('list_criterion');
  } else if (NS4) {
    criteria = target.layers['list_criterion'];
  } else {
    criteria = target.all['list_criterion'];
  }
  row = criteria.rows.item($row_index);
  cells = row.cells;
  for(i=2;i<cells.length;i++) {
    form.elements[i-2].value = cells.item(i).lastChild.value;
  }

}
  
function company_criterion(form,values,i) {
  values.className = 'detailForm';
  cell = values.insertCell(i);
  if(form.tf_company_name.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_name[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_name.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_company_name[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_company_country.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_country[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_country.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_company_country[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_company_zipcode.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_zipcode.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_company_zipcode[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_company_town.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_company_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_company_town.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_company_town[]\" value=\"\" />';
  }

  i++;
  return i;
}

function contact_criterion(form,value,i) {
  values.className = 'detailForm';
  cell = values.insertCell(i);
  if(form.tf_contact_lname.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_lastname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_lname.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_contact_lastname[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_contact_fname.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_firstname[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_fname.value+'\" />';
  }
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_contact_firstname[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_contact_country.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_country[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_country.value+'\" />';
  }  
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_contact_country[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_contact_zipcode.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_zipcode[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_zipcode.value+'\" />';
  } 
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_contact_zipcode[]\" value=\"\" />';
  }

  i++;
  cell = values.insertCell(i);
  if(form.tf_contact_town.value != '') {    
    cell.innerHTML = '<input type=\"text\" name=\"tf_contact_town[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"'+form.tf_contact_town.value+'\" />';
  }  
  else {
    cell.innerHTML = '<input type=\"hidden\" name=\"tf_contact_town[]\" value=\"\" />';
  }

  i++;
  return i;
  }

</script>";
