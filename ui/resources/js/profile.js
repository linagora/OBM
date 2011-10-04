Obm.Profile = {};
Obm.Profile.Autocompleter = {};

Obm.Profile.Autocompleter = new Class ({
  Extends: Autocompleter.Local,

  /**
   *  
   */
  initialize: function(element, tokens, options) {
    this.datas = tokens;
    this.hiddenElement = $(element);
    visibleElement = this.hiddenElement.clone().set('id','').set('name','dummy'+ this.hiddenElement.get('name'));
    visibleElement.inject(this.hiddenElement, 'before');
    this.hiddenElement.setStyle('display','none')
    this.setOptions({overflow: true, selectMode: 'type-ahead', selectFirst: true, autoTrim: false, forceSelect: true});
    this.parent(visibleElement, tokens.getValues(), options);
    this.addEvent('selection', this.updateValue);
    this.addEvent('show', this.updateValue);
    this.addEvent('hide', this.updateValue);
    this.isSelected = false;
  },  
  /**
   * build - Initialize DOM
   *
   * Builds the html structure for choices and appends the events to the element.
   * Override this function to modify the html generation.
   */
  build: function() {
     if ($(this.options.customChoices)) {
       this.choices = this.options.customChoices;
     } else {
       this.choices = new Element('ul', {
         'class': this.options.className,
         'styles': {
           'zIndex': this.options.zIndex
         }
       }).inject(document.body);
       this.relative = false;
       if (this.options.relative || this.element.getOffsetParent() != document.body) {
         this.choices.inject(this.element, 'after');
         this.relative = this.element.getOffsetParent();
       }
       this.fix = new OverlayFix(this.choices);
     }
     if (!this.options.separator.test(this.options.separatorSplit)) {
       this.options.separatorSplit = this.options.separator;
     }
     this.fx = (!this.options.fxOptions) ? null : new Fx.Tween(this.choices, $merge({
       'property': 'opacity',
       'link': 'cancel',
       'duration': 200
     }, this.options.fxOptions)).addEvent('onStart', Chain.prototype.clearChain).set(0);
     this.element.setProperty('autocomplete', 'off')
       .addEvent((Browser.Engine.trident || Browser.Engine.webkit) ? 'keydown' : 'keypress', this.onCommand.bind(this))
       .addEvent('click', this.onCommand.bind(this, [false]))
       .addEvent('focus', this.toggleFocus.create({bind: this, arguments: true, delay: 100}));
     document.addEvent('click', function(e){
       if (e.target != this.choices && e.target != this.element) this.toggleFocus(false);
     }.bind(this));
   },

  /**
   *  
   */
  updateValue: function() {
    if(this.datas.keyOf(this.opted)) {
      this.hiddenElement.value = this.datas.keyOf(this.opted);
      this.isSelected = true;
    } else {
      this.isSelected = false;
      this.hiddenElement.value = '';
    }
    this.fireEvent('onChange', [this.hiddenElement]);
  }

})

