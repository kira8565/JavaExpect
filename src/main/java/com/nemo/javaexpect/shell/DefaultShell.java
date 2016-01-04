package com.nemo.javaexpect.shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nemo.javaexpect.shell.driver.Connection;
import com.nemo.javaexpect.shell.exception.CommandTimeoutException;
import com.nemo.javaexpect.shell.exception.ConnectionException;
import com.nemo.javaexpect.shell.exception.MatchFailedException;
import com.nemo.javaexpect.shell.logger.DefaultShellLogger;
import com.nemo.javaexpect.shell.logger.ShellLogger;

public abstract class DefaultShell implements Shell, ShellLogable {
	public final static String SOURCE_PACKAGE_PARENT = "com.nemo.javaexpect";

	private String lastCommand;

	/**
	 * 用于做expect操作的流大小
	 */
	private byte[] b = new byte[4096];

	/**
	 * expect超时时间
	 */
	private final int EXPECT_SLEEP_TIME_WHEN_NOT_FOUND = 10;

	private StringBuffer buff = new StringBuffer();
	private Connection driver;
	private String waitForPrompt;
	private int commandTimeout;

	public DefaultShell(Connection driver, String waitForPrompt,
			int commandTimeout) {
		this.driver = driver;
		this.waitForPrompt = waitForPrompt;
		this.commandTimeout = commandTimeout;
	}

	private void _close() {
		driver.close();
	}

	@Override
	public void close() {
		log("close()", "\nConnection Closed!!!");
		_close();
	}

	private void _send(String command) {
		if (command == null)
			command = "";
		try {
			driver.getOutputStream().write(command.getBytes());
			driver.getOutputStream().write('\r');
			driver.getOutputStream().flush();
		} catch (IOException e) {
			throw new ConnectionException(e.getMessage());
		}
	}

	@Override
	public void send(String command) {
		log("send(\"" + command + "\")", "");
		_send(command);
	}

	@Override
	public CommandResult execute(String command, String waitForPattern) {
		lastCommand = "execute\"(" + command + "\",\"" + waitForPattern + "\")";
		return execute(command, waitForPattern, commandTimeout);
	}

	private CommandResult _execute(String command, String waitForPattern,
			int timeout) {
		_send(command);
		return _expect(waitForPattern, timeout);
	}

	@Override
	public CommandResult execute(String command) {
		lastCommand = "execute(\"" + command + "\")";
		return _execute(command, waitForPrompt, commandTimeout);
	}

	@Override
	public CommandResult expect(String waitForPattern) {
		lastCommand = "expect(\"" + waitForPattern + "\")";
		return _expect(waitForPattern, commandTimeout);
	}

	private CommandResult _expect(String waitForPattern, int timeout) {

		long start = System.currentTimeMillis();
		long end = timeout * 1000 + start;

		Pattern p = Pattern.compile(waitForPattern);
		while (true) {
			InputStream in = driver.getInputStream();
			try {

				while (in != null && in.available() > 0) {
					int length = in.read(b);
					buff.append(new String(b, 0, length));
				}
			} catch (IOException e) {

			}
			Matcher m = p.matcher(buff);
			if (m.find()) {
				int length = m.end();
				String tmp = buff.substring(0, length);
				log(lastCommand, tmp);
				CommandResult r = new DefaultCommandResult(lastCommand, tmp);
				buff.delete(0, length);
				return r;
			}
			if (System.currentTimeMillis() > end) {
				String tmp = buff.toString();
				log(lastCommand, tmp);
				throw new CommandTimeoutException("Expect: " + waitForPattern
						+ "\nCurrent buffer: " + tmp.toString());
			} else {
				try {
					Thread.sleep(EXPECT_SLEEP_TIME_WHEN_NOT_FOUND);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	@Override
	public CommandResult execute(String command, String waitForPattern,
			int timeout) {
		lastCommand = "execute(\"" + command + "\",\"" + waitForPattern + "\","
				+ String.valueOf(timeout) + ")";
		return _execute(command, waitForPattern, timeout);
	}

	@Override
	public CommandResult execute(String command, int timeout) {
		lastCommand = "execute(\"" + command + "\"," + String.valueOf(timeout)
				+ ")";
		return _execute(command, waitForPrompt, timeout);
	}

	@Override
	public CommandResult expect(String waitForPattern, int timeout) {
		lastCommand = "expect(\"" + waitForPattern + "\","
				+ String.valueOf(timeout) + ")";
		return _expect(waitForPattern, timeout);
	}

	private CommandResult _getLastExitCode() {
		_send("echo XYZ$?ZYX");
		return _expect("XYZ[0-9]*ZYX", commandTimeout);
	}

	@Override
	public CommandResult getLastExitCode() {
		lastCommand = "\"getLastExitCode()\"";

		CommandResult r = _getLastExitCode();
		Pattern pattern = Pattern.compile("XYZ($?)ZYX");
		Matcher matcher = pattern.matcher(r.getCommandResult());
		if (matcher.find()) {
			int ret = Integer.parseInt(matcher.group(1));
			return new DefaultCommandResult("getLastExitCode()", ret);
		} else {
			throw new MatchFailedException(
					"Expect the pattern XYZ($?)ZYX, but actual value is "
							+ r.getCommandResult());
		}
	}

	public static String getStackTrace() {
		Throwable e = new Throwable();
		for (StackTraceElement el : e.getStackTrace()) {
			String str = el.toString();
			if (!str.contains(SOURCE_PACKAGE_PARENT))
				return str;
		}
		return "";

	}

	@Override
	public void log(String command, String result) {
		ShellLogger logger = this.getLogger();
		if (logger != null) {
			String id = this.getShellId();
			String trace = getStackTrace();
			logger.log(trace, id, command, result);
		}
	}
}
