package dev.anarchy.ace;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

public class AceEditorJavaBridge {
	private AceEditor editor;
	
	public AceEditorJavaBridge(AceEditor editor) {
		this.editor = editor;
	}
	
	public void paste() {
		String data = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
		System.out.println("Paste from bridge:\n" + data);
        //editor.paste();
    }
    
	public void copy(Object data) {
		Platform.runLater(()->{
			System.out.println("Copying from bridge:\n" + data);
	    	ClipboardContent content = new ClipboardContent();
	    	content.put(DataFormat.PLAIN_TEXT, data.toString());
	    	Clipboard.getSystemClipboard().setContent(content);
		});
    }
}