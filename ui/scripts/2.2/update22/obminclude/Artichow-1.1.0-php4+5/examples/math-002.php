<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../MathPlot.class.php";


$graph = new Graph(300, 300);

// Set graph title
$graph->title->set('f(x) = x * x');
$graph->title->setBackgroundColor(new White(0));
$graph->title->setPadding(NULL, NULL, 10, 10);
$graph->title->move(0, -10);

$plot = new MathPlot(-3, 3, 10, -2);
$plot->setInterval(0.2);
$plot->setPadding(NULL, NULL, NULL, 20);

// Defines x²
function x2($x) {
	return $x * $x;
}

$function = new MathFunction('x2');
$function->setColor(new Orange);
$plot->add($function);

$graph->add($plot);
$graph->draw();
?>