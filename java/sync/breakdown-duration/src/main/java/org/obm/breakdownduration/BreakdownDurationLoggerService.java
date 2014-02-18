/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.breakdownduration;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

import org.obm.breakdownduration.bean.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BreakdownDurationLoggerService {
	
	private static Logger logger = LoggerFactory.getLogger("BREAKDOWN");
	
	@VisibleForTesting static ThreadLocal<TreeBuilder> treeBuilder = new ThreadLocal<TreeBuilder>() {

		@Override
		protected TreeBuilder initialValue() {
			return TreeBuilder.create();
		}
	};
	@VisibleForTesting static ThreadLocal<Boolean> recordingEnabled = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	
	@Inject
	@VisibleForTesting BreakdownDurationLoggerService() {}
	
	public void enableRecording() {
		recordingEnabled.set(true);
	}

	public void disableRecording() {
		recordingEnabled.set(false);
	}

	public void startRecordingNode(Group group) {
		if (recordingEnabled.get()) {
			treeBuilder.get().beginNewNode(group);
		}
	}

	public void endRecordingNode(long elapsed) {
		if (recordingEnabled.get()) {
			treeBuilder.get().endCurrentNode(elapsed);
		}
	}

	public void cleanSession() {
		recordingEnabled.remove();
		treeBuilder.remove();
	}
	
	public void log() {
		String log = buildLog();
		if (!Strings.isNullOrEmpty(log)) {
			logger.info(log);
		}
	}

	@VisibleForTesting String buildLog() {
		Root root = treeBuilder.get().build();
		Root normalizedTree = new Normalizer(root).normalize();
		return normalizedTree.serialize();
	}
	
	private static class GroupMerger implements Iterable<Node> {
		
		private final Map<Group, Node> elements;
		
		public GroupMerger() {
			this.elements = Maps.newEnumMap(Group.class);
		}

		public GroupMerger(Iterable<Node> nodes) {
			this();
			addAll(nodes);
		}
		
		public void addAll(Iterable<Node> nodes) {
			for (Node node: nodes) {
				add(node);
			}
		}
		
		public void add(Node node) {
			elements.put(node.group, mergeWithExistingValue(node));
		}
		
		private Node mergeWithExistingValue(Node node) {
			Node merger = elements.get(node.group);
			if (merger != null) {
				return node.merge(merger);
			} else {
				return node;
			}
		}
		
		@Override
		public Iterator<Node> iterator() {
			return elements.values().iterator();
		}
	}
	
	private static class Normalizer {

		private final Predicate<Node> notZeroTimeNodePredicate = new Predicate<Node>() {
			
			@Override
			public boolean apply(Node node) {
				return node.timeElapsedInMs > 0;
			}
		};

		private final Comparator<Node> timeOrdering = new Comparator<Node>() {
			
			@Override
			public int compare(Node lhs, Node rhs) {
				return Ordering.natural().reverse().compare(lhs.timeElapsedInMs, rhs.timeElapsedInMs);
			}
		};
		
		private final Root root;

		public Normalizer(Root root) {
			this.root = root;
		}
		
		public Root normalize() {
			Root.Builder rootBuilder = Root.builder();
			rootBuilder.addChildren(normalize(root.children));
			return rootBuilder.build();
		}
		

		private Iterable<Node> normalize(Iterable<Node> nodes) {
			return sortByDecreasingTime(
					new GroupMerger(
							Iterables.transform(nodes, new Function<Node, Node>() {
								@Override
								public Node apply(Node input) {
									return normalize(input);
								}
							})));
		}

		private Node normalize(Node node) {
			GroupMerger children = new GroupMerger();
			Deque<Node> potentialChildren = 
					Queues.newArrayDeque(FluentIterable.from(node.children)
							.filter(notZeroTimeNodePredicate));
			
			while (!potentialChildren.isEmpty()) {
				Node child = potentialChildren.pop();

				if (node.group.equals(child.group)) {
					//reparent nodes
					potentialChildren.addAll(ImmutableList.copyOf(normalize(child.children)));
				} else {
					children.add(normalize(child));
				}
			}
			return Node.builder()
						.timeElapsedInMs(node.timeElapsedInMs)
						.group(node.group)
						.addChildren(sortByDecreasingTime(children))
						.build();
		}
		
		private Iterable<Node> sortByDecreasingTime(GroupMerger groupMerger) {
			return ImmutableSortedSet.orderedBy(timeOrdering).addAll(groupMerger).build();
		}
		

	}
	
	public static class Root {
		
		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {
			
			private ImmutableList.Builder<Node> children;
			
			private Builder() {
				this.children = ImmutableList.builder();
			}
			
			public Builder addChild(Node child) {
				this.children.add(child);
				return this;
			}
			
			public Builder addChildren(Iterable<Node> children) {
				this.children.addAll(children);
				return this;
			}
			
			public Root build() {
				return new Root(children.build());
			}
		}

		@VisibleForTesting ImmutableList<Node> children;
		
		public Root(ImmutableList<Node> children) {
			this.children = children;
		}
		
		public String serialize() {
			return Joiner.on(", ").skipNulls().join(serializeNodes(children));
		}

	}
	
	public static class Node {

		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {

			@VisibleForTesting Group group;
			@VisibleForTesting ImmutableList.Builder<Node> children;
			@VisibleForTesting Long timeElapsedInMs;
			
			private Builder() {
				this.children = ImmutableList.builder();
			}
			
			public Builder group(Group group) {
				this.group = group;
				return this;
			}
			
			public Builder addChild(Node child) {
				this.children.add(child);
				return this;
			}
			
			public Builder addChildren(Iterable<Node> children) {
				this.children.addAll(children);
				return this;
			}
			
			public Builder timeElapsedInMs(long timeElapsedInMs) {
				this.timeElapsedInMs = timeElapsedInMs;
				return this;
			}
			
			public Node build() {
				Preconditions.checkState(timeElapsedInMs != null);
				Preconditions.checkState(group != null);
				return new Node(group, children.build(), timeElapsedInMs);
			}
		}

		@VisibleForTesting final Group group;
		@VisibleForTesting final ImmutableList<Node> children;
		@VisibleForTesting final long timeElapsedInMs;
		@VisibleForTesting final long owntimeElapsedInMs;
		
		private Node(Group group, ImmutableList<Node> children, long timeElapsedInMs) {
			this.group = group;
			this.children = children;
			this.timeElapsedInMs = timeElapsedInMs;
			this.owntimeElapsedInMs = computeOwnTimeInMs();
		}

		private long computeOwnTimeInMs() {
			long own = timeElapsedInMs;
			for (Node child: children) {
				own -= child.timeElapsedInMs;
			}
			return own;
		}
		
		public Node merge(Node toMerge) {
			return Node.builder().group(group)
				.addChildren(mergeChildren(toMerge.children))
				.timeElapsedInMs(timeElapsedInMs + toMerge.timeElapsedInMs)
				.build();
		}
		
		private Iterable<Node> mergeChildren(Iterable<Node> toMerge) {
			return new GroupMerger(Iterables.concat(children, toMerge));
		}

		public String serialize() {
			StringBuilder stringBuilder = new StringBuilder(serializeGroup(group.name(), timeElapsedInMs)); 
			if (!children.isEmpty()) {
				stringBuilder.append(" (")
					.append(Joiner.on(", ").join(serializeNodes(children)))
					.append(serializeOther())
					.append(")");
			}
			return stringBuilder.toString();
		}

		private String serializeGroup(String group, long timeElapsedInMs) {
			return group + ":" + timeElapsedInMs;
		}
		
		private String serializeOther() {
			if (owntimeElapsedInMs > 0) {
				return serializeGroup(", OTHER", owntimeElapsedInMs);
			}
			return "";
		}
		
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("group", group)
					.add("children", children)
					.add("timeElapsedInMs", timeElapsedInMs)
					.add("owntimeElapsedInMs", owntimeElapsedInMs)
		 			.toString();
		}
	}
	
	public static class TreeBuilder {
		
		public static TreeBuilder create() {
			return new TreeBuilder();
		}
		
		@VisibleForTesting Root.Builder root;
		private ArrayDeque<Node.Builder> nodeStack;
		
		private TreeBuilder() {
			this.nodeStack = Queues.newArrayDeque();
			this.root = Root.builder();
		}

		public TreeBuilder endCurrentNode(long timeElapsedInMs) {
			Preconditions.checkState(!nodeStack.isEmpty());
			
			Node node = nodeStack.pop().timeElapsedInMs(timeElapsedInMs).build();
			
			if (nodeStack.isEmpty()) {
				root.addChild(node);
			} else {
				nodeStack.peek().addChild(node);
			}
			return this;
		}
		
		public TreeBuilder beginNewNode(Group group) {
			Node.Builder newNode = Node.builder().group(group);
			nodeStack.push(newNode);
			return this;
		}
		
		public Root build() {
			return root.build();
		}
	}
	
	private static Iterable<String> serializeNodes(Iterable<Node> nodes) {
		return Iterables.transform(nodes, new Function<Node, String>() {
				@Override
				public String apply(Node input) {
					return input.serialize();
				}
			});
	}

}