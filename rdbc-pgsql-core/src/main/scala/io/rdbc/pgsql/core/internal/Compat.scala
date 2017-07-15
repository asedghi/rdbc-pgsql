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

package io.rdbc.pgsql.core.internal

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

private[core] object Compat {

  implicit class FutureObjectCompat(underlying: Future.type) {
    val unit: Future[Unit] = Future.successful(())
  }

  implicit class FutureCompat[+T](underlying: Future[T]) {

    def transformWith[S](f: Try[T] => Future[S])
                        (implicit executor: ExecutionContext): Future[S] = {
      underlying.flatMap { res =>
        f(Success(res))
      }.recoverWith { case ex =>
        f(Failure(ex))
      }
    }
  }

  implicit class TryCompat[+T](underlying: Try[T]) {
    def fold[U](mapFailure: Throwable => U, mapSuccess: T => U): U = {
      try {
        mapSuccess(underlying.get)
      } catch {
        case NonFatal(e) => mapFailure(e)
      }
    }
  }

}
