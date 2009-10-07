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

$plot = new MathPlot(-3, 3, 2, -3);
$plot->setInterval(0.05);

$function = new MathFunction('sqrt', 0);
$plot->add($function, "sqrt(x)");

function x2($x) {
	return - $x * $x;
}

$function = new MathFunction('sin', -2, 2);
$function->setColor(new DarkBlue);
$plot->add($function, "sin(x) (-2 < x < 2)");

$plot->legend->setPosition(0.98, 0.8);
$plot->legend->setTextFont(new Tuffy(8));

$graph->add($plot);
$graph->draw();
?>