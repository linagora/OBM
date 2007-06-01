// TODO Lot of code cleanup.
// Some hard coded HTML
// unreached code
// unusefull code
// unadapted function names
// comments
// Implement mono mode

obm.vars.consts.pgup = 33;
obm.vars.consts.pgdown = 34;

obm.AutoComplete = obm.autocomplete = {};

/////////////////////////////////////////////////////////////////////////////
// used to manage a cache of results
obm.AutoComplete.Cache = new Class({

  initialize: function() {
    this.flush();
    this.EmptyItem = new Element('h2');
  },

  // defines the number of results that will contain the cache
  setSize: function(size) {
    while (size > this.getSize()) {
      this.loadingCache.push(this.EmptyItem.clone())
    }
  },

  // add a result in the cache
  addElement: function(elt) {
    if (this.loadingCache.length>0)
      this.loadingCache.shift()
    this.realCache.push(elt);
  },

  // to get the element at the given index
  getElementAt: function(index) {
    return ( index<this.realCache.length ? this.realCache[index] : this.loadingCache[index-this.realCache.length]);
  },

  // to get the index of the given element
  getIndexOf: function(elt) {
    var index = this.realCache.indexOf(elt);
    return (index != -1 ? index : this.loadingCache.indexOf(elt) ) ;
  },

  // to get the total cache size
  getSize: function() {
    return this.realCache.length + this.loadingCache.length;
  },

  // number of elements in cache
  getCacheSize: function() {
    return this.realCache.length;
  },

  // flush cache
  flush: function() {
    this.realCache = new Array();    // array of loaded results
    this.loadingCache = new Array(); // array of unknown results (= results to request)
  }

});


/////////////////////////////////////////////////////////////////////////////
// used to manage the visible elements of the result list
obm.AutoComplete.View = new Class({

  initialize: function(visibleNb) {
    this.visibleNb = visibleNb; // maximum number of visible results
    this.elementNb = 0;         // current number of elements in the list
    this.first = 0;             // index of the first visible element
  },

  // used to set the number of elements in the list
  setElementNb: function(elementNb) {
    this.elementNb = elementNb;
  },

  // index of the first visible element
  getFirst: function() {
    return this.first;
  },

  // index of the last visible element
  getLast: function() {
    return Math.min(this.elementNb, this.first+this.visibleNb)-1;
  },

  // is it the index of a visible element, or not ?
  inView: function(index) {
    return ( index >= this.first && index <= this.getLast() );
  },

  // used to move the index of visible elements
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
      fieldText: 'Search...',
      extension: null
    }, options || {});
  },


  initialize: function(url, selectedBox, inputField, options) {
    this.setOptions(options);

    this.url = url;                    // url used for ajax requests
    this.inputField = $(inputField);   // field used for the input
    this.name = selectedBox;           // the name of the form validation paramater (also used as a prefix for results id)
    this.selectedBox = $(selectedBox); // box used to add selected results
    this.isMouseOver = false;          // is mouse over the resultBox ?
    this.requestId = 0;                // current request id
    this.selection = -1;               // currently selected (=highlighted) element


    if (this.options.mode == 'mono') {
      this.validateResultValue = this.setResultValue;
      this.resetFunc = this.monoModeReset;
      this.textChangedFunc = function() { this.unvalidateSelection(); this.resetResultBox(); };
    } else {
      this.validateResultValue = this.addResultValue;
      this.resetFunc = this.reset;
      this.textChangedFunc = this.resetResultBox;
    }

    this.inputField.addEvent('keyup', this.onTextChange.bindAsEventListener(this))
                   .addEvent('input', this.onTextChange.bindAsEventListener(this))
                   .addEvent('paste', this.onTextChange.bindAsEventListener(this))
                   .addEvent('keydown', this.onKeyDown.bindWithEvent(this))
                   .addEvent('keypress', this.onKeyPress.bindWithEvent(this))
                   .addEvent('focus', this.onFocus.bindAsEventListener(this))
                   .addEvent('blur', this.onBlur.bindAsEventListener(this));

    var inputCoords = this.inputField.getCoordinates();
    this.resultBox = new Element('div').addClass('autoCompleteResultBox')
                                       .injectInside($(document.body))
                                       .addEvent('mouseenter', function() {this.isMouseOver=true;}.bindAsEventListener(this))
                                       .addEvent('mouseleave', function() {this.isMouseOver=false;}.bindAsEventListener(this))
                                       .setStyles({
                                         'top':(inputCoords.top + inputCoords.height + 2) + 'px',
                                         'left':inputCoords.left + 'px'
                                       });
    this.infos = new Element('h2').injectInside(this.resultBox)
                                  .addEvent('mousedown', function() {this.inputField.focus();}.bindAsEventListener(this))
                                  .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));

    //FIXME Name of those 3 vars
    this.previousResultsBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(-this.options.results);}.bindAsEventListener(this))
                                          .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this))
                                          .setHTML('&lt;&lt;&lt;')
                                          .setStyle('display', 'none')
                                          .injectInside(this.infos);
    this.infoText = new Element('span').injectInside(this.infos);
    this.nextResultsBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(this.options.results);}.bindAsEventListener(this))
                                      .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this))
                                      .setHTML('&gt;&gt;&gt;')
                                      .setStyle('display', 'none')
                                      .injectInside(this.infos);

    this.resetFunc();
  },

  ///////////////////////////////////////////////////////////////////////////
  // focus and blur management

  // focus event
  onFocus: function() {
    if (this.inputField.value==this.options.fieldText) {
      this.inputField.value='';
      this.inputField.removeClass('downlight');
    }
  },

  // blur event
  onBlur: function() {
    if (this.isMouseOver)
      this.inputField.focus(); // keep focus if mouse over the resultBox
    else
      this.resetFunc();
  },

  ///////////////////////////////////////////////////////////////////////////
  // keyboard selection management (up, down, enter, esc., page up, page down)
  // return is useless, use e.stop() to prevent event propagation
  //TODO Tab key mightbe usefull to

  // KeyPress event management
  onKeyPress: function(e) {
  	switch (e.key) {
      case 'enter' : // Enter : choose the selection
        if (this.resultBox.getStyle('display')!='none') {
          var currentSel = $E('.highlight', this.resultBox);
          if (currentSel) {
            currentSel.fireEvent('mousedown');
          }
          e.stop();
        }
        break;

      case 'esc' : // Escape : reset the field
        this.inputField.blur();
        this.resetFunc();
        this.inputField.focus();
        break;

    }
  },

  // KeyDown event management (because IE will not fire KeyPress for arrow keys)
  onKeyDown: function(e) {
    if (this.resultBox.getStyle('display')!='none') {

      if (e.key == 'up') {                           // up : moves the selection up
        this.jumpTo(-1);

      } else if (e.key == 'down') {                  // down : moves the selection down
        this.jumpTo(1);

      } else if (e.code == obm.vars.consts.pgup) {   // Page up : view previous results page
        this.jumpTo(-this.options.results);
        e.stop(); // because pgup works like orig. in IE

      } else if (e.code == obm.vars.consts.pgdown) { // Page down : view next results page
        this.jumpTo(this.options.results);
        e.stop(); // because pgdown works like End in IE
      }
    }
  },

  ///////////////////////////////////////////////////////////////////////////
  // search requests, results and cache management

  // When the text in the input field has changed
  onTextChange: function() {
    if (this.fetchDelay) {
      this.fetchDelay = $clear(this.fetchDelay);
    }
    this.fetchDelay = this.newRequest.delay(this.options.delay, this);
  },

  // send a new request to get first results
  newRequest: function() {
    if (this.inputField.value.clean().length < this.options.chars) {
      this.currentValue = this.inputField.value;
      this.textChangedFunc();
    } else if (this.inputField.value != this.currentValue) {
      this.currentValue = this.inputField.value;
      this.textChangedFunc();
      this.requestId++;
      new Ajax(this.url, {
        method: 'post',
        postBody: 'pattern='+this.currentValue+'&limit='+(this.options.results*3)+'&restriction='+this.options.restriction+'&extension='+this.options.extension,
        onFailure:this.onFailure.bindAsEventListener(this),
        onComplete:this.onNewRequestSuccess.bindAsEventListener(this,[this.requestId])
      }).request();
    }
  },

  // update the cache when it needs to be (call it after the view moved forward)
  cacheRequest: function() {
    if (this.inputField.value == this.currentValue) {
      if (this.view.getFirst()+this.options.results*2>=this.cache.getSize() && this.cache.getSize()<this.nbTotal) {
        var unknownResultsNbr = this.nbTotal-this.cache.getSize();
        var requestNbr = ((this.options.results*2)>unknownResultsNbr ? unknownResultsNbr : this.options.results*2);
        new Ajax(this.url, {
          method: 'post',
          postBody: 'text='+this.currentValue+'&first_row='+this.cache.getSize()+'&limit='+requestNbr+'&restriction='+this.options.restriction+'&extension='+this.options.extension,
          onFailure:this.onFailure.bindAsEventListener(this),
          onComplete:this.onCacheRequestSuccess.bindAsEventListener(this)
        }).request();
        this.cache.setSize(this.cache.getSize()+requestNbr);
      }
    }
  },

  // when an ajax error occurs (during request)
  onFailure: function(response) {
    showErrorMessage('Fatal server error, please reload');
  },

  // when receiving a success response for a new request
  onNewRequestSuccess: function(response,responseId) {
    this.resetResultBox();
    if (response.trim() != '' && this.requestId == responseId) {
      this.parseResponse(response);
      this.drawView();
      this.updateInfo();
      this.showResultBox();
    }
  },

  // when receiving a success response for a cache update request
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

  // parse a response of a request and add results to cache
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
           .addEvent('mousedown', function() { this.validateResultValue(res,data.extension)}.bindAsEventListener(this))
           .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));
      } else {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindAsEventListener(this))
           .addEvent('mousedown', function() {this.validateResultValue(res);}.bindAsEventListener(this))
           .addEvent('mouseup', function() {this.inputField.focus();}.bindAsEventListener(this));
      }
    }.bind(this));
    this.nbTotal = results.length;
    this.view.setElementNb(this.nbTotal);
  },

  ///////////////////////////////////////////////////////////////////////////
  // selection

  // select an element
  selectElement: function(element) {
    this.hideSelection();
    this.selection = this.cache.getIndexOf(element);
    if (this.selection>=0) {
      element.addClass('highlight');
    }
  },

  // jump the selection of offset results
  jumpTo: function(offset) {
    if( this.cache.getSize() <= 0) {
      return false;
    }
    this.hideSelection();
    this.selection += offset;
    if( this.selection < 0) {
      this.selection = 0;
    } else if (this.selection >= this.cache.getSize()) {
      this.selection = this.cache.getSize()-1;
    } 
    if(!this.view.inView(this.selection)) {
        this.flushView();
        this.view.move(offset);
        this.drawView();
        this.updateInfo();
    }
    this.showSelection();
    if(offset > 0) {
      this.cacheRequest();
    }
  },

  // clear the selection
  unselect: function() {
    this.hideSelection();
    this.selection = -1;
  },

  // highlight the selection
  showSelection: function() {
    if (this.selection!=-1 && !this.cache.getElementAt(this.selection).hasClass('highlight')) {
      this.cache.getElementAt(this.selection).addClass('highlight');
    }
  },

  // unhighlight (?) the selection
  hideSelection: function() {
    if (this.selection!=-1 && this.cache.getElementAt(this.selection).hasClass('highlight')) {
      this.cache.getElementAt(this.selection).removeClass('highlight');
    }
  },

  ///////////////////////////////////////////////////////////////////////////
  // show/hide resultBox functions

  // hide the Result Box
  hideResultBox: function() {
    this.resultBox.setStyle('display', 'none');
  },

  // show the Result Box
  showResultBox: function() {
    var inputCoords = this.inputField.getCoordinates();
    this.resultBox.setStyles({                  
      'top':(inputCoords.top + inputCoords.height + 2) + 'px',
      'left':inputCoords.left + 'px'});
    this.resultBox.setStyle('display', '');
  },

  ///////////////////////////////////////////////////////////////////////////
  // view (visible results from the list of results)

  // add viewable results (=results in the view) to the Result Box
  drawView: function() {
    if (this.nbTotal>0) {
      var topLimit = this.view.getLast();
      for (var i=this.view.getFirst(); i<=topLimit; i++) {
        this.cache.getElementAt(i).injectBefore(this.infos);
      }
    }
  },

  // removes previously viewable results from the Result Box
  flushView: function() {
    this.hideSelection();
    $ES('div', this.resultBox).each(function(elt){ elt.remove();});
  },

  ///////////////////////////////////////////////////////////////////////////
  // results information + navigation btns

  // to automatically update the information text
  updateInfo: function() {
    this.previousResultsBtn.setStyle('display', 'none');
    this.nextResultsBtn.setStyle('display', 'none');
    if (this.nbTotal<=1) {
      this.infoText.setHTML(this.nbTotal);
    } else {
      this.infoText.setHTML((this.view.getFirst()+1)+' - '+(this.view.getLast()+1)+' / '+this.nbTotal);
      this.updateNavBtns();
    }
  },

  // to manually set the information text
  setInfoText: function(str) {
    this.previousResultsBtn.setStyle('display', 'none');
    this.nextResultsBtn.setStyle('display', 'none');
    this.infoText.setHTML(str);
    this.updateNavBtns();
  },

  // display navigations buttons if needed
  updateNavBtns: function() {
    if (this.nbTotal>this.view.getLast()+1) {
      this.nextResultsBtn.setStyle('display', '');
    }
    if (this.view.getFirst()>0) {
      this.previousResultsBtn.setStyle('display', '');
    }
  },

  ///////////////////////////////////////////////////////////////////////////
  // (un)choose elements

  // add an element to the box containing selected elements (selectedBox)
  addResultValue: function(element, extension) {
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
        result.adopt(extension);
      }
      new Element('input').setProperty('type','hidden')
                          .setProperty('name',this.name+'[]')
                          .setProperty('value',id)
                          .injectInside(result);
    }
  },

  // NOTUSED (remove_element function used instead)
  // removes an element from the selectedBox
  removeFromSelectedBox: function(element) {
    element.getParent().remove();
    this.inputField.focus();
  },

  ///////////////////////////////////////////////////////////////////////////
  // reset functions

  // reset input and result box
  reset: function() {
    this.inputField.value = this.options.fieldText;
    this.inputField.addClass('downlight');
    this.currentValue = '';
    this.nbTotal = 0;
    this.resetResultBox();
  },

  // reset the result box
  resetResultBox: function() {
    this.hideResultBox();
    this.flushView();
    this.cache = new obm.AutoComplete.Cache();
    this.view = new obm.AutoComplete.View(this.options.results);
    this.view.setElementNb(0);
    this.selection = -1;
  },

  ///////////////////////////////////////////////////////////////////////////
  // "mono" mode specific functions

  // validate the current selection
  setResultValue: function(element, extension) {
    var item_id = element.getProperty('id');
    this.selectedBox.value = item_id.substr(('item_').length,item_id.length);
    this.currentValue = $(item_id+'_label').innerHTML;
    this.inputField.value = this.currentValue;
    this.inputField.setStyle('background-color', '#ffffcc');
    this.resetResultBox();
    this.blur();
  },

  // unvalidate the current validated element
  unvalidateSelection: function() {
    this.selectedBox.value = '';
    this.inputField.setStyle('background-color', '#ffffff');
  },

  // reset input and result box
  monoModeReset: function() {
  	this.unvalidateSelection();
  	this.reset();
  }

});
