var obm = Obm = {};
obm.vars = new Object();
obm.vars.labels = new Object();
obm.vars.images = new Object();
obm.vars.regexp = new Object();
obm.vars.consts = new Object();
obm.initialize = new Chain();
obm.resize = new Chain();

Obm.Menu = new Class({
  
  initialize: function() {
    this.menuItems = new Object();
  },  

  addItem: function(item) {
    slide = new Fx.Slide(item +'-items', {duration: 150});
    sectionItem = $(item +'-items-wrapper');
    sectionBlock = $(item);
    sectionItem.style.top = sectionBlock.getTop() + sectionBlock.offsetHeight + 'px';
    sectionItem.style.left = sectionBlock.getLeft() + 'px';

    slide.hide();
    sectionItem.style.display = 'block';

    this.menuItems[item] = slide;
    sectionBlock.addEvent('click', function(e){
      obm.menu.toggle(this.id)
    });   
    sectionBlock.addEvent('mouseover', function(e){
       this.className='hover';
    });  
    sectionBlock.addEvent('mouseout', function(e){
        this.className='';
    });      
  },

  toggle: function(item) {
    sectionItem = $(item +'-items-wrapper');
    sectionBlock = $(item);
    sectionItem.style.top = sectionBlock.getTop() + sectionBlock.offsetHeight + 'px';
    sectionItem.style.left = sectionBlock.getLeft() + 'px';
    this.menuItems[item].toggle();  
    this.hideMenuBut(item);
  },

  hideMenuBut: function(exception) {
    for(var i in this.menuItems) {
      if(exception != i) {
        this.menuItems[i].hide();
      }
    }
  },

  hideMenu: function() {
    for(var i in this.menuItems) {
      this.menuItems[i].hide();
    }
  }

});


Obm.Portlets = new Class({
  
  initialize: function() {
    if(!$('portlets'))
      return false;
    img = document.createElement('img');
    img.src = obm.vars.images.upArrow;
    this.upArrow = img.src;
    img.src = obm.vars.images.downArrow;
    this.downArrow = img.src; 
    img.src = obm.vars.images.leftArrow;
    this.leftArrow = img.src; 
    img.src = obm.vars.images.rightArrow;
    this.rightArrow = img.src; 
    
    // 
    this.portlets = new Object();
    
    this.sidebar = new Fx.Style('portlets', 'width', {duration: 250});    
    $('portlets').setStyle('overflow', 'hidden');
    this.main = new Fx.Style('mainPanel', 'margin-left',{duration: 250});
    this.delta = $('mainPanel').getOffset('left') - $('portlets').offsetWidth;
    this.width = $('portlets').offsetWidth;

    this.handler = $('portletsHandler');
    this.handler.addEvent('click', function(e){
      obm.portlets.toggle();
    });

    if(Cookie.get("portletHidden") == "true") {
      this.handler.setAttribute("src", this.rightArrow);
      this.sidebar.set(0);
      $('portletsPanel').setStyle('width',this.delta + 'px');
      this.main.set(this.delta);
    }

    elements = $ES('.portlet','portletsPanel');
    
    for(i=0;i<elements.length;i++) {
      el = elements[i];      
      title = el.getFirst();
      content = title.getNext();
      this.portlets[el.id] = new Fx.Slide(content, {duration: 150,wait:false});
      
      img = document.createElement('img');
      if(Cookie.get(el.id + "Hidden")  == "true") {
        this.portlets[el.id].hide();
        img.src = this.downArrow;
      } else {
        img.src = this.upArrow;
      }
      title.insertBefore(img,title.firstChild);
      img = $(img);
      img.addEvent('click', function(e){
        if(this.getAttribute("src") == obm.portlets.upArrow) 
          this.setAttribute("src", obm.portlets.downArrow);
        else 
          this.setAttribute("src", obm.portlets.upArrow);
        obm.portlets.toggleElement(this.parentNode.parentNode.id);
      });
    }
  },

  toggle: function() {
    
    if(this.handler.getAttribute("src") == obm.portlets.leftArrow) 
      this.handler.setAttribute("src", obm.portlets.rightArrow);
    else 
     this.handler.setAttribute("src", obm.portlets.leftArrow);

    if($('mainPanel').offsetLeft > this.width) {
      this.main.custom(this.width + this.delta,this.delta );
      this.sidebar.custom(this.width,0);
      $('portletsPanel').setStyle('width',this.delta + 'px');
    } else {
      this.main.custom(this.delta, this.width + this.delta);
      this.sidebar.custom(0,this.width);
      $('portletsPanel').setStyle('width',this.width + 'px');
    }

    if(Cookie.get("portletHidden")  != "true") {
      Cookie.set("portletHidden", "true");
    } else {
      Cookie.set("portletHidden", "false");
    }
  },

  toggleElement: function(item) {
    this.portlets[item].toggle();
    if(Cookie.get(item + "Hidden")  != "true") {
      Cookie.set(item + "Hidden", "true");
    } else {
      Cookie.set(item + "Hidden", "false");
    }    
  }

});

// This will generate a datePicker widget for all element with
// the css class "datePicker".
// The element with the class datePicker must have a name attribute.

function datePickerGenerator() {
  elements = $S('.datePicker');
  elements.each(function(element){
    img = $(document.createElement('img'));
    img.setAttribute("src", obm.vars.images.datePicker);
    img.injectAfter(element);
    img.addEvent('click', function(e){
      displayDatePicker(element.name);
    });
  });
}

function formValidator() {
  $S('[alt=\"require\"]');
}

function popup(url,name,height,width) {
  if(!width)
    width = obm.vars.consts.popupWidth;
  if(!height)
    height = obm.vars.consts.popupHeight;
  window.open(url,name,'height='+height+',width='+width+',scrollbars=yes');
  return false;
}

