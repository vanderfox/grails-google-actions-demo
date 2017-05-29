package grails.google.actions.demo

import ai.api.model.Fulfillment
import ai.api.web.AIWebhookServlet
import grails.converters.JSON
import org.grails.apiai.AiWebhookController
import org.grails.web.json.JSONElement

class WeatherWebhookController implements AiWebhookController {

    @Override
    void doWebhook(AIWebhookServlet.AIWebhookRequest input, Fulfillment output) {

        String baseurl = 'https://query.yahooapis.com/v1/public/yql?q='
        String city = input.result.parameters.'geo-city'
        String yqlQuery = 'select * from weather.forecast where woeid in ' +
                "(select woeid from geo.places(1) where text=\'${city}\')"
        String yqlUrl = baseurl + URLEncoder.encode(yqlQuery, 'UTF-8') + '&format=json'
        String yqlResponse = yqlUrl.toURL().text
        JSONElement channel = JSON.parse(yqlResponse).query.results.channel
        JSONElement forecast = channel.item.forecast[0] // this is day 1 of a 7 day forecast
        def weather = "The weather in ${channel.location.city} for " +
                "${forecast.date} is a high of ${forecast.high} and a low " +
                "of ${forecast.low} and it will be ${forecast.text}" // units are in F
        output.setSpeech(weather)
        output.setDisplayText(weather)
        //output.contextOut = [] // these are not needed because conversation ends at this point
        //output.data = [:]      // these are not needed because conversation ends at this point
        output.source = 'grails-yahoo-weather'

    }
}
