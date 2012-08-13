/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.codehaus.jparsec;

import java.util.List;

final class RepeatAtLeastParser<T> extends Parser<List<T>> {
  private final Parser<? extends T> parser;
  private final int min;
  private final ListFactory<T> listFactory;

  RepeatAtLeastParser(Parser<? extends T> parser, int min) {
    this(parser, min, ListFactories.<T>arrayListFactory());
  }

  RepeatAtLeastParser(Parser<? extends T> parser, int min, ListFactory<T> listFactory) {
    this.parser = parser;
    this.min = min;
    this.listFactory = listFactory;
  }

  @Override boolean apply(ParseContext ctxt) {
    List<T> result = listFactory.newList();
    if (!ParserInternals.repeat(parser, min, result, ctxt))
      return false;
    if (ParserInternals.many(parser, result, ctxt)) {
      ctxt.result = result;
      return true;
    }
    return false;
  }
  
  @Override public String toString() {
    return "atLeast";
  }
}