package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.scanner.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static ssw.mj.scanner.Token.Kind.*;

public class Scanner {

  // Scanner Skeleton - do not rename fields / methods !
  private static final char EOF = (char) -1;
  private static final char LF = '\n';

  /**
   * Input data to read from.
   */
  private final Reader in;

  /**
   * Lookahead character. (= next (unhandled) character in the input stream)
   */
  private char ch;

  /**
   * Current line in input stream.
   */
  private int line;

  /**
   * Current column in input stream.
   */
  private int col;

  /**
   * According errors object.
   */
  public final Errors errors;

  public Scanner(Reader r) {
    // store reader
    in = r;

    // initialize error handling support
    errors = new Errors();

    line = 1;
    col = 0;
    nextCh(); // read 1st char into ch, incr col to 1
  }

  /**
   * Adds error message to the list of errors.
   */
  public final void error(Token t, Errors.Message msg, Object... msgParams) {
    errors.error(t.line, t.col, msg, msgParams);

    // reset token content (consistent JUnit tests)
    t.numVal = 0;
    t.val = null;
  }


  // ================================================
  // TODO Exercise 1: Implement Scanner (next() + private helper methods)
  // ================================================

  // TODO Exercise 1: Keywords
  /**
   * Mapping from keyword names to appropriate token codes.
   */
  private static final Map<String, Token.Kind> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put(program.label(), program);
    keywords.put(class_.label(), class_);
    keywords.put(if_.label(), if_);
    keywords.put(else_.label(), else_);
    keywords.put(while_.label(), while_);
    keywords.put(read.label(), read);
    keywords.put(print.label(), print);
    keywords.put(return_.label(), return_);
    keywords.put(break_.label(), break_);
    keywords.put(void_.label(), void_);
    keywords.put(final_.label(), final_);
    keywords.put(new_.label(), new_);
  }

  /**
   * Returns next token. To be used by parser.
   */
  public Token next() {
    // TODO Exercise 1: implementation of next method

    while (Character.isWhitespace(ch)) {
      nextCh();
    }

    Token t = new Token(none, line, col);

    switch (ch) {
      case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
              'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z': {
        readName(t);
        break;
      }
      case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
        readNumber(t);
        break;
      }
      case '+': {
        nextCh();
        if (ch == '=') {
          t.kind = plusas;
          nextCh();
        } else if (ch == '+') {
          t.kind = pplus;
          nextCh();
        } else {
          t.kind = plus;
        }
        break;
      }
      case '-': {
        nextCh();
        if (ch == '=') {
          t.kind = minusas;
          nextCh();
        } else if (ch == '-') {
          t.kind = mminus;
          nextCh();
        } else {
          t.kind = minus;
        }
        break;
      }
      case '*': {
        nextCh();
        if (ch == '=') {
          t.kind = timesas;
          nextCh();
        } else if (ch == '*') {
          t.kind = exp;
          nextCh();
        } else {
          t.kind = times;
        }
        break;
      }
      case '/': {
        nextCh();
        if (ch == '*') {
          skipContent(t);
          t = next();
        } else if (ch == '=') {
          t.kind = slashas;
          nextCh();
        } else {
          t.kind = slash;
        }
        break;
      }
      case '%': {
        nextCh();
        if (ch == '=') {
          t.kind = remas;
          nextCh();
        } else {
          t.kind = rem;
        }
        break;
      }
      case '=': {
        nextCh();
        if (ch == '=') {
          t.kind = eql;
          nextCh();
        } else {
          t.kind = assign;
        }
        break;
      }
      case '!': {
        nextCh();
        if (ch == '=') {
          t.kind = neq;
          nextCh();
        } else {
          error(t, Errors.Message.INVALID_CHAR, '!');
        }
        break;
      }
      case '>': {
        nextCh();
        if (ch == '=') {
          t.kind = geq;
          nextCh();
        } else {
          t.kind = gtr;
        }
        break;
      }
      case '<': {
        nextCh();
        if (ch == '=') {
          t.kind = leq;
          nextCh();
        } else {
          t.kind = lss;
        }
        break;
      }
      case '(': {
        t.kind = lpar;
        nextCh();
        break;
      }
      case ')': {
        t.kind = rpar;
        nextCh();
        break;
      }
      case '[': {
        t.kind = lbrack;
        nextCh();
        break;
      }
      case ']': {
        t.kind = rbrack;
        nextCh();
        break;
      }
      case '{': {
        t.kind = lbrace;
        nextCh();
        break;
      }
      case '}': {
        t.kind = rbrace;
        nextCh();
        break;
      }
      case ';': {
        t.kind = semicolon;
        nextCh();
        break;
      }
      case ',': {
        t.kind = comma;
        nextCh();
        break;
      }
      case '.': {
        t.kind = period;
        nextCh();
        break;
      }
      case '&': {
        nextCh();
        if (ch == '&') {
          t.kind = and;
          nextCh();
        } else {
          error(t, Errors.Message.INVALID_CHAR, '&');
        }
        break;
      }
      case '|': {
        nextCh();
        if (ch == '|') {
          t.kind = or;
          nextCh();
        } else {
          error(t, Errors.Message.INVALID_CHAR, '|');
        }
        break;
      }
      case EOF: {
        t.kind = eof;
        break;
      }
      case '\'': {
        readCharConst(t);
        break;
      }
      default: {
        error(t, Errors.Message.INVALID_CHAR, ch);
        nextCh();
        break;
      }
    }

    return t;
  }

  // TODO Exercise 1: private helper methods used by next(), as discussed in the exercise
  private void readName(Token t) {
    // Read in the ident char by char
    StringBuilder sb = new StringBuilder();
    do {
      sb.append(ch);
      nextCh();
    } while((isLetter(ch) || isDigit(ch) || ch == '_') && (!charReadAfterLF || sb.isEmpty())); // Added condition, due to finding a bug not handled by Scanner-UnitTests
    t.val = sb.toString();
    // Checking if it is a keyword
    t.kind = keywords.getOrDefault(t.val, ident);
  }

  private void readNumber(Token t) {
    StringBuilder sb = new StringBuilder();
    int num = 0, chDigit;
    boolean toBig = false;
    do {    // Reading the digits
      sb.append(ch);        // Concatenating to the string
      chDigit = ch;

      try {
        int number = Integer.parseInt(sb.toString());
      } catch (NumberFormatException e) {
        toBig = true;
      }

      if (!toBig) {
        num = num * 10 + (chDigit - 48);
      }
      nextCh();
    } while (isDigit(ch));

    if (!toBig) {    // Check is overflow happened or not
      t.kind = number;
      t.val = sb.toString();
      t.numVal = num;
    } else {
      t.val = sb.toString();
      t.kind = number;
      error(t, Errors.Message.BIG_NUM, t.val);
    }
  }

  private void skipContent(Token t) {
    nextCh();

    // Handling unclosed comment
    if (ch == EOF) {
      error(t, Errors.Message.EOF_IN_COMMENT);
      return;
    }

    // Moving to the first 2 characters of the comment
    char chPrev = ch;
    nextCh();

    int commentDepth = 1; // Track nested comment depth

    do {
      switch (ch) {
        case '*': {
          if (chPrev == '/') {
            commentDepth++;
            chPrev = ch;
            nextCh();
          }
          break;
        }
        case '/': {
          if (chPrev == '*') {
            commentDepth--;
            if (commentDepth != 0) {
              chPrev = ch;
              nextCh();
            }
          }
          break;
        }
      }

      if (ch == EOF) {   // Handles EOF in a comment
        error(t, Errors.Message.EOF_IN_COMMENT);
        break;
      }

      // Moves one to the right
      chPrev = ch;
      nextCh();
    } while (commentDepth > 0);
  }

  // Helper for handling LF or CR LF line endings in constChars and in readName
  private boolean charReadAfterLF = false;
  private static final char CR = '\r';

  private void readCharConst(Token t) {
    nextCh();
    if (ch == EOF) {   // Check for EOF in the char and ending the function after
      t.kind = charConst;
      error(t, Errors.Message.EOF_IN_CHAR);
      return;
    } else if (charReadAfterLF) {     // Invalid LF or CR LF found in charConst
      t.kind = charConst;
      error(t, Errors.Message.ILLEGAL_LINE_END);
      return;
    } else if (ch == '\'') {   // Check for empty char and ending the function after
      t.kind = charConst;
      error(t, Errors.Message.EMPTY_CHARCONST);
      nextCh();
      return;
    } else if (ch == '\\') {    // Checking for Escape-Sequence
      nextCh();
      switch (ch) {
        case 'r': {
          t.kind = charConst;
          t.numVal = CR;
          nextCh();
          break;
        }
        case 'n': {
          t.kind = charConst;
          t.numVal = '\n';
          nextCh();
          break;
        }
        case '\\', '\'': {
          t.kind = charConst;
          t.numVal = ch;
          nextCh();
          break;
        }
        default: {    // Invalid Sequence
          t.kind = charConst;
          error(t, Errors.Message.UNDEFINED_ESCAPE, ch);
          nextCh();
          break;
        }
      }
    } else {    // Otherwise saving the char
      t.numVal = ch;
      t.kind = charConst;
      nextCh();
    }

    if (ch != '\'') {   // Check for char ending quote
      error(t, Errors.Message.MISSING_QUOTE);
    } else {
      nextCh();
    }
  }

  /**
   * Reads next character from input stream into ch. Keeps pos, line and col
   * in sync with reading position.
   */

  private void nextCh() {
    // TODO Exercise 1
    try {
      ch = (char) in.read();
      if (ch == LF) {     // Check for line end
        line++;
        col = 0;
        charReadAfterLF = true;    // Set the helper for readCharConst
      } else {
        charReadAfterLF = (ch == CR);    // Set the helper for readCharConst
        col++;
      }
    } catch (IOException e) {
      ch = EOF;
      col++;
    }
  }

  // ...

  private boolean isLetter(char c) {
    return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
  }

  private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  // ================================================
  // ================================================
}
