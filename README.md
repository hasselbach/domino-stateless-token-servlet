# domino-statelesstoken-servlet
Servlet for authenticating with a stateless token (hashed with HmacSHA256)
Alpha Version

* http://example.com/token/create/?username=CN%3DSven%20Hasselbach%2FOU%3DHasselba%2FO%3DDE&password=XXX
Generates a new token

* http://example.com/token/validate/?token=MDkgRmViIDIwMTUgMjI6NDk6MTIuMzU2IUNOPVN2ZW4gSGFzc2VsYmFjaC9PVT1IYXNzZWxiYS9PPURF!9s7dSS7F67hSS/lLODZHVM0NsTR4IurkZtjiysGzoWA=
Validates a token


---
 Copyright 2015 Sven Hasselbach

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.