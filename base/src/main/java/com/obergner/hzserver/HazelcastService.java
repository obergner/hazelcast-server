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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.impl.FactoryImpl;
import com.hazelcast.impl.FactoryImpl.HazelcastInstanceProxy;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * <p>
 * TODO: Document HazelcastService
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public class HazelcastService implements SmartLifecycle {

	private static final String	            STARTUP_DURATION_GAUGE	= "startup-duration-millis";

	private final Logger	                log	                   = LoggerFactory
	                                                                       .getLogger(getClass());

	private final Config	                configuration;

	private final MetricsRegistry	        metricsRegistry;

	private volatile HazelcastInstanceProxy	hazelcastInstance;

	/**
	 * @param configuration
	 */
	public HazelcastService(final Config configuration,
	        final MetricsRegistry metricsRegistry) {
		this.configuration = checkNotNull(configuration,
		        "Argument 'configuration' must not be null");
		this.metricsRegistry = checkNotNull(metricsRegistry,
		        "Argument 'metricsRegistry' must not be null");
	}

	/**
	 * @see org.springframework.context.Lifecycle#start()
	 */
	@Override
	public void start() {
		checkState(this.hazelcastInstance == null,
		        "{} has already been started", this);
		this.log.info("Starting {} using ...", this.configuration);

		final long start = System.currentTimeMillis();
		this.hazelcastInstance = FactoryImpl
		        .newHazelcastInstanceProxy(this.configuration);
		final long startupDuration = System.currentTimeMillis() - start;
		this.metricsRegistry.newGauge(getClass(), STARTUP_DURATION_GAUGE,
		        new Gauge<Long>() {

			        @Override
			        public Long value() {
				        return startupDuration;
			        }
		        });

		this.log.info("{} started in [{}] ms", this, startupDuration);
	}

	/**
	 * @see org.springframework.context.Lifecycle#stop()
	 */
	@Override
	public void stop() {
		checkState(this.hazelcastInstance != null, "{} is not running", this);
		this.log.info("Shutting down Hazelcast instance {} ...",
		        this.hazelcastInstance);

		this.metricsRegistry.removeMetric(getClass(), STARTUP_DURATION_GAUGE);
		this.hazelcastInstance.shutdown();

		this.log.info("{} shut down", this.hazelcastInstance);

		this.hazelcastInstance = null;
	}

	/**
	 * @see org.springframework.context.Lifecycle#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return this.hazelcastInstance != null;
	}

	/**
	 * @see org.springframework.context.Phased#getPhase()
	 */
	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	/**
	 * @see org.springframework.context.SmartLifecycle#isAutoStartup()
	 */
	@Override
	public boolean isAutoStartup() {
		return true;
	}

	/**
	 * @see org.springframework.context.SmartLifecycle#stop(java.lang.Runnable)
	 */
	@Override
	public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	/**
	 * Exposed solely for testing purposes.
	 */
	HazelcastInstance getHazelcastInstance() {
		return this.hazelcastInstance;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HazelcastService@"
		        + this.hashCode()
		        + "[hazelcastInstance: "
		        + (this.hazelcastInstance != null ? this.hazelcastInstance
		                .getName() : "<NOT-STARTED>") + "]";
	}
}
