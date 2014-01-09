---
layout: docs
title: Convert geometries
prev_section: h2spatial/aggregates
next_section: h2spatial/operators
permalink: /docs/dev/h2spatial/geometry-conversion/
---

The following geometry conversion functions are available:

| Function | Description |
| - | - |
| [`ST_AsBinary`](../ST_AsBinary) | Geometry &rarr; binary value |
| [`ST_AsText`](../ST_AsText) | Alias for [`ST_AsWKT`](../ST_AsWKT) |
| [`ST_AsWKT`](../ST_AsWKT) | Geometry &rarr; Well Known Text |
| [`ST_GeomFromText`](../ST_GeomFromText) | Well Known Text &rarr; Geometry |
| [`ST_LineFromText`](../ST_LineFromText) | String &rarr; `LINESTRING` |
| [`ST_LineFromWKB`](../ST_LineFromWKB) | Well Known Text &rarr; `LINESTRING` |
| [`ST_MLineFromText`](../ST_MLineFromText) |  |
| [`ST_MPointFromText`](../ST_MPointFromText) |  |
| [`ST_MPolyFromText`](../ST_MPolyFromText) |  |
| [`ST_PointFromText`](../ST_PointFromText) |  |
| [`ST_PolyFromText`](../ST_PolyFromText) |  |
| [`ST_PolyFromWKB`](../ST_PolyFromWKB) |  |
