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
package com.obergner.hzserver.snmp;

import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.Phased;
import org.springframework.core.io.Resource;
import org.vafer.jmx2snmp.jmx.JmxIndex;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.snmp.SnmpBridge;

/**
 * <p>
 * TODO: Document SnmpBridgeFactoryBean
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public class SnmpBridgeFactoryBean implements FactoryBean<SnmpBridge>,
        InitializingBean, DisposableBean, Phased {

	private static final String	DEFAULT_MBEAN_LOOKUP_PATTERN	= "*:name=*";

	private final Logger	    log	                            = LoggerFactory
	                                                                    .getLogger(getClass());

	private SnmpBridge	        product;

	private String	            anmpAgentListenHost	            = "localhost";

	private int	                snmpAgentListenPort	            = 1161;

	private Resource	        snmpMappingPropertiesLocation;

	private boolean	            printMisconfigurationsOnStartup	= false;

	private MBeanServer	        mbeanServer;

	private String	            mbeanLookupPattern	            = DEFAULT_MBEAN_LOOKUP_PATTERN;

	/**
	 * @param anmpAgentListenHost
	 *            The anmpAgentListenHost to set
	 */
	@Required
	public final void setAnmpAgentListenHost(final String anmpAgentListenHost) {
		this.anmpAgentListenHost = anmpAgentListenHost;
	}

	/**
	 * @param snmpAgentListenPort
	 *            The snmpAgentListenPort to set
	 */
	@Required
	public final void setSnmpAgentListenPort(final int snmpAgentListenPort) {
		this.snmpAgentListenPort = snmpAgentListenPort;
	}

	/**
	 * @param snmpMappingPropertiesLocation
	 *            The snmpMappingPropertiesLocation to set
	 */
	@Required
	public final void setSnmpMappingPropertiesLocation(
	        final Resource snmpMappingPropertiesLocation) {
		this.snmpMappingPropertiesLocation = snmpMappingPropertiesLocation;
	}

	/**
	 * @param printMisconfigurationsOnStartup
	 *            The printMisconfigurationsOnStartup to set
	 */
	public final void setPrintMisconfigurationsOnStartup(
	        final boolean printMisconfigurationsOnStartup) {
		this.printMisconfigurationsOnStartup = printMisconfigurationsOnStartup;
	}

	/**
	 * @param mbeanServer
	 *            The mbeanServer to set
	 */
	public final void setMbeanServer(final MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	/**
	 * @param mbeanLookupPattern
	 *            The mbeanLookupPattern to set
	 */
	public final void setMbeanLookupPattern(final String mbeanLookupPattern) {
		this.mbeanLookupPattern = mbeanLookupPattern;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public SnmpBridge getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No SnmpBridge has been created yet - did you remember to call afterPropertiesSet() when using this outside Spring?");
		}
		return this.product;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass()
		        : SnmpBridge.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		checkCorrectlyConfigured();

		this.log.debug(
		        "Creating JMX - SNMP bridge for exporting MBean attributes via SNMP [SNMP Host = {}|SNMP Port = {}|Mapping File = {}] ...",
		        this.anmpAgentListenHost, this.snmpAgentListenPort,
		        this.snmpMappingPropertiesLocation);
		final JmxMib jmxMib = new JmxMib();
		jmxMib.load(new InputStreamReader(this.snmpMappingPropertiesLocation
		        .getInputStream()));

		final JmxIndex jmxIndex = new JmxIndex(getMBeanServer(),
		        this.mbeanLookupPattern);

		this.product = new SnmpBridge(
		        InetAddress.getByName(this.anmpAgentListenHost),
		        this.snmpAgentListenPort, jmxIndex, jmxMib);

		this.log.debug("Starting JMX - SNMP bridge {} ...", this.product);
		if (this.printMisconfigurationsOnStartup) {
			this.product.report();
		}
		this.product.start();
		this.log.debug("JMX - SNMP bridge {} started", this.product);
	}

	private void checkCorrectlyConfigured() throws IllegalStateException {
		if (this.snmpMappingPropertiesLocation == null) {
			throw new IllegalStateException(
			        "Property 'snmpMappingPropertiesLocation' must be set");
		}
		if (!this.snmpMappingPropertiesLocation.exists()) {
			throw new IllegalStateException("Mapping properties file '"
			        + this.snmpMappingPropertiesLocation + "' does not exist");
		}
		if (!this.snmpMappingPropertiesLocation.isReadable()) {
			throw new IllegalStateException("Mapping properties file '"
			        + this.snmpMappingPropertiesLocation + "' is not readable");
		}
	}

	private MBeanServer getMBeanServer() {
		return this.mbeanServer != null ? this.mbeanServer : ManagementFactory
		        .getPlatformMBeanServer();
	}

	@Override
	public void destroy() throws Exception {
		this.log.debug("Stopping JMX - SNMP bridge {} ...", this.product);
		this.product.stop();
		this.log.debug("JMX - SNMP bridge {} stopped", this.product);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SnmpBridgeFactoryBean@" + this.hashCode() + "[product: "
		        + this.product + "|anmpAgentListenHost: "
		        + this.anmpAgentListenHost + "|snmpAgentListenPort: "
		        + this.snmpAgentListenPort + "|snmpMappingPropertiesLocation: "
		        + this.snmpMappingPropertiesLocation + "]";
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 1000;
	}
}
