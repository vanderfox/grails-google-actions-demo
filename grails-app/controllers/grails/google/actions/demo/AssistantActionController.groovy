package grails.google.actions.demo

import com.frogermcs.gactions.AssistantActions
import com.frogermcs.gactions.api.StandardIntents
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.ExpectedInputs
import com.frogermcs.gactions.api.response.ExpectedIntent
import com.frogermcs.gactions.api.response.InputPrompt
import com.frogermcs.gactions.api.response.RootResponse
import com.frogermcs.gactions.api.response.SpeechResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.vanderfox.gactions.ColorRequestHandlerFactory
import com.vanderfox.gactions.DarkerColorRequestHandlerFactory
import com.vanderfox.gactions.GrailsResponseHandler
import com.vanderfox.gactions.MainRequestHandlerFactory
import com.vanderfox.gactions.MyPermissionRequestHandlerFactory
import com.vanderfox.gactions.StarWarsQuizHandlerFactory
import com.vanderfox.gactions.TextRequestHandlerFactory
import org.apache.commons.io.IOUtils

import javax.servlet.http.HttpServletRequest

class AssistantActionController {

    def index() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory(StandardIntents.MAIN, new MainRequestHandlerFactory())
                        .addRequestHandlerFactory(StandardIntents.TEXT, new TextRequestHandlerFactory())
                        .addRequestHandlerFactory(StandardIntents.PERMISSION, new MyPermissionRequestHandlerFactory())
                        .addRequestHandlerFactory("color.intent", new ColorRequestHandlerFactory())
                        .addRequestHandlerFactory("color.darker.intent", new DarkerColorRequestHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        // log to file for debugging
        Writer writer = new FileWriter("/tmp/google-action-request-${System.currentTimeMillis()}-debug.json")
        Gson gson = new GsonBuilder().create()
        gson.toJson(rootRequest, writer)
        writer.flush()
        writer.close()
        assistantActions.handleRequest(rootRequest)
        null // we dont want to return a grails view here the handlers do this

    }

    private RootRequest parseActionRequest(HttpServletRequest request) throws IOException {
        JsonReader jsonReader = new JsonReader(request.getReader())
        return new Gson().fromJson(jsonReader, RootRequest.class)
    }

    def color() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory("color.intent", new ColorRequestHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        // log to file for debugging
        Writer writer = new FileWriter("/tmp/google-action-request-color-${System.currentTimeMillis()}-debug.json")
        Gson gson = new GsonBuilder().create()
        gson.toJson(rootRequest, writer)
        writer.flush()
        writer.close()
        assistantActions.handleRequest(rootRequest)
        null // we dont want to return a grails view here the handlers do this

    }
    def darkerColor() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory("color.darker.intent", new DarkerColorRequestHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        // log to file for debugging
        Writer writer = new FileWriter("/tmp/google-action-request-color-${System.currentTimeMillis()}-debug.json")
        Gson gson = new GsonBuilder().create()
        gson.toJson(rootRequest, writer)
        writer.flush()
        writer.close()
        assistantActions.handleRequest(rootRequest)
        null // we dont want to return a grails view here the handlers do this

    }


    def startStarWarsQuiz() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory("start.quiz.intent", new StarWarsQuizHandlerFactory())
                        .addRequestHandlerFactory(StandardIntents.TEXT, new StarWarsQuizHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        // log to file for debugging
        Writer writer = new FileWriter("/tmp/google-action-request-startstarwarzquiz-${System.currentTimeMillis()}-debug.json")
        Gson gson = new GsonBuilder().create()
        gson.toJson(rootRequest, writer)
        writer.flush()
        writer.close()
        assistantActions.handleRequest(rootRequest)
        null // we dont want to return a grails view here the handlers do this

    }

    def answerQuestion() {
        AssistantActions assistantActions =
                new AssistantActions.Builder(new GrailsResponseHandler(response))
                        .addRequestHandlerFactory(StandardIntents.TEXT, new StarWarsQuizHandlerFactory())
                        .build()
        RootRequest rootRequest = parseActionRequest(request)
        // log to file for debugging
        Writer writer = new FileWriter("/tmp/google-action-request-startstarwarzquiz-${System.currentTimeMillis()}-debug.json")
        Gson gson = new GsonBuilder().create()
        gson.toJson(rootRequest, writer)
        writer.flush()
        writer.close()
        assistantActions.handleRequest(rootRequest)
        null // we dont want to return a grails view here the handlers do this

    }



    private static RootResponse askResponse(String message, String conversationToken, String[] noInputPrompts, ExpectedIntent[] expectedIntents) {
        RootResponse rootResponse = new RootResponse();
        rootResponse.expect_user_response = true;
        rootResponse.conversation_token = conversationToken;
        rootResponse.expected_inputs = new ArrayList<>();

        ExpectedInputs expectedInput = new ExpectedInputs();
        expectedInput.input_prompt = new InputPrompt();
        expectedInput.input_prompt.initial_prompts = Collections.singletonList(new SpeechResponse(message, null));

        if (noInputPrompts != null && noInputPrompts.length > 0) {
            expectedInput.input_prompt.no_input_prompts = new ArrayList<>();
            for (String noInputPrompt : noInputPrompts) {
                expectedInput.input_prompt.no_input_prompts.add(new SpeechResponse(noInputPrompt, null));
            }

        }

        expectedInput.possible_intents = new ArrayList<>();
        //expectedIntents.
        expectedInput.possible_intents.addAll(expectedIntents);

        rootResponse.expected_inputs.add(expectedInput);
        return rootResponse;
    }
}
