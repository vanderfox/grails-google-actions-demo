package com.vanderfox.gactions;

import com.frogermcs.gactions.api.RequestHandler
import com.frogermcs.gactions.api.request.RootRequest;

/**
 * Created by froger_mcs on 19/01/2017.
 */
public class ColorRequestHandlerFactory extends RequestHandler.Factory {
    @Override
    RequestHandler create(RootRequest rootRequest) {
        return new ColorRequestHandler(rootRequest)
    }

}
