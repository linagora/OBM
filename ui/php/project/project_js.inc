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

  // MANDATORY : Check that the Project soldtime is not empty
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  else
    return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////

function check_init_project(form) {

  // MANDATORY : Check that the Project soldtime is not empty
  if (trim(form.tf_soldtime.value) == \"\") {
    alert (\"$l_fill_soldtime\");
    return false;
  }

  // MANDATORY : Check that the Project soldtime is not empty
  else if (!(check_number(form.tf_soldtime.value))) {
    alert (\"$l_fill_soldnumber\");
    return false;
  }

  else
    return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Select the appropriate action for the form                                //
///////////////////////////////////////////////////////////////////////////////
function check_taskadd(form)
{
  // MANDATORY : Check that the Project soldtime is not empty
  if (trim(form.tf_tasklabel.value) == \"\") {
    alert (\"$l_fill_tasklabel\");
    return false;
  }
  else {
    return true;
  }
}

// function submit_taskdel(form)
// {
//   form.action = \"project_index.php?action=task_del\"
//   form.submit();
// }

// function submit_taskvalid(form)
// {
//   form.action = \"project_index.php?action=member_fill\"
//   form.submit();
// }

function submit_memberdel(form)
{
  form.action = \"project_index.php?action=member_del\"
  form.submit();
}

function check_membervalid(form, nb_mem)
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
    form.submit();
    return true;
  }
}

function submit_memberadd(form, p_id)
{
    window.name='Liste';
    window.open('../user/user_index.php?action=ext_get_ids&popup=1&title=$l_member_add&ext_action=member_add&ext_url=../project/project_index.php&ext_id='+ p_id +'&ext_target=Liste','','height=400,width=620,scrollbars=yes');
    return false;
}

// -->
</script>
";
