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

obm.AutoComplete.Cache = new Class({

  initialize: function() {
    this.flush();
    this.EmptyItem = new Element('h2');
  },

  setSize: function(size) {
    while (size > this.getSize()) {
      this.loadingCache.push(this.EmptyItem.clone())
    }
  },

  addElement: function(elt) {
    if (this.loadingCache.length>0)
      this.loadingCache.shift()
    this.realCache.push(elt);
  },

  getElementAt: function(index) {
    return ( index<this.realCache.length ? this.realCache[index] : this.loadingCache[index-this.realCache.length]);
  },

  getIndexOf: function(elt) {
    var index = this.realCache.indexOf(elt);
    return (index != -1 ? index : this.loadingCache.indexOf(elt) ) ;
  },

  getSize: function() {
    return this.realCache.length + this.loadingCache.length;
  },

  getCacheSize: function() {
    return this.realCache.length;
  },

  flush: function() {
    this.size = 0;
    this.realCache = new Array();
    this.loadingCache = new Array();
  }

});

//FIXME Improve algorythm
//FIXME Refactor
obm.AutoComplete.View = new Class({

  initialize: function(visibleNb) {
    this.visibleNb = visibleNb;
    this.elementNb = 0;
    this.first = 0;
  },

  setElementNb: function(elementNb) {
    this.elementNb = elementNb;
  },

  getFirst: function() {
    return this.first;
  },

  getLast: function() {
    return Math.min(this.elementNb, this.first+this.visibleNb)-1;
  },

  inView: function(index) {
    return ( index >= this.first && index <= this.getLast() );
  },
  
  move: function(offset) {
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


initialize: function(url, selectedBox, input, options) {
    this.setOptions(options);

    this.url = url;
    this.input = $(input);
    this.name = selectedBox;
    this.selectedBox = $(selectedBox);

    this.toClear = false;
   // FIXME OnBlur event to be rethink
   // FIXME Rename input on field.. input can have many means
    this.input.addEvent('keyup', this.onTextChange.bindAsEventListener(this))
              .addEvent('keydown', this.onKeyDown.bindWithEvent(this))
              .addEvent('keypress', this.onKeyPress.bindWithEvent(this))
              .addEvent('blur',this.reset.bindAsEventListener(this))
              .addEvent('focus',function(){this.input.value='';this.input.removeClass('downlight');}.bindAsEventListener(this));

    var inputCoords = this.input.getCoordinates();
    this.resultBox = new Element('div').addClass('autoCompleteResultBox')
                                       .injectInside($(document.body))
                                       .setStyles({
                                         'top':(inputCoords.top + inputCoords.height + 2) + 'px',
                                         'left':inputCoords.left + 'px'
                                       });
    this.infos = new Element('h2').injectInside(this.resultBox)
                                  .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this));

    //FIXME Name of those 3 vars                                       
    this.previousBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(-this.options.results);}.bindAsEventListener(this))
                                          .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this))
                                          .setHTML('&lt;&lt;&lt;')
                                          .hide()
                                          .injectInside(this.infos);
    this.text = new Element('span').injectInside(this.infos);
    this.nextBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(this.options.results);}.bindAsEventListener(this))
                                      .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this))
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
      this.input.blur();
      this.reset();
      this.input.focus();
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
    if (this.input.value.clean().length < this.options.chars) {
      this.currentValue = this.input.value;
      this.reinitListe();
    } else if (this.input.value != this.currentValue) {
      this.currentValue = this.input.value;
      this.reinitListe();
      new Ajax(this.url, {
        method: 'post',
        postBody: 'pattern='+this.currentValue+'&limit='+(this.options.results*3)+'&restriction='+this.options.restriction+'&extension'+this.options.extension,
        onFailure:this.onFailure.bindAsEventListener(this),
        onComplete:this.onNewRequestSuccess.bindAsEventListener(this)
      }).request();
    }
  },

  cacheRequest: function() { // update the cache when it needs to be (call it after the view moved forward)
    if (this.input.value == this.currentValue) {
      if (this.view.getFirst()+this.options.results*2>=this.cache.getSize() && this.cache.getSize()<this.nbTotal) {
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
      this.hideListe();
      this.reinitListe();
      this.toClear = false;
    }
    if (response.trim() != '') {
      this.parseResponse(response);
      this.drawView();
      this.updateInfo();
      this.showListe();
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
           .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this));
      } else {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindAsEventListener(this))
           .addEvent('mousedown', function() {this.addChoice(res);}.bindAsEventListener(this))
           .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this));
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
  // liste element functions
  //FIXME French
  hideListe: function() {
    this.resultBox.hide();
  },
  showListe: function() {
    var inputCoords = this.input.getCoordinates();
    this.resultBox.setStyles({                  
      'top':(inputCoords.top + inputCoords.height + 2) + 'px',
      'left':inputCoords.left + 'px'});
    this.resultBox.show();
  },
  ///////////////////////////////////////////////////////////////////////////
  // view (fenêtre des résultats visibles)
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
    this.input.focus();
  },
  ///////////////////////////////////////////////////////////////////////////
  // reinit functions
  reset: function() {
    this.hideListe();
    this.input.value = this.options.defaultText;
    this.input.addClass('downlight')
    this.currentValue = '';
    this.nbTotal = 0;
    this.cache = new obm.AutoComplete.Cache();
    this.view = new obm.AutoComplete.View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  },
  //FIXME French
  reinitListe: function() {
    this.hideListe();
    this.flushView();
    this.cache = new obm.AutoComplete.Cache();
    this.view = new obm.AutoComplete.View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  }
});
