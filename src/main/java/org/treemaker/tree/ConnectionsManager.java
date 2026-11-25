package org.treemaker.tree;

import java.util.ArrayList;
import java.util.HashMap;

import org.treemaker.phrases.Phrase;

public class ConnectionsManager {
    private ArrayList<Class<? extends Phrase>> list = new ArrayList<>();
    private HashMap<Class<? extends Phrase>, Integer> map = new HashMap<>();

    private int last = 1;

    /* How does this thing work:
    * Let's say you have to connect some nodes in this order: noun, noun, verb, noun, verb, noun
    * First call will checks if the list contains a Noun.class, since it doesn't it will add Noun.class to the list
    * Then it will put this Noun.class in the hashmap with 'last' as id 
    *   and set the node's connection id to 'last' and increment last
    * The second call, Noun.class will be in the list so it will remove it and set the node's connection id to the one
    *   that was memorized in the hashmap (the previous noun's id)
    * Then the verb will be inserted, followed by a second noun.
    * Then on the second verb we check if there is a verb, which is present, so we remove it and set connection ids
    * With the second noun the process repeats
    * 
    * This code can only connect succequent nouns, it is so unable to represent situations like: noun-1 noun-2 noun-2 noun-1
    * They will be connected following the order they are inserted and not the expected one
    */

    /**
     * Adds a movement to node n of type p
     * @param n to add
     * @param p class of Node n
     * 
     * BREAKS if more then one connection of the same type are parsed at the same time
     * Don't even know if it's possible to have this edge case tho.
     * You are never too careful.
     */
    public void add(Node n, Class<? extends Phrase> p) {
        if(!list.contains(p)){
            list.add(p);
            map.put(p, last);
            n.setConnectionID(last);
            last++;
        }
        else{
            list.remove(p);
            n.setConnectionID(map.get(p));
        }
    }
}
