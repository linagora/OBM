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

</script>
";
</SCRIPT>

