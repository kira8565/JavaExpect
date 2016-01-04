package com.nemo.javaexpect.shell.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nemo.javaexpect.shell.DefaultShell;
import com.nemo.javaexpect.shell.Shell;
import com.nemo.javaexpect.shell.core.Target;
import com.nemo.javaexpect.shell.exception.ConnectionException;
import com.nemo.javaexpect.shell.exception.NemoException;
import com.nemo.javaexpect.shell.logger.ShellLogger;

public class SshDriver extends DefaultShellDriver {
	final static public int SSH_DEFAULT_PORT = 22;
	final static public int SSH_WRITE_CHANNEL_BUFFER = 16 * 1024;

	public SshDriver() {
		super();
		target.setPort(SSH_DEFAULT_PORT);
	}

	public SshDriver(Target target) {
		super(target);
		if (target.isInvalidPort())
			target.setPort(SSH_DEFAULT_PORT);
	}

	@Override
	protected void autoLogin(Shell shell) {
		// send a enter char
		if (target.isInitialCR()) {
			shell.send("");
		}
		shell.expect(target.getLoginPrompt());
	}

	public SshDriver(String host, String loginName, String shellPrompt) {
		this();
		target.setHost(host).setAutoLogin(true).setLoginName(loginName)
				.setLoginPrompt(shellPrompt);
	}

	public SshDriver(String host, String loginName, String loginPassword,
			String shellPrompt) {
		this(host, loginName, shellPrompt);
		target.setLoginPassword(loginPassword);
	}

	protected Shell createShell() {
		Session session = null;

		Connection connection;

		/** ssh Shell 输入输出流 */
		PipedOutputStream out = new PipedOutputStream();

		// jsch 输入流
		InputStream jsch_in;

		try {
			jsch_in = new PipedInputStream(out, SSH_WRITE_CHANNEL_BUFFER);
		} catch (IOException e1) {
			log("", String.format("ssh -l %s -p %d %s", target.getLoginName(),
					target.getPort(), target.getHost()),
					" failed\n" + e1.getMessage());
			throw new ConnectionException(
					"Failed to create connect the PipedInput and PipedOutput Stream\n"
							+ e1.getMessage());
		}

		try {
			// setup a ssh session
			session = createJschSession();

			connection = createJschChannel(session, jsch_in, out);

			// start the vt100 read process
			startVT100FilterThread(connection.getInputStream());

		} catch (Exception e) {
			try {
				session.disconnect();
			} catch (Exception s) {
				s.printStackTrace();
			}
			
			log("", String.format("ssh -l %s -p %d %s", target.getLoginName(),
					target.getPort(), target.getHost()),
					" failed\n" + e.getMessage());
			throw new ConnectionException("failed to create ssh session\n "
					+ e.getMessage());
		}

		final String id = this.getShellIDPrefix() + "@" + connection.hashCode();
		final ShellLogger logger = DefaultShellDriver.getShellLogger();

		log(id, String.format("ssh -l %s -p %d %s", target.getLoginName(),
				target.getPort(), target.getHost()), "\n");

		return new DefaultShell(connection,
				target.isAutoSU() ? target.getSuPrompt()
						: target.getLoginPrompt(), target.getCommandTimeout()) {

			@Override
			public String getShellId() {
				return id;
			}

			@Override
			public ShellLogger getLogger() {
				return logger;
			}

		};
	}

	private Connection createJschChannel(Session session, InputStream jsch_in,
			OutputStream out) throws JSchException, IOException {
		final InputStream in;
		Channel channel;
		// open shell channel with vt100 term
		channel = session.openChannel("shell");
		((ChannelShell) channel).setPtyType("vt100");
		((ChannelShell) channel).setPty(true);
		channel.setInputStream(jsch_in);
		in = channel.getInputStream();

		channel.connect(10000);

		// sleep 500s in case we don't have open channel permission and server
		// close the channel.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		if (!channel.isConnected()) {
			throw new NemoException(
					"channel has been closed by peer. maybe you don't have permission");
		}

		final Session _session = session;
		final InputStream _in = this.createVT100Filter(in);
		final OutputStream _out = out;
		return new Connection() {
			boolean _active = true;

			@Override
			public void close() {
				_active = false;
				_session.disconnect();
			}

			@Override
			public OutputStream getOutputStream() {
				return _out;
			}

			@Override
			public InputStream getInputStream() {
				return _in;
			}

			@Override
			public boolean isActive() {
				return _active && _session.isConnected();
			}

		};

	}

	private Session createJschSession() throws JSchException {
		Session session;
		JSch jsch = new JSch();
		session = jsch.getSession(target.getLoginName(), target.getHost(),
				target.getPort());
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(target.getLoginPassword());
		session.setDaemonThread(true);
		session.connect(60000); // making a connection with 60s timeout.
		return session;
	}

	@Override
	protected void assertTargetParameters() {
		super.assertTargetParameters();

		if (target.getLoginName() == null) {
			throw new NemoException(
					"ssh must have a login name when open the shell");
		}
	}
}
