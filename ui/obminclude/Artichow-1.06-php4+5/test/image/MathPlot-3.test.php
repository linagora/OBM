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

$plot = new MathPlot(-3, 3, 6, 0);
$plot->setInterval(0.2);
$plot->setPadding(NULL, NULL, NULL, 20);

$function = new MathFunction('acos', -1, 1);
$function->setColor(new DarkGreen);
$function->mark->setType(MARK_SQUARE);
$function->mark->setSize(4);
$plot->add($function, "f(x) = acos(x)");

$function = new MathFunction('exp');
$function->setColor(new DarkRed);
$function->mark->setType(MARK_CIRCLE);
$function->mark->setSize(7);
$function->mark->setFill(new Blue);
$function->mark->border->show();
$function->mark->border->setColor(new Black);
$function->line->hide(TRUE);
$plot->add($function, "f(x) = exp(x)");

$plot->legend->setPosition(0.4, 0.1);

$graph->add($plot);
$graph->draw();
?>