<?php
require(dirname(__file__)."/../lib/update_object.inc");

class HashEventExtId extends UpdateObject {
    public function main() {
        $con = $this->get_con();
        $con->begin();
        try {
            $con->lock_table_for_writing('opush_event_mapping');
            $con->query('ALTER TABLE opush_event_mapping '.
                'ADD COLUMN event_ext_id_hash BYTEA');
            $con->query('SELECT DISTINCT event_ext_id FROM opush_event_mapping');
            $event_ext_ids = array();
            while ($con->next_record()) {
                $event_ext_ids[]= $con->f('event_ext_id');
            }
            foreach ($event_ext_ids as $event_ext_id) {
                $event_ext_id_hash = sha1($event_ext_id, true);
                $update_query = "UPDATE opush_event_mapping ".
                    "SET event_ext_id_hash='".pg_escape_bytea(
                        $event_ext_id_hash)."' ".
                    "WHERE event_ext_id='".pg_escape_string($event_ext_id)."'";
                $con->query($update_query);
            }

            $con->query('ALTER TABLE opush_event_mapping '.
                'ALTER COLUMN event_ext_id_hash SET NOT NLL');

            $con->query('CREATE UNIQUE INDEX '.
                'opush_event_mapping_device_id_event_ext_id_fkey '.
                'ON opush_event_mapping (device_id, event_ext_id_hash)');

            $con->unlock_tables();
            $con->commit();
        }
        catch(Exception $ex) {
            fwrite(STDERR, "An error happened during the update, a rollback ".
                "will be attempted.");
            $con->unlock_tables();
            $con->rollback();
            throw $ex;
        }
    }
}

$update = new HashEventExtId();
$update->main();
?>
