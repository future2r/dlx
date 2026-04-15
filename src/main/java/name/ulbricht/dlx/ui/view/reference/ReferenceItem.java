package name.ulbricht.dlx.ui.view.reference;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.asm.Directive;
import name.ulbricht.dlx.asm.Instruction;
import name.ulbricht.dlx.ui.i18n.Messages;

/// Represents a single node in the reference tree view.
///
/// This sealed interface models the three levels of the tree hierarchy:
/// top-level categories, subcategory groups, and individual leaf entries.
sealed interface ReferenceItem {

    /// {@return the display label for this tree node}
    String label();

    /// A top-level category node (e.g. "Instructions", "Directives", "Registers").
    ///
    /// @param resourceKey the resource bundle key for the category label
    record TopCategory(String resourceKey) implements ReferenceItem {

        /// Creates a new top-level category.
        public TopCategory {
            requireNonNull(resourceKey);
        }

        @Override
        public String label() {
            return Messages.getString(this.resourceKey);
        }
    }

    /// A subcategory group of instructions (e.g. "Arithmetic", "Logic").
    ///
    /// @param category the instruction category
    record InstructionGroup(InstructionCategory category) implements ReferenceItem {

        /// Creates a new instruction group.
        public InstructionGroup {
            requireNonNull(category);
        }

        @Override
        public String label() {
            return Messages.getString("reference.instruction.category."
                    + this.category.name().toLowerCase());
        }
    }

    /// A subcategory group of directives (e.g. "Segments", "Data Declarations").
    ///
    /// @param category the directive category
    record DirectiveGroup(DirectiveCategory category) implements ReferenceItem {

        /// Creates a new directive group.
        public DirectiveGroup {
            requireNonNull(category);
        }

        @Override
        public String label() {
            return Messages.getString("reference.directive.category."
                    + this.category.name().toLowerCase());
        }
    }

    /// A leaf node representing a single instruction.
    ///
    /// @param instruction the instruction
    record InstructionEntry(Instruction instruction) implements ReferenceItem {

        /// Creates a new instruction entry.
        public InstructionEntry {
            requireNonNull(instruction);
        }

        @Override
        public String label() {
            return this.instruction.mnemonic();
        }
    }

    /// A leaf node representing a single directive.
    ///
    /// @param directive the directive
    record DirectiveEntry(Directive directive) implements ReferenceItem {

        /// Creates a new directive entry.
        public DirectiveEntry {
            requireNonNull(directive);
        }

        @Override
        public String label() {
            return "." + this.directive.directiveName();
        }
    }

    /// A leaf node representing a register group.
    ///
    /// @param register the register convention
    record RegisterEntry(RegisterConvention register) implements ReferenceItem {

        /// Creates a new register entry.
        public RegisterEntry {
            requireNonNull(register);
        }

        @Override
        public String label() {
            return this.register.displayName();
        }
    }
}
