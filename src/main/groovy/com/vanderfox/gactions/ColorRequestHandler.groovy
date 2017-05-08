package com.vanderfox.gactions

import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import groovy.util.logging.Slf4j
import util.ColorUtils

import java.awt.Color
import java.lang.reflect.Field

/**
 * Created by froger_mcs on 19/01/2017.
 */
@Slf4j
public class ColorRequestHandler extends RequestHandler {

    protected ColorRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    public RootResponse getResponse() {
        log.debug("Inputs=${rootRequest.inputs.toListString()}")
        String color = rootRequest.inputs[0].arguments[0].raw_text.toLowerCase()

        Color parsedColor = null
        try {
            Field field = Class.forName("java.awt.Color").getField(color)
            parsedColor = (Color)field.get(null)
        } catch (NoSuchFieldException ne) {
            return colorNotFound(color)
        }
        if (parsedColor) {
            ColorUtils colorUtils = new ColorUtils()
            String brighter = colorUtils.findBrighterNameForColor(parsedColor).toLowerCase()
            if (brighter != color) {
                return ResponseBuilder.tellResponse("The brighter color for ${color} is ${brighter} ")
            } else {
                //def sorryAnswer = "Sorry I can't find a brighter color for ${color}."
                def sorryAnswer = "<speak><audio src=\"https://actions.google.com/sounds/v1/cartoon/metallic_clank.ogg\">Sorry I can't find a brighter color for ${color}.</speak>"
                return ResponseBuilder.tellResponse(sorryAnswer)
            }

        } else {
            return colorNotFound(color)
        }

    }

    private RootResponse colorNotFound(String color) {
        return ResponseBuilder.tellResponse("Sorry I don't understand the color ${color}.")
    }
}
