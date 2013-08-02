package org.obm.dao.utils;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A base class for Rules (like TemporaryFolder) that set up an external
 * resource before a test (a file, socket, server, database connection, etc.),
 * and guarantee to tear it down afterward:
 *
 * <pre>
 * public static class UsesExternalResource {
 *  Server myServer= new Server();
 *
 *  &#064;Rule
 *  public ExternalResource resource= new ExternalResource() {
 *      &#064;Override
 *      protected void before() throws Throwable {
 *          myServer.connect();
 *         };
 *
 *      &#064;Override
 *      protected void after() {
 *          myServer.disconnect();
 *         };
 *     };
 *
 *  &#064;Test
 *  public void testFoo() {
 *      new Client().run(myServer);
 *     }
 * }
 * </pre>
 *
 * @since 4.7
 */
public abstract class MethodExternalResource implements MethodRule {

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return statement(base);
	}

	private Statement statement(final Statement base) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				try {
					base.evaluate();
				} finally {
					after();
				}
			}
		};
	}

	/**
	 * Override to set up your specific external resource.
	 *
	 * @throws if setup fails (which will disable {@code after}
	 */
	@SuppressWarnings("unused")
	protected void before() throws Throwable {
		// do nothing
	}

	/**
	 * Override to tear down your specific external resource.
	 */
	protected void after() {
		// do nothing
	}
}
