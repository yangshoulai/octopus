package com.octopus.core.exception;

import com.octopus.core.State;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/4/21
 */
public class IllegalStateException extends OctopusException {

  public IllegalStateException(@NonNull State state) {
    super("Illegal octopus state [" + state.getLabel() + "]");
  }
}
