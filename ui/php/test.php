<?php 
/*Only one profiled function within*/
function oneDoThousandArray() {
  doThousandArray();
}

function doThousandArray() {
  for($i=0; $i<1000;$i++) {
    $test = array(); 
  }
}
/*1000 profiled function within*/
function oneThoushandDoArray() {
  for($i=0; $i<1000;$i++) {
    doArray();
  }
}

function doArray() {
  $test = array(); 
}

oneDoThousandArray();
oneThoushandDoArray();
?>
