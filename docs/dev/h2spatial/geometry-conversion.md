---
layout: docs
title: Convert geometries
prev_section: embedded-spatial-db
next_section: h2spatial/operators
permalink: /docs/dev/h2spatial/geometry-conversion/
---

The following geometry conversion functions are available:

| Function | Description |
| - | - |
| [`ST_AsBinary`](../../ST_AsBinary) | Geometry &rarr; Well Known Binary |
| [`ST_AsText`](../../ST_AsText) | Alias for [`ST_AsWKT`](../../ST_AsWKT) |
| [`ST_AsWKT`](../../ST_AsWKT) | Geometry &rarr; Well Known Text |
| [`ST_GeomFromText`](../../ST_GeomFromText) | Well Known Text &rarr; Geometry |
| [`ST_LineFromText`](../../ST_LineFromText) | Well Known Text &rarr; `LINESTRING` |
| [`ST_LineFromWKB`](../../ST_LineFromWKB) | Well Known Binary &rarr; `LINESTRING` |
| [`ST_MLineFromText`](../../ST_MLineFromText) | Well Known Text &rarr; `MULTILINESTRING` |
| [`ST_MPointFromText`](../../ST_MPointFromText) | Well Known Text &rarr; `MULTIPOINT` |
| [`ST_MPolyFromText`](../../ST_MPolyFromText) | Well Known Text &rarr; `MULTIPOLYGON` |
| [`ST_PointFromText`](../../ST_PointFromText) | Well Known Text &rarr; `POINT` |
| [`ST_PolyFromText`](../../ST_PolyFromText) | Well Known Text &rarr; `POLYGON` |
| [`ST_PolyFromWKB`](../../ST_PolyFromWKB) | Well Known Binary &rarr; `POLYGON` |
