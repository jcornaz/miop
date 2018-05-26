# Change log

## 0.0.2-SNAPSHOT (Unreleased)

### Breaking changes
* remove the `capacity` argument from `combineLatest`
* `value` property provided `SubscritbableValue` and `SubscribableVariabel` is removed in favor of suspending `get()` and `set()` methods
* `SubscribableValue` and `SubscribableVariable` no longer implement `ReadOnlyProperty` and `ReadWriteProperty`

### New features
* Add `launchConsumeEach` to launch a consumer coroutine
* Add `SubscribableVariable.bind()` function to easily bind subscribable variables
* Add `distinctUntilChanged` operator

### JavaFx integration
Add `miop-javafx` module, which provide: 
* `ObservableValue.openValueSubscription()` extension function
* `ObservableValue.asSusbcribableValue()` adapter
* `Property.asSubscribableVariable()` adapter
 
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
