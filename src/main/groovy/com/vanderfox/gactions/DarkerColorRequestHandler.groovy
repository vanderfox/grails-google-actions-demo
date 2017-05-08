package com.vanderfox.gactions

import com.frogermcs.gactions.ResponseBuilder
import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest
import com.frogermcs.gactions.api.response.RootResponse
import groovy.util.logging.Slf4j
import util.ColorUtils

import java.awt.*
import java.lang.reflect.Field

/**
 * Created by froger_mcs on 19/01/2017.
 */
@Slf4j
public class DarkerColorRequestHandler extends RequestHandler {
    protected DarkerColorRequestHandler(RootRequest rootRequest) {
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
            String brighter = colorUtils.findDarkerNameForColor(parsedColor).toLowerCase()
            if (brighter != color) {
                return ResponseBuilder.tellResponse("The darker color for ${color} is ${brighter} ")
            } else {
                return ResponseBuilder.tellResponse("Sorry I can't find a darker color for ${color}.")
            }

        } else {
            return colorNotFound(color)
        }

    }

    private RootResponse colorNotFound(String color) {
        return ResponseBuilder.tellResponse("Sorry I don't understand the color ${color}.")
    }
}
