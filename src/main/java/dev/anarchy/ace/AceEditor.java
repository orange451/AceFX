package dev.anarchy.ace;

import java.io.File;
import java.util.ArrayList;

import dev.anarchy.ace.model.Command;
import dev.anarchy.ace.model.EditSession;
import dev.anarchy.ace.model.Editor;
import dev.anarchy.ace.model.ModeData;
import dev.anarchy.ace.model.ThemeData;
import dev.anarchy.ace.model.UndoManager;
import dev.anarchy.ace.util.Commons;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * A fully functional self-sufficient code editor based on ACE. <br/>
 * <br/>
 * <b>Hints</b>:
 * <ul>
 * <li>To create a new editor: <code>AceEditor.createNew()</code></li>
 * <li>To listen to events:
 * <code>addEventHandler(AceEvents.YOUR_EVENT, YOUR_EVENT_HANDLER)</code></li>
 * </ul>
 *
 * @author Sudipto Chandra.
 */
public final class AceEditor extends Control {

	// ace controller
	private JSObject mAce;

	// current editor
	private Editor mEditor;

	// file path to save code
	private File mFilePath;

	// web view where editor is loaded
	private WebView webView;

	// web engine to process java script
	private WebEngine mWebEngine;

	// text used to construct the ace editor
	private String cachedText;

	// Indices weather the web view is loaded
	private boolean isWebViewReady;

	/**
	 * Constructor
	 */
	public AceEditor() {
		this(null);
	}

	public AceEditor(String text) {
		if (text == null)
			text = "";

		setMinSize(0, 0);
		setPrefSize(200, 200);
		setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		initialize(text);
	}

	/**
	 * Initializes view
	 */
	private void initialize(String text) {
		// setup view
		this.cachedText = text;
		this.webView = new WebView();
		this.webView.prefWidthProperty().bind(this.widthProperty());
		this.webView.prefHeightProperty().bind(this.heightProperty());
		this.webView.maxWidthProperty().bind(this.widthProperty());
		this.webView.maxHeightProperty().bind(this.heightProperty());
		mWebEngine = webView.getEngine();
		this.getChildren().add(webView);
		loadAceEditor();

		// process page loading
		mWebEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) {
				if (mWebEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
					// extract javascript objects
					mAce = (JSObject) mWebEngine.executeScript("ace");
					JSObject editor = (JSObject) mAce.call("edit", "editor");
					mEditor = new Editor(editor);

					isWebViewReady = true;

					setEventCatchers(editor);
					setTheme(Themes.Chrome);
					setMode(Modes.Text);
					getSession().setValue(cachedText);

					fireEvent(new Event(AceEvents.onLoadEvent));
					
					JSObject window = (JSObject) mWebEngine.executeScript("window");
				    window.setMember("java", new AceEditorJavaBridge(AceEditor.this));
				}
			}
		});
	}

	/**
	 * Loads the ACE editor in the web engine.
	 */
	private void loadAceEditor() {
		isWebViewReady = false;
		String htmlpath = getClass().getResource("/ace/editor.html").toExternalForm();
		mWebEngine.load(htmlpath);
	}

	/**
	 * Creates event listener. <br/>
	 * This uses the 'upcall' feature from java-script to java.
	 *
	 * @param editor
	 */
	private void setEventCatchers(JSObject editor) {
		// set interface object
		editor.setMember("mAceEvent", new AceEvents(this));

		// on editor events
		editor.eval("this.on('blur', function() { editor.mAceEvent.onBlur(); });");
		editor.eval("this.on('change', function(e) { editor.mAceEvent.onChange(e); });");
		editor.eval("this.on('changeSelectionStyle', function(e) { editor.mAceEvent.onChangeSelectionStyle(e); });");
		editor.eval("this.on('changeSession', function(e) { editor.mAceEvent.onChangeSession(e); });");
		editor.eval("this.on('copy', function(e) { editor.mAceEvent.onCopy(e); });");
		editor.eval("this.on('focus', function() { editor.mAceEvent.onFocus(); });");
		editor.eval("this.on('paste', function(e) { editor.mAceEvent.onPaste(e); });");

		// on edit session events
		editor.eval("this.getSession().on('changeAnnotation', function() { editor.mAceEvent.onChangAnnotation(); });");
		editor.eval("this.getSession().on('changeBackMarker', function() { editor.mAceEvent.onChangeBackMarker(); });");
		editor.eval("this.getSession().on('changeBreakpoint', function() { editor.mAceEvent.onChangeBreakpoint(); });");
		editor.eval("this.getSession().on('changeFold', function() { editor.mAceEvent.onChangeFold(); });");
		editor.eval(
				"this.getSession().on('changeFrontMarker', function() { editor.mAceEvent.onChangeFrontMarker(); });");
		editor.eval("this.getSession().on('changeMode', function() { editor.mAceEvent.onChangeMode(); });");
		editor.eval("this.getSession().on('changeOverwrite', function() { editor.mAceEvent.onChangeOverwrite(); });");
		editor.eval(
				"this.getSession().on('changeScrollLeft', function(e) { editor.mAceEvent.onChangeScrollLeft(e); });");
		editor.eval("this.getSession().on('changeScrollTop', function(e) { editor.mAceEvent.onChangeScrollTop(e); });");
		editor.eval("this.getSession().on('changeTabSize', function() { editor.mAceEvent.onChangeTabSize(); });");
		editor.eval("this.getSession().on('changeWrapLimit', function() { editor.mAceEvent.onChangeWrapLimit(); });");
		editor.eval("this.getSession().on('changeWrapMode', function() { editor.mAceEvent.onChangeWrapMode(); });");
		editor.eval(
				"this.getSession().on('tokenizerUpdate', function(e) { editor.mAceEvent.onTokenizerUpadate(e); });");

		addEventHandler(AceEvents.onChangeEvent, new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				// undoButton.setDisable(!getUndoManager().hasUndo());
				// redoButton.setDisable(!getUndoManager().hasRedo());
			}
		});
	}

	/**
	 * Executes a script on the current web engine.
	 *
	 * @param script Script to execute.
	 * @return
	 */
	public Object executeScript(String script) throws JSException {
		return mWebEngine.executeScript(script);
	}

	/**
	 * Gets the wrapper class for editor that is associated with this control. It
	 * contains various methods to interact with the editor.
	 *
	 * @return the editor attached to this control.
	 */
	public Editor getEditor() {
		return mEditor;
	}

	/**
	 * Gets the wrapper class for edit session that is associated with the editor.
	 * It contains various methods to interact with the document under edit.
	 *
	 * @return the edit session for the editor.
	 */
	public EditSession getSession() {
		return mEditor.getSession();
	}

	/**
	 * Gets the wrapper class for undo manger that is associated with the editor. It
	 * contains methods for undo or redo operations.
	 *
	 * @return the undo manager for the edit session.
	 */
	public UndoManager getUndoManager() {
		return getSession().getUndoManager();
	}

	/**
	 * Gets the current content from the editor. If the editor is not ready an empty
	 * text is returned.
	 *
	 * @return Current content in the editor.
	 */
	public String getText() {
		return isWebViewReady ? mEditor.getValue() : cachedText;

	}

	/**
	 * Sets the given content to the editor.
	 *
	 * @param text the content to display.
	 */
	public void setText(String text) {
		this.cachedText = text;

		if (isWebViewReady)
			getEditor().setValue(text, 1);
	}

	/**
	 * Reloads the whole editor in WebView.
	 */
	public void reload() {
		loadAceEditor();
	}

	/**
	 * Performs an undo operation. Reverts the changes.
	 */
	public void undo() {
		getEditor().undo();
		// undoButton.setDisable(!getUndoManager().hasUndo());
		// redoButton.setDisable(!getUndoManager().hasRedo());
	}

	/**
	 * Performs an redo operation. Re-implements the changes.
	 */
	public void redo() {
		getEditor().redo();
		// undoButton.setDisable(!getUndoManager().hasUndo());
		// redoButton.setDisable(!getUndoManager().hasRedo());
	}

	/**
	 * Paste text from clipboard after the cursor.
	 */
	public void paste() {
		getEditor().insert(Clipboard.getSystemClipboard().getString());
	}

	/**
	 * Copies the selected text to clipboard.
	 *
	 * @return True if performed successfully.
	 */
	public boolean copy() {
		System.out.println("doCopy");
		String copy = mEditor.getCopyText();
		if (copy != null && !copy.isEmpty()) {
			ClipboardContent content = new ClipboardContent();
			content.putString(copy);
			Clipboard.getSystemClipboard().setContent(content);
			return true;
		}
		return false;
	}

	/**
	 * Removes the selected text and copy it to clipboard.
	 */
	public void cut() {
		if (copy()) {
			getEditor().insert("");
		}
	}

	/**
	 * Shows the find dialog.
	 */
	public void showFind() {
		getEditor().execCommand("find");
	}

	/**
	 * Shows the replace dialog.
	 */
	public void showReplace() {
		getEditor().execCommand("replace");
	}

	/**
	 * Shows the options pane.
	 */
	public void showOptions() {
		getEditor().execCommand("showSettingsMenu");
	}

	/**
	 * Select the syntax highlighting mode for ace-editor. Some pre-defined
	 * supported mode can be found in <code>Modes</code> class.
	 *
	 * @see Modes
	 * @param mode Mode like "ace/mode/java".
	 */
	public void setMode(ModeData mode) {
		getEditor().getSession().setMode(mode.getAlias());
	}

	/**
	 * Currently enabled language mode.
	 *
	 * @see EditSession.getMode()
	 * @return the current mode.
	 */
	public ModeData getMode() {
		return Modes.getModeByAlias(getSession().getMode());
	}

	/**
	 * Sets a theme to the editor. Some pre-defined can be found in
	 * <code>Themes</code> class.
	 *
	 * @see Themes
	 * @param theme Theme to set (must contain valid alias).
	 */
	public void setTheme(ThemeData theme) {
		getEditor().setTheme(theme.getAlias());
	}

	/**
	 * Gets the current theme.
	 *
	 * @return
	 */
	public ThemeData getTheme() {
		return Themes.getThemeByAlias(getEditor().getTheme());
	}

	/**
	 * Gets a list of all available command and keyboard shortcuts
	 *
	 * @deprecated for internal usage only.
	 * @return list of available commands
	 */
	@Deprecated
	public ArrayList<Command> getCommandList() {
		JSObject names = (JSObject) mEditor.getModel().eval("this.commands.byName");
		ArrayList<Command> arr = new ArrayList<>();
		for (String str : Commons.getAllProperties(names)) {
			arr.add(new Command((JSObject) names.getMember(str)));
		}
		return arr;
	}

	@Override
	protected Skin<AceEditor> createDefaultSkin() {
		return new CodeEditorSkin(this);
	}
}

class CodeEditorSkin extends SkinBase<AceEditor> implements Skin<AceEditor> {
	private static final double PREFERRED_WIDTH = 100;
	private static final double PREFERRED_HEIGHT = 100;
	private AceEditor control;

	// ******************** Constructors **************************************
	public CodeEditorSkin(final AceEditor control) {
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