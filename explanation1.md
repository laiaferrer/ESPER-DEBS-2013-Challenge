
# Query 1 Explanation

**The system receives continuous sensor events with the following structure:**

> **sid, ts, x, y, z, |v|, |a|, vx, vy, vz, ax, ay, az**

---

##  The Problem

> Monitor and analyze the running intensity of each player in real-time, producing:
>
> 1. **Current running statistics**: Emitted every time a player changes running intensity (after at least 1 second in the previous intensity).
> 2. **Aggregate running statistics**: Continuously updated for each player and intensity level (time spent and distance covered).

---

###  Running Intensity Categories

- **Standing:** 0–1 km/h  
- **Trot:** 1–11 km/h  
- **Low Speed Run**: 11–14 km/h  
- **Medium Speed Run**: 14–17 km/h  
- **High Speed Run**: 17–24 km/h  
- **Sprint**: >24 km/h

> **Note:** Intensities that last less than 1 second are not considered valid segments and are merged with the next valid one.

---

##  Modeling

Input event schema:

```java
public class SensorEvent {
    String sid;
    long ts;
    double x, y, z;
    double v, a;
    double vx, vy, vz;
    double ax, ay, az;
}
```

Output schema:

```java
public class RunningStatisticsEvent {
    long ts_start;
    long ts_stop;
    String player_id;
    String intensity;
    double speed;
}
```

---

##  EPL Implementation

### Context: IntensityContext

```sql
create context IntensityContext
partition by player_id from SensorEvent
initiated by SensorEvent(sid NOT IN ('4', '8', '10', '12', '105', '106')) as a
terminated by SensorEvent(
    a.player_id = player_id AND 
    intensity != a.intensity AND 
    (ts - a.ts) >= 1000000000000
)
```

- Context is partitioned per `player_id`
- Only begins if the sensor ID is not in the excluded set.
- Ends only if:
  - Intensity changes, **and**
  - The player stayed in the previous intensity for **≥ 1 second** (1 trillion picoseconds)

---

### Query: Emit RunningStatisticsEvent

```sql
context IntensityContext
select 
    min(ts) as ts_start,
    max(ts) as ts_stop,
    player_id,
    intensity,
    ((max(ts) - min(ts)) * avg(v)) * 1e-15 as distance,
    avg(v) as speed
from SensorEvent
output last when terminated
```

- Emits one event per valid intensity segment.
- Output is used to feed aggregate tracking in a downstream query.

---

##  Aggregate Statistics

This second query receives the `RunningStatisticsEvent` and tracks intensity transitions:

```sql
context PerPlayerContext
select 
    prev(1, ts_start) as prev_ts_start,
    ts_start,
    player_id,
    prev(1, intensity) as prev_intensity,
    intensity,
    prev(1, speed) as prev_speed
from RunningStatisticsEvent.win:length(2)
```

- Uses a **length window of 2** to track current and previous segments.
- Distance = `prev_speed × (ts_start - prev_ts_start)`
- This is added to the player’s corresponding intensity counters.

---

###  Additional Notes

- This version fixes precision, applies filtering on irrelevant sensors, and logs rich contextual output per intensity transition.
- Data is stored in a central `RunningStatistics` object per player, allowing easy access to cumulative stats.
