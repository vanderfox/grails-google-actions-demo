package com.vanderfox.gactions

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.StandardIntents
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.ExpectedIntent
import com.frogermcs.gactions.api.response.RootResponse
import grails.google.actions.demo.AssistantActionController
import grails.google.actions.demo.Conversation
import grails.google.actions.demo.ConversationService
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import util.ColorUtils

import java.awt.*
import java.lang.reflect.Field
import java.text.NumberFormat

/**
 * Created by froger_mcs on 19/01/2017.
 */
@Slf4j
public class StarWarsQuizRequestHandler extends RequestHandler {

    static final String CURRENT_QUESTION = "currentQuestionNumber"
    static final String LAST_QUESTION_ASKED = "lastQuestionAsked"
    static final int MAX_QUESTIONS = 5
    @Autowired
    ConversationService conversationService
    static final String ONE = "one"
    static final String TWO = "two"
    static final String THREE = "three"
    static final String FOUR = "four"

    protected StarWarsQuizRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    ConversationService getConversationService() {
        if (!conversationService) {
            conversationService = Holders.grailsApplication.mainContext.getBean('conversationService')
        }
    }

    @Override
    public RootResponse getResponse() {
        log.debug("Inputs=${rootRequest.inputs.toListString()}")

        Conversation conversation = getConversationService().getConversation(rootRequest.conversation.conversation_id)
        log.debug("Conversation id=${rootRequest.conversation.conversation_id}")
        if (!conversation) {
            log.debug("No current conversation - starting a new one")
            conversation =  conversationService.startConversation(rootRequest.conversation.conversation_id,[:])
        }

        Map conversationMap = conversationService.getConversationMap(conversation.conversationValueMap)
        String intent = rootRequest.inputs.get(0).intent
        switch (intent) {

            case "start.quiz.intent":
                conversationMap.put(CURRENT_QUESTION,1)
                return startQuiz(rootRequest,conversation,conversationMap)
            break

            case StandardIntents.TEXT: // actions sdk on google's end requires all response types to be hard coded to text (lame!)
                return answerQuestion(rootRequest,conversation,conversationMap)

                break
            case "ask.question":
                return askNextQuestion("",rootRequest,conversation,conversationMap)


            default:

                break


        }


    }

    private RootResponse startQuiz(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        initializeComponents(rootRequest,conversation,conversationMap)
        String speechText = "Welcome to Unofficial Star Wars Quiz.  I'm going to ask you 5 questions to test your Star Wars knowledge.  Say repeat question at any time if you need to hear a question again, or say help if you need some help.  Let's get started. , "
        int questionId = conversationMap.get(CURRENT_QUESTION)
        int tableRowCount = 0
        if (!conversationMap.get("tableRowCount")) {
            // lets get it then
            tableRowCount = getTableRowCount()
        } else {
            tableRowCount = Integer.parseInt((String) conversationMap.get("tableRowCount"))
        }
        int questionIndex = (new Random().nextInt() % tableRowCount).abs()
        speechText += buildAskQuestion(questionIndex)
        conversationMap["answer.question"] = ++questionId
        conversationMap[LAST_QUESTION_ASKED] = questionIndex
        conversationService.updateConversation(rootRequest.conversation.conversation_id,conversationMap)
        ResponseBuilder.askResponse(speechText,conversation.conversationToken, ['Please say the number of the correct question'] as String[])

    }

    private RootResponse answerQuestion(RootRequest rootRequest, Conversation conversation, Map conversationMap) {

        String speechText

        int guessedAnswer = 0
        def rawText = rootRequest.inputs.get(0).arguments.get(0).raw_text
        if (rawText.toLowerCase() == "repeat question") {
            return repeatQuestion(rootRequest,conversation,conversationMap)
        }
        if (rawText.toLowerCase() == "help" || rawText.toLowerCase() == "help me") {
            return getHelpResponse(rootRequest,conversation,conversationMap)
        }

        log.debug("raw guessedAnswer=${rawText}")
        if (rawText.isNumber()) {
            guessedAnswer = Integer.parseInt(rawText)
        } else {
            // let's do this the hard way (ugh google WHY U NO NLP responses!!!)

            switch (rawText) {
                case ONE:
                    guessedAnswer = 1
                    break
                case TWO:
                    guessedAnswer = 2
                    break
                case THREE:
                    guessedAnswer = 3
                    break
                case FOUR:
                    guessedAnswer = 4
                    break
                default:
                    return didNotUnderstand(rootRequest,conversation,conversationMap)
                    break
            }
        }
        log.debug("parsed guessedAnswer=${guessedAnswer}")
        if (!conversationMap.get(CURRENT_QUESTION)) {
            // assume we are on 1
            log.debug("Cannot determine current question setting to 1")
            conversationMap.put(CURRENT_QUESTION,1)

        }
        log.debug("Converation=${conversation}")

        def currentQuestion = (int) conversationMap.get(CURRENT_QUESTION)
        log.debug("Current question count=${currentQuestion}")
        log.debug("Last question id asked=${conversationMap.get(LAST_QUESTION_ASKED)}")
        Question question = getQuestion(conversationMap.get(LAST_QUESTION_ASKED) as Integer)

        def answer = question.getAnswer()
        log.info("correct answer for question index ${question.index} text: ${question.question} is:  " + answer)

        currentQuestion++
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
            //speechText = buildAskQuestion(currentQuestion)
            //return ResponseBuilder.askResponse(speechText,conversation.conversationToken, ["ask.question"] as String[])
            speechText += "\n Next question: \n,"
            return askNextQuestion(speechText,rootRequest,conversation,conversationMap)
        } else {
            int score = (Integer) conversationMap.get("score") ?: 0
            speechText += "\n\nYou answered ${score} questions correctly."

            return ResponseBuilder.tellResponse(speechText)
        }


    }

    RootResponse repeatQuestion(RootRequest rootRequest, Conversation conversation, Map conversationMap) {
        if (!conversationMap.get(CURRENT_QUESTION)) {
            // assume we are on 1
            log.debug("Cannot determine current question setting to 1")
            conversationMap.put(CURRENT_QUESTION,1)

        }
        log.debug("Converation=${conversation}")

        def currentQuestion = (int) conversationMap.get(CURRENT_QUESTION)
        log.debug("Current question count=${currentQuestion}")
        log.debug("Last question id asked=${conversationMap.get(LAST_QUESTION_ASKED)}")
        Question question = getQuestion(conversationMap.get(LAST_QUESTION_ASKED) as Integer)
        String speechText = buildAskQuestion(question.index)

        ResponseBuilder.askResponse(speechText,conversation.conversationToken, ['Please say the number of the correct question'] as String[])
    }

    private String buildAskQuestion(int questionId) {
          Question question = getQuestion(questionId)
        String speechText = "\n"
        speechText += question.getQuestion() + ". \n, "
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + " ., \n\n\n\n" + option + "\n\n\n., "
        }
        speechText

    }

    private RootResponse askNextQuestion(String speechText = "", RootRequest rootRequest, Conversation conversation, Map conversationMap) {

        int currentQuestion = (int) conversationMap.get(CURRENT_QUESTION)
        currentQuestion++

        int tableRowCount = 0
        if (!conversationMap.get("tableRowCount")) {
            // lets get it then
            tableRowCount = getTableRowCount()
        } else {
            tableRowCount = Integer.parseInt((String) conversationMap.get("tableRowCount"))
        }
        int questionIndex = (new Random().nextInt() % tableRowCount).abs()
        Question question = getQuestion(questionIndex)
        log.info("The question index is:  " + questionIndex)
        conversationMap.put(LAST_QUESTION_ASKED, questionIndex)
        conversationService.updateConversation(conversation.conversationId,conversationMap)
        speechText += "\n"
        speechText += question.getQuestion() + ". \n, "
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + " ., \n\n\n\n" + option + "\n\n\n., "
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
        question.setAnswer(questionAnswer)
        question.setIndex(questionIndex)
        log.info("question index retrieved:  " + question.getIndex())
        log.info("question text retrieved:  " + question.getQuestion())
        log.info("question answer retrieved:  " + question.getAnswer())
        log.info("question options:  " + question.getOptions().toString())
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

        AmazonDynamoDBClient amazonDynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient()
        ScanRequest req = new ScanRequest()
        req.setTableName("StarWarsQuiz")
        ScanResult result = amazonDynamoDBClient.scan(req)
        java.util.List quizItems = result.items
        int tableRowCount = quizItems.size()
        conversationMap.put("tableRowCount", Integer.toString(tableRowCount))
        conversationService.updateConversation(rootRequest.conversation.conversation_id,conversationMap)
        log.info("This many rows in the table:  " + tableRowCount)
    }

    int getTableRowCount() {

        AmazonDynamoDBClient amazonDynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient()
        ScanRequest req = new ScanRequest()
        req.setTableName("StarWarsQuiz")
        ScanResult result = amazonDynamoDBClient.scan(req)
        java.util.List quizItems = result.items
        int tableRowCount = quizItems.size()
        log.info("This many rows in the table:  " + tableRowCount)
        tableRowCount

    }
}
