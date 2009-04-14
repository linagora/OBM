This email was automatically sent by OBM
------------------------------------------------------------------
UPDATED APPOINTMENT!
------------------------------------------------------------------

The appointment <?php echo $title; ?>, initially scheduled from <?php echo $old_start; ?> to <?php echo $old_end; ?>, (location : <?php echo $old_location; ?>),
was updated and will take place from <?php echo $start; ?> to <?php echo $end; ?>, (location : <?php echo $location; ?>).

:: More information about this update : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>


:: To accept this update :
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this update : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED
