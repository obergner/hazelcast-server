hazelcast-server
================

A cache server based on the popular open-source distributed data grid [Hazelcast](http://www.hazelcast.com/).

Rationale
---------

[Hazelcast](http://www.hazelcast.com/), an in-memory data-grid, is usually deployed *in process*, i.e. an application *embeds* a hazelcast node running withing the same JVM. Also supported is running a cluster of dedicated hazelcast server instances where applications access that cluster remotely, via Hazelcast's [native client](http://www.hazelcast.com/docs/2.4/manual/multi_html/ch15.html#NativeClient).

The company I'm working for chose the latter option, hoping to thus reap some considerable advantages over the in process deployment:

* Operational visibility

  A centralised server may be more easily monitored and managed than a loose collection of Hazelcast nodes embedded in a diverse range of applications.
  
* Data lifecycle decoupled from application lifecycle

  When embedding Hazelcast directly into an application that application's cached data usually "dies" when it goes down, be that on purpose or by accident. In contrast, when offering caching as a separate service that data survives restarts.
  
* Reduced application complexity

  While embedding a Hazelcast node is almost insanely easy, it nonetheless burdens an application with increased complexity. Depending on the networking options you choose you have to configure multicast on the affected machines, have to drill holes into firewalls and so forth.
  
* More predictable application performance

  Offloading an application's caching needs to a dedicated server benefits it in at least two - to us - important aspects: more predictable startup time and more predictable memory consumption. We strive to keep our applications as small and focused as possible, and this helps a lot.
  
* One-stop shop for all our caching needs

  By setting up only one or at most a few dedicated Hazelcast clusters we hope to never have to worry again about caching in the future. This is a problem that's being taken care of.
  
* Better tuning options

  By keeping our applications' memory consumption low and predictable we hope to be able to tune them better for their specific purposes. In many cases we might be able to get around having to tune Java's notoriously complex garbage collection.
  
To coin YAB - Yet Another Buzzword - we want to introduce *Caching As A Service* (TM).

Requirements
------------

That's all nice and dandy, you may say, but why troubling yourself with implementing an own solution when Hazelcast offers one [out of the box](http://stackoverflow.com/a/10395372/1427088):

    java -cp hazelcast-2.0.3.jar com.hazelcast.examples.StartServer
 
Well, I don't know about you, but to me that's not exactly what I would call production-ready. In our situation we have to satisfy some requirements not covered by that innocent solution above:

* Runs as a linux service

  Yes, all our applications are supposed to be well-behaved linux services, supporting the usual
  
      /etc/init.d/application (start|stop|restart|status|condrestart)
    
* Supports "deploying" map etc. definitions per application

  We want a server each client may easily deploy map definitions into. In other words, we want a *modularized* server configuration, *not* one big kitchen-sink configuration file.
  
* Packaged as an RPM

  *Continuous Delivery* is a hot topic for us, and one building block in our solution is to have as many of our applications as feasible packaged and deployed as RPMs. Use your OS' native package management, the days of the venerable *.tar.gz* are over.
  
Solution
--------

As a proof of concept I implemented a Hazelcast server that does its best to fulfill all the requirements listed above. To be specific I

* found a [solution](https://github.com/obergner/hazelcast-server/tree/master/base/src/main/java/com/obergner/hzserver) for modularizing Hazelcast's usually monolithic XML configuration,
* exposed a few select MBean attributes via SNMP, using Torsten Curdt's excellent little [jmx2snmp](https://github.com/tcurdt/jmx2snmp) library (since operations is having a hard time integrating MBeans and [Icinga](https://www.icinga.org/)),
* plugged all the pieces into each other using [Spring](http://www.springsource.org/), an IOC container you might have heard of already,
* wrapped the result into an RPM package *hazelcast-server-base* using the rather useful [RPM Maven Plugin](http://mojo.codehaus.org/rpm-maven-plugin/), where that RPM includes neither configuration nor a service script (hint: Configurationless Application),
* bundled a sample Hazelcast XML configuration file and a script for running *hazelcast-server* as a linux service - using *Tanuki Software's* as always flawless [Java Service Wrapper](http://wrapper.tanukisoftware.com/doc/german/download.jsp) - into another RPM, *hazelcast-server-service* that declares a dependency on *hazelcast-server-base*, and
* bundled some sample map and queue defintions into yet another RPM, *hazelcast-server-app", to demonstrate how one would go about deploying application-specifif resources into a central Hazelcast server.

RPM package organization
------------------------

*hazelcast-server-base* and *hazelcast-server-service* install the following files and directories:

* `/usr/share/hazelcast-server/{bin,lib,logs,deploy}`

  *hazelcast-server's* `${HOME}`. Mainly contains its jar files in sub directory `lib`. 




