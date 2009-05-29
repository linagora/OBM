<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_js.inc                                                 //
//     - Desc : Group javascript functions File                              //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("obminclude/javascript/check_js.inc");


// to avoid a javascript error when ext_widget is not defined
if ($params["ext_widget"] != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   ext_field = window.opener.document.$params[ext_widget];
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 for(j=0; j< ext_size; j++) {
	   if('cb_g' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$params[ext_widget].options[j].selected =true;
	   }
	 }
       }
     }
   }
}


function check_group_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_group\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      if ('$params[restriction_calendar]' == 'calendar') {
        window.opener.location=('../calendar/calendar_index.php?date=$params[date]&group_id='+valeur+'&new_group=1');
      }
      if ('$params[ext_target]' == '$l_time') {
        window.opener.location=('../time/time_index.php?action=$params[ext_action]&date=$params[date]&group_id='+valeur);
      }
      window.close();
      return true;
    }
}
";
}

$extra_js .= "
function add_email_field(home, name, template) {
  var mailHome = $(home);
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'groupMailHome');
              show_hide_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  var input = new Element('input').setProperty('name',name + '[]').setProperty('type','text');
  div.appendText(' ').adopt(input);
  if(template) {
    div.appendText(' @ ');
    div.adopt(template.clone());
  }
  return input; 
}  

function show_hide_add_button() {
  var button = $('addMailButton'); 
  var parent = $('groupMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_group_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_group_alias] && button) {
      button.dispose();
   } else if(childs.length < $GLOBALS[c_max_group_alias] && !button) {
      $('groupMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_email_field('groupMailHome', 'tf_email', aliasSelectTemplate);
                show_hide_add_button();
              }).setProperty('id','addMailButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}

function add_contact_autocomplete (el) {
  
  new Autocompleter.Ajax.Json(el, '$path/contact/contact_index.php', {
    postVar: 'pattern',
    postData: {action:'ext_search'},
    ajaxOptions: {'secure': false},
    filterSubset: true,
    minLength: 3,
    selectFirst: true,
    selectMode: 'type-ahead',
    injectChoice: function(choice) {
      var el = new Element('li').set('html', this.markQueryValue(choice.name+'&lt;' + choice.email + '&gt;'));
      el.inputValue = choice.email;
      this.addChoiceEvents(el).inject(this.choices);
    }
  });
  el.addEvent((Browser.Engine.trident || Browser.Engine.webkit) ? 'keydown' : 'keypress', function(e) {
    if (e && e.key && !e.shift) {
      switch (e.key) {
        case 'enter':
          e.preventDefault();
          var input = add_email_field('groupContactMailHome', 'tf_contact'); 
          add_contact_autocomplete(input); 
          show_hide_add_button();
          input.focus();
          break;
        case 'backspace' : 
          if(this.get('inputValue') == '') {
            var previous = this.getParent().getPrevious();
            if(previous.get('class') == 'multiple') {
              previous.getElement('input').focus();
            }
            this.getParent().dispose();
          }
          break;
      }
    }
  })           
}
";
if ($params["ext_element"] != "") {
  require_once("$obminclude/of/of_select.inc");
}

?>
