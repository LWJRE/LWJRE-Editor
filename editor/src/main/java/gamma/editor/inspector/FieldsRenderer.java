package gamma.editor.inspector;

import gamma.engine.annotations.*;
import gamma.engine.resources.Resource;
import gamma.engine.resources.Resources;
import gamma.engine.scene.Component;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import vecmatlib.color.Color3f;
import vecmatlib.color.Color4f;
import vecmatlib.vector.Vec2f;
import vecmatlib.vector.Vec3f;
import vecmatlib.vector.Vec4f;

import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public final class FieldsRenderer {

	public static void renderField(Component component, Field field) throws IllegalAccessException {
		if(field.isAnnotationPresent(EditorVariable.class)) {
			field.setAccessible(true);
			EditorVariable annotation = field.getAnnotation(EditorVariable.class);
			ImGui.text(" " + (annotation.value().isEmpty() ? field.getName() : annotation.value()));
			ImGui.sameLine();
			if(field.getType().equals(float.class) || field.getType().equals(double.class)) {
				renderRealField(component, field);
			} else if(field.getType().equals(byte.class)) {
				renderIntegerField(component, field, Byte.MIN_VALUE, Byte.MAX_VALUE);
			} else if(field.getType().equals(short.class)) {
				renderIntegerField(component, field, Short.MIN_VALUE, Short.MAX_VALUE);
			} else if(field.getType().equals(int.class)) {
				renderIntegerField(component, field, Integer.MIN_VALUE, Integer.MAX_VALUE);
			} else if(field.getType().equals(long.class)) {
				renderIntegerField(component, field, Long.MIN_VALUE, Long.MAX_VALUE);
			} else if(field.getType().equals(boolean.class)) {
				renderBooleanField(component, field);
			} else if(field.getType().equals(String.class)) {
				renderTextField(component, field);
			} else if(field.getType().equals(Vec2f.class)) {
				renderVecField2(component, field);
			} else if(field.getType().equals(Vec3f.class)) {
				renderVecField3(component, field);
			} else if(field.getType().equals(Vec4f.class)) {
				renderVecField4(component, field);
			} else if(field.getType().equals(Color3f.class)) {
				renderColorField3(component, field);
			} else if(field.getType().equals(Color4f.class)) {
				renderColorField4(component, field);
			} else if(Resource.class.isAssignableFrom(field.getType())) {
				renderResource(component, field);
			} else {
				ImGui.newLine();
			}
		}
	}

	private static void renderRealField(Component component, Field field) throws IllegalAccessException {
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
			if(ImGui.inputFloat("##" + component.getClass() + ":" + field.getName(), ptr)) {
				field.set(component, ptr.get());
			}
		}
	}

	private static void renderIntegerField(Component component, Field field, float defaultMin, float defaultMax) throws IllegalAccessException {
		if(field.isAnnotationPresent(EditorRange.class)) {
			EditorRange range = field.getAnnotation(EditorRange.class);
			int[] ptr = {(int) field.getLong(component)};
			float step = range != null ? range.step() : 0.001f;
			float min = range != null ? range.min() : defaultMin;
			float max = range != null ? range.max() : defaultMax;
			if(ImGui.dragInt("##" + component.getClass() + ":" + field.getName(), ptr, step, min, max)) {
				field.set(component, ptr[0]);
			}
		} else if(field.isAnnotationPresent(EditorSlider.class)) {
			EditorSlider slider = field.getAnnotation(EditorSlider.class);
			int[] ptr = {(int) field.getLong(component)};
			int min = (int) (slider != null ? slider.min() : defaultMin);
			int max = (int) (slider != null ? slider.max() : defaultMax);
			if(ImGui.sliderInt("##" + component.getClass() + ":" + field.getName(), ptr, min, max)) {
				field.set(component, ptr[0]);
			}
		} else {
			ImInt ptr = new ImInt((int) field.getLong(component));
			if(ImGui.inputInt("##" + component.getClass() + ":" + field.getName(), ptr)) {
				field.set(component, ptr.get());
			}
		}
	}

	private static void renderBooleanField(Component component, Field field) throws IllegalAccessException {
		boolean current = field.getBoolean(component);
		if(ImGui.checkbox("##" + component.getClass() + ":" + field.getName(), current)) {
			field.set(component, !current);
		}
	}

	private static void renderTextField(Component component, Field field) throws IllegalAccessException {
		ImString ptr = new ImString((String) field.get(component), 256);
		if(ImGui.inputText("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, ptr.get());
		}
	}

	private static void renderVecField2(Component component, Field field) throws IllegalAccessException {
		Vec2f vector = (Vec2f) field.get(component);
		float[] ptr = {vector.x(), vector.y()};
		if(field.isAnnotationPresent(EditorRange.class)) {
			EditorRange range = field.getAnnotation(EditorRange.class);
			float step = range != null ? range.step() : 0.001f;
			float min = range != null ? range.min() : Float.NEGATIVE_INFINITY;
			float max = range != null ? range.max() : Float.POSITIVE_INFINITY;
			if(ImGui.dragFloat2("##" + component.getClass() + ":" + field.getName(), ptr, step, min, max)) {
				field.set(component, new Vec2f(ptr[0], ptr[1]));
			}
		} else if(ImGui.inputFloat2("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, new Vec2f(ptr[0], ptr[1]));
		}
	}

	private static void renderVecField3(Component component, Field field) throws IllegalAccessException {
		Vec3f vector = (Vec3f) field.get(component);
		float[] ptr = {vector.x(), vector.y(), vector.z()};
		if(field.isAnnotationPresent(EditorRange.class)) {
			EditorRange range = field.getAnnotation(EditorRange.class);
			float step = range != null ? range.step() : 0.001f;
			float min = range != null ? range.min() : Float.NEGATIVE_INFINITY;
			float max = range != null ? range.max() : Float.POSITIVE_INFINITY;
			if(ImGui.dragFloat3("##" + component.getClass() + ":" + field.getName(), ptr, step, min, max)) {
				field.set(component, new Vec3f(ptr[0], ptr[1], ptr[2]));
			}
		} else if(ImGui.inputFloat3("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, new Vec3f(ptr[0], ptr[1], ptr[2]));
		}
	}

	private static void renderVecField4(Component component, Field field) throws IllegalAccessException {
		Vec4f vector = (Vec4f) field.get(component);
		float[] ptr = {vector.x(), vector.y(), vector.z(), vector.w()};
		if(field.isAnnotationPresent(EditorRange.class)) {
			EditorRange range = field.getAnnotation(EditorRange.class);
			float step = range != null ? range.step() : 0.001f;
			float min = range != null ? range.min() : Float.NEGATIVE_INFINITY;
			float max = range != null ? range.max() : Float.POSITIVE_INFINITY;
			if(ImGui.dragFloat4("##" + component.getClass() + ":" + field.getName(), ptr, step, min, max)) {
				field.set(component, new Vec4f(ptr[0], ptr[1], ptr[2], ptr[3]));
			}
		} else if(ImGui.inputFloat4("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, new Vec4f(ptr[0], ptr[1], ptr[2], ptr[3]));
		}
	}

	private static void renderColorField3(Component component, Field field) throws IllegalAccessException {
		Color3f color = (Color3f) field.get(component);
		float[] ptr = {color.r(), color.g(), color.b()}; // TODO: Color flags
		if(ImGui.colorEdit3("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, new Color3f(ptr[0], ptr[1], ptr[2]));
		}
	}

	private static void renderColorField4(Component component, Field field) throws IllegalAccessException {
		Color4f color = (Color4f) field.get(component);
		float[] ptr = {color.r(), color.g(), color.b(), color.a()}; // TODO: Color flags
		if(ImGui.colorEdit4("##" + component.getClass() + ":" + field.getName(), ptr)) {
			field.set(component, new Color4f(ptr[0], ptr[1], ptr[2], ptr[3]));
		}
	}

	private static void renderResource(Component component, Field field) throws IllegalAccessException {
		Resource resource = (Resource) field.get(component);
		String str = Resources.pathOf(resource);
		if(field.isAnnotationPresent(DefaultValueString.class)) {
			DefaultValueString defaultValue = field.getAnnotation(DefaultValueString.class);
			if(str.equals(defaultValue.value()) && defaultValue.hide()) {
				ImString ptr = new ImString("", 256);
				if(ImGui.inputText("##" + component.getClass() + ":" + field.getName(), ptr)) {
					setResource(component, field, ptr.get());
				}
			} else {
				ImString ptr = new ImString(str, 256);
				if(ImGui.inputText("##" + component.getClass() + ":" + field.getName(), ptr)) {
					String val = ptr.get();;
					setResource(component, field, val.isEmpty() ? defaultValue.value() : val);
				}
			}
			// TODO: Add button to reset to default value
		} else {
			ImString ptr = new ImString(str, 256);
			if(ImGui.inputText("##" + component.getClass() + ":" + field.getName(), ptr)) {
				setResource(component, field, ptr.get());
			}
		}
		if(ImGui.beginDragDropTarget()) {
			Object payload = ImGui.acceptDragDropPayload("Path");
			if(payload instanceof Path path) {
				setResource(component, field, path.toString().substring(19));
			}
			ImGui.endDragDropTarget();
		}
	}

	private static void setResource(Component component, Field field, String value) throws IllegalAccessException {
		try {
			Resource newResource = Resources.getOrLoad(value);
			if(newResource.getClass().isAssignableFrom(field.getType())) {
				EditorVariable annotation = field.getAnnotation(EditorVariable.class);
				if(!annotation.setter().isEmpty()) {
					try {
						component.getClass().getDeclaredMethod(annotation.setter(), newResource.getClass()).invoke(component, newResource);
					} catch (InvocationTargetException | NoSuchMethodException e) {
						e.printStackTrace();
					}
				} else {
					field.set(component, newResource);
				}
			}
		} catch(NoSuchElementException | UncheckedIOException ignored) {}
	}
}