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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.jar.Manifest;

import javax.annotation.PreDestroy;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Phased;

/**
 * <p>
 * TODO: Document ServerInfo
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public final class ServerInfo implements Phased, ServerInfoMBean {

	private static final String	SPECIFICATION_TITLE	       = "Specification-Title";

	private static final String	SPECIFICATION_VERSION	   = "Specification-Version";

	private static final String	IMPLEMENTATION_BRANCH	   = "Implementation-Branch";

	private static final String	IMPLEMENTATION_REVISION	   = "Implementation-Revision";

	private static final String	IMPLEMENTATION_BUILDNUMBER	= "Implementation-BuildNumber";

	private static final String	BUILD_JDK	               = "Build-Jdk";

	private static final String	BUILT_BY	               = "Built-By";

	private final Logger	    log	                       = LoggerFactory
	                                                               .getLogger("HazelcastServer");

	private final Manifest	    serverManifest;

	private final ObjectName	objectName;

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
		this.objectName = new ObjectName(getClass().getPackage().getName()
		        + ":name=ServerInfo");
		ManagementFactory.getPlatformMBeanServer().registerMBean(this,
		        this.objectName);
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

	public void logBootStart() {
		this.log.info("================================================================================================");
		this.log.info(formatLogLine("Name", getName()));
		this.log.info(formatLogLine("Version", getVersion()));
		this.log.info(formatLogLine("Buildnumber", getBuildNumber()));
		this.log.info(formatLogLine("Branch", getBranch()));
		this.log.info(formatLogLine("Revision", getRevision()));
		this.log.info(formatLogLine("Build-JDK", getBuildJdk()));
		this.log.info(formatLogLine("Built-By", getBuiltBy()));
		this.log.info("------------------------------------------------------------------------------------------------");
		this.log.info(formatLogLine("JVM", getJVM()));
		this.log.info("------------------------------------------------------------------------------------------------");
		logBootClassPath();
		this.log.info("------------------------------------------------------------------------------------------------");
		logClassPath();
		this.log.info("------------------------------------------------------------------------------------------------");
		this.log.info(formatLogLine("LibraryPath", getLibraryPath()));
		this.log.info("------------------------------------------------------------------------------------------------");
		logSystemProperties();
		this.log.info("------------------------------------------------------------------------------------------------");
		this.log.info(formatLogLine("Operating System", getOS()));
		this.log.info(formatLogLine("Available Processors",
		        getNumAvailableProcessors()));
		this.log.info("------------------------------------------------------------------------------------------------");
		this.log.info(formatLogLine("Heap Memory Usage (Initial/MB)",
		        getInitialHeapMemoryInMBs()));
		this.log.info(formatLogLine("Heap Memory Usage (Used/MB)",
		        getUsedHeapMemoryInMBs()));
		this.log.info(formatLogLine("Heap Memory Usage (Committed/MB)",
		        getCommittedHeapMemoryInMBs()));
		this.log.info(formatLogLine("Heap Memory Usage (Max/MB)",
		        getMaxHeapMemoryInMBs()));
		this.log.info(formatLogLine("Non Heap Memory Usage (Initial/MB)",
		        getInitialNonHeapMemoryInMBs()));
		this.log.info(formatLogLine("Non Heap Memory Usage (Used/MB)",
		        getUsedNonHeapMemoryInMBs()));
		this.log.info(formatLogLine("Non Heap Memory Usage (Committed/MB)",
		        getCommittedNonHeapMemoryInMBs()));
		this.log.info(formatLogLine("Non Heap Memory Usage (Max/MB)",
		        getMaxNonHeapMemoryInMBs()));
		this.log.info("================================================================================================");
	}

	private String formatLogLine(final String header, final String arg) {
		return String.format("%-43s: %s", header, arg);
	}

	private String formatLogLine(final String header, final long arg) {
		return String.format("%-43s: %d", header, arg);
	}

	private void logBootClassPath() {
		logStringEntries("BootClassPath", getBootClassPath(),
		        File.pathSeparator);
	}

	private void logStringEntries(final String header,
	        final String entriesString, final String sep) {
		final String[] bootClassPathEntries = entriesString.split(sep);
		int idx = 1;
		for (final String bootClassPathEntry : bootClassPathEntries) {
			this.log.info(formatLogLine(header + " (" + idx++ + ")",
			        bootClassPathEntry));
		}
	}

	private void logClassPath() {
		logStringEntries("BootClassPath", getClassPath(), File.pathSeparator);
	}

	private void logSystemProperties() {
		logStringEntries("SystemProperties", getSystemProperties(), " -D");
	}

	public void logBootCompleted() {
		this.log.info("================================================================================================");
		this.log.info("{} booted in [{}] ms", getName(),
		        System.currentTimeMillis() - getStartTime().getTime());
		this.log.info("================================================================================================");
	}

	public void logShutdownCompleted() {
		this.log.info("================================================================================================");
		this.log.info(
		        "{}, started on {}, completed shutdown after an uptime of [{}] ms",
		        getName(), getStartTime(), getUptimeMillis());
		this.log.info("================================================================================================");
	}

	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return getTitle() + " v. " + getVersion() + " (" + getBuildNumber()
		        + ")";
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getTitle()
	 */
	@Override
	public String getTitle() {
		return getAttribute(SPECIFICATION_TITLE);
	}

	private String getAttribute(final String name) {
		return this.serverManifest.getMainAttributes().getValue(name);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getVersion()
	 */
	@Override
	public String getVersion() {
		return getAttribute(SPECIFICATION_VERSION);
	}

	@Override
	public String getBranch() {
		return getAttribute(IMPLEMENTATION_BRANCH);
	}

	@Override
	public String getRevision() {
		return getAttribute(IMPLEMENTATION_REVISION);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuildNumber()
	 */
	@Override
	public String getBuildNumber() {
		return getAttribute(IMPLEMENTATION_BUILDNUMBER);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuildJdk()
	 */
	@Override
	public String getBuildJdk() {
		return getAttribute(BUILD_JDK);
	}

	/**
	 * @see com.obergner.hzserver.ServerInfoMBean#getBuiltBy()
	 */
	@Override
	public String getBuiltBy() {
		return getAttribute(BUILT_BY);
	}

	// ------------------------------------------------------------------------

	@Override
	public String getJVM() {
		return runtime().getVmName() + " " + runtime().getVmVersion() + " ("
		        + runtime().getVmVendor() + ")";
	}

	private RuntimeMXBean runtime() {
		return ManagementFactory.getRuntimeMXBean();
	}

	@Override
	public String getClassPath() {
		return runtime().getClassPath();
	}

	@Override
	public String getBootClassPath() {
		return runtime().getBootClassPath();
	}

	@Override
	public String getLibraryPath() {
		return runtime().getLibraryPath();
	}

	@Override
	public String getSystemProperties() {
		final Map<String, String> sysProps = runtime().getSystemProperties();
		final StringBuilder result = new StringBuilder();
		for (final Map.Entry<String, String> sysProp : sysProps.entrySet()) {
			result.append("-D").append(sysProp.getKey()).append("=")
			        .append(sysProp.getValue()).append(' ');
		}
		return result.toString();
	}

	@Override
	public Date getStartTime() {
		return new Date(runtime().getStartTime());
	}

	@Override
	public long getUptimeMillis() {
		return runtime().getUptime();
	}

	// ------------------------------------------------------------------------

	@Override
	public String getOS() {
		return os().getName() + " " + os().getVersion() + " (" + os().getArch()
		        + ")";
	}

	private OperatingSystemMXBean os() {
		return ManagementFactory.getOperatingSystemMXBean();
	}

	@Override
	public int getNumAvailableProcessors() {
		return os().getAvailableProcessors();
	}

	// ------------------------------------------------------------------------

	@Override
	public int getInitialHeapMemoryInMBs() {
		return bytesToMBs(memory().getHeapMemoryUsage().getInit());
	}

	private MemoryMXBean memory() {
		return ManagementFactory.getMemoryMXBean();
	}

	private int bytesToMBs(final long bytes) {
		return (int) (bytes / (1024 * 1024));
	}

	@Override
	public int getUsedHeapMemoryInMBs() {
		return bytesToMBs(memory().getHeapMemoryUsage().getUsed());
	}

	@Override
	public int getCommittedHeapMemoryInMBs() {
		return bytesToMBs(memory().getHeapMemoryUsage().getCommitted());
	}

	@Override
	public int getMaxHeapMemoryInMBs() {
		return bytesToMBs(memory().getHeapMemoryUsage().getMax());
	}

	@Override
	public int getInitialNonHeapMemoryInMBs() {
		return bytesToMBs(memory().getNonHeapMemoryUsage().getInit());
	}

	@Override
	public int getUsedNonHeapMemoryInMBs() {
		return bytesToMBs(memory().getNonHeapMemoryUsage().getUsed());
	}

	@Override
	public int getCommittedNonHeapMemoryInMBs() {
		return bytesToMBs(memory().getNonHeapMemoryUsage().getCommitted());
	}

	@Override
	public int getMaxNonHeapMemoryInMBs() {
		return bytesToMBs(memory().getNonHeapMemoryUsage().getMax());
	}

	@PreDestroy
	public void unregister() throws MBeanRegistrationException,
	        InstanceNotFoundException {
		ManagementFactory.getPlatformMBeanServer().unregisterMBean(
		        this.objectName);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerInfo@" + this.hashCode() + "[Name: " + this.getName()
		        + "|SpecificationTitle: " + this.getTitle()
		        + "|SpecificationVersion: " + this.getVersion()
		        + "|BuildNumber: " + this.getBuildNumber() + "|BuildJdk: "
		        + this.getBuildJdk() + "|BuiltBy: " + this.getBuiltBy()
		        + "|JVM: " + this.getJVM() + "|ClassPath: "
		        + this.getClassPath() + "|BootClassPath: "
		        + this.getBootClassPath() + "|LibraryPath: "
		        + this.getLibraryPath() + "|SystemProperties: "
		        + this.getSystemProperties() + "|StartTime: "
		        + this.getStartTime() + "|UptimeMillis: "
		        + this.getUptimeMillis() + "|OS: " + this.getOS()
		        + "|NumAvailableProcessors: "
		        + this.getNumAvailableProcessors()
		        + "|InitialHeapMemoryInMBs: "
		        + this.getInitialHeapMemoryInMBs() + "|UsedHeapMemoryInMBs: "
		        + this.getUsedHeapMemoryInMBs() + "|CommittedHeapMemoryInMBs: "
		        + this.getCommittedHeapMemoryInMBs() + "|MaxHeapMemoryInMBs: "
		        + this.getMaxHeapMemoryInMBs() + "|InitialNonHeapMemoryInMBs: "
		        + this.getInitialNonHeapMemoryInMBs()
		        + "|UsedNonHeapMemoryInMBs: "
		        + this.getUsedNonHeapMemoryInMBs()
		        + "|CommittedNonHeapMemoryInMBs: "
		        + this.getCommittedNonHeapMemoryInMBs()
		        + "|MaxNonHeapMemoryInMBs: " + this.getMaxNonHeapMemoryInMBs()
		        + "]";
	}

}
