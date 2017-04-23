package com.vanderfox.gactions;

import com.frogermcs.gactions.api.RequestHandler;

/**
 * Created by froger_mcs on 19/01/2017.
 */
public class MainRequestHandlerFactory implements RequestHandler.Factory {
    @Override
    public RequestHandler create() {
        return new MainRequestHandler()
    }
}
