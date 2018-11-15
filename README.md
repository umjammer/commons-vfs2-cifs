
# commons-vfs2-cifs

[![Build Status](http://img.shields.io/travis/vbauer/commons-vfs2-cifs.svg?style=flat)](https://travis-ci.org/vbauer/commons-vfs2-cifs)
[![License](http://img.shields.io/badge/License-Apache%2C%20Version%202.0-blue.svg?style=flat)](http://opensource.org/licenses/Apache-2.0)
[![Maven](https://img.shields.io/github/tag/vbauer/commons-vfs2-cifs.svg?label=maven)](https://jitpack.io/#vbauer/commons-vfs2-cifs)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/f3f8cb0b44c84349b0f56e13cae6832f)](https://www.codacy.com/app/bauer-vlad/commons-vfs2-cifs)

[Commons VFS](http://commons.apache.org/proper/commons-vfs/) provides a single API for accessing various different file systems. It presents a uniform view of the files from various different sources.

Project "commons-vfs2-cifs" is a SMB/CIFS provider for Commons VFS.


## Requirements

Project "commons-vfs2-cifs" requires:
* [JCIFS library](http://jcifs.samba.org)
* Java 8


## Setup

Gradle:
```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.vbauer:commons-vfs2-cifs:1.2.0'
}
```

Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.vbauer</groupId>
    <artifactId>commons-vfs2-cifs</artifactId>
    <version>1.2.0</version>
</dependency>
```


## Example

```java
// Retrieve file system manager
final FileSystemManager fileManager = VFS.getManager();

// Configure authenticator
final FileSystemOptions fileSystemOptions = new FileSystemOptions();
final StaticUserAuthenticator userAuthenticator =
    new StaticUserAuthenticator(domain, login, password);

DefaultFileSystemConfigBuilder.getInstance()
    .setUserAuthenticator(fileSystemOptions, userAuthenticator);

// Resolve file object file from virtual file system
final String uri = "smb://fs/Documents";
final FileObject fileObject = fileManager.resolveFile(uri, fileSystemOptions);
```


## Known Issues

To date, JCIFS has always tried NetBIOS broadcast lookups in favor of DNS which frequently resulted in a 6 second
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
