## TODOs:
- [x] Increase coverage
- [ ] Write README
- [x] Add Jacoco
- [x] Add Pitest
- [ ] Intercept the exchange name and routing key on rabbitTemplate (AOP?)
- [x] Prepare a report that will show what interaction was mapped to what name
- [x] Prepare a report that will show which user supplied name mappings were now used
- [x] Fix the order of loading beans in the E2E test (create MongoDb instance before the Mongo client - causes an exception currently when running the test)
- [ ] Add timings to the LSD
- [ ] Store the last successful run's traceId, so it's possible to create an automatic diff the next time the test fails
- [ ] Store more information about requests/responses, eg. headers, path, host, status

# lsd-distributed-interceptor-library 
[ ![Download](https://api.bintray.com/packages/integreety/open/lsd-distributed-interceptor-library/images/download.svg) ](https://bintray.com/integreety/open/lsd-distributed-interceptor-library/_latestVersion)
![GitHub](https://img.shields.io/github/license/integreety/lsd-distributed-interceptor-library) 
![CircleCI](https://img.shields.io/circleci/build/gh/integreety/lsd-distributed-interceptor-library)
![Codecov](https://img.shields.io/codecov/c/github/integreety/lsd-distributed-interceptor-library)
![Bintray](https://img.shields.io/bintray/dt/integreety/open/lsd-distributed-interceptor-library)

A set of interceptors gathering information for the LSD (Yatspec) library.

![Interceptor diagram](https://github.com/integreety/lsd-distributed-interceptor-library/blob/master/image/lsd-distributed-interceptor-library.png?raw=true)
