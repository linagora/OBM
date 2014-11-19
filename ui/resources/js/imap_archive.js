/******************************************************************************
Copyright (C) 2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File  : imap_archive.js                                             //
//     - Desc  : IMAP Archive javascript functions File                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

function redraw(repeatKind) {
  switch(repeatKind) {
    case 'DAILY':
      hideByClassName('class_weekly');
      hideByClassName('class_monthly');
      hideByClassName('class_yearly');
      break;
    case 'WEEKLY':
      showByClassName('class_weekly');
      hideByClassName('class_monthly');
      hideByClassName('class_yearly');
      break;
    case 'MONTHLY':
      hideByClassName('class_weekly');
      showByClassName('class_monthly');
      hideByClassName('class_yearly');
      break;
    case 'YEARLY':
      hideByClassName('class_weekly');
      hideByClassName('class_monthly');
      showByClassName('class_yearly');
      break;
    default:
      hideByClassName('class_weekly');
      hideByClassName('class_monthly');
      hideByClassName('class_yearly');
  }
}

function hideByClassName(className) {
  var elements = $$('*.' + className);
  elements.each(function(element) {
    element.setStyle('visibility', 'hidden');
  });
}

function showByClassName(className) {
  var elements = $$('*.' + className);
  elements.each(function(element) {
    element.setStyle('visibility', 'visible');
  });
}
    
function enableConfiguration(enabled) {
  var elements = $$('*.configuration');
  elements.each(function(element) {
    element.disabled = !enabled;
  });
}

function nextTreatmentDate() {
  var activation = $('activation').get('checked');
  var repeatKind = $('repeat_kind').get('value');
  var dayOfWeek = $('day_of_week').get('value');
  var dayOfMonth = $('day_of_month').get('value');
  var dayOfYear = $('day_of_year').get('value');
  var hour = $('hour').get('value');
  var minute = $('minute').get('value');
    
  var configuration = {};
  configuration.enabled = (activation) ? 1 : 0;
  configuration.repeatKind = repeatKind;
  configuration.dayOfWeek = dayOfWeek;
  configuration.dayOfMonth = dayOfMonth;
  configuration.dayOfYear = dayOfYear;
  configuration.hour = hour;
  configuration.minute = minute;
    
  new Request.JSON({
    url: obm.vars.consts.obmUrl+'/imap_archive/imap_archive_index.php',
    secure: false,
    async: true,
    onFailure: function (response) {
      Obm.Error.parseStatus(this);
    },
    onComplete: function(response) {
      $('nextTreatmentDate').set('text', response + " UTC");
    }
  }).get({ajax : 1, action : 'next_treatment_date', 'configuration' : configuration});
}

function loadLogs() {
  var runId = $('runId').get('text');
  
  var run = {};
  run.id = runId;
  
  new Request.JSON({
    url: obm.vars.consts.obmUrl+'/imap_archive/imap_archive_index.php',
    secure: false,
    async: true,
    onFailure: function (response) {
      Obm.Error.parseStatus(this);
    },
    onComplete: function(response) {
//      $('archivingLogs').set('text', response);
    }
  }).get({ajax : 1, action : 'archiving_logs', 'run' : run});
}

function addMailingEmail() {
  var validity = $('mailing_email').validity;
  if (!validity.valid) {
	  return;
  }
  
  var mailingEmail = $('mailing_email').get('value');
  if (mailingEmail) {
    var mailingEmails = $('mailing_emails');
    if (!mailingEmails.rows.namedItem(mailingEmail)) {
      insertNewRow(mailingEmails, mailingEmail);
    }
  }
}

function insertNewRow(table, value) {
  var row = table.insertRow(-1);
  row.id = value;
  
  var valueCell = row.insertCell(0);
  var divId = new Element('div');
  divId.appendText(value);
  divId.inject(valueCell);
  var input = new Element('input', {
      value: value,
      name: 'sel_mailing_emails[]',
      type: 'hidden'
    });
  input.inject(divId);
  
  var deleteButtonCell = row.insertCell(1);
  
  var removeLink = new Element('a', {
      href: 'javascript: deleteMailingEmail("' + value + '");'
  });
  var removeImage = new Element('img', {
      alt: '[Delete]',
      src: '/images/themes/default/images/ico_trash.gif'
    });
  removeImage.inject(removeLink);
  
  removeLink.inject(deleteButtonCell);
}

function deleteMailingEmail(mailingEmail) {
  if (mailingEmail) {
    var mailingEmails = $('mailing_emails');
    var row = mailingEmails.rows.namedItem(mailingEmail);
    if (row) {
    	mailingEmails.deleteRow(row.rowIndex - 1);
    }
  }
}
