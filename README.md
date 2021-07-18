# lsd-distributed-interceptor
![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-interceptors)
[![Build](https://github.com/lsd-consulting/lsd-distributed-interceptors/actions/workflows/macos-build.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-interceptors/actions/workflows/macos-build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-interceptors.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.lsd-consulting%22%20AND%20a:%22lsd-distributed-interceptors%22)
![Codecov](https://img.shields.io/codecov/c/github/integreety/lsd-distributed-interceptor-library)

A set of interceptors gathering information from distributed sources for the LSD library.

![Interceptor diagram](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-distributed-interceptor-library.png?raw=true)

Here is a sample of an LSD that this library can generate:

![LSD](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-example.png?raw=true)

along with the source for the LSD:

![LSD](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-source-example.png?raw=true)


It also generates a component diagram:

![Component diagram](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-component-diagram-example.png?raw=true)

# Usage

To use the `lsd-distributed-interceptors` library just add it to the production dependencies:

```groovy
implementation "io.github.lsd-consulting:lsd-distributed-interceptor-library:1.0.0"
```

and configure through environment properties:

```properties
# the trust store configuration is optional
lsd.db.trustStorePassword={password}
lsd.db.trustStoreLocation={location}

lsd.db.connectionstring={someUrl}
```