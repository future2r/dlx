package name.ulbricht.dlx.ui.view.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.Directive;

@DisplayName("DirectiveCategory")
final class DirectiveCategoryTest {

    @Nested
    @DisplayName("completeness")
    final class Completeness {

        @Test
        @DisplayName("every directive belongs to exactly one category")
        void everyDirectiveCovered() {
            final var covered = EnumSet.noneOf(Directive.class);
            for (final var category : DirectiveCategory.values()) {
                for (final var directive : category.directives) {
                    assertFalse(covered.contains(directive),
                            "Directive " + directive + " appears in multiple categories");
                    covered.add(directive);
                }
            }
            assertEquals(EnumSet.allOf(Directive.class), covered,
                    "Not all directives are covered by categories");
        }

        @Test
        @DisplayName("no category is empty")
        void noCategoryEmpty() {
            for (final var category : DirectiveCategory.values()) {
                assertFalse(category.directives.isEmpty(),
                        "Category " + category + " is empty");
            }
        }
    }

    @Nested
    @DisplayName("categoryOf")
    final class CategoryOf {

        @Test
        @DisplayName("returns correct category for every directive")
        void returnsCorrectCategory() {
            for (final var category : DirectiveCategory.values()) {
                for (final var directive : category.directives) {
                    final var result = DirectiveCategory.categoryOf(directive);
                    assertNotNull(result);
                    assertEquals(category, result);
                }
            }
        }
    }
}
