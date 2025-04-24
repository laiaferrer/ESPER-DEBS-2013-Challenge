# Query 1 Explanation

**The system receives continuous sensor events with the following structure:**

                                   **sid, ts, x, y, z, |v|, |a|, vx, vy, vz, ax, ay, az**

---

## The Problem

> Monitor and analyze the running intensity of each player on the field in real-time. The goal is to output two classes of statistics:
>
> 1. Current running statistics: Emitted every 20ms, detailing the player's activ**ity in the current intensi**ty interval.
> 2. Aggregate running statistics: Emitted every 20ms for four time windows (1 mi\*\*n, 5 min, 20 min, and whole \*\*game), summarizing time and distance per intensity.

### Running Intensity Categories:

- **Standing:** 0-1 km/h
- **Trot:** 1-11 km/h
- **Low Speed Run**: 11-14 km/h
- **Medium Speed Run**: 14-17 km/h
- **High Speed Run**: 17-24 km/h
- **Sprint**: >24 km/h

### Things to keep in mind:

> - Any intensity active for less than 1 second should be merged with the next intensity that lasts at least 1 second.

---

## Modeling

We define the following schema for the input stream:

```java
public SensorEvent(
    String sid,
    long ts,
    double x, 
    double y, 
    double z,
    double v, 
    double a,
    double vx, 
    double vy, 
    double vz,
    double ax, 
    double ay, 
    double az
);

create schema RunningStatisticsEvent(
    long ts_start;
    String player_id;
    String intensity;
    double speed;
);
```

---

## EPL Implementation

### Context: Intensity Partitioning

```sql
create context IntensityContext
partition by player_id from SensorEvent
initiated by SensorEvent(sid NOT IN ('4', '8', '10', '12')) as a
terminated by SensorEvent(
  intensity != a.intensity AND (ts - a.ts) >= 1000000000000
);
```

This updated context now ensures that an intensity segment only ends if the **new intensity is different** *and* the current segment has lasted **at least 1 second** (1000000000000 picoseconds).

If a player switches intensity too quickly, then that time is merged into the previous intensity.

```sql
context IntensityContext
select
  min(ts) as ts_start,
  max(ts) as ts_stop,
  player_id,
  intensity,
  ((max(ts) - min(ts)) * avg(v)) * 0.000000000000001 as distance,
  avg(v) as speed
from SensorEvent
group by player_id, intensity
output last every 20 milliseconds;
```

The events from this query will create other RunningStatisticsEvent that will be sended to another Query to properly give the current running statistics output.

This query would be repeated with different time windows (1min, 5min, 20 min and the hole game).

---

## Aggregate Statistics&#x20;

Another EPL Query recieves the RunningStatisticsEvent and will be the one to create the current running statistics output.

```sql
select 
  prev(1, ts_start) as prev_ts_start, 
  ts_start, 
  player_id, 
  prev(1, intensity) as prev_intensity, 
  prev(1, speed) as prev_speed 
from RunningStatisticsEvent.win:length(2)
group by player_id;
```

- This query uses a **length window of 2** to always keep the **current and previous** events for each player to be able to calculate the ts\_*start and ts\_*stop. This also enables the computation of the **distance** and the **duration**.
- The **distance** is calculated multiplaying the **prev\_speed**, which is the average speed of the context, with the substraction of ts\_start - prev\_ts\_start.
- For the set of aggregate running statistics we have to check the intensity and add the distance and the duration to the corresponding variable.

---

###
