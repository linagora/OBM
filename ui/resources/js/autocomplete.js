// TODO Lot of code cleanup.
// Some hard coded HTML
// French vars name
// unreached code
// unusefull code
// unadapted function names
// dangerous Element.extend 
// comments
// Implement mono mode

obm.vars.consts.pgup = 33;
obm.vars.consts.pgdown = 34;

obm.AutoComplete = obm.autocomplete = {};
//FIXME Not usefull, replace buy getStyle and setStyle
Element.extend({
  hide: function() {
    this.style.display = 'none';
    return this;
  },

  show: function() {
    this.style.display = '';
    return this;
  },
  isVisible: function() {
    return (this.style.display=='');
  }
});

// used to manage a cache of results
obm.AutoComplete.Cache = new Class({

  initialize: function() {
    this.flush();
    this.EmptyItem = new Element('h2');
  },

  setSize: function(size) {     // defines the total results number that will contain the cache
    while (size > this.getSize()) {
      this.loadingCache.push(this.EmptyItem.clone())
    }
  },

  addElement: function(elt) {   // add a result in the cache
    if (this.loadingCache.length>0)
      this.loadingCache.shift()
    this.realCache.push(elt);
  },

  getElementAt: function(index) { // to get the element at the given index
    return ( index<this.realCache.length ? this.realCache[index] : this.loadingCache[index-this.realCache.length]);
  },

  getIndexOf: function(elt) {   // to get the index of the given element
    var index = this.realCache.indexOf(elt);
    return (index != -1 ? index : this.loadingCache.indexOf(elt) ) ;
  },

  getSize: function() {         // to get the total cache size
    return this.realCache.length + this.loadingCache.length;
  },

  getCacheSize: function() {    // number of elements in cache
    return this.realCache.length;
  },

  flush: function() {           // flush cache
    this.size = 0;
    this.realCache = new Array();
    this.loadingCache = new Array();
  }

});

// used to manage the visible elements of the result list
//FIXME Improve algorythm
//FIXME Refactor
obm.AutoComplete.View = new Class({

  initialize: function(visibleNb) {
    this.visibleNb = visibleNb; // maximum number of visible results
    this.elementNb = 0;         // current number of elements in the list
    this.first = 0;             // index of the first visible element
  },

  setElementNb: function(elementNb) { // used to set the number of elements in the list
    this.elementNb = elementNb;
  },

  getFirst: function() {        // index of the first visible element
    return this.first;
  },

  getLast: function() {         // index of the last visible element
    return Math.min(this.elementNb, this.first+this.visibleNb)-1;
  },

  inView: function(index) {     // is it the index of a visible element, or not ?
    return ( index >= this.first && index <= this.getLast() );
  },

  move: function(offset) {      // used to move the index of visible elements
    this.first += offset;
    if ( this.first > (this.elementNb - this.visibleNb)) {
      this.first = (this.elementNb - this.visibleNb);
    } else if ( this.first < 0 ) {
      this.first = 0;
    }
  }

});


obm.AutoComplete.Search = new Class({

  setOptions: function(options){
    this.options = Object.extend({
      chars: 1,
      results: 8,
      delay: 400,
      mode: 'multiple',
      restriction: null,
      defaultText: 'Search...',
      extension: null
    }, options || {});
  },


  initialize: function(url, selectedBox, inputField, options) {
    this.setOptions(options);

    this.url = url;
    this.inputField = $(inputField);
    this.name = selectedBox;
    this.selectedBox = $(selectedBox);

    this.toClear = false;
   // FIXME OnBlur event to be rethink
   // FIXME Rename input on field.. input can have many means
    this.inputField.addEvent('keyup', this.onTextChange.bindAsEventListener(this))
                   .addEvent('keydown', this.onKeyDown.bindWithEvent(this))
                   .addEvent('keypress', this.onKeyPress.bindWithEvent(this))
                   .addEvent('blur',this.reset.bindAsEventListener(this))
                   .addEvent('focus',function(){this.inputField.value='';this.inputField.removeClass('downlight');}.bindAsEventListener(this));

    var inputCoords = this.inputField.getCoordinates();
    this.resultBox = new Element('div').addClass('autoCompleteResultBox')
                                       .injectInside($(document.body))
                                       .setStyles({
                                         'top':(inputCoords.top + inputCoords.height + 2) + 'px',
                                         'left':inputCoords.left + 'px'
                                       });
    this.infos = new Element('h2').injectInside(this.resultBox)
                                  .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));

    //FIXME Name of those 3 vars
    this.previousBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(-this.options.results);}.bindAsEventListener(this))
                                          .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this))
                                          .setHTML('&lt;&lt;&lt;')
                                          .hide()
                                          .injectInside(this.infos);
    this.text = new Element('span').injectInside(this.infos);
    this.nextBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(this.options.results);}.bindAsEventListener(this))
                                      .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this))
                                      .setHTML('&gt;&gt;&gt;')
                                      .hide()
                                      .injectInside(this.infos);

    this.reset();
  },
  ///////////////////////////////////////////////////////////////////////////
  // keyboard selection management (up, down, enter, esc., page up, page down)
  // return is useless, use e.stop() to prevent event propagation
  //TODO Key pressed should manage up down page up and page down to
  //TODO Tab key mightbe usefull to
  onKeyPress: function(e) {
    if (e.key == 'enter') {
      if (this.resultBox.isVisible()) {
        var currentSel = $E('.highlight', this.resultBox);
        if (currentSel) {
          this.addChoice(currentSel);
        }
        e.stop();
      }
      return false;
    }
    return true;
  },
  //FIXME Switch case? French
  onKeyDown: function(e) {
    if (e.key == 'esc') { // Echap : on reinitialise le champs
      this.inputField.blur();
      this.reset();
      this.inputField.focus();
      return false;

    } else if (e.key == 'up' && this.resultBox.isVisible()) { // Haut : on déplace la sélection vers le haut
      this.jumpTo(-1);
      return false;

    } else if (e.key == 'down' && this.resultBox.isVisible()) { // Bas : on déplace la sélection vers le bas
      this.jumpTo(1);
      return false;

    } else if (e.code == obm.vars.consts.pgup && this.resultBox.isVisible()) { // Page précédente : on charge les résultats précédents
      this.jumpTo(-this.options.results);
      return false;

    } else if (e.code == obm.vars.consts.pgdown && this.resultBox.isVisible()) { // Page suivante : on charge les résultats suivants
      this.jumpTo(this.options.results);
      return false;
    }
    return true;
  },
  ///////////////////////////////////////////////////////////////////////////
  // gestion de la saisie et des requetes de recherche
  onTextChange: function() {
    if (this.fetchDelay) {
      this.fetchDelay = $clear(this.fetchDelay);
    }
    this.fetchDelay = this.newRequest.delay(this.options.delay, this);
  },

  newRequest: function() {
    if (this.inputField.value.clean().length < this.options.chars) {
      this.currentValue = this.inputField.value;
      this.resetResultBox();
    } else if (this.inputField.value != this.currentValue) {
      this.currentValue = this.inputField.value;
      this.resetResultBox();
      new Ajax(this.url, {
        method: 'post',
        postBody: 'pattern='+this.currentValue+'&limit='+(this.options.results*3)+'&restriction='+this.options.restriction+'&extension'+this.options.extension,
        onFailure:this.onFailure.bindAsEventListener(this),
        onComplete:this.onNewRequestSuccess.bindAsEventListener(this)
      }).request();
    }
  },

  cacheRequest: function() { // update the cache when it needs to be (call it after the view moved forward)
    if (this.inputField.value == this.currentValue) {
      if (this.view.getFirst()+this.options.results*2>=this.cache.getSize() && this.cache.getSize()<this.nbTotal) {
        //FIXME French
        var nbElemRestant = this.nbTotal-this.cache.getSize();
        //FIXME French
        var nbRecherche = ((this.options.results*2)>nbElemRestant ? nbElemRestant : this.options.results*2);
        new Ajax(this.url, {
          method: 'post',
          postBody: 'text='+this.currentValue+'&first_row='+this.cache.getSize()+'&limit='+nbRecherche+'&restriction='+this.options.restriction+'&extension'+this.options.extension,
          onFailure:this.onFailure.bindAsEventListener(this),
          onComplete:this.onCacheRequestSuccess.bindAsEventListener(this)
        }).request();
        this.cache.setSize(this.cache.getSize()+nbRecherche);
      }
    }
  },

  onFailure: function(response) {
    showErrorMessage('Fatal server error, please reload');
  },

  onNewRequestSuccess: function(response) {
    if (response.trim() == '' || this.toClear) {
      this.hideResultBox();
      this.resetResultBox();
      this.toClear = false;
    }
    if (response.trim() != '') {
      this.parseResponse(response);
      this.drawView();
      this.updateInfo();
      this.showResultBox();
    }
  },

  onCacheRequestSuccess: function(response) {
    if (response.trim() != '') {
      var oldCacheLength = this.cache.getCacheSize();
      this.parseResponse(response);
      if (this.view.getLast()>=oldCacheLength) {
        this.flushView();
        this.drawView();
        this.showSelection();
      }
      this.updateInfo();
    }
  },

  parseResponse: function(response) {
    try {
      var results = eval(response);
    } catch (e) {
      showErrorMessage('Fatal server error, please reload');
    }  
    results.datas.each(function(data) {
      var res = new Element('div').setProperty('id','item_'+data.id)
                                  .adopt(
                                    new Element('span')
                                      .setProperty('id','item_'+data.id+'_label')
                                      .appendText(data.label)
                                  ).adopt(
                                     new Element('em')
                                       .appendText(data.extra)                                   
                                  );
      this.cache.addElement(res);
      if($type(data.extension)) {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindAsEventListener(this))
           .addEvent('mousedown', function() {this.addChoice(res,data.extension);}.bindAsEventListener(this))
           .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));
      } else {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindAsEventListener(this))
           .addEvent('mousedown', function() {this.addChoice(res);}.bindAsEventListener(this))
           .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));
      }
    }.bind(this));
    this.nbTotal = results.length;
    this.view.setElementNb(this.nbTotal);
  },

  ///////////////////////////////////////////////////////////////////////////
  // selection
  selectElement: function(element) {
    this.hideSelection();
    this.selection = this.cache.getIndexOf(element);
    if (this.selection>=0) {
      element.addClass('highlight');
    }
  },
  
  jumpTo: function(offset) {
    if( this.cache.getSize() <= 0) {
      return false;
    }
    this.hideSelection();
    this.selection += offset;
    if( this.selection < 0) {
      this.selection = this.view.getFirst();
    } else if (this.selection >= this.cache.getSize()) {
      this.selection = this.view.getLast();
    } 
    if(!this.view.inView(this.selection)) {
        this.flushView();
        this.view.move(offset);
        this.drawView();
        this.updateInfo();
    }
    this.showSelection();
    if(offset > 0) {
      this.cacheRequest()
    }
  },

  unselect: function() {
    this.hideSelection();
    this.selection = -1;
  },
  showSelection: function() {
    if (this.selection!=-1 && !this.cache.getElementAt(this.selection).hasClass('highlight')) {
      this.cache.getElementAt(this.selection).addClass('highlight');
    }
  },
  hideSelection: function() {
    if (this.selection!=-1 && this.cache.getElementAt(this.selection).hasClass('highlight')) {
      this.cache.getElementAt(this.selection).removeClass('highlight');
    }
  },
  ///////////////////////////////////////////////////////////////////////////
  // show/hide resultBox functions
  hideResultBox: function() {
    this.resultBox.hide();
  },
  showResultBox: function() {
    var inputCoords = this.inputField.getCoordinates();
    this.resultBox.setStyles({                  
      'top':(inputCoords.top + inputCoords.height + 2) + 'px',
      'left':inputCoords.left + 'px'});
    this.resultBox.show();
  },
  ///////////////////////////////////////////////////////////////////////////
  // view (visible results from the list of results)
  drawView: function() {
    if (this.nbTotal>0) {
      var topLimit = this.view.getLast();
      for (var i=this.view.getFirst(); i<=topLimit; i++) {
        this.cache.getElementAt(i).injectBefore(this.infos);
      }
    }
  },
  flushView: function() {
    this.hideSelection();
    $ES('div', this.resultBox).each(function(elt){ elt.remove();});
  },
  ///////////////////////////////////////////////////////////////////////////
  // results information + navigation btns
  updateInfo: function() {
    this.previousBtn.hide();
    this.nextBtn.hide();
    if (this.nbTotal<=1) {
      this.text.setHTML(this.nbTotal);
    } else {
      this.text.setHTML((this.view.getFirst()+1)+' - '+(this.view.getLast()+1)+' / '+this.nbTotal);
      this.updateNavBtns();
    }
  },
  setInfoText: function(str) { // to manually set the information text
    this.previousBtn.hide();
    this.nextBtn.hide();
    this.text.setHTML(str);
    this.updateNavBtns();
  },
  updateNavBtns: function() {
    if (this.nbTotal>this.view.getLast()+1) {
      this.nextBtn.show();
    }
    if (this.view.getFirst()>0) {
      this.previousBtn.show();
    }
  },
  ///////////////////////////////////////////////////////////////////////////
  // (un)choose elements
  // FIXME NAME
  addChoice: function(element, extension) {
    var item_id = element.getProperty('id');
    var id = item_id.substr(('item_').length,item_id.length);
    var div_id = this.name + id;
    var text = $(item_id+'_label').innerHTML;
    if (!$(div_id)) {
      var result = new Element('div').addClass('elementRow');
      result.setProperties({'id': div_id});
      result.injectInside(this.selectedBox);
      new Element('a').adopt(
                        new Element('img')
                          .setProperty('src',obm.vars.images.del)
                      ).addEvent('mousedown',
                        function() {remove_element(div_id,this.name);}.bind(this)
                      ).injectInside(result);
      result.appendText(text);
      if($type(extension)) {
        result.adopt(extension)
      }
      new Element('input').setProperty('type','hidden')
                          .setProperty('name',this.name+'[]')
                          .setProperty('value',id)
                          .injectInside(result);
    }
  },
  removeChoice: function(element) {
    element.getParent().remove();
    this.inputField.focus();
  },
  ///////////////////////////////////////////////////////////////////////////
  // reset functions
  reset: function() {          // reset input and result boxes
    this.hideResultBox();
    this.inputField.value = this.options.defaultText;
    this.inputField.addClass('downlight')
    this.currentValue = '';
    this.nbTotal = 0;
    this.cache = new obm.AutoComplete.Cache();
    this.view = new obm.AutoComplete.View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  },
  resetResultBox: function() { // reset the result box
    this.hideResultBox();
    this.flushView();
    this.cache = new obm.AutoComplete.Cache();
    this.view = new obm.AutoComplete.View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  }
});
