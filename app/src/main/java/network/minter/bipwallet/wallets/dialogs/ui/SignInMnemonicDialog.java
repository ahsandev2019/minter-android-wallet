/*
 * Copyright (C) by MinterTeam. 2020
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

package network.minter.bipwallet.wallets.dialogs.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.edwardstock.inputfield.InputField;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;
import network.minter.bipwallet.R;
import network.minter.bipwallet.auth.contract.SignInMnemonicView;
import network.minter.bipwallet.auth.ui.AuthActivity;
import network.minter.bipwallet.home.ui.HomeActivity;
import network.minter.bipwallet.internal.dialogs.BaseBottomSheetDialogFragment;
import network.minter.bipwallet.internal.helpers.KeyboardHelper;
import network.minter.bipwallet.internal.views.list.ViewElevationOnScrollNestedScrollView;
import network.minter.bipwallet.wallets.dialogs.presentation.SingInMnemonicPresenter;

public class SignInMnemonicDialog extends BaseBottomSheetDialogFragment implements SignInMnemonicView {

    @Inject Provider<SingInMnemonicPresenter> presenterProvider;
    @InjectPresenter SingInMnemonicPresenter presenter;

    @BindView(R.id.dialog_title) TextView title;
    @BindView(R.id.submit) Button submit;
    @BindView(R.id.input_seed) InputField inputMnemonic;
    @BindView(R.id.scroll) NestedScrollView scroll;
    @BindView(R.id.dialog_top) View dialogTop;

    private Unbinder mUnbinder;

    @ProvidePresenter
    public SingInMnemonicPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_signin_wallet, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        title.setText(R.string.btn_sign_in);

        scroll.setOnScrollChangeListener(new ViewElevationOnScrollNestedScrollView(dialogTop));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void setEnableSubmit(boolean enable) {
        submit.setEnabled(enable);
    }

    @Override
    public void setOnSubmitClickListener(View.OnClickListener listener) {
        submit.setOnClickListener(listener);
    }

    @Override
    public void addMnemonicInputTextWatcher(TextWatcher textWatcher) {
        inputMnemonic.addTextChangedListener(textWatcher);
    }

    @Override
    public void setError(CharSequence error) {
        inputMnemonic.setError(error);
    }

    @Override
    public void startHome() {
        KeyboardHelper.hideKeyboard(this);
        ((AuthActivity) getActivity()).startActivityClearTop(getActivity(), HomeActivity.class);
        getActivity().finish();
    }
}
