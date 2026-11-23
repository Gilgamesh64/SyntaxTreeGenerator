package org.treemaker.phrases;

public class UnknownPhraseException extends Exception {

    private final String unknownPhrase;

    public UnknownPhraseException(String unknownPhrase) {
        super("Unknown phrase: " + unknownPhrase);
        this.unknownPhrase = unknownPhrase;
    }

    public String getUnknownPhrase() {
        return unknownPhrase;
    }
}
