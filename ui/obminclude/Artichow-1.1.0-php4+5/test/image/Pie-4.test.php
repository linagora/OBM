<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../Pie.class.php";

function callbackLabel($value) {
	return ".:: *** ::.\n".$value."\n.:: *** ::.";
}

$graph = new Graph(400, 300);

$x = array();

for($i = 0; $i < 9; $i++) {
	$x[] = mt_rand(20, 100);
}

$plot = new Pie($x);
$plot->setSize(0.6, 0.6);
$plot->setCenter(0.4, 0.5);

$plot->legend->shadow->setSize(4);
$plot->legend->setPadding(10, 10, 10, 10);
$plot->legend->setTextMargin(8, 0);
$plot->legend->setPosition(1.45, 0.50);
$plot->legend->setAlign(LEGEND_RIGHT);

$plot->setLegend(array(
	'Un', 'Deux', 'Trois', 'Quatre', 'Cinq', 'Six', 'Sept', 'Huit', 'Neuf'
));

$plot->setLabelNumber(6);
$plot->setLabelMinimum(10);

$plot->label->setCallbackFunction('callbackLabel');
$plot->label->setFont(new Tuffy(8));

$graph->add($plot);
$graph->draw();
?>