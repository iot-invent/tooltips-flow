package io.rtlscloud.vaadin.hyper.tooltip.config;

import elemental.json.JsonValue;
import java.io.Serializable;

interface JsonConvertible extends Serializable {

  Object getValue();

  default JsonValue toJson() {
    return TooltipConfigurationJsonSerializer.toJson(getValue());
  }
}
