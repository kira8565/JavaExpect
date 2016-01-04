package com.nemo.javaexpect.shell.core;

/**
 * 远程连接的目标对象，
 * 存储了所有的用户登录信息
 */

public interface Target {

	/**
	 * 默认的命令执行超时时间
	 */
	public final static int DEFAULT_COMMAND_TIMEOUT = 300;
	public final static int INVALID_PORT = -1;

	/**
	 * 是否自动登录
	 * @return
	 */
	public boolean isAutoLogin();

	/**
	 * @see #setSUWithLoginShell(boolean)
	 */
	public boolean isSUWithLoginShell();

	/** 
	 * @param suWithLoginShell 是否使用登录的Shell执行
	 * 如果为True, 则su的命令如下
	 * <code>su - [user]</code>
	 * 否则
	 * <code>su [user]</code>
	 * */
	public Target setSUWithLoginShell(boolean suWithLoginShell);

	public String getHost();

	/** @param 主机IP或主机名称 */
	public Target setHost(String host);

	public int getPort();

	public Target setPort(int port);

	public String getLoginName();

	public Target setLoginName(String loginName);

	public String getLoginPassword();

	public Target setLoginPassword(String loginPassword);

	public String getLoginPrompt();

	/**
	 * @param	loginPrompt 是一个正则表达式
	 * 参数可以参考 {@link java.lang.String#matches(String regex)}
	 */
	public Target setLoginPrompt(String loginPrompt);

	public Boolean getAutoLogin();

	public Target setAutoLogin(Boolean autoLogin);

	public boolean isInitialCR();

	/**
	 * @param	initialCR
	 * 如果为True，在启动TCP连接之后，Shell会首先发送用户名和密码
	 * 然后在下一行开始之前发送一个换行符
	 * 下面是一个例子 
	 * <code>
	 * <BR/>telnet localhost 
	 * <BR/> login: test
	 * <BR/> password: ***</code>
	 * <BR/> term=vt100?
	 * <BR/> $
	 * </code>
	 * @return Target itself	
	 */
	public Target setInitialCR(boolean initialCR);

	public String getSuName();

	public Target setSuName(String suName);

	public String getSuPassword();

	public Target setSuPassword(String suPassword);

	public String getSuPrompt();

	/**
	 * @param	suPrompt 是一个正则表达式
	 * 参数可以参考 {@link java.lang.String#matches(String regex)}
	 * @return Target itself	
	 */
	public Target setSuPrompt(String suPrompt);

	public boolean isAutoSU();

	public Target setAutoSU(boolean autoSU);

	public int getCommandTimeout();

	public Target setCommandTimeout(int commandTimeout);

	public boolean isInvalidPort();

	public Target setShellID(String id);
	public String getShellID();
}