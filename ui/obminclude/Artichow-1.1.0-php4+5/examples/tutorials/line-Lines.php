<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

$graph = new Graph(450, 400);

$graph->setAntiAliasing(TRUE);

$blue = new Color(0, 0, 200);
$red = new Color(200, 0, 0);

$group = new PlotGroup;
$group->setBackgroundColor(
	new Color(240, 240, 240)
);
$group->setPadding(40, 40);

$values = array(12, 5, 20, 32, 15, 4, 16);

$plot = new LinePlot($values);
$plot->setColor($blue);
$plot->setYAxis(PLOT_LEFT);

$group->add($plot);

$group->axis->left->setColor($blue);
$group->axis->left->title->set("Blue line");

$values = array(6, 12, 14, 2, 11, 5, 21);

$plot = new LinePlot($values);
$plot->setColor($red);
$plot->setYAxis(PLOT_RIGHT);

$group->add($plot);

$group->axis->right->setColor($red);
$group->axis->right->title->set("Red line");

$graph->add($group);
$graph->draw();
?>