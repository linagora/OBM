<table>
  <tbody>
    <?php 
    $_size = floor(100/count($fields)).'%';
    foreach($contacts as $_id => $_contact) {
      $_class = ($_class == 'even')? 'odd':'even';
    ?>
    <tr class="<?php echo $_class ?> <?php echo ($_id == $current['contact'])? 'current':'' ?>" id="contact-<?php echo $_id ?>" onclick="obm.contact.addressbook.selectContact($(this))">
      <?php foreach($fields as $_fieldname => $_metadata) { ?>
      <?php if($_metadata['status'] == 2) { ?><th class="contactHeader"><?php } else { ?><td style='width:<?php echo $_size; ?>'><?php } ?>
      <?php
        switch($_fieldname) {
        case 'address':
          if($_contact->address[0]) echo self::__getaddress($_contact->address[0]);
          break;
        case 'email':
          if($_contact->email[0]) echo self::__getmail($_contact->email[0]['address']);
          break;
        case 'date':
          if($_contact->date) echo self::__getdate($_contact->date);
          break;
        case 'workvoice':
          $phones = $_contact->getCoords('phone','WORK;VOICE');
          if($phones[0]) echo $phones[0]['number'];
          break;
        case 'cellvoice':
          $phones = $_contact->getCoords('phone','CELL;VOICE');
          if($phones[0]) echo $phones[0]['number'];
          break;
        case 'workfax':
          $phones = $_contact->getCoords('phone','WORK;FAX');
          if($phones[0]) echo $phones[0]['number'];
          break;
        case 'homevoice':
          $phones = $_contact->getCoords('phone','HOME;VOICE');
          if($phones[0]) echo $phones[0]['number'];
          break;
        case 'country':
          if($contact->address[0]) echo $contact->address[0]['country'];
          break;
        case 'addressbook' :
          if(isset($addressbooks[$_contact->addressbook_id]))
            echo $addressbooks[$_contact->addressbook_id]->displayname;
          else 
            echo $_contact->addressbook;
          break;
        default:
          echo $_contact->$_fieldname.'';
        }
      ?>
      <?php if($_metadata['status'] == 2) { ?></th><?php } else { ?></td><?php } ?>
      <?php } ?>
    </tr>
    <?php } ?>
  </tbody>
  <?php if(count($contacts) >= 100 || $offset > 0) { ?>
  <tfoot>
    <tr class="<?php echo ($_class == 'even')? 'odd':'even' ?>" >
    <th colspan="<?php echo count($fields) ?>">
    <?php if($offset > 0) { ?>
      <a href='#' onclick="obm.contact.addressbook.moreContact(<?php echo $offset - 100 ?>); return false;">&lt;&lt; <?php echo __('Previous page'); ?></a>
    <?php } ?>
    <?php if(count($contacts) >= 100) { ?>
    | <a href='#' onclick="obm.contact.addressbook.moreContact(<?php echo $offset + 100 ?>); return false;"><?php echo __('Next page'); ?> &gt;&gt;</a>
    <?php } ?>
    </tr>
  </tfoot>
  <?php } ?>
</table>
