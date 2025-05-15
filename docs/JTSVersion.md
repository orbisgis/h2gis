# JTSVersion

## Signature

```sql
STRING JTSVersion();
```
## Description

Return the current version of [JTS](https://github.com/locationtech/jts) stored in the manifest, otherwise return `unknown`.

## Example

```sql
SELECT JTSVersion();
```
Answer: `1.19.0`


## See also

* [H2Version](https://www.h2database.com/html/functions.html?highlight=H2VERSION&search=h2version#h2version), [`H2GISVersion`](../H2GISversion)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/system/JTSVersion.java" target="_blank">Source code</a>