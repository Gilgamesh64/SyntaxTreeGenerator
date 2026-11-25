package org.treemaker.tree;

import java.util.HashMap;
import org.treemaker.parsing.Sentence;
import org.treemaker.phrases.Article;
import org.treemaker.phrases.Noun;
import org.treemaker.phrases.UnknownPhraseException;
import org.treemaker.phrases.Verb;

public class Tree {

    protected Node root;
    private Sentence sentence;
    private ConnectionsManager cm;

    public Tree(Sentence sentence) throws UnknownPhraseException{
        this.sentence = sentence;
        cm = new ConnectionsManager();

        if(sentence.contains(Verb.class)){
            root = new Node("TP");
            writeTP(root);
        }
        else if(sentence.contains(Noun.class)){
            root = new Node("DP");
            writeDP(root);
        }
        checkConnections();
    }

    private void writeTP(Node tp){
        tp.setLeft(new Node("DP"));
        writeDP(tp.getLeft());

        Node t = new Node("T'");
        tp.setRight(t);
        t.setLeft(new Node("T", sentence.getFirst(Verb.class).get()));
        
        cm.add(t.getLeft(), Verb.class);
        
        t.setRight(new Node("VP"));
        writeVP(t.getRight());
    }

    private void writeVP(Node vp){
        if(sentence.contains(Noun.class)){
            vp.setLeft(new Node("DP"));
            writeDP(vp.getLeft());
        }
        Node v = new Node("V'");
        vp.setRight(v);
        Node verb = new Node("V", sentence.getFirst(Verb.class).get());
        v.setLeft(verb);
        cm.add(v.getLeft(), Verb.class);
        
    }

    private void writeNP(Node np){
        Node n = new Node("N'");
        np.setRight(n);
        Node noun = new Node("N", sentence.getFirst(Noun.class).get());
        n.setLeft(noun);
        if(!sentence.contains(Article.class))
            cm.add(noun, Noun.class);
    }

    private void writeDP(Node dp){
        if(sentence.contains(Article.class) && sentence.contains(Verb.class)){
            cm.add(dp, Noun.class);
        }

        Node a = new Node("D'");
        dp.setRight(a);
        if(sentence.contains(Article.class)){
            Node article = new Node("D", sentence.getFirst(Article.class).get());
            a.setLeft(article);
        }
        else{
            Node duplication = new Node("D", sentence.getFirst(Noun.class).get());
            a.setLeft(duplication);
            cm.add(duplication, Noun.class);
        }

        if(sentence.contains(Noun.class)){
            a.setRight(new Node("NP"));
            writeNP(a.getRight());
        }
    }

    private void checkConnections() throws UnknownPhraseException{
        HashMap<Integer, Integer> connectionsFound = new HashMap<>();
        checkConnectionsRec(root, connectionsFound);
        long errors = connectionsFound.values().stream().filter(v -> v != 2).count();
        if(errors != 0){
            throw new UnknownPhraseException("There is an error creating connections: " + connectionsFound.toString());
        }
    }
    private void checkConnectionsRec(Node curr, HashMap<Integer, Integer> connectionsFound){
        if(curr == null) return; 

        if(curr.getConnectionID() != -1) {
            connectionsFound.put(
                curr.getConnectionID(), 
                connectionsFound.getOrDefault(curr.getConnectionID(), 0) + 1);
        }
        checkConnectionsRec(curr.getLeft(), connectionsFound);
        checkConnectionsRec(curr.getRight(), connectionsFound);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printTree(root, sb, "", true);
        return sb.toString();
    }

    private void printTree(Node node, StringBuilder sb, String prefix, boolean isTail) {
        if (node == null)
            return;

        if (node.getRight() != null) {
            printTree(node.getRight(), sb,
                    prefix + (isTail ? "|   " : "    "), false);
        }

        sb.append(prefix)
                .append(isTail ? "|--" : "|-- ")
                .append(node)
                .append('\n');

        if (node.getLeft() != null) {
            printTree(node.getLeft(), sb,
                    prefix + (isTail ? "    " : "|   "), true);
        }
    }

    public String toJson() {
        return nodeToJson(root);
    }

    private String nodeToJson(Node node) {
        if (node == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"value\":\"").append(escapeJson(String.valueOf(node.value))).append("\",");
        
        if(node.phrase != null) sb.append("\"word\":\"").append(escapeJson(node.phrase.getForm())).append("\",");
        if(node.connectionID != 0) sb.append("\"connection\":").append(node.getConnectionID()+"").append(",");
        
        sb.append("\"left\":").append(nodeToJson(node.left)).append(",");
        sb.append("\"right\":").append(nodeToJson(node.right)).append("}");

        return sb.toString();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}