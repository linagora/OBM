<table class='contactPanelHeader'>
  <thead>
    <tr>
      <th>
        <a onclick="obm.contact.addressbook.hideContact(); return false;" href=""><img alt="<?php echo htmlspecialchars(__('Close the window')) ?>" src="<?php echo self::__icon('close') ?>" class="RF"/></a><?php echo htmlspecialchars(__('Contact card')) ?>
      </th>
    </tr>
    <tr>
      <td class="toolbar">
        <ul class="dropDownMenu" id="contactToolbar">
          <?php if($addressbooks[$contact->addressbook_id]->write) { ?>
          <li>
            <input onclick="obm.contact.addressbook.updateContact(<?php echo $contact->id ?>);" type='button' value='<?php echo htmlspecialchars(__('Update')) ?>' title="<?php echo htmlspecialchars(__('Update contact')) ?>" class='updateButton' />
          </li>
          <li>
            <input onclick="obm.contact.addressbook.deleteContact(<?php echo htmlspecialchars($contact->id) ?>, '<?php echo htmlspecialchars(self::toJs($contact->displayname)) ?>');" 
              type='button' value='<?php echo htmlspecialchars(__('Delete')) ?>' title="<?php echo htmlspecialchars(__('Delete contact')) ?>" class='deleteButton' />
          </li>
          <?php } ?> 
          <li>
            <input type='button' value='<?php echo htmlspecialchars(__('More actions')) ?>' title="<?php echo htmlspecialchars(__('More actions')) ?>" class='dropDownButton' />
            <ul>

              <!-- Copy contact -->
              <li><?php echo htmlspecialchars(__('Copy')) ?>
                <ul>
                  <?php foreach($addressbooks as $_id => $_addressbook) { ?>
                  <?php if($_id != $contact->addressbook_id && $_addressbook->write == 1) { ?>
                  <li>
                    <a onclick="obm.contact.addressbook.copyContact(<?php echo htmlspecialchars($contact->id) ?>,<?php echo htmlspecialchars($_id) ?>); return false;" href=""><?php echo htmlspecialchars($_addressbook->displayname) ?></a>
                  </li>
                  <?php } ?> 
                  <?php } ?> 
                </ul>
              </li>

              <!-- Move contact-->
              <?php if($addressbooks[$contact->addressbook_id]->write) { ?>
              <li><?php echo htmlspecialchars(__('Move')) ?>
                <ul>
                  <?php foreach($addressbooks as $_id => $_addressbook) { ?>
                  <?php if($_id != $contact->addressbook_id && $_addressbook->write == 1) { ?>
                  <li>
                    <a onclick="obm.contact.addressbook.moveContact(<?php echo htmlspecialchars($contact->id) ?>,<?php echo htmlspecialchars($_id) ?>); return false;" href=""><?php echo htmlspecialchars($_addressbook->displayname) ?></a>
                  </li>
                  <?php } ?> 
                  <?php } ?> 
                </ul>
              </li>
              <?php } ?> 

              <!-- if archived, remove from archives-->
              <?php if($addressbooks[$contact->addressbook_id]->write && $contact->archive ) { ?>
              <li>
                <a href="<?php echo self::__actionlink('removeFromArchive', array('contact_id' => $contact->id)) ?>"><?php echo htmlspecialchars(__('Remove from archive')) ?></a>
              </li>
              <?php } ?> 

              <!-- Export -->
              <li>
                <a href="<?php echo self::__actionlink('vcard', array('contact_id' => $contact->id)) ?>"><?php echo htmlspecialchars(__('Export as Vcard')) ?></a>
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
          <img alt="<?php echo htmlspecialchars(__('Contact photo')) ?>" class="photo" src="<?php echo self::__getphoto($contact->photo) ?>" />
          <div class="head">
            <h1><?php echo htmlspecialchars($contact->header.' '.$contact->firstname.' '.$contact->mname.' '.$contact->lastname.' '.$contact->suffix) ?></h1>
            <?php if(!empty($contact->aka)) { ?>
            <br/><h3><?php echo htmlspecialchars(__('Also known as')) ?> : </h3><span><?php echo htmlspecialchars($contact->aka) ?></span>
            <?php } ?>
            <?php if(!empty($contact->title)) { ?>
            <br/><h3><?php echo htmlspecialchars(__('Title')) ?> : </h3><span><?php echo htmlspecialchars($contact->title) ?></span>
            <?php } ?>
            <?php if(!empty($contact->company)) { ?>
            <br/><h3><?php echo htmlspecialchars(__('Company')) ?> : </h3><span><?php if($contact->company_id) echo self::__getentitylink($contact->company, $contact->company_id, 'company'); else echo htmlspecialchars($contact->company) ?></span>
            <?php } ?>
            <?php if(!empty($contact->commonname)) { ?>
            <br/><h3><?php echo htmlspecialchars(__('Common name')) ?> : </h3><span><?php echo htmlspecialchars($contact->commonname) ?></span>
            <?php } ?>

          </div>
          <p class="LC"></p>
          <?php if(!empty($contact->address)) { ?>
          <dl id="addressLayout" class="details ">
            <?php foreach($contact->address as $address) { ?>
            <dt><?php echo htmlspecialchars($contact->labelToString($address['label'], 'ADDRESS')) ?> : </dt> <dd><?php echo self::__getaddress($address) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->email)) { ?>
          <dl id="emailLayout" class="details">
            <?php foreach($contact->email as $email) { ?>
            <dt><?php echo htmlspecialchars($contact->labelToString($email['label'], 'EMAIL')) ?> : </dt><dd><?php echo self::__getmail($email['address']) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->im)) { ?>
          <dl id="imLayout" class="details">
            <?php foreach($contact->im as $im) { ?>
            <dt><?php echo htmlspecialchars($GLOBALS['l_im_labels'][$im['protocol']]) ?> : </dt><dd><?php echo htmlspecialchars($im['address']) ?></dd>
            <?php } ?>     
          </dl>
          <?php } ?>
          <?php if(!empty($contact->phone)) { ?>
          <dl id="phoneLayout" class="details ">
            <?php foreach($contact->phone as $phone) { ?>
            <dt><?php echo htmlspecialchars($contact->labelToString($phone['label'], 'PHONE')) ?> : </dt><dd><?php echo htmlspecialchars($phone['number']) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->website)) { ?>
          <dl id="websiteLayout" class="details">
            <?php foreach($contact->website as $website) { ?>
            <dt><?php echo htmlspecialchars($contact->labelToString($website['label'], 'WEBSITE')) ?> : </dt><dd><?php echo self::__getlink($website['url']) ?></dd>
            <?php } ?>        
          </dl>
          <?php } ?>
          <?php if(!empty($contact->date) || !empty($contact->birthday) || !empty($contact->anniversary)) { ?>
          <dl id="anniversaryLayout" class="details ">
            <?php if(!empty($contact->birthday)) { ?>
            <dt><?php echo htmlspecialchars(__('Birthday')) ?> : </dt><dd><?php echo self::__getdate($contact->birthday) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->anniversary)) { ?>
            <dt><?php echo htmlspecialchars(__('Anniversary')) ?> : </dt><dd><?php echo self::__getdate($contact->anniversary) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->date)) { ?>
            <dt><?php echo htmlspecialchars(__('Date')) ?> : </dt><dd><?php echo self::__getdate($contact->date) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->categories)) { ?>
          <dl id='categoriesLayout' class='details'>
            <?php foreach($contact->categories as $_name => $_category) { ?>
            <?php foreach($_category as $_categoryId => $_categoryValue) { ?>
              <dt><?php echo htmlspecialchars($GLOBALS['l_'.$_name]) ?> : </dt><dd><?php echo htmlspecialchars($_categoryValue['label']) ?></dd>
            <?php } ?> 
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->function_id) || !empty($contact->market_id) || !empty($contact->datasource_id) || !empty($contact->kind_id) || !empty($contact->mailok) || !empty($contact->newsletter)) { ?>
          <dl id="crmLayout" class="details">
           <?php if($contact->kind_id) { ?>
            <dt><?php echo htmlspecialchars(__('Language')) ?> : </dt><dd><?php echo htmlspecialchars($contact->language) ?></dd>
            <?php } ?>
            <?php if($contact->datasource_id) { ?>
            <dt><?php echo htmlspecialchars(__('Datasource')) ?> : </dt><dd><?php echo htmlspecialchars($contact->datasource) ?></dd>
            <?php } ?>
            <?php if($contact->function_id) { ?>
            <dt><?php echo htmlspecialchars(__('Function')) ?> : </dt><dd><?php echo htmlspecialchars($contact->function) ?></dd>
            <?php } ?>
            <?php if($contact->market_id) { ?>
            <dt><?php echo htmlspecialchars(__('Marketing manager')) ?> : </dt><dd><?php echo self::__getentitylink($contact->market, $contact->market_id, 'people') ?></dd>
            <?php } ?>
            <?php if($contact->mailok) { ?>
            <dt><?php echo htmlspecialchars(__('Mailing activated')) ?> : </dt><dd><?php echo self::__getboolean($contact->mailok) ?></dd>
            <?php } ?>
            <?php if($contact->newsletter) { ?>
            <dt><?php echo htmlspecialchars(__('Subscribe for newsletter')) ?> : </dt><dd><?php echo self::__getboolean($contact->newsletter) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->spouse) || !empty($contact->manager) || !empty($contact->assistant) || !empty($contact->category) || !empty($contact->service)) { ?>
          <dl id="otherLayout" class="details ">
            <?php if(!empty($contact->spouse)) { ?>
            <dt><?php echo htmlspecialchars(__('Spouse')) ?> : </dt><dd><?php echo htmlspecialchars($contact->spouse) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->manager)) { ?>
            <dt><?php echo htmlspecialchars(__('Manager')) ?> : </dt><dd><?php echo htmlspecialchars($contact->manager) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->assistant)) { ?>
            <dt><?php echo htmlspecialchars(__('Assistant')) ?> : </dt><dd><?php echo htmlspecialchars($contact->assistant) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->category)) { ?>
            <dt><?php echo htmlspecialchars(__('Category')) ?> : </dt><dd><?php echo htmlspecialchars($contact->category) ?></dd>
            <?php } ?>
            <?php if(!empty($contact->service)) { ?>
            <dt><?php echo htmlspecialchars(__('Service')) ?> : </dt><dd><?php echo htmlspecialchars($contact->service) ?></dd>
            <?php } ?>
          </dl>
          <?php } ?>
          <?php if(!empty($contact->comment)) { ?>
            <dl id="commentLayout" class="details">
              <dt><?php echo htmlspecialchars(__('Comments')) ?> : </dt>
              <dd>
                <?php echo nl2br(htmlspecialchars($contact->comment)) ?>
                <?php if(!empty($contact->comment2)) { ?>
                  <br/><?php echo nl2br(htmlspecialchars($contact->comment2)) ?></dl>
                <?php } ?>
                <?php if(!empty($contact->comment3)) { ?>
                  <br/><?php echo nl2br(htmlspecialchars($contact->comment3)) ?></dl>
                <?php } ?>
              </dd>
            </dl>
          <?php } ?>
        </td>
      </tr>
    </tbody>
  </table>
