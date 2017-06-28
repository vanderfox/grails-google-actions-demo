package com.vanderfox.gactions

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import grails.google.actions.demo.Conversation
import grails.google.actions.demo.ConversationService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import util.ColorUtils

import java.awt.*
import java.lang.reflect.Field

/**
 * Created by froger_mcs on 19/01/2017.
 */
@Slf4j
public class StarWarsQuizRequestHandler extends RequestHandler {

    static final String CURRENT_QUESTION = "currentQuestionNumber"
    static final int MAX_QUESTIONS = 5
    @Autowired
    ConversationService conversationService

    protected StarWarsQuizRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    public RootResponse getResponse() {
        log.debug("Inputs=${rootRequest.inputs.toListString()}")

        Conversation conversation = conversationService.getConversation(rootRequest.conversation.conversation_id)

        Map conversationMap = conversationService.getConversationMap(conversation.conversationValueMap)
        String intent = rootRequest.inputs.get(0).intent
        switch (intent) {

            case "start.quiz":
                conversationMap.put(CURRENT_QUESTION,1)
                return startQuiz(rootRequest,conversation,conversationMap)
            break

            case "answer.question":
                return answerQuestion(rootRequest,conversation,conversationMap)

                break
            case "ask.question":
                return askNextQuestion(rootRequest,conversation,conversationMap)


            default:

                break


        }


    }

    private RootResponse startQuiz(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        initializeComponents(rootRequest,conversation,conversationMap)
        String speechText = "Welcome to Unofficial Star Wars Quiz.  I'm going to ask you 5 questions to test your Star Wars knowledge.  Say repeat question at any time if you need to hear a question again, or say help if you need some help.  Let's get started"
        int questionId = conversationMap.get("answer.question")
        speechText += buildAskQuestion(questionId)
        conversationMap["answer.question"] = ++questionId
        conversationService.updateConversation(rootRequest.conversation.conversation_id,conversationMap)
        ResponseBuilder.askResponse(speechText,conversation.conversationToken, ["answer.question"])

    }

    private RootResponse answerQuestion(RootRequest rootRequest, Conversation conversation, Map conversationMap) {

        String speechText

        int guessedAnswer = Integer.parseInt(rootRequest.inputs.get(0).arguments.get(0).raw_text)

        def currentQuestion = (int) conversationMap.get(CURRENT_QUESTION)
        Question question = getQuestion(currentQuestion)

        def answer = question.getAnswer()
        log.info("correct answer is:  " + answer)
        //int questionCounter = Integer.parseInt((String) session.getAttribute("questionCounter"))

        currentQuestion--
        conversationMap.put(CURRENT_QUESTION,currentQuestion)

        if (guessedAnswer == answer) {
            speechText = "You got it right."
            int score = (Integer) conversationMap.get("score") ?: 0
            score++
            conversationMap.put("score", score)
        } else {
            speechText = "You got it wrong."
        }

        log.info("questionCounter:  " + currentQuestion)

        conversationService.updateConversation(rootRequest.conversation.conversation_id,conversationMap)
        log.info("Guessed answer is:  " + guessedAnswer)

        if (currentQuestion > 0 && currentQuestion < MAX_QUESTIONS) {
            speechText = getQuestion(currentQuestion)
            return ResponseBuilder.askResponse(speechText,conversation.conversationToken, ["ask.question"] as String[])
        } else {
            int score = (Integer) conversationMap.get("score") ?: 0
            speechText += "\n\nYou answered ${score} questions correctly."

            return ResponseBuilder.tellResponse(speechText)
        }


    }

    private String buildAskQuestion(int questionId) {
          Question question = getQuestion(questionId)
        String speechText = "\n"
        speechText += question.getQuestion() + "\n"
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + "\n\n\n\n" + option + "\n\n\n"
        }
        speechText

    }

    private RootResponse askNextQuestion(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        String speechText = ""
        int currentQuestion = (int) conversationMap.get(CURRENT_QUESTION)
        currentQuestion++
        Question question = getQuestion(currentQuestion)
        conversationMap.put("lastQuestionAsked", currentQuestion)

        speechText += "\n"
        speechText += question.getQuestion() + "\n"
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + "\n\n\n\n" + option + "\n\n\n"
        }

        ResponseBuilder.askResponse(speechText,conversation.conversationToken,["answer.question"] as String[])

    }


    private Question getQuestion(int questionIndex) {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient())
        Table table = dynamoDB.getTable("StarWarsQuiz")
        Item item = table.getItem("Id", questionIndex)
        def questionText = item.getString("Question")
        def questionAnswer = item.getInt("answer")
        def options = new String[4]
        options[0] = item.getString("option1")
        options[1] = item.getString("option2")
        options[2] = item.getString("option3")
        options[3] = item.getString("option4")
        Question question = new Question()
        question.setQuestion(questionText)
        question.setOptions(options)
        question.setAnswer(questionAnswer - 1)
        question.setIndex(questionIndex)
        log.info("question retrieved:  " + question.getIndex())
        log.info("question retrieved:  " + question.getQuestion())
        log.info("question retrieved:  " + question.getAnswer())
        log.info("question retrieved:  " + question.getOptions().length)
        question
    }


    private RootResponse getHelpResponse(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        String speechText = ""
        speechText = "You can say stop or cancel to end the game at any time.  If you need a question repeated, say repeat question.";
        ResponseBuilder.askResponse(speechText)
    }

    private RootResponse didNotUnderstand(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        String speechText = "I'm sorry.  I didn't understand what you said.  Say help me for help.";
        ResponseBuilder.askResponse(speechText)
    }


    private void initializeComponents(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        AmazonDynamoDBClient amazonDynamoDBClient
        amazonDynamoDBClient = new AmazonDynamoDBClient()
        ScanRequest req = new ScanRequest()
        req.setTableName("StarWarsQuiz")
        ScanResult result = amazonDynamoDBClient.scan(req)
        java.util.List quizItems = result.items
        int tableRowCount = quizItems.size()
        conversationMap.put("tableRowCount", Integer.toString(tableRowCount))
        conversationService.updateConversation(rootRequest.conversation.conversation_id,conversationMap)
        log.info("This many rows in the table:  " + tableRowCount)
    }
}
