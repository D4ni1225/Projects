package ssw.mj.test;

import static ssw.mj.Errors.Message.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Timeout;
import ssw.mj.Errors.Message;

/**
 * Test cases for the <code>Parser</code> class.
 */
@Timeout(Configuration.TIMEOUT)
public class SymbolTableTest extends BaseCompilerTestCase {

  @Test
  public void shortestProgram() {
    init("program Test { void main() { } }");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void definitions() {
    init("program Test" + LF + //
            "  class myClass {" + LF + //
            "    int i;" + LF + //
            "    char c;" + LF + //
            "    int[] ia;" + LF + //
            "    myClass o;" + LF + //
            "    myClass[] oa;" + LF + //
            "  }" + LF + //
            "  final int fi = 20;" + LF + //
            "  final char fc = 'x';" + LF + //
            "  int gi;" + LF + //
            "  char gc;" + LF + //
            "  int[] gia;" + LF + //
            "  myClass go;" + LF + //
            "  myClass[] goa;" + LF + //
            "{" + LF + //
            "  int method() " + LF + //
            "    int i;" + LF + //
            "    char c;" + LF + //
            "    char[] ca;" + LF + //
            "    myClass o;" + LF + //
            "    myClass[] oa;" + LF + //
            "  {" + LF + //
            "    return 1;" + LF + //
            "  }" + LF + //
            "  void main() { }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type myClass: class(5)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");
    expectSymTab("    Local Variable 2: int[] ia");
    expectSymTab("    Local Variable 3: class(5) o");
    expectSymTab("    Local Variable 4: class(5)[] oa");
    expectSymTab("  Constant: int fi = 20");
    expectSymTab("  Constant: char fc = 'x'");
    expectSymTab("  Global Variable 0: int gi");
    expectSymTab("  Global Variable 1: char gc");
    expectSymTab("  Global Variable 2: int[] gia");
    expectSymTab("  Global Variable 3: class(5) go");
    expectSymTab("  Global Variable 4: class(5)[] goa");
    expectSymTab("  Method: int method(0)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");
    expectSymTab("    Local Variable 2: char[] ca");
    expectSymTab("    Local Variable 3: class(5) o");
    expectSymTab("    Local Variable 4: class(5)[] oa");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void types() {
    init("program Test" + LF + //
            "  class C { int i1; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int i1;" + LF + //
            "    int i2;" + LF + //
            "    C o1;" + LF + //
            "    C o2;" + LF + //
            "    C[] oa1;" + LF + //
            "    C[] oa2;" + LF + //
            "  {" + LF + //
            "    i1 = 0;" + LF + //
            "    i1 = i2;" + LF + //
            "    o1 = null;" + LF + //
            "    o1 = o2;" + LF + //
            "    oa1 = null;" + LF + //
            "    oa1 = oa2;" + LF + //
            "    oa1[i1] = o1;" + LF + //
            "    oa1[i1].i1 = i2;" + LF + //
            "    if (0 > i1) { }" + LF + //
            "    if (null != o1) { }" + LF + //
            "    if (null == oa1) { }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: class(1) o1");
    expectSymTab("    Local Variable 3: class(1) o2");
    expectSymTab("    Local Variable 4: class(1)[] oa1");
    expectSymTab("    Local Variable 5: class(1)[] oa2");

    parseAndVerify();
  }

  @Test
  public void exprLocal() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int i1, i2, i3, i4, i5;" + LF + //
            "  {" + LF + //
            "    i1 = i2;" + LF + //
            "    i1 += i2 + 3 * i3 - i4 % i5;" + LF + //
            "    i1++;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");
    expectSymTab("    Local Variable 4: int i5");

    parseAndVerify();
  }

  @Test
  public void exprGlobal() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "  int i1, i2, i3, i4, i5;" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "  {" + LF + //
            "    i1 = i2;" + LF + //
            "    i1 -= i2 + 3 * i3 - i4 % i5;" + LF + //
            "    i1--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Global Variable 0: int i1");
    expectSymTab("  Global Variable 1: int i2");
    expectSymTab("  Global Variable 2: int i3");
    expectSymTab("  Global Variable 3: int i4");
    expectSymTab("  Global Variable 4: int i5");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void exprField() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    C i1, i2, i3, i4, i5;" + LF + //
            "  {" + LF + //
            "    i1.i = i2.i;" + LF + //
            "    i1.i *= i2.i + 3 * i3.i - i4.i % i5.i;" + LF + //
            "    i1.i++;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: class(1) i1");
    expectSymTab("    Local Variable 1: class(1) i2");
    expectSymTab("    Local Variable 2: class(1) i3");
    expectSymTab("    Local Variable 3: class(1) i4");
    expectSymTab("    Local Variable 4: class(1) i5");

    parseAndVerify();
  }

  @Test
  public void exprArray() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    C[] i1, i2, i3, i4, i5;" + LF + //
            "  {" + LF + //
            "    i1[1].i = i2[2].i;" + LF + //
            "    i1[1].i %= i2[2].i + 3 * i3[3].i - i4[4].i % i5[5].i;" + LF + //
            "    i1[1].i--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: class(1)[] i1");
    expectSymTab("    Local Variable 1: class(1)[] i2");
    expectSymTab("    Local Variable 2: class(1)[] i3");
    expectSymTab("    Local Variable 3: class(1)[] i4");
    expectSymTab("    Local Variable 4: class(1)[] i5");

    parseAndVerify();
  }

  @Test
  public void minus() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int i1, i2, i3, i4, i5;" + LF + //
            "  {" + LF + //
            "    i1 = -i2;" + LF + //
            "    i1 = -i2 + (-3) * (-(-i3)) - (-i4) % (-i5);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");
    expectSymTab("    Local Variable 4: int i5");

    parseAndVerify();
  }

  @Test
  public void caseSensitiv() {
    init("program Test" + LF + //
            "  int a, A, b;" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int a, b, B;" + LF + //
            "  {" + LF + //
            "    a = A;" + LF + //
            "    b = B;" + LF + //
            "    B = a;" + LF + //
            "    A = b;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Global Variable 0: int a");
    expectSymTab("  Global Variable 1: int A");
    expectSymTab("  Global Variable 2: int b");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int a");
    expectSymTab("    Local Variable 1: int b");
    expectSymTab("    Local Variable 2: int B");

    parseAndVerify();
  }

  @Test
  public void constTest() {
    init("program Test" + LF + //
            "  final int fi = 20;" + LF + //
            "  final char fc = 'a';" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int i;" + LF + //
            "    char c;" + LF + //
            "  {" + LF + //
            "    i = 4;" + LF + //
            "    i = -1;" + LF + //
            "    i = 10;" + LF + //
            "    i = fi;" + LF + //
            "    i = -fi;" + LF + //
            "    i = fi + 5;" + LF + //
            "    i = 5 * (-fi);" + LF + //
            "    c = fc;" + LF + //
            "    c = 'a';" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Constant: int fi = 20");
    expectSymTab("  Constant: char fc = 'a'");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");

    parseAndVerify();
  }

  @Test
  public void newTest() {
    init("program Test" + LF + // 1
            "  class C { int i; }" + LF + // 2
            "{" + LF + // 3
            "  void main()" + LF + // 4
            "    int i1, i2;" + LF + // 5
            "    C obj;" + LF + // 6
            "    char[] ca;" + LF + // 7
            "    C[] oa;" + LF + // 8
            "  {" + LF + // 9
            "    obj = new C;" + LF + // 10
            "    ca = new char[5];" + LF + // 11
            "    oa = new C[i1 * obj.i - oa[obj.i - 5].i];" + LF + // 12
            "  }" + LF + // 13
            "}" // 14
    );

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(1)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: class(1) obj");
    expectSymTab("    Local Variable 3: char[] ca");
    expectSymTab("    Local Variable 4: class(1)[] oa");

    parseAndVerify();
  }

  @Test
  public void ifGt() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    if (i1 > i2) i1++; else i1--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void ifAnd() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    if (i1 > i2 && i3 < i4) i1++; else i1--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void ifOr() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    if (i1 > i2 || i3 < i4) i1++; else i1--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void ifAndOr() {
    init("program Test {" + LF //
            + "  void main() int i1, i2, i3, i4; {" + LF //
            + "    if (i1 > i2 && i3 < i4 || i1 == i2 && i3 != i4) i1++; else i1--;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void ifNested() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    if (i1 > i2) {" + LF + //
            "      if (i3 < i4) i1++;" + LF + //
            "    } else {" + LF + //
            "      if (i3 > i4) i1--;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void forGt() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    while (i1 > i2) {" + LF + //
            "      i1++;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void forAnd() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    while (i1 > i2 && i3 > i4) {" + LF + //
            "      i1++;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void forOr() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    while (i1 > i2 || i3 > i4) {" + LF + //
            "      i1++;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void forNested() {
    init("program Test {" + LF + //
            "  void main() int i1, i2, i3, i4; {" + LF + //
            "    if (i1 > i2) {" + LF + //
            "      i1++;" + LF + //
            "      if (i3 < i4) {" + LF + //
            "        while (i1 == i2) ;" + LF + //
            "      }" + LF + //
            "      while (i3 != i4) ;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i1");
    expectSymTab("    Local Variable 1: int i2");
    expectSymTab("    Local Variable 2: int i3");
    expectSymTab("    Local Variable 3: int i4");

    parseAndVerify();
  }

  @Test
  public void forBreak() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    while (i < 10) {" + LF + //
            "      i++;" + //
            "      break;" + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i");

    parseAndVerify();
  }

  @Test
  public void predefMeth() {
    init("program Test {" + LF + //
            "  void main() int i; char c; int[] ia; {" + LF + //
            "    i = ord(c);" + LF + //
            "    c = chr(i);" + LF + //
            "    i = len(ia);" + LF + //
            "    print(i);" + LF + //
            "    print(c, 4);" + LF + //
            "    read(i);" + LF + //
            "    read(c);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");
    expectSymTab("    Local Variable 2: int[] ia");

    parseAndVerify();
  }

  @Test
  public void predefFunAsMeth() {
    init("program Test {" + LF + //
            "  void main() int i; char c; int[] ia; {" + LF + //
            "    ord(c);" + LF + //
            "    chr(i);" + LF + //
            "    len(ia);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");
    expectSymTab("    Local Variable 2: int[] ia");

    parseAndVerify();
  }

  @Test
  public void callVoid() {
    init("program Test {" + LF + //
            "  void method() {" + LF + //
            "    return;" + LF + //
            "  }" + LF + //
            "  void main() {" + LF + //
            "    method();" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void method(0)");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void callInt() {
    init("program Test {" + LF + //
            "  int method() {" + LF + //
            "    return 1;" + LF + //
            "  }" + LF + //
            "  void main() {" + LF + //
            "    print(method());" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: int method(0)");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void callParam() {
    init("program Test {" + LF + //
            "  int method(int i, char c, int[] ia) {" + LF + //
            "    return i + ia[ord(c)];" + LF + //
            "  }" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    print(method(i, c, new int[i]));" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: int method(3)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");
    expectSymTab("    Local Variable 2: int[] ia");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int i");
    expectSymTab("    Local Variable 1: char c");

    parseAndVerify();
  }

  @Test
  public void callFuncAsProc() {
    init("program Test {" + LF + //
            "  int method() {" + LF + //
            "    return 1;" + LF + //
            "  }" + LF + //
            "  void main() {" + LF + //
            "    method();" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: int method(0)");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void multReturn() {
    init("program Test {" + LF + //
            "  int method(int p) {" + LF + //
            "    if (p > 0) return 1;" + LF + //
            "    if (p < 0) return -1;" + LF + //
            "    return 0;" + LF + //
            "  }" + LF + //
            "  void main() {" + LF + //
            "    print(method(5));" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: int method(1)");
    expectSymTab("    Local Variable 0: int p");
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void manyLocals() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 127; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test {" + LF + //
            "  void main()" + LF + //
            "    int " + names + ";" + LF + //
            "  {" + LF + //
            "    i0 = i126;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Method: void main(0)");
    for (int i = 0; i < 127; i++) {
      expectSymTab("    Local Variable " + i + ": int i" + i);
    }

    parseAndVerify();
  }

  @Test
  public void manyGlobals() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 300; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test" + LF + //
            "  int " + names + ";" + LF + //
            "{" + LF + //
            "  void main() {" + LF + //
            "    i0 = i299;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    for (int i = 0; i < 300; i++) {
      expectSymTab("  Global Variable " + i + ": int i" + i);
    }
    expectSymTab("  Method: void main(0)");

    parseAndVerify();
  }

  @Test
  public void manyFields() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 300; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test" + LF + //
            "  class C {" + LF + //
            "    int " + names + ";" + LF + //
            "  }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    C c;" + LF + //
            "  {" + LF + //
            "    c.i0 = c.i299;" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program Test:");
    expectSymTab("  Type C: class(300)");
    for (int i = 0; i < 300; i++) {
      expectSymTab("    Local Variable " + i + ": int i" + i);
    }
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: class(300) c");

    parseAndVerify();
  }

  @Test
  public void scriptExample() {
    init("program P" + LF + //
            "  final int size = 10;" + LF + //
            "" + LF + //
            "  class Table {" + LF + //
            "    int[] pos;" + LF + //
            "    int[] neg;" + LF + //
            "  }" + LF + //
            "" + LF + //
            "  Table val;" + LF + //
            "" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int x, i;" + LF + //
            "  { /*---------- Initialize val */" + LF + //
            "    val = new Table;" + LF + //
            "    val.pos = new int[size];" + LF + //
            "    val.neg = new int[size];" + LF + //
            "    i = 0;" + LF + //
            "    while (i < size) {" + LF + //
            "      val.pos[i] = 0; val.neg[i] = 0; i++;" + LF + //
            "    }" + LF + //
            "    /*---------- Read values */" + LF + //
            "    read(x);" + LF + //
            "    while (x != 0) {" + LF + //
            "      if (0 <= x && x < size) {" + LF + //
            "        val.pos[x]++;" + LF + //
            "      } else if (-size < x && x < 0) {" + LF + //
            "        val.neg[-x]++;" + LF + //
            "      }" + LF + //
            "      read(x);" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");

    expectSymTabUniverse();
    expectSymTab("Program P:");
    expectSymTab("  Constant: int size = 10");
    expectSymTab("  Type Table: class(2)");
    expectSymTab("    Local Variable 0: int[] pos");
    expectSymTab("    Local Variable 1: int[] neg");
    expectSymTab("  Global Variable 0: class(2) val");
    expectSymTab("  Method: void main(0)");
    expectSymTab("    Local Variable 0: int x");
    expectSymTab("    Local Variable 1: int i");

    parseAndVerify();
  }

  @Test
  public void tooManyLocals() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 127; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test {" + LF + //
            "  void main()" + LF + //
            "    int " + names + ";" + LF + //
            "    int error;" + LF + //
            "  {" + LF + //
            "  }" + LF + //
            "}");
    expectError(5, 3, TOO_MANY_LOCALS);
    parseAndVerify();
  }

  @Test
  public void tooManyLocals2() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 126; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test {" + LF + //
            "  void foo(int x)" + LF + //
            "    int " + names + ";" + LF + //
            "    int error;" + LF + //
            "  {" + LF + //
            "  }" + LF + //
            "  void main()" + LF + //
            "  {}" + LF + //
            "}");
    expectError(5, 3, TOO_MANY_LOCALS);
    parseAndVerify();
  }

  @Test
  public void tooManyGlobals() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 32767; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test" + LF + //
            "  int " + names + ";" + LF + //
            "  int error;" + LF + //
            "{" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(4, 1, TOO_MANY_GLOBALS);

    parseAndVerify();
  }

  @Test
  public void tooManyFields() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 32767; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("i");
      sb.append(i);
    }
    String names = sb.toString();

    init("program Test" + LF + //
            "  class C {" + LF + //
            "    int " + names + ";" + LF + //
            "    int error;" + LF + //
            "  }" + LF + //
            "{" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(5, 3, TOO_MANY_FIELDS);

    parseAndVerify();
  }

  @Test
  public void doubleDeclVar() {
    init("program Test " + LF + //
            "  int x;" + LF + //
            "  int x;" + LF + //
            "{" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(3, 8, DECL_NAME, "x");
    parseAndVerify();
  }

  @Test
  public void doubleDeclMeth() {
    init("program Test " + LF + //
            "  int x;" + LF + //
            "{" + LF + //
            "  void x() { }" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(4, 9, DECL_NAME, "x");
    parseAndVerify();
  }

  @Test
  public void doubleDeclLocal() {
    init("program Test {" + LF + //
            "  void method(int x)" + LF + //
            "    int x;" + LF + //
            "  { }" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(3, 10, DECL_NAME, "x");
    parseAndVerify();
  }

  @Test
  public void undefNameType() {
    init("program Test" + LF + //
            "  type x;" + LF + //
            "{" + LF + //
            "  void main() { }" + LF + //
            "}");
    expectError(2, 8, NOT_FOUND, "type");
    parseAndVerify();
  }

  @Test
  public void noType() {
    init("program Test" + LF + //
            "  int i;" + LF + //
            "  i s;" + LF + //
            "{ void main() { } }");
    expectError(3, 5, NO_TYPE);
    parseAndVerify();
  }

  @Test
  public void wrongConstTypeInt() {
    init("program Test" + LF + //
            "  final int i = 'a';" + LF + //
            "{ void main() { } }");
    expectError(2, 17, CONST_TYPE);
    parseAndVerify();
  }

  @Test
  public void wrongConstTypeChar() {
    init("program Test" + LF + //
            "  final char ch = 32;" + LF + //
            "{ void main() { } }");
    expectError(2, 19, CONST_TYPE);
    parseAndVerify();
  }

  @Test
  public void wrongConstType() {
    init("program Test" + LF + //
            "  class C { int i; }" + LF + //
            "  final C c = 32;" + LF + //
            "{ void main() { } }");
    expectError(3, 15, CONST_TYPE);
    parseAndVerify();
  }

  @Test
  public void mainNotVoid() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            "  int main() { }" + LF + // 3
            "}" + LF // 6
    );
    expectError(3, 14, Message.MAIN_NOT_VOID);
    parseAndVerify();
  }

  @Test
  public void mainNoParams() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            "  void main(int x) { }" + LF + // 3
            "}" + LF // 6
    );
    expectError(3, 20, Message.MAIN_WITH_PARAMS);
    parseAndVerify();
  }
}
