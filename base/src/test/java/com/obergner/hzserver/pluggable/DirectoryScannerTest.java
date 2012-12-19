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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class DirectoryScannerTest {

	@Test
	public final void assertThanScanCallsCallbackForEveryMatchingFile() {
		final File rootDir = new File(getClass().getResource("data/").getPath());
		final AtomicInteger noScannedFiles = new AtomicInteger(0);
		final DirectoryScanner objectUnderTest = new DirectoryScanner(rootDir,
		        ".xml", new DirectoryScanner.Callback() {

			        @Override
			        public void scanned(final File scannedFile) {
				        noScannedFiles.incrementAndGet();
			        }
		        });

		objectUnderTest.scan();

		assertEquals(
		        "Scanning directory should have called callback two times for two matching files",
		        2, noScannedFiles.get());
	}
}
