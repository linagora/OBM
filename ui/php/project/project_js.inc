<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : project_js.inc                                              //
//     - Desc  : Project javascript functions File                           //
// 2003-07-08 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


echo "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");

echo "

function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////

function check_project(form) {

  // MANDATORY : Check that the Project soldtime is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not empty
  else if ((form.sel_tt.value) == 0) {
    alert (\"$l_fill_tt\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not empty
  else if (trim(form.tf_soldtime.value) == \"\") {
    alert (\"$l_fill_soldtime\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is a number
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not null
  else if ((form.tf_soldtime.value) == 0) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  else
    return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if the init project form                                            //
///////////////////////////////////////////////////////////////////////////////

function check_init_project(form) {

  // MANDATORY : Check that the Project soldtime is not empty
  if (trim(form.tf_soldtime.value) == \"\") {
    alert (\"$l_fill_soldtime\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is a number
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not null
  else if ((form.tf_soldtime.value) == 0) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  else
    return true;
} 

///////////////////////////////////////////////////////////////////////////////
// Check if the short init project form                                      //
///////////////////////////////////////////////////////////////////////////////

function check_short_init_project(form) {

  // MANDATORY : Check that the Project soldtime is not empty
  if (trim(form.tf_soldtime.value) == \"\") {
    alert (\"$l_fill_soldtime\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is a number
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not null
  else if ((form.tf_soldtime.value) == 0) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not null
  else if ((form.sel_member.value) == 0) {
    alert (\"$l_fill_member\");
    return false;
  }
  else
    return true;
} 

///////////////////////////////////////////////////////////////////////////////
// Select the task add form                                                  //
///////////////////////////////////////////////////////////////////////////////
function check_taskadd(form)
{
  // MANDATORY : Check that the ProjectTask label is not empty
  if (trim(form.tf_tasklabel.value) == \"\") {
    alert (\"$l_fill_tasklabel\");
    return false;
  }
  else {
    return true;
  }
}

function submit_memberdel(form)
{
  form.action = \"project_index.php?action=member_del\"
  form.submit();
}

///////////////////////////////////////////////////////////////////////////////
// Select the project members form                                           //
///////////////////////////////////////////////////////////////////////////////
// each member should be assigned to at least one task

function change_status(user_id, user_status, form)
{
  for (i = 0; i < form.length; i++) {
    i_name = form.elements[i].name;
    i_value = form.elements[i].value;
    
    if (i_name == \"param_user\")
      form.elements[i].value = user_id;
    else if (i_name == \"param_status\")
      form.elements[i].value = user_status;
    else if (i_name == \"action\")
      form.elements[i].value = \"member_change\";
    else
      null;
  }

  form.submit();
}

function check_membertimevalid(form, nb_mem)
{
  error = 0;
  idx = 0;
  i_nb = new Array(nb_mem);

  for (i = 0; i < document.forms[0].length; i++) {
    i_name = document.forms[0].elements[i].name.substr(0,12);
    i_value = document.forms[0].elements[i].value;
    
    if (i_name == \"tf_projected\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 1;
      } else if (i_value != \"\") {
	i_nb[idx]=1;
      } else {
	;
      }

      idx++;
      
      if (idx == nb_mem) {
	idx = 0;
      }
    }
  }
  
  for (idx=0; idx<nb_mem; idx++) {
    if (i_nb[idx] != 1) {
      error += 2;
    }
  }

  if (error == 1) {
    alert (\"$l_fill_projnumber\");
    return false;
  } else if (error != 0) {
    alert (\"$l_fill_projected\");
    return false;
  } else {
    form.action = \"project_index.php?action=validate\";
    //    form.submit();
    return true;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Select the advance update form                                            //
///////////////////////////////////////////////////////////////////////////////

function check_advancevalid(form)
{
  error = 0;

  for (i = 0; i < document.forms[0].length; i++) {
    i_name = form.elements[i].name.substr(0,10);
    i_value = form.elements[i].value;

    if (i_name == \"tf_missing\") { 
      if ((i_value != \"\") && (!(check_number(i_value)))) {
	error = 1;
      } else if (i_value == \"\") {
	error += 2 ;
      } else {
	;
      }
    }
  }

  if (error == 1) {
    alert (\"$l_fill_missnumber\");
    return false;
  } else if (error != 0) {
    alert (\"$l_fill_missing\");
    return false;
  } else {
    return true;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Open the userobm list form window                                         //
///////////////////////////////////////////////////////////////////////////////

function submit_memberadd(form, p_id)
{
    window.name='Liste';
    window.open('../user/user_index.php?action=ext_get_ids&popup=1&title=$l_member_add&ext_action=member_add&ext_url=../project/project_index.php&ext_id='+ p_id +'&ext_target=Liste','','height=400,width=620,scrollbars=yes');
    return false;
}

// -->
</script>
";
