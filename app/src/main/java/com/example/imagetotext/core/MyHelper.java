package com.example.imagetotext.core;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MyHelper {
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    public static String getRandomString() {
        return getRandomString(15);
    }

    /**
     * Returns a randomly generated string of up to X characters.<br><br>
     *
     * @param numChars the number of characters long the random string should be
     *
     * @return A randomly generated string
     */
    public static String getRandomString(int numChars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numChars; i++) {
            int index = 97 + RAND.nextInt(26);
            char c = (char) index;
            sb.append(c);
        } // for
        return sb.toString();
    }

    public static int getValueInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Throwable throwable){
            return 0;
        }
    }
}
