Obm.Menu = new Class({
  
  initialize: function() {
    this.menuItems = new Object();

    var sectionItems = $$('.sectionItem');
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
    elements = $$('.portlet');
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
    
    $('portlets').set('slide', {duration: 'normal',mode: 'horizontal', wrapper: 'portletsPanel'});
    $('mainPanel').set('tween',{property: 'margin-left'});

    $('portletsHandler').set('tween',{property: 'left'});

    this.delta = $('mainPanel').getLeft() - $('portlets').offsetWidth;
    this.width = $('portletsPanel').offsetWidth;

    $('portletsHandler').setStyle('height',($('portletsPanel').offsetHeight - 4) + 'px');
    $('portletsHandler').setStyle('left',(this.width - $('portletsHandler').offsetWidth)  + 'px');
    $('portletsHandler').getFirst().getNext().setStyle('display','none'); ;
    $('portletsHandler').addEvent('click', function(e){
      obm.portlets.toggle();
    });

    if(Cookie.read("portletHidden") == "true") {
      $('portletsHandler').getFirst().setStyle('display','none')
                  .getNext().setStyle('display','inline'); 
      $('portlets').get('slide').hide();
      $('portletsPanel').setStyle('width',this.delta + 'px');
      $('mainPanel').tween(this.delta);
      $('portletsHandler').tween(0);
    }

  
    for(i=0;i<elements.length;i++) {
      el = elements[i];      
      title = el.getFirst();
      content = title.getNext();
      this.portlets[el.id] = new Fx.Slide(content, {duration: 150,wait:false});
      
      img = new Element('img');
      if(Cookie.read(el.id + "Hidden")  == "true") {
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

    if($('portletsHandler').getFirst().getStyle('display') == 'none')
       $('portletsHandler').getFirst().setStyle('display','inline')
                   .getNext().setStyle('display','none');
    else
       $('portletsHandler').getFirst().setStyle('display','none')
                   .getNext().setStyle('display','inline');      

    if($('portlets').get('slide').open) {
      $('mainPanel').tween(this.width + this.delta,this.delta);
      $('portlets').slide('out');
      $('portletsHandler').tween(this.width - $('portletsHandler').offsetWidth,0);
      $('portletsPanel').setStyle('width',this.delta + 'px');
    } else {
      $('mainPanel').tween(this.delta,this.width + this.delta);
      $('portlets').slide('in');
      $('portletsHandler').tween(0,this.width - $('portletsHandler').offsetWidth);
      $('portletsPanel').setStyle('width',this.width + 'px');
    }

    if(Cookie.read("portletHidden")  != "true") {
      Cookie.write("portletHidden", "true",{path: '/'});
    } else {
      Cookie.write("portletHidden", "false",{path: '/'});
    }
  },

  toggleElement: function(item) {
    this.portlets[item].toggle();
    if(Cookie.read(item + "Hidden")  != "true") {
      Cookie.write(item + "Hidden", "true",{path: '/'});
    } else {
      Cookie.write(item + "Hidden", "false",{path: '/'});
    }    
  }

});


/*
 *
 * OBM Popup
 *
 */
Obm.Popup = new Class ({

  show: function(id) {
    $(id).setStyle('display', 'block');
		new StickyWinModal({
			content: $(id),
			modalOptions :{hideOnClick:false}
		});
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
    obm.waitingPanel.dispose();
  }
}

function selectAllCb(container) {
  var container = $(container);
  var chks = container.getElements('input[type=checkbox]');
  chks.each(function (chk) {
    chk.checked = true;
  })
}
function unSelectAllCb(container) {
  container = $(container);
  var chks = container.getElements('input[type=checkbox]');
  chks.each(function (chk) {
    chk.checked = false;
  })          
}

function change_view(url) {
	Cookie.write('calendar_view', url,{path: '/'});
	window.location=url;
}

function last_visit_calendar_view() {
  url = obm.vars.consts.calendarUrl;
  if (Cookie.read('calendar_view') != null) {
    url = '/calendar/'+Cookie.read('calendar_view');
  }
	window.location=url;
}
