<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

function color() {
	return new Color(mt_rand(0, 255), mt_rand(0, 255), mt_rand(0, 255), mt_rand(0, 100));
}

$width = mt_rand(200, 400);

$graph = new Graph($width, 400);
$graph->setAntiAliasing(TRUE);
$graph->setBackgroundColor(new Color(50, 50, 50));

$x = array(
	-100,
	35,
	-20,
	15,
	79,
	-50
);

$plot = new LinePlot($x);
$plot->setAbsSize($width - 20, 380);
$plot->setAbsPosition(mt_rand(0, 20), mt_rand(0, 20));

$plot->setThickness(mt_rand(2, 5));
$plot->setBackgroundGradient(new LinearGradient(color(), color(), mt_rand(0, 1) * 90));
$plot->yAxis->setLabelNumber(mt_rand(0, 10));
/*
$plot->setYMin(-80);
$plot->setYMax(120);
*/

$plot->xAxis->setLabelInterval(2);
$plot->xAxis->setTickInterval(2);
$major = $plot->xAxis->tick('major');
$major->setSize(10);
$minor = $plot->xAxis->tick('minor');
$minor->setSize(6);
$plot->xAxis->setNumberByTick('minor', 'major', 4);
$plot->xAxis->label->hideFirst(TRUE);

$plot->grid->setType(LINE_DOTTED);

$plot->label->set($x);
$plot->label->setBackgroundColor(new Color(mt_rand(200, 240), mt_rand(200, 240), mt_rand(200, 240), mt_rand(0, 20)));

$graph->add($plot);
$graph->draw();
?>