package com.vanderfox.gactions;

import com.frogermcs.gactions.ResponseBuilder;
import com.frogermcs.gactions.api.RequestHandler;
import com.frogermcs.gactions.api.request.RootRequest;
import com.frogermcs.gactions.api.response.RootResponse;

/**
 * Created by froger_mcs on 19/01/2017.
 */
public class TextRequestHandler extends RequestHandler {
    protected TextRequestHandler(RootRequest rootRequest) {
        super(rootRequest)
    }

    @Override
    public RootResponse getResponse() {
        return ResponseBuilder.tellResponse("You just told: " + rootRequest.inputs.get(0).raw_inputs.get(0).query)
    }
}
