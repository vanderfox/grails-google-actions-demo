package grails.google.actions.demo

import ai.api.model.Fulfillment
import ai.api.web.AIWebhookServlet
import org.grails.apiai.AiWebhookController

class WebhookController implements AiWebhookController{

    @Override
    void doWebhook(AIWebhookServlet.AIWebhookRequest input, Fulfillment output) {

        output.setSpeech("You said: " + input.getResult().getFulfillment().getSpeech())
        output.setDisplayText("You said: " + input.getResult().getFulfillment().getSpeech())
        //output.contextOut = []
        //output.data = [:]
        output.source = "color-finder"

    }
}
