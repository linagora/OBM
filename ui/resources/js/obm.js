var obm = Obm = {};
obm.vars = new Object();
obm.vars.labels = new Object();
obm.vars.images = new Object();
obm.vars.regexp = new Object();
obm.vars.consts = new Object();
obm.vars.conf = new Object();
obm.initialize = new Chain();
obm.resize = new Chain();

Obm.Menu = new Class({
  
  initialize: function() {
    this.menuItems = new Object();

    var sectionItems = $ES('.sectionItem');
    for(i=0;i<sectionItems.length;i++) {
      var item = sectionItems[i];
      item.addEvent('mouseover', function(e){
        this.addClass('hover');
      });  
      item.addEvent('mouseout', function(e){
        this.removeClass('hover');
      });        
    }

  },  

  addItem: function(item) {
    var slide = new Fx.Slide(item +'-items', {duration: 150,onComplete:this.menuListBoxFix});
    var sectionItem = $(item +'-items-wrapper');
    var sectionBlock = $(item);
    sectionItem.style.top = sectionBlock.getTop() + sectionBlock.offsetHeight + 'px';
    sectionItem.style.left = sectionBlock.getLeft() + 'px';

    slide.hide();
    sectionItem.style.display = 'block';
    var chain = new Chain();
    slide.boxFix = this.menuListBoxFix.bind(slide);
    slide.xHide = function () {
      this.hide();
      this.boxFix(this.element);
    }
    slide.hiddingTimer = new HideTimer(slide.element,{fn:slide.xHide.bind(slide),elems:[sectionBlock]});

    this.menuItems[item] = slide;
    sectionBlock.addEvent('click', function(e){
      obm.menu.toggle(this.id)
    });   
    //sectionBlock.addEvent('mouseover', function(e){
    //   this.addClass('hover');
    //});  
    //sectionBlock.addEvent('mouseout', function(e){
    //    this.removeClass('hover');
    //});      
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
  },
  
  menuListBoxFix: function(element) {
    if (this.wrapper['offset'+this.layout.capitalize()] > 0) {
      overListBoxFix(element,'block');
    } else {
      overListBoxFix(element,'none');
    }
  }
});


Obm.Portlets = new Class({
  
  initialize: function() {
    if(!$('portlets') )
      return false;
    elements = $ES('.portlet',this.panel);
    if (elements.length == 0) {
      $('portletsPanel').setStyle('display','none');
      $('mainPanel').setStyle('margin-left','0');
      return false;
    }
    img = new Element('img');
    img.src = obm.vars.images.minus;
    this.close = img.src;
    img.src = obm.vars.images.plus;
    this.open = img.src; 
    
    // 
    this.portlets = new Object();
    
    this.sidebar = new Fx.Style('portlets', 'width', {duration: 250});    
    this.sidebar.element.setStyle('overflow', 'hidden');
    this.main = new Fx.Style('mainPanel', 'margin-left',{duration: 250});
    this.handle = new Fx.Style('portletsHandler', 'left',{duration: 250});
    this.delta = this.main.element.getLeft() - this.sidebar.element.offsetWidth;
    this.panel = $('portletsPanel');
    this.width = this.panel.offsetWidth;
    this.handler = $('portletsHandler');
    this.handler.setStyle('height',(this.panel.offsetHeight - 4) + 'px');
    this.handler.setStyle('left',(this.width - this.handler.offsetWidth)  + 'px');
    this.handler.getFirst().getNext().setStyle('display','none'); ;
    this.handler.addEvent('click', function(e){
      obm.portlets.toggle();
    });

    if(Cookie.get("portletHidden") == "true") {
      this.handler.getFirst().setStyle('display','none')
                  .getNext().setStyle('display','inline'); 
      this.sidebar.set(0);
      this.panel.setStyle('width',this.delta + 'px');
      this.main.set(this.delta);
      this.handle.set(0);
    }

  
    for(i=0;i<elements.length;i++) {
      el = elements[i];      
      title = el.getFirst();
      content = title.getNext();
      this.portlets[el.id] = new Fx.Slide(content, {duration: 150,wait:false});
      
      img = new Element('img');
      if(Cookie.get(el.id + "Hidden")  == "true") {
        this.portlets[el.id].hide();
        img.src = this.open;
      } else {
        img.src = this.close;
      }
      title.insertBefore(img,title.firstChild);
      img.className = 'handle';
      img = $(img);
      img.addEvent('click', function(e){
        if(this.getAttribute("src") == obm.portlets.close) 
          this.setAttribute("src", obm.portlets.open);
        else 
          this.setAttribute("src", obm.portlets.close);
        obm.portlets.toggleElement(this.parentNode.parentNode.id);
      });
    }

  },

  toggle: function() {
    
    if(this.handler.getFirst().getStyle('display') == 'none')
       this.handler.getFirst().setStyle('display','inline')
                   .getNext().setStyle('display','none');
    else
       this.handler.getFirst().setStyle('display','none')
                   .getNext().setStyle('display','inline');      

    if(this.main.element.getLeft() > this.width) {
      this.main.custom(this.width + this.delta,this.delta );
      this.sidebar.custom(this.width,0);
      this.handle.custom(this.width - this.handler.offsetWidth,0);
      this.panel.setStyle('width',this.delta + 'px');
    } else {
      this.main.custom(this.delta, this.width + this.delta);
      this.sidebar.custom(0,this.width);
      this.handle.custom(0,this.width - this.handler.offsetWidth);
      this.panel.setStyle('width',this.width + 'px');
    }

    if(Cookie.get("portletHidden")  != "true") {
      Cookie.set("portletHidden", "true",{path: '/'});
    } else {
      Cookie.set("portletHidden", "false",{path: '/'});
    }
  },

  toggleElement: function(item) {
    this.portlets[item].toggle();
    if(Cookie.get(item + "Hidden")  != "true") {
      Cookie.set(item + "Hidden", "true",{path: '/'});
    } else {
      Cookie.set(item + "Hidden", "false",{path: '/'});
    }    
  }

});

function popup(url,name,height,width) {
  if(!width)
    width = obm.vars.consts.popupWidth;
  if(!height)
    height = obm.vars.consts.popupHeight;
  window.open(url,name,'height='+height+',width='+width+',scrollbars=yes,menubar=yes');
  return false;
}

function showOkMessage(message) {
  showMessage('ok',message);
}

function showErrorMessage(message) {
  showMessage('error',message);
}

function showMessage(klass, message) {
  var content = $('ajaxMessage');
  content.setStyle('display','block');
  new Element('p').addClass('message')
                  .addClass(klass)
                  .appendText(message)
                  .injectInside(content);
  setTimeout(function () {content.innerHTML = ''; content.setStyle('display','none');}, 1500);
}

function overListBoxFix(overObject, forceDisplay) {
  
  if (navigator.userAgent.toLowerCase().indexOf("msie") == -1)
    return;
  
  try {
    if (!$('listBoxHider')) {
      
      new Element("iframe").setProperty('id','listBoxHider')
        .setProperty("src", "javascript:false;")
        .setProperty("scrolling", "no")
        .setProperty ("frameborder", "0")
        .injectInside($(document.body));
    }
    overObject = $(overObject);
    if(overObject.getStyle("zIndex") == 0) {
      overObject.setStyle("zIndex","1000");
    }
    $('listBoxHider').setStyles({
      position : "absolute",
      width : overObject.offsetWidth ,
      height : overObject.offsetHeight,
      top : overObject.getTop(),
      left : overObject.getLeft(),
      zIndex : overObject.getStyle("zIndex") - 1,
      visibility : overObject.getStyle("visibility") == "hidden" ? "hidden" : "visible",
      display : (forceDisplay) ? forceDisplay : overObject.getStyle("display") 
    });
  } catch (ee) {
    alert(ee);
  }

}

function showWaitingPanel(el) {
  return false; 
  el = $(el);
  obm.waitingPanel = new Element('div')
                      .setStyles({
                          'backgroundColor' : 'gray',
                          'opacity' : '0.5',
                          'position' : 'absolute',
                          'left' : el.getLeft() + 'px',
                          'top' : el.getTop() + 'px',
                          'height' : el.offsetHeight + 'px',
                          'width' : el.offsetWidth + 'px'})
                        .injectInside(document.body)
                        .adopt(new Element('img').setProperty('src',''));
}

function discardWaitingPanel() {;
  if(obm.waitingPanel) {
    obm.waitingPanel.remove();
  }
}
