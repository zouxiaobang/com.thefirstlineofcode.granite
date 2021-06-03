package com.firstlinecode.granite.lite.dba.internal;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HSqlServer {
	private static final Logger logger = LoggerFactory.getLogger(HSqlServer.class);
	
	private static final String PROPERTY_KEY_GRANITE_LITE_HSQL_PORT = "granite.lite.hsql.port";
	
	private Server server;
	
	@PostConstruct
    public void start() {
			String appHome = System.getProperty("granite.app.home");
			String dataDir = String.format("%s%s", appHome, "data");
			
            try {
				doStart(dataDir);
				
				logger.info("HSQLDB started.");
			} catch (Exception e) {
				logger.error("Can't start HSQLDB server.");
				throw new RuntimeException("Can't start HSQLDB server.", e);
			}
    }

	private void doStart(String dataDir) throws IOException, AclFormatException {
		server = new Server();
		HsqlProperties properties = new HsqlProperties();
		properties.setProperty("server.database.0", String.format("file:%s/%s", dataDir, "granite"));
		properties.setProperty("server.dbname.0", "granite");
		
		String sPort = System.getProperty(PROPERTY_KEY_GRANITE_LITE_HSQL_PORT);
		
		int port = 9001;
		if (sPort != null) {
			try {
				port = Integer.parseInt(sPort);
				properties.setProperty("server.port", port);
			} catch (Exception e) {
				// ignore
			}
		}
		System.setProperty(PROPERTY_KEY_GRANITE_LITE_HSQL_PORT, Integer.toString(port));
		
		server.setLogWriter(null);
		server.setProperties(properties);

		server.start();
	}
	
	@PreDestroy
    public void stop() {
		try {
			if (server != null) {
				server.stop();
				server = null;
			}
		} catch (Exception e) {
			logger.error("Can't stop HSQLDB server.", e);
		}
            
		logger.info("HSQLDB stopped.");
    }
}