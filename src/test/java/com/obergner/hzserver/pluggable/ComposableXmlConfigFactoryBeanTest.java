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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.hazelcast.config.Config;

public class ComposableXmlConfigFactoryBeanTest {

	private static final String	CONFIG_FILE	= "global/composable-xml-config-test-config.xml";

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectThrowsIllegalStateExceptionIfCalledBeforeAfterPropertiesSet()
	        throws Exception {
		final ComposableXmlConfigFactoryBean objectUnderTest = newObjectUnderTest();

		objectUnderTest.getObject();
	}

	private ComposableXmlConfigFactoryBean newObjectUnderTest() {
		final ComposableXmlConfigFactoryBean objectUnderTest = new ComposableXmlConfigFactoryBean();
		objectUnderTest.setGlobalXmlConfigFile(new ClassPathResource(
		        CONFIG_FILE, getClass()));
		objectUnderTest.setDataRootDirectory(new ClassPathResource("data/",
		        getClass()));
		objectUnderTest.setDataFileSuffix(".xml");
		return objectUnderTest;
	}

	@Test
	public final void assertThatGetObjectTypeReturnsAppropriateType() {
		final ComposableXmlConfigFactoryBean objectUnderTest = newObjectUnderTest();
		assertTrue("getObjectType() should have returned a subtype of "
		        + Config.class.getName(),
		        Config.class.isAssignableFrom(objectUnderTest.getObjectType()));
	}

	@Test
	public final void assertThatIsSingletonReturnsTrue() {
		final ComposableXmlConfigFactoryBean objectUnderTest = newObjectUnderTest();
		assertTrue("isSingleton() should have returned true",
		        objectUnderTest.isSingleton());
	}

	@Test
	public final void assertThatAfterPropertiesSetCorrectlyCreatesConfig()
	        throws Exception {
		final ComposableXmlConfigFactoryBean objectUnderTest = newObjectUnderTest();
		objectUnderTest.afterPropertiesSet();

		final Config config = objectUnderTest.getObject();

		assertNotNull(
		        "Calling getObjec() after calling afterPropertiesSet() should have returned a non-null Config instance",
		        config);
	}
}
