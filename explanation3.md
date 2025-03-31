# Query 3 Explanation

**The system receives continuous sensor events from players and tracks their positions on the field. The goal is to generate real-time heatmaps indicating how long each player spends in specific field regions.**

---

## The Problem

> For each player, calculate the percentage of time they spend in each cell of the field across multiple grid resolutions and time windows.
>
> The system returns 16 result streams:
>
> - 4 grid resolutions: (8x13), (16x25), (32x50), (64x100)
> - 4 time windows: 1 min, 5 min, 10 min, whole game
>
> Each result stream is updated every second and contains:
>
> `ts, player_id, cell_x1, cell_y1, cell_x2, cell_y2, percent_time_in_time_cell`

---

## Grid Configuration

Each field grid resolution divides the field into rectangular cells:

| Grid Size | Rows | Columns | Total Cells |
| --------- | ---- | ------- | ----------- |
| 8x13      | 8    | 13      | 104         |
| 16x25     | 16   | 25      | 400         |
| 32x50     | 32   | 50      | 1,600       |
| 64x100    | 64   | 100     | 6,400       |

- Field dimensions:
  - X: 52.47 meters (33941 units)
  - Y: 67.92 meters (67925 units total; from -33960 to 33965)

---

## Modeling

```java
public class HeatMapEvent {
    private long ts;
    private String playerId;
    private double cellX;
    private double cellY;
    private long duration;
}
```

This event is generated every time a player moves between positions. It calculates how much time the player spent in a specific cell.

---

## EPL Implementation

### Step 1: Track Player Movement

```sql
create context PlayerContext partition by player_id from SensorEvent;

context PlayerContext
select prev(1, ts) as prev_ts, ts, player_id,
       prev(1, x) as prev_x, prev(1, y) as prev_y
from SensorEvent.win:length(2)
where sid NOT IN ('4', '8', '10', '12')
group by player_id;
```

> This query tracks the movement of each player (excluding the ball sensors). For each movement update, we compute the cell they were in and how long they stayed there.

If the previous event is valid and the location is inside the field, we emit a `HeatMapEvent`.

---

### Step 2: Generate Heat Map Statistics

```sql
select sum(duration) as total_time, ts, playerId
from HeatMapEvent.win:time(5 min)
group by playerId
output every 1 second;

select sum(duration) as total_time_cell, playerId, cellX, cellY, ts
from HeatMapEvent.win:time(5 min)
group by playerId, cellX, cellY
output every 1 second;
```

> The first query gives the total time a player spent on the field. The second query gives the total time a player spent in a specific cell. This way we can compute the % a player has spend in each cell.

---

## Output Formatting

For each `(playerId, cellX, cellY)` at time `ts`:

```java
percent_time_in_cell = (total_time_cell / total_time) * 100;
```

We also compute the actual coordinates of the cell:

```java
cell_x1 = (cellX - 1) * (field_width / gridCols);
cell_x2 = cellX * (field_width / gridCols);
cell_y1 = cellY * (field_height / gridRows);
cell_y2 = (cellY - 1) * (field_height / gridRows);
```

All of this is logged using a queue-based `AsyncLogger`, and values are formatted to two decimal places.

---

## Multi-Resolution Support

This entire process is repeated for all 4 grid configurations and 4 time windows. Each configuration outputs a separate stream, allowing full heatmap granularity across time and space.

---
