package org.obm;

import java.io.File;
import java.util.Arrays;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class DependencyResolverHelper {

	public static File[] projectDependencies(File pomFile) {
		return filterObmDependencies(allObmSyncDependencies(pomFile));
	}

	private static MavenResolvedArtifact[] allObmSyncDependencies(File pomFile) {
		return Maven.resolver()
			.offline()
			.loadPomFromFile(pomFile)
			.importRuntimeDependencies()
			.resolve()
			.withTransitivity()
			.asResolvedArtifact();
	}

	private static File[] filterObmDependencies(MavenResolvedArtifact[] allObmSyncDependencies) {
		return FluentIterable.from(Arrays.asList(
				allObmSyncDependencies))
				.filter(obmDependencyPredicate())
				.transform(artifactAsFile()).toArray(File.class);
	}

	private static Function<MavenResolvedArtifact, File> artifactAsFile() {
		return new Function<MavenResolvedArtifact, File>() {
			@Override
			public File apply(MavenResolvedArtifact input) {
				return input.asFile();
			}
		};
	}

	private static Predicate<MavenResolvedArtifact> obmDependencyPredicate() {
		return new Predicate<MavenResolvedArtifact>() {

			@Override
			public boolean apply(MavenResolvedArtifact input) {
				String groupId = input.getCoordinate().getGroupId();
				return !(groupId.startsWith("com.linagora") || groupId.startsWith("org.obm"));
			}
		};
	}
}
