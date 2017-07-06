package grails.google.actions.demo

import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

@Transactional()
class ConversationService {

    def serviceMethod() {

    }

    Conversation startConversation(String conversationId, Map conversationValues) {
        Conversation conversation = new Conversation(conversationId: conversationId)
        conversation.conversationId = conversationId
        conversation.conversationToken = "convo-${System.nanoTime()}"

        def builder = new JsonBuilder()
        builder(conversationValues)
        log.debug("map= ${builder.toString()}")
        conversation.conversationValueMap = builder.toString()
        log.debug("Saving conversation id: ${conversationId}")
        conversation.save(flush:true, failOnError:true)
        conversation
    }

    Conversation updateConversation(String conversationId, Map conversationValues) {
        Conversation conversation = Conversation.findByConversationId(conversationId)
        if (conversation) {
            def builder = new JsonBuilder()
            builder(conversationValues)
            log.debug("map= ${builder.toString()}")
            conversation.conversationValueMap = builder.toString()
            log.debug("updating conversation id:${conversationId}")
            conversation.save(flush:true)
        } else {
            conversation = startConversation(conversationId,conversationValues)
        }
        conversation
    }

    @Transactional(readOnly = true)
    Conversation getConversation(String conversationId) {
        log.debug("looking up conversation id: ${conversationId}")
        Conversation.findByConversationId(conversationId)
    }

    @Transactional(readOnly = true)
    Map getConversationMap(String conversationValueMap) {
        def slurper = new JsonSlurper()
        (Map)slurper.parseText(conversationValueMap)

    }

}
