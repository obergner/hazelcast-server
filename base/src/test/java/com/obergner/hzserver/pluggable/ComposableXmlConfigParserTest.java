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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ComposableXmlConfigParserTest {

	private static final String	CONFIG_FILE	= "global/composable-xml-config-test-config.xml";

	private static final String	MAPS_FILE	= "data/subdir/composable-xml-config-test-maps.xml";

	private static final String	QUEUES_FILE	= "data/subdir/composable-xml-config-test-queues.xml";

	@Test(expected = NullPointerException.class)
	public final void assertThatConstructorRejectsNullInputStream() {
		new ComposableXmlConfigParser(null);
	}

	@Test(expected = NullPointerException.class)
	public final void asserThatAddMapConfigurationRejectsNullFile() {
		final ComposableXmlConfigParser objectUnderTest = new ComposableXmlConfigParser(
		        new ByteArrayInputStream("DUMMY".getBytes()));
		objectUnderTest.addDistributedDataStructuresConfiguration(null);
	}

	@Test
	public final void assertThatParseSuccessfullyParsesConfigWithoutAddedDataStructures()
	        throws Exception {
		final ComposableXmlConfigParser objectUnderTest = new ComposableXmlConfigParser(
		        loadTestXmlAsStream(CONFIG_FILE));
		final Document document = objectUnderTest.parse();

		assertNotNull(document);
	}

	private InputStream loadTestXmlAsStream(final String fileName)
	        throws FileNotFoundException {
		final InputStream result = getClass().getResourceAsStream(fileName);
		if (result == null) {
			throw new FileNotFoundException(fileName);
		}
		return result;
	}

	@Test
	public final void assertThatParseSuccessfullyParsesConfigWithMapsAdded()
	        throws Exception {
		final ComposableXmlConfigParser objectUnderTest = new ComposableXmlConfigParser(
		        loadTestXmlAsStream(CONFIG_FILE));
		objectUnderTest
		        .addDistributedDataStructuresConfiguration(loadTestXmlAsFile(MAPS_FILE));
		final Document document = objectUnderTest.parse();

		assertNotNull(document);
		assertNodePresent(document, "//hazelcast/map");
	}

	private File loadTestXmlAsFile(final String fileName)
	        throws FileNotFoundException {
		final URL resultUrl = getClass().getResource(fileName);
		if (resultUrl == null) {
			throw new FileNotFoundException(fileName);
		}
		return new File(resultUrl.getPath());
	}

	private void assertNodePresent(final Document doc, final String xpath)
	        throws XPathExpressionException {
		final XPath xpathInst = XPathFactory.newInstance().newXPath();
		final XPathExpression xpathExpr = xpathInst.compile(xpath);
		final NodeList matchingNodes = (NodeList) xpathExpr.evaluate(
		        doc.getDocumentElement(), XPathConstants.NODESET);
		if (matchingNodes.getLength() == 0) {
			throw new AssertionFailedError("Could not find node(s) matching \""
			        + xpath + "\" in document \"" + doc + "\"");
		}
	}

	@Test
	public final void assertThatParseSuccessfullyParsesConfigWithQueuesAdded()
	        throws Exception {
		final ComposableXmlConfigParser objectUnderTest = new ComposableXmlConfigParser(
		        loadTestXmlAsStream(CONFIG_FILE));
		objectUnderTest
		        .addDistributedDataStructuresConfiguration(loadTestXmlAsFile(QUEUES_FILE));
		final Document document = objectUnderTest.parse();

		assertNotNull(document);
		assertNodePresent(document, "//hazelcast/queue");
	}
}
