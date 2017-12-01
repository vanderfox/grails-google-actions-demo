package grails.google.actions.demo

import ai.api.model.Fulfillment
import ai.api.web.AIWebhookServlet
import org.grails.apiai.AiWebhookController
import util.ColorUtils

import java.awt.Color
import java.lang.reflect.Field

class WebhookController implements AiWebhookController{

    @Override
    void doWebhook(AIWebhookServlet.AIWebhookRequest input, Fulfillment output) {

        String color = input?.result.parameters["color"]?.value
        String shade = input?.result.parameters["shade"]?.value

        Color parsedColor = null
        try {
            Field field = Class.forName("java.awt.Color").getField(color)
            parsedColor = (Color)field.get(null)
        } catch (NoSuchFieldException ne) {
            output.setSpeech("Sorry I can't find a ${shade} color for ${color}.")
            output.setDisplayText("Sorry I can't find a ${shade} color for ${color}.")
        }
        if (parsedColor) {
            ColorUtils colorUtils = new ColorUtils()
            String adjustedColor = ""
            if (shade == "darker") {
                adjustedColor = colorUtils.findDarkerNameForColor(parsedColor).toLowerCase()
            } else {
                adjustedColor = colorUtils.findBrighterNameForColor(parsedColor).toLowerCase()
            }

            if (adjustedColor) {
                output.setSpeech("The ${shade} color for ${color} is: ${adjustedColor}")
                output.setDisplayText("The ${shade} color for ${color} is: ${adjustedColor}")

            } else {
                output.setSpeech("Sorry I can't find a ${shade} color for ${color}.")
                output.setDisplayText("Sorry I can't find a ${shade} color for ${color}.")
            }

        } else {
            output.setSpeech("Sorry I can't find a ${shade} color for ${color}.")
            output.setDisplayText("Sorry I can't find a ${shade} color for ${color}.")
        }
        //output.contextOut = []
        //output.data = [:]
        output.source = "color-finder"

    }
}
