<?php
require_once "../Pattern.class.php";
require_once "../Graph.class.php";

$graph = new Graph(300, 200);

// Set title
$graph->title->set('Pattern 1');
$graph->title->move(100, 0);
$graph->title->setFont(new Tuffy(9));
$graph->title->setColor(new DarkRed);

$pattern = Pattern::get('BarDepth');
$pattern->setArgs(array(
	'yForeground' => array(5, 3, 4, 7, 6, 5, 8, 4, 7, NULL, NULL),
	'yBackground' => array(NULL, NULL, 4, 5, 6, 4, 2, 3, 7, 5, 4),
	'legendForeground' => '2003',
	'legendBackground' => '2004'
));

$group = $pattern->create();

$graph->add($group);
$graph->draw();

?>