package dev.anarchy.ace;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CodeEditorTest extends Application {
	
	@Override
	public void start(Stage stage) {
		AceEditor codeEditor = new AceEditor();
		codeEditor.setText("public static void main(String[] args) {\n\tSystem.out.println(\"Hello World\");\n}");
		codeEditor.setOption("scrollPastEnd", 0.5);
		codeEditor.setMode(Modes.Java);
		
		stage.setScene(new Scene(codeEditor, 320, 240));
		stage.centerOnScreen();
		stage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
