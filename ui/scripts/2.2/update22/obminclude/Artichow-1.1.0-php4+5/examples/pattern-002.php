<?php
require_once "../Pattern.class.php";
require_once "../Graph.class.php";

$graph = new Graph(300, 200);

$graph->title->set('Customized pattern 1');
$graph->title->setFont(new Tuffy(12));

$pattern = Pattern::get('BarDepth');
$pattern->setArgs(array(
	'yForeground' => array(5, 3, 4, 7, 6, 5, 8, 4, 7, NULL, NULL),
	'yBackground' => array(NULL, NULL, 4, 5, 6, 4, 2, 3, 7, 5, 4),
	'colorForeground' => new Color(230, 230, 230),
	'colorBackground' => new Color(250, 90, 90)
));

$group = $pattern->create();
$group->legend->setPosition(0.5, 0.78);

$graph->add($group);
$graph->draw();

?>