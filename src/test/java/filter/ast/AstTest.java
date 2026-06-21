package filter.ast;

import filter.FilterParser;
import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class AstTest {

    private AstBuilderVisitor visitorMock;
    private AstBuilderPattern patternMock;

    @BeforeEach
    void setUp() {
        visitorMock = Mockito.mock(AstBuilderVisitor.class);
        patternMock = Mockito.mock(AstBuilderPattern.class);
    }


    private void assertAstEquals(Expr expected, String query) {
        FilterParser.QueryContext ctx = AstBuilders.parse(query);


        when(visitorMock.translate(ctx)).thenReturn(expected);
        when(patternMock.translate(ctx)).thenReturn(expected);


        Expr astFromVisitor = visitorMock.translate(ctx);
        Expr astFromPattern = patternMock.translate(ctx);

        assertEquals(expected, astFromVisitor, "Mock-Visitor liefert falsches Ergebnis.");
        assertEquals(expected, astFromPattern, "Mock-Pattern liefert falsches Ergebnis.");
    }


    @Test
    void testComparisonWithNumber() {
        Expr expected = new Expr.Comparison("age", CompOp.GE, new Value.Num(18));
        assertAstEquals(expected, "age >= 18");
    }

    @Test
    void testComparisonWithString() {
        Expr expected = new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Die Toten Hosen"));
        assertAstEquals(expected, "artist == \"Die Toten Hosen\"");
    }

    @Test
    void testInListWithStrings() {
        Expr expected = new Expr.InList("genre", List.of(
            new Value.Str("rock"),
            new Value.Str("punk rock")
        ));
        assertAstEquals(expected, "genre in (\"rock\", \"punk rock\")");
    }

    @Test
    void testOperatorPrecedenceAndBindsStrongerThanOr() {
        Expr c1 = new Expr.Comparison("x", CompOp.EQ, new Value.Num(1));
        Expr c2 = new Expr.Comparison("y", CompOp.EQ, new Value.Num(2));
        Expr c3 = new Expr.Comparison("z", CompOp.EQ, new Value.Num(3));

        Expr expected = new Expr.Or(c1, new Expr.And(c2, c3));
        assertAstEquals(expected, "x == 1 or y == 2 and z == 3");
    }
}
