<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";


$graph = new Graph(400, 300);
$graph->setAntiAliasing(TRUE);

$x = array(
	1, 2, 5, 0.5, 3, 8, 7, 6, 2, -4
);

$plot = new LinePlot($x);

// Change component padding
$plot->setPadding(10, NULL, NULL, NULL);

// Change component space
$plot->setSpace(5, 5, 5, 5);

// Set a background color
$plot->setBackgroundColor(
	new Color(230, 230, 230)
);

// Change grid background color
$plot->grid->setBackgroundColor(
	new Color(235, 235, 180, 60)
);

// Hide grid
$plot->grid->hide(TRUE);

// Hide labels on Y axis
$plot->yAxis->label->hide(TRUE);

$plot->xAxis->label->setInterval(2);

$plot->label->set($x);
$plot->label->setFormat('%.1f');
$plot->label->setBackgroundColor(new Color(240, 240, 240, 15));
$plot->label->border->setColor(new Color(255, 0, 0, 15));
$plot->label->setPadding(5, 3, 1, 1);

$plot->xAxis->label->move(0, 5);
$plot->xAxis->label->setBackgroundColor(new Color(240, 240, 240, 15));
$plot->xAxis->label->border->setColor(new Color(0, 150, 0, 15));
$plot->xAxis->label->setPadding(5, 3, 1, 1);

$graph->add($plot);
$graph->draw();
?>