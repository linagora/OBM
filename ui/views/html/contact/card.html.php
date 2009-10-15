<table class='contactPanelHeader'>
  <thead>
    <tr>
      <th>
        <a onclick="obm.contact.addressbook.hideContact(); return false;" href=""><img alt="<?php echo __('Close the window') ?>" src="<?php echo self::__icon('close') ?>" class="RF"/></a><?php echo __('Contact card') ?>
      </th>
    </tr>
    <tr>
      <td class="toolbar">
        <ul class="dropDownMenu" id="contactToolbar">
          <?php if($addressbooks[$contact->addressbook]->write) { ?>
          <li>
          <a onclick="obm.contact.addressbook.updateContact(<?php echo $contact->id ?>); return false;" href=""><img title="<?php echo __('Update contact') ?>" alt="<?php echo __('Update contact') ?>" src="<?php echo self::__icon('edit') ?>"/></a>
          </li>
          <li>
            <a onclick='obm.contact.addressbook.deleteContact(<?php echo $contact->id ?>); return false;' href=""><img title="<?php echo __('Delete contact') ?>" alt="<?php echo __('Delete contact') ?>" src="<?php echo self::__icon('delete') ?>"/></a>
          </li>
          <?php } ?> 
          <li>
            <img alt="<?php echo __('Others actions') ?>" title="<?php echo __('Others actions') ?>" src="<?php echo self::__icon('others') ?>"/>
            <ul>
              <li><?php echo __('Copy') ?>
                <ul>
                  <?php foreach($addressbooks as $_id => $_addressbook) { ?>
                  <?php if($_id != $contact->addressbook) { ?>
                  <li>
                    <a onclick="obm.contact.addressbook.copyContact(<?php echo $contact->id ?>,<?php echo $_id ?>); return false;" href=""><?php echo $_addressbook->name ?></a>
                  </li>
                  <?php } ?> 
                  <?php } ?> 
                </ul>
              </li>
              <li>
                <a href="<?php echo self::__actionlink('vcard', array('contact_id' => $contact->id)) ?>"><?php echo __('Export as Vcard') ?></a>
              </li>
            </ul>
          </li>
        </ul>
        <script type='text/javascript'>
          new Obm.DropDownMenu($('contactToolbar'));
        </script>
      </td>
    </tr>
  </thead>
</table>
<div class='contactPanelContainer' id='informationContainer'>
  <table id="contact-card-<?php echo $contact->id ?>">
    <tbody>
      <tr>
        <td>
          <img alt="<?php echo __('Contact photo') ?>" class="photo" src="<?php echo self::__getphoto($contact->photo) ?>">
          <div class="head">
            <h1><?php echo $contact->lastname.' '.$contact->mname.''.$contact->firstname.' '.$contact->suffix ?></h1>
            <?php if(!empty($contact->aka)) { ?>
            <br><h3><?php echo __('Also known as') ?> : </h3><span><?php echo $contact->aka ?></span>
            <?php } ?>
            <?php if(!empty($contact->title)) { ?>
            <br><h3><?php echo __('Title') ?> : </h3><span><?php echo $contact->title ?></span>
            <?php } ?>
            <?php if(!empty($contact->company)) { ?>
            <br><h3><?php echo __('Company') ?> : </h3><span><?php echo $contact->company ?></span>
            <?php } ?>
          </div>
          <p class="LC"></p>
          <?php if(!empty($contact->phone)) { ?>
          <dl id="phoneLayout" class="details ">
            <?php foreach($contact->phone as $phone) { ?>
            <dt><?php echo $contact->labelToString($phone['label'], 'PHONE') ?> : </dt><dd><?php echo $phone['number'] ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->email)) { ?>
          <dl id="emailLayout" class="details">
            <?php foreach($contact->email as $email) { ?>
            <dt><?php echo $contact->labelToString($email['label'], 'EMAIL') ?> : </dt><dd><?php echo self::__getmail($email['address']) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->address)) { ?>
          <dl id="addressLayout" class="details ">
            <?php foreach($contact->address as $address) { ?>
            <dt><?php echo $contact->labelToString($address['label'], 'ADDRESS') ?> : </dt><dd><?php echo self::__getaddress($address) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->website)) { ?>
          <dl id="websiteLayout" class="details">
            <?php foreach($contact->website as $website) { ?>
            <dt><?php echo $contact->labelToString($website['label'], 'WEBSITE') ?> : </dt><dd><?php echo self::__getlink($website['url']) ?></dd>
            <?php } ?>        
          </dl>
          <?php } ?>
          <?php if(!empty($contact->im)) { ?>
          <dl id="imLayout" class="details">
            <?php foreach($contact->im as $im) { ?>
            <dt><?php echo $GLOBALS['l_im_labels'][$im['protocol']] ?> : </dt><dd><?php echo $im['address'] ?></dd>
            <?php } ?>     
          </dl>
          <?php } ?>
          <?php if(!empty($contact->date) || !empty($contact->birthday) || !empty($contact->anniversary)) { ?>
          <dl id="anniversaryLayout" class="details ">
            <?php if(!empty($contact->birthday)) { ?>
            <dt><?php echo __('Birthday') ?> : </dt><dd><?php echo self::__getdate($contact->birthday) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->anniversary)) { ?>
            <dt><?php echo __('Anniversary') ?> : </dt><dd><?php echo self::__getdate($contact->anniversary) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->date)) { ?>
            <dt><?php echo __('Date') ?> : </dt><dd><?php echo self::__getdate($contact->date) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->spouse) || !empty($contact->manager) || !empty($contact->assistant) || !empty($contact->category) || !empty($contact->service) || !empty($contact->mailok) || !empty($contact->newsletter)) { ?>
          <dl id="otherLayout" class="details ">
            <?php if(!empty($contact->spouse)) { ?>
            <dt><?php echo __('Spouse') ?> : </dt><dd><?php echo $contact->spouse ?></dd>
            <?php } ?>
            <?php if(!empty($contact->manager)) { ?>
            <dt><?php echo __('Manager') ?> : </dt><dd><?php echo $contact->manager ?></dd>
            <?php } ?>
            <?php if(!empty($contact->assistant)) { ?>
            <dt><?php echo __('Assistant') ?> : </dt><dd><?php echo $contact->assistant ?></dd>
            <?php } ?>
            <?php if(!empty($contact->category)) { ?>
            <dt><?php echo __('Category') ?> : </dt><dd><?php echo $contact->category ?></dd>
            <?php } ?>
            <?php if(!empty($contact->service)) { ?>
            <dt><?php echo __('Service') ?> : </dt><dd><?php echo $contact->service ?></dd>
            <?php } ?>
            <?php if($contact->mailok) { ?>
            <dt><?php echo __('Mailing activated') ?> : </dt><dd><?php echo self::__getboolean($contact->mailok) ?></dd>
            <?php } ?>
            <?php if($contact->newsletter) { ?>
            <dt><?php echo __('Subscribe for newsletter') ?> : </dt><dd><?php echo self::__getboolean($contact->newsletter) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->comment)) { ?>
          <dl id="commentLayout" class="details"><?php echo nl2br($contact->comment) ?></dl>
          <?php } ?>
          <?php if(!empty($contact->comment2)) { ?>
          <dl id="comment2Layout" class="details"><?php echo nl2br($contact->comment2) ?></dl>
          <?php } ?>
          <?php if(!empty($contact->comment3)) { ?>
          <dl id="comment3Layout" class="details"><?php echo nl2br($contact->comment3) ?></dl>
          <?php } ?>
        </td>
      </tr>
    </tbody>
  </table>
</div>
