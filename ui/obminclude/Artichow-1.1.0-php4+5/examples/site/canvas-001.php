<?php

require_once "../../Graph.class.php";

$graph = new Graph(300, 200);

$driver = $graph->getDriver();

$driver->filledRectangle(
	new Color(230, 230, 230, 0),
	new Line(
		new Point(10, 10),
		new Point(200, 150)
	)
);

for($i = 7; $i < 400; $i += 15) {
	$driver->line(
		new Color(0, 0, 0),
		new Line(
			new Point($i, 0 + 50),
			new Point($i, 30 + 50)
		)
	);
}

for($i = 7; $i < 30; $i += 15) {
	$driver->line(
		new Color(0, 0, 0),
		new Line(
			new Point(0, $i + 50),
			new Point(400, $i + 50)
		)
	);
}

$driver->filledRectangle(
	new Color(0, 100, 200, 50),
	new Line(
		new Point(100, 100),
		new Point(280, 180)
	)
);

$debut = new Color(230, 250, 0);
$fin = new Color(255, 255, 255, 100);

$driver->filledEllipse(
	new RadialGradient(
		$debut,
		$fin
	),
	new Point(105, 135),
	90, 90
);

$text = new Text(
	"Artichow !",
	new Tuffy(15),
	new Color(0, 0, 80),
	45
);

$driver->string($text, new Point(210, 75));

$graph->draw();

?>
