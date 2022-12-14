package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //Ustawienie zmiennych
    Button turnOn, turnOff, refresh, disconnected, door;
    TextView statusConnection, emptyList, info;
    ListView addressList;
    BluetoothAdapter BTAdapter;
    Intent BTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    int requestCodeForEnable = 1, off = 0;
    Boolean[] lightOn = new Boolean[3];
    Button light1, light2, light3, light4;

    String[] BTDeviceListAddres = new String[50];
    BluetoothDevice deviceConnection;
    BluetoothSocket BTSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 0; i <3; i++) {
            lightOn[i] = false;
        }

        //Przechwycenie przyciskow
        turnOn = findViewById(R.id.turnOn);
        turnOff = findViewById(R.id.turnOff);
        refresh = findViewById(R.id.refresh);
        disconnected = findViewById(R.id.disconnected);
        door = findViewById(R.id.door);

        light1 = findViewById(R.id.light1);
        light2 = findViewById(R.id.light2);
        light3 = findViewById(R.id.light3);
        light4 = findViewById(R.id.light4);

        statusConnection = findViewById(R.id.statusConnection);
        emptyList = findViewById(R.id.emptyList);

        addressList = findViewById(R.id.addressList);
        info = findViewById(R.id.info);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        //Ustawienie widocznosci przy starcie oprogramowania
        if(BTAdapter != null) {
            door.setVisibility(View.INVISIBLE);
            light1.setVisibility(View.INVISIBLE);
            light2.setVisibility(View.INVISIBLE);
            light3.setVisibility(View.INVISIBLE);
            light4.setVisibility(View.INVISIBLE);
            //Sprawdzenie czy BT jest w????czony czy wy????czony i ustawienie konkretnego widoku
            if(BTAdapter.isEnabled()) {
                turnOn.setVisibility(View.INVISIBLE);
                disconnected.setVisibility(View.INVISIBLE);
            } else {
                turnOff.setVisibility(View.INVISIBLE);
                addressList.setVisibility(View.INVISIBLE);
                refresh.setVisibility(View.INVISIBLE);
                disconnected.setVisibility(View.INVISIBLE);
                emptyList.setVisibility(View.INVISIBLE);
            }
        } else {
            //Je??eli s?? jakie?? problemy i telefon nie jest w stanie oddtworzy?? oprogramowania
            turnOn.setVisibility(View.INVISIBLE);
            turnOff.setVisibility(View.INVISIBLE);
            addressList.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
            emptyList.setVisibility(View.INVISIBLE);
            disconnected.setVisibility(View.INVISIBLE);
            door.setVisibility(View.INVISIBLE);
            light1.setVisibility(View.INVISIBLE);
            light2.setVisibility(View.INVISIBLE);
            light3.setVisibility(View.INVISIBLE);
            light4.setVisibility(View.INVISIBLE);

            statusConnection.setText("On this device this app can`t work!");
        }

        //Ustawienie przycisku wlacz BT
        turnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BTAdapter == null) {
                    toast("Bluetooth is not avaible on this device!");
                } else {
                    if(!BTAdapter.isEnabled()) {
                        //Wlaczenie BT
                        startActivityForResult(BTIntent, requestCodeForEnable);

                        //Wypisanie listy adresow
                        showList();

                        //Je??eli lista by by??a pusta
                        if(BTDeviceListAddres[0] != "") {
                            emptyList.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        toast("Something wrong, please reset app!");
                    }
                }
            }
        });

        //Ustawienie przycisku wylacz BT
        turnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BTAdapter.isEnabled()) {
                    //Ustawienie widocznosni po wylaczeniu
                    turnOn.setVisibility(View.VISIBLE);
                    turnOff.setVisibility(View.INVISIBLE);
                    addressList.setVisibility(View.INVISIBLE);
                    refresh.setVisibility(View.INVISIBLE);
                    emptyList.setVisibility(View.INVISIBLE);
                    disconnected.setVisibility(View.INVISIBLE);
                    info.setVisibility(View.INVISIBLE);
                    door.setVisibility(View.INVISIBLE);
                    light1.setVisibility(View.INVISIBLE);
                    light2.setVisibility(View.INVISIBLE);
                    light3.setVisibility(View.INVISIBLE);
                    light4.setVisibility(View.INVISIBLE);

                    //Ustawienie tekstu
                    statusConnection.setText("Disconnected");

                    //Wylaczenie BT oraz wy????czenie aktualizacji danych je??eli by??y aktualizowane
                    BTAdapter.disable();
                    off = 1;
                } else {
                    toast("Something wrong, please reset app!");
                }
            }
        });

        //Odswierzenie listy
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList();
            }
        });

        //Wybieranie urzadzenia do parowania
        addressList.setClickable(true);
        addressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //????czenie poprzez BT
                UUID deviceAddres = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                deviceConnection = BTAdapter.getRemoteDevice(BTDeviceListAddres[i]);
                int counter = 0;

                do {
                    try {
                        BTSocket = deviceConnection.createRfcommSocketToServiceRecord(deviceAddres);
                        System.out.println("Socket: " + BTSocket);
                        BTSocket.connect();
                        System.out.println("Connection: " + BTSocket.isConnected());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    counter++;
                } while(counter < 3);

                if(BTSocket.isConnected()) {
                    UpdateData updateData = new UpdateData();
                    updateData.execute();
                } else {
                    toast("You can`t connect with this device!");
                }
            }
        });

        //Roz????czenie z urzadzeniem
        disconnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ustawienie tekstu
                statusConnection.setText("Disconnected");
                addressList.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);
                disconnected.setVisibility(View.INVISIBLE);
                info.setVisibility(View.INVISIBLE);
                door.setVisibility(View.INVISIBLE);
                light1.setVisibility(View.INVISIBLE);
                light2.setVisibility(View.INVISIBLE);
                light3.setVisibility(View.INVISIBLE);
                light4.setVisibility(View.INVISIBLE);

                //Roz????czenie z urz??dzeniem oraz wy????czenie pobierania danych z p??ytki
                try {
                    BTSocket.close();
                    off = 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //W????czanie i wy????czeni ??wiate??
        light1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight(1);
            }
        });

        light2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight(2);
            }
        });

        light3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight(3);
            }
        });

        light4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight(4);
                toast("Nied??ugo zostan?? zgaszone ??wiat??a w ca??ym domu!");
            }
        });

        //otwieranie drzwi
        door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight(5);
            }
        });
    }

    //Skrocenie komendy wypisywania wiadomosci
    void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    //wysy??anie wiadomo??ci do arduino o zapaleniu/zgaszeniu ??wiate??
    void switchLight(int light) {
        String message = "1";

        switch (light) {
            case 1:
                if(lightOn[0] == true) {
                    message = "10";
                } else {
                    message = "1";
                }
                break;
            case 2:
                if(lightOn[1] == true) {
                    message = "20";
                } else {
                    message = "2";
                }
                break;
            case 3:
                if(lightOn[2] == true) {
                    message = "30";
                } else {
                    message = "3";
                }
                break;
            case 5:
                message = "90";
                break;
            case 4:
                message = "0";
                break;
        }

        byte[] stringAsBytes = (message + " ").getBytes();
        stringAsBytes[stringAsBytes.length - 1] = 0;

        try{
            OutputStream outputStream = BTSocket.getOutputStream();
            outputStream.write(stringAsBytes);
            if(message == "90") {
                toast("Nied??ugo drzwi otworz?? si?? na 3 sekundy");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Ustawienie zdarzen przy wlaczaniu BT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == requestCodeForEnable) {
            if(resultCode == RESULT_OK) {
                toast("Bluetooth is On!");

                //Ustawienie widocznosni po wlaczeniu BT
                turnOn.setVisibility(View.INVISIBLE);
                turnOff.setVisibility(View.VISIBLE);
                addressList.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);
            } else if (resultCode == RESULT_CANCELED) {

                //Powiadmonienie przy anulowaniu w????czenia BT
                toast("Bluetooth is Canceled!");
            }
        }
    }

    //Pokazue list?? u????dze?? z kturymi mo??na si?? po????czy??
    void showList () {
        Set<BluetoothDevice> BTList = BTAdapter.getBondedDevices();
        String[] BTDeviceListName = new String[BTList.size()];
        int index = 0;

        if(BTList.size() > 0) {
            emptyList.setVisibility(View.INVISIBLE);

            for(BluetoothDevice device: BTList) {
                BTDeviceListName[index] = device.getName();
                BTDeviceListAddres[index] = device.getAddress();
                index++;
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, BTDeviceListName);
            addressList.setAdapter(arrayAdapter);
        } else {
            emptyList.setVisibility(View.VISIBLE);
        }
    }

    //Program dzia??a asynchronicznie i jest tutaj aktualizowanie danych z p??ytki co 3 sec. Po wy????czeniu Bluetootha lub roz????czneiu z urz??dzeniem pobieranie danych si?? wy????cza
    private class UpdateData extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Ustawienie tekstu
            statusConnection.setText("Connected");

            //Ustawienie widocznosni po sparowaniu
            addressList.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
            disconnected.setVisibility(View.VISIBLE);
            info.setVisibility(View.VISIBLE);
            door.setVisibility(View.VISIBLE);
            light1.setVisibility(View.VISIBLE);
            light2.setVisibility(View.VISIBLE);
            light3.setVisibility(View.VISIBLE);
            light4.setVisibility(View.VISIBLE);

            light1.setText("Pokoj 1: Wy????czone");
            light2.setText("Pokoj 2: Wy????czone");
            light3.setText("Pokoj 3: Wy????czone");

            off = 0;
        }

        //Wypisiwanie prawid??owo danych oraz ustalanie czy ??wiat???? s?? zapalone
        @Override
        protected Void doInBackground(Void... voids) {
            InputStream inputStream = null;

            try{

                inputStream = BTSocket.getInputStream();
                inputStream.skip(inputStream.available());
                String[] allValues = new String[6];
                String line = "";
                String checkLight = "";

                while(true){
                    while(true) {
                        byte b = (byte) inputStream.read();

                        if(b > 31) {
                            line += (char) b;
                        } else if( b == 10){
                            if (line.contains("Wil=")) {
                                allValues[0] = "\uD83C\uDF31" + line + " %";
                            } else if (line.contains("Temp")) {
                                if (line.length() <  7 || line.contains("-") || line.charAt(6) == '1'){
                                    allValues[1] = "??????" + line + " ??C\n";
                                }
                                else {
                                    allValues[1] = "\uD83D\uDD25" + line + " ??C\n";
                                }
                            } else if (line.contains("gaz =")) {
                                allValues[2] = "\uD83D\uDCA8 " + line + " ppm";
                                String value = line.substring(line.length() - 2);
                                Integer intValue = 0;

                                try {
                                    intValue = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                if(intValue != 0) {
                                    if(intValue >= 60) {
                                        allValues[3] = "\uD83D\uDCAD Dym gaz: Wykryto" + "\n";
                                    } else {
                                        allValues[3] = "\uD83D\uDCAD Dym gaz: OK" + "\n";
                                    }
                                } else {
                                    allValues[3] = "\uD83D\uDCAD Dym gaz: OK" + "\n";
                                }
                            } else if (line.contains("wartosc podlania = ")) {
                                allValues[4] = "\uD83D\uDCA7 " + line + " R";
                            } else if (line.contains("Podlej rosliny") || line.contains("Rosliny podlane")) {
                                if(line.contains("Podlej rosliny")) {
                                    allValues[5] = "\uD83E\uDD40 " + line + "\n";
                                } else {
                                    allValues[5] = "\uD83C\uDF39  " + line + "\n";
                                }
                            } else if (line.contains("pokoj 1")) {
                                checkLight = String.valueOf(light1.getText());

                                if(line.contains("wlaczone")) {
                                    light1.setText("Pok??j 1: W????czone");
                                    lightOn[0] = true;
                                } else {
                                    light1.setText("Pok??j 1: Wy????czone ");
                                    lightOn[0] = false;
                                }
                            } else if (line.contains("pokoj 2")) {

                                if(line.contains("wlaczone")) {
                                    light2.setText("Pok??j 2: W????czone");
                                    lightOn[1] = true;
                                } else {
                                    light2.setText("Pok??j 2: Wy????czone");
                                    lightOn[1] = false;
                                }
                            } else if (line.contains("pokoj 3")) {
                                checkLight = String.valueOf(light3.getText());

                                if(line.contains("wlaczone")) {
                                    light3.setText("Pok??j 3: W????czone");
                                    lightOn[2] = true;
                                } else {
                                    light3.setText("Pok??j 3: Wy????czone");
                                    lightOn[2] = false;
                                }
                            }
                            break;
                        }
                    }

                    line = "";

                    for(int i = 0; i < 6; i++) {
                        line += allValues[i] + "\n";
                    }

                    publishProgress(line);

                    line = "";
                }

            } catch (IOException e) {
                addressList.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);
                disconnected.setVisibility(View.INVISIBLE);
                info.setVisibility(View.INVISIBLE);
                door.setVisibility(View.INVISIBLE);
                light1.setVisibility(View.INVISIBLE);
                light2.setVisibility(View.INVISIBLE);
                light3.setVisibility(View.INVISIBLE);
                light4.setVisibility(View.INVISIBLE);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            info.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            addressList.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);

            light1.setVisibility(View.INVISIBLE);
            light2.setVisibility(View.INVISIBLE);
            light3.setVisibility(View.INVISIBLE);
            light4.setVisibility(View.INVISIBLE);

            disconnected.setVisibility(View.INVISIBLE);
            info.setVisibility(View.INVISIBLE);
            info.setText("");
        }
    }
}