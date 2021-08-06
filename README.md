# AceFX
Ace Code Editor Wrapper For JavaFX. See https://ace.c9.io/

[![](https://jitpack.io/v/orange451/AceFX.svg)](https://jitpack.io/#orange451/AceFX)

# About

This project contains a few modifications to the base code for ace editor. These changes are done to force a bridge in to Java for copying/pasting. AceFX works by wrapping Ace editor inside a web-view, but by default this means ace is prone to java's garbage collection. Some efforts are made to ensure none of the core ace features are garbage collected.


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

# Sample usage
```java
import dev.anarchy.ace.AceEditor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CodeEditorTest extends Application {
	@Override
	public void start(Stage stage) {
		AceEditor codeEditor = new AceEditor();
		codeEditor.setText("public static void main(String[] args) {\n\tSystem.out.println(\"Hello World\");\n}");
		codeEditor.setMode(Modes.Java);
		
		stage.setScene(new Scene(codeEditor, 320, 240));
		stage.centerOnScreen();
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
```
