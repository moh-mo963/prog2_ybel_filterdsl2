package filter.ast.builder;

import filter.FilterBaseVisitor;
import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class AstBuilderVisitor extends FilterBaseVisitor<Void> {

    // TODO
    private final Deque<Expr> expressions = new ArrayDeque<>();
    private final Deque<Value> values = new ArrayDeque<>();
    private final Deque<List<Value>> valueLists = new ArrayDeque<>();

    // Public entry point
    public Expr translate(FilterParser.QueryContext ctx) {
        // TODO
        expressions.clear();
        values.clear();
        valueLists.clear();

        visit(ctx);
        return expressions.pop();
    }

    // query  : expr EOF
    @Override
    public Void visitQuery(FilterParser.QueryContext ctx) {
        // TODO
        visit(ctx.expr());
        return null;
    }

    // expr: orExpr
    @Override
    public Void visitExpr(FilterParser.ExprContext ctx) {
        // TODO
        visit(ctx.orExpr());
        return null;
    }

    // orExpr : andExpr (OR andExpr)*
    @Override
    public Void visitOrExpr(FilterParser.OrExprContext ctx) {
        // TODO
        visit(ctx.andExpr(0));
        for (int i = 1; i < ctx.andExpr().size(); i++) {
            visit(ctx.andExpr(i));
            Expr right = expressions.pop();
            Expr left = expressions.pop();
            expressions.push(new Expr.Or(left, right));
        }
        return null;
    }

    // andExpr: notExpr (AND notExpr)*
    @Override
    public Void visitAndExpr(FilterParser.AndExprContext ctx) {
        // TODO
        visit(ctx.notExpr(0));
        for (int i = 1; i < ctx.notExpr().size(); i++) {
            visit(ctx.notExpr(i));
            Expr right = expressions.pop();
            Expr left = expressions.pop();
            expressions.push(new Expr.And(left, right));
        }
        return null;
    }

    // notExpr: NOT notExpr | primary
    @Override
    public Void visitNotExpr(FilterParser.NotExprContext ctx) {
        // TODO
        if (ctx.NOT() != null) {
            visit(ctx.notExpr());
            expressions.push(new Expr.Not(expressions.pop()));
        } else {
            visit(ctx.primary());
        }
        return null;
    }

    // primary: comparison | '(' expr ')'
    @Override
    public Void visitPrimary(FilterParser.PrimaryContext ctx) {
        // TODO
        if (ctx.comparison() != null) {
            visit(ctx.comparison());
        } else {
            visit(ctx.expr());
        }
        return null;
    }

    //   | IDENTIFIER IN '(' literalList ')'
    @Override
    public Void visitComparison(FilterParser.ComparisonContext ctx) {
        // TODO
        String field = ctx.IDENTIFIER().getText();
        if (ctx.op != null) {
            visit(ctx.value);
            expressions.push(new Expr.Comparison(field, CompOp.fromSymbol(ctx.op.getText()), values.pop()));
        } else {
            visit(ctx.literalList());
            expressions.push(new Expr.InList(field, valueLists.pop()));
        }
        return null;
    }

    // literalList: literal (',' literal)*
    @Override
    public Void visitLiteralList(FilterParser.LiteralListContext ctx) {
        // TODO
        List<Value> list = new ArrayList<>();
        for (var literal : ctx.literal()) {
            visit(literal);
            list.add(values.pop());
        }
        valueLists.push(list);
        return null;
    }

    // literal: STRING | NUMBER
    @Override
    public Void visitLiteral(FilterParser.LiteralContext ctx) {
        // TODO
        if (ctx.STRING() != null) {
            values.push(new Value.Str(unquote(ctx.STRING().getText())));
        } else {
            values.push(new Value.Num(Integer.parseInt(ctx.NUMBER().getText())));
        }
        return null;
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
