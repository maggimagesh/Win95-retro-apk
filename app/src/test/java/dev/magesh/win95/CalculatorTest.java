package dev.magesh.win95;
import org.junit.Test;
import static org.junit.Assert.*;
public class CalculatorTest {
 @Test public void respectsPrecedence(){assertEquals("14",Calculator.evaluate("2+3*4"));}
 @Test public void bracketsAndUnary(){assertEquals("-20",Calculator.evaluate("-(2+3)*4"));}
 @Test public void decimalsStayExact(){assertEquals("0.3",Calculator.evaluate("0.1+0.2"));}
 @Test public void divisionIsBounded(){assertEquals("0.333333333333",Calculator.evaluate("1/3"));}
 @Test public void invalidInputsAreRejected(){for(String bad:new String[]{"","2+","(3+2","abc","2..3","1/0","1;2","()"}){try{Calculator.evaluate(bad);fail(bad);}catch(RuntimeException expected){}}}
 @Test public void excessiveInputIsRejected(){try{Calculator.evaluate("1".repeat(201));fail();}catch(IllegalArgumentException expected){}}
}
