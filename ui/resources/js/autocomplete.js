// TODO Lot of code cleanup.
// Some hard coded HTML
// unreached code
// unusefull code
// unadapted function names
// comments
// Implement mono mode

if (!obm.vars.consts.tab)
  obm.vars.consts.tab = 9;
if (!obm.vars.consts.pgup)
  obm.vars.consts.pgup = 33;
if (!obm.vars.consts.pgdown)
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

  Implements: Options,   

  options: {
    chars: 1,                        // min number of chars to type before requesting
    results: 8,                      // number of results per page
    delay: 300,                      // delay before the last key pressed and the request
    mode: 'multiple',                // 'mono' or 'multiple'
    locked: false,                   // only in 'mono' mode : lock a choice, and restore it on blur if no other choice selected
    resetable: false,                // only in 'mono' mode : reset field value
    restriction: null,               // obm needs
    filter_entity: null,            // obm needs
    filter_pattern: 'read',            // obm needs
    fieldText: obm.vars.labels.autocompleteField,          // default text displayed when empty field
    extension: null,                  // obm needs
    resultValue: null,		// obm needs
    strict: true, // If true, the result must be selected in the resultbox.
    name: null
  },


  initialize: function(url, selectedBox, inputField, options) {
    this.setOptions(options);
    this.url = url;                    // url used for ajax requests
    this.inputField = $(inputField);   // field used for the input
    this.inputField.set('autocomplete','off');
    this.inputField.setStyle('position', 'relative');
    if(this.options.name == null) {
      this.name = selectedBox;           // the name of the form validation paramater (also used as a prefix for results id)
    } else {
      this.name = this.options.name;
    }
    this.selectedBox = $(selectedBox); // box used to add selected results
    this.isMouseOver = false;          // is mouse over the resultBox ?
    this.requestId = 0;                // current request id
    this.selection = -1;               // currently selected (=highlighted) element

    if (this.options.mode == 'mono') {
      // 'mono' mode functions
      this.validateResultValue = this.setResultValue;
      this.resetFunc = this.monoModeReset;
      if (this.options.strict) {
        this.textChangedFunc = function() { this.unvalidateSelection(); this.resetResultBox(); };
      } else {
        this.textChangedFunc = function() {this.resetResultBox();};
      }
      // 'mono' mode initializations 
      if (this.selectedBox.value != '')
        this.currentValue = this.inputField.value;
      if (this.inputField.value != '') {
      	this.lockedLabel = this.inputField.value;
      	this.lockedKey = this.selectedBox.value;
      }
      this.inputField.addEvent('focus', this.monoModeOnFocus.bindWithEvent(this))
                     .addEvent('keypress', this.monoModeOnKeyPress.bindWithEvent(this));

    } else {
	    if (this.options.resultValue != null) {
	      this.validateResultValue = this.options.resultValue;
	    } else {
	      this.validateResultValue = this.addResultValue;
	    }
      this.resetFunc = this.reset;
      this.textChangedFunc = this.resetResultBox;
    }
    this.inputField.addEvent('keyup', this.onTextChange.bindWithEvent(this))
                   .addEvent('input', this.onTextChange.bindWithEvent(this))
                   .addEvent('paste', this.onTextChange.bindWithEvent(this))
                   .addEvent('keydown', this.onKeyDown.bindWithEvent(this))
                   .addEvent('keypress', this.onKeyPress.bindWithEvent(this))
                   .addEvent('focus', this.onFocus.bindWithEvent(this))
                   .addEvent('blur', this.onBlur.bindWithEvent(this));
    var inputCoords = this.inputField.getCoordinates();
    if(this.options.resetable) {
      new Element('img').set('src', obm.vars.images.del).injectAfter(this.inputField).addEvent('click', function () {
        this.currentValue = this.options.fieldText;
        this.selectedBox.value = '';
        this.inputField.value = this.options.fieldText;
        this.lockedKey = this.options.fieldText;
        this.lockedLabel = this.options.fieldText;
        this.inputField.addClass('downlight');      
      }.bind(this)).setStyle('cursor', 'pointer');
    }
    this.resultBox = new Element('div').addClass('autoCompleteResultBox')
                                       .injectAfter(this.inputField)
                                       .addEvent('mouseenter', function() {this.isMouseOver=true;}.bindWithEvent(this))
                                       .addEvent('mouseleave', function() {this.isMouseOver=false;}.bindWithEvent(this))
                                       .setStyles({
                                         'top': this.inputField.offsetTop + inputCoords.height + 2 + 'px',
                                         'left': this.inputField.offsetLeft + 'px'
                                       });
    this.infos = new Element('h2').injectInside(this.resultBox)
                                  .addEvent('mousedown', function() {this.inputField.focus();}.bindWithEvent(this))
                                  .addEvent('mouseup', function() {this.inputField.focus();}.bindWithEvent(this));

    //FIXME Name of those 3 vars
    this.previousResultsBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(-this.options.results);}.bindWithEvent(this))
                                          .addEvent('mouseup', function() {this.inputField.focus();}.bindWithEvent(this))
                                          .setStyles({'display':'none','cursor':'pointer' })
                                          .injectInside(this.infos)
                                          .set('html','&lt;&lt;&lt;');
    this.infoText = new Element('span').injectInside(this.infos);
    this.nextResultsBtn = new Element('span').addEvent('mousedown', function() {this.jumpTo(this.options.results);}.bindWithEvent(this))
                                      .addEvent('mouseup', function() {this.inputField.focus();}.bindWithEvent(this))
                                      .setStyles({'display':'none','cursor':'pointer' })
                                      .injectInside(this.infos)
                                      .set('html','&gt;&gt;&gt;');

    this.resetFunc();
  },

  getUrl: function() {
    return this.url;
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
    if (this.isMouseOver) {
      if (Browser.Engine.trident) // FIXME because this.inputField.focus.delay(1,this.inputField) doesn't work on ie
        this.inputField.focus();
      else           // FIXME because this.inputField.focus() doesn't work on ff
        this.inputField.focus.delay(1,this.inputField);
    } else {
      this.resetFunc();
    }
  },

  ///////////////////////////////////////////////////////////////////////////
  // keyboard selection management (up, down, enter, esc., page up, page down)
  // return is useless, use e.stop() to prevent event propagation

  // KeyPress event management
  onKeyPress: function(e) {
  	switch (e.key) {
      case 'enter' : // Enter : choose the selection
        if (this.resultBox.getStyle('display')!='none') {
          var currentSel = this.resultBox.getElement('.highlight');
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

      } else if (e.code == obm.vars.consts.tab) {    // Tab key : select first result
        var currentSel = this.resultBox.getElement('.highlight');
        if (currentSel) {
          currentSel.fireEvent('mousedown');
          this.resetFunc();
        } else {
          this.jumpTo(1);
          e.stop();
        }
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
    } else if (this.inputField.value != this.currentValue && this.inputField.value != this.options.defaultText) {
      this.currentValue = this.inputField.value;
      this.textChangedFunc();
      this.requestId++;
      new Request.JSON({
        url : this.getUrl(),
        secure : false,
        onFailure:this.onFailure.bindWithEvent(this),
        onComplete:this.onNewRequestSuccess.bindWithEvent(this,[this.requestId])
      }).post({
        pattern:this.currentValue, 
        limit:(this.options.results*3),
        filter_pattern: this.options.filter_pattern,
        filter_entity: this.options.filter_entity,
        restriction:this.options.restriction,
        extension:this.options.extension});      
    }
  },

  // update the cache when it needs to be (call it after the view moved forward)
  cacheRequest: function() {
    if (this.inputField.value == this.currentValue) {
      if (this.view.getFirst()+this.options.results*2>=this.cache.getSize() && this.cache.getSize()<this.totalNbr) {
        var unknownResultsNbr = this.totalNbr-this.cache.getSize();
        var requestNbr = ((this.options.results*2)>unknownResultsNbr ? unknownResultsNbr : this.options.results*2);
        new Request.JSON({
          url : this.getUrl(),
          secure : false,
          onFailure:this.onFailure.bindWithEvent(this),
          onComplete:this.onCacheRequestSuccess.bindWithEvent(this)
        }).post({
          pattern:this.currentValue,
           first_row: this.cache.getSize(),
            limit:requestNbr,
            filter_pattern: this.options.filter_pattern,
            filter_entity: this.options.filter_entity,
            restriction:this.options.restriction,
            extension:this.options.extension});        
        this.cache.setSize(this.cache.getSize()+requestNbr);
      }
    }
  },

  // when an ajax error occurs (during request)
  onFailure: function(response) {
    showErrorMessage(obm.vars.labels.fatalServerErr);
  },

  // when receiving a success response for a new request
  onNewRequestSuccess: function(response,responseId) {
    this.resetResultBox();
    if (this.requestId == responseId) {
      this.parseResponse(response);
      this.drawView();
      this.updateInfo();
      this.showResultBox();
    }
  },

  // when receiving a success response for a cache update request
  onCacheRequestSuccess: function(response) {
    var oldCacheLength = this.cache.getCacheSize();
    this.parseResponse(response);
    if (this.view.getLast()>=oldCacheLength) {
      this.flushView();
      this.drawView();
      this.showSelection();
    }
    this.updateInfo();
  },

  // parse a response of a request and add results to cache
  parseResponse: function(response) {
    response.datas.each(function(data) {
      var res = new Element('div').setProperty('id','item_'+data.id)
                                  .adopt(
                                    new Element('span')
                                      .setProperty('id','item_'+data.id+'_label')
                                      .appendText(data.label)
                                  ).adopt(
                                     new Element('em')
                                       .appendText(data.extra)                                   
                                  );
      var item_id = res.getProperty('id');
      var div_id = this.name + '-' +item_id.substr(('item_').length,item_id.length);
      if ($(div_id)) { res.addClass("selected"); }
      this.cache.addElement(res);
      if($type(data.extension)) {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindWithEvent(this))
           .addEvent('mousedown', function() {this.validateResultValue(res,data.extension);}.bindWithEvent(this));
      } else {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindWithEvent(this))
           .addEvent('mousedown', function() {this.validateResultValue(res);}.bindWithEvent(this));
      }
    }.bind(this));
    this.totalNbr = response.length;
    this.view.setElementNb(this.totalNbr);
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
    this.isMouseOver = false;
    this.resultBox.setStyle('display', 'none');
  },

  // show the Result Box
  showResultBox: function() {
    var inputCoords = this.inputField.getCoordinates();
    this.resultBox.setStyles({                  
      'top':  this.inputField.offsetTop + inputCoords.height + 2  + 'px',
     'left': this.inputField.offsetLeft + 'px'
      });
    this.resultBox.setStyle('display', '');
  },

  ///////////////////////////////////////////////////////////////////////////
  // view (visible results from the list of results)

  // add viewable results (=results in the view) to the Result Box
  drawView: function() {
    if (this.totalNbr>0) {
      var topLimit = this.view.getLast();
      for (var i=this.view.getFirst(); i<=topLimit; i++) {
        this.cache.getElementAt(i).injectBefore(this.infos);
      }
    }
  },

  // removes previously viewable results from the Result Box
  flushView: function() {
    this.hideSelection();
    this.resultBox.getElements('div').each(function(elt){ elt.dispose();});
  },

  ///////////////////////////////////////////////////////////////////////////
  // results information + navigation btns

  // to automatically update the information text
  updateInfo: function() {
    this.previousResultsBtn.setStyle('display', 'none');
    this.nextResultsBtn.setStyle('display', 'none');
    if (this.totalNbr<=1) {
      this.infoText.set('html',this.totalNbr);
    } else {
      this.infoText.innerHTML = (this.view.getFirst()+1)+' - '+(this.view.getLast()+1)+' / '+this.totalNbr;
      this.updateNavBtns();
    }
  },

  // to manually set the information text
  setInfoText: function(str) {
    this.previousResultsBtn.setStyle('display', 'none');
    this.nextResultsBtn.setStyle('display', 'none');
    this.infoText.set('html',str);
    this.updateNavBtns();
  },

  // display navigations buttons if needed
  updateNavBtns: function() {
    if (this.totalNbr>this.view.getLast()+1) {
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
    var div_id = this.name + '-' + id;
    var text = $(item_id+'_label').innerHTML;
    if (!$(div_id)) {
      element.addClass("selected");
      var result = new Element('div').addClass('elementRow');
      result.setProperties({'id': div_id});
      result.injectInside(this.selectedBox);
      new Element('a').adopt(
                        new Element('img')
                          .setProperty('src',obm.vars.images.del)
                          .setStyles({'cursor':'pointer' })
                      ).addEvent('mousedown',
                        function() {
                          var item = $(item_id);
                          if (item) { item.removeClass("selected"); }
                          remove_element(div_id,this.name);
                        }.bind(this)
                      ).injectInside(result);
      result.appendText(' ' + text);
      if($type(extension) && extension != '') {
        result.adopt(extension);
      }
      new Element('input').setProperty('type','hidden')
                          .setProperty('name',this.name+'[]')
                          .setProperty('value',id)
                          .injectInside(result);
    }
    this.inputField.blur();
    this.resetFunc();
    this.inputField.focus();
    eval(this.options.selectfunction);
  },

  // removes an element from the selectedBox
  // not used (remove_element function used instead)
  removeFromSelectedBox: function(element) {
    element.getParent().dispose();
    this.inputField.focus();
  },

  ///////////////////////////////////////////////////////////////////////////
  // reset functions

  // reset input and result box
  reset: function() {
    if(this.inputField.value == '' || this.options.strict) {
      this.inputField.setProperty('value', this.options.fieldText);
      this.currentValue = this.inputField.value;
      this.inputField.addClass('downlight');
    }
    this.requestId++; // invalidate latest request
    this.totalNbr = 0;
    this.resetResultBox();
  },

  // reset the result box (and so cache, view)
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
    this.inputField.removeClass('downlight');
    var item_id = element.getProperty('id');
    this.selectedBox.value = item_id.substr(('item_').length,item_id.length);
    this.currentValue = $(item_id+'_label').innerHTML;
    this.inputField.value = this.currentValue;
    if (this.options.locked) {
      this.lockedLabel = this.inputField.value;
      this.lockedKey = this.selectedBox.value;
    }
    this.resetResultBox();
    eval(this.options.selectfunction);
  },

  // unvalidate the current validated element
  unvalidateSelection: function() {
    this.selectedBox.value = '';
  },

  // reset input and result box
  monoModeReset: function() {
    if (this.options.locked) {
      this.currentValue = this.lockedLabel;
      this.inputField.value = this.lockedLabel;
      this.selectedBox.value = this.lockedKey;
    }
    if (this.selectedBox.value != '' && this.currentValue == this.inputField.value) {
      this.resetResultBox();
    } else {
      this.unvalidateSelection();
      this.reset();
    }
    if (this.inputField.value == this.options.fieldText) {
      this.inputField.addClass('downlight');
    }
  },

  monoModeOnKeyPress: function(e) {
    switch (e.key) {
      case 'enter' :
        if(this.resultBox.getStyle('display')!='none') e.stop();
        break;
      case 'esc' : 
        this.inputField.blur();
        break;
    }
  },

  monoModeOnFocus: function() {
    this.inputField.value='';
    this.inputField.removeClass('downlight');
  }

});


obm.AutoComplete.ExtSearch = new Class({
  Extends: obm.AutoComplete.Search,

  /*
   * first parameter is a function used to retrieve request url
   * */
  initialize: function(getUrlFunc, selectedBox, inputField, options) {
    this.parent('', selectedBox, inputField, options);
    this.getUrl = getUrlFunc;
  },

  setSelectedBox: function(selectedBox) {
    this.selectedBox = $(selectedBox);
  }
});

obm.AutoComplete.ShareCalendarSearch = new Class({
  Extends: obm.AutoComplete.Search,

  Implements: Options,   

  options: {
    chars: 1,                        // min number of chars to type before requesting
    results: 8,                      // number of results per page
    delay: 400,                      // delay before the last key pressed and the request
    mode: 'mono',                // 'mono' or 'multiple'
    locked: false,                   // only in 'mono' mode : lock a choice, and restore it on blur if no other choice selected
    resetable: false,                // only in 'mono' mode : reset field value
    restriction: null,               // obm needs
    filter_entity: null,            // obm needs
    filter_pattern: 'read',            // obm needs
    fieldText: obm.vars.labels.autocompleteField,          // default text displayed when empty field
    extension: null,                  // obm needs
    resultValue: null,		// obm needs
    name: null,
    noresult: false,
    strict: false
  },
  
  initialize: function(getUrlFunc, selectedBox, inputField, options) {
    this.parent(getUrlFunc, selectedBox, inputField, options);
    this.currentValue = this.options.fieldText;
    this.inputField.value = this.options.fieldText;
  },

 // parse a response of a request and add results to cache
  parseResponse: function(response) {
    var present = false; 
    var email = this.currentValue;
    response.datas.each(function(data) {
      var res = new Element('div').setProperty('id','item_'+data.id)
                                  .adopt(
                                    new Element('span')
                                      .setProperty('id','item_'+data.id+'_label')
                                      .appendText(data.extra)
                                  );
      var item_id = res.getProperty('id');
      var div_id = this.name + '-' +item_id.substr(('item_').length,item_id.length);
      if ($(div_id)) { res.addClass("selected"); }
      this.cache.addElement(res);
      if($type(data.extension)) {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindWithEvent(this))
           .addEvent('mousedown', function() {this.validateResultValue(res,data.extension);}.bindWithEvent(this));
      } else {
        res.addEvent('mouseover', function() {this.selectElement(res);}.bindWithEvent(this))
           .addEvent('mousedown', function() {this.validateResultValue(res);}.bindWithEvent(this));
      }
      if(email == data.extra) {
        this.noresult = true;
      }
    }.bind(this));
    this.totalNbr = response.length;
    if(!this.noresult){
      if(check_email(email)){
        var res2 = new Element('div').setProperty('id','item_ext')
                                    .adopt(
                                        new Element('span')
                                          .setProperty('id','item_ext_label')
                                          .appendText(email)
                                        );
        var item_id = res2.getProperty('id');
        this.cache.addElement(res2);
        res2.addEvent('mouseover', function() {this.selectElement(res2);}.bindWithEvent(this))
           .addEvent('mousedown', function() {this.validateResultValue(res2);}.bindWithEvent(this));
        this.totalNbr+=1;
      }
    }
    this.view.setElementNb(this.totalNbr);
  },

  // send a new request to get first results
  newRequest: function() {
    if (this.inputField.value.clean().length < this.options.chars) {
      this.currentValue = this.inputField.value;
      this.textChangedFunc();
    } else if (this.inputField.value != this.currentValue && this.inputField.value != this.options.defaultText) {
      this.currentValue = this.inputField.value;
      /*
      if(this.currentValue == '*'){
        var value = 'email:('+this.currentValue+')';
      } else {
        var value = 'email:('+this.currentValue+'*)';
      }
      */
      var value = this.currentValue;
      this.textChangedFunc();
      this.requestId++;
      new Request.JSON({
        url : this.getUrl(),
        secure : false,
        onFailure:this.onFailure.bindWithEvent(this),
        onComplete:this.onNewRequestSuccess.bindWithEvent(this,[this.requestId])
      }).post({
        pattern:value, 
        limit:(this.options.results*3),
        filter_pattern: this.options.filter_pattern,
        filter_entity: this.options.filter_entity,
        restriction:this.options.restriction,
        extension:this.options.extension});
    }
  }
});

