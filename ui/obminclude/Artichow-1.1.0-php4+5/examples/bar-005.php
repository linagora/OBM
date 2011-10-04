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

// Set a title to the graph
$graph->title->set('The title');

// Change graph background color
$graph->setBackgroundColor(new Color(230, 230, 230));

$values = array(8, 2, 6, 1, 3, 5);

// Declare a new BarPlot
$plot = new BarPlot($values);

// Reduce padding around the plot
$plot->setPadding(NULL, NULL, NULL, 20);

// Reduce plot size and move it to the bottom of the graph
$plot->setSize(1, 0.96);
$plot->setCenter(0.5, 0.52);

// Set a background color to the plot
$plot->grid->setBackgroundColor(new White);
// Set a dashed grid
$plot->grid->setType(LINE_DASHED);


$plot->label->set($values);
$plot->label->move(0, -10);
$plot->label->setColor(new DarkBlue);

// Set a shadow to the bars
$plot->barShadow->setSize(2);

// Bar size is at 60%
$plot->setBarSize(0.6);

// Change the color of the bars
$plot->setBarColor(
	new Orange(15)
);

// Add the plot to the graph
$graph->add($plot);

// Draw the graph
$graph->draw();

?>