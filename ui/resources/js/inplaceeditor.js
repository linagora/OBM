obm.InPlaceEditor = obm.inplaceeditor = {};

/////////////////////////////////////////////////////////////////////////////
// Makes any content tag editable "in-place"
//
// Just give a class="editable" attribute to the content tag. Give it also
// a rel="123" attribute to reflect the ID of the record that need to be updated
// in the DB. An Ajax call is made to the provided URL when the user press return.
/////////////////////////////////////////////////////////////////////////////
obm.InPlaceEditor = new Class({

  initialize: function(ajaxUrl) {
    this.ajaxUrl = ajaxUrl;
    $$('.editable').each(function(el) {
      el.addEvent('dblclick',function() {
        var before = el.get('html').trim();
        el.set('html', '');
        var input = new Element('input', {'class':'box', 'value':before});
        input.addEvent('keydown', function(e) { if(e.key == 'enter') { this.fireEvent('blur'); } });
        input.inject(el).select();
        input.addEvent('blur', function() {
          val = input.get('value').trim();
          el.set('text', val).addClass(val != '' ? '' : 'editable-empty');
          
          var url = ajaxUrl + '&id=' + el.get('rel') + '&content=' + el.get('text');
          var request = new Request({
            url:url,
            method:'post',
            onFailure: function() {
              el.set('text', before)
            }
          }).send();
        });
      });
    });
  }
  
});