<?php echo $this->__template('search') ?>
<table id='contactPanel'>
  <tr>
    <td id='addressBookGrid'>
      <table class='contactPanelHeader'>
        <thead>
          <tr><th><?php echo __('Addressbook')?></th></tr>
          <tr><td class='toolbar'>
            <input type='button' value='' id='addContact' title="<?php echo __('Add contact')?>" onclick='obm.contact.addressbook.addContact();'/>
            <input type='button' value='' id='addAddressBook' title="<?php echo __('Add addressbook')?>" onclick="obm.popup.show('addressbookForm');"/>
          </td></tr>
        </thead>           
      </table>
      <div  id='addressBookContainer' class='contactPanelContainer'>
        <?php echo $this->__template($template['addressbooks'], 'addressbooks'); ?>
      </div>
      <form id='addressbookForm' class='obmPopup' onsubmit="obm.popup.hide('addressbookForm');obm.contact.addressbook.storeAddressBook(this); return false;" style='display:none'>
        <h1><? echo __('What would you like to name this addressbook ?') ?></h1>
        <fieldset>
          <input type='text' name='name' value='' />
        </fieldset>
        <fieldset class='buttons'>
          <input type='hidden' name='action' value='storeAddressBook' />
          <input type='submit' value='<?php echo __('Validate') ?>' />
          <input type='button' value='<?php echo __('Close') ?>' onclick="obm.popup.hide('addressbookForm');$(this.form.name).set('value','');"/>
        </fieldset>
      </form>
    </td>
    <td id='dataGrid' class='<?php echo (empty($template['card'])?'expanded':'shrinked')?>'>
      <table class='contactPanelHeader'>
        <thead>
          <tr>
            <?php $_size = floor(100/count($fields)).'%'; ?>
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
        <?php echo $this->__template($template['contacts'], 'contacts'); ?>
      </div>
    </td>
    <td id='informationGrid'>
        <?php echo $this->__template($template['card']); ?>
    </td>
  </tr>
</table>
<script type='text/javascript'>
  obm.initialize.chain(function () {
    obm.contact.addressbook = new Obm.Contact.AddressBook('addressbook-<?php echo $addressbooks->getMyContacts()->id ?>');
    //FIXME No js here should coded on server side;
    <?php if(isset($template['card'])) { ?>
      //obm.contact.addressbook.showContact();
      //OverText.update();
    //obm.contact.addressbook.hideContact();
    <?php } ?>    
  });
</script>
