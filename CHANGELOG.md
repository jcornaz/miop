# Change log

## 0.0.3 (Unreleased)
### Breaking changes
* `launchConsumeEach` uses now `Unconfined` context by defauld

### Update dependencies
* kotlin: 1.2.50
* kotlinx.coroutines: 0.23.3

## 0.0.2 (2018-05-26)
Improvement operators and integration for `SubscribableValue` and `SubscribableVariable` as well as few new features  

### Breaking changes
* remove the `capacity` argument from `combineLatest`
* `value` property provided `SubscritbableValue` and `SubscribableVariabel` is removed in favor of suspending `get()` and `set()` methods
* `SubscribableValue` and `SubscribableVariable` no longer implement `ReadOnlyProperty` and `ReadWriteProperty`

### New features
* Add `launchConsumeEach` to launch a consumer coroutine
* Add `SubscribableVariable.bind()` function to easily bind subscribable variables
* Add `distinctUntilChanged` operator
* Add `IoPool` dispatcher
* Add `openSubscription` extension function on `Iterable` and `Sequence`

### JavaFx integration
Add `miop-javafx` module, which provide: 
* `openValueSubscription()` extension function on `ObservableValue`
* `ObservableValue.asSusbcribableValue()` adapter
* `Property.asSubscribableVariable()` adapter
 
### Dependencies
* Bump to kotlin 1.2.41

## 0.0.1 (2018-04-03)
First bunch of operators

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
