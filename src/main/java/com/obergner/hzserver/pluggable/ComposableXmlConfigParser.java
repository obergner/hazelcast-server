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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * <p>
 * TODO: Document ComposableXmlConfigParser
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
class ComposableXmlConfigParser {

	private final static String	QUEUE_NODES_SELECTOR	                = "//hazelcast/queue";

	private final static String	MAP_NODES_SELECTOR	                    = "//hazelcast/map";

	private final static String	MULTIMAP_NODES_SELECTOR	                = "//hazelcast/multimap";

	private final static String	TOPIC_NODES_SELECTOR	                = "//hazelcast/topic";

	private final static String	SEMAPHORE_NODES_SELECTOR	            = "//hazelcast/semaphore";

	private final InputStream	xmlConfigInputStream;

	private final Set<File>	    distributedDataStructuresConfigurations	= new HashSet<File>();

	/**
	 * @param xmlConfigInputStream
	 */
	ComposableXmlConfigParser(final InputStream xmlConfigInputStream) {
		this.xmlConfigInputStream = checkNotNull(xmlConfigInputStream,
		        "Argument 'xmlConfigInputStream' must not be null");
	}

	ComposableXmlConfigParser addDistributedDataStructuresConfiguration(
	        final File distributedDataStructuresConfigFile) {
		checkNotNull(distributedDataStructuresConfigFile,
		        "Argument 'distributedDataStructuresConfigFile' must not be null");
		this.distributedDataStructuresConfigurations
		        .add(distributedDataStructuresConfigFile);
		return this;
	}

	Document parse() throws Exception {
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		        .newDocumentBuilder();
		final Document result = builder.parse(this.xmlConfigInputStream);

		final XPathFactory xpathFactory = XPathFactory.newInstance();

		insertDistributedDataStructuresConfigurations(builder, xpathFactory,
		        result.getDocumentElement());

		return result;
	}

	private void insertDistributedDataStructuresConfigurations(
	        final DocumentBuilder builder, final XPathFactory xpathFactory,
	        final Node hazelcastNode) throws Exception {
		for (final File mapConfigFile : this.distributedDataStructuresConfigurations) {
			appendFragmentToParentNode(builder, xpathFactory, hazelcastNode,
			        mapConfigFile, QUEUE_NODES_SELECTOR);
			appendFragmentToParentNode(builder, xpathFactory, hazelcastNode,
			        mapConfigFile, MAP_NODES_SELECTOR);
			appendFragmentToParentNode(builder, xpathFactory, hazelcastNode,
			        mapConfigFile, MULTIMAP_NODES_SELECTOR);
			appendFragmentToParentNode(builder, xpathFactory, hazelcastNode,
			        mapConfigFile, TOPIC_NODES_SELECTOR);
			appendFragmentToParentNode(builder, xpathFactory, hazelcastNode,
			        mapConfigFile, SEMAPHORE_NODES_SELECTOR);
		}
	}

	private void appendFragmentToParentNode(
	        final DocumentBuilder documentBuilder,
	        final XPathFactory xpathFactory, final Node parentNode,
	        final File xmlFragmentFile, final String nodesToAppendSelector)
	        throws Exception {
		final Document importedDocument = documentBuilder
		        .parse(new InputSource(new FileReader(xmlFragmentFile)));

		final XPath selector = xpathFactory.newXPath();
		final XPathExpression selectorExpr = selector
		        .compile(nodesToAppendSelector);
		final NodeList nodesToAppend = (NodeList) selectorExpr.evaluate(
		        importedDocument, XPathConstants.NODESET);

		for (int i = 0; i < nodesToAppend.getLength(); i++) {
			final Node nodeToAppend = nodesToAppend.item(i);
			final Node importedNodeToAppend = parentNode.getOwnerDocument()
			        .importNode(nodeToAppend, true);
			parentNode.appendChild(importedNodeToAppend);
		}
	}
}
