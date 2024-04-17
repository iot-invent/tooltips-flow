package io.rtlscloud.vaadin.tooltip.config;

import com.vaadin.flow.component.JsonSerializable;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonNull;
import elemental.json.impl.JreJsonObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

public class TooltipConfigurationJsonSerializer {

  private final static Logger log = Logger.getLogger(TooltipConfigurationJsonSerializer.class.getName());
  private final static JsonFactory jsonFactory = new JreJsonFactory();

  public static JsonValue toJson(Object bean) {
    return toJson(bean, false);
  }

  private static JsonValue toJson(Object bean, final boolean isNullabel) {
    if (bean == null) {
      return isNullabel ? new JreJsonNull() : null;
    }
    if (bean instanceof Collection) {
      return fromCollection((Collection<?>) bean);
    }
    if (bean.getClass().isArray()) {
      return fromCollection(bean);
    }
    if (bean instanceof Map) {
      return fromMapUnchecked(bean);
    }

    Optional<JsonValue> simpleType = tryToConvertToSimpleType(bean);
    if (simpleType.isPresent()) {
      return simpleType.get();
    }

    if (bean instanceof JsonConvertible) {
      return ((JsonConvertible) bean).toJson();
    }
    if (bean instanceof JsonSerializable) {
      return ((JsonSerializable) bean).toJson();
    }
    if (bean instanceof Enum) {
      return Json.create(((Enum<?>) bean).name());
    }
    if (bean instanceof JsonValue) {
      return (JsonValue) bean;
    }

    return fromFields(bean);
  }

  private static <T> JsonObject fromFields(T bean) {
    return fromFields(bean, bean.getClass());
  }

  private static <T> JsonObject fromFields(T bean, Class<? extends T> clazz) {
    JsonObject json;
    Class<?> superclass = clazz.getSuperclass();

    if (superclass == Object.class) {
      json = new JreJsonObject(jsonFactory);

      for (Field field : clazz.getDeclaredFields()) {
        String fieldName = field.getName();
          if (fieldName.equals("serialVersionUID")) {
              continue;
          }

        field.setAccessible(true);
        try {
          JsonValue value = toJson(field.get(bean), false);
            if (value != null) {
                json.put(fieldName, value);
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
          log.warning("Failed to convert field=" + field + " of bean=" + bean);
        }
      }

    } else {
      json = fromFields(bean, superclass);
    }

    return json;
  }

  public static JsonArray fromCollection(Collection<?> beans) {
    JsonArray array = Json.createArray();
    if (beans == null) {
      return array;
    }

    beans.stream()
        .map(bean -> TooltipConfigurationJsonSerializer.toJson(bean, true))
        .forEachOrdered(json -> array.set(array.length(), json));
    return array;
  }

  private static JsonArray fromCollection(Object javaArray) {
    int length = Array.getLength(javaArray);
    JsonArray array = Json.createArray();
    for (int i = 0; i < length; i++) {
      JsonValue beanValue = toJson(Array.get(javaArray, i), true);
        if (beanValue != null) {
            array.set(i, beanValue);
        }
    }
    return array;
  }

  @SuppressWarnings("unchecked")
  private static JsonObject fromMapUnchecked(Object bean) {
    // keys are always (fingers crossed) strings
    return fromMap((Map<String, Object>) bean);
  }

  private static JsonObject fromMap(Map<String, Object> map) {
    JsonObject json = Json.createObject();

    for(Entry<String, Object> entry : map.entrySet()) {
      json.put(entry.getKey(), toJson(entry.getValue()));
    }

    return json;
  }

  private static Optional<JsonValue> tryToConvertToSimpleType(Object bean) {
    if (bean instanceof String) {
      return Optional.of(Json.create((String) bean));
    }
    if (bean instanceof Number) {
      return Optional.of(Json.create(((Number) bean).doubleValue()));
    }
    if (bean instanceof Boolean) {
      return Optional.of(Json.create((Boolean) bean));
    }
    if (bean instanceof Character) {
      return Optional.of(Json.create(Character.toString((char) bean)));
    }
    return Optional.empty();
  }
}
