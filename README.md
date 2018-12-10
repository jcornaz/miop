# miop
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Kotlin version](https://img.shields.io/badge/kotlin-1.3-blue.svg)
[![Project status](https://img.shields.io/badge/status-experimental-yellow.svg)](https://gist.githubusercontent.com/jcornaz/46736c3d1f21b4c929bd97549b7406b2/raw/ProjectStatusFlow)
[![JitPack](https://jitpack.io/v/jcornaz/miop.svg)](https://jitpack.io/#jcornaz/miop)
[![Build status](https://travis-ci.com/jcornaz/miop.svg?branch=master)](https://travis-ci.com/jcornaz/miop)
[![Quality gate](https://sonarcloud.io/api/project_badges/measure?project=jcornaz_miop&metric=alert_status)](https://sonarcloud.io/dashboard?id=jcornaz_miop)

Reactive operator collection for coroutines channels.

## Features
* multi-platform (available modules: `common`, `jvm` and `js`)

### Factory functions
* `emptyReceiveChannel(): ReceiveChannel<T>`
* `failedReceiveChannel(error: Throwable): ReceiveChannel<T>`
* `receiveChannelOf(vararg values: T): ReceiveChannel<T>`
* `CoroutineScope.produce(Iterable<T>): ReceiveChannel<T>`
* `CoroutineScope.produce(Sequence<T>): ReceiveChannel<T>`

### Operators
* `merge` and `mergeWith`
* `combineLatest` and `combineLatestWith`
* `switchMap`
* `buffer` and `conflate`
* `windowed` and `chunked`
* `debounce`
* `scan`

### Parallelization
* `parallel` operator allowing to parallelize any operator chain
* `parallelMap` alias for `parallel { map { /*  transform */ } }`
* `parallelFilter` alias for `parallel { filter { /*  predicate */ } }`

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
Ordered by implementation priority
* Parallelization
  * `parallelMapOrdered`
* New operators
  * `mergeMap`
* Improved collection events computation/handling
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

    // Replace 'jvm' by 'common' or 'js' according to the platform
    compile 'com.github.jcornaz.miop:miop-jvm:0.3.0'
    
    // JavaFx integration
    compile 'com.github.jcornaz.miop:miop-jvm:0.3.0'
   
    // Collekt integration. Replace 'jvm' by 'common' or 'js' according to the platform
    compile 'com.github.jcornaz.miop:miop-collekt-jvm:0.3.0'
}
```
