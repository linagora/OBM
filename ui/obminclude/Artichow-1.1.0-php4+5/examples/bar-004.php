<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../BarPlot.class.php";

$graph = new Graph(400, 400);
$graph->title->set('Two bars with depth');

$group = new PlotGroup;
$group->setPadding(NULL, NULL, 35, NULL);
$group->setSpace(5, 5, NULL, NULL);

$group->grid->hide(TRUE);

$values = array(1, 7, 2, 10, 6, 3, 4, 7);

$plot = new BarPlot($values, 1, 1, 5);
$plot->setBarColor(new LightBlue(25));
$group->add($plot);

$values = array(12, 8, 13, 2, 4, 8, 4, 3);

$plot = new BarPlot($values, 1, 1, 0);
$plot->setBarColor(new LightRed(25));
$group->add($plot);

$graph->add($group);
$graph->draw();

?>