# play-playful plugin

This module provides a set of libraries for Play! Framework 1.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - playful -> playful 1.3.0

repositories:
    - playful:
        type:       http
        artifact:   "http://release.sismics.com/repo/play/[module]-[revision].zip"
        contains:
            - playful -> *
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
