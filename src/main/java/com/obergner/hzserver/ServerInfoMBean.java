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

import org.springframework.jmx.export.annotation.ManagedAttribute;

public interface ServerInfoMBean {

	@ManagedAttribute(description = "This server process' name")
	String getSpecificationTitle();

	@ManagedAttribute(description = "This server process' version")
	String getSpecificationVersion();

	@ManagedAttribute(description = "This server process' buildnumber")
	String getBuildNumber();

	@ManagedAttribute(description = "Which JDK built this server process")
	String getBuildJdk();

	@ManagedAttribute(description = "Who built this server process")
	String getBuiltBy();

}
