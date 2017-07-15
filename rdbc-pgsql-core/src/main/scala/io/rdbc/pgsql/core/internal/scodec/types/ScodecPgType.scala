/*
 * Copyright 2016 rdbc contributors
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

package io.rdbc.pgsql.core.internal.scodec.types

import io.rdbc.pgsql.core.SessionParams
import io.rdbc.pgsql.core.exception.PgDecodeException
import io.rdbc.pgsql.core.types.PgType
import scodec.Attempt.{Failure, Successful}
import scodec.bits.ByteVector

import scala.reflect.ClassTag
import scala.util.{Success, Try}

private[types] abstract class ScodecPgType[T: ClassTag] extends PgType[T] {

  def decoder(implicit sessionParams: SessionParams): scodec.Decoder[T]
  def encoder(implicit sessionParams: SessionParams): scodec.Encoder[T]

  def toObj(binaryVal: ByteVector)(implicit sessionParams: SessionParams): Try[T] = {
    decoder.decodeValue(binaryVal.bits) match {
      case Successful(value) => Success(value)
      case Failure(err) => util.Failure(new PgDecodeException(
        s"Error decoding '${binaryVal.toHex}' of PG type '$name' as '${implicitly[ClassTag[T]]}': "
          + err.messageWithContext
      ))
    }
  }

  def toPgBinary(obj: T)(implicit sessionParams: SessionParams): Try[ByteVector] = {
    encoder.encode(obj) match {
      case Successful(value) => Success(value.bytes)
      case Failure(err) => util.Failure(new PgDecodeException(
        s"Error encoding '$obj' of type '${obj.getClass}' to PG type '$name': "
          + err.messageWithContext
      ))
    }
  }
}