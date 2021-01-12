[![GitHub release](https://img.shields.io/github/release/sismics/play-playful.svg?style=flat-square)](https://github.com/sismics/play-playful/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# play-playful plugin

This module provides a set of libraries for Play! Framework 1.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - playful -> playful 1.7.0

repositories:
    - sismicsNexusRaw:
        type: http
        artifact: "https://nexus.sismics.com/repository/sismics/[module]-[revision].zip"
        contains:
            - playful -> *
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
