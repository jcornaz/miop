# Change log

## 0.0.4-SNAPSHOT (Unreleased)
### Fixes
* Fix exception when `distinctUntilChanged` or `distinctReferenceUntilChanged` was invoked on an empty source.

### Dependencies update
* kotlinx.coroutines: 0.24.0

## 0.0.3 (2018-07-23)
Multi-platform support, StateStore and JavaFx integration improvement.

### Breaking changes
* `launchConsumeEach` uses `Unconfined` context by default
* Remove `bind` extension functions for `SubscribableVariable`. It was an error-prone way of keeping up-to-date a subscribable variable.

### New features
* `StateStore` is an equivalent concept as "store" in [redux](https://redux.js.org/).
  It stores an immutable state and actions may be dispatched in order to move to a new state and notify all subscribers
* `StateStore.map(transformState, transformAction)` makes possible to create a *view* of a StateStore
* `SubscribableValue.openSubscription(transform: (T) -> R)` simplify mapping result of a subscription

### JavaFx Integration
* `ObservableList.openListSubscription()` extension function
* `ReceiveChannel.launchUpdater` extension functions to easily start an update job for `Property` or `ObservableList`

### Enhancements
* All operator always consumes the sources, even if they don't have the time to start at all

### Dependencies update
* kotlin: 1.2.51
* kotlinx.coroutines: 0.23.4

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
