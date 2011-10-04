function $ (id) {
	return document.getElementById(id);
}

var control = {
  check: function () {
  	$('main-toolbar').value = MainToolbar.getCurrentSet();
  	$('compose-toolbar').value = ComposeToolbar.getCurrentSet();
  	$('addressbook-toolbar').value = AddressBookToolbar.getCurrentSet();
  },
}