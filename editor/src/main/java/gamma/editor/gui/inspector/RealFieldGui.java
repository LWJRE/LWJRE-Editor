package gamma.editor.gui.inspector;

import gamma.engine.annotations.EditorDegrees;
import gamma.engine.annotations.EditorRange;
import gamma.engine.annotations.EditorSlider;
import gamma.engine.scene.Component;
import imgui.ImGui;
import imgui.type.ImFloat;

import java.lang.reflect.Field;

public class RealFieldGui implements IFieldGui {

	@Override
	public void drawGui(Component component, Field field) throws IllegalAccessException {
		if(field.isAnnotationPresent(EditorRange.class)) {
			EditorRange range = field.getAnnotation(EditorRange.class);
			float value = (float) field.getDouble(component);
			float[] ptr = {field.isAnnotationPresent(EditorDegrees.class) ? (float) Math.toDegrees(value) : value};
			float step = range != null ? range.step() : 0.001f;
			float min = range != null ? range.min() : Float.NEGATIVE_INFINITY;
			float max = range != null ? range.max() : Float.POSITIVE_INFINITY;
			if(ImGui.dragFloat("##" + component.getClass() + ":" + field.getName(), ptr, step, min, max)) {
				field.set(component, field.isAnnotationPresent(EditorDegrees.class) ? (float) Math.toRadians(ptr[0]) : ptr[0]);
			}
		} else if(field.isAnnotationPresent(EditorSlider.class)) {
			EditorSlider slider = field.getAnnotation(EditorSlider.class);
			float[] ptr = {(float) field.getDouble(component)};
			float min = slider != null ? slider.min() : Float.NEGATIVE_INFINITY;
			float max = slider != null ? slider.max() : Float.POSITIVE_INFINITY;
			if(ImGui.sliderFloat("##" + component.getClass() + ":" + field.getName(), ptr, min, max)) {
				field.set(component, ptr[0]);
			}
		} else {
			ImFloat ptr = new ImFloat((float) field.getDouble(component));
			// TODO: There is also ImGui#inputDouble
			if(ImGui.inputFloat("##" + component.getClass() + ":" + field.getName(), ptr)) {
				field.set(component, ptr.get());
			}
		}
	}
}
