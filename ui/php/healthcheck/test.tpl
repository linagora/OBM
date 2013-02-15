<div class="accordion-group">
	<div class="accordion-heading test-success">
		<a class="accordion-toggle" data-toggle="collapse" data-parent="#{{test-name}}" href="#{{test-name}}-test">
		{{test-name}}<span class="label label-info pull-right" id="{{test-name}}-status">Running</span>
		</a>
	</div>
	<div id="{{test-name}}-test" class="accordion-body collapse">
		<div class="accordion-inner">
		<p>{{testDescription}}</p>
		<span class="pull-right" id="{{test-name}}-button">
			<button class="btn btn-info btn-small" type="button" >Documentation</button>
		</span>
		</div>
	</div>
</div>