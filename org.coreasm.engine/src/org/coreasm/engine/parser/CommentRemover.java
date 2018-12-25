package org.coreasm.engine.parser;

public class CommentRemover {

  private boolean inComment = false;
  private boolean inString = false;
  private StringBuilder buffer;

  public String append(String line) throws ParserException {
    boolean maybeComment = false;
    boolean maybeCommentEnd = false;
    boolean escape = false;
    StringBuilder result = inString ? buffer : new StringBuilder();
    for (char c : line.toCharArray()) {
      if (inComment) {
        if (maybeCommentEnd) {
          if (c == '/')
            inComment = false;
          else
            result.append('*').append(c);
          maybeCommentEnd = false;
        } else if (c == '*')
          maybeCommentEnd = true;
      } else {
        if (!inString) {
          if (c == '"') {
            inString = true;
            result.append(c);
          } else if (maybeComment) {
            if (c == '*')
              inComment = true;
            else if (c == '/')
              return result.toString();
            else
              result.append('/').append(c);
            maybeComment = false;
          } else {
            if (c == '/')
              maybeComment = true;
            else
              result.append(c);
          }
        } else {
          if (c == '\\')
            escape = !escape;
          else {
            if (!escape && c == '"')
              inString = false;
            escape = false;
          }
          result.append(c);
        }
      }
    }
    if (inString) {
      //throw new ParserException("Unbalanced \" in string literal.");
      buffer = result.append("\\n");
      return "";
    }
    return result.toString();
  }

}
