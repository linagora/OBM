<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : host_js.inc                                                 //
//     - Desc  : Host javascript functions File                              //
// 2007-02-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($params["ext_widget"] != "") || ($params["ext_widget_text"] != "")) {
  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_company\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      window.close();
      return true;
    }
  } 
  ";
}

$extra_js .= "
  function modify_stat_interval(interval) {
    //$('stat_img').getElements('img').setProperty('src')
      //$('stat_img').getChildren().each(function(el){
      $('stat_img').getElements('img').each(function(el){
          img = el.getProperty('src');
          regex = new RegExp(\"-day.png|-week.png|-month.png|-year.png\");
          img = img.replace(regex,'-'+interval+'.png');
          el.setProperty('src', img);
     });
  }

  function change_stat_cat(cat) {
      $('stat_img').getElements('img').each(function(el){
          img = el.getProperty('src');
          regex = new RegExp(\"(.*-)(.*)(-[a-z]*.png)\");
          img = img.replace(regex,'$1'+cat+'$3');
          el.setProperty('src', img);

      }); 
  }

";







//window.addEvent('domready', function()
//{
//        var drop = $('large');
//        var dropFx = drop.effect('background-color', {wait: false}); // wait is needed so that to toggle the effect,
//
//        $$('.item').each(function(item)
//        {
//                item.addEvent('click', function(e)
//                {
//                        drop.removeEvents();
//                        drop.empty();
//                        var a = item.clone();
//                        a.inject(drop);
//                        dropFx.start('7389AE').chain(dropFx.start.pass('ffffff', dropFx));
//
//                });
//
//        });
//});



</script>
