package com.feer.windcast.testUtils;

/**
 * Created by Reef on 22/02/2015.
 */

import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class StrictMock
{
    private StrictMock(){}

    public static <T> T create(Class<T> cls) {
        return Mockito.mock(cls, new StrictMockHandler());
    }
    
    private static class StrictMockHandler implements Answer
    {
        public boolean strict = false;
        public Object answer(InvocationOnMock invocation) throws Throwable {
            if (strict) throw new NotStubbedException(invocation.getMethod().getName());
            return null;
        }
    }
    
    public static class NotStubbedException extends Exception {
        public NotStubbedException(String message){
            super(message);
        }
    }
    
    /// Verify that no unstubbed methods
    public static void verifyNoUnstubbedInteractions(Object mock) {
        StrictMockHandler handler = ((StrictMockHandler) new MockUtil().getMockHandler(mock)
                .getMockSettings().getDefaultAnswer());
        handler.strict = true;
    }
}




