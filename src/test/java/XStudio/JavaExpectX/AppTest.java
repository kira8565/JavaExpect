package XStudio.JavaExpectX;

import com.nemo.javaexpect.shell.Shell;
import com.nemo.javaexpect.shell.driver.SshDriver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testSSH() {
		SshDriver driver = new SshDriver("127.0.0.1", "root", "8565", "#");
		Shell shell = driver.open();
		shell.execute("ls");
	}
}
