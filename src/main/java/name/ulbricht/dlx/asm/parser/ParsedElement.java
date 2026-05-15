package name.ulbricht.dlx.asm.parser;

import java.util.UUID;

import name.ulbricht.dlx.util.TextPosition;

/// A common supertype for all parsed elements (data declarations
/// and instructions).
public sealed interface ParsedElement permits ParsedDataDeclaration, ParsedInstruction {

    /// {@return the source position of the element's defining token}
    TextPosition pos();

    /// {@return identifier of the source unit this element was parsed from;
    /// stamped onto every diagnostic the compiler produces against this
    /// element so cross-file error attribution survives the parse stage}
    UUID sourceId();
}
