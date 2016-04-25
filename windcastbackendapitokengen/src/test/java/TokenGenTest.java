import com.feer.helperLib.TokenGen;
import com.feer.helperLib.scrts.scrts;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TokenGenTest {

    // Fragile test to notify if anything changes with the token generation.
    @Test
    public void TokenGen_gettingToken_getsToken(){
        TokenGen.initialise(new scrts());
        Date d = new Date(2005, 03, 28);
        UUID id = new UUID(0, 1);
        String actual = TokenGen.GetToken("TEST Message", d, id);
        String expected = "1jITWaejaY4nfC4GWCqpXrV3VN3N3NoXz9lhAgakidM=";
        assertThat(actual, is(expected));
    }
}