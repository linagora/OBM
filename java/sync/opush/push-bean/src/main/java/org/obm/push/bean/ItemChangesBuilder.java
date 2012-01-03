package org.obm.push.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ItemChangesBuilder implements Builder<List<ItemChange>> {
	
	private final List<Builder<ItemChange>> builders;
	
	public ItemChangesBuilder() {
		builders = new ArrayList<Builder<ItemChange>>();
	}
	
	public ItemChangesBuilder addItemChange(ItemChangeBuilder itemChangeBuilder) {
		builders.add(itemChangeBuilder);
		return this;
	}
	
	@Override
	public List<ItemChange> build() {
		return Lists.transform(builders, new Function<Builder<ItemChange>, ItemChange>() {
			@Override
			public ItemChange apply(Builder<ItemChange> input) {
				return input.build();
			}
		});
	}
}