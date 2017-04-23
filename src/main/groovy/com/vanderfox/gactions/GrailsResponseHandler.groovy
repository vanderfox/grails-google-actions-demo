package com.vanderfox.gactions

import com.frogermcs.gactions.ResponseHandler
import com.frogermcs.gactions.api.response.RootResponse
import com.google.gson.Gson
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletResponse

/**
 * Created by rvanderwerf on 4/14/17.
 */
@Slf4j
class GrailsResponseHandler implements ResponseHandler{
    private final HttpServletResponse httpServletResponse
    private final Gson gson

    public GrailsResponseHandler(HttpServletResponse httpServletResponse) {
        this(httpServletResponse, new Gson())
    }

    public GrailsResponseHandler(HttpServletResponse httpServletResponse, Gson gson) {
        this.httpServletResponse = httpServletResponse
        this.gson = gson
    }

    @Override
    public void onPrepareContentType(String contentType) {
        httpServletResponse.setContentType(contentType)
    }

    @Override
    public void onPrepareResponseHeaders(Map<String, String> headers) {
        for (String headerName : headers.keySet()) {
            httpServletResponse.addHeader(headerName, headers.get(headerName))
        }
    }

    @Override
    public void onResponse(RootResponse rootResponse) {
        try {
            gson.toJson(rootResponse, httpServletResponse.getWriter())
            httpServletResponse.flushBuffer()
        } catch (IOException e) {
            log.error("Error writing response",e)
            e.printStackTrace()
        }
    }

    public String getResponse(RootResponse rootResponse) {

        try {

            gson.toJson(rootResponse)

        } catch (IOException e) {
            log.error("Error getting response",e)
            e.printStackTrace()
        }
    }


}
