<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../ScatterPlot.class.php";

$graph = new Graph(300, 300);
$graph->title->set('One value!');

$y = array(mt_rand(-42, 42));

$plot = new ScatterPlot($y);
$plot->setPadding(NULL, NULL, 40, 20);

// Set dashed lines on the grid
$plot->grid->setType(LINE_DASHED);

$plot->mark->setSize(mt_rand(0, 60));
$plot->mark->setFill(new Black(mt_rand(0, 50)));

$plot->label->set($y);
$plot->label->setColor(new White);

$graph->add($plot);
$graph->draw();

?>