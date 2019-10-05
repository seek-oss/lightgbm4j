# LightGBM4J

A JVM interface 🌯 for [LightGBM](https://github.com/microsoft/LightGBM), written in Scala, for inference in production.

Hopefully makes it a littler easier to use LightGBM from Java/Scala compared to using the SWIG wrappers directly.

LightGBM4J:
* Provides a version of the native linux `lib_lightgbm.so` library compiled without OpenMP. This reduces latency when LightGBM is used to predict concurrently in a multi-threaded application (eg: a http server).
* Is self-contained. The native libraries are packaged together with this library, and loaded for you.
* Correctly handles marshalling and memory management between your application and native code
* Includes Mac OS X native libraries for local development

See the [tests](src/test/scala/au/com/seek/lightgbm4j) for example usage.

## Usage

`make test` run tests  
`make build-libs-linux version=v2.2.2` build specific version of native libs for linux  
`make build-libs-mac version=v2.2.2` build specific version of native libs for mac  


## Who's using this?

Used in production at [SEEK](https://www.seek.com.au).


