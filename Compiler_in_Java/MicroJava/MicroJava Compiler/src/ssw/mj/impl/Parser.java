package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.codegen.Label;
import ssw.mj.codegen.Operand;
import ssw.mj.scanner.Token;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.scanner.Token.Kind.*;

public final class Parser {

  /**
   * Maximum number of global variables per program
   */
  private static final int MAX_GLOBALS = 32767;

  /**
   * Maximum number of fields per class
   */
  private static final int MAX_FIELDS = 32767;

  /**
   * Maximum number of local variables per method
   */
  private static final int MAX_LOCALS = 127;

  /**
   * Last recognized token;
   */
  private Token t;

  /**
   * Lookahead token (not recognized).)
   */
  private Token la;

  /**
   * Shortcut to kind attribute of lookahead token (la).
   */
  private Token.Kind sym;

  /**
   * According scanner
   */
  public final Scanner scanner;

  /**
   * According code buffer
   */
  public final Code code;

  /**
   * According symbol table
   */
  public final Tab tab;
  public Parser(Scanner scanner) {
    this.scanner = scanner;
    tab = new Tab(this);
    code = new Code(this);
    // Pseudo token to avoid crash when 1st symbol has scanner error.
    la = new Token(none, 1, 1);
  }


  /**
   * Reads ahead one symbol.
   */
  private void scan() {
    t = la;
    la = scanner.next();
    sym = la.kind;
    // Increment error distance, with every new token
    errDist++;
  }

  /**
   * Verifies symbol and reads ahead.
   */
  private void check(Token.Kind expected) {
    if (sym == expected) {
      scan();
    } else {
      error(TOKEN_EXPECTED, expected);
    }
  }

  /**
   * Adds error message to the list of errors.
   */
  public void error(Message msg, Object... msgParams) {
    // TODO Exercise 3: Replace panic mode with error recovery (i.e., keep track of error distance)
    // TODO Exercise 3: Hint: Replacing panic mode also affects scan() method
    // Check for being at least 3 tokens from the last error
    if (errDist >= 3) {
      scanner.errors.error(la.line, la.col, msg, msgParams);
    }
    // After sending error message the distance is 0
    errDist = 0;
  }

  /**
   * Starts the analysis.
   */
  public void parse() {
    // TODO Exercise 2: Implementation of parser
    // Setting up the "Universe" before starting the parsing
    tab.openScope();
    tab.init();
    // Start of parsing
    scan();
    Program();
    check(eof);
  }

  // ===============================================
  // TODO Exercise 2: Implementation of parser
  // TODO Exercise 3: Error recovery methods
  // TODO Exercise 4: Symbol table handling
  // TODO Exercise 5-6: Code generation
  // ===============================================

  // TODO Exercise 3: Error distance
  private int errDist = 3;

  // TODO Exercise 2 + Exercise 3: Sets to handle certain first, follow, and recover sets
  private static final EnumSet<Token.Kind> firstStatement = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> firstExpr = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> firstAssignop = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> firstRelop = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> recoverDeclSet = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> recoverMethodDeclSet = EnumSet.noneOf(Token.Kind.class);
  private static final EnumSet<Token.Kind> recoverStatSet = EnumSet.noneOf(Token.Kind.class);

  static {
    // First of Statement
    firstStatement.add(ident);
    firstStatement.add(if_);
    firstStatement.add(while_);
    firstStatement.add(break_);
    firstStatement.add(return_);
    firstStatement.add(read);
    firstStatement.add(print);
    firstStatement.add(lbrace);
    firstStatement.add(semicolon);

    // First of Expression
    firstExpr.add(minus);
    firstExpr.add(ident);
    firstExpr.add(number);
    firstExpr.add(charConst);
    firstExpr.add(new_);
    firstExpr.add(lpar);


    // First of Assignop
    firstAssignop.add(assign);
    firstAssignop.add(plusas);
    firstAssignop.add(minusas);
    firstAssignop.add(timesas);
    firstAssignop.add(slashas);
    firstAssignop.add(remas);

    // First of Relop
    firstRelop.add(eql);
    firstRelop.add(neq);
    firstRelop.add(gtr);
    firstRelop.add(geq);
    firstRelop.add(lss);
    firstRelop.add(leq);

    // Sync. tokens for recoverDecl() function
    recoverDeclSet.add(final_);
    recoverDeclSet.add(ident);
    recoverDeclSet.add(class_);
    recoverDeclSet.add(lbrace);
    recoverDeclSet.add(eof);

    // Sync. tokens for recoverMethodDecl() function
    recoverMethodDeclSet.add(ident);
    recoverMethodDeclSet.add(void_);
    recoverMethodDeclSet.add(rbrace);
    recoverMethodDeclSet.add(eof);

    // Sync. tokens for recoverStat() function
    recoverStatSet.add(if_);
    recoverStatSet.add(while_);
    recoverStatSet.add(break_);
    recoverStatSet.add(return_);
    recoverStatSet.add(read);
    recoverStatSet.add(print);
    recoverStatSet.add(semicolon);
    recoverStatSet.add(rbrace);
    recoverStatSet.add(eof);
  }

  // ---------------------------------

  // TODO Exercise 2: One top-down parsing method per production

  private void Program() {
    // Program = "program" ident { ConstDecl | VarDecl | ClassDecl }
    // "{" {MethodDecl} "}".
    check(program);
    check(ident);

    // SymbolTable: Insert "Program" into "Universe" and open a new scope
    Obj prog = tab.insert(Obj.Kind.Prog, t.val, Tab.noType);
    tab.openScope();

    while (sym != lbrace && sym != eof) {
      switch (sym) {
        case final_ -> ConstDecl();
        case class_ -> ClassDecl();
        case ident -> VarDecl();
        default -> recoverDecl();
      }
    }

    // SymbolTable: Checking for global var limit
    if (tab.curScope.nVars() > MAX_GLOBALS) {
      error(TOO_MANY_GLOBALS);
    }

    check(lbrace);
    while (sym != rbrace && sym != eof) {
      if (sym == ident || sym == void_) {
        MethodDecl();
      } else {
        recoverMethodDecl();
      }
    }

    // SymbolTable: Saving locals and closing the current scope
    prog.locals = tab.curScope.locals();
    code.dataSize = tab.curScope.nVars();
    tab.closeScope();

    check(rbrace);

    // CodeGen: Check if main was found
    if (code.mainpc == -1) {
      error(METH_NOT_FOUND, "main");
    }
  }

  private void ConstDecl() {
    // ConstDecl = "final" Type ident "=" ( number | charConst ) ";".
    check(final_);
    Struct type = Type();
    check(ident);
    Obj newConst = tab.insert(Obj.Kind.Con, t.val, type);
    check(assign);

    // Checking constant type and assign value compatibility
    if ((sym == number && type == Tab.intType) || (sym == charConst && type == Tab.charType)) {
      newConst.val = la.numVal;
      scan();
    } else if (sym == number || sym == charConst) { // Correct constant type, but not matching assign value
      error(CONST_TYPE);
    } else {  // Incorrect constant type
      error(CONST_DECL);
    }
    check(semicolon);
  }

  private void VarDecl() {
    // VarDecl = Type ident { "," ident } ";".
    Struct type = Type();
    check(ident);
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma) {
      check(comma);
      check(ident);
      tab.insert(Obj.Kind.Var, t.val, type);
    }
    check(semicolon);
  }

  private void ClassDecl() {
    // ClassDecl = "class" ident "{" { VarDecl } "}".
    check(class_);
    check(ident);

    // SymbolTable: Creating new class and opening new scope for it
    Obj newClass = tab.insert(Obj.Kind.Type, t.val, new Struct(Struct.Kind.Class));
    check(lbrace);
    tab.openScope();
    
    while (sym == ident) {
      VarDecl();
    }

    // SymbolTable: Checking for maximum field number and saving them
    if (tab.curScope.nVars() > MAX_FIELDS) {
      error(TOO_MANY_FIELDS);
    }
    newClass.type.fields = tab.curScope.locals();
    tab.closeScope();

    check(rbrace);
  }

  private void MethodDecl() {
    // MethodDecl = ( Type | "void" ) ident "(" [ FormPars ] ")"
    //{ VarDecl } Block.

    // SymbolTable: Predefining struct for saving type
    Struct type = Tab.noType;

    if (sym == ident) {
      type = Type();
      if (type.isRefType()) {
        error(INVALID_METH_RETURN_TYPE);
      }
    } else if (sym == void_) {
      scan();
    } else {
      error(INVALID_METH_DECL);
    }
    check(ident);

    // SymbolTable: Saving name of method and inserting into current scope
    String methodName = t.val;
    Obj meth = tab.insert(Obj.Kind.Meth, methodName, type);

    // CodeGen: Saving the address and specifically checking for main method
    meth.adr = code.pc;
    if (methodName.equals("main")) {
      code.mainpc = code.pc;
    }

    check(lpar);
    tab.openScope();
    if (sym == ident) {
      FormPars();
    }
    check(rpar);

    // SymbolTable: Checking main format and saving parameters after
    if (tab.curScope.nVars() > 0 && methodName.equals("main")) {
      error(MAIN_WITH_PARAMS);
    }
    if (type != Tab.noType && methodName.equals("main")) {
      error(MAIN_NOT_VOID);
    }
    meth.nPars = tab.curScope.nVars();

    while (sym == ident) {
      VarDecl();
    }

    // SymbolTable: Checking for maximum local vars and saving them
    if (tab.curScope.nVars() > MAX_LOCALS) {
      error(TOO_MANY_LOCALS);
    }
    meth.locals = tab.curScope.locals();

    // CodeGen: Putting OpCode enter with number of params and variables
    code.put(Code.OpCode.enter);
    code.put(meth.nPars);
    code.put(tab.curScope.nVars());

    Block(type, null);

    // CodeGen: Checking if method was main, otherwise using trap, due to missing return
    if (meth.type == Tab.noType) {
      code.put(Code.OpCode.exit);
      code.put(Code.OpCode.return_);
    } else {
      code.put(Code.OpCode.trap);
      code.put(1);
    }

    tab.closeScope();
  }

  private void FormPars() {
    // FormPars = Type ident { "," Type ident }.
    Struct type = Type();
    check(ident);
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma) {
      check(comma);
      type = Type();
      check(ident);
      tab.insert(Obj.Kind.Var, t.val, type);
    }
  }

  private Struct Type() {
    // Type = ident [ "[" "]" ].
    check(ident);

    // SymbolTable: Checking if it is a valid type
    Obj o = tab.find(t.val);
    if (o.kind != Obj.Kind.Type) {
      error(NO_TYPE);
    }
    Struct type = o.type;

    if (sym == lbrack) {
      scan();
      check(rbrack);
      type = new Struct(type);
    }
    return type;
  }

  private void Block(Struct methodType, Label breakLab) {
    // Block = "{" { Statement } "}".
    check(lbrace);
    // Checking in the EnumSet for start of a Statement
    while (sym != rbrace && sym != eof) {
      if (firstStatement.contains(sym)) {
        Statement(methodType, breakLab);
      } else {
        recoverStat();
      }
    }
    check(rbrace);
  }

  private void Statement(Struct methodType, Label breakLab) {
    // Handling every Statement type with a different switch case
    switch (sym) {
      // Designator ( Assignop Expr | ActPars | "++" | "--" ) ";"
      case ident: {
        Operand x = Designator();
          if (firstAssignop.contains(sym)) {
            // CodeGen: Cannot assign value to method or constant
            if (x.kind == Operand.Kind.Meth || x.kind == Operand.Kind.Con) {
              error(CANNOT_ASSIGN_TO, x.kind);
            } else {
              Assignop();
              String assignOp = t.kind.label();
              // CodeGen: Prepping for compound assignment with dup and dup2
              if (!assignOp.equals("=")) {
                code.compoundAssignmentPrepare(x);
              }
              Operand y = Expr();
              if (assignOp.equals("=")) {
                if (!y.type.assignableTo(x.type)) {
                  System.out.println(y.type);
                  error(INCOMP_TYPES);
                } else {
                  code.assign(x, y);
                }
              } else {
                // CodeGen: If any of the two is not int compound assign cannot be performed
                if (!x.type.isEqual(Tab.intType) || !y.type.isEqual(Tab.intType)) {
                  error(NO_INT_OPERAND);
                } else {
                  code.load(y);
                  switch (assignOp) {
                    case "+=" -> code.put(Code.OpCode.add);
                    case "-=" -> code.put(Code.OpCode.sub);
                    case "/=" -> code.put(Code.OpCode.div);
                    case "%=" -> code.put(Code.OpCode.rem);
                    case "*=" -> code.put(Code.OpCode.mul);
                  }
                  code.assign(x, y);
                }
              }
            }
          } else if (sym == lpar) {
            ActPars(x);
            code.methodCall(x);
            if (x.type != Tab.noType) {
              code.put(Code.OpCode.pop);
            }
          } else if (sym == pplus || sym == mminus) {
            int changeValue = 1;
            if (sym == mminus) {
              changeValue = -1;
            }
            if (x.kind == Operand.Kind.Meth || x.kind == Operand.Kind.Con) {
              error(CANNOT_ASSIGN_TO, x.kind);
            } else if (!x.type.isEqual(Tab.intType)) {
              error(NO_INT_OPERAND);
            } else if (x.kind == Operand.Kind.Local){
              // CodeGen: Inc is only usable if x is local
              code.inc(x, changeValue);
            } else {
              code.compoundAssignmentPrepare(x);
              Operand y = new Operand(changeValue);
              code.load(y);
              code.put(Code.OpCode.add);
              code.assign(x, y);
            }
            scan();
          } else {
            error(DESIGN_FOLLOW);
          }
        check(semicolon);
        break;
      }
      // "if" "(" Condition ")" Statement [ "else" Statement ]
      case if_: {
        scan();
        check(lpar);
        // CodeGen: Performing false jump on the condition
        Operand x = Condition();
        code.fJump(x.op, x.fLabel);
        x.tLabel.here();

        check(rpar);
        Statement(methodType, breakLab);

        // CodeGen: Defining end label to skip else branches
        Label end = new Label(code);
        if (sym == else_) {
          code.jump(end);
          scan();
          x.fLabel.here();
          Statement(methodType, breakLab);
        } else {
          x.fLabel.here();
        }
        end.here();
        break;
      }
      // "while" "(" Condition ")" Statement
      case while_: {
        scan();
        check(lpar);
        // CodeGen: Defining top and breakLab(helper for breaks in the loop)
        Label top = new Label(code);
        breakLab = new Label(code);
        top.here();
        // CodeGen: Performing false jump on the condition
        Operand x = Condition();
        code.fJump(x.op, x.fLabel);
        x.tLabel.here();

        check(rpar);
        Statement(methodType, breakLab);

        code.jump(top);
        x.fLabel.here();
        breakLab.here();
        break;
      }
      // "break" ";"
      case break_: {
        scan();
        // CodeGen: Missing breakLab indicates break outside of loop
        if (breakLab == null) {
          error(NO_LOOP);
        } else {
          code.jump(breakLab);
        }
        check(semicolon);
        break;
      }
      // "return" [ Expr ] ";"
      case return_: {
        scan();
        // Checking for first of Expression
        if (firstExpr.contains(sym)) {
          // CodeGen: Void function shouldn't have a return
          if (methodType.isEqual(Tab.noType)) {
            error(RETURN_VOID);
          }
          Operand x = Expr();
          if (!x.type.isEqual(methodType)) {
            error(NON_MATCHING_RETURN_TYPE);
          }
          code.load(x);
          code.put(Code.OpCode.exit);
          code.put(Code.OpCode.return_);
        } else if (!methodType.isEqual(Tab.noType)){
          error(RETURN_NO_VAL);
        }
        check(semicolon);
        break;
      }
      // "read" "(" Designator ")" ";"
      case read: {
        scan();
        check(lpar);
        Operand x = Designator();
        // CodeGen: Performing read and saving it
        if (x.type == Tab.intType) {
          code.put(Code.OpCode.read);
        } else if (x.type == Tab.charType){
          code.put(Code.OpCode.bread);
        } else {
          error(READ_VALUE);
        }
        switch (x.kind) {
          case Local -> {
            switch (x.adr) {
              case 0 -> code.put(Code.OpCode.store_0);
              case 1 -> code.put(Code.OpCode.store_1);
              case 2 -> code.put(Code.OpCode.store_2);
              case 3 -> code.put(Code.OpCode.store_3);
              default -> {
                code.put(Code.OpCode.store);
                code.put(x.adr);
              }
            }
          }
          case Static -> {
            code.put(Code.OpCode.putstatic);
            code.put2(x.adr);
          }
          case Fld -> {
            code.put(Code.OpCode.putfield);
            code.put2(x.adr);
          }
          case Elem -> {
            if (x.type == Tab.charType) {
              code.put(Code.OpCode.bastore);
            } else {
              code.put(Code.OpCode.astore);
            }
          }
          default -> error(NO_VAL);
        }
        check(rpar);
        check(semicolon);
        break;
      }
      // "print" "(" Expr [ "," number ] ")" ";"
      case print: {
        scan();
        check(lpar);
        Operand x = Expr();
        code.load(x);
        // CodeGen: Loading needed number of spaces
        if (sym == comma) {
          scan();
          check(number);
          code.loadConst(t.numVal);
        } else {
          // No spaces after
          code.loadConst(0);
        }

        if (x.type == Tab.intType) {
          code.put(Code.OpCode.print);
        } else if (x.type == Tab.charType) {
          code.put(Code.OpCode.bprint);
        } else {
          error(PRINT_VALUE);
        }
        check(rpar);
        check(semicolon);
        break;
      }
      // lbrace is first of Block
      case lbrace: {
        Block(methodType, breakLab);
        break;
      }
      // Empty Statement
      case semicolon: {
        scan();
        break;
      }
      default: {
        error(INVALID_STAT);
        break;
      }
    }
  }

  private void Assignop() {
    // Assignop = "=" | "+=" | "-=" | "*=" | "/=" | "%=".
    if (firstAssignop.contains(sym)) {
      scan();
    } else {
      error(ASSIGN_OP);
    }
  }

  private void ActPars(Operand m) {
    // ActPars = "(" [ Expr { "," Expr } ] ")".
    check(lpar);

    if (m.kind != Operand.Kind.Meth) {
      error(NO_METH);
      m.obj = Tab.noObj;
    }

    // Active and formal parameters
    int aPars = 0;
    int fPars = m.obj.nPars;
    // Creating an iterator for the locals
    Map<String, Obj> objList = m.obj.locals;
    Iterator<Map.Entry<String, Obj>> iterator = objList.entrySet().iterator();
    Obj fp = null; // Initialize fp as null
    if (iterator.hasNext()) {
      Map.Entry<String, Obj> firstEntry = iterator.next();
      fp = firstEntry.getValue();
    }

    if (firstExpr.contains(sym)) {
      Operand x = Expr();
      code.load(x);
      // Increment active parameter count and checking for requested type
      aPars++;
      if (fp != null) {
        if (!x.type.assignableTo(fp.type)) {
          error(PARAM_TYPE);
        }
      }
      while (sym == comma) {
        scan();
        x = Expr();
        code.load(x);
        aPars++;
        // If there still are formal params we move the iterator
        if (iterator.hasNext()) {
          Map.Entry<String, Obj> firstEntry = iterator.next();
          fp = firstEntry.getValue();
        } else {
          fp = null;
        }
        if (fp != null) {
          if (!x.type.assignableTo(fp.type)) {
            error(PARAM_TYPE);
          }
        }
      }
    }
    // Comparing active and formal parameter counts
    // only if they are equal can we go forward
    if (aPars > fPars) {
      error(MORE_ACTUAL_PARAMS);
    } else if (aPars < fPars) {
      error(LESS_ACTUAL_PARAMS);
    }
    check(rpar);
  }

  private Operand Condition() {
    // Condition = CondTerm { "||" CondTerm }.
    Operand x = CondTerm();
    while (sym == or) {
      // CodeGen: Performing true jump after or
      code.tJump(x.op, x.tLabel);
      scan();
      x.fLabel.here();
      Operand y = CondTerm();
      x.fLabel = y.fLabel;
      x.op = y.op;
    }
    return x;
  }

  private Operand CondTerm() {
    // CondTerm = CondFact { "&&" CondFact }.
    Operand x = CondFact();
    while (sym == and) {
      // CodeGen: Performing false jump after and
      code.fJump(x.op, x.fLabel);
      scan();
      Operand y = CondFact();
      x.op = y.op;
    }
    return x;
  }

  private Operand CondFact() {
    // CondFact = Expr Relop Expr.
    Operand x = Expr();
    Code.CompOp op = Relop();
    Operand compare = new Operand(op, code);
    Operand y = Expr();

    code.load(x);
    code.load(y);
    if (!x.type.compatibleWith(y.type)) {
      error(INCOMP_TYPES);
    }
    // CodeGen: On reference types only equality checks can be performed
    if (x.type.isRefType() && op != Code.CompOp.eq && op != Code.CompOp.ne) {
      error(EQ_CHECK);
    }

    return compare;
  }

  private Code.CompOp Relop() {
    // Relop = "==" | "!=" | ">" | ">=" | "<" | "<=".
    if (firstRelop.contains(sym)) {
      scan();
      switch (t.kind) {
        case eql -> { return Code.CompOp.eq; }
        case neq -> { return Code.CompOp.ne; }
        case gtr -> { return Code.CompOp.gt; }
        case geq -> { return Code.CompOp.ge; }
        case lss -> { return Code.CompOp.lt; }
        case leq -> { return Code.CompOp.le; }
      }
    } else {
      error(REL_OP);
    }
    // CodeGen: Returning equal operator in case of error,
    // so the recover methods can read through it
    // and not be stopped by a null value
    return Code.CompOp.eq;
  }

  private Operand Expr() {
    // Expr = [ "–" ] Term { Addop Term }.
    boolean negation = false;
    if (sym == minus) {
      scan();
      negation = true;
    }

    Operand x = Term();

    if (negation && !x.type.isEqual(Tab.intType)) {
      error(NO_INT_OPERAND);
    } else if (negation) {
      if (x.kind != Operand.Kind.Con) {
        code.load(x);
        code.put(Code.OpCode.neg);
      } else {
        x.val = -x.val;
      }
    }

    while (sym == plus || sym == minus) {
      Token.Kind o = sym;
      code.load(x);
      Addop();
      Operand y = Term();
      code.load(y);

      if (!x.type.isEqual(Tab.intType) || !y.type.isEqual(Tab.intType)) {
        error(NO_INT_OPERAND);
      } else {
        if (o == minus) {
          code.put(Code.OpCode.sub);
        } else {
          code.put(Code.OpCode.add);
        }
      }
    }
    return x;
  }

  private Operand Term() {
    // Term = Factor { Mulop Factor | "**" number }.
    Operand x = Factor();
    while (sym == times || sym == slash || sym == rem || sym == exp) {
      switch (sym) {
        case times, slash, rem -> {
          Token.Kind o = sym;
          Mulop();
          code.load(x);
          Operand y = Factor();
          code.load(y);
          if (!x.type.isEqual(Tab.intType) || !y.type.isEqual(Tab.intType)) {
            error(NO_INT_OPERAND);
          } else {
            switch (o) {
              case times -> code.put(Code.OpCode.mul);
              case slash -> code.put(Code.OpCode.div);
              case rem -> code.put(Code.OpCode.rem);
            }
          }
        }
        case exp -> {
          scan();
          check(number);
          if (!x.type.isEqual(Tab.intType)) {
            error(NO_INT_OPERAND);
          } else {
            Label baseTwo = new Label(code);
            Label endExp = new Label(code);
            int y = t.numVal;
            code.load(x);
            if (y == 0) {
              code.put(Code.OpCode.pop);
              code.loadConst(1);
            } else if (y >= 2)  {
              // CodeGen: Comparing the base with 2 with a true jump
              code.put(Code.OpCode.dup);
              code.loadConst(2);
              code.tJump(Code.CompOp.eq, baseTwo);

              // CodeGen: Block for base neq to 2
              for (int i = 1; i < y; i++) {
                code.put(Code.OpCode.dup);
              }
              for (int i = 1; i < y; i++) {
                code.put(Code.OpCode.mul);
              }
              code.jump(endExp);

              // CodeGen: Block for base 2
              baseTwo.here();
              code.loadConst(y - 1);
              code.put(Code.OpCode.shl);

              endExp.here();
            }
          }
        }
      }
    }
    return x;
  }

  private Operand Factor() {
    Operand x = new Operand(new Struct(Struct.Kind.None));
    switch (sym) {
      // Designator [ ActPars ]
      case ident -> {
        x = Designator();
        if (sym == lpar) {
          if (x.type.isEqual(Tab.noType)) {
            error(INVALID_CALL);
          } else {
            if (x.kind == Operand.Kind.Meth) {
              x.obj = tab.find(t.val);
              ActPars(x);
              code.methodCall(x);
              x.kind = Operand.Kind.Stack;
            } else {
              error(METH_NOT_FOUND, t.val);
            }
          }
        }
      }
      // number | charConst
      case number, charConst -> {
        scan();
        x = new Operand(t.numVal);
        if (t.kind == charConst) {
          x.type = Tab.charType;
        }
      }
      // "new" ident [ "[" Expr "]" ]
      case new_ -> {
        scan();
        check(ident);
        Obj o = tab.find(t.val);
        if (o.kind != Obj.Kind.Type) {
          error(NO_TYPE);
        } else {
          Struct type = o.type;
          if (sym == lbrack) {
            scan();
            x = Expr();
            if (x.type != Tab.intType) {
              error(ARRAY_SIZE);
            }
            code.load(x);
            code.put(Code.OpCode.newarray);
            if (type == Tab.charType) {
              code.put(0);
            } else {
              code.put(1);
            }
            x = new Operand(new Struct(type));
            check(rbrack);
          } else {
            if (type.kind != Struct.Kind.Class) {
              error(NO_CLASS_TYPE);
            } else {
              code.put(Code.OpCode.new_);
              code.put2(type.nrFields());
              x = new Operand(type);
            }
          }
        }
      }
      // "(" Expr ")"
      case lpar -> {
        scan();
        x = Expr();
        check(rpar);
      }
      default -> error(INVALID_FACT);
    }
    return x;
  }

  private Operand Designator() {
    // Designator = ident { "." ident | "[" Expr "]" }.
    check(ident);
    Operand x = new Operand(tab.find(t.val), this);
    while (sym == period || sym == lbrack) {
      if (sym == period) {
        // CodeGen: Loading the field of the object
        if(x.type.kind != Struct.Kind.Class) {
          error(NO_CLASS);
        }
        scan();
        code.load(x);
        check(ident);

        Obj obj = tab.findField(t.val, x.type);
        x.kind = Operand.Kind.Fld;
        x.type = obj.type;
        x.adr = obj.adr;
      } else {
        // CodeGen: Loading the value of the array at given index
        scan();
        code.load(x);
        Operand y = Expr();
        if(x.type.kind != Struct.Kind.Arr) {
          error(NO_ARRAY);
        } else {
          if (y.type == Tab.intType) {
            code.load(y);
            x.kind = Operand.Kind.Elem;
            x.type = x.type.elemType;
            check(rbrack);
          } else {
            error(ARRAY_INDEX);
          }
        }
      }
    }
    return x;
  }

  private void Addop() {
    // Addop = "+" | "–".
    if (sym == plus || sym == minus) {
      scan();
    } else {
      error(ADD_OP);
    }
  }

  private void Mulop() {
    // Mulop = "*" | "/" | "%".
    if (sym == times || sym == slash || sym == rem) {
      scan();
    } else {
      error(MUL_OP);
    }
  }

  // ------------------------------------

  // TODO Exercise 3: Error recovery methods: recoverDecl, recoverMethodDecl and recoverStat
  private void recoverDecl() {
    error(INVALID_DECL);
    do {
      scan();
    } while (!recoverDeclSet.contains(sym));
  }
  private void recoverMethodDecl() {
    error(INVALID_METH_DECL);
    do {
      scan();
    } while (!recoverMethodDeclSet.contains(sym));
  }

  private void recoverStat() {
    error(INVALID_STAT);
    do {
      scan();
    } while (!recoverStatSet.contains(sym));
  }

  // ====================================
  // ====================================
}
