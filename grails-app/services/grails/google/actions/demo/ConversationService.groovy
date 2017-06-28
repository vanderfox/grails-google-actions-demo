package grails.google.actions.demo

import grails.transaction.Transactional

@Transactional
class ConversationService {

    def serviceMethod() {

    }

    Conversation startConversation(String conversationId, Map conversationValues) {
        Conversation conversation = new Conversation(conversationId: conversationId)
        conversation.conversationToken = "convo-${System.nanoTime()}"
        conversation.conversationValueMap = conversationValues.toMapString(conversationValues.size())
        conversation.save()
        conversation
    }

    Conversation updateConversation(String conversationId, Map conversationValues) {
        Conversation conversation = Conversation.findByConversationId(conversationId)
        if (conversation) {
            conversation.conversationValueMap = conversationValues.toMapString(conversationValues.size())
        } else {
            conversation = startConversation(conversationId,conversationValues)
        }
        conversation
    }

    Conversation getConversation(String conversationId) {
        Conversation.findByConversationId(conversationId)
    }

    Map getConversationMap(String stringMap) {
        def conversationMap =
                // Take the String value between
                // the [ and ] brackets.
                conversation.conversationValueMap[1..-2]
                // Split on , to get a List.
                        .split(', ')
                // Each list item is transformed
                // to a Map entry with key/value.
                        .collectEntries { entry ->
                    def pair = entry.split(':')
                    [(pair.first()): pair.last()]
                }
    }

}
