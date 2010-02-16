<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

/**
 * Defines overall status possible values for a calendar component
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
  
class Vpdi_Icalendar_Status {
  
  /**
   * For "VEVENT"
   */
  const TENTATIVE = 'TENTATIVE';
  
  const CONFIRMED = 'CONFIRMED';
  
  /**
   * For "VTODO"
   */
  const NEEDS_ACTION = 'NEEDS-ACTION';
  
  const COMPLETED = 'COMPLETED';
  
  const IN_PROCESS = 'IN-PROCESS';
  
  /**
   * For "VJOURNAL"
   */
  const DRAFT = 'DRAFT';
  
  const _FINAL = 'FINAL'; // ....
  
  /**
   * Shared
   */
  const CANCELLED = 'CANCELLED';
}