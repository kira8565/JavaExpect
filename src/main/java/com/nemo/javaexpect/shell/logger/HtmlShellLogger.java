package com.nemo.javaexpect.shell.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HtmlShellLogger implements ShellLogger {

	@Override
	public void log(String lineIntestCase, String id, String command, String shellOutput) {
		if (shellOutput == null)
			return;
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String time = df.format(date);
		String out = String.format("<shell><time>%s</time><lineInCase>%s</lineInCase><shellID>%s</shellID>"+
			"<shellCommand>%s</shellCommand><shellOutput>%s</shellOutput></shell>",
			time,
			lineIntestCase,
			id,
			command,
			shellOutput);
		System.out.println(out);
	}
	
}
