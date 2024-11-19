/**
 * I, Julia Hutchison, have neither given nor received unauthorized aid on this program.
 */
import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.*;
import java.lang.Math;

public class SpamDetection {
    static Double numSpam = 0.0;
    static Double numHam = 0.0;
    static Map<String,Double> SpamMap;
    static Map<String,Double> HamMap;
    static Set<String> Vocab;
    static int totalCorrect =0;
    static int total = 0;

    public static void main(String[] args) {
        Vocab = new HashSet<>();
        SpamMap = new HashMap<>();
        HamMap = new HashMap<>();
        int i =0;
        Scanner scan = new Scanner(System.in);

        //Asking for training debug info
        System.out.println("Would you like training info? (y: yes n:no) ");
        String info= scan.nextLine();

        //1) training phase
        while(i<2){
            System.out.println("Enter Training filename: ");
            String fileName = scan.nextLine();
            processTrainingFile(fileName);
            i++;
        }
        if(info.contains("y") || info.contains("Y")) printTest();

        //2.) testing phase
        System.out.println("Beginning tests... \n");
        System.out.println("Enter Spam Test filename: ");
        String spamTestFileName = scan.nextLine();
        System.out.println("Enter Ham Test filename: ");
        String hamTestFileName = scan.nextLine();

        processTestFile(spamTestFileName);
        processTestFile(hamTestFileName);

        System.out.printf("Total: %d/%d emails classified correctly", totalCorrect, total );

    }

    /*
    Will process the test files and will classify
     each email by using MAP -> uses a helper funciton to help classify
     */
    public static void processTestFile(String FileName){
        InputStream is = SpamDetection.class.getResourceAsStream(FileName);
        if(is ==null){
            System.err.println("Bad file name:" + FileName);
            System.exit(1);
        }

        //The set contains all the words in a single email. Gets reset with every new email
        Set<String> ThisEmailWordSet = new HashSet<>();
        int EmailCount = 0;
        String eType = FileName.contains("spam")? "spam": "ham";

        Scanner scan = new Scanner(is);
        while(scan.hasNextLine()){
            String line = scan.nextLine();

            //we have reached the end of an email so let us run the calcultions
            if(line.contains("</BODY>")){
                EmailCount ++;
                classify(ThisEmailWordSet, EmailCount, eType);
                ThisEmailWordSet.clear();
                continue;
            }
            //Skip over non-important HTML Tags
            if(line.contains("<") || line.contains(">")) continue;

            //Reached actual text info so lets collect it into our email set
            String[] words = line.split(" ");
            for(String word: words){
                if(word.equals(" ") ||word.equals("")) {continue;}
                if(Vocab.contains(word.toLowerCase())) {ThisEmailWordSet.add(word.toLowerCase());}

            }

        }
    }
    /*
    Helper function that will classify the email and does the necessary calculations.
    For the calculations we are getting the probs of every word from the training
    Set Set: words in a specifc email
    int count: test num we are on
    string type: the type of email we are processing based on file
     */
    public static void classify( Set<String> set, int count, String eType){
        total++;
        //Starting off with our posterior vals
        Double Spam = Math.log(numSpam / (numHam+numSpam));
        Double Ham  = Math.log(  (numHam) / (numHam + numSpam));

        for(String word: Vocab){
            if(set.contains(word)){
                Spam+= Math.log((SpamMap.get(word) +1) / (numSpam+2));
                Ham+= Math.log((HamMap.get(word)+1) / (numHam+2));
            }
            else{ //not in the set so we need to negate it
                Spam += Math.log( ((numSpam+2)-(SpamMap.get(word) +1)) / (numSpam+2));
                Ham +=  Math.log( ((numHam+2)- (HamMap.get(word)+1))   /(numHam+2) );
            }
        }
        //Double prob = (Spam>Ham)? Spam: Ham;
        String classified = (Spam>Ham)? "spam": "ham";
        String desicion = (classified.equals(eType))? "right":"wrong";
        totalCorrect += (classified.equals(eType))? 1:0;

        //Printing out detection info of the email
        System.out.printf("TEST %d %d/%d features true %.3f %.3f %s %s \n",
                            count, set.size(), Vocab.size(),Spam,Ham, classified, desicion );

    }


    /*
    process the training file. Add words to the Vocab set and add words to the spam and Ham maps
    It will count how many times a word occurs per email.
     */
    public static void processTrainingFile(String FileName){
        InputStream is = SpamDetection.class.getResourceAsStream(FileName);
        if(is ==null){
            System.err.println("Bad file name:" + FileName);
            System.exit(1);
        }
        Boolean isSpam = true;
        if(FileName.contains("ham")){
            isSpam = false;
        }
        Set<String> emailWordSet = new HashSet<>();

        Scanner scan = new Scanner(is);
        while(scan.hasNextLine()){
            String line = scan.nextLine();

            //Reached the end of an email so let's compute the data
            if(line.contains("</BODY>")){
                //run through the set of words collected in this email and add it to the spam/ham map info
                for(String word: emailWordSet){
                    if(isSpam){
                        SpamMap.compute(word, (k,v)->v+=1);
                    }
                    else{
                        HamMap.compute(word, (k,v)->v +=1);
                    }
                }

                emailWordSet.clear();
                if(isSpam) numSpam++;
                else numHam++;
                continue;
            }
            //Skip over other HTML tags
            if(line.contains("<") && line.contains(">")) continue;

            //for any other line lets collect the words and add it to maps and sets
            String[] words = line.split(" ");
            for(String word: words){
                if(word.equals(" ") ||word.equals("")  ) continue;
                Vocab.add(word.toLowerCase());
                emailWordSet.add(word.toLowerCase()); // <- collecting words in a single email
                SpamMap.putIfAbsent(word.toLowerCase(), (double) 0); // <- here we are just making sure it is present in the maps
                HamMap.putIfAbsent(word.toLowerCase(), (double) 0);
            }

        }

    }

    /*
    prints test info if needed
     */
    public static void printTest(){
        System.out.printf("Num of spam: %,.2f | ham: %,.2f\n", numSpam, numHam);
        System.out.printf(" entire vocab: %s \n entire vocab size: %d \n", Vocab.toString(), Vocab.size());

        System.out.printf("Spam words: ");
        SpamMap.forEach((k,v) -> System.out.printf("'%s': %.1f ", k,v));
        System.out.println("");
        System.out.printf("Ham words: ");
        HamMap.forEach((k,v) -> System.out.printf("'%s': %.1f ", k,v));
        System.out.println("");

    }
}