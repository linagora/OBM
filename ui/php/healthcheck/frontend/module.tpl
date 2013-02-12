<div class="accordion-group visibility-hidden" id="{{id}}-header">
	<div class="accordion-heading test-info module" id="{{id}}">
		<a class="accordion-toggle" data-toggle="collapse" data-parent="#{{id}}" href="#{{id}}-inner">
			{{name}}
			<span class="label label-info pull-right">Running</span> 
			<span class="label label-success pull-right">Success</span> 
			<span class="label label-error pull-right">Error</span>
		</a>
	</div>
	<div id="{{id}}-inner" class="accordion-body collapse">
		<div class="accordion-inner">
			<div class="accordion" id="{{name}}-tests">
				{{description}}
				{{#checks}}
				<div class="accordion-group visibility-hidden" id="{{id}}-header">
					<div class="accordion-heading test-info check" id="{{id}}">
						<a class="accordion-toggle" data-toggle="collapse" data-parent="#{{id}}" href="#{{id}}-test">
						{{name}}
						<span class="label label-info pull-right">Running</span> 
						<span class="label label-success pull-right">Success</span> 
						<span class="label label-error pull-right">Error</span>
						</a>
					</div>
					<div id="{{id}}-test" class="accordion-body collapse">
						<div class="accordion-inner">
						<p>{{description}}</p>
						<span class="pull-right" id="{{id}}-button">
							<a class="btn btn-info btn-small" type="button" href="{{url}}">Documentation</a>
						</span>
						</div>
					</div>
				</div>
				{{/checks}}
			</div>
		</div>
	</div>
</div>