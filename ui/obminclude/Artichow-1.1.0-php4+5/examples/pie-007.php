<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../Pie.class.php";


$graph = new Graph(400, 250);
$graph->setAntiAliasing(TRUE);

$graph->title->set("Pie (example 7)");

$values = array(8, 4, 6, 2, 5, 3, 4);

$plot = new Pie($values, PIE_DARK);
$plot->setCenter(0.4, 0.55);
$plot->setSize(0.7, 0.6);
$plot->set3D(5);
$plot->setBorderColor(new White);

$plot->setLegend(array(
	'Mon',
	'Tue',
	'Wed',
	'Thu',
	'Fri',
	'Sat',
	'Sun'
));

$plot->legend->setPosition(1.3);
$plot->legend->setBackgroundColor(new VeryLightGray(30));
$plot->legend->shadow->setPosition(SHADOW_RIGHT_TOP);

$plot->label->setPadding(2, 2, 2, 2);
$plot->label->border->setColor(new Red(60));
$plot->label->setFont(new Tuffy(7));
$plot->label->setBackgroundGradient(
	new LinearGradient(
		new Red(80),
		new White(80),
		0
	)
);
$plot->setLabelPrecision(1);


$graph->add($plot);
$graph->draw();

?>