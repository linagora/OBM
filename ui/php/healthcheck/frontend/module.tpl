<div class="accordion-group module-visible">
	<div class="accordion-heading test-info">
		<a class="accordion-toggle" data-toggle="collapse" data-parent="#modules" href="#{{id}}-inner">
			{{name}}<span class="label label-info pull-right" id="{{id}}-status">Running</span>
		</a>
	</div>
	<div id="{{id}}-inner" class="accordion-body collapse">
		<div class="accordion-inner">
			<div class="accordion" id="{{name}}-tests">
				{{description}}
				{{#checks}}
				<div class="accordion-group">
					<div class="accordion-heading test-info">
						<a class="accordion-toggle" data-toggle="collapse" data-parent="#{{id}}" href="#{{id}}-test">
						{{name}}<span class="label label-info pull-right" id="{{id}}-status">Running</span>
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