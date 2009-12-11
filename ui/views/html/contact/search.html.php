<div id='searchHolder'>
  <div id='basicSearch'>
    <form id='searchForm' onsubmit='obm.contact.addressbook.searchContact(this); return false;'>
      <input type='text' id='searchpattern' name='searchpattern' value='<?php echo $searchpattern ?>' />
      <input type='submit' value='<?php echo __('Search') ?>' />
      <input type='hidden' name='action' value='search' />
      <a href="#" onclick="$$('#advancedSearch','#basicSearch').toggle();"><?php echo __('Advanced search') ?></a>
    </form>
  </div>
  <div id='advancedSearch' style='display:none;'>
    <div class='header'>
      <span class='link'><a href="#" onclick="$$('#advancedSearch','#basicSearch').toggle();"><?php echo __('Hide advanced search') ?></a></span>
      <span class='title'><?php echo __('Advanced search') ?></span>
    </div>
    <div class='content'>
      <form id='advancedSearchForm' onsubmit='obm.contact.addressbook.searchContact(this);return false;'>
        <table>
          <tbody>
            <tr>            
              <th><label for="displaynameSearch"><?php echo __('Displayname') ?></label></th><td><input  title="Displayname" id="displaynameSearch" name="displayname" value="" type="text"></td>
              <th><label for="companySearch"><?php echo __('Company') ?></label></th><td><input  title="Company" id="companySearch" name="company" value="" type="text"></td>
              <th><label for="archiveSearch"><?php echo __('Archived') ?></label></th><td>
                <input type="checkbox" name="is" id="archiveSearch" value="archive"></td>
            </tr>
            <tr>   
              <th><label for="lastnameSearch"><?php echo __('Lastname') ?></label></th><td><input  title="Lastname" id="lastnameSearch" name="lastname" value="" type="text"></td>
              <th><label for="firstnameSearch"><?php echo __('Firstname') ?></label></th><td><input  title="Firstname" id="firstnameSearch" name="firstname" value="" type="text"></td>
              <th><label for="titleSearch"><?php echo __('Title') ?></label></th><td><input  title="Title" id="titleSearch" name="title" value="" type="text"></td>
            </tr>
            <tr>
              <th><label for="emailSearch"><?php echo __('Email') ?></label></th><td><input  title="Email" id="emailSearch" name="email" value="" type="text"></td>
              <th><label for="phoneSearch"><?php echo __('Phone') ?></label></th><td><input  title="Phone" id="phoneSearch" name="phone" value="" type="text"></td>
              <th><label for="addressbookSearch"><?php echo __('Addressbook') ?></label></th><td><input  title="Addressbook" id="addressbookSearch" name="in" value="" type="text"></td>
            </tr>
            <tr>
              <th><label for="countrySearch"><?php echo __('Country') ?></label></th><td><input  title="Country" id="countrySearch" name="country" value="" type="text"></td>
              <th><label for="townSearch"><?php echo __('Town') ?></label></th><td><input  title="Town" id="townSearch" name="town" value="" type="text"></td>
              <th><label for="zipcodeSearch"><?php echo __('Zip code') ?></label></th><td><input  title="Zip code" id="zipcodeSearch" name="zipcode" value="" type="text"></td>
            </tr>
            <tr>
              <th><label for="newsletterSearch"><?php echo __('Subscribed for newsletter') ?></label></th><td>
                <input type="checkbox" name="is" id="newsletterSearch" value="newsletter"></td>
              <th><label for="mailokSearch"><?php echo __('Mailing activated') ?></label></th><td>
                <input type="checkbox" name="is" id="mailokSearch" value="mailing"></td>
            </tr>
          </tbody>
        </table>
        <input type='submit' value='<?php echo __('Search') ?>' />
        <input type='button' onclick="$$('#advancedSearch','#basicSearch').toggle();" value='<?php echo __('Cancel') ?>'>
      </form>
    </div>
  </div>
  <p class='RC'></p>
</div>
