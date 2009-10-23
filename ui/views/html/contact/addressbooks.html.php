<table>  
  <tbody>
    <?php foreach($addressbooks as $_id => $_addressbook ) { ?>
    <tr>
    <td class="<?php echo ($_id == $current['addressbook'])? 'current':'' ?>" id="addressbook-<?php echo $_id ?>" >
      <?php /*FIXME*/?>
      <form onsubmit="obm.contact.addressbook.storeAddressBook(this);return false;">
        <ul class="dropDownMenu addressBookMenu" >
          <li>
            <img alt="<?php echo __('Addressbook menu')?>" src="<?php echo self::__icon('dropdown') ?>" />
            <ul>
              <li><a href='<?php echo self::__actionlink('export', array('searchpattern' => 'in:'.$_id))?>'><?php echo __('Export') ?></a></li>
              <?php if ($_addressbook->write == 1) { ?>
              <li><a href='<?php echo self::__actionlink('import', array('addressbook' => $_id))?>'><?php echo __('Import') ?></a></li>
              <?php } ?>  
              <?php if (!$_addressbook->isDefault && $_addressbook->admin) { ?>
              <li><a href="" onclick="$('addressbook-<?php echo $_id ?>').getElement('input').show().getNext().hide();return false;" ><?php echo __('Update') ?></a></li>
              <li><a href="" onclick="obm.contact.addressbook.deleteAddressBook(<?php echo $_id ?>, '<?php echo $this->toJs($_addressbook->name) ?>'); return false;" ><?php echo __('Delete') ?></a></li>
              <?php } ?> 
              <?php if ($_addressbook->admin) { ?>
              <li><a href="<?php echo self::__actionlink('rights_admin', array('entity_id' => $_id))?>"><?php echo __('Right management') ?></a></li>
              <?php } ?> 
              <?php if ($_addressbook->syncable && $_addressbook->synced) { ?>
              <li><a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><?php echo __('Do not synchronize') ?></a></li>
              <?php } elseif ($_addressbook->syncable) { ?>
              <li><a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><?php echo __('Synchronize') ?></a></li>
              <?php } ?> 
            </ul> 
          </li>
        </ul>
        <?php if($_addressbook->synced) { ?>
        <a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><img alt='<?php echo __('Synchronized') ?>' title='<?php echo __('Synchronized') ?>' src="<?php echo self::__icon('sync') ?>"/></a>
        <?php } else { ?>
        <a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><img alt='<?php echo __('Not synchronized') ?>' title='<?php echo __('Not synchronized') ?>' src="<?php echo self::__icon('unsync') ?>"/></a>
        <?php } ?>
        <?php if ($_addressbook->write == 1) { ?>
        <input onblur="$(this).hide();$(this).getNext().show();$(this).set('value', '<?php echo self::toJs($_addressbook->name); ?>')" type="text" style='display: none' name='name' value="<?php echo $_addressbook->name ?>" />
        <?php } ?>
        <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-<?php echo $_id ?>')); return false;"><?php echo $_addressbook->displayname; ?></a> 
        <input type='hidden' name='action' value='storeAddressBook' />
        <input type='hidden' name='id' value='<?php echo $_id ?>' />
        <script type='text/javascript'>
          $('addressbook-<?php echo $_id ?>').store('write', <?php echo $_addressbook->write ?>);
          $('addressbook-<?php echo $_id ?>').store('search', 'in:<?php echo $_id ?> archive:0');
          new Obm.DropDownMenu($('addressbook-<?php echo $_id ?>').getElement('ul'));
        </script>
      </form>
      </td>
    </tr>
    <?php  } ?>
    <tr>
      <td class="<?php echo ('archive' == $current['addressbook'])? 'current':'' ?>" id='addressbook-archive'>
        <div>
          <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-archive')); return false;"><?php echo __('Archive'); ?></a>
          <script type='text/javascript'>
            $('addressbook-archive').set('write', 0);
            $('addressbook-archive').set('search', 'archive:1');
          </script>            
        </div>
      </td>
    </tr>
    <?php /*FIXME*/?>
    <tr style="display: none;">
      <td class="<?php echo ('search' == $current['addressbook'])? 'current':'' ?>" id="addressbook-search">
        <div>
          <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-search')); return false;"><?php echo __('Search results') ?></a>
          <script type='text/javascript'>
            $('addressbook-search').set('write', 0);
            $('addressbook-search').set('search', '');
          </script>                  
        </div>
      </td>
    </tr>
    <tr class="filler">
      <th> </th>
    </tr>
  </tbody>
</table>
