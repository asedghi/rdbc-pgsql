/*
 * Copyright 2016 Krzysztof Pado
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rdbc.pgsql.codec

import io.rdbc.pgsql.core.messages.backend.{Header, PgBackendMessage, ServerCharset}
import scodec.bits.ByteVector

trait Decoder {
  def decodeMsg(bytes: ByteVector)(implicit serverCharset: ServerCharset): Either[DecodingError, Decoded[PgBackendMessage]]

  def decodeHeader(bytes: ByteVector): Either[DecodingError, Decoded[Header]]
}