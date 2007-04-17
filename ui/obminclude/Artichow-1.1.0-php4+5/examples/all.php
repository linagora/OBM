<?php
function table($files, $re = NULL) {

	echo "<table cellpadding='4'>";

	foreach($files as $key => $file) {

		if($key%2 == 0) {
			echo "<tr>";
		}

		if($re === NULL or eregi($re, $file)) {
			image($file);
		}

		if($key%2 == 1) {
			echo "</tr>";
		}


	}

	if($key%2 == 0) {
		echo "</tr>";
	}

	echo "</table>";

}

function image($file) {
	echo "<td>
		<h3>".$file."</h3>
		<a href='".$file."'><img src='".$file."' style='border: 0px'/></a>
	</td>";
}
?>
<h2>Artichow examples</h2>
<?php
$glob = glob("*.php");

table($glob, "[a-z]+\-[0-9]{3}\.php");
?>
<h2>Artichow.org examples</h2>
<?php
$glob = glob("site/*.php");

table($glob);
?>
<h2>Artichow tutorials</h2>
<?php
$glob = glob("tutorials/*.php");

table($glob);
?>