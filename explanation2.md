# Query 2 Explanation

**The system receives continuous sensor events for all players and the ball, with the same structure as the one expressed in Query1. The goal is to monitor and analyze ball possession in real time.**

---

## The Problem

> Detect which player and which team currently possesses the ball.\
> Output two types of statistics:
>
> 1. **Per player possession stream** – containing total possession time and number of hits.
> 2. **Per team aggregate possession stream** – containing total time and percentage of possession over rolling windows (1 min, 5 min, 20 min, and whole game).

---

## Ball Possession Rules

- A **ball hit** is registered if:
  - The **ball** is within **1 meter** of any **player**, and
  - The \*\*acceleration of the ball \*\*exceeds **55 m/s²**&#x20;
- The ball stays in the player's possession until:
  - Another valid hit is detected,
  - The ball goes **off the field**, or
  - The game is **stopped**.

> The possession **continues** even if the ball temporarily leaves the player’s proximity.

---

## Modeling

We define the following key events:

```java
public class ShotEvent {
    private String sid;
    private long ts;
    private String playerId;
    private double x;
    private double y;
    private double z;
    private double v;  
    private double vx;
    private double vy;
    private double vz;
    private double a;
    private double ax;
    private double ay;
    private double az;
    private String team_id;
}

public class PlayerPosession {
    private long ts;
    private String playerId;
    private long duration;
    private int hits;
    private String teamId;

}
```

---

## EPL Implementation

### Step 1: Detect Ball Hits

```sql
create window NotHitLastBallEvent.win:length(1) as BallEvent;

insert into NotHitLastBallEvent
select * from BallEvent
where sid in ('4', '8', '10', '12');
```

> This creates a window holding the **latest ball sensor data**, once we detect the ball has been hit, we then remove the BallEvent from NotHitLastBallEvent

```sql
select
    b.sid, p.ts, p.player_id, p.team_id,
    b.x, b.y, b.z, b.v, b.vx, b.vy, b.vz,
    b.a, b.ax, b.ay, b.az
from Players p, NotHitLastBallEvent b
where
    distance_squared(p, b) <= 1000000 and
    b.a >= 55000000 and
    b.ts > p.ts;
```

> This detects a **hit** when the player is close to the ball **and** the ball acceleration is high.\
> When a hit is detected, a **ShotEvent** is emitted and the last ball event is deleted from the window.

---

### Step 2: Detect Ball Going Off Field

```sql
select * from NotHitLastBallEvent
where x < 0 or x > 33941 or y > 33965 or y < -33960;
```

> Emits a **ShotEvent** with `player_id = "off"` to represent the ball leaving the field and end the current possession. This way the posession will be ended if the ball goes off court.

---

## Player Possession Stream

```sql
select
    prev(1, ts) as prev_ts,
    ts,
    prev(1, playerId) as prev_playerId,
    prev(1, teamId) as prev_teamId
from ShotEvent.win:length(2);
```

> This maintains a sliding window of **2 recent ShotEvents**. We need to do this to allways have the start time of the shot and the end time, which will be the ts in which another player has made a Shoot.
>
> \
> On every new hit, we compute:
>
> - The **duration** = `ts - prev_ts`
> - **Hits** are incremented
> - We store/update possession data in a map for each player
>
> A new `PlayerPosession` event is created and sent to the runtime. This will be used to calculate the posession of each team.

---

## Team Possession Aggregation

> We calculate team posession summing all the players duration of each team and then also summing all the players duration, both using sliding time windows. We can change the duration of each window depending on what we find more interesting.

```sql
select SUM(duration) as total from PlayerPosession.win:time(1 min);

select SUM(duration) as total_time
from PlayerPosession.win:time(1 min) where teamId = 'teamA';

select SUM(duration) as total_time
from PlayerPosession.win:time(1 min) where teamId = 'teamB';
```

> Using this, we compute the **time\_percent** for each team:

```
time_percent = (teamA_time / total_time) * 100
```

> This is done for multiple time windows:

- **1 min**
- **5 min**
- **20 min**
- **Whole game**
-

---

