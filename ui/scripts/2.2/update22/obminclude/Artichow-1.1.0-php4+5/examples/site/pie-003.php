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
$graph->setAntiAliasing(TRUE);

$graph->title->set("Customized colors");
$graph->title->setFont(new Tuffy(12));
$graph->title->move(80, 10);

$values = array(16, 9, 13, 23);
$colors = array(
	new LightOrange,
	new LightPurple,
	new LightBlue,
	new LightRed,
	new LightPink
);

$plot = new Pie($values, $colors);
$plot->setCenter(0.3, 0.53);
$plot->setAbsSize(200, 200);
$plot->setBorderColor(new White);
$plot->setStartAngle(234);

$plot->setLegend(array(
	'Arthur',
	'Abel',
	'Pascal',
	'Thamer'
));

$plot->setLabelPosition(-40);
$plot->label->setPadding(2, 2, 2, 2);
$plot->label->setFont(new Tuffy(7));
$plot->label->setBackgroundColor(new White(60));

$plot->legend->setPosition(1.38);

$graph->add($plot);
$graph->draw();

?>