/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WalletSendDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = Utils.getDialogBuilder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_send, null);
        final EditText amount = (EditText)view.findViewById(R.id.sendAmount);
        final TextView feeText = (TextView)view.findViewById(R.id.walletSendFee);
        if (MainActivity.theme == MainActivity.Theme.CUSTOM)
            feeText.setTextColor(Color.GRAY);
        if (!SiaMobileApplication.prefs.getBoolean("feesEnabled", false))
            feeText.setVisibility(View.GONE);
        amount.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (amount.getText().toString().equals(""))
                    feeText.setText("0.5% App fee: 0.000");
                else
                    feeText.setText("0.5% App fee: " + new BigDecimal(s.toString()).multiply(Utils.devFee).setScale(3, RoundingMode.FLOOR).toPlainString() + " SC");
            }
            public void afterTextChanged(Editable s) {

            }
        });
        builder.setTitle("Send Siacoins")
                .setView(view)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BigDecimal sendAmount = Wallet.scToHastings(amount.getText().toString());
                        if (SiaMobileApplication.prefs.getBoolean("feesEnabled", false))
                            Wallet.sendSiacoinsWithDevFee(sendAmount,
                                    ((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
                                    new SiaRequest.VolleyCallback() {
                                        public void onSuccess(JSONObject response) {
                                            Utils.successSnackbar(view);
                                        }
                                        public void onError(SiaRequest.Error error) {
                                            error.snackbar(view);
                                        }
                                    });
                        else
                            Wallet.sendSiacoins(sendAmount,
                                    ((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
                                    new SiaRequest.VolleyCallback() {
                                        public void onSuccess(JSONObject response) {
                                            Utils.successSnackbar(view);
                                        }
                                        public void onError(SiaRequest.Error error) {
                                            error.snackbar(view);
                                        }
                                    });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_send, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletSendDialog().show(fragmentManager, "receive dialog");
    }
}
