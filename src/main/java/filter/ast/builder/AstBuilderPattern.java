package filter.ast.builder;

import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AstBuilderPattern {

    // Public entry point
    // query  : expr EOF
    public Expr translate(FilterParser.QueryContext ctx) {
        return buildExpr(ctx.expr());
    }

    // expr: orExpr
    private Expr buildExpr(FilterParser.ExprContext ctx) {
        return buildOrExpr(ctx.orExpr());
    }

    // orExpr : andExpr (OR andExpr)*
    private Expr buildOrExpr(FilterParser.OrExprContext ctx) {
        Expr result = buildAndExpr(ctx.andExpr(0));
        for (int i = 1; i < ctx.andExpr().size(); i++) {
            result = new Expr.Or(result, buildAndExpr(ctx.andExpr(i)));
        }
        return result;
    }

    // andExpr: notExpr (AND notExpr)*
    private Expr buildAndExpr(FilterParser.AndExprContext ctx) {
        Expr result = buildNotExpr(ctx.notExpr(0));
        for (int i = 1; i < ctx.notExpr().size(); i++) {
            result = new Expr.And(result, buildNotExpr(ctx.notExpr(i)));
        }
        return result;
    }

    // notExpr: NOT notExpr | primary
    private Expr buildNotExpr(FilterParser.NotExprContext ctx) {
        return switch (ctx) {
            case FilterParser.NotExprContext not when not.NOT() != null ->
                new Expr.Not(buildNotExpr(not.notExpr()));
            case FilterParser.NotExprContext primary -> buildPrimary(primary.primary());
        };
    }

    // primary: comparison | '(' expr ')'
    private Expr buildPrimary(FilterParser.PrimaryContext ctx) {
        return switch (ctx) {
            case FilterParser.PrimaryContext comparison when comparison.comparison() != null ->
                buildComparison(comparison.comparison());
            case FilterParser.PrimaryContext nested -> buildExpr(nested.expr());
        };
    }

    // comparison
    //   : IDENTIFIER op=COMPOP value=literal
    //   | IDENTIFIER IN '(' literalList ')'
    private Expr buildComparison(FilterParser.ComparisonContext ctx) {
        String field = ctx.IDENTIFIER().getText();
        return switch (ctx) {
            case FilterParser.ComparisonContext comparison when comparison.op != null ->
                new Expr.Comparison(
                    field, CompOp.fromSymbol(comparison.op.getText()), buildLiteral(comparison.value));
            case FilterParser.ComparisonContext inList ->
                new Expr.InList(field, buildLiteralList(inList.literalList()));
        };
    }

    // literalList: literal (',' literal)*
    private List<Value> buildLiteralList(FilterParser.LiteralListContext ctx) {
        List<Value> values = new ArrayList<>();
        for (var literal : ctx.literal()) {
            values.add(buildLiteral(literal));
        }
        return values;
    }

    // literal: STRING | NUMBER
    private Value buildLiteral(FilterParser.LiteralContext ctx) {
        TerminalNode literal = ctx.STRING() != null ? ctx.STRING() : ctx.NUMBER();
        return switch (literal.getSymbol().getType()) {
            case FilterParser.STRING -> new Value.Str(unquote(literal.getText()));
            case FilterParser.NUMBER -> new Value.Num(Integer.parseInt(literal.getText()));
            default -> throw new IllegalArgumentException("Unknown literal: " + literal.getText());
        };
    }

    private String unquote(String text) {
        String content = text.substring(1, text.length() - 1);
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                result.append(c);
            }
        }
        if (escaped) {
            result.append('\\');
        }
        return result.toString();
    }
}
