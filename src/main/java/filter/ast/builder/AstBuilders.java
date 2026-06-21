package filter.ast.builder;

import filter.FilterLexer;
import filter.FilterParser;
import filter.ast.nodes.Expr;
import java.util.function.Function;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AstBuilders {

    public static Expr fromQuery(String query, Function<FilterParser.QueryContext, Expr> translator) {
        return simplify(translator.apply(parse(query)));
    }

    public static Expr simplify(Expr e) {
        return switch (e) {
            case Expr.Not(Expr.Not(var inner)) -> simplify(inner); // Doppelte Negation eliminieren
            case Expr.And(var l, var r) -> new Expr.And(simplify(l), simplify(r)); // And-Ausdrücke vereinfachen
            case Expr.Or(var l, var r) -> new Expr.Or(simplify(l), simplify(r)); // Or-Ausdrücke vereinfachen
            case Expr.Not(var inner) -> new Expr.Not(simplify(inner)); // Not-Ausdrücke vereinfachen
            case Expr.Comparison(var field, var op, var value) -> e; // Vergleichsausdrücke bleiben unverändert
            case Expr.InList(var field, var values) -> e; // In-List-Ausdrücke bleiben unverändert
        };
    }

    public static FilterParser.QueryContext parse(String query) {
        var cs = CharStreams.fromString(query);
        var lexer = new FilterLexer(cs);
        var tokens = new CommonTokenStream(lexer);
        var parser = new FilterParser(tokens);

        var ctx = parser.query();
        if (parser.getNumberOfSyntaxErrors() > 0)
            throw new IllegalStateException("Syntax errors in query: " + query);

        return ctx;
    }
}
