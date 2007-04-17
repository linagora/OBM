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

$plot = new MathPlot(-3, 3, 3, -3);
$plot->setInterval(0.1);

$function = new MathFunction('cos');
$plot->add($function);

$graph->add($plot);
$graph->draw();
?>