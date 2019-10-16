![](https://github.com/seek-oss/lightgbm4j/workflows/CI/badge.svg)

# LightGBM4J

A JVM interface for [LightGBM](https://github.com/microsoft/LightGBM), written in Scala, for inference in production.

Hopefully makes it a littler easier to use LightGBM from Java/Scala compared to using the SWIG wrappers directly.

LightGBM4J:
* Provides a version of the native linux `lib_lightgbm.so` library compiled without OpenMP. This reduces latency when 
LightGBM is used to predict concurrently in a multi-threaded application (eg: a http server).
* Is self-contained. The native libraries are packaged together with this library, and loaded for you.
* Correctly handles marshalling and memory management between your application and native code
* Includes Mac OS X native libraries for local development

See the [tests](src/test/scala/au/com/seek/lightgbm4j) for example usage.

### Versions 

The LightGBM4J version uses the base LightGBM version with LightGBM4J release appended with a dash, e.g. `2.2.2-1` is 
the first LightGBM4J release based on LightGBM4J version `2.2.2`.

## Usage

`make test` run tests  
`make build-libs-linux` build native libs for linux  
`make build-libs-mac` build native libs for mac


## Who's using this?

Used in production at [SEEK](https://www.seek.com.au).


