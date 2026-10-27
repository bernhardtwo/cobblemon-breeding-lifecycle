# Third-party notices

The distributed jar, `breeding-lifecycle-<version>.jar`, bundles the libraries below as nested jars, unmodified.

## Server Translations API
- Version: `2.3.1+1.21-pre2` (`xyz.nucleoid:server-translations-api`), bundled with Loom's `include`.
- License: MIT, Copyright (c) 2020 CatCore.
- Source: https://github.com/NucleoidMC/Server-Translations

Its jar does not carry a license file, so its license text follows in full:

```text
The MIT License (MIT)

Copyright (c) 2020 CatCore

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

## Packet Tweaker
- Version: `0.5.1+24w03b` (`xyz.nucleoid:packet-tweaker`), by Gegy and Patbox.
- License: GNU Lesser General Public License v3 (LGPLv3).
- Source: https://github.com/Patbox/packet-tweaker

It is not bundled by this project directly: it is nested, unmodified, inside the Server Translations API jar, at
`META-INF/jars/packet-tweaker-0.5.1+24w03b.jar`, and carries its own license file, `LICENSE_packet-tweaker`. Its
source code is available at the URL above.
