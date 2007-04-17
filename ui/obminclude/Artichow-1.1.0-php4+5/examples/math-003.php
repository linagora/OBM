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

$plot = new MathPlot(-3, 3, 20, -1);
$plot->setInterval(0.2);
$plot->setPadding(NULL, NULL, NULL, 20);

$plot->yAxis->setLabelInterval(4);

$function = new MathFunction('exp');
$function->setColor(new DarkRed);
$function->mark->setType(MARK_SQUARE);
$function->mark->setSize(3);
$function->mark->setFill(new DarkBlue);
$plot->add($function, "f(x) = exp(x)", LEGEND_MARK);

$plot->legend->setPosition(0.4, 0.2);
$plot->legend->setPadding(3, 3, 3, 3, 3);

$graph->add($plot);
$graph->draw();
?>