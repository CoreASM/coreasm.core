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

final class SkipTimesParser extends Parser<Void> {
  private final Parser<?> parser;
  private final int min;
  private final int max;

  SkipTimesParser(Parser<?> parser, int min, int max) {
    this.parser = parser;
    this.min = min;
    this.max = max;
  }

  @Override boolean apply(ParseContext ctxt) {
    if (!ParserInternals.repeat(parser, min, ctxt)) return false;
    if (ParserInternals.repeatAtMost(parser, max - min, ctxt)) {
      ctxt.result = null;
      return true;
    }
    return false;
  }
  
  @Override public String toString() {
    return "skipTimes";
  }
}