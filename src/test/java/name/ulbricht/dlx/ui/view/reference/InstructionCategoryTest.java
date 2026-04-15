package name.ulbricht.dlx.ui.view.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.Instruction;

@DisplayName("InstructionCategory")
final class InstructionCategoryTest {

    @Nested
    @DisplayName("completeness")
    final class Completeness {

        @Test
        @DisplayName("every instruction belongs to exactly one category")
        void everyInstructionCovered() {
            final var covered = EnumSet.noneOf(Instruction.class);
            for (final var category : InstructionCategory.values()) {
                for (final var instruction : category.instructions()) {
                    assertFalse(covered.contains(instruction),
                            "Instruction " + instruction + " appears in multiple categories");
                    covered.add(instruction);
                }
            }
            assertEquals(EnumSet.allOf(Instruction.class), covered,
                    "Not all instructions are covered by categories");
        }

        @Test
        @DisplayName("no category is empty")
        void noCategoryEmpty() {
            for (final var category : InstructionCategory.values()) {
                assertFalse(category.instructions().isEmpty(),
                        "Category " + category + " is empty");
            }
        }
    }

    @Nested
    @DisplayName("categoryOf")
    final class CategoryOf {

        @Test
        @DisplayName("returns correct category for every instruction")
        void returnsCorrectCategory() {
            for (final var category : InstructionCategory.values()) {
                for (final var instruction : category.instructions()) {
                    final var result = InstructionCategory.categoryOf(instruction);
                    assertNotNull(result);
                    assertEquals(category, result);
                }
            }
        }
    }
}
