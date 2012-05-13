Eclipse Buildroot Toolchain plugin
==================================

Introduction
------------

This Eclipse plugin allows to integrate Buildroot project toolchains with Eclipse CDT.

[Buildroot](http://buildroot.org) is a tool to build embedded Linux system using cross-compilation. It allows to build a cross-compilation toolchain (or re-use an existing one), a root filesystem image with applications and libraries, a kernel image and a bootloader image or any combination of these.

Since Buildroot generates a toolchain and install libraries and headers to develop applications for the target embedded system, it is desirable to easily access these toolchains from Eclise CDT. Accessing those toolchains allows to build application for the target directly within Eclipse.

Usage
-----

Once this plugin is installed in Eclipse, it will automatically make your Buildroot toolchains appear in the C/C++ Settings of your C/C++ projects.

In order for Buildroot toolchains to be visible, you must enable the `BR2_ECLIPSE_REGISTER` Buildroot option when building your project. The Eclipse plugin will then show all toolchains of Buildroot projects that had this option enabled.

Implementation
--------------

When a Buildroot project is built with `BR2_ECLIPSE_REGISTER`, it adds a few information describing the generated toolchain into `$HOME/.buildroot-eclipse.toolchains`. The Eclipse plugin reads this file, and then creates the necessary objects in Eclipse to make these toolchains usable for C/C++ projects.

This plugin has been developed by re-using code from the [Eclipse Blackfin plugin](http://docs.blackfin.uclinux.org/doku.php?id=toolchain:eclipse:install) and the [GNU ARM Eclipse plugin](http://sourceforge.net/projects/gnuarmeclipse/). Compared to these plugins, the Buildroot Eclipse plugin is much more dynamic: it allows to dynamically register an arbitrary number of toolchains.

Authors
-------

* Mélanie Bats, <melanie.bats@obeo.fr>, Eclipse plug-in development
* Thomas Petazzoni, <thomas.petazzoni@free-electrons.com>, Buildroot integration

