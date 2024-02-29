package ssw.mj.test;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;

import ssw.mj.Errors;
import ssw.mj.Interpreter;
import ssw.mj.scanner.Token;
import ssw.mj.codegen.Decoder;
import ssw.mj.impl.Parser;
import ssw.mj.impl.Scanner;
import ssw.mj.impl.Tab;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for test cases with utility methods used by all tests.
 */
public abstract class BaseCompilerTestCase {

  public static final String CR = "\r";
  public static final String LF = "\n";
  private List<String> expectedErrors;
  private List<String> expectedTokens;
  private List<String> expectedSymTab;
  private String source;
  private Scanner scanner;
  protected Parser parser;
  private String callingClassAndMethod;
  private final List<String> runInputs = new ArrayList<>();
  private final List<String> expectedOutputs = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    // initialize expected compiler output
    expectedErrors = new ArrayList<>();
    expectedTokens = new ArrayList<>();
    expectedSymTab = new ArrayList<>();

    if (Configuration.ALSO_PRINT_SUCCESSFUL_TESTCASES) {
      // print header for console output
      System.out.println("--------------------------------------------------");
    }
  }

  protected void initScanner(String s) {
    source = s;
    scanner = new Scanner(new StringReader(s));
  }

  protected void init(String s) {
    initScanner(s);
    parser = new Parser(scanner);
  }

  protected void initScannerFile(String filename) {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      URL resource = classLoader.getResource(filename);
      if (resource == null) {
        throw new RuntimeException("resource %s not found".formatted(filename));
      }
      String path = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
      File file = new File(path);
      scanner = new Scanner(new FileReader(file));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  protected void initFile(String s) {
    initScannerFile(s);
    parser = new Parser(scanner);
  }

  private List<String> splitString(String s) {
    StringTokenizer st = new StringTokenizer(s, "\n");
    List<String> result = new ArrayList<>();
    while (st.hasMoreTokens()) {
      result.add(st.nextToken());
    }
    return result;
  }

  private void print(String title, List<String> expected, List<String> actual) {
    if (expected.isEmpty() && actual.isEmpty()) {
      return;
    }
    System.out.format("%s - %s\n", callingClassAndMethod, title);
    if (Configuration.ALSO_PRINT_SUCCESSFUL_TESTCASES || !expected.equals(actual)) {
      System.out.format("  %-60s %s\n", "expected", "actual");
      int lines = Math.max(expected.size(), actual.size());
      for (int i = 0; i < lines; i++) {
        String expectedLine = (i < expected.size() ? expected.get(i) : "");
        String actualLine = (i < actual.size() ? actual.get(i) : "");
        System.out.format("%s %-60s %s\n", (expectedLine.equals(actualLine) ? " " : "x"), expectedLine,
                actualLine);
      }
    } else {
      if (expected.equals(actual)) {
        System.out.println("  correct (exact comparison hidden, enable via Configuration.ALSO_PRINT_SUCCESSFUL_TESTCASES)");
      }
    }
  }

  protected void addExpectedRun(String output) {
    addExpectedRun("", output);
  }

  protected void addExpectedRun(String input, String output) {
    runInputs.add(input);
    expectedOutputs.add(output);
  }

  protected void scanAndVerify() {
    callingClassAndMethod = getCallingClassAndMethod(1);

    System.out.println();
    List<String> actualTokens = new ArrayList<>();
    // scan only the expected number of tokens to prevent endless loops
    for (int i = 0; i < getExpectedTokens().size(); i++) {
      actualTokens.add(scanner.next().toString());
    }

    printErrors();
    printTokens(actualTokens);

    verifyErrors();
    verifyTokens(actualTokens);
  }

  protected void parseAndVerify() {
    callingClassAndMethod = getCallingClassAndMethod(1);

    System.out.println();
    try {
      parser.parse();
      assertEquals(scanner.next().kind, Token.Kind.eof, "Complete input should be scanned");
    } catch (Errors.PanicMode error) {
      // Ignore, nothing to do
    }

    printErrors();
    printSymTab();

    verifyErrors();
    verifySymTab();

    printAndVerifyByteCode(callingClassAndMethod);

    if (ByteCodeTestSupport.GENERATE_REFERENCE_BYTE_CODE && expectedErrors.isEmpty()) {
      ByteCodeTestSupport.generateReferenceByteCode(callingClassAndMethod, parser);
    }

    for (int i = 0; i < runInputs.size(); i++) {
      run(i);
    }
  }

  private static String getCallingClassAndMethod(int up) {
    // [0] getStackTrace -> [1] getCallingMethodName -> [2] caller of getCallingMethodName -> [3] ...
    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
    StackTraceElement e = stacktrace[2 + up];
    String fullyQualifiedClassName = e.getClassName();
    String className = fullyQualifiedClassName.substring(Math.max(fullyQualifiedClassName.lastIndexOf(".") + 1, 0));
    return className + "." + e.getMethodName() + "()";
  }

  private void run(int i) {
    Interpreter.BufferIO io = new Interpreter.BufferIO(runInputs.get(i));
    Interpreter interpreter = new Interpreter(
            parser.code.buf,
            parser.code.mainpc,
            parser.code.dataSize,
            io,
            Configuration.PRINT_INTERPRETER_DEBUG_OUTPUT);
    interpreter.run();
    String output = io.getOutput();
    verifyOutput(i, output);
  }


  private void printErrors() {
    print("Errors", expectedErrors, getActualErrors());
  }

  private void printTokens(List<String> actualTokens) {
    print("Tokens", getExpectedTokens(), actualTokens);
  }

  private void printSymTab() {
    if (!expectedSymTab.isEmpty()) {
      print("Symbol Table", getExpectedSymTab(), getActualSymTab());
    }
  }

  private void verifyErrors() {
    assertEquals(expectedErrors, getActualErrors(), "Errors");
  }

  private void verifyTokens(List<String> actualTokens) {
    assertEquals(getExpectedTokens(), actualTokens, "Tokens");
    assertTrue(scanner.next().toString().contains("end of file"), "Complete Input Scanned");
  }

  private void verifySymTab() {
    if (!expectedSymTab.isEmpty()) {
      assertEquals(getExpectedSymTab(), getActualSymTab(), "Symbol Table");
    }
  }

  private void printAndVerifyByteCode(String callingClassAndMethod) {
    if (ByteCodeTestSupport.BYTE_CODES.containsKey(callingClassAndMethod)) {
      List<String> possibleByteCodes = ByteCodeTestSupport.BYTE_CODES.get(callingClassAndMethod);
      if (possibleByteCodes.size() == 1) {
        List<String> expected = getExpectedByteCodeLines(possibleByteCodes.get(0));
        print("Bytecode", expected, getActualByteCodeLines());
        // Verify that the bytecode is correct
        assertEquals(expected, getActualByteCodeLines(), "Byte Code");
      } else {
        int matchIdx = -1;
        for (int i = 0; i < possibleByteCodes.size(); i++) {
          List<String> expected = getExpectedByteCodeLines(possibleByteCodes.get(i));
          if (expected.equals(getActualByteCodeLines())) {
            matchIdx = i;
            break;
          }
        }
        if (matchIdx < 0) {
          // No bytecode matched
          // print all
          for (int i = 0; i < possibleByteCodes.size(); i++) {
            List<String> expected = getExpectedByteCodeLines(possibleByteCodes.get(i));
            print("Possible Bytecode %d".formatted(i + 1), expected, getActualByteCodeLines());
          }
          // fail assert on first
          assertEquals(getExpectedByteCodeLines(possibleByteCodes.get(0)), getActualByteCodeLines(), "Byte Code");
        } else {
          // bytecode at idx matchIdx correctly generated
          // print working bytecode
          print("Bytecode", getExpectedByteCodeLines(possibleByteCodes.get(matchIdx)), getActualByteCodeLines());
          // assert not really necessary since we already know we matched successfully
          assertEquals(getExpectedByteCodeLines(possibleByteCodes.get(matchIdx)), getActualByteCodeLines(), "Byte Code");
        }
      }
    }
  }

  private void verifyOutput(int runIdx, String actualOutput) {
    assertEquals(expectedOutputs.get(runIdx), actualOutput, "Unexpected result when input is \"" + runInputs.get(runIdx) + "\": ");
  }

  private List<String> getExpectedByteCodeLines(String bytecode) {
    return Arrays.stream(bytecode.split("\n")).toList();
  }

  private List<String> getActualByteCodeLines() {
    return Arrays.stream(new Decoder().decode(parser.code).split("\n")).toList();
  }

  private List<String> getActualErrors() {
    return splitString(scanner.errors.dump());
  }

  private List<String> getExpectedTokens() {
    return expectedTokens;
  }

  private List<String> getExpectedSymTab() {
    return expectedSymTab;
  }

  private List<String> getActualSymTab() {
    return splitString(dump(parser.tab));
  }

  protected void expectError(int line, int col, Errors.Message msg, Object... msgParams) {
    expectedErrors.add("-- line " + line + " col " + col + ": " + msg.format(msgParams));
  }

  protected void expectToken(Token.Kind kind, int line, int col) {
    expectedTokens.add("line " + line + ", col " + col + ", kind " + kind);
  }

  protected void expectToken(Token.Kind kind, int line, int col, String str) {
    expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", str " + str);
  }

  protected void expectToken(Token.Kind kind, int line, int col, int val) {
    expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", str " + val + ", val " + val);
  }

  protected void expectToken(Token.Kind kind, int line, int col, int val, String str) {
    expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", str " + str + ", val " + val);
  }

  protected void expectToken(Token.Kind kind, int line, int col, char val) {
    expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", val '" + val + "'");
  }

  protected void expectSymTab(String line) {
    expectedSymTab.add(line);
  }

  protected void expectSymTabUniverse() {
    // first part of the symbol table (universe) that is equal for all
    // programs
    expectSymTab("-- begin scope (0 variables) --");
    expectSymTab("Type int: int");
    expectSymTab("Type char: char");
    expectSymTab("Constant: class(0) null = 0");
    expectSymTab("Method: char chr(1)");
    expectSymTab("  Local Variable 0: int i");
    expectSymTab("Method: int ord(1)");
    expectSymTab("  Local Variable 0: char ch");
    expectSymTab("Method: int len(1)");
    expectSymTab("  Local Variable 0: void[] arr");
  }

  private static String dump(Tab tab) {
    StringBuilder sb = new StringBuilder();
    if (tab.curScope != null) {
      dump(tab.curScope, sb);
    }
    return sb.toString();
  }

  private static void dump(Scope scope, StringBuilder sb) {
    sb.append("-- begin scope (").append(scope.nVars()).append(" variables) --\n");
    if (!scope.locals().isEmpty()) {
      dump(scope.locals().values(), sb, "");
    }
    if (scope.outer() != null) {
      sb.append("\n");
      dump(scope.outer(), sb);
    }
  }

  private static void dump(Collection<Obj> objects, StringBuilder sb, String indent) {
    for (Obj obj : objects) {
      dump(obj, sb, indent);
    }
  }

  private static void dump(Obj obj, StringBuilder sb, String indent) {
    sb.append(indent);

    switch (obj.kind) {
      case Con -> {
        sb.append("Constant: ");
        if (obj.type != null) {
          dump(obj.type, sb, indent, false);
        }
        sb.append(" ").append(obj.name).append(" = ");
        if (obj.type == Tab.charType) {
          sb.append("'").append((char) obj.val).append("'");
        } else {
          sb.append(obj.val);
        }
      }
      case Var -> {
        if (obj.level == 0) {
          sb.append("Global Variable ");
        } else {
          sb.append("Local Variable ");
        }
        sb.append(obj.adr).append(": ");
        if (obj.type != null) {
          dump(obj.type, sb, indent, false);
        }
        sb.append(" ").append(obj.name);
      }
      case Type -> {
        sb.append("Type ").append(obj.name).append(": ");
        if (obj.type != null) {
          dump(obj.type, sb, indent + "  ", true);
        }
      }
      case Meth -> {
        sb.append("Method: ");
        if (obj.type != null) {
          dump(obj.type, sb, indent, false);
        }
        sb.append(" ").append(obj.name).append("(").append(obj.nPars);
        sb.append(")");
      }
      case Prog -> sb.append("Program ").append(obj.name).append(":");
    }

    if (obj.locals != null) {
      sb.append("\n");
      dump(obj.locals.values(), sb, indent + "  ");
    }
    sb.append("\n");
  }

  private static void dump(Struct struct, StringBuilder sb, String indent, boolean dumpFields) {
    switch (struct.kind) {
      case None -> sb.append("void");
      case Int -> sb.append("int");
      case Char -> sb.append("char");
      case Arr -> {
        if (struct.elemType != null) {
          dump(struct.elemType, sb, indent, dumpFields);
        }
        sb.append("[]");
      }
      case Class -> {
        sb.append("class(").append(struct.nrFields()).append(")");
        if (dumpFields && struct.fields != null) {
          sb.append("\n");
          dump(struct.fields.values(), sb, indent);
        }
      }
    }
  }
}