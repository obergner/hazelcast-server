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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Manifest;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Phased;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * <p>
 * TODO: Document ServerInfo
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
@ManagedResource(objectName = "com.obergner.hzserver:name=ServerInfo", description = "Reports general information on this server process: name, version, buildnumber etc.")
public final class ServerInfo implements Phased, ServerInfoMBean {

	private static final String	SPECIFICATION_TITLE	  = "Specification-Title";

	private static final String	SPECIFICATION_VERSION	= "Specification-Version";

	private static final String	BUILD_NUMBER	      = "BuildNumber";

	private static final String	BUILD_JDK	          = "Build-Jdk";

	private static final String	BUILT_BY	          = "Built-By";

	private final Logger	    log	                  = LoggerFactory
	                                                          .getLogger("HazelcastServer");

	private final Manifest	    serverManifest;

	private final RuntimeMXBean	runtimeMBean;

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
	public ServerInfo() throws MalformedURLException, IOException,
	        InstanceAlreadyExistsException, MBeanRegistrationException,
	        NotCompliantMBeanException, MalformedObjectNameException,
	        NullPointerException {
		this.serverManifest = loadServerManifest();
		this.runtimeMBean = ManagementFactory.getRuntimeMXBean();
		ManagementFactory.getPlatformMBeanServer().registerMBean(this,
		        new ObjectName("vnet.hz-server:name=ServerInfo"));
	}

	private Manifest loadServerManifest() throws MalformedURLException,
	        IOException {
		final Class<?> clazz = getClass();
		final String className = clazz.getSimpleName() + ".class";
		final String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			// Class not from JAR -> probably running in a unit test context
			return new Manifest(getClass().getClassLoader()
			        .getResourceAsStream("META-INF/MANIFEST.MF"));
		}
		final String manifestPath = classPath.substring(0,
		        classPath.lastIndexOf("!") + 1)
		        + "/META-INF/MANIFEST.MF";
		return new Manifest(new URL(manifestPath).openStream());
	}

	@Override
	public int getPhase() {
		return Integer.MIN_VALUE + 1000;
	}

	public void logBanner() {
		this.log.info("#######################################################################");
		this.log.info("Name:            {}", getAttribute(SPECIFICATION_TITLE));
		this.log.info("Version:         {}",
		        getAttribute(SPECIFICATION_VERSION));
		this.log.info("Buildnumber:     {}", getAttribute(BUILD_NUMBER));
		this.log.info("Build-JDK:       {}", getAttribute(BUILD_JDK));
		this.log.info("Built-By:        {}", getAttribute(BUILT_BY));
		this.log.info("-----------------------------------------------------------------------");
		this.log.info("Runtime:         {}", this.runtimeMBean.getName());
		this.log.info("JVM:             {}", this.runtimeMBean.getVmName());
		this.log.info("JVM Version:     {}", this.runtimeMBean.getVmVersion());
		this.log.info("JVM Vendor:      {}", this.runtimeMBean.getVmVendor());
		this.log.info("JVM Arguments:   {}",
		        this.runtimeMBean.getInputArguments());
		this.log.info("#######################################################################");
	}

	private String getAttribute(final String name) {
		return this.serverManifest.getMainAttributes().getValue(name);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getSpecificationTitle()
	 */
	@Override
	@ManagedAttribute(description = "This server process' name")
	public String getSpecificationTitle() {
		return getAttribute(SPECIFICATION_TITLE);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getSpecificationVersion()
	 */
	@Override
	@ManagedAttribute(description = "This server process' version")
	public String getSpecificationVersion() {
		return getAttribute(SPECIFICATION_VERSION);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuildNumber()
	 */
	@Override
	@ManagedAttribute(description = "This server process' buildnumber")
	public String getBuildNumber() {
		return getAttribute(BUILD_NUMBER);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuildJdk()
	 */
	@Override
	@ManagedAttribute(description = "Which JDK built this server process")
	public String getBuildJdk() {
		return getAttribute(BUILD_JDK);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuiltBy()
	 */
	@Override
	@ManagedAttribute(description = "Who built this server process")
	public String getBuiltBy() {
		return getAttribute(BUILT_BY);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerInfo@" + this.hashCode() + "[SpecificationTitle: "
		        + this.getSpecificationTitle() + "|SpecificationVersion: "
		        + this.getSpecificationVersion() + "|BuildNumber: "
		        + this.getBuildNumber() + "|BuildJdk: " + this.getBuildJdk()
		        + "|BuiltBy: " + this.getBuiltBy() + "]";
	}
}
