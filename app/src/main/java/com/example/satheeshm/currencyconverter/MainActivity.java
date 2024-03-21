package com.example.satheeshm.currencyconverter;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {

    private  Spinner fromDropDown ;
    private Spinner toDropDown;
    private ArrayAdapter<String> dropDownAdapter;
    private ImageButton interChange;
    private TextView fromSymbol;
    private TextView fromCurrency;
    private EditText fromCurrencyValue;
    private TextView toSymbol;
    private TextView toCurrency;
    private EditText toCurrencyValue;
    private TextView fromCurrencyRate;
    private TextView toCurrencyRate;
    private  GetCurrencies getCurrencies;
    private String dateStr ="";
    private Button byDateButton;
    private Button shareButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         fromDropDown = (Spinner) findViewById(R.id.spinnerFrom);
         toDropDown = (Spinner) findViewById(R.id.spinnerTo);
         interChange = (ImageButton) findViewById(R.id.imageButtonInterchange);
         fromSymbol = (TextView) findViewById(R.id.textViewFromSymbol);
         fromCurrency = (TextView) findViewById(R.id.textViewFromCurrency);
         fromCurrencyValue = (EditText) findViewById(R.id.editTextFromCurrencyValue);
        toSymbol = (TextView) findViewById(R.id.textViewToSymbol);
        toCurrency = (TextView) findViewById(R.id.textViewToCurrency);
        toCurrencyValue = (EditText) findViewById(R.id.editTextToCurrencyValue);
        fromCurrencyRate = (TextView) findViewById(R.id.textViewFromCurrencyRate);
        toCurrencyRate = (TextView) findViewById(R.id.textViewToCurrencyRate);
        byDateButton = (Button) findViewById(R.id.buttonGetOnParticularDate);
        shareButton = (Button) findViewById(R.id.buttonShare);

        try {

            makeText(this,R.string.WELCOME_MSG,Toast.LENGTH_SHORT).show();

                if(InternetConnection.checkConnection(this)){
                    getCurrencies = new GetCurrencies();
                    getCurrencies.execute();
                }else{
                    makeText(this,R.string.CONNECT_TO_INTERNET_AND_RESTART_APP,Toast.LENGTH_LONG).show();
                    fromCurrencyValue.setFocusableInTouchMode(false);
                    byDateButton.setClickable(false);
                    shareButton.setClickable(false);
                }


           fromDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
               {
                   int fromPosition = fromDropDown.getSelectedItemPosition();
                   String fromSymbolStr = getCurrencies.getCurrencySymbol(fromPosition);
                   fromSymbol.setText(fromSymbolStr);
                   String fromCurrencyStr =  getCurrencies.getCurrencyString(fromPosition);
                   fromCurrency.setText(fromCurrencyStr);
                   callCurrencyConverter();
               }
               @Override
               public void onNothingSelected(AdapterView<?> parent) { }
           });

            toDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    int toPosition = toDropDown.getSelectedItemPosition();
                    String toSymbolStr = getCurrencies.getCurrencySymbol(toPosition);
                    toSymbol.setText(toSymbolStr);
                    String toCurrencyStr = getCurrencies.getCurrencyString(toPosition);
                    toCurrency.setText(toCurrencyStr);
                    callCurrencyConverter();

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            fromCurrencyValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    fromCurrencyValue.removeTextChangedListener(this);
                    try {
                        String originalString = s.toString();
                        Long longval;
                        if (originalString.contains(",")) {
                            originalString = originalString.replaceAll(",", "");
                        }
                        longval = Long.parseLong(originalString);
                        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                        formatter.applyPattern("#,###,###,###");
                        String formattedString = formatter.format(longval);

                        //setting text after format to EditText
                        fromCurrencyValue.setText(formattedString);
                       fromCurrencyValue.setSelection(fromCurrencyValue.getText().length());
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    fromCurrencyValue.addTextChangedListener(this);
                    callCurrencyConverter();
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Error"+Thread.currentThread().getStackTrace()[2],paramThrowable.getLocalizedMessage());
            }
        });

    }

    public void onInterchange(View v)
    {
        int fromPosition = fromDropDown.getSelectedItemPosition();
        int toPosition = toDropDown.getSelectedItemPosition();
        toDropDown.setSelection(fromPosition);
        fromDropDown.setSelection(toPosition);
    }

    public void callCurrencyConverter()
    {
        List<String> inputs = getInputFields();
        Converter currencyConverter  = new Converter();
        currencyConverter.execute(inputs.get(0),inputs.get(1),inputs.get(2));
    }

    private List<String> getInputFields()
    {
        List<String> inputs = new ArrayList<>();
        int toPosition = toDropDown.getSelectedItemPosition();
        int fromPosition = fromDropDown.getSelectedItemPosition();

        String fromSymbolStr = getCurrencies.getCurrencySymbol(fromPosition);
        String toSymbolStr = getCurrencies.getCurrencySymbol(toPosition);
        String fromCurrValue = fromCurrencyValue.getText().toString();
        fromCurrValue = fromCurrValue.replaceAll(",", "");

        inputs.add(fromSymbolStr);
        inputs.add(toSymbolStr);
        inputs.add(fromCurrValue);

        return  inputs;
    }

    public void shareResult(View v)
    {
        String fromCurrValue = fromCurrencyValue.getText().toString();
        String toCurrValue = toCurrencyValue.getText().toString();
        if(fromCurrValue.isEmpty() || toCurrValue.isEmpty()){
            makeText(this,R.string.NO_DATA,Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message = constructMessage(fromCurrValue,toCurrValue);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,message);
            shareIntent.setType("text/plain");

            Intent sendIntent = Intent.createChooser(shareIntent,null);
            startActivity(sendIntent);

        }
    }

    private String constructMessage(String fromCurrValue, String toCurrValue)
    {
        int toPosition = toDropDown.getSelectedItemPosition();
        int fromPosition = fromDropDown.getSelectedItemPosition();
        String toSymbolStr = getCurrencies.getCurrencySymbol(toPosition);
        String fromSymbolStr = getCurrencies.getCurrencySymbol(fromPosition);

        String message = fromCurrValue +" "+fromSymbolStr+" = "+toCurrValue+" "+toSymbolStr+"\n"
                +"\n"+fromCurrencyRate.getText().toString()+"\n"
                + "\n"+toCurrencyRate.getText().toString();

        return message;
    }

    public void getByDate(View v)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2009,12,1);
        Calendar currentCalendar = Calendar.getInstance();

        final DatePicker datePicker = new DatePicker(this);
        datePicker.setMinDate(calendar.getTimeInMillis());
        datePicker.setMaxDate(currentCalendar.getTimeInMillis());

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Select Date")
                .setView(datePicker)
                .setPositiveButton("GO",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                           int day = datePicker.getDayOfMonth();
                           int month = datePicker.getMonth()+1;
                           int year = datePicker.getYear();

                          dateStr =  String.valueOf(year) +"-"+String.valueOf(month)+"-"+String.valueOf(day);
                          callCurrencyConverter();
                          makeText(MainActivity.this,getString(R.string.ON_DATE)+dateStr,Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Back to Today", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dateStr="";
                        callCurrencyConverter();
                        makeText(MainActivity.this,R.string.TODAY,Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private class GetCurrencies extends AsyncTask<Void,Void,List<String>>
    {
       private  List<String>  currSymbols = new ArrayList<>();
       private  List<String>  currNames = new ArrayList<>();
       private JSONObject getCurrencies ;
       private JSONObject getCurrencySymbolsVsNames ;
       private String responseString = "";
       private OkHttpClient client = new OkHttpClient();
       private Request request;
       private Response response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!InternetConnection.checkConnection(MainActivity.this)){
                makeText(MainActivity.this,R.string.CONNECT_TO_INTERNET,Toast.LENGTH_LONG).show();
                this.cancel(true);
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try{

                request = new Request.Builder()
                        .url("https://currency-converter5.p.rapidapi.com/currency/list?format=json")
                        .get()
                        .addHeader("x-rapidapi-host", "currency-converter5.p.rapidapi.com")
                        .addHeader("x-rapidapi-key", "12ad43c70bmsh5f37337e80e1355p1b6274jsn6e0bf2c995d3")
                        .build();
                response = client.newCall(request).execute();
                if(response.isSuccessful()) {
                    responseString = response.body().string();
                    getCurrencies = new JSONObject(responseString);
                    getCurrencySymbolsVsNames = getCurrencies.getJSONObject("currencies");
                    Iterator<String> currSymbolsItr = getCurrencySymbolsVsNames.keys();
                    while (currSymbolsItr.hasNext()) {
                        String currSymbol = currSymbolsItr.next();
                        currSymbols.add(currSymbol);
                        currNames.add(getCurrencySymbolsVsNames.getString(currSymbol));
                    }
                }
                else {
                    Log.e("RESP_FAILS","Response Failed");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return currNames;
        }

        @Override
        protected void onPostExecute(List<String> currNames) {
            super.onPostExecute(currNames);
            dropDownAdapter = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item, currNames);
            dropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fromDropDown.setAdapter(dropDownAdapter);
            toDropDown.setAdapter(dropDownAdapter);
            int fromPosition = fromDropDown.getSelectedItemPosition()+1;
            fromDropDown.setSelection(fromPosition);
            int toPosition = toDropDown.getSelectedItemPosition()+2;
            toDropDown.setSelection(toPosition);
        }

        public String getCurrencySymbol(int position)
        {
            return currSymbols.get(position);
        }

        public String getCurrencyString(int position)
        {
            return currNames.get(position);
        }
    }

    private class Converter extends AsyncTask<String,Void,List<String>>
    {
       private OkHttpClient client = new OkHttpClient();
       private Request request;
       private Response response;
       private String responseString = "";
       private JSONObject ratesJSON ;
       private JSONObject toRateJSON;
       private JSONObject toCurrencyRates;
       private String rate = "";
       private String rateForAmount = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!InternetConnection.checkConnection(MainActivity.this)){
                makeText(MainActivity.this,R.string.CONNECT_TO_INTERNET,Toast.LENGTH_LONG).show();
                toCurrencyValue.setText("");
                this.cancel(true);
            }

            int toPosition = toDropDown.getSelectedItemPosition();
            int fromPosition = fromDropDown.getSelectedItemPosition();
            if(fromPosition == toPosition){
                makeText(MainActivity.this,R.string.FROM_AND_TO_CANNOT_BE_EQUAL,Toast.LENGTH_SHORT).show();
                makeText(MainActivity.this,R.string.CHANGE_CURRENCY_IN_FROM_OR_TO,Toast.LENGTH_SHORT).show();
                toCurrencyValue.setText("");
                this.cancel(true);
            }

            if(fromCurrencyValue.getText().toString().isEmpty()){
                makeText(MainActivity.this,R.string.ENTER_FROM_VALUE,Toast.LENGTH_SHORT).show();
                toCurrencyValue.setText("");
                this.cancel(true);
            }
        }

        @Override
        protected List<String> doInBackground(String... inputs) {
            List<String> rates = new ArrayList<>();
           try {
               if (dateStr.isEmpty()) {
                   request = new Request.Builder()
                           .url("https://currency-converter5.p.rapidapi.com/currency/convert?format=json&from=" + inputs[0] + "&to=" + inputs[1] + "&amount=" + inputs[2])
                           .get()
                           .addHeader("x-rapidapi-host", "currency-converter5.p.rapidapi.com")
                           .addHeader("x-rapidapi-key", "12ad43c70bmsh5f37337e80e1355p1b6274jsn6e0bf2c995d3")
                           .build();
                   response = client.newCall(request).execute();
               } else {
                   request = new Request.Builder()
                           .url("https://currency-converter5.p.rapidapi.com/currency/historical/"+dateStr+"?format=json&to=" + inputs[1] + "&from=" + inputs[0] + "&amount=" + inputs[2])
                           .get()
                           .addHeader("x-rapidapi-host", "currency-converter5.p.rapidapi.com")
                           .addHeader("x-rapidapi-key", "12ad43c70bmsh5f37337e80e1355p1b6274jsn6e0bf2c995d3")
                           .build();
                   response = client.newCall(request).execute();
               }

               if (response.isSuccessful()) {
                   responseString = response.body().string();
                   ratesJSON = new JSONObject(responseString);
                   toRateJSON = ratesJSON.getJSONObject("rates");
                   toCurrencyRates = toRateJSON.getJSONObject(inputs[1]);

                   rate = toCurrencyRates.getString("rate");
                   rateForAmount = toCurrencyRates.getString("rate_for_amount");
                   rates.add(rate);
                   rates.add(rateForAmount);
                   rates.add(inputs[0]);
                   rates.add(inputs[1]);
               }
               else {
                   Log.e("RESP_FAILS", "Response Failed");
               }
           }
           catch (Exception e){
               e.printStackTrace();
           }
            return rates;
        }

        @Override
        protected void onPostExecute(List<String> rates) {
            super.onPostExecute(rates);

            Double toCurrVal = Double.valueOf(rates.get(1));
            DecimalFormat currValFormat = new DecimalFormat("###,###.##");
            String toCurrValStr = currValFormat.format(toCurrVal);
            toCurrencyValue.setText(toCurrValStr);

            String fromCurrRateStr = "1 "+rates.get(2)+" = "+rates.get(0)+" "+rates.get(3);
            Double rate = Double.valueOf(rates.get(0));
            rate = 1/rate;
            DecimalFormat decimalFormat = new DecimalFormat("#.####");
            String rateStr = decimalFormat.format(rate);
            String toCurrRateStr = "1 "+rates.get(3)+" = "+rateStr+" "+rates.get(2);
            fromCurrencyRate.setText(fromCurrRateStr);
            toCurrencyRate.setText(toCurrRateStr);
        }
    }
}

