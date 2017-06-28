package com.vanderfox.gactions

/**
 * Created by lfox on 6/16/16.
 */
class Question implements Serializable {

    private static final long serialVersionUID = 1L

    String question
    String[] options
    int answer
    int index
}
