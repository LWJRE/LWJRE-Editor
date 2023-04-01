package gamma.editor.gui;

import gamma.engine.scene.Entity;
import gamma.engine.scene.Scene;
import gamma.engine.window.Window;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import vecmatlib.vector.Vec2i;

public class SceneTreeGui implements IEditorGui {

	private final InspectorGui inspector;

	private Entity renaming;
	private String newName;

	private Entity clipboardEntity;
	private String clipboardName;

	public SceneTreeGui(InspectorGui inspector) {
		this.inspector = inspector;
	}

	@Override
	public void draw() {
		Vec2i windowSize = Window.getCurrent().getSize();
		ImGui.setNextWindowPos(5.0f, 15.0f, ImGuiCond.FirstUseEver);
		ImGui.setNextWindowSize(windowSize.x() / 8.0f, windowSize.y() / 2.0f - 10.0f, ImGuiCond.FirstUseEver);
		ImGui.begin("Scene tree");
		this.drawEntity("root", Scene.getCurrent().root, true);
		if(this.renaming != null && this.renaming.getParent() != null && this.newName != null) {
			if(!this.newName.isEmpty()) {
				String actualName = this.newName;
				int i = 1;
				while(this.renaming.getParent().hasChild(actualName)) {
					actualName = this.newName + i;
					i++;
				}
				this.renaming.getParent().renameChild(this.renaming, actualName);
			}
			this.renaming = null;
			this.newName = null;
		}
		ImGui.end();
	}

	private void drawEntity(String name, Entity entity, boolean isRoot) {
		int flags = ImGuiTreeNodeFlags.FramePadding | ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.AllowItemOverlap;
		if(entity.getChildCount() == 0)
			flags = flags | ImGuiTreeNodeFlags.Leaf;
		if(entity == this.inspector.entity)
			flags = flags | ImGuiTreeNodeFlags.Selected;
		if(entity == this.clipboardEntity)
			ImGui.pushStyleColor(ImGuiCol.Text, 0.8f, 0.8f, 0.8f, 1.0f);
		// Draw entity as tree node
		if(ImGui.treeNodeEx(name, flags, this.renaming != entity ? name : "")) {
			if(entity == this.clipboardEntity)
				ImGui.popStyleColor();
			// Draw text input if the entity is being renamed
			if(this.renaming == entity) {
				ImGui.sameLine();
				ImGui.setKeyboardFocusHere();
				ImString ptr = new ImString(name, 256);
				if(ImGui.inputText("##" + entity, ptr, ImGuiInputTextFlags.EnterReturnsTrue)) {
					this.newName = ptr.get();
				}
			}
			// Set the entity in the inspector when clicked
			if(ImGui.isItemClicked()) {
				this.inspector.entity = entity;
			}
			// Drag and drop source
			if(ImGui.beginDragDropSource()) {
				ImGui.setDragDropPayload("Entity", new Object[] {entity, name});
				ImGui.text(name);
				ImGui.endDragDropSource();
			}
			// Drag and drop target
			if(ImGui.beginDragDropTarget()) {
				Object[] payload = ImGui.acceptDragDropPayload("Entity");
				if(payload != null && payload[0] instanceof Entity entityToMove && payload[1] instanceof String key) {
					Entity parent = entity.getParent();
					while(parent != null && parent != entityToMove)
						parent = parent.getParent();
					if(parent != entityToMove && !entityToMove.hasParent(entity)) {
						String newKey = key;
						int i = 1;
						while(entity.hasChild(newKey))
							newKey = key + i;
						entityToMove.setParent(newKey, entity);
					}
				}
				ImGui.endDragDropTarget();
			}
			// Stop renaming when another entity is clicked
			if(ImGui.isItemHovered() && ImGui.isMouseClicked(0) && this.renaming != null) {
				this.renaming = null;
			}
			// Enable renaming when an entity is double-clicked
			if(this.inspector.entity == entity && ImGui.isMouseDoubleClicked(0) && !isRoot) {
				this.renaming = entity;
			}
			if(ImGui.beginPopupContextItem()) {
				if(ImGui.menuItem("Add child entity", "Ctrl+A")) {
					entity.addChild(new Entity());
				}
				ImGui.separator();
				if(ImGui.menuItem("Cut", "Ctrl+X")) {
					this.clipboardEntity = entity;
					this.clipboardName = name;
				}
				if(ImGui.menuItem("Copy", "Ctrl+C")) {
					this.clipboardEntity = entity; // TODO: Clone entity
					this.clipboardName = name;
				}
				if(ImGui.menuItem("Paste", "Ctrl+V")) {
					if(this.clipboardEntity != null) {
						this.clipboardEntity.setParent(this.clipboardName, entity);
//						this.clipboardEntity = this.clipboardEntity; // TODO: Clone entity
					}
				}
				ImGui.separator();
				if(ImGui.menuItem("Delete entity", "Delete")) {
					entity.removeFromScene();
				}
				ImGui.endPopup();
			}
			// Render children
			entity.forEachChild((key, child) -> this.drawEntity(key, child, false));
			// End node
			ImGui.treePop();
		}
	}
}