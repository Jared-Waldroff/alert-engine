# Architecture — Pason Threshold Alert Engine

## Overview

The Alert Engine is a Spring Boot application that processes real-time sensor data and triggers configurable alerts. It uses two core design patterns:

- **Strategy Pattern** — `AlertCondition` implementations provide interchangeable evaluation logic
- **Observer Pattern** — `AlertDispatcher` implementations provide pluggable alert delivery channels

## Layer Architecture

```
┌─────────────────────────────────────────────┐
│  API Layer (controllers, DTOs, validation)   │
├─────────────────────────────────────────────┤
│  Service Layer (AlertRuleService)            │
├─────────────────────────────────────────────┤
│  Engine Layer (AlertEngine, ReadingWindow)   │
├─────────────────────────────────────────────┤
│  Domain Layer (models, conditions, dispatch) │
├─────────────────────────────────────────────┤
│  Persistence (JPA entities, repositories)    │
├─────────────────────────────────────────────┤
│  Infrastructure (MySQL, Flyway, Docker)      │
└─────────────────────────────────────────────┘
```

**Dependency direction:** Each layer only depends on layers below it. The domain layer has zero framework dependencies.

## Processing Pipeline

1. Sensor reading arrives via `POST /api/readings` (or from the simulator)
2. Reading is persisted for dashboard display
3. `AlertEngine.processReading()` is called
4. Engine retrieves recent history from `ReadingWindow`
5. Engine evaluates all enabled rules whose sensor type matches
6. For each triggered condition, an `Alert` is created
7. All registered `AlertDispatcher` implementations are notified
8. Dispatchers handle delivery independently (console, database, log file)

## Thread Safety

- `SensorReading` and `Alert` are immutable (final fields, no setters)
- `ReadingWindow` uses `ConcurrentHashMap` + `ConcurrentLinkedDeque`
- `AlertEngine` uses `CopyOnWriteArrayList` for rules
- Processing metrics use `AtomicLong`

## Adding New Features

### New Condition Type
Implement `AlertCondition`, add serialization in `ConditionConfigMapper`, write tests.

### New Dispatcher
Implement `AlertDispatcher`, annotate with `@Component`. Spring auto-discovers it.

### New Sensor Type
Add to `SensorType` enum, add a profile in `DrillingSensorSimulator`, create rules.
