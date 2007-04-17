<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../Pie.class.php";


$graph = new Graph(300, 175);
$graph->setBackgroundGradient(
	new LinearGradient(
		new VeryLightGray(40),
		new White,
		90
	)
);
$graph->title->set("Arbitrary labels");
$graph->title->setAngle(90);
$graph->title->move(120, NULL);

$values = array(8, 4, 6, 2, 5, 3, 4);

$plot = new Pie($values);
$plot->setCenter(0.45, 0.5);
$plot->setSize(0.55, 0.55 * 300 / 175);

$plot->label->set(array(
	'Arthur', 'Abel', 'Bernard', 'Thierry', 'Paul', 'Gaston', 'Joe'
));

$plot->label->setCallbackFunction(NULL); // We must disable the default callback function
$plot->setLabelPosition(10);

$plot->setLegend(array(
	'ABC',
	'DEF',
	'GHI',
	'JKL',
	'MNO',
	'PQR',
	'STU'
));

$plot->legend->hide(TRUE);

$graph->add($plot);
$graph->draw();

?>