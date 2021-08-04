# AceFX
Ace Code Editor Wrapper For JavaFX

See https://ace.c9.io/

# About

This has a few modifications to ace. Mainly for fixing buggy java-fx copy/paste bridge code. This project is a little hacky internally but should functional well on both windows and mac.


# Add to your project:
Gradle:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	implementation 'com.github.orange451:AceFX:main-SNAPSHOT'
}
```


Maven:
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.orange451</groupId>
    <artifactId>AceFX</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```
