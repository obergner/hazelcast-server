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

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import com.hazelcast.config.Config;

/**
 * <p>
 * TODO: Document ComposableXmlConfigFactoryBean
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public class ComposableXmlConfigFactoryBean implements FactoryBean<Config>,
        InitializingBean {

	private Config	 product;

	private Resource	globalXmlConfigFile;

	private Resource	dataRootDirectory;

	private String	 dataFileSuffix;

	/**
	 * @param globalXmlConfigFile
	 *            The globalXmlConfigFile to set
	 */
	@Required
	public final void setGlobalXmlConfigFile(final Resource globalXmlConfigFile) {
		this.globalXmlConfigFile = globalXmlConfigFile;
	}

	/**
	 * @param dataRootDirectory
	 *            The dataRootDirectory to set
	 */
	@Required
	public final void setDataRootDirectory(final Resource dataRootDirectory) {
		this.dataRootDirectory = dataRootDirectory;
	}

	/**
	 * @param dataFileSuffix
	 *            The dataFileSuffix to set
	 */
	public final void setDataFileSuffix(final String dataFileSuffix) {
		this.dataFileSuffix = dataFileSuffix;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public Config getObject() throws Exception {
		checkState(
		        this.product != null,
		        "No Config has been created yet - did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		return this.product;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass() : Config.class;
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
		checkState(this.dataRootDirectory != null,
		        "Property 'dataRootDirectory' has not been set");
		checkState(this.globalXmlConfigFile != null,
		        "Property 'globalXmlConfigFile' has not been set");
		InputStream globalXmlConfigStream = null;
		try {
			globalXmlConfigStream = this.globalXmlConfigFile.getInputStream();
			final ComposableXmlConfigBuilder configBuilder = new ComposableXmlConfigBuilder(
			        globalXmlConfigStream);
			final DataFileScanCallback dataFileScanCallback = new DataFileScanCallback(
			        configBuilder);

			final DirectoryScanner directoryScanner = new DirectoryScanner(
			        this.dataRootDirectory.getFile(), this.dataFileSuffix,
			        dataFileScanCallback);
			directoryScanner.scan();

			this.product = configBuilder.build();
		} finally {
			if (globalXmlConfigStream != null) {
				globalXmlConfigStream.close();
			}
		}
	}

	private static final class DataFileScanCallback implements
	        DirectoryScanner.Callback {

		private final ComposableXmlConfigBuilder	configBuilder;

		DataFileScanCallback(final ComposableXmlConfigBuilder configBuilder) {
			this.configBuilder = configBuilder;
		}

		@Override
		public void scanned(final File scannedFile) {
			this.configBuilder
			        .addDistributedDataStructuresConfiguration(scannedFile);
		}
	}
}
