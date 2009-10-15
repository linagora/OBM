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
            <img alt="<?php echo __('Add fields') ?>" title="<?php echo __('Add fields') ?>" src="<?php echo self::__icon('add') ?>"/>
            <ul>
              <li><?php echo __('Address') ?>
                <ul>
                  <?php foreach($GLOBALS['l_address_labels'] as $label => $title) { ?>
                  <li>
                    <a href="" onclick=" new Obm.Contact.AddressWidget({label: {value: '<?php echo self::toJs($label) ?>', label:'<?php echo self::toJs($title) ?>'}},{container:'Address'});return false;"><?php echo $title ?></a>
                  </li>
                  <?php } ?>
                </ul>
              </li>
              <li><?php echo __('Email') ?>
                <ul>
                  <?php foreach($GLOBALS['l_email_labels'] as $label => $title) { ?>
                  <li>
                    <a href="" onclick=" new Obm.Contact.EmailWidget({label: {value: '<?php echo self::toJs($label) ?>', label:'<?php echo self::toJs($title) ?>'}},{container:'Email'});return false;"><?php echo $title ?></a>
                  </li>
                  <?php } ?>
                </ul>
              </li>
              <li><?php echo __('IM') ?>
                <ul>
                  <?php foreach($GLOBALS['l_im_labels'] as $label => $title) { ?>
                  <li>
                    <a href="" onclick="$('IM').removeClass('H'); new Obm.Contact.IMWidget({protocol: {value: '<?php echo self::toJs($label) ?>', label:'<?php echo self::toJs($title) ?>'}},{container:'IM'});return false;"><?php echo $title ?></a>
                  </li>
                  <?php } ?>
                </ul>
              </li>
              <li><?php echo __('Phone') ?>
                <ul>
                  <?php foreach($GLOBALS['l_phone_labels'] as $label => $title) { ?>
                  <li>
                    <a href="" onclick=" new Obm.Contact.PhoneWidget({label: {value: '<?php echo self::toJs($label) ?>', label:'<?php echo self::toJs($title) ?>'}},{container:'Phone'});return false;"><?php echo $title ?></a>
                  </li>
                  <?php } ?>
                </ul>
              </li>
              <li><?php echo __('Website') ?>
                <ul>
                  <?php foreach($GLOBALS['l_website_labels'] as $label => $title) { ?>
                  <li>
                    <a href="" onclick="$('Website').removeClass('H'); new Obm.Contact.WebsiteWidget({label: {value: '<?php echo self::toJs($label) ?>', label:'<?php echo self::toJs($title) ?>'}},{container:'Website'});return false;"><?php echo $title ?></a>
                  </li>
                  <?php } ?>
                </ul>
              </li>
              <?php if(empty($contact->mname)) { ?>
              <li><a href="" onclick="$('mname').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Middle name') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->suffix)) { ?>
              <li><a href="" onclick="$('suffix').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Suffix') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->aka)) { ?>
              <li><a href="" onclick="$('aka').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Also known as') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->title)) { ?>
              <li><a href="" onclick="$('title').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Title') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->birtday)) { ?>
              <li><a href="" onclick="$('birthday').getParent().removeClass('H');$('birthday').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Birthday') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->anniversary)) { ?>
              <li><a href="" onclick="$('anniversary').getParent().removeClass('H');$('anniversary').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Anniversary') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->date)) { ?>
              <li><a href="" onclick="$('date').getParent().removeClass('H');$('date').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Date') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->manager)) { ?>
              <li><a href="" onclick="$('manager').getParent().removeClass('H');$('manager').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Manager') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->spouse)) { ?>
              <li><a href="" onclick="$('spouse').getParent().removeClass('H');$('spouse').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Spouse') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->category)) { ?>
              <li><a href="" onclick="$('category').getParent().removeClass('H');$('category').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Category') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->service)) { ?>
              <li><a href="" onclick="$('service').getParent().removeClass('H');$('service').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Service') ?></a></li>
              <?php } ?>
              <?php if(!$contact->mailok) { ?>
              <li><a href="" onclick="$('mailok').getParent().removeClass('H');$('mailok').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Mailing') ?></a></li>
              <?php } ?>
              <?php if(!$contact->newsletter) { ?>
              <li><a href="" onclick="$('newsletter').getParent().removeClass('H');$('newsletter').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Newsletter') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->comment2)) { ?>
              <li><a href="" onclick="$('comment2').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Notes') ?></a></li>
              <?php } ?>
              <?php if(empty($contact->comment3)) { ?>
              <li><a href="" onclick="$('comment3').removeClass('H');this.getParent().dispose();return false;"><?php echo __('Other comment') ?></a></li>
              <?php } ?>
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
          <form id='contactForm' name='contactForm' action='#' method='post' onsubmit="obm.contact.addressbook.storeContact($(this), '$contact->id'); return false;">
            <img alt="<?php echo __('Contact photo') ?>" class="photo" src="<?php echo self::__getphoto($contact->photo) ?>">
            <fieldset class="head">
              <input id="lastname" size="12" type="text" name="lastname" value="<?php echo $contact->lastname ?>" title="<?php echo __('Lastname') ?>" />
              <input id="mname" size="5" class="<?php echo (empty($contact->mname)?'H':'') ?>" type="text" name="mname" value="<?php echo $contact->mname ?>" title="<?php echo __('Middle name') ?>" /> 
              <input id="firstname" size="12" type="text" name="firstname" value="<?php echo $contact->firstname ?>" title="<?php echo __('Firstname') ?>" />
              <input id="suffix" size="5" class="<?php echo (empty($contact->suffix)?'H':'') ?>" type="text" name="suffix" value="<?php echo $contact->suffix ?>" title="<?php echo __('Middle name') ?>" /> 
              <br />
              <span id="aka" class="formField <?php echo (empty($contact->aka)?'H':'') ?>">
                <label for="akaField"><?php echo __('Also known as') ?> : </label>
                <input type="text" name="aka" id="akaField" value="<?php echo $contact->aka ?>" title="<?php echo __('Also known as') ?>" />
              </span>
              <span id="title" class="formField <?php echo (empty($contact->title)?'H':'') ?>">
                <label for="titleField"><?php echo __('Title') ?> : </label>
                <input type="text" name="title" id="titleField" value="<?php echo $contact->title ?>" title="<?php echo __('Title') ?>" />
              </span>
              <br />
              <span id="company" class="formField">
                <label for="companyField"><?php echo __('Company') ?> : </label>
                <input id="companyField" type="text" name="company" id="companyField" value="<?php echo $contact->company ?>" title="<?php echo __('Company') ?>" />
              </span>
            </fieldset>
            <p class="LC"></p>
            <fieldset id="Address" class="details ">
              <legend><?php echo __('Addresses') ?></legend>
              <script type="text/javascript">
                <?php if(!empty($contact->address)) foreach($contact->address as $address) { ?>
                new Obm.Contact.AddressWidget({label: {value: '<?php echo self::toJs($contact->labelToString($address['label'], 'Address', false)) ?>', label:'<?php echo ($contact->labelToString($address['label'], 'Address')) ?>'}, street: {value: '<?php echo self::toJs($address['street']) ?>'}, zipcode: {value: '<?php echo self::toJs($address['zipcode']) ?>'}, town: {value: '<?php echo self::toJs($address['town']) ?>'}, expresspostal: {value: '<?php echo self::toJs($address['expresspostal']) ?>'}, country: {value: '<?php echo self::toJs($address['country']) ?>'}},{container:'Address'});
                <?php } else { ?>
                new Obm.Contact.AddressWidget({},{container:'Address'}); 
                <?php } ?>
              </script>
            </fieldset>
            <fieldset id="Email" class="details ">
              <legend><?php echo __('Emails') ?></legend>
              <script type="text/javascript">
                <?php if(!empty($contact->email)) foreach($contact->email as $email) { ?>
                new Obm.Contact.EmailWidget({label: {value: '<?php echo self::toJs($contact->labelToString($email['label'], 'Email', false)) ?>', label:'<?php echo ($contact->labelToString($email['label'], 'Email')) ?>'}, address: {value: '<?php echo self::toJs($email['address']) ?>'}},{container:'Email'});
                <?php } else { ?>
                new Obm.Contact.EmailWidget({},{container:'Email'}); 
                <?php } ?>
              </script>
            </fieldset>
            <fieldset id="IM" class="details <?php echo (empty($contact->im)?'H':'') ?>">
              <legend><?php echo __('IMs') ?></legend>
              <script type="text/javascript">
                <?php if(!empty($contact->im)) foreach($contact->im as $im) { ?>
                new Obm.Contact.IMWidget({protocol: {value: '<?php echo self::toJs($im['protocol']) ?>', label:'<?php echo ($contact->labelToString($GLOBALS['l_im_labels'][$im['protocol']], 'IM')) ?>'}, address: {value: '<?php echo self::toJs($im['address']) ?>'}},{container:'IM'});
                <?php } ?>
              </script>
            </fieldset>
            <fieldset id="Phone" class="details">
              <legend><?php echo __('Phones') ?></legend>
              <script type="text/javascript">
                <?php if(!empty($contact->phone)) foreach($contact->phone as $phone) { ?>
                new Obm.Contact.PhoneWidget({label: {value: '<?php echo self::toJs($contact->labelToString($phone['label'], 'Phone', false)) ?>', label:'<?php echo ($contact->labelToString($phone['label'], 'Phone')) ?>'}, number: {value: '<?php echo self::toJs($phone['number']) ?>'}},{container:'Phone'});
                <?php } else { ?>
                new Obm.Contact.PhoneWidget({},{container:'Phone'}); 
                <?php } ?>
              </script>
            </fieldset>
            <fieldset id="Website" class="details <?php echo (empty($contact->website)?'H':'') ?>">
              <legend><?php echo __('Websites') ?></legend>
              <script type="text/javascript">
                <?php if(!empty($contact->website)) foreach($contact->website as $website) { ?>
                new Obm.Contact.WebsiteWidget({label: {value: '<?php echo self::toJs($contact->labelToString($website['label'], 'Website', false)) ?>', label:'<?php echo ($contact->labelToString($website['label'], 'Website')) ?>'}, url: {value: '<?php echo self::toJs($website['url']) ?>'}},{container:'Website'});
                <?php } ?>
              </script>
            </fieldset>
            <fieldset id="anniversaryLayout" class="details <?php echo (empty($contact->date) && empty($contact->birthday) && empty($contact->anniversary))? 'H':''; ?>">
              <legend><?php echo __('Dates') ?></legend>
              <span id="birthday" class="formField <?php echo (empty($contact->birthday))?'H':'' ?>">
                <label for="birthdayField"><?php echo __('Birthday') ?> : </label>
                <?php echo self::__setdate('birthday', $contact->birthday, 'Birthday') ?>
              </span>            
              <span id="anniversary" class="formField <?php echo (empty($contact->anniversary))?'H':'' ?>">
                <label for="anniversaryField"><?php echo __('Anniversary') ?> : </label>
                <?php echo self::__setdate('anniversary', $contact->anniversary, 'Anniversary') ?>
              </span>    
              <span id="date" class="formField <?php echo (empty($contact->date))?'H':'' ?>">
                <label for="dateField"><?php echo __('Date') ?> : </label>
                <?php echo self::__setdate('date', $contact->date, 'Date') ?>
              </span>    
            </fieldset>
            <fieldset id="otherLayout" class="details <?php echo (empty($contact->spouse) && empty($contact->manager) && empty($contact->assistant) && empty($contact->category) && empty($contact->service) && empty($contact->mailok) && empty($contact->newsletter))? 'H':'' ?>">
              <legend><?php echo __('Other properties') ?></legend>
              <span id="spouse" class="formField <?php echo (empty($contact->spouse))?'H':'' ?>">
                <label for="spouseField"><?php echo __('Spouse') ?> : </label>
                <input type="text" name="spouse" id="spouseField" value="<?php echo $contact->spouse?>" title="<?php echo __('Spouse') ?>" />
              </span>  
              <span id="manager" class="formField <?php echo (empty($contact->manager))?'H':'' ?>">
                <label for="managerField"><?php echo __('Manager') ?> : </label>
                <input type="text" name="manager" id="managerField" value="<?php echo $contact->manager?>" title="<?php echo __('Manager') ?>" />
              </span>
              <span id="assistant" class="formField <?php echo (empty($contact->assistant))?'H':'' ?>">
                <label for="assistantField"><?php echo __('Assistant') ?> : </label>
                <input type="text" name="assistant" id="assistantField" value="<?php echo $contact->assistant?>" title="<?php echo __('Assistant') ?>" />
              </span>
              <span id="category" class="formField <?php echo (empty($contact->category))?'H':'' ?>">
                <label for="categoryField"><?php echo __('Category') ?> : </label>
                <input type="text" name="category" id="categoryField" value="<?php echo $contact->category?>" title="<?php echo __('Category') ?>" />
              </span>
              <span id="service" class="formField <?php echo (empty($contact->service))?'H':'' ?>">
                <label for="serviceField"><?php echo __('Service') ?> : </label>
                <input type="text" name="service" id="serviceField" value="<?php echo $contact->service?>" title="<?php echo __('Service') ?>" />
              </span>
              <span id="mailok" class="formField <?php echo (empty($contact->mailok))?'H':'' ?>">
                <label for="mailokField"><?php echo __('Mailing activated') ?> : </label>
                <?php echo self::__setboolean('mailok', $contact->mailok, 'Mailing activated') ?>
              </span>
              <span id="newsletter" class="formField <?php echo (empty($contact->newsletter))?'H':'' ?>">
                <label for="newsletterField"><?php echo __('Subscribe for newsletter') ?> : </label>
                <?php echo self::__setboolean('newsletter', $contact->newsletter, 'Subscribe for newsletter') ?>
              </span>
            </fieldset>
            <fieldset id="comment" class="details">
              <legend><?php echo __('Comment') ?></legend>
              <textarea name='comment'><?php echo $contact->comment ?></textarea>
            </fieldset>
            <fieldset id="comment2" class="details <?php echo (empty($contact->comment2))?'H':'' ?>">
              <legend><?php echo __('Notes') ?></legend>
              <textarea name='comment2'><?php echo $contact->comment2 ?></textarea>
            </fieldset>
            <fieldset id="comment3" class="details <?php echo (empty($contact->comment3))?'H':'' ?>">
              <legend><?php echo __('Other comment') ?></legend>
              <textarea name='comment3'><?php echo $contact->comment3 ?></textarea>
            </fieldset>
            <p class='LC C'>
              <input type='hidden' name='action' value='storeContact'  />
              <input type='hidden' name='id' value='<?php echo $contact->id ?>'  />
              <input type='hidden' name='addressbook' value='<?php echo $contact->addressbook ?>' />
              <input type='submit' value='<?php echo __('Save') ?>' />
              <input type='button' value='<?php echo __('Cancel') ?>' onclick="obm.contact.addressbook.consultContact('<?php echo $contact->id ?>');" />
            </p>
          </form>
        </td>
      </tr>
    </tbody>
  </table>
</div>
