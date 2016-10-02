package io.lostinreality.lir_android_app;

/**
 * Created by jose on 01/08/16.
 */

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;


public class MultipartRequest extends JsonRequest<JSONObject> {

    private HttpEntity mHttpEntity;
    private MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    private final Response.Listener<JSONObject> mListener;
    //private final Map<String, String> mKeyValue;
    private String mFileName;
    private FileBody mFileBody;
    private List<FileBody> mFileBodyList;

    public MultipartRequest(String url, String fileName, FileBody fileBody , Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);

        mListener = listener;
        //mKeyValue = params;
        mFileName = fileName;
        mFileBody = fileBody;
        buildMultipartEntity();

    }

    public MultipartRequest(String url, List<FileBody> fblist , Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);

        mListener = listener;
        //mKeyValue = params;
        mFileBodyList = fblist;

        for (FileBody fb : fblist) {
            builder.addPart(fb.getFile().getName(),fb);
        }
        mHttpEntity = builder.build();
    }

    private void buildMultipartEntity() {

//        for (Map.Entry<String, String> entry : mKeyValue.entrySet()) {
//            builder.addTextBody(entry.getKey(), entry.getValue());
//        }
        builder.addPart(mFileName, mFileBody);
        mHttpEntity = builder.build();
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}