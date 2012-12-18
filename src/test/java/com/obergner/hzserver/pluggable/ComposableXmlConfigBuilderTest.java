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
package com.obergner.hzserver.pluggable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

import com.hazelcast.config.Config;

public class ComposableXmlConfigBuilderTest {

	private static final String	CONFIG_FILE	= "global/composable-xml-config-test-config.xml";

	private static final String	MAPS_FILE	= "data/composable-xml-config-test-maps.xml";

	private static final String	QUEUES_FILE	= "data/composable-xml-config-test-queues.xml";

	@Test
	public final void assertThatBuildProducesCompleteConfiguration()
	        throws FileNotFoundException {
		final ComposableXmlConfigBuilder objectUnderTest = new ComposableXmlConfigBuilder(
		        loadTestXmlAsStream(CONFIG_FILE));
		objectUnderTest
		        .addDistributedDataStructuresConfiguration(loadTestXmlAsFile(MAPS_FILE));
		objectUnderTest
		        .addDistributedDataStructuresConfiguration(loadTestXmlAsFile(QUEUES_FILE));

		final Config config = objectUnderTest.build();

		assertNotNull("build() returned null", config);
		assertFalse("build() returned a Config that contains no MapConfigs",
		        config.getMapConfigs().isEmpty());
		assertNotNull("build() returned a Config that contains no QueueConfig",
		        config.getQueueConfig("tasks"));
		assertNotNull(
		        "build() returned a Config that has not xmlConfig string set",
		        config.getXmlConfig());
	}

	private InputStream loadTestXmlAsStream(final String fileName)
	        throws FileNotFoundException {
		final InputStream result = getClass().getResourceAsStream(fileName);
		if (result == null) {
			throw new FileNotFoundException(fileName);
		}
		return result;
	}

	private File loadTestXmlAsFile(final String fileName)
	        throws FileNotFoundException {
		final URL resultUrl = getClass().getResource(fileName);
		if (resultUrl == null) {
			throw new FileNotFoundException(fileName);
		}
		return new File(resultUrl.getPath());
	}

}
