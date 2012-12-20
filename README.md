hazelcast-server
================

A cache server based on the popular open-source distributed data grid [Hazelcast](http://www.hazelcast.com/).

Synopsis
--------

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
  
