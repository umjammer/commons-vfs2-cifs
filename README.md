[![Release](https://jitpack.io/v/umjammer/commons-vfs2-cifs.svg)](https://jitpack.io/#umjammer/commons-vfs2-cifs)
[![Java CI](https://github.com/umjammer/commons-vfs2-cifs/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/commons-vfs2-cifs/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/commons-vfs2-cifs/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/commons-vfs2-cifs/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# commons-vfs2-cifs

[Commons VFS](http://commons.apache.org/proper/commons-vfs/) provides a single API for accessing various different file systems. It presents a uniform view of the files from various different sources.

Project "commons-vfs2-cifs" is a SMB/CIFS provider for Commons VFS.

⚠ this project uses **"cifs"** as the vsf2 protocol name instead of "smb"

## Requirements

Project "commons-vfs2-cifs" requires:
* [jcifs-ng library](https://github.com/AgNO3/jcifs-ng)
* Java 17

## Install

* [maven](https://jitpack.io/#umjammer/commons-vfs2-cifs)

## Example

```java
// Retrieve file system manager
FileSystemManager fileManager = VFS.getManager();

// Configure authenticator
FileSystemOptions fileSystemOptions = new FileSystemOptions();
StaticUserAuthenticator userAuthenticator =
    new StaticUserAuthenticator(domain, login, password);

DefaultFileSystemConfigBuilder.getInstance()
    .setUserAuthenticator(fileSystemOptions, userAuthenticator);

// Resolve file object file from virtual file system
String uri = "cifs://fs/Documents";
FileObject fileObject = fileManager.resolveFile(uri, fileSystemOptions);
```

## Known Issues

To date, JCIFS has always tried NetBIOS broadcast lookups in favor of DNS which frequently resulted in 6 seconds
delay  if the jcifs.resolveOrder property was not adjusted. This behavior has been changed to try  DNS before NetBIOS
broadcast lookups which should result in much less frequent delays when using default settings. To restore the old
behavior, simply set **jcifs.resolveOrder=LMHOSTS,BCAST,DNS**.

## Might also like

* [jconditions](https://github.com/vbauer/jconditions) - Extra conditional annotations for JUnit.
* [jackdaw](https://github.com/vbauer/jackdaw) - Java Annotation Processor which allows to simplify development.
* [houdini](https://github.com/vbauer/houdini) - Type conversion system for Spring framework.
* [herald](https://github.com/vbauer/herald) - Logging annotation for Spring framework.
* [caesar](https://github.com/vbauer/caesar) - Library that allows to create async beans from sync beans.
* [avconv4java](https://github.com/vbauer/avconv4java) - Java interface to avconv tool.

## License

```
Copyright 2014 Vladislav Bauer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
