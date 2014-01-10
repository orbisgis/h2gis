# H2GIS's website

H2GIS's website is built using [Jekyll](http://jekyllrb.com/docs/home/) and
[Github pages](http://pages.github.com/). Consult Jekyll's documentation first,
especially the section on [YAML front
matter](http://jekyllrb.com/docs/frontmatter/). Or if you're really impatient,
just take a look at the
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
