<table>
  <tbody>
    <?php 
    foreach($contacts as $_id => $_contact) {
      $_class = ($_class == 'even')? 'odd':'even';
    ?>
    <tr class="<?php echo $_class ?> <?php echo ($_id == $current['contact'])? 'current':'' ?>" id="contact-<?php echo $_id ?>" onclick="obm.contact.addressbook.selectContact(this)">
      <?php foreach($fields as $_fieldname => $_metadata) { ?>
      <?php if($_metadata['status'] == 2) { ?><th><?php } else { ?><td><?php } ?>
      <?php
        switch($_fieldname) {
        case 'address':
          if($_contact->address[0]) echo self::__getaddress($_contact->address[0]);
          break;
        case 'email':
          if($_contact->email[0]) echo self::__getmail($_contact->email[0]['address']);
          break;
        case 'date':
          if($_contact->date) echo self::__getdate($contact->date);
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
        default:
          echo $_contact->$_fieldname.'';
        }
      ?>
      <?php } ?>
    </tr>
    <?php } ?>
    <tr class='filler'><th> </th></tr>
  </tbody>
</table>
