<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

// Return a random color
function color($a = NULL) {
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

function formatLabel($value) {
	return sprintf("%.2f", $value);
}

$graph = new Graph(150, 100);

$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setXAxisZero(FALSE);
$group->setBackgroundColor(new Color(197, 180, 210, 80));

$group->setPadding(25, 10, 10, 20);

$group->axis->left->setLabelNumber(2);
$group->axis->left->setLabelPrecision(1);

// Display two lines
for($n = 0; $n < 2; $n++) {

	$x = array();
	
	for($i = 0; $i < 10; $i++) {
		$x[] = (cos($i * M_PI / 5)) / ($n + 1);
	}
	
	$plot = new LinePlot($x);
	$plot->setColor(color(10)); // Random line color
	$plot->setFillColor(color(90)); // Random background color
	
	$group->add($plot);
	
}

$graph->add($group);
$graph->draw();
?>