<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";

// Return a random color
function color($a = NULL) {
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

function formatLabel($value) {
	return sprintf("%.2f", $value);
}

$graph = new Graph(450, 400);
$graph->setAntiAliasing(TRUE);
$graph->title->set("Some lines");

$group = new PlotGroup;
$group->setXAxisZero(FALSE);
$group->setBackgroundColor(new Color(197, 180, 210, 80));

$group->setPadding(40, NULL, 50, NULL);

$group->axis->left->setLabelNumber(8);
$group->axis->left->setLabelPrecision(1);
$group->axis->left->setTickStyle(TICK_OUT);

$group->axis->bottom->setTickStyle(TICK_OUT);

// Display two lines
for($n = 0; $n < 2; $n++) {

	$x = array();
	
	for($i = 0; $i < 10; $i++) {
		$x[] = (cos($i * M_PI / 5)) / ($n + 1);
	}
	
	$plot = new LinePlot($x);
	$plot->setColor(color(10)); // Random line color
	$plot->setFillColor(color(90)); // Random background color

	$plot->label->set($x);
	$plot->label->setBackgroundColor(new Color(220, 234, 230, 25));
	$plot->label->setPadding(1, 0, 0, 0);
	$plot->label->setCallbackFunction("formatLabel");
	$plot->label->setInterval(2);
	
	$group->add($plot);
	$group->legend->add($plot, "Line #".($n + 1), LEGEND_LINE);
	
}

$group->legend->setSpace(12);
$group->legend->setBackgroundColor(new Color(255, 255, 255));
$group->setPadding(NULL, 100, NULL, NULL);

$graph->add($group);
$graph->draw();
?>