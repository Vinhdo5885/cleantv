package com.cleantv.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cleantv.launcher.utils.PrefUtils;

public class LockActivity extends Activity {

    private int triggerClickCount = 0;
    private long lastTriggerTime = 0;
    private Dialog passcodeDialog;
    private final StringBuilder enteredPin = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lock);

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleSecretTrigger();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.setScreenLocked(this, true);
    }

    /**
     * Bắt sự kiện phím Remote: Nhấn phím OK 5 lần liên tiếp để mở bảng nhập mật khẩu
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            handleSecretTrigger();
            return true;
        }

        // Chặn hoàn toàn nút Back để không thể thoát khóa
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleSecretTrigger();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleSecretTrigger() {
        long currentTime = System.currentTimeMillis();
        // Nếu khoảng cách giữa 2 lần bấm quá 2.5 giây thì đếm lại từ đầu
        if (currentTime - lastTriggerTime > 2500) {
            triggerClickCount = 1;
        } else {
            triggerClickCount++;
        }
        lastTriggerTime = currentTime;

        if (triggerClickCount >= 5) {
            triggerClickCount = 0;
            showPasscodeDialog();
        }
    }

    private void showPasscodeDialog() {
        if (passcodeDialog != null && passcodeDialog.isShowing()) {
            return;
        }

        enteredPin.setLength(0);
        passcodeDialog = new Dialog(this);
        passcodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passcodeDialog.setContentView(R.layout.dialog_passcode);
        if (passcodeDialog.getWindow() != null) {
            passcodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final TextView tvDisplay = (TextView) passcodeDialog.findViewById(R.id.tv_pin_display);
        updatePinDisplay(tvDisplay);

        View.OnClickListener numListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredPin.length() < 6) {
                    Button b = (Button) v;
                    enteredPin.append(b.getText().toString());
                    updatePinDisplay(tvDisplay);

                    // Tự động kiểm tra nếu đã nhập đủ 6 số
                    if (enteredPin.length() == 6) {
                        verifyPasscode(tvDisplay);
                    }
                }
            }
        };

        // Gán sự kiện cho các nút số 0-9
        int[] numBtnIds = {
                R.id.btn_num_0, R.id.btn_num_1, R.id.btn_num_2, R.id.btn_num_3,
                R.id.btn_num_4, R.id.btn_num_5, R.id.btn_num_6, R.id.btn_num_7,
                R.id.btn_num_8, R.id.btn_num_9
        };
        for (int id : numBtnIds) {
            Button btn = (Button) passcodeDialog.findViewById(id);
            if (btn != null) btn.setOnClickListener(numListener);
        }

        Button btnClear = (Button) passcodeDialog.findViewById(R.id.btn_num_clear);
        if (btnClear != null) {
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (enteredPin.length() > 0) {
                        enteredPin.deleteCharAt(enteredPin.length() - 1);
                        updatePinDisplay(tvDisplay);
                    }
                }
            });
        }

        Button btnOk = (Button) passcodeDialog.findViewById(R.id.btn_num_ok);
        if (btnOk != null) {
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifyPasscode(tvDisplay);
                }
            });
        }

        Button btnCancel = (Button) passcodeDialog.findViewById(R.id.btn_cancel_dialog);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passcodeDialog.dismiss();
                }
            });
        }

        // Hỗ trợ bấm trực tiếp các phím số từ remote vật lý
        passcodeDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                        int num = keyCode - KeyEvent.KEYCODE_0;
                        if (enteredPin.length() < 6) {
                            enteredPin.append(num);
                            updatePinDisplay(tvDisplay);
                            if (enteredPin.length() == 6) {
                                verifyPasscode(tvDisplay);
                            }
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (enteredPin.length() > 0) {
                            enteredPin.deleteCharAt(enteredPin.length() - 1);
                            updatePinDisplay(tvDisplay);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        passcodeDialog.show();
    }

    private void updatePinDisplay(TextView tvDisplay) {
        if (tvDisplay == null) return;
        if (enteredPin.length() == 0) {
            tvDisplay.setText("------");
        } else {
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < enteredPin.length(); i++) {
                masked.append("● ");
            }
            tvDisplay.setText(masked.toString().trim());
        }
    }

    private void verifyPasscode(TextView tvDisplay) {
        String pin = enteredPin.toString();
        if (PrefUtils.checkDynamicPasscode(pin)) {
            // MỞ KHÓA THÀNH CÔNG!
            PrefUtils.setScreenLocked(LockActivity.this, false);
            // Đặt lại thời gian bắt đầu xem tiếp
            PrefUtils.setLockStartTime(LockActivity.this, System.currentTimeMillis());

            if (passcodeDialog != null && passcodeDialog.isShowing()) {
                passcodeDialog.dismiss();
            }
            Toast.makeText(LockActivity.this, "✓ Mở khóa thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Sai mã PIN
            enteredPin.setLength(0);
            updatePinDisplay(tvDisplay);
            Toast.makeText(LockActivity.this, "✗ Sai mã bảo mật! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Chặn phím Back
    }
}
