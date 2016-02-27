package com.feer.helperLib;

import com.feer.helperLib.scrts.impl.scrtsImpl;
import com.feer.helperLib.scrts.scrts;

public class TokenGen {
    private static scrts mScrt = new scrtsImpl();
    public static void initialise(scrts scrt){
        mScrt = scrt;
    }

    public static String GetToken() {
        return mScrt.getScrt();
    }


}
