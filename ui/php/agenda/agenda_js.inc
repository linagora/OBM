<?
echo "
<script type=\"text/javascript\">
<!--
";
require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");
echo "
function check_calendar(form) {

  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }
  else if (check_date(form.tf_date_begin, \"$l_incorect_datebegin\") == false) {
    return false;
  }
  else if (check_date(form.tf_date_end, \"$l_incorect_dateend\") == false) {
    return false;
  }
  else if ((trim(form.tf_repeat_end.value) != \"\") && (check_date(form.tf_repeat_end, \"$l_incorect_repeat\") == false)) {
    return false;
  }

    return true;
  
} 
function check_all_force(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value.toString() == 'force') {
      form.elements[i].checked = 1;

    }
  }
} 
function check_all_replace(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value.toString() == 'replace') {
      form.elements[i].checked = 1;

    }
  }
} 
function check_all_cancel(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value == 'cancel') {
      form.elements[i].checked = 1;
    }
  }
} 

//-->
</script>

";
?>
