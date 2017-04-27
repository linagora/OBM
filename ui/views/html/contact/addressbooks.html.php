<table>  
  <tbody>
    <?php foreach($addressbooks as $_id => $_addressbook ) { ?>
    <?php if(!$_addressbook->read) continue; ?>
    <tr>
    <td class="<?php echo ($_id == $current['addressbook'])? 'current':'' ?>" id="addressbook-<?php echo $_id ?>" >
      <form onsubmit="obm.contact.addressbook.storeAddressBook(this);return false;">
        <ul class="dropDownMenu addressBookMenu" >
          <li>
            <img alt="<?php echo __('Addressbook menu')?>" src="<?php echo self::__icon('dropdown') ?>" />
            <ul>
              <li><a href='<?php echo self::__actionlink('save', array('searchpattern' => 'addressbookId:'.$_id))?>'><?php echo __('Save') ?></a></li>
              <?php if ($_addressbook->write == 1) { ?>
              <li><a href='<?php echo self::__actionlink('import', array('addressbook' => $_id))?>'><?php echo __('Import') ?></a></li>
              <?php } ?>  
              <li><a href='<?php echo self::__actionlink('export', array('searchpattern' => 'addressbookId:'.$_id))?>'><?php echo __('Export') ?></a></li>
              <?php if (!$_addressbook->isDefault && $_addressbook->admin) { ?>
              <li><a href="" onclick="$('addressbook-<?php echo $_id ?>').getElement('input').show().getNext().hide();return false;" ><?php echo __('Rename') ?></a></li>
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
        <?php if($_addressbook->synced && $_addressbook->syncable) { ?>
        <a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><img alt='<?php echo __('Synchronized') ?>' title='<?php echo __('Synchronized') ?>' src="<?php echo self::__icon('sync') ?>"/></a>
        <?php } elseif(!$_addressbook->synced && $_addressbook->syncable) { ?>
        <a href="" onclick="obm.contact.addressbook.setSubscription(<?php echo $_id ?>);return false;" ><img alt='<?php echo __('Not synchronized') ?>' title='<?php echo __('Not synchronized') ?>' src="<?php echo self::__icon('unsync') ?>"/></a>
        <?php } elseif($_addressbook->synced && !$_addressbook->syncable) { ?>
        <img alt='<?php echo __('Synchronized') ?>' title='<?php echo __('Synchronized') ?>' src="<?php echo self::__icon('sync_lock') ?>"/>
        <?php } elseif(!$_addressbook->synced && !$_addressbook->syncable) { ?>
        <img alt='<?php echo __('Not synchronized') ?>' title='<?php echo __('Not synchronized') ?>' src="<?php echo self::__icon('unsync_lock') ?>"/>
        <?php } ?>
        <?php if ($_addressbook->write == 1) { ?>
        <input onblur="$(this).hide();$(this).getNext().show();$(this).set('value', '<?php echo self::toJs($_addressbook->name); ?>')" type="text" style='display: none' name='name' value="<?php echo $_addressbook->name ?>" />
        <?php } ?>
        <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-<?php echo $_id ?>')); return false;"><?php echo $_addressbook->displayname; ?></a> 
        <input type='hidden' name='action' value='storeAddressBook' />
        <input type='hidden' name='addressbook_id' value='<?php echo $_id ?>' />
        <script type='text/javascript'>
          $('addressbook-<?php echo $_id ?>').store('write', <?php echo $_addressbook->write ?>);
          $('addressbook-<?php echo $_id ?>').store('search', 'addressbookId:<?php echo $_id ?> -is:archive');
          new Obm.DropDownMenu($('addressbook-<?php echo $_id ?>').getElement('ul'), { positioning: 'fixed' });
        </script>
      </form>
      <span class='I legend' style='margin-left:20px;'><span id='count_addressbook_<?php echo $_id;?>'><?php echo $addressbooks->countContacts('addressbookId:'. $_id .' -is:archive'); ?></span><?php echo " ".__('contact(s)'); ?></span>
      </td>
    </tr>
    <?php  } ?>
    <tr>
      <td class="<?php echo ('archive' == $current['addressbook'])? 'current':'' ?>" id='addressbook-archive'>
        <form>
          <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-archive')); return false;"><?php echo __('Archive'); ?></a>
          <script type='text/javascript'>
            $('addressbook-archive').store('write', 0);
            $('addressbook-archive').store('search', 'is:archive addressbookId:( <?php echo implode(' OR ',array_keys($addressbooks->getAddressbooks('read'))); ?>)');
          </script>            
        </form>
        <span class='I legend' style='margin-left:20px;'><span id='count_addressbook_archive'>
          <?php echo $addressbooks->countContacts('is:archive addressbookId:('.implode(' OR ', array_keys($addressbooks->getAddressbooks('read'))).')'); ?></span><?php echo " ".__('contact(s)'); ?></span>
      </td>
    </tr>
    <tr style="<?php echo ('search' == $current['addressbook'])? '':'display:none;' ?>">
      <td class="<?php echo ('search' == $current['addressbook'])? 'current':'' ?>" id="addressbook-search">
        <form>
          <ul class="dropDownMenu addressBookMenu" >
            <li>
              <img onclick='setSearchFolderLinks();' alt="<?php echo __('Addressbook menu')?>" src="<?php echo self::__icon('dropdown') ?>" />
              <ul>
                <li><a id='saveSearchFolder' href='contact_index.php?action=save&amp;searchpattern='><?php echo __('Save') ?></a></li>
                <li><a id='exportSearchFolder' href='contact_index.php?action=export&amp;searchpattern='><?php echo __('Export') ?></a></li>
              </ul> 
            </li>
          </ul>
          <a href='' onclick="obm.contact.addressbook.selectAddressBook($('addressbook-search'), true); return false;"><?php echo __('Search results') ?></a>
          <script type='text/javascript'>
            $('addressbook-search').store('write', 0);
            $('addressbook-search').store('search', '<?php $search ?>');
            new Obm.DropDownMenu($('addressbook-search').getElement('ul'), { positioning: 'fixed' });
            function setSearchFolderLinks() {
              var pattern = $('searchpattern').value;
              $('saveSearchFolder').href = 'contact_index.php?action=save&searchpattern='+pattern;
              $('exportSearchFolder').href = 'contact_index.php?action=export&searchpattern='+pattern;
            }
          </script>                  
        </form>
        <span class='I legend' style='margin-left:20px;'><span id='count_addressbook_search'><?php echo $_addressbook->countContacts(); ?></span><?php echo " ".__('contact(s)'); ?></span>
      </td>
    </tr>
  </tbody>
</table>
