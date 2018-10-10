---
layout: docs
title: H2Network
category: applications/h2network
prev_section: system
permalink: /docs/dev/h2network/
---



`H2Network` extends the `H2` / `H2GIS` geospatial database to provide graph routing functionalities. 

`H2Network` is based on the [Java Network Analyzer](https://github.com/orbisgis/java-network-analyzer) (`JNA`) library. 

JNA provides a collection of graph theory and social network analysis algorithms. These algorithms are implemented on mathematical graphs using the [JGraphT](https://jgrapht.org/) library.

### Install H2Network

To install `H2Network` execute the two following instructions.

{% highlight mysql %}
CREATE ALIAS IF NOT EXISTS H2GIS_NETWORK FOR "org.h2gis.network.functions.NetworkFunctions.load";
CALL H2GIS_NETWORK();
{% endhighlight %}


### H2Network functions

The following H2Network functions are available:

{% include table_of_functions.html %}

### Bibliography

* *Erwan Bocher, Gwendall Petit, Mireille Lecoeuvre. **H2Network : un outil pour la modélisation et l’analyse de graphes dans le Système d’Information Géographique OrbisGIS**. [Rapport de recherche] IRSTV FR CNRS 2488; IFSTTAR. 2014.* [Link](https://halshs.archives-ouvertes.fr/halshs-01133333) *(in french)*
* *Adam Gouge, Erwan Bocher, Nicolas Fortin, Gwendall Petit. **H2Network: A tool for understanding the influence of urban mobility plans (UMP) on spatial accessibility**. Open Source Geospatial Research and Education Symposium 2014, Jun 2014, Espoo, Finland. ISBN: 978-952-60-5706-4 (electronic) / 978-952-60-5707-1 (printed), 2014, Proceedings of the 3rd Open Source Geospatial Research & Education Symposium OGRS 2014.* [Link](https://halshs.archives-ouvertes.fr/halshs-01093330/)