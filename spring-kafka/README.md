# spring-kafka interceptors

This module provides a Spring based Kafka interceptor as well as a non-Spring version.

## Usage
TODO

## Target determination order
TODO

## Tests
### &#95;&#95;TypeId&#95;&#95; header

`SpringKafkaInteractionHttpRecordingIT` uses `KafkaTemplate` which generates the &#95;&#95;TypeId&#95;&#95; header
therefore making it possible to test it.

`KafkaInteractionHttpRecordingIT` on the other hand doesn't use `KafkaTemplate`. It uses a `KafkaProducer` directly
which doesn't generate the &#95;&#95;TypeId&#95;&#95; header. That's why this case is not tested
by `KafkaInteractionHttpRecordingIT`.  
