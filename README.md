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
* bundled a sample Hazelcast XML configuration file and a script for running *hazelcast-server* as a linux service - using *Tanuki Software's* as always flawless [Java Service Wrapper](http://wrapper.tanukisoftware.com/doc/german/download.jsp) - into another RPM, *hazelcast-server-service*, that declares a dependency on *hazelcast-server-base*, and
* bundled some sample map and queue defintions into yet another RPM, *hazelcast-server-app*, to demonstrate how one would go about deploying application-specific resources into a central Hazelcast server.

RPM package organization
------------------------

*hazelcast-server-base* installs the following files and directories:

* `/etc/hazelcast-server/{hazelcast-server.properties,logback.xml,wrapper.conf}`

  *hazelcast-server's* configuration directory, containing some basic settings, [logback's](http://logback.qos.ch/) and *Java service Wrapper's* configuration files.
  
* `/var/lib/hazelcast-server/{deploy}`

  Where each application using *hazelcast-server* is supposed to deploy its map, queue a.s.f. definitions into. There is one subdirectory beneath `deploy/` per application.
  
* `/var/run/hazelcast-server`

  Where *hazelcast-server's* service script writes its pid file to.
  
* `/var/log/hazelcast-server`

  Where *hazelcast-server* writes its log files to.
  
* `/var/cache/{tmp,heapdumps}`

  Where tmpfiles and heapdumps are written to. *hazelcast-server* starts with `-Djava.io.tmpdir=/var/cache/tmp` and `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/cache/heapdumps`.

* `/usr/share/hazelcast-server/{bin,lib,conf,logs,deploy}`

  *hazelcast-server's* `${HOME}`. Mainly contains its jar files in sub directory `lib`, whereas `bin` contains files needed by *Java Service Wrapper*.  `conf`, `logs` and `deploy` are symlinks to `/etc/hazelcast-server`, `/var/log/hazelcast-server` and `/var/lib/hazelcast-server/deploy`, respectively.

* `/etc/logrotate.d/hazelcast-server`

  Simple [logrotate](http://en.gentoo-wiki.com/wiki/Logrotate) configuration for *hazelcast-server*.
  
*hazelcast-server-service* adds

* `/etc/hazelcast-server/hazelcast-server.xml`

  Hazelcast's main XML configuration file, *not* including any map or queue a.s.f. definitions.
  
* `/etc/init.d/hazelcast-server`

  *hazelcast-server's* service script for starting, stopping, restarting a.s.f.
  
Finally, *hazelcast-server-app* installs

* `/var/lib/hazelcast-server/deploy/app/{app-maps.xml,app-queues.xml}`

  Two sample files containing map and queue definitions which will be "deployed" upon server startup.
  
Caveat Emptor
-------------

While I have proven at least to my own satisfaction that the approach we chose basically works, some more or less serious shortcomings in *hazelcast-server's* current incarnation remain. Depending on context these may or may not be relevant in your specific circumstances.

* Not a turnkey solution

  It is unlikely that what you find here - with the possible exception of *hazelcast-server-base* - will fulfill your needs out of the box. You will probably need to adapt *hazelcast-server.xml* to your specific requirements and will therefore have to build your own *hazelcast-server-service* variant. Not a big deal, though. Same goes obviously for deploying your own applications' map a.s.f. definitions into *hazelcast-server*.
  
* No support for multiple Hazelcast instances

  As of today we don't have a need for multiple Hazelcast instances per JVM. Always eager to avoid needless complexity I didn't bother to implement support for this feature. Adding it should be straightforward, though, the only challenge being how to have deployed map definitions reference the Hazelcast instance they want to live in.
  
* No support for Hazelcast's [Distributed Executor Service](http://www.hazelcast.com/docs/2.4/manual/multi_html/ch09.html)

  This is a coooooool - sorry, the techy in me went overboard - feature, but again, we don't use it. Plus I'm not sure if I like the idea of *hazelcast-server* morphing into some kind of application container. The jury's still out on this. At any rate, I have one or two ideas how to implement this. Of course, you are always free and welcome to issue a pull request ;-)
  
* Modularized configuration somewhat hackish

  There, I said it. My approach to supporting modularized configuration in Hazelcast is a hack. Take a look at [this line](https://github.com/obergner/hazelcast-server/blob/master/base/src/main/java/com/obergner/hzserver/pluggable/ComposableXmlConfigBuilder.java#L76) in `ComposableXmlConfigBuilder.java` and try to tell me that this abomination in the face of Knuth does not pain the god-fearing, self-respecting coder in you. And the only excuse I may call to my aid once disdain and contempt will rightfully be heaped upon me is that pathetic "But it works! And it get's the job done!". Now, how lame's that!? But still ... OK, OK, I'll shut up.
  
But if you can will yourself to turn a blind eye to my little Frankenstein's shortcomings I truly hope that it will serve you well. I don't expect world domination any time soon, or world peace for that matter, but maybe it will help someone out there.

Contributing
------------

Fork it, clone it, take it to places where it's never been. Maybe issue a pull-request if you feel like it. Consider dropping me a note when you find *hazelcast-server* useful. File a bug report or a feature request. I won't promise anything, but I will surely look into it.

License
-------
