<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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



/**
 * Represents an attendee of a calendar event
 * 
 * It is a property containing a CAL-ADDRESS value, with additional parameters
 * regarding the organizer property
 * 
 * Example : ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;
 *              CN="J. Doe";RSVP=TRUE:mailto:jdoe@example.com
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
  
class Vpdi_Icalendar_Attendee extends Vpdi_Icalendar_Organizer {
  /**
   * Status of the attendee's participation
   * 
   * @var string
   */
  public $partstat;
  
  /**
   * Indicates whether the favor of a reply is requested
   * 
   * @var boolean
   */
  public $rsvp;
  
  /**
   * Type of calendar user
   * 
   * @var string
   */
  public $cutype;
  
  /**
   * Groups that the attendee belongs to
   * 
   * @var string
   */
  public $member;
  
  /**
   * Indicates that the original request was delegated to
   * 
   * @var string
   */
  public $delegatedTo;
  
  /**
   * Indicates whom the original request was delegated from
   * 
   * @var string
   */
  public $delegatedFrom;
  
  protected $propName = 'ATTENDEE';
  
  public static function decode(Vpdi_Property $ATTENDEE) {
    $org = new Vpdi_Icalendar_Attendee($ATTENDEE->value());
    $org->decodeParameters($ATTENDEE);
    return $org;
  }
  
  public function __construct($uri = '', $cn = null) {
    parent::__construct($uri, $cn);
    $this->paramMapping+= array(
      'partstat' => array('PARTSTAT', 'ParamText'),
      'rsvp' => array('RSVP', 'Boolean'),
      'cutype' => array('CUTYPE', 'ParamText'),
      'member' => array('MEMBER', 'TextList'), // a ParamValueList could be better...
      'delegatedTo' => array('DELEGATED-TO', 'TextList'),
      'delegatedFrom' => array('DELEGATED-FROM', 'TextList')
    );
  }
}