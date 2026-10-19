package dev.magesh.win95;

import java.math.BigDecimal;
import java.math.MathContext;

/** Small, bounded recursive-descent calculator. No scripting engine or eval. */
public final class Calculator {
    private final String source;
    private int position;
    private Calculator(String source) { this.source = source.replace(" ", ""); }
    public static String evaluate(String source) {
        if (source == null || source.trim().isEmpty() || source.length() > 200) throw new IllegalArgumentException("Enter a calculation");
        Calculator parser = new Calculator(source);
        BigDecimal answer = parser.expression();
        if (parser.position != parser.source.length()) throw new IllegalArgumentException("Invalid calculation");
        return answer.round(new MathContext(12)).stripTrailingZeros().toPlainString();
    }
    private BigDecimal expression() {
        BigDecimal value = term();
        while (position < source.length()) {
            char op = source.charAt(position);
            if (op != '+' && op != '-') break;
            position++;
            value = op == '+' ? value.add(term()) : value.subtract(term());
        }
        return value;
    }
    private BigDecimal term() {
        BigDecimal value = factor();
        while (position < source.length()) {
            char op = source.charAt(position);
            if (op != '*' && op != '/') break;
            position++;
            value = op == '*' ? value.multiply(factor()) : value.divide(factor(), MathContext.DECIMAL64);
        }
        return value;
    }
    private BigDecimal factor() {
        if (position >= source.length()) throw new IllegalArgumentException("Incomplete calculation");
        if (source.charAt(position) == '+') { position++; return factor(); }
        if (source.charAt(position) == '-') { position++; return factor().negate(); }
        if (source.charAt(position) == '(') {
            position++; BigDecimal value = expression();
            if (position >= source.length() || source.charAt(position++) != ')') throw new IllegalArgumentException("Missing bracket");
            return value;
        }
        int start = position;
        while (position < source.length() && (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) position++;
        if (start == position) throw new IllegalArgumentException("Expected number");
        return new BigDecimal(source.substring(start, position));
    }
}
