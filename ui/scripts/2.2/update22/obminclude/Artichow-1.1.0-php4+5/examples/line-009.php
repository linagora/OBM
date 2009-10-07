<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";


$graph = new Graph(400, 200);
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setXAxisZero(FALSE);
$group->grid->setType(LINE_DASHED);

$group->setBackgroundColor(new Color(197, 180, 210, 80));

$group->setPadding(40, NULL, 20, NULL);

$group->axis->left->setLabelNumber(8);
$group->axis->left->setLabelPrecision(1);
$group->axis->left->setTickStyle(TICK_IN);
$group->axis->left->label->move(-4, 0);

$group->axis->bottom->setTickStyle(TICK_OUT);
$group->axis->bottom->label->move(0, 4);

$x = array();

for($i = 0; $i < 15; $i++) {
	$x[] = cos($i * M_PI / 5);
}

$plot = new LinePlot($x);
$plot->setColor(new Color(40, 40, 150, 10));
$plot->setFillColor(new Color(40, 40, 150, 90));

$group->add($plot);
$group->legend->add($plot, "Ligne #1", LEGEND_LINE);

$x = array();

for($i = 5; $i < 15; $i++) {
	$x[] = (cos($i * M_PI / 5)) / 2;
}

$plot = new LinePlot($x);
$plot->setColor(new Color(120, 120, 30, 10));
$plot->setFillColor(new Color(120, 120, 30, 90));

$group->add($plot);
$group->legend->add($plot, "Ligne #2", LEGEND_LINE);

$group->legend->setTextFont(new Tuffy(8));
$group->legend->shadow->setSize(0);
$group->legend->setSpace(12);
$group->legend->setBackgroundColor(new Color(255, 255, 255));
$group->setPadding(NULL, 100, NULL, NULL);

$graph->add($group);
$graph->draw();
?>