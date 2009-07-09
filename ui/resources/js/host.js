
//
//function modify_stat_interval(interval) {
//  $('stat_img').getElements('img').each(function(el){
//      img = el.getProperty('src');
//      regex = new RegExp("-day.png|-week.png|-month.png|-year.png");
//      img = img.replace(regex,'-'+interval+'.png');
//      el.setProperty('src', img);
//      });
//}
//
//function change_stat_cat(cat) {
//  $('stat_img').getElements('img').each(function(el){
//      img = el.getProperty('src');
//      regex = new RegExp("(.*-)(.*)(-[a-z]*.png)");
//      img = img.replace(regex,'$1'+cat+'$3');
//      el.setProperty('src', img);
//
//      });
//}
//
obm.initialize.chain(function () {
    //add event on day,week,month,day
    $try(function(){
      $('menuDay').getElements('a').each(function(el){
        el.addEvent('click', function(){
          interval = el.getProperty('title');
          $('stat_img').getElements('img').each(function(el){
            img = el.getProperty('src');
            regex = new RegExp("-day.png|-week.png|-month.png|-year.png");
            img = img.replace(regex,'-'+interval+'.png');
            el.setProperty('src', img);
          });
        });
      });
    });
    //add event on categories
    $try(function(){
      $('stat_menu').getElements('a').each(function(el){
        el.addEvent('click', function(){
          category = el.getProperty('title');
          $('stat_img').getElements('img').each(function(el){
            img = el.getProperty('src');
            regex = new RegExp("(.*-)(.*)(-[a-z]*.png)");
            img = img.replace(regex,'$1'+category+'$3');
            el.setProperty('src', img);
          });
        });
      });
    });

});
