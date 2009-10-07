<?php
require_once "../Pattern.class.php";
require_once "../Graph.class.php";

$graph = new Graph(400, 200);

// Set title
$graph->title->set('Pattern 2');
$graph->title->setFont(new Tuffy(12));
$graph->title->setColor(new DarkRed);

$pattern = Pattern::get('LightLine');
$pattern->setArgs(array(
	'y' => array(5, 3, 4, 7, 6, 5, 8, 4, 7),
	'legend' => 'John Doe'
));

$plot = $pattern->create();

$graph->add($plot);
$graph->draw();

?>