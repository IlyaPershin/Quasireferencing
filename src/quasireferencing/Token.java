/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quasireferencing;

/**
 *
 * @author Илья
 */
public class Token {
    String string;
    float count;
    
    public Token(String _string){
        this.string = _string;
        this.count = 0;
    }
    
    public void IncrementTokenCount(){
        this.count++;
    }
    
    public float GetCount(){
        return this.count;
    }
    
    public void SetCount(float _count){
        this.count = _count;
    }
    
    public String GetString(){
        return this.string;
    }
}
