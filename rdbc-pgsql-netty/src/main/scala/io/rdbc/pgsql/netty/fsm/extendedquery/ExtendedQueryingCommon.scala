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

package io.rdbc.pgsql.netty.fsm.extendedquery

import io.rdbc.pgsql.core.messages.backend.{NoticeMessage, PgBackendMessage}
import io.rdbc.pgsql.netty.fsm.State.Outcome

abstract protected class ExtendedQueryingCommon extends ExtendedQuerying {

  private var _warnings = Vector.empty[NoticeMessage]

  protected def warnings = _warnings

  protected def handleCommon: PartialFunction[PgBackendMessage, Outcome] = {
    case notice: NoticeMessage if notice.isWarning =>
      _warnings = _warnings :+ notice
      stay
  }
}
