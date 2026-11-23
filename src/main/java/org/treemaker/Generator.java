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
            pstmt.setString(1, key);

            var rs = pstmt.executeQuery();

            int count = 0;

            while (rs.next()) {
                String form = rs.getString("form");
                String lemma = rs.getString("lemma");
                String features = rs.getString("features");
                as.add(Phrase.from(form, lemma, features));
                count++;
            }
            rs.close();
            if(count == 0){
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
