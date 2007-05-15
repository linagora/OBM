// TODO Lot of code cleanup.
// Some hard coded HTML
// French vars name
// unreached code
// unusefull code
// unadapted function names
// dangerous Element.extend 
// Style and class names

//FIXME Must be set with the other constants 
AAS_PGUP = 33;
AAS_PGDOWN = 34;

obm.AutoComplete = obm.autocomplete = {};

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

// FIXME Name
var AAS_Cache = new Class({
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

  getEltNb: function() {
    return this.realCache.length;
  },

  flush: function() {
    this.size = 0;
    this.realCache = new Array();
    this.loadingCache = new Array();
  }
});

// FIXME Name
var AAS_View = new Class({
  initialize: function(visibleNb) {
    this.visibleNb = visibleNb;
    this.elementNb = 0;
    this.first = 0;
  },
  setElementNb: function(elementNb) {
    this.elementNb = elementNb;
  },
  getFirstVisible: function() {
    return this.first;
  },
  getLastVisible: function() {
    return Math.min(this.elementNb, this.first+this.visibleNb)-1;
  },
  moveUp: function(nb) {
    this.first = Math.max(this.first-nb,0);
  },
  moveDown: function(nb) {
    this.first = Math.min(this.first+nb,this.elementNb-this.visibleNb);
    if (this.first<0)
      this.first = 0;
  }
});

obm.AutoComplete.Search = new Class({

  setOptions: function(options){
    this.options = Object.extend({
      chars: 1,
      results: 8,
      delay: 400,
      restriction: null
    }, options || {});
  },


initialize: function(url, selectedBox, input, options) {
    this.setOptions(options);

    
    this.url = url;

    this.input = $(input);
    this.name = selectedBox;
    this.selectedBox = $(selectedBox);
    this.prefix = this.input.id;

    this.toClear = false;

    this.input.addEvent('keyup', this.onTextChange.bindAsEventListener(this))
              .addEvent('keydown', this.onKeyDown.bindWithEvent(this))
              .addEvent('keypress', this.onKeyPress.bindWithEvent(this))
              .addEvent('blur',this.reset.bindAsEventListener(this));

// FIXME French
    var inputCoords = this.input.getCoordinates();
    this.resultBox = new Element('div').addClass('autoCompleteResultBox')
                                       .injectInside($(document.body))
                                       .setStyles({
                                         'top':(inputCoords.top + inputCoords.height + 2) + 'px',
                                         'left':inputCoords.left + 'px'
                                       });
                                      
    this.infos = new Element('h2').injectInside(this.resultBox)
                                  .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this));

    this.previousBtn = new Element('span').addEvent('mousedown', function() {this.jumpSelectionPreviousPage();}.bindAsEventListener(this))
                                          .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this))
                                          .setHTML('&lt;&lt;&lt;')
                                          .hide()
                                          .injectInside(this.infos);

    this.text = new Element('span').injectInside(this.infos);

    this.nextBtn = new Element('span').addEvent('mousedown', function() {this.jumpSelectionNextPage();}.bindAsEventListener(this))
                                      .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this))
                                      .setHTML('&gt;&gt;&gt;')
                                      .hide()
                                      .injectInside(this.infos);

    this.reset();
  },
  ///////////////////////////////////////////////////////////////////////////
  // keyboard selection management (up, down, enter, esc., page up, page down)
  onKeyPress: function(e) {
    if (e.key == 'enter') {
      if (this.resultBox.isVisible()) {
        var currentSel = $E('.highlight', this.resultBox);
        if (currentSel) {
          this.addChoice(currentSel);
        }
      }
      return false;
    }
    return true;
  },
  onKeyDown: function(e) {
    if (e.key == 'esc') { // Echap : on reinitialise le champs
      this.input.blur();
      this.reset();
      this.input.focus();
      return false;

    } else if (e.key == 'up' && this.resultBox.isVisible()) { // Haut : on déplace la sélection vers le haut
      this.jumpSelectionPreviousOne();
      return false;

    } else if (e.key == 'down' && this.resultBox.isVisible()) { // Bas : on déplace la sélection vers le bas
      this.jumpSelectionNextOne();
      return false;

    } else if (e.code == AAS_PGUP && this.resultBox.isVisible()) { // Page précédente : on charge les résultats précédents
      this.jumpSelectionPreviousPage();
      return false;

    } else if (e.code == AAS_PGDOWN && this.resultBox.isVisible()) { // Page suivante : on charge les résultats suivants
      this.jumpSelectionNextPage();
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
        postBody: 'pattern='+this.currentValue+'&limit='+(this.options.results*3)+'&restriction='+this.options.restriction,
        onFailure:this.onFailure.bindAsEventListener(this),
        onComplete:this.onNewRequestSuccess.bindAsEventListener(this)
      }).request();
    }
  },
  cacheRequest: function() { // update the cache when it needs to be (call it after the view moved forward)
    if (this.input.value == this.currentValue) {
      if (this.view.getFirstVisible()+this.options.results*2>=this.cache.getSize() && this.cache.getSize()<this.nbTotal) {
        var nbElemRestant = this.nbTotal-this.cache.getSize();
        //FIXME French
        var nbRecherche = ((this.options.results*2)>nbElemRestant ? nbElemRestant : this.options.results*2);
        new Ajax(this.url, {
          method: 'post',
          postBody: 'text='+this.currentValue+'&first_row='+this.cache.getSize()+'&limit='+nbRecherche+'&restriction='+this.options.restriction,
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
      var oldCacheLength = this.cache.getEltNb();
      this.parseResponse(response);
      if (this.view.getLastVisible()>=oldCacheLength) {
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
      res.addEvent('mouseover', function() {this.selectElement(res);}.bindAsEventListener(this))
         .addEvent('mousedown', function() {this.addChoice(res);}.bindAsEventListener(this))
         .addEvent('mouseup', function() {this.input.focus();}.bindAsEventListener(this));
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
  jumpSelectionPreviousOne: function() {
    if (this.selection>0) {
      this.hideSelection();
      this.selection--;
      if (this.selection<this.view.getFirstVisible()) {
        this.flushView();
        this.view.moveUp(1);
        this.drawView();
        this.updateInfo();
      }
      this.showSelection();
    }
  },
  jumpSelectionPreviousPage: function() {
    this.hideSelection();
    if (this.view.getFirstVisible()>0) {
      this.flushView();
      this.view.moveUp(this.options.results);
      this.drawView();
      this.updateInfo();
    }
    this.selection = Math.max(this.selection-this.options.results,this.view.getFirstVisible());
    this.showSelection();
  },
  jumpSelectionNextOne: function() {
    if (this.selection==-1 && this.cache.getSize()>0) {
      this.selection = this.view.getFirstVisible();
      this.showSelection();
    } else if (this.selection+1<this.nbTotal) {
      this.hideSelection();
      this.selection++;
      if (this.selection>this.view.getLastVisible()) {
        this.flushView();
        this.view.moveDown(1);
        this.drawView();
        this.updateInfo();
      }
      this.showSelection();
    }
    this.cacheRequest();
  },
  jumpSelectionNextPage: function() {
    this.hideSelection();
    if (this.view.getLastVisible()+1<this.nbTotal) {
      this.flushView();
      this.view.moveDown(this.options.results);
      this.drawView();
      this.updateInfo();
    }
    this.selection = Math.min(this.selection+this.options.results,this.view.getLastVisible());
    this.showSelection();
    this.cacheRequest();
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
      var topLimit = this.view.getLastVisible();
      for (var i=this.view.getFirstVisible(); i<=topLimit; i++) {
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
      this.text.setHTML((this.view.getFirstVisible()+1)+' - '+(this.view.getLastVisible()+1)+' / '+this.nbTotal);
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
    if (this.nbTotal>this.view.getLastVisible()+1) {
      this.nextBtn.show();
    }
    if (this.view.getFirstVisible()>0) {
      this.previousBtn.show();
    }
  },
  ///////////////////////////////////////////////////////////////////////////
  // (un)choose elements
  // FIXME NAME
  addChoice: function(element) {
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
    this.input.value = '';
    this.currentValue = '';
    this.nbTotal = 0;
    this.cache = new AAS_Cache();
    this.view = new AAS_View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  },
  //FIXME French
  reinitListe: function() {
    this.hideListe();
    this.flushView();
    this.cache = new AAS_Cache();
    this.view = new AAS_View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  }
});
