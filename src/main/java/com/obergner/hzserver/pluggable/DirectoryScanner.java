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
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TODO: Document DirectoryScanner
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public class DirectoryScanner {

	public interface Callback {

		void scanned(File scannedFile);
	}

	private static final String	DEFAULT_SUFFIX	= ".xml";

	private final Logger	    log	           = LoggerFactory
	                                                   .getLogger(getClass());

	private final File	        rootDirectory;

	private final String	    suffix;

	private final Callback	    callback;

	/**
	 * @param rootDirectory
	 * @param suffix
	 * @param callback
	 */
	public DirectoryScanner(final File rootDirectory, final String suffix,
	        final Callback callback) {
		this.rootDirectory = checkNotNull(rootDirectory,
		        "Argument 'rootDirectory' must not be null");
		checkState(rootDirectory.canRead(),
		        "Root directory %s is not readable", rootDirectory);
		this.suffix = suffix != null ? suffix : DEFAULT_SUFFIX;
		this.callback = checkNotNull(callback,
		        "Argument 'callback' must not be null");
	}

	public void scan() {
		this.log.info("Scanning files in directory [{}] matching [*{}] ...",
		        this.rootDirectory, this.suffix);
		final FileFilter suffixBasedFilter = new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isFile()
				        && pathname.getName().endsWith(
				                DirectoryScanner.this.suffix);
			}
		};
		final File[] allMatchingFiles = this.rootDirectory
		        .listFiles(suffixBasedFilter);
		this.log.debug("Found {} files matching [*{}] in directory [{}]: {}",
		        new Object[] { allMatchingFiles.length, this.suffix,
		                this.rootDirectory, Arrays.toString(allMatchingFiles) });
		for (final File matchingFile : allMatchingFiles) {
			scanFile(matchingFile);
		}
		this.log.info("Scanned {} files in directory [{}] matching [*{}] ...",
		        allMatchingFiles.length, this.rootDirectory, this.suffix);
	}

	private void scanFile(final File file) {
		try {
			this.log.debug("Scanning file [{}] ...", file);
			this.callback.scanned(file);
			this.log.debug("File [{}] scanned", file);
		} catch (final Exception e) {
			this.log.error("Failed to scan file [" + file
			        + "] - will be skipped", e);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DirectoryScanner@" + this.hashCode() + "[rootDirectory: "
		        + this.rootDirectory + "|suffix: " + this.suffix
		        + "|callback: " + this.callback + "]";
	}
}
