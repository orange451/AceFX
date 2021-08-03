package dev.anarchy.ace;

import dev.anarchy.ace.control.CodeEditor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CodeEditorTest extends Application {
	
	@Override
	public void start(Stage stage) {
		CodeEditor codeEditor = new CodeEditor("public static void main(String[] args) {\n\tSystem.out.println(\"Hello World\");\n}");
		codeEditor.setSyntax(CodeSyntax.JAVA);
		
		stage.setScene(new Scene(codeEditor, 320, 240));
		stage.centerOnScreen();
		stage.show();
		
		new Thread(() ->{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Platform.runLater(()->{
				codeEditor.selectAll();
			});
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Platform.runLater(()->{
				codeEditor.cut();
			});
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Platform.runLater(()->{
				codeEditor.paste();
			});
			
		}).start();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
