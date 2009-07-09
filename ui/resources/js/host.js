function modify_stat_interval(interval) {
    $('stat_img').getElements('img').each(function(el){
        img = el.getProperty('src');
        regex = new RegExp("-day.png|-week.png|-month.png|-year.png");
        img = img.replace(regex,'-'+interval+'.png');
        el.setProperty('src', img);
        });
}

function change_stat_cat(cat) {
  $('stat_img').getElements('img').each(function(el){
      img = el.getProperty('src');
      regex = new RegExp("(.*-)(.*)(-[a-z]*.png)");
      img = img.replace(regex,'$1'+cat+'$3');
      el.setProperty('src', img);

      });
}

