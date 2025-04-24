
# Query 3 Explanation

**The system receives continuous sensor events from players and tracks their positions on the field. The goal is to generate real-time heatmaps indicating how long each player spends in specific field regions.**

---

##  The Problem

> For each player, calculate the percentage of time they spend in each cell of the field across multiple grid resolutions and time windows.  
> The system returns 16 result streams:

- 4 grid resolutions: (8x13), (16x25), (32x50), (64x100)
- 4 time windows: 1 min, 5 min, 10 min, whole game

Each result stream is updated every second and contains:

```
ts, player_id, cell_x1, cell_y1, cell_x2, cell_y2, percent_time_in_time_cell
```

---

##  Grid Configuration

| Grid Size | Rows | Columns | Total Cells |
| --------- | ---- | ------- | ----------- |
| 8x13      | 8    | 13      | 104         |
| 16x25     | 16   | 25      | 400         |
| 32x50     | 32   | 50      | 1,600       |
| 64x100    | 64   | 100     | 6,400       |

- Field dimensions:
  - X: 52.47 meters (33941 units)
  - Y: 67.92 meters (from -33960 to 33965)

---

##  Modeling

```java
public class HeatMapEvent {
    long ts;
    String playerId;
    double cellX;
    double cellY;
    long duration;
}
```

This event is emitted each time a player changes position. It logs how long the player stayed in a specific cell.

---

##  EPL Implementation

### Step 1: Track Player Movement

```sql
select prev(1, ts) as prev_ts, ts, prev(1, player_id) as player_id,
       prev(1, x) as prev_x, prev(1, y) as prev_y
from SensorEvent.win:length(2)
where sid NOT IN ('4', '8', '10', '12', '105', '106')
group by player_id;
```

> This query captures movement updates per player.  
> If the previous event is valid and inside the field, a `HeatMapEvent` is emitted.

---

### Step 2: Generate Heat Map Statistics

```sql
select sum(duration) as total_time, ts, playerId
from HeatMapEvent.win:time(5 min)
group by playerId;

select sum(duration) as total_time_cell, playerId, cellX, cellY, ts
from HeatMapEvent.win:time(5 min)
group by playerId, cellX, cellY;
```

> First query: total time spent on the field.  
> Second query: time spent in each cell.  
> Percent time is calculated as: `(total_time_cell / total_time) * 100`

---

##  Output Formatting

For each `(playerId, cellX, cellY)` at timestamp `ts`:

```java
percent_time_in_cell = (total_time_cell / total_time) * 100;
```

Cell boundaries are computed as:

```java
cell_x1 = (cellX - 1) * (field_width / gridCols);
cell_x2 = cellX * (field_width / gridCols);
cell_y1 = cellY * (field_height / gridRows);
cell_y2 = (cellY - 1) * (field_height / gridRows);
```

All outputs are logged with `AsyncLogger`, rounded to two decimal places.

---

##  Multi-Resolution Support

The logic above is repeated for all combinations of grid sizes and time windows. Each pair produces a unique result stream, offering fine-grained spatial and temporal heatmap statistics.

---
