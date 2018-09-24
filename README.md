# miop
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Project status](https://img.shields.io/badge/status-experimental-yellow.svg)](https://gist.githubusercontent.com/jcornaz/46736c3d1f21b4c929bd97549b7406b2/raw/ProjectStatusFlow)
[![JitPack](https://jitpack.io/v/jcornaz/miop.svg)](https://jitpack.io/#jcornaz/miop)
[![Build status](https://travis-ci.org/jcornaz/miop.svg?branch=master)](https://travis-ci.org/jcornaz/miop)
[![Code quality](https://codebeat.co/badges/99c78c20-42e7-425e-8a32-e2d56b0a0050)](https://codebeat.co/projects/github-com-jcornaz-miop-master)

Reactive operator collection for coroutines channels which are not yet included in [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines).

**ATTENTION:** Version `0.2.0` shall be used with Kotlin `1.3-rc-57` only. For Kotlin 1.2.+, use the version `0.0.4`.

## Features
* multi-platform (available modules: `common`, `jvm` and `js`)

### Factory functions
* `emptyReceiveChannel(): ReceiveChannel<T>`
* `failedReceiveChannel(error: Throwable): ReceiveChannel<T>`
* `receiveChannelOf(vararg values: T): ReceiveChannel<T>`
* `Iterable<T>.openSubscription(): ReceiveChannel<T>`
* `Sequence<T>.openSubscription(): ReceiveChannel<T>`

### Operators
* `merge` and `mergeWith`
* `combineLatest` and `combineLatestWith`
* `switchMap`
* `buffer` and `conflate`
* `windowed` and `chunked`
* `debounce`

### Subscribables (Experimental)
* `SubscribableValue` and `SubscribableVariable`
* `map`, `switchMap` and `combine` operators for `SubscribableValue`
* `StateStore` (same concept as *store* in [redux](https://redux.js.org/))
* `map(transformState, transformEvent)` operator for `StateStore`  

### Event computation for collection (Experimental)
* `ReceiveChannel<Set<E>>.toSetEvents(): ReceiveChannel<SetEvent<E>>`
* `ReceiveChannel<Map<K, V>>.toSetEvents(): ReceiveChannel<MapEvent<K, V>>`

### [Collekt](https://github.com/jcornaz/collekt) Integration
**modules 'miop-collekt-common', 'miop-collekt-jvm' and 'miop-collekt-js'**
* `PersistentMap.plus(MapEvent)` operator
* `PersistentSet.plus(SetEvent)` operator

### JavaFx integration
**module 'miop-javafx'**
* `openValueSubscription(): ReceiveChannel<T>` extension function on `ObservableValue<T>` 
* `ObservableValue.asSubscribableValue()` adapter 
* `Property.asSubscribableVariable()` adapter
* Launch updaters for JavaFx property and observable value according to a `ReceiveChannel` source 

## Incoming features
* New operators
  * `mergeMap`
  * `scan`
* Improved collection events computation/handling
* Kotlin/Native support

## Add the library to your project
Artifacts are accessible for build tools with [Jitpack](https://jitpack.io/#jcornaz/miop).

Here is an example with gradle:
```groovy
repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/kotlin/kotlin-eap' }
    maven { url "https://jitpack.io" }
}

dependencies {

    // Replace 'jvm' by 'common' or 'js' according to the platform
    compile 'com.github.jcornaz.miop:miop-jvm:0.2.0' // Needs Kotlin 1.3-rc-57
    compile 'com.github.jcornaz.miop:miop-jvm:0.0.4' // Needs Kotlin 1.2.+ a
    
    // JavaFx integration
    compile 'com.github.jcornaz.miop:miop-jvm:0.2.0' // Needs Kotlin 1.3-rc-57
    compile 'com.github.jcornaz.miop:miop-jvm:0.0.4' // Needs Kotlin 1.2.+ a
   
    // Collekt integration. Replace 'jvm' by 'common' or 'js' according to the platform
    compile 'com.github.jcornaz.miop:miop-collekt-jvm:0.2.0' // Needs Kotlin 1.3-rc-57
}
```
