<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../MathPlot.class.php";


$graph = new Graph(400, 400);
$graph->setTiming(TRUE);

$plot = new MathPlot(mt_rand(-10, 0), mt_rand(1, 10), mt_rand(1, 10), mt_rand(-10, 0));
$plot->setInterval(0.05);

$function = new MathFunction('asin', -1, 1);
$function->setColor(new DarkGreen);
$function->line->setStyle(LINE_DASHED); // Don't work as expected
$plot->add($function, "f(x) = asin(x)");

$function = new MathFunction('log', 0.0000001);
$function->setColor(new DarkRed);
$plot->add($function, "f(x) = log(x)");

$plot->legend->setPosition(0.5, 0.85);

$graph->add($plot);
$graph->draw();
?>