<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
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
?>
This email was automatically sent by OBM
------------------------------------------------------------------
NEW RESOURCE RESERVATION !
------------------------------------------------------------------

A new resource reservation was scheduled

from     : <?php echo $start; ?>

to       : <?php echo $end; ?>

subject  : <?php echo $title; ?>

location : <?php echo $location; ?>

author   : <?php echo $auteur; ?>


:: To accept or refuse this reservation :
<?php echo $this->host; ?>/calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>
