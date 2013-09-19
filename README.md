Beeline
=======

Simple mobile application (Android) for displaying the linear distance between locations.

Beeline lets you add locations by a geo coordinate (latitude/longitude) and calculates their linear distance (bee-line) to your current location.


Build using Eclipse
-------------------

[Eclipse](http://eclipse.org/) project files are *not* included. Just use the [ADT plugin](http://developer.android.com/tools/sdk/eclipse-adt.html) for Eclipse and create a new project from existing sources. Make sure to select `app/` as the base directory.

Build using Ant
---------------

There is a `build.xml` in the `app/` directory for use with [Ant](http://ant.apache.org/).

```sh
$ ant
$ ant debug
$ ant release
```

