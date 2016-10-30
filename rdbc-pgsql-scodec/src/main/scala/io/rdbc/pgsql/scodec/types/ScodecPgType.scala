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

package io.rdbc.pgsql.scodec.types

import io.rdbc.pgsql.core.SessionParams
import io.rdbc.pgsql.core.types.PgType
import scodec.Attempt.{Failure, Successful}
import scodec.Codec
import scodec.bits.BitVector

trait ScodecPgType[T] extends PgType[T] {

  def decodeCodec(implicit sessionParams: SessionParams): Codec[T]
  def encodeCodec(implicit sessionParams: SessionParams): Codec[T]

  override def toObj(binaryVal: Array[Byte])(implicit sessionParams: SessionParams): T = decodeCodec.decodeValue(BitVector.view(binaryVal)) match {
    case Successful(value) => value
    case Failure(err) => throw new RuntimeException(err.messageWithContext) //TODO DecodeException
  }

  override def toPgBinary(obj: T)(implicit sessionParams: SessionParams): Array[Byte] = encodeCodec.encode(obj) match {
    case Successful(value) => value.toByteArray
    case Failure(err) => throw new RuntimeException(err.messageWithContext) //TODO EncodeException
  }
}