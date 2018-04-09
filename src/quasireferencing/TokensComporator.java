/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasireferencing;

import java.util.Comparator;

/**
 *
 * @author Илья
 */
public class TokensComporator implements Comparator<Token> {

    @Override
    public int compare(Token o1, Token o2) {
        if (o1.equals(o2)) return 0;
        if (o1.GetCount() > o2.GetCount()){
            return -1;
        }
        if (o1.GetCount() < o2.GetCount()){
            return 1;
        }
        return 0;
    }
}
