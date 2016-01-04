package com.nemo.javaexpect.shell;

import java.util.regex.Pattern;

import com.nemo.javaexpect.shell.exception.NemoException;

public class DefaultCommandResult implements CommandResult {
	String command;
	String commandResult;
	Integer exitCode;

	public DefaultCommandResult(String command, int exitCode) {
		this.exitCode = new Integer(exitCode);
		this.command = command;
	}

	public DefaultCommandResult(String command, String result) {
		this.commandResult = result;
		this.command = command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nemo.service.shell.CommandResult#getCommandResult()
	 */
	@Override
	public String getCommandResult() {
		return commandResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nemo.service.shell.CommandResult#getCommand()
	 */
	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public int getExitCode() {
		if (exitCode == null)
			throw new NemoException("exitCode is null");
		return exitCode.intValue();
	}

	@Override
	public CommandResult requireText(String expected) {
		if (expected == null) {
			throw new NullPointerException();
		}
		Pattern p = Pattern.compile(expected);
		if (!p.matcher(commandResult).find()) {
			String.format("Expected value: %s, actual value: %s", expected,
					commandResult);
		}
		return this;
	}

	@Override
	public CommandResult requireText(Pattern pattern) {
		if (pattern == null) {
			throw new NullPointerException();
		}
		if (!pattern.matcher(commandResult).find()) {
			String.format("Expected value: %s, actual value: %s",
					pattern.pattern(), commandResult);
		}
		return this;
	}

	@Override
	public CommandResult requireExitCode(int expected) {
		int actual = getExitCode();
		if (actual != expected) {
			throw new NemoException(String.format(
					"Expected value: %d, actual value: %d", expected, actual));
		}
		return this;
	}
}
