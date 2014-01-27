# H2GIS's website

H2GIS's website is built using [Jekyll](http://jekyllrb.com) and [Github
pages](http://pages.github.com/). Consult Jekyll's
[documentation](http://jekyllrb.com/docs/home/) first, especially the section
on [YAML front matter](http://jekyllrb.com/docs/frontmatter/). Or if you're
really impatient, just take a look at the
[source](https://raw2.github.com/irstv/H2GIS/gh-pages/docs/dev/h2spatial-ext/ST_Rotate.md)
of one of the [existing
pages](http://www.h2gis.org/docs/dev/h2spatial-ext/ST_Rotate/) on the website.

To install Jekyll and serve the website locally, consult GitHub's help page
[Using Jekyll with
Pages](https://help.github.com/articles/using-jekyll-with-pages).

### Contributing

To contribute, fork H2GIS and clone it locally. We will assume your username is
`user`.

```bash
~ $ git clone https://github.com/user/H2GIS.git
```

The website is located on the branch `gh-pages`. So checkout a new branch
`work` which tracks your fork's `gh-pages` branch.

```bash
~ $ cd H2GIS
~/H2GIS $ git checkout -b work origin/gh-pages
```

Make changes, commit and push to your fork as usual.

```bash
~/H2GIS $ git commit -m "Made some changes"
~/H2GIS $ git push origin work
```

When you are ready, submit a pull request from `user/work` to `irstv/gh-pages`.
Your work will be peer-reviewed and accepted when approved.

### Style guide

Each function's documentation contains three sections: Signature(s),
Description and Example(s).

1. Global comments:
    1. When refering to geometries in a global sense (and not as WKT), write
       Geometry or Geometries (and not `GEOMETRY`). But we still write
       `GEOMETRY` in case 1.ii.
    1. WKT geometries should be in UPPERCASE (`POLYGON` and not `Polygon` or
       `polygon`).
    1. Function names should be written as `ST_CompactnessRatio` and not
       `st_compactnessratio` or `ST_COMPACTNESSRATIO`.
    1. Put spaces after commas.
1. Signature(s) - Give all possible function signatures.
    1. Write Signature if there is only one.
    1. Include a semicolon at the end of the signature.
    1. Primitives should be in lowercase (`double` and not `Double` or
       `DOUBLE`).
    1. Do not skip lines between signatures.
1. Description and `description: `
    1. Description - Explain what the function does.
        1. Reference the various `variables` included in the signature.
        1. Conjugate the verb in third-person singular tense (*Rotates* and not *Rotate*).
    1. `description: ` field - Include a short summarizing description in the
       front-matter `description: ` field.
        1. Do not reference variables.
        1. Do not conjugate the verb (*Rotate* and not *Rotates*).
1. Example(s) - Insert examples in SQL.
    1. Write Example if there is only one.
    1. All SQL keywords should be in UPPERCASE.
    1. SQL variables should be in lowercase.
    1. Insert illustrations as necessary.
    1. Tables may be written in Markdown or SQL comments. If you choose SQL
       comments, use proper alignment.
    1. Align long geometries vertically by inserting linebreaks as necessary.

Good:
```mysql
-- Answer: LINESTRING(0.3839745962155607 2.0669872981077813,
--                    2.1160254037844384 1.0669872981077806,
--                    2.6160254037844384 1.933012701892219)
```

Bad:
```mysql
-- Answer: LINESTRING(0.3839745962155607 2.0669872981077813, 2.1160254037844384 1.0669872981077806, 2.6160254037844384 1.933012701892219)
```
