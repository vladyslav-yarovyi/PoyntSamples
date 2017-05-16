package co.poynt.samples.codesamples;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.poynt.api.model.Discount;
import co.poynt.api.model.Fee;
import co.poynt.api.model.OrderItem;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.os.model.Intents;
import co.poynt.os.model.ReceiptOption;
import co.poynt.os.model.ReceiptType;
import co.poynt.os.services.v2.IPoyntActionButtonListener;
import co.poynt.os.services.v2.IPoyntEmailEntryListener;
import co.poynt.os.services.v2.IPoyntPhoneEntryListener;
import co.poynt.os.services.v2.IPoyntReceiptChoiceListener;
import co.poynt.os.services.v2.IPoyntScanCodeListener;
import co.poynt.os.services.v2.IPoyntSecondScreenService;
import co.poynt.os.services.v2.IPoyntSignatureListener;
import co.poynt.os.services.v2.IPoyntTipListener;

public class SecondScreenServiceV2Activity extends Activity {

    private static final String TAG = SecondScreenServiceV2Activity.class.getSimpleName();
    @Bind(R.id.captureTip)
    Button captureTip;
    @Bind(R.id.showCartConfirmation)
    Button showCartConfirmation;
    @Bind(R.id.captureReceiptChoice)
    Button captureReceiptChoice;
    @Bind(R.id.captureSignature)
    Button captureSignature;
    @Bind(R.id.displayMessage)
    Button displayMessage;
    @Bind(R.id.collectAgreement)
    Button collectAgreement;
    @Bind(R.id.scanCode)
    Button scanCode;

    private IPoyntSecondScreenService secondScreenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen_service_v2);

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SECOND_SCREEN_SERVICE_V2),
                secondScreenServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(secondScreenServiceConnection);
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SecondScreenServiceV2Activity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_second_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection secondScreenServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            secondScreenService = IPoyntSecondScreenService.Stub.asInterface(iBinder);
            Log.d(TAG, "SecondScreenService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            secondScreenService = null;
            Log.d(TAG, "SecondScreenService Disconnected");
        }
    };


    @OnClick(R.id.captureTip)
    public void captureTip() {
        try {
            TransactionAmounts transactionAmounts = new TransactionAmounts();
            transactionAmounts.setOrderAmount(1000l);
            transactionAmounts.setCurrency("USD");
            transactionAmounts.setTipAmount(100l);

            Bundle options = new Bundle();
            /**
             *                 MODE: AMOUNTS or PERCENTS or CUSTOM
             *                 TIP_AMOUNT1,TIP_AMOUNT2, TIP_AMOUNT3 when mode is AMOUNT
             *                 TIP_PERCENT1, TIP_PERCENT2, TIP_PERCENT3 when mode is PERCENTAGE
             *                 TITLE - to set custom title
             *                 SECONDARY_TITLE
             */
            options.putString(Intents.EXTRA_TITLE, "Give Me Tip please!");
            options.putString(Intents.EXTRA_SECONDARY_TITLE, "yey enter more...");
            //options.putString(Intents.EXTRA_MODE, Intents.EXTRA_TIP_MODE_CUSTOM);
            options.putString(Intents.EXTRA_MODE, Intents.EXTRA_TIP_MODE_PERCENTS);
            options.putDouble(Intents.EXTRA_TIP_PERCENT1, 10.0);
            options.putDouble(Intents.EXTRA_TIP_PERCENT2, 20.0);
            options.putDouble(Intents.EXTRA_TIP_PERCENT3, 30.0);
//            options.putString(Intents.EXTRA_MODE, Intents.EXTRA_TIP_MODE_AMOUNTS);
//            options.putLong(Intents.EXTRA_TIP_AMOUNT1, 100);
//            options.putLong(Intents.EXTRA_TIP_AMOUNT2, 200);
//            options.putLong(Intents.EXTRA_TIP_AMOUNT3, 300);
            secondScreenService.captureTip(1000l, "USD", options, new IPoyntTipListener.Stub() {
                @Override
                public void onTipAdded(long l, double v) throws RemoteException {
                    showToast(String.format("Tip Amount: %d Tip Percent:%f", l, v));
                    showConfirmation("You're awesome!");
                }

                @Override
                public void onNoTip() throws RemoteException {
                    showToast("No tip Selected");
                    showConfirmation("miser");
                }

            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.showCartConfirmation)
    public void showCartConfirmation() {
        // create some dummy items to display in second screen
        List<OrderItem> items = new ArrayList<OrderItem>();
        OrderItem item1 = new OrderItem();
        // these are the only required fields for second screen display
        item1.setName("Item1");
        item1.setUnitPrice(100l);
        item1.setQuantity(1.0f);
        items.add(item1);

        OrderItem item2 = new OrderItem();
        // these are the only required fields for second screen display
        item2.setName("Item2");
        item2.setUnitPrice(100l);
        item2.setQuantity(1.0f);

        //item2.setDiscount(-50l);
        List<Discount> itemDiscounts = new ArrayList<>();
        Discount discount = new Discount();
        discount.setAmount(-50l);
        discount.setId(UUID.randomUUID().toString());
        discount.setCustomName("My custom discount");

        itemDiscounts.add(discount);
        item2.setDiscounts(itemDiscounts);
        // IMP: you have to set both discounts array and discount amount
        item2.setDiscount(-50l);

        items.add(item2);

        OrderItem item3 = new OrderItem();
        // these are the only required fields for second screen display
        item3.setName("Item3");
        item3.setUnitPrice(100l);
        item3.setQuantity(2.0f);

        items.add(item3);

        // add discounts
        List<Discount> orderDiscounts = new ArrayList<>();
        Discount discount1 = new Discount();
        discount1.setAmount(-50l);
        discount1.setId(UUID.randomUUID().toString());
        discount1.setCustomName("Order discount1");
        orderDiscounts.add(discount1);
        Discount discount2 = new Discount();
        discount2.setAmount(-50l);
        discount2.setId(UUID.randomUUID().toString());
        discount2.setCustomName("Order discount2");
        orderDiscounts.add(discount2);

        List<Fee> fees = new ArrayList<>();

        Fee fee = new Fee();
        fee.setAmount(25l);
        fee.setName("Recycle fee");
        fees.add(fee);

        try {
            if (secondScreenService != null) {
                BigDecimal total = new BigDecimal(0);
                for (OrderItem item : items) {
                    BigDecimal price = new BigDecimal(item.getUnitPrice());
                    price.setScale(2, RoundingMode.HALF_UP);
                    price = price.multiply(new BigDecimal(item.getQuantity()));
                    total = total.add(price);
                }

                TransactionAmounts transactionAmounts = new TransactionAmounts();
                transactionAmounts.setOrderAmount(total.longValue());
                transactionAmounts.setCurrency("USD");
                transactionAmounts.setTipAmount(100l);

                // options
                Bundle options = new Bundle();
                /**
                 * TITLE, LEFT_BUTTON_TITLE, RIGHT_BUTTON_TITLE
                 */
                options.putString(Intents.EXTRA_TITLE, "Confirm");
                options.putString(Intents.EXTRA_LEFT_BUTTON_TITLE, "NO TIP");
                options.putString(Intents.EXTRA_RIGHT_BUTTON_TITLE, "ADD TIP");
                secondScreenService.showCartConfirmation(items,
                        itemDiscounts,
                        fees,
                        transactionAmounts,
                        true,
                        options,
                        new IPoyntActionButtonListener.Stub() {
                            @Override
                            public void onLeftButtonClicked() throws RemoteException {
                                showConfirmation("why did you decline?");
                            }

                            @Override
                            public void onRightButtonClicked() throws RemoteException {
                                showConfirmation("Thanks for confirming!");
                            }
                        });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.captureSignature)
    public void collectSignature() {
        try {
            TransactionAmounts transactionAmounts = new TransactionAmounts();
            transactionAmounts.setOrderAmount(1000l);
            transactionAmounts.setCurrency("USD");
            transactionAmounts.setTransactionAmount(1100l);
            transactionAmounts.setTipAmount(100l);

            Bundle options = new Bundle();
            options.putString(Intents.EXTRA_TITLE, "autograph please");
            options.putString(Intents.EXTRA_RIGHT_BUTTON_TITLE, "I agree");
            options.putString(Intents.EXTRA_TEXT_UNDER_LINE, "Lorem ipsum dolor sit amet, consectetur adipiscing elit");
            secondScreenService.captureSignature(null, options, new IPoyntSignatureListener.Stub() {
                @Override
                public void onSignatureEntered(Bitmap bitmap) throws RemoteException {
                    showConfirmation("Thanks for the beautiful signature!");
                }

            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.captureReceiptChoice)
    public void collectReceipt() {
        try {
            TransactionAmounts transactionAmounts = new TransactionAmounts();
            transactionAmounts.setOrderAmount(1000l);
            transactionAmounts.setCurrency("USD");
            transactionAmounts.setTransactionAmount(1100l);
            transactionAmounts.setTipAmount(100l);

            /**
             *  TITLE
             *  FOOTER_TEXT
             */
            Bundle options = new Bundle();
            options.putString(Intents.EXTRA_TITLE, "Save trees!");
            options.putString(Intents.EXTRA_FOOTER_TEXT, "*Global warming is real - let's get serious about it!");
            List<ReceiptOption> receiptOptions = new ArrayList<>();
            ReceiptOption option3 = new ReceiptOption();
            option3.setType(ReceiptType.EMAIL);
            option3.setId("EMAIL");
            option3.setData("notthepraveenuknow@gmail.com");
            receiptOptions.add(option3);
            ReceiptOption option4 = new ReceiptOption();
            option4.setType(ReceiptType.SMS);
            option4.setId("TEXT");
            option4.setData("40802183491");
            receiptOptions.add(option4);
            ReceiptOption option2 = new ReceiptOption();
            option2.setType(ReceiptType.NONE);
            option2.setId("NO-PAPER");
            receiptOptions.add(option2);
            ReceiptOption option1 = new ReceiptOption();
            option1.setType(ReceiptType.PAPER);
            option1.setId("PAPER");
            option1.setLabel("Kill a tree");
            receiptOptions.add(option1);
            secondScreenService.captureReceiptChoice(transactionAmounts,
                    receiptOptions, options, new IPoyntReceiptChoiceListener.Stub() {
                        @Override
                        public void onReceiptChoice(ReceiptType receiptType, String id, String data) throws RemoteException {
                            if (receiptType == ReceiptType.EMAIL) {
                                if (data != null) {
                                    showConfirmation(data);
                                } else {
                                    Bundle options = new Bundle();
                                    //options.putString(Intents.EXTRA_EMAIL, "praveen@poynt.co");
                                    options.putString(Intents.EXTRA_LEFT_BUTTON_TITLE, "BACK");
                                    options.putString(Intents.EXTRA_RIGHT_BUTTON_TITLE, "CONTINUE");
                                    secondScreenService.captureEmail(options, new IPoyntEmailEntryListener.Stub() {
                                        @Override
                                        public void onEmailEntered(String s) throws RemoteException {
                                            showConfirmation(s);
                                        }

                                        @Override
                                        public void onCancel() throws RemoteException {
                                            showConfirmation("canceled");
                                        }

                                    });
                                }
                            } else if (receiptType == ReceiptType.SMS) {
                                if (data != null) {
                                    showConfirmation(data);
                                } else {
                                    Bundle options = new Bundle();
                                    //options.putString(Intents.EXTRA_PHONE, "4082183491");
                                    options.putString(Intents.EXTRA_LEFT_BUTTON_TITLE, "BACK");
                                    options.putString(Intents.EXTRA_RIGHT_BUTTON_TITLE, "CONTINUE");
                                    secondScreenService.capturePhone(options, new IPoyntPhoneEntryListener.Stub() {
                                        @Override
                                        public void onPhoneEntered(String s) throws RemoteException {
                                            showConfirmation(s);
                                        }

                                        @Override
                                        public void onCancel() throws RemoteException {
                                            showConfirmation("canceled");
                                        }

                                    });
                                }
                            } else {
                                showConfirmation(receiptType.name());
                            }
                        }
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.displayMessage)
    public void showConfirmation() {
        try {
            // Supported options
            // Intents.EXTRA_BACKGROUND_IMAGE  (value should be a Bitmap object)
            // Intents.EXTRA_CONTENT_TYPE
            Bundle options = new Bundle();
            //secondScreenService.displayMessage("Happy Friday!", options);
//            options.putString(Intents.EXTRA_CONTENT_TYPE, Intents.EXTRA_CONTENT_TYPE_HTML);
//            secondScreenService.displayMessage("<h2>Thank You</h2><p>Good-bye!</p>", options);
            options.putString(Intents.EXTRA_CONTENT_TYPE, Intents.EXTRA_CONTENT_TYPE_URL);
            secondScreenService.displayMessage("https://poynt.com", options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showConfirmation(String message) {
        try {
            Bitmap customBackgroundImage = BitmapFactory.decodeResource(getResources(),
                    R.drawable.thank_you_screen_bg);
            Bundle options = new Bundle();
            options.putParcelable(Intents.EXTRA_BACKGROUND_IMAGE, customBackgroundImage);
            secondScreenService.displayMessage(message, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.collectAgreement)
    public void showCollectAgreement() {
        try {
            Bundle options = new Bundle();
            options.putString(Intents.EXTRA_LEFT_BUTTON_TITLE, "Nope");
            options.putString(Intents.EXTRA_RIGHT_BUTTON_TITLE, "I do");
            secondScreenService.captureAgreement("https://s3.amazonaws.com/poynt-store/terms/poynt_apps_agreement_US.html",
                    options, new IPoyntActionButtonListener.Stub() {
                        @Override
                        public void onLeftButtonClicked() throws RemoteException {
                            showConfirmation("Why not ?");
                        }

                        @Override
                        public void onRightButtonClicked() throws RemoteException {
                            showConfirmation("Yey!");
                        }

                    });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.scanCode)
    public void showScanCode() {
        try {
            Bundle options = new Bundle();
            options.putString(Intents.EXTRA_TITLE, "Scan code please");
            options.putString(Intents.EXTRA_CANCEL_BUTTON_TITLE, "Nah");
            secondScreenService.scanCode(options, new IPoyntScanCodeListener.Stub() {
                @Override
                public void onCodeScanned(String code, String schema) throws RemoteException {
                    showConfirmation(code + "-" + schema);
                }

                @Override
                public void onCodeEntryCanceled() throws RemoteException {
                    showConfirmation("code entry canceled");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
