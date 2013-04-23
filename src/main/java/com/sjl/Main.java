package com.sjl;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	private static final String SEPARATOR_LINE = "--------------------------------";
	private static final String PROPERTY_CONF_SERVICE_URL = "conf-service-url";
	private static final String PROPERTY_PROPERTY_FILE_LOCATION = "property-file-location";
	private static final String PROPERTY_JETTY_PORT= "jetty-port";
	private WebServer server;

	public Main() {
		server = new WebServer(8000);
	}

	public void start() throws Exception {
		server.start();
		server.join();
	}

	public static void main(String... anArgs) throws Exception {
		// here read arguments from commandline
		if (anArgs.length == 1 && "--help".equals(anArgs[0])) {
			printHelp();
		}

		// first check -D properties if all present and valid run server
		System.out.println("Trying to get configuration from java properties ( -Dprop=value )");
		String jettyPort = System.getProperty(PROPERTY_JETTY_PORT);
		String propertyFileLocation = System.getProperty(PROPERTY_PROPERTY_FILE_LOCATION);
		String confServiceUrl = System.getProperty(PROPERTY_CONF_SERVICE_URL);
		if( validateProperties( jettyPort, propertyFileLocation, confServiceUrl )){
			new Main().start();
			return;
		}else{
			System.out.println("Could not get valid configuration from java properties.");
		}

		if (anArgs.length == 1 && !"--help".equals(anArgs[0])) {
			// assume the path to properties file given
			File f = new File(anArgs[0]);
			System.out.println("Trying to get configuration from given file ["+f.getAbsoluteFile()+"]");
			if(f.exists()){
				Properties properties = new Properties();
				properties.load(new FileInputStream(f));
				if( validateProperties( properties.getProperty(PROPERTY_JETTY_PORT),properties.getProperty(PROPERTY_PROPERTY_FILE_LOCATION), properties.getProperty(PROPERTY_CONF_SERVICE_URL) )){
				
					PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
					configurer.setProperties(properties);

					ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
					context.addBeanFactoryPostProcessor(configurer);
					context.setConfigLocation("/META-INF/webapp/WEB-INF/application-context.xml");
					context.refresh();
					
					new Main().start();
					return;
				}else{
					System.out.println("File ["+f.getAbsoluteFile()+"] exists but does NOT contains valid configuration");
				}
			}else{
				System.out.println("File ["+f.getAbsoluteFile()+"] does not exists");
			}
		}

		// last chance look for installator.properties
		// validate properties and run
		File f = new File("installator.properties");
		System.out.println("Trying to get configuration from default file ["+f.getAbsoluteFile()+"]");
		if(f.exists()){
			Properties properties = new Properties();
			properties.load(new FileInputStream(f));
			if( validateProperties( properties.getProperty(PROPERTY_JETTY_PORT),properties.getProperty(PROPERTY_PROPERTY_FILE_LOCATION), properties.getProperty(PROPERTY_CONF_SERVICE_URL) )){
			
				PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
				configurer.setProperties(properties);

				ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
				context.addBeanFactoryPostProcessor(configurer);
				context.setConfigLocation("/META-INF/webapp/WEB-INF/application-context.xml");
				context.refresh();
				
				new Main().start();
				return;
			}else{
				System.out.println("File ["+f.getAbsoluteFile()+"] exists but does NOT contains valid configuration");
			}
		}else{
			System.out.println("File ["+f.getAbsoluteFile()+"] does not exists");
		}
		
		// if here print help and error mesage
		System.out.println("Could not determine valid configuration. Giving up.");
		printHelp();
	}

	private static void printHelp() throws UnsupportedEncodingException {
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		int index = decodedPath.lastIndexOf(File.separator);
		String jarName = decodedPath.substring(index+1);

		
		System.out.println(SEPARATOR_LINE);
		System.out.println("Usage:");
		System.out.println("java -jar "+jarName+" <path to installator.properties file>");
		System.out.println("OR");
		System.out.println("Usage: java  -D"+PROPERTY_JETTY_PORT+"=8888 -D"+PROPERTY_PROPERTY_FILE_LOCATION+"=/home/... -D"+PROPERTY_CONF_SERVICE_URL+"=http://localhost:9999   -jar "+jarName);
		System.out.println("");
		System.out
				.println("If path to installator.properties file not present application will try to use installator.properties from current directory");
		System.out
				.println("If installator.properties not present in current directory and all required properties were not passed via -D application will fail");
		System.out.println(SEPARATOR_LINE);
		System.exit(0);
	}

	private static boolean  validateProperties(String jettyPort, String propertyFileLocation, String confServiceUrl) {
		if(jettyPort==null){
			System.out.print("Error: missing property: "+PROPERTY_JETTY_PORT);
			return false;
		}
		
		if( propertyFileLocation== null){
			System.out.print("Error: missing property: "+PROPERTY_PROPERTY_FILE_LOCATION);
			return false;
		}
		if(confServiceUrl ==null ){
			System.out.print("Error: missing property: "+PROPERTY_CONF_SERVICE_URL);
			return false;
		}
		
		try{
			Integer.parseInt(jettyPort);
		}catch(NumberFormatException e){
			System.out.println("Error: invalid port value ["+jettyPort+"]");
			return false;
		}
		
		File f = new File(propertyFileLocation);
		if( ! f.exists()){
			System.out.println("Error: invalid file location. File ["+propertyFileLocation+"] does NOT exist");
			return false;
		}
		
		try {
			new URL(confServiceUrl);
		} catch (MalformedURLException e) {
			System.out.println("Error: invalid url value. URL ["+confServiceUrl+"] is invalid "+e.getMessage());
			return false;
		}
		
		System.out.println(SEPARATOR_LINE);
		System.out.println("Got valid configuration:");
		System.out.println(PROPERTY_JETTY_PORT+"="+jettyPort);
		System.out.println(PROPERTY_PROPERTY_FILE_LOCATION+"="+propertyFileLocation);
		System.out.println(PROPERTY_CONF_SERVICE_URL+"="+confServiceUrl);
		System.out.println(SEPARATOR_LINE);
		return true;
	}

}
