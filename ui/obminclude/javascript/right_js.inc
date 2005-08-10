<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : JavaScript Embedded in PHP                               //
//     - File     : right_js.inc                                             //
// 2005-08-08 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// Fonctions JAVASCRIPT gestion des droits                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$                     //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script type=\"text/javascript\">

function remove_allelements(parentId) {
  // Remove all elements in the list form
  target = window.document;
  container = target.getElementById(parentId)
  while(container.hasChildNodes()==true){
    var ChildN=container.childNodes[0];
    container.removeChild(ChildN);
  }
}

function show_hide_read_right(field) {
  // Show/Hide read rights
  target = window.document;
  lnk_read = target.getElementById('lnk_accept_read');
  sel_read = target.getElementById('sel_accept_read');
  if(field.checked) {
    remove_allelements('sel_accept_read');
    lnk_read.style.display = 'none';
  } else {
    lnk_read.style.display = 'inline';
  }
}

function show_hide_write_right(field) {
  // Show/Hide write rights 
  target = window.document;
  lnk_write = target.getElementById('lnk_accept_write');
  sel_write = target.getElementById('sel_accept_write');
  if(field.checked) {
    remove_allelements('sel_accept_write');
    lnk_write.style.display = 'none';
  } else {
    lnk_write.style.display = 'inline';
  }
}

</script>
";

</script>
