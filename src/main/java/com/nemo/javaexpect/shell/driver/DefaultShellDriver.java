package com.nemo.javaexpect.shell.driver;

import java.io.InputStream;

import com.nemo.javaexpect.shell.DefaultShell;
import com.nemo.javaexpect.shell.Shell;
import com.nemo.javaexpect.shell.core.DefaultTarget;
import com.nemo.javaexpect.shell.core.Target;
import com.nemo.javaexpect.shell.exception.NemoException;
import com.nemo.javaexpect.shell.logger.DefaultShellLogger;
import com.nemo.javaexpect.shell.logger.ShellLogger;
import com.nemo.javaexpect.term.VT100InputStream;

public abstract class DefaultShellDriver implements ShellDriver {
	private boolean skipVT100Filter = false;

	/**
	 * 
	 * @param flag
	 *            如果设置为true，则不过滤vt100控制字符 设置为false，则过滤vt100控制字符
	 */
	public void setSkipVT100Filter(boolean flag) {
		skipVT100Filter = flag;
	}

	protected InputStream createVT100Filter(InputStream in) {
		if (skipVT100Filter) {
			return in;
		}
		return new VT100InputStream(in);
	}

	protected Target target;
	private static ShellLogger logger = new DefaultShellLogger();

	public static ShellLogger getShellLogger() {
		return logger;
	}

	public static void setShellLogger(ShellLogger logger) {
		DefaultShellDriver.logger = logger;
	}

	protected void log(String id, String command, String output) {
		ShellLogger logger = DefaultShellDriver.logger;
		if (logger != null) {
			logger.log(DefaultShell.getStackTrace(), id, command, command
					+ output);
		}
	};

	private static Target createDefaultTarget() {
		Target target = new DefaultTarget();
		return target;
	}

	protected void startVT100FilterThread(InputStream in) {
		if (in != null && in instanceof VT100InputStream) {
			Thread thread = new Thread((Runnable) in);
			thread.setDaemon(true);
			thread.start();
		}
	}

	protected void assertTargetParameters() {
		if (target.isAutoLogin()) {
			if (target.getLoginName() == null) {
				throw new NemoException(
						"loginName can't be null when autoLogin");
			}

			if (target.getLoginPrompt() == null) {
				throw new NemoException(
						"loginPrompt can't be null when autoLogin");
			}
		}

		if (target.isAutoSU() == true) {
			if (target.getSuPrompt() == null) {
				throw new NemoException("suPrompt can't be null when autoSU");
			}
		}
	}

	protected abstract Shell createShell();

	protected void autoLogin(Shell shell) {
		shell.expect(".*ogin:");
		shell.send(target.getLoginName());

		shell.expect(".*assword:");
		shell.send(target.getLoginPassword());

		// send a enter char
		if (target.isInitialCR()) {
			shell.send("");
		}
		shell.expect(target.getLoginPrompt());
	}

	protected void autoSU(Shell shell) {
		String suCommand;
		String suName = target.getSuName() == null ? "root" : target
				.getSuName();

		if (target.isSUWithLoginShell())
			suCommand = "su - " + suName;
		else
			suCommand = "su " + suName;
		shell.send(suCommand);
		shell.expect(".*assword:");
		shell.send(target.getSuPassword());
		shell.expect(target.getSuPrompt());
	}

	@Override
	public Shell open() {
		assertTargetParameters();

		Shell shell = createShell();

		try {

			if (target.isAutoLogin()) {
				autoLogin(shell);
			}

			if (target.isAutoSU()) {
				autoSU(shell);
			}
		} catch (Exception e) {
			throw new NemoException("Failed to do autoLogin or auto SU: "
					+ e.getMessage());
		}
		return shell;
	}

	public DefaultShellDriver() {
		this(createDefaultTarget());
	}

	public DefaultShellDriver(Target target) {
		this.target = target;
	}

	@Override
	public ShellDriver setHost(String host) {
		target.setHost(host);
		return this;
	}

	@Override
	public ShellDriver setPort(int port) {
		target.setPort(port);
		return this;
	}

	@Override
	public ShellDriver setAutoLogin(String loginName, String loginPassword,
			String shellPrompt) {
		target.setAutoLogin(true).setLoginName(loginName)
				.setLoginPassword(loginPassword).setLoginPrompt(shellPrompt);
		return this;
	}

	@Override
	public ShellDriver setAutoSU(String suName, String suPassword,
			String shellPrompt) {
		target.setAutoSU(true).setSuName(suName).setSuPassword(suPassword)
				.setSuPrompt(shellPrompt);
		return this;
	}

	@Override
	public ShellDriver setCommandTimeout(int timeout) {
		target.setCommandTimeout(timeout);
		return this;
	}

	@Override
	public ShellDriver setSendInitialCR(boolean sendInitialCR) {
		target.setInitialCR(sendInitialCR);
		return this;
	}

	protected String getShellIDPrefix() {
		return target.getShellID() == null ? target.getHost() + ":"
				+ target.getPort() : target.getShellID();
	}
}
