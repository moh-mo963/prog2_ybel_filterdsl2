package filter.ast;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.printer.AstPrinter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

public class ApprovalTest {

    @Test
    public void testAstBuilderPattern() {
        String query = "artist == \"Die Toten Hosen\" and (year >= 1996 and genre in (\"rock\", \"punk rock\"))";
        var ast = new AstBuilderPattern().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }

    @Test
    public void testAstBuilderVisitor() {
        String query = "artist == \"Die Toten Hosen\" and (year >= 1996 and genre in (\"rock\", \"punk rock\"))";
        var ast = new AstBuilderVisitor().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }


    @Test
    public void testComplexQuery() {
        String query = "artist == \"Die Toten Hosen\" and (year >= 1996 or (genre in (\"rock\", \"punk rock\") and not (album == \"Ein kleines bisschen Horrorschau\")))";
        var ast = new AstBuilderPattern().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }

    @Test
    public void testComplexQueryVisitor() {
        String query = "artist == \"Die Toten Hosen\" and (year >= 1996 or (genre in (\"rock\", \"punk rock\") and not (album == \"Ein kleines bisschen Horrorschau\")))";
        var ast = new AstBuilderVisitor().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }

    @Test
    public void testSimpleQuery() {
        String query = "artist == \"Die Toten Hosen\"";
        var ast = new AstBuilderPattern().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }

    @Test
    public void testSimpleQueryVisitor() {
        String query = "artist == \"Die Toten Hosen\"";
        var ast = new AstBuilderVisitor().translate(AstBuilders.parse(query));
        String printedAst = AstPrinter.toString(ast);
        Approvals.verify(printedAst);
    }
}
