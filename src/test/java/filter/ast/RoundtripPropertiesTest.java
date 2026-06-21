package filter.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.printer.AstPrinter;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

public class RoundtripPropertiesTest {

    // TODO : Formulieren Sie verschiedene Eigenschaften für die AST-Builder.Beispielsweise sollte ein "Roundtrip" für jeweils beide Builder (individuell, aber auch gegenseitig verschränkt) möglich sein: Query -> Parsing -> AST -> Pretty-Print -> Parsing -> AST. Sie könnten mit einer festen, selbstdefinierten Query starten oder als Parameter für einen Property-Test mit jqwik die in der Klasse filter.ast.RoundtripPropertiesTest vordefinierten Arbitraries nutzen: @Property boolean foo(@ForAll("simpleQueries") String query) {...}.Treffen Sie zusätzlich Annahmen über Expressions und Values, beispielsweise über die logischen Eigenschaften der AND-Verknüpfung.Implementieren Sie diese Property Tests in der Klasse filter.ast.RoundtripPropertiesTest.

    @Property
    boolean roundtripPattern(@ForAll("simpleQueries") String query) {
        var ast1 = new AstBuilderPattern().translate(AstBuilders.parse(query));
        String printed = AstPrinter.toString(ast1);
        var ast2 = new AstBuilderPattern().translate(AstBuilders.parse(printed));
        return ast1.equals(ast2);
    }

    @Property
    boolean roundtripVisitor(@ForAll("simpleQueries") String query) {
        var ast1 = new AstBuilderVisitor().translate(AstBuilders.parse(query));
        String printed = AstPrinter.toString(ast1);
        var ast2 = new AstBuilderVisitor().translate(AstBuilders.parse(printed));
        return ast1.equals(ast2);
    }

    @Property
    boolean crossRoundtrip(@ForAll("simpleQueries") String query) {
        var ast1 = new AstBuilderPattern().translate(AstBuilders.parse(query));
        String printed = AstPrinter.toString(ast1);
        var ast2 = new AstBuilderVisitor().translate(AstBuilders.parse(printed));
        return ast1.equals(ast2);
    }


    // ---------- @Provide-Methods for Arbitraries ----------

    @Provide
    Arbitrary<String> fields() {
        return Arbitraries.of("title", "artist", "genre", "year");
    }

    @Provide
    Arbitrary<String> stringLiterals() {
        return Arbitraries.strings()
            .withChars("abcxyz")
            .ofMinLength(1)
            .ofMaxLength(5)
            .map(s -> "\"" + s + "\"");
    }

    @Provide
    Arbitrary<String> numberLiterals() {
        return Arbitraries.integers().between(1900, 2025).map(Object::toString);
    }

    @Provide
    Arbitrary<String> comparisons() {
        Arbitrary<String> ops = Arbitraries.of("==", "!=", "<", "<=", ">", ">=");

        Arbitrary<String> stringComp =
            Combinators.combine(fields(), ops, stringLiterals())
                .as((f, op, lit) -> f + " " + op + " " + lit);

        Arbitrary<String> numberComp =
            Combinators.combine(Arbitraries.of("year"), ops, numberLiterals())
                .as((f, op, lit) -> f + " " + op + " " + lit);

        return Arbitraries.oneOf(stringComp, numberComp);
    }

    @Provide
    Arbitrary<String> simpleQueries() {
        return comparisons()
            .list()
            .ofMinSize(1)
            .ofMaxSize(3)
            .map(
                list -> {
                    if (list.size() == 1) return list.getFirst();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) {
                            String conn = Arbitraries.of(" and ", " or ").sample();
                            sb.append(conn);
                        }
                        sb.append(list.get(i));
                    }
                    return sb.toString();
                });
    }

}
