package com.nemo.javaexpect.shell;

import java.util.regex.Pattern;

public interface CommandResult {

	public String getCommandResult();
	public String getCommand();

	public int getExitCode ();

	/**
	 * 断言结果是与给定正则式匹配的 
	 * @param	期望的正则式
	 * @throws NullPointerException 如果给定的正则式为null {@code null}.
	 * @throw NemoException 如果未匹配成功	
	 */
	public CommandResult requireText(String expected);
	
	/**
	 * 断言结果是与给定正则式匹配的 
	 * @param 期望的正则式
     * @throws NullPointerException 如果给定的正则式为null {@code null}.
	 * @throw NemoException 如果未匹配成功	
	 */
	public CommandResult requireText(Pattern pattern);

	/**
	 * 断言结果与给定的编码匹配 
	 * @param 匹配的编码
	 * @throw NemoException 未匹配成功	
	 */	
	public CommandResult requireExitCode(int expected);
}