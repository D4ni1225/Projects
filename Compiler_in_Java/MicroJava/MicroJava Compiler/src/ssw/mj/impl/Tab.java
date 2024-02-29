package ssw.mj.impl;

import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;

import static ssw.mj.Errors.Message.*;

public final class Tab {

  // Universe
  public static final Struct noType = new Struct(Struct.Kind.None);
  public static final Struct intType = new Struct(Struct.Kind.Int);
  public static final Struct charType = new Struct(Struct.Kind.Char);
  public static final Struct nullType = new Struct(Struct.Kind.Class);

  // TODO Exercise 4: Assign universe objects in constructor
  public static Obj noObj = null, chrObj = null, ordObj = null, lenObj = null;

  /**
   * Only used for reporting errors.
   */
  private final Parser parser;
  /**
   * The current top scope.
   */
  public Scope curScope = null;
  // First scope opening (universe) will increase this to -1
  /**
   * Nesting level of current scope.
   */
  private int curLevel = -2;

  public Tab(Parser p) {
    parser = p;

    // TODO Exercise 4: set up "universe" (= predefined names)
    // opening scope (curLevel goes to -1, which is the universe level)
    openScope();
    // TODO Exercise 4 ...
    init();
    closeScope();
  }

  // ===============================================
  // TODO Exercise 4: implementation of symbol table
  // ===============================================

  public void openScope() {
    // TODO Exercise 4
    curScope = new Scope(curScope);
    curLevel++;
  }

  public void closeScope() {
    // TODO Exercise 4
    curScope = curScope.outer();
    curLevel--;
  }

  public Obj insert(Obj.Kind kind, String name, Struct type) {
    // TODO Exercise 4
    Obj obj = new Obj(kind, name, type);
    if (obj.kind == Obj.Kind.Var) {
      obj.level = curLevel;
      obj.adr = curScope.nVars();
    }

    if (curScope.findLocal(name) != null) {
      parser.error(DECL_NAME, name);
    } else {
      curScope.insert(obj);
    }
    return obj;
  }

  /**
   * Retrieves the object with <code>name</code> from the innermost scope.
   */
  public Obj find(String name) {
    // TODO Exercise 4
    Obj p = curScope.findGlobal(name);
    if (p == null) {
      parser.error(NOT_FOUND, name);
      return noObj;
    }
    return p;
  }

  /**
   * Retrieves the field <code>name</code> from the fields of
   * <code>type</code>.
   */
  public Obj findField(String name, Struct type) {
    // TODO Exercise 4
    Obj o = type.findField(name);
    if (o == null) {
      parser.error(NO_FIELD, name);
      return noObj;
    }
    return type.findField(name);
  }

  public void init() {
    // Inserting predefined types into the "Universe"
    insert(Obj.Kind.Type, "int", intType);
    insert(Obj.Kind.Type, "char", charType);
    insert(Obj.Kind.Con, "null", nullType);

    // Setting up predefined functions for the "Universe"
    chrObj = new Obj(Obj.Kind.Meth, "chr", charType);
    ordObj = new Obj(Obj.Kind.Meth, "ord", intType);
    lenObj = new Obj(Obj.Kind.Meth, "len", intType);
    noObj = new Obj(Obj.Kind.Var, "noObj", intType);

    // Inserting predefined functions into "Universe"
    chrObj.nPars = 1;
    Obj i = new Obj(Obj.Kind.Var, "i", intType);
    insertPredefinedObject(chrObj, i);

    ordObj.nPars = 1;
    Obj ch = new Obj(Obj.Kind.Var, "ch", charType);
    insertPredefinedObject(ordObj, ch);

    lenObj.nPars = 1;
    Obj arr = new Obj(Obj.Kind.Var, "arr", new Struct(noType));
    insertPredefinedObject(lenObj, arr);
  }

  void insertPredefinedObject(Obj o, Obj param) {
    final int LOCAL = 1;

    curScope.insert(o);
    openScope();
    // Saving the parameter
    param.level = LOCAL;
    curScope.insert(param);
    o.locals = curScope.locals();

    closeScope();
  }
  // ===============================================
  // ===============================================
}
