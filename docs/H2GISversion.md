# H2GISVersion

## Signature

```sql
STRING H2GISVersion();
```
## Description

Return the current version of H2GIS stored in the manifest, otherwise return `unknown`.

## Example

```sql
SELECT H2GISversion();
```
Answer: ``2.2.3-SNAPSHOT``


## See also

* [H2Version](https://www.h2database.com/html/functions.html?highlight=H2VERSION&search=h2version#h2version), [`JTSVersion`](../JTSVersion)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/system/H2GISversion.java" target="_blank">Source code</a>