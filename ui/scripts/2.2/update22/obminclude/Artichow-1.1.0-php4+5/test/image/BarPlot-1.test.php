<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";


$graph = new Graph(400, 300/*, "Albert", time() + mt_rand(2, 15)*/);
$graph->setTiming(TRUE);
$graph->setAntiAliasing(TRUE);

$x = array();

for($i = 0; $i < 7; $i++) {
	$x[] = mt_rand(-20, 100);
}

$y = array(
	'Lundi',
	'Mardi',
	'Mercredi',
	'Jeudi',
	'Vendredi',
	'Samedi',
	'Dimanche'
);

$plot = new BarPlot($x);

$plot->setSpace(6, 6, 10, 10);

$plot->setBarColor(new Color(100, 100, 100, 50));
$plot->setBarPadding(mt_rand(0, 30) / 100, mt_rand(0, 30) / 100);

$plot->label->hideFirst(TRUE);
$plot->label->hideLast(TRUE);
$plot->label->set($x);
$plot->label->move(0, -23);
$plot->label->setBackgroundGradient(new LinearGradient(new Color(250, 250, 250, 10), new Color(255, 200, 200, 30), 0));
$plot->label->border->setColor(new Color(20, 20, 20, 20));
$plot->label->setPadding(0, 0, 0, 1);
$plot->label->setFont(new Tuffy(10));

$plot->setBackgroundGradient(new LinearGradient(new Color(210, 210, 210), new Color(255, 255, 255), 0));

$plot->grid->setBackgroundColor(new Color(235, 235, 180, 60));

$plot->yAxis->setLabelPrecision(2);
$plot->yAxis->label->setFont(new Tuffy(8));

$plot->xAxis->setLabelText($y);
$plot->xAxis->label->setFont(new Tuffy(8));
$plot->xAxis->label->setAngle(mt_rand(0, 90));

$plot->legend->add($plot, "Test", LEGEND_BACKGROUND);
$plot->legend->setModel(LEGEND_MODEL_BOTTOM);
$plot->legend->setPadding(10, 10, 10, 10);
$plot->legend->setPosition(NULL, 0.85);
$plot->legend->setTextMargin(8, 0);

$graph->add($plot);
$graph->draw();
?>