package io.github.lwjre.editor.gui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A popup that can take a string input.
 *
 * @author Nico
 */
public class TextInputPopup implements GuiComponent {

	/** The popup's title */
	private String title = "Text input";
	/** Text to write in the popup's body */
	private String content = "";

	/** The text input */
	private String input = "";
	/** Action to perform when the text is entered */
	private Consumer<String> onConfirm = str -> {};

	/** Set to true to open the popup */
	private boolean shouldOpen = false;

	@Override
	public void draw() {
		if(this.shouldOpen) {
			ImGui.openPopup(this.title);
			this.shouldOpen = false;
		}
		if(ImGui.beginPopupModal(this.title)) {
			ImGui.text(this.content);
			ImString ptr = new ImString(this.input, 256);
			ImGui.setKeyboardFocusHere();
			if(ImGui.inputText("##input", ptr, ImGuiInputTextFlags.EnterReturnsTrue)) {
				this.onConfirm.accept(ptr.get());
				ImGui.closeCurrentPopup();
			}
			if(ImGui.button("Cancel") || ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				ImGui.closeCurrentPopup();
			}
			ImGui.endPopup();
		}
	}

	/**
	 * Sets the title of the popup.
	 *
	 * @param title The title of the popup (must not be null)
	 */
	public void setTitle(String title) {
		this.title = Objects.requireNonNull(title);
	}

	/**
	 * Sets the content of the popup.
	 *
	 * @param content The content of the popup (must not be null)
	 */
	public void setContent(String content) {
		this.content = Objects.requireNonNull(content);
	}

	/**
	 * Sets the current text input.
	 *
	 * @param input The text input (must not be null)
	 */
	public void setInput(String input) {
		this.input = Objects.requireNonNull(input);
	}

	/**
	 * Opens the popup.
	 *
	 * @param onConfirm Action to perform when the text is entered
	 */
	public void open(Consumer<String> onConfirm) {
		this.onConfirm = Objects.requireNonNull(onConfirm);
		this.shouldOpen = true;
	}
}
