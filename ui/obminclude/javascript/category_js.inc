<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : JavaScript Embedded in PHP                               //
//     - File     : check_js.inc                                             //
// 2005-12-01 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// JAVASCRIPT functions for categories                                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


$extra_js .= "
///////////////////////////////////////////////////////////////////////////////
// Check if the Category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_category_new(label,category) {
   if (label.value != \"\") {
      return true;
   } else {
      msg = category+' : $l_c_j_create';
      alert(msg);
      return false;
   }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the Category check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_category_checkdel(sel,category) {
   if (sel.selectedIndex >= 0) {
      return true;
   } else {
      msg = category+' : $l_c_j_delete';
      alert(msg);
      return false;
   }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the Category update is possible
///////////////////////////////////////////////////////////////////////////////
function check_category_upd(label,sel,sel_d,category) {
   cat1_pos = sel_d.selectedIndex;
   if ((cat1_pos >= 0) && (label.value != \"\")) {
     sel.value=sel_d.options[cat1_pos].value;
     return true;
   } else {
      msg = category+' : $l_c_j_update';
      alert(msg);
      return false;
   }
}

";
</script>
