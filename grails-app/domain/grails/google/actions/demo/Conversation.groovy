package grails.google.actions.demo

class Conversation {

    static constraints = {
        conversationId nullable: false, blank: false
        conversationToken nullable: true, blank: true
        conversationValueMap nullable: true, blank: true
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    Date dateCreated
    Date lastUpdated
    String conversationId
    String conversationToken
    String conversationValueMap

}
