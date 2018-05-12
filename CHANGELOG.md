# Change log

## 0.0.2-SNAPSHOT (Unreleased)
### Operators
* **[BREAKING CHANGE]** remove the `capacity` argument from `combineLatest` 
* Add `launchConsumeEach` to launch a consumer coroutine

### Dependencies
* Bump to kotlin 1.2.41

## 0.0.1 (2018-04-03)
### Builders
* Add `emptyReceiveChannel(): ReceiveChannel<T>`
* Add `receiveChannelOf(vararg values: T): ReceiveChannel<T>`

### Merge operators
* Add `merge` and `mergeWith`
* Add `combineLatest` and `combineLatestWith`
* Add `switchMap`

### Subscribables
* Add `SubscribableValue` and `SubscribableVariable`
* Add `map`, `switchMap` and `combine` operators for `SubscribableValue`
