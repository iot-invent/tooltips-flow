package io.rtlscloud.vaadin.hyper.tooltip.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
/**
 * Determines if the tippy sticks to the reference element while it is mounted.
 *
 * Documentation: https://atomiks.github.io/tippyjs/v6/all-props/#sticky
 *
 * @author Gerrit Sedlaczek
 */
public enum Sticky implements JsonConvertible {
  FALSE(false),
  TRUE(true),
  REFERENCE("reference"),
  POPPER("POPPER");

  @Getter
  private final Object value;
}
