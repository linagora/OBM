<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";
require_once "../../LinePlot.class.php";

$graph = new Graph(450, 400);

$graph->setAntiAliasing(TRUE);

$blue = new Color(150, 150, 230, 50);
$red = new Color(240, 50, 50, 25);

$group = new PlotGroup;
$group->setSpace(5, 5, 5, 0);
$group->setBackgroundColor(
	new Color(240, 240, 240)
);

$values = array(18, 12, 14, 21, 11, 7, 9, 16, 7, 23);

$plot = new BarPlot($values);
$plot->setBarColor($red);

$group->add($plot);

$values = array(12, 8, 6, 12, 7, 5, 4, 9, 3, 12);

$plot = new LinePlot($values, LINEPLOT_MIDDLE);
$plot->setFillColor($blue);

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setSize(7);
$plot->mark->setFill(new Color(255, 255, 255));
$plot->mark->border->show();

$group->add($plot);

$graph->add($group);
$graph->draw();
?>