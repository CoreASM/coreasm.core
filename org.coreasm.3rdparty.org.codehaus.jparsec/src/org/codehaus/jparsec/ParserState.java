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


/**
 * Represents {@link ParseContext} for token level parsing.
 * 
 * @author Ben Yu
 */
final class ParserState extends ParseContext {
  
  private final Token[] input;
  
  // in case a terminating eof token is not explicitly created, the implicit one is used.
  private final int endIndex;

  @Override boolean isEof() {
    return at >= input.length;
  }
  
  @Override int toIndex(int pos) {
    if (pos >= input.length) return endIndex;
    return input[pos].index();
  }

  @Override Token getToken() {
    return input[at];
  }
  
  ParserState(String module, CharSequence source, Token[] input, int at,
      SourceLocator locator, int endIndex, Object result) {
    super(source, result, at, module, locator);
    this.input = input;
    this.endIndex = endIndex;
  }
  
  @Override char peekChar() {
    throw new IllegalStateException("Cannot scan characters on tokens.");
  }
  
  @Override CharSequence characters() {
    throw new IllegalStateException("Cannot scan characters on tokens.");
  }

  @Override String getInputName(int pos) {
    if (pos >= input.length) return EOF;
    return input[pos].toString();
  }
}
