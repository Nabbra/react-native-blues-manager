package com.nabbra.rnbluesmanager.contracts

import com.facebook.react.bridge.WritableMap

interface MappableContract {
  /**
   * Implement to provide the [WritableMap] representation of this object.
   *
   * @return
   */
  fun map(): WritableMap?
}
