package gamma.editor.core.gui;

import gamma.engine.core.scene.Scene;
import gamma.engine.core.utils.YamlUtils;
import gamma.engine.core.window.Window;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTreeNodeFlags;
import vecmatlib.vector.Vec2i;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class FileSystemGui implements IEditorGui {

	@Override
	public void draw() {
		Vec2i windowSize = Window.getCurrent().getSize();
		ImGui.setNextWindowPos(5.0f, windowSize.y() / 2.0f + 5.0f, ImGuiCond.Once);
		ImGui.setNextWindowSize(windowSize.x() / 8.0f, windowSize.y() / 2.0f - 10.0f, ImGuiCond.Once);
		ImGui.begin("File system");
		showTreeNode(Path.of("src/main/resources"));
		ImGui.end();
	}

	private static void showTreeNode(Path path) {
		int flags = ImGuiTreeNodeFlags.FramePadding | ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen;
		if(!Files.isDirectory(path))
			flags = flags | ImGuiTreeNodeFlags.Leaf;
		String fileName = path.getFileName().toString();
		if(ImGui.treeNodeEx(fileName, flags, fileName)) {
			if(ImGui.beginDragDropSource()) {
				ImGui.setDragDropPayload("Path", path);
				ImGui.text(fileName);
				ImGui.endDragDropSource();
			}
			if(ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
				if(fileName.endsWith(".yaml")) {
					Scene scene = YamlUtils.parseFile(path.toString(), Scene.class);
					Scene.changeScene(scene);
				}
			}
			if(ImGui.beginDragDropTarget()) {
				Object payload = ImGui.acceptDragDropPayload("Path");
				if(payload instanceof Path pathToMove) {
					Path destination = Path.of((Files.isDirectory(path) ? path : path.getParent()).toString(), pathToMove.getFileName().toString());
					try {
						Files.move(pathToMove, destination);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				ImGui.endDragDropTarget();
			}
			if(Files.isDirectory(path)) {
				try(Stream<Path> files = Files.list(path)) {
					files.forEach(FileSystemGui::showTreeNode);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ImGui.treePop();
		}
	}
}
