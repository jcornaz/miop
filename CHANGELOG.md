# Change log

## 0.3.1-SNAPSHOT (Unreleased)

## 0.3.0 (2018-12-10)
Kotlin 1.3 and parallelization 

### Added
* `produceAtomic` coroutine builder. An alternative to `produce` which guarantee that the block is always invoked.
* `parallel` operator to easily build parallel pipelines.
* `parallelMap` and `parallelFilter` operators.
* `produce(Iterable)` and `produce(Sequence)` functions. They replace `Iterable.openSubscription`, `Sequence.openSubscription` which are deprecated.
* `ReceiveChannel.sendTo(SendChannel)` suspending extension function which allow to pipe a `ReceiveChannel` to a `SendChannel`.
* `scan` operator allowing to accumulate elements and send the intermediate accumulation results.
 
### Dependencies
* Update Kotlin from `1.3.0-rc-57` to `1.3.10`
* Update kotlinx.coroutines from `0.26.1-eap13` to `1.0.0`
* **[BREAKING]** Hide dependencies on `collekt-core-*` and publish the dependency on `collekt-api-*` only (in modules `miop-java` and `miop-collekt-*`)
* Update dependency of collekt from `0.0.2` to `0.0.3`

### New xperimental statuses
* All channel operators have been marked with `ObsoleteCoroutinesApi`
* Factory functions have been marked with `ExperimentalCoroutinesApi`
   
## 0.2.0 (2018-09-23)
Migrate to Kotlin 1.3 RC and coroutines 0.26 (Structured concurrency)

### Added
* `CoroutineScope.launchFx*Updater` functions to replace `ReceiveChannel.launchFx*Updater` functions (in `miop-javafx` module)
* `CoroutineScope.launchFxListUpdater` capable of react to *key-set* subscription, creating and disposing the item of the updated list (in `miop-javafx` module)
* Operators `windowed` and `chunked`
* `failedReceiveChannel` factory function
* integration modules for [collekt](https://github.com/jcornaz/collekt): (`miop-collekt-common`, `miop-collekt-jvm` and `miop-collekt-js`)
    * Provide `PersistentMap.plus(MapEvent)` and `PersistentSet.plus(SetEvent)` operators$
  
### Changed
* **[Breaking]** `ReceiveChannel.transform` provides a `CoroutineScope` as a receiver for the lambda
* **[Breaking]** `StateStore<S, (S) -> S>(initialState)` factory signature has been simplified to `StateStore<S>(initialState: S)` 

### Marked Experimental
* `com.github.jcornaz.miop.property.ExperimentalSubscribable` (level=warning) is applied to `SubcribableValue`,  `SubscribableVariable`, `StateStore` and related functions
* **[Breaking]** `com.github.jcornaz.miop.collection.ExperimentalCollectionEvent` (level=error) is applied to collection events and related functions 

### Deprecated
* `launchConsumeEach` (use `launch { consumeEach {} }` instead)
* `ReceiveChannel.launchFx*Updater` functions (use `CoroutineScope.launchFx*Updater` functions instead)
* `SubscribableValue` adapters for JavaFx properties (use updaters and subscriptions instead)
* `IoPool` (use kotlinx.coroutines' `Dispatchers.IO` instead)

### Dependencies updated
* **[Breaking]** Kotlin: 1.3-rc-57
* **[Breaking]** kotlinx.coroutines: 0.26.1-eap13

## 0.1.0 (2018-08-26)
Migration to Kotlin 1.3 and a bunch of new operators

### Changed
* **[Breaking]** Kotlin updated to `1.3-M1`
* **[Breaking]** Kotlinx.coroutines updated to `0.25.0-eap13`
* **[Breaking]** Packages renamed. (`experimental` has been dropped)  

### Added
* `toSetEvents` and `toMapEvents` operators for channel which compute delta of emitted set/map and emits the corresponding events.
* `filterIsInstance` operator for channels
* `debounce` operator
* `buffer` and `conflate` operators

### Deprecated
* `IoPool` in jvm module has been deprecated in favor of `kotlinx.coroutines.IO`

## 0.0.4 (2018-07-28)
Bug fix, improvement of the semantic of StateStore and new JavaFx updaters.

### Changed
* **[break source compatibility]** In `EventStore` `action` arguments have been renamed `event`.
* **[break compatibility]** In `EventStore.dispatch` has been made an extension function and `suspend fun handle(event: E): S` has been added in the interface.
* **[break binary compatibility]** Provide a `context` argument for all operators taking a suspending lambda argument.     

### Fixed
* Exception when `distinctUntilChanged` or `distinctReferenceUntilChanged` was invoked on an empty source.

### Dependencies updated
* kotlinx.coroutines: 0.24.0

### Module 'miop-javafx'
#### Changed
* **[break compatibility]** `launchUpdater` functions have been renamed for `launchFxUpdater` and `launchFxListUpdater`

#### Added
* `launchFxCollectionUpdater` which keep up-to-date the collection ignoring the order of the elements. (suitable for lists and sets)
* `launchFxSetUpdater` which is (currently) simply an alias on `launchFxCollectionUpdater`.
* `launchFxMapUpdater` which keep upt-to-date a map.

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

### JavaFx Integration (module 'miop-javafx')
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

### JavaFx Integration (module 'miop-javafx')
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
