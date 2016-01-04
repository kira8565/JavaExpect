package com.nemo.javaexpect.shell.driver;

import com.nemo.javaexpect.shell.Shell;

/**
 * 一个用于登陆系统的驱动信息接口
 * 
 */
public interface ShellDriver {
	Shell open();

	/**
	 * 主机名或IP
	 * 
	 * @param host
	 * @return
	 */
	ShellDriver setHost(String host);

	/**
	 * 端口
	 * 
	 * @param port
	 * @return
	 */
	ShellDriver setPort(int port);

	/**
	 * 登陆成功后自动输入用户名和密码
	 * 
	 * @param loginName
	 * @param loginPassword
	 * @param shellPrompt
	 * @return
	 */
	ShellDriver setAutoLogin(String loginName, String loginPassword,
			String shellPrompt);

	/**
	 * 获取到Shell之前先切换账号
	 * 
	 * @param suName
	 * @param suPassword
	 * @param shellPrompt
	 * @return
	 */
	ShellDriver setAutoSU(String suName, String suPassword, String shellPrompt);

	/**
	 * 默认命令执行超时时间
	 * 
	 * @param timeout
	 * @return
	 */
	ShellDriver setCommandTimeout(int timeout);

	/**
	 * 执行命令的时候是否发送回车换行符
	 * 
	 * @param sendInitialCR
	 * @return
	 */
	ShellDriver setSendInitialCR(boolean sendInitialCR);
}
