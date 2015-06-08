package com.beeva.beenote;

/**
 * Created by marianclaudiu on 5/06/15.
 */
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.beeva.beenote.models.MetaNoteMessage;
import com.beeva.beenote.models.MetaNoteResponse;

public interface NoteProxy {

    @LambdaFunction(functionName = "TestUbiq")
    MetaNoteResponse invoke(MetaNoteMessage message);

}