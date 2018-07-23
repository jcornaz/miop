# miop
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Project status](https://img.shields.io/badge/status-incubating-orange.svg)](https://gist.githubusercontent.com/jcornaz/46736c3d1f21b4c929bd97549b7406b2/raw/ProjectStatusFlow)
[![JitPack](https://jitpack.io/v/jcornaz/miop.svg)](https://jitpack.io/#jcornaz/miop)
[![Build status](https://travis-ci.org/jcornaz/miop.svg?branch=master)](https://travis-ci.org/jcornaz/miop)
[![Code quality](https://codebeat.co/badges/99c78c20-42e7-425e-8a32-e2d56b0a0050)](https://codebeat.co/projects/github-com-jcornaz-miop-master)

Reactive operator collection for coroutines channels which are not yet included in [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines).

## Features
* multi-platform (available modules: `common`, `jvm` and `js`)

### Factory functions
* `emptyReceiveChannel(): ReceiveChannel<T>`
* `receiveChannelOf(vararg values: T): ReceiveChannel<T>`
* `Iterable<T>.openSubscription(): ReceiveChannel<T>`
* `Sequence<T>.openSubscription(): ReceiveChannel<T>`

### Merge operators
* `merge` and `mergeWith`
* `combineLatest` and `combineLatestWith`
* `switchMap`

### Subscribables
* `SubscribableValue` and `SubscribableVariable`
* `map`, `switchMap` and `combine` operators for `SubscribableValue`
* `StateStore` same concept as *store* in [redux](https://redux.js.org/)
* `map(transformState, transformAction)` operator for `StateStore`  

### JavaFx integration (module 'miop-javafx')
* `openValueSubscription(): ReceiveChannel<T>` extension function on `ObservableValue<T>` 
* `ObservableValue.asSubscribableValue()` adapter 
* `Property.asSubscribableVariable()` adapter
* `ReceiveChannel<T>.launchUpdater(Property<in T>)` and `ReceiveChannel<List<E>>.launchUpdater(ObservableList<E>)`

## Incoming features
* New operators
  * `buffer`, `conflate`
  * `debounce`
  * `mergeMap`
  * `chunked` and `windowed`
* Kotlin/Native support

## Add the library to your project
Artifacts are accessible for build tools with [Jitpack](https://jitpack.io/#jcornaz/miop).

Here is an example with gradle:
```groovy
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}

dependencies {

    // replace 'jvm' by 'common' or 'js' according to the module type
    compile 'com.github.jcornaz.miop:miop-jvm:0.0.3'
}
```
