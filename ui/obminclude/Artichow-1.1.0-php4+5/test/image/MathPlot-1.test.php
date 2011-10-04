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

$plot = new MathPlot(-5, 5, 5, -5);
$plot->grid->hide(FALSE);
$plot->grid->setType(LINE_DOTTED);
$plot->setInterval(mt_rand(1, 10) / 10);

function x2($x) {
	return $x * $x;
}

function random($x) {
	return mt_rand(-3, 3);
}

$function = new MathFunction('x2');
$plot->add($function, "f(x) = x * x");

$function = new MathFunction('random');
$function->setColor(new Orange);
$plot->add($function);

$function = new MathFunction('sqrt', 0);
$function->setColor(new DarkBlue);
$function->line->setThickness(3);
$plot->add($function, "f(x) = sqrt(x)");

$function = new MathFunction('sin');
$function->setColor(new DarkGreen);
$function->line->setStyle(LINE_DASHED);
$plot->add($function, "f(x) = sin(x)");

$plot->legend->setPosition(NULL, 0.85);

$graph->add($plot);
$graph->draw();
?>