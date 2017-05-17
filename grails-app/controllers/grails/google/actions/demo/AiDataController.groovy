package grails.google.actions.demo

import ai.api.AIServiceException
import ai.api.model.AIResponse
import org.grails.apiai.AiServiceController

class AiDataController implements AiServiceController {

 def index() {
     try {
         AIResponse aiResponse = request(request.getParameter("query"), request.getSession())
         response.setContentType("text/plain")
         response.getWriter().append(aiResponse.getResult().getFulfillment().getSpeech())
     } catch (AIServiceException e) {
         log.error("Error talking to remote service: ${e.message}",e)
     }

 }
}
