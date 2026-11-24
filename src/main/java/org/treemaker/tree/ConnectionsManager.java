package org.treemaker.tree;

import java.util.ArrayList;
import java.util.HashMap;

import org.treemaker.phrases.Phrase;

public class ConnectionsManager {
    private ArrayList<Class<? extends Phrase>> list = new ArrayList<>();
    private HashMap<Class<? extends Phrase>, Integer> map = new HashMap<>();

    private int last = 1;

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
