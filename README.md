## TODOs:
- [x] Increase coverage
- [ ] Write README
- [x] Add Jacoco
- [x] Add Pitest
- [ ] Intercept the exchange name and routing key on rabbitTemplate (AOP?)
- [x] Prepare a report that will show what interaction was mapped to what name
- [x] Prepare a report that will show which user supplied name mappings were now used
- [ ] Fix the order of loading beans in the E2E test (create MongoDb instance before the Mongo client - causes an exception currently when running the test)

# lsd-distributed-interceptor-library 
[ ![Download](https://api.bintray.com/packages/integreety/open/lsd-distributed-interceptor-library/images/download.svg) ](https://bintray.com/integreety/open/lsd-distributed-interceptor-library/_latestVersion)
![GitHub](https://img.shields.io/github/license/integreety/lsd-distributed-interceptor-library) 
![CircleCI](https://img.shields.io/circleci/build/gh/integreety/lsd-distributed-interceptor-library)
![Codecov](https://img.shields.io/codecov/c/github/integreety/lsd-distributed-interceptor-library)
![Bintray](https://img.shields.io/bintray/dt/integreety/open/lsd-distributed-interceptor-library)

A set of interceptors gathering information for the LSD (Yatspec) library.
