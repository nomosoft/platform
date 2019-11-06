package lsfusion.http.controller;

import lsfusion.base.col.heavy.OrderedMap;
import lsfusion.http.authentication.LSFAuthenticationToken;
import lsfusion.http.provider.navigator.NavigatorProviderImpl;
import lsfusion.http.provider.session.SessionProvider;
import lsfusion.http.provider.session.SessionSessionObject;
import lsfusion.interop.logics.LogicsSessionObject;
import lsfusion.interop.session.ExecInterface;
import lsfusion.interop.session.ExternalUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.list;
import static lsfusion.base.ServerMessages.getString;

public class ExternalLogicsAndSessionRequestHandler extends ExternalRequestHandler {
    
    @Autowired
    SessionProvider sessionProvider;

    @Override
    protected void handleRequest(LogicsSessionObject sessionObject, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sessionID = null;
        boolean closeSession = false;
        try {
            String queryString = request.getQueryString();
            String query = queryString != null ? queryString : "";
            String contentTypeString = request.getContentType();
            ContentType contentType = contentTypeString != null ? ContentType.parse(contentTypeString) : null;

            String[] headerNames = list((Enumeration<String>)request.getHeaderNames()).toArray(new String[0]);
            String[] headerValues = getRequestHeaderValues(request, headerNames);

            OrderedMap<String, String> cookiesMap = getRequestCookies(request);
            String[] cookieNames = cookiesMap.keyList().toArray(new String[0]);
            String[] cookieValues = cookiesMap.values().toArray(new String[0]);

            sessionID = request.getParameter("session");
            ExecInterface remoteExec;
            if(sessionID != null) {
                if(sessionID.endsWith("_close")) {
                    closeSession = true;
                    sessionID = sessionID.substring(0, sessionID.length() - "_close".length());
                }

                SessionSessionObject sessionSessionObject = sessionProvider.getSessionSessionObject(sessionID);
                if(sessionSessionObject == null)
                    sessionSessionObject = sessionProvider.createSession(sessionObject.remoteLogics, request, sessionID);
                remoteExec = sessionSessionObject.remoteSession;
            } else {
                remoteExec = ExternalUtils.getExecInterface(LSFAuthenticationToken.getAppServerToken(),
                        NavigatorProviderImpl.getSessionInfo(request), sessionObject.remoteLogics);
            }

            String logicsHost = sessionObject.connection.host != null && !sessionObject.connection.host.equals("localhost") && !sessionObject.connection.host.equals("127.0.0.1")
                    ? sessionObject.connection.host : request.getServerName();

            InputStream requestInputStream = getRequestInputStream(request, contentType, query);
            
            ExternalUtils.ExternalResponse responseHttpEntity = ExternalUtils.processRequest(remoteExec, request.getRequestURL().toString(), 
                    request.getRequestURI(), query, requestInputStream, new HashMap<String, String[]>(), contentType, headerNames, headerValues, cookieNames, cookieValues,
                    logicsHost, sessionObject.connection.port, sessionObject.connection.exportName);

            if (responseHttpEntity.response != null) {
                sendResponse(response, responseHttpEntity);
            } else {
                sendResponse(response, getString(request, "executed.successfully"), Charset.forName("UTF-8"));
            }

        } catch (RemoteException e) {
            closeSession = true; // closing session if there is a RemoteException
            throw e;
        } finally {
            if(sessionID != null && closeSession) {
                sessionProvider.removeSessionSessionObject(sessionID);
            }
        }
    }
    
    // if content type is 'application/x-www-form-urlencoded' the body of the request appears to be already read somewhere else. 
    // so we have empty InputStream and have to read body parameters from parameter map
    private InputStream getRequestInputStream(HttpServletRequest request, ContentType contentType, String query) throws IOException {
        InputStream inputStream = request.getInputStream();
        if (contentType != null && ContentType.APPLICATION_FORM_URLENCODED.getMimeType().equals(contentType.getMimeType()) && inputStream.available() == 0) {
            Charset charset = ExternalUtils.getCharsetFromContentType(contentType);
            List<NameValuePair> queryParams = URLEncodedUtils.parse(query, charset);
            StringBuilder bodyParams = new StringBuilder();

            Map parameterMap = request.getParameterMap();
            for (Object o : parameterMap.entrySet()) {
                Object paramName = ((Map.Entry) o).getKey();
                Object paramValues = ((Map.Entry) o).getValue();

                if (paramName instanceof String && paramValues instanceof String[]) {
                    for (String paramValue : (String[]) paramValues) {
                        NameValuePair parameter = new BasicNameValuePair((String) paramName, paramValue);
                        if (!queryParams.contains(parameter)) {
                            if (bodyParams.length() > 0) {
                                bodyParams.append("&");
                            }
                            bodyParams.append((String) paramName).append("=").append(paramValue);
                        }
                    }
                }
            }

            inputStream = new ByteArrayInputStream(bodyParams.toString().getBytes(charset));
        }
        return inputStream;
    }

    private String[] getRequestHeaderValues(HttpServletRequest request, String[] headerNames) {
        String[] headerValuesArray = new String[headerNames.length];
        for (int i = 0; i < headerNames.length; i++) {
            headerValuesArray[i] = StringUtils.join(list(request.getHeaders(headerNames[i])).iterator(), ",");
        }
        return headerValuesArray;
    }

    private OrderedMap<String, String> getRequestCookies(HttpServletRequest request) {
        OrderedMap<String, String> cookiesMap = new OrderedMap<>();
        String cookies = request.getHeader("Cookie");
        if (cookies != null) {
            for (String cookie : cookies.split(";")) {
                String[] splittedCookie = cookie.split("=");
                if (splittedCookie.length == 2) {
                    cookiesMap.put(splittedCookie[0], splittedCookie[1]);
                }
            }
        }
        return cookiesMap;
    }
}
