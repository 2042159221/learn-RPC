package com.ming.rpc.protocol.serialize;

import java.io.IOException;

/**
 * serializer interface
 */

public interface Serializer {
 /**
  * serialize object 
  * @param object 
  * @param <T>
  * @return
  * @throws IOException
  */
  <T> byte[] serialize(T object) throws IOException;

  /**
   * deserialize object
   * @param <T>
   * @param bytes
   * @param type
   * @return
   * @throws IOException
   */
  <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;
}
