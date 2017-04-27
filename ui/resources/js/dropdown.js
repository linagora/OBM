
Obm.DropDownMenu= new Class({
	Implements:Options,
	
	options:{
		clickToOpen:true,	//if set to true,  must click to open submenues
		openDelay:10,	//if hover mode, duration the mouse must stay on target before submenu is opened. if exits before delay expires, timer is cleared 
		closeDelay:350,	//delay before the submenu close when mouse exits. If mouse enter the submenu again before timer expires, it's cleared
		link:'cancel',
		mode:'horizontal', //if set to horizontal, the top level menu will be displayed horizontally. If set to vertical, it will be displayed vertically. If it does not match any of those two words, 'horizontal' will be used.
		positioning: 'absolute' // or 'fixed'
	},

	initialize: function(menu,options){
		this.menu = menu;
		this.setOptions(options);
		if(this.options.mode != 'horizontal' && this.options.mode != 'vertical'){
			this.options.mode = 'horizontal';
		}
    this.build();
	},
	
	build:function(){
		this.menu = $(this.menu);
		//start setup
		this.menu.setStyles({
			overflow:'hidden',
      zIndex: 1,
			height:0
		});
		//we call the createSubmenu function on the main UL, which is a recursive function
		this.createSubmenu(this.menu);
		//the LIs must be floated to be displayed horisotally
		if(this.options.mode=='horizontal'){
			this.menu.getChildren('li').setStyles({
				'float':'left',
				display:'block',
				top:0
			});
		
			//We create an extar LI which role will be to clear the floats of the others
			var clear = new Element('li',{
				html:"&nbsp;",
				styles:{
					clear:'left',
					display:(Browser.Engine.trident?'inline':'block'), //took me forever to find that fix
					position:'relative',
					top:0,
					height:0,
					width:0,
					fontSize:0,
					lineHeight:0,
					padding:0
				}
			}).inject(this.menu);
		}else{
			this.menu.getChildren('li').setStyles({
				display:'block',
				top:0
			});
		}
		this.menu.setStyles({
			height:'auto',
			overflow:'visible',
			display:''
		});
		//hack for IE, again
		//this.menu.getElements('a').setStyle('display',(Browser.Engine.trident?'inline-block':'block'));
	},
	
	createSubmenu:function(ul){
		//we collect all the LI of the ul
		var LIs = ul.getChildren('li');
		//loop through the LIs
		LIs.each(function(li){
			li.setStyles({
				position:'relative',
				display:'block',
				zIndex:1
			});
			var innerUl = li.getFirst('ul');
			//if the current LI contains a UL
			if($defined(innerUl)){
				ul.getElements('ul').setStyle('display','none');
				//if the current UL is the main one, that means we are still in the top row, and the submenu must drop down
				if(ul == this.menu && this.options.mode == 'horizontal'){
					li.addClass('submenu-down');
					var x = 0;
					var y = li.getSize().y;
					this.options.link='cancel';
				//if the current UL is not the main one, the sub menu must pop from the side
				}else{
					li.addClass('submenu-left');
					var x = li.getSize().x-1*li.getStyle('border-left-width').toInt();
					var y = -li.getStyle('border-bottom-width').toInt();
					this.options.link='chain';
				}
				innerUl.setStyles({
					position: this.options.positioning,
					top:y,
					opacity:0
				});
				ul.getElements('ul').setStyle('display','block');
                                innerUl.setStyles({
                                });
				//we call the createsubmenu function again, on the new UL
				this.createSubmenu(innerUl);
				//apply events to make the submenu appears when hovering the LI
				if(this.options.clickToOpen){
					li.addEvent('mouseenter',function(){
							$clear(li.retrieve('closeDelay'));
						}.bind(this)
					);
					li.addEvent('click',function(e){
                                                var target = $(e.target);
                                                if(target.get('tag') == 'a') {
                                                  target.addEvent('click', function(e) {e.stop()})
                                                  $clear(li.retrieve('closeDelay'));
                                                  this.hideChildList(li);
                                                } else {
                                                  e.stop();
                                                  $clear(li.retrieve('closeDelay'));
                                                  this.showChildList(li, e);
                                                }
					}.bind(this));
				}else{
					li.addEvent('mouseenter',function(){
						$clear(li.retrieve('closeDelay'));
						li.store('openDelay',this.showChildList.delay(this.options.openDelay,this,li));
					}.bind(this));
				}
				li.addEvent('mouseleave', function(){
					$clear(li.retrieve('openDelay'));
					li.store('closeDelay',this.hideChildList.delay(this.options.closeDelay,this,li));
				}.bind(this));
			}
		},this);
	},
	
	//display submenu
	showChildList:function(li, event){
		var dropdownMenuStyles = {
			opacity : 1,
			display :'block'
		};

        if (this.options.positioning === 'fixed') {
            dropdownMenuStyles.left = event.event.pageX;
            dropdownMenuStyles.top = event.event.pageY;
        }

		this.menu.setStyle('z-index', 1000);
		li.setStyle('z-index', 1000);
		li.getFirst('ul').setStyles(dropdownMenuStyles);
		li.getFirst('ul').getChildren('li').setStyles({'opacity' : 1, 'display' :'block', 'z-index' : 1000});
	},
	
	//hide the menu
	hideChildList:function(li){
          try {
            this.menu.setStyle('z-index', 1);
            li.setStyle('z-index', 1);
            li.getFirst('ul').setStyles({'opacity' : 0, 'display' : 'none'});
            li.getFirst('ul').getChildren('li').setStyles({'opacity' : 0, 'display' : 'none'});        
            li.getFirst('ul').getChildren('li').setStyles({'opacity' : 1, 'display' :'block', 'z-index' : 1});
          } catch (e) {};
	}
});
