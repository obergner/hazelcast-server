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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.obergner.hzserver.HazelcastService;
import com.yammer.metrics.Metrics;

public class HazelcastServiceTest {

	@After
	public void shutdownAllHazelcastInstances() {
		Hazelcast.shutdownAll();
	}

	@Test
	public final void assertThatStartCreatesHazelcastInstance() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.start();

		assertNotNull("Calling start did not create a HazelcastInstance",
		        objectUnderTest.getHazelcastInstance());
	}

	private HazelcastService newObjectUnderTest() {
		return new HazelcastService(new Config(), Metrics.defaultRegistry());
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatCallingStartTwiceInARowFails() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.start();
		objectUnderTest.start();
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatCallingStopWithoutHavingCalledStartFails() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.stop();
	}

	@Test
	public final void assertThatStopDeletesHazelcastInstance() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.start();
		objectUnderTest.stop();

		assertNull("Calling stop() should have deleted HazelcastInstance",
		        objectUnderTest.getHazelcastInstance());
		assertTrue(
		        "Calling stop left behind a least one running HazelcastInstance",
		        Hazelcast.getAllHazelcastInstances().isEmpty());
	}

	@Test
	public final void assertThatIsRunningReturnsFalseIfStartHasNotBeenCalled() {
		final HazelcastService objectUnderTest = newObjectUnderTest();

		assertFalse("isRunning() should return false prior to calling start()",
		        objectUnderTest.isRunning());
	}

	@Test
	public final void assertThatIsRunningReturnsTrueAfterCallingStart() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.start();

		assertTrue("isRunning() should return true after to calling start()",
		        objectUnderTest.isRunning());
	}

	@Test
	public final void assertThatGetPhaseReturnsIntegerMaxValue() {
		final HazelcastService objectUnderTest = newObjectUnderTest();

		assertEquals("getPhase() should return Integer.MAX_VALUE",
		        Integer.MAX_VALUE, objectUnderTest.getPhase());
	}

	@Test
	public final void assertThatIsAutoStartupReturnsTrue() {
		final HazelcastService objectUnderTest = newObjectUnderTest();

		assertTrue("isAutoStartup() should return true",
		        objectUnderTest.isAutoStartup());
	}

	@Test
	public final void assertThatStopCallsRunOnProvidedRunnable() {
		final HazelcastService objectUnderTest = newObjectUnderTest();
		objectUnderTest.start();

		final AtomicBoolean runCalled = new AtomicBoolean(false);
		final Runnable stopRunnable = new Runnable() {
			@Override
			public void run() {
				runCalled.set(true);
			}
		};
		objectUnderTest.stop(stopRunnable);

		assertTrue(
		        "stop(Runnable) should have called run() on the provided Runnable",
		        runCalled.get());
	}

}
