<?php include($this->__template('search')) ?>
<table id='contactPanel'>
  <tr>
    <td id='addressBookGrid'>
      <table class='contactPanelHeader'>
        <thead>
          <tr><th><?php echo __('Addressbook')?></th></tr>
          <tr><td class='toolbar'></td></tr>
        </thead>           
      </table>
      <div  id='addressBookContainer' class='contactPanelContainer'>
        <?php include($this->__template('addressbooks')); ?>
      </div>
      <form class='addressbookForm' onsubmit="obm.contact.addressbook.storeAddressBook(this); $(this).hide();return false;" style='display:none'>
        <input type='text' name='name' value='' />
        <input type='hidden' name='action' value='storeAddressBook' />
      </form>
      <input type='button' value='+' id='addAddressBook' onclick="$(this).getPrevious().toggle().name.set('value','');"/>
    </td>
    <td id='dataGrid'>
      <table class='contactPanelHeader'>
        <thead>
          <tr>
            <?php foreach($fields as $_fieldname => $_metadata)  { ?>
            <?php if($_metadata['status'] == 2) { ?>
            <th><?php echo $GLOBALS['fieldnames'][$_fieldname] ?></th>
            <?php } else { ?>
            <td><?php echo $GLOBALS['fieldnames'][$_fieldname] ?></td>
            <?php }?>
            <?php } ?>
            <td class='filler'> </td>
          </tr>
          <tr>
            <th class='search'>
              <form name='contactfilter' onsubmit='obm.contact.addressbook.filterContact(this); return false;' id='contactFilterForm' >    
                <input type='hidden' name='action' value='filterContact'  />
                <input size='15' type='text' value='' name='contactfilter' id='contactfilter' />
              </form>
            </th>
            <td colspan='<?php echo count($fields) ?>'>
            </td>
          </tr>
        </thead>
      </table>
      <div id='dataContainer' class='contactPanelContainer'>
        <?php include($this->__template('contacts')); ?>
      </div>
      <input type='button' value='+' id='addContact' onclick='obm.contact.addressbook.addContact();'/>
    </td>
    <td id='informationGrid'>
        <?php if(isset($contact)) include($this->__template('card')); ?>
    </td>
  </tr>
</table>
<script type='text/javascript'>
  obm.initialize.chain(function () {
    obm.contact.addressbook = new Obm.Contact.AddressBook();
    <?php if(isset($contact)) { ?>
    //obm.contact.addressbook.hideContact();
    <?php } else { ?>
    //obm.contact.addressbook.showContact();
    <?php } ?>    
  });
</script>
