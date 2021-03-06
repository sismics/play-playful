package controllers.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import com.google.gson.*;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.multipart.*;
import org.junit.Before;
import play.Invoker;
import play.Play;
import play.classloading.enhancers.ControllersEnhancer;
import play.libs.Codec;
import play.mvc.*;
import play.test.BaseTest;
import play.test.FunctionalTest;
import play.test.TestEngine;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * @author jtremeaux
 */
public abstract class RestFunctionalTest extends BaseTest {
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String APPLICATION_JSON = "application/json";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final String SESSION_COOKIE = Play.configuration.getProperty("application.session.cookie" + "_SESSION");

    /**
     * The reponse to the last request.
     */
    public static Http.Response response;

    /**
     * Cookies stored between calls.
     */
    public static Map<String, Http.Cookie> savedCookies;

    /**
     * Headers reused between calls.
     */
    public static Map<String, Http.Header> headers;


    private static Map<String, Object> renderArgs = new HashMap<>();

    @Before
    public void clearQuery() {
        savedCookies = new HashMap<>();
        headers = new HashMap<>();
        setHeader("accept", APPLICATION_JSON);
    }

    public static void setHeader(String key, String value) {
        headers.put(key, new Http.Header(key, value));
    }

    public static void setBasicAuthentication(String username, String password) {
        setHeader("authorization", "Basic " + Codec.encodeBASE64(username + ":" + password));
    }

    public static void setOrigin(String origin) {
        if (origin != null) {
            setHeader("origin", origin);
        } else {
            headers.remove("origin");
        }
    }

    public static Http.Cookie getCookie(String key) {
        if (savedCookies == null) {
            return null;
        }
        return savedCookies.get(key);
    }

    public static void setCookie(Http.Cookie cookie) {
        if (savedCookies == null) {
            savedCookies = new HashMap<>();
        }
        savedCookies.put(cookie.name, cookie);
    }

    public static Http.Cookie getSessionCookie() {
        return getCookie(SESSION_COOKIE);
    }

    public static void setSessionCookie(Http.Cookie cookie) {
        setCookie(cookie);
    }

    public static Http.Response GET(Object url) {
        return GET(newRequest(), url);
    }

    public static Http.Response GET(Object url, Map<String, String> queryParams) {
        Multimap map = LinkedHashMultimap.<String, String>create();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return GET(url, map);
    }

    public static Http.Response GET(Object url, Multimap<String, String> queryParams) {
        String queryString = "";
        if (!queryParams.isEmpty()) {
            boolean firstParam = true;
            for (Map.Entry<String, String> entry : queryParams.entries()) {
                if (firstParam) {
                    queryString += "?";
                    firstParam = false;
                } else {
                    queryString += "&";
                }
                queryString += entry.getKey() + "=" + UrlEscapers.urlFormParameterEscaper().escape(entry.getValue());
            }
        }
        return GET(newRequest(), url + queryString);
    }

    /**
     * Sends a GET request to the application under tests.
     *
     * @param url Relative url such as <em>"/products/1234"</em>
     * @param followRedirect Indicates if request have to follow redirection (status 302)
     * @return The response
     */
    public static Http.Response GET(Object url, boolean followRedirect) {
        GET(url);
        if (Http.StatusCode.FOUND == response.status && followRedirect) {
            Http.Header redirectedTo = response.headers.get("Location");
            String location = redirectedTo.value();
            if (location.contains("http")) {
                java.net.URL redirectedUrl = null;
                try {
                    redirectedUrl = new java.net.URL(redirectedTo.value());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                response = GET(redirectedUrl.getPath());
            } else {
                response = GET(location);
            }
        }
        return response;
    }

    /**
     * sends a GET request to the application under tests.
     *
     * @param request The request
     * @param url relative url such as <em>"/products/1234"</em>
     * @return The response
     */
    public static Http.Response GET(Http.Request request, Object url) {
        String path = "";
        String queryString = "";
        String turl = url.toString();
        if (turl.contains("?")) {
            path = turl.substring(0, turl.indexOf("?"));
            queryString = turl.substring(turl.indexOf("?") + 1);
        } else {
            path = turl;
        }
        request.method = "GET";
        request.url = turl;
        request.path = path;
        request.querystring = queryString;
        request.body = new ByteArrayInputStream(new byte[0]);
        request.headers.putAll(headers);
        if (savedCookies != null) {
            request.cookies = savedCookies;
        }
        return makeRequest(request);
    }

    // convenience methods
    public static Http.Response POST(Object url) {
        return POST(url, APPLICATION_X_WWW_FORM_URLENCODED, "");
    }

    public static Http.Response POST(Http.Request request, Object url) {
        return POST(request, url, APPLICATION_X_WWW_FORM_URLENCODED, "");
    }

    public static Http.Response POST(Object url, String contenttype, String body) {
        return POST(newRequest(), url, contenttype, body);
    }

    public static Http.Response POST(Object url, JsonElement json) {
        String body = new Gson().toJson(json);
        return POST(newRequest(), url, APPLICATION_JSON, body);
    }

    public static Http.Response POST(Http.Request request, Object url, String contenttype, String body) {
        return POST(request, url, contenttype, new ByteArrayInputStream(body.getBytes()));
    }

    public static Http.Response POST(Object url, String contenttype, InputStream body) {
        return POST(newRequest(), url, contenttype, body);
    }

    /**
     * Sends a POST request to the application under tests.
     *
     * @param request The request
     * @param url relative url such as <em>"/products/1234"</em>
     * @param contenttype content-type of the request
     * @param body posted data
     * @return The response
     */
    public static Http.Response POST(Http.Request request, Object url, String contenttype, InputStream body) {
        String path = "";
        String queryString = "";
        String turl = url.toString();
        if (turl.contains("?")) {
            path = turl.substring(0, turl.indexOf("?"));
            queryString = turl.substring(turl.indexOf("?") + 1);
        } else {
            path = turl;
        }
        request.method = "POST";
        request.contentType = contenttype;
        request.url = turl;
        request.path = path;
        request.querystring = queryString;
        request.body = body;
        if (savedCookies != null) {
            request.cookies = savedCookies;
        }
        request.headers.putAll(headers);
        return makeRequest(request);
    }

    /**
     * Sends a POST request to the application under tests as a multipart form.
     * Designed for file upload testing.
     *
     * @param url relative url such as <em>"/products/1234"</em>
     * @param parameters map of parameters to be posted
     * @param files map containing files to be uploaded
     * @return The response
     */
    public static Http.Response POST(Object url, Map<String, String> parameters, Map<String, File> files) {
        return POST(newRequest(), url, parameters, files);
    }

    public static Http.Response POST(Object url, Map<String, String> parameters) {
        return POST(newRequest(), url, parameters, new HashMap<>());
    }

    public static Http.Response POST(Http.Request request, Object url, Map<String, String> parameters, Map<String, File> files) {
        List<Part> parts = new ArrayList<Part>();

        for (String key : parameters.keySet()) {
            final StringPart stringPart = new StringPart(key, parameters.get(key), request.contentType, Charset.forName(request.encoding));
            parts.add(stringPart);
        }

        for (Map.Entry<String, File> entry : files.entrySet()) {
            File file = entry.getValue();
            if (file != null) {
                Part filePart = new FilePart(entry.getKey(), entry.getValue());
                parts.add(filePart);
            }
        }

        MultipartBody requestEntity = null;
        /*
         * ^1 MultipartBody::read is not working (if parts.isEmpty() == true)
         * byte[] array = null;
         **/
        FunctionalTest._ByteArrayOutputStream baos = null;
        try {
            requestEntity = MultipartUtils.newMultipartBody(parts, new FluentCaseInsensitiveStringsMap());
            request.headers.putAll(ImmutableMap.<String, Http.Header>builder()
                    .put("content-type", new Http.Header("content-type", requestEntity.getContentType()))
                    .putAll(headers)
                    .build());
            long contentLength = requestEntity.getContentLength();
            if (contentLength < Integer.MIN_VALUE || contentLength > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(contentLength + " cannot be cast to int without changing its value.");
            }
            // array = new byte[(int) contentLength]; // ^1
            // requestEntity.read(ByteBuffer.wrap(array)); // ^1
            baos = new FunctionalTest._ByteArrayOutputStream((int) contentLength);
            requestEntity.transferTo(0, Channels.newChannel(baos));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // InputStream body = new ByteArrayInputStream(array != null ? array :
        // new byte[0]); // ^1
        InputStream body = new ByteArrayInputStream(baos != null ? baos.getByteArray() : new byte[0]);
        return POST(request, url, MULTIPART_FORM_DATA, body);
    }

    public static Http.Response PUT(Object url, String contenttype, String body) {
        return PUT(newRequest(), url, contenttype, body);
    }

    /**
     * Sends a PUT request to the application under tests.
     *
     * @param request The request
     * @param url relative url such as <em>"/products/1234"</em>
     * @param contenttype content-type of the request
     * @param body data to send
     * @return The response
     */
    public static Http.Response PUT(Http.Request request, Object url, String contenttype, String body) {
        String path = "";
        String queryString = "";
        String turl = url.toString();
        if (turl.contains("?")) {
            path = turl.substring(0, turl.indexOf("?"));
            queryString = turl.substring(turl.indexOf("?") + 1);
        } else {
            path = turl;
        }
        request.method = "PUT";
        request.contentType = contenttype;
        request.url = turl;
        request.path = path;
        request.querystring = queryString;
        request.body = new ByteArrayInputStream(body.getBytes());
        if (savedCookies != null) {
            request.cookies = savedCookies;
        }
        return makeRequest(request);
    }

    public static Http.Response DELETE(String url) {
        return DELETE(newRequest(), url);
    }

    /**
     * Sends a DELETE request to the application under tests.
     *
     * @param request The request
     * @param url relative url eg. <em>"/products/1234"</em>
     * @return The response
     */
    public static Http.Response DELETE(Http.Request request, Object url) {
        String path = "";
        String queryString = "";
        String turl = url.toString();
        if (turl.contains("?")) {
            path = turl.substring(0, turl.indexOf("?"));
            queryString = turl.substring(turl.indexOf("?") + 1);
        } else {
            path = turl;
        }
        request.method = "DELETE";
        request.url = turl;
        request.path = path;
        request.querystring = queryString;
        request.headers.putAll(ImmutableMap.of("accept", new Http.Header("accept", APPLICATION_JSON)));
        if (savedCookies != null) {
            request.cookies = savedCookies;
        }
        request.body = new ByteArrayInputStream(new byte[0]);
        return makeRequest(request);
    }

    public static void makeRequest(final Http.Request request, final Http.Response response) {
        final CountDownLatch actionCompleted = new CountDownLatch(1);
        TestEngine.functionalTestsExecutor.submit(new Invoker.Invocation() {

            @Override
            public void execute() throws Exception {
                renderArgs.clear();
                ActionInvoker.invoke(request, response);

                if (Scope.RenderArgs.current().data != null) {
                    renderArgs.putAll(Scope.RenderArgs.current().data);
                }
            }

            @Override
            public void onSuccess() throws Exception {
                try {
                    super.onSuccess();
                } finally {
                    onActionCompleted();
                }
            }

            @Override
            public void onException(final Throwable e) {
                try {
                    super.onException(e);
                } finally {
                    onActionCompleted();
                }
            }

            private void onActionCompleted() {
                actionCompleted.countDown();
            }

            @Override
            public Invoker.InvocationContext getInvocationContext() {
                ActionInvoker.resolve(request);
                return new Invoker.InvocationContext(Http.invocationType, request.invokedMethod.getAnnotations(),
                        request.invokedMethod.getDeclaringClass().getAnnotations());
            }

        });
        try {
            if (!actionCompleted.await(60 * 10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Request did not complete in time");
            }
            if (savedCookies == null) {
                savedCookies = new HashMap<>();
            }
            for (Map.Entry<String, Http.Cookie> e : response.cookies.entrySet()) {
                // If Max-Age is unset, browsers discard on exit; if
                // 0, they discard immediately.
                if (e.getValue().maxAge == null || e.getValue().maxAge > 0) {
                    savedCookies.put(e.getKey(), e.getValue());
                } else {
                    // cookies with maxAge zero still remove a previously
                    // existing cookie,
                    // like PLAY_FLASH.
                    savedCookies.remove(e.getKey());
                }
            }
            response.out.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Http.Response makeRequest(final Http.Request request) {
        Http.Response response = newResponse();
        makeRequest(request, response);

        if (response.status == 302) { // redirect
            // if Location-header is pressent, fix it to "look like" a functional-test-url
            Http.Header locationHeader = response.headers.get("Location");
            if (locationHeader != null) {
                String locationUrl = locationHeader.value();
                if (locationUrl.startsWith("http://localhost/")) {
                    locationHeader.values.clear();
                    locationHeader.values.add(locationUrl.substring(16));// skip
                    // 'http://localhost'
                }
            }
        }
        RestFunctionalTest.response = response;
        return response;
    }

    public static Http.Response newResponse() {
        Http.Response response = new Http.Response();
        response.out = new ByteArrayOutputStream();
        return response;
    }

    public static Http.Request newRequest() {
        Http.Request request = Http.Request.createRequest(null, "GET", "/", "", null, null, null, null, false, 80, "localhost", false, null, null);
        return request;
    }

    /**
     * Get the HTTP response as a JSON object.
     *
     * @param response The HTTP response
     * @return The parsed JSON response
     */
    public static JsonObject getJsonResult(Http.Response response) {
        return (JsonObject) new JsonParser().parse(getContent(response));
    }

    /**
     * Get the last HTTP response as a JSON object.
     *
     * @return The parsed JSON response
     */
    public static JsonObject getJsonResult() {
        return getJsonResult(response);
    }

    /**
     * Extract an array of items from the response.
     *
     * @return The array of items
     */
    protected static JsonArray getItems() {
        JsonObject json = getJsonResult();
        return json.getAsJsonArray("items");
    }

    /**
     * Extract an item from the response.
     *
     * @return The item
     */
    protected static JsonObject getItem() {
        JsonObject json = getJsonResult();
        return json.getAsJsonObject("item");
    }

    /**
     * Extract the first item from an array of items from the response.
     *
     * @return The item
     */
    protected static JsonObject getFirstItem() {
        JsonArray items = getItems();
        return items.get(0).getAsJsonObject();
    }

    /**
     * Extract the ID from the response.
     *
     * @return The ID
     */
    public static String getId() {
        JsonObject json = getItem();
        return json.get("id").getAsString();
    }

    public static void assertIsOk() {
        assertIsOk(response);
    }

    public static void assertIsOk(Http.Response response) {
        assertStatus(200, response);
    }

    public static void assertIsNotFound(Http.Response response) {
        assertStatus(404, response);
    }

    public static void assertIsNotFound(String string) {
        assertIsNotFound();
        String contents = getContent(response);
        assertTrue("Response content not found '" + string + "'", contents.contains(string));
    }

    public static void assertIsNotFound() {
        assertIsNotFound(response);
    }

    public static void assertIsBadRequest() {
        assertIsBadRequest(response);
    }

    public static void assertIsBadRequest(Http.Response response) {
        assertStatus(Http.StatusCode.BAD_REQUEST, response);
    }

    public static void assertIsBadRequest(String string) {
        assertIsBadRequest();
        String contents = getContent(response);
        assertTrue("Response content not found '" + string + "'", contents.contains(string));
    }

    public static void assertIsForbidden() {
        assertIsForbidden(response);
    }

    public static void assertIsForbidden(Http.Response response) {
        assertStatus(Http.StatusCode.FORBIDDEN, response);
    }

    public static void assertIsFound() {
        assertIsFound(response);
    }

    public static void assertIsFound(Http.Response response) {
        assertStatus(302, response);
    }

    /**
     * Asserts response status code
     *
     * @param status
     *            expected HTTP response code
     * @param response
     *            server response
     */
    public static void assertStatus(int status, Http.Response response) {
        assertEquals("Response status, out was: " + response.out, (Object) status, response.status);
    }

    /**
     * Exact equality assertion on response body
     *
     * @param content
     *            expected body content
     * @param response
     *            server response
     */
    public static void assertContentEquals(String content, Http.Response response) {
        assertEquals(content, getContent(response));
    }

    /**
     * Asserts response body matched a pattern or contains some text.
     *
     * @param pattern
     *            a regular expression pattern or a regular text, ( which must
     *            be escaped using Pattern.quote)
     * @param response
     *            server response
     */
    public static void assertContentMatch(String pattern, Http.Response response) {
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(getContent(response)).find();
        assertTrue("Response content does not match '" + pattern + "'", ok);
    }

    /**
     * Verify response charset encoding, as returned by the server in the
     * Content-Type header. Be aware that if no charset is returned, assertion
     * will fail.
     *
     * @param charset
     *            expected charset encoding such as "utf-8" or "iso8859-1".
     * @param response
     *            server response
     */
    public static void assertCharset(String charset, Http.Response response) {
        int pos = response.contentType.indexOf("charset=") + 8;
        String responseCharset = (pos > 7) ? response.contentType.substring(pos).toLowerCase() : "";
        assertEquals("Response charset", charset.toLowerCase(), responseCharset);
    }

    /**
     * Verify the response content-type
     *
     * @param contentType
     *            expected content-type without any charset extension, such as
     *            "text/html"
     * @param response
     *            server response
     */
    public static void assertContentType(String contentType, Http.Response response) {
        String responseContentType = response.contentType;
        assertNotNull("Response contentType missing", responseContentType);
        assertTrue("Response contentType unmatched : '" + contentType + "' !~ '" + responseContentType + "'",
                responseContentType.startsWith(contentType));
    }

    /**
     * Exact equality assertion on a response header value
     *
     * @param headerName
     *            header to verify. case-insensitive
     * @param value
     *            expected header value
     * @param response
     *            server response
     */
    public static void assertHeaderEquals(String headerName, String value, Http.Response response) {
        assertNotNull("Response header " + headerName + " missing", response.headers.get(headerName));
        assertEquals("Response header " + headerName + " mismatch", value, response.headers.get(headerName).value());
    }

    /**
     * obtains the response body as a string
     *
     * @param response
     *            server response
     * @return The response body as an <em>utf-8 string</em>
     */
    public static String getContent(Http.Response response) {
        byte[] data = response.out.toByteArray();
        try {
            return new String(data, response.encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the response as a string.
     *
     * @return The response
     */
    public String getResponseAsString() {
        return getContent(response);
    }

    /**
     * obtains the response body as a string
     *
     * @param response
     *            server response
     * @return The response body as an <em>utf-8 string</em>
     */
    public static String getDirect(Http.Response response) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ByteStreams.copy((InputStream) response.direct, os);
            return new String(os.toByteArray(), response.encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object renderArgs(String name) {
        return renderArgs.get(name);
    }

    // Utils

    public void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1_000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static RestFunctionalTest.URL reverse() {
        ControllersEnhancer.ControllerInstrumentation.stopActionCall();
        Router.ActionDefinition actionDefinition = new Router.ActionDefinition();
        Controller._currentReverse.set(actionDefinition);
        return new RestFunctionalTest.URL(actionDefinition);
    }

    public static class URL {

        Router.ActionDefinition actionDefinition;

        URL(Router.ActionDefinition actionDefinition) {
            this.actionDefinition = actionDefinition;
        }

        @Override
        public String toString() {
            return actionDefinition.url;
        }

    }

    public static final class _ByteArrayOutputStream extends ByteArrayOutputStream {
        public _ByteArrayOutputStream(int size) {
            super(size);
        }

        public byte[] getByteArray() {
            return this.buf;
        }
    }
}
