<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : document_js.inc                                              //
//     - Desc  : Document javascript functions File                           //
// 2003-08-22 Rande Mehdi                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
echo "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");
echo "
function check_document(form) {
  // MANDATORY : Check that the Document title is not empty
  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }

  // Check the author
  lzip = form.tf_author.value;
  if (trim(form.tf_author.value) == \"\") {
    alert (\"$l_fill_author\");
    return false;
  }

  // Check the path
  if (trim(form.tf_path.value) == \"\") {
    alert (\"$l_fill_path\");
    return false;
  }

  return true;
}

function check_repository(form) {
  // MANDATORY : Check that the Document title is not empty
  if (trim(form.tf_repository_name.value) == \"\") {
    alert (\"$l_fill_repository_name\");
    return false;
  }

  return true;
}

function check_cat1_new(form)
{
   if (form.tf_cat1.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_cat1_create\");
      return false;
   }
}

function check_cat2_new(form)
{
   if (form.tf_cat2.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_cat2_create\");
      return false;
   }
}

function check_mime_new(form)
{
   if (form.tf_mime.value != \"\"
       && form.tf_extension.value != \"\"
       && form.tf_mimetype.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_mime_create\");
      return false;
   }
}

function check_cat1_upd(form,formd)
{
  cat1_pos = formd.sel_cat1.selectedIndex;
  if ((cat1_pos >= 0) && (form.tf_cat1.value != \"\")) {
    form.sel_cat1.value=formd.sel_cat1.options[cat1_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat1_update\");
     return false;
   }
}


function check_cat2_upd(form,formd)
{
  cat2_pos = formd.sel_cat2.selectedIndex;
  if ((cat2_pos >= 0) && (form.tf_cat2.value != \"\")) {
    form.sel_cat2.value=formd.sel_cat2.options[cat2_pos].value;
    return true;
   } else {
     alert(\"$l_j_cat2_update\");
     return false;
   }
}

function check_mime_upd(form,formd)
{
  mime_pos = formd.sel_mime.selectedIndex;
  if ((mime_pos >= 0) && (form.tf_mime.value != \"\")
       && (form.tf_extension.value != \"\") && (form.tf_mimetype.value != \"\")) {
    form.sel_mime.value=formd.sel_mime.options[mime_pos].value;
    return true;
   } else {
     alert(\"$l_j_mime_update\");
     return false;
   }
}

function check_cat1_checkdel(form)
{
   if (form.sel_cat1.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat1_delete\");
      return false;
   }
}

function check_cat2_checkdel(form)
{
   if (form.sel_cat2.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_cat2_delete\");
      return false;
   }
}


function check_mime_checkdel(form)
{
   if (form.sel_mime.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_mime_delete\");
      return false;
   }
}

NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	

function show_hide_repo(name)
{
  if (W3C) {
    if(document.getElementById(name).style.display != \"none\") {
      hide(name);
     }
     else{
        show(name);
     }
  } else if (NS4) {
    if(document.layers[name].display != \"none\") {
      hide(name);
    }
    else{
      show(name);
     }

  } else {
    if(document.all[name].style.display != \"none\") {
       hide(name);
    }
    else{
      show(name);
    }
  
  }

}

function hide ( name ) {
  if (W3C) {
    document.getElementById(name).style.display = \"none\";
    document.getElementById(name).style.height = \"0px\";
    document.getElementById('img_'+name).src = \"/images/$set_theme/$ico_directory_close\";    
  } else if (NS4) {
    document.layers[name].display = \"hide\";
    document.layers[name].height = \"0px\";
    document.layers['img_'+name].src = \"/images/$set_theme/$ico_directory_close\";    
  } else {
    document.all[name].style.display = \"none\";
    document.all[name].style.height = \"0px\";
    document.all['img_'+name].src = \"/images/$set_theme/$ico_directory_close\";
  }
}

function show ( name ) {
  if (W3C) {
    document.getElementById(name).style.display = \"\";
    document.getElementById(name).style.height = \"auto\"; 
    document.getElementById('img_'+name).src = \"/images/$set_theme/$ico_directory_open\";
  } else if (NS4) {
    document.layers[name].display = \"\";
    document.layers[name].height = \"auto\";
    document.layers['img_'+name].src = \"/images/$set_theme/$ico_directory_open\";
  } else {
    document.all[name].style.display = \"\";
    document.all[name].style.height = \"auto\";
    document.all['img_'+name].src = \"/images/$set_theme/$ico_directory_open\";
  }
}


function show_hide_field()
{ 
  radios = document.f_mod_document.rd_kind;
  if(radios[0].checked == true) {
    basic_show('browse_img');
    basic_hide('browse_img_file');
    basic_show('browse_field');
    basic_hide('url_field');
    document.f_mod_document.fi_file.value = false;
  }
  else if(radios[1].checked == true) {
    basic_hide('browse_img');
    basic_show('browse_img_file');
    basic_hide('browse_field');
    basic_hide('url_field');
    
  }
  else if(radios[2].checked == true) {
    basic_hide('browse_img_file');
    basic_show('browse_img');
    basic_hide('browse_field');
    basic_show('url_field');
    document.f_mod_document.preurl.value =\"http://\";
  }
  else if(radios[3].checked == true) {
    basic_hide('browse_img_file');
    basic_show('browse_img');
    basic_hide('browse_field');
    basic_show('url_field');
    document.f_mod_document.preurl.value =\"https://\";
  }  
}


function basic_hide(name) {
  if (W3C) {
    document.getElementById(name).style.display = \"none\";
   
  } else if (NS4) {
    document.layers[name].display = \"none\";
  } else {
    document.getElementById(name).style.display = \"none\";
  }
}


function basic_show(name)
{ 
  if (W3C) {
    document.getElementById(name).style.display = \"\";
  } else if (NS4) {
    document.layers[name].display = \"\";
  } else {
    document.all[name].style.display = \"\";

  }
}







";
if(isset($ext_target)) {
  echo "
   function fill_ext_form(repo_path) {
     ext_field = window.opener.document.$ext_target;
     ext_field.value = repo_path;
     window.close();
  }
  ";
}
echo "
-->
</script>
";
</SCRIPT>

