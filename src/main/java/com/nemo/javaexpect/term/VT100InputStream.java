package com.nemo.javaexpect.term;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PushbackInputStream;

import com.nemo.javaexpect.shell.exception.ConnectionException;

/**
 * 过滤 VT100 控制字符串
 * 
 *
 */
public class VT100InputStream extends FilterInputStream implements Runnable{
	static final String ESC_pattern = "[ -/]*([0-Z\\-~]|\\[[ -/]*[0-?]*[@-~])";
	private PipedOutputStream outputStream;
	private PipedInputStream inputStream;
	private void internWrite(String s) {
		try {
		//	System.out.print(s);
			outputStream.write(s.getBytes());
		} catch (IOException e) {
			System.out.println("not enough buffer " + e.getMessage());
		}
	}
	private void createPipeInputOutputStream() {
		outputStream = new PipedOutputStream();
		try {
			inputStream = new PipedInputStream(outputStream, 32*1024);
		} catch (IOException e1) {
			throw new ConnectionException("Failed to create connect the PipedInput and PipedOutput Stream\n" + e1.getMessage());
		}	
	}
	public VT100InputStream(InputStream in) {
		super(new PushbackInputStream(in));

		createPipeInputOutputStream();
	}

	@Override
	public int available() throws IOException {
		return inputStream.available();
	}


	@Override
	public boolean markSupported() {
		return inputStream.markSupported();		
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return inputStream.read(b);
	}


	@Override
	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	@Override
	public void close() throws IOException {
		super.close();

		outputStream.close();
		inputStream.close();
		in.close();
		
		outputStream = null;
		inputStream = null;
		in = null;		
	}

	@Override
	public synchronized void mark(int readlimit) {
		inputStream.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		inputStream.reset();
	}
	
	public void run() {

		byte b;
		try {
			while (true) {

				b = getChar();
	
				if (b == 0) {
					continue;
				}

				// ESC
				if (b == 0x1b) {
					 handleESCSequences();
					 continue;
				}

				if (b == 0x07) { // bel ^G
					continue;
				}
				if (b == 0x08) {
					continue;
				}
				if (b == 0x09) { // ht(^I)
					continue;
				}

				if (b == 0x0f) { // rmacs ^O // end alternate character set (P)
					continue;
				}

				if (b == 0x0e) { // smacs ^N // start alternate character set
									// (P)
					continue;
				}
				else { 
					pushChar(b);
				    handleASCIISequences(); 
					continue;
				}
			}
		} catch (Exception e) {
		}
	}
	private void handleASCIISequences() throws IOException {
		byte b;
		b=getChar();

		if((b&0x80)!=0){
		  internWrite(new String(new byte[]{b, getChar()}));
		}
		else if (isASCII(b)){
			StringBuilder build = new StringBuilder();
			while (isASCII(b) && in.available()>0){
				build.append((char)b);
				b = getChar();
			}
			
			if (isASCII(b)) {
				// no more char is readable
				build.append((char)b);
				internWrite(build.toString());
			}
			else
			{
				// read a non-ascii char
				internWrite(build.toString());
				pushChar(b);
			}
		}
		else {
			System.out.println(String.format("drop byte: %d", b ));
		}
	}
	private void handleESCSequences() throws IOException {
		int count = 0;
		StringBuilder build = new StringBuilder();
		while (true) {
			byte x = getChar();
			build.append((char)x);
			
			if (build.toString().matches(ESC_pattern)) {
				break;
			}
			count ++;						
			
			if (count > 20) {
				System.out.print("We meet invalid Escape Sequences, skip the first ESC");
				pushChar(build);
				break;
			}
			
		}
	}
	
	private boolean isASCII(byte b) {
		if(0x20<=b&&b<=0x7f) 
			return true;
		if (b == 0x0a ||b == 0x0d) 
			return true;
		return false;
	}
	private void pushChar(StringBuilder build) throws IOException {
		for (int i=0; i< build.length(); i++)
			pushChar((byte)build.charAt(i));
		
	}
	private void pushChar(byte b) throws IOException {
		PushbackInputStream stream = (PushbackInputStream )in;
			stream.unread(b);
	}
	private byte getChar() throws IOException {
			return (byte) in.read();
	}
}
