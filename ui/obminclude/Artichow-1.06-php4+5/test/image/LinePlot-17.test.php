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
$group->setXAxisZero(FALSE);
$group->grid->setType(LINE_DASHED);

$group->setBackgroundColor(new Color(197, 180, 210, 80));

$group->setPadding(40, NULL, 20, NULL);

$group->axis->left->setLabelNumber(8);
$group->axis->left->setLabelPrecision(1);
$group->axis->left->setTickStyle(TICK_OUT);

$x = array(NULL);

for($i = 1; $i < 10; $i++) {
	$x[] = cos($i * M_PI / 8) - 1;
}

$plot = new LinePlot($x);
$plot->setXAxis(PLOT_TOP);
$plot->setColor(new Color(40, 40, 150, 10));
$plot->setFillColor(new Color(40, 40, 150, 90));

$plot->mark->setType(MARK_BOOK);
$plot->mark->move(mt_rand(0, 10), mt_rand(0, 10));

$group->add($plot);

$x = array(NULL, NULL, NULL);

for($i = 8; $i < 14; $i++) {
	$x[] = (cos($i * M_PI / mt_rand(2, 8))) / 2 - 0.2;
}

$x[] = NULL;

$plot = new LinePlot($x);
$plot->setXAxis(PLOT_TOP);
$plot->setColor(new Color(120, 120, 30, 10));
$plot->setFillColor(new Color(120, 120, 30, 90));

$group->add($plot);

$graph->add($group);
$graph->draw();
?>