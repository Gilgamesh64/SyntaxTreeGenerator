package org.treemaker.phrases;

public class Phrase {
    protected String form;
    protected String lemma;

    Phrase(String form, String lemma, String features){
        this.form = form;
        this.lemma = lemma;
    }

    public static Phrase from(String form, String lemma, String features) throws UnknownPhraseException{
        String category = "";
        String regex = "[A-Z]+";
        if(features.matches(regex)){
            category = features;
        }
        else{
            String withColon = features.split(":", -1)[0];
            String withDash = features.split("-", -1)[0];
            if(withColon.matches(regex)){
                category = withColon;
            }
            else if(withDash.matches(regex)){
                category = withDash;
            }
        }
        String restOf = features.replace(category, "");
        if(!restOf.isEmpty()) restOf = restOf.substring(1);
        return switch (category){
            case "VER" -> new Verb(form, lemma, restOf);
            case "NOUN", "NPR"-> new Noun(form, lemma, restOf);
            case "ART" -> new Article(form, lemma, restOf, false);
            case "ARTPRE" -> new Article(form, lemma, restOf, true);
            case "ADJ" -> new Adjective(form, lemma, restOf);
            case "PRO" -> new Pronoun(form, lemma, restOf);
            default -> throw new UnknownPhraseException(form);
        };
    }

    public String getForm() { return form; }
    public String getLemma() { return lemma; }

    @Override
    public String toString() {
        return "Form: " + form + ", Lemma: " + lemma + ", ";
    }
}