package grails.google.actions.demo

import ai.api.model.Fulfillment
import ai.api.web.AIWebhookServlet
import grails.converters.JSON
import org.grails.apiai.AiWebhookController
import org.grails.web.json.JSONElement

class WeatherWebhookController implements AiWebhookController{

    @Override
    void doWebhook(AIWebhookServlet.AIWebhookRequest input, Fulfillment output) {

        String baseurl = "https://query.yahooapis.com/v1/public/yql?q="
        //java.net.URLEncoder.encode(toEncode, "UTF-8")
        String city = input.getResult().parameters.'geo-city'
        String yql_query = "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text='" + city + "')"
        String yql_url = baseurl + java.net.URLEncoder.encode(yql_query,"UTF-8") + "&format=json"
        String yqlResponse = yql_url.toURL().text
        JSONElement channel = JSON.parse(yqlResponse).query.results.channel
        JSONElement forecast = channel.item.forecast[0]
        def weather = "The weather in ${channel.location.city} for ${forecast.date} is a high of ${forecast.high} and a low of ${forecast.low} and it will be ${forecast.text}"
        output.setSpeech(weather)
        output.setDisplayText(weather)
        //output.contextOut = []
        //output.data = [:]
        output.source = "grails-yahoo-weather"

    }
}
