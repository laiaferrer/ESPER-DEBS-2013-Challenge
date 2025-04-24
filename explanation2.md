
# Query 2 Explanation

**The system receives continuous sensor events for all players and the ball, with the same structure as used in Query 1. The goal is to monitor and analyze ball possession in real time.**

---

##  The Problem

> Detect which player and which team currently possesses the ball.  
> Output two types of statistics:

1. **Per-player possession stream** – includes total possession time and number of hits.
2. **Per-team aggregate possession stream** – includes total time and percentage of possession over sliding time windows (1 min, 5 min, 20 min, and whole game).

---

##  Ball Possession Rules

- A **ball hit** is detected if:
  - The **ball** is within **1 meter** of any **player**, and
  - The **acceleration** of the ball is at least **55 m/s²** (in μm/s²: 55,000,000).
- Possession ends if:
  - Another valid hit is detected,
  - The ball goes **off the field**, or
  - The game is **stopped**.

> Possession **persists** even if the ball leaves the player's proximity briefly.

---

##  Modeling

### Key Events

```java
public class ShotEvent {
    String sid;
    long ts;
    String playerId;
    double x, y, z;
    double v, vx, vy, vz;
    double a, ax, ay, az;
    String team_id;
}

public class PlayerPosession {
    long ts;
    String playerId;
    long duration;
    int hits;
    String teamId;
}
```

---

##  EPL Implementation

### Step 1: Track Ball Hits

```sql
create window NotHitLastBallEvent.win:length(1) as BallEvent;

insert into NotHitLastBallEvent
select * from BallEvent where sid in ('4', '8', '10', '12');
```
>This creates a window holding the `latest ball sensor data`, once we detect the ball has been hit, we then remove the BallEvent from NotHitLastBallEvent
```sql
select
    b.sid, b.ts, p.player_id, p.team_id,
    b.x, b.y, b.z, b.v, b.vx, b.vy, b.vz,
    b.a, b.ax, b.ay, b.az
from Players p, NotHitLastBallEvent b
where
    ((p.x - b.x)^2 + (p.y - b.y)^2 + (p.z - b.z)^2) <= 1000000 and
    b.a >= 55000000 and
    b.ts > p.ts;
```

> This detects a `hit` when the player is close to the ball `and` the ball acceleration is high.
> When a hit is detected, a ShotEvent is emitted and the last ball event is deleted from the window.

---

### Step 2: Detect Off-field Ball Events

```sql
select * from NotHitLastBallEvent
where x < 0 or x > 33941 or y > 33965 or y < -33960;
```

> This emits a `ShotEvent` with `player_id = "off"` to end possession.

---

##  Player Possession Stream

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

##  Team Possession Aggregation

> We calculate team posession summing all the players duration of each team and then also summing all the players duration, both using sliding time windows. We can change the duration of each window depending on what we find more interesting.

```sql
select SUM(duration) as total from PlayerPosession.win:time(1 min);

select SUM(duration) as total_time from PlayerPosession.win:time(1 min) where teamId = 'teamA';
select SUM(duration) as total_time from PlayerPosession.win:time(1 min) where teamId = 'teamB';
```

### Compute percentage:

```java
time_percent = (team_time / total_time) * 100;
```

> This is repeated for the following time windows:
- 1 min
- 5 min
- 20 min
- Whole game

---

##  Additional Notes

- Query 2 is split into multiple classes:
  - `Query2` detects hits and off-field events.
  - `Query2_2` tracks player-level possession segments and writes logs.
  - `Query2_3` computes and logs team-level aggregates.
