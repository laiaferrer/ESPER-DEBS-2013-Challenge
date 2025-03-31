# Query 4 Explanation

**The system continuously analyzes player and ball sensor data to detect shots on goal. A shot on goal is defined as a hit on the ball by a player that causes the ball to be projected toward the opponent’s goal area within 1.5 seconds of the hit.**

---

## The Problem

> Detect a shot when:
>
> - A player hits the ball (acceleration ≥ 55 m/s²)
> - The projected ball movement intersects the opponent's goal area within 1.5 seconds
>
> Once detected, continuously emit the ball's motion and the shooting player's ID **until**:
>
> - The ball leaves the field, or
> - The trajectory changes such that the goal would no longer be hit

Output stream:

`ts, player_id, x, y, z, |v|, vx, vy, vz, |a|, ax, ay, az`

---

## Goal Area Definitions

- **Team A's goal** (attacked by Team B):
  - `x` in [22560, 29880], `y` in [-31468, -33968], `z` < 2440.0
- **Team B's goal** (attacked by Team A):
  - `x` in [22578.5, 29898.5], `y` in [31441, 33941], `z` < 2440.0

---

## Modeling

```java
public class GoalShotEvent {
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
```

This event captures when a player shoots the ball toward the opponent’s goal.

---

## EPL Implementation

### Step 1: Detect Ball Hits and Shot Prediction

```sql
create window LastBallEvent.win:length(1) as BallEvent;

insert into LastBallEvent
select * from BallEvent
where sid in ('4', '8', '10', '12');

create window Players.std:unique(sid) as SensorEvent;

insert into Players
select * from SensorEvent
where sid NOT IN ('4', '8', '10', '12');
```

> Maintain the latest ball event and player sensor events separately. Keep in mind that even though the window is named Players, what is actually unique is the sensor id, and there are different sensors for each player.

```sql
select b.sid, b.ts, p.player_id, b.x, b.y, b.z, b.v, b.vx, b.vy, b.vz,
       b.a, b.ax, b.ay, b.az, p.team_id
from Players p, LastBallEvent b
where distance_squared(p, b) <= 1m²
  and b.a >= 55 m/s²
  and p.ts < b.ts
  and (
    (p.team_id = 'teamA' and
     (b.x + b.vx * 1.5 / 1000) between 22560.0 and 29880.0 and
     (b.y + b.vy * 1.5 / 1000) between -31468.0 and -33968.0)
    or
    (p.team_id = 'teamB' and
     (b.x + b.vx * 1.5 / 1000) between 22578.5 and 29898.5 and
     (b.y + b.vy * 1.5 / 1000) between 31441.0 and 33941.0)
  );
```

> This query first checks if the event is a shot by evaluating proximity and acceleration, then projects the ball's motion to determine if it will reach the opponent's goal. Emits a `GoalShotEvent` if conditions are met.

---

### Step 2: Define Context for Shot Duration

```sql
create context GoalShotContext
initiated by GoalShotEvent as a
terminated by BallEvent(
  x < 0 or x > 52477 or y < -33960 or y > 33965 or
  (vy < 0 and x > 22578.5 and x < 29898.5 and y <= 33941.0 and y > 31441) or
  (vy > 0 and x > 22560.0 and x < 29880.0 and y > -33968.0 and y < -31468)
);
```

> Context is created per shot. It ends when the ball either:
>
> - Leaves the field
> - Changes trajectory away from the goal area

### Step 3: Stream Ball Data During Shot

```sql
context GoalShotContext
select context.a.playerId as player_id, ts, x, y, z, v, vx, vy, vz, a, ax, ay, az
from BallEvent;
```

> Emits ball data while a shot is considered active. Logged to file `query4.txt`.

---

## Notes on Implementation

- Ball movement is extrapolated simply using the velocity vector
- No advanced physics (e.g., gravity or air resistance) is modeled
- Output stream frequency matches incoming sensor data

---

##
