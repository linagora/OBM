This email was automatically sent by OBM
------------------------------------------------------------------
NEW APPOINTMENT
------------------------------------------------------------------

You are invited to participate to an appointment

from     : <?php echo $start; ?>

to       : <?php echo $end; ?>

subject  : <?php echo $title; ?>

location : <?php echo $location; ?>

author   : <?php echo $auteur; ?>


:: More information about this appointment : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>


:: To accept this appointment : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this appointment : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED
