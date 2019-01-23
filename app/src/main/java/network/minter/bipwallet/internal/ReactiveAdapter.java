/*
 * Copyright (C) by MinterTeam. 2019
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package network.minter.bipwallet.internal;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import network.minter.bipwallet.apis.dummies.BCExplorerResultErrorMapped;
import network.minter.bipwallet.apis.dummies.BCResultErrorMapped;
import network.minter.bipwallet.apis.dummies.ExpResultErrorMapped;
import network.minter.bipwallet.apis.dummies.ProfileResultErrorMapped;
import network.minter.blockchain.MinterBlockChainApi;
import network.minter.blockchain.models.BCResult;
import network.minter.explorer.MinterExplorerApi;
import network.minter.explorer.models.BCExplorerResult;
import network.minter.explorer.models.ExpResult;
import network.minter.profile.MinterProfileApi;
import network.minter.profile.models.ProfileResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class ReactiveAdapter {

    // MyMinter
    public static <T> Observable<T> rxCallProfile(Call<T> call) {
        return Observable.create(emitter -> call.clone().enqueue(new Callback<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(@NonNull Call<T> call1, @NonNull Response<T> response) {
                if (response.body() == null) {
                    emitter.onNext((T) createProfileErrorResult(response));
                } else {
                    emitter.onNext(response.body());
                }

                emitter.onComplete();
            }

            @Override
            public void onFailure(@NonNull Call<T> call1, @NonNull Throwable t) {
                emitter.onError(t);
            }
        }));
    }

    public static <T> Function<? super Throwable, ? extends ObservableSource<? extends ProfileResult<T>>> convertToProfileErrorResult() {
        return (Function<Throwable, ObservableSource<? extends ProfileResult<T>>>) throwable
                -> {

            final ProfileResultErrorMapped<T> errResult = new ProfileResultErrorMapped<>();
            if (errResult.mapError(throwable)) {
                return Observable.just(errResult);
            }

            if (throwable instanceof HttpException) {
                return Observable.just(createProfileErrorResult(((HttpException) throwable)));
            }

            return Observable.error(throwable);
        };
    }

    public static <T> ProfileResult<T> createProfileErrorResult(final String json, int code, String message) {
        Gson gson = MinterProfileApi.getInstance().getGsonBuilder().create();

        ProfileResult<T> out;
        try {
            if (json == null || json.isEmpty()) {
                out = createProfileEmpty(code, message);
            } else {
                out = gson.fromJson(json, new TypeToken<ProfileResult<T>>() {
                }.getType());
            }

        } catch (Exception e) {
            Timber.e(e, "Unable to parse profile error: %s", json);
            out = createProfileEmpty(code, message);
        }

        return out;
    }

    public static <T> ProfileResult<T> createProfileErrorResult(final Response<T> response) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = response.errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createProfileEmpty(response.code(), response.message());
        }

        return createProfileErrorResult(errorBodyString, response.code(), response.message());
    }

    public static <T> ProfileResult<T> createProfileErrorResult(final HttpException exception) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = ((HttpException) exception).response().errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createProfileEmpty(exception.code(), exception.message());
        }

        return createProfileErrorResult(errorBodyString, exception.code(), exception.message());
    }

    public static <T> ProfileResult<T> createProfileEmpty(int code, String message) {
        ProfileResult<T> out = new ProfileResult<>();
        out.error = new ProfileResult.Error();
        out.error.code = String.valueOf(code);
        out.error.message = String.format("%d %s", code, message);
        return out;
    }


    // Blockhain

    public static <T> Observable<T> rxCallBc(Call<T> call) {
        return Observable.create(emitter -> call.clone().enqueue(new Callback<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(@NonNull Call<T> call1, @NonNull Response<T> response) {
                if (response.body() == null) {
                    emitter.onNext((T) createBcErrorResult(response));
                } else {
                    emitter.onNext(response.body());
                }

                emitter.onComplete();
            }

            @Override
            public void onFailure(@NonNull Call<T> call1, @NonNull Throwable t) {
                emitter.onError(t);
            }
        }));
    }

    public static <T> Function<? super Throwable, ? extends ObservableSource<? extends BCResult<T>>> convertToBcErrorResult() {
        return (Function<Throwable, ObservableSource<? extends BCResult<T>>>) throwable
                -> {

            final BCResultErrorMapped<T> errResult = new BCResultErrorMapped<>();
            if (errResult.mapError(throwable)) {
                return Observable.just(errResult);
            }

            if (throwable instanceof HttpException) {
                return Observable.just(createBcErrorResult(((HttpException) throwable)));
            }

            return Observable.error(throwable);
        };
    }

    public static <T> BCResult<T> createBcErrorResultMessage(final String errorMessage, BCResult.ResultCode code, int statusCode) {
        BCResult<T> errorRes = new BCResult<>();
        errorRes.error = new BCResult.ErrorResult();
        errorRes.error.data = errorMessage;
        errorRes.error.code = code.getValue();
        errorRes.statusCode = statusCode;
        return errorRes;
    }

    public static <T> BCResult<T> createBcErrorResult(final String json, int code, String message) {
        Gson gson = MinterBlockChainApi.getInstance().getGsonBuilder().create();

        BCResult<T> out;
        try {
            if (json == null || json.isEmpty()) {
                out = createBcEmpty(code, message);
            } else {
                out = gson.fromJson(json, new TypeToken<BCResult<T>>() {
                }.getType());
            }

        } catch (Exception e) {
            Timber.e(e, "Unable to parse blockchain error: %s", json);
            out = createBcEmpty(code, message);
        }

        return out;
    }

    public static <T> BCResult<T> createBcErrorResult(final Response<T> response) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = response.errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createBcEmpty(response.code(), response.message());
        }

        final BCResult<T> out = createBcErrorResult(errorBodyString, response.code(), response.message());
        out.statusCode = response.code();
        return out;
    }

    public static <T> BCResult<T> createBcErrorResult(final HttpException exception) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = ((HttpException) exception).response().errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createBcEmpty(exception.code(), exception.message());
        }

        return createBcErrorResult(errorBodyString, exception.code(), exception.message());
    }

    public static <T> BCResult<T> createBcEmpty(int code, String message) {
        BCResult<T> out = new BCResult<>();
        out.statusCode = code;
        out.error = new BCResult.ErrorResult();
        out.error.message = String.format("%d %s", code, message);
        return out;
    }


    // Explorer

    // -- blockchain proxy - methods those used BCExplorerResult

    public static <T> Observable<T> rxCallBcExp(Call<T> call) {
        return Observable.create(emitter -> call.clone().enqueue(new Callback<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(@NonNull Call<T> call1, @NonNull Response<T> response) {
                if (response.body() == null) {
                    emitter.onNext((T) createBcExpErrorResult(response));
                } else {
                    emitter.onNext(response.body());
                }

                emitter.onComplete();
            }

            @Override
            public void onFailure(@NonNull Call<T> call1, @NonNull Throwable t) {
                emitter.onError(t);
            }
        }));
    }

    public static <T> Function<? super Throwable, ? extends ObservableSource<? extends BCExplorerResult<T>>> convertToBcExpErrorResult() {
        return (Function<Throwable, ObservableSource<? extends BCExplorerResult<T>>>) throwable
                -> {
            final BCExplorerResultErrorMapped<T> errResult = new BCExplorerResultErrorMapped<>();
            if (errResult.mapError(throwable)) {
                return Observable.just(errResult);
            }

            if (throwable instanceof HttpException) {
                return Observable.just(createBcExpErrorResult(((HttpException) throwable)));
            }

            return Observable.error(throwable);
        };
    }

    public static <T> BCExplorerResult<T> createBcExpErrorResultMessage(final String errorMessage, int code, int statusCode) {
        BCExplorerResult<T> errorRes = new BCExplorerResult<>();
        errorRes.error = new BCExplorerResult.ErrorResult();
        errorRes.error.message = errorMessage;
        errorRes.statusCode = statusCode;
        errorRes.error.code = code;
        return errorRes;
    }

    public static <T> BCExplorerResult<T> createBcExpErrorResult(final String json, int code, String message) {
        Gson gson = MinterBlockChainApi.getInstance().getGsonBuilder().create();

        BCExplorerResult<T> out;
        try {
            if (json == null || json.isEmpty()) {
                out = createBcExpEmpty(code, message);
            } else {
                out = gson.fromJson(json, new TypeToken<BCExplorerResult<T>>() {
                }.getType());
            }
        } catch (Exception e) {
            Timber.w(e, "Unable to parse explorer (blockchain) error: %s", json);
            out = createBcExpEmpty(code, message);
        }

        return out;
    }

    public static <T> BCExplorerResult<T> createBcExpErrorResult(final Response<T> response) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = response.errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createBcExpEmpty(response.code(), response.message());
        }

        final BCExplorerResult<T> out = createBcExpErrorResult(errorBodyString, response.code(), response.message());
        out.statusCode = response.code();
        return out;
    }

    public static <T> BCExplorerResult<T> createBcExpErrorResult(final HttpException exception) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = ((HttpException) exception).response().errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createBcExpEmpty(exception.code(), exception.message());
        }

        return createBcExpErrorResult(errorBodyString, exception.code(), exception.message());
    }

    public static <T> BCExplorerResult<T> createBcExpEmpty(int code, String message) {
        BCExplorerResult<T> out = new BCExplorerResult<>();
        out.error = new BCExplorerResult.ErrorResult();
        out.error.code = BCResult.ResultCode.UnknownError.getValue();
        out.error.message = String.format("%d (%s) %s", code, out.getErrorCode().name(), message);
        return out;
    }

    public static <T> BCExplorerResult<T> createBcExpEmpty() {
        BCExplorerResult<T> out = new BCExplorerResult<>();
        out.error = new BCExplorerResult.ErrorResult();
        return out;
    }

    // normal explorer methods

    public static <T> Observable<T> rxCallExp(Call<T> call) {
        return Observable.create(emitter -> call.clone().enqueue(new Callback<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(@NonNull Call<T> call1, @NonNull Response<T> response) {
                if (response.body() == null) {
                    emitter.onNext((T) createExpErrorResult(response));
                } else {
                    emitter.onNext(response.body());
                }

                emitter.onComplete();
            }

            @Override
            public void onFailure(@NonNull Call<T> call1, @NonNull Throwable t) {
                emitter.onError(t);
            }
        }));
    }

    public static <T> Function<? super Throwable, ? extends ObservableSource<? extends ExpResult<T>>> convertToExpErrorResult() {
        return (Function<Throwable, ObservableSource<? extends ExpResult<T>>>) throwable
                -> {

            if (throwable instanceof HttpException) {
                return Observable.just(createExpErrorResult(((HttpException) throwable)));
            }

            ExpResultErrorMapped<T> errResult = new ExpResultErrorMapped<>();
            if (errResult.mapError(throwable)) {
                return Observable.just(errResult);
            }

            return Observable.error(throwable);
        };
    }

    public static <T> ExpResult<T> createExpErrorResult(final String json, int code, String message) {
        Gson gson = MinterExplorerApi.getInstance().getGsonBuilder().create();
        ExpResult<T> out;
        try {
            if (json == null || json.isEmpty()) {
                out = createExpEmpty(code, message);
            } else {
                out = gson.fromJson(json, new TypeToken<ExpResult<T>>() {
                }.getType());
            }
        } catch (Exception e) {
            out = createExpEmpty(code, message);
        }

        return out;
    }

    public static <T> ExpResult<T> createExpErrorResult(final Response<T> response) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = response.errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createExpEmpty(response.code(), response.message());
        }

        return createExpErrorResult(errorBodyString, response.code(), response.message());
    }

    public static <T> ExpResult<T> createExpErrorResult(final HttpException exception) {
        final String errorBodyString;
        try {
            // нельзя после этой строки пытаться вытащить body из ошибки,
            // потому что retrofit по какой-то причине не хранит у себя это значение
            // а держит в буффере до момента первого доступа
            errorBodyString = ((HttpException) exception).response().errorBody().string();
        } catch (IOException e) {
            Timber.e(e, "Unable to resolve http exception response");
            return createExpEmpty(exception.code(), exception.message());
        }

        return createExpErrorResult(errorBodyString, exception.code(), exception.message());
    }

    public static <T> ExpResult<T> createExpEmpty(int code, String message) {
        ExpResult<T> out = new ExpResult<>();
        out.code = code;
        out.error = message;
        return out;
    }
}
