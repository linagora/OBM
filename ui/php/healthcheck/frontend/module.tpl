<div class="accordion-group">
	<div class="accordion-heading test-info">
		<a class="accordion-toggle" data-toggle="collapse" data-parent="#modules" href="#{{moduleName}}-inner">
			{{moduleName}}<span class="label label-info pull-right" id="{{moduleName}}-status">Running</span>
		</a>
	</div>
	<div id="{{moduleName}}-inner" class="accordion-body collapse in">
		<div class="accordion-inner">
			<div class="accordion" id="{{moduleName}}-tests">
				{{testsList}}
			</div>
		</div>
	</div>
</div>