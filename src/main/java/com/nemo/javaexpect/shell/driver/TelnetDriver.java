package com.nemo.javaexpect.shell.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.telnet.TelnetClient;

import com.nemo.javaexpect.shell.DefaultShell;
import com.nemo.javaexpect.shell.Shell;
import com.nemo.javaexpect.shell.core.Target;
import com.nemo.javaexpect.shell.exception.ConnectionException;
import com.nemo.javaexpect.shell.logger.ShellLogger;
import com.nemo.javaexpect.term.VT100InputStream;

public class TelnetDriver extends DefaultShellDriver {

	@Override
	protected Shell createShell() {
		final TelnetClient telnet = new TelnetClient("VT100");
		try {
            // Connect to the specified server			
            telnet.connect(target.getHost(), target.getPort());
		} catch (Exception e) {
			log("", String.format("telnet %s %d", target.getHost(), target.getPort()), " failed: \n" + e.getMessage());
			throw new ConnectionException("Failed to create telnet client: " + e.getMessage());
		}    
		
		final InputStream in = createVT100Filter(telnet.getInputStream());
				
		startVT100FilterThread( in);
		
		final OutputStream out = telnet.getOutputStream();
		
		Connection conn = new Connection () {
			TelnetClient _telnet = telnet;
			InputStream _in = in;
			OutputStream _out = out;
			
			@Override
			public void close() {
				try {
					_telnet.disconnect();
				} catch (IOException e) {
				}
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
				return _telnet.isConnected();
			}			
		};
		final String id = this.getShellIDPrefix() + "@" + conn.hashCode();
		final ShellLogger logger = DefaultShellDriver.getShellLogger();
		
		log(id, String.format("telnet %s %d", target.getHost(), target.getPort()), "\n");
		return new DefaultShell(conn, 
				target.isAutoSU()? target.getSuPrompt(): target.getLoginPrompt() ,
				target.getCommandTimeout()) {

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

	static public int TELNET_DEFAULT_PORT = 23;

	public TelnetDriver(){
		super();
		target.setPort(TELNET_DEFAULT_PORT);
	}
	
	public TelnetDriver(Target target) {		
		super(target);
		if (target.isInvalidPort())
			target.setPort(TELNET_DEFAULT_PORT);
	}
	
	public TelnetDriver(String host){
		this();
		target.setHost(host);
	}
	public TelnetDriver(String host, String loginName, String shellPrompt) {
		this(host);
		target.setAutoLogin(true).
			setLoginName(loginName).
			setLoginPrompt(shellPrompt);
	}
	
	public TelnetDriver(String host, String loginName, String loginPassword, String shellPrompt) {
		this(host, loginName, shellPrompt);
		target.setLoginPassword(loginPassword);
	}	
}
