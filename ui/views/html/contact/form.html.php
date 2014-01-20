<table class='contactPanelHeader'>
  <thead>
    <tr>
      <th>
        <a onclick="obm.contact.addressbook.hideContact(); return false;" href=""><img alt="<?php echo htmlspecialchars(__('Close the window')) ?>" src="<?php echo htmlspecialchars(self::__icon('close')) ?>" class="RF"/></a><?php echo htmlspecialchars(__('Contact card')) ?>
      </th>
    </tr>
    <tr>
      <td class="toolbar">
        <ul class="dropDownMenu" id="contactToolbar">
          <?php if($addressbooks[$contact->addressbook_id]->read && $contact->id) { ?>
          <li>
            <input onclick="obm.contact.addressbook.consultContact(<?php echo htmlspecialchars($contact->id) ?>);" type='button' value='<?php echo htmlspecialchars(__('Consult')) ?>' title="<?php echo htmlspecialchars(__('Consult contact')) ?>" class='updateButton' />
          </li>
          <?php } ?> 
          <?php if($addressbooks[$contact->addressbook_id]->write && $contact->id) { ?>
          <li>
            <input onclick='obm.contact.addressbook.deleteContact(<?php echo htmlspecialchars($contact->id) ?>, "<?php echo htmlspecialchars(self::toJs($contact->displayname)) ?>");' type='button' value='<?php echo htmlspecialchars(__('Delete')) ?>' title="<?php echo htmlspecialchars(__('Delete contact')) ?>" class='deleteButton' />
          </li>
          <?php } ?> 
          <li>
            <input type='button' value='<?php echo htmlspecialchars(__('Add fields')) ?>' title="<?php echo htmlspecialchars(__('Add Fields')) ?>" class='dropDownButton' />
            <ul>
              <?php if(empty($contact->mname) && empty($contact->suffix)) { ?>
              <li><a href="" onclick="$('kindField').removeClass('H');$('extendedName').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Extended name')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->aka)) { ?>
              <li><a href="" onclick="$('aka').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Also known as')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->title)) { ?>
              <li><a href="" onclick="$('title').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Title')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->commonname)) { ?>
              <li><a href="" onclick="$('commonname').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Common name')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->im)) { ?>
              <li><a  href="" onclick="$('IMLayout').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Instant messaging')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->website)) { ?>
              <li><a  href="" onclick="$('WebsiteLayout').removeClass('H');OverText.update(); return false;"><?php echo htmlspecialchars(__('Website')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->birthday) && empty($contact->anniversary) && empty($contact->date)) { ?>
              <li><a href="" onclick="$('datesLayout').removeClass('H'); return false;"><?php echo htmlspecialchars(__('Dates')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->function_id) && empty($contact->market_id) && empty($contact->datasource_id) && empty($contact->kind_id) && empty($contact->mailok) && empty($contact->newsletter)) { ?>
              <li><a href="" onclick="$('crmLayout').removeClass('H'); return false;"><?php echo htmlspecialchars(__('Commercial fields')) ?></a></li> 
              <?php } ?>
              <?php if(empty($contact->manager) && empty($contact->spouse) && empty($contact->assistant) && empty($contact->category) && empty($contact->service)) { ?>
              <li><a href="" onclick="$('otherLayout').removeClass('H'); return false;"><?php echo htmlspecialchars(__('Other properties')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->comment2)) { ?>
              <li><a href="" onclick="$('comment2').removeClass('H'); return false;"><?php echo htmlspecialchars(__('Notes')) ?></a></li>
              <?php } ?>
              <?php if(empty($contact->comment3)) { ?>
              <li><a href="" onclick="$('comment3').removeClass('H'); return false;"><?php echo htmlspecialchars(__('Other comment')) ?></a></li>
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
  <table id="contact-card-<?php echo htmlspecialchars($contact->id) ?>">
    <tbody>
      <tr>
        <td>
          <form id='contactForm' name='contactForm' action='#' method='post' onsubmit="obm.contact.addressbook.storeContact($(this), '<?php echo htmlspecialchars($contact->id) ?>'); return false;">
            <img alt="<?php echo htmlspecialchars(__('Contact photo')) ?>" class="photo" src="<?php echo self::__getphoto($contact->photo) ?>" />
            <select class='addressbookSelector' name='addressbook'>
              <?php foreach($addressbooks as $_id => $_addressbook ) { ?>
                <?php if ($_addressbook->isWritable()) { ?>
                  <?php if ($_addressbook->id == $contact->addressbook_id) { ?>
                  <option selected='selected' value='<?php echo htmlspecialchars($_addressbook->id) ?>' ><?php echo htmlspecialchars($_addressbook->displayname) ?></option>
                  <?php } else { ?>
                  <option value='<?php echo htmlspecialchars($_addressbook->id) ?>' ><?php echo htmlspecialchars($_addressbook->displayname) ?></option>
                  <?php }?>
                <?php } ?>
              <?php } ?>
            </select>
            <br />
            <fieldset class="head">
              <span id="kindField" class="<?php echo (empty($contact->mname) && empty($contact->suffix) && empty($contact->kind_id)?'H':'') ?>">
                <?php echo self::__setlist('kind', $kinds, 'Kind', $contact->kind_id, true) ?>
              </span>
              <input id="firstname" size="12" type="text" name="firstname" value="<?php echo htmlspecialchars($contact->firstname) ?>" title="<?php echo htmlspecialchars(__('Firstname')) ?>" />
              <input id="lastname" size="12" type="text" name="lastname" value="<?php echo htmlspecialchars($contact->lastname) ?>" title="<?php echo htmlspecialchars(__('Lastname')) ?>" />
              <br />
              <span id='extendedName' class="formField <?php echo (empty($contact->mname) && empty($contact->suffix)?'H':'') ?>">
                <input id="mname" type="text" name="mname" value="<?php echo htmlspecialchars($contact->mname) ?>" title="<?php echo htmlspecialchars(__('Middle name')) ?>" /> 
                <input id="suffix" type="text" name="suffix" value="<?php echo htmlspecialchars($contact->suffix) ?>" title="<?php echo htmlspecialchars(__('Suffix')) ?>" /> 
              </span>
              <script type="text/javascript">
                new OverText('#lastname, #mname, #firstname, #suffix');
              </script>
              <span id="aka" class="formField <?php echo (empty($contact->aka)?'H':'') ?>">
                <label for="akaField"><?php echo htmlspecialchars(__('Also known as')) ?> : </label>
                <input type="text" name="aka" id="akaField" value="<?php echo htmlspecialchars($contact->aka) ?>" title="<?php echo htmlspecialchars(__('Also known as')) ?>" />
              </span>
              <span id="title" class="formField <?php echo (empty($contact->title)?'H':'') ?>">
                <label for="titleField"><?php echo htmlspecialchars(__('Title')) ?> : </label>
                <input type="text" name="title" id="titleField" value="<?php echo htmlspecialchars($contact->title) ?>" title="<?php echo htmlspecialchars(__('Title')) ?>" />
              </span>
              <span id="commonname" class="formField <?php echo (empty($contact->commonname)?'H':'') ?>">
                <label for="commonnameField"><?php echo htmlspecialchars(__('Common name')) ?> : </label>
                <input type="text" name="commonname" id="commonnameField" value="<?php echo htmlspecialchars($contact->commonname) ?>" title="<?php echo htmlspecialchars(__('Common name')) ?>" />
              </span>
              <br />
              <span id="company" class="formField">
                <label for="companyField"><?php echo htmlspecialchars(__('Company')) ?> : </label>
                <?php echo self::__setentitylink('company', $contact->company, $contact->company_id, 'company', 'Company'); ?>
              </span>
              <p class='CL' />
            </fieldset>
            <p class="LC"></p>
            <fieldset id="AddressLayout" class="details ">
              <legend><?php echo htmlspecialchars(__('Addresses')) ?></legend>
              <?php if(!empty($contact->address)) foreach($contact->address as $_address) { ?>
              <?php echo self::__setaddress($_address) ?>
              <?php } else { ?>
              <?php echo self::__setaddress() ?>
              <?php }?>
              <script type="text/javascript">
                new Obm.MultipleField($('AddressLayout'),'table.coordinate', {overtext: 'input, textarea'});
              </script>
            </fieldset>
            <fieldset id="EmailLayout" class="details ">
              <legend><?php echo htmlspecialchars(__('Emails')) ?></legend>
              <?php if(!empty($contact->email)) foreach($contact->email as $_email) { ?>
              <?php echo self::__setemail($_email) ?>
              <?php } else { ?>
              <?php echo self::__setemail() ?>
              <?php }?>
              <script type="text/javascript">
                new Obm.MultipleField($('EmailLayout'),'table.coordinate', {overtext: 'input, textarea'});
              </script>
            </fieldset>
            <fieldset id="IMLayout" class="details <?php echo (empty($contact->im)?'H':'') ?>">
              <legend><?php echo htmlspecialchars(__('Instant messagings')) ?></legend>
              <?php if(!empty($contact->im)) foreach($contact->im as $_im) { ?>
              <?php echo self::__setim($_im) ?>
              <?php } else { ?>
              <?php echo self::__setim() ?>
              <?php }?>
              <script type="text/javascript">
                new Obm.MultipleField($('IMLayout'),'table.coordinate', {overtext: 'input, textarea'});
              </script>
            </fieldset>
            <fieldset id="PhoneLayout" class="details">
              <legend><?php echo htmlspecialchars(__('Phones')) ?></legend>
              <?php if(!empty($contact->phone)) foreach($contact->phone as $_phone) { ?>
              <?php echo self::__setphone($_phone) ?>
              <?php } else { ?>
              <?php echo self::__setphone() ?>
              <?php }?>
              <script type="text/javascript">
                new Obm.MultipleField($('PhoneLayout'),'table.coordinate', {overtext: 'input, textarea'});
              </script>
            </fieldset>
            <fieldset id="WebsiteLayout" class="details <?php echo (empty($contact->website)?'H':'') ?>">
              <legend><?php echo htmlspecialchars(__('Websites')) ?></legend>
              <?php if(!empty($contact->website)) foreach($contact->website as $_website) { ?>
              <?php echo self::__setwebsite($_website) ?>
              <?php } else { ?>
              <?php echo self::__setwebsite() ?>
              <?php }?>
              <script type="text/javascript">
                new Obm.MultipleField($('WebsiteLayout'),'table.coordinate', {overtext: 'input, textarea'});
              </script>
            </fieldset>
            <fieldset id="datesLayout" class="details <?php echo (empty($contact->date) && empty($contact->birthday) && empty($contact->anniversary))? 'H':''; ?>">
              <legend><?php echo htmlspecialchars(__('Dates')) ?></legend>
              <span id="birthday" class="formField">
                <label for="birthdayField"><?php echo htmlspecialchars(__('Birthday')) ?> : </label>
                <?php echo self::__setdate('birthday', $contact->birthday, 'Birthday') ?>
              </span>            
              <span id="anniversary" class="formField">
                <label for="anniversaryField"><?php echo htmlspecialchars(__('Anniversary')) ?> : </label>
                <?php echo self::__setdate('anniversary', $contact->anniversary, 'Anniversary') ?>
              </span>    
              <span id="date" class="formField">
                <label for="dateField"><?php echo htmlspecialchars(__('Date')) ?> : </label>
                <?php echo self::__setdate('date', $contact->date, 'Date') ?>
              </span>    
            </fieldset>
            <fieldset id="categories" class="details <?php echo (empty($categories))?'H':'' ?>">
              <legend><?php echo htmlspecialchars(__('Other categories')) ?></legend>
              <?php foreach($categories as $_name => $_category) { ?>
              <?php if($_category['mode'] == 'mono') { ?>
              <span id='cateogry-<?php echo htmlspecialchars($_name) ?>' class='formField'>
                <label for='cateogry-<?php echo htmlspecialchars($_name) ?>Field'><?php echo htmlspecialchars($GLOBALS['l_'.$_name]) ?></label>
                <?php echo self::__setlist($_name, $_category['values'], $GLOBALS['l_'.$_name], @key($contact->categories[$_name]), true) ?>
              </span>
              <?php } else { ?>
              <div id='category-<?php echo htmlspecialchars($_name) ?>'>
              <?php if(is_array($contact->categories[$_name])) foreach($contact->categories[$_name] as $_categoryId => $_categoryValue) { ?>
              <span class='formField'>
                <label for='cateogry-<?php echo htmlspecialchars($_name) ?>Field'><?php echo htmlspecialchars($GLOBALS['l_'.$_name]) ?></label>
                <?php echo self::__setlist($_name.'[]', $_category['values'], $GLOBALS['l_'.$_name], $_categoryId, true);  ?>
              </span>
              <?php } ?>
              <span class='formField'>
                <label for='cateogry-<?php echo htmlspecialchars($_name ) ?>Field'><?php echo htmlspecialchars($GLOBALS['l_'.$_name]) ?></label>              
                <?php echo self::__setlist($_name.'[]', $_category['values'], $GLOBALS['l_'.$_name], NULL, true);  ?>
              </span>
              </div>
              <script language='text/javascript'>
                new Obm.MultipleField($('category-<?php echo $_name  ?>'),'span.formField')
              </script>
              <?php } ?>
              <?php } ?>
            </fieldset>
            <fieldset id="crmLayout" class="details <?php echo (empty($contact->function_id) && empty($contact->market_id) && empty($contact->datasource_id) && empty($contact->kind_id) && empty($contact->mailok) && empty($contact->newsletter))? 'H':'' ?>">
              <legend><?php echo htmlspecialchars(__('CRM Fields')) ?></legend>
              <span id="datasource" class="formField">
                <label for="datasourceField"><?php echo htmlspecialchars(__('Datasource')) ?> : </label>
                <?php echo self::__setlist('datasource', $datasources, 'Datasource', $contact->datasource_id, true); ?>
              </span>  
              <span id="function" class="formField">
                <label for="functionField"><?php echo htmlspecialchars(__('Function')) ?> : </label>
                <?php echo self::__setlist('function', $functions, 'Function', $contact->function_id, true); ?>
              </span>  
              <span id="market" class="formField">
                <label for="marketField"><?php echo htmlspecialchars(__('Marketing manager')) ?> : </label>
                <?php echo self::__setlist('market', $markets, 'Market', $contact->market_id, true); ?>
              </span>  
              <span id="mailok" class="formField">
                <label for="mailokField"><?php echo htmlspecialchars(__('Mailing activated')) ?> : </label>
                <?php echo self::__setboolean('mailok', $contact->mailok, 'Mailing activated') ?>
              </span>
              <span id="newsletter" class="formField">
                <label for="newsletterField"><?php echo htmlspecialchars(__('Subscribe for newsletter')) ?> : </label>
                <?php echo self::__setboolean('newsletter', $contact->newsletter, 'Subscribe for newsletter') ?>
              </span>
            </fieldset>
            <fieldset id="otherLayout" class="details <?php echo (empty($contact->spouse) && empty($contact->manager) && empty($contact->assistant) && empty($contact->category) && empty($contact->service))? 'H':'' ?>">
              <legend><?php echo htmlspecialchars(__('Other properties')) ?></legend>
              <span id="spouse" class="formField">
                <label for="spouseField"><?php echo htmlspecialchars(__('Spouse')) ?> : </label>
                <input type="text" name="spouse" id="spouseField" value="<?php echo htmlspecialchars($contact->spouse) ?>" title="<?php echo htmlspecialchars(__('Spouse')) ?>" />
              </span>  
              <span id="manager" class="formField">
                <label for="managerField"><?php echo htmlspecialchars(__('Manager')) ?> : </label>
                <input type="text" name="manager" id="managerField" value="<?php echo htmlspecialchars($contact->manager) ?>" title="<?php echo htmlspecialchars(__('Manager')) ?>" />
              </span>
              <span id="assistant" class="formField">
                <label for="assistantField"><?php echo htmlspecialchars(__('Assistant')) ?> : </label>
                <input type="text" name="assistant" id="assistantField" value="<?php echo htmlspecialchars($contact->assistant) ?>" title="<?php echo htmlspecialchars(__('Assistant')) ?>" />
              </span>
              <span id="category" class="formField">
                <label for="categoryField"><?php echo htmlspecialchars(__('Category')) ?> : </label>
                <input type="text" name="category" id="categoryField" value="<?php echo htmlspecialchars($contact->category) ?>" title="<?php echo htmlspecialchars(__('Category')) ?>" />
              </span>
              <span id="service" class="formField">
                <label for="serviceField"><?php echo htmlspecialchars(__('Service')) ?> : </label>
                <input type="text" name="service" id="serviceField" value="<?php echo htmlspecialchars($contact->service) ?>" title="<?php echo htmlspecialchars(__('Service')) ?>" />
              </span>
            </fieldset>
            <fieldset id="comment" class="details">
              <legend><?php echo htmlspecialchars(__('Comment')) ?></legend>
              <textarea name='comment'><?php echo htmlspecialchars($contact->comment) ?></textarea>
            </fieldset>
            <fieldset id="comment2" class="details <?php echo (empty($contact->comment2))?'H':'' ?>">
              <legend><?php echo htmlspecialchars(__('Notes')) ?></legend>
              <textarea name='comment2'><?php echo htmlspecialchars($contact->comment2) ?></textarea>
            </fieldset>
            <fieldset id="comment3" class="details <?php echo (empty($contact->comment3))?'H':'' ?>">
              <legend><?php echo htmlspecialchars(__('Other comment')) ?></legend>
              <textarea name='comment3'><?php echo htmlspecialchars($contact->comment3) ?></textarea>
            </fieldset>
            <p class='LC C'>
              <input type='hidden' name='company_id' value='<?php echo $contact->company_id ?>'  />
              <input type='hidden' name='action' value='storeContact'  />
              <input type='hidden' name='id' value='<?php echo htmlspecialchars($contact->id) ?>'  />
              <input type='submit' value='<?php echo htmlspecialchars(__('Save')) ?>' />
              <input type='button' value='<?php echo htmlspecialchars(__('Cancel')) ?>' onclick="if(confirm('<?php echo self::toJs(__('Are you sure you want to stop editing this contact? Unsaved changes will be lost.')) ?>')) obm.contact.addressbook.consultContact('<?php echo htmlspecialchars($contact->id) ?>');" />
            </p>
          </form>
        </td>
      </tr>
    </tbody>
  </table>
