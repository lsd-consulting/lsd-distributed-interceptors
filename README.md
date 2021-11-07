# lsd-distributed-interceptor
![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-interceptors)
[![Build](https://github.com/lsd-consulting/lsd-distributed-interceptors/actions/workflows/macos-build.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-interceptors/actions/workflows/macos-build.yml)
![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-interceptor)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-interceptors)

A set of interceptors gathering information from distributed sources for the LSD library.

![Interceptor diagram](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-distributed-interceptor.png?raw=true)

Here is a sample of an LSD that this library can generate:

![LSD](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-example.png?raw=true)

along with the source for the LSD:

![LSD Source](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-source-example.png?raw=true)


It also generates a component diagram:

![Component diagram](https://github.com/lsd-consulting/lsd-distributed-interceptors/blob/master/image/lsd-component-diagram-example.png?raw=true)

# Usage

To use the `lsd-distributed-interceptors` library just add it to the production dependencies:

```groovy
implementation "io.github.lsd-consulting:lsd-distributed-interceptor:<version>"
```

and configure through app properties:

```properties
# the trust store configuration is optional
lsd.dist.db.trustStorePassword={password}
lsd.dist.db.trustStoreLocation={location}

lsd.dist.db.connectionString={someUrl}
```

## Obfuscation
If there is need to obfuscate sensitive information sent through HTTP headers, it's possible to set the headers to be obfuscated through a property:
```properties
lsd.dist.obfuscator.sensitiveHeaders=Authorization, SomeOtherHeaderName
```

If the value is not set or empty, there will be no obfuscation applied.
