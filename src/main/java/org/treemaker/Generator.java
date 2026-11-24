package org.treemaker;

import org.sqlite.SQLiteConfig;
import org.treemaker.parsing.AmbiguousSentence;
import org.treemaker.phrases.Phrase;
import org.treemaker.phrases.UnknownPhraseException;
import org.treemaker.parsing.Sentence;
import org.treemaker.tree.Tree;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Generator {

    public static String getTree(String input) throws SQLException, UnknownPhraseException {
        String output = "";

        String url = "jdbc:sqlite:dizionario.db";
        String[] words = input.split(" ", -1);

        SQLiteConfig config = new SQLiteConfig();

        config.setReadOnly(true);

        Connection conn = DriverManager.getConnection(url, config.toProperties());
        Statement st = conn.createStatement();
        var pstmt = conn.prepareStatement("SELECT * FROM words WHERE form = ?");

        AmbiguousSentence as = new AmbiguousSentence();

        for (String key : words) {
            boolean found = false;
            int tries = 3;

            for (int i = 0; i < tries && !found; i++) {
                String word = key;  //first try: base string
                if(i == 1){ //second try: to lower (checks auto maiusc on first letter on mobile) 
                    word = key.toLowerCase(); 
                } 
                if(i == 2){  //last try: capitalize first letter (checks names)
                    word = key.substring(0, 1).toUpperCase() + key.substring(1);   
                }
                pstmt.setString(1, word);

                var rs = pstmt.executeQuery();

                while (rs.next()) {
                    found = true;
                    String form = rs.getString("form");
                    String lemma = rs.getString("lemma");
                    String features = rs.getString("features");
                    Phrase p = Phrase.from(form, lemma, features);
                    as.add(p);
                }
                rs.close();
            }

            if (!found) {
                throw new UnknownPhraseException("The sentence is either empty or invalid");
            }
        }
        Sentence s = as.solve();

        Tree tree = new Tree(s);
        output = tree.toJson();

        st.close();
        pstmt.close();
        conn.close();
        return output;
    }
}
