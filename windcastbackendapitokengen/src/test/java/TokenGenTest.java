import com.feer.helperLib.TokenGen;
import com.feer.helperLib.scrts.scrts;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TokenGenTest {

    @Test
    public void TokenGen_gettingtoken_getsToken(){
        TokenGen.initialise(new scrts());
        String actual = TokenGen.GetToken();
        assertThat(actual, is("NOT_THE_SECRET_YOU_WERE_LOOKING_FOR"));
    }
}