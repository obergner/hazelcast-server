/**
 * Copyright (C) 2012.
 * Olaf Bergner.
 * Hamburg, Germany. olaf.bergner@gmx.de
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.obergner.hzserver;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>
 * TODO: Document Main
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public final class Main {

	/**
	 * A class for intercepting the hang up signal and do a graceful shutdown of
	 * the Camel.
	 */
	private static final class HangupInterceptor extends Thread {
		private final Logger	log	= LoggerFactory.getLogger(this.getClass());
		private final Main		mainInstance;

		public HangupInterceptor(final Main main) {
			this.mainInstance = main;
		}

		@Override
		public void run() {
			this.log.info("Received hang up - stopping Hazelcast Server.");
			try {
				this.mainInstance.stop();
			} catch (final Exception ex) {
				this.log.warn(
				        "Error during stopping Hazelcast Server: "
				                + ex.getMessage(), ex);
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		new Main().start();
	}

	private final Logger	 log	= LoggerFactory.getLogger(getClass());

	private final ServerInfo	serverInfo;

	/**
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws NullPointerException
	 * @throws MalformedObjectNameException
	 * @throws NotCompliantMBeanException
	 * @throws MBeanRegistrationException
	 * @throws InstanceAlreadyExistsException
	 * 
	 */
	Main() throws MalformedURLException, IOException,
	        InstanceAlreadyExistsException, MBeanRegistrationException,
	        NotCompliantMBeanException, MalformedObjectNameException,
	        NullPointerException {
		this.serverInfo = new ServerInfo();
	}

	void start() throws Exception {
		enableHangupSupport();

		this.serverInfo.logBanner();

		new ClassPathXmlApplicationContext(
		        "classpath:META-INF/spring/hz-server-main-context.xml",
		        "classpath*:META-INF/spring/hz-cache-context.xml")
		        .registerShutdownHook();
	}

	void stop() throws Exception {
		this.log.info("Stopping HazelcastServer ....");
		this.log.info("HazelcastServer stopped");
	}

	/**
	 * Enables the hangup support. Gracefully stops by calling stop() on a
	 * Hangup signal.
	 */
	private void enableHangupSupport() {
		final HangupInterceptor interceptor = new HangupInterceptor(this);
		Runtime.getRuntime().addShutdownHook(interceptor);
	}
}
