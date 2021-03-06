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

import io.rdbc.ImmutIndexedSeq
import io.rdbc.pgsql.core.StmtArgsConverter
import io.rdbc.sapi.exceptions._
import io.rdbc.pgsql.core.internal.PgNativeStatement.Params.{Named, Positional}
import io.rdbc.pgsql.core.internal.protocol.Argument
import io.rdbc.sapi._
import io.rdbc.util.Logging
import io.rdbc.util.Preconditions._
import org.reactivestreams.Publisher

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

private[core] class PgStatement(stmtExecutor: PgStatementExecutor,
                                nativeStmt: PgNativeStatement,
                                argConverter: StmtArgsConverter)
                               (implicit ec: ExecutionContext)
  extends Statement
    with Logging {

  //TODO try to limit exceptions to public methods only - scan the code

  def bind(args: (String, Any)*): ExecutableStatement = traced {
    checkNotNull(args)
    val pgArgs = namedArgsToPgArgs(Map(args: _*)).get
    pgParametrizedStatement(pgArgs)
  }

  def bindByIdx(args: Any*): ExecutableStatement = traced {
    checkNotNull(args)
    val pgArgs = positionalArgsToPgArgs(args.toVector).get
    pgParametrizedStatement(pgArgs)
  }

  def noArgs: ExecutableStatement = traced(bindByIdx())

  private def namedArgsToPgArgs(namedArgs: Map[String, Any]): Try[Vector[Argument]] = traced {
    nativeStmt.params match {
      case Positional(_) =>
        namedArgs.headOption match {
          case Some((param, _)) => Failure(new NoSuchParamException(param))
          case None => Success(Vector.empty)
        }

      case namedParams: Named =>
        argConverter.convertNamedArgs(namedArgs, namedParams)(stmtExecutor.sessionParams)
    }
  }

  private def positionalArgsToPgArgs(posArgs: ImmutIndexedSeq[Any]): Try[Vector[Argument]] = traced {
    val size = nativeStmt.params match {
      case Positional(count) => count
      case Named(seq) => seq.size
    }
    if (posArgs.size < size) {
      nativeStmt.params match {
        case Positional(_) => Failure(new MissingParamValException((posArgs.size + 1).toString))
        case Named(seq) => Failure(new MissingParamValException(seq(posArgs.size)))
      }
    } else if (posArgs.size > size) {
      Failure(new TooManyParamsException(provided = posArgs.size, expected = size))
    } else {
      argConverter.convertPositionalArgs(posArgs.toVector)(stmtExecutor.sessionParams)
    }
  }

  def streamArgs(argsPublisher: Publisher[_ <: Map[String, Any]]): Future[Unit] = traced {
    checkNotNull(argsPublisher)
    stmtExecutor.subscribeToStatementArgsStream(
      nativeStmt, argsPublisher,
      argsConverter = namedArgsToPgArgs
    )
  }

  def streamArgsByIdx(argsPublisher: Publisher[_ <: ImmutIndexedSeq[Any]]): Future[Unit] = traced {
    checkNotNull(argsPublisher)
    stmtExecutor.subscribeToStatementArgsStream(
      nativeStmt, argsPublisher,
      argsConverter = positionalArgsToPgArgs
    )
  }

  private def pgParametrizedStatement(pgParamValues: Vector[Argument]): PgExecutableStatement = traced {
    new PgExecutableStatement(
      executor = stmtExecutor,
      nativeSql = nativeStmt.sql,
      params = pgParamValues
    )
  }

}
