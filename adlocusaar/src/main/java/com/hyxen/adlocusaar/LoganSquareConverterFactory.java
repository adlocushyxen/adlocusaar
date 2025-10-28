package com.hyxen.adlocusaar;

import com.bluelinelabs.logansquare.LoganSquare;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class LoganSquareConverterFactory extends Converter.Factory {

    public static LoganSquareConverterFactory create() {
        return new LoganSquareConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return value -> LoganSquare.parse(value.string(), (Class<?>) type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        return value -> RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=UTF-8"),
                LoganSquare.serialize(value)
        );
    }
}
