# ST_Graph

## Signatures

```sql
BOOLEAN ST_Graph(inputTable varchar);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double, orientBySlope boolean);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double, orientBySlope boolean, 
                 deleteTables boolean);                 
```

## Description

Produces two tables (nodes and edges) from the geometries contained in column
`columnName` of table `inputTable`. If no column is specified, then the first
Geometry column is used. Returns `true` if the operation is successful.

| Variable        | Default value                   |
|-----------------|---------------------------------|
| `columnName`    | The first geometry column found |
| `tolerance`     | `0.0`                           |
| `orientBySlope` | `false`                         |

If `deleteTables` is equal to `1`, existing tables (with the same prefix `inputTable`) are removed.

:::{warning}
**The column must only contain `LINESTRING`s**

Otherwise, the operation will fail and `ST_Graph` will return `false`
:::

:::{warning}
**The `inputTable`s must contain a Primary Key**

Otherwise, the operation will fail
:::

:::{note}
**If the input table is named `input`, then the output tables will be named `input_nodes` and `input_edges`**

The `input_nodes` table contains:

- an integer id `node_id`
- a `POINT` Geometry representing each node

The input_edges table is a copy of the input table with three extra integer id columns:

- `edge_id`
- `start_node`
- `end_node`

The last two columns correspond to the `node_ids` in the `input_nodes` table.
:::

:::{tip}
When the endpoints of certain `LINESTRING`s are very close together, we often wish to snap them together. The `tolerance` value allows us to do that. It specifies the side length of a square Envelope around each node used to snap together other nodes within the same Envelope. Note:

- Edge geometries are left untouched.
- Coordinates within a given tolerance of each other are not necessarily snapped together. Only the first and last coordinates of a Geometry are considered to be nodes, and only nodes within a given tolerance of each other are snapped together.
:::

:::{warning}
**The tolerance works only in metric units**
:::

:::{note}
**Hydrologists, watch out!**

By setting `orientBySlope` to `true`, you can specify that edges should be oriented from the endpoint with greatest *z*-value to the endpoint with least *z*-value.
:::


## Examples

### First Geometry column detection

```sql
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR, way LINESTRING);
INSERT INTO test VALUES
('1','LINESTRING(0 0, 1 2)', 'road1', 'LINESTRING(1 1, 2 2, 3 1)'),
('2','LINESTRING(1 2, 2 3, 4 3)', 'road2', 'LINESTRING(3 1, 2 0, 1 1)'),
('3','LINESTRING(4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING(1 1, 2 1)'),
('4','LINESTRING(4 3, 5 2)', 'road4', 'LINESTRING(2 1, 3 1)');

-- We first demonstrate automatic Geometry column detection.
-- ST_Graph finds and uses the 'road' column.
SELECT ST_Graph('test');
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM  |
--     |---------|-------------|
--     |    1    | POINT(0 0)  |
--     |    2    | POINT(1 2)  |
--     |    3    | POINT(4 3)  |
--     |    4    | POINT(5 2)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    3     |
-- |    3    |     3      |    2     |
-- |    4    |     3      |    4     |

-- We may also choose which Geometry column we want to use.
-- Here we specify the 'way' column.
DROP TABLE test_nodes;
DROP TABLE test_edges;
SELECT ST_Graph('test', 'way');
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM  |
--     |---------|-------------|
--     |    1    | POINT(1 1)  |
--     |    2    | POINT(3 1)  |
--     |    3    | POINT(2 1)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    1     |
-- |    3    |     1      |    3     |
-- |    4    |     3      |    2     |
```

### Using a tolerance

```sql
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                                 description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0, 1 0)', 'road1'),
                        ('2', 'LINESTRING(1.05 0, 2 0)', 'road2'),
                        ('3', 'LINESTRING(2.05 0, 3 0)', 'road3'),
                        ('4', 'LINESTRING(1 0.1, 1 1)', 'road4'),
                        ('5', 'LINESTRING(2 0.05, 2 1)', 'road5');

-- This example shows that coordinates within a tolerance of 0.05 of
-- each other are considered to be a single node. Note, however, that
-- edge geometries are left untouched.
SELECT ST_Graph('test', 'road', 0.05);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM    |
--     |---------|---------------|
--     |    1    | POINT(0 0)    |
--     |    2    | POINT(1.05 0) |
--     |    3    | POINT(2.05 0) |
--     |    4    | POINT(3 0)    |
--     |    5    | POINT(1 1)    |
--     |    6    | POINT(2 1)    |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    3     |
-- |    3    |     3      |    4     |
-- |    4    |     2      |    5     |
-- |    5    |     3      |    6     |
```

### Orienting by z-values

```sql
-- This test proves that orientation by slope works. Three cases:
--     1. first.z == last.z -- Orient first --> last
--     2. first.z > last.z -- Orient first --> last
--     3. first.z < last.z -- Orient last --> first

--------------------------------------
-- CASE 1: 0 == 0.
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 0, 1 0 0)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM    |
--     |---------|---------------|
--     |    1    | POINT(0 0 0)  |
--     |    2    | POINT(1 0 0)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |

--------------------------------------
-- CASE 2: 1 > 0.
DROP TABLE test;
DROP TABLE test_nodes;
DROP TABLE test_edges;
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 1, 1 0 0)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM    |
--     |---------|---------------|
--     |    1    | POINT(0 0 1)  |
--     |    2    | POINT(1 0 0)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |

--------------------------------------
-- CASE 3: 0 < 1.
DROP TABLE test;
DROP TABLE test_nodes;
DROP TABLE test_edges;
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 0, 1 0 1)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |   THE_GEOM    |
--     |---------|---------------|
--     |    1    | POINT(0 0 0)  |
--     |    2    | POINT(1 0 1)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     2      |    1     |
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topology/ST_Graph.java" target="_blank">Source code</a>
