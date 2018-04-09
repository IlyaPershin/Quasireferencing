package quasireferencing;

import java.awt.FileDialog;
import java.awt.Frame;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Quasireferencing {

    private static final Pattern PERFECTIVEGROUND = Pattern.compile("((ив|ивши|ившись|ыв|ывши|ывшись)|((&lt;=[ая])(в|вши|вшись)))$");
    private static final Pattern REFLEXIVE = Pattern.compile("(с[яь])$");
    private static final Pattern ADJECTIVE = Pattern.compile("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$");
    private static final Pattern PARTICIPLE = Pattern.compile("((ивш|ывш|ующ)|((?<=[ая])(ем|нн|вш|ющ|щ)))$");
    private static final Pattern VERB = Pattern.compile("((ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю)|((?<=[ая])(ла|на|ете|йте|ли|й|л|ем|н|ло|но|ет|ют|ны|ть|ешь|нно)))$");
    private static final Pattern NOUN = Pattern.compile("(а|ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$");
    private static final Pattern RVRE = Pattern.compile("^(.*?[аеиоуыэюя])(.*)$");
    private static final Pattern DERIVATIONAL = Pattern.compile(".*[^аеиоуыэюя]+[аеиоуыэюя].*ость?$");
    private static final Pattern DER = Pattern.compile("ость?$");
    private static final Pattern SUPERLATIVE = Pattern.compile("(ейше|ейш)$");
    private static final Pattern I = Pattern.compile("и$");
    private static final Pattern P = Pattern.compile("ь$");
    private static final Pattern NN = Pattern.compile("нн$");
    
    
    // Автор Першин Илья ПИ-15-1
    public static void main(String[] args) throws IOException {
        
        ArrayList<String> stopWords = readFromFile("List.txt");
        String regex = "(-? ?[A-ZА-Я](([A-ZА-Я][.])|([А-Я]?[а-яa-z\\d\\s\\-\\,\\:]))+[.!?:,])";
        
        String text = "";
        ArrayList<String> lines = readFromFile("MyText.txt");
        text = lines.stream().map((str) -> str).reduce(text, String::concat);
        ArrayList<String> sentences = Pars(regex, text);
        
        regex = "(\\b[А-ЯA-Z](([.][А-ЯA-Z][.][А-ЯA-Z][а-яa-z]*)|([а-яa-z]+))\\b)|(\\b[а-яa-z]*\\b)";
        ArrayList<Token> tokens = ParsAndStem(regex, text, stopWords);
        Collections.sort(tokens, new TokensComporator());

        int countOfWords = (int)(tokens.size()/3);
        {
            ArrayList<Token> tokens2 = new ArrayList();
            for (int  i = 0; i < countOfWords; i++){
                tokens2.add(tokens.get(i + countOfWords));
            }
            tokens = tokens2;
        }

        Scanner in = new Scanner(System.in);
        System.out.print("Процент сжатия текста (0..100): ");
        float procentes = (float)in.nextInt() / 100;
        
        ArrayList<Token> sent = FindTheValueOfSentence(sentences, tokens);
        {
            ArrayList<Token> sent2 = new ArrayList(sent);
            Collections.sort(sent2, new TokensComporator());
            int count = (int)((float) sent2.size() * procentes);
            ArrayList<Token> sent3 = new ArrayList();
            for (int i = 0; i < count && i < sent.size(); i++){
                sent3.add(sent.get(sent.indexOf(sent2.get(i))));
                sent3.get(i).SetCount(sentences.indexOf(sent3.get(i).GetString()));
            }
            sent = new ArrayList(sent3);
            Collections.sort(sent, new TokensComporator());
        }

        Collections.reverse(sent);
        for (int i = 0; i < sent.size(); i++){
            System.out.println(sent.get(i).GetString());
        }
    
    }
    
    public static ArrayList<String> Pars(String regex, String text){
        ArrayList<String> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()){
            String sentence = matcher.group();
            sentences.add(sentence);
        }
        return sentences;
    }
    
    public static ArrayList<Token> ParsAndStem(String regex, String text, ArrayList<String> stopWords){
        ArrayList<String> usedStopWords = new ArrayList<>();
        ArrayList<Token> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()){
            String word = matcher.group();
            int i = stopWords.indexOf(word);
            
            if (i == -1 && !word.isEmpty()){
                word = stem(word);
                int j = usedStopWords.indexOf(word);
                
                if (j == -1){
                    sentences.add(new Token(word));
                    sentences.get(sentences.size()-1).IncrementTokenCount();
                    usedStopWords.add(word);
                }
                else{
                    sentences.get(j).IncrementTokenCount();
                }
            }
        }
        return sentences;
    }

    public static String stem(String word) {
        word = word.toLowerCase();
        word = word.replace('ё', 'е');
        Matcher m = RVRE.matcher(word);
        if (m.matches()) {
            String pre = m.group(1);
            String rv = m.group(2);
            String temp = PERFECTIVEGROUND.matcher(rv).replaceFirst("");
            if (temp.equals(rv)) {
                rv = REFLEXIVE.matcher(rv).replaceFirst("");
                temp = ADJECTIVE.matcher(rv).replaceFirst("");
                if (!temp.equals(rv)) {
                    rv = temp;
                    rv = PARTICIPLE.matcher(rv).replaceFirst("");
                } else {
                    temp = VERB.matcher(rv).replaceFirst("");
                    if (temp.equals(rv)) {
                        rv = NOUN.matcher(rv).replaceFirst("");
                    } else {
                        rv = temp;
                    }
                }

            } else {
                rv = temp;
            }

            rv = I.matcher(rv).replaceFirst("");

            if (DERIVATIONAL.matcher(rv).matches()) {
                rv = DER.matcher(rv).replaceFirst("");
            }

            temp = P.matcher(rv).replaceFirst("");
            if (temp.equals(rv)) {
                rv = SUPERLATIVE.matcher(rv).replaceFirst("");
                rv = NN.matcher(rv).replaceFirst("н");
            }else{
                rv = temp;
            }
            word = pre + rv;

        }

        return word;
    }
    
    public static ArrayList<String> readFromFile(String filePath){
        Path path = Paths.get(filePath);
        List<String> list = new ArrayList<>();
        Charset charset = Charset.forName("UTF-8");
        try {
            list = Files.readAllLines(path, charset);
        } 
        catch (IOException e) {
        }
        return (new ArrayList<>(list));
    }
    
    public static ArrayList<Token> FindTheValueOfSentence(ArrayList<String> sent, ArrayList<Token> tokens){
        ArrayList<Token> sentences = new ArrayList();
        for (int i = 0; i < sent.size(); i++){
            String s = sent.get(i);
            sentences.add(new Token(s));
            float count = 0;
            for (int j = 0; j < tokens.size(); j++){
                if (s.toLowerCase().indexOf(tokens.get(j).GetString()) != -1){
                    count++;
                }
            }
            sentences.get(sentences.size()-1).SetCount(count / (s.split(" ").length));
        }
        
        return sentences;
    }
}