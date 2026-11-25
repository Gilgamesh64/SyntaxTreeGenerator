package org.treemaker.parsing;

import java.util.ArrayList;
import java.util.Optional;

import org.treemaker.phrases.Phrase;

public class Sentence{
    private ArrayList<Phrase> phrases = new ArrayList<>();

    public void add(Phrase phrase){ phrases.add(phrase); }

    public <T extends Phrase> int count(Class<T> phrase){
        int amount = 0;
        for (Phrase p : phrases) {
            if(phrase.isInstance(p))
                amount++;
        }
        return amount;
    }

    public <T extends Phrase> boolean contains(Class<T> phrase){
        return phrases.stream().anyMatch(phrase::isInstance);
    }

    public <T extends Phrase> ArrayList<T> getAll(Class<T> phrase){
        ArrayList<T> out = new ArrayList<>();
        for(Phrase p : phrases){
            if(phrase.isInstance(p)){
                out.add(phrase.cast(p));
            }
        }
        return out;
    }

    public <T extends Phrase> Optional<T> getFirst(Class<T> phrase){
        for(Phrase p : phrases){
            if(phrase.isInstance(p))
                return Optional.of(phrase.cast(p));
        }
        return Optional.empty();
    }

    
    public <T extends Phrase, K extends Phrase> Optional<T> getBefore(Class<T> before, Class<K> after){
        T inst = null;
        for(Phrase p : phrases){
            if(inst != null && after.isInstance(p)){
                return Optional.of(inst);
            }
            if(before.isInstance(p)){
                inst = before.cast(p);
            }
        }
        return Optional.empty();
    }

    public <T extends Phrase, K extends Phrase, J extends Phrase> Optional<K> getBetween(Class<T> before, Class<K> middle, Class<J> after){
        Optional<T> isBefore = getBefore(before, middle);
        Optional<K> isMiddle = getBefore(middle, after);

        if(isBefore.isEmpty() || isMiddle.isEmpty())
            return Optional.empty();

        return isMiddle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Phrase s : phrases) {
            sb.append(s.toString()).append("\n");
        }
        return sb.toString();
    }
}