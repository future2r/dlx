package name.ulbricht.dlx.asm.parser;

import name.ulbricht.dlx.util.TextPosition;

/// A common supertype for all parsed elements (data declarations
/// and instructions).
public sealed interface ParsedElement permits ParsedDataDeclaration, ParsedInstruction {

    /// {@return the source position of the element's defining token}
    TextPosition pos();
}
