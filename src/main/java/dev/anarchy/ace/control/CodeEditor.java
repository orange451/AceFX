package dev.anarchy.ace.control;

import dev.anarchy.ace.CodeSyntax;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Border;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping Ace code
 * editor in a WebView.
 */
public class CodeEditor extends Control {
	/** webview used to encapsulate Ace JavaScript. */
	private final WebView webview;

	/**
	 * a snapshot of the code to be edited kept for easy initilization and reversion
	 * of editable code.
	 */
	private String cachedContent;

	/**
	 * Syntax of the code editor
	 */
	private CodeSyntax syntax;
	
	/**
	 * Whether or not the webview has finished loading.
	 */
	private boolean webViewReady;
	
	/**
	 * Whether or not the editor is read only
	 */
	private boolean readOnly;

	/**
	 * Create a new code editor.
	 */
	public CodeEditor() {
		this("");
	}

	/**
	 * Create a new code editor.
	 */
	public CodeEditor(String editingCode) {
		if ( editingCode == null )
			editingCode = "";
		
		this.cachedContent = editingCode;
		this.syntax = CodeSyntax.JAVA;
		
		this.setPadding(Insets.EMPTY);
		this.setBorder(Border.EMPTY);

		this.webview = new WebView();
		this.webview.prefWidthProperty().bind(this.widthProperty());
		this.webview.prefHeightProperty().bind(this.heightProperty());
		this.webview.maxWidthProperty().bind(this.widthProperty());
		this.webview.maxHeightProperty().bind(this.heightProperty());
		webview.getEngine().load(getClass().getResource("/ace/editor.html").toExternalForm());

		this.getChildren().add(webview);

		this.webview.getEngine().getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
			if (newState == State.SUCCEEDED) {
				webViewReady = true;
				refresh();
				applyCode(true);
				
				JSObject window = (JSObject) this.webview.getEngine().executeScript("window");
			    window.setMember("java", new CodeEditorJavaBridge(CodeEditor.this));
			}
		});
	}

	/**
	 * Set the syntax used to style this code editor.
	 */
	public void setSyntax(CodeSyntax syntax) {
		this.syntax = syntax;
		this.applySyntax(this.getSyntax());
	}

	/**
	 * Return the current syntax used to style this code editor.
	 */
	public CodeSyntax getSyntax() {
		return this.syntax;
	}

	/**
	 * Sets the current code in the editor and creates an editing snapshot of the
	 * code which can be reverted to.
	 */
	public void setText(String newCode) {
		if ( newCode == null )
			newCode = "";
		
		this.cachedContent = newCode;
		this.applyCode(false);
	}
	
	private Object executeScript(String json) {
		if ( !this.webViewReady )
			return null;
		
		try {
			return webview.getEngine().executeScript(json);
		} catch (Exception e) {
			//
		}
		return null;
	}

	/**
	 * Returns the current code in the editor.
	 */
	public String getText() {
		String text = (String) executeScript("editor.getValue();");
		if (text == null)
			text = this.cachedContent;
		return text;
	}

	/**
	 * Returns whether the editor is in read-only state. When read only the user cannot manipulate the contents of the editor.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * Returns the read-only state of the editor.
	 */
	public void setReadOnly(boolean value) {
		this.readOnly = value;
		this.applyReadOnly(value);
	}
	
	/**
	 * Transfers the contents in the clipboard into this text, replacing the current selection.
	 */
	public void paste() {
		String content = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
		this.executeScript("editor.onPaste(" + CodeEditor.toJavaScriptString(content) + ");");
	}
	
	/**
	 * Gets which text is currently selected.
	 */
	public String getSelectedText() {
		return (String) this.executeScript("editor.getSelectedText();");
	}
	
	/**
	 * If possible, undoes the last modification.
	 */
	public void undo() {
		this.executeScript("editor.undo();");
	}
	
	/**
	 * If possible, redoes the last undone modification.
	 */
	public void redo() {
		this.executeScript("editor.redo();");
	}
	
	/**
	 * Deletes the character that follows the current caret position from the text if there is no selection, or deletes the selection if there is one.
	 */
	public void deleteNextChar() {
		this.executeScript("editor.remove(\"right\");");
	}
	
	/**
	 * Deletes the character that precedes the current caret position from the text if there is no selection, or deletes the selection if there is one.
	 */
	public void deletePreviousChar() {
		this.executeScript("editor.remove(\"left\");");
	}
	
	/**
	 * Transfers the currently selected range in the text to the clipboard, removing the current selection.
	 */
	public void cut() {
		this.executeScript("editor.onCut();");
	}
	
	/**
	 * Transfers the currently selected range in the text to the clipboard, leaving the current selection.
	 */
	public void copy() {
		String text = (String) this.executeScript("editor.getCopyText();");
		
    	ClipboardContent content = new ClipboardContent();
    	content.put(DataFormat.PLAIN_TEXT, text);
    	Clipboard.getSystemClipboard().setContent(content);
	}
	
	public void selectAll() {
		this.executeScript("editor.selectAll();");
	}
	
	/**
	 * replaces all occurences of find with replace.
	 */
	public void findAndReplace(String find, String replace) {
		String json = "editor.find(${val1}); editor.replace(${val2});";
		json.replace("${val1}", toJavaScriptString(find));
		json.replace("${val2}", toJavaScriptString(replace));
		this.executeScript(json);
	}
	
	/**
	 * Gets the value of the property length.
	 */
	public int getLength() {
		return this.getText().length();
	}
	
	private void refresh() {
		applyCode(false);
		applySyntax(this.getSyntax());
		applyReadOnly(this.isReadOnly());
	}

	private void applyCode(boolean resetHistory) {
		String js = "editor.setValue(${val});";
		if (resetHistory)
			js = "editor.session.setValue(${val});";

		String encoded = toJavaScriptString(this.cachedContent);
		if ( encoded == null )
			return;
		
		encoded = js.replace("${val}", encoded);
		
		this.executeScript(encoded);
	}
	
	private void applyReadOnly(boolean value) {
		this.executeScript(("editor.setReadOnly(${val})").replace("${val}", "" + value));
	}

	private void applySyntax(CodeSyntax type) {
		this.executeScript(("editor.session.setMode(${val});").replace("${val}", toJavaScriptString(type.getType())));
	}

	@Override
	protected Skin<CodeEditor> createDefaultSkin() {
		return new CodeEditorSkin(this);
	}

	protected static String toJavaScriptString(String value) {
		if ( value == null )
			return value;
		
		value = value.replace("\u0000", "\\0")
				.replace("'", "\\'")
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
		
		return "\"" + value + "\"";
	}
}

class CodeEditorSkin extends SkinBase<CodeEditor> implements Skin<CodeEditor> {
	private static final double PREFERRED_WIDTH = 100;
	private static final double PREFERRED_HEIGHT = 100;
	private CodeEditor control;

	// ******************** Constructors **************************************
	public CodeEditorSkin(final CodeEditor control) {
		super(control);
		this.control = control;
		initGraphics();
	}

	// ******************** Initialization ************************************
	private void initGraphics() {
		if (Double.compare(control.getPrefWidth(), 0.0) <= 0 || Double.compare(control.getPrefHeight(), 0.0) <= 0
				|| Double.compare(control.getWidth(), 0.0) <= 0 || Double.compare(control.getHeight(), 0.0) <= 0) {
			if (control.getPrefWidth() > 0 && control.getPrefHeight() > 0) {
				control.setPrefSize(control.getPrefWidth(), control.getPrefHeight());
			} else {
				control.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
			}
		}
	}

	// ******************** Methods *******************************************
	@Override
	protected double computePrefWidth(final double height, final double top, final double right, final double bottom,
			final double left) {
		return super.computePrefWidth(height, top, right, bottom, left);
	}

	@Override
	protected double computePrefHeight(final double width, final double top, final double right, final double bottom,
			final double left) {
		return super.computePrefHeight(width, top, right, bottom, left);
	}

	@Override
	public void dispose() {
		control = null;
	}

	// ******************** Layout ********************************************
	@Override
	public void layoutChildren(final double x, final double y, final double width, final double height) {
		super.layoutChildren(x, y, width, height);
	}
}