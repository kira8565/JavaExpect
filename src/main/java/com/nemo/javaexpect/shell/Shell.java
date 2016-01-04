package com.nemo.javaexpect.shell;

import com.nemo.javaexpect.shell.exception.CommandTimeoutException;

/**
 *
 */
public interface Shell {
	/**
	 * 关闭远程连接
	 */
	public void close();

	/**
	 * 	发送命令到远端执行
	 */
	public void send(String command);
	
	/**
	 * 发送命令到远端并且等到指定的字符串返回
	 * @param command 命令
	 * @param waitForPattern 等待的字符串 
	 * @see java.lang.String#matches(String regex)
	 * @return 
	 * @throws CommandTimeoutException 超时则抛出异常
	 */
	public CommandResult execute(String command, String waitForPattern);

	/**
	 * 	发送命令到远端并且等到指定的字符串返回
	 * @param command 命令
	 * @param waitForPattern 等待的字符串  
	 * @see java.lang.String#matches(String regex)
	 * @param timeout 超时时间
	 * @return 
	 * @throws CommandTimeoutException 超时则抛出异常
	 */
	public CommandResult execute(String command, String waitForPattern, int timeout);
	
	/**
	 * 	发送命令到远端并且等到指定的字符串返回
	 * @param command 命令
	 * @return 
	 * @throws CommandTimeoutException if CommandTimeout reached
	 */	
	public CommandResult execute(String command);

	/**
	 * 	发送命令到远端并且等到指定的字符串返回
	 * @param command 命令
	 * @param timeout 超时时间
	 * @return 
	 * @throws CommandTimeoutException 超时则抛出异常
	 */	
	public CommandResult execute(String command, int timeout);
	
	/**
	 * 在执行完 {@link #send(String)} 执行，用于等待特定的字符串
	 * @param waitForPattern 特定的字符串（正则式） 
	 * @see java.lang.String#matches(String regex)
	 * @return 
	 * @throws CommandTimeoutException 超时则抛出异常
	 */
	public CommandResult expect(String waitForPattern);
	
	/**
	 * 在执行完 {@link #send(String)} 执行，用于等待特定的字符串
	 * @param waitForPattern 特定的字符串（正则式） 
	 * @see java.lang.String#matches(String regex)
	 * @param timeout 命令超时时间
	 * @return 
	 * @throws CommandTimeoutException 超时则抛出异常
	 */
	public CommandResult expect(String waitForPattern, int timeout);	

	/**
	 * 返回最后执行命令退出的编码
	 * 它会首先发送 "echo XYZ$?ZYX"到远端,
	 * 然后再处理返回的结果
	 * @return 执行命令返回的状态编码 
	 * @throws CommandTimeoutException  超时则抛出异常
	 */
	public CommandResult getLastExitCode() ;
}
