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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.impl.Util;

/**
 * <p>
 * TODO: Document ComposableXmlConfigBuilder
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public class ComposableXmlConfigBuilder {

	private final Logger	                log	= LoggerFactory
	                                                    .getLogger(getClass());

	private final ComposableXmlConfigParser	configParser;

	public ComposableXmlConfigBuilder(final String xmlFileName)
	        throws FileNotFoundException {
		this(new FileInputStream(xmlFileName));
	}

	public ComposableXmlConfigBuilder(final InputStream xmlFileInputStream) {
		this.configParser = new ComposableXmlConfigParser(xmlFileInputStream);
	}

	public ComposableXmlConfigBuilder addDistributedDataStructuresConfiguration(
	        final File distributedDataStructuresConfigFile) {
		this.configParser
		        .addDistributedDataStructuresConfiguration(distributedDataStructuresConfigFile);
		return this;
	}

	/**
	 * @return
	 * @see com.hazelcast.config.XmlConfigBuilder#build()
	 */
	public Config build() {
		try {
			this.log.info("Building Hazelcast configuration ...");
			final Document configDoc = this.configParser.parse();
			final Config config = new XmlConfigBuilder(
			        new ByteArrayInputStream("IGNORED".getBytes()))
			        .build(configDoc.getDocumentElement());

			injectXmlConfigIntoConfig(config, configDoc);

			this.log.info("Hazelcast configuration successfully built: {}",
			        config);
			return config;
		} catch (final Exception e) {
			throw new RuntimeException(
			        "Failed to parse Hazelcast server configuration: "
			                + e.getMessage(), e);
		}
	}

	private void injectXmlConfigIntoConfig(final Config config,
	        final Document configDoc) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Util.streamXML(configDoc, baos);
		final byte[] bytes = baos.toByteArray();
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		config.setXmlConfig(Util.inputStreamToString(bais));
	}
}
