package com.appunite.likefollowsubscribe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;

public class ResponseOrError<T> {
    @Nullable
    private final T data;
    @Nullable
    private final Throwable error;

    private ResponseOrError(@Nullable T data, @Nullable Throwable error) {
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseOrError<T> fromError(@Nonnull Throwable t) {
        return new ResponseOrError(null, t);
    }

    public static <T> ResponseOrError<T> fromData(@Nonnull T data) {
        return new ResponseOrError(data, null);
    }

    @Nonnull
    public static <T> Observable.Transformer<T, ResponseOrError<T>> toResponseOrErrorObservable() {
        return ResponseOrError::toResponseOrErrorObservable;
    }

    @Nonnull
    private static <T> Observable<ResponseOrError<T>> toResponseOrErrorObservable(@Nonnull Observable<T> observable) {
        return observable
                .map(ResponseOrError::fromData)
                .onErrorResumeNext(throwable -> Observable.just(ResponseOrError.fromError(throwable)));
    }

    public boolean isData() {
        return this.data != null;
    }

    public boolean isError() {
        return !this.isData();
    }
}