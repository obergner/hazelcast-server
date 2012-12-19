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

import java.util.Date;

public interface ServerInfoMBean {

	// ------------------------------------------------------------------------

	String getName();

	String getTitle();

	String getVersion();

	String getBranch();

	String getRevision();

	String getBuildNumber();

	String getBuildJdk();

	String getBuiltBy();

	// ------------------------------------------------------------------------

	String getJVM();

	String getClassPath();

	String getBootClassPath();

	String getLibraryPath();

	String getSystemProperties();

	Date getStartTime();

	long getUptimeMillis();

	// ------------------------------------------------------------------------

	String getOS();

	int getNumAvailableProcessors();

	// ------------------------------------------------------------------------

	int getInitialHeapMemoryInMBs();

	int getUsedHeapMemoryInMBs();

	int getCommittedHeapMemoryInMBs();

	int getMaxHeapMemoryInMBs();

	int getInitialNonHeapMemoryInMBs();

	int getUsedNonHeapMemoryInMBs();

	int getCommittedNonHeapMemoryInMBs();

	int getMaxNonHeapMemoryInMBs();
}
