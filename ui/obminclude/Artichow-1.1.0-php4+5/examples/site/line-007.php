<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(300, 200);

$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->grid->setType(LINE_DASHED);

$group->setPadding(40, NULL, 20, NULL);

$group->axis->left->setLabelNumber(8);
$group->axis->left->setLabelPrecision(1);
$group->axis->left->setTickStyle(TICK_OUT);

$x = array(2, 4, 8, 16, 32, 48, 56, 60, 62);

$plot = new LinePlot($x);
$plot->setColor(new Orange());
$plot->setFillColor(new LightOrange(80));

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->setFill(new MidRed);
$plot->mark->setSize(6);

$group->legend->add($plot, "John", LEGEND_MARK);
$group->add($plot);

$x = array(NULL, NULL, NULL, 10, 12, 14, 18, 26, 42);

$plot = new LinePlot($x);
$plot->setColor(new Color(120, 120, 30, 10));
$plot->setFillColor(new Color(120, 120, 60, 90));

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setFill(new DarkGreen);
$plot->mark->setSize(5);

$group->add($plot);

function setYear($value) {
	return $value + 2000;
}

$group->axis->bottom->label->setCallbackFunction('setYear');

function setK($value) {
	return round($value).'K';
}

$group->axis->left->label->setCallbackFunction('setK');

$group->legend->add($plot, "George", LEGEND_MARK);
$group->legend->setPosition(0.45, 0.25);
$group->legend->shadow->smooth(TRUE);

$graph->add($group);

$graph->draw();
?>